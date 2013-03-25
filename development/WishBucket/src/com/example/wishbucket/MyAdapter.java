package com.example.wishbucket;
import com.example.wishbucket.database.*;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

public class MyAdapter extends SimpleCursorTreeAdapter {

	public MyAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
		super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        super.bindChildView(view, context, cursor, isLastChild);
        TextView wishText = (TextView) view.findViewById(R.id.wish_label);
        CheckBox wishCheck = (CheckBox)view.findViewById(R.id.wish_checkbox);

        wishText.setText(cursor.getString(cursor.getColumnIndex(WishTable.COLUMN_NAME_WISH)));
        wishCheck.setChecked((cursor.getInt(cursor.getColumnIndex(WishTable.COLUMN_COMPLETED))==0? false:true));
	}

	@Override
	protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
		// TODO Auto-generated method stub
		super.bindGroupView(view, context, cursor, isExpanded);
		
		TextView groupText = (TextView) view.findViewById(R.id.groupText);
		groupText.setText(cursor.getString(cursor.getColumnIndexOrThrow(StatusTable.COLUMN_NAME_STATUS)));
	}

	@Override
	public ViewBinder getViewBinder() {
		// TODO Auto-generated method stub
		return super.getViewBinder();
	}

	@Override
	public void setViewBinder(ViewBinder viewBinder) {
		// TODO Auto-generated method stub
		super.setViewBinder(viewBinder);
	}

	@Override
	protected void setViewImage(ImageView v, String value) {
		// TODO Auto-generated method stub
		super.setViewImage(v, value);
	}

	@Override
	public void setViewText(TextView v, String text) {
		// TODO Auto-generated method stub
		super.setViewText(v, text);
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		// TODO Auto-generated method stub
		return null;
	}
}
