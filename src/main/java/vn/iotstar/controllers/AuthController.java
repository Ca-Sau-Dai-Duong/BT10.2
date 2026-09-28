package vn.iotstar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Controller trả về trang HTML (Thymeleaf) cho phần "Render in Ajax" */
@Controller
@RequestMapping("/")
public class AuthController {

    @GetMapping
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("login")
    public String index() {
        return "login";
    }

    @GetMapping("user/profile")
    public String profile() {
        return "profile";
    }
}
