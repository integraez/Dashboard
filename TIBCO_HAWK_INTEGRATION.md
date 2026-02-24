# TIBCO Hawk Integration - Application Status Monitor

## Overview
This integration provides real-time monitoring of TIBCO applications across multiple Hawk domains. It queries TIBCO Hawk agents to retrieve application status information and presents it through REST APIs and a web interface.

## Components Created

### 1. Configuration
**File:** `TibcoHawkProperties.java`
- Configures TIBCO Hawk domains with Rendezvous transport settings
- Supports multiple domains with different RV daemon/service/network configurations

### 2. Model
**File:** `HawkApplicationStatus.java`
- Represents application status information from Hawk agents
- Contains fields: domain, agent name, application name, status, host, uptime, version, etc.

### 3. Service Layer
**File:** `TibcoHawkService.java`
- Connects to TIBCO Hawk domains using AMI (Agent Management Interface)
- Queries microagents to retrieve application status
- Includes fallback mock data when Hawk libraries are unavailable
- Implements connection pooling and timeout handling

### 4. Controller
**File:** `HawkController.java`
- Exposes REST APIs for application status retrieval
- Provides filtering, searching, and aggregation capabilities

### 5. Web Interface
**File:** `hawk-monitor.html`
- Interactive dashboard for viewing application statuses
- Real-time updates with auto-refresh
- Filtering by domain, status, and search text

## API Endpoints

### Base Path: `/hawk`

#### 1. Get All Applications
```
GET /hawk/api/applications
```
Returns all application statuses from all configured domains.

**Response:**
```json
[
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
]
```

#### 2. Get Applications by Domain
```
GET /hawk/api/applications/domain/{domainName}
```
Returns applications for a specific Hawk domain.

#### 3. Get Running Applications
```
GET /hawk/api/applications/running
```
Returns only applications with status "RUNNING".

#### 4. Get Stopped Applications
```
GET /hawk/api/applications/stopped
```
Returns only applications that are not running.

#### 5. Get Applications by Host
```
GET /hawk/api/applications/host/{hostname}
```
Returns applications running on a specific host.

#### 6. Get Summary Statistics
```
GET /hawk/api/summary
```
Returns aggregated statistics across all domains.

**Response:**
```json
{
  "totalApplications": 50,
  "runningApplications": 45,
  "stoppedApplications": 5,
  "totalDomains": 2,
  "timestamp": "2026-02-24T10:30:00.000+00:00",
  "applicationsByDomain": {
    "PRD_DOMAIN": 30,
    "SHIP_DOMAIN": 20
  }
}
```

#### 7. Get Configured Domains
```
GET /hawk/api/domains
```
Returns all configured Hawk domains with connection status.

**Response:**
```json
[
  {
    "name": "PRD_DOMAIN",
    "rvService": "7474",
    "rvNetwork": "",
    "rvDaemon": "tcp:7500",
    "status": "CONNECTED"
  }
]
```

#### 8. Test Connection
```
GET /hawk/api/test-connection
```
Tests connectivity to all configured domains.

#### 9. Health Check
```
GET /hawk/api/health
```
Returns service health and domain connectivity status.

#### 10. Monitor Web Page
```
GET /hawk/monitor
```
Displays the interactive web dashboard.

## Configuration

### application.yml
Add Hawk domain configuration:

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

**Configuration Parameters:**
- `name`: Unique domain identifier
- `rvService`: Rendezvous service port
- `rvNetwork`: Rendezvous network (leave empty for default)
- `rvDaemon`: Rendezvous daemon endpoint
- `timeout`: Query timeout in milliseconds (default: 5000)

### pom.xml
The following dependencies have been added:

```xml
<!-- TIBCO Hawk Libraries -->
<dependency>
    <groupId>com.tibco.hawk</groupId>
    <artifactId>ami</artifactId>
    <version>6.2.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/src/main/java/com/integrationhub/dashboard/lib/ami.jar</systemPath>
</dependency>
<dependency>
    <groupId>com.tibco.hawk</groupId>
    <artifactId>talon</artifactId>
    <version>6.2.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/src/main/java/com/integrationhub/dashboard/lib/talon.jar</systemPath>
</dependency>
<dependency>
    <groupId>com.tibco.hawk</groupId>
    <artifactId>agent</artifactId>
    <version>6.2.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/src/main/java/com/integrationhub/dashboard/lib/agent.jar</systemPath>
</dependency>
<dependency>
    <groupId>com.tibco</groupId>
    <artifactId>tibrv</artifactId>
    <version>8.6.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/src/main/java/com/integrationhub/dashboard/lib/tibrv.jar</systemPath>
</dependency>
```

## Required Libraries

Ensure the following JAR files are in `src/main/java/com/integrationhub/dashboard/lib/`:
- `ami.jar` - Hawk Agent Management Interface (REQUIRED)
- `talon.jar` - Hawk Talon API (REQUIRED)
- `agent.jar` - Hawk Agent library (REQUIRED)
- `tibrv.jar` - TIBCO Rendezvous (OPTIONAL - uncomment dependency in pom.xml if available)

## Usage Examples

### Using cURL

Get all applications:
```bash
curl http://localhost:8080/hawk/api/applications
```

Get summary:
```bash
curl http://localhost:8080/hawk/api/summary
```

Get applications for specific domain:
```bash
curl http://localhost:8080/hawk/api/applications/domain/PRD_DOMAIN
```

### Using Browser
Navigate to: `http://localhost:8080/hawk/monitor`

This opens the interactive dashboard where you can:
- View all applications in real-time
- Filter by domain and status
- Search by application or agent name
- Auto-refresh every 30 seconds

## Fallback Mode

If TIBCO Hawk libraries are not available, the service automatically switches to **mock mode** and returns sample data. This allows development and testing without actual Hawk connectivity.

Mock mode is indicated by:
- Domain status shows "MOCK_MODE"
- Log message: "Using mock data - TIBCO Hawk libraries not available"

## Troubleshooting

### Libraries Not Found
**Symptom:** Service runs in mock mode despite libraries being present.

**Solution:**
1. Verify JAR files exist in `lib/` folder
2. Check file permissions
3. Rebuild the project: `mvnw clean install`

### Connection Timeout
**Symptom:** Domain status shows "ERROR: timeout"

**Solution:**
1. Verify Rendezvous daemon is running
2. Check network connectivity to RV daemon host
3. Verify rvService and rvDaemon configuration match your environment
4. Increase timeout value in configuration

### No Microagents Found
**Symptom:** Empty application list despite successful connection

**Solution:**
1. Verify Hawk agents are running in the domain
2. Check domain name matches actual Hawk domain
3. Verify RV service/network/daemon parameters are correct

## Performance Considerations

- Queries are executed with configurable timeouts (default 5 seconds)
- Large domains with many microagents may take longer to query
- Consider increasing timeout for large environments
- Auto-refresh frequency can be adjusted in the HTML (default 30 seconds)

## Security Notes

- Hawk connections do not currently use authentication
- Ensure network-level security (firewalls, VLANs) for Rendezvous traffic
- Consider implementing role-based access for the endpoints
- Sensitive domain configuration is in application.yml (protect this file)
