package com.rh360.rh360.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.User;

@Repository
public interface UsersRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<User> findByNameContainingIgnoreCase(@Param("search") String search, Pageable pageable);
} 
