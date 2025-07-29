package com.swp.myleague.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.swp.myleague.model.entities.Role;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.entities.match.MatchEvent;
import com.swp.myleague.model.entities.match.MatchEventType;
import com.swp.myleague.model.entities.match.MatchPlayerStat;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.informationservice.PlayerService;
import com.swp.myleague.model.service.matchservice.MatchClubStatService;
import com.swp.myleague.model.service.matchservice.MatchEventService;
import com.swp.myleague.model.service.matchservice.MatchPlayerStatService;
import com.swp.myleague.model.service.matchservice.MatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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

    @Autowired
    UserService userService;

    @Autowired
    PlayerService playerService;

    @GetMapping("")
    public String getAllMatch(@RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "searchPlayerName", required = false) String searchPlayerName,
            @RequestParam(name = "searchClubName", required = false) String searchClubName,
            Model model) {
        List<Match> matcheList = matchService.getAll().stream()
                .filter(m -> m.getMatchStartTime().compareTo(LocalDateTime.now()) < 0).toList();
        if (search != null && !search.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchDescription().contains(search)).toList();
        }
        if (searchPlayerName != null && !searchPlayerName.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchPlayerStats().stream()
                    .anyMatch(mps -> mps.getPlayer().getPlayerFullName().contains(searchPlayerName))).toList();
        }
        if (searchClubName != null && !searchClubName.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchClubStats().stream()
                    .anyMatch(mcs -> mcs.getClub().getClubName().contains(searchClubName))).toList();
        }
        model.addAttribute("matches", matcheList);
        model.addAttribute("searchPlayerName", searchPlayerName);
        model.addAttribute("searchClubName", searchClubName);
        model.addAttribute("search", search);
        return "Match";
    }

    @GetMapping("/{matchId}")
    public String getMatchById(@PathVariable(name = "matchId") String matchId, Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Boolean isAdmin = user.getRole() == Role.ADMIN;

        List<MatchPlayerStat> playerStats = matchPlayerStatService.getAllByMatchId(matchId);
        List<MatchClubStat> clubStats = new ArrayList<>();
        try {
            clubStats = matchClubStatService.getAllByMatchId(matchId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Match match = matchService.getById(matchId);

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("match", match);
        model.addAttribute("matchPlayerStats", playerStats);
        model.addAttribute("clubStats", clubStats);
        model.addAttribute("startingLineupHome", matchService.getStartingLineup(matchId,
                match.getMatchClubStats().get(0).getClub().getClubId().toString()));
        model.addAttribute("startingLineupAway", matchService.getStartingLineup(matchId,
                match.getMatchClubStats().get(1).getClub().getClubId().toString()));
        model.addAttribute("substitueLineupHome", matchService.getSubstitueLineup(matchId,
                match.getMatchClubStats().get(0).getClub().getClubId().toString()));
        model.addAttribute("substitueLineupAway", matchService.getSubstitueLineup(matchId,
                match.getMatchClubStats().get(1).getClub().getClubId().toString()));

        MatchPlayerStat mpsMOM = matchPlayerStatService
                .getAllByMatchId(match.getMatchId().toString())
                .stream()
                .max(Comparator.comparing(MatchPlayerStat::getRating))
                .orElse(null);
        model.addAttribute("motm", mpsMOM);
        return "DetailMatch";
    }

    @GetMapping("/fixture")
    public String getFixture(@RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "searchPlayerName", required = false) String searchPlayerName,
            @RequestParam(name = "searchClubName", required = false) String searchClubName,
            Model model) {
        List<Match> matcheList = matchService.getAll().stream()
                .filter(m -> m.getMatchStartTime().compareTo(LocalDateTime.now()) > 0).toList();
        if (search != null && !search.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchDescription().contains(search)).toList();
        }
        if (searchPlayerName != null && !searchPlayerName.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchPlayerStats().stream()
                    .anyMatch(mps -> mps.getPlayer().getPlayerFullName().contains(searchPlayerName))).toList();
        }
        if (searchClubName != null && !searchClubName.isBlank()) {
            matcheList = matcheList.stream().filter(m -> m.getMatchClubStats().stream()
                    .anyMatch(mcs -> mcs.getClub().getClubName().contains(searchClubName))).toList();
        }
        model.addAttribute("matches", matcheList);
        model.addAttribute("searchPlayerName", searchPlayerName);
        model.addAttribute("searchClubName", searchClubName);
        model.addAttribute("search", search);
        return "Fixture";
    }

    @PostMapping("/add-motm")
    public String addManOfTheMatch(@RequestParam String matchId, @RequestParam String manOfTheMatch) {
        Match match = matchService.getById(matchId);
        match.setMatchMOM(UUID.fromString(manOfTheMatch));
        matchService.save(match);
        return "redirect:/match/" + matchId;
    }

    @PostMapping("/add-highlight")
    public String addHighlight(@RequestParam String matchId,
            @RequestParam int matchEventMinute,
            @RequestParam String matchEventTitle,
            @RequestParam String vidUrl,
            @RequestParam("hightlightThumnail") MultipartFile matchEventThumnails) {
        Match match = matchService.getById(matchId);
        MatchEvent event = new MatchEvent();
        event.setMatch(match);
        event.setMatchEventMinute(matchEventMinute);
        event.setMatchEventTitle(matchEventTitle);
        event.setVidUrl(vidUrl);
        event.setMatchEventType(MatchEventType.Highlight); // Enum

        if (!matchEventThumnails.isEmpty()) {
            File imageFile = new File(
                    "src/main/resources/static/images/Storage-Files" + File.separator
                            + matchEventThumnails.getOriginalFilename());
            try {
                Files.copy(matchEventThumnails.getInputStream(), imageFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                event.setMatchEventThumnails("/images/Storage-Files/" + matchEventThumnails.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace(); // có thể log ra logger
            }
        }

        matchEventService.save(event);
        return "redirect:/match/" + matchId;
    }

    @PostMapping("/import-lineup")
    public String importLineup(@RequestParam("file") MultipartFile file,
            @RequestParam("matchId") UUID matchId) {
        try (InputStream input = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(input);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // skip header

                String playerId = row.getCell(0).getStringCellValue();
                boolean isStarter = false;
                if (row.getCell(5).getStringCellValue().equals("Starter")) {
                    isStarter = true;
                }

                Player player = playerService.getById(playerId);
                Match match = matchService.getById(matchId.toString());
                if (!match.getMatchClubStats().stream()
                        .anyMatch(mcs -> mcs.getClub().getClubId().equals(player.getClub().getClubId()))) {
                    continue;
                }

                MatchPlayerStat stat = new MatchPlayerStat();
                stat.setMatch(match);
                stat.setPlayer(player);
                stat.setIsStarter(isStarter);
                stat.setMatchPlayerGoal(0);
                stat.setMatchPlayerAssist(0);
                stat.setMatchPlayerMinutedPlayed(0);
                stat.setMatchPlayerPass(0);
                stat.setMatchPlayerShoots(0);
                stat.setRating(0.00);
                matchPlayerStatService.save(stat);
            }
            workbook.close();
            return "redirect:/match/" + matchId + "?success";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/match/" + matchId + "?error";
        }
    }

}
