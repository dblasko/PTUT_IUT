package fr.anerdunicorn.notification;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Locale;

public class ConfigActivity extends AppCompatActivity {

    private ConstraintLayout layoutRepeatable;
    private ConstraintLayout layoutDate;
    private int notificationId;
    private Calendar time;
    private RadioButton radioButtonRepeatable;
    private RadioButton radioButtonDate;
    private Calendar now;
    private TextView textViewDate;
    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        //Récupération de l'id de la notification
        notificationId = getIntent().getIntExtra("notificationId", 0);

        //Récupération de la notification correspondante dans la base de données
        NotificationDatabaseManager notificationDatabaseManager = new NotificationDatabaseManager(this);
        notificationDatabaseManager.open();
        notification = notificationDatabaseManager.getNotification(notificationId);
        notificationDatabaseManager.close();
        System.out.println("DAY: " + notification.getDay());

        //Initialisation des RadioButtons
        radioButtonRepeatable = findViewById(R.id.radioButtonRepeatable);
        radioButtonDate = findViewById(R.id.radioButtonDate);

        //Initialisation des layouts de répétition et de date
        layoutRepeatable = findViewById(R.id.layoutRepeatable);
        layoutDate = findViewById(R.id.layoutDate);

        //Initialisation de la TextView de la date
        textViewDate = findViewById(R.id.textViewDate);

        //Initialisation d'un calendrier à l'instant t
        now = Calendar.getInstance();

        //Initialisation du timepicker
        final TimePicker timePicker = findViewById(R.id.configTimePicker);
        timePicker.setIs24HourView(true);

        //Initialisation d'un calendrier a l'heure de la notification et changement de l'heure du TimePicker
        time = Calendar.getInstance();
        if(notification.getYear() != -1) {
            time.set(notification.getYear(), notification.getMonth(), notification.getDay(), notification.getHour(), notification.getMinute());
            timePicker.setHour(notification.getHour());
            timePicker.setMinute(notification.getMinute());
        }
        else {
            time.setTimeInMillis(now.getTimeInMillis());
            timePicker.setHour(8);
            timePicker.setMinute(0);
        }

        if(notification.getRepeatable() == 1) {
            layoutDate.setVisibility(View.INVISIBLE);

            //Conversion de l'entier days en tableau de booleans
            boolean[] days = Notification.intToDays(notification.getDays());

            //Setup de l'état des CheckBox du layout de répétition en fonction de leur valeur dans les SharedPreferences
            int i = 0;
            for(Days day : Days.values()) {
                CheckBox checkBox = layoutRepeatable.findViewWithTag(day.toString());
                checkBox.setChecked(days[i++]);
            }
        }
        else {
            //Changement du radioButton activé
            radioButtonDate.setChecked(true);

            //Setup du layout
            layoutRepeatable.setVisibility(View.INVISIBLE);
            layoutDate.setVisibility(View.VISIBLE);
            textViewDate.setText(getDateString());
        }

        Button buttonAccept = findViewById(R.id.configButtonAccept);
        Button buttonCancel = findViewById(R.id.configButtonCancel);
        Button buttonChangeDate = findViewById(R.id.configButtonChangeDate);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                final RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                textViewDate.setText(getDateString());
                if(radioButton.getText().equals("Date")){
                    layoutRepeatable.setVisibility(View.INVISIBLE);
                    layoutDate.setVisibility(View.VISIBLE);
                }
                else{
                    layoutDate.setVisibility(View.INVISIBLE);
                    layoutRepeatable.setVisibility(View.VISIBLE);
                }
            }
        });



        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Création d'un boolean pour finir l'activité seulement si la date est bonne
                boolean validated = true;

                //Setup du calendar à l'heure choisie
                time.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                time.set(Calendar.MINUTE, timePicker.getMinute());
                time.set(Calendar.SECOND, 0);

                if(radioButtonRepeatable.isChecked()) {
                    boolean[] days = new boolean[7];
                    int i = 0;
                    for(Days day : Days.values()) {
                        CheckBox checkBox = layoutRepeatable.findViewWithTag(day.toString());
                        days[i] = checkBox.isChecked();
                        i++;
                    }
                    notification.setDays(Notification.daysToInt(days));
                    notification.setRepeatable(1);
                }
                else {
                    if(time.after(now)) {
                        notification.setRepeatable(0);
                    }
                    else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Impossible de plannifier une notification pour une date antérieure", Toast.LENGTH_SHORT);
                        ((TextView)((LinearLayout)toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER_HORIZONTAL);
                        toast.show();
                        validated = false;
                    }
                }

                if(validated) {
                    //Modification de la notification
                    notification.setYear(time.get(Calendar.YEAR));
                    notification.setMonth(time.get(Calendar.MONTH));
                    notification.setDay(time.get(Calendar.DAY_OF_MONTH));
                    notification.setHour(time.get(Calendar.HOUR_OF_DAY));
                    notification.setMinute(time.get(Calendar.MINUTE));

                    //Modification de la notification dans la base de données
                    NotificationDatabaseManager notificationDatabaseManager = new NotificationDatabaseManager(getApplicationContext());
                    notificationDatabaseManager.open();
                    notificationDatabaseManager.updateNotification(notification);
                    notificationDatabaseManager.close();

                    //Changement de l'état du switch (annulation et plannification automatique de la notification)


                    finish();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buttonChangeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDate();
            }
        });

    }

    private String getDateString() {
        //Retourne la date choisie sous form de string
        String month = time.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        return ("Date choisie : " + time.get(Calendar.DAY_OF_MONTH) + " " + month + " " + time.get(Calendar.YEAR));
    }

    private void changeDate() {
        DatePickerDialog dpd = new DatePickerDialog(ConfigActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                time.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                textViewDate.setText(getDateString());
            }
        }, time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

}
