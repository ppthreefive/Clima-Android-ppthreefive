package com.londonappbrewery.climapm;

import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity
{
    // Request codes:
    final int REQUEST_CODE = 123;
    final int NEW_CITY_CODE = 456;

    // Base URL for OpenWeatherMap API.
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "1d6fd2f50a1f96db417801d8116162f7";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;
    final String LOGCAT_TAG = "Clima";

    // Set LOCATION_PROVIDER here:
    //String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER; // Uses Wi-Fi or cell towers for location.
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER; //<-- Fine location, good for emulation.

    // Member Variables:
    boolean mUseLocation = true;
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);

        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* Intents are a data structure that holds information on an action that needs to be performed.
                 * They can name specific activities that should respond OR it can just describe
                 * the action that needs to be performed, and let the Android OS decide how to handle it.*/
                Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);

                // Using startActivityForResult since we just get back the city name,
                // and providing an arbitrary request code so we can check against it later.
                startActivityForResult(myIntent, NEW_CITY_CODE);
            }
        });
    }

    // Add onResume() life cycle callback:
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("Clima", "onResume() called");

        Intent myIntent = getIntent();

        if(mUseLocation)
        {
            getWeatherForCurrentLocation();
        }
        /*String city = myIntent.getStringExtra("City");

        if(city != null)
        {
            getWeatherForNewCity(city);
        }
        else
        {
            Log.d("Clima", "Getting weather for current location");
            getWeatherForCurrentLocation();
        }

        Log.d("Clima", "Getting weather for current location");
        getWeatherForCurrentLocation();*/
    }

    // Callback received when a new city name is entered on the second screen.
    // Checking request code and if result is OK before making the API call
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOGCAT_TAG, "onActivityResult() called");

        if (requestCode == NEW_CITY_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                String city = data.getStringExtra("City");
                Log.d(LOGCAT_TAG, "New city is " + city);

                mUseLocation = false;
                getWeatherForNewCity(city);
            }
        }
    }

    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city)
    {
        Log.d(LOGCAT_TAG, "Getting weather for new city");
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }

    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation()
    {
        Log.d(LOGCAT_TAG, "Getting weather for current location");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {

                Log.d(LOGCAT_TAG, "onLocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d(LOGCAT_TAG, "longitude is: " + longitude);
                Log.d(LOGCAT_TAG, "latitude is: " + latitude);

                // Providing 'lat' and 'lon' (spelling: Not 'long') parameter values
                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                // Log statements to help you debug your app.
                Log.d(LOGCAT_TAG, "onStatusChanged() callback received. Status: " + status);
                Log.d(LOGCAT_TAG, "2 means AVAILABLE, 1: TEMPORARILY_UNAVAILABLE, 0: OUT_OF_SERVICE");
            }

            @Override
            public void onProviderEnabled(String provider)
            {
                Log.d(LOGCAT_TAG, "onProviderEnabled() callback received. Provider: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider)
            {
                Log.d(LOGCAT_TAG, "onProviderDisabled() callback received. Provider: " + provider);
            }
        };

        // This is the permission check to access Coarse location.

        //changed fine to course in params
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        // Some additional log statements to help you debug
        Log.d(LOGCAT_TAG, "Location Provider used: "
                + mLocationManager.getProvider(LOCATION_PROVIDER).getName());
        Log.d(LOGCAT_TAG, "Location Provider is enabled: "
                + mLocationManager.isProviderEnabled(LOCATION_PROVIDER));
        Log.d(LOGCAT_TAG, "Last known location (if any): "
                + mLocationManager.getLastKnownLocation(LOCATION_PROVIDER));
        Log.d(LOGCAT_TAG, "Requesting location updates");


        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    // This is the callback that is received when the permission is granted (or denied).
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(LOGCAT_TAG, "onRequestPermissionResult(): Permission granted!");
                getWeatherForCurrentLocation();
            }
            else
            {
                Log.d(LOGCAT_TAG, "Permission denied =( ");
            }
        }
    }

    // This is the networking code with the parameters configured
    private void letsDoSomeNetworking(RequestParams params)
    {
        // AsyncHttpClient belongs to the loopj dependency
        AsyncHttpClient client = new AsyncHttpClient();

        // Making an HTTP GET request by providing a URL and the parameters
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler()
        {
           @Override
           public void onSuccess(int statusCode, Header[] headers, JSONObject response)
           {
                Log.d(LOGCAT_TAG, "Success! JSON: " + response.toString());

                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);
           }

           @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response)
           {
               Log.e(LOGCAT_TAG, "Fail " + e.toString());
               Log.d(LOGCAT_TAG, "Status code " + statusCode);
               Log.d(LOGCAT_TAG, "Here's what we got instead " + response.toString());
               Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();
           }
        });
    }

    // Updates the information shown on the screen.
    private void updateUI(WeatherDataModel weather)
    {
        mCityLabel.setText(weather.getCity());
        mTemperatureLabel.setText(weather.getTemperature());
        mWeatherImage.setImageResource(getResources().getIdentifier(weather.getIconName(),
                "drawable", getPackageName()));
    }

    // Freeing up resources when the app is paused.
    @Override
    protected void onPause()
    {
        super.onPause();

        if (mLocationManager != null)
        {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}