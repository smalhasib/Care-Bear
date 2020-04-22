package com.hasib.carebear.doctor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hasib.carebear.R;
import com.hasib.carebear.doctor.adapter.RecyclerViewAdapter;
import com.hasib.carebear.doctor.authentication.SignInActivityForDoctor;
import com.hasib.carebear.doctor.container.Chamber;
import com.hasib.carebear.doctor.container.UserDetails;
import com.hasib.carebear.doctor.fragment.ChamberAddingDialog;
import com.hasib.carebear.doctor.fragment.ChamberEditingDialog;
import com.hasib.carebear.doctor.fragment.DoctorProfileActivity;
import com.hasib.carebear.doctor.listener.ChamberDialogListener;
import com.hasib.carebear.doctor.listener.ChamberEventListener;
import com.hasib.carebear.doctor.listener.ChamberAddingDialogTimeSetListener;
import com.hasib.carebear.doctor.listener.TimePickerListener;
import com.hasib.carebear.support.ImageSupport;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DoctorDashBoardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ChamberDialogListener, ChamberEventListener, TimePickerListener {
    private static final String TAG = "DoctorDashBoardActivity";

    //Floating Action "+" Bar
    private FloatingActionButton chamberAddingButton;

    //Main parent drawer layout
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    //Navigation Menu
    private NavigationView navigationView;
    private View navHeader;

    //Chamber class
    private List<Chamber> chamberList;

    //Tapped chamber position
    private int position;
    private String chamberTime;

    //For showing chamber details to user
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    //Firebase authenticator
    private FirebaseAuth mAuth;

    //Firebase realtime database
    private DatabaseReference databaseReference;

    //Menu Item
    private MenuItem editButton;
    private MenuItem deleteButton;

    /**
     * Interface for setting time on ChamberAddingDialog after choosing time
     */
    private ChamberAddingDialogTimeSetListener testing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dash_board);
        this.setTitle("Chambers Dashboard");

        //Enable HamBurger Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Floating action button
        chamberAddingButton = findViewById(R.id.chamberAddingButtonId);

        //Navigation panel control
        navigationView = findViewById(R.id.navigationId);
        drawerLayout = findViewById(R.id.drawerId);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //Navigation View on click listener
        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);

        //Firebase authenticator initialization
        mAuth = FirebaseAuth.getInstance();

        //Firebase realtime database initialization
        databaseReference = databaseReference = FirebaseDatabase.getInstance().getReference("doctors_profile_info");

        //Chamber class initialization;
        chamberList = new ArrayList<>();


        //Floating button on click listener
        chamberAddingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: chamber adding button pressed");

                //chamber info adding dialog box
                openDialog();
            }
        });

        //Recycler view initialization method
        initChamberRecyclerView();

        //Adapter Listener
        this.adapter.setListener(this);

        //for hiding edit & delete button
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editButton.setVisible(false);
                deleteButton.setVisible(false);
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(DoctorDashBoardActivity.this, SignInActivityForDoctor.class));
        }

        initNavigationHeader(mAuth.getCurrentUser());
    }

    private void initNavigationHeader(final FirebaseUser user) {
        MaterialTextView profileName = navHeader.findViewById(R.id.profileNameId);
        MaterialTextView profileEmail = navHeader.findViewById(R.id.profileEmailId);
        final ImageView profileImage = navHeader.findViewById(R.id.profileImageId);

        Log.d(TAG, "initNavigationHeader: "+ user.getPhotoUrl());

        Glide.with(DoctorDashBoardActivity.this)
                .load(user.getPhotoUrl())
                .fitCenter()
                .circleCrop()
                .into(profileImage);

        profileName.setText(user.getDisplayName());
        profileEmail.setText(user.getEmail());
    }

    //Method for chamber adding dialog box
    private void openDialog() {
        //Chamber adding alert dialog box initialization
        ChamberAddingDialog chamberAddingDialog = new ChamberAddingDialog(this);

        //Getting fragment support
        chamberAddingDialog.show(getSupportFragmentManager(), "Chamber Adding Dialog");
    }

    private void initChamberRecyclerView() {
        Log.d(TAG, "initChamberRecyclerView: init recyclerView");

        //Initializing recycler view
        recyclerView = findViewById(R.id.doctorChamberRecyclerView);

        //Initializing recycler view adapter
        adapter = new RecyclerViewAdapter(this, chamberList);

        //setting adapter to recycler view
        recyclerView.setAdapter(adapter);

        //Recycler view layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_chamber_menu, menu);

        //Menu item initialization
        editButton = menu.findItem(R.id.editChamberId);
        deleteButton = menu.findItem(R.id.deleteChamberId);

        return super.onCreateOptionsMenu(menu);
    }

    //Method for controlling navigation view close or open hamburger button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.editChamberId: {
                Log.d(TAG, "onOptionsItemSelected: Chamber "+chamberList.get(position).toString());

                ChamberEditingDialog chamberEditingDialog = new ChamberEditingDialog(this, chamberList.get(position), position);
                chamberEditingDialog.show(getSupportFragmentManager(), "Edit Chamber Info");
            }
            break;

            case R.id.deleteChamberId: {
                new AlertDialog.Builder(this)
                        .setTitle("Are you want to delete this chamber?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                chamberList.remove(position);

                                // TODO: 10-Apr-20 have to add firebase database support on chamber delete

                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //On navigation view item pressed
        switch (item.getItemId()) {
            case R.id.homeMenuId: {
                Log.d(TAG, "onNavigationItemSelected: Tapped on Home Button");

                //Intenting to this activity
                startActivity(new Intent(DoctorDashBoardActivity.this, DoctorDashBoardActivity.class));
            }
            break;

            case R.id.profileMenuId: {
                Log.d(TAG, "onNavigationItemSelected: Profile Button Pressed");

                //Intenting to doctor's profile activity
                startActivity(new Intent(DoctorDashBoardActivity.this, DoctorProfileActivity.class));
            }
            break;

            case R.id.signOutMenuId: {
                Log.d(TAG, "onNavigationItemSelected: user signing out");

                //Signing out current user
                mAuth.signOut();
                finish();

                //Intenting to sign in activity for doctor
                startActivity(new Intent(DoctorDashBoardActivity.this, SignInActivityForDoctor.class));
            }
            break;

            case R.id.shareMenuId : {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");

                String subject = "Care Bear- In a way of Healthiness";
                String body = "This is a medical type app. It helps you to find catagorized doctor\n" +
                        "easily. It's both for a doctor and a patient.\n" +
                        "Download Link.......";

                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);

                startActivity(Intent.createChooser(intent, "Share with"));
            }
            break;
        }

        return false;
    }

    //Method for getting data from chamber adding dialog box
    @Override
    public void chamberAddingTexts(String name, String fess, Map<String, Boolean> activeDays, String address, LatLng latLng) {
        chamberList.add(new Chamber(name, fess, address, latLng, chamberTime, activeDays));
        Log.d(TAG, "chamberAddingTexts: "+ activeDays.toString());


        // TODO: 10-Apr-20 have to add firebase database support

        //Notifying recycler view for adapter on data change
        adapter.notifyDataSetChanged();
    }

    @Override
    public void chamberEditingTexts(String name, String fees, Map<String, Boolean> activeDays, String address, LatLng latLng, int position) {
        chamberList.set(position, new Chamber(name, fees, address, latLng, chamberTime, activeDays));
        editButton.setVisible(false);
        deleteButton.setVisible(false);

        // TODO: 10-Apr-20 have to add firebase database support on chamber info change

        adapter.notifyDataSetChanged();
    }

    @Override
    public void chamberEditingCancel() {
        editButton.setVisible(false);
        deleteButton.setVisible(false);
    }

    @Override
    public void onChamberClick(final Chamber chamber, int position) {
        Log.d(TAG, "onChamberClick: It's working");
    }

    @Override
    public void onChamberLongClick(Chamber chamber, final int position) {
        Log.d(TAG, "onChamberLongClick: Long Click is working");

        editButton.setVisible(true);
        deleteButton.setVisible(true);

        this.position = position;
    }

    /**
     * Interface listener method for getting time from TimePickerDialog
     */
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Log.d(TAG, "onTimeSet: on doctor dash time class");
        Time tme = new Time(hour, minute, 0);
        Format formatter = new SimpleDateFormat("h:mm a");
        chamberTime = formatter.format(tme);

        testing.setTime(chamberTime);
    }

    /**
     * Interface method for
     */
    public void setChamberAddingDialogTimeSetListener(ChamberAddingDialogTimeSetListener chamberAddingDialogTimeSetListener) {
        this.testing = chamberAddingDialogTimeSetListener;
    }
}