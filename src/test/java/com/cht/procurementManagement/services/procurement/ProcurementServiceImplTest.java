package com.cht.procurementManagement.services.procurement;

import com.cht.procurementManagement.dto.UserDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Procurement Service - Unit Tests")
class ProcurementServiceImplTest {
    @InjectMocks
    private ProcurementServiceImpl procurementService;

    //repository dependencies to mock
    @Mock
    private ProcurementRepository procurementRepository;
    @Mock
    private ProcurementMapper procurementMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private ProcurementStatusRepository procurementStatusRepository;
    @Mock
    private VendorRepository vendorRepository;
    @Mock
    private ProcurementSourceRepository procurementSourceRepository;
    @Mock
    private ProcurementStatusUpdateRepository procurementStatusUpdateRepository;



    //other service classes to mock
    @Mock
    private AuthService authService;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificationService notificationService;

    @Mock
    private AttachmentService attachmentService;

    //re-used objects
    //(procurement has objects: User(assignedTo), User(createdBy), User (lastUpdatedBy),
    // ProcurementStatus (status), ProcurementSource (source), Vendor(vendor),
    // Request (request),
    private Procurement procurement;
    private Procurement mappedProcurement;
    private ProcurementCreateDto createDto;
    private ProcurementResponseDto responseDto;
    private Request request;
    private ProcurementStatus status;
    private ProcurementSource source;
    private Vendor vendor;
    private User assignedTo;

    //other required objects
    private User loggedUser;
    private UserDto loggedUserDto;

    //for request
    private Admindiv admindivForRequest;
    private Subdiv subdivForRequest;
    private Designation designationForAdmindiv;

    //for user
    private Designation designationForUser;
    private Admindiv admindivForUser;
    private Subdiv subdivForUser;

    //initializing each re-used object
    @BeforeEach
    void setup(){
        Date date2025 = java.sql.Date.valueOf(LocalDate.of(2025, 1, 15));
        Date date2026 = java.sql.Date.valueOf(LocalDate.of(2026, 1, 15));

    //initializing the objects re-used & other required objects
        //for procurement -> source, status, vendor, assignedTo
        this.source = new ProcurementSource();
        this.source.setId(1L);
        this.source.setName("102");
        this.source.setDescription("Government Funding");

        this.status = new ProcurementStatus();
        status.setId(1L);
        status.setName("Preparing Tender Documents");

        this.vendor = new Vendor();
        vendor.setId(1L);
        vendor.setName("Abans pvt Ltd");
        vendor.setComments("Worked with in 2020");

        //designation for User
        designationForUser = new Designation();
        designationForUser.setId(3L);
        designationForUser.setTitle("Supplies Clerk");
        designationForUser.setCode("SD");

          //designation for the admindiv
        designationForAdmindiv = new Designation();
        designationForAdmindiv.setId(2L);
        designationForAdmindiv.setTitle("Assistant Registrar");
        designationForAdmindiv.setCode("AR");

         //admindiv for User
        admindivForUser = new Admindiv();
        admindivForUser.setId(2L);
        admindivForUser.setName("Administration");
        admindivForUser.setCode("ADMIN");
        admindivForUser.setResponsibleDesignation(designationForAdmindiv);

        //subdiv for User
        subdivForUser = new Subdiv();
        subdivForUser.setId(1L);
        subdivForUser.setName("Supplies Division");
        subdivForUser.setCode("SUP-ADMIN");
        subdivForUser.setAdmindiv(admindivForUser);
        //User - assignedTo
        this.assignedTo = new User();
        assignedTo.setId(1L);
        assignedTo.setEmail("jayani@gmail.com");
        assignedTo.setUserRole(UserRole.SUPPLIESUSER);
        assignedTo.setDesignation(designationForUser);
        assignedTo.setSubdiv(subdivForUser);
        assignedTo.setAdmindiv(admindivForUser);


        //for request -> loggeduser object, subdiv List and admindiv objects

        //loggedUser & loggedUserDto
        loggedUser = new User();
        loggedUser.setId(2L);
        loggedUser.setEmail("danu@gmail.com");
        loggedUser.setUserRole(UserRole.SUPPLIESUSER);
        loggedUser.setDesignation(designationForUser);
        loggedUser.setSubdiv(subdivForUser);
        loggedUser.setAdmindiv(admindivForUser);
        //dto
        loggedUserDto = new UserDto();
        loggedUserDto.setId(2L);

        //admindiv for request
        admindivForRequest = new Admindiv();
        admindivForRequest.setId(1L);
        admindivForRequest.setName("Faculty of Science");
        admindivForRequest.setCode("FOS");
        admindivForRequest.setResponsibleDesignation(designationForAdmindiv);

        //subdiv for request
        subdivForRequest = new Subdiv();
        subdivForRequest.setId(1L);
        subdivForRequest.setName("Department of Chemistry");
        subdivForRequest.setCode("DOC-FOS");
        subdivForRequest.setAdmindiv(admindivForRequest);

        //request
        request = new Request();
        request.setId(1L);
        request.setTitle("Keyboards");
        request.setQuantity("10");
        request.setEstimation("20000");
        request.setStatus(RequestStatus.PENDING_PROCUREMENT);
        request.setCreatedBy(loggedUser);
        request.setSubdivList(List.of(subdivForRequest));
        request.setAdmindiv(admindivForRequest);
        request.setCreatedDate(date2025);

        //procurement & dtos
        //mapped procurement by mapping service
        mappedProcurement = new Procurement();
        mappedProcurement.setName("Computer keyboards");
        mappedProcurement.setQuantity(10L);
        mappedProcurement.setEstimatedAmount(BigDecimal.valueOf(20000));

        //saved & returned procurement
        procurement = new Procurement();
        procurement.setId(1L);
        procurement.setName("Computer keyboards");
        procurement.setQuantity(10L);
        procurement.setEstimatedAmount(BigDecimal.valueOf(20000));

        procurement.setCreatedOn(date2026);
        procurement.setAssignedTo(assignedTo);
        procurement.setProcurementStage(ProcurementStage.PROCUREMENT_PROCESS_NOT_COMMENCED);
        procurement.setStatus(status);
        procurement.setSource(source);
        procurement.setVendor(vendor);
        procurement.setRequest(request);
        procurement.setCreatedBy(loggedUser);

        //create dto
        this.createDto = new ProcurementCreateDto();
        createDto.setName("Computer keyboards");
        createDto.setQuantity(10L);
        createDto.setEstimatedAmount(BigDecimal.valueOf(20000));
        createDto.setAssignedToUserId(1L);
        createDto.setRequestId(1L);
        createDto.setSourceId(1L);
        createDto.setVendorId(1L);

        //response dto
        this.responseDto = new ProcurementResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Computer Keyboards");
        responseDto.setQuantity(10L);
        responseDto.setEstimatedAmount(BigDecimal.valueOf(20000));
        responseDto.setAssignedToUserId(1L);
        responseDto.setRequestId(1L);
        responseDto.setSourceId(1L);
        responseDto.setStatusId(1L);
        responseDto.setVendorId(1L);
        responseDto.setCreatedOn(date2026);
        responseDto.setUserIdCreatedBy(2L);
    }

