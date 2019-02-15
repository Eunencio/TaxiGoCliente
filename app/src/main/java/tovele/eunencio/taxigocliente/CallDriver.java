package tovele.eunencio.taxigocliente;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import tovele.eunencio.taxigocliente.Common.Common;
import tovele.eunencio.taxigocliente.Model.Rider;
import tovele.eunencio.taxigocliente.Remote.IFCMService;

public class CallDriver extends AppCompatActivity {

    CircleImageView avatar_image;
    TextView txt_name, txt_phone, txt_rate;
    Button btn_call_driver, btn_call_driver_phone;

    String driverId;
    Location mLastLocation;

    IFCMService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);

        mService = Common.getFCMService();

        avatar_image = (CircleImageView) findViewById(R.id.avatar_image);
        txt_name = (TextView)findViewById(R.id.txt_name);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_rate = (TextView) findViewById(R.id.txt_rate);
        btn_call_driver = (Button) findViewById(R.id.btn_call_driver);
        btn_call_driver_phone = (Button) findViewById(R.id.btn_call_driver_phone);

        btn_call_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(driverId != null && !driverId.isEmpty())
                {
                    Common.sendRequestToDriver(driverId, mService, getBaseContext(), mLastLocation);
                }
            }
        });

        btn_call_driver_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+txt_phone.getText().toString()));

                if(ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }

                    startActivity(intent);
            }
        });

        if(getIntent() != null)
        {
            driverId = getIntent().getStringExtra("driverId");
            double lat = getIntent().getDoubleExtra("lat", -1.0);
            double lng = getIntent().getDoubleExtra("lng", -1.0);

            mLastLocation = new Location("");
            mLastLocation.setLatitude(lat);
            mLastLocation.setLongitude(lng);

            loadDriverInfo(driverId);
        }
    }

    private void loadDriverInfo(String driverId) {
        FirebaseDatabase.getInstance()
                .getReference(Common.user_driver_tbl)
                .child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Rider driverUser = dataSnapshot.getValue(Rider.class);

                        if(!driverUser.getAvatarUrl().isEmpty())
                        {
                            Picasso.with(getBaseContext())
                                    .load(driverUser.getAvatarUrl())
                                    .into(avatar_image);
                        }
                        txt_name.setText(driverUser.getName());
                        txt_phone.setText(driverUser.getPhone());
                        txt_rate.setText(driverUser.getRates());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
