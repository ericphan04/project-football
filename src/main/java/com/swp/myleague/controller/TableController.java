package com.swp.myleague.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.matchservice.MatchClubStatService;

@Controller
@RequestMapping(value = {"/table", "/table/"})
public class TableController {
    
    @Autowired
    ClubService clubService;

    @Autowired
    MatchClubStatService matchClubStatService;

}
