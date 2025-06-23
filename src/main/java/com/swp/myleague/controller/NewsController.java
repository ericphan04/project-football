package com.swp.myleague.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.service.blogservice.BlogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping(value = {"/news", "/news/"})
public class NewsController {
    
    @Autowired BlogService blogService;

    @GetMapping("")
    public String getNews(Model model) {
        model.addAttribute("blogs", blogService.getAll());
        return "News";
    }
    
    @GetMapping("/{blogId}")
    public String getDetailBlog(@PathVariable(name = "blogId") String blogId, Model model) {
        Blog blog = blogService.getById(blogId);
        model.addAttribute("blog", blog);
        model.addAttribute("relatedNews", blogService.getAll().stream().filter(blg -> blg.getBlogCategory().equals(blog.getBlogCategory()) || blg.getBlogTitle().compareTo(blog.getBlogTitle()) < 100).toList());
        return "DetailNews";
    }
    

}
