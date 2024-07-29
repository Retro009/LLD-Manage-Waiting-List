package com.scaler.repositories;

import com.scaler.models.WaitListPosition;

import java.util.*;

public class WaitListPositionRepositoryImpl implements WaitListPositionRepository{
    private List<WaitListPosition> waitingList = new LinkedList<>();
    private static long idCounter = 0;

    @Override
    public WaitListPosition save(WaitListPosition waitListPosition) {
        if(waitListPosition.getId()==0)
            waitListPosition.setId(++idCounter);
        waitingList.add(waitListPosition);
        return waitListPosition;
    }

    @Override
    public List<WaitListPosition> findAll() {
        return waitingList.stream().toList();
    }


    @Override
    public WaitListPosition delete(WaitListPosition waitListPosition) {
        waitingList.remove(waitListPosition);
        return waitListPosition;
    }
}