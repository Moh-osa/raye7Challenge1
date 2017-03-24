package com.example.mosama.testraye7;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MOsama on 3/24/2017.
 */

public  class RoutingHelper  extends AppCompatActivity {
  public static Activity main;

    ClearableEditText autocompleteFrom = null;
    ClearableEditText autocompleteTo = null;
    public static boolean AutoSrcIsFrom = false;
    public static GoogleMap mMap;
    LocationManager locationManager = null;
    Boolean isLocationPermissionGranted = false;
    boolean isGPSEnabled = false;
    static Marker startMarker = null;
    static Marker endMarker = null;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    AutocompleteFilter EgyptFilter = new AutocompleteFilter.Builder().setCountry("eg").build();
    ImageButton btnCurrentLocation = null;
    public static List<RouteStructure> routes = new ArrayList<>();
    public static List<Polyline> polylines = new ArrayList<>();
    static CardView cardview = null;
    static TextView cardSummary = null;
    static TextView cardDuration = null;
    Geocoder geocoder;

   public class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
        Context mContext;
        LatLng currentPlace;
        //  boolean updateTextStart = false;
        //  boolean updateTextEnd = false;
        boolean updateStart = false;
        boolean updateEnd = false;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        // Finding address using reverse geocoding
        @Override
        protected String doInBackground(LatLng... params) {
            Geocoder geocoder = new Geocoder(mContext);
            currentPlace = params[0];
            double latitude = params[0].latitude;
            double longitude = params[0].longitude;
            List<Address> addresses = null;
            String addressText = "";
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                // Thread.sleep(500);


                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressText += address.getAddressLine(i);
                        if (i + 1 < address.getMaxAddressLineIndex())
                            addressText += ", ";
                        else
                            addressText += ".";
                    }
//                    addressText = String.format("%s, %s, %s",
//                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
//                            address.getLocality(),
//                            address.getCountryName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //  selectedLocAddress = addressText;
            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            // Setting the title for the marker.
            // This will be displayed on taping the marker
            //  markerOptions.title(addressText);
            // Placing a marker on the touched position
            //   myMap.addMarker(markerOptions);
            if (updateStart) {
                autocompleteFrom.setText(addressText);
                autocompleteFrom.setClearIconVisible(true);
                updateStartMarker(true, currentPlace, addressText);

            } else if (updateEnd) {
                autocompleteTo.setText(addressText);
                autocompleteTo.setClearIconVisible(true);
                updateEndMarker(true, currentPlace, addressText);

            }
        }
    }

   public class ParserTask extends AsyncTask<String, Integer, List<RouteStructure>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<RouteStructure> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<RouteStructure> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<RouteStructure> result) {
            MainActivity.routes = result;
            MainActivity.drawRoutes(getApplicationContext());
        }
    }

   public class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }
    void updateStartMarker(boolean view, LatLng currentPlace, String address) {
        if (view) {
            if (startMarker == null) {
                startMarker = mMap.addMarker(new MarkerOptions().position(currentPlace).title(address));
                startMarker.setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            } else {
                startMarker.setPosition(currentPlace);
                startMarker.setTitle(address);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPlace, 10));
        } else {
            if (startMarker != null) {
                startMarker.remove();
                startMarker = null;
            }
        }
        updateMap();
    }

    void updateEndMarker(boolean view, LatLng currentPlace, String address) {
        if (view) {
            if (endMarker == null) {
                endMarker = mMap.addMarker(new MarkerOptions().position(currentPlace).title(address));
            } else {
                endMarker.setPosition(currentPlace);
                endMarker.setTitle(address);
            }
        } else {
            if (endMarker != null) {
                endMarker.remove();
                endMarker = null;
            }
        }

        updateMap();
    }
    void updateMap() {
        if (startMarker != null && endMarker != null) {

            String url = getMapsApiDirectionsUrl(startMarker.getPosition(), endMarker.getPosition());
            FetchUrl fetchUrl = new FetchUrl();
            fetchUrl.execute(url);


        } else {
            cardview.setVisibility(View.GONE);
            for (Polyline p : polylines)
                p.remove();
        }
    }
    private String getMapsApiDirectionsUrl(LatLng start, LatLng end) {
        String origin = "origin=" + start.latitude + "," + start.longitude;
        String destination = "destination=" + end.latitude + "," + end.longitude;

        String params = origin + "&" + destination + "&units=metric&alternatives=true&sensor=false&departure_time=now";
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }


}
