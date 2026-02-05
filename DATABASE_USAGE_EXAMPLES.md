# Database Table Viewer - Usage Examples

## Complete Working Example

### Scenario: Display Customer Records

#### Step 1: Create Database Table
```sql
CREATE TABLE Customers (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    status NVARCHAR(50),
    value INT,
    createdDate NVARCHAR(50)
);

-- Insert sample data
INSERT INTO Customers (name, description, status, value, createdDate) VALUES
('Alice Johnson', 'Premium Customer', 'active', 5000, '2026-01-15'),
('Bob Smith', 'Standard Customer', 'active', 2500, '2026-01-20'),
('Charlie Brown', 'Inactive Account', 'inactive', 1000, '2025-12-01'),
('Diana Prince', 'Pending Review', 'pending', 3500, '2026-02-01');
```

#### Step 2: Update Entity Class

Replace `SampleTable.java` content with:

```java
package com.integrationhub.dashboard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Customers")
public class SampleTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "value")
    private Integer value;

    @Column(name = "createdDate")
    private String createdDate;

    // Constructors, getters, setters (same as before)
}
```

#### Step 3: Update Database Configuration

In `application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=YourDatabaseName;encrypt=true;trustServerCertificate=true;
spring.datasource.username=sa
spring.datasource.password=YourPassword
```

#### Step 4: Build & Run

```powershell
# Stop existing app
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force

# Build
cd "c:\Users\lad28788\Downloads\dashboard\dashboard"
.\mvnw.cmd clean package -DskipTests -q

# Run
java -jar target/dashboard-0.0.1-SNAPSHOT.jar
```

#### Step 5: View Results

Open browser: `http://localhost:8080/database/table`

You should see a table with 4 customer records displayed with:
- ID column (1-4)
- Names (Alice, Bob, Charlie, Diana)
- Descriptions
- Status badges (green for active, red for inactive, yellow for pending)
- Values
- Created dates

---

## API Usage Examples

### Using cURL

#### Get All Customers
```bash
curl http://localhost:8080/database/api/all
```

Response:
```json
[
  {
    "id": 1,
    "name": "Alice Johnson",
    "description": "Premium Customer",
    "status": "active",
    "value": 5000,
    "createdDate": "2026-01-15"
  },
  {
    "id": 2,
    "name": "Bob Smith",
    "description": "Standard Customer",
    "status": "active",
    "value": 2500,
    "createdDate": "2026-01-20"
  }
]
```

#### Get Specific Customer (ID=1)
```bash
curl http://localhost:8080/database/api/1
```

Response:
```json
{
  "id": 1,
  "name": "Alice Johnson",
  "description": "Premium Customer",
  "status": "active",
  "value": 5000,
  "createdDate": "2026-01-15"
}
```

#### Get All Active Customers
```bash
curl http://localhost:8080/database/api/status/active
```

#### Get Record Count
```bash
curl http://localhost:8080/database/api/count
```

Response: `2`

#### Create New Customer
```bash
curl -X POST http://localhost:8080/database/api/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Eve Wilson",
    "description": "New Customer",
    "status": "pending",
    "value": 4000,
    "createdDate": "2026-02-05"
  }'
```

Response (201 Created):
```json
{
  "id": 5,
  "name": "Eve Wilson",
  "description": "New Customer",
  "status": "pending",
  "value": 4000,
  "createdDate": "2026-02-05"
}
```

#### Update Customer Status
```bash
curl -X PUT http://localhost:8080/database/api/update/3 \
  -H "Content-Type: application/json" \
  -d '{"status": "active"}'
```

#### Delete Customer
```bash
curl -X DELETE http://localhost:8080/database/api/delete/5
```

Response: 204 No Content

---

### Using JavaScript/Fetch

#### Get All Records
```javascript
fetch('http://localhost:8080/database/api/all')
  .then(response => response.json())
  .then(data => {
    console.log('All customers:', data);
    // Display data in table
    displayTable(data);
  })
  .catch(error => console.error('Error:', error));

function displayTable(records) {
  const html = records.map(r => `
    <tr>
      <td>${r.id}</td>
      <td>${r.name}</td>
      <td>${r.description}</td>
      <td>${r.status}</td>
      <td>${r.value}</td>
    </tr>
  `).join('');
  document.getElementById('tableBody').innerHTML = html;
}
```

