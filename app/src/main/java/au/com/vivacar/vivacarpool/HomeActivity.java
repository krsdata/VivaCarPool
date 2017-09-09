package au.com.vivacar.vivacarpool;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import au.com.vivacar.vivacarpool.config.Config;
import au.com.vivacar.vivacarpool.utils.CircleTransform;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private EditText onwardDateText, onwardTimeText;
  //  private EditText returnDateText, returnTimeText;

    private AutoCompleteTextView fromLoc, toLoc;

    private static final int REQUEST_CODE_AUTOCOMPLETE_FROM_LOC = 1;

    private static final int REQUEST_CODE_AUTOCOMPLETE_TO_LOC = 2;

    private GoogleApiClient mGoogleApiClient;

    private Button searchButton;

    ProgressDialog prgDialog;

  //  private RelativeLayout returnDateLayout;

   // private CheckBox oneWayCheck, returnCheck;
  //  private CheckBox returnCheck;

    private TextView userName;
    private ImageView userProfilePic;

    Calendar myCalendar;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTitle("Viva Car Pool");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddRideActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String userEmail = pref.getString("user_email",null);

        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);


        //Toast.makeText(getApplicationContext(),"Email = "+userEmail,Toast.LENGTH_SHORT).show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

       // View navHeader = navigationView.findViewById(R.id.nav_header_home);
        View header = navigationView.getHeaderView(0);
        userName = (TextView) header.findViewById(R.id.username);
        userProfilePic = (ImageView) header.findViewById(R.id.user_profile_pic);

        userName.setText(pref.getString("user_name",null));

        Glide.with(this).load(pref.getString("user_profile_pic",""))
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(userProfilePic);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

       // returnDateLayout = (RelativeLayout) findViewById(R.id.return_date_layout);
       // returnDateLayout.setVisibility(View.GONE);
        onwardDateText = (EditText) findViewById(R.id.ride_onward_date);

        onwardTimeText = (EditText) findViewById(R.id.ride_onward_time);

