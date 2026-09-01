package com.cht.procurementManagement.services.admin;

import com.cht.procurementManagement.dto.*;
import com.cht.procurementManagement.entities.*;
import com.cht.procurementManagement.enums.UserRole;
import com.cht.procurementManagement.repositories.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jdk.jfr.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.ExpressionException;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Service - Unit Tests")
class AdminServiceImplTest {

    //service class being tested
    @InjectMocks
    private AdminServiceImpl adminService;
    
    //dependencies to mock
    @Mock
    private  ProcurementSourceRepository procurementSourceRepository;

    @Mock
    private DesignationRepository designationRepository;
    @Mock
    private AdmindivRepository admindivRepository;
    @Mock
    private SubdivRepository subdivRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProcurementStatusRepository procurementStatusRepository;
    @Mock
    private VendorRepository vendorRepository;




    @Nested
    @DisplayName("Source Tests")
    class sourceTests{
        //re-used objects in methods (as input to test methods)
        private ProcurementSourceDto dto;
        private ProcurementSource procurementSource;
        @BeforeEach
        void setup(){
            //initializing the objects required commonly by methods
            //dto object
            this.dto = new ProcurementSourceDto("102", "Government Funding");
            //object - setting the id value also
            this.procurementSource = new ProcurementSource();
            this.procurementSource.setId(1L);
            this.procurementSource.setName("102");
            this.procurementSource.setDescription("Government Funding");

        }

        //Create source method:
        //case 01 - name doesn't exist, successfully create & return dto
        //case 02 - name already exists, throws error
        //case 03 - name in dto is null, throws error
        @Test
        @DisplayName("Create source - successfully when valid name exists")
        void testCreateSource_Success(){
            //Arrange - input for service method - setup already

                //mock to return Optional.empty()
            when(procurementSourceRepository.findFirstByName(dto.getName()))
                    .thenReturn(Optional.empty());
                //mock save() to return an entity
            when(procurementSourceRepository.save(any(ProcurementSource.class)))
                    .thenReturn(procurementSource);

            //Act - call to service method
                //(the method returns an object)
            ProcurementSourceDto result = adminService.createSource(dto);

            //Assert - expected results comparison
                //(checking the returned created object is not null)
            assertNotNull(result);
            assertEquals(procurementSource.getId(), result.getId());
            assertEquals(dto.getName(), result.getName());
            assertEquals(dto.getDescription(), result.getDescription());


            //(verify all steps in the method)
            verify(procurementSourceRepository, times(1)).findFirstByName(dto.getName());
//            verify(procurementSourceRepository).save(any(ProcurementSource.class));

            //verify if the name and descriptions are set when saved
            //capture the object passed into save()
//            ArgumentCaptor<ProcurementSource> captor = ArgumentCaptor.forClass(ProcurementSource.class);
//            verify(procurementSourceRepository).save(captor.capture());
//            //get the value of passed object
//            ProcurementSource capturedSource = captor.getValue();
//            //verify steps of setting name & description with argument passed into save()
//            assertEquals(dto.getName(), capturedSource.getName());
//            assertEquals(dto.getDescription(), capturedSource.getDescription());


            verify(procurementSourceRepository, times(1)).save(
                    argThat(source -> source.getName().equals(dto.getName()) && source.getDescription().equals(dto.getDescription()))
            );
        }

        @Test
        @DisplayName("Create Source - Throw Exception when source name already exists")
        void testCreateSource_ThrowsWhenNameAlreadyExists(){
            //Arrange
             //name already exists
            when(procurementSourceRepository.findFirstByName(dto.getName()))
                    .thenReturn(Optional.of(procurementSource));
            //Act & Assert
            final EntityExistsException exception = assertThrows(EntityExistsException.class, () -> {
                adminService.createSource(dto);
            });
            assertEquals("Source name already exists", exception.getMessage());
            //no interaction with repository save() method
            verify(procurementSourceRepository,never()).save(any());

        }

