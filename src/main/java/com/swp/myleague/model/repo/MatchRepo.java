package com.swp.myleague.model.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.match.Match;

@Repository
public interface MatchRepo extends JpaRepository<Match, UUID> {

    @Query("""
        SELECT m FROM Match m
        JOIN m.matchClubStats mcs
        WHERE mcs.club.clubId = :clubId
          AND m.matchStartTime > CURRENT_TIMESTAMP
        ORDER BY m.matchStartTime ASC
        """)
    List<Match> findUpcomingMatchesByClub(@Param("clubId") UUID clubId);

    List<Match> findByMatchDescription(String roundDescription);

    List<Match> findByMatchStartTimeBetweenAndMatchEventsIsEmpty(LocalDateTime from, LocalDateTime to);
    
}
