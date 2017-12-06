package com.londonappbrewery.climapm;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataModel
{
    // Member variables that hold weather information
    private String mTemperature, mCity, mIconName;
    private int mCondition;

    // Create a WeatherDataModel from a JSON
    // We will call this instead of the standard constructor
    public static WeatherDataModel fromJson(JSONObject jsonObject)
    {
        // JSON parsing needs to be surrounded by Try-Catch in case it doesn't work
        try
        {
            WeatherDataModel weatherData = new WeatherDataModel();

            weatherData.mCity = jsonObject.getString("name");
            weatherData.mCondition = jsonObject.getJSONArray("weather")
                    .getJSONObject(0).getInt("id");

            weatherData.mIconName = updateWeatherIcon(weatherData.mCondition);
            double tempResult = (((jsonObject.getJSONObject("main").getDouble("temp")
                    - 273.15)*9.0)/5.0) + 32;

            int roundedValue = (int) Math.rint(tempResult);

            weatherData.mTemperature = Integer.toString(roundedValue);

            return weatherData;
        }
        catch(JSONException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    // Get the weather image name from OpenWeatherMap's condition (marked by a number code)
    private static String updateWeatherIcon(int condition) {

        if (condition >= 0 && condition < 300) {return "tstorm1";}
        else if (condition >= 300 && condition < 500) {return "light_rain";}
        else if (condition >= 500 && condition < 600) {return "shower3";}
        else if (condition >= 600 && condition <= 700) {return "snow4";}
        else if (condition >= 701 && condition <= 771) {return "fog";}
        else if (condition >= 772 && condition < 800) {return "tstorm3";}
        else if (condition == 800) {return "sunny";}
        else if (condition >= 801 && condition <= 804) {return "cloudy2";}
        else if (condition >= 900 && condition <= 902) {return "tstorm3";}
        else if (condition == 903) {return "snow5";}
        else if (condition == 904) {return "sunny";}
        else if (condition >= 905 && condition <= 1000) {return "tstorm3";}

        return "dunno";
    }

    // Getter methods
    public String getTemperature()
    {
        return mTemperature + "°";
    }

    public String getIconName()
    {
        return mIconName;
    }

    public String getCity()
    {
        return mCity;
    }
}