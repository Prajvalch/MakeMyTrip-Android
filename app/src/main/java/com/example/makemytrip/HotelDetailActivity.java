package com.example.makemytrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;

public class HotelDetailActivity extends AppCompatActivity {

    NumberPicker numberPickerRooms;
    private String selectedDates="1/02/2024";
    private int Price=0;
    Button buttonDatePicker, buttonBookHotel,buttonDatePicker2 ,submit;
    FirebaseAuth mAuth;

    String formattedAverageRating ;
    String formattedTotalRating ,  formattednewRating;
    DatabaseReference databaseReference;
    TextView textViewHotelName, textViewHotelAddress ,ratingcount;
    private TextView ratinginwords , hotelRating;
    private RatingBar ratingBar;
    float totalRating;
    int numRatings;
    float averageRating;
    String selectedCountry;

    Ratings ratings;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Handle the back button
            case android.R.id.home:
                onBackPressed(); // This will call the default back button behavior
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Set your desired icon for the navigation drawer toggle
        }

        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setTitle("Hotel Details");
        // Retrieve hotel details from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("hotel")) {
            Hotel hotel = intent.getParcelableExtra("hotel");


           if (hotel!=null) {
               Price = hotel.getPrice();
               // Now you have the 'hotel' object, you can display its details in the activity
               TextView textViewHotelName = findViewById(R.id.textViewHotelName);
               TextView textViewHotelAddress = findViewById(R.id.textViewHotelAddress);
               ImageView imageViewHotel = findViewById(R.id.imageViewHotel);
               hotelRating = findViewById(R.id.hotelRating);
               ratinginwords = findViewById(R.id.ratinginwords);
               ratingcount = findViewById(R.id.ratingcount);
               ImageView otherImage1 = findViewById(R.id.otherImage1);
               ImageView otherImage2 = findViewById(R.id.otherImage2);
               if (hotel.getOtherImages() != null && hotel.getOtherImages().size() > 0) {
                   Picasso.get().load(hotel.getOtherImages().get(0)).into(otherImage1);
                   Picasso.get().load(hotel.getOtherImages().get(1)).into(otherImage2);
               }

               FirebaseAuth mAuth = FirebaseAuth.getInstance();
               FirebaseUser user = mAuth.getCurrentUser();

               if (user != null) {
                   String userId = user.getUid();

                   // Get reference to the user's information node in the database
                   DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("userInfo");
                   userRef.addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                           UserInfo userInfo = snapshot.getValue(UserInfo.class);
                            selectedCountry = userInfo.getSelectedCountry();
                           databaseReference=FirebaseDatabase.getInstance()
                                   .getReference("countries").child(selectedCountry)
                                   .child("states").child(hotel.getState())
                                   .child("cities")
                                   .child(hotel.getCity())
                                   .child("hotels")
                                   .child(hotel.getId())
                                   .child("ratings");
                           databaseReference.addValueEventListener(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   ratings =snapshot.getValue(Ratings.class);
                                   if (ratings !=null) {
//                       Toast.makeText(HotelDetailActivity.this, "Ratings" + ratings.getAverageRating(), Toast.LENGTH_SHORT).show();
                                       averageRating = ratings.getAverageRating();
                                       numRatings = ratings.getNumRatings();
                                       totalRating = ratings.getTotalRating();
                                       hotelRating.setText(String.valueOf(averageRating));
                                       ratinginwords.setText(ratings.setRatinginwords());
                                       String count =  String.valueOf(numRatings);
                                       ratingcount.setText("(" + count + ")");
                                   }
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError error) {

                       }
                   });
                   // Store the selected country under a new node (e.g., "selectedCountry")

               }



               ratingBar = findViewById(R.id.ratingBar);
               submit = findViewById(R.id.submit);

               submit.setVisibility(View.GONE);




               ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                   @Override
                   public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                       // Change visibility of the button based on the rating change
                       if (rating > 0) {
                           submit.setVisibility(View.VISIBLE);
                           submit.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   // Assuming you have the 'newRating' value, and 'hotel' object already defined
//                                   Toast.makeText(HotelDetailActivity.this, "New rating" + rating, Toast.LENGTH_SHORT).show();
                                  updateRatingInFirebase(rating, databaseReference);
//                                   hotelRating.setText(String.valueOf(rating));
                               }
                           });
                       } else {
                           submit.setVisibility(View.GONE);
                       }
                   }
               });

               textViewHotelName.setText(hotel.getName());
               textViewHotelAddress.setText(hotel.getAddress());

               Picasso.get().load(hotel.getImageUrl()).into(imageViewHotel);
           }
        } else {
            // Handle the case where no hotel details are provided
            Toast.makeText(this, "Error: No hotel details found", Toast.LENGTH_SHORT).show();
            finish(); // Finish the activity if there's an error
        }

        //set min and max values to rooms picker
        numberPickerRooms = findViewById(R.id.numberPickerRooms);
        numberPickerRooms.setMinValue(1);
        numberPickerRooms.setMaxValue(10); // Set your desired maximum value here
        numberPickerRooms.setValue(1); // Set an initial value

        numberPickerRooms.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.drawable.np_number_picker_pressed);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundResource(R.drawable.np_number_picker_default);
            }
            return false;
        });

        numberPickerRooms.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.setBackgroundResource(R.drawable.np_number_picker_focused);
            } else {
                v.setBackgroundResource(R.drawable.np_number_picker_default);
            }
        });

        //choose dates functionality
        buttonDatePicker = findViewById(R.id.buttonDatePicker);
        buttonDatePicker2 = findViewById(R.id.buttonDatePicker2);
        buttonDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(buttonDatePicker);
            }
        });
        buttonDatePicker2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(buttonDatePicker2);
            }
        });


        buttonBookHotel = findViewById(R.id.buttonBookHotel);
        textViewHotelName = findViewById(R.id.textViewHotelName);
        textViewHotelAddress = findViewById(R.id.textViewHotelAddress);
