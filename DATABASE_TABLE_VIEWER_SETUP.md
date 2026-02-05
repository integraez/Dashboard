# Database Table Viewer - Setup & Configuration Guide

## Overview

A new controller has been added to the Dashboard application that enables reading from a database table and displaying the data in the browser. The solution includes REST API endpoints and an HTML web interface for viewing database records.

---

## What Was Created

### 1. **Entity Model** (`SampleTable.java`)
- Location: `src/main/java/com/integrationhub/dashboard/model/SampleTable.java`
- JPA entity that maps to a database table
- Fields: `id`, `name`, `description`, `status`, `value`, `createdDate`

### 2. **Repository** (`SampleTableRepository.java`)
- Location: `src/main/java/com/integrationhub/dashboard/repository/SampleTableRepository.java`
- Spring Data JPA repository
- Provides CRUD operations and custom queries

### 3. **Service Layer** (`SampleTableService.java`)
- Location: `src/main/java/com/integrationhub/dashboard/service/SampleTableService.java`
- Business logic layer
- Handles database operations and transformations

### 4. **Controller** (`ShipEndpointController.java`)
- Location: `src/main/java/com/integrationhub/dashboard/ShipEndpointController.java`
- REST API endpoints
- Web page view rendering

### 5. **HTML Template** (`database-table.html`)
- Location: `src/main/resources/templates/database-table.html`
- Thymeleaf template for displaying database records
- Modern, responsive UI with styling

### 6. **Dependencies** (Updated `pom.xml`)
- Added `spring-boot-starter-data-jpa` for JPA/Hibernate support

### 7. **Configuration** (Updated `application.properties`)
- Database connection settings
- JPA/Hibernate properties

---

## How to Configure

### Step 1: Update Database Connection

Edit `src/main/resources/application.properties` and update these values:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=YourDatabaseName;encrypt=true;trustServerCertificate=true;
spring.datasource.username=sa
spring.datasource.password=YourPassword
```

**For SQL Server:**
```properties
spring.datasource.url=jdbc:sqlserver://SERVER_NAME:1433;databaseName=DATABASE_NAME;encrypt=true;trustServerCertificate=true;
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

**For MySQL (if you add the driver):**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/database_name
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### Step 2: Map Your Table

Edit `SampleTable.java` to match your actual database table:

```java
@Entity
@Table(name = "YourActualTableName")  // Change this
public class SampleTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "your_column_name")
    private String yourField;
    
    // Add more fields matching your table structure
}
```

**Example for a Customers table:**
```java
@Entity
@Table(name = "Customers")
public class SampleTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_name")
    private String name;
    
    @Column(name = "email")
    private String description;
    
    @Column(name = "account_status")
    private String status;
    
    @Column(name = "total_purchases")
    private Integer value;
    
    @Column(name = "registration_date")
    private String createdDate;
}
```

### Step 3: Rebuild and Run

```powershell
cd "c:\Users\lad28788\Downloads\dashboard\dashboard"
$env:MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true"
.\mvnw.cmd clean package -DskipTests -q
Start-Process -FilePath "java" -ArgumentList @("-jar", "target/dashboard-0.0.1-SNAPSHOT.jar") -WindowStyle Hidden
```

---

## Access the Application

### Web Interface
Open your browser and navigate to:
```
http://localhost:8080/database/table
```

This displays all database records in a formatted HTML table.

### REST API Endpoints

| Method | Endpoint | Purpose | Returns |
|--------|----------|---------|---------|
| GET | `/database/api/all` | Get all records | JSON array |
| GET | `/database/api/{id}` | Get single record by ID | Single JSON object |
| GET | `/database/api/status/{status}` | Filter by status | JSON array |
| GET | `/database/api/count` | Get total record count | Number |
| POST | `/database/api/create` | Create new record | Created record (JSON) |
| PUT | `/database/api/update/{id}` | Update record | Updated record (JSON) |
| DELETE | `/database/api/delete/{id}` | Delete record | None (204 No Content) |

### Example API Calls

**Get all records:**
```bash
curl http://localhost:8080/database/api/all
```

**Get record by ID:**
```bash
curl http://localhost:8080/database/api/1
```

**Create new record:**
```bash
curl -X POST http://localhost:8080/database/api/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "description": "Sample Description",
    "status": "active",
    "value": 100,
    "createdDate": "2026-02-05"
  }'
```

**Update record:**
```bash
curl -X PUT http://localhost:8080/database/api/update/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "inactive"
  }'
```

