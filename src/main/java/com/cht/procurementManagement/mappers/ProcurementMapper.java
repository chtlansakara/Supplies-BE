package com.cht.procurementManagement.mappers;

import com.cht.procurementManagement.dto.procurement.ProcurementCreateDto;
import com.cht.procurementManagement.dto.procurement.ProcurementResponseDto;
import com.cht.procurementManagement.entities.Procurement;
import com.cht.procurementManagement.entities.Request;
import com.cht.procurementManagement.entities.Subdiv;
import com.cht.procurementManagement.entities.User;
import com.cht.procurementManagement.enums.ProcurementStage;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcurementMapper {

    //method to convert Procurement -> ProcurementResponseDto
    public ProcurementResponseDto toResponseDto(Procurement procurement) {
        ProcurementResponseDto responseDto = new ProcurementResponseDto();

        responseDto.setId(procurement.getId());
        responseDto.setNumber(procurement.getNumber());
        responseDto.setName(procurement.getName());
        responseDto.setQuantity(procurement.getQuantity());
        responseDto.setEstimatedAmount(procurement.getEstimatedAmount());
        responseDto.setCategory(procurement.getCategory());

        responseDto.setDonorName(procurement.getDonorName());
        responseDto.setMethod(procurement.getMethod());
        responseDto.setAuthorityLevel(procurement.getAuthorityLevel());
        responseDto.setPriorityStatus(procurement.getPriorityStatus());
        responseDto.setRemarks(procurement.getRemarks());
        responseDto.setVendorDetails(procurement.getVendorDetails());
        responseDto.setScheduledCommenceDate(procurement.getScheduledCommenceDate());
        responseDto.setExpectedCompletionDate(procurement.getExpectedCompletionDate());
        responseDto.setCreatedOn(procurement.getCreatedOn());
        responseDto.setLastUpdatedOn(procurement.getLastUpdatedOn());
        responseDto.setCommencedDate(procurement.getCommencedDate());
        responseDto.setCompletedDate(procurement.getCompletedDate());
        responseDto.setPoNumber(procurement.getPoNumber());
        responseDto.setPoValue(procurement.getPoValue());
        //setting stage
        responseDto.setProcurementStage(procurement.getProcurementStage().toString());
        //for objects
        //assigned to
        if (procurement.getAssignedTo() != null) {
            responseDto.setAssignedToUserId(procurement.getAssignedTo().getId());
            responseDto.setAssignedToUserEmail(procurement.getAssignedTo().getEmail());
            responseDto.setAssignedToUserName(procurement.getAssignedTo().getName());
            responseDto.setAssignedToUserEmployeeId(procurement.getAssignedTo().getEmployeeId());
            if (procurement.getAssignedTo().getDesignation() != null) {
                responseDto.setAssignedToUserDesignation(procurement.getAssignedTo().getDesignation().getCode());
            }
        }
        //status
        if (procurement.getStatus() != null) {
            responseDto.setStatusId(procurement.getStatus().getId());
            responseDto.setStatusName(procurement.getStatus().getName());
        }
        //vendor
        if (procurement.getVendor() != null) {
            responseDto.setVendorId(procurement.getVendor().getId());
            responseDto.setVendorName(procurement.getVendor().getName());
            responseDto.setVendorRegisteredDate(procurement.getVendor().getRegisteredDate());
            responseDto.setVendorComments(procurement.getVendor().getComments());
        }
        //source
        if(procurement.getSource() != null){
            responseDto.setSourceId(procurement.getSource().getId());
            responseDto.setSourceName(procurement.getSource().getName());
            responseDto.setSourceDescription(procurement.getSource().getDescription());
        }
        //request list
//        if (procurement.getRequestList() != null) {
//            responseDto.setRequestIdList(procurement.getRequestList()
//                    .stream()
//                    .map(Request::getId)
//                    .collect(Collectors.toList()));
//
//            responseDto.setRequestTitleList(procurement.getRequestList()
//                    .stream()
//                    .map(Request::getTitle)
//                    .collect(Collectors.toList()));
//        }
        if(procurement.getRequest() != null){
            responseDto.setRequestId(procurement.getRequest().getId());
            responseDto.setRequestTitle(procurement.getRequest().getTitle());
            responseDto.setRequestFund(procurement.getRequest().getFund());
            responseDto.setRequestEstimation(procurement.getRequest().getEstimation());
            responseDto.setRequestApprovedDate(procurement.getRequest().getApprovedDate());
            responseDto.setRequestCreatedDate(procurement.getRequest().getCreatedDate());
            responseDto.setRequestApprovedBy(procurement.getRequest().getAuthorizedBy());

            responseDto.setRequestAdmindivId(procurement.getRequest().getAdmindiv().getId());
            responseDto.setRequestAdmindivCode(procurement.getRequest().getAdmindiv().getCode());
            responseDto.setRequestAdmindivName(procurement.getRequest().getAdmindiv().getName());
            responseDto.setRequestAdmindivResponsible(procurement.getRequest().getAdmindiv().getResponsibleDesignation().getCode());

            responseDto.setRequestUserIdCreatedBy(procurement.getRequest().getCreatedBy().getId());
            responseDto.setRequestUserEmailCreatedBy(procurement.getRequest().getCreatedBy().getEmail());
            responseDto.setRequestEmployeeCreatedBy(procurement.getRequest().getCreatedBy().getEmployeeId());
            responseDto.setRequestSubdivCodeCreatedBy(procurement.getRequest().getCreatedBy().getSubdiv().getCode());
            responseDto.setRequestAdmindivCodeCreatedBy(procurement.getRequest().getCreatedBy().getAdmindiv().getCode());
            responseDto.setRequestSubdivIdList(procurement.getRequest().getSubdivList()
                    .stream()
                    .map(Subdiv::getId).collect(Collectors.toList()));
            responseDto.setRequestSubdivCodeList(procurement.getRequest().getSubdivList()
                    .stream()
                    .map(Subdiv::getCode)
                    .collect(Collectors.toList()));
            responseDto.setRequestSubdivNameList(procurement.getRequest().getSubdivList()
                    .stream()
                    .map(Subdiv::getName)
                    .collect(Collectors.toList()));
        }


        if(procurement.getCreatedBy() != null){
            responseDto.setUserIdCreatedBy(procurement.getCreatedBy().getId());
            responseDto.setEmailCreatedBy(procurement.getCreatedBy().getEmail());
            responseDto.setUserNameCreatedBy(procurement.getCreatedBy().getName());
            responseDto.setEmployeeIdCreatedBy(procurement.getCreatedBy().getEmployeeId());
            responseDto.setUserRoleCreatedBy(procurement.getCreatedBy().getUserRole());
            responseDto.setDesignationCreatedBy(procurement.getCreatedBy().getDesignation().getCode());

            responseDto.setSubdivCreatedBy(procurement.getCreatedBy().getSubdiv().getName());
            responseDto.setSubdivCodeCreatedBy(procurement.getCreatedBy().getSubdiv().getCode());
            responseDto.setAdmindivCreatedBy(procurement.getCreatedBy().getAdmindiv().getName());
            responseDto.setAdmindivCodeCreatedBy(procurement.getCreatedBy().getAdmindiv().getCode());
        }

        //user
        if(procurement.getLastUpdatedBy() != null){
            responseDto.setUserIdLastUpdatedBy(procurement.getLastUpdatedBy().getId());
            responseDto.setEmailLastUpdatedBy(procurement.getLastUpdatedBy().getEmail());
            responseDto.setUserNameLastUpdatedBy(procurement.getLastUpdatedBy().getName());
            responseDto.setEmployeeIdLastUpdatedBy(procurement.getLastUpdatedBy().getEmployeeId());
            responseDto.setUserRoleLastUpdatedBy(procurement.getLastUpdatedBy().getUserRole());
            responseDto.setDesignationUpdatedBy(procurement.getLastUpdatedBy().getDesignation().getCode());


            responseDto.setSubdivLastUpdatedBy(procurement.getLastUpdatedBy().getSubdiv().getName());
            responseDto.setSubdivCodeLastUpdatedBy(procurement.getLastUpdatedBy().getSubdiv().getCode());
            responseDto.setAdmindivLastUpdatedBy(procurement.getLastUpdatedBy().getAdmindiv().getName());
            responseDto.setAdmindivCodeLastUpdatedBy(procurement.getLastUpdatedBy().getAdmindiv().getCode());
        }
        return responseDto;
    }


    public Procurement dtoToProcurement(ProcurementCreateDto dto) {
        Procurement procurement = new Procurement();

        procurement.setNumber(dto.getNumber());
        procurement.setName(dto.getName());
        procurement.setQuantity(dto.getQuantity());
        procurement.setEstimatedAmount(dto.getEstimatedAmount());
        procurement.setCategory(dto.getCategory());

        procurement.setDonorName(dto.getDonorName());
        procurement.setMethod(dto.getMethod());
        procurement.setAuthorityLevel(dto.getAuthorityLevel());
        procurement.setPriorityStatus(dto.getPriorityStatus());
        procurement.setRemarks(dto.getRemarks());
        procurement.setVendorDetails(dto.getVendorDetails());
        procurement.setScheduledCommenceDate(dto.getScheduledCommenceDate());
        procurement.setExpectedCompletionDate(dto.getExpectedCompletionDate());
        procurement.setPoNumber(dto.getPoNumber());
        procurement.setPoValue(dto.getPoValue());


        return procurement;
    }


    public List<ProcurementResponseDto> toResponseDtoList(List<Procurement> procurements) {
        return procurements
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public Procurement updateProcurementWithDto(Procurement procurement, ProcurementCreateDto dto) {
        procurement.setNumber(dto.getNumber());
        procurement.setName(dto.getName());
        procurement.setQuantity(dto.getQuantity());
        procurement.setEstimatedAmount(dto.getEstimatedAmount());
        procurement.setCategory(dto.getCategory());

        procurement.setDonorName(dto.getDonorName());
        procurement.setMethod(dto.getMethod());
        procurement.setAuthorityLevel(dto.getAuthorityLevel());
        procurement.setPriorityStatus(dto.getPriorityStatus());
        procurement.setRemarks(dto.getRemarks());
        procurement.setVendorDetails(dto.getVendorDetails());
        procurement.setScheduledCommenceDate(dto.getScheduledCommenceDate());
        procurement.setExpectedCompletionDate(dto.getExpectedCompletionDate());
        procurement.setPoNumber(dto.getPoNumber());
        procurement.setPoValue(dto.getPoValue());

        return procurement;
    }


}
