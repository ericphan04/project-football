package com.swp.myleague.model.entities.match;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "'match'")
public class Match {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID matchId;

    private String matchTitle;
    private LocalDateTime matchStartTime;
    private String matchDescription;
    private String matchLinkLivestream;

    @OneToMany(mappedBy = "match", fetch = FetchType.EAGER)
    private List<MatchEvent> matchEvents;

    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY)
    private List<MatchPlayerStat> matchPlayerStats;

    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY)
    private List<MatchClubStat> matchClubStats;
    
}
