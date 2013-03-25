package com.example.wishbucket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class TabFragment extends Fragment {
    
	private static final int HOME_TAB = 1;
    private static final int DISCOVER_TAB = 2;
    private static final int DEALS_TAB = 3;
    
    private int mTabState;
    private ImageButton dealsTab;
    private ImageButton discoverViewTab;
    private ImageButton homeViewTab;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	View view = inflater.inflate(R.layout.fragment_tab, container, false);
        
        // Grab the tab buttons from the layout and attach event handlers. The code just uses standard
        // buttons for the tab widgets. These are bad tab widgets, design something better, this is just
        // to keep the code simple.
        homeViewTab = (ImageButton) view.findViewById(R.id.home_tab);
        discoverViewTab = (ImageButton) view.findViewById(R.id.discover_tab);
        dealsTab = (ImageButton) view.findViewById(R.id.deals_tab);
        
        homeViewTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch the tab content to display the home view.
                gotoHomeView();
                discoverViewTab.setImageResource(R.drawable.discoverbutton);
                dealsTab.setImageResource(R.drawable.dealsbutton);
                homeViewTab.setImageResource(R.drawable.homepressed);
            }
        });
        
        discoverViewTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch the tab content to display the discover view.
                gotoDiscoverView();
                discoverViewTab.setImageResource(R.drawable.discoverpressed);
                dealsTab.setImageResource(R.drawable.dealsbutton);
                homeViewTab.setImageResource(R.drawable.homebutton);
            }
        });
        
        dealsTab.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// Switch the tab content to display the deals view.
        		gotoDealsView();
                discoverViewTab.setImageResource(R.drawable.discoverbutton);
                dealsTab.setImageResource(R.drawable.dealspressed);
                homeViewTab.setImageResource(R.drawable.homebutton);
        	}
        });
        
        return view;
    }
    
    public void gotoHomeView() {
        // mTabState keeps track of which tab is currently displaying its contents.
        // Perform a check to make sure the home tab content isn't already displaying.
        
        if (mTabState != HOME_TAB) {
            // Update the mTabState 
            mTabState = HOME_TAB;
            
            ((TabActivity)getActivity()).getBackStack().clear();
            ((TabActivity)getActivity()).createState(TabActivity.HOME);
        }
    }
    
    public void gotoDiscoverView() {
        // See gotoListView(). This method does the same thing except it loads
        // the grid tab.
        
        if (mTabState != DISCOVER_TAB) {
            mTabState = DISCOVER_TAB;
            
            ((TabActivity)getActivity()).getBackStack().clear();
            ((TabActivity)getActivity()).createState(TabActivity.DISCOVER);
        }
    }
    
    public void gotoDealsView() {
        // See gotoListView(). This method does the same thing except it loads
        // the grid tab.
        
        if (mTabState != DEALS_TAB) {
            mTabState = DEALS_TAB;
            
            ((TabActivity)getActivity()).getBackStack().clear();
            ((TabActivity)getActivity()).createState(TabActivity.DEALS);
        }
    }
}
