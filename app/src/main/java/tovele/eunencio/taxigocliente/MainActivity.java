package tovele.eunencio.taxigocliente;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import tovele.eunencio.taxigocliente.Common.Common;
import tovele.eunencio.taxigocliente.Model.Rider;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static tovele.eunencio.taxigocliente.R.id.activity_main;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnRegister;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    RelativeLayout rootLayout;

    private final static int PERMISSION=1000;

    TextView txt_forgot_pwd;



    @Override
    protected void attachBaseContext(Context newBase) {

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_main);

        //instancias de FireBase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_rider_tbl);
        rootLayout = (RelativeLayout) findViewById(activity_main);

        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        txt_forgot_pwd = (TextView)findViewById(R.id.txt_forgot_password);
        txt_forgot_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialogForgotPwd();
                return false;
            }
        });

        //Eventos
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });
    }

    private void showDialogForgotPwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("FORGOT PASSWORD");
        alertDialog.setMessage("Por favor Insira seu EMAIL");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_pwd, null);

        final MaterialEditText edtEmail = (MaterialEditText) forgot_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forgot_pwd_layout);

        //set button
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                final AlertDialog waitingDialog =  new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout, "O link do reset foi enviado", Snackbar.LENGTH_LONG )
                                        .show();

                             }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLayout, ""+e, Snackbar.LENGTH_LONG )
                                .show();
                    }
                });
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showLoginDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Login");
        dialog.setMessage("Por favor use o seu Email para o Login");

        LayoutInflater inflanter = LayoutInflater.from(this);
        View login_layout = inflanter.inflate(R.layout.layout_login, null);

        final MaterialEditText edtEmail = (MaterialEditText) login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = (MaterialEditText) login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);

        //setButton
        AlertDialog.Builder login = dialog.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                dialog.dismiss();

                final AlertDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Por favor insira seu Email", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Por favor insira sua Senha", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }

                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();

                                //fectch
                                FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Common.currendUser = dataSnapshot.getValue(Rider.class);

                                                waitingDialog.dismiss();

                                                Intent intent = new Intent(MainActivity.this, Home.class);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout, "Erro!", Snackbar.LENGTH_SHORT)
                                .show();

                        btnSignIn.setEnabled(true);
                    }
                });

            }
        });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Registro");
        dialog.setMessage("Por favor use o seu Email para o registro");

        LayoutInflater inflanter = LayoutInflater.from(this);
        View register_layout = inflanter.inflate(R.layout.layout_register, null);

        final MaterialEditText edtEmail = (MaterialEditText) register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = (MaterialEditText) register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = (MaterialEditText) register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText) register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        //setButton
        dialog.setPositiveButton("Registrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    if (TextUtils.isEmpty(edtEmail.getText().toString())){
                        Snackbar.make(rootLayout, "Por favor insira seu Email", Snackbar.LENGTH_SHORT)
                                .show();
                        return;

                    }

                    if (TextUtils.isEmpty(edtPassword.getText().toString())){
                        Snackbar.make(rootLayout, "Por favor insira sua Senha", Snackbar.LENGTH_SHORT)
                                .show();
                        return;

                    }
                    if (edtPassword.getText().toString().length()<6){
                        Snackbar.make(rootLayout, "Sua Senha deve ter mais de 5 digitos", Snackbar.LENGTH_SHORT)
                                .show();
                        return;

                    }


                    if (TextUtils.isEmpty(edtName.getText().toString())){
                        Snackbar.make(rootLayout, "Por favor insira seu Nome", Snackbar.LENGTH_SHORT)
                                .show();
                        return;

                    }

                    if (TextUtils.isEmpty(edtPhone.getText().toString())){
                        Snackbar.make(rootLayout, "Por favor insira seu Numero de Telefone", Snackbar.LENGTH_SHORT)
                                .show();
                        return;

                    }


                }


                //Registrar User
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //Save User to DB
                                Rider rider = new Rider();
                                rider.setEmail(edtEmail.getText().toString());
                                rider.setPassword(edtPassword.getText().toString());
                                rider.setName(edtName.getText().toString());
                                rider.setPhone(edtPhone.getText().toString());

                                rider.setAvatarUrl("");
                                rider.setRates("0");

                                //Use email to key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(rider)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Snackbar.make(rootLayout, "Cadastrado com Sucesso!", Snackbar.LENGTH_SHORT)
                                                        .show();
                                                return;


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout, "Erro ao Cadastrar!"+e.getMessage(), Snackbar.LENGTH_SHORT)
                                                        .show();
                                                return;
                                            }
                                        });


                            }
                        })

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout, "Erro ao Cadastrar!"+e.getMessage(), Snackbar.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                        });
            }
        });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
