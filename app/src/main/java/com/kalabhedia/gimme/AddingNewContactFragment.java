package com.kalabhedia.gimme;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddingNewContactFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static DatabaseReference NotificationReferernce;
    private static Context context;
    ArrayList<String> contactName;
    ArrayList<String> contactNumber;
    ArrayList<HashMap<String, String>> contactdetail;
    ArrayList<String> contactView;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    private int READ_CONTACT_PERMISSION = 1;
    private String senderUserID;
    private String receiverKey;
    EditText amount;
    String amountEntered;
    private String number;

    public static void sendNotificationToUser(String senderUserID, String receiverUserID, String phoneNumber, String amountEntered) {
        HashMap<String, String> notificationData = new HashMap<>();
        notificationData.put("phone_number", phoneNumber);
        notificationData.put("Amount", amountEntered);
        notificationData.put("From", senderUserID);
        notificationData.put("Type", "request");
        NotificationReferernce.child(receiverUserID).push().setValue(notificationData).addOnFailureListener(e ->
                Toast.makeText(context, "Error in sending data ", Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Notifications");
            Query applesQuery = ref.child(receiverUserID).orderByChild("From").equalTo(senderUserID);

            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                        appleSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Notification", "onCancelled", databaseError.toException());
                }
            });
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        NotificationReferernce = FirebaseDatabase.getInstance().getReference().child("Notifications");
        ((MainActivity) getActivity()).actionbar.setTitle("Add Bill");
        contactName = new ArrayList<>();
        contactNumber = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_adding_new_contact, container, false);
        contactdetail = new ArrayList<>();
        amount = view.findViewById(R.id.amount_entry);


        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Gimme", Context.MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString("phonenumber", null);
        senderUserID = sharedPreferences.getString("Current_user_id", null);
        Log.w("Sender id", senderUserID + " ");
        context = getContext();
        Button clearText = view.findViewById(R.id.bn_clear_txt);

        AutoCompleteTextView contact = view.findViewById(R.id.contacts);
        if (checkExternalPermission()) {
            clearText.setOnClickListener(view13 -> {
                number = null;
                contact.setText("");
                contact.setFocusableInTouchMode(true);
            });
            getActivity().getSupportLoaderManager().initLoader(1, null, this);
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, contactName);
            SimpleAdapter adapter = new SimpleAdapter(getContext(),
                    contactdetail,
                    R.layout.name_number_view,
                    new String[]{"Name", "Number"},
                    new int[]{R.id.line_a, R.id.line_b});
            contact.setThreshold(1);
            contact.setAdapter(adapter);
            contact.setOnItemClickListener((adapterView, view12, i, l) -> {
                HashMap<String, String> selected = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                contact.setText(selected.get("Name"));
                number = selected.get("Number");
                contact.setFocusable(false);
            });
        } else
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, READ_CONTACT_PERMISSION);
        Button button = view.findViewById(R.id.bn_save);
        button.setOnClickListener(view1 -> {
            amountEntered = amount.getText().toString();
            if (number != null) {
                if (!amountEntered.isEmpty()) {
                    amountEntered = "₹" + amountEntered;
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference("Users").addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        Log.w("Device numbers", data.child("device_number").getValue().toString());
                                        String[] conversion = data.child("device_number").getValue().toString().split(" ");
                                        String converted = "";
                                        for (String i : conversion) {
                                            converted += i;
                                        }
                                        if (converted.equals(number)) {
                                            Log.w("result", "number present");
                                            receiverKey = data.getKey();
                                            Log.w("receiverKey", receiverKey);
                                        }
                                    }
                                    if (receiverKey == null) {
                                        Toast.makeText(getContext(), "User does not have this app", Toast.LENGTH_SHORT).show();
                                        //todo receiver not found in database
                                    } else {
                                        sendNotificationToUser(senderUserID, receiverKey, phoneNumber, amountEntered);
                                        OneFragment.fab.setVisibility(View.VISIBLE);
                                        ((MainActivity) getActivity()).viewPager.setVisibility(View.VISIBLE);
                                        amount.setFocusable(false);
                                        contact.setFocusable(false);
                                        ((MainActivity) getActivity()).actionbar.setTitle("Gimme");
                                        getFragmentManager().beginTransaction()
                                                .remove(AddingNewContactFragment.this).commit();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.w("MyApp", "getUser:onCancelled", databaseError.toException());
                                }
                            });
                    ((MainActivity) getActivity()).viewPager.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "Amount field can't be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Contact Field can't be empty", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Loader<Cursor> cursorLoader = new CursorLoader(getActivity(), CONTENT_URI, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        HashMap<String, String> item;
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            contactName.add(name);
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactNumber.add(number);
            item = new HashMap<>();
            item.put("Name", name);
            item.put("Number", number);
            contactdetail.add(item);
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).tabLayout.setVisibility(View.VISIBLE);
    }

    private boolean checkExternalPermission() {
        String permission = android.Manifest.permission.READ_CONTACTS;
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


}
