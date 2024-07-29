package com.scaler.services;

import com.scaler.exceptions.UnauthorizedAccessException;
import com.scaler.exceptions.UserNotFoundException;
import com.scaler.models.User;
import com.scaler.models.UserType;
import com.scaler.models.WaitListPosition;
import com.scaler.repositories.UserRepository;
import com.scaler.repositories.WaitListPositionRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class WaitListServiceImpl implements WaitListService{
    private UserRepository userRepository;
    private WaitListPositionRepository waitListPositionRepository;

    public WaitListServiceImpl(UserRepository userRepository, WaitListPositionRepository waitListPositionRepository){
        this.userRepository = userRepository;
        this.waitListPositionRepository = waitListPositionRepository;
    }
    @Override
    public int addUserToWaitList(long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(("User not found")));

        int waitListPosition = getWaitListPosition(userId);

        if(waitListPosition == -1){
            WaitListPosition position = new WaitListPosition();
            position.setUser(user);
            position.setInsertedAt(new Date());
            waitListPositionRepository.save(position);

            return waitListPositionRepository.findAll().size();
        }

        return waitListPosition;
    }

    @Override
    public int getWaitListPosition(long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(("User not found")));
        List<WaitListPosition> waitingList = waitListPositionRepository.findAll();
        for(int i=0;i<waitingList.size();i++){
            if(waitingList.get(i).getId()==userId)
                return i+1;
        }
        return -1;
    }

    @Override
    public void updateWaitList(long adminUserId, int numberOfSpots) throws UserNotFoundException, UnauthorizedAccessException {
        User adminUser = userRepository.findById(adminUserId).orElseThrow(()-> new UserNotFoundException("User Not Found"));
        if(adminUser.getUserType().equals(UserType.USER))
            throw new UnauthorizedAccessException("ACCESS DENIED");
        List<WaitListPosition> waitingList = waitListPositionRepository.findAll();
        numberOfSpots = Math.min(numberOfSpots,waitingList.size());
        for(int i=0;i<numberOfSpots;i++){
            waitListPositionRepository.delete(waitingList.get(i));
        }
    }
}
