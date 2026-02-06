# Dashboard Application - Tomcat Deployment Guide

## Overview
The Dashboard application is a Spring Boot 4.0.2 application built with Java 17 that can be deployed on Apache Tomcat servers. The application monitors TIBCO EMS servers and displays real-time queue information.

## Prerequisites
- **Java**: JDK 17 or higher
- **Apache Tomcat**: 10.0.x or higher (compatible with Servlet 5.0+)
- **Memory**: Minimum 1GB heap, recommended 2GB
- **Network**: Access to configured TIBCO EMS servers
- **User**: User with permissions to write to Tomcat directories

## Build Instructions

### Building the WAR File
```bash
cd dashboard
./mvnw.cmd clean package -DskipTests
```

This generates: `target/dashboard-0.0.1-SNAPSHOT.war`

**Size**: ~39.5 MB (includes all dependencies)

## Deployment Methods

### Method 1: Direct WAR Deployment (Recommended)

**Step 1:** Stop Tomcat
```bash
# Windows
$CATALINA_HOME/bin/shutdown.bat

# Linux/Mac
$CATALINA_HOME/bin/shutdown.sh
```

**Step 2:** Copy WAR file
```bash
# Windows
copy target\dashboard-0.0.1-SNAPSHOT.war %CATALINA_HOME%\webapps\dashboard.war

# Linux/Mac
cp target/dashboard-0.0.1-SNAPSHOT.war $CATALINA_HOME/webapps/dashboard.war
```

**Step 3:** Start Tomcat
```bash
# Windows
%CATALINA_HOME%\bin\startup.bat

# Linux/Mac
$CATALINA_HOME/bin/startup.sh
```

**Step 4:** Access the application
```
http://localhost:8080/dashboard
```

### Method 2: Tomcat Manager Deployment

1. Open Tomcat Manager: `http://localhost:8080/manager`
2. Scroll to "Deploy" section
3. Select the WAR file: `target/dashboard-0.0.1-SNAPSHOT.war`
4. Enter context path: `/dashboard`
5. Click "Deploy"

## Configuration

### Application Settings
Edit `application.yml` before building the WAR:

```yaml
spring:
  application:
    name: dashboard
  boot:
    context:
      properties:
        ignore-unbound-configuration-properties: true

tibco:
  ems:
    servers:
      - name: SERVER_NAME
        host: server.host.com
        port: 7222
        username: admin
        password: password
        ssl-enabled: false
```

### Logging Configuration
Logging is configured in `logback-spring.xml`:
- **Development**: Detailed console output
- **Production**: File-based logging with rotation
- **Log Location**: `logs/spring.log` (configurable via `LOG_FILE` environment variable)

### Environment Variables
```bash
# Set Tomcat CATALINA_OPTS
set CATALINA_OPTS=-Xmx2g -Xms1g -Dspring.profiles.active=prod

# Or in setenv.sh (Linux/Mac)
export CATALINA_OPTS="-Xmx2g -Xms1g -Dspring.profiles.active=prod"
```

## Production Setup

### 1. Configure JVM Settings
Edit `$CATALINA_HOME/bin/setenv.bat` (Windows) or `setenv.sh` (Linux/Mac):

```bash
set CATALINA_OPTS=-Xmx2gb -Xms1gb -XX:+UseG1GC -XX:MaxGCPauseMillis=200
set "CATALINA_OPTS=%CATALINA_OPTS% -Dspring.profiles.active=prod"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dlogging.file.name=logs/dashboard.log"
```

### 2. Configure Log Rotation
Logs are automatically rotated:
- Maximum file size: 10 MB
- Maximum history: 30 days
- Total cap: 1 GB

### 3. Security Configuration
- HTTPS/SSL: Configure in `server.xml`
- Authentication: Current implementation handles in SecurityConfig.java
- Network: Restrict Tomcat port access via firewall

### 4. Monitoring
Monitor these key metrics:
- **Heap Usage**: Alert if >85% utilized
- **Thread Count**: Alert if > CPU cores × 4
- **Disk Space**: Ensure sufficient space for logs
- **TIBCO Connectivity**: Check `/api/status` endpoint

