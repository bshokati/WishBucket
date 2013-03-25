package com.example.wishbucket.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ListTable {
	
	  // Database table
	  public static final String TABLE_LIST = "List";
	  public static final String COLUMN_ID_LIST = "list_id";
	  public static final String COLUMN_NAME_LIST = "list_name";
	  public static final String COLUMN_PICTURE_LIST = "list_picture";
	  
	  // Database creation SQL statement
	  private static final String DATABASE_CREATE_LIST = "create table " 
	      + TABLE_LIST
	      + "(" 
	      + COLUMN_ID_LIST + " integer primary key autoincrement, " 
	      + COLUMN_NAME_LIST + " text not null, "
	      + COLUMN_PICTURE_LIST + " text "
	      + ");";

	  public static void onCreate(SQLiteDatabase database) {
		    database.execSQL(DATABASE_CREATE_LIST);
	  }
	  
	  public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		    Log.w(ListTable.class.getName(), "Upgrading database from version "
		        + oldVersion + " to " + newVersion
		        + ", which will destroy all old data");
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
		    onCreate(database);
	  }

}
