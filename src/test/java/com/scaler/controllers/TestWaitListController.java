package com.scaler.controllers;

import com.scaler.dtos.*;
import com.scaler.models.User;
import com.scaler.models.UserType;
import com.scaler.models.WaitListPosition;
import com.scaler.repositories.UserRepository;
import com.scaler.repositories.WaitListPositionRepository;
import com.scaler.services.WaitListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWaitListController {

    private UserRepository userRepository;
    private WaitListPositionRepository waitListPositionRepository;
    private WaitListService waitListService;
    private WaitListController waitListController;

    @BeforeEach
    public void setupTest() throws Exception {
        initializeComponents();
    }

    private void initializeComponents() throws Exception {
        initializeRepositories();
        initializePaymentService();
        initializePaymentController();
    }

    private <T> T createInstance(Class<T> interfaceClass, Reflections reflections) throws Exception {
        Set<Class<? extends T>> implementations = reflections.getSubTypesOf(interfaceClass);
        if (implementations.isEmpty()) {
            throw new Exception("No implementation for " + interfaceClass.getSimpleName() + " found");
        }

        Class<? extends T> implementationClass = implementations.iterator().next();
        return getT(implementationClass);
    }

    private static <T> T getT(Class<? extends T> implementationClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<? extends T> constructor = implementationClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private <T> T createInstanceWithArgs(Class<T> interfaceClass, Reflections reflections, List<Object> dependencies) throws Exception {
        Set<Class<? extends T>> implementations = reflections.getSubTypesOf(interfaceClass);
        if (implementations.isEmpty()) {
            throw new Exception("No implementation for " + interfaceClass.getSimpleName() + " found");
        }
        Class<? extends T> implementationClass = implementations.iterator().next();
        Constructor<?>[] constructors = implementationClass.getConstructors();
        Constructor<?> constructor = Arrays.stream(constructors)
                .filter(constructor1 -> constructor1.getParameterCount() == dependencies.size())
                .findFirst().orElseThrow(() -> new Exception("No constructor with " + dependencies.size() + " arguments found"));
        constructor.setAccessible(true);
        Object[] args = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            for (Object dependency : dependencies) {
                if (constructor.getParameterTypes()[i].isInstance(dependency)) {
                    args[i] = dependency;
                    break;
                }
            }
        }
        return (T) constructor.newInstance(args);
    }

    private void initializeRepositories() throws Exception {
        Reflections repositoryReflections = new Reflections(UserRepository.class.getPackageName(), new SubTypesScanner(false));
        this.userRepository = createInstance(UserRepository.class, repositoryReflections);
        this.waitListPositionRepository = createInstance(WaitListPositionRepository.class, repositoryReflections);
    }

    private void initializePaymentService() throws Exception {
        Reflections serviceReflections = new Reflections(WaitListService.class.getPackageName(), new SubTypesScanner(false));
        this.waitListService = createInstanceWithArgs(WaitListService.class, serviceReflections, Arrays.asList(this.userRepository, this.waitListPositionRepository));
    }


    private void initializePaymentController() {
        this.waitListController = new WaitListController(this.waitListService);
    }

    @Test
    public void testUpdateWaitList_Success(){
        User user = new User();
        user.setName("Test User");
        user.setUserType(UserType.ADMIN);
        user = this.userRepository.save(user);
        WaitListPosition waitListPosition = new WaitListPosition();
        waitListPosition.setUser(user);
        waitListPosition.setInsertedAt(new Date());
        waitListPosition = this.waitListPositionRepository.save(waitListPosition);

        UpdateWaitListRequestDto updateWaitListRequestDto = new UpdateWaitListRequestDto();
        updateWaitListRequestDto.setUserId(user.getId());
        updateWaitListRequestDto.setNumberOfSeats(1);
        UpdateWaitListResponseDto updateWaitListResponseDto = waitListController.updateWaitList(updateWaitListRequestDto);
        assertEquals(ResponseStatus.SUCCESS, updateWaitListResponseDto.getResponseStatus(), "Response status should be SUCCESS");
        List<WaitListPosition> all = this.waitListPositionRepository.findAll();
        assertEquals(0, all.size(), "Waitlist should be empty");
    }

    @Test
    public void testUpdateWaitList_Success_RemovingMoreUsersThanInTheQueue(){
        User user = new User();
        user.setName("Test User");
        user.setUserType(UserType.ADMIN);
        user = this.userRepository.save(user);
        WaitListPosition waitListPosition = new WaitListPosition();
        waitListPosition.setUser(user);
        waitListPosition.setInsertedAt(new Date());
        waitListPosition = this.waitListPositionRepository.save(waitListPosition);

        UpdateWaitListRequestDto updateWaitListRequestDto = new UpdateWaitListRequestDto();
        updateWaitListRequestDto.setUserId(user.getId());
        updateWaitListRequestDto.setNumberOfSeats(2);
        UpdateWaitListResponseDto updateWaitListResponseDto = waitListController.updateWaitList(updateWaitListRequestDto);
        assertEquals(ResponseStatus.SUCCESS, updateWaitListResponseDto.getResponseStatus(), "Response status should be SUCCESS");
        List<WaitListPosition> all = this.waitListPositionRepository.findAll();
        assertEquals(0, all.size(), "Waitlist should be empty");
    }

    @Test
    public void testUpdateWaitList_Failure_Unauthorized(){
        User user = new User();
        user.setName("Test User");
        user.setUserType(UserType.USER);
        user = this.userRepository.save(user);
        WaitListPosition waitListPosition = new WaitListPosition();
        waitListPosition.setUser(user);
        waitListPosition.setInsertedAt(new Date());
        waitListPosition = this.waitListPositionRepository.save(waitListPosition);

        UpdateWaitListRequestDto updateWaitListRequestDto = new UpdateWaitListRequestDto();
        updateWaitListRequestDto.setUserId(user.getId());
        updateWaitListRequestDto.setNumberOfSeats(1);
        UpdateWaitListResponseDto updateWaitListResponseDto = waitListController.updateWaitList(updateWaitListRequestDto);
        assertEquals(ResponseStatus.FAILURE, updateWaitListResponseDto.getResponseStatus(), "Response status should be FAILURE");
        List<WaitListPosition> all = this.waitListPositionRepository.findAll();
        assertEquals(1, all.size(), "Waitlist should not be empty");
    }

    @Test
    public void testAddUserToWaitList_Success(){
        User user = new User();
        user.setName("Test User");
        user.setUserType(UserType.USER);
        user = this.userRepository.save(user);

        AddUserToWaitListRequestDto addUserToWaitListRequestDto = new AddUserToWaitListRequestDto();
        addUserToWaitListRequestDto.setUserId(user.getId());
        AddUserToWaitListResponseDto addUserToWaitListResponseDto = waitListController.addUserToWaitList(addUserToWaitListRequestDto);
        assertEquals(1, addUserToWaitListResponseDto.getPosition(), "Position should be 1");

        List<WaitListPosition> all = this.waitListPositionRepository.findAll();
        assertEquals(1, all.size(), "Waitlist should not be empty");
    }

    @Test
    public void testAddUserToWaitList_AddingDuplicateUser(){
        User user = new User();
        user.setName("Test User");
        user.setUserType(UserType.USER);
        user = this.userRepository.save(user);

        AddUserToWaitListRequestDto addUserToWaitListRequestDto = new AddUserToWaitListRequestDto();
        addUserToWaitListRequestDto.setUserId(user.getId());
        AddUserToWaitListResponseDto addUserToWaitListResponseDto = waitListController.addUserToWaitList(addUserToWaitListRequestDto);
        assertEquals(ResponseStatus.SUCCESS, addUserToWaitListResponseDto.getResponseStatus(), "Response status should be SUCCESS");
        assertEquals(1, addUserToWaitListResponseDto.getPosition(), "Position should be 1");

        List<WaitListPosition> all = this.waitListPositionRepository.findAll();
        assertEquals(1, all.size(), "Waitlist should not be empty");

        addUserToWaitListResponseDto = waitListController.addUserToWaitList(addUserToWaitListRequestDto);
        assertEquals(ResponseStatus.SUCCESS, addUserToWaitListResponseDto.getResponseStatus(), "Response status should be SUCCESS");
        assertEquals(1, addUserToWaitListResponseDto.getPosition(), "Position should be 1");
    }

    @Test
    public void testAddUserToWaitList_Failure_UserNotFound(){
        AddUserToWaitListRequestDto addUserToWaitListRequestDto = new AddUserToWaitListRequestDto();
        addUserToWaitListRequestDto.setUserId(1);
        AddUserToWaitListResponseDto addUserToWaitListResponseDto = waitListController.addUserToWaitList(addUserToWaitListRequestDto);
        assertEquals(ResponseStatus.FAILURE, addUserToWaitListResponseDto.getResponseStatus(), "Response status should be FAILURE");
        List<WaitListPosition> all = this.waitListPositionRepository.findAll();
        assertEquals(0, all.size(), "Waitlist should be empty");
    }

    @Test
    public void testGetWaitListStatus_Success(){
        User user = new User();
        user.setName("Test User");
        user.setUserType(UserType.USER);
        user = this.userRepository.save(user);
        WaitListPosition waitListPosition = new WaitListPosition();
        waitListPosition.setUser(user);
        waitListPosition.setInsertedAt(new Date());
        waitListPosition = this.waitListPositionRepository.save(waitListPosition);

        GetUserWaitListRequestDto getUserWaitListRequestDto = new GetUserWaitListRequestDto();
        getUserWaitListRequestDto.setUserId(user.getId());
        GetUserWaitListResponseDto getUserWaitListResponseDto = waitListController.getWaitListStatus(getUserWaitListRequestDto);
        assertEquals(ResponseStatus.SUCCESS, getUserWaitListResponseDto.getResponseStatus(), "Response status should be SUCCESS");
        assertEquals(1, getUserWaitListResponseDto.getPosition(), "Position should be 1");
        List<WaitListPosition> all = this.waitListPositionRepository.findAll();
        assertEquals(1, all.size(), "Waitlist should not be empty");

        // Removing the user from waitlist
        waitListPositionRepository.delete(waitListPosition);
        getUserWaitListResponseDto = waitListController.getWaitListStatus(getUserWaitListRequestDto);
        assertEquals(ResponseStatus.SUCCESS, getUserWaitListResponseDto.getResponseStatus(), "Response status should be SUCCESS");
        assertEquals(-1, getUserWaitListResponseDto.getPosition(), "Position should be -1");
    }

    @Test
    public void testGetWaitListStatus_Failure_UserNotFound(){
        GetUserWaitListRequestDto getUserWaitListRequestDto = new GetUserWaitListRequestDto();
        getUserWaitListRequestDto.setUserId(1);
        GetUserWaitListResponseDto getUserWaitListResponseDto = waitListController.getWaitListStatus(getUserWaitListRequestDto);
        assertEquals(ResponseStatus.FAILURE, getUserWaitListResponseDto.getResponseStatus(), "Response status should be FAILURE");
    }

    @Test
    public void testSimulateWaitList(){
        List<User> users = setupUsers();
        User adminUser = setupAdminUser();

        // Adding 1st user to waitlist
        AddUserToWaitListRequestDto addUserToWaitListRequestDto = new AddUserToWaitListRequestDto();
        addUserToWaitListRequestDto.setUserId(users.get(0).getId());
        AddUserToWaitListResponseDto addUserToWaitListResponseDto = waitListController.addUserToWaitList(addUserToWaitListRequestDto);
        assertEquals(1, addUserToWaitListResponseDto.getPosition(), "Position should be 1");

        // Adding 2nd user to waitlist
        addUserToWaitListRequestDto = new AddUserToWaitListRequestDto();
        addUserToWaitListRequestDto.setUserId(users.get(1).getId());
        addUserToWaitListResponseDto = waitListController.addUserToWaitList(addUserToWaitListRequestDto);
        assertEquals(2, addUserToWaitListResponseDto.getPosition(), "Position should be 2");

        // User 2 checks their position
        GetUserWaitListRequestDto getUserWaitListRequestDto = new GetUserWaitListRequestDto();
        getUserWaitListRequestDto.setUserId(users.get(1).getId());
        GetUserWaitListResponseDto getUserWaitListResponseDto = waitListController.getWaitListStatus(getUserWaitListRequestDto);
        assertEquals(2, getUserWaitListResponseDto.getPosition(), "Position should be 2");

        // Adding 3rd user to waitlist
        addUserToWaitListRequestDto = new AddUserToWaitListRequestDto();
        addUserToWaitListRequestDto.setUserId(users.get(2).getId());
        addUserToWaitListResponseDto = waitListController.addUserToWaitList(addUserToWaitListRequestDto);
        assertEquals(3, addUserToWaitListResponseDto.getPosition(), "Position should be 3");

        // Emptying 2 spots from waitlist
        UpdateWaitListRequestDto updateWaitListRequestDto = new UpdateWaitListRequestDto();
        updateWaitListRequestDto.setUserId(adminUser.getId());
        updateWaitListRequestDto.setNumberOfSeats(2);
        UpdateWaitListResponseDto updateWaitListResponseDto = waitListController.updateWaitList(updateWaitListRequestDto);
        assertEquals(ResponseStatus.SUCCESS, updateWaitListResponseDto.getResponseStatus(), "Response status should be SUCCESS");

        // User 1 checks their position -> should be -1
        getUserWaitListRequestDto = new GetUserWaitListRequestDto();
        getUserWaitListRequestDto.setUserId(users.get(0).getId());
        getUserWaitListResponseDto = waitListController.getWaitListStatus(getUserWaitListRequestDto);
        assertEquals(-1, getUserWaitListResponseDto.getPosition(), "Position should be -1");

        // User 2 checks their position -> should be -1
        getUserWaitListRequestDto = new GetUserWaitListRequestDto();
        getUserWaitListRequestDto.setUserId(users.get(1).getId());
        getUserWaitListResponseDto = waitListController.getWaitListStatus(getUserWaitListRequestDto);
        assertEquals(-1, getUserWaitListResponseDto.getPosition(), "Position should be -1");

        // User 4 added to waitlist
        addUserToWaitListRequestDto = new AddUserToWaitListRequestDto();
        addUserToWaitListRequestDto.setUserId(users.get(3).getId());
        addUserToWaitListResponseDto = waitListController.addUserToWaitList(addUserToWaitListRequestDto);
        assertEquals(2, addUserToWaitListResponseDto.getPosition(), "Position should be 2");

        // User 3 checks their position -> should be 1
        getUserWaitListRequestDto = new GetUserWaitListRequestDto();
        getUserWaitListRequestDto.setUserId(users.get(2).getId());
        getUserWaitListResponseDto = waitListController.getWaitListStatus(getUserWaitListRequestDto);
        assertEquals(1, getUserWaitListResponseDto.getPosition(), "Position should be 1");
    }

    private User setupAdminUser() {
        User user = new User();
        user.setName("Admin User");
        user.setUserType(UserType.ADMIN);
        return userRepository.save(user);
    }

    private List<User> setupUsers() {
        List<User> users = new ArrayList<>();
        for(int i=0; i<5; i++){
            User user = new User();
            user.setName("User " + i);
            user.setUserType(UserType.USER);
            users.add(userRepository.save(user));
        }
        return users;
    }
}
