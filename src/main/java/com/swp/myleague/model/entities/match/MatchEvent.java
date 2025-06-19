package com.swp.myleague.model.entities.match;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.swp.myleague.model.entities.information.Player;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class MatchEvent {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID matchEventId;

    private String matchEventTitle;
    
    @Enumerated(EnumType.STRING)
    private MatchEventType matchEventType;

    @ManyToOne
    @JoinColumn(name = "matchId")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "playerId")
    private Player player;

}
