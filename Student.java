package com.example.myfirstapp;

public class Student
{
    private String name;
    private String Id;
    private String icon;
    private String address;
    private String phone;
    private String debt;

    public Student(String Id,String name, String address, String phone, String debt)
    {
        this.Id = Id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.debt = debt;
        this.icon = "icon_person_color";
    }

    public String getName()
    {
        return this.name;
    }

    public String getId()
    {
        return this.Id;
    }

    public String getAddress()
    {
        return this.address;
    }

    public String getPhone()
    {
        return this.phone;
    }

    public String getDebt()
    {
        return this.debt;
    }

    public String getIcon()
    {
        return this.icon;
    }
}
