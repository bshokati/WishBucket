package com.example.wishbucket;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DealsAdapter extends ArrayAdapter<Deal> {

	private List<Deal> deals;
	
	public DealsAdapter(Context context, int resource, List<Deal> deals) {
		super(context, resource, deals);
		this.deals = deals;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
	    if (v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.deals_row, null);
	    }
		
	    Deal deal = deals.get(position);
	    
	    TextView title = (TextView) v.findViewById(R.id.deal_label);
	    title.setText(deal.toString());
	    
	    v.setBackgroundDrawable(deal.getImage());
	    v.setMinimumHeight(350);
	    
		return v;
	}

}
