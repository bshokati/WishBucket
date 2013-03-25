package com.example.wishbucket;


import java.io.File;
import java.io.IOException;

import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.wishbucket.contentprovider.*;
import com.example.wishbucket.database.*;

public class HomeFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	// private Cursor cursor;
	private SimpleCursorAdapter adapter;
	// info for context menu
	private AdapterContextMenuInfo info;
	// the warning dialog
	private AlertDialog dialog;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// get the lists
		fillData();
		
		// click and hold a list for options
		registerForContextMenu(getListView());
		
		/* delete and edit dialog that pops out when you hold a list*/
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Add the buttons
		builder.setPositiveButton(R.string.dialog_delete_ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   // User clicked OK button
		        	   String selection = WishTable.COLUMN_ID_LIST_NUM + "=" + info.id;
		        	   // delete all items with that list id
		        	   getActivity().getContentResolver().delete(WBContentProvider.CONTENT_URI_WISH, selection, null);
		        	   
		        	   // delete that list
		        	   Uri uri = Uri.parse(WBContentProvider.CONTENT_URI_LIST + "/" + info.id);
		        	   getActivity().getContentResolver().delete(uri, null, null);
		        	   dialog.dismiss();
		           }
		       });
		builder.setNegativeButton(R.string.dialog_delete_cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User cancelled the dialog
		        	   // Do nothing
		        	   dialog.dismiss();
		           }
		       });
		
		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage(R.string.dialog_delete_warning);
		// 3. Get the AlertDialog from create()
		dialog = builder.create();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        // onCreateView() is a lifecycle event that is unique to a Fragment. This is called when Android
        // needs the layout for this Fragment. The call to LayoutInflater::inflate() simply takes the layout
        // ID for the layout file, the parent view that will hold the layout, and an option to add the inflated
        // view to the parent. This should always be false or an exception will be thrown. Android will add
        // the view to the parent when necessary.
        View view = inflater.inflate(R.layout.activity_wish_bucket, container, false);
        
		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		((TabActivity)getActivity()).onItemClick(l, v, position, id);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { ListTable.COLUMN_ID_LIST + " AS _id ", ListTable.COLUMN_NAME_LIST, ListTable.COLUMN_PICTURE_LIST };
	    CursorLoader cursorLoader = new CursorLoader(getActivity(), WBContentProvider.CONTENT_URI_LIST, projection, null, null, null);
	    return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
				
		// display hint
    	TextView instructions = (TextView) getActivity().findViewById(R.id.list_instructions);
		
	    if(adapter.getCount() > 0) {
	    	instructions.setVisibility(TextView.VISIBLE);
	    }
	    else {
	    	instructions.setVisibility(TextView.GONE);
	    }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
	    adapter.swapCursor(null);
	}
	
	public String getlistName(int position) {
	    // place list title in intent
	    Cursor cursor = adapter.getCursor();
	    int cursorPosition = cursor.getPosition();
	    cursor.moveToPosition(position);
	    String listName = cursor.getString(cursor.getColumnIndexOrThrow(ListTable.COLUMN_NAME_LIST));
	    cursor.moveToPosition(cursorPosition);
	    
	    return listName;
	}
	
	private void fillData() {
		// Fields from the database (projection)
	    // Must include the _id column for the adapter to work
	    String[] from = new String[] { ListTable.COLUMN_NAME_LIST, ListTable.COLUMN_PICTURE_LIST };
	    // Fields on the UI to which we map
	    int[] to = new int[] { R.id.label2, R.id.thumbnail };

	    getLoaderManager().initLoader(0, null, this);
	    adapter = new SimpleCursorAdapter(getActivity(), R.layout.activity_wb_list_row, null, from, to, 0);
	    adapter.setViewBinder( new SimpleCursorAdapter.ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				
				if(view.getId() == R.id.label2 && columnIndex == cursor.getColumnIndexOrThrow(ListTable.COLUMN_NAME_LIST)) {
					TextView title = (TextView) view.findViewById(R.id.label2);
					title.setText(cursor.getString(cursor.getColumnIndexOrThrow(ListTable.COLUMN_NAME_LIST)));
					return true;
				}
				else if(view.getId() == R.id.thumbnail && columnIndex == cursor.getColumnIndexOrThrow(ListTable.COLUMN_PICTURE_LIST)) {
					ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);
					String picturePath = cursor.getString(cursor.getColumnIndexOrThrow(ListTable.COLUMN_PICTURE_LIST));
					
					if(picturePath != null) {
				        File file = new File(picturePath);
				         
				        BitmapScaler scaler;
						try {
						    scaler = new BitmapScaler(file, 160);
						    imageView.setImageBitmap(scaler.getScaled());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					return true;
				}
				
				return false;
			}
	    	
	    });
	    
	    setListAdapter(adapter);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    menu.setHeaderTitle(R.string.context_menu_title);
	    menu.add(0, TabActivity.DELETE_LIST, 1, R.string.menu_delete_list);
	    menu.add(0, TabActivity.EDIT_LIST, 0, R.string.menu_edit_list);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
	    	case TabActivity.DELETE_LIST:
	    		// show the warning dialog
	    		dialog.show();
	    		return true;
	    }
		
		return super.onContextItemSelected(item);
	}
}
