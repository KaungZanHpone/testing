package com.example.site_and_map_kzp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.UiSettings;
import com.huawei.hms.maps.common.util.DistanceCalculator;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.LatLngBounds;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.PointOfInterest;
import com.huawei.hms.maps.model.Polyline;
import com.huawei.hms.maps.model.PolylineOptions;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.DetailSearchRequest;
import com.huawei.hms.site.api.model.DetailSearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapViewDemoActivity";
    private static final String[] RUNTIME_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private static final int REQUEST_CODE = 100;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    SettingsClient settingsClient;
    LocationCallback mLocationCallback;
    private HuaweiMap hMap;
    private MapView mMapView;
    private Marker mMarker;
    private SearchService searchService;
    public static final String API_KEY="CgB6e3x9uzxzgAb7Ea6ZzJ84toFhQvQdPYYEi/zvxVZV5LR7rEXMmd+NaglYQEcWCCnFHIK3B+B9x4+nBGHs0Mw/";
    // Location interaction object.
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Polyline polyline;
    // Location request object.
    private LocationRequest mLocationRequest;
    private boolean locationButtonClicked=false;
    private static double mLat,mLng;
    private FloatingActionButton locateMe,deleteRoute;

    public static final String ROOT_URL = "https://mapapi.cloud.huawei.com/mapApi/v1/routeService/";

    public static final String conection = "?key=";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    static String a_response = "";
    private LatLngBounds mLatLngBounds;
    private Marker mMarkerDestination;
    private List<Polyline> mPolylines = new ArrayList<>();
    private List<List<LatLng>> mPaths = new ArrayList<>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    renderRoute(mPaths, mLatLngBounds);
                    break;
                case 1:
                    Bundle bundle = msg.getData();
                    String errorMsg = bundle.getString("errorMsg");
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate:");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapview_mapviewdemo);
        locateMe=findViewById(R.id.locateMe);
        deleteRoute=findViewById(R.id.deleteRoute);
        //searchService = SearchServiceFactory.create(MainActivity.this, "CgB6e3x9uzxzgAb7Ea6ZzJ84toFhQvQdPYYEi/zvxVZV5LR7rEXMmd+NaglYQEcWCCnFHIK3B+B9x4+nBGHs0Mw/");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Bundle mapViewBundle = null;
        settingsClient = LocationServices.getSettingsClient(this);
        try {
            searchService = SearchServiceFactory.create(MainActivity.this, URLEncoder.encode("CgB6e3x9uzxzgAb7Ea6ZzJ84toFhQvQdPYYEi/zvxVZV5LR7rEXMmd+NaglYQEcWCCnFHIK3B+B9x4+nBGHs0Mw/", "utf-8"));
            Toast.makeText(this, "SearchService successful", Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "Error while making searchService", Toast.LENGTH_SHORT).show();
        }
        locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationButtonClicked=true;
                getLocation();
                Log.i("ResultsOf:",mLat+" "+mLng);
            }
        });
        deleteRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLngData.setLat(0);
                LatLngData.setLng(0);
                LatLngData.setDeslat(0);
                LatLngData.setDeslng(0);
                mMarker.remove();
                mPolylines.clear();
                mPaths.clear();
                polyline.remove();
                mMarkerDestination.remove();
                fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                        // Define callback for success in stopping requesting location updates.
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //...
                            }
                        })
                        // Define callback for failure in stopping requesting location updates.
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // ...
                            }
                        });
            }
        });

        /*locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationSettings();
                getLocation();
            }
        });*/


        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE);
        }

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        checkLocationSettings();
        getLocation();
    }


    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setCoordinates(double lat,double lng){
        mLat=lat;
        mLng=lng;
    }

    private void getLocation(){
        //Toast.makeText(this, "Refreshing location...", Toast.LENGTH_SHORT).show();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    setCoordinates(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude());
                    LatLngData.setLat(locationResult.getLastLocation().getLatitude());
                    LatLngData.setLng(locationResult.getLastLocation().getLongitude());
                    Toast.makeText(MainActivity.this, "New Current Location: "+LatLngData.getLat()+" , "+LatLngData.getLng(), Toast.LENGTH_SHORT).show();
                    Log.i("NewCurrentLoc: ",mLat+" , "+mLng);
                    if(mMarker!=null){
                        mMarker.remove();
                    }
                    if(mPaths.size()>0){
                        polyline.remove();
                        mPaths.clear();
                        mPolylines.clear();
                        mMarkerDestination.remove();
                    }
                    MarkerOptions options = new MarkerOptions()
                            .position(new LatLng(mLat,mLng))
                            .title("Your ")
                            .snippet("current location");
                    mMarker = hMap.addMarker(options);
                    if(locationButtonClicked) {
                        hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLng), 15));
                        locationButtonClicked=false;
                    }
                    if(LatLngData.getDeslat()!=0 && LatLngData.getDeslng()!=0 ){
                        String serviceName = "driving";
                        try {
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
                                    new Request.Builder().url(ROOT_URL + serviceName + conection + URLEncoder.encode(API_KEY, "UTF-8"))
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
                                    generateRoute(a_response);

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }
            }
        };

        fusedLocationProviderClient
                .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Processing when the API call is successful.
                        //Toast.makeText(MainActivity.this, "Location request made successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Processing when the API call fails.
                    }
                });
        // Note: When requesting location updates is stopped, the mLocationCallback object must be the same as LocationCallback in the requestLocationUpdates method.

        // Obtain the last known location.

    }
    private void checkLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        //mLocationRequest = new LocationRequest();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
