package com.auto.autoreservation.application;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class GangdongController {
    @GetMapping("/gangdongMain")
    public String gangdongMain(Model model) {
        return "gangdong";
    }
}
