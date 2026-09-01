package com.cht.procurementManagement.controllers.admin;

import com.cht.procurementManagement.services.report.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminReportController {
    private ReportService reportService;
    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/report/users/{format}")
    public String generateUsersReportWFormat(@PathVariable String format) {
        try {
            return reportService.exportReport(format);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
