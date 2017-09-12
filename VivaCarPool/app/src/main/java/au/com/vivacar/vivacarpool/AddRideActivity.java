package au.com.vivacar.vivacarpool;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import au.com.vivacar.vivacarpool.config.Config;
import au.com.vivacar.vivacarpool.utils.DateTimeUtil;
import au.com.vivacar.vivacarpool.utils.WebServiceConfig;

public class AddRideActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private AutoCompleteTextView from, to, stopOver;

    private Spinner seats;

    private EditText price, onwardDate, onwardTime, returnDate, comments;

    private CheckBox returnCheck;

    private LinearLayout returnDateLayout, stopOverLayout;

    private EditText returnTime;

    private CheckBox ac, music, pets;

    private Spinner luggage, paymentMethod;

    private EditText carModel, carNumber, carColor;

    private Button addRideButton;

    private static final int REQUEST_CODE_AUTOCOMPLETE_FROM_LOC = 1;

    private static final int REQUEST_CODE_AUTOCOMPLETE_TO_LOC = 2;

    private static final int REQUEST_CODE_AUTOCOMPLETE_STOPOVER = 3;

    private Calendar myCalendar;

    ProgressDialog prgDialog;

    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);

        setTitle("Add a Ride");

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
         userEmail = pref.getString("manual_token","");

        from = (AutoCompleteTextView) findViewById(R.id.add_ride_from);
        to = (AutoCompleteTextView) findViewById(R.id.add_ride_to);
        stopOver = (AutoCompleteTextView) findViewById(R.id.add_stop_over);

        seats = (Spinner) findViewById(R.id.add_no_of_seats);
        price = (EditText) findViewById(R.id.add_ride_cost);

        onwardDate = (EditText) findViewById(R.id.add_ride_onward_date);
        onwardTime = (EditText) findViewById(R.id.add_ride_onward_time);

        returnDate = (EditText) findViewById(R.id.add_ride_return_date);
        returnTime = (EditText) findViewById(R.id.add_ride_return_time);

        comments = (EditText) findViewById(R.id.owner_comments);

        ac = (CheckBox) findViewById(R.id.add_check_ac);

        pets = (CheckBox) findViewById(R.id.add_check_pets);

        music = (CheckBox) findViewById(R.id.add_check_music);

        luggage = (Spinner) findViewById(R.id.add_ride_luggage);
        paymentMethod = (Spinner) findViewById(R.id.add_ride_pay_by);

        carModel = (EditText) findViewById(R.id.add_ride_car_make);
        carNumber = (EditText) findViewById(R.id.add_ride_car_number);
        carColor = (EditText) findViewById(R.id.add_ride_car_color);

        addRideButton = (Button) findViewById(R.id.add_ride_button);

        returnCheck = (CheckBox) findViewById(R.id.add_check_round_trip);
        returnDateLayout = (LinearLayout) findViewById(R.id.add_return_date_layout);
        returnDateLayout.setVisibility(View.GONE);

        stopOverLayout = (LinearLayout) findViewById(R.id.stopover_layout);



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
        );

        to.setOnTouchListener(new View.OnTouchListener() {
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
                            .build(AddRideActivity.this);
                    startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_TO_LOC);
                } catch (GooglePlayServicesRepairableException e) {

                    GoogleApiAvailability.getInstance().getErrorDialog(AddRideActivity.this, e.getConnectionStatusCode(),
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


        from.setOnTouchListener(new View.OnTouchListener() {
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
                            .build(AddRideActivity.this);

                    startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_FROM_LOC);
                } catch (GooglePlayServicesRepairableException e) {

                    GoogleApiAvailability.getInstance().getErrorDialog(AddRideActivity.this, e.getConnectionStatusCode(),
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

                onwardDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        View.OnTouchListener otlOnward = new View.OnTouchListener() {
            public boolean onTouch (View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date d = new Date();

                    DatePickerDialog dpd = new DatePickerDialog(AddRideActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                    dpd.getDatePicker().setMinDate(d.getTime());
                            dpd.show();
                return true; // the listener has consumed the event
                }
                else{
                    return false;
                }

            }
        };

        onwardDate.setOnTouchListener(otlOnward);


        View.OnTouchListener otlReturn = new View.OnTouchListener() {
            public boolean onTouch (View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date d = new Date();

                    DatePickerDialog dpd = new DatePickerDialog(AddRideActivity.this, date, myCalendar
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


        returnDate.setOnTouchListener(otlReturn);

        final DatePickerDialog.OnDateSetListener returnDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                onwardDate.setText(sdf.format(myCalendar.getTime()));
            }

        };




        onwardTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddRideActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        onwardTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        returnTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddRideActivity.this, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        onwardTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        stopOver.setOnTouchListener(new View.OnTouchListener() {
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
                                .build(AddRideActivity.this);

                        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_STOPOVER);
                    } catch (GooglePlayServicesRepairableException e) {

                        GoogleApiAvailability.getInstance().getErrorDialog(AddRideActivity.this, e.getConnectionStatusCode(),
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

        final RequestParams params = new RequestParams();

        addRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Integer journeyTime = DateTimeUtil.getTimeInMinutes(onwardTime.getText().toString());


                String stopOverPoints ="";

                if(stopOverLayout.getChildCount()>0){
                    for(int i=0;i< stopOverLayout.getChildCount();i++){
                        stopOverPoints = stopOverPoints + ((TextView)stopOverLayout.getChildAt(i)).getText().toString()+",";
                    }
                }


                String acStr="", musicStr="", petsStr="", returnStr="";

                if(ac.isChecked()){
                    acStr = "Available";
                }
                else{
                    acStr = "Not Available";
                }
                if(music.isChecked()){
                    musicStr = "Available";
                }
                else{
                    musicStr = "Not Available";
                }
                if(pets.isChecked()){
                    petsStr = "Available";
                }
                else{
                    petsStr = "Not Available";
                }
                if(returnCheck.isChecked()){
                    returnStr = "true";
                }
                else{
                    returnStr = "false";
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

                Date today = new Date();

                String formattedDate = sdf.format(today);

                params.put("from", from.getText().toString());
                params.put("to", to.getText().toString());
                params.put("date", onwardDate.getText().toString());
                params.put("time", onwardTime.getText().toString());
                params.put("stop_over", stopOverPoints);
                params.put("seats", seats.getSelectedItem().toString());
                params.put("price", price.getText().toString());
                params.put("is_return", returnStr);
                params.put("ac", acStr);
                params.put("music", musicStr);
                params.put("pets", petsStr);
                params.put("luggage", luggage.getSelectedItem().toString());
                params.put("car_model", carModel.getText().toString());
                params.put("car_number", carNumber.getText().toString());
                params.put("car_color", carColor.getText().toString());
                params.put("posted_by", userEmail);
                params.put("posted_on", formattedDate);
                params.put("owner_comments", comments.getText().toString());
                params.put("pay_by", paymentMethod.getSelectedItem().toString());
                params.put("status", "Upcoming");

                invokeWS(params);

            }
        });

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE_FROM_LOC) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);

                System.out.println("place name = "+place.getAddress());

                // Format the place's details and display them in the TextView.
                //from.setText(formatPlaceDetails(getResources(), place.getName()));

                from.setText(formatPlaceDetails(getResources(), place.getAddress().toString().split(",")[0]));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

            } else if (resultCode == RESULT_CANCELED) {

            }
        }

        if (requestCode == REQUEST_CODE_AUTOCOMPLETE_TO_LOC) {
            if (resultCode == RESULT_OK) {

                Place place = PlaceAutocomplete.getPlace(this, data);

                to.setText(formatPlaceDetails(getResources(), place.getAddress().toString().split(",")[0]));


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

            } else if (resultCode == RESULT_CANCELED) {

            }
        }

        if (requestCode == REQUEST_CODE_AUTOCOMPLETE_STOPOVER) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);

                // Format the place's details and display them in the TextView.
                //from.setText(formatPlaceDetails(getResources(), place.getName()));

                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lparams.setMargins(5,5,5,5);
                final TextView tv=new TextView(this);
                tv.setLayoutParams(lparams);
                tv.setText(place.getAddress().toString().split(",")[0]);
                tv.setTextSize(20);
                tv.setTextColor(Color.WHITE);
                tv.setPaddingRelative(10,4,10,4);
                tv.setBackgroundResource(R.drawable.textviewshape);

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(AddRideActivity.this)
                                .setTitle("Remove Stop over point ?")
                                .setMessage("Are you sure you want to remove this point ?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        tv.setVisibility(View.GONE);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
                this.stopOverLayout.addView(tv);


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

            } else if (resultCode == RESULT_CANCELED) {

            }
        }


    }

    private static Spanned formatPlaceDetails(Resources res, CharSequence name) {

        return Html.fromHtml(res.getString(R.string.place_details, name));

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void invokeWS(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(WebServiceConfig.ADD_RIDE,params ,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String responseBody) {
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(responseBody);
                    System.out.println(obj);
                    // When the JSON response has status boolean value assigned with true
                    if(obj.getBoolean("success")){
                        Toast.makeText(getApplicationContext(), "Ride added successfully", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), RideHistoryActivity.class);
                        intent.putExtra("rideadded","success");
                        startActivity(intent);

                    }
                    // Else display error message
                    else{
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("msg"), Toast.LENGTH_LONG).show();
                    }
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
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }


        });
    }

    View.OnClickListener ocl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            new AlertDialog.Builder(AddRideActivity.this)
                    .setTitle("Remove Stop over point ?")
                    .setMessage("Are you sure you want to remove this point ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    };
}