        @Test
        @DisplayName("Create Source - Throw Exception when source name is null")
        void testCreateSource_ThrowsWhenNameIsNull(){
            //Arrange -
            // no values for name in dto
           ProcurementSourceDto nullNameDto = new ProcurementSourceDto();
            nullNameDto.setDescription("Funding from China");

            //Act & Assert
            final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                adminService.createSource(nullNameDto);
            });
            assertEquals("Source should have a name", exception.getMessage());
            //no interactions with repository
            verifyNoInteractions(procurementSourceRepository);
        }


        //Update source method:
        //case 01: source id exists, name doesn't exist, successfully updates & return dto
        //case 02: source id doesn't exist, throws error
        //case 02: new name already exists & not the same, throws error
        @Test
        @DisplayName("Update source - successfully when id & valid name exist")
        void testUpdateSource_Success(){
            //ARRANGE:
            Long sourceId = 1L;
            //updating dto object
            ProcurementSourceDto updatingDto = new ProcurementSourceDto();
            updatingDto.setName("105");
            updatingDto.setDescription("International Fund");

            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.of(procurementSource));
            when(procurementSourceRepository.findFirstByName(updatingDto.getName()))
                    .thenReturn(Optional.empty());
            when(procurementSourceRepository.save(any(ProcurementSource.class)))
                    .thenReturn(procurementSource);

            //ACT:
            ProcurementSourceDto result = adminService.updateSource(sourceId,updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(sourceId, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getDescription(), result.getDescription());

            //VERIFY:
            verify(procurementSourceRepository).findById(sourceId);
            verify(procurementSourceRepository).findFirstByName(updatingDto.getName());
            verify(procurementSourceRepository).save(any(ProcurementSource.class));

        }

        @Test
        @DisplayName("Update source - successfully when it has the same name")
        void testUpdateSource_SuccessWithSameName(){
            //ARRANGE:
            Long sourceId = 1L;
            //updating dto object
            ProcurementSourceDto updatingDto = new ProcurementSourceDto();
            updatingDto.setName("102");  //same as the object above
            updatingDto.setDescription("International Fund");

            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.of(procurementSource));

            when(procurementSourceRepository.save(any(ProcurementSource.class)))
                    .thenReturn(procurementSource);

            //ACT:
            ProcurementSourceDto result = adminService.updateSource(sourceId,updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(sourceId, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getDescription(), result.getDescription());

            //VERIFY:
            verify(procurementSourceRepository).findById(sourceId);
            //this step should not be called
            verify(procurementSourceRepository, never()).findFirstByName(updatingDto.getName());
            verify(procurementSourceRepository).save(any(ProcurementSource.class));

        }

        @Test
        @DisplayName("Update source - successfully when it has the same name but with different cases")
        void testUpdateSource_SuccessWithSameNameDifferentCases(){
            //ARRANGE:
            Long sourceId = 2L;
            //existing object
            ProcurementSource existing = new ProcurementSource();
            existing.setId(2L);
            existing.setName("donor");
            existing.setDescription("Donor is mentioned");

            //updating dto object
            ProcurementSourceDto updatingDto = new ProcurementSourceDto();
            updatingDto.setName("Donor");  //same as the object above but with different case
            updatingDto.setDescription("International Fund");

            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.of(existing));

            when(procurementSourceRepository.save(any(ProcurementSource.class)))
                    .thenReturn(existing);

            //ACT:
            ProcurementSourceDto result = adminService.updateSource(sourceId,updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(sourceId, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getDescription(), result.getDescription());

            //VERIFY:
            verify(procurementSourceRepository).findById(sourceId);
            //this step should not be called
            verify(procurementSourceRepository, never()).findFirstByName(updatingDto.getName());
            verify(procurementSourceRepository).save(any(ProcurementSource.class));

        }

        @Test
        @DisplayName("Update Source - Throw exception when source id doesn't exist")
        void testUpdateSource_throwsWhenNonExistingSourceId(){
            //ARRANGE:
            Long sourceId = 1L;
            //updating dto object
            ProcurementSourceDto updatingDto = new ProcurementSourceDto();
            updatingDto.setName("105");
            updatingDto.setDescription("International Fund");

            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateSource(sourceId, updatingDto));

            assertEquals("Source not found", exception.getMessage());
            //not executing save method
            verify(procurementSourceRepository,never()).save(any(ProcurementSource.class));

        }

        @Test
        @DisplayName("Update Source - Throw exception when name already exist")
        void testUpdateSource_throwsWhenNameAlreadyExists(){
            //ARRANGE:
            Long sourceId = 1L;
            //updating dto object
            ProcurementSourceDto updatingDto = new ProcurementSourceDto();
            updatingDto.setName("105");
            updatingDto.setDescription("International Fund");

            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.of(procurementSource));

            when(procurementSourceRepository.findFirstByName(updatingDto.getName()))
                    .thenReturn(Optional.of(procurementSource));

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateSource(sourceId, updatingDto));

            assertEquals("Source name already exists", exception.getMessage());
            //not executing save method
            verify(procurementSourceRepository,never()).save(any(ProcurementSource.class));

        }

        //Get all sources method:
        //case 01 - sources exists, successfully return as a list of dto
        //case 02 - sources don't exist, returns empty list
        @Test
        @DisplayName("Get All Sources - Successfully returns sources list")
        void testGetAllSources_Success(){
            //ARRANGE:
            ProcurementSource source2 = new ProcurementSource(3L, "100","Other");
            List<ProcurementSource> sourcesList = new ArrayList<>();
            sourcesList.add(procurementSource); //name = 102
            sourcesList.add(source2); //name = 100

            when(procurementSourceRepository.findAll())
                    .thenReturn(sourcesList);
            //ACT:
            List<ProcurementSourceDto> result = adminService.getAllSources();

            //ASSERT:
            assertNotNull(result);
            assertEquals(2,result.size());
            //make sure it is sorted
            assertEquals("100", result.get(0).getName());
            assertEquals("102", result.get(1).getName());

            //VERIFY:
            verify(procurementSourceRepository).findAll();

        }
        @Test
        @DisplayName("Get All Sources - when no sources returns empty list")
        void testGetAllSources_EmptyList(){
            //ARRANGE:
            when(procurementSourceRepository.findAll())
                    .thenReturn(List.of());

            //ACT:
            List<ProcurementSourceDto> result = adminService.getAllSources();

            //ASSERT:
            assertNotNull(result);
            assertTrue(result.isEmpty());

            //VERIFY:
            verify(procurementSourceRepository).findAll();
        }


        //Get source by id method:
        //case 01 - source id exists, successfully return as dto
        //case 02 - source id doesn't exist, throws error
        @Test
        @DisplayName("Get Source By Id - Successfully returns source")
        void testGetSourceById_Success(){
            //ARRANGE:
            Long sourceId = 1L;
            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.of(procurementSource));
            //ACT:
            ProcurementSourceDto result = adminService.getSourceById(sourceId);
            //ASSERT:
            assertNotNull(result);
            assertEquals(sourceId, result.getId());
            assertEquals(procurementSource.getName(), result.getName());
            assertEquals(procurementSource.getDescription(),result.getDescription());
            //VERIFY:
            verify(procurementSourceRepository).findById(sourceId);

        }

        @Test
        @DisplayName("Get Source By Id - Throw exception when id doesn't exist")
        void testGetSourceById_throwsWhenIdDoesNotExist(){
            //ARRANGE:
            Long sourceId = 1L;
            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.empty());
            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.getSourceById(sourceId));

            assertEquals("Source not found", exception.getMessage());

            //VERIFY:
            verify(procurementSourceRepository).findById(sourceId);
        }

        //Delete source by id method:
        //case 01 - source id exists, successfully delete source
        //case 02 - source id doesn't exist, throws error

        @Test
        @DisplayName("Delete Source By Id - Successfully when id exists")
        void testDeleteSourceById_Success(){
            //ARRANGE:
            Long sourceId = 1L;
            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.of(procurementSource));

            doNothing().when(procurementSourceRepository).deleteById(sourceId);

            //ACT:
            adminService.deleteSourceById(sourceId);

            //VERIFY:
            verify(procurementSourceRepository).findById(sourceId);
            verify(procurementSourceRepository).deleteById(sourceId);
        }

        @Test
        @DisplayName("Delete Source By Id - Throws error when id doesn't exist")
        void testDeleteSourceById_throwsWhenIdDoesNotExist(){
            //ARRANGE:
            Long sourceId = 1L;
            when(procurementSourceRepository.findById(sourceId))
                    .thenReturn(Optional.empty());
            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.deleteSourceById(sourceId) );

            //ASSERT
            assertEquals("Source is not found", exception.getMessage());


            //VERIFY:
            verify(procurementSourceRepository).findById(sourceId);
            verify(procurementSourceRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Vendor Tests")
    class vendorTests{
        //re-used objects
        private Vendor vendor;
        private VendorDto dto;
        @BeforeEach
        void setup() throws ParseException {
            Date registeredDate = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-15");

            this.vendor = new Vendor();
            vendor.setId(1L);
            vendor.setName("Abans pvt Ltd");
            vendor.setRegisteredDate(registeredDate);
            vendor.setComments("Worked with in 2020");

            this.dto = new VendorDto();
            dto.setName("Abans pvt Ltd");
            dto.setRegisteredDate(registeredDate);
            dto.setComments("Worked with in 2020");
        }

        //Get vendor by id method:
        //case 01 - vendor id exists, successfully return as dto
        //case 02 - vendor id doesn't exist, throws error

        @Test
        @DisplayName("Get Vendor By Id - Successfully returns vendor")
        void testGetVendorById_Success(){
            //ARRANGE:
            Long vendorId = 1L;
            when(vendorRepository.findById(vendorId))
                    .thenReturn(Optional.of(vendor));
            //ACT:
            VendorDto result = adminService.getVendorById(vendorId);
            //ASSERT:
            assertNotNull(result);
            assertEquals(vendorId, result.getId());
            assertEquals(vendor.getName(), result.getName());
            assertEquals(vendor.getRegisteredDate(), result.getRegisteredDate());
            assertEquals(vendor.getComments(), result.getComments());

            //VERIFY:
            verify(vendorRepository).findById(vendorId);
        }

        @Test
        @DisplayName("Get Vendor By Id - Throw exception when id doesn't exist")
        void testGetVendorById_throwsWhenIdDoesNotExist(){
            //ARRANGE:
            Long vendorId = 1L;
            when(vendorRepository.findById(vendorId))
                    .thenReturn(Optional.empty());
            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.getVendorById(vendorId)
            );
            assertEquals("Vendor with id "+vendorId+ " is not found!", exception.getMessage());
            //VERIFY:
            verify(vendorRepository).findById(vendorId);
        }

        //Delete vendor by id method:
        //case 01 - vendor id exists, successfully delete vendor
        //case 02 - vendor id doesn't exist, throws error

        @Test
        @DisplayName("Delete Vendor By Id - Successfully when id exists")
        void testDeleteVendorById_Success(){
            //ARRANGE:
            Long vendorId = 1L;
            when(vendorRepository.findById(vendorId))
                    .thenReturn(Optional.of(vendor));

            doNothing().when(vendorRepository).deleteById(vendorId);

            //ACT:
            adminService.deleteVendor(vendorId);

            //VERIFY:
            verify(vendorRepository).findById(vendorId);
            verify(vendorRepository).deleteById(vendorId);
        }

        @Test
        @DisplayName("Delete Vendor By Id - Throws error when id doesn't exist")
        void testDeleteVendorById_throwsWhenIdDoesNotExist(){
            //ARRANGE:
            Long vendorId = 1L;
            when(vendorRepository.findById(vendorId))
                    .thenReturn(Optional.empty());
            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.deleteVendor(vendorId) );

            //ASSERT
            assertEquals("Vendor not found", exception.getMessage());


            //VERIFY:
            verify(vendorRepository).findById(vendorId);
            verify(vendorRepository, never()).deleteById(any());
        }

        //Get all vendor method:
        //case 01 - vendor exists, successfully return as a list of dto
        //case 02 - vendor don't exist, returns empty list
        @Test
        @DisplayName("Get All Vendors - Successfully returns vendor list")
        void testGetAllVendors_Success(){
            //ARRANGE:
            Vendor vendor2 = new Vendor();
            vendor2.setId(2L);
            vendor2.setName("Singer Pvt Ltd");
            List<Vendor> vendorList = new ArrayList<>();
            vendorList.add(vendor);
            vendorList.add(vendor2);

            when(vendorRepository.findAll())
                    .thenReturn(vendorList);

            //ACT:
            List<VendorDto> result = adminService.getVendors();

            //ASSERT:
            assertNotNull(result);
            assertEquals(2,result.size());
            //make sure it is sorted
            assertEquals("Abans pvt Ltd", result.get(0).getName());
            assertEquals("Singer Pvt Ltd", result.get(1).getName());

            //VERIFY:
            verify(vendorRepository).findAll();
        }
        @Test
        @DisplayName("Get All Vendors - when no vendor returns empty list")
        void testGetAllVendors_EmptyList(){
            //ARRANGE:
            when(vendorRepository.findAll())
                    .thenReturn(List.of());

            //ACT:
            List<VendorDto> result = adminService.getVendors();

            //ASSERT:
            assertNotNull(result);
            assertTrue(result.isEmpty());

            //VERIFY:
            verify(vendorRepository).findAll();
        }

        //Create vendor method:
        //case 01 - successfully create with date & return dto
        //case 02 - successfully create without date & return dto
        //case 03 - name is null, throws error

        @Test
        @DisplayName("Create vendor - successfully with date")
        void testCreateVendor_Success(){
            //ARRANGE:

            when(vendorRepository.save(any(Vendor.class)))
                    .thenReturn(vendor);
            //ACT:
            VendorDto result = adminService.createVendor(dto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(dto.getName(), result.getName());
            assertEquals(dto.getRegisteredDate(), result.getRegisteredDate());
            assertEquals(dto.getComments(), result.getComments());

            //VERIFY:
            verify(vendorRepository).save(
                    argThat(vendor -> vendor.getName().equals(dto.getName())
                    && vendor.getComments().equals(dto.getComments())
                    && vendor.getRegisteredDate().equals(dto.getRegisteredDate()))
            );
        }

        @Test
        @DisplayName("Create vendor - successfully without date")
        void testCreateVendor_SuccessWithoutDate(){
            //ARRANGE:
            //set date to null
            dto.setRegisteredDate(null);
            when(vendorRepository.save(any(Vendor.class)))
                    .thenReturn(vendor);
            //ACT:
            VendorDto result = adminService.createVendor(dto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(dto.getName(), result.getName());

            assertEquals(dto.getComments(), result.getComments());

            //VERIFY:
            //date should not be null
            verify(vendorRepository).save(
                    argThat(v -> v.getName().equals(dto.getName())
                            && v.getComments().equals(dto.getComments())
                            && v.getRegisteredDate() != null)
            );
        }

        @Test
        @DisplayName("Create Vendor - Throw Exception when name is null")
        void testCreateVendor_ThrowsWhenNameIsNull(){
            //ARRANGE:
            //name is null
            dto.setName(null);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createVendor(dto)
            );

            assertEquals("Name is required", exception.getMessage());

            //VERIFY:
            verifyNoInteractions(vendorRepository);
        }


        //Update vendor method:
        //case 01: vendor id exists, successfully updates with new date
        //case 02: vendor id exists, successfully updates without new date
        //case 03: vendor id doesn't exist, throws error


        @Test
        @DisplayName("Update vendor - successfully when id exists & has new date")
        void testUpdateVendor_SuccessWithNewDate() throws ParseException {
            //ARRANGE:
            Long vendorId = 1L;
            Date newRegisteredDate = new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-15");

            //updating dto
            VendorDto updatingDto = new VendorDto();
            updatingDto.setName("Maximus pvt Ltd");
            //has new date
            updatingDto.setRegisteredDate(newRegisteredDate);
            updatingDto.setComments("Office in Colombo 6");

            when(vendorRepository.findById(vendorId))
                    .thenReturn(Optional.of(vendor));

            when(vendorRepository.save(any(Vendor.class)))
                    .thenReturn(vendor);
            //ACT:
            VendorDto result = adminService.updateVendor(vendorId, updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(vendorId, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getRegisteredDate(), result.getRegisteredDate());
            assertEquals(updatingDto.getComments(), result.getComments());

            //VERIFY:
            verify(vendorRepository).findById(vendorId);
            verify(vendorRepository).save(any(Vendor.class));

        }
        @Test
        @DisplayName("Update vendor - successfully when id exists")
        void testUpdateVendor_SuccessWithoutNewDate(){
            //ARRANGE:
            Long vendorId = 1L;

            //updating dto
            VendorDto updatingDto = new VendorDto();
            updatingDto.setName("Maximus pvt Ltd");
            //no new date
            updatingDto.setComments("Office in Colombo 6");

            when(vendorRepository.findById(vendorId))
                    .thenReturn(Optional.of(vendor));

            when(vendorRepository.save(any(Vendor.class)))
                    .thenReturn(vendor);
            //ACT:
            VendorDto result = adminService.updateVendor(vendorId, updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(vendorId, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getComments(), result.getComments());

            //VERIFY:
            verify(vendorRepository).findById(vendorId);
            verify(vendorRepository).save(
                    argThat(v -> v.getRegisteredDate().equals(vendor.getRegisteredDate()))
            );


        }

        @Test
        @DisplayName("Update vendor - Throws when id doesn't exist")
        void testUpdateVendor_ThrowsWhenIdDoesNotExist(){
            //ARRANGE:
            Long vendorId = 1L;
            //updating dto
            VendorDto updatingDto = new VendorDto();
            updatingDto.setName("Maximus pvt Ltd");
            //no new date
            updatingDto.setComments("Office in Colombo 6");

            when(vendorRepository.findById(vendorId))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateVendor(vendorId, updatingDto));

            assertEquals("Vendor not found", exception.getMessage());

            verify(vendorRepository).findById(vendorId);
            //not executing save method
            verify(vendorRepository,never()).save(any(Vendor.class));
        }



    }

    @Nested
    @DisplayName("Status Tests")
    class statusTests{
        //re-used objects
        private ProcurementStatus status;
        private ProcurementStatusDto dto;

        @BeforeEach
        void setup(){
            //initializing re-used objects
            //object
            this.status = new ProcurementStatus();
            status.setId(1L);
            status.setName("Preparing Tender Documents");
            //dto
            this.dto = new ProcurementStatusDto();
            dto.setName("Preparing Tender Documents");
        }

        //Create status method:
        //case 01 - name doesn't exist, successfully create & return dto
        //case 02 - name already exists, throws error
        //case 03 - name in dto is null, throws error
        @Test
        @DisplayName("Create status - successfully when valid name exists")
        void testCreateStatus_Success(){
            //ARRANGE:
            when(procurementStatusRepository.findFirstByName(dto.getName()))
                    .thenReturn(Optional.empty());
            when(procurementStatusRepository.save(any(ProcurementStatus.class)))
                    .thenReturn(status);
            //ACT:
            ProcurementStatusDto result = adminService.createProcurementStatus(dto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(dto.getName(), result.getName());

            //VERIFY:
            verify(procurementStatusRepository).findFirstByName(dto.getName());
//            verify(procurementStatusRepository).save(any(ProcurementStatus.class));
            verify(procurementStatusRepository).save(
                    argThat(status -> status.getName().equals(dto.getName()))
            );
        }

        @Test
        @DisplayName("Create Status - Throw Exception when status name already exists")
        void testCreateStatus_ThrowsWhenNameAlreadyExists(){
            //ARRANGE:
            when(procurementStatusRepository.findFirstByName(dto.getName()))
                    .thenReturn(Optional.of(status));

            //ACT & ASSERT:
            EntityExistsException exception = assertThrows(EntityExistsException.class,
                    () -> adminService.createProcurementStatus(dto)
            );

            assertEquals("Status name already exists", exception.getMessage());

            //VERIFY:
            verify(procurementStatusRepository).findFirstByName(dto.getName());
            //not executed
            verify(procurementStatusRepository, never()).save(any(ProcurementStatus.class));
        }

        @Test
        @DisplayName("Create Status - Throw Exception when status name is null")
        void testCreateStatus_ThrowsWhenNameIsNull(){
            //ARRANGE:
            //name is null
            dto.setName(null);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createProcurementStatus(dto)
            );

            assertEquals("Status name is required", exception.getMessage());

            //VERIFY:
            verifyNoInteractions(procurementStatusRepository);
        }


        //Update status method:
        //case 01: status id exists, new name doesn't exist, successfully updates & return dto
        //case 02: status id exists, same name with different case, successfully updates
        //case 02: status id doesn't exist, throws error
        //case 03: new name already exists , throws error


        @Test
        @DisplayName("Update status - successfully when id & valid name exist")
        void testUpdateStatus_Success(){
            //ARRANGE:
            Long statusId = 1L;
            //updating dto object
            ProcurementStatusDto updatingDto = new ProcurementStatusDto();
            updatingDto.setName("Obtaining TEC approval");

            when(procurementStatusRepository.findById(statusId))
                    .thenReturn(Optional.of(status));
            when(procurementStatusRepository.findFirstByName(updatingDto.getName()))
                    .thenReturn(Optional.empty());
            when(procurementStatusRepository.save(any(ProcurementStatus.class)))
                    .thenReturn(status);

            //ACT:
            ProcurementStatusDto result = adminService.updateProcurementStatus(statusId,updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(statusId, result.getId());
            assertEquals(updatingDto.getName(), result.getName());

            //VERIFY:
            verify(procurementStatusRepository).findById(statusId);
            verify(procurementStatusRepository).findFirstByName(updatingDto.getName());
            verify(procurementStatusRepository).save(any(ProcurementStatus.class));
        }

        @Test
        @DisplayName("Update status - successfully when id exists & name in different case")
        void testUpdateStatus_SuccessSameName(){
            //ARRANGE:
            Long statusId = 1L;
            //updating dto object
            ProcurementStatusDto updatingDto = new ProcurementStatusDto();
            //same with different case
            updatingDto.setName("preparing tender documents");

            when(procurementStatusRepository.findById(statusId))
                    .thenReturn(Optional.of(status));
            //no need to check the name
            when(procurementStatusRepository.save(any(ProcurementStatus.class)))
                    .thenReturn(status);

            //ACT:
            ProcurementStatusDto result = adminService.updateProcurementStatus(statusId,updatingDto);

            //ASSERT:
            assertNotNull(result);
            assertEquals(statusId, result.getId());
            assertEquals(updatingDto.getName(), result.getName());

            //VERIFY:
            verify(procurementStatusRepository).findById(statusId);
            verify(procurementStatusRepository).save(any(ProcurementStatus.class));
            //not executed
            verify(procurementStatusRepository, never()).findFirstByName(updatingDto.getName());
        }

        @Test
        @DisplayName("Update status - Throws when status id doesn't exist")
        void testUpdateStatus_ThrowsWhenIdDoesNotExist(){
            //ARRANGE:
            Long statusId = 1L;
            //updating dto object
            ProcurementStatusDto updatingDto = new ProcurementStatusDto();
            updatingDto.setName("Obtaining TEC approval");

            when(procurementStatusRepository.findById(statusId))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateProcurementStatus(statusId, updatingDto));

            assertEquals("Status not found", exception.getMessage());
            //not executing save method
            verify(procurementStatusRepository, never()).findFirstByName(updatingDto.getName());
            verify(procurementStatusRepository,never()).save(any(ProcurementStatus.class));
        }
        @Test
        @DisplayName("Update status - Throws when name exists")
        void testUpdateStatus_ThrowsWhenNameExists(){
            //ARRANGE:
            Long statusId = 1L;
            //updating dto object
            ProcurementStatusDto updatingDto = new ProcurementStatusDto();
            //different name
            updatingDto.setName("Obtaining TEC approval");

            when(procurementStatusRepository.findById(statusId))
                    .thenReturn(Optional.of(status));
            //new name already exists
            when(procurementStatusRepository.findFirstByName(updatingDto.getName()))
                    .thenReturn(Optional.of(status));

            //ACT & ASSERT:
            EntityExistsException exception = assertThrows(EntityExistsException.class,
                    () -> adminService.updateProcurementStatus(statusId, updatingDto));

            assertEquals("Status name already exists", exception.getMessage());
            verify(procurementStatusRepository).findFirstByName(updatingDto.getName());
            //not executing save method
            verify(procurementStatusRepository,never()).save(any(ProcurementStatus.class));
        }

        //Get all status method:
        //case 01 - status exists, successfully return as a list of dto
        //case 02 - status don't exist, returns empty list
        @Test
        @DisplayName("Get All Status - Successfully returns status list")
        void testGetAllStatus_Success(){
            //ARRANGE:
            ProcurementStatus status2 = new ProcurementStatus();
            status2.setId(2L);
            status2.setName("Preparing Payment Voucher");
            List<ProcurementStatus> statusList = new ArrayList<>();
            statusList.add(status);
            statusList.add(status2);

            when(procurementStatusRepository.findAll())
                    .thenReturn(statusList);

            //ACT:
            List<ProcurementStatusDto> result = adminService.getProcurementStatus();

            //ASSERT:
            assertNotNull(result);
            assertEquals(2,result.size());
            //make sure it is sorted
            assertEquals("Preparing Payment Voucher", result.get(0).getName());
            assertEquals("Preparing Tender Documents", result.get(1).getName());

            //VERIFY:
            verify(procurementStatusRepository).findAll();
        }

        @Test
        @DisplayName("Get All Status - when no status returns empty list")
        void testGetAllStatus_EmptyList(){
            //ARRANGE:
            when(procurementStatusRepository.findAll())
                    .thenReturn(List.of());

            //ACT:
            List<ProcurementStatusDto> result = adminService.getProcurementStatus();

            //ASSERT:
            assertNotNull(result);
            assertTrue(result.isEmpty());

            //VERIFY:
            verify(procurementStatusRepository).findAll();
        }

        //Get status by id method:
        //case 01 - status id exists, successfully return as dto
        //case 02 - status id doesn't exist, throws error
        @Test
        @DisplayName("Get Status By Id - Successfully returns status")
        void testGetStatusById_Success(){
            //ARRANGE:
            Long statusId = 1L;
            when(procurementStatusRepository.findById(statusId))
                    .thenReturn(Optional.of(status));
            //ACT:
            ProcurementStatusDto result = adminService.getProcurementStatusById(statusId);
            //ASSERT:
            assertNotNull(result);
            assertEquals(statusId, result.getId());
            assertEquals(status.getName(), result.getName());

            //VERIFY:
            verify(procurementStatusRepository).findById(statusId);
        }

        @Test
        @DisplayName("Get Status By Id - Throw exception when id doesn't exist")
        void testGetStatusById_throwsWhenIdDoesNotExist(){
            //ARRANGE:
            Long statusId = 1L;
            when(procurementStatusRepository.findById(statusId))
                    .thenReturn(Optional.empty());
            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.getProcurementStatusById(statusId)
            );
            assertEquals("Procurement status with id "+statusId+ " is not found!", exception.getMessage());
            //VERIFY:
            verify(procurementStatusRepository).findById(statusId);
        }

        //Delete status by id method:
        //case 01 - status id exists, successfully delete status
        //case 02 - status id doesn't exist, throws error
        @Test
        @DisplayName("Delete Status By Id - Successfully when id exists")
        void testDeleteStatusById_Success(){
            //ARRANGE:
            Long statusId = 1L;
            when(procurementStatusRepository.findById(statusId))
                    .thenReturn(Optional.of(status));

            doNothing().when(procurementStatusRepository).deleteById(statusId);

            //ACT:
            adminService.deleteProcurementStatus(statusId);

            //VERIFY:
            verify(procurementStatusRepository).findById(statusId);
            verify(procurementStatusRepository).deleteById(statusId);
        }
        @Test
        @DisplayName("Delete Status By Id - Throws error when id doesn't exist")
        void testDeleteStatusById_throwsWhenIdDoesNotExist(){
            //ARRANGE:
            Long statusId = 1L;
            when(procurementStatusRepository.findById(statusId))
                    .thenReturn(Optional.empty());
            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.deleteProcurementStatus(statusId) );

            //ASSERT
            assertEquals("Status not found", exception.getMessage());


            //VERIFY:
            verify(procurementStatusRepository).findById(statusId);
            verify(procurementStatusRepository, never()).deleteById(any());
        }

    }


    @Nested
    @DisplayName("User Test")
    class userTests {
        //re-used objects
        private User user;
        private UserDto userDto;
        //other objects needed
        private Designation designationForUser;
        private Subdiv subdivForUser;
        private Admindiv admindivForUser;

        @BeforeEach
        void setup() {
            //designation object for admin div
            Designation designationForAdmindiv = new Designation();
            designationForAdmindiv.setId(1L);
            designationForAdmindiv.setTitle("Senior Registrar");
            designationForAdmindiv.setCode("SR");
            //designation for user
            this.designationForUser = new Designation();
            designationForUser.setId(2L);
            designationForUser.setTitle("Subject Clerk");
            designationForUser.setCode("SF");
            //admin div object for user
            this.admindivForUser = new Admindiv();
            admindivForUser.setId(1L);
            admindivForUser.setName("Faculty of Science");
            admindivForUser.setCode("FOS");
            admindivForUser.setResponsibleDesignation(designationForAdmindiv);
            //sub div object for user
            this.subdivForUser = new Subdiv();
            subdivForUser.setId(1L);
            subdivForUser.setName("Department of Mathematics");
            subdivForUser.setCode("FOS-DOM");
            subdivForUser.setAdmindiv(admindivForUser);
            //user object
            this.user = new User();
            user.setId(1L);
            user.setName("Dhanushka");
            user.setEmail("danu@gmail.com");
            user.setNic("22992929");
            user.setSubdiv(subdivForUser);
            user.setAdmindiv(admindivForUser);
            user.setDesignation(designationForUser);
            user.setUserRole(UserRole.SUBDIVUSER);
            //dto
            this.userDto = new UserDto();
            userDto.setName("Dhanushka");
            userDto.setEmail("danu@gmail.com");
            userDto.setNic("22992929");
            userDto.setSubdivId(1L);
            userDto.setAdmindivId(1L);
            userDto.setDesignationId(2L);
            userDto.setUserRole(UserRole.SUBDIVUSER);
        }

        //Get  by id method:
        //case 01 - id exists, successfully return as dto
        //case 02 - id doesn't exist, throws error
        @Test
        @DisplayName("Get User by Id - when id exists")
        void testGetUserById_Success() {
            //ARRANGE:
            Long id = 1L;
            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            //ACT:
            UserDto result = adminService.getUserById(id);

            //ASSERT:
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(user.getEmail(), result.getEmail());
            assertEquals(user.getName(), result.getName());
            assertEquals(user.getNic(), result.getNic());
            assertEquals(user.getUserRole(), result.getUserRole());
            assertEquals(user.getSubdiv().getId(), result.getSubdivId());
            assertEquals(user.getAdmindiv().getId(), result.getAdmindivId());
            assertEquals(user.getDesignation().getId(), result.getDesignationId());

            //VERIFY:
            verify(userRepository).findById(id);
        }

        @Test
        @DisplayName("Get User by Id - Throws when id doesn't exist")
        void testGetUserById_WhenIdDoesNotExist() {
            //ARRANGE:
            Long id = 1L;
            when(userRepository.findById(id))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.getUserById(id)
            );

            //ASSERT:
            assertEquals("User with id " + id + " is not found!", exception.getMessage());

            //VERIFY:
            verify(userRepository).findById(id);
        }

        //Delete by id method:
        //case 01 -  id exists, successfully delete
        //case 02 -  id doesn't exist, throws error
        @Test
        @DisplayName("Delete User- Successfully when id exists")
        void testDeleteUser_WhenIdExists() {
            //ARRANGE:
            Long id = 1L;
            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            //returns nothing by the method
            doNothing().when(userRepository).deleteById(id);

            //ACT:
            adminService.deleteUser(id);

            //VERIFY:
            verify(userRepository).findById(id);
            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("Delete User - Throws when id doesn't exist")
        void testDeleteUser_WhenIdDoesNotExist() {
            //ARRANGE:
            Long id = 1L;
            when(userRepository.findById(id))
                    .thenReturn(Optional.empty());

            //ACT & Assert:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.deleteUser(id)
            );

            assertEquals("User with id " + id + " is not found!", exception.getMessage());

            //VERIFY:
            verify(userRepository).findById(id);
            verify(userRepository, never()).deleteById(any());
        }


        //Get all  method:
        //case 01 - exists, successfully return as a list of dto
        //case 02 - returns empty list
        @Test
        @DisplayName("Get All Users - Successfully returns a list")
        void testGetAllUsers_Success() {
            //ARRANGE:
            User user2 = new User();
            user2.setId(2L);
            user2.setName("Dilshan");
            user2.setEmail("dilshan@gmail.com");
            user2.setNic("2299292922");
            user2.setSubdiv(subdivForUser);
            user2.setAdmindiv(admindivForUser);
            user2.setDesignation(designationForUser);
            user2.setUserRole(UserRole.ADMINDIVUSER);

            List<User> userList = new ArrayList<>();
            userList.add(user);
            userList.add(user2);

            when(userRepository.findAll())
                    .thenReturn(userList);
            //ACT:
            List<UserDto> result = adminService.getUsers();

            //ASSERT:
            assertNotNull(result);
            assertEquals(2,result.size());
            //make sure it is sorted
            assertEquals("danu@gmail.com", result.get(0).getEmail());
            assertEquals("dilshan@gmail.com", result.get(1).getEmail());

            //VERIFY:
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Get All Users - when returns an empty list")
        void testGetAllUsers_EmptyList(){
            //ARRANGE:
            when(userRepository.findAll())
                    .thenReturn(List.of());

            //ACT:
            List<UserDto> result = adminService.getUsers();

            //ASSERT:
            assertNotNull(result);
            assertTrue(result.isEmpty());

            //VERIFY:
            verify(userRepository).findAll();
        }

        //Create User Method:
        //Case 01: email doesn't exist, subdiv, admindiv, & designation not null, & exist, successfully create
        //Case 02: email exists, throws error
        //Case 03: nic is null, throws error
        //case 04: subdiv id is null, throws error
        //case 05: admindiv id is null, throws error
        //case 06: designation id is null, throws error
        //case 07: subdiv doesn't exist, throws error
        //case 08: admindiv doesn't exist, throws error
        //case 09: designation doesn't exist, throws error

        @Test
        @DisplayName("Create User - Successfully when email doesn't exist & subdiv, admindiv, & designation exist")
        void testCreateUser_Success(){
            //ARRANGE:
            //no existent email (has unique constraint on email)
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.empty());

            when(subdivRepository.findById(userDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));

            when(admindivRepository.findById(userDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForUser));

            when(designationRepository.findById(userDto.getDesignationId()))
                    .thenReturn(Optional.of(designationForUser));


            //return object with id when save() called
            when(userRepository.save(any(User.class)))
                    .thenReturn(user);
            //ACT:
            UserDto result = null;
            try {
                result = adminService.createUser(userDto);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //ASSERT:
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(userDto.getEmail(), result.getEmail());
            assertEquals(userDto.getName(), result.getName());
            assertEquals(userDto.getUserRole(), result.getUserRole());

            assertEquals(userDto.getSubdivId(), result.getSubdivId());
            assertEquals(userDto.getAdmindivId(), result.getAdmindivId());
            assertEquals(userDto.getDesignationId(), result.getDesignationId());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            verify(subdivRepository).findById(userDto.getSubdivId());
            verify(admindivRepository).findById(userDto.getAdmindivId());
            verify(designationRepository).findById(userDto.getDesignationId());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Create User - Throws error when email exists")
        void testCreateUser_ThrowsWhenEmailExists(){
            //ARRANGE:
            //email already exists
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.of(user));

            //ACT:
            EntityExistsException exception = assertThrows(EntityExistsException.class,
                    () -> adminService.createUser(userDto)
            );
            //ASSERT:
            assertEquals("Email already exists", exception.getMessage());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            //not executed
            verify(subdivRepository, never()).findById(userDto.getSubdivId());
            verify(admindivRepository, never()).findById(userDto.getAdmindivId());
            verify(designationRepository, never()).findById(userDto.getDesignationId());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Create User - Throws error when nic is null")
        void testCreateUser_ThrowsWhenNICIsNull(){
            //ARRANGE:
            //nic is null
            userDto.setNic(null);
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.empty());

            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createUser(userDto)
            );
            //ASSERT:
            assertEquals("NIC is required", exception.getMessage());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            //not executed
            verify(subdivRepository, never()).findById(userDto.getSubdivId());
            verify(admindivRepository, never()).findById(userDto.getAdmindivId());
            verify(designationRepository, never()).findById(userDto.getDesignationId());
            verify(userRepository, never()).save(any());
        }
        @Test
        @DisplayName("Create User - Throws error when subdiv id is null")
        void testCreateUser_ThrowsWhenSubdivIdIsNull(){
            //ARRANGE:
            //sub div id is null
            userDto.setSubdivId(null);
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.empty());

            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createUser(userDto)
            );
            //ASSERT:
            assertEquals("Sub division, Admin division & Designation are required", exception.getMessage());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            //not executed
            verify(subdivRepository, never()).findById(userDto.getSubdivId());
            verify(admindivRepository, never()).findById(userDto.getAdmindivId());
            verify(designationRepository, never()).findById(userDto.getDesignationId());
            verify(userRepository, never()).save(any());
        }
        @Test
        @DisplayName("Create User - Throws error when admindiv id is null")
        void testCreateUser_ThrowsWhenAdmindivIdIsNull(){
            //ARRANGE:
            //admin div id is null
            userDto.setAdmindivId(null);
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.empty());

            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createUser(userDto)
            );
            //ASSERT:
            assertEquals("Sub division, Admin division & Designation are required", exception.getMessage());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            //not executed
            verify(subdivRepository, never()).findById(userDto.getSubdivId());
            verify(admindivRepository, never()).findById(userDto.getAdmindivId());
            verify(designationRepository, never()).findById(userDto.getDesignationId());
            verify(userRepository, never()).save(any());
        }
        @Test
        @DisplayName("Create User - Throws error when designation id is null")
        void testCreateUser_ThrowsWhenDesignationIdIsNull(){
            //ARRANGE:
            //designation id is null
            userDto.setDesignationId(null);
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.empty());

            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createUser(userDto)
            );
            //ASSERT:
            assertEquals("Sub division, Admin division & Designation are required", exception.getMessage());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            //not executed
            verify(subdivRepository, never()).findById(userDto.getSubdivId());
            verify(admindivRepository, never()).findById(userDto.getAdmindivId());
            verify(designationRepository, never()).findById(userDto.getDesignationId());
            verify(userRepository, never()).save(any());
        }
        @Test
        @DisplayName("Create User - Throws error when subdiv doesn't exist")
        void testCreateUser_ThrowsWhenSubdivDoesNotExist(){
            //ARRANGE:
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.empty());
            when(subdivRepository.findById(userDto.getSubdivId()))
                    .thenReturn(Optional.empty());

            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.createUser(userDto)
            );
            //ASSERT:
            assertEquals("Sub division not found!", exception.getMessage());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            verify(subdivRepository).findById(userDto.getSubdivId());
            //not executed
            verify(admindivRepository, never()).findById(userDto.getAdmindivId());
            verify(designationRepository, never()).findById(userDto.getDesignationId());
            verify(userRepository, never()).save(any());
        }
        @Test
        @DisplayName("Create User - Throws error when admindiv doesn't exist")
        void testCreateUser_ThrowsWhenAdmindivDoesNotExist(){
            //ARRANGE:
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.empty());
            when(subdivRepository.findById(userDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));
            when(admindivRepository.findById(userDto.getAdmindivId()))
                    .thenReturn(Optional.empty());

            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.createUser(userDto)
            );
            //ASSERT:
            assertEquals("Admin division not found!", exception.getMessage());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            verify(subdivRepository).findById(userDto.getSubdivId());
            verify(admindivRepository).findById(userDto.getAdmindivId());
            //not executed
            verify(designationRepository, never()).findById(userDto.getDesignationId());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Create User - Throws error when designation doesn't exist")
        void testCreateUser_ThrowsWhenDesignationDoesNotExist(){
            //ARRANGE:
            when(userRepository.findFirstByEmail(userDto.getEmail()))
                    .thenReturn(Optional.empty());
            when(subdivRepository.findById(userDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));
            when(admindivRepository.findById(userDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForUser));
            when(designationRepository.findById(userDto.getDesignationId()))
                    .thenReturn(Optional.empty());

            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.createUser(userDto)
            );
            //ASSERT:
            assertEquals("Designation not found!", exception.getMessage());
            //VERIFY:
            verify(userRepository).findFirstByEmail(userDto.getEmail());
            verify(subdivRepository).findById(userDto.getSubdivId());
            verify(admindivRepository).findById(userDto.getAdmindivId());
            verify(designationRepository).findById(userDto.getDesignationId());
            //not executed
            verify(userRepository, never()).save(any());
        }

        //Update User Method:
        //Case 01: user, subdiv, admindiv, designation  don't exist, different email doesn't exist successfully update
        //Case 02: email same, successfully update
        //Case 03: email same, new password, successfully update
        //Case 04: different email, new password, successfully update
        //case 05: user doesn't exist, throws error
        //Case 06: user, subdiv, admindiv, designation exist, email not same & exists, throws error
        //Case 07: user exists, subdiv doesn't exist, throws error
        //Case 08: user, subdiv exist, admindiv doesn't exist, throws error
        //Case 09: user, subdiv, admindiv exist, designation doesn't exist, throws error


        @Test
        @DisplayName("Update User - Successfully when user, subdiv, admindiv, designation don't exist")
        void testUpdateUser_SuccessSameEmail() throws IOException {
            //ARRANGE
            Long id = 1L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //same email
            updatingDto.setEmail("danu@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);

            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            when(subdivRepository.findById(updatingDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForUser));
            when(designationRepository.findById(updatingDto.getDesignationId()))
                    .thenReturn(Optional.of(designationForUser));
            when(userRepository.save(any(User.class)))
                    .thenReturn(user);
            //ACT
            UserDto result = adminService.updateUser(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getEmail(),result.getEmail());
            assertEquals(updatingDto.getUserRole(), result.getUserRole());
            assertEquals(updatingDto.getSubdivId(), result.getSubdivId());
            assertEquals(updatingDto.getAdmindivId(), result.getAdmindivId());
            assertEquals(updatingDto.getDesignationId(), result.getDesignationId());

            //VERIFY
            verify(userRepository).findById(id);
            verify(subdivRepository).findById(updatingDto.getSubdivId());
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            verify(designationRepository).findById(updatingDto.getDesignationId());
            verify(userRepository).save(any(User.class));
            //not executed
            verify(userRepository, never()).findFirstByEmail(any());
        }
        @Test
        @DisplayName("Update User - Successfully when user, subdiv, admindiv, designation, & email don't exist")
        void testUpdateUser_SuccessNotSameEmail(){
            //ARRANGE
            Long id = 1L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //different email
            updatingDto.setEmail("tissia@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);

            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            when(userRepository.findFirstByEmail(updatingDto.getEmail()))
                    .thenReturn(Optional.empty());
            when(subdivRepository.findById(updatingDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForUser));
            when(designationRepository.findById(updatingDto.getDesignationId()))
                    .thenReturn(Optional.of(designationForUser));
            when(userRepository.save(any(User.class)))
                    .thenReturn(user);
            //ACT
            UserDto result = null;
            try {
                result = adminService.updateUser(id,updatingDto);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getEmail(),result.getEmail());
            assertEquals(updatingDto.getUserRole(), result.getUserRole());
            assertEquals(updatingDto.getSubdivId(), result.getSubdivId());
            assertEquals(updatingDto.getAdmindivId(), result.getAdmindivId());
            assertEquals(updatingDto.getDesignationId(), result.getDesignationId());

            //VERIFY
            verify(userRepository).findById(id);
            verify(userRepository).findFirstByEmail(updatingDto.getEmail());
            verify(subdivRepository).findById(updatingDto.getSubdivId());
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            verify(designationRepository).findById(updatingDto.getDesignationId());
            verify(userRepository).save(any(User.class));
        }
        @Test
        @DisplayName("Update User - Successfully with new password & when user, subdiv, admindiv, designation, don't exist")
        void testUpdateUser_SuccessSameEmailNewPassword(){
            //ARRANGE
            Long id = 1L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //same email
            updatingDto.setEmail("danu@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);
            //set a password
            updatingDto.setPassword("tissaia2323");

            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            //no need to check email
            when(subdivRepository.findById(updatingDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForUser));
            when(designationRepository.findById(updatingDto.getDesignationId()))
                    .thenReturn(Optional.of(designationForUser));
            when(userRepository.save(any(User.class)))
                    .thenReturn(user);
            //ACT
            UserDto result = null;
            try {
                result = adminService.updateUser(id,updatingDto);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getEmail(),result.getEmail());
            assertEquals(updatingDto.getUserRole(), result.getUserRole());
            assertEquals(updatingDto.getSubdivId(), result.getSubdivId());
            assertEquals(updatingDto.getAdmindivId(), result.getAdmindivId());
            assertEquals(updatingDto.getDesignationId(), result.getDesignationId());

            //VERIFY
            verify(userRepository).findById(id);
            verify(subdivRepository).findById(updatingDto.getSubdivId());
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            verify(designationRepository).findById(updatingDto.getDesignationId());
            verify(userRepository).save(any(User.class));
            //not executed
            verify(userRepository, never()).findFirstByEmail(updatingDto.getEmail());
        }
        @Test
        @DisplayName("Update User - Successfully with new password & when user, subdiv, admindiv, designation, & email don't exist")
        void testUpdateUser_SuccessDifferentEmailNewPassword(){
            //ARRANGE
            Long id = 1L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //different email
            updatingDto.setEmail("tissia@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);
            //update password
            updatingDto.setPassword("tissaia2323");

            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            when(userRepository.findFirstByEmail(updatingDto.getEmail()))
                    .thenReturn(Optional.empty());
            when(subdivRepository.findById(updatingDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForUser));
            when(designationRepository.findById(updatingDto.getDesignationId()))
                    .thenReturn(Optional.of(designationForUser));
            when(userRepository.save(any(User.class)))
                    .thenReturn(user);
            //ACT
            UserDto result = null;
            try {
                result = adminService.updateUser(id,updatingDto);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getName(), result.getName());
            assertEquals(updatingDto.getEmail(),result.getEmail());
            assertEquals(updatingDto.getUserRole(), result.getUserRole());
            assertEquals(updatingDto.getSubdivId(), result.getSubdivId());
            assertEquals(updatingDto.getAdmindivId(), result.getAdmindivId());
            assertEquals(updatingDto.getDesignationId(), result.getDesignationId());

            //VERIFY
            verify(userRepository).findById(id);
            verify(userRepository).findFirstByEmail(updatingDto.getEmail());
            verify(subdivRepository).findById(updatingDto.getSubdivId());
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            verify(designationRepository).findById(updatingDto.getDesignationId());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Update User - Throws error when user id doesn't exist")
        void testUpdateUser_ThrowsWhenIdDoesNotExist(){
            //ARRANGE
            Long id = 4L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //different email
            updatingDto.setEmail("tissia@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);

            when(userRepository.findById(id))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateUser(id, updatingDto)
            );

            assertEquals("User not found!", exception.getMessage());
            verify(userRepository).findById(id);
            //not executed
            verify(userRepository, never()).findFirstByEmail(updatingDto.getEmail());
            verify(subdivRepository, never()).findById(updatingDto.getSubdivId());
            verify(admindivRepository, never()).findById(updatingDto.getAdmindivId());
            verify(designationRepository, never()).findById(updatingDto.getDesignationId());
            verify(userRepository, never()).save(any(User.class));
        }
        @Test
        @DisplayName("Update User - Throws error when email exists")
        void testUpdateUser_ThrowsWhenEmailExists(){
            //ARRANGE
            Long id = 4L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //different email
            updatingDto.setEmail("tissia@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);

            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            //email exists
            when(userRepository.findFirstByEmail(updatingDto.getEmail()))
                    .thenReturn(Optional.of(user));

            //ACT & ASSERT:
            EntityExistsException exception = assertThrows(EntityExistsException.class,
                    () -> adminService.updateUser(id, updatingDto)
            );

            assertEquals("Email already exists", exception.getMessage());
            verify(userRepository).findById(id);
            verify(userRepository).findFirstByEmail(updatingDto.getEmail());
            //not executed
            verify(subdivRepository, never()).findById(updatingDto.getSubdivId());
            verify(admindivRepository, never()).findById(updatingDto.getAdmindivId());
            verify(designationRepository, never()).findById(updatingDto.getDesignationId());
            verify(userRepository, never()).save(any(User.class));
        }
        @Test
        @DisplayName("Update User - Throws error when subdiv id doesn't exist")
        void testUpdateUser_ThrowsWhenSubdivIdDoesNotExist(){
            //ARRANGE
            Long id = 4L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //different email
            updatingDto.setEmail("tissia@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);

            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            when(userRepository.findFirstByEmail(updatingDto.getEmail()))
                    .thenReturn(Optional.empty());
            //sub div doesn't exist
            when(subdivRepository.findById(updatingDto.getSubdivId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateUser(id, updatingDto)
            );

            assertEquals("Sub division not found!", exception.getMessage());
            verify(userRepository).findById(id);
            verify(userRepository).findFirstByEmail(updatingDto.getEmail());
            verify(subdivRepository).findById(updatingDto.getSubdivId());
            //not executed
            verify(admindivRepository, never()).findById(updatingDto.getAdmindivId());
            verify(designationRepository, never()).findById(updatingDto.getDesignationId());
            verify(userRepository, never()).save(any(User.class));
        }
        @Test
        @DisplayName("Update User - Throws error when admindiv id doesn't exist")
        void testUpdateUser_ThrowsWhenAdmindivIdDoesNotExist(){
            //ARRANGE
            Long id = 4L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //different email
            updatingDto.setEmail("tissia@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);

            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            when(userRepository.findFirstByEmail(updatingDto.getEmail()))
                    .thenReturn(Optional.empty());
            when(subdivRepository.findById(updatingDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));
            //admin div doesn't exist
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.empty());


            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateUser(id, updatingDto)
            );

            assertEquals("Admin division not found!", exception.getMessage());
            verify(userRepository).findById(id);
            verify(userRepository).findFirstByEmail(updatingDto.getEmail());
            verify(subdivRepository).findById(updatingDto.getSubdivId());
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            //not executed
            verify(designationRepository, never()).findById(updatingDto.getDesignationId());
            verify(userRepository, never()).save(any(User.class));
        }
        @Test
        @DisplayName("Update User - Throws error when designation id doesn't exist")
        void testUpdateUser_ThrowsWhenDesignationIdDoesNotExist(){
            //ARRANGE
            Long id = 4L;
            UserDto updatingDto = new UserDto();

            updatingDto.setName("Tissaia");
            //different email
            updatingDto.setEmail("tissia@gmail.com");
            updatingDto.setUserRole(UserRole.SUPPLIESUSER);
            updatingDto.setSubdivId(1L);
            updatingDto.setAdmindivId(1L);
            updatingDto.setDesignationId(2L);

            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));
            when(userRepository.findFirstByEmail(updatingDto.getEmail()))
                    .thenReturn(Optional.empty());
            when(subdivRepository.findById(updatingDto.getSubdivId()))
                    .thenReturn(Optional.of(subdivForUser));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForUser));
            //designation doesn't exist
            when(designationRepository.findById(updatingDto.getDesignationId()))
                    .thenReturn(Optional.empty());


            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateUser(id, updatingDto)
            );

            assertEquals("Designation not found!", exception.getMessage());
            verify(userRepository).findById(id);
            verify(userRepository).findFirstByEmail(updatingDto.getEmail());
            verify(subdivRepository).findById(updatingDto.getSubdivId());
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            verify(designationRepository).findById(updatingDto.getDesignationId());
            //not executed
            verify(userRepository, never()).save(any(User.class));
        }


    }
    @Nested
    @DisplayName("Subdiv Tests")
    class subdivTests{
        //re-used objects
        private Subdiv subdiv;
        private SubdivDto subdivDto;
        private Admindiv admindivForSubdiv;
        private Designation designationForAdmindivOfSubdiv;

        //initialization
        @BeforeEach
        void setup(){
            //designation object for admin div for sub div
            this.designationForAdmindivOfSubdiv = new Designation();
            designationForAdmindivOfSubdiv.setId(1L);
            designationForAdmindivOfSubdiv.setTitle("Senior Registrar");
            designationForAdmindivOfSubdiv.setCode("SR");
            //admin div object for sub div
            this.admindivForSubdiv = new Admindiv();
            admindivForSubdiv.setId(1L);
            admindivForSubdiv.setName("Faculty of Science");
            admindivForSubdiv.setCode("FOS");
            admindivForSubdiv.setResponsibleDesignation(designationForAdmindivOfSubdiv);
            //sub div object
            this.subdiv = new Subdiv();
            subdiv.setId(1L);
            subdiv.setName("Department of Mathematics");
            subdiv.setCode("FOS-DOM");
            subdiv.setAdmindiv(admindivForSubdiv);
            //dto
            this.subdivDto = new SubdivDto();
            subdivDto.setName("Department of Mathematics");
            subdivDto.setCode("FOS-DOM");
            subdivDto.setAdmindivId(1L);
            }

        //Get  by id method:
        //case 01 - id exists, successfully return as dto
        //case 02 - id doesn't exist, throws error

        @Test
        @DisplayName("Get Subdiv by Id - When id exists")
        void testGetSubdivById_Success(){
            //ARRANGE:
            Long id = 1L;
            when(subdivRepository.findById(id))
                    .thenReturn(Optional.of(subdiv));
            //ACT:
            SubdivDto result = adminService.getSubdivById(id);

            //ASSERT:
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(subdiv.getCode(), result.getCode());
            assertEquals(subdiv.getName(), result.getName());
            assertEquals(subdiv.getAdmindiv().getId(), result.getAdmindivId());

            //VERIFY:
            verify(subdivRepository).findById(id);
        }

        @Test
        @DisplayName("Get Subdiv by Id - Throws when id doesn't exist")
        void testGetSubdivById_WhenIdDoesNotExist(){
            //ARRANGE:
            Long id = 1L;
            //not found
            when(subdivRepository.findById(id))
                    .thenReturn(Optional.empty());
            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.getSubdivById(id));

            assertEquals("Sub division with id "+id+ " is not found!", exception.getMessage());

            //VERIFY:
            verify(subdivRepository).findById(id);
        }



        //Delete by id method:
        //case 01 -  id exists, successfully delete
        //case 02 -  id doesn't exist, throws error
        @Test
        @DisplayName("Delete Subdiv- Successfully when id exists")
        void testDeleteSubdiv_WhenIdExists(){
            //ARRANGE:
            Long id = 1L;
            when(subdivRepository.findById(id))
                    .thenReturn(Optional.of(subdiv));
            //returns nothing by the method
            doNothing().when(subdivRepository).deleteById(id);

            //ACT:
            adminService.deleteSubdiv(id);

            //VERIFY:
            verify(subdivRepository).findById(id);
            verify(subdivRepository).deleteById(id);
        }

        @Test
        @DisplayName("Delete Subdiv - Throws when id doesn't exist")
        void testDeleteSubdiv_WhenIdDoesNotExist(){//ARRANGE:
            Long id = 1L;
            when(subdivRepository.findById(id))
                    .thenReturn(Optional.empty());
            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.deleteSubdiv(id) );

            //ASSERT
            assertEquals("Sub division with id "+id+ " is not found!", exception.getMessage());


            //VERIFY:
            verify(subdivRepository).findById(id);
            verify(subdivRepository, never()).deleteById(any());
        }

        //Get all  method:
        //case 01 - exists, successfully return as a list of dto
        //case 02 - returns empty list
        @Test
        @DisplayName("Get All Subdivs - Successfully returns a list")
        void testGetAllSubdivs_Success(){
            //ARRANGE:
            Subdiv subdiv2 = new Subdiv();
            subdiv2.setId(2L);
            subdiv2.setName("Department of Biochemistry");
            subdiv2.setCode("FOS-DOB");
            subdiv2.setAdmindiv(admindivForSubdiv);

            List<Subdiv> subdivList = new ArrayList<>();
            subdivList.add(subdiv);
            subdivList.add(subdiv2);

            when(subdivRepository.findAll())
                    .thenReturn(subdivList);
            //ACT:
            List<SubdivDto> result = adminService.getSubdivs();

            //ASSERT:
            assertNotNull(result);
            assertEquals(2,result.size());
            //make sure it is sorted
            assertEquals("FOS-DOB", result.get(0).getCode());
            assertEquals("FOS-DOM", result.get(1).getCode());

            //VERIFY:
            verify(subdivRepository).findAll();
        }

        @Test
        @DisplayName("Get All Subdivs - when returns an empty list")
        void testGetAllSubdivs_EmptyList(){
            //ARRANGE:
            when(subdivRepository.findAll())
                    .thenReturn(List.of());

            //ACT:
            List<SubdivDto> result = adminService.getSubdivs();

            //ASSERT:
            assertNotNull(result);
            assertTrue(result.isEmpty());

            //VERIFY:
            verify(subdivRepository).findAll();
        }

        //Get Sub divs by  admin div id method:
        //case 01 - admin id exists, successfully return sub div dto list
        //case 02 - id doesn't exist, throws error
        @Test
        @DisplayName("Get All Subdivs of the admin div - Successfully returns a list")
        void testGetSubdivsByAdmindivId_Success(){
            Long admindivId = 1L;
            //ARRANGE:
            Subdiv subdiv2 = new Subdiv();
            subdiv2.setId(2L);
            subdiv2.setName("Department of Biochemistry");
            subdiv2.setCode("FOS-DOB");
            subdiv2.setAdmindiv(admindivForSubdiv);

            List<Subdiv> subdivList = new ArrayList<>();
            subdivList.add(subdiv);
            subdivList.add(subdiv2);

            when(admindivRepository.findById(admindivId))
                    .thenReturn(Optional.of(admindivForSubdiv));

            when(subdivRepository.findByAdmindivId(admindivId))
                    .thenReturn(subdivList);
            //ACT:
            List<SubdivDto> result = adminService.getSubdivsByAdmindivId(admindivId);

            //ASSERT:
            assertNotNull(result);
            assertEquals(2,result.size());
            //make sure it is sorted
            assertEquals("FOS-DOB", result.get(0).getCode());
            assertEquals("FOS-DOM", result.get(1).getCode());

            //VERIFY:
            verify(admindivRepository).findById(admindivId);
            verify(subdivRepository).findByAdmindivId(admindivId);
        }

        @Test
        @DisplayName("Get All Subdivs of the admin div - When returns an empty list ")
        void testGetSubdivsByAdmindivId_EmptyList(){
            Long admindivId = 1L;
            //ARRANGE:
            when(admindivRepository.findById(admindivId))
                    .thenReturn(Optional.of(admindivForSubdiv));
            when(subdivRepository.findByAdmindivId(admindivId))
                    .thenReturn(List.of());

            //ACT:
            List<SubdivDto> result = adminService.getSubdivsByAdmindivId(admindivId);

            //ASSERT:
            assertNotNull(result);
            assertTrue(result.isEmpty());

            //VERIFY:
            verify(admindivRepository).findById(admindivId);
            verify(subdivRepository).findByAdmindivId(admindivId);
        }
        @Test
        @DisplayName("Get All Subdivs of the admin div -Throws when admin div doesn't exist")
        void testGetSubdivsByAdmindivId_ThrowsWhenAdmindivDoesNotExist(){
            Long admindivId = 1L;
            //ARRANGE:
            when(admindivRepository.findById(admindivId))
                    .thenReturn(Optional.empty());
            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    ()-> adminService.getSubdivsByAdmindivId(admindivId)
            );
            assertEquals("Admin div not found!", exception.getMessage());
            //VERIFY:
            verify(admindivRepository).findById(admindivId);
            verify(subdivRepository, never()).findByAdmindivId(admindivId);
        }


        //Create Sub div Method:
        //Case 01: code doesn't exist, admin div exists, successfully create
        //Case 02: code exists, throws error
        //case 03: admin div id is null, throws error
        //case 04: admin div id doesn't exist, throws error
        @Test
        @DisplayName("Create Subdiv - Successfully when code doesn't exist & admin div exists")
        void testCreateSubdiv_Success(){
            //ARRANGE:
            //no existent code (has unique constraint on code)
            when(subdivRepository.existsByCode(subdivDto.getCode()))
                    .thenReturn(false);

            //admin div exists
            when(admindivRepository.findById(subdivDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForSubdiv));

            //return object with id when save() called
            when(subdivRepository.save(any(Subdiv.class)))
                    .thenReturn(subdiv);
            //ACT:
            SubdivDto result = adminService.createSubdiv(subdivDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(subdivDto.getName(), result.getName());
            assertEquals(subdivDto.getCode(), result.getCode());
            assertEquals(subdivDto.getAdmindivId(), result.getAdmindivId());
            //VERIFY:
            verify(subdivRepository).existsByCode(subdivDto.getCode());
            verify(admindivRepository).findById(subdivDto.getAdmindivId());
            verify(subdivRepository).save(any(Subdiv.class));
        }

        @Test
        @DisplayName("Create Subdiv - Throws error when code already exists")
        void testCreateSubdiv_ThrowsWhenCodeExists(){
            //ARRANGE:
            when(subdivRepository.existsByCode(subdivDto.getCode()))
                    .thenReturn(true);

            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createSubdiv(subdivDto)
            );
            //ASSERT:
            assertEquals("Code already exists", exception.getMessage());
            //VERIFY:
            verify(subdivRepository).existsByCode(subdivDto.getCode());
            //not executed
            verify(admindivRepository, never()).findById(subdivDto.getAdmindivId());
            verify(subdivRepository, never()).save(any());
        }
        @Test
        @DisplayName("Create Subdiv - Throws error when admin div id is null")
        void testCreateSubdiv_ThrowsWhenAdmindivIdIsNull(){
            //ARRANGE:
            //admin div id is null
            this.subdivDto.setAdmindivId(null);

            when(subdivRepository.existsByCode(subdivDto.getCode()))
                    .thenReturn(false);

            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createSubdiv(subdivDto)
            );
            //ASSERT:
            assertEquals("Should have an admin division", exception.getMessage());
            //VERIFY:
            verify(subdivRepository).existsByCode(subdivDto.getCode());

            //not executed
            verify(admindivRepository, never()).findById(subdivDto.getAdmindivId());
            verify(subdivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Create Subdiv - Throws error when admin div doesn't exist")
        void testCreateSubdiv_ThrowsAdmindivDoesNotExist(){
            //ARRANGE:
            when(subdivRepository.existsByCode(subdivDto.getCode()))
                    .thenReturn(false);
            //admin div doesn't exist
            when(admindivRepository.findById(subdivDto.getAdmindivId()))
                    .thenReturn(Optional.empty());


            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.createSubdiv(subdivDto)
            );
            //ASSERT:
            assertEquals("Admin division is not found!", exception.getMessage());
            //VERIFY:
            verify(subdivRepository).existsByCode(subdivDto.getCode());
            verify(admindivRepository).findById(subdivDto.getAdmindivId());
            //not executed
            verify(subdivRepository, never()).save(any());
        }

        //Update Subdiv Method:
        //Case 01: subdiv, admindiv, code (not same but) don't exist, successfully update
        //Case 02: subdiv exists, code same, successfully update
        //Case 03: subdiv doesn't exist, throws error
        //case 04: admin div id is null, throws error
        //case 05: admindiv doesn't exist, throws error
        //Case 06: code exists, throws error


        @Test
        @DisplayName("Update Subdiv - Successfully when subdiv, admindiv exists &  code doesn't")
        void testUpdateSubdiv_SuccessNotSameCode(){
            //ARRANGE
            Long id = 1L;
            SubdivDto updatingDto = new SubdivDto();
            //not same
            updatingDto.setName("Department of BioChemistry");
            updatingDto.setCode("FOS-DOB");
            updatingDto.setAdmindivId(1L);

            when(subdivRepository.findById(id))
                    .thenReturn(Optional.of(subdiv));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForSubdiv));
            when(subdivRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);
            when(subdivRepository.save(any(Subdiv.class)))
                    .thenReturn(subdiv);
            //ACT
            SubdivDto result = adminService.updateSubdiv(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getName(),result.getName());
            assertEquals(updatingDto.getAdmindivId(), result.getAdmindivId());

            //VERIFY
            verify(subdivRepository).findById(id);
            verify(subdivRepository).existsByCode(updatingDto.getCode());
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            verify(subdivRepository).save(any(Subdiv.class));
        }

        @Test
        @DisplayName("Update Subdiv - Successfully when subdiv, admindiv exists &  code is same")
        void testUpdateSubdiv_SuccessSameCode(){
            //ARRANGE
            Long id = 1L;
            SubdivDto updatingDto = new SubdivDto();
            updatingDto.setName("Department of Mathematics");
            //same code
            updatingDto.setCode("FOS-DOM");
            updatingDto.setAdmindivId(1L);

            when(subdivRepository.findById(id))
                    .thenReturn(Optional.of(subdiv));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForSubdiv));

            when(subdivRepository.save(any(Subdiv.class)))
                    .thenReturn(subdiv);
            //ACT
            SubdivDto result = adminService.updateSubdiv(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getName(),result.getName());
            assertEquals(updatingDto.getAdmindivId(), result.getAdmindivId());

            //VERIFY
            verify(subdivRepository).findById(id);
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            verify(subdivRepository).save(any(Subdiv.class));
            //not invoked
            verify(subdivRepository, never()).existsByCode(updatingDto.getCode());
        }

        @Test
        @DisplayName("Update Subdiv - Throws error when Sub div id doesn't exist")
        void testUpdateSubdiv_ThrowsWhenIdDoesNotExist(){
            //ARRANGE
            Long id = 4L;
            SubdivDto updatingDto = new SubdivDto();

            updatingDto.setName("Department of Biochemistry");
            updatingDto.setCode("FOS-DOB");
            updatingDto.setAdmindivId(1L);

            when(subdivRepository.findById(id))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateSubdiv(id, updatingDto)
            );

            assertEquals("Sub division is not found!", exception.getMessage());
            verify(subdivRepository).findById(id);
            //not invoked
            verify(subdivRepository, never()).existsByCode(updatingDto.getCode());
            verify(admindivRepository, never()).findById(any());
            verify(subdivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Subdiv - Throws error when Admin div id is null")
        void testUpdateSubdiv_ThrowsWhenAdmindivIdIsNull(){
            //ARRANGE
            Long id = 1L;
            SubdivDto updatingDto = new SubdivDto();

            updatingDto.setName("Department of Biochemistry");
            updatingDto.setCode("FOS-DOB");
            //no value for admindivId

            when(subdivRepository.findById(id))
                    .thenReturn(Optional.of(subdiv));


            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateSubdiv(id, updatingDto)
            );

            assertEquals("Should have an admin division", exception.getMessage());
            verify(subdivRepository).findById(id);
            //not invoked
            verify(subdivRepository, never()).existsByCode(updatingDto.getCode());
            verify(admindivRepository, never()).findById(any());
            verify(subdivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Subdiv - Throws error when Admin div id doesn't exist")
        void testUpdateSubdiv_ThrowsWhenAdmindivIdDoesNotExist(){
            //ARRANGE
            Long id = 1L;
            SubdivDto updatingDto = new SubdivDto();

            updatingDto.setName("Department of Biochemistry");
            updatingDto.setCode("FOS-DOB");
            updatingDto.setAdmindivId(1L);

            when(subdivRepository.findById(id))
                    .thenReturn(Optional.of(subdiv));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateSubdiv(id, updatingDto)
            );

            assertEquals("Admin division is not found!", exception.getMessage());
            verify(subdivRepository).findById(id);
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            //not invoked
            verify(subdivRepository, never()).existsByCode(updatingDto.getCode());
            verify(subdivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Subdiv - Throws error when code exists & not same")
        void testUpdateSubdiv_ThrowsWhenCodeExistsNotSame(){
            //ARRANGE
            Long id = 1L;
            SubdivDto updatingDto = new SubdivDto();

            updatingDto.setName("Department of Biochemistry");
            //not same
            updatingDto.setCode("FOS-DOB");
            updatingDto.setAdmindivId(1L);

            when(subdivRepository.findById(id))
                    .thenReturn(Optional.of(subdiv));
            when(admindivRepository.findById(updatingDto.getAdmindivId()))
                    .thenReturn(Optional.of(admindivForSubdiv));
            //code exists
            when(subdivRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(true);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateSubdiv(id, updatingDto)
            );

            assertEquals("Code already exists", exception.getMessage());
            verify(subdivRepository).findById(id);
            verify(admindivRepository).findById(updatingDto.getAdmindivId());
            verify(subdivRepository).existsByCode(updatingDto.getCode());
            //not invoked
            verify(subdivRepository, never()).save(any());
        }




    }

    @Nested
    @DisplayName("Admindiv Tests")
    class admindivTests{
        //re-used objects
        private Admindiv admindiv;
        private AdmindivDto admindivDto;
        private Designation designationForAdmindiv;

        //initialization
        @BeforeEach
        void setup(){
            //designation object for admin div
            this.designationForAdmindiv = new Designation();
            designationForAdmindiv.setId(1L);
            designationForAdmindiv.setCode("ASR");
            designationForAdmindiv.setTitle("Assistant Senior Registrar");
            //object
            this.admindiv = new Admindiv();
            admindiv.setId(1L);
            admindiv.setName("Faculty of Science");
            admindiv.setCode("FOS");
            admindiv.setResponsibleDesignation(designationForAdmindiv);
            //dto
            this.admindivDto = new AdmindivDto();
            admindivDto.setName("Faculty of Science");
            admindivDto.setCode("FOS");
            admindivDto.setResponsibleDesignationId(1L);
        }

        //Get  by id method:
        //case 01 - id exists, successfully return as dto
        //case 02 - id doesn't exist, throws error
        @Test
        @DisplayName("Get Admindiv by Id - when id exists")
        void testGetAdmindivById_Success(){
            //ARRANGE:
            Long id = 1L;
            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));
            //ACT:
            AdmindivDto result = adminService.getAdmindivById(id);

            //ASSERT:
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(admindiv.getCode(), result.getCode());
            assertEquals(admindiv.getName(), result.getName());
            assertEquals(admindiv.getResponsibleDesignation().getId(), result.getResponsibleDesignationId());

            //VERIFY:
            verify(admindivRepository).findById(id);
        }

        @Test
        @DisplayName("Get Admindiv by Id - Throws when id doesn't exist")
        void testGetAdmindivById_WhenIdDoesNotExist(){
            //ARRANGE:
            Long id = 1L;
            //not found
            when(admindivRepository.findById(id))
                    .thenReturn(Optional.empty());
            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.getAdmindivById(id));

            assertEquals("Admin division with id " + id + " is not found!", exception.getMessage());

            //VERIFY:
            verify(admindivRepository).findById(id);
        }

        //Delete by id method:
        //case 01 -  id exists, successfully delete
        //case 02 -  id doesn't exist, throws error
        @Test
        @DisplayName("Delete Admindiv- Successfully when id exists")
        void testDeleteAdmindiv_WhenIdExists(){
            //ARRANGE:
            Long id = 1L;
            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));
            //returns nothing by the method
            doNothing().when(admindivRepository).deleteById(id);

            //ACT:
            adminService.deleteAdmindiv(id);

            //VERIFY:
            verify(admindivRepository).findById(id);
            verify(admindivRepository).deleteById(id);
        }

        @Test
        @DisplayName("Delete Admindiv - Throws when id doesn't exist")
        void testDeleteAdmindiv_WhenIdDoesNotExist(){
            //ARRANGE:
            Long id = 1L;
            when(admindivRepository.findById(id))
                    .thenReturn(Optional.empty());
            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.deleteAdmindiv(id) );

            //ASSERT
            assertEquals("Admin division with id "+id+ " is not found!", exception.getMessage());


            //VERIFY:
            verify(admindivRepository).findById(id);
            verify(admindivRepository, never()).deleteById(any());
        }

        //Get all  method:
        //case 01 - exists, successfully return as a list of dto
        //case 02 - returns empty list
        @Test
        @DisplayName("Get All Admindivs - Successfully returns a list")
        void testGetAllAdmindivs_Success(){
            //ARRANGE:
            Admindiv admindiv2 = new Admindiv();
            admindiv2.setId(2L);
            admindiv2.setName("Faculty of Commerce");
            admindiv2.setCode("FOC");
            admindiv2.setResponsibleDesignation(designationForAdmindiv);

            List<Admindiv> admindivList = new ArrayList<>();
            admindivList.add(admindiv);
            admindivList.add(admindiv2);

            when(admindivRepository.findAll())
                    .thenReturn(admindivList);
            //ACT:
            List<AdmindivDto> result = adminService.getAllAdmindivs();

            //ASSERT:
            assertNotNull(result);
            assertEquals(2,result.size());
            //make sure it is sorted
            assertEquals("FOC", result.get(0).getCode());
            assertEquals("FOS", result.get(1).getCode());

            //VERIFY:
            verify(admindivRepository).findAll();
        }

        @Test
        @DisplayName("Get All Admindivs - when returns an empty list")
        void testGetAllAdmindivs_EmptyList(){
            //ARRANGE:
            when(admindivRepository.findAll())
                    .thenReturn(List.of());

            //ACT:
            List<AdmindivDto> result = adminService.getAllAdmindivs();

            //ASSERT:
            assertNotNull(result);
            assertTrue(result.isEmpty());

            //VERIFY:
            verify(admindivRepository).findAll();
        }

        //Create Admindiv Method:
        //Case 01: code, title don't exist, designation exists, successfully create
        //Case 02: code exists, throws error
        //case 03: name exists, throws error
        //case 04: designation id is null, throws error
        //case 05: designation doesn't exist, throws error

        @Test
        @DisplayName("Create Admindiv - Successfully when code and name don't exist & designation exists")
        void testCreateAdmindiv_Success(){
            //ARRANGE:
            //no existent code or name ( has unique constraint on both)
            when(admindivRepository.existsByCode(admindivDto.getCode()))
                    .thenReturn(false);
            when(admindivRepository.existsByName(admindivDto.getName()))
                    .thenReturn(false);
            //designation exists
            when(designationRepository.findById(admindivDto.getResponsibleDesignationId()))
                    .thenReturn(Optional.of(designationForAdmindiv));

            //return object with id when save() called
            when(admindivRepository.save(any(Admindiv.class)))
                    .thenReturn(admindiv);
            //ACT:
            AdmindivDto result = adminService.createAdmindiv(admindivDto);

            //ASSERT:
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(admindivDto.getName(), result.getName());
            assertEquals(admindivDto.getCode(), result.getCode());
            assertEquals(admindivDto.getResponsibleDesignationId(), result.getResponsibleDesignationId());
            //VERIFY:
            verify(admindivRepository).existsByCode(admindivDto.getCode());
            verify(admindivRepository).existsByName(admindivDto.getName());
            verify(admindivRepository).save(any(Admindiv.class));
        }

        @Test
        @DisplayName("Create Admindiv - Throws error when designation id is null")
        void testCreateAdmindiv_ThrowsWhenDesignationIdIsNull(){
            //ARRANGE:
            //designation id is null
            this.admindivDto.setResponsibleDesignationId(null);

            when(admindivRepository.existsByCode(admindivDto.getCode()))
                    .thenReturn(false);
            when(admindivRepository.existsByName(admindivDto.getName()))
                    .thenReturn(false);


            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createAdmindiv(admindivDto)
            );
            //ASSERT:
            assertEquals("Should have a designation", exception.getMessage());
            //VERIFY:
            verify(admindivRepository).existsByCode(admindivDto.getCode());
            verify(admindivRepository).existsByName(admindivDto.getName());

            //not executed
            verify(designationRepository, never()).findById(admindivDto.getResponsibleDesignationId());
            verify(admindivRepository, never()).save(any());
        }
        @Test
        @DisplayName("Create Admindiv - Throws error when designation doesn't exist")
        void testCreateAdmindiv_ThrowsDesignationDoesNotExist(){
            //ARRANGE:
            when(admindivRepository.existsByCode(admindivDto.getCode()))
                    .thenReturn(false);
            when(admindivRepository.existsByName(admindivDto.getName()))
                    .thenReturn(false);
            //designation doesn't exist
            when(designationRepository.findById(admindivDto.getResponsibleDesignationId()))
                    .thenReturn(Optional.empty());


            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.createAdmindiv(admindivDto)
            );
            //ASSERT:
            assertEquals("Designation is not found", exception.getMessage());
            //VERIFY:
            verify(admindivRepository).existsByCode(admindivDto.getCode());
            verify(admindivRepository).existsByName(admindivDto.getName());
            verify(designationRepository).findById(admindivDto.getResponsibleDesignationId());
            //not executed
            verify(admindivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Create Admindiv - Throws error when code already exists")
        void testCreateAdmindiv_ThrowsWhenCodeExists(){
            //ARRANGE:
            when(admindivRepository.existsByCode(admindivDto.getCode()))
                    .thenReturn(true);

            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createAdmindiv(admindivDto)
            );
            //ASSERT:
            assertEquals("Code already exists", exception.getMessage());
            //VERIFY:
            verify(admindivRepository).existsByCode(admindivDto.getCode());
            //not executed
            verify(admindivRepository, never()).existsByName(admindivDto.getName());
            verify(designationRepository, never()).findById(admindivDto.getResponsibleDesignationId());
            verify(admindivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Create Admindiv - Throws error when name already exists")
        void testCreateAdmindiv_ThrowsWhenNameExists(){
            //ARRANGE:
            when(admindivRepository.existsByCode(admindivDto.getCode()))
                    .thenReturn(false);
            when(admindivRepository.existsByName(admindivDto.getName()))
                    .thenReturn(true);

            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createAdmindiv(admindivDto)
            );
            //ASSERT:
            assertEquals("Name already exists", exception.getMessage());
            //VERIFY:
            verify(admindivRepository).existsByCode(admindivDto.getCode());
            verify(admindivRepository).existsByName(admindivDto.getName());
            //not executed
            verify(designationRepository, never()).findById(admindivDto.getResponsibleDesignationId());
            verify(admindivRepository, never()).save(any());
        }

        //Update Admindiv Method:
        //Case 01: admindiv, designation, code and name (not same but) don't exist, successfully update
        //Case 02: admindiv exists, code same, successfully update
        //Case 03: admindiv exists, name same, successfully update
        //Case 04: admindiv exists, name & code both same, successfully update
        //Case 05: admindiv doesn't exist, throws error
        //Case 06: code not same & exists, throws error
        //case 07: name not same &  exists, throws error
        //case 08: designation id is null, throws error
        //case 09: designation doesn't exist, throws error

        @Test
        @DisplayName("Update Admindiv - Successfully when admindiv, designation, code and name don't exist")
        void testUpdateAdmindiv_SuccessNotSameCodeORName(){
            //ARRANGE
            Long id = 1L;
            AdmindivDto updatingDto = new AdmindivDto();
            //not same
            updatingDto.setName("Faulty of Humanities");
            updatingDto.setCode("FOH");
            updatingDto.setResponsibleDesignationId(1L);

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));
            when(admindivRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);
            when(admindivRepository.existsByName(updatingDto.getName()))
                    .thenReturn(false);
            when(designationRepository.findById(updatingDto.getResponsibleDesignationId()))
                            .thenReturn(Optional.of(designationForAdmindiv));
            when(admindivRepository.save(any(Admindiv.class)))
                    .thenReturn(admindiv);
            //ACT
            AdmindivDto result = adminService.updateAdmindiv(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getName(),result.getName());
            assertEquals(updatingDto.getResponsibleDesignationId(), result.getResponsibleDesignationId());

            //VERIFY
            verify(admindivRepository).findById(id);
            verify(admindivRepository).existsByCode(updatingDto.getCode());
            verify(admindivRepository).existsByName(updatingDto.getName());
            verify(designationRepository).findById(updatingDto.getResponsibleDesignationId());
            verify(admindivRepository).save(any(Admindiv.class));
        }

        @Test
        @DisplayName("Update Admindiv - Successfully when code is same")
        void testUpdateAdmindiv_SuccessSameCodeNotName(){
            //ARRANGE
            Long id = 1L;
            AdmindivDto updatingDto = new AdmindivDto();
            //not same
            updatingDto.setName("Faulty of Humanities");
            //same code
            updatingDto.setCode("FOS");
            updatingDto.setResponsibleDesignationId(1L);

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));
            //not checking code (since same - short-circuited)
            when(admindivRepository.existsByName(updatingDto.getName()))
                    .thenReturn(false);
            when(designationRepository.findById(updatingDto.getResponsibleDesignationId()))
                    .thenReturn(Optional.of(designationForAdmindiv));
            when(admindivRepository.save(any(Admindiv.class)))
                    .thenReturn(admindiv);
            //ACT
            AdmindivDto result = adminService.updateAdmindiv(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getName(),result.getName());
            assertEquals(updatingDto.getResponsibleDesignationId(), result.getResponsibleDesignationId());

            //VERIFY
            verify(admindivRepository).findById(id);

            verify(admindivRepository).existsByName(updatingDto.getName());
            verify(designationRepository).findById(updatingDto.getResponsibleDesignationId());
            verify(admindivRepository).save(any(Admindiv.class));
        }

        @Test
        @DisplayName("Update Admindiv - Successfully when name is same")
        void testUpdateAdmindiv_SuccessSameNameNotCode(){
            //ARRANGE
            Long id = 1L;
            AdmindivDto updatingDto = new AdmindivDto();
            //same name
            updatingDto.setName("Faculty of Science");
            updatingDto.setCode("FOH");
            updatingDto.setResponsibleDesignationId(1L);

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));
            when(admindivRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);

            when(designationRepository.findById(updatingDto.getResponsibleDesignationId()))
                    .thenReturn(Optional.of(designationForAdmindiv));
            when(admindivRepository.save(any(Admindiv.class)))
                    .thenReturn(admindiv);
            //ACT
            AdmindivDto result = adminService.updateAdmindiv(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getName(),result.getName());
            assertEquals(updatingDto.getResponsibleDesignationId(), result.getResponsibleDesignationId());

            //VERIFY
            verify(admindivRepository).findById(id);
            verify(admindivRepository).existsByCode(updatingDto.getCode());

            verify(designationRepository).findById(updatingDto.getResponsibleDesignationId());
            verify(admindivRepository).save(any(Admindiv.class));
        }

        @Test
        @DisplayName("Update Admindiv - Successfully when name & code are same")
        void testUpdateAdmindiv_SuccessSameNameAndCode(){
            //ARRANGE
            Long id = 1L;
            AdmindivDto updatingDto = new AdmindivDto();
            //both same
            updatingDto.setName("Faculty of Science");
            updatingDto.setCode("FOS");
            updatingDto.setResponsibleDesignationId(1L);

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));


            when(designationRepository.findById(updatingDto.getResponsibleDesignationId()))
                    .thenReturn(Optional.of(designationForAdmindiv));
            when(admindivRepository.save(any(Admindiv.class)))
                    .thenReturn(admindiv);
            //ACT
            AdmindivDto result = adminService.updateAdmindiv(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getName(),result.getName());
            assertEquals(updatingDto.getResponsibleDesignationId(), result.getResponsibleDesignationId());

            //VERIFY
            verify(admindivRepository).findById(id);

            verify(designationRepository).findById(updatingDto.getResponsibleDesignationId());
            verify(admindivRepository).save(any(Admindiv.class));
        }
        //Case 05: admindiv doesn't exist, throws error
        //Case 06: code not same & exists, throws error
        //case 07: name not same &  exists, throws error
        //case 08: designation id is null , throws error
        //case 09: designation doesn't exist, throws error

        @Test
        @DisplayName("Update Admindiv - Throws error when id doesn't exist")
        void testUpdateAdmindiv_ThrowsWhenIdDoesNotExist(){
            //ARRANGE
            Long id = 4L;
            AdmindivDto updatingDto = new AdmindivDto();

            updatingDto.setName("Faculty of Science");
            updatingDto.setCode("FOS");
            updatingDto.setResponsibleDesignationId(1L);

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateAdmindiv(id, updatingDto)
            );

            assertEquals("Admin div not found", exception.getMessage());
            verify(admindivRepository).findById(id);

            verify(admindivRepository, never()).existsByCode(updatingDto.getCode());
            verify(admindivRepository, never()).existsByName(updatingDto.getName());
            verify(designationRepository, never()).findById(any());
            verify(admindivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Admindiv - Throws error when code exists")
        void testUpdateAdmindiv_ThrowsWhenCodeExists(){
            //ARRANGE
            Long id = 1L;
            AdmindivDto updatingDto = new AdmindivDto();
            updatingDto.setName("Faculty of Science");
            //not same
            updatingDto.setCode("FOSc");
            updatingDto.setResponsibleDesignationId(1L);

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));
            //code exists
            when(admindivRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(true);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateAdmindiv(id, updatingDto)
            );

            assertEquals("Code already exists", exception.getMessage());
            verify(admindivRepository).findById(id);
            verify(admindivRepository).existsByCode(updatingDto.getCode());

            verify(admindivRepository, never()).existsByName(updatingDto.getName());
            verify(designationRepository, never()).findById(any());
            verify(admindivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Admindiv - Throws error when name exists")
        void testUpdateAdmindiv_ThrowsWhenNameExists(){
            //ARRANGE
            Long id = 1L;
            AdmindivDto updatingDto = new AdmindivDto();
            //not same
            updatingDto.setName("Faculty of Commerce");
            updatingDto.setCode("FOC");
            updatingDto.setResponsibleDesignationId(1L);

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));

            when(admindivRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);
            //name exists
            when(admindivRepository.existsByName(updatingDto.getName()))
                    .thenReturn(true);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateAdmindiv(id, updatingDto)
            );

            assertEquals("Name already exists", exception.getMessage());
            verify(admindivRepository).findById(id);
            verify(admindivRepository).existsByCode(updatingDto.getCode());
            verify(admindivRepository).existsByName(updatingDto.getName());


            verify(designationRepository, never()).findById(any());
            verify(admindivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Admindiv - Throws error when designation doesn't exist")
        void testUpdateAdmindiv_ThrowsWhenDesignationIsNull(){
            //ARRANGE
            Long id = 1L;
            AdmindivDto updatingDto = new AdmindivDto();
            //not same
            updatingDto.setName("Faculty of Commerce");
            updatingDto.setCode("FOC");
            updatingDto.setResponsibleDesignationId(1L);

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));

            when(admindivRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);
            when(admindivRepository.existsByName(updatingDto.getName()))
                    .thenReturn(false);
            //designation does not exists
            when(designationRepository.findById(updatingDto.getResponsibleDesignationId()))
                    .thenReturn(Optional.empty());


            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateAdmindiv(id, updatingDto)
            );

            assertEquals("Designation not found", exception.getMessage());
            verify(admindivRepository).findById(id);
            verify(admindivRepository).existsByCode(updatingDto.getCode());
            verify(admindivRepository).existsByName(updatingDto.getName());
            verify(designationRepository).findById(updatingDto.getResponsibleDesignationId());

            verify(admindivRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Admindiv -Throws error when designation id is null")
        void testUpdateAdmindiv_ThrowsWhenDesignationDoesNotExist(){
            //ARRANGE
            Long id = 1L;
            AdmindivDto updatingDto = new AdmindivDto();
            //not same
            updatingDto.setName("Faculty of Commerce");
            updatingDto.setCode("FOC");
            //designation id is null

            when(admindivRepository.findById(id))
                    .thenReturn(Optional.of(admindiv));

            when(admindivRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);
            when(admindivRepository.existsByName(updatingDto.getName()))
                    .thenReturn(false);


            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateAdmindiv(id, updatingDto)
            );

            assertEquals("Should have a designation", exception.getMessage());
            verify(admindivRepository).findById(id);
            verify(admindivRepository).existsByCode(updatingDto.getCode());
            verify(admindivRepository).existsByName(updatingDto.getName());


            verify(designationRepository, never()).findById(any());
            verify(admindivRepository, never()).save(any());
        }



    }

    @Nested
    @DisplayName("Designation Tests")
    class designationTests{
        //re-used objects
        private Designation designation;
        private Designation newDesignation;

        //initialization
        @BeforeEach
            void setup(){
            //saved object
              this.designation = new Designation();
              designation.setId(1L);
              designation.setTitle("Assistant Registrar");
              designation.setCode("AR");

              //dto object - has no id
              this.newDesignation = new Designation();
              newDesignation.setTitle("Assistant Registrar");
              newDesignation.setCode("AR");
            }


        //Create Designation Method:
        //Case 01: code, title don't exist, successfully create
        //Case 02: code exists, throws error
        //case 03: title exists, throws error
        @Test
        @DisplayName("Create Designation - Successfully when code and title don't exist")
        void testCreateDesignation_Success(){
            //ARRANGE:
            //no existent code or title ( has unique constraint on both)
            when(designationRepository.existsByCode(newDesignation.getCode()))
                    .thenReturn(false);
            when(designationRepository.existsByTitle(newDesignation.getTitle()))
                    .thenReturn(false);
            //return object with id when save() called
            when(designationRepository.save(newDesignation))
                    .thenReturn(designation);
            //ACT:
            Designation result = adminService.createDesignation(newDesignation);
            //ASSERT:
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(newDesignation.getTitle(), result.getTitle());
            assertEquals(newDesignation.getCode(), result.getCode());
            //VERIFY:
            verify(designationRepository).existsByCode(newDesignation.getCode());
            verify(designationRepository).existsByTitle(newDesignation.getTitle());
            verify(designationRepository).save(newDesignation);

        }

        @Test
        @DisplayName("Create Designation - Throws error when code already exists")
        void testCreateDesignation_ThrowsWhenCodeExists(){
            //ARRANGE:
            when(designationRepository.existsByCode(newDesignation.getCode()))
                    .thenReturn(true);

            //ACT:
           RuntimeException exception = assertThrows(RuntimeException.class,
                   () -> adminService.createDesignation(newDesignation)
           );
            //ASSERT:
            assertEquals("Code already exists", exception.getMessage());
            //VERIFY:
            verify(designationRepository).existsByCode(newDesignation.getCode());
            //not executed
            verify(designationRepository, never()).existsByTitle(newDesignation.getTitle());
            verify(designationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Create Designation - Throws error when title already exists")
        void testCreateDesignation_ThrowsWhenTitleExists(){
            //ARRANGE:
            when(designationRepository.existsByCode(newDesignation.getCode()))
                    .thenReturn(false);
            when(designationRepository.existsByTitle(newDesignation.getTitle()))
                    .thenReturn(true);
            //ACT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.createDesignation(newDesignation)
            );
            //ASSERT:
            assertEquals("Title already exists", exception.getMessage());
            //VERIFY:
            verify(designationRepository).existsByCode(newDesignation.getCode());
            verify(designationRepository).existsByTitle(newDesignation.getTitle());
            //not executed
            verify(designationRepository, never()).save(any());
        }


        //Update Designation Method:
        //Case 01: designation, code, title (not same but) don't exist, successfully update
        //Case 02: designation exist, code same, successfully update
        //Case 03: designation exist, title same, successfully update
        //Case 04: designation doesn't exist, throws error
        //Case 05: code not same & exists, throws error
        //case 06: title not same &  exists, throws error
        @Test
        @DisplayName("Update Designation - Successfully when designation, code and title don't exist")
        void testUpdateDesignation_SuccessNotSameCodeORTitle(){
            //ARRANGE
            Long id = 1L;
            Designation updatingDto = new Designation();
            //not same
            updatingDto.setTitle("Demonstrator");
            updatingDto.setCode("DEMO");

            when(designationRepository.findById(id))
                    .thenReturn(Optional.of(designation));
            when(designationRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);
            when(designationRepository.existsByTitle(updatingDto.getTitle()))
                    .thenReturn(false);
            when(designationRepository.save(designation))
                    .thenReturn(designation);
            //ACT
            Designation result = adminService.updateDesignation(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getTitle(),result.getTitle());

            //VERIFY
            verify(designationRepository).existsByCode(updatingDto.getCode());
            verify(designationRepository).existsByTitle(updatingDto.getTitle());
            verify(designationRepository).save(designation);
        }

        @Test
        @DisplayName("Update Designation - Successfully when designation exists, code is same")
        void testUpdateDesignation_SuccessSameCodeNotTitle(){
            //ARRANGE
            Long id = 1L;
            Designation updatingDto = new Designation();
            //not same title
            updatingDto.setTitle("Asst. Registrar");
            //same code
            updatingDto.setCode("AR");

            when(designationRepository.findById(id))
                    .thenReturn(Optional.of(designation));
            //no need to check for code
            when(designationRepository.existsByTitle(updatingDto.getTitle()))
                    .thenReturn(false);
            when(designationRepository.save(designation))
                    .thenReturn(designation);
            //ACT
            Designation result = adminService.updateDesignation(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getTitle(),result.getTitle());

            //VERIFY
            verify(designationRepository).existsByTitle(updatingDto.getTitle());
            verify(designationRepository).save(designation);
            //not checked as it is same (short-circuited)
            verify(designationRepository, never()).existsByCode(updatingDto.getCode());
        }

        @Test
        @DisplayName("Update Designation - Successfully when designation exists, title is same")
        void testUpdateDesignation_SuccessSameTitleNotCode(){
            //ARRANGE
            Long id = 1L;
            Designation updatingDto = new Designation();
            //same title
            updatingDto.setTitle("Assistant Registrar");
            //not same code
            updatingDto.setCode("A-R");

            when(designationRepository.findById(id))
                    .thenReturn(Optional.of(designation));
            //no need to check for title
            when(designationRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);
            when(designationRepository.save(designation))
                    .thenReturn(designation);
            //ACT
            Designation result = adminService.updateDesignation(id,updatingDto);

            //ASSERT
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updatingDto.getCode(), result.getCode());
            assertEquals(updatingDto.getTitle(),result.getTitle());

            //VERIFY
            verify(designationRepository).existsByCode(updatingDto.getCode());
            verify(designationRepository).save(designation);
            //not checked as it is same (short-circuited)
            verify(designationRepository, never()).existsByTitle(updatingDto.getTitle());
        }


        @Test
        @DisplayName("Update Designation - Throws error when designation doesn't exist")
        void testUpdateDesignation_ThrowsWhenDesignationDoesNotExist(){
            //ARRANGE
            Long id = 1L;
            Designation updatingDto = new Designation();
            updatingDto.setTitle("Demonstrator");
            updatingDto.setCode("DEMO");

            when(designationRepository.findById(id))
                    .thenReturn(Optional.empty());

            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.updateDesignation(id, updatingDto)
            );

            assertEquals("Designation not found", exception.getMessage());
            verify(designationRepository).findById(id);

            verify(designationRepository, never()).existsByCode(updatingDto.getCode());
            verify(designationRepository, never()).existsByTitle(updatingDto.getTitle());
            verify(designationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Designation - Throws error when code does exist")
        void testUpdateDesignation_ThrowsWhenCodeExists(){
            //ARRANGE
            Long id = 1L;
            Designation updatingDto = new Designation();
            updatingDto.setTitle("Demonstrator");
            updatingDto.setCode("DEMO");

            when(designationRepository.findById(id))
                    .thenReturn(Optional.of(designation));
            //code exists
            when(designationRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(true);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateDesignation(id, updatingDto)
            );
            assertEquals("Code already exists", exception.getMessage());
            verify(designationRepository).findById(id);
            verify(designationRepository).existsByCode(updatingDto.getCode());

            verify(designationRepository, never()).existsByTitle(any());
            verify(designationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Designation - Throws error when title does exist")
        void testUpdateDesignation_ThrowsWhenTitleExists(){
            //ARRANGE
            Long id = 1L;
            Designation updatingDto = new Designation();
            updatingDto.setTitle("Demonstrator");
            updatingDto.setCode("DEMO");

            when(designationRepository.findById(id))
                    .thenReturn(Optional.of(designation));
            when(designationRepository.existsByCode(updatingDto.getCode()))
                    .thenReturn(false);
            //title exists
            when(designationRepository.existsByTitle(updatingDto.getTitle()))
                    .thenReturn(true);

            //ACT & ASSERT:
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> adminService.updateDesignation(id, updatingDto)
            );
            assertEquals("Title already exists", exception.getMessage());
            verify(designationRepository).findById(id);
            verify(designationRepository).existsByCode(updatingDto.getCode());
            verify(designationRepository).existsByTitle(updatingDto.getTitle());

            verify(designationRepository, never()).save(any());
        }

        //Get  by id method:
        //case 01 - id exists, successfully return as dto
        //case 02 - id doesn't exist, throws error
        @Test
        @DisplayName("Get Designation by Id - when id exists")
        void testGetDesignationById_Success(){
            //ARRANGE:
            Long id = 1L;
            when(designationRepository.findById(id))
                    .thenReturn(Optional.of(designation));
            //ACT:
            Designation result = adminService.getDesignationById(id);
            //ASSERT:
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(designation.getCode(), result.getCode());
            assertEquals(designation.getTitle(), result.getTitle());

            //VERIFY:
            verify(designationRepository).findById(id);

        }
        @Test
        @DisplayName("Get Designation by Id - Throws when id doesn't exist")
        void testGetDesignationById_WhenIdDoesNotExist(){
            //ARRANGE:
            Long id = 1L;
            //not found
            when(designationRepository.findById(id))
                    .thenReturn(Optional.empty());
            //ACT & ASSERT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.getDesignationById(id));

            assertEquals("Designation with id " +id+ " is not found!", exception.getMessage());

            //VERIFY:
            verify(designationRepository).findById(id);

        }

        //Delete designation by id method:
        //case 01 -  id exists, successfully delete
        //case 02 -  id doesn't exist, throws error
        @Test
        @DisplayName("Delete Designation - Successfully when id exists")
        void testDeleteDesignation_WhenIdExists(){
            //ARRANGE:
            Long id = 1L;
            when(designationRepository.findById(id))
                    .thenReturn(Optional.of(designation));
            //returns nothing by the method
            doNothing().when(designationRepository).deleteById(id);

            //ACT:
            adminService.deleteDesignation(id);

            //VERIFY:
            verify(designationRepository).findById(id);
            verify(designationRepository).deleteById(id);
        }

        @Test
        @DisplayName("Delete Designation - Throws when id doesn't exist")
        void testDeleteDesignation_WhenIdDoesNotExist(){
            //ARRANGE:
            Long id = 1L;
            when(designationRepository.findById(id))
                    .thenReturn(Optional.empty());
            //ACT:
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> adminService.deleteDesignation(id) );

            //ASSERT
            assertEquals("Designation with id "+id+ " is not found!", exception.getMessage());


            //VERIFY:
            verify(designationRepository).findById(id);
            verify(designationRepository, never()).deleteById(any());
        }



        //Get all  method:
        //case 01 - exists, successfully return as a list of dto
        //case 02 - returns empty list
        @Test
        @DisplayName("Get All Designations - Successfully returns designations list")
        void testGetAllDesignations_Success(){
            //ARRANGE:
            Designation designation2 = new Designation();
            designation2.setId(2L);
            designation2.setCode("SAR");
            designation2.setTitle("Senior Assistant Registrar");

            List<Designation> designationsList = new ArrayList<>();
            designationsList.add(designation); //code = "AR"
            designationsList.add(designation2); //name = "SAR"

            when(designationRepository.findAll())
                    .thenReturn(designationsList);
            //ACT:
            List<Designation> result = adminService.getAllDesignations();

            //ASSERT:
            assertNotNull(result);
            assertEquals(2,result.size());
            //make sure it is sorted
            assertEquals("Assistant Registrar", result.get(0).getTitle());
            assertEquals("Senior Assistant Registrar", result.get(1).getTitle());

            //VERIFY:
            verify(designationRepository).findAll();

        }


        @Test
        @DisplayName("Get All Designations - when no designations returns empty list")
        void testGetAllDesignations_EmptyList(){
            //ARRANGE:
            when(designationRepository.findAll())
                    .thenReturn(List.of());

            //ACT:
            List<Designation> result = adminService.getAllDesignations();

            //ASSERT:
            assertNotNull(result);
            assertTrue(result.isEmpty());

            //VERIFY:
            verify(designationRepository).findAll();
        }





    }
}