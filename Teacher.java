package com.example.myfirstapp;

public class Teacher
{
    private String name;
    private String Id;
    private String area;
    private String phone;
    private String price;
    private String car;
    private String carType;
    private String seniority;
    private String icon;

    public Teacher(String Id,String name, String area, String phone, String price,String seniority, String car, String carType)
    {
        this.Id = Id;
        this.name = name;
        this.area = area;
        this.phone = phone;
        this.price = price;
        this.seniority = seniority;
        this.car = car;
        this.carType = carType;
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

    public String getArea()
    {
        return this.area;
    }

    public String getPhone()
    {
        return this.phone;
    }

    public String getPrice()
    {
        return this.price;
    }

    public String getSeniority(){ return this.seniority; }

    public String getCar()
    {
        return this.car;
    }

    public String getCarType()
    {
        return this.carType;
    }

    public String getIcon()
    {
        return this.icon;
    }
}
