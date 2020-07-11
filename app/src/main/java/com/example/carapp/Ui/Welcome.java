package com.example.carapp.Ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.carapp.R;

public class Welcome extends AppCompatActivity {
    private Button customer;
    private Button driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        customer = (Button)findViewById(R.id.welcome_customer_btn);
        driver = (Button)findViewById(R.id.welcome_driver_btn);
        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginintent = new Intent(Welcome.this, CustomerLoginRegisterActivity.class);
                startActivity(loginintent);

            }
        });
        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginintent = new Intent(Welcome.this, DriverLoginRegisterActivity.class);
                startActivity(loginintent);

            }
        });
    }
}
