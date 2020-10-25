package com.example.retoapps.activity;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.retoapps.model.Position;
import com.example.retoapps.model.User;
import com.example.retoapps.util.LocationWorker;
import com.example.retoapps.R;
import com.example.retoapps.model.Hueco;
import com.example.retoapps.util.Constants;
import com.example.retoapps.util.HTTPSWebUtilDomi;
import com.example.retoapps.util.TrackHuecosWorker;
import com.example.retoapps.util.TrackUsersWorker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private String username;
    private LocationManager manager;
    private Marker me;
    private ArrayList<Marker> markerUsers;
    private ArrayList<Marker> markerHuecos;
    private ArrayList<Hueco> huecosArray;
    private Button addBtn;
    private TextView advText;

    private AlertDialog.Builder dialogB;
    private AlertDialog dialog;

    private TextView coord;
    private TextView direc;

    private boolean paraConfirm;

    private Button send;


    //Modelar lugares
    //private ArrayList<Polygon> huecos;

    private LocationWorker locationWorker;
    private Position currentPosition;
    private TrackUsersWorker usersWorker;
    private TrackHuecosWorker huecosWorker;

    private Hueco huecoProv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        username = getIntent().getExtras().getString("username");
        markerUsers = new ArrayList<>();
        markerHuecos = new ArrayList<>();
        huecosArray = new ArrayList<>();
        addBtn = findViewById(R.id.addBtn);
        advText = findViewById(R.id.adviceText);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        paraConfirm = false;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMyLocationEnabled(true);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 2, this);
        setInitialPos();
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        huecosWorker = new TrackHuecosWorker(this);
        huecosWorker.start();

        locationWorker = new LocationWorker(this);
        locationWorker.start();

        usersWorker = new TrackUsersWorker(this);
        usersWorker.start();

        addBtn.setOnClickListener(
                (v)->{
                    if(paraConfirm){
                        Gson gson = new Gson();
                        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
                        huecoProv.setConfirmado(true);
                        String json = gson.toJson(huecoProv);
                        new Thread(
                                ()->{
                                    String response = https.PUTrequest(Constants.BASEURL+"huecos/"+ huecoProv.getId() +".json", json);
                                }
                        ).start();
                    }else {
                        popUp();
                    }
                }
        );
    }
    public void popUp(){
        dialogB = new AlertDialog.Builder(this);
        final View popUpView = getLayoutInflater().inflate(R.layout.popup,null);
        send = popUpView.findViewById(R.id.sendBtn);
        dialogB.setView(popUpView);
        dialog = dialogB.create();
        dialog.show();

        coord = popUpView.findViewById(R.id.coord);
        direc = popUpView.findViewById(R.id.direct);

        coord.setText(me.getPosition().latitude + ", " + me.getPosition().longitude);
        String direc = getCityName(new LatLng(me.getPosition().latitude , me.getPosition().longitude ));


        send.setOnClickListener(
                (v)->{
                    Gson gson = new Gson();
                    HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
                    if(me !=null){
                        me.getTitle();
                        me.showInfoWindow();
                        Position pos = new Position(me.getPosition().latitude, me.getPosition().longitude);
                        Hueco hueco = new Hueco(username, direc, false, pos.getLat(), pos.getLng(), UUID.randomUUID().toString());
                        String json = gson.toJson(hueco);
                        new Thread(
                                ()->{
                                    String response = https.PUTrequest(Constants.BASEURL+"huecos/"+ hueco.getId() +".json", json);
                                }
                        ).start();
                    }
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me.getPosition(), 1));
                    dialog.dismiss();
                }
        );
    }

    public String getCityName(LatLng coordenadas){
        String direccion = "";
        Geocoder geocoder = new Geocoder( this, Locale.getDefault());
        try {
            List<Address> directions = geocoder.getFromLocation(me.getPosition().latitude,me.getPosition().longitude,  1);
             direccion = directions.get(0).getAddressLine(0);
            direc.setText(direccion);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return direccion;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(
                ()->{
                    HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
                    usersWorker.finish();
                    locationWorker.finish();
                    try {
                        Thread.sleep(3000);
                        String response = https.DELETErequest(Constants.BASEURL+"users/"+ username +".json");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        ).start();
        huecosWorker.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("MissingPermission")
    public void setInitialPos(){
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null){
            updateMyLocation(location);
        }
    }
    @SuppressLint("ResourceAsColor")
    private void options(Hueco hue, double min){
        if ((hue.getName().compareTo(username)) != 0) {
            confimarHueco(hue);
            if (hue.isConfirmado()) {
                addBtn.setTextColor(getResources().getColor(R.color.colorEnable,getTheme()));
                addBtn.setEnabled(false);
                addBtn.setText("Hueco confirmado");
                paraConfirm = false;
            }
        } else {
            addBtn.setTextColor(getResources().getColor(R.color.colorEnable,getTheme()));
            addBtn.setEnabled(false);
            addBtn.setText("Reportado por ti");
            paraConfirm = false;
        }
    }
    @SuppressLint("ResourceAsColor")
    private void computeDistances() {
        if (markerHuecos != null) {
            double distanceToHueco = 100000000;
            for (int i = 0; i < markerHuecos.size(); i++) {

                Hueco hue = huecosWorker.getHuecosArray().get(i);
                LatLng huecoLoc = new LatLng(hue.getLat(),hue.getLng());
                LatLng meLoc = me.getPosition();
                double meters = SphericalUtil.computeDistanceBetween(huecoLoc, meLoc);

                distanceToHueco = Math.min(meters, distanceToHueco);
                advText.setText("Hueco a " + (int) distanceToHueco + " metros");

                if(distanceToHueco <5){
                    options(hue,distanceToHueco);
                    return;
                }else {
                    addBtn.setTextColor(getResources().getColor(R.color.colorWhite,getTheme()));
                    addBtn.setEnabled(true);
                    addBtn.setText("Reportar hueco");
                    paraConfirm = false;
                }
            }
        }
    }
    @SuppressLint("ResourceAsColor")
    public void confimarHueco(Hueco h){
        addBtn.setTextColor(getResources().getColor(R.color.colorWhite,getTheme()));
        addBtn.setEnabled(true);
        addBtn.setText("Confirmar Hueco");
        paraConfirm = true;
        huecoProv = h;
    }

    @Override
    public void onLocationChanged(Location location) {
        updateMyLocation(location);


    }

    public void updateMyLocation(Location location) {
        LatLng myPos = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos, 19));
        currentPosition = new Position(location.getLatitude(),location.getLongitude());
        computeDistances();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onProviderDisabled(String provider) { }
    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {}

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public String getUsername() {
        return username;
    }
    public void updateMarkersHuecos(ArrayList<Hueco> huecosA){
        runOnUiThread(
                ()->{
                        for (int i= 0; i<markerHuecos.size(); i++){
                            Marker m = markerHuecos.get(i);
                            m.remove();
                        }
                        markerHuecos.clear();

                        for (int i= 0; i<huecosA.size(); i++){
                            Hueco h = huecosA.get(i);
                            LatLng latLng = new LatLng(h.getLat(), h.getLng());
                            Marker m = null;
                            if (h.isConfirmado() == true) {
                                m = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.huecosi)));
                            } else if (h.isConfirmado() == false) {
                                m = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.huecono)));
                            }
                            markerHuecos.add(m);
                        }
                    }

        );

    }

    public void updateMarkers(ArrayList<User> usersA){
        runOnUiThread(
                ()->{
                    for (int i= 0; i<markerUsers.size(); i++){
                        Marker m = markerUsers.get(i);
                        m.remove();
                    }
                    markerUsers.clear();

                    for (int i= 0; i<usersA.size(); i++){
                        User u = usersA.get(i);
                        Position pos = u.getContainer().getLocation();
                        LatLng latLng = new LatLng(pos.getLat(), pos.getLng());
                        if(u.getName().equals(username)){
                            me = mMap.addMarker(new MarkerOptions().position(latLng).title("Yo " + u.getName()));
                            markerUsers.add(me);
                        }else{
                            Marker m = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .title(u.getName()));
                            markerUsers.add(m);
                        }

                    }

                }
        );

    }
}