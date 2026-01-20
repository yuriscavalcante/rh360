package com.rh360.rh360.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.TimeClock;

@Repository
public interface TimeClockRepository extends JpaRepository<TimeClock, UUID> {

    List<TimeClock> findByUser_IdOrderByTimestampDesc(UUID userId);
}
