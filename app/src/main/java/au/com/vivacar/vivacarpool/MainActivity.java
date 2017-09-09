package au.com.vivacar.vivacarpool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.login.widget.LoginButton;

import au.com.vivacar.vivacarpool.config.Config;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        MultiDex.install(this);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        updateWithToken(AccessToken.getCurrentAccessToken());

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String token = pref.getString("user_email","");
        if(token!=null && token!=""){
            Intent i = new Intent(MainActivity.this, SplashScreenActivity.class);
            startActivity(i);
            finish();
        }
        loginButton = (Button)findViewById(R.id.main_login_button);
        signupButton = (Button)findViewById(R.id.main_signup_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

    }

    private void updateWithToken(AccessToken currentAccessToken) {

        if (currentAccessToken != null) {

            Intent i = new Intent(MainActivity.this, SplashScreenActivity.class);
            startActivity(i);
            finish();
        }
    }
}
