package com.cht.procurementManagement.entities;

import com.cht.procurementManagement.enums.ProcurementStage;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Procurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    private String name;
    private Long quantity;
    private BigDecimal estimatedAmount;
    private String category;
    private String donorName;
    private String method;
    private String authorityLevel;
    private String priorityStatus;
    private String remarks;
    private String vendorDetails;
    private Date scheduledCommenceDate;
    private Date expectedCompletionDate;

    //added when updating
    private Long poNumber;
    private BigDecimal poValue;

// must be entered by backend

    //through status update
    private Date commencedDate;
    private Date completedDate;

    //through create, update
    private Date createdOn;
    private Date lastUpdatedOn;


//objects selected by user
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "procurement_assignedTo")
    private User assignedTo;
    @Enumerated(EnumType.STRING)
    private ProcurementStage procurementStage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procurement_status")
    private ProcurementStatus status;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "procurement_vendor")
    private Vendor vendor;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne
    @JoinColumn(name = "procurement_source")
    private ProcurementSource source;

    //owning side is Request
//    @OneToMany(fetch = FetchType.EAGER, mappedBy = "procurement")
//    private List<Request> requestList;

//objects set by backend
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "procurement_createdBy")
    private User createdBy;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "procurement_lastUpdatedBy")
    private User lastUpdatedBy;


    //mapped
    @OneToMany(
            mappedBy = "procurement",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ProcurementStatusUpdate> statusUpdates = new ArrayList<>();





//get-set methods


    public Long getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(Long poNumber) {
        this.poNumber = poNumber;
    }

    public BigDecimal getPoValue() {
        return poValue;
    }

    public void setPoValue(BigDecimal poValue) {
        this.poValue = poValue;
    }

    public List<ProcurementStatusUpdate> getStatusUpdates() {
        return statusUpdates;
    }

    public void setStatusUpdates(List<ProcurementStatusUpdate> statusUpdates) {
        this.statusUpdates = statusUpdates;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getVendorDetails() {
        return vendorDetails;
    }

    public void setVendorDetails(String vendorDetails) {
        this.vendorDetails = vendorDetails;
    }

    public Date getScheduledCommenceDate() {
        return scheduledCommenceDate;
    }

    public void setScheduledCommenceDate(Date scheduledCommenceDate) {
        this.scheduledCommenceDate = scheduledCommenceDate;
    }

    public Date getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(Date expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }

    public Date getCommencedDate() {
        return commencedDate;
    }

    public void setCommencedDate(Date commencedDate) {
        this.commencedDate = commencedDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Date lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public ProcurementStage getProcurementStage() {
        return procurementStage;
    }

    public void setProcurementStage(ProcurementStage procurementStage) {
        this.procurementStage = procurementStage;
    }

    public ProcurementStatus getStatus() {
        return status;
    }

    public void setStatus(ProcurementStatus status) {
        this.status = status;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public ProcurementSource getSource() {
        return source;
    }

    public void setSource(ProcurementSource source) {
        this.source = source;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }
}
