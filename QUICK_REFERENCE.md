# Dashboard App - Quick Reference Card

## 🚀 FAST DEPLOYMENT (5 minutes)

```bash
# 1. Copy WAR file
copy target\dashboard-0.0.1-SNAPSHOT.war %CATALINA_HOME%\webapps\dashboard.war

# 2. Stop Tomcat
%CATALINA_HOME%\bin\shutdown.bat

# 3. Start Tomcat
%CATALINA_HOME%\bin\startup.bat

# 4. Access app
# Open: http://localhost:8080/dashboard
```

## 📋 CRITICAL CONFIGURATION

**File**: `application.yml`

```yaml
tibco:
  ems:
    servers:
      - name: YOUR_SERVER
        host: your.server.com
        port: 7222
        username: admin
        password: secret
        ssl-enabled: false
```

## ⚙️ ENVIRONMENT VARIABLES

```batch
set JAVA_HOME=C:\Program Files\Java\jdk-17
set CATALINA_HOME=C:\apache-tomcat-10.0.x
set CATALINA_OPTS=-Xmx2g -Xms1g -Dspring.profiles.active=production
```

## ✅ QUICK VALIDATION

```bash
# Test endpoints
curl http://localhost:8080/dashboard
curl http://localhost:8080/dashboard/api/status
curl http://localhost:8080/dashboard/api/configured-servers

# Check logs
tail -f %CATALINA_HOME%\logs\catalina.out
```

## 🛑 EMERGENCY COMMANDS

**Stop Application**
```bash
%CATALINA_HOME%\bin\shutdown.bat
```

**Kill Java Process**
```bash
taskkill /F /IM java.exe
```

**Check Port 8080**
```bash
netstat -an | findstr 8080
```

## 📊 KEY METRICS

| Metric | Target | Alert |
|--------|--------|-------|
| Heap Usage | <80% | >85% |
| CPU Usage | <50% idle | >75% |
| Disk Space | >10GB free | <5GB |
| Response Time | <500ms | >1000ms |
| Error Rate | 0% | >1% |

## 🐛 QUICK TROUBLESHOOTING

| Problem | Quick Fix |
|---------|-----------|
| Won't start | Check `catalina.out` logs |
| 404 errors | Verify context path = `/dashboard` |
| UNKNOWN servers | Check TIBCO addresses in `application.yml` |
| Out of memory | Increase `-Xmx4g` |
| No logs | Check `logs/dashboard.log` path |

## 📁 IMPORTANT FILES

```
dashboard-0.0.1-SNAPSHOT.war     ← Deploy this
application.yml                  ← Update server config
DEPLOYMENT_GUIDE.md              ← Full instructions
DEPLOYMENT_CHECKLIST.md          ← Step-by-step
logback-spring.xml               ← Logging config
```

## 📞 SUPPORT LINKS

- **Tomcat**: https://tomcat.apache.org/
- **Java 17**: https://docs.oracle.com/en/java/javase/17/
- **Spring Boot**: https://spring.io/projects/spring-boot

## 🆘 EMERGENCY ROLLBACK

```bash
# 1. Stop Tomcat
%CATALINA_HOME%\bin\shutdown.bat

# 2. Restore backup
copy dashboard.war.backup %CATALINA_HOME%\webapps\dashboard.war

# 3. Start Tomcat
%CATALINA_HOME%\bin\startup.bat
```

---

**Status**: ✅ READY FOR DEPLOYMENT  
**Version**: 0.0.1-SNAPSHOT  
**Build Date**: 2026-02-06  
**WAR Size**: 37.7 MB
