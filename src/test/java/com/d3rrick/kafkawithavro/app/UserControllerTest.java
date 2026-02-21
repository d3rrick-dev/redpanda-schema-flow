package com.d3rrick.kafkawithavro.app;

import com.d3rrick.kafkawithavro.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserKafkaProducer producer;

    @Test
    void shouldReturnSuccessMessageOnPost() throws Exception {
        mockMvc.perform(post("/users/1/Derrick/9000"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sent user: Derrick"));

        verify(producer, times(1)).sendUser(any(User.class));
    }

    @Test
    void shouldReturnSuccessMessageOnPostWithAddedPhoneField() throws Exception {
        mockMvc.perform(post("/users/1/Derrick/9000"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sent user: Derrick"));

        verify(producer, times(1)).sendUser(any(User.class));
    }
}