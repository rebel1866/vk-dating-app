package com.melnikov.dao.model.constant;

public enum Relation {
    N0("",0),
    N1("Не замужем",1),
    N2("Есть друг",2),
    N3("Помолвлена",3),
    N4("Замужем",4),
    N5("Все сложно",5),
    N6("В активном поиске",6),
    N7("Влюблена",7),
    N8("В гражданском браке",8);

    private final String label;
    private final int number;

    public String getLabel() {
        return label;
    }

    public int getNumber() {
        return number;
    }

    Relation(String label, int number) {
        this.label = label;
        this.number = number;
    }
}
