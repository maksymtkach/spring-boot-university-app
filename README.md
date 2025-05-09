# SimpleUniversity Console Application

A simple Spring Boot console application to manage a university’s departments and lectors. Lectors can work in multiple departments and have one of three degrees: **ASSISTANT**, **ASSOCIATE_PROFESSOR**, or **PROFESSOR**. All data is stored in a relational database (PostgreSQL by default).

## Supported Commands

- **Stats**  
  - who is head of department `{departmentName}`
  - show `{departmentName}` statistics 
  - show the average salary for the department `{departmentName}`  
  - show count of employee for `{departmentName}`  
  - global search by `{template}`  

- **Create**  
  - add department `{departmentName}` head `{lectorKey}`  
  - add lector `{firstName}` `{lastName}` degree `{ASSISTANT | ASSOCIATE_PROFESSOR | PROFESSOR}` salary `{salary}` `[departments dept1,dept2,…]`  

- **Update**  
  - update department `{deptKey}` head `{newHeadKey}`  
  - update lector `{lectorKey}` `{field}` `{newValue}`  

- **Delete**  
  - delete department `{deptKey}`  
  - delete lector `{lectorKey}`  

- **List**  
  - list departments
  - list lectors 

- **Exit**  
  - exit

---

## Getting Started

### Prerequisites

- **Java 17** or higher  
- **Maven 3.6+**  
- **PostgreSQL** (or another JDBC-compatible RDBMS)

### Database Setup

#### 1. Create the database

```bash
psql -U postgres
CREATE DATABASE university;
\q
```



### Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.application.name=SimpleUniversity

spring.datasource.url=jdbc:postgresql://localhost:5432/university
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

> **Note**: replace `postgres` and `your_password` with your actual PostgreSQL username and password.

> **Note**: replace `university` with your actual PostgreSQL DB name.

---

## Usage & Examples

### Query Commands

* **Who is head of department**

  ```text
  who is head of department Physics
  ```
  
  > Head of Physics department is Albert Einstein

* **Show department statistics**

  ```text
  show Mathematics statistics
  ```

  > assistants - 2
  > 
  > associate professors - 1
  > 
  > professors - 0
  

* **Show average salary**

  ```text
  show the average salary for the department Physics
  ```

  > The average salary of Physics is 6233.33

* **Show employee count**

  ```text
  show count of employee for Chemistry
  ```

  > 3

* **Global search by name fragment**

  ```text
  global search by van
  ```

  > Ivan Petrenko, Petro Ivanov

---

### Create Commands

* **Add a new department**

  ```text
  add department CS head 3
  add department History head "Jane Smith"
  ```

  > Department created.

* **Add a new lector**

  ```text
  add lector Alice Johnson degree ASSISTANT salary 5000
  add lector Bob Lee degree PROFESSOR salary 8000 departments Physics,Mathematics
  ```

  > Lector created.

---

### Update Commands

* **Change a department’s head**

  ```text
  update department CS head 2
  update department Physics head "Albert Einstein"
  ```

  Department head updated.

* **Update a lector’s field**

  ```text
  update lector 5 salary 9000
  update lector "Alice Johnson" degree ASSOCIATE_PROFESSOR
  update lector Bob Lee departments CS,History
  ```

  Lector updated.

---

### Delete Commands

* **Delete a department**

  ```text
  delete department History
  ```

  > Department deleted.

* **Delete a lector**

  ```text
  delete lector 7
  ```

  > Lector deleted.

---

### List Commands

* **List all departments**

  ```text
  list departments
  ```

  > id=1 name=Physics head=Albert Einstein
  > 
  > id=2 name=Mathematics head=John Doe


* **List all lectors**

  ```text
  list lectors
  ```

  > id=1 John Doe ASSOCIATE_PROFESSOR salary=5000.00
  > 
  > id=2 Jane Smith ASSISTANT salary=4500.00

---

### Exit

```text
exit
```

---