package com.example.produtivhead.Notifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

import com.example.produtivhead.DB.NotificationDAO;
import com.example.produtivhead.R;

import java.util.List;

public class CustomNotificationButtonAdapter extends ArrayAdapter<CustomNotificationButton> {

    //Variables
    private Activity activity;
    private List<CustomNotificationButton> customNotificationButtons;
    private Context c;

    public CustomNotificationButtonAdapter(Activity activity, Context context, List<CustomNotificationButton> customNotificationButtons) {
        super(context, 0, customNotificationButtons);
        this.activity = activity;
        this.c = context;
        this.customNotificationButtons = customNotificationButtons;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        //Récupération du CustomNotificationButton
        final CustomNotificationButton customNotificationButton = getItem(position);

        //Création d'une view si elle n'existe pas encore
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_notification_button, parent, false);
        }

        CustomNotificationButtonHolder viewHolder = (CustomNotificationButtonHolder) convertView.getTag();

        if(viewHolder == null){

            //Instanciation d'un CustomNotificationButtonHolder
            viewHolder = new CustomNotificationButtonHolder();
            viewHolder.content = convertView.findViewById(R.id.textViewNotificationButton);
            viewHolder.aSwitch = convertView.findViewById(R.id.switchNotificationButton);
            viewHolder.menuButton = convertView.findViewById(R.id.imageButtonNotificationButton);


            convertView.setTag(viewHolder);

            //Affichage du dialogue de configuration si on clique sur le boutton
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ConfigActivity.class);
                    intent.putExtra("notificationId", customNotificationButton.getId());
                    c.startActivity(intent);
                }
            });

            //Activation ou annulation de la notification en fonction du changement d'état du switch
            viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b)
                        NotificationManager.scheduleNotification(getContext(), customNotificationButton.getId());
                    else
                        NotificationManager.cancelNotification(getContext(), customNotificationButton.getId());
                }
            });

            //Instanciation des menus
            final PopupMenu popupMenu = new PopupMenu(getContext(), viewHolder.menuButton, Gravity.START);
            popupMenu.inflate(R.menu.notification_button_menu);

            //Actions effectuées lorsqu'on clique sur un item du menu
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    //En fonction de l'id de l'item, l'action sera différente
                    switch (menuItem.getItemId()) {

                        //Action = modifier
                        case R.id.actionModifier:

                            //Initialisation du EditText
                            final EditText input = new EditText(getContext());
                            input.setHint("Entrez le contenu de la notification");
                            input.setText(customNotificationButton.getContent());

                            //Création du dialogue permettant d'entrer le contenu de la notification
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                            //Modification du CustomNotificationButton lorsqu'on valide
                            builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //Update du contenu du CustomNotificationButton
                                    customNotificationButton.setContent(input.getText().toString());

                                    //Annulation / plannification de la notification pour s'adapter au changement de contenu
                                    NotificationManager.cancelNotification(getContext(), customNotificationButton.getId());
                                    NotificationManager.scheduleNotification(getContext(), customNotificationButton.getId());

                                    //Sauvegarde des données dans la base de données
                                    NotificationDAO notificationDAO = new NotificationDAO(getContext());
                                    notificationDAO.open();
                                    Notification notification = notificationDAO.getNotification(customNotificationButton.getId());
                                    notification.setContent(customNotificationButton.getContent());
                                    notificationDAO.deleteNotification(customNotificationButton.getId());
                                    notificationDAO.addNotification(notification);
                                    notificationDAO.close();
                                }
                            });

                            //Annulation de la création du CustomNotificationButton
                            builder.setNegativeButton("Annuler", null);

                            //Setup de la View du dialogue
                            builder.setView(input);

                            //Création du dialogue
                            Dialog dialog = builder.create();

                            //Affichage du dialogue
                            dialog.show();
                            break;

                        //Action = supprimer
                        case R.id.actionSupprimer:

                            //Annulation de la notification
                            NotificationManager.cancelNotification(getContext(), customNotificationButton.getId());

                            //Sauvegarde de l'état de la notification
                            NotificationDAO notificationDAO = new NotificationDAO(getContext());
                            notificationDAO.open();
                            notificationDAO.deleteNotification(customNotificationButton.getId());
                            notificationDAO.close();

                            //Suppression du boutton dans la liste
                            customNotificationButtons.remove(position);

                            //Actualisation de la ListView
                            CustomNotificationButtonAdapter.this.notifyDataSetChanged();

                            break;
                    }
                    return false;
                }
            });

            //Affichage du menu lorsqu'on clique sur le boutton
            viewHolder.menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.show();
                }
            });

        }

        //Setup de l'état du switch en fonction de si la notification est activée ou non
        NotificationDAO notificationDAO = new NotificationDAO(getContext());
        notificationDAO.open();
        Notification notification = notificationDAO.getNotification(customNotificationButton.getId());
        notificationDAO.close();
        viewHolder.aSwitch.setChecked(notification.getActive() == 1);

        //Mise en place du texte du boutton
        viewHolder.content.setText(customNotificationButton.getContent());

        return convertView;
    }

    private class CustomNotificationButtonHolder {
        public TextView content;
        public Switch aSwitch;
        public ImageButton menuButton;
    }

}