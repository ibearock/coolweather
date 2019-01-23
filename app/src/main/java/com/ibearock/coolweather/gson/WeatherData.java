package com.ibearock.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherData {

    public Yesterday yesterday;
    public String city;

    @SerializedName("forecast")
    public List<Forecast> forecastList;

    public String ganmao;
    public String wendu;

}
