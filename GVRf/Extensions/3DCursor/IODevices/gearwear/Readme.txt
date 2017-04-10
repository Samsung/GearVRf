The gearwear directory contains necessary projects to enable the use of a 
Samsung GearS2/Gear2 as an IoDevice in the 3DCursorLibrary

    GearInputConsumer:
        Has the tizen project for the app on the watch side.

    PhoneSide/gearinputprovider:
        Implements the provider side for the GearInputConsumer app on the watch.

    PhoneSide/gearwearlibrary:
        Library that any app can use to get events from the watch.

    GearWearIoDevice:
        Implements an IoDevice as defined by the 3DCursorLibrary using the
        gearwearlibrary. Also has the vendor_info.txt.

*********************************************************************************
To use the gearS2 with the gvr-3dcursor app you will need the following:

Prerequisutes:
1. The gearS2 is paired to your phone. 
2. You have the Tizen IDE and Tizen SDK for wearables installed. Follow this guide to 
install Tizen IDE and SDK: https://developer.tizen.org/development/tools/download/installing-sdk#gui
After the installation is complete it will prompt you to run the Tizen Update Manager, click yes and 
install all the packages under the Wearable Tab and the Samsung Certificate Extension and Wearable 
Extension under the Extras Tab.
 

1. Building the .wgt file
1.1 Launch the tizen ide. Executable should be in tizen-sdk/ide
1.2 Generate certificates by following this guide: http://developer.samsung.com/gear/develop/getting-certificates. Look at the sections on “Create new certificates” and “Permit device to install apps”. 
1.3 Launch the project and install it on the watch. This will generate a .wgt file. 

2. Copy the .wgt file to GVRf/Extensions/3DCursor/IODevices/gearwear/PhoneSide/gearinputprovider/src/main/assets/

3. Open the PhoneSide project at GVRf/Extensions/3DCursor/IODevices/gearwear/PhoneSide in android studio. 

4. Build and install the InputProviderService on your phone. 

5. Install the gvr-3dcursor app on your phone. 

The app on the gear watch will work with the gvr-3dcursor app now. 
