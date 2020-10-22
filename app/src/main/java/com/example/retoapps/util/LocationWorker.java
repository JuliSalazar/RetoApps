package com.example.retoapps.util;

import com.example.retoapps.activity.MapsActivity;
import com.example.retoapps.model.Position;
import com.google.gson.Gson;

public class LocationWorker extends Thread {

    private MapsActivity ref;
    private Boolean isAlive;

    public LocationWorker(MapsActivity ref) {
        this.ref = ref;
        this.isAlive = true;
    }

    @Override
    public void run() {
        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
        Gson gson = new Gson();
        while (isAlive){
            delay(2000);
            //Put de nuestra posicion
            if(ref.getCurrentPosition()!=null){
                https.PUTrequest(Constants.BASEURL+"users/"+ref.getUsername()+"/location.json", gson.toJson(ref.getCurrentPosition()));
            }
        }

    }

    public void delay(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        this.isAlive = false;

    }
}
