package com.swp.myleague.model.service.informationservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.model.repo.ClubRepo;

@Service
public class ClubService implements IService<Club> {

    @Autowired ClubRepo clubRepo;

    @Override
    public List<Club> getAll() {
        return clubRepo.findAll();
    }

    @Override
    public Club getById(String id) {
        return clubRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    public Club getByUserId(UUID userId) {
        return clubRepo.findByUserId(userId);
    }

    @Override
    public Club save(Club e) {
        return clubRepo.save(e);
    }

    @Override
    public Club delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    
}
