package com.itcrazy.mybatis.generator.model;

/**
 * @author: itcrazy0717
 * @version: $ DatabaseDTO.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class DatabaseDTO {

    private String name;
    private int value;
    private String driverClass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    @Override
    public String toString() {
        return name;
    }

}
