package com.example.optimaltravel.repo;

import com.example.optimaltravel.data.Route;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Repository {
    DatabaseReference userRoutes;
    String  userId=FirebaseAuth.getInstance().getUid();
    String routeId=null;
    Route route;

    public void insertRoute(Route route){
         userRoutes  =
                FirebaseDatabase.getInstance().getReference("Users/"+userId);
        routeId=userRoutes.push().getKey();
        if (routeId == null)
            return;
        route.setRouteID(routeId);
        DatabaseReference requestRoutes=  FirebaseDatabase.getInstance().getReference("Users/"+userId+"/"+routeId);
        requestRoutes.setValue(route);
    }
}
