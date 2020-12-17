package com.far.nowaste;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.far.nowaste.Fragments.CalendarioFragment;
import com.far.nowaste.Fragments.ContattaciFragment;
import com.far.nowaste.Fragments.CuriositaFragment;
import com.far.nowaste.Fragments.DetailUserFragment;
import com.far.nowaste.Fragments.HomeFragment;
import com.far.nowaste.Fragments.ImpostazioniFragment;
import com.far.nowaste.Fragments.LuoghiFragment;
import com.far.nowaste.Objects.Utente;
import com.far.nowaste.Other.SearchToolbarAnimation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // toolbar
    MenuItem mSearchItem;
    private Toolbar mToolbar;

    // navigationView
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;

    static public Utente CURRENTUSER;

    View header;
    TextView mFullName, mEmail;
    ImageView mImage;

    // firebase
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    // home 1, profilo 2, curiosità 3, calendario 4, luoghi 5, contattaci 6, impostazioni 7
    static public int FRAGMENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CURRENTUSER = null;

        // toolbar
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));

        // navigationView
        drawerLayout = findViewById(R.id.main_drawerlayout);
        navigationView = findViewById(R.id.main_navView);

        // navigationView
        navigationView.bringToFront();
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar, R.string.openNavDrawer, R.string.closeNavDrawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // associazione view dell'header
        header = navigationView.getHeaderView(0);

        mFullName = header.findViewById(R.id.navHeader_nameTextView);
        mEmail = header.findViewById(R.id.navHeader_emailTextView);
        mImage = header.findViewById(R.id.navHeader_userImageView);

        // header onclick
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth = FirebaseAuth.getInstance();
                if (fAuth.getCurrentUser() == null) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                } else {
                    mToolbar.setTitle("Profilo");
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout, new DetailUserFragment()).commit();
                    FRAGMENT = 2;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
            FRAGMENT = 1;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // imposta CURRENTUSER e i dati nell'header
        fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() != null) {
            fStore = FirebaseFirestore.getInstance();
            FirebaseUser user = fAuth.getCurrentUser();
            fStore.collection("users").document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    CURRENTUSER = value.toObject(Utente.class);

                    mFullName.setText(CURRENTUSER.getFullName());
                    mEmail.setText(CURRENTUSER.getEmail());
                    mFullName.setVisibility(View.VISIBLE);
                    if (CURRENTUSER.getImage() != null) {
                        Glide.with(getApplicationContext()).load(CURRENTUSER.getImage()).apply(RequestOptions.circleCropTransform()).into(mImage);
                    }
                    fAuth.getCurrentUser().getPhotoUrl();
                }
            });
            fStore.terminate();
        } else {
            mEmail.setText("Accedi al tuo account");
            mFullName.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkFragmentRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        mSearchItem = menu.findItem(R.id.m_search);

        // crea le animazioni
        new SearchToolbarAnimation(getWindow(), mToolbar, mSearchItem, getApplicationContext(), getResources()).setAnimation();

        //set queryListener searchView
        SearchView wasteSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        wasteSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent listaSearchActivity = new Intent(getApplicationContext(), ListaSearchActivity.class);
                listaSearchActivity.putExtra("com.far.nowaste.SEARCH_QUERY", query);
                startActivity(listaSearchActivity);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }


    // onclick sulla navigation
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (item.getItemId()){
            case R.id.nav_home:
                if (FRAGMENT != 1) {
                    mToolbar.setTitle("NoWaste");
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .replace(R.id.main_frameLayout, new HomeFragment()).commit();
                    FRAGMENT = 1;
                }
                break;
            case R.id.nav_curiosita:
                if (FRAGMENT != 3) {
                    mToolbar.setTitle("Curiosità");
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_frameLayout, new CuriositaFragment()).commit();
                    FRAGMENT = 3;
                }
                break;
            case R.id.nav_calendario:
                if (FRAGMENT != 4) {
                    mToolbar.setTitle("Calendario");
                    getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_frameLayout, new CalendarioFragment()).commit();
                    FRAGMENT = 4;
                }
                break;
            case R.id.nav_luoghi:
                if (FRAGMENT != 5) {
                    mToolbar.setTitle("Luoghi");
                    getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_frameLayout, new LuoghiFragment()).commit();
                    FRAGMENT = 5;
                }
                break;
            case R.id.nav_contattaci:
                if (FRAGMENT != 6) {
                    mToolbar.setTitle("Contattaci");
                    getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_frameLayout, new ContattaciFragment()).commit();
                    FRAGMENT = 6;
                }
                break;
            case R.id.nav_impostazioni:
                if (FRAGMENT != 7) {
                    mToolbar.setTitle("Impostazioni");
                    getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_frameLayout, new ImpostazioniFragment()).commit();
                    FRAGMENT = 7;
                }
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // chiude la navigation quando premi back
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(FRAGMENT != 1){
            mToolbar.setTitle("NoWaste");
            getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .replace(R.id.main_frameLayout, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
            FRAGMENT = 1;
        } else {
            super.onBackPressed();
        }
    }

    private void checkFragmentRequest(){
        // num: se 1 home e logout, se 2 profilo, se 3 home e deleteAccount
        if (LoginActivity.NUM == 1) {
            LoginActivity.NUM = 0;
            mToolbar.setTitle("NoWaste");
            getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .replace(R.id.main_frameLayout, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
            FRAGMENT = 1;
            logout();
        } else if(LoginActivity.NUM == 2) {
            LoginActivity.NUM = 0;
            mToolbar.setTitle("Profilo");
            getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.main_frameLayout, new DetailUserFragment()).commit();
            FRAGMENT = 2;
        } else if (LoginActivity.NUM == 3) {
            LoginActivity.NUM = 0;
            mToolbar.setTitle("NoWaste");
            getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .replace(R.id.main_frameLayout, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
            FRAGMENT = 1;
            deleteAccount();
        }
    }

    // logout from settings
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        CURRENTUSER = null;
        Toast.makeText(MainActivity.this, "Logout effettuato.", Toast.LENGTH_SHORT).show();
        mEmail.setText("Accedi al tuo account");
        mFullName.setVisibility(View.GONE);
        Drawable defaultImage = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_user);
        mImage.setImageDrawable(defaultImage);
        mToolbar.setTitle("NoWaste");
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout, new HomeFragment()).commit();
        FRAGMENT = 1;
    }

    // delete account from settings
    public void deleteAccount() {
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();

        // re-authenticate the user
        AuthCredential credential;
        credential = EmailAuthProvider.getCredential(user.getEmail(), ImpostazioniFragment.PASSWORD);
        ImpostazioniFragment.PASSWORD = null;

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("TAG", "User re-authenticated.");
            }
        });

        // cancella l'utente da firestore
        fStore.collection("users").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "DocumentSnapshot successfully deleted!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Error deleting document", e);
            }
        });
        fStore.terminate();

        // cancella l'utente da fireauth
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Account eliminato", Toast.LENGTH_SHORT).show();
                }
            }
        });

        CURRENTUSER = null;
    }
}