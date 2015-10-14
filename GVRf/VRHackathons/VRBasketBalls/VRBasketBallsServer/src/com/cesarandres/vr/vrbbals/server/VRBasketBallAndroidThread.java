package com.cesarandres.vr.vrbbals.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class VRBasketBallAndroidThread extends Thread {
    private Socket socket = null;
    private PrintWriter out;
    private BufferedReader in;
    
    public VRBasketBallAndroidThread(Socket socket) {
        super(VRBasketBallAndroidThread.class.getName());
        this.socket = socket;
    }
    
    public void run() {
        try{
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
            
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
                if (inputLine.equals("Close"))
                    break;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendMessage(String command){
    	try{
    		out.println(command);
    	}catch(Exception e){
    		System.out.println(e.getLocalizedMessage());
    	}
    }
}