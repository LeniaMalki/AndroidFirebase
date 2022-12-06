package fr.eurecom.firebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;


public class MyContactAdapter extends ArrayAdapter<MyContact> {


    public MyContactAdapter(@NonNull Context context, ArrayList<MyContact> contacts) {
        super(context, 0, contacts);
    }

    public View getView(int position, View view, ViewGroup parent) {
        MyContact contact_person = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.row_contacts, parent, false);
        }
        TextView txtName = view.findViewById(R.id.namesurname);
        TextView txtPhone = view.findViewById(R.id.number);

        txtName.setText(contact_person.name);
        txtPhone.setText(contact_person.phone);

        return view;
    }

}
