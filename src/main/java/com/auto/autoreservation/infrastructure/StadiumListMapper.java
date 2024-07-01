package com.auto.autoreservation.infrastructure;

import java.util.HashMap;
import java.util.Map;

public class StadiumListMapper {
    private static final Map<String, String> stadiumMap = new HashMap<>();

    static {
        stadiumMap.put("제1테니스장 1번코트", "HNRS-014");
        stadiumMap.put("제1테니스장 2번코트", "HNRS-015");
        stadiumMap.put("제1테니스장 3번코트", "HNRS-016");
        stadiumMap.put("제1테니스장 4번코트", "HNRS-017");
        stadiumMap.put("제1테니스장 (레슨) 5번코트", "HNRS-018");
        stadiumMap.put("제1테니스장 (레슨) 6번코트", "HNRS-019");
        stadiumMap.put("제2테니스장 1번코트", "HNRS-020");
        stadiumMap.put("제2테니스장 2번코트", "HNRS-021");
        stadiumMap.put("제2테니스장 3번코트", "HNRS-022");
        stadiumMap.put("제2테니스장 4번코트", "HNRS-023");
        stadiumMap.put("제2테니스장 5번코트", "HNRS-024");
    }

    public static Map<String,String> getStadiumMap() {
        return stadiumMap;
    }
}
