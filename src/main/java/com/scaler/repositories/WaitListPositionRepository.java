package com.scaler.repositories;

import com.scaler.models.WaitListPosition;

import java.util.List;

public interface WaitListPositionRepository {

    WaitListPosition save(WaitListPosition waitListPosition);

    List<WaitListPosition> findAll();

    WaitListPosition delete(WaitListPosition waitListPosition);
}
