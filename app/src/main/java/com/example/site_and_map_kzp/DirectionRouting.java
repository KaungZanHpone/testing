package com.example.site_and_map_kzp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DirectionRouting {
    public static final String ROOT_URL = "https://mapapi.cloud.huawei.com/mapApi/v1/routeService/";

    public static final String conection = "?key=";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    static String a_response = "";

    public static String driving(String serviceName, String apiKey) throws Exception {
        JSONObject json = new JSONObject();
        JSONObject origin = new JSONObject();
        JSONObject destination = new JSONObject();


        try {
            origin.put("lng", LatLngData.getLng());
            origin.put("lat", LatLngData.getLat());
            destination.put("lng", LatLngData.getDeslng());
            destination.put("lat", LatLngData.getDeslat());
            json.put("origin", origin);
            json.put("destination", destination);
        } catch (JSONException e) {
            Log.e("error", e.getMessage());
        }
        RequestBody body = RequestBody.create(JSON, String.valueOf(json));

        OkHttpClient client = new OkHttpClient();
        Request request =
                new Request.Builder().url(ROOT_URL + serviceName + conection + URLEncoder.encode(apiKey, "UTF-8"))
                        .post(body)
                        .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("driving", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                a_response=response.body().string();
                Log.d("driving", a_response);

            }
        });

        return a_response;
    }
}
