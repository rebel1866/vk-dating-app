package com.melnikov.dao.model.constant;

public enum Political {
    N0(""),
    N1("Коммунистические"),
    N2("Социалистические"),
    N3("Умеренные"),
    N4("Либеральные"),
    N5("Консервативные"),
    N6("Монархические"),
    N7("Ультраконсервативные"),
    N8("Индифферентные"),
    N9("Либертарианские");

    private final String label;

    public String getLabel() {
        return label;
    }

    Political(String label) {
        this.label = label;
    }
}
