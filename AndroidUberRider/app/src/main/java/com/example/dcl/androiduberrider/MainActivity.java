package com.example.dcl.androiduberrider;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.dcl.androiduberrider.Common.Common;
import com.example.dcl.androiduberrider.Model.Rider;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class MainActivity extends AppCompatActivity {



     FirebaseAuth auth;
     FirebaseDatabase db;
     DatabaseReference riders;
     Button  sing_in,registation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        riders=db.getReference(Common.user_rider_tb1);

        sing_in=findViewById(R.id.rider_sing_in);
        registation=findViewById(R.id.rider_registation);

       /* if (FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)!=null){
            Toast.makeText(getApplicationContext(),"Key Id :: "+FirebaseAuth.getInstance().getCurrentUser().getIdToken(true),Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),"Firebase Auth ar not active in this project",Toast.LENGTH_LONG).show();
        }*/


        sing_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText email,password;
                final AlertDialog.Builder alert=new AlertDialog.Builder(MainActivity.this);
                View v= LayoutInflater.from(getApplicationContext()).inflate(R.layout.sing_in_design,null);
                email=v.findViewById(R.id.edt_email);
                password=v.findViewById(R.id.edt_password);
                alert.setView(v);
                alert.setPositiveButton("Sing In",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.isEmpty(email.getText().toString())||!TextUtils.isEmpty(password.getText().toString())){
                            riders.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(email.getText().toString())){
                                        riders.child(email.getText().toString()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Rider rider=dataSnapshot.getValue(Rider.class);
                                                Toast.makeText(getApplicationContext(),"Email ="+rider.getEmail()+"Password ="+rider.getPassword(),Toast.LENGTH_LONG).show();

                                                if (!password.getText().toString().equals(rider.getPassword())){
                                                   Toast.makeText(getApplicationContext(),"Password not match",Toast.LENGTH_LONG).show();
                                                }else {
                                                    Common.rider=rider;
                                                    startActivity(new Intent(MainActivity.this,Display.class));
                                                  // startActivity(new Intent(MainActivity.this,D.class));
                                                }



                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });







                                    }else {
                                        Toast.makeText(getApplicationContext(),"You are not registered",Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

               alert.show();
            }
        });

        registation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText email,password,name,number;
                final AlertDialog.Builder alrt=new AlertDialog.Builder(MainActivity.this);
                View v=LayoutInflater.from(getApplicationContext()).inflate(R.layout.registation_dialog,null);
                email=v.findViewById(R.id.edt_email);
                password=v.findViewById(R.id.edt_password);
                name=v.findViewById(R.id.edt_name);
                number=v.findViewById(R.id.edt_number);
                alrt.setView(v);

                alrt.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {




                       /*auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                           @Override
                           public void onSuccess(AuthResult authResult) {
                               Rider rider=new Rider();

                               rider.setEmail(email.getText().toString());
                               rider.setPassword(password.getText().toString());
                               rider.setPhone(number.getText().toString());
                               rider.setName(name.getText().toString());

                               riders.child(auth.getCurrentUser().getUid()).setValue(rider)
                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       Toast.makeText(getApplicationContext(),"Succefully Saved",Toast.LENGTH_LONG).show();
                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       Toast.makeText(getApplicationContext(),"failed to save data", Toast.LENGTH_LONG).show();
                                   }
                               });
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(getApplicationContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
                           }
                       });*/


                       Rider rider=new Rider();

                       rider.setEmail(email.getText().toString());
                       rider.setPassword(password.getText().toString());
                       rider.setPhone(number.getText().toString());
                       rider.setName(name.getText().toString());

                       riders.child(rider.getEmail().toString()).setValue(rider).addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void aVoid) {
                               Toast.makeText(getApplicationContext(),"Registation Succefully",Toast.LENGTH_LONG).show();
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(getApplicationContext(),"Registation Faild",Toast.LENGTH_LONG).show();

                           }
                       });

                       dialogInterface.dismiss();

                   }

               });

               alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.dismiss();
                   }
               });
    alrt.show();
            }

        });


    }


}
