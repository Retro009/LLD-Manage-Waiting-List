# Manage Wait List for a Restaurant

## Problem Statement
You are building a Restaurant Management System. As a part of this system, you need to build a feature using which the restaurant can manage the wait list of customers.

## Assignment

Your task is to implement the wait list management system.

### Task 1 - Adding Customers to the Wait List

We need to implement a functionality using which customers can add themselves to the wait list.

#### Requirements
1. This functionality will be used by customers.
2. A customer should be already registered with the restaurant (i.e. their details should be present in the database) before they can add themselves to the wait list.
3. If a customer is not registered with the restaurant, they should get a error message saying "User not found".
4. Once a customer is added to the wait list, we should return their position in the wait list in the response.
5. If a customer is already present in the wait list, we should return their position in the wait list in the response.

#### Instructions

* We need to implement the `addUserToWaitList` method in `WaitListController` class.
* Refer the DTOs `AddUserToWaitListRequestDto` and `AddUserToWaitListResponseDto` for request and response details.
* Implement the relevant methods of `WaitListService`, `UserRepository` and `WaitListRepository` classes to achieve the above requirements.
* Refer the models package to understand the models to be used.
* Take a look at the test cases in WaitListControllerTest class to understand the requirements better.


### Task 2 - Get Position in the Wait List

We need to implement a functionality using which customers can get their position in the wait list.

#### Requirements
1. This functionality will be used by customers.
2. A customer should be already registered with the restaurant (i.e. their details should be present in the database) before they can get their position in the wait list.
3. If a customer is not registered with the restaurant, they should get a error message saying "User not found".
4. If a customer is not present in the wait list, they should get -1 as their position in the wait list.
5. If a customer is present in the wait list, they should get their position in the wait list.


#### Instructions

* We need to implement the `getWaitListStatus` method in `WaitListController` class.
* Refer the DTOs `GetUserWaitListRequestDto` and `GetUserWaitListResponseDto` for request and response details.
* Implement the relevant methods of `WaitListService`, `UserRepository` and `WaitListRepository` classes to achieve the above requirements.
* Refer the models package to understand the models to be used.
* Take a look at the test cases in WaitListControllerTest class to understand the requirements better.

### Task 3 - Remove Customer from the Wait List

We need to implement a functionality using which restaurant admin can remove customers from the wait list.

#### Requirements
1. This functionality should be accessible by restaurant admin.
2. If a customer tries to access this functionality, they should get a error message saying "Access Denied".
3. The restaurant admin should be already registered with the restaurant (i.e. their details should be present in the database) before they can remove customers from the wait list.
4. The restaurant admin will give the number of customers to be removed from the wait list.
5. If the number of customers to be removed is greater than the number of customers in the wait list, we should remove all the customers from the wait list.
6. If the number of customers to be removed is less than or equal to the number of customers in the wait list, we should remove the given number of customers from the wait list.

#### Instructions

* We need to implement the `updateWaitList` method in `WaitListController` class.
* Refer the DTOs `UpdateWaitListRequestDto` and `UpdateWaitListResponseDto` for request and response details.
* Implement the relevant methods of `WaitListService`, `UserRepository` and `WaitListRepository` classes to achieve the above requirements.
* Refer the models package to understand the models to be used.
* Take a look at the test cases in WaitListControllerTest class to understand the requirements better.