//        returnDateText = (EditText) findViewById(R.id.ride_return_date);

  //      returnTimeText = (EditText) findViewById(R.id.ride_return_time);

        searchButton = (Button) findViewById(R.id.ride_search_button);

        fromLoc = (AutoCompleteTextView) findViewById(R.id.ride_from);
        fromLoc.setText("Melbourne");
        fromLoc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    try {

                        AutocompleteFilter fromFilter = new AutocompleteFilter.Builder()
                                .setTypeFilter(Place.TYPE_COUNTRY).setCountry("AU")
                                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                .build();

                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                .setFilter(fromFilter)
                                .build(HomeActivity.this);

                        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_FROM_LOC);
                    } catch (GooglePlayServicesRepairableException e) {

                        GoogleApiAvailability.getInstance().getErrorDialog(HomeActivity.this, e.getConnectionStatusCode(),
                                0).show();
                    } catch (GooglePlayServicesNotAvailableException e) {

                        String message = "Google Play Services is not available: " +
                                GoogleApiAvailability.getInstance().getErrorString(e.errorCode);


                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
                else{
                    return false;
                }
            }
        });


        /*fromLoc.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP )
                {
                    if(event.getRawX() >= fromLoc.getTotalPaddingRight()) {

                        if (mGoogleApiClient != null) {

                            if ( ContextCompat.checkSelfPermission( HomeActivity.this
                                    , android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

                                ActivityCompat.requestPermissions( HomeActivity.this
                                        , new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                                        1 );
                            }

                            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);

                            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                                @Override
                                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                                    Log.i("Place", "onResult");
                                    if (likelyPlaces.getCount() <= 0) {
                                        Toast.makeText(HomeActivity.this, "No place found", Toast.LENGTH_SHORT).show();
                                    }
                                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                        Log.i("Place", String.format("Place '%s' has likelihood: %g",
                                                placeLikelihood.getPlace().getName(),
                                                placeLikelihood.getLikelihood()));
                                    }
                                    likelyPlaces.release();
                                }
                            });
                        }
                        return true;
                    }


                }
                return true;
            }
        });
*/
        toLoc = (AutoCompleteTextView) findViewById(R.id.ride_to);
        toLoc.setText("Sydney");
        toLoc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    try {

                        AutocompleteFilter fromFilter = new AutocompleteFilter.Builder()
                                .setTypeFilter(Place.TYPE_COUNTRY).setCountry("AU")
                                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                .build();

                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                .setFilter(fromFilter)
                                .build(HomeActivity.this);
                        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_TO_LOC);
                    } catch (GooglePlayServicesRepairableException e) {

                        GoogleApiAvailability.getInstance().getErrorDialog(HomeActivity.this, e.getConnectionStatusCode(),
                                0 /* requestCode */).show();
                    } catch (GooglePlayServicesNotAvailableException e) {

                        String message = "Google Play Services is not available: " +
                                GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                else{
                    return false;
                }

            }
        });

     //   oneWayCheck = (CheckBox) findViewById(R.id.check_one_way);

      //  returnCheck = (CheckBox) findViewById(R.id.check_round_trip);

        myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                onwardDateText.setText(sdf.format(myCalendar.getTime()));
            }

        };

        View.OnTouchListener otlOnward = new View.OnTouchListener() {
            public boolean onTouch (View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date d = new Date();

                    DatePickerDialog dpd = new DatePickerDialog(HomeActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));

                    dpd.getDatePicker().setMinDate(d.getTime());
                    dpd.show();
                    return true;
                }
                else{
                    return false;
                }

            }
        };

        onwardDateText.setOnTouchListener(otlOnward);


        View.OnTouchListener otlReturn = new View.OnTouchListener() {
            public boolean onTouch (View v, MotionEvent event) {
                new DatePickerDialog(HomeActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                return true; // the listener has consumed the event
            }
        };

       /* onwardDateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(HomeActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });*/


       /* final DatePickerDialog.OnDateSetListener returnDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                onwardDateText.setText(sdf.format(myCalendar.getTime()));
            }

        };

        returnDateText.setOnTouchListener(otlReturn);*/

       /* returnDateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(HomeActivity.this, returnDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });*/

        onwardTimeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(HomeActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        onwardTimeText.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        /*returnTimeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(HomeActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        onwardTimeText.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });



        returnCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

               @Override
               public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {

                   if(returnCheck.isChecked()) {

                       returnDateLayout.setVisibility(View.VISIBLE);
                   }

                   if(!returnCheck.isChecked()) {

                       returnDateLayout.setVisibility(View.GONE);

                   }
               }
           }
        );*/

        final RequestParams params = new RequestParams();

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /*Intent intent = new Intent(getApplicationContext(), RideListActivity.class);
                startActivity(intent);*/
              /*  params.put("from", fromLoc.getText().toString());
                // Put Http parameter password with value of Password Edit Value control
                params.put("to", toLoc.getText().toString());
                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
*/
               Intent intent = new Intent(getApplicationContext(), RideListActivity.class);
                intent.putExtra("from",fromLoc.getText().toString());
                intent.putExtra("to",toLoc.getText().toString());
                intent.putExtra("journeydate",onwardDateText.getText().toString());
                intent.putExtra("leavingafter",onwardTimeText.getText().toString());

                startActivity(intent);

                //Intent intent = new Intent(getApplicationContext(), PaymentActvity.class);
                //startActivity(intent);

                //attemptLogin();


                /*Intent intent = new Intent(getApplicationContext(), FirebaseNotificationActivity.class);
                startActivity(intent);*/

                /*Intent intent = new Intent(getApplicationContext(), FilUploadTest.class);
                startActivity(intent);*/

            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);

            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_messages) {
            Intent intent = new Intent(getApplicationContext(), ChatListActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_notifications) {

            Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_find_ride) {
            // Handle the camera action
        } else if (id == R.id.nav_settings) {

            Intent intent = new Intent(getApplicationContext(), PaymentHistoryActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_my_rides) {

            Intent intent = new Intent(getApplicationContext(), RideHistoryActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_contact) {

            Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {

            Intent intent = new Intent(getApplicationContext(), FAQActivity.class);
            //Intent intent = new Intent(getApplicationContext(), ActivityFriendDetails.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {


            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "HEY THERE ! I AM USING VIVA CAR POOL !!";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Viva Car Pool");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));


        } else if (id == R.id.nav_logout) {

            System.out.println("In logout ");
            AccessToken.setCurrentAccessToken(null);
            SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("user_email", null);
            editor.commit();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE_FROM_LOC) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);

                // Format the place's details and display them in the TextView.
                fromLoc.setText(formatPlaceDetails(getResources(), place.getAddress().toString().split(",")[0]));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                //Log.e(TAG, "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }

        if (requestCode == REQUEST_CODE_AUTOCOMPLETE_TO_LOC) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);

                // Format the place's details and display them in the TextView.
                toLoc.setText(formatPlaceDetails(getResources(), place.getAddress().toString().split(",")[0]));


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                //Log.e(TAG, "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }

    }

     private static Spanned formatPlaceDetails(Resources res, CharSequence name) {
        /*Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));*/
        return Html.fromHtml(res.getString(R.string.place_details, name));

    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "onConnected", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_LONG).show();
    }

    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.SEARCH_RIDES, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {

                    JSONObject o = new JSONObject(responseBody);
                    JSONArray arr = o.getJSONArray("rides");

                    Log.i("abc","Hello"+arr.toString());
                    for(int i=0;i<arr.length();i++){
                        JSONObject obj = arr.getJSONObject(i);
                        Toast.makeText(getApplicationContext(), obj.getString("luggage"), Toast.LENGTH_LONG).show();

                    }

                    /*
                    // JSON Object
                    JSONObject obj = new JSONObject(responseBody);
                    System.out.println(obj);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("success")) {
                        Toast.makeText(getApplicationContext(), "Found Rides!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), RideListActivity.class);
                        startActivity(intent);

                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("msg"), Toast.LENGTH_LONG).show();
                    }*/
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {

                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }


        });
    }

}
