package com.mycompany.smartcampus_w2153601.model;

import java.util.ArrayList;
import java.util.List;

public class Sensor {

    private Long id;
    private Long roomId;
    private String name;
    private String type;
    private String unit;
    private Double currentValue;
    private List<SensorReading> readings = new ArrayList<SensorReading>();

    public Sensor() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public List<SensorReading> getReadings() {
        return readings;
    }

    public void setReadings(List<SensorReading> readings) {
        if (readings == null) {
            this.readings = new ArrayList<SensorReading>();
            return;
        }
        this.readings = readings;
    }
}