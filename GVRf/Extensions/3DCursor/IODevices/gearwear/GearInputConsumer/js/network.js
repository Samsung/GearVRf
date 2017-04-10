/*
 * Copyright (C) 2014 Samsung Electronics. All Rights Reserved.
 * Source code is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * IMPORTANT LICENSE NOTE:
 * The IMAGES AND RESOURCES are licensed under the Creative Commons BY-NC-SA 3.0
 * License (http://creativecommons.org/licenses/by-nc-sa/3.0/).
 * The source code is allows commercial re-use, but IMAGES and RESOURCES forbids it.
 */

var NETWORK = (function network() {
	var moduleName = "NETWORK";
	var SAAgent = null;
	var SASocket = null;
	var SAPeerAgent = null;
	var messageListener;
	var connectionListener;

	var CHANNEL_ID = 104;
	var PROVIDER_APP_NAME = "GearInputProvider";

	var MESSAGE_TYPE = {
		"CONNECTION_STATUS" : "CONNECTION_STATUS",
		"ERROR" : "ERROR",
		"INPUT" : "INPUT"
	};

	var INPUT_EVENT = {
		"TOUCH_START" : "TOUCH_START",
		"TOUCH_MOVE" : "TOUCH_MOVE",
		"TOUCH_END" : "TOUCH_END",
		"CLICK" : "CLICK",
		"ROTARY" : "ROTARY",
		"SWIPE" : "SWIPE",
		"BACK" : "BACK"
	};

	var ROTARY_DIRECTION = {
		"CW" : "CW", // Clockwise
		"CCW" : "CCW", // Counter-clockwise
	}

	var SWIPE_DIRECTION = {
		"UP" : "UP",
		"DOWN" : "DOWN",
		"LEFT" : "LEFT",
		"RIGHT" : "RIGHT"
	}

	var requestServiceConnectionCallback = {
		onrequest : function(peerAgent) {
			log("Connection requested from peerAgent "
					+ peerAgent.appName);
		},
		onconnect : function(socket) {
			log("requestServiceConnectionCallback: onconnect");
			SASocket = socket;
			alert("Connection established with GearInputProvider");
			try {
				SASocket.setDataReceiveListener(onReceiveData);
				SASocket.setSocketStatusListener(function(reason) {
					log("Service connection lost, reason: [" + reason
							+ "]");
					if (SASocket != null) {
						disconnect(true);
					}
					connectionListener(false);
				});
				connectionListener(true);
			} catch (err) {
				log(UTILITY.createErrorString(
						"Internal Error: onconnect()", err));
				alert("Internal Error: onconnect()");
				connectionListener(false);
			}
		},
		onerror : function(errorCode) {
			log("requestServiceConnectionCallback: errorCode="
					+ errorCode);
			alert("requestServiceConnectionCallback error");
			connectionListener(false);
		}
	};

	var peerAgentFindCallback = {
		onpeeragentfound : function(peerAgent) {
			if (peerAgent.appName == PROVIDER_APP_NAME) {
				try {
					SAPeerAgent = peerAgent;
					SAAgent
							.setServiceConnectionListener(requestServiceConnectionCallback);
					log("Requesting service connection with "
							+ peerAgent.appName);
					SAAgent.requestServiceConnection(peerAgent);
				} catch (err) {
					log(UTILITY.createErrorString(
							"Internal Error: requestServiceConnection()", err));
					alert("Internal Error: requestServiceConnection()");
					connectionListener(false);
				}
			} else {
				log("Found unexpected peerAgent: " + peerAgent.appName);
				alert("Found unexpected peerAgent: " + peerAgent.appName);
				connectionListener(false);
			}
		},
		onerror : function(errorCode) {
			log("peerAgentFindCallback: errorCode=" + errorCode);
			alert("Error finding peer agent");
			connectionListener(false);
		}
	};

	function onRequestSAAgentSuccess(agents) {
		if (agents.length > 0) {
			log("onRequestSAAgentSuccess: local SAAgent found");
			SAAgent = agents[0];

			try {
				SAAgent.setPeerAgentFindListener(peerAgentFindCallback);
				log("onRequestSAAgentSuccess: Finding peer agents");
				SAAgent.findPeerAgents();
			} catch (err) {
				alert("Internal Error: findPeerAgents()");
				log(UTILITY.createErrorString(
						"onRequestSAAgentSuccess", err));
				connectionListener(false);
			}
		} else {
			log("onRequestSAAgentSuccess: No local SAAgent found");
			alert("No local SAAgent found");
			connectionListener(false);
		}
	}

	function onRequestSAAgentError(err) {
		log(UTILITY.createErrorString("Error requesting local SAAgent",
				err));
		alert("Error requesting local SAAgent");
		connectionListener(false);
	}

	function onDeviceStatusChange(type, status) {
		log("onDeviceStatusChange: type=" + type + " status=" + status);
		if (status == "ATTACHED") {
			log("onDeviceStatusChange: Attached to host");
		} else if (status == "DETACHED") {
			log("onDeviceStatusChange: Detached from host");
			if (SAAgent !== null && SASocket === null) {
				// Can attempt to findProvider here
			}
		}
	}

	function init(networkMessageListener, networkConnectionListener) {
		if (!networkMessageListener) {
			log("Invalid networkMessageListener");
			return;
		}

		if (!networkConnectionListener) {
			log("Invalid networkConnectionListener");
			return;
		}

		messageListener = networkMessageListener;
		connectionListener = networkConnectionListener;
		log("Network initialized");
	}

	function destroy() {
		log("destroy");
		disconnect(false);
		messageListener = null;
	}

	function connect() {
		log("connect");
		if (SASocket) {
			log("Already connected");
			alert("Already connected!");
			return false;
		}

		try {
			log("Requesting SAAgent");
			webapis.sa.requestSAAgent(onRequestSAAgentSuccess,
					onRequestSAAgentError);
			webapis.sa.setDeviceStatusListener(onDeviceStatusChange);
		} catch (err) {
			log(UTILITY.createErrorString("Internal Error: connect()",
					err));
			alert("Internal Error: connect()");
			connectionListener(false);
			return;
		}
	}

	function disconnect(enable) {
		if (SASocket != null) {
			log("Disconnecting");
			try {
				SASocket.close();
			} catch (err) {
				log(UTILITY.createErrorString(
						"Error closing connection", err));
			} finally {
				SASocket = null;
				UTILITY.showAlert("Disconnected from peer", enable);
			}
		} else {
			log("Not disconnecting, already disconnected");
			UTILITY.showAlert("Already disconnected", enable);
		}
	}

	function onReceiveData(channelId, data) {
		log("Received data: " + data);
		var object = JSON.parse(data);

		if (object == null) {
			log("Parsed object is null");
			return;
		}

		if (!messageListener) {
			log("No messageListener");
			return;
		}

		messageListener(object);
	}

	function sendString(data, enable) {
		if (SASocket === null) {
			log("SASocket is null, cannot send string");
			UTILITY.showAlert("You need to press connect first", enable);
			return;
		}

		var jsonString = JSON.stringify(data);

		if (SAPeerAgent != null
				&& jsonString.length > SAPeerAgent.maxAllowedDataSize) {
			log("jsonString (" + jsonString
					+ ") exceeds max allowed data size");
			UTILITY.showAlert("Cannot send data, string is too long", enable);
			return;
		}

		try {
			log("sendString: " + jsonString);
			SASocket.sendData(CHANNEL_ID, jsonString);
		} catch (err) {
			log(UTILITY.createErrorString("sendString", err));
			UTILITY.showAlert("Internal Error: sendString", enable);
		}
	}

	function sendTouchStart(x, y) {
		var message = {
			"type" : MESSAGE_TYPE.INPUT,
			"event" : INPUT_EVENT.TOUCH_START,
			"x" : x,
			"y" : y
		};

		sendString(message, false);
	}

	function sendTouchMove(x, y) {
		var message = {
			"type" : MESSAGE_TYPE.INPUT,
			"event" : INPUT_EVENT.TOUCH_MOVE,
			"x" : x,
			"y" : y
		};

		sendString(message, false);
	}

	function sendTouchEnd() {
		var message = {
			"type" : MESSAGE_TYPE.INPUT,
			"event" : INPUT_EVENT.TOUCH_END
		};

		sendString(message, false);
	}

	function sendClick(x, y) {
		log("sendClick");
		var message = {
			"type" : MESSAGE_TYPE.INPUT,
			"event" : INPUT_EVENT.CLICK,
			"x" : x,
			"y" : y
		};

		sendString(message, false);
	}

	function sendRotary(direction) {
		switch (direction) {
		case "CW":
			direction = ROTARY_DIRECTION.CW;
			break;
		case "CCW":
			direction = ROTARY_DIRECTION.CCW;
			break;
		default:
			log("Unknown swipe direction: " + direction);
			return;
		}
		var message = {
			"type" : MESSAGE_TYPE.INPUT,
			"event" : INPUT_EVENT.ROTARY,
			"direction" : direction
		};

		sendString(message, false);
	}

	function sendSwipe(direction) {
		switch (direction) {
		case "up":
			direction = SWIPE_DIRECTION.UP;
			break;
		case "down":
			direction = SWIPE_DIRECTION.DOWN;
			break;
		case "left":
			direction = SWIPE_DIRECTION.LEFT;
			break;
		case "right":
			direction = SWIPE_DIRECTION.RIGHT;
			break;
		default:
			log("Unknown swipe direction: " + direction);
			return;
		}

		var message = {
			"type" : MESSAGE_TYPE.INPUT,
			"event" : INPUT_EVENT.SWIPE,
			"direction" : direction
		};

		sendString(message, false);
	}

	function sendBack() {
		var message = {
			"type" : MESSAGE_TYPE.INPUT,
			"event" : INPUT_EVENT.BACK
		}

		sendString(message, false);
	}

	log("Loaded module: " + moduleName);

	return {
		init : init,
		destroy : destroy,
		connect : connect,
		disconnect : disconnect,
		sendTouchStart : sendTouchStart,
		sendTouchMove : sendTouchMove,
		sendTouchEnd : sendTouchEnd,
		sendClick : sendClick,
		sendRotary : sendRotary,
		sendSwipe : sendSwipe,
		sendBack : sendBack
	};

}());