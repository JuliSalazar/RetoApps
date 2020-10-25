package com.example.retoapps.util;

import android.util.Log;

import com.example.retoapps.activity.MapsActivity;
import com.example.retoapps.model.Position;
import com.example.retoapps.model.PositionContainer;
import com.example.retoapps.model.User;
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
            delay(3000);
            String json = https.GETrequest(Constants.BASEURL+"users.json");
                Type type = new TypeToken< HashMap<String, PositionContainer> >(){}.getType();
                HashMap<String, PositionContainer> users = gson.fromJson(json, type);
            if(users != null) {
                ArrayList<User> usersArray = new ArrayList<>();
                users.forEach((key, value) -> {
                    PositionContainer positionContainer = value;
                    User user = new User(key, positionContainer);
                    double lat = user.getContainer().getLocation().getLat();
                    double lng = user.getContainer().getLocation().getLng();
                    user.getContainer().setLocation(new Position(lat, lng));
                    usersArray.add(user);
                });
                ref.updateMarkers(usersArray);
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

    public void finish(){
        this.isAlive = false;
    }
}
