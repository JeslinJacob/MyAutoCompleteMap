package com.example.blackmask.myautocompletemap;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener{

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40,-168),new LatLng(71,136)
    );
    private static final String TAG ="our error" ;
    private GoogleMap mMap;

    // we require GoogleAPIclient as a parameter when we try to locate the place using itts placeid
    private GoogleApiClient mGoogleApiClient;
    protected GeoDataClient mGeoDataClient;
    //this is a adapter for auto compleate text view available in developers page : just copy past the class
    PlaceAutocompleteAdapter autocompleteAdapter;
    // declairing the auto complete text view
    AutoCompleteTextView searchtxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);


       //initiating the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //defining the gooogleAPIClient which we require as a parameter to locate a place with the place id
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)

                //implement onConnectionfail listner if there is a error in this line and implement its methods
                .enableAutoManage(this, this)
                .build();


        //defining the Auto compleate dapter
        autocompleteAdapter =new PlaceAutocompleteAdapter(this,mGeoDataClient,LAT_LNG_BOUNDS,null);

        //declairing the auto completae search box
        searchtxt=(AutoCompleteTextView)findViewById(R.id.searchbox);

        // seeting the adapter for the auto compleate search box
        searchtxt.setAdapter(autocompleteAdapter);


//        // ONLY APPLICABLE IF U ARE TRYING TO SEARCH WITH EDIT TEXT ALONE
//        searchtxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if (actionId== EditorInfo.IME_ACTION_SEARCH
//                        ||actionId==EditorInfo.IME_ACTION_DONE
//                        ||keyEvent.getAction()==keyEvent.ACTION_DOWN
//                        ||keyEvent.getAction()==keyEvent.KEYCODE_ENTER){
//
//                    //execute our method for search
//                    geoLocate();
//                }
//                return false;
//            }
//        });
//        // ONLY APPLICABLE IF U ARE TRYING TO SEARCH WITH EDIT TEXT ALONE ( TILL THIS LINE )

        searchtxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

                final AutocompletePrediction item = autocompleteAdapter.getItem(position);
                final String placeId = item.getPlaceId();

                Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place myPlace = places.get(0);
                                    LatLng queriedLocation = myPlace.getLatLng();



                                    MarkerOptions options=new MarkerOptions()
                                            .position(queriedLocation)
                                            ;
                                    mMap.addMarker(options);

                                    CameraUpdate center=CameraUpdateFactory.newLatLng(queriedLocation);
                                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(7);
                                    mMap.moveCamera(center);
                                    mMap.animateCamera(zoom);



                                }
                                places.release();
                            }
                        });

//                Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
//                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
//                            @Override
//                            public void onResult(PlaceBuffer places) {
//                                if (places.getStatus().isSuccess() && places.getCount() > 0) {
//                                    final Place myPlace = places.get(0);
//                                    Log.i("1", "Place found: " + myPlace.getName());
//                                    LatLng latlangObj = myPlace.getLatLng();
//                                    Log.i("3", "Place longitude: " + latlangObj.longitude);
//
//                                } else {
//                                    Log.e("2", "Place not found");
//                                }
//                                places.release();
//                            }
//                        });

                final CharSequence primaryText = item.getPrimaryText(null);

                Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                        Toast.LENGTH_SHORT).show();
                hidesoftkeybord();

            }
        });

    }

//    // ONLY APPLICABLE IF U ARE TRYING TO SEARCH WITH EDIT TEXT ALONE
//    public void geoLocate()
//    {
//        String searchString = searchtxt.getText().toString().trim();
//
//        Geocoder geocoder = new Geocoder(getApplicationContext());
//        List<Address> list = new ArrayList<>();
//        try {
//                list=geocoder.getFromLocationName(searchString,1);
//        }catch (IOException e)
//        {
//            Log.e(TAG,"geolocate : IOexeption"+e.getMessage());
//        }
//        if (list.size()>0){
//            Address address=list.get(0);
//            Log.i(TAG,"our searced address is : "+address.toString());
//
//            Double Lat =address.getLatitude();
//            Double Lng=address.getLongitude();
////
//            LatLng searchplace = new LatLng(Lat, Lng);
//
//
//
//            CameraUpdate center=CameraUpdateFactory.newLatLng(searchplace);
//            CameraUpdate zoom=CameraUpdateFactory.zoomTo(5);
//
//
//
//            mMap.moveCamera(center);
//            mMap.animateCamera(zoom);
//
//////            mMap.addMarker(new MarkerOptions().position(searchplace).title(""+list.get(0)));
////            mMap.moveCamera(CameraUpdateFactory.newLatLng(searchplace));
////
////
////            MarkerOptions options=new MarkerOptions()
////                    .position(searchplace)
////                    .title(list.get(0).toString());
////            mMap.addMarker(options);
////
////            hidesoftkeybord();
//        }
//    }
//    // ONLY APPLICABLE IF U ARE TRYING TO SEARCH WITH EDIT TEXT ALONE


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "error: " ,
                Toast.LENGTH_SHORT).show();
    }

    private void hidesoftkeybord(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
