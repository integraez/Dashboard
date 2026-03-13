# TIBCO EMS Integration Setup

## To Enable Real-Time Queue Monitoring:

1. **Locate TIBCO EMS JAR files** on your system (typically in TIBCO EMS installation):
   - `tibjms.jar`
   - `tibjmsadmin.jar`
   - `jms-2.0.jar` (or javax.jms.jar)

2. **Create lib directory** in project root:
   ```
   mkdir lib
   ```

3. **Copy JAR files** to the lib directory:
   ```
   dashboard/
   ├── lib/
   │   ├── tibjms.jar
   │   ├── tibjmsadmin.jar
   │   └── jms-2.0.jar
   ├── src/
   └── pom.xml
   ```

4. **Add to classpath** when running:
   - For Maven: Files in `lib/` will be auto-detected via reflection
   - For direct Java: Add `-cp lib/*` to java command

## Current Behavior:

- **Without TIBCO JARs**: Application shows mock queue data from TST servers
- **With TIBCO JARs**: Application connects to actual TIBCO EMS servers and displays real-time queue data

## Features:

- Connects to all configured TST servers concurrently
- Filters queues with >3000 messages
- Shows top 20 high-volume queues sorted by message count
- Color-coded status: Critical (>10,000), Warning (>5,000), OK
- 5-second timeout per server to prevent hanging
- Automatic fallback to mock data if connection fails

## Security:

Passwords are encrypted in application.properties using TIBCO's password encryption format.
