package com.swp.myleague.model.service.informationservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.repo.PlayerRepo;

@Service
public class PlayerService implements IService<Player> {

    @Autowired PlayerRepo playerRepo;

    @Override
    public List<Player> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    public List<Player> getPlayersByClubId(String clubId) {
        return playerRepo.findAllByClubClubId(UUID.fromString(clubId));
    }

    // public List<Player> getTop10ScorePlayers() {
    //     return playerRepo.getTop10ScorePlayers();
    // }

    @Override
    public Player getById(String id) {
        return playerRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    @Override
    public Player save(Player e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Player delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    
}
