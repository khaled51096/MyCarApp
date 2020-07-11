package com.example.carapp.Ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carapp.Pojo.DriversMapActivity;
import com.example.carapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginRegisterActivity extends AppCompatActivity {
    private Button driver_login;
    private Button driver_create;
    private Button driver_register;
    private TextView driv_stuts;
    private TextView have1;
    private EditText driver_email;
    private TextInputLayout driver_password;
    private FirebaseAuth mauth;
    private DatabaseReference driverdatabaseref;
    private String onlinedriverid;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        driver_login = (Button)findViewById(R.id.login_driver_btn);
        driver_create = (Button)findViewById(R.id.driver_register_btn);
        driver_register = (Button)findViewById(R.id.driver_register_btn2);
        driv_stuts = (TextView)findViewById(R.id.driver_stuts);
        have1 = (TextView)findViewById(R.id.driver_have);
        driver_email = (EditText)findViewById(R.id.driver_login_email);
        driver_password = (TextInputLayout)findViewById(R.id.driver_login_pass);

        mauth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);


        driver_register.setVisibility(View.INVISIBLE);
        driver_register.setEnabled(false);

        driver_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driver_create.setVisibility(View.INVISIBLE);
                driver_login.setVisibility(View.INVISIBLE);
                have1.setVisibility(View.INVISIBLE);
                driv_stuts.setText("Driver Register");
                driver_register.setVisibility(View.VISIBLE);
                driver_register.setEnabled(true);
            }
        });

        driver_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = driver_email.getText().toString();
                String password = driver_password.getEditText().getText().toString();

                RegisterDriver(email, password);
            }
        });
        driver_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = driver_email.getText().toString();
                String password = driver_password.getEditText().getText().toString();

                SigninDriver(email, password);
            }
        });
    }

    private void SigninDriver(String email, String password) {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(DriverLoginRegisterActivity.this,"please write your email",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(DriverLoginRegisterActivity.this,"please write your password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            dialog.setTitle("Driver Sign In");
            dialog.setMessage("please wait , while we are check your Auth");
            dialog.show();

            mauth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        Intent driverintent =new Intent (DriverLoginRegisterActivity.this, DriversMapActivity.class);
                        startActivity(driverintent);

                        Toast.makeText(DriverLoginRegisterActivity.this,"Driver Signing In done successfuly",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }
                    else
                    {
                        Toast.makeText(DriverLoginRegisterActivity.this,"Login Failed , Try Again",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                }
            });

        }

    }

    private void RegisterDriver(String email, String password) {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(DriverLoginRegisterActivity.this,"please write your email",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(DriverLoginRegisterActivity.this,"please write your password",Toast.LENGTH_SHORT).show();
        }
        else if (password.length()<6)
        {
            Toast.makeText(this, "Password length must br greater than 6 chars", Toast.LENGTH_SHORT).show();
        }
        else
        {
            dialog.setTitle("Driver Registration");
            dialog.setMessage("please wait , while we are register your data");
            dialog.show();

            mauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        onlinedriverid = mauth.getCurrentUser().getUid();
                        driverdatabaseref = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Drivers").child(onlinedriverid);
                        driverdatabaseref.setValue(true);

                        Intent driverintent =new Intent (DriverLoginRegisterActivity.this,DriversMapActivity.class);
                        startActivity(driverintent);

                        Toast.makeText(DriverLoginRegisterActivity.this,"Driver Registration done successfuly",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();


                    }
                    else
                    {
                        Toast.makeText(DriverLoginRegisterActivity.this,"Register Failed , Try Again",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                }
            });

        }
    }
}
