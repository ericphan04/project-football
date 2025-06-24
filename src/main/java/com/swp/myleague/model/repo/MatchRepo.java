package com.swp.myleague.model.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.match.Match;

@Repository
public interface MatchRepo extends JpaRepository<Match, UUID> {
    
}
