package com.example.justin.freebies;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class InfoWindowGMap implements GoogleMap.InfoWindowAdapter {

    private String std_image = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/1000px-No_image_available.svg.png";
    private boolean first_call = false;
    private List<Marker> markerCheck = new ArrayList<Marker>();
    private Context context;

    public InfoWindowGMap(Context ctx) {
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.map_custom_infowindow, null);

        TextView title_tv = view.findViewById(R.id.title);
        TextView description_tv = view.findViewById(R.id.description);
        ImageView img = view.findViewById(R.id.pic);
        TextView date_tv = view.findViewById(R.id.date);
        TextView location_tv = view.findViewById(R.id.location);

        title_tv.setText(marker.getTitle());
        description_tv.setText(marker.getSnippet());

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        if(infoWindowData.getImage() == ""){
            img.setVisibility(View.INVISIBLE);
        }
        else {
            if (infoWindowData.getImage().contains("http")) {
                if(markerCheck.contains(marker)){
                    Picasso.get().load(infoWindowData.getImage()).resize(300, 400).centerCrop().into(img);
                }
                else{
                    markerCheck.add(marker);
                    Picasso.get().load(infoWindowData.getImage()).resize(300, 400).centerCrop().into(img, new InfoWindowRefresher(marker));
                }
            }
            else if (infoWindowData.getImage() != null && !infoWindowData.getImage().isEmpty()) {
                byte[] decodedByteArray = android.util.Base64.decode(infoWindowData.getImage(), Base64.DEFAULT);
                img.setImageBitmap(BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length));
            }
            else {
                //markerCheck.add(marker);
                //Picasso.get().load(std_image).resize(300, 400).centerCrop().into(img, new InfoWindowRefresher(marker));
            }
        }

        if(infoWindowData.getDate().length() > 0){
            date_tv.setText(infoWindowData.getDate());
        }
        else{
            date_tv.setVisibility(View.INVISIBLE);
        }
        if(infoWindowData.getLocation().length() > 0){
            location_tv.setText(infoWindowData.getLocation());
        }
        else{
            location_tv.setVisibility(View.INVISIBLE);
        }

        return view;
    }


    private class InfoWindowRefresher implements Callback {
        private Marker markerToRefresh;

        private InfoWindowRefresher(Marker markerToRefresh) {
            this.markerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            markerToRefresh.showInfoWindow();
        }

        @Override
        public void onError(Exception e) {

        }
    }
}