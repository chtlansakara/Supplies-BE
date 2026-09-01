package com.cht.procurementManagement.services.admin;

import com.cht.procurementManagement.dto.*;
import com.cht.procurementManagement.entities.*;
import com.cht.procurementManagement.enums.UserRole;
import com.cht.procurementManagement.repositories.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.expression.ExpressionException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    //repository injections

    private final AdmindivRepository admindivRepository;
    private final DesignationRepository designationRepository;
    private final SubdivRepository subdivRepository;
    private final UserRepository userRepository;
    private final ProcurementStatusRepository procurementStatusRepository;
    private final VendorRepository vendorRepository;
    private final ProcurementSourceRepository procurementSourceRepository;

    public AdminServiceImpl(
            AdmindivRepository admindivRepository,
            DesignationRepository designationRepository,
            SubdivRepository subdivRepository,
            UserRepository userRepository,
            ProcurementStatusRepository procurementStatusRepository,
            VendorRepository vendorRepository,
            ProcurementSourceRepository procurementSourceRepository) {
        this.admindivRepository = admindivRepository;
        this.designationRepository = designationRepository;
        this.subdivRepository = subdivRepository;
        this.userRepository = userRepository;
        this.procurementStatusRepository = procurementStatusRepository;
        this.vendorRepository = vendorRepository;
        this.procurementSourceRepository = procurementSourceRepository;
    }

    //Procurement Sources -----------------------------------------------------------------------------
    //create
    @Override
    public ProcurementSourceDto createSource(ProcurementSourceDto procurementSourceDto){
        //when source name is null
        if(procurementSourceDto.getName()==null){
            throw new RuntimeException("Source should have a name");
        }

        //check for unique constraints with name
        if(procurementSourceRepository.findFirstByName(procurementSourceDto.getName()).isPresent()){
            throw new EntityExistsException("Source name already exists");
        }

        ProcurementSource source = new ProcurementSource();
        source.setName(procurementSourceDto.getName());
        source.setDescription(procurementSourceDto.getDescription());
        return procurementSourceRepository.save(source).getdto();
    }

    //update
    @Override
    public ProcurementSourceDto updateSource(Long id,ProcurementSourceDto procurementSourceDto){
        ProcurementSource existingSource = procurementSourceRepository.findById(id)
                        .orElseThrow(()->  new RuntimeException("Source not found"));

        boolean isSame = existingSource.getName().equalsIgnoreCase(procurementSourceDto.getName());
        //check if new name is already present
        if( !isSame &&
                procurementSourceRepository.findFirstByName(procurementSourceDto.getName()).isPresent()){
            throw new EntityExistsException("Source name already exists");
        }
        existingSource.setName(procurementSourceDto.getName());
        existingSource.setDescription(procurementSourceDto.getDescription());
        return procurementSourceRepository.save(existingSource).getdto();
    }

    //get all
    @Override
    public List<ProcurementSourceDto>  getAllSources(){
        return procurementSourceRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ProcurementSource::getName))
                .map(ProcurementSource::getdto)
                .collect(Collectors.toList());
    }

    //get by id
    @Override
    public ProcurementSourceDto getSourceById(Long id){
        return procurementSourceRepository.findById(id).map(ProcurementSource::getdto)
                .orElseThrow(() -> new RuntimeException("Source not found"));
    }

    //delete
    @Override
    public void deleteSourceById(Long id){
        if(procurementSourceRepository.findById(id).isEmpty()){
            throw new RuntimeException("Source is not found");
        }
        procurementSourceRepository.deleteById(id);
    }


    //Vendors -----------------------------------------------------------------------------


    @Override
    public VendorDto createVendor(VendorDto vendorDto) {
        if(vendorDto.getName() == null){
            throw new RuntimeException("Name is required");
        }
        //create new object
        Vendor vendor = new Vendor();
        vendor.setName(vendorDto.getName());

        //if date does not exist, set current date
        if(vendorDto.getRegisteredDate() == null) {
            vendor.setRegisteredDate(new Date());
        }else{
            vendor.setRegisteredDate(vendorDto.getRegisteredDate());
        }

        vendor.setComments(vendorDto.getComments());
        //save to db & return dto
        return vendorRepository.save(vendor).getVendorDto();
    }

    @Override
    public List<VendorDto> getVendors() {
        return vendorRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Vendor::getName))
                .map(Vendor::getVendorDto)
                .collect(Collectors.toList());
    }

    @Override
    public VendorDto getVendorById(Long id) {
        return vendorRepository.findById(id).map(Vendor::getVendorDto)
                .orElseThrow(() -> new EntityNotFoundException("Vendor with id "+id+ " is not found!"));
    }

    @Override
    public VendorDto updateVendor(Long id, VendorDto vendorDto) {
        //check if the vendor id exists
        Optional<Vendor> optionalVendor = vendorRepository.findById(id);
        if(optionalVendor.isEmpty()){
            throw new EntityNotFoundException("Vendor not found");
        }
        //update
        Vendor existing = optionalVendor.get();
        existing.setName(vendorDto.getName());
        //if date exists, update
        if(vendorDto.getRegisteredDate() != null) {
            existing.setRegisteredDate(vendorDto.getRegisteredDate());
        }
        existing.setComments(vendorDto.getComments());
        return vendorRepository.save(existing).getVendorDto();
    }

    @Override
    public void deleteVendor(Long id) {
        //check if the vendor id exists
        Optional<Vendor> optionalVendor = vendorRepository.findById(id);
        if(optionalVendor.isEmpty()){
            throw new EntityNotFoundException("Vendor not found");
        }
        //delete
        vendorRepository.deleteById(id);
    }


    //Users -----------------------------------------------------------------------------

    @Override
    public ProcurementStatusDto createProcurementStatus(ProcurementStatusDto procurementStatusDto) {
        //check if name is null
        if(procurementStatusDto.getName() == null){
            throw new RuntimeException("Status name is required");
        }

        //check for unique constraints with name
        if(procurementStatusRepository.findFirstByName(procurementStatusDto.getName()).isPresent()){
            throw new EntityExistsException("Status name already exists");
        }

        //create new object
        ProcurementStatus procurementStatus = new ProcurementStatus();
        procurementStatus.setName(procurementStatusDto.getName());
        //save to db & return dto
        return procurementStatusRepository.save(procurementStatus).getProcurementStatusDto();

    }

    @Override
    public List<ProcurementStatusDto> getProcurementStatus() {
        return procurementStatusRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ProcurementStatus::getName))
                .map(ProcurementStatus::getProcurementStatusDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProcurementStatusDto getProcurementStatusById(Long id) {
        return procurementStatusRepository.findById(id).map(ProcurementStatus::getProcurementStatusDto)
                .orElseThrow(() -> new EntityNotFoundException("Procurement status with id "+id+ " is not found!"));
    }

    @Override
    public ProcurementStatusDto updateProcurementStatus(Long id, ProcurementStatusDto procurementStatusDto) {
        //check if the status exists
        Optional<ProcurementStatus> optionalProcurementStatus = procurementStatusRepository.findById(id);
        if(optionalProcurementStatus.isEmpty()){
            throw new EntityNotFoundException("Status not found");
        }
        ProcurementStatus existing = optionalProcurementStatus.get();
        boolean isSame = existing.getName().equalsIgnoreCase(procurementStatusDto.getName());
        //check if new name is already present
        if( !isSame &&
                procurementStatusRepository.findFirstByName(procurementStatusDto.getName()).isPresent()){
            throw new EntityExistsException("Status name already exists");
        }
        //update

        existing.setName(procurementStatusDto.getName());
        //save & return as dto
        return procurementStatusRepository.save(existing).getProcurementStatusDto();
    }

    @Override
    public void deleteProcurementStatus(Long id) {
        //check if the status exists
        Optional<ProcurementStatus> optionalProcurementStatus = procurementStatusRepository.findById(id);
        if(optionalProcurementStatus.isEmpty()){
            throw new RuntimeException("Status not found");
        }
        //delete
        procurementStatusRepository.deleteById(id);

    }



    //Users -----------------------------------------------------------------------------

    @Override
    public UserDto createUser(UserDto userDto) throws IOException {

        if(userRepository.findFirstByEmail(userDto.getEmail()).isPresent()){
            throw new EntityExistsException("Email already exists");
        }

        //ids can not be null
        if(userDto.getSubdivId() == null || userDto.getAdmindivId() == null || userDto.getDesignationId() == null) {
            throw new RuntimeException("Sub division, Admin division & Designation are required");
        }

        //nic can not be null
        if(userDto.getNic() == null){
            throw new RuntimeException("NIC is required");
        }


        //finding its sub div
        Subdiv existingSubdiv = subdivRepository.findById(userDto.getSubdivId())
                .orElseThrow(() -> new EntityNotFoundException("Sub division not found!"));


        //finding its admin div
        Admindiv existingAdmindiv = admindivRepository.findById(userDto.getAdmindivId())
                .orElseThrow(() -> new EntityNotFoundException("Admin division not found!"));

        //finding its designation
        Designation existingDesignation = designationRepository.findById(userDto.getDesignationId())
                .orElseThrow(() -> new EntityNotFoundException("Designation not found!"));


            User user = new User();
            user.setEmail(userDto.getEmail());
            //map user role string to enum - using class method
            user.setUserRole(mapStringToUserRole(String.valueOf(userDto.getUserRole())));
            user.setName(userDto.getName());
            user.setEmployeeId(userDto.getEmployeeId());
            user.setNic(userDto.getNic());
            //setting password as nic automatically - when creating
            user.setPassword( new BCryptPasswordEncoder().encode(userDto.getNic()));
            user.setTelephone(userDto.getTelephone());
            user.setBirthdate(userDto.getBirthdate());
            if(userDto.getRecommendationFile() != null) {
                user.setRecommendation(userDto.getRecommendationFile().getBytes());
            }
            //setting objects
            user.setDesignation(existingDesignation);
            user.setAdmindiv(existingAdmindiv);
            user.setSubdiv(existingSubdiv);
            //saving to database & return
            User createdUser = userRepository.save(user);
            return createdUser.getUserDto();

    }

    //method to map user role enum with its string values
    private UserRole mapStringToUserRole(String userRole){
        return switch (userRole){
            case "ADMIN" -> UserRole.ADMIN;
            case "ADMINDIVUSER" -> UserRole.ADMINDIVUSER;
            case "SUBDIVUSER" -> UserRole.SUBDIVUSER;
            default -> UserRole.SUPPLIESUSER;
        };
    }


    //get all users - need to filter out ADMIN as he doesn't have subdiv
    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user-> user.getUserRole() != UserRole.ADMIN)
                .sorted(Comparator.comparing(User::getEmail))
                .map(User::getUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.map(User::getUserDto)
                .orElseThrow( () -> new EntityNotFoundException("User with id "+id+ " is not found!"));
    }


    @Override
    public UserDto updateUser(Long id, UserDto userDto) throws IOException {
        //find the User object
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));

        if(!existingUser.getEmail().equals(userDto.getEmail())){
            //check if email exists
            if(userRepository.findFirstByEmail(userDto.getEmail()).isPresent()){
                throw new EntityExistsException("Email already exists");
            }
            //update with new details
            existingUser.setEmail(userDto.getEmail());
        }

        //finding its sub div
        Subdiv existingSubdiv = subdivRepository.findById(userDto.getSubdivId())
                .orElseThrow(() -> new EntityNotFoundException("Sub division not found!"));


        //finding its admin div
        Admindiv existingAdmindiv = admindivRepository.findById(userDto.getAdmindivId())
                .orElseThrow(() -> new EntityNotFoundException("Admin division not found!"));

        //finding its designation
        Designation existingDesignation = designationRepository.findById(userDto.getDesignationId())
                .orElseThrow(() -> new EntityNotFoundException("Designation not found!"));



        //change password only if there is a value provided
        if(userDto.getPassword()!= null){
            existingUser.setPassword(new BCryptPasswordEncoder().encode(userDto.getPassword()));
        }

        //map user role string to enum
        existingUser.setUserRole(mapStringToUserRole(String.valueOf(userDto.getUserRole())));

        existingUser.setName(userDto.getName());
        existingUser.setEmployeeId(userDto.getEmployeeId());
        existingUser.setNic(userDto.getNic());
        existingUser.setTelephone(userDto.getTelephone());
        existingUser.setBirthdate(userDto.getBirthdate());
