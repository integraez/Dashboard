# TIBCO Hawk Integration - Quick Start Guide

## What Was Created

### New Controller: HawkController
**Location:** `com.integrationhub.dashboard.HawkController`
**Base URL:** `/hawk`

This controller provides REST APIs to read application statuses from TIBCO Hawk domains.

## Quick Test

### 1. Access the Web Dashboard
Once the app is running, navigate to:
```
http://localhost:8080/hawk/monitor
```

### 2. Test the API Endpoints
```bash
# Get all applications
curl http://localhost:8080/hawk/api/applications

# Get summary statistics  
curl http://localhost:8080/hawk/api/summary

# Get configured domains
curl http://localhost:8080/hawk/api/domains

# Test connection to domains
curl http://localhost:8080/hawk/api/test-connection

# Health check
curl http://localhost:8080/hawk/api/health
```

## Files Created

1. **HawkController.java** - REST API endpoints
2. **TibcoHawkService.java** - Service layer with Hawk connectivity
3. **HawkApplicationStatus.java** - Model for application status data
4. **TibcoHawkProperties.java** - Configuration properties
5. **hawk-monitor.html** - Interactive web dashboard
6. **TIBCO_HAWK_INTEGRATION.md** - Complete documentation

## Configuration Updated

### application.yml
Added TIBCO Hawk domain configuration:
```yaml
tibco:
  hawk:
    domains:
      - name: PRD_DOMAIN
        rvService: 7474
        rvNetwork: ""
        rvDaemon: tcp:7500
        timeout: 5000
      - name: SHIP_DOMAIN
        rvService: 7474
        rvNetwork: ""
        rvDaemon: tcp:rhlpaems611.na.rccl.com:7500
        timeout: 5000
```

### pom.xml
Added TIBCO Hawk library dependencies (ami.jar, talon.jar, agent.jar)

## Mock Data Mode

The service will automatically run in **mock mode** if TIBCO Hawk libraries are not available or cannot be loaded. This allows testing the API and UI without actual Hawk connectivity.

In mock mode:
- Returns sample application data
- Domain status shows "MOCK_MODE"
- All API endpoints work normally
- No actual Hawk connections are made

## Customizing for Your Environment

### Update Domain Configuration
Edit `src/main/resources/application.yml`:

```yaml
tibco:
  hawk:
    domains:
      - name: YOUR_DOMAIN_NAME
        rvService: YOUR_RV_SERVICE_PORT     # e.g., 7474
        rvNetwork: YOUR_RV_NETWORK          # Usually empty ""
        rvDaemon: YOUR_RV_DAEMON_ENDPOINT   # e.g., tcp:hostname:7500
        timeout: 5000                       # Query timeout in ms
```

### Required Information
To connect to your TIBCO Hawk environment, you need:
1. **Domain Name** - The Hawk domain identifier
2. **RV Service** - Rendezvous service port (typically 7474)
3. **RV Network** - Network specification (often empty for default)
4. **RV Daemon** - Rendezvous daemon endpoint (hostname:port)

## API Response Example

```json
{
  "domainName": "PRD_DOMAIN",
  "agentName": "HawkAgent_1",
  "microAgentName": "COM.TIBCO.adapter.bw.engine.1",
  "applicationName": "BWEngine_App_1",
  "status": "RUNNING",
  "running": true,
  "host": "server1.example.com",
  "processId": "1001",
  "version": "6.6.0",
  "uptime": 3600000,
  "timestamp": "2026-02-24T10:30:00.000+00:00"
}
```

## Features

### Web Dashboard (`/hawk/monitor`)
- View all applications across all domains
- Real-time status updates (auto-refresh every 30 seconds)
- Filter by domain and status
- Search by application or agent name
- Summary statistics

### API Endpoints
- **GET /hawk/api/applications** - All applications
- **GET /hawk/api/applications/domain/{name}** - Filter by domain
- **GET /hawk/api/applications/running** - Only running apps
- **GET /hawk/api/applications/stopped** - Only stopped apps
- **GET /hawk/api/applications/host/{hostname}** - Filter by host
- **GET /hawk/api/summary** - Aggregate statistics
- **GET /hawk/api/domains** - List configured domains
- **GET /hawk/api/test-connection** - Test connectivity
- **GET /hawk/api/health** - Service health check

## Next Steps

1. **Update Configuration**: Modify `application.yml` with your actual Hawk domain settings
2. **Add Libraries**: If not already present, add required JAR files to `lib/` folder:
   - ami.jar
   - talon.jar
   - agent.jar
3. **Test Connection**: Use `/hawk/api/test-connection` to verify connectivity
4. **Customize Queries**: Modify `TibcoHawkService.queryMicroAgent()` to extract specific microagent data

## Troubleshooting

**Service returns mock data:**
- Check if Hawk JAR files are in the lib folder
- Verify library paths in pom.xml
- Check application logs for "TIBCO Hawk library not found" message

**Connection timeout:**
- Verify RV daemon is running and accessible
- Check network connectivity
- Verify service/network/daemon settings match your environment
- Increase timeout value in configuration

**No applications found:**
- Verify Hawk agents are running in the domain
- Check domain name matches actual Hawk domain
- Verify RV settings are correct

For detailed documentation, see **TIBCO_HAWK_INTEGRATION.md**
