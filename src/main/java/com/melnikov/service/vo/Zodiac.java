package com.melnikov.service.vo;

public enum Zodiac {
    OVEN("20.03","19.04"),
    LEV("23.07","22.08"),
    STRELEC("23.11","20.12"),
    TELEC("20.04","20.05"),
    DEVA("23.08","22.09"),
    KOZEROG("21.12","19.01"),
    RAK("21.06","22.07"),
    SCORPION("23.10","22.11"),
    RYBY("18.02","20.03"),
    BLIZNECY("21.05","20.06"),
    VECY("23.09","22.10"),
    VODOLEY("20.01","17.02");


    private String startDate;
    private String endDate;

    Zodiac(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
