package com.swp.myleague.model.service.matchservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.match.MatchEvent;
import com.swp.myleague.model.repo.MatchEventRepo;

@Service
public class MatchEventService implements IService<MatchEvent> {

    @Autowired MatchEventRepo matchEventRepo;

    @Override
    public List<MatchEvent> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    public List<MatchEvent> getAllByMatchId(String matchId){
        return matchEventRepo.findAllByMatchMatchId(UUID.fromString(matchId));
    }

    @Override
    public MatchEvent getById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public MatchEvent save(MatchEvent e) {
        return matchEventRepo.save(e);
    }

    @Override
    public MatchEvent delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    
}
