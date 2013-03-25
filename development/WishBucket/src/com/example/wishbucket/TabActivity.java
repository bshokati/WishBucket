package com.example.wishbucket;

import com.example.wishbucket.contentprovider.WBContentProvider;
import com.example.wishbucket.database.WishTable;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import java.util.Arrays;
import java.util.Stack;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.net.Uri;
import android.os.Bundle;


public class TabActivity extends FragmentActivity implements OnItemClickListener, FBLogIn.NoticeDialogListener {

	/* STATE CODES */
	public static final int HOME = 0;
	public static final int SHOW_LIST = 1;
	public static final int NEW_WISH = 2;
	public static final int NEW_LIST = 3;
	public static final int SHOW_WISH = 4;
	public static final int DISCOVER = 5;
	public static final int DEALS = 6;
	
	/* CONTEXT MENU CODES */
	public static final int EDIT_WISH = 7;
	public static final int DELETE_WISH = 8;
	public static final int EDIT_LIST = 9;
	public static final int DELETE_LIST = 10;
	public static final int SHARE_WISH = 11;
	public static final int FB_LOGOUT = 12;
	
	/* KEYS TO PASS AROUND DATA BTWN ACTIVITIES & FRAGS */
	public final static String EXTRA_LIST_TITLE = "com.example.wishbucket.LIST_TITLE";
	public final static String EXTRA_LIST_ID = "com.example.wishbucket.WBListActivity.LIST_ID";
	public final static String EXTRA_STATE = "com.example.wishbucket.STATE";
	public final static String EXTRA_LOADER_ID = "com.example.wishbucket.LOADER_ID";
	private static final String EXTRA_BACKSTACK = "com.example.wishbucket.BACKSTACK";
	
	/* TAB TEXTS */
	public final static String HOME_TAB = "Home";
	public final static String DISCOVER_TAB = "Discover";
	public final static String DEALS_TAB = "Deals";
	
