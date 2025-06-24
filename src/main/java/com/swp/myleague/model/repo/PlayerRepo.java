package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.information.Player;

@Repository
public interface PlayerRepo extends JpaRepository<Player, UUID> {

    List<Player> findAllByClubClubId(UUID fromString);

    // List<Player> getTop10ScorePlayers();
    
}
