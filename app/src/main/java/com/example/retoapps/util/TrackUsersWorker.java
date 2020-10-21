package com.example.retoapps.util;

import com.example.retoapps.activity.MapsActivity;
import com.example.retoapps.model.Position;
import com.example.retoapps.model.PositionContainer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class TrackUsersWorker extends Thread {

    private MapsActivity ref;
    private Boolean isAlive;

    public TrackUsersWorker(MapsActivity ref) {
        this.ref = ref;
        this.isAlive = true;
    }

    @Override
    public void run() {
        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
        Gson gson = new Gson();
        while (isAlive){
            delay(5000);
            String json = https.GETrequest(Constants.BASEURL+"users.json");
            Type type = new TypeToken<HashMap<String, PositionContainer>>(){}.getType();
            HashMap<String, PositionContainer> users = gson.fromJson(json, type);
            ArrayList<Position> positions = new ArrayList<>();
            users.forEach((key,value)->{
                PositionContainer positionContainer = value;
                double lat = positionContainer.getLocation().getLat();
                double lng = positionContainer.getLocation().getLng();
                positions.add(new Position(lat,lng));
            });
            ref.updateMarkers(positions);
        }
    }

    public void delay (long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void finish(){
        this.isAlive = false;
    }
}
