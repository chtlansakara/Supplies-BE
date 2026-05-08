package com.cht.procurementManagement.controllers.admin;

import com.cht.procurementManagement.dto.UserDto;
import com.cht.procurementManagement.enums.AuditEntityType;
import com.cht.procurementManagement.services.AuditLog.AuditLogService;
import com.cht.procurementManagement.services.admin.AdminService;
import com.cht.procurementManagement.services.auth.AuthService;
import com.cht.procurementManagement.services.requests.RequestService;
import com.cht.procurementManagement.utils.DatabaseBackupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {
    //injecting service class
    private final AdminService adminService;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final RequestService requestService;
    private final DatabaseBackupService databaseBackupService;
    public AdminController(AdminService adminService,
                           AuthService authService,
                           AuditLogService auditLogService,
                           RequestService requestService,
                           DatabaseBackupService databaseBackupService) {
        this.adminService = adminService;
        this.authService = authService;
        this.auditLogService = auditLogService;
        this.requestService = requestService;
        this.databaseBackupService = databaseBackupService;
    }

    //backup db
    @PostMapping("/backup")
    public ResponseEntity<String> backup(){
        try{
            databaseBackupService.backup();
            return ResponseEntity.ok("Backup is successful");
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("Backup failed: "+ e.getMessage());
        }
    }

    //restore db

    @PostMapping("/restore")
    public ResponseEntity<String> restore() {
        try {
            databaseBackupService.restore();
            return ResponseEntity.ok("Restoring is successful");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Restore failed: " + e.getMessage());
        }
    }

        //get users list
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(){
        return ResponseEntity.ok(adminService.getUsers());
    }

    //create user
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@ModelAttribute UserDto userDto){
        //validate the file input
        if(userDto.getRecommendationFile() != null && !userDto.getRecommendationFile().isEmpty()){
            if(userDto.getRecommendationFile().getSize() > 5 * 1024 * 1024){
                throw new IllegalArgumentException("File size should be less than 5MB");
            }
            if(!Objects.equals(userDto.getRecommendationFile().getContentType(), "application/pdf")){
                throw new IllegalArgumentException("Only PDF files can be uploaded");
            }
        }

        UserDto createdUserDto = null;
        try {
            createdUserDto = adminService.createUser(userDto);
        } catch (IOException e) {
            throw new RuntimeException("Error creating new user : IO Exception!");
        }

        //check if created
        if(createdUserDto == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User couldn't be created");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDto);
    }


    //get user by id
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(adminService.getUserById(id));
    }


    @PostMapping(value= "/users/{id}",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestPart("user") UserDto userDto,
                                              @RequestPart(value = "recommendationFile", required = false) MultipartFile file) throws IOException {
        //validate the file input
        if(file!=null){
            if(!file.isEmpty()) {
                if (file.getSize() > 5 * 1024 * 1024) {
                    throw new RuntimeException("File size should be less than 5MB");
                }
                if (!Objects.equals(file.getContentType(), "application/pdf")) {
                    throw new RuntimeException("Only PDF files can be uploaded");
                }
            }

            //update userDto with file
            userDto.setRecommendationFile(file);
        }

        UserDto updatedUserDto = adminService.updateUser(id, userDto);
        if(updatedUserDto == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUserDto);
    }


    //delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        adminService.deleteUser(id);
        return ResponseEntity.ok(null);
    }


    //delete audit logs for a procurement
    @DeleteMapping("/procurement/auditLog/{id}")
    public ResponseEntity<Void> deleteAuditLog(@PathVariable Long id){
        auditLogService.deleteAuditlogs(AuditEntityType.PROCUREMENT, id);
        return ResponseEntity.ok(null);
    }

    //delete requests (force delete in testing)
    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> deleteRequestByForce(@PathVariable Long id){
        requestService.deleteRequestById(id);
        return ResponseEntity.ok(null);
    }

}
