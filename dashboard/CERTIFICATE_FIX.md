# SSL Certificate Fix Guide

## Problem
SSL certificate validation fails when connecting to Spring IO API or Maven repositories.

## Solutions

### Option 1: Import Corporate Certificate (RECOMMENDED)

1. **Get the certificate from your browser:**
   - Visit https://api.spring.io/projects in your browser
   - Click the padlock icon → Certificate → Details → Export
   - Save as `spring-io.cer`

2. **Import into Java truststore:**
   ```powershell
   # Find your Java installation
   $JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME")
   
   # Import certificate (run as Administrator)
   & "$JAVA_HOME\bin\keytool" -import -alias spring-io `
     -file spring-io.cer `
     -keystore "$JAVA_HOME\lib\security\cacerts" `
     -storepass changeit
   ```

### Option 2: Disable SSL Verification (DEV ONLY)

Add to `application.properties`:
```properties
# WARNING: Development only!
server.ssl.enabled=false
```

Set environment variable before running Maven:
```powershell
$env:MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true"
```

### Option 3: Configure Proxy Settings

If behind a corporate proxy, add to `settings.xml` (in `%USERPROFILE%\.m2\settings.xml`):
```xml
<proxies>
  <proxy>
    <id>corporate-proxy</id>
    <active>true</active>
    <protocol>http</protocol>
    <host>your-proxy-host</host>
    <port>8080</port>
    <nonProxyHosts>localhost|127.0.0.1</nonProxyHosts>
  </proxy>
</proxies>
```

### Option 4: Use System Certificates

Add JVM argument to use system certificates:
```
-Djavax.net.ssl.trustStore=NUL -Djavax.net.ssl.trustStoreType=Windows-ROOT
```

## Testing

After applying the fix, test with:
```powershell
mvn clean install
```

Or run your application:
```powershell
mvn spring-boot:run
```
