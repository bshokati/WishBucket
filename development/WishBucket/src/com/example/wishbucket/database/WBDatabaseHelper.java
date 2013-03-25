package com.example.wishbucket.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WBDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "tables.db";
	private static final int DATABASE_VERSION = 1;
	
	public WBDatabaseHelper(Context context) {
		    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		if (!database.isReadOnly()) {
		    // Enable foreign key constraints
		    database.execSQL("PRAGMA foreign_keys=ON;");
		}
		
		ListTable.onCreate(database);
		WishTable.onCreate(database);
		SuggestionsTable.onCreate(database);
		StatusTable.onCreate(database);
	}

	// Method is called during an upgrade of the database,
    // e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		ListTable.onUpgrade(database, oldVersion, newVersion);
		WishTable.onUpgrade(database, oldVersion, newVersion);
		SuggestionsTable.onUpgrade(database, oldVersion, newVersion);
		StatusTable.onUpgrade(database, oldVersion, newVersion);
	}

}
