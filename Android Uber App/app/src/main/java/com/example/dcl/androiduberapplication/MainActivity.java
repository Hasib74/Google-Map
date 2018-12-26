package com.example.dcl.androiduberapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dcl.androiduberapplication.Common.Common;
import com.example.dcl.androiduberapplication.Model.API;
import com.example.dcl.androiduberapplication.Model.User;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
     Button sing_in,registationBtn;

     FirebaseDatabase db;
     DatabaseReference users;
     TextView btn_forgetPassword;
    public  static  DatabaseReference api_key_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db=FirebaseDatabase.getInstance();
        users=db.getReference(Common.user_driver_tb1);


        sing_in=findViewById(R.id.sing_in);
        registationBtn=findViewById(R.id.registation);
        btn_forgetPassword=findViewById(R.id.forgetPassword);

       // Toast.makeText(this,auth.getCurrentUser().getUid().toString(),Toast.LENGTH_LONG).show();

        sing_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showLogInDialog();
            }
        });
        registationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showregistationDialog();
            }
        });
        btn_forgetPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
             /*   showDialogForgetPassword();*/


             Toast.makeText(getApplicationContext(),"Tecenically problem",Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

   /* private void showDialogForgetPassword() {
        AlertDialog.Builder alrt=new AlertDialog.Builder(this);
        alrt.setTitle("FORGET PASSWORD");
        alrt.setMessage("Please enter your email address");

        View v =LayoutInflater.from(this).inflate(R.layout.forget_passworddesign,null);
        alrt.setView(v);

        final EditText enter_email=v.findViewById(R.id.edit_email);

        alrt.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                auth.sendPasswordResetEmail(enter_email.getText().toString().trim())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Reset Password link is send  to your email",Toast.LENGTH_LONG).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed to send link",Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
        alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alrt.show();


    }*/

    private void showLogInDialog() {
        final EditText sing_in_email,sing_in_password;
        AlertDialog.Builder alrt=new AlertDialog.Builder(this);
        View v=LayoutInflater.from(this).inflate(R.layout.log_in_dialog_design,null);
        alrt.setView(v);
        sing_in_email=v.findViewById(R.id.sing_in_email);
        sing_in_password=v.findViewById(R.id.sing_in_password);


        alrt.setPositiveButton("Log In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                users.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(sing_in_email.getText().toString())){
                            users.child(sing_in_email.getText().toString()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user=dataSnapshot.getValue(User.class);
                                    Toast.makeText(getApplicationContext(),"Email ="+user.getEmail()+"Password ="+user.getPassword(),Toast.LENGTH_LONG).show();

                                    if (!sing_in_password.getText().toString().equals(user.getPassword())){
                                        Toast.makeText(getApplicationContext(),"Password not match",Toast.LENGTH_LONG).show();
                                    }else {
                                        Common.currentUser=user;
                                       startActivity(new Intent(MainActivity.this,Welcome.class));
                                       // startActivity(new Intent(MainActivity.this,MapTest.class));
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


                api_key_ref=FirebaseDatabase.getInstance().getReference("API");

                api_key_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        API api=dataSnapshot.getValue(API.class);

                        String API_KEY=api.getApi_key();

                        Common.currentApi=api;
                        Toast.makeText(getApplicationContext(),"KEY :="+API_KEY,Toast.LENGTH_LONG).show();


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });







      /*  alrt.setPositiveButton("Log In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                auth.signInWithEmailAndPassword(sing_in_email.getText().toString(),sing_in_password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                            //    AlertDialog waitingDialog=new SpotsDialog(MainActivity.this);
                               FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                                       .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                       .addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot) {
                                               Common.currentUser=dataSnapshot.getValue(User.class);

                                           }

                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {

                                           }
                                       });

                               startActivity(new Intent(MainActivity.this,Welcome.class));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                             Toast.makeText(getApplicationContext(),"Failed to Log In"+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });*/

        alrt.show();


    }

    private void showregistationDialog() {
        final EditText reg_email,reg_password,reg_name,reg_phone_number;
        AlertDialog.Builder alrt=new AlertDialog.Builder(this);
        View v= LayoutInflater.from(getApplicationContext()).inflate(R.layout.registation_design,null);
        alrt.setTitle("REGISTATION");
        alrt.setMessage("Please registation here if you are not member of this app");
        alrt.setView(v);
        reg_email=v.findViewById(R.id.reg_email);
        reg_password=v.findViewById(R.id.reg_password);
        reg_name=v.findViewById(R.id.reg_name);
        reg_phone_number=v.findViewById(R.id.reg_phone);



        alrt.setPositiveButton("Registation", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                final User user=new User();
                user.setEmail(reg_email.getText().toString());
                user.setPassword(reg_password.getText().toString());
                user.setName(reg_name.getText().toString());
                user.setPhone(reg_phone_number.getText().toString());

                users.child(reg_name.getText().toString())
                        .setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Successfully Register",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        alrt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });






       /* alrt.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            //    Toast.makeText(getApplicationContext(),""+reg_email.getText().toString()+""+reg_password.getText().toString(),Toast.LENGTH_LONG).show();
                auth.createUserWithEmailAndPassword(reg_email.getText().toString(),reg_password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user=new User();
                                user.setEmail(reg_email.getText().toString());
                                user.setPassword(reg_password.getText().toString());
                                user.setName(reg_name.getText().toString());
                                user.setPhone(reg_phone_number.getText().toString());


                                users.child(auth.getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(),"Registation successfully",Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(),"Registation Failed",Toast.LENGTH_LONG).show();

                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error_auth",e.getMessage());
                        Toast.makeText(getApplicationContext(),"Failed"+e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
        alrt.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });*/

        alrt.show();
    }
}
