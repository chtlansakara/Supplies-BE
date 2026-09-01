package com.cht.procurementManagement.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class DatabaseBackupService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${app.database.name}")
    private String dbName;
    @Value("${app.backup.file_name}")
    private String fileName;

    @Value("${app.backup.dir}")
    private String backupDir;

    //automatic backup on 7PM every friday per week
    @Scheduled(cron = "0 0 19 * * FRI")
    public void scheduledBackup(){
        try{
            backup();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //saving in the same backup file
    private String getBackupFilePath(){
        return backupDir + fileName;
    }


    public void backup() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-u" + dbUsername,
                "-p" + dbPassword,
                "--add-drop-table",
                dbName,
                "-r",
                getBackupFilePath()
        );

        Process process = pb.start();
        String errors = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Backup failed: " + errors);
        }

    }

    public void restore() throws Exception{
        ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-u"+dbUsername,
                "-p"+dbPassword,
                dbName
        );

        pb.redirectInput(new File(getBackupFilePath()));

        Process process = pb.start();
        String errors = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();

        if(exitCode != 0){
            throw new RuntimeException("Restore failed: " +errors);
        }
    }





}