    @Nested
    @DisplayName("Create Procurement Tests")
    class createProcurementTests{
        //case 01 - success with vendor
        //case 02 - success with out vendor
        //case 03 - throws when assignedTo User does not exist
        //case 04 - throws when invalid assigned User
        //case 05 - throws when vendor doesn't exist
        //case 06 - throws when source doesn't exist
        //case 07 - throws when request is null
        //case 08 - throws when request doesn't exist
        //case 09 - throws when invalid status of request
        //case 10 - throws when logged user doesn't exist

        @Test
        @DisplayName("Create Procurement - Successfully With Vendor")
        void createProcurement_Success(){
            //ARRANGE:
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(createDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(createDto.getSourceId()))
                    .thenReturn(Optional.of(source));
            when(requestRepository.findById(createDto.getRequestId()))
                    .thenReturn(Optional.of(request));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(requestRepository.save(any(Request.class)))
                    .thenReturn(request);
            when(procurementMapper.toResponseDto(procurement))
                    .thenReturn(responseDto);

            //ACT:
            ProcurementResponseDto result = procurementService.createProcurement(createDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(mappedProcurement.getCreatedOn());
            assertEquals(1L, result.getId());
            assertEquals(ProcurementStage.PROCUREMENT_PROCESS_NOT_COMMENCED, mappedProcurement.getProcurementStage());
            assertEquals(assignedTo, mappedProcurement.getAssignedTo());
            assertEquals(loggedUser, mappedProcurement.getCreatedBy());
            assertEquals(loggedUser, mappedProcurement.getLastUpdatedBy());
            assertEquals(source,     mappedProcurement.getSource());
            assertEquals(RequestStatus.PROCUREMENT_CREATED, request.getStatus());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            verify(vendorRepository).findById(createDto.getVendorId());
            verify(procurementSourceRepository).findById(createDto.getSourceId());
            verify(requestRepository).findById(createDto.getRequestId());
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));

