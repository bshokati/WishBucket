package com.example.wishbucket.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import com.example.wishbucket.database.*;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class WBContentProvider extends ContentProvider {
	
	// database
	private WBDatabaseHelper database;
	
	// Used for the UriMacher
	private static final int LISTS = 10;
	private static final int LIST_ID = 20;
	private static final int WISHS = 30;
	private static final int WISH_ID = 40;
	private static final int SUGGESTIONS = 50;
	private static final int SUGGESTION_ID = 60;
	private static final int STATUSES = 70;
	private static final int STATUS_ID = 80;


	private static final String AUTHORITY = "com.example.wishbucket.contentprovider";
	
	/* for the list query */
	private static final String BASE_PATH_LIST = "list";
	public static final Uri CONTENT_URI_LIST = Uri.parse("content://" + AUTHORITY
		      + "/" + BASE_PATH_LIST);
	public static final String CONTENT_TYPE_LISTS = ContentResolver.CURSOR_DIR_BASE_TYPE
		      + "/lists";
	public static final String CONTENT_ITEM_TYPE_LIST = ContentResolver.CURSOR_ITEM_BASE_TYPE
		      + "/list";
	
	/* for the wish query */
	private static final String BASE_PATH_WISH = "wish";
	public static final Uri CONTENT_URI_WISH = Uri.parse("content://" + AUTHORITY
		      + "/" + BASE_PATH_WISH);
	public static final String CONTENT_TYPE_WISHS = ContentResolver.CURSOR_DIR_BASE_TYPE
		      + "/wishs";
	public static final String CONTENT_WISH_TYPE_WISH = ContentResolver.CURSOR_ITEM_BASE_TYPE
		      + "/wish";
	
	/* for the suggestion query */
	private static final String BASE_PATH_SUGGESTION = "suggestion";
	public static final Uri CONTENT_URI_SUGGESTION = Uri.parse("content://" + AUTHORITY
		      + "/" + BASE_PATH_SUGGESTION);
	public static final String CONTENT_TYPE_SUGGESTIONS = ContentResolver.CURSOR_DIR_BASE_TYPE
		      + "/suggestions";
	public static final String CONTENT_ITEM_TYPE_SUGGESTION = ContentResolver.CURSOR_ITEM_BASE_TYPE
		      + "/suggestion";
	
	/* for the status query */
	private static final String BASE_PATH_STATUS = "status";
	public static final Uri CONTENT_URI_STATUS = Uri.parse("content://" + AUTHORITY
		      + "/" + BASE_PATH_STATUS);
	public static final String CONTENT_TYPE_STATUSES = ContentResolver.CURSOR_DIR_BASE_TYPE
		      + "/statuses";
	public static final String CONTENT_ITEM_TYPE_STATUS = ContentResolver.CURSOR_ITEM_BASE_TYPE
		      + "/status";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	  static {
	    sURIMatcher.addURI(AUTHORITY, BASE_PATH_LIST, LISTS);
	    sURIMatcher.addURI(AUTHORITY, BASE_PATH_LIST + "/#", LIST_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, BASE_PATH_WISH, WISHS);
	    sURIMatcher.addURI(AUTHORITY, BASE_PATH_WISH + "/#", WISH_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, BASE_PATH_SUGGESTION, SUGGESTIONS);
	    sURIMatcher.addURI(AUTHORITY, BASE_PATH_SUGGESTION + "/#", SUGGESTION_ID);
	    
	    sURIMatcher.addURI(AUTHORITY, BASE_PATH_STATUS, STATUSES);
	    sURIMatcher.addURI(AUTHORITY, BASE_PATH_STATUS + "/#", STATUS_ID);
	  }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    String id;
	    
	    switch (uriType) {
	    	case LISTS:
	    		rowsDeleted = sqlDB.delete(ListTable.TABLE_LIST, selection, selectionArgs);
	    		break;
	    	case LIST_ID:
	    		id = uri.getLastPathSegment();
	    		if (TextUtils.isEmpty(selection)) {
	    			rowsDeleted = sqlDB.delete(ListTable.TABLE_LIST, ListTable.COLUMN_ID_LIST + "=" + id, null);
	    		} else {
	    			rowsDeleted = sqlDB.delete(ListTable.TABLE_LIST, ListTable.COLUMN_ID_LIST + "=" + id + " and " + selection, selectionArgs);
	    		}
	    		break;
	    	case WISHS:
	    		rowsDeleted = sqlDB.delete(WishTable.TABLE_WISH, selection, selectionArgs);
	    		break;
	    	case WISH_ID:
	    		id = uri.getLastPathSegment();
	    		if (TextUtils.isEmpty(selection)) {
	    			rowsDeleted = sqlDB.delete(WishTable.TABLE_WISH, WishTable.COLUMN_ID_WISH + "=" + id, null);
	    		} else {
	    			rowsDeleted = sqlDB.delete(WishTable.TABLE_WISH, WishTable.COLUMN_ID_WISH + "=" + id + " and " + selection, selectionArgs);
	    		}
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsDeleted;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
	    long id = 0;
	    
	    switch (uriType) {
	    	case LISTS:
	    		id = sqlDB.insert(ListTable.TABLE_LIST, null, values);
	    	    getContext().getContentResolver().notifyChange(uri, null);
	    	    return Uri.parse(BASE_PATH_LIST + "/" + id);
	    	case WISHS:
	    		id = sqlDB.insert(WishTable.TABLE_WISH, null, values);
	    	    getContext().getContentResolver().notifyChange(uri, null);
	    	    return Uri.parse(BASE_PATH_WISH + "/" + id);
	    	default:
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	}

	@Override
	public boolean onCreate() {
		 database = new WBDatabaseHelper(getContext());
		 return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		// Using SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	    
	    // Check if the caller has requested a column which does not exists
	    checkColumns(projection);
	    
	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    	case LISTS:
	    	    queryBuilder.setTables(ListTable.TABLE_LIST);
	    		break;
	    	case LIST_ID:
	    	    // Set the table
	    	    queryBuilder.setTables(ListTable.TABLE_LIST);
	    		// Adding the ID to the original query
	    		queryBuilder.appendWhere(ListTable.COLUMN_ID_LIST + "=" + uri.getLastPathSegment());
	    		break;
	    	case WISHS:
	    		// set the table
	    		queryBuilder.setTables(WishTable.TABLE_WISH);
	    		break;
	    	case WISH_ID:
	    	    // Set the table
	    	    queryBuilder.setTables(WishTable.TABLE_WISH);
	    		// Adding the ID to the original query
	    		queryBuilder.appendWhere(WishTable.COLUMN_ID_WISH + "=" + uri.getLastPathSegment());
	    		break;
	    	case SUGGESTIONS:
	    		// set the table
	    		queryBuilder.setTables(SuggestionsTable.TABLE_SUGGESTIONS);
	    		break;
	    	case SUGGESTION_ID:
	    	    // Set the table
	    	    queryBuilder.setTables(SuggestionsTable.TABLE_SUGGESTIONS);
	    		// Adding the ID to the original query
	    		queryBuilder.appendWhere(SuggestionsTable.COLUMN_ID_SUGGESTION + "=" + uri.getLastPathSegment());
	    		break;
	    	case STATUSES:
	    		// set the table
	    		queryBuilder.setTables(StatusTable.TABLE_STATUS);
	    		break;
	    	case STATUS_ID:
	    	    // Set the table
	    	    queryBuilder.setTables(StatusTable.TABLE_STATUS);
	    		// Adding the ID to the original query
	    		queryBuilder.appendWhere(StatusTable.COLUMN_ID_STATUS + "=" + uri.getLastPathSegment());
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    SQLiteDatabase db = database.getWritableDatabase();
	    Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    // Make sure that potential listeners are getting notified
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
	    return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsUpdated = 0;
	    String id;
	    
	    switch (uriType) {
	    	case LISTS:
	    		rowsUpdated = sqlDB.update(ListTable.TABLE_LIST, values, selection, selectionArgs);
	    		break;
	    	case LIST_ID:
	    		id = uri.getLastPathSegment();
	    		if (TextUtils.isEmpty(selection)) {
	    			rowsUpdated = sqlDB.update(ListTable.TABLE_LIST, values, ListTable.COLUMN_ID_LIST + "=" + id, null);
	    		} 
	    		else {
	    			rowsUpdated = sqlDB.update(ListTable.TABLE_LIST, values, ListTable.COLUMN_ID_LIST + "=" + id + " and " + selection, selectionArgs);
	    		}
	    		break;
	    	case WISHS:
	    		rowsUpdated = sqlDB.update(WishTable.TABLE_WISH, values, selection, selectionArgs);
	    		break;
	    	case WISH_ID:
	    		id = uri.getLastPathSegment();
	    		if (TextUtils.isEmpty(selection)) {
	    			rowsUpdated = sqlDB.update(WishTable.TABLE_WISH, values, WishTable.COLUMN_ID_WISH + "=" + id, null);
	    		} 
	    		else {
	    			rowsUpdated = sqlDB.update(WishTable.TABLE_WISH, values, WishTable.COLUMN_ID_WISH + "=" + id + " and " + selection, selectionArgs);
	    		}
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsUpdated;
	}
	
	private void checkColumns(String[] projection) {
	    String[] available = { ListTable.COLUMN_NAME_LIST, ListTable.COLUMN_ID_LIST, ListTable.COLUMN_ID_LIST + " AS _id ", ListTable.COLUMN_PICTURE_LIST,
	    					   WishTable.COLUMN_COMPLETED, WishTable.COLUMN_ID_WISH, WishTable.COLUMN_NAME_WISH, WishTable.COLUMN_ID_LIST_NUM, WishTable.COLUMN_ID_WISH + " AS _id ",
	    					   SuggestionsTable.COLUMN_ID_SUGGESTION, SuggestionsTable.COLUMN_ID_SUGGESTION + " AS _id ", SuggestionsTable.COLUMN_NAME_SUGGESTION,
	    					   StatusTable.COLUMN_ID_STATUS, StatusTable.COLUMN_ID_STATUS + " AS _id ", StatusTable.COLUMN_NAME_STATUS };
	    if (projection != null) {
	      HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
	      HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
	      // Check if all columns which are requested are available
	      if (!availableColumns.containsAll(requestedColumns)) {
	        throw new IllegalArgumentException("Unknown columns in projection");
	      }
	    }
	}

}
