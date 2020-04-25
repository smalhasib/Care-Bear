package com.tinvodai.carebear.doctor.fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tinvodai.carebear.R;
import com.tinvodai.carebear.doctor.container.UserDetails;

public class DoctorProfileActivity extends AppCompatActivity {

    private static final String TAG = "DoctorProfileActivity";

    private ImageView profileImage;
    private TextView profileName, degree, specialist, registration, mobile, email, presentAddress;
    private Button editButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private UserDetails userDetailsForPassing;

    private AlertDialog builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);
        this.setTitle("Profile Of Doctor");

        //Enable back button on Menu Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initViews();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("doctors_profile_info");
        databaseReference.keepSynced(true);


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorProfileActivity.this, DoctorProfileEditActivity.class);
                intent.putExtra("user", userDetailsForPassing);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        builder = new AlertDialog.Builder(this)
                .setTitle("Please Wait")
                .setCancelable(false)
                .setView(R.layout.dialog_progress)
                .create();

        builder.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                initInfo();
            }
        }).start();

    }

    private void initViews() {
        editButton = findViewById(R.id.editButtonId);
        profileImage = findViewById(R.id.imageViewId);
        profileName = findViewById(R.id.nameTextViewId);
        degree = findViewById(R.id.degreeId);
        specialist = findViewById(R.id.specialistId);
        registration = findViewById(R.id.bmdcRegNoId);
        mobile = findViewById(R.id.mobileText);
        email = findViewById(R.id.emailId);
        presentAddress = findViewById(R.id.presentAddressId);
    }

    private void initInfo() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserDetails userDetails = snapshot.getValue(UserDetails.class);

                    if (mAuth.getCurrentUser().getEmail().equals(userDetails.getEmail())) {
                        userDetailsForPassing = userDetails;

                        Glide.with(DoctorProfileActivity.this)
                                .load(userDetails.getDoctorImageUrl())
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .override(600, 600)
                                .into(profileImage);

                        profileName.setText(userDetails.getFullName());
                        degree.setText(userDetails.getCheckBoxInfo());
                        specialist.setText(userDetails.getSpecialist());
                        registration.setText(userDetails.getRegistrationInfo());
                        mobile.setText(userDetails.getMobile());
                        email.setText(userDetails.getEmail());
                        presentAddress.setText(userDetails.getPresentAddressInfo());

                        builder.dismiss();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //This Function is needed for back button.. Without this function
    //back button wouldn't work properly..
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
