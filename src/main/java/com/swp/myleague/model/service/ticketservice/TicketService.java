package com.swp.myleague.model.service.ticketservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.ticket.Ticket;
import com.swp.myleague.model.repo.TicketRepo;

@Service
public class TicketService implements IService<Ticket> {

    @Autowired TicketRepo ticketRepo;

    @Override
    public List<Ticket> getAll() {
        return ticketRepo.findAll();
    }

    @Override
    public Ticket getById(String id) {
        return ticketRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    public List<Ticket> getByMatchId(String id) {
        return ticketRepo.findAllByMatchMatchId(UUID.fromString(id));
    }

    

    @Override
    public Ticket save(Ticket e) {
        return ticketRepo.save(e);
    }

    @Override
    public Ticket delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }


    
}
