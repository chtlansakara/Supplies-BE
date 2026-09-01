package com.cht.procurementManagement.dto.procurement;

import java.math.BigDecimal;
import java.util.Date;

public class ProcurementReportDTO {
    private Long id;
    private String name;
    private Long quantity;
    private String category;
    private String method;
    private String authorityLevel;
    private String priorityStatus;
    private Date commencedDate;
    private String sourceName;
    private String procurementStage;
    private BigDecimal estimatedAmount;
    private Long requestId;
    private String adminDivision;

//    private String subdivisions;

    public ProcurementReportDTO() {
    }

    public ProcurementReportDTO(Long id, String name, Long quantity, String category, String method, String authorityLevel, String priorityStatus, Date commencedDate, String sourceName, String procurementStage, BigDecimal estimatedAmount,Long requestId, String adminDivision) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.method = method;
        this.authorityLevel = authorityLevel;
        this.priorityStatus = priorityStatus;
        this.commencedDate = commencedDate;
        this.sourceName = sourceName;
        this.procurementStage = procurementStage;
        this.estimatedAmount = estimatedAmount;
        this.requestId = requestId;
        this.adminDivision = adminDivision;
    }

//getters and setters

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }




    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAuthorityLevel() {
        return authorityLevel;
    }

    public void setAuthorityLevel(String authorityLevel) {
        this.authorityLevel = authorityLevel;
    }

    public String getPriorityStatus() {
        return priorityStatus;
    }

    public void setPriorityStatus(String priorityStatus) {
        this.priorityStatus = priorityStatus;
    }

    public Date getCommencedDate() {
        return commencedDate;
    }

    public void setCommencedDate(Date commencedDate) {
        this.commencedDate = commencedDate;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getProcurementStage() {
        return procurementStage;
    }

    public void setProcurementStage(String procurementStage) {
        this.procurementStage = procurementStage;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public String getAdminDivision() {
        return adminDivision;
    }

    public void setAdminDivision(String adminDivision) {
        this.adminDivision = adminDivision;
    }
}
