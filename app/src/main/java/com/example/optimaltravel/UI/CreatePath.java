package com.example.optimaltravel.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.optimaltravel.R;
import com.example.optimaltravel.data.Route;
import com.example.optimaltravel.json.PlaceConverter;
import com.example.optimaltravel.repo.Repository;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CreatePath extends AppCompatActivity {
    public static List<String> keysList = new LinkedList<String>();
    public static HashMap<String, LatLng> keyMap = new HashMap<String, LatLng>();
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    Repository repository = new Repository();
    Route route = new Route();
    Intent browserIntent;
    ListView listView = null;
    @SuppressLint("ResourceType")
    ArrayAdapter<String> adapter;
    Button bCalculateRoutes;
    Button bAddStop;
    HashMap<String, String> map;
    List<String> pointNamesList = new LinkedList<String>();
    List<Double> currentLocation = new LinkedList<Double>();
    StringBuilder currentPlaceID = new StringBuilder();
    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        setContentView(R.layout.activity_create_path);
        sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);
        this.setFinishOnTouchOutside(false);
        listView = (ListView) findViewById(R.id.lvAddress);
        adapter = new ArrayAdapter<String>(this, R.id.lvAddress, pointNamesList);
        bAddStop = findViewById(R.id.bAddStop);
        bCalculateRoutes = findViewById(R.id.bCalculateRoute);
        // bShowMap = findViewById(R.id.btShowMap);
        listView.setAdapter(adapter);
        map = new HashMap<String, String>();
        getCurrentLocation();
        // Remove stop-point
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                pointNamesList.remove(position);
                adapter.notifyDataSetChanged();
                return true;
            }
        });


        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, pointNamesList);

        ListView listView = (ListView) findViewById(R.id.lvAddress);
        listView.setAdapter(adapter);
        PlaceConverter.mld.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEnable) {
                bAddStop.setEnabled(true);
                bCalculateRoutes.setEnabled(true);
                adapter.notifyDataSetChanged();
                route.setPoint(pointNamesList);
                repository.insertRoute(route);
                OpenInGoogleMaps();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("RestrictedApi")
    public void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationProviderClient;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //     Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 5);
        else {
            // Android version is lesser than 6.0 or the permission is already granted.
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLocation.clear();
                                currentLocation.add(location.getLatitude());
                                currentLocation.add(location.getLongitude());
                            }
                        }
                    });
        }
    }

    public void googleAutoComplete(View view) {
        String api = "AIzaSyBUPxQMO2iI0DS_WTeetlcND9mpWaUCyyY";
        Places.initialize(this, api);
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                map.put(place.getId(), place.getAddress());
                keyMap.put(place.getId(), place.getLatLng());
                keysList.add(place.getId());
                pointNamesList.add(place.getAddress());
                adapter.notifyDataSetChanged();
                Log.i("shit", "Place: " + place.getAddress() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("shit", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void runParse(View view) throws IOException, JSONException {
        if (pointNamesList.size() == 0) { // No stop points
            return;
        }
        bCalculateRoutes.setEnabled(false);
        bAddStop.setEnabled(false);
        new Thread() {
            public void run() {
                try {
                    PlaceConverter.placesListFromJson(map, currentPlaceID, currentLocation, pointNamesList, keysList);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void btShowMapClick(View view) {
        startActivity(new Intent(this, MapsActivity.class));
    }

    public void OpenInGoogleMaps() {
        // https://www.google.com/maps/dir/?api=1&origin=Afula&origin_place_id=ChIJ-zbFi8NTHBURSwqqD4dNEuM&destination=tel+aviv&destination_place_id=ChIJH3w7GaZMHRURkD-WwKJy-8E&waypoints=b|a|a&waypoint_place_ids=ChIJ1XXAkwRAHRURIj88VL6V2Sw|adasdasassad|dasdasdsad
        if (keysList.size() == 0)
            return;
        String origin = "https://www.google.com/maps/dir/?api=1&origin=GPS&origin_place_id=" + currentPlaceID.toString() + "&destination=GPS&destination_place_id=" + currentPlaceID.toString();
        String wayP = "&waypoints=";
        String wayPid = "&waypoint_place_ids=";

        for (int i = 0; i < keysList.size(); ++i) {
            wayP += "wp";
            wayPid += "" + keysList.get(i);
            if (i < keysList.size() - 1) { //if is not the last stop
                wayP += "|";
                wayPid += "|";
            }
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(origin + wayP + wayPid + "&travelmode=driving")));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keyMap.clear();
        keysList.clear();
    }

    public void logOut(View view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FirebaseAuth.getInstance().getUid(), false);
        editor.apply();
        finish();
    }

}