/*******************************************************************************
 *                                                                             *
 *     Programmer: Aiswarya Gurram(z1887998)                                   *
 *                                                                             *
 *       App Name : WeatherViewer                                              *
 *                                                                             *
 * Purpose: In this java class, we get a 7 day forecast from the longitude and *
 *          latitude coordinates. These coordinates are obtained by converting *
 *          the city name.                                                     *
 ******************************************************************************/
package edu.niu.cs.z1887998.weatherviewer;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private List<Weather> weatherList = new ArrayList<>();                  //List of Weather objects representing the forecast
    private WeatherArrayAdapter weatherArrayAdapter;                        //ArrayAdapter for binding Weather objects to a ListView
    private ListView weatherListView;                                       //displays weather information
    //Configuring Toolbar, ListView and FloatingActionButton
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //autogenerated code to inflate layout and configure Toolbar
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //creating ArrayAdapter
        weatherListView = (ListView)findViewById(R.id.weatherListView);
        FloatingActionButton fab =(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);    //getting the location Text
                List<LatLng> ll = get_lat_long_by_city(locationEditText.getText().toString()); // method for getting latitude and longitude from city name
                URL url = createURL(String.valueOf(ll.get(0).latitude),String.valueOf(ll.get(0).longitude));
                if (ll.size() > 0)
                {
                    //hide keyboard and initiate a GetWeatherTask to download weather data from OpenWeatherMap.org in a separated thread
                    if (url != null)
                    {
                        dismisskeyboard(locationEditText);
                        GetWeatherTask getlocalWeatherTask = new GetWeatherTask();
                        getlocalWeatherTask.execute(url);
                    }
                    else
                    {
                        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
                else
                    {
                    Snackbar.make(view, "Oops coordinates are not found try again or correctly type city!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }
    //When the user touches the floatingActionButton, it dismisses the keyboard
    private void dismisskeyboard(View view)
    {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
    //creating openweathermap.prg web service URL using city
    private URL createURL(String latitude,String longitude)
    {
        String baseUrl = getString(R.string.web_service_url);
        try
        {// here we have build complete url to pass to out http connection to get json response from api
            return new URL(baseUrl+"lat="+latitude+"&lon="+longitude+"&exclude=hourly,minutely,current&&units=imperial&appid=bf3ba4359313c89bebdee4cd35f57ddd");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    // this is async worker that work in background to fetch our data
    private class GetWeatherTask extends AsyncTask<URL,Void,JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (IOException e) {
                        Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    return new JSONObject(builder.toString());
                } else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error, Snackbar.LENGTH_LONG).show();


                }
            } catch (Exception e) {

                 Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error, Snackbar.LENGTH_LONG).show();

                e.printStackTrace();
            } finally {
                connection.disconnect();                                    //closes the HttpURLConnection
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject weather)
        {
            // here we got our json response now passing json to method which is our parser to parse data and show it in our listview
            convertJSONtoArrayList(weather);
            weatherListView.smoothScrollToPosition(0);                     //scroll to top
        }
    }
    //creates Weather objects from JSONObject containing the forecast
    private void convertJSONtoArrayList(JSONObject forecast)
    {
        weatherList.clear();                                                // clear out old weather data
        try {
            JSONArray list = forecast.getJSONArray("daily");
            for(int i = 0; i < list.length(); ++i)                          //converts each element of list to weather object
            {
                JSONObject day = list.getJSONObject(i);                     //gets one day's data
                JSONObject temperatures = day.getJSONObject("temp");        //gets the day's temperatures JSONObject

                //get day's "weather" JSONObject for the description and icon
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                //add new Weather object to weatherList
                weatherList.add(new Weather(day.getLong("sunrise"),
                        temperatures.getDouble("min"),                  //minimum temperature
                        temperatures.getDouble("max"),                  //maximum temperature
                        day.getDouble("humidity"),                      //percent humidity
                        weather.getString("description"),               //weather conditions
                        weather.getString("icon")));                    //icon name
            }
            weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
            weatherListView.setAdapter(weatherArrayAdapter);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

    }
    // this is method for getting coordinates from location
    private  List<LatLng> get_lat_long_by_city(String location)
    {
        List<LatLng> ll = null;
        if(Geocoder.isPresent())
        {
            try
            {
                Geocoder gc = new Geocoder(this);
                List<Address> addresses= gc.getFromLocationName(location, 5); // get the found Address Objects
                ll = new ArrayList<LatLng>(addresses.size()); // A list to save the coordinates if they are available
                for(Address a : addresses)
                {
                    if(a.hasLatitude() && a.hasLongitude())
                    {
                        ll.add(new LatLng(a.getLatitude(), a.getLongitude()));
                    }
                }
                } catch (IOException e) {
                    // handle the exception
                }
            }
            else {
                Toast.makeText(MainActivity.this,"Geocoder not found",Toast.LENGTH_LONG).show();
            }
            return ll;
        }
}