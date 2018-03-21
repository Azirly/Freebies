package com.example.justin.freebies;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class AccountPage extends AppCompatActivity implements View.OnClickListener{

    private Button buttonSignOut;
    private TextView userEmail;

    private FirebaseAuth firebaseAuth;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch(item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(AccountPage.this, MainPage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    break;

                case R.id.navigation_events:
                    Intent intent2 = new Intent(AccountPage.this, EventsBlogPage.class);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent2);
                    overridePendingTransition(0,0);
                    break;

                case R.id.navigation_map:
                    Intent intent3 = new Intent(AccountPage.this, MapPage.class);
                    intent3.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent3);
                    overridePendingTransition(0,0);
                    break;

                case R.id.navigation_account:
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);
        userEmail = (TextView) findViewById(R.id.userEmail);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null)
        {
            finish();
            startActivity(new Intent(this, StartUpLogin.class));
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();

        userEmail.setText("Welcome "+user.getEmail());

        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, StartUpLogin.class));
        }

        buttonSignOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == buttonSignOut) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, StartUpLogin.class));
        }
    }
}
