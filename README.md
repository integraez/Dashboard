# Dashboard
# Dashboard Tool User Guide

## 1) What this tool does
The Dashboard web app helps operations teams monitor:
- TIBCO EMS queue metrics
- TIBCO Hawk application health
- Shows Duplicated BW process deployments by host
- Ship Endpoint/system status data check

## 2) Prerequisites
- Java 17+
- Maven 3.9+ (or use `mvnw.cmd`)
- Network access to SQL Server and TIBCO endpoints
- Local TIBCO client libraries in `src/main/java/com/integrationhub/dashboard/lib`

## 3) Configure the app
Primary runtime config file:
- `src/main/resources/application.yml`

Recommended production practice:
- Move sensitive values (DB credentials, TIBCO credentials) to environment variables or externalized config

### Duplicate-process data file
The Process Monitor page requires a JSON input file.
Set one of:
- Environment variable: `TIBCO_PROCESS_MONITOR_FILE`
- Property: `tibco.process-monitor.data-file`

Example (PowerShell):
```powershell
$env:TIBCO_PROCESS_MONITOR_FILE='C:\data\tibco_duplicate_processes.json'
```

### Hawk application-status data file
The Hawk Monitor page now reads from a text file (instead of calling Hawk on each server).
Set one of:
- Environment variable: `TIBCO_HAWK_MONITOR_FILE`
- Property: `tibco.hawk.data-file`

If the configured Hawk file is not available, the application automatically falls back to bundled mock data in:
- `src/main/resources/sample-data/hawk-monitor.sample.txt`

Example (PowerShell):
```powershell
$env:TIBCO_HAWK_MONITOR_FILE='C:\data\hawk-monitor-status.txt'
```

Expected format excerpt:
```text
Application Name: STG81/Lufthansa/XControl
  Service Name: ToXControl.par
  Deployment Status: Disabled
    Service Instance Name: ToXControl-01-01
    Machine Name: nlbw981.nowlab.tstsh.tstrccl.com
    Status: Unknown
```

## 4) Build and run
From `dashboard_v1.1`:

### Development run
```powershell
.\mvnw.cmd spring-boot:run
```

### Build WAR
```powershell
.\mvnw.cmd clean package
```
Generated artifact:
- `target/dashboard-0.0.1-SNAPSHOT.war`

### Run packaged WAR
```powershell
java -jar target\dashboard-0.0.1-SNAPSHOT.war
```

## 5) Main pages and APIs
After startup (default `http://localhost:8080`):
- `/login`
- `/home`
- `/dashboard`
- `/hawk-monitor`
- `/endpoints-ship`
- `/tibco-process-monitor`

Process monitor API:
- `/tibco-process-monitor/api/duplicates`

## 6) JSON format for duplicate process monitor
Expected shape:
```json
[
  {
    "server": "adbw931",
    "duplicate_processes": "OrderFlow, BillingFlow"
  },
  {
    "server": "albw931",
    "duplicate_processes": "no duplicates"
  }
]
```

## 7) Troubleshooting
- Build fails on locked JAR in `target/`: stop running Java process, delete `target`, rebuild.
- Process monitor page is empty: validate JSON path and file readability.
- Hawk monitor page is empty: validate text file path in `TIBCO_HAWK_MONITOR_FILE` and file formatting.
- DB startup failures: validate SQL Server URL/credentials and network/firewall rules.
- Missing TIBCO classes: verify all required JARs exist under `lib` directory.

## 8) Operational workflow for duplicate process reporting
1. Run the Ansible playbook that scans BW processes across servers.
2. Ensure the JSON report is written to the file path configured in `TIBCO_PROCESS_MONITOR_FILE`.
3. Refresh `/tibco-process-monitor` to view grouped duplicate processes by hostname.
