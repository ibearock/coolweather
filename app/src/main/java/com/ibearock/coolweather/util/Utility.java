package com.ibearock.coolweather.util;

import android.text.TextUtils;

import com.ibearock.coolweather.db.City;
import com.ibearock.coolweather.db.County;
import com.ibearock.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    // 返回省级数据
    public static boolean handleProvinceResponse(String response){
        if (TextUtils.isEmpty(response)){ return false;}

        try{
            JSONArray allprovinces = new JSONArray(response);
            for (int i = 0; i < allprovinces.length(); i++){
                JSONObject provinceObject = allprovinces.getJSONObject(i);
                Province province = new Province();
                province.setProvinceName(provinceObject.getString("name"));
                province.setId(provinceObject.getInt("id"));
                province.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }

    // 返回市级数据
    public static boolean handleCityResponse(String response){
        if (TextUtils.isEmpty(response)){ return false;}

        try{
            JSONArray allcitys = new JSONArray(response);
            for (int i = 0; i < allcitys.length(); i++){
                JSONObject cityObject = allcitys.getJSONObject(i);
                City city = new City();
                city.setCityName(cityObject.getString("name"));
                city.setId(cityObject.getInt("id"));
                city.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }
    // 返回县级数据
    public static boolean handleCountResponse(String response){
        if (TextUtils.isEmpty(response)){ return false;}

        try{
            JSONArray allCounties = new JSONArray(response);
            for (int i = 0; i < allCounties.length(); i++){
                JSONObject countObject = allCounties.getJSONObject(i);
                County county = new County();
                county.setCountyName(countObject.getString("name"));
                county.setId(countObject.getInt("id"));
                county.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }
}
