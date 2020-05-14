package com.example.account.Controller;

import com.example.account.Dao.UserDaoRepository;
import com.example.account.Model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private UserDaoRepository userDaoRepository;

    @Autowired
    private ObjectMapper objectMapper;




    @Test
    void adduser() throws Exception {
        User user = new User();
        user.setEmail("manu@gmail.com");
        user.setPassword("manu123");
        user.setName("manu");
        user.setUserPermission("admin");
        System.out.println(user);
        Mockito.when(userDaoRepository.saveAndFlush(user)).
                thenReturn(user);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(user);

        System.out.println(requestJson);

        mockMvc.perform(post("/User/UserRegister")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print());
    }

    @Test
    void addadmin() throws Exception {
        User user = new User();
        user.setEmail("manu@gmail.com");
        user.setPassword("manu123");
        user.setName("manu");
        user.setUserPermission("admin");
        Mockito.when(userDaoRepository.saveAndFlush(user)).thenReturn(user);

        mockMvc.perform(post("/User/UserLogin").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());


    }

    @Test
    void verifyuser() throws Exception {
        User user = new User();
        Mockito.when(userDaoRepository.findByEmailAndPasswordAndUserPermission("manu@gmail.com",
                "manu@123", "customer"))
                .thenReturn(user);


        mockMvc.perform(post("/User/UserLogin").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());


    }

    @Test
    void verifyadmin() throws Exception {
        User user = new User();
        Mockito.when(userDaoRepository.findByEmailAndPasswordAndUserPermission("manu@gmail.com",
                "manu@123", "admin"))
                .thenReturn(user);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(user);

        System.out.println(requestJson);

        mockMvc.perform(post("/User/AdminLogin")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print());
    }


    @Test
    void find_all() throws Exception {
        User user = new User();
        user.setUserid(1);
        user.setEmail("manu@gmail.com");
        user.setPassword("manu123");
        user.setName("manu");
        user.setUserPermission("admin");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authentication","Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJVU0VSX1NIT1BQSU5HIiwiU0VTU0lPTl9QQVNTIjoibWFudUAxMjMiLCJTRVNTSU9OX0VNQUlMIjoibWFudTExQGdtYWlsLmNvbSIsIlNFU1NJT05fVFlQRSI6ImFkbWluIiwiZXhwIjoxNTg5NDU1OTQxLCJpYXQiOjE1ODkzNjk1NDF9.Z_1BPh3XyTateJu88qzyuib10bISFl9ViD_iElTDGjlg5sG7g_30-3wnuPUu_F4KP65T-OqwjBmcL0S43ZX8XA");

        Mockito.when( userDaoRepository.findAll())
                .thenReturn(List.of(user));
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(user);

        System.out.println(requestJson);

        mockMvc.perform(post("/User/AdminLogin")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print());
    }

    @Test
    void find_By_id() throws Exception {
        User user = new User();
        user.setUserid(1);
        user.setEmail("manu@gmail.com");
        user.setPassword("manu123");
        user.setName("manu");
        user.setUserPermission("admin");
        Mockito.when(userDaoRepository.findById(Mockito.anyInt()))
                .thenReturn(Optional.of(user));
        mockMvc.perform(get("/User/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userid", is(1)));


    }




}