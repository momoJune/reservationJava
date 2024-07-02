package com.auto.autoreservation.infrastructure;

import lombok.Data;

import java.util.List;

@Data
public class GangdongVO {
    private String username;
    private String password;
    private String pickedStadium;
    private int numPeople;
    private int year;
    private int month;
    private String time;
    private List<String> day;

    // Getters and Setters
}