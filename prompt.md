PROJECT: GreenLog — Tree Plantation Drive Tracker (Full Stack)

ROLE: You are my AI coding assistant (Antigravity CLI) working inside my IntelliJ project. I am a beginner-level developer learning as I build this. Follow the instructions below exactly.

====================================================
0. FIRST STEP — UNDERSTAND CURRENT PROJECT
====================================================
Before writing or changing anything, inspect my current project setup:
- Folder structure (Spring Boot + Maven: package com.example.greenlog)
- pom.xml (Dependencies: Spring Web, Spring Data JPA, Validation, Lombok, MariaDB Driver)
- application.properties
Tell me what you found. Then ask me for my MariaDB connection details before proceeding.

====================================================
1. PROJECT SCOPE & GRADING RUBRIC (STRICT)
====================================================
Build ONLY what is described below. This is an academic project graded strictly on technical implementation, service-layer logic, and clean error handling. 
- Backend: Spring Boot, MariaDB, Java.
- Frontend: Plain HTML, CSS, Vanilla JS placed in `src/main/resources/static/`. NO modern frameworks. UI must strictly match the "Early 2000s Enterprise ERP" style defined below.
- Do not add authentication systems, third-party APIs, or extra entities.

====================================================
2. DATABASE ENTITIES (MariaDB via Spring Data JPA)
====================================================
Create these exact entities with proper relationships (@OneToMany, @ManyToOne):
1. PlantationDrive (id, name, location, date)
2. Tree (id, species, location_gps, date_planted, status, plantation_drive_id)
3. Volunteer (id, name, total_trees_planted)
4. CheckIn (id, check_in_date, status_reported (Alive/Dead), tree_id, volunteer_id)

====================================================
3. STRICT BUSINESS RULES (Must be enforced in Service Layer)
====================================================
1. "Dead Tree" Rule: A tree marked 'DEAD' in a check-in cannot receive further check-ins. The service layer MUST block this and throw a custom exception before hitting the DB.
2. Auto-Updating Stats: Survival rate must be recalculated automatically whenever a new check-in is recorded.
3. Global Error Handling: Use @ControllerAdvice and Validation (@NotNull, @Positive). Bad requests MUST return a clean JSON error message with the correct HTTP status code (e.g., 400 Bad Request), NOT a Java stack trace.

====================================================
4. REST APIs (Core Endpoints + Full CRUD)
====================================================
Provide full CRUD (Create, Read, Update, Delete) for all entities so the frontend can manage the data fully, PLUS these 5 specific grading endpoints:
1. POST: Log a new plantation entry with species, location, and date.
2. POST: Submit a survival check-in (alive/dead).
3. GET: View survival rate per drive and per species.
4. GET: List trees due for their next check-in.
5. GET: Generate a leaderboard of volunteers by trees planted.

====================================================
5. FRONTEND UI THEME — EARLY 2000s ENTERPRISE ERP STYLE
====================================================
Apply this visual style strictly to the frontend.
- Tech: Plain HTML, CSS, and Vanilla JS ONLY. NO React, Tailwind, or Bootstrap.
- Overall Aesthetic: Classic early-2000s enterprise software, legacy ERP system, or Java Swing desktop application. NO modern flat design.
- Typography: Classic system fonts (Tahoma, Arial), small dense text (11px-13px).
- Top Application Bar: Dark olive green (e.g., #435E42) with bold white text for the App Title.
- Main Background: Pale beige, off-white, or light grey (e.g., #EEEEEE).
- UI Elements: Use thin grey borders (#CCCCCC) and classic 3D beveled button edges. Structure must look rigid.
- Page Layout: 
  - Menu Bar: Classic dropdowns directly below the title bar (File, View, Tools, Help).
  - Action Toolbar: Light grey strip with primary action buttons ("+ New", "Refresh All").
  - KPI Row: Rectangular summary cards (Total Trees, Active Drives, Overall Survival Rate) with thin borders.
  - Navigation: File-folder style tabs to switch between app sections.
- Tables: Dense, rigid tabular layouts. Light tan gradient headers, alternating white/pale-beige rows.
- Dialog Boxes (Modals): All data entry/alerts happen inside centered popup windows. Dark green title bars with a classic red 'X' close button. Classic "OK" / "Cancel" buttons at the bottom.

====================================================
6. FRONTEND TABS & INTEGRATION
====================================================
The UI must map directly to the backend APIs:
- "Drives & Trees" Tab: Table of trees. Context menus/buttons to Add, Edit, or Delete trees (Dialog modals).
- "Health Check-ins" Tab: Table of trees due for check-ins. Dialog popup to submit 'Alive' or 'Dead'. If the backend blocks it (dead tree rule), show a classic error popup displaying the backend's clean error message.
- "Leaderboard & Stats" Tab: Tables showing the Volunteer Leaderboard and calculated survival rates.

====================================================
7. HOW I WANT YOU TO WORK WITH ME
====================================================
Build in STAGES, in this exact order. Do not skip ahead or combine stages:
  Stage 1: Database connection setup + JPA Entities & Relationships.
  Stage 2: Repositories & strict Service Layer logic (Custom exceptions, Dead tree rule).
  Stage 3: REST Controllers (The 5 core APIs + CRUD) & Global Exception Handler.
  Stage 4: Frontend Base Shell (Apply theme: toolbars, metric cards, tab system).
  Stage 5: "Drives & Trees" UI Tab (Fetch data, Add/Edit/Delete forms).
  Stage 6: "Health Check-ins" UI Tab (Submission form, error popup integration).
  Stage 7: "Leaderboard & Stats" UI Tab.

After each stage:
- Tell me exactly what manual step I need to take (e.g., "restart server", "run Maven", "open localhost:8080").
- Give me a clear, SHORT explanation of what we just built.
- Wait for my confirmation before moving to the next stage.

Confirm you understand this full scope, then tell me what you need from me to begin Stage 1.