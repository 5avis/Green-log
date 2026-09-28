# 🌱 GreenLog — Enterprise Tree Plantation Drive Tracker

> **A mission-critical, full-stack ERP system for tracking environmental plantation drives, monitoring sapling health check-ins, enforcing survival business rules, and evaluating volunteer contributions.**  
> Built with **Spring Boot 3**, **Spring Data JPA**, **MariaDB / MySQL**, and an authentic **Early-2000s Enterprise ERP Vanilla UI**.

---

## 📋 Table of Contents
- [System Architecture & Tech Stack](#system-architecture--tech-stack)
- [Core Business Rules](#core-business-rules)
- [REST API Specification](#rest-api-specification)
- [Relational Database Schema](#relational-database-schema)
- [Early-2000s ERP Frontend Design](#early-2000s-erp-frontend-design)
- [Getting Started & Local Setup](#getting-started--local-setup)
- [Cloud & Container Deployment](#cloud--container-deployment)
- [Automated Test Suite](#automated-test-suite)
- [Project Directory Layout](#project-directory-layout)

---

## 🏛 System Architecture & Tech Stack

```
+-------------------------------------------------------------+
|         Early-2000s ERP Web Interface (Vanilla JS)          |
|  Tahoma 11px | 3D Beveled Borders | Olive Drab Theme | Tabs |
+-------------------------------------------------------------+
                              │ HTTP / REST JSON
                              ▼
+-------------------------------------------------------------+
|               Spring Boot 3.5 REST Backend                  |
|  - @RestController Layer with Global Exception Handling     |
|  - Transactional Service Layer (@Transactional)             |
|  - Spring Data JPA Repositories (Derived & JPQL Queries)    |
+-------------------------------------------------------------+
                              │ JDBC
                              ▼
+-------------------------------------------------------------+
|                 MariaDB / MySQL Database                    |
|  Tables: plantation_drives, volunteers, trees, check_ins    |
+-------------------------------------------------------------+
```

* **Backend Framework:** Java 21, Spring Boot 3.5.0
* **Persistence & ORM:** Spring Data JPA, Hibernate, MariaDB JDBC Driver
* **Database Engine:** MariaDB / MySQL (`green_log` / `defaultdb`)
* **Containerization:** Multi-stage Dockerfile (Eclipse Temurin 21 JRE Alpine)
* **Frontend:** Vanilla HTML5, Pure CSS3 (Windows Classic / ERP theme), Vanilla JavaScript (ES6 Modules/Fetch API — zero external runtime frameworks)
* **Automated Testing:** JUnit 5, MockMvc, AssertJ, Spring Boot Test

---

## ⚖️ Core Business Rules

1. **The Dead Tree Invariance Rule (DeadTreeException)**:
   * Trees are initially recorded as `ALIVE`.
   * A health check-in can update a tree's status to `DEAD` if the sapling failed.
   * **Once marked `DEAD`, no further check-ins are permitted on that tree.**
   * Submitting a check-in for a deceased tree immediately aborts with a custom `DeadTreeException` and produces an HTTP `400 Bad Request` containing a structured JSON error response.
2. **Automated Inspection Due-Queue**:
   * Health inspectors must follow up on saplings periodically.
   * Any tree planted over 30 days ago without any check-ins, or whose most recent check-in is older than 30 days, is automatically routed into the **Health Check-ins Due** queue with badge alerts.
3. **Real-Time Dynamic Survival Metrics**:
   * Survival rates are calculated in real time across two distinct dimensions:
     * **By Plantation Drive**: Quantifies site and event efficacy.
     * **By Tree Species**: Identifies species resilience and climate suitability.
4. **Gamified Volunteer Contribution**:
   * Planting records automatically increment or assign volunteer totals.
   * A real-time leaderboard ranks volunteers in descending order of trees planted.

---

## 📡 REST API Specification

### 1. Assessment / Core Grading Endpoints

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/trees` | Register a new tree planting (supports assigning existing or auto-registering new volunteer) | `201 Created` |
| `POST` | `/api/checkins` | Submit health check-in (updates height, remarks, alive/dead status; blocks if already dead) | `201 Created` / `400 Bad Request` |
| `GET` | `/api/stats/survival` | Get survival statistics grouped by drive and by species | `200 OK` |
| `GET` | `/api/trees/due-checkins` | Fetch all saplings overdue for periodic health evaluation | `200 OK` |
| `GET` | `/api/stats/leaderboard` | Get volunteer leaderboard ranked by trees planted | `200 OK` |

### 2. Comprehensive CRUD Operations

| Entity | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Trees** | `GET` | `/api/trees` | List all registered trees (with drive and volunteer data) |
| | `GET` | `/api/trees/{id}` | Retrieve specific tree details |
| | `PUT` | `/api/trees/{id}` | Update tree details (species, coordinates, drive assignment) |
| | `DELETE` | `/api/trees/{id}` | Delete tree record and associated check-ins |
| **Drives** | `GET` | `/api/drives` | List all plantation drives |
| | `POST` | `/api/drives` | Create a new drive event |
| | `PUT` | `/api/drives/{id}` | Modify drive parameters (name, location, target date) |
| | `DELETE` | `/api/drives/{id}` | Delete drive (cascades safely) |
| **Volunteers** | `GET` | `/api/volunteers` | List all registered volunteers |
| | `POST` | `/api/volunteers` | Register a new volunteer |
| | `PUT` | `/api/volunteers/{id}` | Update volunteer profile or tree count |
| | `DELETE` | `/api/volunteers/{id}` | Remove volunteer record |
| **System** | `DELETE` | `/api/stats/purge-all` | Wipe all 4 tables in safe foreign-key sequence for a complete reset |

---

## 🗄 Relational Database Schema

```
   +----------------------+             +----------------------+
   |   PLANTATION_DRIVES  |             |      VOLUNTEERS      |
   +----------------------+             +----------------------+
   | id          (PK, AI) |             | id          (PK, AI) |
   | name        VARCHAR  |             | name        VARCHAR  |
   | location    VARCHAR  |             | total_trees INT      |
   | date        DATE     |             +----------------------+
   +----------------------+                         │
              │ 1                                   │ 0..1
              │                                     │
              │                   +─────────────────┘
              ▼ *                 ▼ *
   +-----------------------------------------------------------+
   |                           TREES                           |
   +-----------------------------------------------------------+
   | id                   BIGINT (PK, AI)                      |
   | species              VARCHAR                              |
   | location_gps         VARCHAR                              |
   | date_planted         DATE                                 |
   | status               VARCHAR ('ALIVE' / 'DEAD')           |
   | plantation_drive_id  BIGINT (FK -> plantation_drives.id)  |
   | volunteer_id         BIGINT (FK -> volunteers.id)         |
   +-----------------------------------------------------------+
                                  │ 1
                                  ▼ *
   +-----------------------------------------------------------+
   |                         CHECK_INS                         |
   +-----------------------------------------------------------+
   | id                   BIGINT (PK, AI)                      |
   | check_in_date        DATE                                 |
   | status               VARCHAR ('ALIVE' / 'DEAD')           |
   | current_height       DOUBLE (Meters)                      |
   | remarks              VARCHAR                              |
   | tree_id              BIGINT (FK -> trees.id)              |
   | volunteer_id         BIGINT (FK -> volunteers.id)         |
   +-----------------------------------------------------------+
```

---

## 🖥 Early-2000s ERP Frontend Design

The client interface is crafted to replicate the tactile reliability and high-density productivity of an authentic early-2000s enterprise ERP software suite (SAP R/3, Windows Classic ERP):

* **Default Scale**: Calibrated with a default 110% root scale (`zoom: 1.1`) for high readability across modern desktop monitors.
* **Color Palette**: Olive drab headers (`#435E42`), Windows classic light grey window chrome (`#ECE9D8` / `#D4D0C8`), and dark blue highlight rows (`#0A246A`).
* **Rigid 3D Borders**: Inset (`border: 2px inset #FFF`) and outset (`border: 2px outset #FFF`) beveled borders on all inputs, table headers, and command buttons.
* **Compact Typography**: Fixed 11px `Tahoma, "MS Sans Serif", Arial, sans-serif`.
* **Zero Browser Alert Boxes**: Replaced native browser `alert()` and `confirm()` with custom centered Windows-styled 3D modal dialogs.
* **Persistent Bottom Status Bar**: Pinned multi-segment status bar displaying real-time database connection state, pending inspection counters, and transactional log messages.
* **Interactive File-Folder Tabs**:
  * **Drives & Trees**: Master tree ledger with instant drive filtering, single-row edits, and deletions.
  * **Health Check-ins Due**: Urgent inspection queue with dynamic tree inspection launchpad.
  * **Leaderboard & Stats**: Multi-column breakdown of Volunteer rankings, Drive Survival %, and Species Survival %.

---

## 🚀 Getting Started & Local Setup

### 1. Prerequisites
* **Java 21** or later (`java -version`)
* **MariaDB or MySQL Server** running locally on port `3306`

### 2. Configure Database
Ensure MariaDB is running, and create the database:
```sql
CREATE DATABASE green_log CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update `src/main/resources/application.properties` if local credentials differ:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/green_log
spring.datasource.username=root
spring.datasource.password=maria
spring.jpa.hibernate.ddl-auto=update
```

### 3. Build & Run Application
```bash
./mvnw clean spring-boot:run
```
Once started, access the ERP interface in your local browser at port `8080`.

---

## ☁️ Cloud & Container Deployment

The project is container-ready and supports zero-downtime deployment on cloud container hosts:

### 1. Multi-Stage Container Build
The included `Dockerfile` packages the application in two optimized stages:
* **Build Stage**: Maven 3.9 + Temurin JDK 21 compiles the source code.
* **Runtime Stage**: Lightweight Eclipse Temurin 21 JRE Alpine image with memory constraints (`-XX:MaxRAMPercentage=75.0`).

### 2. Dynamic Environment Variables
Cloud deployments configure database connectivity dynamically via environment variables without modifying source files:

| Environment Variable | Description |
| :--- | :--- |
| `PORT` | Dynamic HTTP port assigned by cloud host |
| `DB_URL` | JDBC connection string to cloud database with SSL mode enabled |
| `DB_USERNAME` | Cloud database administrative username |
| `DB_PASSWORD` | Cloud database secret password |

---

## 🧪 Automated Test Suite

A rigorous test suite covers both unit logic and web controller integration:

```bash
./mvnw test
```

### Key Test Coverage:
* `ServiceLayerTests.java`:
  * Verification of the **Dead Tree Rule** throwing `DeadTreeException`.
  * Volunteer tree increment logic upon planting.
  * Mathematical accuracy of species and drive survival rates.
  * Auto-detection of due check-ins.
* `ApiControllerTests.java`:
  * MockMvc tests ensuring `400 Bad Request` response payload format when attempting illegal check-ins on dead trees.
  * Proper HTTP status codes on all endpoints (`201 Created`, `200 OK`, `204 No Content`).
  * Full JSON structure validation for analytics and leaderboard outputs.

---

## 📁 Project Directory Layout

```
Green-log/
├── Dockerfile                            # Multi-stage container deployment specification
├── pom.xml                               # Maven project configuration
├── README.md                             # Technical system documentation
├── src/
│   ├── main/
│   │   ├── java/com/example/greenlog/
│   │   │   ├── GreenLogApplication.java  # Main Spring Boot Entry Point
│   │   │   ├── entity/                   # JPA Entity Models
│   │   │   │   ├── PlantationDrive.java
│   │   │   │   ├── Tree.java
│   │   │   │   ├── Volunteer.java
│   │   │   │   └── CheckIn.java
│   │   │   ├── repository/               # Spring Data Repositories
│   │   │   ├── service/                  # Business Logic & Rules Engine
│   │   │   │   ├── CheckInService.java   # DeadTreeException enforcement
│   │   │   │   ├── TreeService.java
│   │   │   │   ├── StatsService.java     # Real-time analytics recalculation
│   │   │   │   └── VolunteerService.java
│   │   │   ├── controller/               # REST API Endpoints
│   │   │   ├── dto/                      # Transfer Objects & Payloads
│   │   │   └── exception/                # Global Controller Advice & Custom Exceptions
│   │   └── resources/
│   │       ├── application.properties    # Dynamic port and DB configuration
│   │       └── static/                   # Early-2000s ERP Frontend
│   │           ├── index.html            # Main Single-Page ERP UI
│   │           ├── css/erp-theme.css     # 3D Windows Classic & 110% zoom styles
│   │           ├── js/app.js             # Pure JavaScript Controller
│   │           └── icons8-fruit-48.png   # Application icon
│   └── test/
│       └── java/com/example/greenlog/
│           ├── ServiceLayerTests.java    # Business logic verification
│           └── ApiControllerTests.java   # MockMvc REST API tests
```

---

## 📄 License
This project was developed for the **GreenLog Environmental Initiative** as an open-source plantation drive tracker.