//        existingUser.setRecommendation(userDto.getRecommendationFile().getBytes());
        if(userDto.getRecommendationFile() != null){
            if (!userDto.getRecommendationFile().isEmpty()) {
            existingUser.setRecommendation(userDto.getRecommendationFile().getBytes());
            }else {
                existingUser.setRecommendation(null);
            }
        }
        //setting related objects
        existingUser.setDesignation(existingDesignation);
        existingUser.setSubdiv(existingSubdiv);
        existingUser.setAdmindiv(existingAdmindiv);
        //saving to database & return
        return userRepository.save(existingUser).getUserDto();


    }


    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id "+id+ " is not found!"));
        userRepository.deleteById(id);
    }

    //Sub div -----------------------------------------------------------------------------

    @Override
    public SubdivDto createSubdiv(SubdivDto subdivDto) {
        //check for unique constraints
        if(subdivRepository.existsByCode(subdivDto.getCode())){
            throw new RuntimeException("Code already exists");
        }

        if(subdivDto.getAdmindivId() == null) {
            throw new RuntimeException("Should have an admin division");
        }

        //finding its admin div

        Admindiv admindiv = admindivRepository.findById(subdivDto.getAdmindivId())
                .orElseThrow(() -> new EntityNotFoundException("Admin division is not found!"));

            Subdiv subdiv = new Subdiv();
            subdiv.setEmail(subdivDto.getEmail());
            subdiv.setName(subdivDto.getName());
            subdiv.setCode(subdivDto.getCode());
            subdiv.setTelephone(subdivDto.getTelephone());
            subdiv.setAddress(subdivDto.getAddress());
            subdiv.setAdmindiv(admindiv);
            //save amd return
            return subdivRepository.save(subdiv).getSubdivDto();

    }

    @Override
    public List<SubdivDto> getSubdivs() {
        return subdivRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Subdiv::getCode))
                .map(Subdiv::getSubdivDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubdivDto getSubdivById(Long id) {
        Optional<Subdiv> optionalSubdiv = subdivRepository.findById(id);
        return optionalSubdiv.map(Subdiv::getSubdivDto)
                .orElseThrow( () -> new EntityNotFoundException("Sub division with id "+id+ " is not found!"));
    }

    @Override
    public List<SubdivDto> getSubdivsByAdmindivId(Long id) {
        admindivRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Admin div not found!"));
        return subdivRepository.findByAdmindivId(id)
                .stream()
                .sorted(Comparator.comparing(Subdiv::getCode))
                .map(Subdiv::getSubdivDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubdivDto updateSubdiv(Long id, SubdivDto subdivDto) {
        //check for sub div
        Subdiv existingSubdiv = subdivRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sub division is not found!"));

        //check if admin div id is null
        if(subdivDto.getAdmindivId() == null) {
            throw new RuntimeException("Should have an admin division");
        }

        //finding the admin div
        Admindiv admindiv = admindivRepository.findById(subdivDto.getAdmindivId())
                .orElseThrow(() -> new EntityNotFoundException("Admin division is not found!"));

        //if both present -
        //check for unique constraints - if not the same
        if(!(existingSubdiv.getCode().equals(subdivDto.getCode()))
                && subdivRepository.existsByCode(subdivDto.getCode())){
            throw new RuntimeException("Code already exists");
        }

        existingSubdiv.setEmail(subdivDto.getEmail());
        existingSubdiv.setName(subdivDto.getName());
        existingSubdiv.setCode(subdivDto.getCode());
        existingSubdiv.setTelephone(subdivDto.getTelephone());
        existingSubdiv.setAddress(subdivDto.getAddress());
        //setting the admin div object
        existingSubdiv.setAdmindiv(admindiv);
        //save & return as dto
        return  subdivRepository.save(existingSubdiv).getSubdivDto();


    }

    @Override
    public void deleteSubdiv(Long id) {
        Optional<Subdiv> optionalSubdiv= subdivRepository.findById(id);
        if(!optionalSubdiv.isPresent()) {
            throw new EntityNotFoundException("Sub division with id "+id+ " is not found!");
        }
        subdivRepository.deleteById(id);
    }


    //Admin div -----------------------------------------------------------------------------



    @Override
    public AdmindivDto createAdmindiv(AdmindivDto admindivDto) {
        //check for unique constraints
        if(admindivRepository.existsByCode(admindivDto.getCode())){
            throw new RuntimeException("Code already exists");
        }
        if(admindivRepository.existsByName(admindivDto.getName())){
            throw new RuntimeException("Name already exists");
        }

        if(admindivDto.getResponsibleDesignationId() == null){
            throw new RuntimeException("Should have a designation");
        }
        //check for designation object
        Designation responsible = designationRepository.findById((admindivDto.getResponsibleDesignationId()))
                .orElseThrow(() -> new EntityNotFoundException("Designation is not found"));

        Admindiv newAdmindiv = new Admindiv();

        newAdmindiv.setEmail(admindivDto.getEmail());
        newAdmindiv.setName(admindivDto.getName());
        newAdmindiv.setCode(admindivDto.getCode());
        newAdmindiv.setTelephone(admindivDto.getTelephone());
        newAdmindiv.setAddress(admindivDto.getAddress());
        //set designation object
        newAdmindiv.setResponsibleDesignation(responsible);

        return admindivRepository.save(newAdmindiv).getAdmindivDto();
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
    public AdmindivDto getAdmindivById(Long id) {
        //returns an error if not found
        Optional<Admindiv> optionalAdmindiv = admindivRepository.findById(id);

        if(optionalAdmindiv.isPresent()) {
            return optionalAdmindiv.get().getAdmindivDto();
        }else {
            throw new EntityNotFoundException("Admin division with id " + id + " is not found!");
        }

    }

    @Override
    public AdmindivDto updateAdmindiv(Long id, AdmindivDto admindivDto) {

        Optional<Admindiv> optionalAdmindiv = admindivRepository.findById(id);

        if(optionalAdmindiv.isPresent()){
            Admindiv existingAdmindiv = optionalAdmindiv.get();

            //check for unique constraints - if not the same
            if(!(existingAdmindiv.getCode().equals(admindivDto.getCode())) && admindivRepository.existsByCode(admindivDto.getCode())){
                throw new RuntimeException("Code already exists");
            }
            if(!(existingAdmindiv.getName().equals(admindivDto.getName()))&& admindivRepository.existsByName(admindivDto.getName())){
                throw new RuntimeException("Name already exists");
            }

            if(admindivDto.getResponsibleDesignationId()== null){
                throw new RuntimeException("Should have a designation");
            }

            //find the designation object
            Designation responsible = designationRepository.findById(admindivDto.getResponsibleDesignationId())
                    .orElseThrow(() ->  new EntityNotFoundException("Designation not found"));
            //set the new designation
            existingAdmindiv.setResponsibleDesignation(responsible);

            //set other fields
            existingAdmindiv.setEmail(admindivDto.getEmail());
            existingAdmindiv.setName(admindivDto.getName());
            existingAdmindiv.setCode(admindivDto.getCode());
            existingAdmindiv.setTelephone(admindivDto.getTelephone());
            existingAdmindiv.setAddress(admindivDto.getAddress());
            return  admindivRepository.save(existingAdmindiv).getAdmindivDto();
        }else{
            throw new EntityNotFoundException("Admin div not found");
        }

    }

    @Override
    public void deleteAdmindiv(Long id) {
        Optional<Admindiv> optionalAdmindiv= admindivRepository.findById(id);
        if(!optionalAdmindiv.isPresent()) {
            throw new EntityNotFoundException("Admin division with id "+id+ " is not found!");
        }
        admindivRepository.deleteById(id);
    }


    //Designations -----------------------------------------------------------------------------

    //create designation
    @Override
    public Designation createDesignation(Designation newDesignation) {
        //check for unique constraints
        if(designationRepository.existsByCode(newDesignation.getCode())){
            throw new RuntimeException("Code already exists");
        }
        if(designationRepository.existsByTitle(newDesignation.getTitle())){
            throw new RuntimeException("Title already exists");
        }

        //check for compound unique constraints
//        if(designationRepository.existsByTitleAndGrade(newDesignation.getTitle(), newDesignation.getGrade())){
//            throw new RuntimeException("Title & grade already exists");
//        }
//        try{
//            return designationRepository.save(newDesignation);
//        }catch (DataIntegrityViolationException e){
//            throw new RuntimeException("Duplicate entry for title + grade");
//        }

        return designationRepository.save(newDesignation);
    }

    //get designation list
    @Override
    public List<Designation> getAllDesignations() {
        //return sorted by title
        return this.designationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Designation::getTitle))
                .collect(Collectors.toList());
    }
    //get designation by id
    @Override
    public Designation getDesignationById(Long id) {
        //returns null if not found
        Optional<Designation> optionalDesignation = designationRepository.findById(id);
        return optionalDesignation.orElseThrow( () -> new EntityNotFoundException("Designation with id "+id+ " is not found!") );
    }

    //update designation
    @Override
    public Designation updateDesignation(Long id, Designation designation) {
      Designation existingDesignation = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found"));

        //check for unique constraints - if not the same as the existing
        if(!(existingDesignation.getCode().equals(designation.getCode())) && designationRepository.existsByCode(designation.getCode())){
            throw new RuntimeException("Code already exists");
        }
        if(!(existingDesignation.getTitle().equals(designation.getTitle())) && designationRepository.existsByTitle(designation.getTitle())){
            throw new RuntimeException("Title already exists");
        }

//            if(!(existingDesignation.getTitle().equals(designation.getTitle()) && existingDesignation.getGrade().equals(designation.getGrade())) && designationRepository.existsByTitleAndGrade(designation.getTitle(), designation.getGrade())){
//                throw new RuntimeException("Title & grade already exists");
//            }

        existingDesignation.setTitle(designation.getTitle());
        existingDesignation.setCode(designation.getCode());
        return  designationRepository.save(existingDesignation);

    }

    //delete designation
    @Override
    public void deleteDesignation(Long id) {
        Optional<Designation> optionalDesignation = designationRepository.findById(id);
        if(!optionalDesignation.isPresent()) {
            throw new EntityNotFoundException("Designation with id "+id+ " is not found!");
        }
        designationRepository.deleteById(id);
    }

//-----------------------------------------------------------------------------------------------

}
