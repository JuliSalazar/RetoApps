package com.example.retoapps;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retoapps.model.Hueco;
import com.example.retoapps.util.Constants;
import com.example.retoapps.util.HTTPSWebUtilDomi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;


import java.util.ArrayList;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private String username;
    private LocationManager manager;
    private Marker me;
    private ArrayList<Marker> points;
    private Button addBtn;
    private TextView advText;
    //Modelar lugares
    private Polygon hueco;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        username = getIntent().getExtras().getString("username");
        points = new ArrayList<>();
        addBtn = findViewById(R.id.addBtn);
        advText = findViewById(R.id.adviceText);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 2, this);
        setInitialPos();
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        addBtn.setOnClickListener(
                (v)->{
                    Hueco hueco = new Hueco(UUID.randomUUID().toString(), "una dirección", "lati", "longi", username);

                    Gson gson = new Gson();
                    String json = gson.toJson(hueco);
                    HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();

                    new Thread(
                            ()->{
                                String response = https.POSTrequest(Constants.BASEURL+"huecos/"+ hueco.getUsername()+".json", json);
                            }
                    );
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me.getPosition(), 1));
                }
        );

        hueco = mMap.addPolygon(
                new PolygonOptions()
                        .add(new LatLng(3.470278833189764,-76.49836029857397))
                        .add(new LatLng(3.4703517893594515,-76.49831973016262))
                        .add(new LatLng(3.4702681240267643,-76.49823322892188))
                        .add(new LatLng(3.470278833189764,-76.49836029857397))
                        .fillColor(Color.argb(10,255,0,0))
                        .strokeColor(Color.BLACK)

        );


    }
    @SuppressLint("MissingPermission")
    public void setInitialPos(){
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null){
            updateMyLocation(location);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateMyLocation(location);

        boolean iamAtHueco = PolyUtil.containsLocation(new LatLng(location.getLatitude(),location.getLongitude()), hueco.getPoints(), false);
        if(iamAtHueco){
            addBtn.setText("Adentro");
        }
    }
    public void updateMyLocation(Location location) {
        LatLng myPos = new LatLng(location.getLatitude(),location.getLongitude());
        if(me == null){
            me =  mMap.addMarker(new MarkerOptions().position(myPos).title("Yo"));
        }else{
            me.setPosition(myPos);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos, 15));
        computeDistances();
    }

    private void computeDistances() {
        for(int i=0; i <points.size(); i++){
            Marker marker = points.get(i);
            LatLng markerLoc = marker.getPosition();
            LatLng meLoc = me.getPosition();

            double meters = SphericalUtil.computeDistanceBetween(markerLoc,meLoc);
            if(meters<50){
                addBtn.setText("Usted está pisando un marcador");
            }
        }
        if(hueco != null){
            double distanceToHueco = 100000;
            for (int i=0; i<hueco.getPoints().size(); i++){
                LatLng punto = hueco.getPoints().get(i);
                double meters = SphericalUtil.computeDistanceBetween(punto,me.getPosition());
                distanceToHueco = Math.min(meters,distanceToHueco);
            }
            advText.setText("Hueco a "+distanceToHueco+" metros");
        }

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

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker p =  mMap.addMarker(new MarkerOptions().position(latLng).title("Marcador").snippet("subtitulo"));
        points.add(p);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this,marker.getPosition().latitude+","+marker.getPosition().longitude, Toast.LENGTH_LONG).show();
        Log.e(">>>",marker.getPosition().latitude+","+marker.getPosition().longitude);
        marker.showInfoWindow();
        return false;
    }
}