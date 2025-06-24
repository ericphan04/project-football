package com.swp.myleague.model.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.information.Club;

@Repository
public interface ClubRepo extends JpaRepository<Club, UUID> {

    Club findByUserId(UUID fromString);
    
}
