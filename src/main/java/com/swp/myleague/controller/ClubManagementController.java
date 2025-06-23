package com.swp.myleague.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.information.PlayerPosition;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.blogservice.BlogService;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.informationservice.PlayerService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping(value = { "/clubmanager", "/clubmanage/" })
public class ClubManagementController {

    @Autowired
    ClubService clubService;

    @Autowired
    PlayerService playerService;

    @Autowired
    BlogService blogService;

    @Autowired
    UserService userService;

    @GetMapping("")
    public String getClub(Model model, Principal principal) {
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());
        model.addAttribute("club", club);
        model.addAttribute("positions", PlayerPosition.values());
        return "ClubManagementPage";
    }

    @PostMapping("/updateinfo")
    public String updateClubInfo(@RequestParam(name = "clubName") String clubName,
            @RequestParam(name = "clubStadium") String clubStadium,
            @RequestParam(name = "clubDescription") String clubDescription,
            @RequestParam(name = "clubPrimaryColor") String clubPrimaryColor,
            @RequestParam(name = "clubSecondaryColor") String clubSecondaryColor,
            @RequestParam(name = "clubId") String clubId,
            Model model) {
        Club club = clubService.getById(clubId);
        club.setClubName(clubName);
        club.setClubStadium(clubStadium);
        club.setClubDescription(clubDescription);
        club.setClubPrimaryColor(clubPrimaryColor);
        club.setClubSecondaryColor(clubSecondaryColor);
        club = clubService.save(club);
        return "redirect:/clubmanager";
    }

    @PostMapping("/addplayer")
    public String addPlayer(@RequestParam(name = "playerFullName") String playerName,
            @RequestParam(name = "position") String playerPosition,
            @RequestParam(name = "playerNationaly") String playerNationaly,
            @RequestParam(name = "playerNumber") String playerNumber,
            Model model, Principal principal) {
        Player player = new Player();
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());
        player.setClub(club);
        player.setPlayerFullName(playerName);
        player.setPlayerPosition(Arrays.asList(PlayerPosition.values()).stream()
                .filter(pp -> pp.name().equals(playerPosition)).findFirst().orElseThrow());
        player.setPlayerNationaly(playerNationaly);
        player.setPlayerNumber(Integer.parseInt(playerNumber));

        player = playerService.save(player);
        return "redirect:/clubmanager";
    }

    @PostMapping("/editplayer")
    public String editPlayer(@RequestParam(name = "playerFullName") String playerName,
            @RequestParam(name = "position") String playerPosition,
            @RequestParam(name = "playerNationaly") String playerNationaly,
            @RequestParam(name = "playerNumber") String playerNumber,
            @RequestParam(name = "playerId") String playerId,
            Model model, Principal principal) {
        Player player = playerService.getById(playerId);
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());
        player.setClub(club);
        player.setPlayerFullName(playerName);
        player.setPlayerPosition(Arrays.asList(PlayerPosition.values()).stream()
                .filter(pp -> pp.name().equals(playerPosition)).findFirst().orElseThrow());
        player.setPlayerNationaly(playerNationaly);
        player.setPlayerNumber(Integer.parseInt(playerNumber));

        player = playerService.save(player);
        return "redirect:/clubmanager";
    }

    @PostMapping("/addblog")
    public String addBlog(@RequestParam(name = "title") String blogTitle,
            @RequestParam(name = "content") String blogContent,
            Model model, Principal principal) {
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());

        Blog blog = new Blog();
        blog.setBlogTitle(blogTitle);
        blog.setBlogContent(blogContent);
        blog.setClub(club);
        blog.setBlogDateCreated(LocalDateTime.now());

        blog = blogService.save(blog);

        return "redirect:/clubmanager";
    }

    @PostMapping("/editblog")
    public String editBlog(@RequestParam(name = "title") String blogTitle,
            @RequestParam(name = "content") String blogContent,
            @RequestParam(name = "blogId") String blogId,
            Model model, Principal principal) {
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username);
        Club club = clubService.getByUserId(user.getUserId());

        Blog blog = blogService.getById(blogId);
        blog.setBlogTitle(blogTitle);
        blog.setBlogContent(blogContent);
        blog.setClub(club);
        blog.setBlogDateCreated(LocalDateTime.now());

        blog = blogService.save(blog);

        return "redirect:/clubmanager";
    }

}
