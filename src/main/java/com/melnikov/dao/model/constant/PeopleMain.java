package com.melnikov.dao.model.constant;

public enum PeopleMain {
    N1("Ум и креативность"),
    N2("Доброта и честность"),
    N3("Красота и здоровье"),
    N4("Власть и богатство"),
    N5("Смелость и упорство"),
    N6("Юмор и жизнелюбие");

    private final String label;

    public String getLabel() {
        return label;
    }

    PeopleMain(String label) {
        this.label = label;
    }
}
