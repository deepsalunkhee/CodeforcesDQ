package com.deepsalunkhee.cfdqServer.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepsalunkhee.cfdqServer.Services.UserServices;
import com.deepsalunkhee.cfdqServer.models.UserModel;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/v1")
public class SecureControllers {

    @Autowired
    private  UserServices userServices;

    @PostMapping("/createweek")
    public ResponseEntity<String> createWeek(HttpServletRequest request) {
        Map<String,String> headers = new HashMap<>();
        headers.put("handle", request.getHeader("handle"));

        //check if user exists
        UserModel currUser= userServices.getUserByHandle(headers.get("handle"));
        if(currUser==null){
           UserModel newUser = new UserModel();
            newUser.setHandle(headers.get("handle"));
            userServices.createUser(newUser);
        }

        return ResponseEntity.ok("Checking");
        

    }

}
