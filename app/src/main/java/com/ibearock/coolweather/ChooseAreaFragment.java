package com.ibearock.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ibearock.coolweather.db.City;
import com.ibearock.coolweather.db.County;
import com.ibearock.coolweather.db.Province;

import org.litepal.LitePal;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    public static final int LEVEL_COUNTY = 3;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        queryFromExcel();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 给List每个点击事件进行监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCountes();
                }

            }
        });
        // 回退按钮进行监听
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                } else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        // 查询省级
        queryProvinces();
    }

    // 查询省级
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province: provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
    }

    // 查询县级
    private void queryCountes() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("parentcode=?", String.valueOf(selectedCity.getCityCode())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County county: countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
    }

    // 查询市级
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("parentcode=?", String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
    }

    private void queryFromExcel(){
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0){return;}

        try {
            InputStream is = getContext().getAssets().open("location_name.xls");
            Workbook book = Workbook.getWorkbook(is);
            // 如果excel不仅仅一个则退出
            if (book.getSheets().length != 1) {return;}

            Sheet sheet = book.getSheet(0);
            int Rows = sheet.getRows();

            for (int i = 1; i < Rows; ++i) {
                String sCode = sheet.getCell(0, i).getContents();
                if (sCode.isEmpty()) {continue;}

                String sName = sheet.getCell(1, i).getContents();
                String sParentId = sheet.getCell(2, i).getContents();
                String sType = sheet.getCell(4, i).getContents();

                if (Integer.valueOf(sType) == LEVEL_PROVINCE){
                    Province province = new Province();
                    province.setProvinceCode(sCode);
                    province.setProvinceName(sName);
                    province.save();
                } else if (Integer.valueOf(sType) == LEVEL_CITY){
                    City city = new City();
                    city.setCityCode(sCode);
                    city.setCityName(sName);
                    city.setParentCode(sParentId);
                    city.save();
                } else if (Integer.valueOf(sType) == LEVEL_COUNTY){
                    County county = new County();
                    county.setCountyCode(sCode);
                    county.setCountyName(sName);
                    county.setParentCode(sParentId);
                    county.save();
                }
            }
            book.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
