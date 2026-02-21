package com.d3rrick.kafkawithavro.app;

import com.d3rrick.kafkawithavro.User;
import com.d3rrick.kafkawithavro.app.producer.UserController;
import com.d3rrick.kafkawithavro.app.producer.UserKafkaProducer;
import com.d3rrick.kafkawithavro.app.util.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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
    void sendUser_shouldSendMessageToKafka() throws Exception {
        var requestObj = new UserController.UserRequest("123", "Derrick", "derrick@gmail.com", "555-0199");
        var request = Utils.newObjectMapper().writeValueAsString(requestObj);

        mockMvc.perform(post("/users/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string("Sent user: Derrick"));

        verify(producer, times(1)).sendUser(any(User.class));
    }
}