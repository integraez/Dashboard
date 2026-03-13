# Integration Dashboard – User Presentation

## Slide 1 – Purpose
- Centralized operations dashboard for TIBCO-centric integrations
- Single UI for queue visibility, app health, endpoint status, and duplicate BW process detection
- Reduces manual troubleshooting and host-by-host checks

## Slide 2 – Who should use it
- Integration Operations
- Middleware Support
- On-call Engineers
- Platform Reliability teams

## Slide 3 – Key capabilities
- EMS queue monitoring and visibility
- Hawk-based application status monitoring
- Endpoint ship/system status dashboard
- Duplicate BW process monitoring from generated JSON inputs

## Slide 4 – Business value
- Faster incident triage
- Reduced outage detection time
- Better visibility across distributed servers
- Standardized operational view for support teams

## Slide 5 – How users interact with it
1. Login to the dashboard
2. Navigate to required monitor page (`dashboard`, `hawk-monitor`, `endpoints-ship`, `tibco-process-monitor`)
3. Review status and duplicate-process findings
4. Trigger remediation in external systems/runbooks

## Slide 6 – Data flow (high level)
- Sources: SQL Server, TIBCO EMS/Hawk data, server-generated JSON from Ansible scans
- App layer: Spring Boot services aggregate and normalize data
- UI layer: Thymeleaf pages present operational status

## Slide 7 – Reliability and operations
- Built as a WAR for standard deployment models
- Supports external file configuration for process-monitor JSON
- Designed for environment-based configuration and operational flexibility

## Slide 8 – Adoption checklist
- Confirm required TIBCO libs are available
- Configure environment properties and credentials
- Validate DB and network connectivity
- Set `TIBCO_PROCESS_MONITOR_FILE`
- Build and deploy WAR, then validate pages/endpoints
