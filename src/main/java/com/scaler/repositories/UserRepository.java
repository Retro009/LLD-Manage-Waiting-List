package com.scaler.repositories;

import com.scaler.models.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(long id);
}
