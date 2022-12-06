package fr.eurecom.firebase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private final ArrayList<MyContact> contactArray = new ArrayList<>();
    EditText name;
    EditText number;
    private MyContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Asking user for permission if not already granted
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission: ", "To be checked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else Log.i("Permission: ", "GRANTED");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference mFirebaseInstance = database.getReference("Contacts");

        final Button btn = findViewById(R.id.goButton);
        btn.setOnClickListener(v -> {

            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            @SuppressLint("Recycle") Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            MyContact myContact = new MyContact();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            @SuppressLint("Recycle") Cursor names = getContentResolver().query(uri, projection, null, null, null);
            int indexName = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int indexNumber = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);


            names.moveToPosition(-1);
            cur.moveToPosition(-1);

            while (cur.moveToNext()) {
                while (names.moveToNext()) {
                    myContact.name = names.getString(indexName);
                    myContact.phone = names.getString(indexNumber);
                    String key = mFirebaseInstance.push().getKey();
                    assert key != null;
                    mFirebaseInstance.child(key).setValue(myContact);

                }
            }
        });

        ListView listView = findViewById(R.id.contactList);

        Collections.sort(contactArray, MyContact.nameAscending);
        adapter = new MyContactAdapter(this, contactArray);
        listView.setAdapter(adapter);

        mFirebaseInstance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactArray.clear();
                adapter.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MyContact read_contact = snapshot.getValue(MyContact.class);
                    contactArray.add(read_contact);
                    adapter.notifyDataSetChanged();
                }

                Collections.sort(contactArray, MyContact.nameAscending);
                adapter = new MyContactAdapter(getApplicationContext(), contactArray);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {

            TextView v = view.findViewById(R.id.number);
            Toast.makeText(getApplicationContext(), "selected Item Number is " + v.getText(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + v.getText()));
            startActivity(intent);

        });

        final Button delAllButton = findViewById(R.id.deleteAll);
        delAllButton.setOnClickListener(v -> {

            if (!contactArray.isEmpty()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Delete all contacts?").setTitle("Warning");
                builder.setPositiveButton("YES", (dialog, id) -> {

                    DatabaseReference nodeKeyRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
                    nodeKeyRef.removeValue();
                    Toast toast = Toast.makeText(getApplicationContext(), "All contacts deleted", Toast.LENGTH_LONG);
                    toast.show();
                });

                builder.setNegativeButton("NO", (dialogInterface, id) -> {
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "No contacts to delete", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        name = findViewById(R.id.ïnputName);
        number = findViewById(R.id.inputNumber);
        final Button createContact = findViewById(R.id.createContact);
        createContact.setOnClickListener(v -> {
            MyContact newContact = new MyContact();
            newContact.name = name.getText().toString();
            newContact.phone = number.getText().toString();
            String key = mFirebaseInstance.push().getKey();
            assert key != null;
            mFirebaseInstance.child(key).setValue(newContact);
            contactArray.add(newContact);
            Collections.sort(contactArray, MyContact.nameAscending);
            adapter = new MyContactAdapter(getApplicationContext(), contactArray);
            listView.setAdapter(adapter);

            number.setText(null);
            name.setText(null);

        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("Access:", "Now permissions are granted");
            } else {
                Log.i("Access:", " permissions are denied");
            }
        }
    }

}