package com.bombbomb.bombsight;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by cos-mbp-don on 3/22/17.
 */

public class AddAddressLocationDialog implements Runnable {

    public interface AddAddressLocationCallbacks {
        void OnAddressLocationSaveClicked(String streetAddress, String zipCode);
    }

    private AddAddressLocationCallbacks callbacks;
    private Activity appContext;
    private EditText streetAddressEditText;
    private EditText zipCodeEditText;

    public AddAddressLocationDialog(AddAddressLocationCallbacks callingClass, Activity context){
        this.callbacks = callingClass;
        this.appContext = context;
    }


    @Override
    public void run() {
        final Dialog dialog = new Dialog(appContext);
        // build custom dialog from XML layout
        dialog.setContentView(R.layout.add_address_location);
        dialog.setTitle("Enter an address");

        // get the label EditText
        streetAddressEditText = (EditText) dialog.findViewById(R.id.add_street_address_editText);
        zipCodeEditText = (EditText) dialog.findViewById(R.id.add_zip_editText);

        Button cancelButton = (Button) dialog.findViewById(R.id.add_address_location_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        Button saveButton = (Button) dialog.findViewById(R.id.add_address_location_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String streetAddress = streetAddressEditText.getText().toString();
                String zipCode = zipCodeEditText.getText().toString();
                callbacks.OnAddressLocationSaveClicked(streetAddress, zipCode);
                dialog.dismiss();
            }
        });

        try {
            dialog.show();
        } catch (Exception ex){
            String message = ex.getMessage();
        }
    }
}
