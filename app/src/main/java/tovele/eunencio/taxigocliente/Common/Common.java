package tovele.eunencio.taxigocliente.Common;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tovele.eunencio.taxigocliente.Home;
import tovele.eunencio.taxigocliente.Model.DataMessage;
import tovele.eunencio.taxigocliente.Model.FCMResponse;
import tovele.eunencio.taxigocliente.Model.Rider;
import tovele.eunencio.taxigocliente.Model.Token;
import tovele.eunencio.taxigocliente.Remote.FCMClient;
import tovele.eunencio.taxigocliente.Remote.GoogleMapAPI;
import tovele.eunencio.taxigocliente.Remote.IFCMService;
import tovele.eunencio.taxigocliente.Remote.IGoogleAPI;

/**
 * Created by Eunencio Tovele on 8/31/2018.
 */

public class Common {

    public static final int PICK_IMAGE_REQUEST = 9999;
    public static boolean isDriverFound=false;
    public static String driverId = "";

    public static Location mLastLocation;

    public static Rider currendUser = new Rider();

    public static final String BROADCAST_DROP_OFF = "chegou";


    public static final String driver_tbl  = "Drivers";
    public static final String user_driver_tbl  = "DriversInformation";
    public static final String user_rider_tbl  = "RidersInformation";
    public static final String pickup_request_tbl  = "PickupRequest";
    public static final String token_tbl  = "Tokens";
    public static final  String rate_detail_tbl = "RateDetails";

    public static final String fcmURL = "https://fcm.googleapis.com";
    public static final String googleAPIUrl = "https://maps.googleapis.com";

    private static double base_fare = 2.55;
    private static double time_rate = 0.35;
    private static double distance_rate = 1.75;

    public static double getPrice(double km, int min)
    {
        return (base_fare+(time_rate*min)+(distance_rate*km));
    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static IGoogleAPI getGoogleService()
    {
        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }


    public static void sendRequestToDriver(String driverId, final IFCMService mService, final Context context, final Location currentLocation) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);//get token object from database with key

                            //Make raw Payload
                            String riderToken = FirebaseInstanceId.getInstance().getToken();


                            Map<String, String> content = new HashMap<>();
                            content.put("customer", riderToken);
                            content.put("lat", String.valueOf(currentLocation.getLatitude()));
                            content.put("lng", String.valueOf(currentLocation.getLongitude()));
                            DataMessage dataMessage = new DataMessage(token.getToken(), content);

                            mService.sendMessage(dataMessage)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if(response.body().success==1)
                                                Toast.makeText(context, "Request sent!",Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(context, "failend!",Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("Error", t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

}
