package com.swp.myleague.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swp.myleague.model.service.informationservice.ClubService;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping(value = { "/home", "/home/" })
public class HomeController {

    @Autowired ClubService clubService;

    @GetMapping("")
    public String returnHome(Model model) {
        model.addAttribute("leagueTable",
                clubService.getAll().stream().filter(c -> c.getClubLogoPath().contains("/images/logoclub")).toList());
        return "Home";
    }

}
