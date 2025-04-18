package com.example.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@CrossOrigin
public class MivdhiServer {

    public static void main(String[] args) {
        SpringApplication.run(MivdhiServer.class, args);
    }

}
