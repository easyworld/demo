package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.util.StringUtils;

@Controller
public class LoginController {
    @Autowired
    private UserDetailsService jdbcUserDetailsManager;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(Model model, String username, String password) {
        if (StringUtils.isEmptyOrWhitespace(username) || StringUtils.isEmptyOrWhitespace(password)) {
            model.addAttribute("msg", "empty username or password");
        } else if (((JdbcUserDetailsManager) jdbcUserDetailsManager).userExists(username)) {
            model.addAttribute("msg", "User exist");
        } else {
            UserDetails user = User.withUsername(username).password(password).roles("user").passwordEncoder((f) -> new BCryptPasswordEncoder().encode(f)).build();
            ((JdbcUserDetailsManager) jdbcUserDetailsManager).createUser(user);
            model.addAttribute("msg", "Register success");
        }
        return "register";
    }

}