	/* INSTANCE VARS */
	private int currentState;
	private String listName;
	private String listId;
	private Stack<String> backStack;
	
	
	// fb permissions to request
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	// facebook ui changer
	private UiLifecycleHelper uiHelper;
	// facebook name
	private String name;
	// callback for facebook
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Remove notification bar
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_tab);
		
		backStack = new Stack<String>();
		
		// load home list and set each row as clickable
		View view = getLayoutInflater().inflate(R.layout.activity_wish_bucket, null);
		ListView home = (ListView)view.findViewById(android.R.id.list);
		home.setOnItemClickListener(this);

		// set initial state of the activity's fragment
		int newState = HOME;
		// if returning from an older state
		if( savedInstanceState != null ) {
			
			int oldState = savedInstanceState.getInt(EXTRA_STATE, -1);
			String [] tempBackStack = savedInstanceState.getStringArray(EXTRA_BACKSTACK);
			if(tempBackStack != null) {
				backStack.addAll(Arrays.asList(tempBackStack));
			}
			
			if(oldState != -1) {
				newState = oldState;
				// preserve list view state
				if(oldState == SHOW_LIST){
					setListId(savedInstanceState.getString(EXTRA_LIST_ID));
					setListName(savedInstanceState.getString(EXTRA_LIST_TITLE));
				}
				else {
					setListId(null);
					setListName(null);
				}
			}
			// set the new/old state
			setCurrentState(newState);
			// WARNING DO NOT CALL CREATESTATE(), ANDROID HAS ALREADY SAVED ONE
		}
		else {
			// set the new/old state
			// start the transaction for the fragment
	        TabFragment tabFragment = (TabFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tab);
	        tabFragment.gotoHomeView();
	        ImageButton homeViewTab = (ImageButton) findViewById(R.id.home_tab);
            homeViewTab.setImageResource(R.drawable.homepressed);
		}
		
		/* FACEBOOK STUFF FROM HERE ON*/
		if (savedInstanceState != null) {
		    pendingPublishReauthorization = savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
		}
		
	    uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null && (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }
	    
	    uiHelper.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	    uiHelper.onPause();
	}

	@Override
	protected void onPause() {
		super.onPause();
	    uiHelper.onDestroy();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}
	
	public String getTag(int state) {
		String tag = String.valueOf(state);
		
		if(state == SHOW_LIST) {
			tag += "_" + getListId();
		}
		return tag;
	}

	public void createState(int nextState) {
		
		String tag = getTag(nextState);
		
		// grab a the fragment if created before
		Fragment next = getSupportFragmentManager().findFragmentByTag(tag);
		
		// get the current fragment to save it for future use
		Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_content);
		
		// start a transaction to switch fragments
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
		if(current != null) {
			// detach the current fragment
			transaction.detach(current);
			if( shouldISetBackStack(nextState) ) {
				// save the fragment in the back button
				backStack.push(current.getTag());
			}
		}
		
		// if not created before, make a new instance
		if(next == null) {
			switch(nextState) {
				case(HOME):
					next = new HomeFragment();
					break;
				case(SHOW_LIST):
					next = new WBListFragment();
					break;
				case(DISCOVER):
					next = new SuggestionFragment();
					break;
				case(DEALS):
					next = new DealsFragment();
					break;
				default:
					throw new IllegalStateException("Invalid state to start in TabActivity");
			}
			// add the new fragment
			transaction.add(R.id.fragment_content, next, tag);
		}
		else {
			// attach the fragment
			transaction.attach(next);
		}
		// commit and start the fragment
		transaction.commit();
		getFragmentManager().executePendingTransactions();
		setCurrentState(nextState);
	}
	
	public boolean shouldISetBackStack(int nextState) {
		return ( getCurrentState() == HOME && nextState == SHOW_LIST );
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		
		if(getCurrentState() == HOME) {
		    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_content);
			// set vars
		    setListName(((HomeFragment)current).getlistName(position));
			setListId(String.valueOf(id));
			
			// set the new state
			createState(SHOW_LIST);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	if( !getBackStack().isEmpty() ) {
	    		String tag = getBackStack().pop();
	    		if(tag != null) {
	    			if(getCurrentState() == SHOW_LIST) {
	    				// reset vars
	    				createState(HOME);
	    				setListName(null);
	    				setListId(null);
		    			return true;
	    			}
	    		}
	    	}
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}
	
	public Stack<String> getBackStack() {
		return backStack;
	}

	public void setBackStack(Stack<String> backStack) {
		this.backStack = backStack;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(EXTRA_STATE, getCurrentState());
		outState.putString(EXTRA_LIST_ID, getListId());
		outState.putString(EXTRA_LIST_TITLE, getListName());
		outState.putStringArray(EXTRA_BACKSTACK, (String [])backStack.toArray(new String[0]));
	    outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
	    uiHelper.onSaveInstanceState(outState);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_tab, menu);
		return true;
	}
	
	/** called when someone clicks the Add Wish button */
	public void addNewWish(View view) {
		
		// START ACTIVITY FOR RESULT FOR NEW WISH HERE
		Intent intent = new Intent(this, CreateNewWish.class);
		
		// add wish clicked from list page
		if(getCurrentState() == SHOW_LIST) {
			// give this id to set default list in spinner
		    intent.putExtra(EXTRA_LIST_ID, getListId());
		}
		startActivityForResult(intent,NEW_WISH);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		ExpandableListView.ExpandableListContextMenuInfo mInfo = null;
		AdapterContextMenuInfo info = null;
		
		if(getCurrentState() == SHOW_LIST) {
		    mInfo = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		} else {
			info = (AdapterContextMenuInfo) item.getMenuInfo();
		}
		
		switch (item.getItemId()) {
	    	case EDIT_LIST:
	    		// START ACTIVITY FOR RESULT FOR NEW LIST
	    		editList(info.id);
	    		return true;
	    		
	    	case EDIT_WISH:
	    		// START ACTIVITY FOR RESULT FOR NEW WISH
	    		WBListFragment current = (WBListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
	    		int groupPos = 0, childPos = 0;
	    		int type = ExpandableListView.getPackedPositionType(mInfo.packedPosition);
	    		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	    			groupPos = ExpandableListView.getPackedPositionGroup(mInfo.packedPosition);
	    			childPos = ExpandableListView.getPackedPositionChild(mInfo.packedPosition);
	    			long childId = ((MyAdapter)(current.getAdapter())).getChildId(groupPos, childPos);
	    			editWish(childId);
	    		}
	    		return true;
	    }
		
		return super.onContextItemSelected(item);
	}
	
	public void editList(long id) {		
		Intent intent = new Intent(this, CreateNewList.class);
	    Uri editListUri = Uri.parse(WBContentProvider.CONTENT_URI_LIST + "/" + id);
	    intent.putExtra(WBContentProvider.CONTENT_ITEM_TYPE_LIST, editListUri);
		
		startActivityForResult(intent,EDIT_LIST);
	}
	
	private void editWish(long id) {
	    Intent intent = new Intent(this, CreateNewWish.class);
	    
		// get list id #
		String list_id = getListId();
		// inserting into this list
	    intent.putExtra(EXTRA_LIST_ID, list_id);
	    
	    Uri itemUri = Uri.parse(WBContentProvider.CONTENT_URI_WISH + "/" + id);
	    intent.putExtra(WBContentProvider.CONTENT_TYPE_WISHS, itemUri);

	    // Activity returns an result if called with startActivityForResult
	    startActivityForResult(intent, SHOW_WISH);
	}
	
	public void changeWishStatus(View view) {
		Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_content);
		
		if(getCurrentState() == SHOW_LIST) {
			((WBListFragment)current).changeWishStatus(view);
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new FBLogIn();
        dialog.show(getSupportFragmentManager(), "FBLogIn");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
    	Session session = Session.getActiveSession();
		if (session != null && session.getState().isOpened()) {
	        Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
	        	@Override
	            public void onCompleted(GraphUser user, Response response) {
	                if (user != null) {
	                    // Example: typed access (name)
	                    // - no special permissions required
	                    setName(user.getName());
	                }
	        	}
	        });
	        
	        publishFeedDialog();
		}
		else {
			Toast.makeText(this, "Please log in to facebook to share your wish.", Toast.LENGTH_LONG).show();
		}
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        dialog.dismiss();
    }
    
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        // Request user data for the name
	        Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
	        	@Override
	            public void onCompleted(GraphUser user, Response response) {
	                if (user != null) {
	                    // Example: typed access (name)
	                    // - no special permissions required
	                    setName(user.getName());
	                }
	        	}
	        });
	        
	    }
	}
	
	public void publishFeedDialog() {
	    Bundle params = new Bundle();
	    
	    if( getName() == null ) {
	        // Request user data for the name
	        Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
	        	@Override
	            public void onCompleted(GraphUser user, Response response) {
	                if (user != null) {
	                    // Example: typed access (name)
	                    // - no special permissions required
	                    setName(user.getName());
	                }
	        	}
	        });
	    }
        
	    // get the wish that was clicked on
	    WBListFragment frag = (WBListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
	    Cursor cursor = frag.getAdapter().getChild(frag.getClickedGroupPos(), frag.getClickedChildPos());
	    String wish = cursor.getString(cursor.getColumnIndexOrThrow(WishTable.COLUMN_NAME_WISH));
        
        if(getName() != null) {
    	    params.putString("name", getName() + " has posted a wish from a Wish Bucket list!");
        }
        else {
        	params.putString("name", "I have posted a wish from my Wish Bucket list!");
        }
	    params.putString("caption", "List: " + getListName());
	    params.putString("description", wish);
	    params.putString("link", "http://wishbucket777.wix.com/cse190");
	    params.putString("picture", "https://raw.github.com/bshokati/WishBucket/master/Images/wb.jpg");

	    WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {
	            @Override
	            public void onComplete(Bundle values, FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(TabActivity.this, "Posted your Wish Bucket list!", Toast.LENGTH_SHORT).show();
	                    } 
	                    else {
	                        // User clicked the Cancel button
	                        Toast.makeText(TabActivity.this, "Publish cancelled", Toast.LENGTH_SHORT).show();
	                    }
	                } 
	                else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(TabActivity.this, "Publish cancelled", Toast.LENGTH_SHORT).show();
	                } 
	                else {
	                    // Generic, ex: network error
	                    Toast.makeText(TabActivity.this, "Error posting story", Toast.LENGTH_SHORT).show();
	                }
	            }
	        }).build();
	    feedDialog.show();
	}
}
