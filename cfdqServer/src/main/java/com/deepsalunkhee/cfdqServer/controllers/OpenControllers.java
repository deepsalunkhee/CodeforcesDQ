package com.deepsalunkhee.cfdqServer.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class OpenControllers {

    @GetMapping("")
    public String home() {
        return "cfdq server is running";
    }
}