// Check the device location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
                // Define callback for success in checking the device location settings.
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        // Initiate location requests when the location settings meet the requirements.
                        fusedLocationProviderClient
                                .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                                // Define callback for success in requesting location updates.
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // ...
                                    }
                                });
                    }
                })
                // Define callback for failure in checking the device location settings.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Device location settings do not meet the requirements.
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    // Call startResolutionForResult to display a pop-up asking the user to enable related permission.
                                    rae.startResolutionForResult(MainActivity.this, 0);
                                } catch (IntentSender.SendIntentException sie) {
                                    // ...
                                }
                                break;
                        }
                    }
                });
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        Log.d(TAG, "onMapReady: ");
        hMap = huaweiMap;
        hMap.setMyLocationEnabled(true);
        UiSettings uiSettings=hMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        hMap.setTrafficEnabled(true);
        hMap.setBuildingsEnabled(true);
        hMap.setIndoorEnabled(true);
        hMap.setWatermarkEnabled(true);
        hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.8567154 ,96.1840702 ), 15)); //  ,
        hMap.setOnPoiClickListener(new HuaweiMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                Toast.makeText(getApplicationContext(),"You have clicked: "+pointOfInterest.name
                        +"\n  PlaceID: "+pointOfInterest.placeId,Toast.LENGTH_LONG).show();




                DetailSearchRequest request = new DetailSearchRequest();
                request.setSiteId(pointOfInterest.placeId);
                request.setLanguage("en");
                request.setChildren(false);
                SearchResultListener<DetailSearchResponse> resultListener = new SearchResultListener<DetailSearchResponse>() {
                    // Return search results upon a successful search.
                    @Override
                    public void onSearchResult(DetailSearchResponse result) {
                        Site site=result.getSite();
                        if (result == null || (site = result.getSite()) == null) {
                            Toast.makeText(MainActivity.this, "Result null", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //Toast.makeText(MainActivity.this, "Data found!!!", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                        LinearLayout linearLayout=new LinearLayout(MainActivity.this);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        TextView name=new TextView(MainActivity.this);
                        TextView address=new TextView(MainActivity.this);
                        TextView distance=new TextView(MainActivity.this);
                        distance.setText("Distance from current location: "+DistanceCalculator.computeDistanceBetween(new LatLng(mLat,mLng),pointOfInterest.latLng)+" meters");
                        name.setText(site.getName());

                        address.setText(site.getFormatAddress());
                        linearLayout.addView(name);
                        linearLayout.addView(address);
                        linearLayout.addView(distance);
                        if(site.getPoi().getInternationalPhone()!=null){
                            TextView phone=new TextView(MainActivity.this);
                            phone.setText(site.getPoi().getInternationalPhone());
                            linearLayout.addView(phone);
                        }

                        builder.setView(linearLayout);
                        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Routing mLat,mLng
                                LatLngData.setDeslat(pointOfInterest.latLng.latitude);
                                LatLngData.setDeslng(pointOfInterest.latLng.longitude);
                                if(mMarkerDestination!=null) {
                                    mMarkerDestination.remove();
                                    mPaths.clear();
                                    mPolylines.clear();
                                    polyline.remove();
                                }

                                String serviceName = "driving";
                                try {
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
                                            new Request.Builder().url(ROOT_URL + serviceName + conection + URLEncoder.encode(API_KEY, "UTF-8"))
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
                                            generateRoute(a_response);

                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                    // Return the result code and description upon a search exception.
                    @Override
                    public void onSearchError(SearchStatus status) {
                        // Toast.makeText(MainActivity.this, "Search error occurred", Toast.LENGTH_SHORT).show();
                    }
                };
                searchService.detailSearch(request, resultListener);





            }
        });
    }

    private void generateRoute(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray routes = jsonObject.optJSONArray("routes");
            if (null == routes || routes.length() == 0) {
                return;
            }
            JSONObject route = routes.getJSONObject(0);

            // get route bounds
            JSONObject bounds = route.optJSONObject("bounds");
            if (null != bounds && bounds.has("southwest") && bounds.has("northeast")) {
                JSONObject southwest = bounds.optJSONObject("southwest");
                JSONObject northeast = bounds.optJSONObject("northeast");
                LatLng sw = new LatLng(southwest.optDouble("lat"), southwest.optDouble("lng"));
                LatLng ne = new LatLng(northeast.optDouble("lat"), northeast.optDouble("lng"));
                mLatLngBounds = new LatLngBounds(sw, ne);
            }

            // get paths
            JSONArray paths = route.optJSONArray("paths");
            for (int i = 0; i < paths.length(); i++) {
                JSONObject path = paths.optJSONObject(i);
                List<LatLng> mPath = new ArrayList<>();

                JSONArray steps = path.optJSONArray("steps");
                for (int j = 0; j < steps.length(); j++) {
                    JSONObject step = steps.optJSONObject(j);

                    JSONArray polyline = step.optJSONArray("polyline");
                    for (int k = 0; k < polyline.length(); k++) {
                        if (j > 0 && k == 0) {
                            continue;
                        }
                        JSONObject line = polyline.getJSONObject(k);
                        double lat = line.optDouble("lat");
                        double lng = line.optDouble("lng");
                        LatLng latLng = new LatLng(lat, lng);
                        mPath.add(latLng);
                    }
                }
                mPaths.add(i, mPath);
            }
            mHandler.sendEmptyMessage(0);

        } catch (JSONException e) {
            Log.e(TAG, "JSONException" + e.toString());
        }
    }

    private void renderRoute(List<List<LatLng>> paths, LatLngBounds latLngBounds) {
        if (null == paths || paths.size() <= 0 || paths.get(0).size() <= 0) {
            return;
        }


        for (int i = 0; i < paths.size(); i++) {
            List<LatLng> path = paths.get(i);
            PolylineOptions options = new PolylineOptions().color(Color.BLUE).width(5);
            for (LatLng latLng : path) {
                options.add(latLng);
            }

            polyline = hMap.addPolyline(options);
            mPolylines.add(i, polyline);

        }

        addOriginMarker(paths.get(0).get(0));
        addDestinationMarker(paths.get(0).get(paths.get(0).size() - 1));

        if (null != latLngBounds) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 5);
            hMap.moveCamera(cameraUpdate);
        } else {
            hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paths.get(0).get(0), 13));
        }

    }
    private void addOriginMarker(LatLng latLng) {
        if (null != mMarker) {
            mMarker.remove();
        }
        mMarker = hMap.addMarker(new MarkerOptions().position(latLng)
                .anchor(0.5f, 0.9f)
                // .anchorMarker(0.5f, 0.9f)
                .title("Origin")
                .snippet(latLng.toString()));
    }

    private void addDestinationMarker(LatLng latLng) {
        if (null != mMarkerDestination) {
            mMarkerDestination.remove();
        }
        mMarkerDestination = hMap.addMarker(
                new MarkerOptions().position(latLng).anchor(0.5f, 0.9f).title("Destination").snippet(latLng.toString()));
    }


    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.map:
                startActivity(new Intent(MainActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.blog:
                startActivity(new Intent(MainActivity.this,WebList.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.reminders:

        }
        return true;
    }
}