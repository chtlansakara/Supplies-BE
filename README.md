Backend of the procurement management web application developed with Spring Boot Framework with JWT, and RBAC.


<img width="185" height="65" alt="image" src="https://github.com/user-attachments/assets/f215d0bc-52bf-43e3-a76f-dd3492cc1fcd" />
</br>

<h2>Supplies Web Application</h2>
<i>Can be used for procurement management in an institution which contains many admin-divisions, and each admin-division has multiple sub-divisions, where all the procurement related activities
are managed by a single Supplies division for the whole institution.</i>

<h4>User Roles</h4> 
<ul>
  <li>Admin</li>
  <li>Supplies Division User</li>
  <li>Admin Division User</li>
  <li>Sub-division User</li>
</ul>

<br>
<hr>

<h4>Core Functionalities</h4> 
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

