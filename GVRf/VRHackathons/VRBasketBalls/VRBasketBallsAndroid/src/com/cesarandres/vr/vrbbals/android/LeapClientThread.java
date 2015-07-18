package com.cesarandres.vr.vrbbals.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.cesarandres.vr.vrbbals.android.MainActivity.COMMANDS;

import android.util.Log;

public class LeapClientThread extends Thread {

	private MainActivity activity;
	public static final String SERVER_ADDRESS = "192.168.0.104";

	public LeapClientThread(MainActivity activity) {
		super(LeapClientThread.class.getName());
		this.activity = activity;
	}

	public void run() {
		while (true) {
			try (Socket kkSocket = new Socket(SERVER_ADDRESS, 9090);
					PrintWriter out = new PrintWriter(
							kkSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(kkSocket.getInputStream()));) {
				BufferedReader stdIn = new BufferedReader(
						new InputStreamReader(System.in));
				String fromServer;
				String fromUser;

				activity.postEvent(COMMANDS.CONNECTED.toString()); 

				while ((fromServer = in.readLine()) != null) {
					if (fromServer.equals("END"))
						break;

					fromUser = stdIn.readLine();
					if (fromUser != null) {
						out.println(fromUser);
					}
					activity.postEvent(fromServer);
				}
			} catch (UnknownHostException e) {
				Log.e(this.getName(), "Don't know about host");
			} catch (IOException e) {
				Log.e(this.getName(), "Couldn't get I/O for the connection");
			}
			activity.postEvent(COMMANDS.DISCONNECTED.toString()); 
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
		}
	}
}