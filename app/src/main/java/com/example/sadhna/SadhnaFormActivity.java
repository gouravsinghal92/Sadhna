package com.example.sadhna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import android.view.View;
import android.widget.Button;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;

import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SadhnaFormActivity extends AppCompatActivity {

    private static String mToken = null;
    private EditText etName, etRounds, etDate, etTime, etbookReadyToday;

    private EditText etreadingTime, etlectureTopic, etlectureSpeaker, ethearingDuration, etdayRest, ettotalSleep, etmorningProgramStartTime;

    private  EditText etmorningProgramEndingTime, etroundsTill7AM, et16RoundTime;

    private static final String SPREADSHEET_ID = "1I-QZ_xm1ukCFpWFYbaApmaBhXH7QSwcXhYjGfh1Vkik";

    private Sheets sheetsService;
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private Switch alarmSwitch;

    private int REQUEST_CODE = 9878;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sadhna_form);

        // Initialize form fields
        etName = findViewById(R.id.etName);
        etRounds = findViewById(R.id.etRounds);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etbookReadyToday = findViewById(R.id.bookReadyToday);
        etroundsTill7AM = findViewById(R.id.roundsTill7AM);
        etdayRest = findViewById(R.id.dayRest);
        ethearingDuration = findViewById(R.id.hearingDuration);
        etlectureSpeaker = findViewById(R.id.lectureSpeaker);
        etmorningProgramEndingTime = findViewById(R.id.morningProgramEndingTime);
        etmorningProgramStartTime = findViewById(R.id.morningProgramStartTime);
        etreadingTime = findViewById(R.id.readingTime);
        etlectureTopic = findViewById(R.id.lectureTopic);
        ettotalSleep = findViewById(R.id.totalSleep);
        et16RoundTime = findViewById(R.id.et16RoundsTime);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String userName =  sharedPreferences.getString("Name", "").toString();
        TextView prabhuNameTextView = findViewById(R.id.prabhuName);
        prabhuNameTextView.setText("Hari Bol " + userName + " prabhu!");

        // Set the default date to the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        etDate.setText(currentDate);
        etDate.setFocusable(false); // Disable editing

        // Set the default time to the current time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = timeFormat.format(Calendar.getInstance().getTime());
        etTime.setText(currentTime);
        etTime.setFocusable(false); // Disable editing

        // Initialize the Switch
        alarmSwitch = findViewById(R.id.alarmSwitch);

        // Check the current alarm state (if it's scheduled)
        boolean isAlarmOn = AlarmScheduler.isAlarmScheduled(this);
        alarmSwitch.setChecked(isAlarmOn);

        // Set a listener for the Switch
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Turn on the alarm
                    // Check if the app has the SET_ALARM permission
                    AlarmScheduler.scheduleAlarm(SadhnaFormActivity.this);

                } else {
                    // Turn off the alarm
                    AlarmScheduler.cancelAlarm(SadhnaFormActivity.this);
                }
            }
        });

        // Initialize the Google Sheets API
        try {
            initializeSheetsAPI();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFormData();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                AlarmScheduler.scheduleAlarm(SadhnaFormActivity.this);

            } else {
                // Permission is denied, handle accordingly (e.g., show a message or disable alarm functionality)
                AlarmScheduler.cancelAlarm(SadhnaFormActivity.this);
            }
        }
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = SadhnaFormActivity.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline")
                .build();

        // Authorize with credentials
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .build()
                .setAccessToken(mToken)
                .setExpirationTimeMilliseconds(Long.MAX_VALUE);

        return credential;
    }

    private void initializeSheetsAPI() throws GeneralSecurityException, IOException {
        // Initialize credentials and service
        // Build a new authorized API client service.

        SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
        mToken = preferences.getString("token", "");

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = SadhnaFormActivity.class.getResourceAsStream("/serviceaccount.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        Credentials credentials = ServiceAccountCredentials.fromStream(in);

        // Create an HttpRequestInitializer using the credentials
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        sheetsService =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                        .setApplicationName("Sadhna App")
                        .build();
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Perform your background operation
            saveDataToServer();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // This method is executed on the UI thread after the background task completes.
            // You can update the UI here.
        }
    }

    private List<Object> data = null;
    private void saveFormData() {

        //Get shared preference data
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String userName =  sharedPreferences.getString("Name", "").toString();
        String mobileNumber = sharedPreferences.getString("Mobile", "").toString();
        String facilitator = sharedPreferences.getString("Facilitator", "").toString();

        // Get user input
        String name = etName.getText().toString();
        String roundsStr = etRounds.getText().toString();
        int rounds = Integer.parseInt(roundsStr); // Parse the number of rounds
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        String bookReadyToday = etbookReadyToday.getText().toString();
        int readingTime = Integer.parseInt(etreadingTime.getText().toString());
        String lectureTopic = etlectureTopic.getText().toString();
        String lectureSpeaker = etlectureSpeaker.getText().toString();
        int hearingDuration = Integer.parseInt(ethearingDuration.getText().toString());
        int dayRest = Integer.parseInt(etdayRest.getText().toString());
        int totalSleep = Integer.parseInt(ettotalSleep.getText().toString());
        String morningTimeProgramTime = etmorningProgramStartTime.getText().toString();
        String morningTimeEndTime = etmorningProgramEndingTime.getText().toString();
        int roundsTill7AM = Integer.parseInt(etroundsTill7AM.getText().toString());
        String roundTime16thRound = et16RoundTime.getText().toString();

        // Process and save the data (you can customize this part)
        String message = "Name: " + name + "\nRounds: " + rounds + "\nDate: " + date + "\nTime: " + time;
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // You can further save this data to a database or send it to Google Sheets
        // Implement the data storage logic here
        // Create a list of values to be added to the spreadsheet
        data = Arrays.asList(userName, mobileNumber, facilitator, rounds, date, time, bookReadyToday, readingTime, lectureSpeaker, lectureTopic, hearingDuration, dayRest, totalSleep, morningTimeProgramTime, morningTimeEndTime, roundsTill7AM, roundTime16thRound);

        new BackgroundTask().execute();
    }

    private void saveDataToServer() {

        final UpdateValuesResponse[] result = new UpdateValuesResponse[1];

        ValueRange body = new ValueRange().setValues(Collections.singletonList(data));

        try {
            AppendValuesResponse response = sheetsService.spreadsheets().values()
                    .append(SPREADSHEET_ID, "Sheet1", body)
                    .setValueInputOption("RAW")
                    .execute();

            if(response != null) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SadhnaFormActivity.this, "Hari Bol! Data Saved!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}