package com.rh360.rh360.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

  @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Team> findByNameContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
