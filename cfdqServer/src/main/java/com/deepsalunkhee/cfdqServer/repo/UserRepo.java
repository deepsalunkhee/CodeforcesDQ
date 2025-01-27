package com.deepsalunkhee.cfdqServer.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.deepsalunkhee.cfdqServer.models.UserModel;


@Repository("mongoUserRepo")
public interface UserRepo extends MongoRepository<UserModel, String> {

    Optional<UserModel> findByHandle(String handle);

}
