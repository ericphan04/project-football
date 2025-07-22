package com.swp.myleague.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swp.myleague.model.service.matchservice.MatchClubStatService;
import com.swp.myleague.model.service.matchservice.MatchEventService;
import com.swp.myleague.model.service.matchservice.MatchPlayerStatService;
import com.swp.myleague.model.service.matchservice.MatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping(value = { "/match", "/match/" })
public class MatchController {

    @Autowired
    MatchService matchService;

    @Autowired
    MatchClubStatService matchClubStatService;

    @Autowired
    MatchPlayerStatService matchPlayerStatService;

    @Autowired
    MatchEventService matchEventService;

    @GetMapping("")
    public String getAllMatch(Model model) {
        model.addAttribute("matches", matchService.getAll().stream()
                .filter(m -> m.getMatchStartTime().compareTo(LocalDateTime.now()) < 0).toList());
        return "Match";
    }

    @GetMapping("/{matchId}")
    public String getMatchById(@PathVariable(name = "matchId") String matchId, Model model) {
        model.addAttribute("match", matchService.getById(matchId));
        return "DetailMatch";
    }

    @GetMapping("/fixture")
    public String getFixture(Model model) {
        model.addAttribute("matches", matchService.getAll().stream()
                .filter(m -> m.getMatchStartTime().compareTo(LocalDateTime.now()) > 0).toList());
        return "Fixture";
    }

}
