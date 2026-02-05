# Database Table Viewer - Quick Start Guide

## What You Got

A complete database reading solution with:
- ✅ Web interface to view table data
- ✅ REST API for CRUD operations
- ✅ Modern responsive HTML dashboard
- ✅ JPA/Hibernate ORM layer

---

## 3-Step Setup

### 1. Configure Database Connection
Edit: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:sqlserver://YOUR_SERVER:1433;databaseName=YOUR_DB;encrypt=true;trustServerCertificate=true;
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### 2. Map Your Table
Edit: `src/main/java/com/integrationhub/dashboard/model/SampleTable.java`

Change the table name and add your columns:
```java
@Table(name = "YourTableName")
```

### 3. Rebuild & Run
```powershell
cd "c:\Users\lad28788\Downloads\dashboard\dashboard"
.\mvnw.cmd clean package -DskipTests -q
java -jar target/dashboard-0.0.1-SNAPSHOT.jar
```

---

## Access Your Data

### View in Browser
```
http://localhost:8080/database/table
```

### Get JSON Data
```
http://localhost:8080/database/api/all
```

---

## Files Created

| File | Purpose |
|------|---------|
| `SampleTable.java` | Database entity model |
| `SampleTableRepository.java` | Data access layer |
| `SampleTableService.java` | Business logic |
| `ShipEndpointController.java` | REST API & web routes |
| `database-table.html` | HTML dashboard |
| `application.properties` | Database config |

---

## REST API Endpoints

```bash
# Get all records
GET /database/api/all

# Get by ID
GET /database/api/1

# Create record
POST /database/api/create
Body: { "name": "test", "status": "active", ... }

# Update record
PUT /database/api/update/1
Body: { "status": "inactive" }

# Delete record
DELETE /database/api/1

# Count records
GET /database/api/count

# Filter by status
GET /database/api/status/active
```

---

## Common Issues & Fixes

| Issue | Fix |
|-------|-----|
| "Connection refused" | Check database server is running and port is correct |
| "No records found" | Verify table name matches `@Table(name = "...")` |
| "Column not found" | Update entity fields to match database column names |
| Build fails | Run `mvn clean install` to download dependencies |

---

## Need to Change Table Structure?

1. Edit `SampleTable.java` - add/remove fields
2. Update `@Column(name = "db_column_name")` annotations
3. Add getters/setters for new fields
4. Rebuild: `mvn clean package -DskipTests -q`

---

**For detailed setup: See `DATABASE_TABLE_VIEWER_SETUP.md`**
