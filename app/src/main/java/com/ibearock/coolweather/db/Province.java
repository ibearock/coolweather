package com.ibearock.coolweather.db;

import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {

    private int id;
    private String provinceName;
    private inte provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public inte getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(inte provinceCode) {
        this.provinceCode = provinceCode;
    }
}
