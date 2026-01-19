package com.rh360.rh360.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
}
