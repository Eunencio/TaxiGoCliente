package tovele.eunencio.taxigocliente;

import android.app.AlertDialog;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import tovele.eunencio.taxigocliente.Common.Common;
import tovele.eunencio.taxigocliente.Model.Rate;

public class RateActivity extends AppCompatActivity {

    Button btnSubmmit;
    MaterialRatingBar ratingBar;
    MaterialEditText edtComent;

    FirebaseDatabase database;
    DatabaseReference rateDerailRef;
    DatabaseReference driverInformationRef;

    double ratingStars = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        database = FirebaseDatabase.getInstance();
        rateDerailRef = database.getReference(Common.rate_detail_tbl);
        driverInformationRef = database.getReference(Common.user_driver_tbl);

        //Int view

        btnSubmmit = (Button) findViewById(R.id.btnSubmit);
        ratingBar = (MaterialRatingBar) findViewById(R.id.ratingBar);
        edtComent = (MaterialEditText) findViewById(R.id.edtComment);

        //Event
        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStars = rating;
            }
        });

        btnSubmmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRateDetails(Common.driverId);
            }
        });
    }

    private void submitRateDetails(final String driverId) {
        final AlertDialog alertDialog = new SpotsDialog(this);
        alertDialog.show();

        Rate rate = new Rate();
        rate.setRates(String.valueOf(ratingStars));
        rate.setComments(edtComent.getText().toString());

        rateDerailRef.child(driverId)
                .push()
                .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        rateDerailRef.child(driverId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        double averageStars = 0.0;
                                        int count = 0;
                                        for(DataSnapshot postSnatshot: dataSnapshot.getChildren())
                                        {
                                            Rate rate = postSnatshot.getValue(Rate.class);
                                            averageStars+=Double.parseDouble(rate.getRates());
                                            count++;
                                        }
                                        double finalAverage = averageStars/count;
                                        DecimalFormat df = new DecimalFormat("#.#");
                                        String valueUpdate = df.format(finalAverage);

                                        Map<String, Object> driverUpdateRate = new HashMap<String, Object>();
                                        driverUpdateRate.put("rates", valueUpdate);

                                        driverInformationRef.child(Common.driverId)
                                                .updateChildren(driverUpdateRate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        alertDialog.dismiss();
                                                        Toast.makeText(RateActivity.this, "Obrigado pela avaliacao!", Toast.LENGTH_SHORT);
                                                        finish();
                                                    }
                                                })

                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        alertDialog.dismiss();
                                                        Toast.makeText(RateActivity.this, "can't write to Driver Information", Toast.LENGTH_SHORT);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.dismiss();
                        Toast.makeText(RateActivity.this, "Rate Failed", Toast.LENGTH_SHORT);
                    }
                });

    }
}
