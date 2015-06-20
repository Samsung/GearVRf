package pw.ian.vrtransit.data;

import com.firebase.client.Firebase;

public class Weather {
	private static Firebase ref = new Firebase(
			"https://publicdata-weather.firebaseio.com/sanfrancisco/currently");

	/**
	 * This function returns the weather in degrees as a string
	 **/
	public static String getWeather() {
		return ref.child("temperature").toString();
	}

}