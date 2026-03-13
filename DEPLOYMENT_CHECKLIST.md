# Dashboard Application - Tomcat Deployment Checklist

## Pre-Deployment

- [ ] **Download & Install Java 17+**
  - Download from Oracle or OpenJDK
  - Verify: `java -version` (should show Java 17+)

- [ ] **Download & Install Apache Tomcat 10.0.x+**
  - Set `CATALINA_HOME` environment variable
  - Verify: `echo %CATALINA_HOME%` (Windows) or `echo $CATALINA_HOME` (Linux)

- [ ] **Review Configuration**
  - [ ] Update TIBCO server addresses in `application-production.yml`
  - [ ] Configure server credentials
  - [ ] Set appropriate logging paths
  - [ ] Plan memory allocation (min 2GB recommended)

- [ ] **Prepare Environment Variables**
  ```bash
  # Windows (setenv.bat)
  set JAVA_HOME=C:\Program Files\Java\jdk-17
  set CATALINA_HOME=C:\apache-tomcat-10.0.x
  set CATALINA_OPTS=-Xmx2g -Xms1g -Dspring.profiles.active=production
  
  # Linux/Mac (setenv.sh)
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
  export CATALINA_HOME=/opt/tomcat
  export CATALINA_OPTS="-Xmx2g -Xms1g -Dspring.profiles.active=production"
  ```

## Build & Package

- [ ] **Build WAR File**
  ```bash
  cd dashboard
  ./mvnw.cmd clean package -DskipTests    # Windows
  ./mvnw clean package -DskipTests          # Linux/Mac
  ```

- [ ] **Verify WAR Created**
  - Check: `target/dashboard-0.0.1-SNAPSHOT.war` exists
  - Size should be ~39.5 MB

## Deployment

### Option A: Manual Deployment (Recommended)

1. [ ] **Backup Existing Deployment** (if applicable)
   ```bash
   copy %CATALINA_HOME%\webapps\dashboard.war dashboard.war.backup
   ```

2. [ ] **Stop Tomcat**
   ```bash
   %CATALINA_HOME%\bin\shutdown.bat    # Windows
   $CATALINA_HOME/bin/shutdown.sh      # Linux/Mac
   ```
   - Wait 30 seconds for clean shutdown

3. [ ] **Deploy WAR**
   ```bash
   copy target\dashboard-0.0.1-SNAPSHOT.war %CATALINA_HOME%\webapps\dashboard.war
   ```

4. [ ] **Start Tomcat**
   ```bash
   %CATALINA_HOME%\bin\startup.bat    # Windows
   $CATALINA_HOME/bin/startup.sh      # Linux/Mac
   ```

5. [ ] **Verify Deployment**
   - Wait 30-60 seconds for startup
   - Open: http://localhost:8080/dashboard
   - Check logs: `%CATALINA_HOME%\logs\catalina.out`

### Option B: Tomcat Manager Deployment

1. [ ] Open Tomcat Manager: http://localhost:8080/manager/html
2. [ ] Authenticate with admin credentials
3. [ ] Upload WAR file from `target/dashboard-0.0.1-SNAPSHOT.war`
4. [ ] Set context path: `/dashboard`
5. [ ] Click "Deploy"
6. [ ] Verify app appears in application list as "Started"

## Post-Deployment Verification

- [ ] **Test Home Page**
  - URL: http://localhost:8080/dashboard/
  - Should display landing page with header

- [ ] **Test Dashboard Page**
  - URL: http://localhost:8080/dashboard/dashboard
  - Should display server tiles and queue information
  - Should show "Last update: HH:MM:SS" in Fleet panel

- [ ] **Test API Endpoints**
  ```bash
  curl http://localhost:8080/dashboard/api/status
  curl http://localhost:8080/dashboard/api/configured-servers
  curl http://localhost:8080/dashboard/api/queues
  ```

- [ ] **Check Application Logs**
  - Location: `logs/dashboard.log` (configured in logback-spring.xml)
  - Should contain: "TibcoEmsService initialized with X servers"
  - No ERROR or EXCEPTION entries

- [ ] **Monitor System Resources**
  - [ ] Heap usage should be stable (not continuously growing)
  - [ ] CPU usage should be low when idle
  - [ ] Check disk space for logs

## Production Runtime

### Daily Monitoring

- [ ] **Check Application Status**
  ```bash
  curl http://localhost:8080/dashboard/api/status
  ```

- [ ] **Verify Logs**
  - Check for errors: `grep ERROR logs/dashboard.log`
  - Monitor log file size
  - Check for authentication issues

### Weekly Tasks

- [ ] Review application logs for anomalies
- [ ] Verify TIBCO server connectivity
- [ ] Check available disk space for logs
- [ ] Monitor heap usage trends

### Monthly Tasks

- [ ] Review and clean up old log files
- [ ] Test data backup procedures
- [ ] Verify disaster recovery plan
- [ ] Update documentation if needed

## Troubleshooting Guide

### Application Won't Start

**Symptom**: 404 error or connection refused when accessing app

**Steps**:
1. Check Tomcat status: `ps aux | grep tomcat` (Linux) or Task Manager (Windows)
2. Review logs: `tail -f logs/catalina.out`
3. Verify port 8080 is not in use: `netstat -an | grep 8080`
4. Check JAVA_HOME is set correctly: `echo %JAVA_HOME%`
5. Increase memory and retry: `-Xmx2g`

### TIBCO Servers Show "UNKNOWN" Status

**Symptom**: Dashboard loads but all servers show UNKNOWN status

**Steps**:
1. Verify TIBCO addresses in `application.yml`
2. Test connectivity: `ping <tibco-server-host>`
3. Verify firewall allows port 7222
4. Check credentials
5. Review: `grep "setServers\|initialized with" logs/dashboard.log`

### Out of Memory Error

**Symptom**: Application crashes with OutOfMemoryError

**Steps**:
1. Increase heap: Set `-Xmx4g` if current is `-Xmx2g`
2. Enable garbage collection logging:
   ```bash
   -XX:+PrintGCDetails -Xloggc:gc.log
   ```
3. Check for memory leaks in dashboard.js
4. Verify auto-refresh parameters in application.yml

### High CPU Usage

**Symptom**: CPU usage remains high when idle

**Steps**:
1. Check dashboard.js auto-refresh interval (default: 5 minutes)
2. Verify TIBCO server response times are reasonable
3. Enable JFR (Java Flight Recorder) to profile:
   ```bash
   -XX:StartFlightRecording=duration=30s,filename=recording.jfr
   ```
4. Analyze thread dump: `jstack <pid>`

## Rollback Procedure

If issues occur after deployment:

1. [ ] **Stop Tomcat**
   ```bash
   %CATALINA_HOME%\bin\shutdown.bat
   ```

2. [ ] **Remove New Deployment**
   ```bash
   del %CATALINA_HOME%\webapps\dashboard.war
   rmdir /S %CATALINA_HOME%\webapps\dashboard
   ```

3. [ ] **Restore Backup** (if available)
   ```bash
   copy dashboard.war.backup %CATALINA_HOME%\webapps\dashboard.war
   ```

4. [ ] **Restart Tomcat**
   ```bash
   %CATALINA_HOME%\bin\startup.bat
   ```

5. [ ] **Verify Previous Version**
   - Access http://localhost:8080/dashboard
   - Confirm functionality

## Support Contacts & Resources

- **Tomcat Documentation**: https://tomcat.apache.org/
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Java 17 Docs**: https://docs.oracle.com/en/java/javase/17/

---

**Created**: February 6, 2026
**Version**: 1.0
**Last Updated**: 2026-02-06
