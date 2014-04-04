package course.labs.locationlab;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class PlaceViewActivity extends ListActivity implements LocationListener {
	private static final long FIVE_MINS = 5 * 60 * 1000;

	private static String TAG = "Lab-Location";

	private Location mLastLocationReading;
	private PlaceViewAdapter mAdapter;

	// default minimum time between new readings
	private long mMinTime = 5000;

	// default minimum distance between old and new readings.
	private float mMinDistance = 1000.0f;

	private LocationManager mLocationManager;

	// A fake location provider used for testing
	private MockLocationProvider mMockLocationProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


        // This class is a ListActivity, so it has its own ListView
        // ListView's adapter should be a PlaceViewAdapter
        mAdapter = new PlaceViewAdapter(getApplicationContext());
        setListAdapter(mAdapter);
		
        // You can use footer_view.xml to define the footer
        ListView list = getListView();
        View footerView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view,null,false);
        list.addFooterView(footerView);

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		footerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                log("Entered footerView.OnClickListener.onClick()");


                if (mLastLocationReading == null) {
                    log("Location data is not available");
                    return;
                }

//                boolean beenHere = false;
//                int locationsCount = mAdapter.getCount();
//
//                for (int i = 0; i < locationsCount; i++) {
//                    PlaceRecord record = (PlaceRecord)mAdapter.getItem(i);
//                    if (record.intersects(mLastLocationReading)) {
//                        beenHere = true;
//                        break;
//                    }
//                }

                if (mAdapter.intersects(mLastLocationReading)) {
                    Toast.makeText(getApplicationContext(),"You have already been here",Toast.LENGTH_LONG);
                    log("You already have this location badge");
                } else {
                    log("Starting Place Download");
                    new PlaceDownloaderTask(PlaceViewActivity.this).execute(mLastLocationReading);
                }


            }
        });
        //  - When the footerView's onClick() method is called, it must issue the

        
        // footerView must respond to user clicks.
        // Must handle 3 cases:
        // 1) The current location is new - download new Place Badge. Issue the
        // following log call:
        // log("Starting Place Download");

        // 2) The current location has been seen before - issue Toast message.
        // Issue the following log call:
        // log("You already have this location badge");
        
        // 3) There is no current location - response is up to you. The best
        // solution is to disable the footerView until you have a location.
        // Issue the following log call:
        // log("Location data is not available");
 		

	}

	@Override
	protected void onResume() {
		super.onResume();

		mMockLocationProvider = new MockLocationProvider(
				LocationManager.NETWORK_PROVIDER, this);

//        mMockLocationProvider
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,mMinTime,mMinDistance,this);

        if (mLastLocationReading == null || age(location) >  FIVE_MINS)
            return;
        mLastLocationReading = location;


	}

	@Override
	protected void onPause() {

		mMockLocationProvider.shutdown();

		mLocationManager.removeUpdates(this);

		super.onPause();
	}

	// Callback method used by PlaceDownloaderTask
	public void addNewPlace(PlaceRecord place) {

		log("Entered addNewPlace()");
		mAdapter.add(place);

	}

	@Override
	public void onLocationChanged(Location currentLocation) {

        if (mLastLocationReading == null  ||
                currentLocation.getTime() > mLastLocationReading.getTime())
            mLastLocationReading = currentLocation;
        // Cases to consider
        // 1) If there is no last location, keep the current location.
        // 2) If the current location is older than the last location, ignore
        // the current location
        // 3) If the current location is newer than the last locations, keep the
        // current location.


	}

	@Override
	public void onProviderDisabled(String provider) {
		// not implemented
	}

	@Override
	public void onProviderEnabled(String provider) {
		// not implemented
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// not implemented
	}

	private long age(Location location) {
		return System.currentTimeMillis() - location.getTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.print_badges:
			ArrayList<PlaceRecord> currData = mAdapter.getList();
			for (int i = 0; i < currData.size(); i++) {
				log(currData.get(i).toString());
			}
			return true;
		case R.id.delete_badges:
			mAdapter.removeAllViews();
			return true;
		case R.id.place_one:
			mMockLocationProvider.pushLocation(37.422, -122.084);
			return true;
		case R.id.place_invalid:
			mMockLocationProvider.pushLocation(0, 0);
			return true;
		case R.id.place_two:
			mMockLocationProvider.pushLocation(38.996667, -76.9275);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static void log(String msg) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, msg);
	}

}