            verify(requestRepository).save(request);
            verify(procurementMapper).toResponseDto(procurement);

            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.CREATED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );
            verify(notificationService).onProcurementCreation(procurement, request);
        }

        @Test
        @DisplayName("Create Procurement - Successfully Without Vendor")
        void createProcurement_SuccessWithoutVendor(){
            //ARRANGE:
            //no vendor id set
            createDto.setVendorId(null);
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));

            when(procurementSourceRepository.findById(createDto.getSourceId()))
                    .thenReturn(Optional.of(source));
            when(requestRepository.findById(createDto.getRequestId()))
                    .thenReturn(Optional.of(request));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(requestRepository.save(any(Request.class)))
                    .thenReturn(request);
            when(procurementMapper.toResponseDto(procurement))
                    .thenReturn(responseDto);

            //ACT:
            ProcurementResponseDto result = procurementService.createProcurement(createDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(mappedProcurement.getCreatedOn());
            assertEquals(1L, result.getId());
            assertEquals(ProcurementStage.PROCUREMENT_PROCESS_NOT_COMMENCED, mappedProcurement.getProcurementStage());
            assertEquals(assignedTo, mappedProcurement.getAssignedTo());
            assertEquals(loggedUser, mappedProcurement.getCreatedBy());
            assertEquals(loggedUser, mappedProcurement.getLastUpdatedBy());
            assertEquals(source,     mappedProcurement.getSource());
            assertEquals(RequestStatus.PROCUREMENT_CREATED, request.getStatus());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            //not executed
            verify(vendorRepository, never()).findById(createDto.getVendorId());
            verify(procurementSourceRepository).findById(createDto.getSourceId());
            verify(requestRepository).findById(createDto.getRequestId());
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));

            verify(requestRepository).save(request);
            verify(procurementMapper).toResponseDto(procurement);

            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.CREATED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );
            verify(notificationService).onProcurementCreation(procurement, request);
        }

        @Test
        @DisplayName("Create Procurement - Throws when assigned User doesn't exist")
        void createProcurement_ThrowsWhenAssignedUserDoesNotExist(){
            //ARRANGE:
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.createProcurement(createDto));

            assertEquals("Assigned user not found", exception.getMessage());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            //not executed
            verify(vendorRepository, never()).findById(createDto.getVendorId());
            verify(procurementSourceRepository, never()).findById(createDto.getSourceId());
            verify(requestRepository, never()).findById(createDto.getRequestId());
            verify(authService, never()).getLoggedUserDto();
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));

            verify(requestRepository, never()).save(request);
            verify(procurementMapper, never()).toResponseDto(procurement);

           verifyNoInteractions(auditService);
           verifyNoInteractions(notificationService);

        }

        @Test
        @DisplayName("Create Procurement - Throws when invalid assigned User")
        void createProcurement_ThrowsWhenAssignedUserIsInvalid(){
            //ARRANGE:
            //change the userRole of assigned To User
            assignedTo.setUserRole(UserRole.ADMINDIVUSER);
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.createProcurement(createDto));

            assertEquals("Assigned user must be a supplies user", exception.getMessage());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            //not executed
            verify(vendorRepository, never()).findById(createDto.getVendorId());
            verify(procurementSourceRepository, never()).findById(createDto.getSourceId());
            verify(requestRepository, never()).findById(createDto.getRequestId());
            verify(authService, never()).getLoggedUserDto();
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));

            verify(requestRepository, never()).save(request);
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
            verifyNoInteractions(notificationService);
        }

        @Test
        @DisplayName("Create Procurement - Throws when vendor doesn't exist")
        void createProcurement_ThrowsWhenVendorDoesNotExist(){
            //ARRANGE:
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(createDto.getVendorId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.createProcurement(createDto));

            assertEquals("Vendor not found", exception.getMessage());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            verify(vendorRepository).findById(createDto.getVendorId());
            //not executed
            verify(procurementSourceRepository, never()).findById(createDto.getSourceId());
            verify(requestRepository, never()).findById(createDto.getRequestId());
            verify(authService, never()).getLoggedUserDto();
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));

            verify(requestRepository, never()).save(request);
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
            verifyNoInteractions(notificationService);

        }

        @Test
        @DisplayName("Create Procurement - Throws when source doesn't exist")
        void createProcurement_ThrowsWhenSourceDoesNotExist(){
            //ARRANGE:
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(createDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(createDto.getSourceId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.createProcurement(createDto));

            assertEquals("Source not found", exception.getMessage());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            verify(vendorRepository).findById(createDto.getVendorId());
            verify(procurementSourceRepository).findById(createDto.getSourceId());
            //not executed
            verify(requestRepository, never()).findById(createDto.getRequestId());
            verify(authService, never()).getLoggedUserDto();
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));

            verify(requestRepository, never()).save(request);
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
            verifyNoInteractions(notificationService);

        }

        @Test
        @DisplayName("Create Procurement - Throws when request is null")
        void createProcurement_ThrowsWhenRequestIsNull(){
            //ARRANGE:
            //set request to null
            createDto.setRequestId(null);
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(createDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(createDto.getSourceId()))
                    .thenReturn(Optional.of(source));

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.createProcurement(createDto));

            assertEquals("Request is empty", exception.getMessage());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            verify(vendorRepository).findById(createDto.getVendorId());
            verify(procurementSourceRepository).findById(createDto.getSourceId());
            //not executed
            verify(requestRepository, never()).findById(createDto.getRequestId());
            verify(authService, never()).getLoggedUserDto();
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));

            verify(requestRepository, never()).save(request);
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
            verifyNoInteractions(notificationService);

        }

        @Test
        @DisplayName("Create Procurement - Throws when request doesn't exist")
        void createProcurement_ThrowsWhenRequestDoesNotExist(){
            //ARRANGE:
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(createDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(createDto.getSourceId()))
                    .thenReturn(Optional.of(source));
            when(requestRepository.findById(createDto.getRequestId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.createProcurement(createDto));

            assertEquals("Request selected is not found", exception.getMessage());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            verify(vendorRepository).findById(createDto.getVendorId());
            verify(procurementSourceRepository).findById(createDto.getSourceId());
            verify(requestRepository).findById(createDto.getRequestId());
            //not executed
            verify(authService, never()).getLoggedUserDto();
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));

            verify(requestRepository, never()).save(request);
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
            verifyNoInteractions(notificationService);

        }

        @Test
        @DisplayName("Create Procurement - Throws when request status is invalid")
        void createProcurement_ThrowsWhenRequestStatusIsInvalid(){
            //ARRANGE:
            //set request status to an invalid status
            request.setStatus(RequestStatus.PENDING_SUPPLIES_APPROVAL);
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(createDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(createDto.getSourceId()))
                    .thenReturn(Optional.of(source));
            when(requestRepository.findById(createDto.getRequestId()))
                    .thenReturn(Optional.of(request));


            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.createProcurement(createDto));

            assertEquals("Request is not ready for procurement", exception.getMessage());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            verify(vendorRepository).findById(createDto.getVendorId());
            verify(procurementSourceRepository).findById(createDto.getSourceId());
            verify(requestRepository).findById(createDto.getRequestId());
            //not executed
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));

            verify(requestRepository, never()).save(request);
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(authService);
            verifyNoInteractions(notificationService);


        }


        @Test
        @DisplayName("Create Procurement - Throws when logged user doesn't exist")
        void createProcurement_ThrowsWhenLoggedUserDoesNotExist(){
            //ARRANGE:
            when(procurementMapper.dtoToProcurement(createDto))
                    .thenReturn(mappedProcurement);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(createDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(createDto.getSourceId()))
                    .thenReturn(Optional.of(source));
            when(requestRepository.findById(createDto.getRequestId()))
                    .thenReturn(Optional.of(request));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.createProcurement(createDto));

            assertEquals("Logged user not found", exception.getMessage());

            //VERIFY:

            verify(procurementMapper).dtoToProcurement(createDto);
            verify(userRepository).findById(createDto.getAssignedToUserId());
            verify(vendorRepository).findById(createDto.getVendorId());
            verify(procurementSourceRepository).findById(createDto.getSourceId());
            verify(requestRepository).findById(createDto.getRequestId());
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(loggedUserDto.getId());
            //not executed
            verify(procurementRepository, never()).save(any(Procurement.class));

            verify(requestRepository, never()).save(request);
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
            verifyNoInteractions(notificationService);


        }





    }

    @Nested
    @DisplayName("Update Procurement Tests")
    class updateProcurementTests{
        Long procurementId = 1L;

        private ProcurementCreateDto updatingDto;
        private Request newRequest;
        private ProcurementSource newSource;

        @BeforeEach
        void setup(){
            newRequest = new Request();
            newRequest.setId(2L);
            newRequest.setStatus(RequestStatus.PENDING_PROCUREMENT);

            newSource = new ProcurementSource();
            newSource.setId(2L);
            newSource.setName("201");

            //updating dto
            updatingDto = new ProcurementCreateDto();
            updatingDto.setName("keyboards");
            updatingDto.setQuantity(8L);
            updatingDto.setAssignedToUserId(1L);
            updatingDto.setRequestId(2L);
            updatingDto.setSourceId(2L);
            updatingDto.setVendorId(1L);

            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
        }

        //case 01 - success without new source or request
        //case 02 - success with new source
        //case 03 - success with new request
        //case 04 - success with both source and request
        //case 05 - throws when procurement doesn't exist
        //case 06 - throws when invalid stage of procurement
        //case 07 - throws when the logged user is not allowed to update
        //case 08 - throws when assigned user doesn't exist
        //case 09 - throws when invalid assigned user
        //case 10 - throws when vendor doesn't exist
        //case 11 - throws when source doesn't exist
        //case 12 - throws when request is null
        //case 13 - throws when request doesn't exist
        //case 14 - throws when invalid request status for update
        //case 15 - throws when request is invalid
        //case 16 - throws when logged User does not exist

        @Test
        @DisplayName("Update Procurement - Successfully Without new source or request")
        void updateProcurement_Success(){

            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(createDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(createDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
//            when(procurementSourceRepository.findById(createDto.getSourceId()))
//                    .thenReturn(Optional.of(source));
            when(requestRepository.findById(createDto.getRequestId()))
                    .thenReturn(Optional.of(request));

            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
//            when(requestRepository.save(any(Request.class)))
//                    .thenReturn(request);
            when(procurementMapper.toResponseDto(procurement))
                    .thenReturn(responseDto);

            //ACT:
            ProcurementResponseDto result = procurementService.updateProcurement(procurementId,createDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(procurement.getLastUpdatedOn());
            assertEquals(loggedUser.getId(), procurement.getLastUpdatedBy().getId());
            assertEquals(request, procurement.getRequest());
            assertEquals(RequestStatus.PENDING_PROCUREMENT, request.getStatus());


            //VERIFY:
            //not called same request
            verify(requestRepository,never()).save(any(Request.class));

            verify(procurementMapper).updateProcurementWithDto(procurement,createDto);
//            verify(userRepository).findById(createDto.getAssignedToUserId());
            verify(vendorRepository).findById(createDto.getVendorId());
//            verify(procurementSourceRepository).findById(createDto.getSourceId());
            verify(requestRepository).findById(createDto.getRequestId());
            verify(authService, times(2)).getLoggedUserDto();
            verify(userRepository , times(2)).findById(1L);
            verify(procurementRepository).save(any(Procurement.class));

//            verify(requestRepository).save(any(Request.class));
            verify(procurementMapper).toResponseDto(procurement);

            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.UPDATED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update Procurement - Successfully With new source")
        void updateProcurement_SuccessWithNewSource(){
            //same request
            updatingDto.setRequestId(1L);
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
                    .thenReturn(Optional.of(newSource));
            when(requestRepository.findById(updatingDto.getRequestId()))
                    .thenReturn(Optional.of(request));

            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
//            when(requestRepository.save(any(Request.class)))
//                    .thenReturn(request);
            when(procurementMapper.toResponseDto(procurement))
                    .thenReturn(responseDto);

            //ACT:
            ProcurementResponseDto result = procurementService.updateProcurement(procurementId,updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(procurement.getLastUpdatedOn());
            assertEquals(loggedUser.getId(), procurement.getLastUpdatedBy().getId());
            assertEquals(request, procurement.getRequest());
            assertEquals(RequestStatus.PENDING_PROCUREMENT, request.getStatus());


            //VERIFY:
            //not called same request
            verify(requestRepository,never()).save(any(Request.class));

            verify(procurementMapper).updateProcurementWithDto(procurement,updatingDto);
//            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository).findById(updatingDto.getSourceId());
            verify(requestRepository).findById(updatingDto.getRequestId());
            verify(authService, times(2)).getLoggedUserDto();
//            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));

//            verify(requestRepository).save(any(Request.class));
            verify(procurementMapper).toResponseDto(procurement);

            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.UPDATED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update Procurement - Successfully With new request")
        void updateProcurement_SuccessWithNewRequest(){
            //ARRANGE:
            //not new source
            updatingDto.setSourceId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
//            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
//                    .thenReturn(Optional.of(source));
            when(requestRepository.findById(updatingDto.getRequestId()))
                    .thenReturn(Optional.of(newRequest));

            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(requestRepository.save(any(Request.class)))
                    .thenReturn(request);
            when(procurementMapper.toResponseDto(procurement))
                    .thenReturn(responseDto);

            //ACT:
            ProcurementResponseDto result = procurementService.updateProcurement(procurementId,updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(procurement.getLastUpdatedOn());
            assertEquals(loggedUser.getId(), procurement.getLastUpdatedBy().getId());
            assertEquals(newRequest, procurement.getRequest());
            assertEquals(RequestStatus.PROCUREMENT_CREATED, newRequest.getStatus());


            //VERIFY:
            //called with new request
            verify(requestRepository, times(2)).save(any(Request.class));

            verify(procurementMapper).updateProcurementWithDto(procurement,updatingDto);
//            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());
//            verify(procurementSourceRepository).findById(updatingDto.getSourceId());
            verify(requestRepository).findById(updatingDto.getRequestId());
            verify(authService, times(2)).getLoggedUserDto();
//            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));


            verify(procurementMapper).toResponseDto(procurement);

            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.UPDATED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update Procurement - Successfully With new request & new source")
        void updateProcurement_SuccessWithNewSourceAndRequest(){
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
                    .thenReturn(Optional.of(newSource));
            when(requestRepository.findById(updatingDto.getRequestId()))
                    .thenReturn(Optional.of(newRequest));

            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(requestRepository.save(any(Request.class)))
                    .thenReturn(request);
            when(procurementMapper.toResponseDto(procurement))
                    .thenReturn(responseDto);

            //ACT:
            ProcurementResponseDto result = procurementService.updateProcurement(procurementId,updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(procurement.getLastUpdatedOn());
            assertEquals(loggedUser.getId(), procurement.getLastUpdatedBy().getId());
            assertEquals(newRequest, procurement.getRequest());
            assertEquals(RequestStatus.PROCUREMENT_CREATED, newRequest.getStatus());


            //VERIFY:
            //called with new request
            verify(requestRepository, times(2)).save(any(Request.class));

            verify(procurementMapper).updateProcurementWithDto(procurement,updatingDto);
//            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository).findById(updatingDto.getSourceId());
            verify(requestRepository).findById(updatingDto.getRequestId());
            verify(authService, times(2)).getLoggedUserDto();
//            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));


            verify(procurementMapper).toResponseDto(procurement);

            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.UPDATED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update Procurement - Throws when procurement doesn't exist")
        void updateProcurement_ThrowsWhenProcurementDoesNotExist(){
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Procurement not found", exception.getMessage());

            //VERIFY:

            verify(procurementRepository).findById(procurementId);

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
            verify(userRepository, never()).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository, never()).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository, never()).findById(updatingDto.getSourceId());
            verify(requestRepository, never()).findById(updatingDto.getRequestId());
            verify(authService, never()).getLoggedUserDto();
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update Procurement - Throws when invalid procurement stage")
        void updateProcurement_ThrowsWhenInvalidProcurementStage(){
            //ARRANGE:
            //setting invalid stage
            procurement.setProcurementStage(ProcurementStage.PAID_AND_COMPLETED);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("This procurement can not be updated/deleted", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
            verify(userRepository, never()).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository, never()).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository, never()).findById(updatingDto.getSourceId());
            verify(requestRepository, never()).findById(updatingDto.getRequestId());
            verify(authService, never()).getLoggedUserDto();
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);

        }

        @Test
        @DisplayName("Update Procurement - Throws when invalid logged user")
        void updateProcurement_ThrowsWhenInvalidLoggedUser(){
            //ARRANGE:
            //change logged user Id
            loggedUserDto.setId(3L);


            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("The procurement is not allowed to be updated by this user!", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
            verify(userRepository, never()).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository, never()).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository, never()).findById(updatingDto.getSourceId());
            verify(requestRepository, never()).findById(updatingDto.getRequestId());
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }
        @Test
        @DisplayName("Update Procurement - Throws when assigned user doesn't exist")
        void updateProcurement_ThrowsWhenAssignedUserDoesNotExist(){
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Assigned User not found", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(updatingDto.getAssignedToUserId());

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
            verify(vendorRepository, never()).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository, never()).findById(updatingDto.getSourceId());
            verify(requestRepository, never()).findById(updatingDto.getRequestId());
//            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update Procurement - Throws when invalid assigned user")
        void updateProcurement_ThrowsWhenInvalidAssignedUser(){
            //ARRANGE:
            //setting assigned user to invalid
            assignedTo.setUserRole(UserRole.SUBDIVUSER);

            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));

            //ACT & ASSERT:
           RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Assigned user must be a supplies user", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(updatingDto.getAssignedToUserId());

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
            verify(vendorRepository, never()).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository, never()).findById(updatingDto.getSourceId());
            verify(requestRepository, never()).findById(updatingDto.getRequestId());
//            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update Procurement - Throws when vendor doesn't exist")
        void updateProcurement_ThrowsWhenVendorDoesNotExist(){
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Vendor not found", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
            verify(procurementSourceRepository, never()).findById(updatingDto.getSourceId());
            verify(requestRepository, never()).findById(updatingDto.getRequestId());
//            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update Procurement - Throws when source doesn't exist")
        void updateProcurement_ThrowsWhenSourceDoesNotExist(){
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Source not found", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository).findById(updatingDto.getSourceId());

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
            verify(requestRepository, never()).findById(updatingDto.getRequestId());
//            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update Procurement - Throws when request is null")
        void updateProcurement_ThrowsWhenRequestIsNull(){
            //ARRANGE:
            //set request to null
            updatingDto.setRequestId(null);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
                    .thenReturn(Optional.of(newSource));

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Request is empty", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository).findById(updatingDto.getSourceId());

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
            verify(requestRepository, never()).findById(updatingDto.getRequestId());
//            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update Procurement - Throws when request doesn't exist")
        void updateProcurement_ThrowsWhenRequestDoesNotExist(){
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
                    .thenReturn(Optional.of(newSource));
            when(requestRepository.findById(updatingDto.getRequestId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Request selected is not found", exception.getMessage());

            //VERIFY:

            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository).findById(updatingDto.getSourceId());
            verify(requestRepository).findById(updatingDto.getRequestId());

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
//            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update Procurement - Throws when invalid request status")
        void updateProcurement_ThrowsWhenInvalidRequestStatus(){
            //ARRANGE:
            //set request status to invalid status
            newRequest.setStatus(RequestStatus.PENDING_ADMIN_APPROVAL);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
                    .thenReturn(Optional.of(newSource));
            when(requestRepository.findById(updatingDto.getRequestId()))
                    .thenReturn(Optional.of(newRequest));

            //ACT & ASSERT:
           RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Request can not be added for procurement", exception.getMessage());

            //VERIFY:

            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository).findById(updatingDto.getSourceId());
            verify(requestRepository).findById(updatingDto.getRequestId());

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
//            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update Procurement - Throws when invalid request")
        void updateProcurement_ThrowsWhenInvalidRequest(){
            //ARRANGE:
            //set new request status
            newRequest.setStatus(RequestStatus.PROCUREMENT_CREATED);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(updatingDto.getAssignedToUserId()))
                    .thenReturn(Optional.of(assignedTo));
            when(vendorRepository.findById(updatingDto.getVendorId()))
                    .thenReturn(Optional.of(vendor));
            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
                    .thenReturn(Optional.of(newSource));
            when(requestRepository.findById(updatingDto.getRequestId()))
                    .thenReturn(Optional.of(newRequest));


            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateProcurement(procurementId,updatingDto)
            );

            //ASSERT:
            assertEquals("Can not select requests in other procurement", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(userRepository).findById(updatingDto.getAssignedToUserId());
            verify(vendorRepository).findById(updatingDto.getVendorId());
            verify(procurementSourceRepository).findById(updatingDto.getSourceId());
            verify(requestRepository).findById(updatingDto.getRequestId());

            verify(procurementMapper, never()).updateProcurementWithDto(procurement,updatingDto);
//            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementMapper, never()).toResponseDto(procurement);

            verifyNoInteractions(auditService);
        }

//        @Test
//        @DisplayName("Update Procurement - Throws when logged user doesn't exist")
//        void updateProcurement_ThrowsWhenLoggedUserDoesNotExist(){
//            //ARRANGE:
//
//            when(procurementRepository.findById(procurementId))
//                    .thenReturn(Optional.of(procurement));
//            when(authService.getLoggedUserDto())
//                    .thenReturn(loggedUserDto);
//            when(userRepository.findById(updatingDto.getAssignedToUserId()))
//                    .thenReturn(Optional.of(assignedTo));
//            when(vendorRepository.findById(updatingDto.getVendorId()))
//                    .thenReturn(Optional.of(vendor));
//            when(procurementSourceRepository.findById(updatingDto.getSourceId()))
//                    .thenReturn(Optional.of(newSource));
//            when(requestRepository.findById(updatingDto.getRequestId()))
//                    .thenReturn(Optional.of(newRequest));
//
//            when(userRepository.findById(loggedUserDto.getId()))
//                    .thenReturn(Optional.empty());
//
//            //ACT & ASSERT:
//            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
//                    () -> procurementService.updateProcurement(procurementId,updatingDto)
//            );
//
//            //ASSERT:
//            assertEquals("Current user not found", exception.getMessage());
//
//            //VERIFY:
//
//            verify(procurementRepository).findById(procurementId);
//            verify(authService, times(2)).getLoggedUserDto();
//            verify(userRepository).findById(updatingDto.getAssignedToUserId());
//            verify(vendorRepository).findById(updatingDto.getVendorId());
//            verify(procurementSourceRepository).findById(updatingDto.getSourceId());
//            verify(requestRepository).findById(updatingDto.getRequestId());
//            verify(procurementMapper).updateProcurementWithDto(procurement,updatingDto);
//            verify(userRepository).findById(loggedUserDto.getId());
//
//
//            verify(procurementRepository, never()).save(any(Procurement.class));
//            verify(requestRepository, never()).save(any(Request.class));
//            verify(procurementMapper, never()).toResponseDto(procurement);
//
//            verifyNoInteractions(auditService);
//        }


    }


    @Nested
    @DisplayName("Delete Procurement Tests")
    class deleteProcurementTests{
        //case 01 - success
        //case 02 - throws when procurement doesn't exist
        //case 03 - throws when procurement stage is invalid
        //case 04 - throws when logged user is invalid
        //case 05 - throws when logged user doesn't exist
        //case 06 - throws when related request is not found

        Long procurementId = 1L;

        @Test
        @DisplayName("Delete Procurement - Successfully")
        void deleteProcurement_success(){
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(loggedUser.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(requestRepository.findById(procurement.getRequest().getId()))
                    .thenReturn(Optional.of(request));
            when(requestRepository.save(any(Request.class)))
                    .thenReturn(request);


            doNothing().when(procurementRepository).deleteById(procurementId);
            //ACT:
            procurementService.deleteProcurement(procurementId);
            assertEquals(RequestStatus.PENDING_PROCUREMENT, request.getStatus());
            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService, times(2)).getLoggedUserDto();
            verify(requestRepository).findById(procurement.getRequest().getId());
            verify(requestRepository).save(any(Request.class));
            verify(userRepository).findById(loggedUser.getId());
            verify(procurementRepository).deleteById(procurementId);

            verify(notificationService).deleteNotifications(AuditEntityType.PROCUREMENT,procurementId);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.DELETED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );
        }

        @Test
        @DisplayName("Delete Procurement - Throws when procurement doesn't exist")
        void deleteProcurement_ThrowsWhenProcurementDoesNotExist(){
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.deleteProcurement(procurementId)
            );
            assertEquals("Procurement not found", exception.getMessage());
            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            //not executed
            verify(authService,never()).getLoggedUserDto();
            verify(requestRepository, never()).findById(procurement.getRequest().getId());
            verify(requestRepository, never()).save(any(Request.class));
            verify(userRepository, never()).findById(loggedUser.getId());
            verify(procurementRepository, never()).deleteById(procurementId);

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }

        @Test
        @DisplayName("Delete Procurement - Throws when invalid procurement stage")
        void deleteProcurement_ThrowsWhenInvalidProcurementStage(){
            //ARRANGE:
            //change procurement stage
            procurement.setProcurementStage(ProcurementStage.PAID_AND_COMPLETED);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.deleteProcurement(procurementId)
            );
            assertEquals("This procurement can not be updated/deleted", exception.getMessage());
            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            //not executed
            verify(authService,never()).getLoggedUserDto();
            verify(requestRepository, never()).findById(procurement.getRequest().getId());
            verify(requestRepository, never()).save(any(Request.class));
            verify(userRepository, never()).findById(loggedUser.getId());
            verify(procurementRepository, never()).deleteById(procurementId);

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }
        @Test
        @DisplayName("Delete Procurement - Throws when invalid logged user")
        void deleteProcurement_ThrowsWhenInvalidLoggedUser(){
            //ARRANGE:
            //change the logged user id (not assignedTo or createdBy users)
            loggedUserDto.setId(3L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.deleteProcurement(procurementId)
            );
            assertEquals("The procurement is not allowed to be updated by this user!", exception.getMessage());
            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            //not executed
            verify(requestRepository, never()).findById(procurement.getRequest().getId());
            verify(requestRepository, never()).save(any(Request.class));
            verify(userRepository, never()).findById(loggedUser.getId());
            verify(procurementRepository, never()).deleteById(procurementId);

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Delete Procurement - Throws when logged user does not exist")
        void deleteProcurement_ThrowsWhenLoggedUserDoesNotExist(){
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(loggedUser.getId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.deleteProcurement(procurementId)
            );
            assertEquals("Logged user not found", exception.getMessage());
            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService, times(2)).getLoggedUserDto();
            verify(userRepository).findById(loggedUser.getId());
            //not executed
            verify(requestRepository, never()).findById(procurement.getRequest().getId());
            verify(requestRepository, never()).save(any(Request.class));
            verify(procurementRepository, never()).deleteById(procurementId);

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);
        }
        @Test
        @DisplayName("Delete Procurement - Throws when request doesn't exist")
        void deleteProcurement_ThrowsWhenRequestDoesNotExist(){
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //ARRANGE:
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(userRepository.findById(loggedUser.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(requestRepository.findById(procurement.getRequest().getId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.deleteProcurement(procurementId)
            );
            assertEquals("Related request not found", exception.getMessage());
            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService, times(2)).getLoggedUserDto();
            verify(userRepository).findById(loggedUser.getId());
            verify(requestRepository).findById(procurement.getRequest().getId());
            //not executed
            verify(requestRepository, never()).save(any(Request.class));

            verify(procurementRepository, never()).deleteById(procurementId);

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);
        }


    }


    @Nested
    @DisplayName("Update Status Tests")
    class updateStatusTests{
        private Long procurementId = 1L;
        private ProcurementStatusUpdate statusUpdate;
        private ProcurementStatusUpdateDto statusUpdateDto;

        private ProcurementStatus newStatus;

        Date date2025 = java.sql.Date.valueOf(LocalDate.of(2025, 1, 15));
        Date date2026 = java.sql.Date.valueOf(LocalDate.of(2026, 1, 15));

        @BeforeEach
        void setup(){
            //new status
            newStatus = new ProcurementStatus();
            newStatus.setId(2L);
            newStatus.setName("Preparing TEC Report");

            //dto
            statusUpdateDto = new ProcurementStatusUpdateDto();
            statusUpdateDto.setComment("Delayed than expected");
            statusUpdateDto.setProcurementStage(ProcurementStage.PURCHASE_ORDERS_ISSUED.toString());
            statusUpdateDto.setProcurementStatusId(2L);
            statusUpdateDto.setProcurementId(1L);
            statusUpdateDto.setStatusChangedOn(date2025);

            //object
            statusUpdate = new ProcurementStatusUpdate();
            statusUpdate.setId(1L);
            statusUpdate.setComment("Delayed than expected");
            statusUpdate.setProcurementStage(ProcurementStage.PURCHASE_ORDERS_ISSUED);

            statusUpdate.setCreatedOn(date2026);
            statusUpdate.setStatusChangedOn(date2025);
            statusUpdate.setCreatedBy(loggedUser);
            statusUpdate.setProcurement(procurement);
            statusUpdate.setStatus(newStatus);




        }

        //case 01- success with status + date
        //case 02 - success with status only
        //case 03 - success with date only
        //case 04 - success without status or date
        //case 05 - success with completed stage (saves completed date)
        //case 06 - success with completed stage and date
        //case 07 - success when commenced stage (saves commenced date)
        //case 08 - success with commenced stage and date

        //case 09- throws when procurement doesn't exist
        //case 10 - throws when assignedTo User is null
        //case 11 - throws when invalid logged User (not assignedTo User)
        //case 12 - throws when invalid procurement (already completed)
        //case 13 - throws when stage is null
        //case 14 - throws when same status is selected
        //case 15 - throws when status doesn't exist
        //case 16 - throws when logged user doesn't exist

        @Test
        @DisplayName("Update status - Successfully with Status & Date")
        void updateStatus_Success(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(procurementStatusUpdateRepository.save(any(ProcurementStatusUpdate.class)))
                    .thenReturn(statusUpdate);

            //ACT:
            ProcurementStatusUpdateDto result = procurementService.updateStatus(procurementId, statusUpdateDto);

            //ASSERT:
            assertNotNull(result);
            assertNull(procurement.getCommencedDate());
            assertNull(procurement.getCompletedDate());
            assertEquals(ProcurementStage.PURCHASE_ORDERS_ISSUED, procurement.getProcurementStage());
            assertEquals(newStatus, procurement.getStatus());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository).save(any(ProcurementStatusUpdate.class));

            verify(notificationService).onProcurementStatusChanged(procurement);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.STATUS_CHANGED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update status - Successfully with status")
        void updateStatus_SuccessWithStatus(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //set date to null
            statusUpdateDto.setStatusChangedOn(null);

            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(procurementStatusUpdateRepository.save(any(ProcurementStatusUpdate.class)))
                    .thenReturn(statusUpdate);

            //ACT:
            ProcurementStatusUpdateDto result = procurementService.updateStatus(procurementId, statusUpdateDto);

            //ASSERT:
            assertNotNull(result);
            assertNull(procurement.getCommencedDate());
            assertNull(procurement.getCompletedDate());
            assertEquals(ProcurementStage.PURCHASE_ORDERS_ISSUED, procurement.getProcurementStage());
            assertEquals(newStatus, procurement.getStatus());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository).save(any(ProcurementStatusUpdate.class));

            verify(notificationService).onProcurementStatusChanged(procurement);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.STATUS_CHANGED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update status - Successfully with date")
        void updateStatus_SuccessWithDate(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //set no status
            statusUpdateDto.setProcurementStatusId(null);

            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
//            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
//                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(procurementStatusUpdateRepository.save(any(ProcurementStatusUpdate.class)))
                    .thenReturn(statusUpdate);

            //ACT:
            ProcurementStatusUpdateDto result = procurementService.updateStatus(procurementId, statusUpdateDto);

            //ASSERT:
            assertNotNull(result);
            assertNull(procurement.getCommencedDate());
            assertNull(procurement.getCompletedDate());
            assertEquals(ProcurementStage.PURCHASE_ORDERS_ISSUED, procurement.getProcurementStage());
            //status should be removed
            assertNull(procurement.getStatus());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
//            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository).save(any(ProcurementStatusUpdate.class));

            verify(notificationService).onProcurementStatusChanged(procurement);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.STATUS_CHANGED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update status - Successfully without status & date")
        void updateStatus_SuccessWithoutStatusAndDate(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //set no status
            statusUpdateDto.setProcurementStatusId(null);
            //set date to null
            statusUpdateDto.setStatusChangedOn(null);

            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
//            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
//                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(procurementStatusUpdateRepository.save(any(ProcurementStatusUpdate.class)))
                    .thenReturn(statusUpdate);

            //ACT:
            ProcurementStatusUpdateDto result = procurementService.updateStatus(procurementId, statusUpdateDto);

            //ASSERT:
            assertNotNull(result);
            assertNull(procurement.getCommencedDate());
            assertNull(procurement.getCompletedDate());
            assertEquals(ProcurementStage.PURCHASE_ORDERS_ISSUED, procurement.getProcurementStage());
            //status should be removed
            assertNull( procurement.getStatus());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
//            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository).save(any(ProcurementStatusUpdate.class));

            verify(notificationService).onProcurementStatusChanged(procurement);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.STATUS_CHANGED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }


        @Test
        @DisplayName("Update status - Successfully with Stage commenced & date")
        void updateStatus_SuccessWithCommencedStageAndDate(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //set commenced as the stage
            statusUpdateDto.setProcurementStage(ProcurementStage.PURCHASE_PROCESS_COMMENCED.toString());
            statusUpdate.setProcurementStage(ProcurementStage.PURCHASE_PROCESS_COMMENCED);

            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(procurementStatusUpdateRepository.save(any(ProcurementStatusUpdate.class)))
                    .thenReturn(statusUpdate);

            //ACT:
            ProcurementStatusUpdateDto result = procurementService.updateStatus(procurementId, statusUpdateDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(date2025, procurement.getCommencedDate());
            assertEquals(ProcurementStage.PURCHASE_PROCESS_COMMENCED, procurement.getProcurementStage());
            assertEquals(newStatus, procurement.getStatus());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository).save(any(ProcurementStatusUpdate.class));

            verify(notificationService).onProcurementStatusChanged(procurement);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.STATUS_CHANGED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update status - Successfully with Stage commenced without date")
        void updateStatus_SuccessWithCommencedStageAndWithOutDate(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //set commenced as the stage
            statusUpdateDto.setProcurementStage(ProcurementStage.PURCHASE_PROCESS_COMMENCED.toString());
            statusUpdate.setProcurementStage(ProcurementStage.PURCHASE_PROCESS_COMMENCED);
            //set the date null
            statusUpdateDto.setStatusChangedOn(null);

            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(procurementStatusUpdateRepository.save(any(ProcurementStatusUpdate.class)))
                    .thenReturn(statusUpdate);

            //ACT:
            ProcurementStatusUpdateDto result = procurementService.updateStatus(procurementId, statusUpdateDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(procurement.getCommencedDate());
            assertNotEquals(date2025, procurement.getCommencedDate());
            assertEquals(ProcurementStage.PURCHASE_PROCESS_COMMENCED, procurement.getProcurementStage());
            assertEquals(newStatus, procurement.getStatus());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository).save(any(ProcurementStatusUpdate.class));

            verify(notificationService).onProcurementStatusChanged(procurement);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.STATUS_CHANGED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update status - Successfully with Stage completed & date")
        void updateStatus_SuccessWithCompletedStageAndDate(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //set commenced as the stage
            statusUpdateDto.setProcurementStage(ProcurementStage.PAID_AND_COMPLETED.toString());
            statusUpdate.setProcurementStage(ProcurementStage.PAID_AND_COMPLETED);

            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(procurementStatusUpdateRepository.save(any(ProcurementStatusUpdate.class)))
                    .thenReturn(statusUpdate);

            //ACT:
            ProcurementStatusUpdateDto result = procurementService.updateStatus(procurementId, statusUpdateDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(date2025, procurement.getCompletedDate());
            assertEquals(ProcurementStage.PAID_AND_COMPLETED, procurement.getProcurementStage());
            assertEquals(newStatus, procurement.getStatus());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository).save(any(ProcurementStatusUpdate.class));

            verify(notificationService).onProcurementStatusChanged(procurement);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.STATUS_CHANGED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update status - Successfully with Stage completed without date")
        void updateStatus_SuccessWithCompletedStageAndWithOutDate(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            //set commenced as the stage
            statusUpdateDto.setProcurementStage(ProcurementStage.PAID_AND_COMPLETED.toString());
            statusUpdate.setProcurementStage(ProcurementStage.PAID_AND_COMPLETED);
            //set the date null
            statusUpdateDto.setStatusChangedOn(null);

            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.of(loggedUser));
            when(procurementRepository.save(any(Procurement.class)))
                    .thenReturn(procurement);
            when(procurementStatusUpdateRepository.save(any(ProcurementStatusUpdate.class)))
                    .thenReturn(statusUpdate);

            //ACT:
            ProcurementStatusUpdateDto result = procurementService.updateStatus(procurementId, statusUpdateDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(procurement.getCompletedDate());
            assertNotEquals(date2025, procurement.getCompletedDate());
            assertEquals(ProcurementStage.PAID_AND_COMPLETED, procurement.getProcurementStage());
            assertEquals(newStatus, procurement.getStatus());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            verify(procurementRepository).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository).save(any(ProcurementStatusUpdate.class));

            verify(notificationService).onProcurementStatusChanged(procurement);
            verify(auditService).log(
                    eq(loggedUser.getEmail()),
                    eq(loggedUser.getEmployeeId()),
                    eq(AuditAction.STATUS_CHANGED),
                    eq(AuditEntityType.PROCUREMENT),
                    eq(procurement.getId()),
                    anyString()
            );

        }

        @Test
        @DisplayName("Update status - Throws when procurement doesn't exist")
        void updateStatus_ThrowsWhenProcurementDoesNotExist(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.updateStatus(procurementId, statusUpdateDto)
            );
            assertEquals("Procurement not found", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            //not executed
            verify(authService, never()).getLoggedUserDto();
            verify(procurementStatusRepository, never()).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository,never()).save(any(ProcurementStatusUpdate.class));

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }


        @Test
        @DisplayName("Update status - Throws when AssignedTo User is Null")
        void updateStatus_ThrowsWhenAssignedUserIsNull(){
            //ARRANGE:
            //set assigned user of the procurement null
            procurement.setAssignedTo(null);
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateStatus(procurementId, statusUpdateDto)
            );
            assertEquals("To update status procurement should have an assigned employee", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            //not executed
            verify(authService, never()).getLoggedUserDto();
            verify(procurementStatusRepository, never()).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository,never()).save(any(ProcurementStatusUpdate.class));

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }

        @Test
        @DisplayName("Update status - Throws when Invalid Logged User")
        void updateStatus_ThrowsWhenInvalidLoggedUser(){
            //ARRANGE:
            //logged user is not the assigned user
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateStatus(procurementId, statusUpdateDto)
            );
            assertEquals("The procurement is not allowed to be updated by this user!", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            //not executed
            verify(procurementStatusRepository, never()).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository,never()).save(any(ProcurementStatusUpdate.class));

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }

        @Test
        @DisplayName("Update status - Throws when Invalid Procurement")
        void updateStatus_ThrowsWhenInvalidProcurement(){
            //ARRANGE:
            //set completed date to the procurement
            procurement.setCompletedDate(date2026);
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateStatus(procurementId, statusUpdateDto)
            );
            assertEquals("Can not update status as the procurement is already completed.", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            //not executed
            verify(procurementStatusRepository, never()).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository,never()).save(any(ProcurementStatusUpdate.class));

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }

        @Test
        @DisplayName("Update status - Throws when Stage is null")
        void updateStatus_ThrowsWhenStageIsNull(){
            //ARRANGE:
            //set the stage of dto to null
            statusUpdateDto.setProcurementStage(null);
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateStatus(procurementId, statusUpdateDto)
            );
            assertEquals("The stage should be selected", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            //not executed
            verify(procurementStatusRepository, never()).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository,never()).save(any(ProcurementStatusUpdate.class));

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }

        @Test
        @DisplayName("Update status - Throws when same Status")
        void updateStatus_ThrowsWhenSameStatus(){
            //ARRANGE:
            //set the status to the same
            statusUpdateDto.setProcurementStatusId(1L);
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> procurementService.updateStatus(procurementId, statusUpdateDto)
            );
            assertEquals("Can not select the same status.", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            //not executed
            verify(procurementStatusRepository, never()).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository,never()).save(any(ProcurementStatusUpdate.class));

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }

        @Test
        @DisplayName("Update status - Throws when Status doesn't exist")
        void updateStatus_ThrowsWhenStatusDoesNotExist(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.updateStatus(procurementId, statusUpdateDto)
            );
            assertEquals("Status selected not found", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            //not executed
            verify(userRepository, never()).findById(loggedUserDto.getId());
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository,never()).save(any(ProcurementStatusUpdate.class));

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("Update status - Throws when Logged User doesn't exist")
        void updateStatus_ThrowsWhenLoggedUserDoesNotExist(){
            //ARRANGE:
            //set assignedTo user as logged user
            loggedUserDto.setId(1L);
            loggedUser.setId(1L);
            when(procurementRepository.findById(procurementId))
                    .thenReturn(Optional.of(procurement));
            when(authService.getLoggedUserDto())
                    .thenReturn(loggedUserDto);
            when(procurementStatusRepository.findById(statusUpdateDto.getProcurementStatusId()))
                    .thenReturn(Optional.of(newStatus));
            when(userRepository.findById(loggedUserDto.getId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> procurementService.updateStatus(procurementId, statusUpdateDto)
            );
            assertEquals("Logged user not found", exception.getMessage());

            //VERIFY:
            verify(procurementRepository).findById(procurementId);
            verify(authService).getLoggedUserDto();
            verify(procurementStatusRepository).findById(statusUpdateDto.getProcurementStatusId());
            verify(userRepository).findById(loggedUserDto.getId());
            //not executed
            verify(procurementRepository, never()).save(any(Procurement.class));
            verify(procurementStatusUpdateRepository,never()).save(any(ProcurementStatusUpdate.class));

            verifyNoInteractions(notificationService);
            verifyNoInteractions(auditService);

        }


    }





}