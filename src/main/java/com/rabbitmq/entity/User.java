package com.rabbitmq.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {

    int id;
    String name;
    String date = new Date().toString();

}
