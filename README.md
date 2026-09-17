Backend of the procurement management web application, developed with Spring Boot, JWT, and RBAC.

🟩 [Checkout the Frontend repo](https://github.com/chtlansakara/Supplies-FE)


<table  >
<tr>
<td style="vertical-align:middle;">
<img height="100%"  alt="Supplies logo" src="https://github.com/user-attachments/assets/f215d0bc-52bf-43e3-a76f-dd3492cc1fcd" />
</td>
<td style="vertical-align:middle;">
<h1>Supplies Web Application — A Procurement Management System</h1>
</td>
</tr>
</table>

A digital, role-based procurement workflow for an institution which contains many admin-divisions, where each admin-division has multiple sub-divisions, and all the procurement related activities
are managed by a single Supplies division for the whole institution.
---

<i>This application was built with Angular frontend, Spring Boot REST API, MySQL relational schema, role-based security, and PDF/Excel report generation.</i>

 ---

### Key Features
- 🔐 **Role-based access control** — four distinct roles (Subdivision, Administrative Division, Supplies Division, Admin), each scoped to only their own data
- 📝 **Digital request submission** — subdivisions submit procurement requests through validated web forms instead of paper letters
- ✅ **Multi-level approval workflow** — requests move subdivision → administrative division → supplies division, with conditional approval logic enforced server-side
- 📊 **Full procurement lifecycle tracking** — real-time status from request → approval → procurement number assignment → completion
- 🧾 **Automated report generation** — Procurement Request, Progress, and Summary reports generated as **PDF and Excel (.xlsx)** via JasperReports
- 🕵️ **Automated audit logging** — every status change and deletion is logged automatically, no manual record-keeping
- 🔔 **Notification module** — keeps requesting divisions informed as their request moves through the workflow
- 🔒 **JWT-based authentication & authorization** — Spring Security enforces access at the API layer, not just the UI

  ---

### Tech Stack
 
| Layer | Technology |
|---|---|
| Frontend | Angular 19, Angular Material, TypeScript, HTML, CSS |
| Backend | Spring Boot, Spring Security (JWT), Java |
| Database | MySQL 8, Spring Data JPA / Hibernate |
| Reporting | JasperReports (PDF & XLSX generation) |
| Testing | JUnit, Mockito, Jasmine, Karma, Postman (automated API tests) |
| Build tools | Maven, npm, Angular CLI |
| Version control | Git & GitHub |
 
**Architecture:** Three-tier — Angular presentation layer → Spring Boot application layer (REST API + business logic) → MySQL data layer. 
 
<!-- 
---
### Demo
-->
---

### The Problem Addressed

Processing procurement through physical letters, ledgers, and scattered spreadsheets, passed by hand across three approval levels — ***subdivision → administrative division → supplies division*** — is extremely inefficient for an institution.

Also, no existing commercial procurement tool is built with a **simple, accessible UI** designed for office clerks with limited IT literacy.

This system replaces that manual process with a web application that provides digital request submission, automated role-based routing, real-time status tracking, and one-click report generation.


 ---

### System Architecture
<img width="562"  alt="image" src="https://github.com/user-attachments/assets/f66e9f02-caea-4160-8425-7838a9dc1e19" />

---

### Organizational Approval Workflow for Procurement Requests
<img width="562"  alt="image" src="https://github.com/user-attachments/assets/0aace852-e2a1-42c1-9a2c-3bd7e8296b56" />

### Core Functionalities 
<ul>
  <h5>Procurement Requests</h5>
  <li>Sub-division users can create procurement requests for the sub-division.</li>
  <li>Relevant admin-division for the sub-division can then approve or decline the procurement request.</li>
  <li>Approved procurement requests by the admin-division, can be approved or declined by the Supplies division.</li>
  <h5>Procurement</h5>
  <li>A procurement can be created for approved procurement requests by Supplies division users.</li>
  <li>Each procurement created must be assigned to a Supplies division user.</li>
  <li>Only the assigned Supplies division user can update the status of the procurement throughout the procurement process.</li>
  <li>Related procurement documents can be attached to each procurement as PDF documents.</li>
  <h5>Procurement Reports</h5>
  <li>Procurement reports can be generated as progress reports and summary reports as PDF or XLSX documents.</li>
  <li>Procurement detailed reports can be generated as XLSX or CSV documents, only by Supplies Division users.</li>
  <h5>Admin Tasks </h5>
  <li>Admin users can manage application details such as users, designations, admin-divisions, sub-divisions, vendors, procurement sources, and procurement status.</li>
  <li>Admin users can create backups and restore application when required.</li>
  <h5>Logs </h5>
  <li>An audit log is automatically generated for all the Procurement related activities.</li>
</ul>



---

### Testing
 
The system was validated with a layered testing strategy:
 
- **Unit testing** — JUnit + Mockito (backend), Jasmine + Karma (frontend)
- **Component & integration testing**
- **Automated API testing** — Postman
- **Performance testing** — Postman Runner
- **System testing** — end-to-end role-based workflow validation
- **User evaluation** — task completion, interface clarity, and functionality assessed with real supplies division staff
---

### Screenshots
 
| | |
|---|---|
| **Login** |<img width="1117" height="702" alt="image" src="https://github.com/user-attachments/assets/4a19f40f-7b7d-45fd-9ef4-03e38a1a4e1a" />|
| **Create Request (Subdivision User)** | <img width="1118" height="908" alt="image" src="https://github.com/user-attachments/assets/4e3d3cb0-fa51-4c15-af37-4c890a7cc02b" />|
| **View Requests (Subdivision User)** | <img width="1122" height="935" alt="image" src="https://github.com/user-attachments/assets/c6731823-d16d-4098-9d21-fa091167c97b" />|
| **Review & Approve (Administrative Division)** | <img width="1041" height="842" alt="image" src="https://github.com/user-attachments/assets/da323b85-b827-4e93-a5c0-c44c574880c6" />|
| **Approve Request (Supplies Division)** | <img width="1913" height="910" alt="image" src="https://github.com/user-attachments/assets/849e0273-25d6-4e88-b370-ae2cbbd2d5fd" />|
| **View Procurement (Supplies Division User)** | <img width="1910" height="945" alt="image" src="https://github.com/user-attachments/assets/09487973-57f5-45e5-b727-92f7a12fe54e" />|
| **Create Procurement (Supplies Division User)** | <img width="1908" height="898" alt="image" src="https://github.com/user-attachments/assets/179eeae8-b0a8-487e-b31d-da2f4cf186ea" />|
| **View Procurement Status (Supplies Division User)** | <img width="1681" height="802" alt="image" src="https://github.com/user-attachments/assets/7a10bbeb-1ae8-4601-9a44-a8106bd58a72" />|
| **Generate Report (PDF / Excel)** |<img width="1912" height="926" alt="image" src="https://github.com/user-attachments/assets/838f6630-ecf0-4dc8-8107-5a86a80841f9" />|
| **View Audit Log (Supplies Division User)** | <img width="1907" height="944" alt="image" src="https://github.com/user-attachments/assets/1033a76c-fad4-49f5-97ab-105fb0e650d5" /> |
 
<br>
<hr>





