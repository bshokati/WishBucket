package com.example.wishbucket.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WishTable {
	
	  public static final String TABLE_WISH = "Wish";
	  public static final String COLUMN_ID_WISH = "wish_id";
	  public static final String COLUMN_NAME_WISH = "wish_name";
	  public static final String COLUMN_COMPLETED = "wish_completed";
	  public static final String COLUMN_ID_LIST_NUM = "list_id_num";
	  
	  private static final String DATABASE_CREATE_WISH = "create table " 
		      + TABLE_WISH
		      + "("
		      + COLUMN_ID_WISH + " integer primary key autoincrement, " 
		      + COLUMN_NAME_WISH + " text not null, "
		      + COLUMN_COMPLETED + " integer not null, "
		      + COLUMN_ID_LIST_NUM + " integer, "
		      + "foreign key(" + COLUMN_ID_LIST_NUM + ") references " 
		      + ListTable.TABLE_LIST + "(" + ListTable.COLUMN_ID_LIST + ")"
		      + ");";
	  
	  public static void onCreate(SQLiteDatabase database) {
		  database.execSQL(DATABASE_CREATE_WISH);
	  }
	  
	  public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		    Log.w(ListTable.class.getName(), "Upgrading database from version "
		        + oldVersion + " to " + newVersion
		        + ", which will destroy all old data");
		    database.execSQL("DROP TABLE IF EXISTS " + TABLE_WISH);
		    onCreate(database);
	  }
}
