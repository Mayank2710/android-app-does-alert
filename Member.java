package com.example.medicinereminder;

public class Member {
    private String id;
    private String name;
    private String relation;

    // Required empty constructor for Realtime DB
    public Member() { }

    public Member(String name, String relation) {
        this.name = name;
        this.relation = relation;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getRelation() { return relation; }
}