package com.example.optimaltravel.json;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class JsonParser {

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static JSONObject getJson(List<Double> currentLocation, List<String> wayPoints) throws IOException {
        JSONObject json = null;
        String begin = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        int size = wayPoints.size();
        String origin = currentLocation.get(0)+","+currentLocation.get(1) + "&";
        String points = "destination=" +origin + "waypoints=optimize:true";
        for (int i = 0; i < size; ++i)
            points += "|place_id:" + wayPoints.get(i);
        String end = "&key=AIzaSyBUPxQMO2iI0DS_WTeetlcND9mpWaUCyyY";
        String link = begin + origin + points + end;
        InputStream is = new URL(link).openStream();
        // InputStream is = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=afula&destination=Tel-Aviv&key=AIzaSyBUPxQMO2iI0DS_WTeetlcND9mpWaUCyyY").openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = "";
            String line = rd.readLine();


            while (line != null && !line.equals("   ],")) {
                jsonText += line + "\n";
                line = rd.readLine();
            }
            json = new JSONObject(jsonText += "]}");


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
        return json;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}