//        TextView textViewSelectedDates = findViewById(R.id.textViewSelectedDates);
        buttonBookHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(buttonDatePicker.getText().toString().equals("Starting Date") || buttonDatePicker2.getText().toString().equals("Ending Date")){
                    Toast.makeText(HotelDetailActivity.this, "choose the duration", Toast.LENGTH_SHORT).show();
                    return;
                }


                Intent intent = getIntent();
                String ImageUrl="demo2";
                if (intent != null && intent.hasExtra("hotel")) {
                    Hotel hotel = intent.getParcelableExtra("hotel");
                    ImageUrl=hotel.getImageUrl();
                    Intent intent1 = new Intent(HotelDetailActivity.this, PaymentGateway.class);
                    HotelBookedInfo hotelBookedInfo = new HotelBookedInfo(textViewHotelName.getText().toString(),textViewHotelAddress.getText().toString(),numberPickerRooms.getValue()
                           ,buttonDatePicker.getText().toString(),buttonDatePicker2.getText().toString(),ImageUrl);
                    intent1.putExtra("hotel",hotel);
                    intent1.putExtra("hotelBookedInfo",hotelBookedInfo);
                    startActivity(intent1);

                }

                //save this hotel booked details under logged in user in firebase realtime database
                //get email of person logged in
                //get hotel name, address, image url, dates, rooms, days staying
                //save this data under user email in firebase realtime database
//                FirebaseUser user = mAuth.getCurrentUser();
//                if(user!=null){
//                    String LoggedUserEmail = user.getUid();
//                    databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(LoggedUserEmail).child("bookedHotels");
//                    String UserId=databaseReference.push().getKey();
//                    Date selectedDate = new Date(selectedDates);
//                    HotelBookedInfo hotelBookedInfo = new HotelBookedInfo(textViewHotelName.getText().toString(),textViewHotelAddress.getText().toString(),numberPickerRooms.getValue()
//                            ,buttonDatePicker.getText().toString(),buttonDatePicker2.getText().toString(),ImageUrl);
//                    databaseReference.child(UserId).setValue(hotelBookedInfo);
//
//                }else{
//                    Toast.makeText(HotelDetailActivity.this, "Error: No user logged in", Toast.LENGTH_SHORT).show();
//                }

            }
        });


        buttonBookHotel.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Button pressed, scale down
                    scaleButton(buttonBookHotel, 0.95f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Button released or touch canceled, scale back to the original size
                    scaleButton(buttonBookHotel, 1f);
                    break;
            }
            return false;
        });

    }

    private void updateRatingInFirebase(float newRating, DatabaseReference databaseReference) {

        // Retrieve the current total rating and the number of ratings
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    float totalRating = dataSnapshot.child("totalRating").getValue(Float.class);
                    int numRatings = dataSnapshot.child("numRatings").getValue(Integer.class);
                    formattednewRating = String.format("%.1f", newRating);
                    if (formattednewRating.matches("\\d+\\.0")) {
                        formattednewRating = String.format("%.1f", newRating + 0.1);
                    }
// Update the total rating and number of ratings
                    totalRating += newRating;
                    numRatings++;

// Calculate the average rating
                    float averageRating = totalRating / numRatings;



// Format values with one decimal place
                 formattedTotalRating = String.format("%.1f", totalRating);
                     formattedAverageRating = String.format("%.1f", averageRating);

// Add 0.1 if the first decimal place is 0
                    if (formattedTotalRating.matches("\\d+\\.0")) {
                        formattedTotalRating = String.format("%.1f", totalRating + 0.1);
                    }
                    if (formattedAverageRating.matches("\\d+\\.0")) {
                        formattedAverageRating = String.format("%.1f", averageRating + 0.1);
                    }

// Update the Firebase node with the new values
                    databaseReference.child("totalRating").setValue(Float.parseFloat(formattedTotalRating));
                    databaseReference.child("numRatings").setValue(numRatings);
                    databaseReference.child("averageRating").setValue(Float.parseFloat(formattedAverageRating));




                }
                Toast.makeText(HotelDetailActivity.this, "Rating will be updated shortly.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
    }








    private void showDatePickerDialog(Button buttonDatePickerx) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Handle the selected date
                selectedDates = dayOfMonth + "/" + (month + 1) + "/" + year;
                //buttonDatePicker.setText(selectedDates);
                buttonDatePickerx.setText(selectedDates);
            }
        };
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // Create and show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                HotelDetailActivity.this,
                dateSetListener,
                year, month, day
        );
        datePickerDialog.show();
    }

    private void scaleButton(Button button, float scale) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", scale);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", scale);
        scaleDownX.setDuration(150);
        scaleDownY.setDuration(150);
        scaleDownX.start();
        scaleDownY.start();
    }

}