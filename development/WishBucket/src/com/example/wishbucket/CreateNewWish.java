package com.example.wishbucket;

import com.example.wishbucket.contentprovider.WBContentProvider;
import com.example.wishbucket.database.*;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

public class CreateNewWish extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final int EMPTY_TITLE = 0;
	private static final int EMPTY_STATUS = 1;
	private static final int EMPTY_CATEGORY = 2;
	
	private Uri itemUri;
	private EditText mTitleText;
	private RadioButton mRadioButtonComplete;
	private RadioButton mRadioButtonIncomplete;
	private Spinner mCategorySpinner;
	private String listId;
	
	// private Cursor cursor;
	private SimpleCursorAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_wish);
		
		// get all fields
		mTitleText = (EditText) findViewById(R.id.Create_New_Wish_Title_Edit);
	    mRadioButtonComplete = (RadioButton) findViewById(R.id.radio_complete);
	    mRadioButtonIncomplete = (RadioButton) findViewById(R.id.radio_incomplete);
	    mCategorySpinner = (Spinner) findViewById(R.id.Create_New_Wish_Category_Spinner);
	    
	    String text = getIntent().getStringExtra(SuggestionFragment.EXTRA_SUGGESTION_NAME);
	    if( text != null ) {
	    	mTitleText.setText(text);
	    }
	    
	    // is it an existing item?
	    Bundle extras = getIntent().getExtras();
	    
	    // Or passed from the other activity
	    if (extras != null) {
	      itemUri = extras.getParcelable(WBContentProvider.CONTENT_TYPE_WISHS);
	    }
	  
	    // if existing item, fill in info
	    if(itemUri != null) {
		  fillData(itemUri);
		  setTitle(R.string.edit_wish_info);
		}
	    
	    // set spinner to a category
	    fillSpinnerData();
	    setCategory(getIntent());
	    
	    // SCROLL VIEW HACK
	    // BOGUS
	    ScrollView view = (ScrollView)findViewById(R.id.Create_New_Wish_ScrollView);
	    view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
	    view.setFocusable(true);
	    view.setFocusableInTouchMode(true);
	    view.setOnTouchListener(new View.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            v.requestFocusFromTouch();
	            return false;
	        }
	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_create_new_wish, menu);
		return true;
	}
	
	public void saveNewWish(View view) {
		Intent intent = new Intent(this, TabActivity.class);
		
		if (TextUtils.isEmpty(mTitleText.getText().toString())) {
			// check if the user has not entered a title
			makeToast(EMPTY_TITLE);
		}
		else if(!(mRadioButtonComplete.isChecked() || mRadioButtonIncomplete.isChecked())) {
			makeToast(EMPTY_STATUS);
		}
		else if(mCategorySpinner.getSelectedItem() == null) {
			makeToast(EMPTY_CATEGORY);
		}
		else {
			// go back to main activity
			saveState();
			setResult(Activity.RESULT_OK,intent);
			finish();
		}
	}
	
	private void fillData(Uri uri) {
		String[] projection = { WishTable.COLUMN_NAME_WISH, WishTable.COLUMN_COMPLETED };
	    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
	    if (cursor != null) {
	      cursor.moveToFirst();

	      mTitleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(WishTable.COLUMN_NAME_WISH)));
	      if(cursor.getInt(cursor.getColumnIndexOrThrow(WishTable.COLUMN_COMPLETED)) == 1) {
	    	  mRadioButtonComplete.setChecked(true);  
	      }
	      else {
	    	  mRadioButtonIncomplete.setChecked(true);
	      }

	      // Always close the cursor
	      cursor.close();
	    }
	}
	
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putParcelable(WBContentProvider.CONTENT_WISH_TYPE_WISH, itemUri);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	}
	
	private void saveState() {
	    
		String name = mTitleText.getText().toString();
	    int checked;
	    int categoryPosition = mCategorySpinner.getSelectedItemPosition();
	    long listId = adapter.getItemId(categoryPosition);
	    
	    
	    if(mRadioButtonIncomplete.isChecked())
	    	checked = 0;
	    else if(mRadioButtonComplete.isChecked())
	    	checked = 1;
	    else
	    	checked = 2;

	    // Only save if name, status, and category is available
	    if (name.length() == 0 || checked == 2 || categoryPosition == AdapterView.INVALID_POSITION) {
	      return;
	    }

	    ContentValues values = new ContentValues();
	    values.put(WishTable.COLUMN_NAME_WISH, name);
	    values.put(WishTable.COLUMN_COMPLETED, checked);
	    values.put(WishTable.COLUMN_ID_LIST_NUM, listId);

	    if (itemUri == null) {
	    	// New todo
	    	itemUri = getContentResolver().insert(WBContentProvider.CONTENT_URI_WISH, values);
	    } 
	    else {
	    	// Update todo
	    	getContentResolver().update(itemUri, values, null, null);
	    }
	}
	
	private void makeToast(int code) {
		switch(code) {
			case(EMPTY_TITLE):
				Toast.makeText(CreateNewWish.this, "Please enter a Wish Title.", Toast.LENGTH_LONG).show();
				break;
			case(EMPTY_STATUS):
				Toast.makeText(CreateNewWish.this, "Please select the status of this Wish.", Toast.LENGTH_LONG).show();
				break;
			case(EMPTY_CATEGORY):
				Toast.makeText(CreateNewWish.this, "Please create and select a WishBucket List.", Toast.LENGTH_LONG).show();
				break;
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { ListTable.COLUMN_ID_LIST + " AS _id ", ListTable.COLUMN_NAME_LIST };
	    CursorLoader cursorLoader = new CursorLoader(this, WBContentProvider.CONTENT_URI_LIST, projection, null, null, null);
	    return cursorLoader;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		setCategory(getIntent());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
	    adapter.swapCursor(null);
	    setCategory(getIntent());
	}
	
	private void fillSpinnerData() {
		// Fields from the database (projection)
	    // Must include the _id column for the adapter to work
	    String[] from = new String[] { ListTable.COLUMN_NAME_LIST };
	    // Fields on the UI to which we map
	    int[] to = new int[] { android.R.id.text1 };

	    getLoaderManager().initLoader(0, null, this);
	    adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null, from, to, 0);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
	    mCategorySpinner.setAdapter(adapter);
	}
	
	/** Called when someone clicks the add list button */
	public void addNewList(View view) {
		Intent intent = new Intent(this, CreateNewList.class);
		startActivityForResult(intent,TabActivity.NEW_LIST);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		setIntent(data);
	}
	
	private int getCategoryPosition(String id) {
		String[] projection = { ListTable.COLUMN_ID_LIST + " AS _id ", ListTable.COLUMN_NAME_LIST };
	    Cursor cursor = getContentResolver().query(WBContentProvider.CONTENT_URI_LIST, projection, null, null, null);
	    String value;
	    int ret = -1;
	    
	    for(int i = 0; i < cursor.getCount(); i++) {
	    	cursor.moveToPosition(i);
	    	value = cursor.getString(cursor.getColumnIndex("_id"));
	    	if(value.equals(id)) {
	    		ret = cursor.getPosition();
	    		break;
	    	}
	    }
	    
	    cursor.close();
		return ret;
	}
	
	private void setCategory(Intent intent) {    	
		if( intent != null ) {
			// needed if creating a new category
			listId = intent.getStringExtra(TabActivity.EXTRA_LIST_ID);
			setSpinnerSelection(listId);
		}
	}
	
	private void setSpinnerSelection(String id) {
		int listPosition;
		if(id != null) {
	    	listPosition = getCategoryPosition(id);
	    	if(listPosition != -1) {
		    	mCategorySpinner.setSelection(listPosition, true);
	    	}
	    	else {
	    		throw new IllegalStateException("invalid list id provided");
	    	}
	    }
	}
}
