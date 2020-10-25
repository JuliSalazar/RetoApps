package com.example.retoapps.util;

import android.util.Log;

import com.example.retoapps.activity.MapsActivity;
import com.example.retoapps.model.Hueco;
import com.example.retoapps.model.Position;
import com.example.retoapps.model.PositionContainer;
import com.example.retoapps.model.User;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class TrackHuecosWorker extends Thread {

    private MapsActivity ref;
    private Boolean isAlive;
    private ArrayList<Hueco> huecosArray;

    public TrackHuecosWorker(MapsActivity ref) {
        this.ref = ref;
        this.isAlive = true;
        this.huecosArray = new ArrayList<>();
    }

    @Override
    public void run() {
        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
        Gson gson = new Gson();
        while (isAlive){
            delay(3000);
            String json = https.GETrequest(Constants.BASEURL+"huecos.json");
                Type type = new TypeToken< HashMap<String, Hueco> >(){}.getType();
                HashMap<String, Hueco> huecos = gson.fromJson(json, type);
            huecosArray.clear();

            if(huecos != null){
                huecos.forEach((key,value)->{
                    double lat = value.getLat();
                    double lng = value.getLng();
                    Hueco h = new Hueco(value.getName(), value.getDirection(), value.isConfirmado(), lat, lng, key);
                    huecosArray.add(h);
                });
                ref.updateMarkersHuecos(huecosArray);
            }

        }
    }

    public void delay (long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Hueco> getHuecosArray() {
        return huecosArray;
    }

    public void setHuecosArray(ArrayList<Hueco> huecosArray) {
        this.huecosArray = huecosArray;
    }

    public void finish(){
        this.isAlive = false;
    }
}
