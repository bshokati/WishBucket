package com.example.wishbucket.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StatusTable {
	
	  // Database table
	  public static final String TABLE_STATUS = "Status";
	  public static final String COLUMN_ID_STATUS = "status_id";
	  public static final String COLUMN_NAME_STATUS = "status_name";
	  
	  private static final String[] STATUSES = {"Current", "Completed"};
	  
	  private static final String DATABASE_CREATE_StatusS = "create table " 
		      + TABLE_STATUS
		      + "("
		      + COLUMN_ID_STATUS + " integer primary key autoincrement, " 
		      + COLUMN_NAME_STATUS + " text not null" 
		      + ");";

	  public static void onCreate(SQLiteDatabase database) {
		    database.execSQL(DATABASE_CREATE_StatusS);
		    
			  for(int i = 0; i < STATUSES.length; i++){
				  ContentValues value1 = new ContentValues();
				  value1.put(COLUMN_NAME_STATUS, STATUSES[i]);
				  database.insert(TABLE_STATUS, null, value1);
			  }
	  }
	  
	  public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		    Log.w(StatusTable.class.getName(), "Upgrading database from version "
		        + oldVersion + " to " + newVersion
		        + ", which will destroy all old data");
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
		    onCreate(database);
	  }

}