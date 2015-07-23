package com.cesarandres.vr.vrbbals.server;

import java.net.*;
import java.io.*;

public class VRBasketBallsLeapThread extends Thread {
    private Socket socket = null;

    public VRBasketBallsLeapThread(Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
    }
    
    public void run() {

        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
        ) {
            String inputLine, outputLine = null;

            while ((inputLine = in.readLine()) != null) {
                outputLine = inputLine;
                out.println(outputLine);
                if (outputLine.equals(""))
                    break;
            }
            inputLine = in.readLine();
            System.out.println(inputLine);
            VRBasketBallServer.getAndroidThread().sendMessage(inputLine);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}