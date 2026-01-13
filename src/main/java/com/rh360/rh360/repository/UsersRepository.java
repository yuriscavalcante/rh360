package com.rh360.rh360.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.User;

@Repository
public interface UsersRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);
} 
