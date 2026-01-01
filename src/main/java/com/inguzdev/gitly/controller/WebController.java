package com.inguzdev.gitly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @RequestMapping("/**")
    public String fallback(Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "This short link doesn't exist or may have expired");
        return "error";
    }
}
