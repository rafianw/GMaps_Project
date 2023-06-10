package com.example.projectgmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private SearchView mapSearchView;

    private int currentMapType = GoogleMap.MAP_TYPE_NORMAL;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapSearchView = findViewById(R.id.mapSearch);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        Places.initialize(getApplicationContext(), "AIzaSyC0JQUisYAJH7cCGIBT05wBn7RlSKTqzyQ");

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    myMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageButton myLocationButton = findViewById(R.id.myLocationButton);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentLocation != null && myMap != null) {
                    LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            }
        });

        Button changeMapTypeButton = findViewById(R.id.changeMapTypeButton);
        changeMapTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentMapType) {
                    case GoogleMap.MAP_TYPE_NORMAL:
                        myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        currentMapType = GoogleMap.MAP_TYPE_SATELLITE;
                        break;
                    case GoogleMap.MAP_TYPE_SATELLITE:
                        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        currentMapType = GoogleMap.MAP_TYPE_HYBRID;
                        break;
                    case GoogleMap.MAP_TYPE_HYBRID:
                        myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        currentMapType = GoogleMap.MAP_TYPE_TERRAIN;
                        break;
                    case GoogleMap.MAP_TYPE_TERRAIN:
                        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        currentMapType = GoogleMap.MAP_TYPE_NORMAL;
                        break;
                }
            }
        });


    }

    private void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MainActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setOnMapClickListener(this);

        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);

        // Add a marker for your current location
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        myMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

        // Enable My Location Button and Compass
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(true);
        myMap.getUiSettings().setCompassEnabled(true);

        // Add nearby places
        PlacesClient placesClient = Places.createClient(this);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);

        // Perform a nearby search using the Places API
        LatLngBounds bounds = LatLngBounds.builder().include(currentLatLng).build();
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationRestriction(RectangularBounds.newInstance(bounds))
                .setOrigin(currentLatLng)
                .setCountries("ID")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FindAutocompletePredictionsResponse response = task.getResult();
                        List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                        for (AutocompletePrediction prediction : predictions) {
                            Log.i("TAG", "Place: " + prediction.getFullText(null));
                            Log.i("TAG", "Place ID: " + prediction.getPlaceId());
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("TAG", "Place not found: " + exception.getMessage());
                        }
                    }
                });

        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();

                Toast.makeText(MainActivity.this, "Latitude: " + latitude + "\nLongitude: " + longitude + "\nStreet Address: " + streetAddress, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0); // Get the street address
                double latitude = latLng.latitude;
                double longitude = latLng.longitude;

                // Display the information (you can use any UI element to display the information)
                Toast.makeText(MainActivity.this, "Latitude: " + latitude + "\nLongitude: " + longitude + "\nStreet Address: " + streetAddress, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}