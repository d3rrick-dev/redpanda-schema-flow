package com.d3rrick.kafkawithavro.app.producer;

import com.d3rrick.kafkawithavro.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserKafkaProducer userKafkaProducer;

    @PostMapping("/{id}/{name}/{phoneNumber}")
    public String sendUser(@PathVariable String id, @PathVariable String name, @PathVariable String phoneNumber) {
        var user = User.newBuilder()
                .setId(id)
                .setName(name)
                .setEmail(name.toLowerCase() + "@test.com")
                .setPhoneNumber(phoneNumber)
                .build();
        userKafkaProducer.sendUser(user);
        return "Sent user: " + name;
    }
}