**Delete record:**
```bash
curl -X DELETE http://localhost:8080/database/api/delete/1
```

---

## Features

### Web Interface (`/database/table`)
- ✅ Display all records in a responsive table
- ✅ Show total record count
- ✅ Color-coded status indicators
- ✅ Auto-refresh button
- ✅ Export as JSON button
- ✅ Mobile-responsive design
- ✅ Last update timestamp

### REST API
- ✅ Full CRUD operations (Create, Read, Update, Delete)
- ✅ Filter by status
- ✅ Get record by ID
- ✅ Get total count
- ✅ Error handling with appropriate HTTP status codes
- ✅ JSON request/response format

### Status Indicator Colors
- 🟢 **Active** - Green background
- 🔴 **Inactive** - Red background
- 🟡 **Pending** - Yellow background

---

## Database Schema Example

If your database doesn't have a table yet, you can create one:

```sql
CREATE TABLE SampleTable (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255),
    description NVARCHAR(MAX),
    status NVARCHAR(50),
    value INT,
    createdDate NVARCHAR(50)
);

-- Sample data
INSERT INTO SampleTable (name, description, status, value, createdDate)
VALUES 
    ('Record 1', 'First record', 'active', 100, '2026-02-05'),
    ('Record 2', 'Second record', 'pending', 200, '2026-02-04'),
    ('Record 3', 'Third record', 'inactive', 150, '2026-02-03');
```

---

## Troubleshooting

### Issue: "No records found"
**Solution:** 
1. Verify database connection in `application.properties`
2. Ensure table exists in database
3. Check that table name matches `@Table(name = "...")` in entity
4. Verify user credentials have read permissions

### Issue: "Connection refused"
**Solution:**
1. Check database server is running
2. Verify correct host and port in connection string
3. Check firewall settings
4. Verify database name is correct

### Issue: "Table not found" error
**Solution:**
1. Verify table exists in database with `SELECT * FROM INFORMATION_SCHEMA.TABLES`
2. Check table name capitalization
3. Verify schema name (default: `dbo` for SQL Server)

### Issue: "Column not found"
**Solution:**
1. Update entity field names to match database column names
2. Use `@Column(name = "actual_db_column_name")` annotation
3. Run `SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'YourTable'`

### Issue: Build fails with JPA dependency
**Solution:**
1. Run `mvn clean install` to download dependencies
2. Check internet connection
3. Check Maven settings for proxy configuration

---

## Customization

### Add More Fields

Edit `SampleTable.java`:
```java
@Column(name = "email")
private String email;

@Column(name = "phone")
private String phone;

@Column(name = "salary")
private Double salary;
```

Add corresponding getters/setters.

### Add Custom Queries

Edit `SampleTableRepository.java`:
```java
@Query("SELECT s FROM SampleTable s WHERE s.name LIKE %:name%")
List<SampleTable> searchByName(@Param("name") String name);

List<SampleTable> findByValueBetween(Integer min, Integer max);
```

Then expose via controller:
```java
@GetMapping("/api/search/{name}")
@ResponseBody
public ResponseEntity<List<SampleTable>> searchRecords(@PathVariable String name) {
    return ResponseEntity.ok(sampleTableService.getRecordsByName(name));
}
```

### Customize HTML Template

Edit `database-table.html` to:
- Change colors in CSS
- Add/remove table columns
- Add search functionality
- Add pagination
- Add export to CSV functionality

---

## Database Connection Pool Configuration

For better performance with many requests, add connection pooling to `application.properties`:

```properties
# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

---

## Security Considerations

1. **Credentials:** Store database credentials in environment variables instead of `application.properties`
   ```properties
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   ```

2. **SQL Injection:** Always use parameterized queries (Spring Data JPA handles this)

3. **Authentication:** Add Spring Security to endpoints if needed

4. **Input Validation:** Add `@Valid` and `@NotNull` annotations to entity fields

---

## Performance Tips

1. **Index frequently queried columns** in your database
2. **Enable query pagination** for large tables
3. **Use `spring.jpa.show-sql=false`** in production
4. **Configure connection pool** appropriately
5. **Use database native queries** for complex operations

---

## Support Files

- Entity: `SampleTable.java`
- Repository: `SampleTableRepository.java`
- Service: `SampleTableService.java`
- Controller: `ShipEndpointController.java`
- Template: `database-table.html`
- Config: `application.properties`

---

**Created:** February 5, 2026  
**Status:** Ready for deployment  
**Next Steps:** Configure database connection and rebuild application
