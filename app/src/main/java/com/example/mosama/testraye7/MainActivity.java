package com.example.mosama.testraye7;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.places.*;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends RoutingHelper implements OnMapReadyCallback {


    RoutingHelper routingHelper = new RoutingHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        routingHelper.main = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        cardview = (CardView) findViewById(R.id.card_view);
        cardSummary = (TextView) findViewById(R.id.tvRouteSummary);
        cardDuration = (TextView) findViewById(R.id.tvRouteDuration);
        btnCurrentLocation = (ImageButton) findViewById(R.id.btnCurrentLocation);
        btnCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location l = getCurrentLocation();
                if (l != null) {
                    LatLng currentPlace = new LatLng(l.getLatitude(), l.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPlace, 10));
                    //  String Address = addressFromlatLon(currentPlace);
                    ReverseGeocodingTask reverseGeocodingTask = new ReverseGeocodingTask(getApplicationContext());
                    reverseGeocodingTask.updateStart = true;
                    reverseGeocodingTask.execute(currentPlace);
                    //   autocompleteFrom.setText(Address);
                    //   autocompleteFrom.setClearIconVisible(true);

                    //     updateStartMarker(true, currentPlace, Address);
                }
                //  mMap.addMarker(new MarkerOptions().position(cairo).title("Marker in Cairo"));

            }
        });
        autocompleteFrom = (ClearableEditText) findViewById(R.id.place_autocomplete_fragment_from);
        autocompleteFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("s", "clicked on from");
                launchAutoComplete(true);

            }
        });
        autocompleteFrom.setListener(new ClearableEditText.Listener() {
            @Override
            public void didClearText() {

                updateStartMarker(false, null, "");
                autocompleteFrom.setClearIconVisible(false);
            }
        });

        autocompleteTo = (ClearableEditText) findViewById(R.id.place_autocomplete_fragment_to);

        autocompleteTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("s", "clicked on to");
                launchAutoComplete(false);

            }
        });
        autocompleteTo.setListener(new ClearableEditText.Listener() {
            @Override
            public void didClearText() {
                updateEndMarker(false, null, "");
                autocompleteTo.setClearIconVisible(false);
            }
        });

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Cairo, Egypt, and move the camera.
        LatLng currentPlace = new LatLng(30.055, 31.21);
        Location l = getCurrentLocation();
        if (l != null) {
            currentPlace = new LatLng(l.getLatitude(), l.getLongitude());

        }
        //  mMap.addMarker(new MarkerOptions().position(cairo).title("Marker in Cairo"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPlace, 10));
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                ReverseGeocodingTask reverseGeocodingTask = new ReverseGeocodingTask(getApplicationContext());
                reverseGeocodingTask.updateEnd = true;
                //   String Address = addressFromlatLon(latLng);
                reverseGeocodingTask.execute(latLng);
//                updateEndMarker(true, latLng, Address);
//                autocompleteTo.setText(Address);
//                autocompleteTo.setClearIconVisible(true);
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ///todo hide google options
                return false;
            }
        });
        //  mMap.setTrafficEnabled(true);
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                if (polylines != null && polylines.size() > 0) {
                    for (int i = 0; i < polylines.size(); i++) {
                        if ((int) polyline.getTag() == i) {
                            routes.get(i).selected = true;
                        } else {
                            routes.get(i).selected = false;
                        }

                    }
                    drawRoutes(getApplicationContext());


                }
            }
        });
    }

    public void launchAutoComplete(boolean src) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(EgyptFilter)
                            .build(this);
            //   intent.putExtra("src", src);
            AutoSrcIsFrom = src;
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    //   Bundle extras2=   data.getExtras();
                    //  boolean src = extras2.getBoolean("src");
                    if (AutoSrcIsFrom) //from
                    {
                        autocompleteFrom.setText(place.getAddress());
                        autocompleteFrom.setClearIconVisible(true);
                        updateStartMarker(true, place.getLatLng(), place.getAddress().toString());
                    } else {
                        autocompleteTo.setText(place.getAddress());
                        autocompleteTo.setClearIconVisible(true);
                        updateEndMarker(true, place.getLatLng(), place.getAddress().toString());

                    }
                    //  Log.i(TAG, "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    //  Log.i(TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
            }
        } catch (Exception ex) {
            Log.e("ss", ex.getMessage());
        }
    }

    public boolean isLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //   Log.v(TAG,"Permission is granted");
                return true;
            } else {

                //  Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //   Log.v(TAG,"Permission is granted");
                    return true;
                } else {
                    return false;
                }
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            // Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    LocationListener updatedOnce = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {


            LatLng currentPlace = new LatLng(location.getLatitude(), location.getLongitude());
            ReverseGeocodingTask reverseGeocodingTask = new ReverseGeocodingTask(getApplicationContext());
            reverseGeocodingTask.updateStart = true;
            reverseGeocodingTask.execute(currentPlace);

//            String address = addressFromlatLon(currentPlace);
//            autocompleteFrom.setText(address);
//            updateStartMarker(true, currentPlace, address);


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private Location getCurrentLocation() {
        isLocationPermissionGranted = isLocationPermissionGranted();
        if (isLocationPermissionGranted) {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGPSEnabled) {
                // no network provider is enabled
                Toast.makeText(getApplicationContext(), "GPS is disabled", Toast.LENGTH_SHORT);

            } else {

                try {

                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null && !veryOld(location)) {
                        return location;

                    } else
                        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, updatedOnce, Looper.myLooper());

                } catch (SecurityException ex) {


                }
            }


        }
        return null;
    }

    //check if location age is older than one minute
    private boolean veryOld(Location last) {
        long duration = (SystemClock.elapsedRealtimeNanos() - last
                .getElapsedRealtimeNanos()) / 1000000; //duration in ms

        return duration < 60 * 1000;
    }


    public static void drawRoutes(Context ctx) {
        int selectedIndex = -1;
        if (polylines != null) {
            for (Polyline p : polylines)
                p.remove();
        }
        if (routes != null && routes.size() > 0) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            polylines.clear();
            // Traversing through all the routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                RouteStructure path = routes.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.points.size(); j++) {
                    HashMap<String, String> point = path.points.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(20);
                if (routes.get(i).isSelected()) {
                    selectedIndex = i;
                    lineOptions.color(Color.BLUE);
                    cardview.setVisibility(View.VISIBLE);
                    cardSummary.setText(routes.get(i).summary.length() > 15 ? routes.get(i).summary.substring(0, 15) + ".." : routes.get(i).summary);
                    int secs = Integer.parseInt(routes.get(i).duration);
                    int mins = (secs / 60) % 60;
                    int hours = secs / 3600;
                    cardDuration.setText("ETA " + hours + " h " + mins + " m");

                } else
                    lineOptions.color(Color.rgb(160, 160, 160));


                Log.d("onPostExecute", "onPostExecute lineoptions decoded");


                // Drawing polyline in the Google Map for the i-th route
                if (lineOptions != null) {

                    Polyline tmp = mMap.addPolyline(lineOptions);
                    tmp.setClickable(true);
                    tmp.setTag(i);
                    polylines.add(tmp);
                } else {
                    Log.d("onPostExecute", "without Polylines drawn");
                }
            }
            polylines.get(selectedIndex).setZIndex(polylines.size());
            updateCamera();
        } else {
            Log.e("draw route", "No route found!");
            Toast.makeText(ctx, "No route found!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void updateCamera() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startMarker.getPosition());
        builder.include(endMarker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }



}


