package com.d3rrick.kafkawithavro.app.producer;

import com.d3rrick.kafkawithavro.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserKafkaProducer userKafkaProducer;

    @PostMapping("/send")
    public String sendUser(@RequestBody UserRequest request) {
        var user = User.newBuilder()
                .setId(request.id())
                .setName(request.name())
                .setEmail(request.email())
                .setPhoneNumber(request.phoneNumber())
                .build();

        userKafkaProducer.sendUser(user);
        return "Sent user: " + request.name();
    }

    public record UserRequest(String id, String name, String email, String phoneNumber){}
}