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

import com.example.carapp.Pojo.CustomersMapActivity;
import com.example.carapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginRegisterActivity extends AppCompatActivity {
    private Button customer_login;
    private Button customer_create;
    private Button customer_register;
    private TextView cust_stuts;
    private TextView have;
    private EditText customer_email;
    private TextInputLayout customer_password;
    private FirebaseAuth mauth;
    private DatabaseReference customerdatabaseref;
    private String onlinecustomerid;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        mauth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);
        customer_login = (Button)findViewById(R.id.login_customer_btn);
        customer_create = (Button)findViewById(R.id.customer_register_btn);
        customer_register = (Button)findViewById(R.id.customer_register_btn2);
        cust_stuts = (TextView)findViewById(R.id.customer_stuts);
        have = (TextView)findViewById(R.id.customer_have);
        customer_email = (EditText)findViewById(R.id.customer_login_email);
        customer_password = (TextInputLayout)findViewById(R.id.customer_login_pass);


        customer_register.setVisibility(View.INVISIBLE);
        customer_register.setEnabled(false);

        customer_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customer_create.setVisibility(View.INVISIBLE);
                customer_login.setVisibility(View.INVISIBLE);
                have.setVisibility(View.INVISIBLE);
                cust_stuts.setText("Customer Register");
                customer_register.setVisibility(View.VISIBLE);
                customer_register.setEnabled(true);
            }
        });
        customer_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = customer_email.getText().toString();
                String password = customer_password.getEditText().getText().toString();

                RegiseterCustomer(email, password);
            }
        });
        customer_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = customer_email.getText().toString();
                String password = customer_password.getEditText().getText().toString();

                SigninCustomer(email, password);
            }
        });

    }

    private void SigninCustomer(String email, String password) {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this,"please write your email",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this,"please write your password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            dialog.setTitle("Customer SignIn");
            dialog.setMessage("please wait , while we are check your SigningIn");
            dialog.show();

            mauth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        Intent customerintent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapActivity.class);
                        startActivity(customerintent);

                        Toast.makeText(CustomerLoginRegisterActivity.this,"Customer Signing In done successfuly",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(CustomerLoginRegisterActivity.this,"Login Failed , Try Again",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                }
            });

        }

    }

    private void RegiseterCustomer(String email, String password) {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this,"please write your email",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this,"please write your password",Toast.LENGTH_SHORT).show();
        }
        else if (password.length()<6)
        {
            Toast.makeText(this, "Password length must br greater than 6 chars", Toast.LENGTH_SHORT).show();
        }
        else
        {
            dialog.setTitle("Customer Registraion");
            dialog.setMessage("please wait , while we are register your data");
            dialog.show();

            mauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        onlinecustomerid = mauth.getCurrentUser().getUid();
                        customerdatabaseref = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Customers").child(onlinecustomerid);
                        customerdatabaseref.setValue(true);

                        Intent driverintent = new Intent(CustomerLoginRegisterActivity.this,CustomersMapActivity.class);
                        startActivity(driverintent);

                        Toast.makeText(CustomerLoginRegisterActivity.this,"Customer registration done successfuly",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(CustomerLoginRegisterActivity.this,"Register Failed , Try Again",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                }
            });

        }
    }
}
