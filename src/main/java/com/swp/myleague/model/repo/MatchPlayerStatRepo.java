package com.swp.myleague.model.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.match.MatchPlayerStat;

@Repository
public interface MatchPlayerStatRepo extends JpaRepository<MatchPlayerStat, UUID> {
    
}
