package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.payload.ClubStandingDTO;

@Repository
public interface MatchClubStatRepo extends JpaRepository<MatchClubStat, UUID> {

    public List<MatchClubStat> findAllByMatchMatchId(UUID matchId);

    public List<MatchClubStat> findAllByClubClubId(UUID fromString);

    @Query(value = """
                SELECT
                    c.club_name AS clubName,
                    YEAR(m.match_start_time) AS season,
                    SUM(
                        CASE
                            WHEN m.match_start_time <= NOW()
                            AND mcs.match_club_stat_score > opp.match_club_stat_score THEN 3
                            WHEN m.match_start_time <= NOW()
                            AND mcs.match_club_stat_score = opp.match_club_stat_score THEN 1
                            ELSE 0
                        END
                    ) AS points,
                    c.club_logo_path AS logoUrl,
                    COUNT(mcs.match_id) AS played,
                    SUM(CASE WHEN m.match_start_time <= NOW() AND mcs.match_club_stat_score > opp.match_club_stat_score THEN 1 ELSE 0 END) AS wins,
                    SUM(CASE WHEN m.match_start_time <= NOW() AND mcs.match_club_stat_score = opp.match_club_stat_score THEN 1 ELSE 0 END) AS draws,
                    SUM(CASE WHEN m.match_start_time <= NOW() AND mcs.match_club_stat_score < opp.match_club_stat_score THEN 1 ELSE 0 END) AS losses,
                    SUM(mcs.match_club_stat_score) AS goalsFor,
                    SUM(opp.match_club_stat_score) AS goalsAgainst,
                    SUM(mcs.match_club_stat_score - opp.match_club_stat_score) AS goalDifference,
                    '' AS nextLogoUrl -- Tạm thời để trống
                FROM match_club_stat mcs
                JOIN `'match'` m ON m.match_id = mcs.match_id
                JOIN club c ON c.club_id = mcs.club_id
                JOIN match_club_stat opp
                    ON opp.match_id = mcs.match_id AND opp.club_id != mcs.club_id
                WHERE YEAR(m.match_start_time) = :season
                GROUP BY c.club_name, c.club_logo_path, YEAR(m.match_start_time)
                ORDER BY points DESC, goalDifference DESC, goalsFor DESC
            """, nativeQuery = true)
    List<ClubStandingDTO> findClubStandingsBySeason(@Param("season") Integer season);

}