#### Create New Record
```javascript
fetch('http://localhost:8080/database/api/create', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: 'Frank Miller',
    description: 'Premium Plus',
    status: 'active',
    value: 7500,
    createdDate: '2026-02-05'
  })
})
  .then(response => response.json())
  .then(data => console.log('Created:', data))
  .catch(error => console.error('Error:', error));
```

#### Update Record
```javascript
fetch('http://localhost:8080/database/api/update/1', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    status: 'inactive',
    value: 5500
  })
})
  .then(response => response.json())
  .then(data => console.log('Updated:', data))
  .catch(error => console.error('Error:', error));
```

#### Delete Record
```javascript
fetch('http://localhost:8080/database/api/delete/1', {
  method: 'DELETE'
})
  .then(response => {
    if (response.ok) {
      console.log('Record deleted successfully');
    }
  })
  .catch(error => console.error('Error:', error));
```

---

### Using PowerShell

#### Get All Records
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/database/api/all" -Method Get
$response | Format-Table
```

#### Create Record
```powershell
$body = @{
    name = "Grace Lee"
    description = "Enterprise Customer"
    status = "active"
    value = 10000
    createdDate = "2026-02-05"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/database/api/create" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

$response | Format-Table
```

#### Update Record
```powershell
$body = @{
    status = "inactive"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/database/api/update/1" `
    -Method Put `
    -ContentType "application/json" `
    -Body $body

$response | Format-Table
```

#### Delete Record
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/database/api/delete/1" -Method Delete
Write-Host "Record deleted"
```

---

## Advanced Customization

### Add Email Validation

Edit `SampleTable.java`:
```java
@Column(name = "email")
@Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$")
private String email;
```

Edit `database-table.html` to add email column:
```html
<th>Email</th>
...
<td th:text="${record.email}">-</td>
```

### Add Search Filter

Add to `SampleTableRepository.java`:
```java
@Query("SELECT s FROM SampleTable s WHERE s.name LIKE %:searchTerm% OR s.description LIKE %:searchTerm%")
List<SampleTable> search(@Param("searchTerm") String searchTerm);
```

Add to `SampleTableService.java`:
```java
public List<SampleTable> searchRecords(String searchTerm) {
    return sampleTableRepository.search(searchTerm);
}
```

Add to `ShipEndpointController.java`:
```java
@GetMapping("/api/search/{term}")
@ResponseBody
public ResponseEntity<List<SampleTable>> search(@PathVariable String term) {
    List<SampleTable> results = sampleTableService.searchRecords(term);
    return ResponseEntity.ok(results);
}
```

### Add Pagination

Add to `SampleTableService.java`:
```java
@Autowired
private SampleTableRepository repository;

public Page<SampleTable> getPaginatedRecords(int page, int size) {
    return repository.findAll(PageRequest.of(page, size));
}
```

Add to controller:
```java
@GetMapping("/api/page")
@ResponseBody
public ResponseEntity<Page<SampleTable>> getPage(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(sampleTableService.getPaginatedRecords(page, size));
}
```

---

## Testing the Application

### Verify Database Connection
```powershell
# Check if app starts without errors
java -jar target/dashboard-0.0.1-SNAPSHOT.jar

# Open browser and navigate to:
# http://localhost:8080/database/table

# Check if records appear (no error message)
```

### Test API Endpoints
```powershell
# Test all endpoints
$endpoints = @(
    "http://localhost:8080/database/api/all",
    "http://localhost:8080/database/api/count",
    "http://localhost:8080/database/api/1"
)

foreach ($endpoint in $endpoints) {
    Write-Host "Testing: $endpoint"
    Invoke-RestMethod -Uri $endpoint -Method Get | Format-Table
}
```

---

## Performance Monitoring

Monitor how many queries hit your database:

In `application.properties`, enable SQL logging:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

This will show all SQL queries in console output during development.

---

**Created:** February 5, 2026  
**Status:** Complete with examples  
**Last Updated:** February 5, 2026
