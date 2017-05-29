The modules in this folders are required in order to use the mobile device as an IO device

1. gvrmobiledevice: This IO module should be imported into the VR application. The Hand that is visible in the application is
		    the cursor and the behavious of the hand/cursor exemplifies the use of phone as an IO device. The phone on the 
		    the headset side is the server. It spins a thread that continously reads the incoming data, that is a bytearray.
The size of this byte array is 257. The first 256 bytes encode the quaternion and the last byte encodes if it should be used for 
position or orientation. 

2. PhoneIODevice: This app should be installed on the device that will be held in the hand. It obtains and converts the quaternion into byte array and 
		  writes it to the outputstream.This is the client. When the app is opened a listview of devices on the same network is populated.
		  The name of the device on the headset should be selected, and also its IP address should be enetered into the tab. Then click on
connect to establish a connection with the server. This will render a inputstream and an outputstream.

Todo: Make the apps more modular in terms of using any type of communication - bluetooth or wifi. Currently the app uses only wifi.
		    
            
