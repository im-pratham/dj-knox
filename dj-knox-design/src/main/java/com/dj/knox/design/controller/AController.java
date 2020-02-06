package com.dj.knox.design.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AController {

    private final UserDetailsService userDetailsService;

    @GetMapping("/users/{username}")
    public UserDetails getUser(@PathVariable("username") String userName) {
        return this.userDetailsService.loadUserByUsername(userName);
    }
    
}