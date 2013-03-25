package com.example.wishbucket;

import java.util.Arrays;
import java.util.List;

import com.facebook.widget.LoginButton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class FBLogIn extends DialogFragment {
	
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }
	
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    View v = inflater.inflate(R.layout.fblogin, null);
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(v)
	    // Add action buttons
	           .setPositiveButton(R.string.dialog_fb_ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   // Send the positive button event back to the host activity
                       mListener.onDialogPositiveClick(FBLogIn.this);
	               }
	           })
	           .setNegativeButton(R.string.dialog_fb_cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	// Send the negative button event back to the host activity
                       mListener.onDialogPositiveClick(FBLogIn.this);
	               }
	           })
	           .setMessage(R.string.fb_login);
	    
		// fb login
	    
		LoginButton authButton = (LoginButton) v.findViewById(R.id.authButton);
		authButton.setPublishPermissions(PERMISSIONS);
	    
	    return builder.create();
    }
}
