package com.example.wishbucket.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SuggestionsTable {
	
	  public static final String TABLE_SUGGESTIONS = "Suggestion";
	  public static final String COLUMN_ID_SUGGESTION = "suggestion_id";
	  public static final String COLUMN_NAME_SUGGESTION = "suggestion_name";
	  
	  private static final String DATABASE_CREATE_SUGGESTIONS = "create table " 
		      + TABLE_SUGGESTIONS
		      + "("
		      + COLUMN_ID_SUGGESTION + " integer primary key autoincrement, " 
		      + COLUMN_NAME_SUGGESTION + " text not null " 
		      + ");";
	  
	  private static final String[] SUGGESTIONS = {"Run a Marathon", "Skydive", "See the Northern Lights",
		  "Get a Tattoo", "Swim with Dolphins", "Go on a Cruise", "Get Married", "Go Zip Lining",
		  "Ride an Elephant", "Go White Water Rafting", "Write a Book", "Visit Pyramids in Egypt",
		  "Learn to Surf", "Visit Grand Canyon", "Buy a House", "Scuba Dive", "Donate Blood", "Parasail"};

	  
	  public static void onCreate(SQLiteDatabase database) {
		  database.execSQL(DATABASE_CREATE_SUGGESTIONS);
		  
		  for(int i = 0; i < SUGGESTIONS.length; i++){
			  ContentValues value1 = new ContentValues();
			  value1.put(COLUMN_NAME_SUGGESTION, SUGGESTIONS[i]);
			  database.insert(TABLE_SUGGESTIONS, null, value1);
		  }
	  } 
	  
	  public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		    Log.w(ListTable.class.getName(), "Upgrading database from version "
		        + oldVersion + " to " + newVersion
		        + ", which will destroy all old data");
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_SUGGESTIONS);
		    onCreate(database);
	  }
}
