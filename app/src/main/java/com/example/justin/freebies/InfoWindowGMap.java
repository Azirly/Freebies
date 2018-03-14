package com.example.justin.freebies;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindowGMap implements GoogleMap.InfoWindowAdapter{
    private Context context;

    public InfoWindowGMap(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.map_custom_infowindow, null);

        TextView title_tv = view.findViewById(R.id.title);
        TextView description_tv = view.findViewById(R.id.description);
        ImageView img = view.findViewById(R.id.pic);

        TextView date_tv = view.findViewById(R.id.date);
        TextView location_tv = view.findViewById(R.id.location);
        //TextView transport_tv = view.findViewById(R.id.transport);

       title_tv.setText(marker.getTitle());
        description_tv.setText(marker.getSnippet());

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        //int imageId = context.getResources().getIdentifier(infoWindowData.getImage().toLowerCase(),"drawable", context.getPackageName());
        //img.setImageResource(imageId);

        date_tv.setText(infoWindowData.getDate());
        location_tv.setText(infoWindowData.getLocation());
        //transport_tv.setText(infoWindowData.getTransport());

        return view;
    }
}
