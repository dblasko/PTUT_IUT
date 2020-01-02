package com.example.testapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class acceuil_todolist extends AppCompatActivity{


    public static final String nom ="rien pour linstant";
    private EditText text;
    private DatePickerDialog.OnDateSetListener dateurDeNotifDeb;
    private DatePickerDialog.OnDateSetListener dateurDeNotifFin;
    private TextView dateDebut;

    /*private ListView listeTache;
    private ArrayAdapter<String> adapter;
    private ArrayList<String>  arrayList;
    private EditText RecupnomTache;
    private Button btn;*/
    private TimePickerDialog.OnTimeSetListener timeurNotif;
    private TextView heure;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acceuil_todolist);




        /*
        listeTache = (ListView) findViewById(R.id.listeT);
        String[] items= {"Lina"};
        arrayList= new ArrayList<String>(Arrays.asList(items));
        adapter= new ArrayAdapter<String>(this, R.layout.liste_taches,R.id.listeT,arrayList);
        listeTache.setAdapter(adapter);
        RecupnomTache= (EditText) findViewById(R.id.nomTache);
        btn= (Button) findViewById(R.id.valideur);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newItem= RecupnomTache.getText().toString();
                arrayList.add(newItem);
                adapter.notifyDataSetChanged();
            }
        });*/
        /*Button valideur = (Button) findViewById(R.id.valideur);
        valideur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.i("Lina","Omg");
                Toast.makeText(getApplicationContext(), "C'est Validé !", Toast.LENGTH_SHORT).show();
            }
        });*/

//-------------TIMEUR DE DATE DE DEBUT !!!

        heure = (TextView) findViewById(R.id.heure);
        heure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(acceuil_todolist.this, R.style.timePickerDialogS,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        /*String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }*/
                        heure.setText(String.format("%02d:%02d", hourOfDay, minutes) );
                    }
                }, currentHour, currentMinute, true);
                timePickerDialog.show();
            }
        });





        dateDebut = (TextView) findViewById(R.id.debut);
        dateDebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendrier = Calendar.getInstance();
                int annee = calendrier.get(Calendar.YEAR);
                int mois = calendrier.get(Calendar.MONTH);
                int jour = calendrier.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialogueur = new DatePickerDialog(acceuil_todolist.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateurDeNotifDeb, annee, mois, jour);
                dialogueur.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogueur.show();

            }
        });

        dateurDeNotifDeb = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int annee, int mois, int jour) {
                mois= mois + 1;
                Log.d("MainActivity","onDateSet : jj/mm/yy : " + jour +"/"+mois+"/"+annee);
                String date;
                if(mois <10){
                    date = jour +"/0"+mois+"/"+annee;
                }
                else{
                    date= jour +"/"+mois+"/"+annee;
                }

                dateDebut.setText(date);
            }
        };

        //DEUXIEME DATE QUI EST LA DATE DE FIN DE TACHE
        final TextView dateFin = (TextView) findViewById(R.id.fin);
        dateFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendrier = Calendar.getInstance();
                int annee = calendrier.get(Calendar.YEAR);
                int mois = calendrier.get(Calendar.MONTH);
                int jour = calendrier.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialogueur = new DatePickerDialog(acceuil_todolist.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateurDeNotifFin, annee, mois, jour);
                dialogueur.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogueur.show();

            }
        });

        dateurDeNotifFin = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int annee, int mois, int jour) {
                mois= mois + 1;
                Log.d("MainActivity","onDateSet : jj/mm/yy : " + jour +"/"+mois+"/"+annee);
                String date;
                if(mois <10){
                    date = jour +"/0"+mois+"/"+annee;
                }
                else{
                    date= jour +"/"+mois+"/"+annee;
                }

                dateFin.setText(date);
            }
        };
    }



    public void envoyer(View view){


        Toast.makeText(getApplicationContext(), "C'est Validé !", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,liste_taches.class);
        EditText editText = (EditText) findViewById(R.id.nomTache);
        String nomTt= editText.getText().toString();

        //Initialisation de l'accès à la base de données pour sauvegarder la tâche créée
        TachesDAO tachesDAO = new TachesDAO(this);
        tachesDAO.open();

        //Sauvegarde de la tâche dans la bd
        tachesDAO.addTache(nomTt);

       startActivity(intent);

    }



    public void obtenirCalend(View v) {

        RadioButton notificationb = (RadioButton) findViewById(R.id.notificationb);
        boolean coche = ((RadioButton) v).isChecked();

        switch (v.getId()) {

            case R.id.notificationb:
                if (coche) {
                    Toast.makeText(getApplicationContext(), "Vous serez notifié !", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }



    public void liste_taches(View view){
        startActivity(new Intent(this,liste_taches.class));
    }
    }
