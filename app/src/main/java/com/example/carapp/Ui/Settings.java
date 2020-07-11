package com.example.carapp.Ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carapp.Pojo.CustomersMapActivity;
import com.example.carapp.Pojo.DriversMapActivity;
import com.example.carapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class Settings extends AppCompatActivity {
    private CircleImageView profile;
    private EditText name_button,phone_button,drivercarname;
    private ImageView closebutton,savebutton;
    private TextView changepicbutton;
    private String gettype;
    private String checker ="";
    Uri uri_image;
    String my_uri="";
    private static final int GALLERY_REQUEST = 1;
    private StorageTask uploadtask;
    private StorageReference storageReferencepic;
    private DatabaseReference databaseReference;
    private FirebaseAuth mauth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        gettype = getIntent().getStringExtra("type");

        mauth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(gettype);
        storageReferencepic = FirebaseStorage.getInstance().getReference().child("Profile Pictures");

        profile = (CircleImageView)findViewById(R.id.img_acc_user);
        name_button = (EditText)findViewById(R.id.name);
        phone_button = (EditText)findViewById(R.id.phone);
        drivercarname = (EditText)findViewById(R.id.driver_car_name);
        closebutton = (ImageView)findViewById(R.id.close_btn);
        savebutton = (ImageView)findViewById(R.id.save_btn);
        changepicbutton = (TextView)findViewById(R.id.change_pic_btn);
        if (gettype.equals("Drivers"))
        {
            drivercarname.setVisibility(View.VISIBLE);
        }

        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gettype.equals("Drivers"))
                {
                    startActivity(new Intent(Settings.this, DriversMapActivity.class));
                }
                else
                {
                    startActivity(new Intent(Settings.this, CustomersMapActivity.class));
            }
            }
        });
        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (checker.equals("clicked"))
                {
                  ValidateControl();
                }
                else
                {
                    ValdiateandSaveonlyInformation();
                }

            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "clicked";
                CropImage.activity().setAspectRatio(1,1)
                        .start(Settings.this);
            }
        });
        changepicbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "clicked";
                CropImage.activity().setAspectRatio(1,1)
                        .start(Settings.this);
            }
        });
        getUserInfo();

    }

  /*  public void insertImage(View view) {

        CropImage.activity().start(this);
        checker = "clicked";

    }*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri_image = result.getUri();
                //  Toast.makeText(this, uri_image+"", Toast.LENGTH_SHORT).show();
                profile.setImageURI(uri_image);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error + "", Toast.LENGTH_SHORT).show();
            }
        }


    }
    private void ValidateControl()
    {
        if (TextUtils.isEmpty(name_button.getText().toString()))
        {
            Toast.makeText(Settings.this,"please provide your name",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phone_button.getText().toString()))
        {
            Toast.makeText(Settings.this,"please provide your phone number",Toast.LENGTH_SHORT).show();
        }
        else if (gettype.equals("Drivers") && TextUtils.isEmpty(drivercarname.getText().toString()))
        {
            Toast.makeText(Settings.this,"please provide your car name",Toast.LENGTH_SHORT).show();
        }
        else if (checker.equals("clicked"))
        {
            UploadProfileimage();
        }
    }

    private void UploadProfileimage()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Settings Account Information");
        progressDialog.setMessage("please wait , while we are setting your account information");
        progressDialog.show();

        if (uri_image!=null)
        {
            final StorageReference fileref = storageReferencepic
                    .child(mauth.getCurrentUser().getUid()  +  ".jpg");
            uploadtask = fileref.putFile(uri_image);
            uploadtask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                     Uri downloadURi = task.getResult();
                     my_uri = downloadURi.toString();

                        HashMap<String, Object> usermap = new HashMap<>();
                        usermap.put("uid",mauth.getCurrentUser().getUid());
                        usermap.put("name",name_button.getText().toString());
                        usermap.put("phone",phone_button.getText().toString());
                        usermap.put("image",my_uri);

                        if (gettype.equals("Drivers"))
                        {
                            usermap.put("car",drivercarname.getText().toString());
                        }
                        databaseReference.child(mauth.getCurrentUser().getUid()).updateChildren(usermap);

                        progressDialog.dismiss();

                        if (gettype.equals("Drivers"))
                        {
                            startActivity(new Intent(Settings.this,DriversMapActivity.class));
                        }
                        else
                        {
                            startActivity(new Intent(Settings.this,CustomersMapActivity.class));

                        }


                    }
                }
            });

        }
        else {
            Toast.makeText(Settings.this,"your picture not selected",Toast.LENGTH_SHORT).show();
        }
    }
    private void ValdiateandSaveonlyInformation()
    {

        if (TextUtils.isEmpty(name_button.getText().toString()))
        {
            Toast.makeText(Settings.this,"please provide your name",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phone_button.getText().toString()))
        {
            Toast.makeText(Settings.this,"please provide your phone number",Toast.LENGTH_SHORT).show();
        }
        else if (gettype.equals("Drivers") && TextUtils.isEmpty(drivercarname.getText().toString()))
        {
            Toast.makeText(Settings.this,"please provide your car name",Toast.LENGTH_SHORT).show();
        }
       else
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Settings Account Information");
            progressDialog.setMessage("please wait , while we are setting your account information");
            progressDialog.show();
            HashMap<String, Object> usermap = new HashMap<>();
            usermap.put("uid",mauth.getCurrentUser().getUid());
            usermap.put("name",name_button.getText().toString());
            usermap.put("phone",phone_button.getText().toString());

            if (gettype.equals("Drivers"))
            {
                usermap.put("car",drivercarname.getText().toString());
            }
            databaseReference.child(mauth.getCurrentUser().getUid()).updateChildren(usermap);
            progressDialog.dismiss();


            if (gettype.equals("Drivers"))
            {
                startActivity(new Intent(Settings.this,DriversMapActivity.class));
            }
            else
            {
                startActivity(new Intent(Settings.this,CustomersMapActivity.class));

            }
        }
    }
    private void getUserInfo()
    {
        databaseReference.child(mauth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    name_button.setText(name);
                    phone_button.setText(phone);
                    if (gettype.equals("Drivers")) {
                        String car = dataSnapshot.child("car").getValue().toString();
                        drivercarname.setText(car);
                    }
                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profile);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