## API Endpoints

### Health Check
```bash
GET /api/status
Response: {"tibcoLibAvailable": true, "serverCount": 50, "classPath": "..."}
```

### Get Configured Servers
```bash
GET /api/configured-servers
Response: [{"name": "TST2-ESB", "status": "UNKNOWN"}, ...]
```

### Get Queues (High Volume)
```bash
GET /api/queues
Response: [{"serverName": "...", "queueName": "...", "messageCount": 0}, ...]
```

### Dashboard
```bash
GET /dashboard
Response: HTML with interactive dashboard
```

### Home Page
```bash
GET /
Response: HTML landing page with navigation
```

## Troubleshooting

### Application Won't Start
**Symptom**: WAR deploys but application doesn't respond

**Solutions**:
1. Check `$CATALINA_HOME/logs/catalina.out` for errors
2. Verify Java version: `java -version` (must be 17+)
3. Increase heap size: `-Xmx2g -Xms1g`
4. Check port availability: `netstat -an | grep 8080`

### TIBCO Connection Timeout
**Symptom**: Dashboard loads but shows "UNKNOWN" status for all servers

**Solutions**:
1. Verify TIBCO server addresses in `application.yml`
2. Check network connectivity to TIBCO servers
3. Verify firewall allows port 7222 (default TIBCO port)
4. Check TIBCO server authentication credentials

### Out of Memory
**Symptom**: Application crashes with OutOfMemoryError

**Solutions**:
1. Increase heap size: `-Xmx4g`
2. Enable garbage collection logging: `-XX:+PrintGCDetails -Xloggc:gc.log`
3. Monitor long-running operations

### High CPU Usage
**Symptom**: Application consumes excessive CPU

**Solutions**:
1. Check dashboard.js auto-refresh interval
2. Verify TIBCO server response times
3. Use G1GC for better pause times: `-XX:+UseG1GC`

## Maintenance

### Regular Tasks
1. **Weekly**: Check log file sizes and disk space
2. **Monthly**: Review application logs for errors
3. **Quarterly**: Update Java security patches
4. **Annually**: Review TIBCO server configuration

### Backup
Backup essential files:
```bash
# Configuration
$CATALINA_HOME/webapps/dashboard/WEB-INF/classes/application.yml

# Logs (optional)
logs/dashboard.log*
```

### Updates
To deploy a new version:
1. Stop Tomcat
2. Remove old WAR: `rm $CATALINA_HOME/webapps/dashboard.war`
3. Remove old extract: `rm -rf $CATALINA_HOME/webapps/dashboard/`
4. Deploy new WAR
5. Start Tomcat

## Performance Optimization

### JVM Tuning
```bash
CATALINA_OPTS="-Xmx2g -Xms2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+ParallelRefProcEnabled \
  -XX:GCTimeRatio=4 \
  -XX:InitiatingHeapOccupancyPercent=35"
```

### Tomcat Tuning
Edit `$CATALINA_HOME/conf/server.xml`:
```xml
<Connector port="8080" protocol="HTTP/1.1"
   maxThreads="200"
   minSpareThreads="25"
   maxConnections="500"
   connectionTimeout="20000"
   enableLookups="false"
   redirectPort="8443" />
```

## Support & Documentation

### Project Files
- **Dashboard Controller**: `src/main/java/com/integrationhub/dashboard/DashboardController.java`
- **Configuration**: `src/main/resources/application.yml`
- **Logging**: `src/main/resources/logback-spring.xml`
- **Templates**: `src/main/resources/templates/`
- **Static Resources**: `src/main/resources/static/`

### Architecture
- **Framework**: Spring Boot 4.0.2
- **View Engine**: Thymeleaf
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Build Tool**: Maven 3.x

### Contact
For issues or questions, refer to the project README.md in the root directory.

---

**Created**: February 6, 2026
**Application Version**: 0.0.1-SNAPSHOT
**Deployment Guide Version**: 1.0
