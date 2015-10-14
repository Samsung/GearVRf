VRBasketBall
============

The VRBasketBall project is a simple demo/game where you have to keep the balls spinning by using Leap Motion gestures. This project is composed of three components, the application running in the GearVR, the server that will relay information to the application from the HTML site that interacts with the LeapMotion. 

A new interaction mode has been added recently and now the server can connect to the LeapMotion and send events to the application connecteed to it.

# GestureRecognizer
This HTML page will uses the LeapMotion Javascript sdk to read the swipe motions and it will then send the message to the server. Currently this component is not working, we will update it soon.

# VRBasketBallsAndroid
This is the game itself running in the GearVR. The objective is simple, spin the balls and don't let spin two slow or you will lose. As you play more balls will start appearing and the challenge will increase. What your highest score will be?

To spin the balls look at them and then tap the touchpad or make a swipe motion when running a VRBasketBallsServer. If you want to restart the game, touch and hold the touchpad for three seconds.

# VRBasketBallsServer
To communicate the LeapMotion events to the phone this server will act as an intermediary. As an added feature now the server can also be connected to the LeapMotion and can send events to the phone. Currently the LeapMotion support in the server is only for Mac.

## License
MIT
