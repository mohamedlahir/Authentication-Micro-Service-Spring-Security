//package com.authentication.service.auth.controllers;
//
//import com.authentication.service.auth.models.Users;
//import com.authentication.service.auth.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/users")
//public class UserControllers {
//
//    @Autowired
//    UserService service;
//
//    @GetMapping("/getuser")
//    public List<Users> getUser(Users user)
//    {
//        return service.getUser(user);
//    }
//}
