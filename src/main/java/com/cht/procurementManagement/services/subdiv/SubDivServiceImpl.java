package com.cht.procurementManagement.services.subdiv;

import com.cht.procurementManagement.dto.*;
import com.cht.procurementManagement.dto.procurement.ProcurementReportDTO;
import com.cht.procurementManagement.dto.procurement.ProcurementResponseDto;
import com.cht.procurementManagement.entities.*;
import com.cht.procurementManagement.enums.EntityType;
import com.cht.procurementManagement.enums.RequestStatus;
import com.cht.procurementManagement.enums.UserRole;
import com.cht.procurementManagement.mappers.ProcurementMapper;
import com.cht.procurementManagement.repositories.*;
import com.cht.procurementManagement.services.Approval.ApprovalService;
import com.cht.procurementManagement.services.Comment.CommentService;
import com.cht.procurementManagement.services.attachment.AttachmentService;
import com.cht.procurementManagement.services.auth.AuthService;
import com.cht.procurementManagement.services.notification.NotificationService;
import com.cht.procurementManagement.services.requests.RequestService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class SubDivServiceImpl implements SubDivService {


    private final UserRepository userRepository;
    private final SubdivRepository subdivRepository;
    private final RequestRepository requestRepository;
    //to get logged user details
    private final AuthService authService;
    private final RequestService requestService;
    private final CommentService commentService;
    private final ApprovalService approvalService;
    private final AttachmentService attachmentService;
    private final AdmindivRepository admindivRepository;
    private final ApprovalRepository approvalRepository;
    private final ProcurementMapper procurementMapper;
    private final ProcurementRepository procurementRepository;
    private final NotificationService notificationService;
    public SubDivServiceImpl(UserRepository userRepository,
                             SubdivRepository subdivRepository,
                             RequestRepository requestRepository,
                             AuthService authService,
                             RequestService requestService,
                             CommentService commentService,
                             ApprovalService approvalService,
                             AttachmentService attachmentService,
                             AdmindivRepository admindivRepository,
                             ApprovalRepository approvalRepository,
                             ProcurementMapper procurementMapper,
                             ProcurementRepository procurementRepository,
                             NotificationService notificationService) {
        this.userRepository = userRepository;
        this.subdivRepository = subdivRepository;
        this.requestRepository = requestRepository;
        this.authService = authService;
        this.requestService = requestService;
        this.commentService = commentService;
        this.approvalService = approvalService;
        this.attachmentService = attachmentService;
        this.admindivRepository = admindivRepository;
        this.approvalRepository = approvalRepository;
        this.procurementMapper = procurementMapper;
        this.procurementRepository = procurementRepository;
        this.notificationService = notificationService;
    }

    //get sub-div dto of the logged user
    @Override
    public SubdivDto getSubdiv() {
        return subdivRepository.findById(getSubdivIdofLoggedUser())
                .map(Subdiv::getSubdivDto)
                .orElseThrow( () -> new EntityNotFoundException("Subdiv is not found"));
    }

    //get all requests RELATED to sub-div
    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getAllRequestsRelatedBySubdivId() {
        //getting sub div id of the user logged in
        return  requestRepository.findAllRequestsRelatedBySubdivId(getSubdivIdofLoggedUser())
                .stream()
                .map(Request::getRequestDto)
                .collect(Collectors.toList());
    }

    //get all requests ONLY for sub-div
    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getAllRequestsOnlyBySubdivId() {
        //getting sub div id of the user logged in
        return  requestRepository.findAllRequestsOnlyBySubdivId(getSubdivIdofLoggedUser())
                .stream()
                .sorted(Comparator.comparing(Request::getId).reversed())
                .map(Request::getRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public RequestDto createRequestBySubdiv(RequestDto requestDto, MultipartFile file) {
        //sub div of the user & the status is set here

        //getting user's sub-div details from logged details - using class method
        Long subdivIdOfUser = getSubdivIdofLoggedUser();
        //creating a list for sub div ids with the sub div id - need to pass as a List
        List<Long> subdivList = List.of(subdivIdOfUser);
        //1. update the requestDto with subdiv List
        requestDto.setSubdivIdList(subdivList);
        //2. set the status of requestDto
        requestDto.setStatus(RequestStatus.PENDING_ADMIN_APPROVAL);

        //create request through request service - sets the user createdBy and created Date
        RequestDto createdRequestDto = requestService.createRequest(requestDto);

        //5. save the file
        if(file != null && !file.isEmpty()) {
            attachmentService.uploadFile(file, "Request approval", createdRequestDto.getId(), EntityType.REQUEST);
        }

        return createdRequestDto;

    }

    //get request by id
    @Override
    public RequestDto getRequestById(Long id) {
        //validate as sub-div request & return as dto
        return validateAsSubdivRequest(id).getRequestDto();
    }

    @Override
    public Optional<PDFAttachment> getSubdivRequestAttachment(Long requestId){
        //validate as subdiv request
        validateAsSubdivRequest(requestId);
        //find attachment if exists
        return attachmentService.getAttachment(requestId, EntityType.REQUEST);
    }

    @Override
    public Optional<PDFAttachment> getSubdivApprovalAttachment(Long approvalId){
        //find the request of the approval
        Optional<Approval> existingApprovalDto = approvalRepository.findById(approvalId);

        if (existingApprovalDto.isPresent()) {
            Long requestId = existingApprovalDto.get().getRequest().getId();

            //validate as sub div request first
            validateAsSubdivRequest(requestId);

            //find the attachment if exists
            return attachmentService.getAttachment(approvalId, EntityType.APPROVAL);
        }

        return Optional.empty();
    }

    @Override
    public Resource downloadSubdivAttachment(Long fileId){
        PDFAttachment existingAttachment = attachmentService.getAttachmentById(fileId);

        //check for relevance
        if(existingAttachment.getReferenceType().equals(EntityType.PROCUREMENT)){
            throw new RuntimeException("Access denied for procurement documents.");

        }else if(existingAttachment.getReferenceType().equals(EntityType.APPROVAL)){

            Approval existingApproval = approvalRepository.findById(existingAttachment.getReferenceId())
                    .orElseThrow(() -> new RuntimeException("Approval not found."));
            Long requestId = existingApproval.getRequest().getId();
            //validate for admin div
            validateAsSubdivRequest(requestId);
        }else{
            //for Entity Type REQUEST
            Long requestId = existingAttachment.getReferenceId();
            //validate for admin div
            validateAsSubdivRequest(requestId);
        }

        //return attachment
        try {
            return attachmentService.getFileResource(fileId);
        } catch (IOException e) {
            throw new RuntimeException("File not found");
        }
    }

    @Override
   public PDFAttachment uploadRequestAttachment(MultipartFile file, Long requestId){
       validateAsSubdivRequest(requestId);

       //remove if there is an existing attachment for the request
       attachmentService.getAttachment(requestId, EntityType.REQUEST).ifPresent(existing ->{
           throw new RuntimeException("Delete existing file attachment first");
       });

       return attachmentService.uploadFile(file, "Request approval", requestId,  EntityType.REQUEST);
   }

    @Override
    public void deleteRequestAttachment(Long fileId){
        //find requestId
        PDFAttachment attachment = attachmentService.getAttachmentById(fileId);
        EntityType type = attachment.getReferenceType();
        Long referenceId = attachment.getReferenceId();
        if(type.equals(EntityType.REQUEST)) {
            validateAsSubdivRequest(referenceId);
            attachmentService.deleteAttachment(fileId);
        }else{
            throw new RuntimeException("File is not a request attachment");
        }
    }
    @Override
    public void deleteRequestById(Long id) {
        //1. Check authorization for the request for sub-div user - using class method
        Request existingRequest = validateRequestUpdateDeleteForSubdivUser(id);
        //2. Delete using request servie
        requestService.deleteRequest(existingRequest);
    }

    @Transactional
    @Override
    public RequestDto updateRequestById(Long id, RequestDto requestDto) {

        //1. Check authorization for the request for sub-div user - using class method
        Request existingRequest = validateRequestUpdateDeleteForSubdivUser(id);

        //2. set sub div list of the request dto -- as user's
        //getting user's sub-div details from logged details - using class method
        Long subdivIdOfUser = getSubdivIdofLoggedUser();
        requestDto.setSubdivIdList(List.of(subdivIdOfUser));

        //3. set status of dto:
        requestDto.setStatus(RequestStatus.PENDING_ADMIN_APPROVAL);

        return requestService.updateRequest(existingRequest, requestDto);
    }

    @Override
    public List<CommentDto> getCommentsByRequestId(Long id) {

        Request request = validateAsSubdivRequest(id);
        return commentService.getCommentsByRequestId(id);

    }

    @Override
    public List<ApprovalDto> getApprovalsByRequestId(Long id){

        Request request = validateAsSubdivRequest(id);
        return approvalService.getApprovalsByRequestId(id);
    }


    //get Procurements related to Sub-div requests
    @Transactional(readOnly = true)
    @Override
    public List<ProcurementResponseDto> getAllProcurementOnlyBySubdivId() {
        List<Long> subdivRequestIds = requestRepository.findAllRequestsOnlyBySubdivId(getSubdivIdofLoggedUser())
                .stream()
                .map(Request::getId)
                .collect(Collectors.toList());
        //get Procurement from request repository query
        List<Procurement> subdivProcurement= procurementRepository.findByRequestIdIn(subdivRequestIds);

        return procurementMapper.toResponseDtoList(subdivProcurement);
    }



    //get procurement by id - subdiv
    @Transactional
    @Override
    public ProcurementResponseDto getProcurementByIdForSubdiv(Long id) {
        //first need to check if the procurement is related to subdiv
        //1. find requests related to admin div -> as id list
        List<Long> subdivRequestIds = requestRepository.findAllRequestsOnlyBySubdivId(getSubdivIdofLoggedUser())
                .stream()
                .map(Request::getId)
                .collect(Collectors.toList());
        //2.get Procurement from request repository query -> as procurement id list
        List<Long> subdivProcurementIds= procurementRepository.findByRequestIdIn(subdivRequestIds)
                .stream()
                .map(Procurement::getId)
                .toList();
        //3. check if the checking id contains in the list for subdiv
        if(!subdivProcurementIds.contains(id)){
            throw new RuntimeException("This procurement can not be accessed by this sub-division");
        }
        //then return the procurement
        Procurement procurement = procurementRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Procurement not found"));
        return procurementMapper.toResponseDto(procurement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestReportDTO> getSubdivRequestReportData(Date startDate, Date endDate){
        Long subdivId = getSubdivIdofLoggedUser();
        List<RequestReportDTO> reportDTOS = requestRepository.findRequestBySubdivReportData(subdivId, startDate, endDate);

        //finding & adding relevant subdivisions to each request
        reportDTOS.forEach(dto -> {
            List<Subdiv> subdivs = requestRepository.findSubdivsByRequestId(dto.getId());
            if(!subdivs.isEmpty()){
                String subdivNames = subdivs.stream()
                        .map(Subdiv::getName)
                        .collect(Collectors.joining(", "));
                dto.setSubdivisions(subdivNames);
            }else{
                dto.setSubdivisions("N/A");
            }
        });

        return reportDTOS;
    }

    @Override
    public List<ProcurementReportDTO> getAllProcurementForSubdivReport(Date startDate, Date endDate) {
        List<Long> subdivRequestIds = requestRepository.findAllRequestsOnlyBySubdivId(getSubdivIdofLoggedUser())
                .stream()
                .map(Request::getId)
                .collect(Collectors.toList());
        //get Procurement from request repository query
        return procurementRepository.findDivisionProcurementReportData(startDate, endDate,subdivRequestIds);
    }

//class-methods

    //class method to get sub div id from the logged user details
    private Long getSubdivIdofLoggedUser(){
        //getting sub-div id from the logged details
        UserDto loggedUser = authService.getLoggedUserDto();
        Long subdivIdOfUser = loggedUser.getSubdivId();
        return  subdivIdOfUser;
    }

    //authorization - validate method for update and delete a request by a sub-div user
    private Request validateRequestUpdateDeleteForSubdivUser(Long requestId){
        //find the request of object
        Request request = requestRepository.findById(requestId)
                .orElseThrow( () -> new RuntimeException("Request not found"));

        //get sub-div list of request
        List<Subdiv> requestSubdivList =  request.getSubdivList();

        //get logged user id
        Long subdivIdOfUser = getSubdivIdofLoggedUser();

        //1. check for single sub-div in the list
        if(requestSubdivList == null || requestSubdivList.size() != 1){
            throw new RuntimeException("Request has more than one sub-division");
        }

        //2. check if it's the user's sub-division
        if(!requestSubdivList.get(0).getId().equals(subdivIdOfUser)){
            throw new RuntimeException("Not authorized to access other sub-division's requests");
        }

        //3. check for status
        if(!RequestStatus.PENDING_ADMIN_APPROVAL.equals(request.getStatus())){
            throw new RuntimeException("Not allowed to update/delete due to current status of request");
        }

        //4. check user role
        if(!UserRole.SUBDIVUSER.equals(request.getCreatedBy().getUserRole())){
            throw new RuntimeException("Request is not created by the sub-division");
        }

        return request;

    }

    //check sub-div requests
    private Request validateAsSubdivRequest(Long requestId){
        //find the request object
        Request request = requestRepository.findById(requestId)
                .orElseThrow( () -> new RuntimeException("Request not found"));

        //get sub-div list of request
        List<Subdiv> requestSubdivList =  request.getSubdivList();

        //get logged user id
        Long subdivIdOfUser = getSubdivIdofLoggedUser();

        //1. check for single sub-div in the list
        if(requestSubdivList == null || requestSubdivList.size() != 1){
            throw new RuntimeException("Request has more than one sub-division");
        }

        //2. check if it's the user's sub-division
        if(!requestSubdivList.get(0).getId().equals(subdivIdOfUser)){
            throw new RuntimeException("Not authorized to access other sub-division's requests");
        }

        return request;

    }

}
