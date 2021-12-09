
/*****************************************************************************
 *          App Name: WeatherViewer                                          *
 *        class Name: WeatherArrayAdapter.java                               *
 *           purpose: An ArrayAdapter for displaying a List<Weather>'s       *
 *                    elements in a ListView                                 *
 ****************************************************************************/
package edu.niu.cs.z1887998.weatherviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherArrayAdapter extends ArrayAdapter<Weather>
{
    //class for reusing views as list items scroll off and onto the screen
    private static class ViewHolder
    {
        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView highTextView;
        TextView humidityTextView;
    }
    private Map<String, Bitmap> bitmaps = new HashMap<>();                   //stores already downloaded Bitmaps for reuse
    public WeatherArrayAdapter(Context context, List<Weather> forecast)      //constructor to initialize superclass inherited members
    {
        super(context, -1, forecast);
    }
    //creates the custom views for the ListView's items
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Weather day = getItem(position);                                    //geting Weather object for this specified ListView position
        ViewHolder viewHolder;                                              //object that reference's list item's views
        //checks for reusable ViewHolder form a ListView item that scrolled offscreen; otherwise , creates a new ViewHolder
        if (convertView == null)                                            //no reusable ViewHolder, so creating one
        {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.conditionImageView = (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView = (TextView) convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView = (TextView) convertView.findViewById(R.id.lowTextView);
            viewHolder.highTextView = (TextView) convertView.findViewById(R.id.highTextView);
            viewHolder.humidityTextView = (TextView) convertView.findViewById(R.id.humidityTextView);
        }
        //else reuses existing ViewHolder stored as the list item's tag
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //if weather condition icon already downloaded, use it;otherwise, download icon in a separate thread
        if (bitmaps.containsKey(day.iconURL))
        {
            try
            {
                viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconURL));
            }
            catch (Exception e)
            {
            }
        }
        else {
            try
            {   //download and display weather condition image
                new LoadImageTask(viewHolder.conditionImageView).execute(day.iconURL);
            }
            catch (Exception e){
            }
        }
        //get other data from Weather object and place into views
        try {
            Context context = getContext();                                         //for loading string resources
            viewHolder.dayTextView.setText(context.getString(R.string.day_description, day.dayOfWeek, day.description));
            viewHolder.lowTextView.setText(context.getString(R.string.low_temp, day.minTemp));
            viewHolder.highTextView.setText(context.getString(R.string.high_temp, day.maxTemp));
            viewHolder.humidityTextView.setText(context.getString(R.string.humidity, day.humidity));

        }
        catch (Exception e){
        }
        return convertView;                                                         //return completed list item to display
    }
    //AsyncTask to load weather-condition icons in a separate thread
    private class LoadImageTask extends AsyncTask<String,  Void, Bitmap>
    {
        private ImageView imageView;                                                //displays the thumbnail
        //store ImageView on which to set the downloaded Bitmap
        public LoadImageTask(ImageView imageView)
        {
            this.imageView = imageView;
        }
        //load image: params[0] is the String URL representing the image
        @Override
        protected Bitmap doInBackground(String... params)
        {
            Bitmap bitmap = null;
            HttpURLConnection  connection = null;
            try
            {
                URL url;
                url = new URL(params[0]);                                           //creates URL for image
                //opens an HttpURLConnection, get its InputStream and downloaded the image
                connection = (HttpURLConnection) url.openConnection();
                try(InputStream inputStream = connection.getInputStream())
                {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(params[0],bitmap);                                  //cache for later use
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally {
                connection.disconnect();                                           //close the HttpURLConnection
            }
            return bitmap;
        }
        //set weather-condition image in List item
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageView.setImageBitmap(bitmap);
        }
    }
}
