package com.example.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Shams on 24-01-2017.
 */
public class IndicationOfInterest {
    private int id;
    private int quantity;
    private String ticker;
    private double price;

    public IndicationOfInterest(int quantity, String ticker, double price) {
        this.quantity = quantity;
        this.ticker = ticker;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "IndicationOfInterest{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", ticker='" + ticker + '\'' +
                ", price=" + price +
                '}';
    }
}
