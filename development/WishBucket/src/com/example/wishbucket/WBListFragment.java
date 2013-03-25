package com.example.wishbucket;

import com.example.wishbucket.contentprovider.WBContentProvider;
import com.example.wishbucket.database.*;
import com.facebook.*;

import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

public class WBListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	// for the db query
	public static final int INCOMPLETED_WISHES = 0;
	private static final int COMPLETED_WISHES = 1;
	private static final int GROUPS = 2;
	
	// private Cursor cursor;
	private MyAdapter adapter;
	// the warning dialog
	private AlertDialog dialog;
	// for handling edit/delete
	private long clickedChildId = -1;
	// for handling fb share
	private int clickedChildPos = -1;
	// for handling fb, share
	private int clickedGroupPos = -1;
	// the parent activity
	private TabActivity activity;
	// reset the loader
	private boolean firstRun = true;
	// expandable list
	private ExpandableListView expList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
		adapter.notifyDataSetChanged();
        expList.invalidateViews();
 
        if(requestCode == TabActivity.NEW_WISH && resultCode == Activity.RESULT_OK) {
	        // --------- the other magic lines ----------
	        // call restart because we want the background work to be executed exagain
	        getLoaderManager().restartLoader(INCOMPLETED_WISHES, null, this);
	        getLoaderManager().restartLoader(COMPLETED_WISHES, null, this);
        }
	}

	@Override
	public void onPause() {
	    super.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// save the parent activity to get data
		activity = (TabActivity)getActivity();
			
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		// Add the buttons
		builder.setPositiveButton(R.string.dialog_delete_ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   // User clicked OK button
		        	   
		        	   // delete that list
		        	   Uri uri = Uri.parse(WBContentProvider.CONTENT_URI_WISH + "/" + clickedChildId);
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
		
	    fillData();
	    
		registerForContextMenu(expList);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
		View view = inflater.inflate(R.layout.activity_wb_list, container, false);
		
		View emptyView = inflater.inflate(R.layout.activity_wish_empty, null);
		expList = (ExpandableListView) view.findViewById(android.R.id.list);
		expList.setEmptyView(emptyView);
		expList.setOnGroupExpandListener( new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				getLoaderManager().initLoader(groupPosition, null, WBListFragment.this);
			}
			
		});
		
		expList.setOnGroupCollapseListener( new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
		        // --------- the other magic lines ----------
		        // call restart because we want the background work to be executed exagain
		        getLoaderManager().restartLoader(groupPosition, null, WBListFragment.this);
			}
		});
        
		return view;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();

		int groupPos = 0, childPos = 0;
		setClickedChildId(-1);
		setClickedChildPos(-1);
		setClickedGroupPos(-1);
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
			setClickedChildId(adapter.getChildId(groupPos, childPos));
			setClickedChildPos(childPos);
			setClickedGroupPos(groupPos);
			
			switch (item.getItemId()) {
		    	case TabActivity.DELETE_WISH:
		    		// show the warning dialog
		    		dialog.show();
		    		return true;
		    		
		    	case TabActivity.SHARE_WISH:
		    		Session session = Session.getActiveSession();
		    		if (session != null && session.getState().isOpened()) {
		    			activity.publishFeedDialog();
		    		}
		    		else {
		    			activity.showNoticeDialog();
		    		}
		    		return true;
		    	
		    	case TabActivity.FB_LOGOUT:
		    		activity.showNoticeDialog();
		    		return true;
			}
		}

		return super.onContextItemSelected(item);
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    
	    ExpandableListView.ExpandableListContextMenuInfo mInfo = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
	    
	    int type = ExpandableListView.getPackedPositionType(mInfo.packedPosition);
	    
	    //Only create a context menu for child items
	    if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
		    menu.setHeaderTitle(R.string.context_menu_title);
		    menu.add(0, TabActivity.DELETE_WISH, 2, R.string.menu_delete_wish);
		    menu.add(0, TabActivity.EDIT_WISH, 1, R.string.menu_edit_wish);
		    menu.add(0, TabActivity.SHARE_WISH, 0, R.string.menu_share_wish);
	        // User touched the dialog's positive button
	    	Session session = Session.getActiveSession();
			if (session != null && session.getState().isOpened()) {
				menu.add(0, TabActivity.FB_LOGOUT, 3, R.string.fb_logout);
			}
	    }
	}
	
	public void fillData() {
		if( firstRun ) {
			getLoaderManager().initLoader(GROUPS, null, this);
			firstRun = false;
			
			String [] groupFrom = { "_id" };
			String [] childFrom = { WishTable.COLUMN_NAME_WISH };
			int [] groupTo = { R.id.groupText };
			int [] childTo = { R.id.wish_label };
			
		    adapter = new MyAdapter(getActivity(), null, R.layout.activity_wb_list_group, groupFrom, groupTo, R.layout.activity_wish_row, childFrom, childTo);
		}
		else {
			
			adapter.notifyDataSetChanged();
	        expList.invalidateViews();

	        // --------- the other magic lines ----------
	        // call restart because we want the background work to be executed exagain
			getLoaderManager().restartLoader(INCOMPLETED_WISHES, null, this);
			getLoaderManager().restartLoader(COMPLETED_WISHES, null, this);
	        getLoaderManager().restartLoader(GROUPS, null, this);
		}

	    // instructions for clicking and holding
	    View v = getActivity().getLayoutInflater().inflate(R.layout.wish_instructions, null);
	    expList.setAdapter(adapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cursorLoader = null;
		String as_id_string = " AS _id ";
		String[] selectionArgs = null;
		String sortOrder = null;
		
		switch(id) {
			case(INCOMPLETED_WISHES):
				String[] projection = { WishTable.COLUMN_ID_WISH + as_id_string, WishTable.COLUMN_NAME_WISH, WishTable.COLUMN_ID_LIST_NUM, WishTable.COLUMN_COMPLETED  };
				String selection = WishTable.COLUMN_ID_LIST_NUM + "=" + getListId() + " and " + WishTable.COLUMN_COMPLETED + " = " + INCOMPLETED_WISHES;
				cursorLoader = new CursorLoader(getActivity(), WBContentProvider.CONTENT_URI_WISH, projection, selection, selectionArgs, sortOrder);
				break;
			case(COMPLETED_WISHES):
				String[] projection2 = { WishTable.COLUMN_ID_WISH + as_id_string, WishTable.COLUMN_NAME_WISH, WishTable.COLUMN_ID_LIST_NUM, WishTable.COLUMN_COMPLETED };
				String selection2 = WishTable.COLUMN_ID_LIST_NUM + "=" + getListId() + " and " + WishTable.COLUMN_COMPLETED + " = " + COMPLETED_WISHES;
				cursorLoader = new CursorLoader(getActivity(), WBContentProvider.CONTENT_URI_WISH, projection2, selection2, selectionArgs, sortOrder);
				break;
			case(GROUPS):
				String[] projection3 = { StatusTable.COLUMN_ID_STATUS + as_id_string, StatusTable.COLUMN_NAME_STATUS };
				cursorLoader = new CursorLoader(getActivity(), WBContentProvider.CONTENT_URI_STATUS, projection3, null, selectionArgs, sortOrder);
				break;
		}
	    
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(loader != null && !loader.isReset()) {
			if(loader.getId() == GROUPS) {
				adapter.setGroupCursor(data);
				getLoaderManager().initLoader(INCOMPLETED_WISHES, null, this);
				getLoaderManager().initLoader(COMPLETED_WISHES, null, this);
			}
			else if(loader.getId() == INCOMPLETED_WISHES) {
				adapter.setChildrenCursor(INCOMPLETED_WISHES, data);
			}
			else if(loader.getId() == COMPLETED_WISHES ){
				adapter.setChildrenCursor(COMPLETED_WISHES, data);
			}
		}
		
		// display hint
    	TextView instructions = (TextView) getActivity().findViewById(R.id.wish_instructions);
		
	    if(adapter.getGroupCount() > 0) {
	    	instructions.setVisibility(TextView.VISIBLE);
	    }
	    else {
	    	instructions.setVisibility(TextView.GONE);
	    }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
	
	public String getListId() {
		return activity.getListId();
	}

	public String getListName() {
		return activity.getListName();
	}

	public long getClickedChildId() {
		return clickedChildId;
	}

	public void setClickedChildId(long clickedChildId) {
		this.clickedChildId = clickedChildId;
	}

	public int getClickedChildPos() {
		return clickedChildPos;
	}

	public void setClickedChildPos(int clickedChildPos) {
		this.clickedChildPos = clickedChildPos;
	}

	public int getClickedGroupPos() {
		return clickedGroupPos;
	}

	public void setClickedGroupPos(int clickedGroupPos) {
		this.clickedGroupPos = clickedGroupPos;
	}

	public MyAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(MyAdapter adapter) {
		this.adapter = adapter;
	}

	public void changeWishStatus(View view) {
		
		CheckBox checkWish = (CheckBox) view;
	    ContentValues values = new ContentValues();

		if(checkWish.isChecked()) {
			values.put(WishTable.COLUMN_COMPLETED, 1);
		}
		else {
			values.put(WishTable.COLUMN_COMPLETED, 0);
		}
		
		// to modify the complete field of the wish
		int flatWishPosition = expList.getPositionForView(view);
		int childWishPos = ExpandableListView.getPackedPositionChild(expList.getExpandableListPosition(flatWishPosition));
		int groupWishPos = ExpandableListView.getPackedPositionGroup(expList.getExpandableListPosition(flatWishPosition));
		
		long id = adapter.getChildId(groupWishPos, childWishPos);
		Uri itemUri = Uri.parse(WBContentProvider.CONTENT_URI_WISH + "/" + id);
    	getActivity().getContentResolver().update(itemUri, values, null, null);
		
    	// to display a congrats message
		Cursor cursor = adapter.getChild(groupWishPos, childWishPos);
		String name = cursor.getString(cursor.getColumnIndexOrThrow(WishTable.COLUMN_NAME_WISH));
		
		if(checkWish.isChecked()) {
			Toast.makeText(getActivity(), "Congratulations! \"" + name + "\" has been moved to your achievements.", Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(getActivity(), "\"" + name + "\" has been moved to back to your current wishes.", Toast.LENGTH_LONG).show();
		}
	}
}
