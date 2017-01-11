package com.c2line.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/1/11.
 */

public class Area extends DataSupport {
    private int id;
    private String cityId;
    private String cityNamePy;
    private String city;
    private String country;
    private String province;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityNamePy() {
        return cityNamePy;
    }

    public void setCityNamePy(String cityNamePy) {
        this.cityNamePy = cityNamePy;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
