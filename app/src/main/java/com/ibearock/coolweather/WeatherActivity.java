package com.ibearock.coolweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ibearock.coolweather.gson.Forecast;
import com.ibearock.coolweather.gson.Weather;
import com.ibearock.coolweather.service.AutoUpdateService;
import com.ibearock.coolweather.util.HttpUtil;
import com.ibearock.coolweather.util.Utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity {

    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        loadRefreshLayout();
        loadWeatherInfo(false);
        loadBingPic();
        loaDrawLayout();
    }

    private void loaDrawLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void loadRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadWeatherInfo(true);
            }
        });
    }

    private void loadWeatherInfo(Boolean isReflash) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            if (isReflash) {
                requestWeather(weather.data.city);
            } else {
                showWeatherInfo(weather);
            }
        } else {
            String locationName = getIntent().getStringExtra("name");
            requestWeather(locationName);
        }
        Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
        startService(intent);
    }

    private void loadBingPic() {

        ImageView bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        String requestBingPic = "https://cn.bing.com//az/hprichbg/rb/ApfelTag_ZH-CN7906570680_1920x1080.jpg";
        Glide.with(WeatherActivity.this).load(requestBingPic).into(bingPicImg);
    }

    public void requestWeather(String locationName) {
        String weatherUrl = "http://wthrcdn.etouch.cn/weather_mini?city=" + locationName;
        HttpUtil.sendOKhttpRequest(weatherUrl, new Callback(){

            @Override
            public void onResponse(Call call, Response response) throws IOException{
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "OK".equals(weather.desc)){
                            SharedPreferences.Editor editer = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editer.putString("weather", responseText);
                            editer.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("myLife", "run: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {

        ScrollView weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        TextView titleCity = (TextView) findViewById(R.id.title_city);
        TextView titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        TextView degreeText = (TextView) findViewById(R.id.degree_text);
        TextView weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        LinearLayout forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        TextView aqiText = (TextView) findViewById(R.id.aqi_text);
        TextView pm25Text = (TextView) findViewById(R.id.pm25_text);
        TextView comfortText = (TextView) findViewById(R.id.confort_text);
        TextView carWashText = (TextView) findViewById(R.id.car_wash_text);
        TextView sportText = (TextView) findViewById(R.id.sport_text);

        String cityName = weather.data.city;
        Calendar calendar = Calendar.getInstance();
        String updateTime = calendar.get((Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日";
        String degree = weather.data.wendu + "°C";
        String weatherInfo = weather.data.ganmao;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        // 昨天
        TextView yesdataText = (TextView) findViewById(R.id.yesdata_text);
        TextView yesinfoTypeText = (TextView) findViewById(R.id.yesinfo_type);
        TextView yesinfoText = (TextView) findViewById(R.id.yesinfo_text);
        TextView yesmaxText = (TextView) findViewById(R.id.yesmax_text);
        TextView yesminText = (TextView) findViewById(R.id.yesmin_text);
        yesdataText.setText(weather.data.yesterday.date);
        yesinfoTypeText.setText(weather.data.yesterday.type);
        String yesfengLi = weather.data.yesterday.fl;
        yesfengLi = yesfengLi.replace("<![CDATA[", "");
        yesfengLi = yesfengLi.replace("]]>", "");
        yesinfoText.setText(weather.data.yesterday.fx + yesfengLi);
        yesmaxText.setText(weather.data.yesterday.high);
        yesminText.setText(weather.data.yesterday.low);

        // 未来
        for (Forecast forecast: weather.data.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.data_text);
            TextView infoTypeText = (TextView) view.findViewById(R.id.info_type);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoTypeText.setText(forecast.type);
            String fengLi = forecast.fengli;
            fengLi = fengLi.replace("<![CDATA[", "");
            fengLi = fengLi.replace("]]>", "");
            infoText.setText(forecast.fengxiang + fengLi);
            maxText.setText(forecast.high);
            minText.setText(forecast.low);
            forecastLayout.addView(view);
        }
        aqiText.setText("无数据");
        pm25Text.setText("无数据");

        String comfort = "舒适度：无数据";
        String carWash = "洗车指数：无数据";
        String sport = "运动建议：无数据";

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
    }
}
