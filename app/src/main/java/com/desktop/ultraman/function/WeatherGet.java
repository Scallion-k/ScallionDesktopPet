package com.desktop.ultraman.function;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class WeatherGet {
    public LocationClient mLocationClient=null;
    private MyLocationListener myListener=new MyLocationListener();
    private LocationClientOption option=new LocationClientOption();
    private String city="厦门市";
    private String text=null;
    private Context context;
    private RequestQueue mRequestQueue;
    private String url;

    public WeatherGet(Context context){
        this.context=context;
        mLocationClient=new LocationClient(context);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            /*
            1、国家：location.getCountry()
            2、城市：location.getCity()
            3、区域：location.getDistrict()
            4、街道：location.getStreet()
            5、详细地址：location.getAddStr()
             */
            Log.i("debug.city","type:"+bdLocation.getLocType());
            Log.i("debug.city","la:"+bdLocation.getLatitude()+"  lg:"+bdLocation.getLongitude());
            Log.i("debug.city","received: "+bdLocation.getCityCode());
            city=bdLocation.getCity();
        }
    }

    private String getCity() {
        Log.i("debug.city",""+city);
        return city;
    }
    public void Getweather(){
        text="null";
        if(getCity()==null){
            text="无法获取城市信息";
          //  return text;
        }
        String theCity=getCity().replace("市","");
        mRequestQueue=Volley.newRequestQueue(context);
        mRequestQueue.start();
        Log.i("debug.weather"," process 2");
        url="http://wthrcdn.etouch.cn/weather_mini?city="+theCity;
        JsonObjectRequest mRequest=new myJsonObjectRequest(url, null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    Log.i("debug.weather","weather get");
                    JSONObject data=new JSONObject(jsonObject.getString("data"));
                    JSONArray forecast=data.getJSONArray("forecast");
                    JSONObject today=forecast.getJSONObject(0);
                    String wendu=data.getString("wendu");
                    String high=today.getString("high");
                    String low=today.getString("low");
                    String date=today.getString("date");
                    String type=today.getString("type");
                    String city=data.getString("city");
                    Log.i("debug.weather",type);
                    text="今日"+date+" "+city+"当前气候 "+wendu+"℃ "+type+'\n'+high+low;
                    Log.i("debug.weather"," process 3"+'\n'+text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("debug.weather","weather get error");
            }
        }){
            @Override
            public byte[] getBody() {
                try {
                    return url.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        mRequestQueue.add(mRequest);
        Log.i("debug.weather"," process 4: "+text);
       // return text;
    }

    private class myJsonObjectRequest extends JsonObjectRequest {
        public myJsonObjectRequest(String url, JSONObject jsonRequest,
                                   Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){
            super(url,jsonRequest,listener,errorListener);
        }
        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            response.headers.put("HTTP.CONTENT_TYPE", "utf-8");
            try {
                String jsonString = new String(response.data, "utf-8");
                return Response.success(new JSONObject(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                return Response.error(new ParseError(e));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                return Response.error(new ParseError(e));
            }
        }
    }

    public String getText(){
        Log.i("debug.weather"," process 1"+text);
        return text;

    }

}
