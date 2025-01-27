package com.deepsalunkhee.cfdqServer.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deepsalunkhee.cfdqServer.models.UserModel;
import com.deepsalunkhee.cfdqServer.repo.UserRepo;

@Service
public class UserServices {

    private final UserRepo userRepo;

    @Autowired
    public UserServices(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public UserModel createUser(UserModel user){
        return userRepo.save(user);
    }

    public UserModel getUserByHandle(String handle){
        return userRepo.findByHandle(handle).orElse(null);
    }

}
