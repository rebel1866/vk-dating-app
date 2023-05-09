package com.melnikov.dao.model.constant;

public enum Smoking {
    N0(""),
    N1("Резко негативное"),
    N2("Негативное"),
    N3("Компромиссное"),
    N4("Нейтральное"),
    N5("Положительное");
    private final String label;

    public String getLabel() {
        return label;
    }

    Smoking(String label) {
        this.label = label;
    }
}
