package com.scaler.services;

import com.scaler.exceptions.UnauthorizedAccessException;
import com.scaler.exceptions.UserNotFoundException;

public interface WaitListService {

    int addUserToWaitList(long userId) throws UserNotFoundException;

    int getWaitListPosition(long userId) throws UserNotFoundException;

    void updateWaitList(long adminUserId, int numberOfSpots) throws UserNotFoundException, UnauthorizedAccessException;

}