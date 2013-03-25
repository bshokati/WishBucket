package com.example.wishbucket;

import java.io.File;
import java.io.IOException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wishbucket.contentprovider.*;
import com.example.wishbucket.database.*;

public class CreateNewList extends Activity {
	
	private static final int RESULT_LOAD_IMAGE = 1;

	private Uri listUri;
	private EditText mTitleText;
	private String picturePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_list);
		
		mTitleText = (EditText) findViewById(R.id.Create_New_List_Title_Edit);
		
		// Check from the saved Instance
	    listUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState.getParcelable(WBContentProvider.CONTENT_ITEM_TYPE_LIST);

	    Bundle extras = getIntent().getExtras();
	    
	    // Or passed from the other activity
	    if (extras != null) {
	      listUri = extras.getParcelable(WBContentProvider.CONTENT_ITEM_TYPE_LIST);

	      // we are probably editing a list
	      setTitle("Edit List Info");
	      
	      fillData(listUri);
	    }
	    

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_create_new_list, menu);
		return true;
	}
	
	public void saveNewList(View view){
		Intent intent = new Intent(this, CreateNewWish.class);
		
		if (TextUtils.isEmpty(mTitleText.getText().toString())) {
			  // check if the user has not entered a title
	          makeToast();
		}
		else {
			// go back to main activity
			setResult(Activity.RESULT_OK,intent);
			saveState();
			// get list id #
			String list_id = listUri.getLastPathSegment();
			
			// inserting into this list
		    intent.putExtra(TabActivity.EXTRA_LIST_ID, list_id);
			finish();
		}
	}
	
	private void fillData(Uri uri) {
	    
		String[] projection = { ListTable.COLUMN_NAME_LIST, ListTable.COLUMN_PICTURE_LIST };
	    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
	    
	    if (cursor != null) {
	      cursor.moveToFirst();

	      mTitleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ListTable.COLUMN_NAME_LIST)));
	      picturePath = cursor.getString(cursor.getColumnIndexOrThrow(ListTable.COLUMN_PICTURE_LIST));
	      
	      if(picturePath != null) {
	          ImageView imageView = (ImageView) findViewById(R.id.imgView);
	          File file = new File(picturePath);
	         
	          BitmapScaler scaler;
			  try {
			      scaler = new BitmapScaler(file, 160);
			      imageView.setImageBitmap(scaler.getScaled());
			  } catch (IOException e) {
				 e.printStackTrace();
			  }
	      }

	      // Always close the cursor
	      cursor.close();
	    }
	}
	
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	}
	
	private void saveState() {
	    String name = mTitleText.getText().toString();

	    // Only save if either summary or description
	    // is available
	    if (name.length() == 0) {
	      return;
	    }

	    ContentValues values = new ContentValues();
	    values.put(ListTable.COLUMN_NAME_LIST, name);
	    
	    if(picturePath != null) {
	    	values.put(ListTable.COLUMN_PICTURE_LIST, picturePath);
	    }

	    if (listUri == null) {
	      // New todo
	      listUri = getContentResolver().insert(WBContentProvider.CONTENT_URI_LIST, values);
	    } 
	    else {
	      // Update todo
	      getContentResolver().update(listUri, values, null, null);
	    }
	}
	
	private void makeToast() {
	    Toast.makeText(CreateNewList.this, "Please enter a list title.", Toast.LENGTH_LONG).show();
	}
	
	public void loadPicture(View view) {
		Intent i = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				 
		startActivityForResult(i, RESULT_LOAD_IMAGE);
	}
	
	@Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	      
	     if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	         Uri selectedImage = data.getData();
	         String[] filePathColumn = { MediaStore.Images.Media.DATA };
	 
	         Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	         cursor.moveToFirst();
	 
	         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	         picturePath = cursor.getString(columnIndex);
	         cursor.close();   
	         
	         ImageView imageView = (ImageView) findViewById(R.id.imgView);
	         File file = new File(picturePath);
	         
	         BitmapScaler scaler;
			 try {
			     scaler = new BitmapScaler(file, 160);
			     imageView.setImageBitmap(scaler.getScaled());
			 } catch (IOException e) {
				e.printStackTrace();
			 }
	     }
	}

}
