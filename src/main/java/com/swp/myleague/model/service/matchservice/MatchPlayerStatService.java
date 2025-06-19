package com.swp.myleague.model.service.matchservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.match.MatchPlayerStat;
import com.swp.myleague.model.repo.MatchPlayerStatRepo;

@Service
public class MatchPlayerStatService implements IService<MatchPlayerStat> {

    @Autowired
    MatchPlayerStatRepo matchPlayerStatRepo;

    @Override
    public List<MatchPlayerStat> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    public List<MatchPlayerStat> getAllByPlayerId(String playerId) {
        return matchPlayerStatRepo.findAllByPlayerPlayerId(UUID.fromString(playerId));
    }

    public List<MatchPlayerStat> getAllByMatchId(String matchId) {
        return matchPlayerStatRepo.findAllByMatchMatchId(UUID.fromString(matchId));
    }

    public List<MatchPlayerStat> getAllByPlayerAndMatch(String playerId, String matchId) {
        return matchPlayerStatRepo.findAllByPlayerPlayerId(UUID.fromString(playerId)).stream()
                .filter(mps -> mps.getMatch().getMatchId().equals(UUID.fromString(matchId))).toList();
    }

    @Override
    public MatchPlayerStat getById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public MatchPlayerStat save(MatchPlayerStat e) {
        return matchPlayerStatRepo.save(e);
    }

    @Override
    public MatchPlayerStat delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
