package com.melnikov.dao.model.constant;

public enum LifeMain {
    N0(""),
    N1("Семья и дети"),
    N2("Карьера и деньги"),
    N3("Развлечения и отдых"),
    N4("Наука и исследования"),
    N5("Совершенствование мира"),
    N6("Саморазвитие"),
    N7("Красота и искусство"),
    N8("Слава и влияние");

    private final String label;

    public String getLabel() {
        return label;
    }

    LifeMain(String label) {
        this.label = label;
    }
}
