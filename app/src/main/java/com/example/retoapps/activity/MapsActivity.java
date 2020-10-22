package com.example.retoapps.activity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retoapps.model.Position;
import com.example.retoapps.model.User;
import com.example.retoapps.util.LocationWorker;
import com.example.retoapps.R;
import com.example.retoapps.model.Hueco;
import com.example.retoapps.util.Constants;
import com.example.retoapps.util.HTTPSWebUtilDomi;
import com.example.retoapps.util.TrackUsersWorker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private ArrayList<Marker> markerUsers;
    private Button addBtn;
    private TextView advText;

    private AlertDialog.Builder dialogB;
    private AlertDialog dialog;



    private Button send;


    //Modelar lugares
    private ArrayList<Polygon> huecos;

    private LocationWorker locationWorker;
    private Position currentPosition;
    private TrackUsersWorker usersWorker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        username = getIntent().getExtras().getString("username");
        markerUsers = new ArrayList<>();
        huecos = new ArrayList<>();
        addBtn = findViewById(R.id.addBtn);
        //send = findViewById(R.id.sendBtn);
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
        mMap.setMyLocationEnabled(true);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 2, this);
        setInitialPos();
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        if(markerUsers !=null){
            for (int i= 0; i<markerUsers.size(); i++) {
                Marker u = markerUsers.get(i);
                u.showInfoWindow();
            }
        }

        if(huecos !=null){
            for (int i= 0; i<huecos.size(); i++) {
                Polygon u = huecos.get(i);
                //u.
            }
        }
        addBtn.setOnClickListener(
                (v)->{
                    popUp();
                }

        );




         locationWorker = new LocationWorker(this);
         locationWorker.start();

         usersWorker = new TrackUsersWorker(this);
         usersWorker.start();

    }
    public void popUp(){
        dialogB = new AlertDialog.Builder(this);
        final View popUpView = getLayoutInflater().inflate(R.layout.popup,null);
        send = popUpView.findViewById(R.id.sendBtn);
        dialogB.setView(popUpView);
        dialog = dialogB.create();
        dialog.show();

        send.setOnClickListener(
                (v)->{
                    Gson gson = new Gson();
                    HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
                    double space = 0.00002;
                    if(me !=null){
                        me.getTitle();
                        me.showInfoWindow();
                        Hueco hueco = new Hueco(UUID.randomUUID().toString(), "una dirección", me.getPosition().latitude, me.getPosition().latitude, "No confirmado");
                        String json = gson.toJson(hueco);
                        huecos.add(
                                mMap.addPolygon(
                                        new PolygonOptions()
                                                .add(new LatLng(me.getPosition().latitude + space, me.getPosition().longitude - space))
                                                .add(new LatLng(me.getPosition().latitude + space, me.getPosition().longitude + space))
                                                .add(new LatLng(me.getPosition().latitude - space, me.getPosition().longitude + space))
                                                .add(new LatLng(me.getPosition().latitude - space, me.getPosition().longitude - space))
                                                .add(new LatLng(me.getPosition().latitude + space, me.getPosition().longitude - space))
                                                .fillColor(Color.argb(10,255,0,0))
                                                .strokeColor(Color.BLACK)
                                )
                        );

                        new Thread(
                                ()->{
                                    String response = https.POSTrequest(Constants.BASEURL+"huecos/"+ hueco.getId()+".json", json);
                                }
                        ).start();
                    }

                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me.getPosition(), 1));
                    dialog.dismiss();
                }
        );
    }

    @Override
    protected void onDestroy() {
        locationWorker.finish();
        usersWorker.finish();
        super.onDestroy();
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
        if(huecos !=null){
            for (int i= 0; i<huecos.size(); i++) {
                Polygon h = huecos.get(i);
                boolean iamAtHueco = PolyUtil.containsLocation(new LatLng(location.getLatitude(),location.getLongitude()), h.getPoints(), false);
                if(iamAtHueco){
                    Log.e("AQUI", "ESTOY DENTRO DE UN HUECO");
                    //addBtn.setText("Adentro");
                }
            }
        }



    }
    public void updateMyLocation(Location location) {
        LatLng myPos = new LatLng(location.getLatitude(),location.getLongitude());
        /*if(me == null){
            me =  mMap.addMarker(new MarkerOptions().position(myPos).title("Yo"));
            me.showInfoWindow();
        }else{
            me.setPosition(myPos);
        }*/
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos, 19));
        computeDistances();


        currentPosition = new Position(location.getLatitude(),location.getLongitude());

    }

    private void computeDistances() {
       /* for(int i=0; i <points.size(); i++){
            Marker marker = points.get(i);
            LatLng markerLoc = marker.getPosition();
           // LatLng meLoc = me.getPosition();

            double meters = SphericalUtil.computeDistanceBetween(markerLoc,meLoc);
            if(meters<50){
                addBtn.setText("Usted está pisando un marcador");
            }
        }*/
        if(huecos != null){
            double distanceToHueco = 100000;
            for (int i=0; i<huecos.size(); i++){
                Polygon hueco = huecos.get(i);
                for (int j=0; j<hueco.getPoints().size(); j++) {
                    LatLng punto = hueco.getPoints().get(i);
                    double meters = SphericalUtil.computeDistanceBetween(punto, me.getPosition());
                    distanceToHueco = Math.min(meters, distanceToHueco);
                }
            }
            advText.setText("Hueco a "+(int) distanceToHueco+" metros");
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
        //Marker p =  mMap.addMarker(new MarkerOptions().position(latLng).title("Marcador").snippet("subtitulo"));
        //points.add(p);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Toast.makeText(this,marker.getPosition().latitude+","+marker.getPosition().longitude, Toast.LENGTH_LONG).show();
        //Log.e(">>>",marker.getPosition().latitude+","+marker.getPosition().longitude);

        return false;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public String getUsername() {
        return username;
    }

    public void updateMarkers(ArrayList<User> usersArray){
        runOnUiThread(
                ()->{

                    for (int i= 0; i<markerUsers.size(); i++){
                        Marker m = markerUsers.get(i);
                        m.remove();
                    }
                    markerUsers.clear();

                    for (int i= 0; i<usersArray.size(); i++){
                        User u = usersArray.get(i);
                        Position pos = u.getContainer().getLocation();
                        LatLng latLng = new LatLng(pos.getLat(), pos.getLng());
                        if(u.getName().equals(username)){
                            me = mMap.addMarker(new MarkerOptions().position(latLng).title("Yo" + u.getName()));
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