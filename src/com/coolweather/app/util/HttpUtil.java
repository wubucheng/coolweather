package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import com.coolweather.app.model.Province;

import android.net.Uri;
import android.util.Log;

public class HttpUtil {
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpsURLConnection connection=null;
				try {
					URL url=new URL(address);
					connection=(HttpsURLConnection)url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(80000);
					connection.setReadTimeout(80000);
					InputStream in=connection.getInputStream();
					BufferedReader reader=new BufferedReader(new InputStreamReader(in));
					StringBuilder response=new StringBuilder();
					String line;
					while((line=reader.readLine())!=null)
					{
						response.append(line);
					}
					//parserJSONWithJSONObject(response);
					if(listener!=null)
					{
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					listener.onError(e);
				}
				finally
				{
					if(connection!=null)
					{
						 
						connection.disconnect();
					}
				}
			}
		}).start();
	}
	/*private void parserJSONWithJSONObject(String jsonData)
	{
		try {
			JSONArray jsonArray=new JSONArray(jsonData);
			for(int i=0;i<jsonArray.length();i++)
			{
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				String id=jsonObject.getString("id");
				String provinceName=jsonObject.getString("provinceName");
				String provinceCode=jsonObject.getString("provinceCode")
			}
			Log.d("HttpUtil","id is "+id);
			Log.d("HttpUtil", "provinceName"+provinceName);
			Log.d("HttpUtil", "provinceCode"+provinceCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}
