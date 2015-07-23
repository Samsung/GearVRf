package com.cesarandres.vr.vrbbals.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Listener;

public class VRBasketBallServer extends Listener {
	
	private static VRBasketBallAndroidThread androidThread;

    public static final int PORT_LEAP_NUMBER = 8080;
    public static final int PORT_ANDROID_NUMBER = 9090;
    
    public float lastActionTime;
    
    public class AndroidServer implements Runnable{
		@Override
		public void run() {
	        try (ServerSocket serverSocketAndroid = new ServerSocket(PORT_ANDROID_NUMBER)) {
	        	while (true) {
	        		VRBasketBallAndroidThread newAndroidThread = new VRBasketBallAndroidThread(serverSocketAndroid.accept());
	        		VRBasketBallServer.setAndroidThreas(newAndroidThread);
	        		newAndroidThread.start();
	        	}
	        } catch (IOException e) {
	            System.err.println("There was an error: " + e.getLocalizedMessage());
	            System.exit(-1);
	        }
		}
    }
    
    public class LeapServer implements Runnable{
		@Override
		public void run() {
			try (ServerSocket serverSocket = new ServerSocket(PORT_LEAP_NUMBER)) { 
	            while (true) {
	                new VRBasketBallsLeapThread(serverSocket.accept()).start();
	            }
	        } catch (IOException e) {
	            System.err.println("There was an error: " + e.getLocalizedMessage());
	            System.exit(-1);
	        }
		}
    }
    
    public static void setAndroidThreas(VRBasketBallAndroidThread androidThread){
    	synchronized(VRBasketBallServer.class){
    		VRBasketBallServer.androidThread = androidThread;
    	}
    }
    
    public static VRBasketBallAndroidThread getAndroidThread(){
    	synchronized(VRBasketBallServer.class){
    		return VRBasketBallServer.androidThread;
    	}    	
    }
    
    public static void main(String[] args) throws IOException {
    	VRBasketBallServer server = new VRBasketBallServer();
   	 	(new Thread(server.new AndroidServer())).start(); 
   	 	(new Thread(server.new LeapServer())).start();   
   	 	Controller controller = new Controller();
   	 	controller.addListener(server);
   	 	// Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the sample listener when done
        controller.removeListener(server);
    }
    
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();
              
        for(Gesture gestureObj : frame.gestures())
        {                    
        	if(gestureObj.type() != Gesture.Type.TYPE_SWIPE){
        		continue;
        	}
            switch (gestureObj.state()) {
                case STATE_START:
                    //Handle starting gestures
                    break;
                case STATE_UPDATE:
                    //Handle continuing gestures
                    break;
                case STATE_STOP:
                	if (new Date().getTime() - lastActionTime < 500){
                		break;
                	}
                	
                    System.out.println("LEFT");                	
                    VRBasketBallServer.getAndroidThread().sendMessage("LEFT");
                	lastActionTime = new Date().getTime();
                    break;
                default:
                    //Handle unrecognized states
                    break;
            }
        }
    }
    
    public void onConnect(Controller controller) {
        System.out.println("Connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        /*controller.config().setFloat("Gesture.Swipe.MinLength", 200.0f);
        controller.config().setFloat("Gesture.Swipe.MinVelocity", 750f);
        controller.config().save();*/
    }
}