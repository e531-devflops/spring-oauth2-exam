package kr.daoko.exam.controller;

import kr.daoko.exam.annotation.SocialUser;
import kr.daoko.exam.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/")
    public String getAuthorizationMessage() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/success")
    public String success(@SocialUser User user) {
        return "success";
    }

    @GetMapping("/failure")
    public String failure() {
        return "failure";
    }
}
