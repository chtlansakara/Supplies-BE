package com.cht.procurementManagement.services.procurement;

import com.cht.procurementManagement.dto.*;
import com.cht.procurementManagement.dto.procurement.ProcurementCreateDto;
import com.cht.procurementManagement.dto.procurement.ProcurementResponseDto;
import com.cht.procurementManagement.dto.procurement.ProcurementStatusUpdateDto;
import com.cht.procurementManagement.entities.*;
import com.cht.procurementManagement.enums.*;
import com.cht.procurementManagement.mappers.ProcurementMapper;
import com.cht.procurementManagement.repositories.*;
import com.cht.procurementManagement.services.attachment.AttachmentService;
import com.cht.procurementManagement.services.auth.AuthService;
import com.cht.procurementManagement.services.notification.NotificationService;
import com.cht.procurementManagement.utils.AuditService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class ProcurementServiceImpl implements ProcurementService{
    private final ProcurementMapper procurementMapper;
    private final UserRepository userRepository;
    private final ProcurementStatusRepository procurementStatusRepository;
    private final VendorRepository vendorRepository;
    private final RequestRepository requestRepository;
    private final AuthService authService;
    private final ProcurementRepository procurementRepository;
    private final AuditService auditService;

    private final ProcurementStatusUpdateRepository procurementStatusUpdateRepository;
    private final ProcurementSourceRepository procurementSourceRepository;
    private final NotificationService notificationService;

    private final AttachmentService attachmentService;


    public ProcurementServiceImpl(ProcurementMapper procurementMapper,
                                  UserRepository userRepository,
                                  ProcurementStatusRepository procurementStatusRepository,
                                  VendorRepository vendorRepository,
                                  RequestRepository requestRepository,
                                  AuthService authService,
                                  ProcurementRepository procurementRepository,
                                  AuditService auditService,
                                  ProcurementStatusUpdateRepository procurementStatusUpdateRepository,
                                  ProcurementSourceRepository procurementSourceRepository, NotificationService notificationService, AttachmentService attachmentService) {
        this.procurementMapper = procurementMapper;
        this.userRepository = userRepository;
        this.procurementStatusRepository = procurementStatusRepository;
        this.vendorRepository = vendorRepository;
        this.requestRepository = requestRepository;
        this.authService = authService;
        this.procurementRepository = procurementRepository;
        this.auditService = auditService;
        this.procurementStatusUpdateRepository = procurementStatusUpdateRepository;
        this.procurementSourceRepository = procurementSourceRepository;
        this.notificationService = notificationService;
        this.attachmentService = attachmentService;
    }

    @Override
    public List<UserDto> getAssignedToUsersList() {
        //only users of supplies division must be in the list
        return userRepository.findAll()
                .stream()
                .filter(user-> user.getUserRole() == UserRole.SUPPLIESUSER)
                .sorted(Comparator.comparing(User::getEmail))
                .map(User::getUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorDto> getVendorsList() {
        return vendorRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Vendor::getName))
                .map(Vendor::getVendorDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcurementStatusDto> getProcurementStatusList() {
        //not sending the first status when updating the status
        return procurementStatusRepository.findAll()
                .stream()
//                .filter(status-> status.getId() != 2)
                .sorted(Comparator.comparing(ProcurementStatus::getName))
                .map(ProcurementStatus::getProcurementStatusDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getProcurmentStagesList() {
        return Arrays.stream(ProcurementStage.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Override
    public List<ProcurementSourceDto> getProcurementSources(){
        return procurementSourceRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ProcurementSource::getName))
                .map(ProcurementSource::getdto)
                .collect(Collectors.toList());
    }



//Procurement CRUD:

    @Override
    @Transactional
    public ProcurementResponseDto createProcurement(ProcurementCreateDto createDto) {
        //1. create procurement from dto -using mapper method
        Procurement procurement = procurementMapper.dtoToProcurement(createDto);

        //2. find objects from ids in dto
            //i. fetch assignedTo User- User

            User assignedTo = userRepository.findById(createDto.getAssignedToUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));
            //check if the user is a supplies user
            if(!assignedTo.getUserRole().equals(UserRole.SUPPLIESUSER)){
                throw new RuntimeException("Assigned user must be a supplies user");
            }

            //set
            procurement.setAssignedTo(assignedTo);

            //ii. fetch Vendor- Vendor
        if(createDto.getVendorId() != null){
            Vendor vendor = vendorRepository.findById(createDto.getVendorId())
                    .orElseThrow(()-> new EntityNotFoundException("Vendor not found"));
            //set
            procurement.setVendor(vendor);
        }
        //check for source
        ProcurementSource procurementSource = procurementSourceRepository.findById(createDto.getSourceId())
                    .orElseThrow(() -> new EntityNotFoundException("Source not found"));

        //set source
        procurement.setSource(procurementSource);



            //iii. assign Requests - List-Request
        //find if sent request list is empty
        if(createDto.getRequestId() == null ) {
            throw new RuntimeException("Request is empty");
        }
        //validate request ids - using class method
        Request validatedRequest= validateAsValidRequest(createDto.getRequestId(),true);
        //set
        procurement.setRequest(validatedRequest);


        //3.find and assign other backend objects
            //i. set procurement stage
        procurement.setProcurementStage(ProcurementStage.PROCUREMENT_PROCESS_NOT_COMMENCED);

            //ii. assign createdBy -User
        User loggedUser = getLoggedUser();
            //set
        procurement.setCreatedBy(loggedUser);

        procurement.setLastUpdatedBy(loggedUser);

            //iii. assign createdOn - Date
        procurement.setCreatedOn(new Date());

        //6.save Procurement to db
        Procurement savedProcurement = procurementRepository.save(procurement);


        //7. change requests status
        validatedRequest.setStatus(RequestStatus.PROCUREMENT_CREATED);
        requestRepository.save(validatedRequest);


        //create audit string
        StringBuilder auditDetails = new StringBuilder();

        auditDetails.append("Created procurement ID:").append(savedProcurement.getId()).append(" - ");
        auditDetails.append(savedProcurement.getName());
        auditDetails.append(" (request ID:").append(createDto.getRequestId()).append(")");

        //add vendor info - only if there is
        if(createDto.getVendorId() != null){
            auditDetails.append(", with vendor ID:").append(createDto.getVendorId());
            auditDetails.append(" and name: ").append(savedProcurement.getVendor().getName());
        }

        //8. Add to audit log
        auditService.log(
                loggedUser.getEmail(),
                loggedUser.getEmployeeId(),
                AuditAction.CREATED,
                AuditEntityType.PROCUREMENT,
                savedProcurement.getId(),
                auditDetails.toString()
        );

        //send notifications
        notificationService.onProcurementCreation(savedProcurement, validatedRequest);

        //7.return as dto
        return procurementMapper.toResponseDto(savedProcurement);
    }

    //get procurement list
    @Override
    public List<ProcurementResponseDto> getProcurement() {
         List<Procurement> procurements = procurementRepository.findAll()
                 .stream()
                 .sorted(Comparator.comparing(Procurement::getId).reversed())
                 .collect(Collectors.toList());
         return procurementMapper.toResponseDtoList(procurements);
    }

    @Override
    public ProcurementResponseDto getProcurementById(Long id) {
        Procurement procurement = procurementRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Procurement not found"));
        return procurementMapper.toResponseDto(procurement);
    }

    @Override
    @Transactional
    public ProcurementResponseDto updateProcurement(Long id, ProcurementCreateDto createDto) {
        //1. find the procurement - using class method
        Procurement existingProcurement = validateToUpdateDeleteProcurement(id);

        //storing existing information
        Long oldRequestId = existingProcurement.getRequest().getId();
        String procurementName = existingProcurement.getName();
        //finding existing vendor name & id (for audit log)
        boolean isVendorChanged = false;
        String oldVendorInfo = null;

        //2. find other objects from ids in dto
            //i. fetch new assignedTo - User
        if(createDto.getAssignedToUserId()!= null) {
            User assignedTo = userRepository.findById(createDto.getAssignedToUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Assigned User not found"));
            //check if the user is a supplies user
            if (!assignedTo.getUserRole().equals(UserRole.SUPPLIESUSER)) {
                throw new RuntimeException("Assigned user must be a supplies user");
            }

            //set
            existingProcurement.setAssignedTo(assignedTo);
        }

        //ii. fetch Vendor- Vendor

        //for audit log
        if(existingProcurement.getVendor() != null){
            Vendor oldVendor = existingProcurement.getVendor();
            oldVendorInfo =  " and vendor with ID:" + oldVendor.getId() + " with name: "+ oldVendor.getName();
        }

        if(createDto.getVendorId() != null){
            //for audit log
            if(existingProcurement.getVendor() != null && !createDto.getVendorId().equals(existingProcurement.getVendor().getId())){
                isVendorChanged = true;
            }

            Vendor vendor = vendorRepository.findById(createDto.getVendorId())
                    .orElseThrow(()-> new EntityNotFoundException("Vendor not found"));
            //set
            existingProcurement.setVendor(vendor);
        }


        //check for source
        if(createDto.getSourceId()!= null && !createDto.getSourceId().equals(existingProcurement.getSource().getId())) {
            ProcurementSource procurementSource = procurementSourceRepository.findById(createDto.getSourceId())
                    .orElseThrow(() -> new EntityNotFoundException("Source not found"));

            //set source
            existingProcurement.setSource(procurementSource);
        }

        //iii. assign Request
        //find if sent request is null
        if(createDto.getRequestId() == null ) {
            throw new RuntimeException("Request is empty");
        }
        //validate request id - using class method
        Request requestNew= validateAsValidRequest(createDto.getRequestId(),false);

        //if requests in the list has 'procurement created' then it should be equal to the one already in the procurement
        //i. get already procurement request list
        Request requestOld =  existingProcurement.getRequest();
        if(requestNew.getStatus().equals(RequestStatus.PROCUREMENT_CREATED)){
            if(!requestNew.getId().equals(oldRequestId)){
                throw new RuntimeException("Can not select requests in other procurement");
            }
        }

        //set
        existingProcurement.setRequest(requestNew);

        //4. update with details
        procurementMapper.updateProcurementWithDto(existingProcurement,createDto);


        //5. set updated details
        Long loggedUserId = authService.getLoggedUserDto().getId();
        User updatedBy = userRepository.findById(loggedUserId)
                .orElseThrow(()-> new EntityNotFoundException("Current user not found"));
        existingProcurement.setLastUpdatedBy(updatedBy);
        existingProcurement.setLastUpdatedOn(new Date());

        //6. save to db
        Procurement updatedProcurement = procurementRepository.save(existingProcurement);
        //save request status
        if(!requestNew.getId().equals(oldRequestId)){
            requestNew.setStatus(RequestStatus.PROCUREMENT_CREATED);
            requestOld.setStatus(RequestStatus.PENDING_PROCUREMENT);
            //save to db
            requestRepository.save(requestNew);
            requestRepository.save(requestOld);
        }


        //7.create audit log
        StringBuilder auditDetails = new StringBuilder();
        auditDetails.append("Updated procurement ID:").append(id).append(" - ");
        auditDetails.append(procurementName);
        auditDetails.append(" (request ID:").append(oldRequestId).append(")");
        //add update request info - only if the request is changed
        if(!requestNew.getId().equals(oldRequestId)){
            auditDetails.append(", changed to request ID:").append(createDto.getRequestId());
        }
        //add previous vendor info - only if there was one
        if(oldVendorInfo!=null){
            auditDetails.append(oldVendorInfo);
        }
        //add new vendor info - only if there is
        if(createDto.getVendorId() != null){
            if(isVendorChanged){
                auditDetails.append(", updated to vendor with ID:").append(createDto.getVendorId());
                auditDetails.append(" and name: ").append(existingProcurement.getVendor().getName());
            }else{
                auditDetails.append(", with vendor ID:").append(createDto.getVendorId());
                auditDetails.append(" and name: ").append(existingProcurement.getVendor().getName());
            }
        }



        auditService.log(
                updatedBy.getEmail(),
                updatedBy.getEmployeeId(),
                AuditAction.UPDATED,
                AuditEntityType.PROCUREMENT,
                updatedProcurement.getId(),
                auditDetails.toString()

        );

        //8.return as response dto
        return procurementMapper.toResponseDto(updatedProcurement);
    }

    //for update form - we need request list with either approved or in procurement
    @Transactional
    @Override
    public List<RequestDto> getRequestsForUpdateProcurement() {
        return requestRepository.findAll()
                .stream()
                .filter(request ->
                request.getStatus().equals(RequestStatus.PROCUREMENT_CREATED)
                        || request.getStatus().equals(RequestStatus.PENDING_PROCUREMENT))
                .sorted(Comparator.comparing(Request::getId).reversed())
                .map(Request::getRequestDto)
                .collect(Collectors.toList());
    }


    //on status update - create new object 'ProcurementStatusUpdate' & change status in 'Procurement'
    @Transactional
    @Override
    public ProcurementStatusUpdateDto updateStatus(Long procurementId, ProcurementStatusUpdateDto statusUpdateDto) {
        //for audit log
        ProcurementStatus newStatus = null;
        String oldStatus = null;
        boolean isStageUpdated = false;

        //1. find procurement to update
        Procurement existingProcurement = procurementRepository.findById(procurementId)
                .orElseThrow(()-> new EntityNotFoundException("Procurement not found"));

        //2. to update the assigned user can not be null
        if(existingProcurement.getAssignedTo() == null){
            throw new RuntimeException("To update status procurement should have an assigned employee");
        }

        //3. Check if the user logged is allowed to update status
        //should be the one  assigned
        Long loggedUserId = authService.getLoggedUserDto().getId();
        //since assigned user could be null
        Long assignedUserId = existingProcurement.getAssignedTo().getId();
        if(!loggedUserId.equals(assignedUserId)){
            throw new RuntimeException("The procurement is not allowed to be updated by this user!");
        }

        //extra - check if procurement is already completed
        if(existingProcurement.getCompletedDate() != null){
            throw new RuntimeException("Can not update status as the procurement is already completed.");
        }

        //4. find the status
        //find stage selected
        if(statusUpdateDto.getProcurementStage() == null){
            throw new RuntimeException("The stage should be selected");
        }

        //save previous values for audit log before updating (for audit log)
        String previousStage = existingProcurement.getProcurementStage().toString();
        if(!previousStage.equals(statusUpdateDto.getProcurementStage())) isStageUpdated = true;

    //taking care of status added
        if(statusUpdateDto.getProcurementStatusId() != null) {
            //if existing stage and status is selected again
            if(existingProcurement.getStatus() != null && existingProcurement.getStatus().getId().equals(statusUpdateDto.getProcurementStatusId())) {
                throw new RuntimeException("Can not select the same status.");
            }

            ProcurementStatus changedStatus = procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId())
                    .orElseThrow(() -> new EntityNotFoundException("Status selected not found"));

            if(existingProcurement.getStatus() != null){
                oldStatus = existingProcurement.getStatus().getName();
            }

            //change
            existingProcurement.setStatus(changedStatus);
            //save to method variable
            newStatus = changedStatus;
        }else{
            //remove status if not added to update-status
            existingProcurement.setStatus(null);
        }

        //extra - check if the stage changed to completed - add completed date to procurement
        boolean isCompletedStage = statusUpdateDto.getProcurementStage().equals(ProcurementStage.PAID_AND_COMPLETED.toString());
        //set completed date of procurement if it is
        if(isCompletedStage){
            Date completedDate = statusUpdateDto.getStatusChangedOn() != null ?
                    statusUpdateDto.getStatusChangedOn() : new Date();
            existingProcurement.setCompletedDate(completedDate);
        }

        //extra - check if status is changed to commenced - add commenced date to procurement
        boolean isCommencedStage = statusUpdateDto.getProcurementStage().equals(ProcurementStage.PURCHASE_PROCESS_COMMENCED.toString());
        //set completed date of procurement if it is
        if(isCommencedStage){
            Date commencedDate = statusUpdateDto.getStatusChangedOn() != null ?
                    statusUpdateDto.getStatusChangedOn() : new Date();
            existingProcurement.setCommencedDate(commencedDate);
        }

        //5. find the updating user
        User updatedBy = userRepository.findById(loggedUserId)
                .orElseThrow(()-> new EntityNotFoundException("Logged user not found"));

        //5.update the stage of procurement
       existingProcurement.setProcurementStage(ProcurementStage.valueOf(statusUpdateDto.getProcurementStage()));

        //6. create new statusUpdate object
        ProcurementStatusUpdate statusUpdate= new ProcurementStatusUpdate();
        statusUpdate.setComment(statusUpdateDto.getComment());
        //if date is given, set or set current date
        if(statusUpdateDto.getStatusChangedOn()== null){
            statusUpdate.setStatusChangedOn(new Date());
        }else{
            statusUpdate.setStatusChangedOn(statusUpdateDto.getStatusChangedOn());
        }
        //set createdOn
        statusUpdate.setCreatedOn(new Date());
        //if status is given
        if(newStatus != null) {
            statusUpdate.setStatus(newStatus);
        }
        statusUpdate.setProcurementStage(ProcurementStage.valueOf(statusUpdateDto.getProcurementStage()));
        statusUpdate.setProcurement(existingProcurement);
        statusUpdate.setCreatedBy(updatedBy);

        //set updated details in procurement
//        existingProcurement.setLastUpdatedOn(new Date());
//        existingProcurement.setLastUpdatedBy(updatedBy);

        //7. save both to db
        Procurement savedProcurement = procurementRepository.save(existingProcurement);
        ProcurementStatusUpdate savedStatusUpdate = procurementStatusUpdateRepository.save(statusUpdate);

        //send notifications
        notificationService.onProcurementStatusChanged(savedProcurement);

        //create audit log
        StringBuilder auditDetails = new StringBuilder();
        auditDetails.append("Status updated of procurement ID:").append(procurementId).append(" - ");
        auditDetails.append(savedProcurement.getName());
        auditDetails.append(" (request ID:").append(savedProcurement.getRequest().getId()).append(")");
        //add new stage info - only if updated to new stage
        if(statusUpdateDto.getProcurementStage() != null){
            if(isStageUpdated){
                auditDetails.append(", updated from stage: ").append(previousStage);
                auditDetails.append(" to stage: ").append(savedStatusUpdate.getProcurementStage().toString());
            }else{
                auditDetails.append(" with stage: ").append(savedStatusUpdate.getProcurementStage().toString());
            }
        }
        //add status change if there is both old and new
        if(newStatus != null && oldStatus != null){
            auditDetails.append(". Status updated from ").append(oldStatus);
            auditDetails.append(" to status: ").append(newStatus.getName()).append(".");
        }
        //add new status
        if(oldStatus == null && newStatus!= null){
            auditDetails.append(". Status changed to ").append(newStatus.getName()).append(".");
        }
        //add comments if any
        if(statusUpdateDto.getComment() != null){
            auditDetails.append(" Comments added as '").append(statusUpdateDto.getComment()).append("'.");
        }


        //7. add to audit log
        auditService.log(
                updatedBy.getEmail(),
                updatedBy.getEmployeeId(),
                AuditAction.STATUS_CHANGED,
                AuditEntityType.PROCUREMENT,
                savedProcurement.getId(),
                auditDetails.toString()

        );

        //8.return as response dto
        return savedStatusUpdate.getDto();
    }

    @Override
    public List<ProcurementStatusUpdateDto> getStatusUpdatesByProcurementId(Long procurementId) {
        return procurementStatusUpdateRepository.findAllByProcurementId(procurementId)
                .stream()
                .sorted(Comparator.comparing(ProcurementStatusUpdate::getCreatedOn).reversed())
                .map(ProcurementStatusUpdate::getDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void deleteProcurement(Long id) {
        Procurement existingProcurement = validateToUpdateDeleteProcurement(id);

        // for audit log
        User updatedBy = getLoggedUser();

        //update related request
        Request request = requestRepository.findById(existingProcurement.getRequest().getId())
                .orElseThrow(() -> new EntityNotFoundException("Related request not found"));
        //change status of request
        request.setStatus(RequestStatus.PENDING_PROCUREMENT);

        //save to db
        requestRepository.save(request);

        //create audit string
        StringBuilder auditDetails = new StringBuilder();
        auditDetails.append("Deleted procurement ID:").append(existingProcurement.getId()).append(" - ");
        auditDetails.append(existingProcurement.getName());
        auditDetails.append(" (request ID:").append(request.getId()).append(")");
        //add vendor info - only if there is
        if(existingProcurement.getVendor() != null){
            auditDetails.append(", with vendor ID:").append(existingProcurement.getVendor().getId());
            auditDetails.append(" and name: ").append(existingProcurement.getVendor().getName());
        }

        //delete related notifications
        notificationService.deleteNotifications(AuditEntityType.PROCUREMENT, id);

        //delete related procurement attachments

        attachmentService.deleteAllAttachmentsOfAnEntity(id, EntityType.PROCUREMENT);

        //delete procurement
        procurementRepository.deleteById(id);

        auditService.log(
                updatedBy.getEmail(),
                updatedBy.getEmployeeId(),
                AuditAction.DELETED,
                AuditEntityType.PROCUREMENT,
                id,
                auditDetails.toString()
        );
    }

    @Override
    public PDFAttachment uploadProcurementAttachment(MultipartFile file, String name, Long procurementId) throws IOException{
        validateToUpdateDeleteProcurement(procurementId);
        return attachmentService.uploadFile(file, name, procurementId, EntityType.PROCUREMENT);
    }

    @Override
    public void deleteProcurementAttachment(Long fileId) throws IOException {
        //find procurementId
        PDFAttachment attachment = attachmentService.getAttachmentById(fileId);
        EntityType type = attachment.getReferenceType();
        Long referenceId = attachment.getReferenceId();
        if(type.equals(EntityType.PROCUREMENT)) {
            validateToUpdateDeleteProcurement(referenceId);
            attachmentService.deleteAttachment(fileId);
        }else{
            throw new RuntimeException("File is not a procurement attachment");
        }
    }


//class methods

    private Procurement validateToUpdateDeleteProcurement(Long id){
        //1. find procurement to update
        Procurement existingProcurement = procurementRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Procurement not found"));

        //2.check for correct stage (not completed)
        ProcurementStage stage = existingProcurement.getProcurementStage();
        if(stage.equals(ProcurementStage.PAID_AND_COMPLETED)) {
            throw new RuntimeException("This procurement can not be updated/deleted");
        }

        //3. Check if the user logged is allowed to update/delete
        //should be the one created procurement or assigned
        Long loggedUserId = authService.getLoggedUserDto().getId();
        //since assigned user could be null
//        Long assignedUserId = existingProcurement.getAssignedTo()!= null ? existingProcurement.getAssignedTo().getId() : null;
//        Long createdUserId = existingProcurement.getCreatedBy().getId();
//        if(!loggedUserId.equals(assignedUserId) && !loggedUserId.equals(createdUserId)){
//            throw new RuntimeException("The procurement is not allowed to be updated by this user!");
//        }

        //making strict-only the assigned user can update
        Long assignedUserId = existingProcurement.getAssignedTo()!= null ? existingProcurement.getAssignedTo().getId() : null;
        if(assignedUserId == null){
            throw new RuntimeException("There is no assigned user to update this procurement");
        }
        if(!loggedUserId.equals(assignedUserId)){
            throw new RuntimeException("The procurement is not allowed to be updated by this user!");
        }

        return existingProcurement;

    }


    private Request validateAsValidRequest(Long requestId, boolean isCreate){
        //find request object
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request selected is not found"));

        if(isCreate) {
            if(!request.getStatus().equals(RequestStatus.PENDING_PROCUREMENT)){
                throw new RuntimeException("Request is not ready for procurement");
            }
        }else{
            if(!request.getStatus().equals(RequestStatus.PENDING_PROCUREMENT) &&
            !(request.getStatus().equals(RequestStatus.PROCUREMENT_CREATED))){
                throw new RuntimeException("Request can not be added for procurement");
            }
        }

        return request;
    }

    private User getLoggedUser(){

        Long loggedUserId = authService.getLoggedUserDto().getId();

        //find in db
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new EntityNotFoundException("Logged user not found"));

        return loggedUser;
    }

}
