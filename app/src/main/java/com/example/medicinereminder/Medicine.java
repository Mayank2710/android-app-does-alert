package com.example.medicinereminder;

public class Medicine {
    private String id, name, dosage, status, startDate, endDate;

    public Medicine() {} // Required for Firebase

    public Medicine(String name, String dosage, String status, String startDate, String endDate) {
        this.name = name;
        this.dosage = dosage;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getStatus() { return status; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}