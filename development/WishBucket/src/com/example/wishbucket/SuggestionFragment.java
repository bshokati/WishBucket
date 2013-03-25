package com.example.wishbucket;


import com.example.wishbucket.contentprovider.WBContentProvider;
import com.example.wishbucket.database.SuggestionsTable;

import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;

public class SuggestionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

	// private Cursor cursor;
	private SimpleCursorAdapter adapter;
	
	public static final String EXTRA_SUGGESTION_NAME = "extra_suggestion_name";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	    fillData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_suggestions, container, false);
		return view;
	}
	
	private void fillData() {
	    
		String[] projection = { SuggestionsTable.COLUMN_NAME_SUGGESTION };
	    int[] to = new int[] { R.id.suggestion_label };
	    getLoaderManager().initLoader(0, null, this);
	    adapter = new SimpleCursorAdapter(getActivity(), R.layout.activity_suggestions_row, null, projection, to, 0);

	    setListAdapter(adapter);
	}
	
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader cursorLoader = null;
		
		String[] projection = { SuggestionsTable.COLUMN_ID_SUGGESTION + " AS _id ", SuggestionsTable.COLUMN_NAME_SUGGESTION };
		cursorLoader = new CursorLoader(getActivity(), WBContentProvider.CONTENT_URI_SUGGESTION, projection, null, null, null);
	    
		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);		
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
	    adapter.swapCursor(null);
		
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);
		String value = cursor.getString(cursor.getColumnIndexOrThrow(SuggestionsTable.COLUMN_NAME_SUGGESTION));
		Intent intent = new Intent(getActivity(), CreateNewWish.class);
		intent.putExtra(EXTRA_SUGGESTION_NAME, value);
		startActivityForResult(intent, TabActivity.NEW_WISH);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	

}
