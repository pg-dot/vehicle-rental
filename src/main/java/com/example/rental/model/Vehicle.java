package com.example.rental.model;

public class Vehicle {
    private int id;
    private String plateNo;
    private String brand;
    private String model;
    private double dailyRate;
    private String status; // AVAILABLE, RENTED, MAINTENANCE

    public Vehicle() {}

    public Vehicle(int id, String plateNo, String brand, String model, double dailyRate, String status) {
        this.id = id;
        this.plateNo = plateNo;
        this.brand = brand;
        this.model = model;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override public String toString() {
        return plateNo + " (" + brand + " " + model + ") - Rs." + dailyRate + "/day [" + status + "]";
    }
}

