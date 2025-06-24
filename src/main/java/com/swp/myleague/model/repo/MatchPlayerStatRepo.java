package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.match.MatchPlayerStat;

@Repository
public interface MatchPlayerStatRepo extends JpaRepository<MatchPlayerStat, UUID> {

    List<MatchPlayerStat> findAllByPlayerPlayerId(UUID fromString);

    List<MatchPlayerStat> findAllByMatchMatchId(UUID fromString);
    
}
