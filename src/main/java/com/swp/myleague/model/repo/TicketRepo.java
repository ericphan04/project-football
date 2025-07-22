package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.ticket.Ticket;

@Repository
public interface TicketRepo extends JpaRepository<Ticket, UUID> {

    List<Ticket> findAllByMatchMatchId(UUID fromString);
    
}
