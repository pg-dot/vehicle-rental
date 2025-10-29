package com.example.rental.model;

import java.time.LocalDate;

public class Rental {
    private int id;
    private int customerId;
    private int vehicleId;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // nullable
    private double baseCost;
    private double lateFee;
    private double totalCost;
    private String status; // OPEN | RETURNED | CANCELED

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public double getBaseCost() { return baseCost; }
    public void setBaseCost(double baseCost) { this.baseCost = baseCost; }
    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
