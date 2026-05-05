package com.cht.procurementManagement.services.supplies;

import com.cht.procurementManagement.dto.*;
import com.cht.procurementManagement.entities.*;
import com.cht.procurementManagement.enums.*;
import com.cht.procurementManagement.repositories.AdmindivRepository;
import com.cht.procurementManagement.repositories.RequestRepository;
import com.cht.procurementManagement.repositories.SubdivRepository;
import com.cht.procurementManagement.repositories.UserRepository;
import com.cht.procurementManagement.services.Approval.ApprovalService;
import com.cht.procurementManagement.services.Comment.CommentService;
import com.cht.procurementManagement.services.attachment.AttachmentService;
import com.cht.procurementManagement.services.auth.AuthService;
import com.cht.procurementManagement.services.requests.RequestService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuppliesServiceImpl implements SuppliesService {
    private final RequestRepository requestRepository;

    //to get logged user details
    private final AuthService authService;
    private final UserRepository userRepository;
    private final SubdivRepository subdivRepository;
    private final CommentService commentService;
    private final RequestService requestService;
    private final ApprovalService approvalService;
    private final AdmindivRepository admindivRepository;
    private final AttachmentService attachmentService;

    public SuppliesServiceImpl(RequestRepository requestRepository,
                               AuthService authService,
                               UserRepository userRepository,
                               SubdivRepository subdivRepository,
                               CommentService commentService,
                               RequestService requestService,
                               ApprovalService approvalService,
                               AdmindivRepository admindivRepository,
                               AttachmentService attachmentService) {
        this.requestRepository = requestRepository;
        this.authService = authService;
        this.userRepository = userRepository;
        this.subdivRepository = subdivRepository;
        this.commentService = commentService;
        this.requestService = requestService;
        this.approvalService = approvalService;
        this.admindivRepository = admindivRepository;
        this.attachmentService = attachmentService;
    }

    //get all requests
    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getAllRequests() {
        return requestRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Request::getId).reversed())
                .map(Request::getRequestDto)
                .collect(Collectors.toList());
    }



    //get requests pending approval by supplies division
    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getRequestsPendingSuppliesApproval() {
        return requestRepository.findAll()
                .stream()
                .filter(request -> request.getStatus() == RequestStatus.PENDING_SUPPLIES_APPROVAL)
                .sorted(Comparator.comparing(Request::getId).reversed())
                .map(Request::getRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getRequestsApproved() {
        return requestRepository.findAll()
                .stream()
                .filter(request -> request.getStatus() == RequestStatus.PENDING_PROCUREMENT)
                .sorted(Comparator.comparing(Request::getId).reversed())
                .map(Request::getRequestDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    @Override
    public RequestDto getRequestById(Long requestId) {
        //using request service method
        return requestService.getRequestById(requestId);
    }




    //create request
    @Override
    public RequestDto createRequestBySupplies(RequestDto requestDto, MultipartFile file) {

        //1. check for sub divs list
        if(requestDto.getSubdivIdList() != null && !requestDto.getSubdivIdList().isEmpty()) {

            //check if sub divs exist in db
            if(!checkIfSubdivIdsExist(requestDto.getSubdivIdList())) {
                throw new RuntimeException("Some sub divisions entered may not exist!");
            }

            //check if all selected sub divs selected to one admin division
            if(!checkIfSubdivIdsForOneAdmindiv(requestDto.getSubdivIdList())){
                throw new RuntimeException("Sub-divisions must belong to one admin-division");
            }


            //2. set status of request
            requestDto.setStatus(RequestStatus.PENDING_PROCUREMENT);

            //3. create request through request service
            RequestDto createdRequestDto =  requestService.createRequest(requestDto);

            //4. save the file
            if(file != null && !file.isEmpty()) {
                attachmentService.uploadFile(file, "Request approval", createdRequestDto.getId(), EntityType.REQUEST);
            }

            return createdRequestDto;

        }else{
            throw new EntityNotFoundException("Sub-division/s not found!");
        }
    }

//    @Override
//    public RequestDto createRequestBySupplies(RequestDto requestDto) {
//
//        //1. check for sub divs list
//        if(requestDto.getSubdivIdList() != null && !requestDto.getSubdivIdList().isEmpty()) {
//
//            //check if sub divs exist in db
//            if(!checkIfSubdivIdsExist(requestDto.getSubdivIdList())) {
//                throw new RuntimeException("Some sub divisions entered may not exist!");
//            }
//
//            //check if all selected sub divs selected to one admin division
//            if(!checkIfSubdivIdsForOneAdmindiv(requestDto.getSubdivIdList())){
//                throw new RuntimeException("Sub-divisions must belong to one admin-division");
//            }
//
//
//            //2. set status of request
//            requestDto.setStatus(RequestStatus.PENDING_PROCUREMENT);
//
//            //3. create request through request service
//            return requestService.createRequest(requestDto);
//
//        }else{
//            throw new EntityNotFoundException("Sub-division/s not found!");
//        }
//    }

    //update (replace)
    @Override
    public PDFAttachment uploadRequestAttachment(MultipartFile file, Long requestId){
        validateRequestForUpdateDeleteForSuppliesUser(requestId);

        //remove if there is an existing attachment for the request
        attachmentService.getAttachment(requestId, EntityType.REQUEST).ifPresent(existing ->{
            throw new RuntimeException("Delete existing file attachment first");
        });

        return attachmentService.uploadFile(file, "Request approval", requestId,  EntityType.REQUEST);
    }

    //delete request attachment
    @Override
    public void deleteRequestAttachment(Long fileId){
        //find requestId
        PDFAttachment attachment = attachmentService.getAttachmentById(fileId);
        EntityType type = attachment.getReferenceType();
        Long referenceId = attachment.getReferenceId();
        if(type.equals(EntityType.REQUEST)) {
            validateRequestForUpdateDeleteForSuppliesUser(referenceId);
            attachmentService.deleteAttachment(fileId);
        }else{
            throw new RuntimeException("File is not a request attachment");
        }
    }


    @Override
    public RequestDto updateRequest(Long requestId, RequestDto requestDto) {
        //check for validation for supplies user - using class method
        Request existingRequest = validateRequestForUpdateDeleteForSuppliesUser(requestId);

        //2. check for sub div list in new request dto
        if(requestDto.getSubdivIdList() != null && !requestDto.getSubdivIdList().isEmpty()) {


            //check if sub divs exist in db
            if(!checkIfSubdivIdsExist(requestDto.getSubdivIdList())) {
                throw new RuntimeException("Some sub divisions entered may not exist!");
            }

            //check if all selected sub divs selected to one admin division
            if(!checkIfSubdivIdsForOneAdmindiv(requestDto.getSubdivIdList())){
                throw new RuntimeException("Sub-divisions must belong to one admin-division");
            }

            //3. set status of request
            requestDto.setStatus(RequestStatus.PENDING_PROCUREMENT);

            //4. create request through request service
            return requestService.updateRequest(existingRequest, requestDto);

        }else{
            throw new EntityNotFoundException("Sub-division/s in updated request not found!");
        }

    }

    @Override
    public void deleteRequest(Long requestId) {
        //check for validation for supplies user - using class method
        Request existingRequest = validateRequestForUpdateDeleteForSuppliesUser(requestId);

        //delete related attachment
        attachmentService.deleteAllAttachmentsOfAnEntity(requestId, EntityType.REQUEST);

        //delete request
        requestService.deleteRequest(existingRequest);
    }

//related to approve and reject requests

    //reject a request from a sub div - create comment & change request status
    @Override
    public CommentDto rejectRequestBySupplies(Long requestId, CommentDto commentDto) {

        //1. find the request object to reject
        Request existingRequest = requestRepository.findById(requestId)
                .orElseThrow( () ->  new RuntimeException("Request not found"));

        //2. check status
        if(!existingRequest.getStatus().equals(RequestStatus.PENDING_SUPPLIES_APPROVAL)) {
            throw new RuntimeException("Request is not due for review");
        }

        //3. change status of request
        existingRequest.setStatus(RequestStatus.REJECTED_SUPPLIES_APPROVAL);

        //4. save request to db
        requestRepository.save(existingRequest);

        //5. update commentDto with request id and type
        commentDto.setType(ReviewType.SUPPLIES);
        commentDto.setRequestId(requestId);

        //6. create comment through comment service
        return commentService.createComment(commentDto);

    }

    @Override
    public ApprovalDto approveRequestBySupplies(Long requestId, ApprovalDto approvalDto, MultipartFile file){

        //1. find the request object to approve
        Request existingRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request is not found"));

        //2. check for status
        if(!existingRequest.getStatus().equals(RequestStatus.PENDING_SUPPLIES_APPROVAL)) {
            throw new RuntimeException("Request is not due for review");
        }


        //3. change request status
        existingRequest.setStatus(RequestStatus.PENDING_PROCUREMENT);

        //4. save request to db
        requestRepository.save(existingRequest);

        //5. update Approval dto with request id and type
        approvalDto.setType(ApprovalType.SUPPLIES);
        approvalDto.setRequestId(requestId);

        //6.create new approval object through Approval service
        ApprovalDto savedApprovalDto =  approvalService.createApproval(approvalDto);

        //7. save the file
        if(file != null && !file.isEmpty()) {
            attachmentService.uploadFile(file, "Approval Document", savedApprovalDto.getId(), EntityType.APPROVAL);
        }

        return savedApprovalDto;

    }

    @Override
    public List<CommentDto> getCommentsByRequestId(Long requestId) {
        //1. find the request object to reject
        Request existingRequest = requestRepository.findById(requestId)
                .orElseThrow( () ->  new RuntimeException("Request not found"));
        return commentService.getCommentsByRequestId(requestId);
    }

    @Override
    public List<ApprovalDto> getApprovalsByRequestId(Long requestId) {
        //1. find the request object to reject
        Request existingRequest = requestRepository.findById(requestId)
                .orElseThrow( () ->  new RuntimeException("Request not found"));
        return approvalService.getApprovalsByRequestId(requestId);
    }


//other methods
    @Override
    public List<SubdivDto> getAllSubdivs() {
        return subdivRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Subdiv::getName))
                .map(Subdiv::getSubdivDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubdivDto> getSubdivsByAdmindivId(Long id) {
        return subdivRepository.findByAdmindivId(id)
                .stream()
                .sorted(Comparator.comparing(Subdiv::getCode))
                .map(Subdiv::getSubdivDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubdivGroupedDto> getGroupedSubdivs() {
        //get all sub divs from db
        List<Subdiv> subdivList = subdivRepository.findAll();

        Map<Long, List<Subdiv>> groupedSubdiv = subdivList
                .stream()
                .collect(Collectors.groupingBy(subdiv -> subdiv.getAdmindiv().getId()));

        return groupedSubdiv.entrySet()
                .stream()
                .map(entry -> {
                    Long admindivId = entry.getKey();

                    List<Subdiv> subdivs = entry.getValue();
                    String admindivName = subdivs.get(0).getAdmindiv().getName();

                    List<SubdivDto> subdivDtos = subdivs
                            .stream()
                            .sorted(Comparator.comparing(Subdiv::getName))
                            .map(Subdiv::getSubdivDto)
                            .collect(Collectors.toList());

                    return new SubdivGroupedDto(admindivId, admindivName, subdivDtos);
                })
                .sorted(Comparator.comparing(SubdivGroupedDto::getAdmindivName))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdmindivDto> getAllAdmindivs() {
        return admindivRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Admindiv::getCode))
                .map(Admindiv::getAdmindivDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubdivDto> getSubdivs() {
        return subdivRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Subdiv::getCode))
                .map(Subdiv::getSubdivDto)
                .collect(Collectors.toList());
    }

//class methods
    private Request validateRequestForUpdateDeleteForSuppliesUser(Long requestId){

            //1. find the request object to reject
            Request existingRequest = requestRepository.findById(requestId)
                    .orElseThrow( () ->  new RuntimeException("Request not found"));

            //3. check created user
            if(!UserRole.SUPPLIESUSER.equals(existingRequest.getCreatedBy().getUserRole())){
                throw new RuntimeException("Request is not created by the supplies division");
            }

            //2. check status
            if(!existingRequest.getStatus().equals(RequestStatus.PENDING_PROCUREMENT)) {
                throw new RuntimeException("Not allowed to update/delete due to current status of request");
            }

            return existingRequest;
        }


    private boolean checkIfSubdivIdsExist(List<Long> subdivIds){
        //count in the request
        Long uniqueInputCount = subdivIds.stream().distinct().count();

        //count in db
        Long dbCount = subdivRepository.countByIdIn(subdivIds);

        //compare to find if it equals
        return uniqueInputCount == dbCount;
    }


    private boolean checkIfSubdivIdsForOneAdmindiv(List<Long> subdivIds){
        Long  count = subdivRepository.countDistinctAdminDivisions(subdivIds);
        return count ==1;
    }
}
