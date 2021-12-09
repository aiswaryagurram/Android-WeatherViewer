
/*****************************************************************************
 *          App Name: WeatherViewer                                          *
 *        class Name: Weather.java                                           *
 *           purpose: This java class is used to  Maintain one day's weather *
 *                    information                                            *
 ****************************************************************************/
package edu.niu.cs.z1887998.weatherviewer;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Weather {
    public final String dayOfWeek;
    public final String minTemp;
    public final String maxTemp;
    public final String humidity;
    public final String description;
    public final String iconURL;


    public Weather(long timeStamp, double minTemp, double maxTemp, double humidity, String description, String iconName)
    {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);
        this.dayOfWeek = convertTimeStampToDay(timeStamp);

        //The following two statements append Fahrenheit to the integer temperatures.
        //The API also supports Kelvin (the default) and celsius temperatures.
        this.minTemp = numberFormat.format(minTemp) + "\u00B0F";
        this.maxTemp = numberFormat.format(maxTemp) + "\u00B0F";

        //NumberFormat is called for local-specific percentage formatting to format the humidity percentage.
        //The web service returns this percentage as a whole number, so we divide that by 100.0 for formatting-in the U.S.
        //locale,1.00 is formatted as 100%, 0.5 is formatted as 50%,etc.
        this.humidity = NumberFormat.getPercentInstance().format(humidity / 100.0);
        this.description = description;
        this.iconURL = "http://openweathermap.org/img/w/" + iconName + ".png";
    }

    //converts timestamp to a day's name
    private static String convertTimeStampToDay(long timeStamp)
    {
        Calendar calendar = Calendar.getInstance();                 //creates calendar
        calendar.setTimeInMillis(timeStamp * 1000);                 //sets time
        TimeZone tz = TimeZone.getDefault();                        //gets the device's time zone
        calendar.add(calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));   //adjust's the time for the device's time zone
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");           //SimpleDateFormat that returns the day's name
        return dateFormatter.format(calendar.getTime());
    }
}


