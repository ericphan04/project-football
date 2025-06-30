package com.swp.myleague.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.model.service.saleproductservice.ProductService;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping(value = {"/admin", "/admin/"})
public class AdminController {
    
    @Autowired ProductService productService;

    @Autowired UserService userService;

    @Autowired MatchService matchService;

    @GetMapping("")
    public String getAdminDashboard(Model model) {
        model.addAttribute("users", userService.getUser());
        model.addAttribute("products", productService.getAll());
        model.addAttribute("matches", matchService.getAll());
        return "AdminDashboard";
    }
    

}
