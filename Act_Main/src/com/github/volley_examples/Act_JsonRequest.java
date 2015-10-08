/**
 * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.volley_examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.volley_examples.R;
import com.github.volley_examples.app.MyVolley;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Demonstrates how to execute <code>JsonObjectRequest</code>
 * @author Ognyan Bankov
 *
 */
public class Act_JsonRequest extends Activity {
	private static final String TAG = "com.lms.volleydemo.JsonActivity";

	private RequestQueue mQueue;
	
	private static final String WEATHER_LINK = "http://www.weather.com.cn/data/sk/101280101.html";
	
	private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(); 
	
	private ListView lvWeather;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.act__json_request);
		
		lvWeather = (ListView)findViewById(R.id.lvWeather);
		
		mQueue = Volley.newRequestQueue(this);
		//getWeatherInfo();
		//{"weatherinfo":{"city":"广州","cityid":"101280101","temp":"12","WD":"东风","WS":"2级","SD":"95%",
		//"WSE":"2","time":"21:05","isRadar":"1","Radar":"JC_RADAR_AZ9200_JB"}}
		
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// super.handleMessage(msg);
				init();
			}
		};
		
		

		Runnable update_thread = new Runnable() {
			public void run() {
				getWeatherInfo();
				handler.sendEmptyMessageDelayed(99, 2000);

			}
		};

		handler.postDelayed(update_thread,300);
		
		
	}
	
	
	
	private void init() {
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, list, 
				android.R.layout.simple_list_item_2, new String[] {"title","content"}, 
				new int[] {android.R.id.text1, android.R.id.text2});			
		
		lvWeather.setAdapter(simpleAdapter);
		
	}



	public void getWeatherInfo(){
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(WEATHER_LINK, null, 
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject arg0) {
						list.clear();
						Iterator<String> it = arg0.keys();
						while(it.hasNext()){
							String key = it.next();
							JSONObject obj = null;
							try {
								 obj = arg0.getJSONObject(key);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (obj != null) {
								Iterator<String> objIt = obj.keys();
								while (objIt.hasNext()) {
									String objKey = objIt.next();
									String objValue;
									try {
										objValue = obj.getString(objKey);
										HashMap<String, String> map = new HashMap<String, String>();
										map.put("title", objKey);
										map.put("content", objValue);
										Log.v(TAG, "title = " + objKey + " | content = " + objValue);
										list.add(map);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
						Log.v(TAG, "list.size = " + list.size());
					}			
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						// TODO Auto-generated method stub
						
					}
				});
		mQueue.add(jsonObjectRequest);
		
		
	}
}
