package tovele.eunencio.taxigocliente;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tovele.eunencio.taxigocliente.Common.Common;
import tovele.eunencio.taxigocliente.Helper.CustomInfoWindow;
import tovele.eunencio.taxigocliente.Model.DataMessage;
import tovele.eunencio.taxigocliente.Model.FCMResponse;
import tovele.eunencio.taxigocliente.Model.Rider;
import tovele.eunencio.taxigocliente.Model.Token;
import tovele.eunencio.taxigocliente.Remote.IFCMService;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, ValueEventListener {

    SupportMapFragment mapFragment;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7000;

    private LocationRequest mlocationRequest;
    private GoogleApiClient mgoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTEVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mUserMarker, markerDestination;

    //Bottomsheet
    ImageView imgExpandable;
    BottomSheetClienteFragment mBottomSheet;
    Button btnPickupRequest;



    int radius = 1; //1km
    int distance = 1; //3km
    private static final int Limit = 3;


    //Send alert
    IFCMService mService;

    //presense system
    DatabaseReference driversAvailable;

    PlaceAutocompleteFragment place_location, place_destination;
    AutocompleteFilter typeFilter;

    String mPlaceLocation, mPlaceDestination;

    //new update Information
    CircleImageView imageAvatar;
    TextView txtRiderName, txtStars;

    //FireStorage to upload avatar
    FirebaseStorage storage;
    StorageReference storageReference;

    //vehicle type
    ImageView taxi, txopela;
    boolean isTxopela = true;
    private BroadcastReceiver mCancelBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Common.driverId="";
            Common.isDriverFound = false;

            btnPickupRequest.setText("Pickup Request");
            btnPickupRequest.setEnabled(false);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCancelBroadCast, new IntentFilter("cancel_request"));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCancelBroadCast, new IntentFilter(Common.BROADCAST_DROP_OFF));

        mService = Common.getFCMService();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //int Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //add find vieew for image avatar
        View navigationHeaderView = navigationView.getHeaderView(0);
        txtRiderName = navigationHeaderView.findViewById(R.id.txtRiderName);
        txtRiderName.setText(String.format("%s", Common.currendUser.getName()));
        txtStars = navigationHeaderView.findViewById(R.id.txtStars);
        txtStars.setText(String.format("%s", Common.currendUser.getRates()));
        imageAvatar = navigationHeaderView.findViewById(R.id.imageAvatar);

        taxi = (ImageView)findViewById(R.id.select_taxi);
        txopela = (ImageView)findViewById(R.id.select_txopela);

        txopela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isTxopela = true;
                if(isTxopela)
                {
                    txopela.setImageResource(R.drawable.txopela);
                    taxi.setImageResource(R.drawable.taxi);
                }else{
                    txopela.setImageResource(R.drawable.txopelaa);
                    taxi.setImageResource(R.drawable.taxii);
                }
                if(driversAvailable != null)
                    driversAvailable.removeEventListener(Home.this);
                driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(isTxopela?"Txopela":"Taxi");
                driversAvailable.addValueEventListener(Home.this);
                loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        });

        taxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isTxopela = false;
                if(isTxopela)
                {
                    txopela.setImageResource(R.drawable.txopela);
                    taxi.setImageResource(R.drawable.taxi);
                }else{
                    txopela.setImageResource(R.drawable.txopelaa);
                    taxi.setImageResource(R.drawable.taxii);
                }
                mMap.clear();
                if(driversAvailable != null)
                    driversAvailable.removeEventListener(Home.this);
                driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(isTxopela?"Txopela":"Taxi");
                driversAvailable.addValueEventListener(Home.this);
                loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        });

        //Load avatar
        if(Common.currendUser.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currendUser.getAvatarUrl()))
        {
            Picasso.with(this)
                    .load(Common.currendUser.getAvatarUrl())
                    .into(imageAvatar);
        }


        //map
       mapFragment = (SupportMapFragment) getSupportFragmentManager()
               .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        //geofire  Remover as duas linhas abaixo
       // drivers = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbt);
        //geoFire = new GeoFire(drivers);


        btnPickupRequest = (Button) findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Common.isDriverFound) {
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());

                }else {
                    Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(), Common.mLastLocation);
                    btnPickupRequest.setEnabled(false);
                }
            }
        });

        place_destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);
        place_location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);
        typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(3)
                    .build();

        //Event
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceLocation = place.getAddress().toString();
                //Remove old Marker
                mMap.clear();

                mUserMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                .title("Pickup Here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));


            }

            @Override
            public void onError(Status status) {

            }
        });
        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mPlaceDestination = place.getAddress().toString();

                //destination Marker
                mMap.addMarker(new MarkerOptions()
                                .position(place.getLatLng())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                                .title("Destino"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));

                //Show information in bottom
               BottomSheetClienteFragment mBottomSheet = BottomSheetClienteFragment.newInstance(mPlaceLocation, mPlaceDestination,false);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());

            }

            @Override
            public void onError(Status status) {

            }
        });

        setUpLocation();

       updateFirebaseToken();

    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }


    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

        if(mUserMarker.isVisible())
            mUserMarker.remove();

        // add new Marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                        .title("Pickup Here")
                        .snippet("")
                        .position(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUserMarker.showInfoWindow();

        btnPickupRequest.setText("Getting your Driver...");

        findDriver();
    }

    private void findDriver() {
        DatabaseReference driverLocation;
        if(isTxopela)
            driverLocation  = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("Txopela");
        else
            driverLocation  = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("Taxi");

        GeoFire gf = new GeoFire(driverLocation);

        final GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //if found driver
                if(!Common.isDriverFound)
                {
                    Common.isDriverFound = true;
                    Common.driverId=key;
                    btnPickupRequest.setText("CALL DRIVER");
                   // Toast.makeText(Home.this, ""+key, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                //if still not found driver, increase distance
                    if(!Common.isDriverFound && radius < Limit)
                    {
                        radius++;
                        findDriver();
                    }
                else {
                         if (Common.isDriverFound) {
                            Toast.makeText(Home.this, "Sem automovel disponivel por perto", Toast.LENGTH_SHORT).show();
                            btnPickupRequest.setText("REQUEST PICKUP");
                            geoQuery.removeAllListeners();
                        }
                    }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            //Request runTime Permission
            ActivityCompat.requestPermissions(this, new String[]{

                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE
            },MY_PERMISSION_REQUEST_CODE);
        }
        else {

                buildLocationCallBack();
                createLocationRequest();
                    displayLocation();
        }

    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size()-1);
                displayLocation();

            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED)
                {
                   setUpLocation();
                }
                break;
        }
    }

    private void createLocationRequest() {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTEVAL);
        mlocationRequest.setFastestInterval(FATEST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }


    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;


                if(Common.mLastLocation != null)
                {
                    //Create LATLONG from mL
                    LatLng center = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                    LatLng northSide = SphericalUtil.computeOffset(center, 100000,0);
                    LatLng southSide = SphericalUtil.computeOffset(center, 100000,180);

                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northSide)
                            .include(southSide)
                            .build();

                    place_location.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);

                    place_destination.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);


                    //presense system
                    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(isTxopela?"Txopela":"Taxi");
                    driversAvailable.addValueEventListener(Home.this);

                    final double latitude = Common.mLastLocation.getLatitude();
                    final double longetude = Common.mLastLocation.getLongitude();



                    loadAllAvailableDriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                    Log.d("Tovele", String.format("Sua Localizacao mudou: %f/%f", latitude, longetude));
                }
                else
                {
                    Log.d("Tovele", "Nao foi possivel obter a Localizacao");
                }

            }
        });

    }

    private void loadAllAvailableDriver(final LatLng location){

        //Adicionar Marcador
        mMap.clear();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(location)
                .title(String.format("Sua Localizacao")));

        //Mover a camera para esta posicao
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));




        //Load All Available Driver in Distance 3km
        DatabaseReference driverLocation;
        if(isTxopela)
            driverLocation  = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("Txopela");
        else
            driverLocation  = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("Taxi");

        GeoFire gf = new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                                .child(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Rider rider = dataSnapshot.getValue(Rider.class);

                                        if(isTxopela)
                                        {
                                            if(rider.getCarType().equals("Txopela"))
                                            {
                                                //driver to map
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(location.latitude, location.longitude))
                                                        .flat(true)
                                                        .title(rider.getName())
                                                        .snippet("Driver ID : "+dataSnapshot.getKey())
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                                            }else
                                            {
                                                if(rider.getCarType().equals("Taxi"))
                                                {
                                                    //driver to map
                                                    mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(location.latitude, location.longitude))
                                                            .flat(true)
                                                            .title(rider.getName())
                                                            .snippet("Driver ID : "+dataSnapshot.getKey())
                                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                                                }
                                            }
                                        }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(distance <= Limit)
                {
                    distance++;
                    loadAllAvailableDriver(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signOut) {
            // Handle the camera action
        } else if (id == R.id.nav_updateIformation) {
                showUpdateInformationDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showUpdateInformationDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
        dialog.setTitle("Update Information");
        dialog.setMessage("Por favor use o seu Email para o registro");

        LayoutInflater inflanter = LayoutInflater.from(this);
        final View update_info_layout = inflanter.inflate(R.layout.layout_update_information, null);

        final MaterialEditText edtName = (MaterialEditText) update_info_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText) update_info_layout.findViewById(R.id.edtPhone);
        final MaterialEditText imgAvatar = (MaterialEditText) update_info_layout.findViewById(R.id.imgAvatar);

        dialog.setView(update_info_layout);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImageUpload();
            }
        });

        dialog.setView(update_info_layout);

        //set Buttom
        dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                final AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();

                Map<String, Object> update = new HashMap<>();
                if(!TextUtils.isEmpty(name))
                    update.put("name", name);
                if(!TextUtils.isEmpty(phone))
                    update.put("phone", phone);

                //update
                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);
                riderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        waitingDialog.dismiss();
                        if(task.isSuccessful())
                            Toast.makeText(Home.this, "Information updated", Toast.LENGTH_SHORT);
                        else
                            Toast.makeText(Home.this, "Information wasn't updated", Toast.LENGTH_SHORT);
                    }
                });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    private void chooseImageUpload() {
        //start intent to choose image

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select Picture"),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null )
        {
            Uri saveUri = data.getData();
            if(saveUri != null)
            {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading...");
                progressDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("images/"+imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();


                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Map<String, Object> update = new HashMap<>();
                                        update.put("avatarUrl", uri.toString());

                                        DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);
                                        riderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                    Toast.makeText(Home.this, "Avatar was uploaded", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(Home.this, "Avatar wasn't updated", Toast.LENGTH_SHORT);
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                            }
                        })

                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded" +progress+"%");
                                }
                            });
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try{
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map)
            );
            if(isSuccess)
                Log.e("ERROR","Map Style load failed");
        }catch (Resources.NotFoundException ex){
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if(markerDestination != null)
                    markerDestination.remove();
                markerDestination = mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                                        .position(latLng)
                                        .title("Destino"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));


                //Show bottom sheet
                BottomSheetClienteFragment mBottomSheet = BottomSheetClienteFragment.newInstance(String.format("%f, %f", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                        String.format("%f, %f", latLng.latitude, latLng.longitude),
                        true);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());

            }
        });

        mMap.setOnInfoWindowClickListener(this);

        if(ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){

            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(mlocationRequest, locationCallback, Looper.myLooper());
    }



    @Override
    public void onInfoWindowClick(Marker marker) {

        if(!marker.getTitle().equals("Sua Localizacao"))
        {
           Intent intent = new Intent(Home.this, CallDriver.class);

           intent.putExtra("driverId", marker.getSnippet().replaceAll("\\D+",""));
           intent.putExtra("lat", Common.mLastLocation.getLatitude());
           intent.putExtra("lng", Common.mLastLocation.getLongitude());
           startActivity(intent);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        loadAllAvailableDriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCancelBroadCast);
        super.onDestroy();
    }
}
