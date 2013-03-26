package com.example.wishbucket;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.wishbucket.contentprovider.WBContentProvider;
import com.example.wishbucket.database.ListTable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import android.os.AsyncTask;

public class DealsFragment extends ListFragment implements LocationListener {

	private List<Deal> deals;
	private ArrayAdapter<Deal> adapter;
	private double longitude;
	private double latitude;
	private LocationManager locationManager;
	private String providerName;
	private AsyncTask<Void, Integer, String> task; 
	private AsyncTask<Void, Integer, List<Drawable>> imageTask;
	private Map<String, List<Deal>> dealsByTag;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setRetainInstance(true);
		// make the divider bigger!!
		getListView().setDividerHeight(10);
		
		locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
		
	    LocationProvider provider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
	    
	    if(provider == null) {
	    	provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
	    }
	    
	    if(provider != null) {
	    	providerName = provider.getName();
	    }
	    
	    Location location = locationManager.getLastKnownLocation(providerName);

	    // Initialize the location fields
	    if (location != null) {
			System.out.println("Provider " + providerName + " has been selected.");
			onLocationChanged(location);
			locationManager.requestLocationUpdates(providerName, 100000, 100, this);
	    }
	    else {
	    	Toast.makeText(getActivity(), "No GPS location available to retrieve Groupon deals.", Toast.LENGTH_LONG).show();
	    }
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if( task != null ) {
			task.cancel(true);
		}
		if( imageTask != null ) {
			imageTask.cancel(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		locationManager.removeUpdates(this);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_deals, container, false);
		deals = new ArrayList<Deal>();
		dealsByTag = new HashMap<String, List<Deal>>();
		
		View grouponView = getActivity().getLayoutInflater().inflate(R.layout.groupon_deals_logo, null);
	
		ListView list = (ListView) view.findViewById(android.R.id.list);
		list.addHeaderView(grouponView, null, false);
		
		return view;
	}
	
	/* class for sending http get request to groupon to get deal image */
	public class GetDealsImageTask extends AsyncTask<Void, Integer, List<Drawable>> {

		private ProgressBar pbM;
		private TextView emptyTextM;
		
		public GetDealsImageTask(ProgressBar pb, TextView emptyText) {
			pbM = pb;
			emptyTextM = emptyText;
		}
		
		@Override
		protected List<Drawable> doInBackground(Void... arg0) {
			List<Drawable> dealImages = null;
			
			if( !imageTask.isCancelled() ) {
				dealImages = new ArrayList<Drawable>();
				for(Deal deal : deals) {
					/* get deal image */
				    Drawable drw = ImageOperations(getActivity(), deal.getSmallImageUrl(), deal.toString());
					dealImages.add(drw);
				}
				
				publishProgress(75);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return dealImages;
		}
	    
		protected void onProgressUpdate(Integer... progress) {
	    	if(!imageTask.isCancelled()) {
		    	super.onProgressUpdate(progress[0]);
		    	pbM.setProgress(progress[0]);
	    	}
	    }
		
		protected void onPostExecute(List<Drawable> dealImages) {
			if(!imageTask.isCancelled() && dealImages != null) {
				super.onPostExecute(dealImages);
				Iterator<Drawable> iterImages = dealImages.iterator();
				Iterator<Deal> iterDeals = deals.iterator();
				
				while(iterImages.hasNext() && iterDeals.hasNext()) {
					iterDeals.next().setImage(iterImages.next());
				}
				
				if(adapter != null) {
					adapter.notifyDataSetChanged();
				}
				else {
					adapter = new DealsAdapter(getActivity(), R.layout.deals_row, deals);
					setListAdapter(adapter);
					adapter.notifyDataSetChanged();
				}
				
				pbM.setProgress(0);
				pbM.setVisibility(View.GONE);
				emptyTextM.setText(R.string.no_deals);
			}
		}
		
	}

	/* class for sending http get request to groupon */
	public class GetDealsTask extends AsyncTask< Void, Integer, String> {
		
		private ProgressBar pbM;
		private TextView emptyTextM;
		
		public GetDealsTask(ProgressBar pb, TextView emptyText) {
			pbM = pb;
			emptyTextM = emptyText;
		}
		
		protected void onPreExecute() {
			if(!task.isCancelled()) {
				super.onPreExecute();
				pbM.setVisibility(View.VISIBLE);
			}
		}
		
