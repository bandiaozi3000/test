package com.hotel.test;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class People {
    private Integer id;

    private String name;

    @JsonSerialize(using = CustomerDoubleSerialize.class)
    private Double price;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "People{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
