package com.swp.myleague.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swp.myleague.model.entities.information.PlayerPosition;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.informationservice.PlayerService;
import com.swp.myleague.model.service.matchservice.MatchClubStatService;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.utils.GoogleMapApiService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
@RequestMapping(value = { "/club", "/club/" })
public class ClubController {

    @Autowired
    ClubService clubService;

    @Autowired
    PlayerService playerService;

    @Autowired
    MatchService matchService;

    @Autowired
    MatchClubStatService matchClubStatService;

    @Autowired
    GoogleMapApiService googleMapApiService;

    @GetMapping("")
    public String getAllClubs(Model model) {
        model.addAttribute("clubs",
                clubService.getAll().stream().filter(c -> c.getClubLogoPath().contains("/images/logoclub")).toList());
        model.addAttribute("allTimeClubs", clubService.getAll());
        return "Club";
    }

    @GetMapping("/{clubId}")
    public String getDetailClub(@PathVariable("clubId") String clubId, Model model) {
        model.addAttribute("club", clubService.getById(clubId));
        return "DetailClubOverview";
    }

    @GetMapping("/{clubId}/squad")
    public String getSquadClub(@PathVariable("clubId") String clubId, Model model) {
        model.addAttribute("club", clubService.getById(clubId));
        model.addAttribute("goalkeepers", playerService.getPlayersByClubId(clubId).stream()
                .filter(p -> p.getPlayerPosition().equals(PlayerPosition.GK)));
        model.addAttribute("defenders", playerService.getPlayersByClubId(clubId).stream()
                .filter(p -> p.getPlayerPosition().equals(PlayerPosition.CB)));
        model.addAttribute("midfielders", playerService.getPlayersByClubId(clubId).stream()
                .filter(p -> p.getPlayerPosition().equals(PlayerPosition.MD)));
        model.addAttribute("forwards", playerService.getPlayersByClubId(clubId).stream()
                .filter(p -> p.getPlayerPosition().equals(PlayerPosition.FW)));
        return "DetailClubSquad";
    }

    @GetMapping("/player/{playerId}")
    public String getDetailPlayer(@PathVariable("playerId") String playerId, Model model) {
        model.addAttribute("player", playerService.getById(playerId));
        return "DetailPlayer";
    }

    @GetMapping("/{clubId}/results")
    public String getClubResults(@PathVariable("clubId") String clubId, Model model) {
        model.addAttribute("club", clubService.getById(clubId));
        try {
            List<MatchClubStat> matchClubStats = matchClubStatService.getAllByClubId(clubId);
            List<Match> matches = matchService.getAll().stream()
                    .filter(m -> m.getMatchClubStats().stream()
                            .anyMatch(mcs -> matchClubStats.stream()
                                    .anyMatch(mcss -> mcss.getMatchClubStatId().equals(mcs.getMatchClubStatId()))))
                    .toList();
            model.addAttribute("matches", matches);
            return "DetailClubResults";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DetailClubOverview";

    }

    @GetMapping("/{clubId}/fixtures")
    public String getStatsOfClub(@PathVariable("clubId") String clubId, Model model) {
        model.addAttribute("club", clubService.getById(clubId));
        return "DetailClubFixture";
    }

    @GetMapping("/{clubId}/stadium")
    public String showMap(@PathVariable("clubId") String clubId, Model model) {
        model.addAttribute("club", clubService.getById(clubId));
        return "DetailClubStadium";
    }
    

}
