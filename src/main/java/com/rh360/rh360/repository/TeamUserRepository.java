package com.rh360.rh360.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.TeamUser;
import com.rh360.rh360.entity.TeamUserId;

@Repository
public interface TeamUserRepository extends JpaRepository<TeamUser, TeamUserId> {
    
    List<TeamUser> findByIdTeamId(UUID teamId);
    
    List<TeamUser> findByIdUserId(UUID userId);
    
    void deleteByIdTeamId(UUID teamId);
    
    void deleteByIdTeamIdAndIdUserId(UUID teamId, UUID userId);
}
