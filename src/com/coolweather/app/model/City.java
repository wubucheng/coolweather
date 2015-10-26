package com.coolweather.app.model;

public class City {
	private int id;
	private String cityName;
	private String cityCode;
	private int provinceId;
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id=id;
	}
	public String  getcityName()
	{
		return cityName;
	}
	public void setcityName(String cityName)
	{
		this.cityName=cityName;
	}
	public String getcityCode()
	{
		return cityName;
	}
	public void setcityCode(String citycode)
	{
		this.cityCode=citycode;
	}
	public int  getProvinceId()
	{
		return provinceId;
	}
	public void setProvinceId()
	{
		this.provinceId=provinceId;
	}
}
