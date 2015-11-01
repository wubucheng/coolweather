package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.R.id;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
//import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.AdapterView.OnItemClickListener;
public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String>adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String>dataList=new ArrayList<String>();
	private List<Province>provinceList;
	private List<City>cityList;
	private List<County>countyList;
	
	private Province selectedProvince;
	private City selectedCity;
	private int currentLevel;
	
	private boolean isFromWeatherActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity)
		{
		Intent intent =new Intent(this,WeatherActivity.class);
		startActivity(intent);
		finish();
		return;
	   }
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		//listView.setOnItemClickListener(this);
		
			/*@Override
			public void onItemClick(AdapterView<?>arg0,View view,int index,long arg3)
			{
				if(currentLevel==LEVEL_PROVINCE)
				{
					selectedProvince=provinceList.get(index);
					queryCities();
				}
				else if(currentLevel==LEVEL_CITY)
				{
					selectedCity=cityList.get(index);
					queryCounties();
				}
			}
			*/
		listView.setOnItemClickListener(new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?>arg0,View view,int index,long arg3)
		{
			if(currentLevel==LEVEL_PROVINCE)
			{
				selectedProvince=provinceList.get(index);
				queryCities();
			}
			else if(currentLevel==LEVEL_CITY)
			{
				selectedCity=cityList.get(index);
				queryCounties();
			}
			else if(currentLevel==LEVEL_COUNTY)
			{
				String countyCode=countyList.get(index).getcountyCode();
				Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
				intent.putExtra("county_code", countyCode);
				startActivity(intent);
				finish();
			}
		}
		
		});
		
		queryProvinces();
	}
	private void queryProvinces()
	{
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0)
		{
			dataList.clear();
			for(Province province : provinceList)
			{
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
			
		}
		else {
			queryFromServer(null,"province");
		}
	}
	
	private void queryCities()
	{
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0)
		{
			dataList.clear();
			for(City city:cityList)
			{
				dataList.add(city.getcityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}
		else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	private void queryCounties()
	{
		countyList=coolWeatherDB.loadCouncities(selectedCity.getId());
		if(countyList.size()>0)
		{
			dataList.clear();
			for(County county:countyList)
			{
				dataList.add(county.getcountyName());
				
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getcityName());
			currentLevel=LEVEL_COUNTY;
		}
		else {
			queryFromServer(selectedCity.getcityCode(),"county");
		}
	}
	private void queryFromServer(final String code,final String type)
	{
		String address;
		if(!TextUtils.isEmpty(code))
		{
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
			//address="http://m.weather.com.cn/atad/101010100"+code+".html";
		}
		else {
			address="http://www.weather.com.cn/data/list3/city.xml";
			address="http://m.weather.com.cn/atad/101010100.html";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result=false;
				if("province".equals(type))
				{
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
				}
				else if("city".equals(type))
				{
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}
				else if("county".equals(type))
				{
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result)
				{
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type))
							{
								queryProvinces();
							}
							else if("city".equals(type))
							{
								queryCities();
							}
							else if("county".equals(type))
							{
								queryCounties();
							}
						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
						
					}
				});
				
			}
		});
	}
	private void showProgressDialog()
	{
		if(progressDialog==null)
		{
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	private void closeProgressDialog()
	{
		if(progressDialog!=null)
			progressDialog.dismiss();
	}
	@Override
	public void onBackPressed()
	{
		if(currentLevel==LEVEL_COUNTY)
		{
			queryCities();
		}
		else if(currentLevel==LEVEL_CITY)
		{
			queryProvinces();
		}
		else if (isFromWeatherActivity) {
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
		}
		else {
			finish();
		}
	}
}
