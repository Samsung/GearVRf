/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// TODO: Extract constants, figure out why formatting is breaking up 'const varName' into 2 lines
// TODO: Exit application on page hide (home button press) since back press is overriden
// TODO: Proper lifecycle of network (on show/hide)
// TODO: See how connect/disconnect lifecycle should be. Is there a way to automatically connect/disconnect (when watch connects/disconnects from phone)
// TODO: See if we can use WIFI as well
// TODO: Better structure for sending json
// TODO: If network not connected, don't try sending events
// TODO: Don't show too many popups if network not connected
var MAIN = (function main() {
	var moduleName = "MAIN";
	const
	POINTER_FIRST = 0;

	const
	SHOW_UI = true;

	var contentText;
	var fading = false;
	var networkConnected = false;

	function updateUi() {
		if (!SHOW_UI) {
			return;
		}

		if (!networkConnected) {
			return;
		}

		contentText.innerHTML = sprintf.apply(this, arguments);
		if (!fading) {
			fading = true;
			$("#content-text").hide(0).fadeIn(50, function() {
				fading = false;
			});
		}
	}

	/**
	 * Toggle between connected UI and disconnected UI
	 */
	function toggleConnectUi(connected) {
		if (connected) {
			$("#connect").hide();
			$("#content-text").show();
		} else {
			$("#content-text").hide();
			$("#connect").show();
		}
	}

	function handleTouchStart(evt) {
		var touch = evt.touches[POINTER_FIRST];
		var x = touch.screenX;
		var y = touch.screenY;
		log("touch start: index=%d, x=%d, y=%d", POINTER_FIRST, x, y);

		updateUi("start: x=%d, y=%d", x, y);
		NETWORK.sendTouchStart(x, y);
	}

	function handleTouchMove(evt) {
		var touch = evt.touches[POINTER_FIRST];
		var x = touch.screenX;
		var y = touch.screenY;
		log("touch move: index=%d, x=%d, y=%d", POINTER_FIRST, x, y);

		updateUi("move: x=%d, y=%d", x, y);
		NETWORK.sendTouchMove(x, y)
	}

	function registerSwipeDetection() {
		tau.event.enableGesture(document, new tau.event.gesture.Swipe({
			orientation : "horizontal"
		}));
		tau.event.enableGesture(document, new tau.event.gesture.Swipe({
			orientation : "vertical"
		}));

		document.addEventListener("swipe", function(evt) {
			var direction = evt.detail.direction;
			log("swipe direction = " + direction);
			updateUi("swipe: " + direction);
			NETWORK.sendSwipe(direction)
		});
	}

	function registerTouchDetection() {
		document.addEventListener("touchstart", handleTouchStart, false);
		document.addEventListener("touchmove", handleTouchMove, false);
		document.addEventListener("touchend", function(evt) {
			log("touchend");
			updateUi("touchend");
			NETWORK.sendTouchEnd();
		}, false);
	}

	function registerClickDetection() {
		document.addEventListener("click", function(evt) {
			var x = evt.screenX;
			var y = evt.screenY;
			log("click: x=%d, y=%d", x, y);
			updateUi("Click: x=%d, y=%d", x, y);
			NETWORK.sendClick(x, y);
		}, false);
	}

	function registerRotaryDetection() {
		document.addEventListener("rotarydetent", function(ev) {
			var direction = ev.detail.direction;
			log("rotary event: direction=" + direction);
			var text = "Rotary Event:<br/>%s";
			if (direction == "CCW") {
				updateUi(text, "Counter-clockwise");
			} else {
				updateUi(text, "Clockwise");
			}
			NETWORK.sendRotary(direction);
		}, false);
	}

	function networkMessageListener(object) {
		var type = object.type;
		if (type == null) {
			log("Object type is null");
			return;
		}

		switch (type) {
		default:
			log("Received unhandled message type: " + type);
			break;
		}
	}

	function networkConnectionListener(connected) {
		log('connectionListener: connected=' + connected);
		networkConnected = connected;
		toggleConnectUi(connected);
	}

	function onConnectClick() {
		NETWORK.connect();
	}

	function onLoad() {
		log("onLoad");
		init();
	}

	function onUnload() {
		log("onUnload");
	}

	function onVisibilityChange() {
		if (document[hidden]) {
			log("Page hidden");
		} else {
			log("Page shown");
		}
	}

	if (typeof document.hidden !== "undefined") {
		hidden = "hidden";
		visibilityChange = "visibilitychange";
	} else if (typeof document.webkitHidden !== "undefined") {
		hidden = "webkitHidden";
		visibilityChange = "webkitvisibilitychange";
	}

	function bindEvents() {
		window.addEventListener('load', onLoad);
		window.addEventListener('unload', onUnload);

		document.addEventListener(visibilityChange, onVisibilityChange);
		document.getElementById("connect").addEventListener("click",
				onConnectClick);

		// back event (back HW button, swipe down from top edge)
		document.addEventListener('tizenhwkey', function(ev) {
			log('tizenhwkey pressed: ' + ev.keyName);
			if (ev.keyName === "back") {
				updateUi("Back pressed");
				NETWORK.sendBack();
			}
		});

		registerTouchDetection();
		registerRotaryDetection();
		registerSwipeDetection();
		registerClickDetection();
	}

	function init() {
		log("init");

		contentText = document.querySelector('#content-text');

		toggleConnectUi(false);
		NETWORK.init(networkMessageListener, networkConnectionListener);
		NETWORK.connect();
	}

	function destroy() {
		log("destroy");

		NETWORK.destroy();
	}

	log("Loaded module: " + moduleName);
	bindEvents();
}());