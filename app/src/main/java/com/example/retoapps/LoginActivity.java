package com.example.retoapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.retoapps.model.User;
import com.example.retoapps.util.Constants;
import com.example.retoapps.util.HTTPSWebUtilDomi;
import com.google.gson.Gson;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private EditText userEt;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEt = findViewById(R.id.edtusername);
        loginBtn = findViewById(R.id.sendbtn);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION},1
        );

        loginBtn.setOnClickListener(
                (v) -> {
                    String username = userEt.getText().toString();
                    if(username != ""){
                        User user = new User(username, UUID.randomUUID().toString());
                        Gson gson = new Gson();
                        String json = gson.toJson(user);

                        HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();
                        new Thread(
                                ()->{
                                    String response = https.PUTrequest(Constants.BASEURL+"users/"+user.getName()+".json", json);
                                }
                        ).start();

                        Intent i = new Intent(this, MapsActivity.class);
                        i.putExtra("username", username);
                        startActivity(i);
                    }else{
                        Toast.makeText(this, "Ingresa un nombre de usuario",Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}