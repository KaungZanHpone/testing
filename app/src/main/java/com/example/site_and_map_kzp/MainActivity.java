package com.example.site_and_map_kzp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.PointOfInterest;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.DetailSearchRequest;
import com.huawei.hms.site.api.model.DetailSearchResponse;
import com.huawei.hms.site.api.model.HwLocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
    Button button,locateMe;
    private SearchService searchService;
    // Location interaction object.
    private FusedLocationProviderClient fusedLocationProviderClient;
    // Location request object.
    private LocationRequest mLocationRequest;
    private static double mLat,mLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate:");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.goToWebView);
        locateMe=findViewById(R.id.locate);
        mMapView = findViewById(R.id.mapview_mapviewdemo);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Bundle mapViewBundle = null;
        settingsClient = LocationServices.getSettingsClient(this);
        try {
            searchService = SearchServiceFactory.create(MainActivity.this, URLEncoder.encode("CgB6e3x9uzxzgAb7Ea6ZzJ84toFhQvQdPYYEi/zvxVZV5LR7rEXMmd+NaglYQEcWCCnFHIK3B+B9x4+nBGHs0Mw/", "utf-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "encode apikey error");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ViewWebsite.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationSettings();
                getLocation();
            }
        });


        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE);
        }

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
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

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    // Process the location callback result.
                    setCoordinates(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude());
                    Toast.makeText(getApplicationContext(),"Current Latitude : "+mLat+"\n Current Longitude"+mLng,Toast.LENGTH_LONG).show();
                    hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat,mLng ), 15));

                    MarkerOptions options = new MarkerOptions()
                            .position(new LatLng(mLat,mLng))
                            .title("Your ")
                            .snippet("current location");
                    mMarker = hMap.addMarker(options);


                    hMap.setOnPoiClickListener(new HuaweiMap.OnPoiClickListener() {
                        @Override
                        public void onPoiClick(PointOfInterest pointOfInterest) {
                           Toast.makeText(getApplicationContext(),"You have clicked: "+pointOfInterest.name
                                    +"\n  PlaceID: "+pointOfInterest.placeId,Toast.LENGTH_LONG).show();
                            /*String uriString = "mapapp://route?saddr="+mLat+","+mLng+"&daddr="+pointOfInterest.latLng.latitude+
                                    ","+pointOfInterest.latLng.longitude+"&type=drive";
                            Uri content_url = Uri.parse(uriString);
                            Intent intent = new Intent(Intent.ACTION_VIEW, content_url);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }*/
                            // Declare a SearchService object.
                            //SearchService searchService;
                            /* */
                            String uriString = "mapapp://route?saddr="+mLat+","+mLng+"&daddr="+pointOfInterest.latLng.latitude+","+pointOfInterest.latLng.longitude+"&type=drive";
                            Uri content_url = Uri.parse(uriString);
                            Intent intent = new Intent(Intent.ACTION_VIEW, content_url);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }
                            //searchService = SearchServiceFactory.create(MainActivity.this, "CgB6e3x9uzxzgAb7Ea6ZzJ84toFhQvQdPYYEi/zvxVZV5LR7rEXMmd+NaglYQEcWCCnFHIK3B+B9x4+nBGHs0Mw/");
//                            NearbySearchRequest request = new NearbySearchRequest();
//                            Coordinate location = new Coordinate(mLat, mLng);
//                            request.setLocation(location);
//                            request.setQuery("");
//                            //request.setRadius(10000);
//                            request.setHwPoiType(HwLocationType.HEALTH_CARE);
//                            //request.setLanguage("fr");
//                            //request.setPageIndex(1);
////                            request.setPageSize(5);
//                            //request.setStrictBounds(false);
//// Create a search result listener.
//                            SearchResultListener<NearbySearchResponse> resultListener = new SearchResultListener<NearbySearchResponse>() {
//                                // Return search results upon a successful search.
//                                @Override
//                                public void onSearchResult(NearbySearchResponse results) {
//                                    if (results == null || results.getTotalCount() <= 0) {
//                                        Toast.makeText(getApplicationContext(),"No data found",Toast.LENGTH_LONG).show();
//                                        return;
//                                    }
//                                    List<Site> sites = results.getSites();
//                                    if(sites == null || sites.size() == 0){
//                                        Toast.makeText(getApplicationContext(),"Sites list null",Toast.LENGTH_LONG).show();
//                                        return;
//                                    }
//
//
//                                    AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
//                                    LinearLayout linearLayout=new LinearLayout(MainActivity.this);
//                                    linearLayout.setOrientation(LinearLayout.VERTICAL);
//                                    for(Site site:sites){
//                                        if(site.getSiteId().equals(pointOfInterest.placeId)) {
//                                            TextView name = new TextView(MainActivity.this);
//                                            name.setText(site.getName());
//                                            linearLayout.addView(name);
//                                            TextView phone = new TextView(MainActivity.this);
//                                            phone.setText("Phone: "+site.getPoi().getPhone());
//                                            linearLayout.addView(phone);
//                                            TextView address = new TextView(MainActivity.this);
//                                            address.setText("Address: "+site.getFormatAddress());
//                                            linearLayout.addView(address);
//                                        }
//                                    }
//                                    builder.setView(linearLayout);
//                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.cancel();
//                                        }
//                                    });
//                                    builder.show();
//
//
//
//
//
//                                }
//                                // Return the result code and description upon a search exception.
//                                @Override
//                                public void onSearchError(SearchStatus status) {
//                                    Toast.makeText(getApplicationContext(),"Search error occurred",Toast.LENGTH_LONG).show();
//                                }
//                            };
//
//                            searchService.nearbySearch(request, resultListener);
//
//
//

                        }
                    });
                    hMap.setOnMarkerClickListener(new HuaweiMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                           // Toast.makeText(getApplicationContext(),marker.getTitle(),Toast.LENGTH_LONG).show();
                            return false;
                        }
                    });
                }
            }
        };
        fusedLocationProviderClient
                .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Processing when the API call is successful.
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Processing when the API call fails.
                    }
                });
    }
    private void checkLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        mLocationRequest = new LocationRequest();
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
        hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.6804 ,139.7690 ), 15)); // 16.8567154 , 96.1840702
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
}