	    protected String doInBackground(Void... v) {
	    	String result = null;
	    	if(!task.isCancelled()) {
	
		    	// Instantiate the Web Service Class with he URL of the web service not that you must pass
		    	WebService webService = new WebService("http://api.groupon.com/v2/deals");
		   
				//Pass the parameters if needed , if not then pass dummy one as follows
				Map<String, String> params = new HashMap<String, String>();
				/** INSERT YOUR UNIQUE CLIENT ID FROM GROUPON API BELOW **/
				params.put("client_id", "0000000000000");
	
				params.put("lat", String.valueOf(latitude));
				params.put("lng", String.valueOf(longitude));
				
				publishProgress(25);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Get JSON response from server the "" are where the method name would normally go if needed example
				// webService.webGet("getMoreAllerts", params);
				result = webService.webGet("", params);
				
				publishProgress(50);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return result;
	    }

	    protected void onProgressUpdate(Integer... progress) {
	    	if(!task.isCancelled()) {

		    	super.onProgressUpdate(progress[0]);
		    	pbM.setProgress(progress[0]);
	    	}
	    }

	    protected void onPostExecute(String result) {
	    	if(!task.isCancelled() && result != null) {
		    	super.onPostExecute(result);
		    	try {
					
					//Parse Response into our object
					JsonParser jsonParser = new JsonParser();
					JsonArray jDeals = jsonParser.parse(result).getAsJsonObject().get("deals").getAsJsonArray();
					   
					Deal deal = null;
					String title;
					String smallImageUrl;
					String dealUrl;
					
					for( JsonElement jDeal : jDeals) {
						deal = new Deal();
						title = jDeal.getAsJsonObject().get("title").toString();
						title = title.replaceAll("^\"|\"$", "");
						String shortenedTitle = null;
						if(title.length() > 35) {
							shortenedTitle = title.substring(0, 35) + "\n";
							if( title.length() > 70 ) {
								shortenedTitle += title.substring(35, 70) + "...";
							}
							title = shortenedTitle;
						}
						deal.setTitle(title);
						smallImageUrl = jDeal.getAsJsonObject().get("largeImageUrl").toString();
						smallImageUrl = smallImageUrl.replaceAll("^\"|\"$", "");
						deal.setSmallImageUrl(smallImageUrl);
						dealUrl = jDeal.getAsJsonObject().get("dealUrl").toString();
						dealUrl = dealUrl.replaceAll("^\"|\"$", "");
						deal.setDealUrl(dealUrl);
					    JsonArray options = jDeal.getAsJsonObject().get("options").getAsJsonArray();
					    JsonObject price = options.get(0).getAsJsonObject().get("price").getAsJsonObject();
					    String amount = price.get("formattedAmount").toString();
					    amount = amount.replaceAll("^\"|\"$", "");
						deal.setPrice(amount);
						
						
						/*tags */
						JsonArray tagArray = jDeal.getAsJsonObject().get("tags").getAsJsonArray();
						String tagString = null;
						List<Deal> tagDealsList = null;
						for(JsonElement tag : tagArray) {
							tagString = tag.getAsJsonObject().get("name").toString();
							tagString = tagString.replaceAll("^\"|\"$", "");
							tagString = tagString.toLowerCase(Locale.US);
							
							if(dealsByTag.containsKey(tagString)) {
								tagDealsList = dealsByTag.get(tagString);
								tagDealsList.add(deal);
								dealsByTag.put(tagString, tagDealsList);
							}
							else {
								tagDealsList = new ArrayList<Deal>();
								tagDealsList.add(deal);
								dealsByTag.put(tagString, tagDealsList);
							}
						}
						
						deals.add(deal);
					}
					
					deals = simpleMatch();
					imageTask = new GetDealsImageTask(pbM, emptyTextM);
					Void v = null;
					imageTask.execute(v);
		    	}
			    catch(Exception e)
			    {
			        e.printStackTrace();
			    }
		    }
	    }
	}

	@Override
	public void onLocationChanged(Location location) {
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
		
		if(getActivity() != null) {
			if(isNetworkAvailable()) {
				ProgressBar pbDefaultM = (ProgressBar) getActivity().findViewById(R.id.pbDefault);
				TextView emptyText = (TextView) getActivity().findViewById(android.R.id.empty);
				emptyText.setText(R.string.loading_deals);
				Void v = null;
				task = new GetDealsTask(pbDefaultM, emptyText);
				task.execute(v);
			}
			else {
			    Toast.makeText(getActivity(), "No internet access available. Please connect to the Internet for deals." + providerName, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
    private boolean isNetworkAvailable() {
    	ConnectivityManager connectivityManager = null;
    	NetworkInfo activeNetworkInfo = null;
    	
    	try {
        	connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        	activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

	@Override
	public void onProviderDisabled(String provider) {
	    Toast.makeText(getActivity(), "GPS Disabled, Enable GPS for Groupon deals." + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		// open up web browser to view deal
		Deal deal = adapter.getItem(position - 1); // subtract by one to adjust for header
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(deal.getDealUrl()));
		startActivity(i);
	}
	
	private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, saveFilename);
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object fetch(String address) throws MalformedURLException,IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }
    
    public List<Deal> simpleMatch() {
    	HashSet<Deal> filteredDeals = new HashSet<Deal>();
    	
    	String [] projectionList = { ListTable.COLUMN_NAME_LIST };
    	Cursor listCursor = getActivity().getContentResolver().query(WBContentProvider.CONTENT_URI_LIST, projectionList, null, null, null);
    	
    	listCursor.moveToFirst();
    	String listName = null;
    	List<Deal> dealsList = null;
    	for(int i = 0; i < listCursor.getCount(); i++) {
    		listName = listCursor.getString(listCursor.getColumnIndexOrThrow(ListTable.COLUMN_NAME_LIST));
    		listName = listName.toLowerCase();
    		if(dealsByTag.containsKey(listName)) {
    			dealsList = dealsByTag.get(listName);
    			filteredDeals.addAll(dealsList);
    		}
    	}
    	
    	listCursor.close();
    	
    	dealsList = new ArrayList<Deal>(filteredDeals);
    	
    	return dealsList;
    }

}
