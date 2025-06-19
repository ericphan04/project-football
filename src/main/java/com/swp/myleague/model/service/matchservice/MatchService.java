package com.swp.myleague.model.service.matchservice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.repo.MatchRepo;

@Service
public class MatchService implements IService<Match> {
    
    @Autowired MatchRepo matchRepo;

    @Override
    public List<Match> getAll() {
        return matchRepo.findAll();
    }

    @Override
    public Match getById(String id) {
        return matchRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    @Override
    public Match save(Match e) {
        e.setMatchStartTime(LocalDateTime.now());
        return matchRepo.save(e);
    }

    @Override
    public Match delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public Player getPom(String matchId){
        return null;
    }

    public List<Player> getScorerOfTheMatch(String matchId){
        return null;
    }   


}
