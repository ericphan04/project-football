package com.swp.myleague.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping(value = { "/home", "/home/" })
public class HomeController {

    @GetMapping("")
    public String returnHome() {

        return "Home";
    }

}
