package pw.ian.vrtransit.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import pw.ian.vrtransit.Constants;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

public class TransitDataAccessor {
	private Query ref = new Firebase(
			"https://publicdata-transit.firebaseio.com/sf-muni/vehicles")
			.limitToLast(Constants.MAX_OBJECTS);

	private ArrayBlockingQueue<BusUpdate> pendingUpdates = new ArrayBlockingQueue<>(
			10000);

	private String type;

	public TransitDataAccessor(String type) {
		this.type = type;

		ref.addChildEventListener(new ChildEventListener() {

			@Override
			public void onCancelled(FirebaseError arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onChildAdded(DataSnapshot ds, String key) {
				String id = ds.child("id").getValue(String.class);
				String route = ds.child("routeTag").getValue(String.class);
				double lat = ds.child("lat").getValue(Double.class);
				double lon = ds.child("lon").getValue(Double.class);
				String type = ds.child("vtype").getValue(String.class);
				if (!type.equals(TransitDataAccessor.this.type))
					return;
				pendingUpdates.add(new BusUpdate(id, route, lat, lon, type));
			}

			@Override
			public void onChildChanged(DataSnapshot ds, String prevKey) {
				String id = ds.child("id").getValue(String.class);
				String route = ds.child("routeTag").getValue(String.class);
				double lat = ds.child("lat").getValue(Double.class);
				double lon = ds.child("lon").getValue(Double.class);
				String type = ds.child("vtype").getValue(String.class);
				if (!type.equals(TransitDataAccessor.this.type))
					return;
				pendingUpdates.add(new BusUpdate(id, route, lat, lon, type));
			}

			@Override
			public void onChildMoved(DataSnapshot arg0, String arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onChildRemoved(DataSnapshot ds) {
				String id = ds.child("id").getValue(String.class);
				String route = ds.child("routeTag").getValue(String.class);
				double lat = ds.child("lat").getValue(Double.class);
				double lon = ds.child("lon").getValue(Double.class);
				String type = ds.child("vtype").getValue(String.class);
				if (!type.equals(TransitDataAccessor.this.type))
					return;
				BusUpdate bu = new BusUpdate(id, route, lat, lon, type);
				bu.remove = true;
				pendingUpdates.add(bu);
			}
		});

	}

	public List<BusUpdate> nextUpdates() {
		List<BusUpdate> ret = new ArrayList<>();
		pendingUpdates.drainTo(ret);
		return ret;
	}
}
