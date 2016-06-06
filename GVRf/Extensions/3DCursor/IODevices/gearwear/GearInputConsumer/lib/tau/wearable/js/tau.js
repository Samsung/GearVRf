/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function(window, document, undefined) {

var ns = window.tau = {},
nsConfig = window.tauConfig = window.tauConfig || {};
nsConfig.rootNamespace = 'tau';
nsConfig.fileName = 'tau';
ns.version = '0.10.29-14';
/*global window, console, define, ns, nsConfig */
/*jslint plusplus:true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Core namespace
 * Object contains main framework methods.
 * @class ns
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Maciej Moczulski <m.moczulski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 * @author Tomasz Lukawski <t.lukawski@samsung.com>
 */
(function (document, ns, nsConfig) {
	
			var idNumberCounter = 0,
			currentDate = +new Date(),
			slice = [].slice,
			rootNamespace = nsConfig.rootNamespace,
			fileName = nsConfig.fileName,
			infoForLog = function (args) {
				var dateNow = new Date();
				args.unshift('[' + rootNamespace + '][' + dateNow.toLocaleString() + ']');
			};

		/**
		* Return unique id
		* @method getUniqueId
		* @static
		* @return {string}
		* @member ns
		*/
		ns.getUniqueId = function () {
			return rootNamespace + "-" + ns.getNumberUniqueId() + "-" + currentDate;
		};

		/**
		* Return unique id
		* @method getNumberUniqueId
		* @static
		* @return {number}
		* @member ns
		*/
		ns.getNumberUniqueId = function () {
			return idNumberCounter++;
		};

		/**
		* logs supplied messages/arguments
		* @method log
		* @static
		* @param {...*} argument
		* @member ns
		*/
		ns.log = function () {
			var args = slice.call(arguments);
			infoForLog(args);
			if (console) {
				console.log.apply(console, args);
			}
		};

		/**
		* logs supplied messages/arguments ad marks it as warning
		* @method warn
		* @static
		* @param {...*} argument
		* @member ns
		*/
		ns.warn = function () {
			var args = slice.call(arguments);
			infoForLog(args);
			if (console) {
				console.warn.apply(console, args);
			}
		};

		/**
		* logs supplied messages/arguments and marks it as error
		* @method error
		* @static
		* @param {...*} argument
		* @member ns
		*/
		ns.error = function () {
			var args = slice.call(arguments);
			infoForLog(args);
			if (console) {
				console.error.apply(console, args);
			}
		};

		/**
		* get from nsConfig
		* @method getConfig
		* @param {string} key
		* @param {*} defaultValue
		* @return {*}
		* @static
		* @member ns
		*/
		ns.getConfig = function (key, defaultValue) {
			return nsConfig[key] === undefined ? defaultValue : nsConfig[key];
		};

		/**
		 * set in nsConfig
		 * @method setConfig
		 * @param {string} key
		 * @param {*} value
		 * @param {boolean} [asDefault=false] value should be treated as default (doesn't overwrites the config[key] if it already exists)
		 * @static
		 * @member ns
		*/
		ns.setConfig = function (key, value, asDefault) {
			if (!asDefault || (asDefault && nsConfig[key] === undefined)) {
				nsConfig[key] = value;
			}
		};

		/**
		 * Return path for framework script file.
		 * @method getFrameworkPath
		 * @returns {?string}
		 * @member ns
		 */
		ns.getFrameworkPath = function () {
			var scripts = document.getElementsByTagName('script'),
				countScripts = scripts.length,
				i,
				url,
				arrayUrl,
				count;
			for (i = 0; i < countScripts; i++) {
				url = scripts[i].src;
				arrayUrl = url.split('/');
				count = arrayUrl.length;
				if (arrayUrl[count - 1] === fileName + '.js' || arrayUrl[count - 1] === fileName + '.min.js') {
					return arrayUrl.slice(0, count - 1).join('/');
				}
			}
			return null;
		};

		}(window.document, ns, nsConfig));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint plusplus: true, nomen: true */
//  * @TODO add support of $.mobile.buttonMarkup.hoverDelay
/*
 * Defaults settings object
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @class ns.defaults
 */
(function (ns) {
	
	
			ns.defaults = {};

			Object.defineProperty(ns.defaults, "autoInitializePage", {
				 get: function(){
					 return ns.getConfig("autoInitializePage", true);
				 },
				 set: function(value){
					 return ns.setConfig("autoInitializePage", value);
				 }
			});

			Object.defineProperty(ns.defaults, "dynamicBaseEnabled", {
				 get: function(){
					 return ns.getConfig("dynamicBaseEnabled", true);
				 },
				 set: function(value){
					 return ns.setConfig("dynamicBaseEnabled", value);
				 }
			});

			Object.defineProperty(ns.defaults, "pageTransition", {
				 get: function(){
					 return ns.getConfig("pageTransition", "none");
				 },
				 set: function(value){
					 return ns.setConfig("pageTransition", value);
				 }
			});

			Object.defineProperty(ns.defaults, "popupTransition", {
				 get: function(){
					 return ns.getConfig("popupTransition", "none");
				 },
				 set: function(value){
					 return ns.setConfig("popupTransition", value);
				 }
			});

			Object.defineProperty(ns.defaults, "popupFullSize", {
				get: function(){
					return ns.getConfig("popupFullSize", false);
				},
				set: function(value){
					return ns.setConfig("popupFullSize", value);
				}
			});

			Object.defineProperty(ns.defaults, "enablePageScroll", {
				get: function(){
					return ns.getConfig("enablePageScroll", false);
				},
				set: function(value){
					return ns.setConfig("enablePageScroll", value);
				}
			});

			Object.defineProperty(ns.defaults, "scrollEndEffectArea", {
				get: function(){
					return ns.getConfig("scrollEndEffectArea", "content");
				},
				set: function(value){
					return ns.setConfig("scrollEndEffectArea", value);
				}
			});

			Object.defineProperty(ns.defaults, "enablePopupScroll", {
				get: function(){
					return ns.getConfig("enablePopupScroll", false);
				},
				set: function(value){
					return ns.setConfig("enablePopupScroll", value);
				}
			});
			}(ns));

/*global window, define*/
/*jslint bitwise: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (ns) {
	
	
			// Default configuration properties
			ns.setConfig('rootDir', ns.getFrameworkPath(), true);
			ns.setConfig('version', '');
			ns.setConfig('allowCrossDomainPages', false, true);
			ns.setConfig('domCache', false, true);
			// .. other possible options
			// ns.setConfig('autoBuildOnPageChange', true);
			// ns.setConfig('autoInitializePage', true);
			// ns.setConfig('container', document.body); // for defining application container
			// ns.setConfig('pageContainer', document.body); // same as above, but for wearable version

			}(ns));

/*global window, define*/
/*jslint bitwise: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @class ns.support
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 */
(function (window, document, ns) {
	
			var isTizen = !(typeof tizen === "undefined");

			function isCircleShape() {
				var testDiv = document.createElement("div"),
					fakeBody = document.createElement("body"),
					html = document.getElementsByTagName('html')[0],
					style = getComputedStyle(testDiv),
					isCircle;

				testDiv.classList.add("is-circle-test");
				fakeBody.appendChild(testDiv);
				html.insertBefore(fakeBody, html.firstChild);
				isCircle = style.width === "1px";
				html.removeChild(fakeBody);

				return isCircle;
			}

			ns.support = {
				cssTransitions: true,
				mediaquery: true,
				cssPseudoElement: true,
				touchOverflow: true,
				cssTransform3d: true,
				boxShadow: true,
				scrollTop: 0,
				dynamicBaseTag: true,
				cssPointerEvents: false,
				boundingRect: true,
				browser: {
					ie: false,
					tizen: isTizen
				},
				shape: {
					circle: isTizen ? window.matchMedia("(-tizen-geometric-shape: circle)").matches : isCircleShape(),
				},
				gradeA : function () {
					return true;
				}
			};
			}(window, window.document, ns));

/*global window, define*/
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint bitwise: true */
/*
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (ns) {
	
				// Default configuration properties for wearable
			ns.setConfig("autoBuildOnPageChange", false, true);

			if(ns.support.shape.circle) {
				ns.setConfig("pageTransition", "pop");
				ns.setConfig("popupTransition", "pop");

				ns.setConfig("popupFullSize", true);
				ns.setConfig("scrollEndEffectArea", "screen");
				ns.setConfig("enablePageScroll", true);
				ns.setConfig("enablePopupScroll", true);
			} else {
				ns.setConfig("popupTransition", "slideup");
				ns.setConfig("enablePageScroll", false);
				ns.setConfig("enablePopupScroll", false);
			}
			// .. other possible options
			// ns.setConfig('autoInitializePage', true);
			// ns.setConfig('pageContainer', document.body); // defining application container for wearable

			}(ns));

/*global window, define, XMLHttpRequest, console, Blob */
/*jslint nomen: true, browser: true, plusplus: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Utilities
 *
 * The Tizen Advanced UI (TAU) framework provides utilities for easy-developing
 * and fully replaceable with jQuery method. When user using these DOM and
 * selector methods, it provide more light logic and it proves performance
 * of web app. The following table displays the utilities provided by the
 * TAU framework.
 *
 * @class ns.util
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 */
(function (window, document, ns) {
	
				var currentFrame = null,
				/**
				 * requestAnimationFrame function
				 * @method requestAnimationFrame
				 * @static
				 * @member ns.util
				*/
				requestAnimationFrame = (window.requestAnimationFrame ||
					window.webkitRequestAnimationFrame ||
					window.mozRequestAnimationFrame ||
					window.oRequestAnimationFrame ||
					window.msRequestAnimationFrame ||
					function (callback) {
						currentFrame = window.setTimeout(callback.bind(callback, +new Date()), 1000 / 60);
					}).bind(window),
				util = ns.util || {},
				slice = [].slice;

			/**
			 * fetchSync retrieves a text document synchronously, returns null on error
			 * @param {string} url
			 * @param {=string} [mime=""] Mime type of the resource
			 * @return {string|null}
			 * @static
			 * @member ns.util
			 */
			function fetchSync(url, mime) {
				var xhr = new XMLHttpRequest(),
					status;
				xhr.open("get", url, false);
				if (mime) {
					xhr.overrideMimeType(mime);
				}
				xhr.send();
				if (xhr.readyState === 4) {
					status = xhr.status;
					if (status === 200 || (status === 0 && xhr.responseText)) {
						return xhr.responseText;
					}
				}

				return null;
			}
			util.fetchSync = fetchSync;

			/**
			 * Removes all script tags with src attribute from document and returns them
			 * @param {HTMLElement} container
			 * @return {Array.<HTMLElement>}
			 * @private
			 * @static
			 * @member ns.util
			 */
			function removeExternalScripts(container) {
				var scripts = slice.call(container.querySelectorAll("script[src]")),
					i = scripts.length,
					script;

				while (--i >= 0) {
					script = scripts[i];
					script.parentNode.removeChild(script);
				}

				return scripts;
			}

			/**
			 * Evaluates code, reason for a function is for an atomic call to evaluate code
			 * since most browsers fail to optimize functions with try-catch blocks, so this
			 * minimizes the effect, returns the function to run
			 * @param {string} code
			 * @return {Function}
			 * @static
			 * @member ns.util
			 */
			function safeEvalWrap(code) {
				return function () {
					try {
						window.eval(code);
					} catch (e) {
						if (typeof console !== "undefined") {
							if (e.stack) {
								console.error(e.stack);
							} else if (e.name && e.message) {
								console.error(e.name, e.message);
							} else {
								console.error(e);
							}
						}
					}
				};
			}
			util.safeEvalWrap = safeEvalWrap;

			/**
			 * Calls functions in supplied queue (array)
			 * @param {Array.<Function>} functionQueue
			 * @static
			 * @member ns.util
			 */
			function batchCall(functionQueue) {
				var i,
					length = functionQueue.length;
				for (i = 0; i < length; ++i) {
					functionQueue[i].call(window);
				}
			}
			util.batchCall = batchCall;

			/**
			 * Creates new script elements for scripts gathered from a differnt document
			 * instance, blocks asynchronous evaluation (by renaming src attribute) and
			 * returns an array of functions to run to evalate those scripts
			 * @param {Array.<HTMLElement>} scripts
			 * @param {HTMLElement} container
			 * @return {Array.<Function>}
			 * @private
			 * @static
			 * @member ns.util
			 */
			function createScriptsSync(scripts, container) {
				var scriptElement,
					scriptBody,
					i,
					length,
					queue = [];

				// proper order of execution
				for (i = 0, length = scripts.length; i < length; ++i) {
					scriptBody = fetchSync(scripts[i].src, "text/plain");
					if (scriptBody) {
						scriptElement = document.adoptNode(scripts[i]);
						scriptElement.setAttribute("data-src", scripts[i].src);
						scriptElement.removeAttribute("src"); // block evaluation
						queue.push(safeEvalWrap(scriptBody));
						if (container) {
							container.appendChild(scriptElement);
						}
					}
				}

				return queue;
			}

			util.requestAnimationFrame = requestAnimationFrame;

			/**
			* cancelAnimationFrame function
			* @method cancelAnimationFrame
			* @return {Function}
			* @member ns.util
			* @static
			*/
			util.cancelAnimationFrame = (window.cancelAnimationFrame ||
					window.webkitCancelAnimationFrame ||
					window.mozCancelAnimationFrame ||
					window.oCancelAnimationFrame ||
					window.msCancelAnimationFrame ||
					function () {
						// propably wont work if there is any more than 1
						// active animationFrame but we are trying anyway
					window.clearTimeout(currentFrame);
				}).bind(window);

			/**
			 * Method make asynchronous call of function
			 * @method async
			 * @inheritdoc #requestAnimationFrame
			 * @member ns.util
			 * @static
			 */
			util.async = requestAnimationFrame;

			/**
			 * Appends element from different document instance to current document in the
			 * container element and evaluates scripts (synchronously)
			 * @param {HTMLElement} element
			 * @param {HTMLElement} container
			 * @method importEvaluateAndAppendElement
			 * @member ns.util
			 * @static
			 */
			util.importEvaluateAndAppendElement = function (element, container) {
				var externalScriptsQueue = createScriptsSync(removeExternalScripts(element), element),
					newNode = document.importNode(element, true);

				container.appendChild(newNode); // append and eval inline
				batchCall(externalScriptsQueue);

				return newNode;
			};

			/**
			* Checks if specified string is a number or not
			* @method isNumber
			* @return {boolean}
			* @member ns.util
			* @static
			*/
			util.isNumber = function (query) {
				var parsed = parseFloat(query);
				return !isNaN(parsed) && isFinite(parsed);
			};

			/**
			 * Reappend script tags to DOM structure to correct run script
			 * @method runScript
			 * @param {string} baseUrl
			 * @param {HTMLScriptElement} script
			 * @member ns.util
			 * @deprecated 2.3
			 */
			util.runScript = function (baseUrl, script) {
				var newScript = document.createElement("script"),
					scriptData = null,
					i,
					scriptAttributes = slice.call(script.attributes),
					src = script.getAttribute("src"),
					path = util.path,
					request,
					attribute,
					status;

				// 'src' may become null when none src attribute is set
				if (src !== null) {
					src = path.makeUrlAbsolute(src, baseUrl);
				}

				//Copy script tag attributes
				i = scriptAttributes.length;
				while (--i >= 0) {
					attribute = scriptAttributes[i];
					if (attribute.name !== "src") {
						newScript.setAttribute(attribute.name, attribute.value);
					} else {
						newScript.setAttribute("data-src", attribute.value);
					}
				}

				if (src) {
					scriptData = fetchSync(src, "text/plain");
									} else {
					scriptData = script.textContent;
				}

				if (scriptData) {
					// add the returned content to a newly created script tag
					newScript.src = URL.createObjectURL(new Blob([scriptData], {type: "text/javascript"}));
					newScript.textContent = scriptData; // for compatibility with some libs ex. templating systems
				}
				script.parentNode.replaceChild(newScript, script);
			};

			ns.util = util;
			}(window, window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Array Utility
 * Utility helps work with arrays.
 * @class ns.util.array
 */
(function (window, document, ns) {
	
				/**
			 * Create an array containing the range of integers or characters
			 * from low to high (inclusive)
			 * @method range
			 * @param {number|string} low
			 * @param {number|string} high
			 * @param {number} step
			 * @static
			 * @return {Array} array containing continous elements
			 * @member ns.util.array
			 */
			function range(low, high, step) {
				// Create an array containing the range of integers or characters
				// from low to high (inclusive)
				//
				// version: 1107.2516
				// discuss at: http://phpjs.org/functions/range
				// +   original by: Waldo Malqui Silva
				// *	example 1: range ( 0, 12 );
				// *	returns 1: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
				// *	example 2: range( 0, 100, 10 );
				// *	returns 2: [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
				// *	example 3: range( 'a', 'i' );
				// *	returns 3: ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i']
				// *	example 4: range( 'c', 'a' );
				// *	returns 4: ['c', 'b', 'a']
				var matrix = [],
					inival,
					endval,
					plus,
					walker = step || 1,
					chars = false;

				if (!isNaN(low) && !isNaN(high)) {
					inival = low;
					endval = high;
				} else if (isNaN(low) && isNaN(high)) {
					chars = true;
					inival = low.charCodeAt(0);
					endval = high.charCodeAt(0);
				} else {
					inival = (isNaN(low) ? 0 : low);
					endval = (isNaN(high) ? 0 : high);
				}

				plus = inival <= endval;
				if (plus) {
					while (inival <= endval) {
						matrix.push((chars ? String.fromCharCode(inival) : inival));
						inival += walker;
					}
				} else {
					while (inival >= endval) {
						matrix.push((chars ? String.fromCharCode(inival) : inival));
						inival -= walker;
					}
				}

				return matrix;
			}

			/**
			 * Check object is arraylike (arraylike include array and
			 * collection)
			 * @method isArrayLike
			 * @param {Object} object
			 * @return {boolean} Whether arraylike object or not
			 * @member ns.util.array
			 * @static
			 */
			function isArrayLike(object) {
				var type = typeof object,
					length = object && object.length;

				// if object exists and is different from window
				// window object has length property
				if (object && object !== object.window) {
					// If length value is not number, object is not array and collection.
					// Collection type is not array but has length value.
					// e.g) Array.isArray(document.childNodes) ==> false
					return Array.isArray(object) || object instanceof NodeList || type === "function" &&
						(length === 0 || typeof length === "number" && length > 0 && (length - 1) in object);
				}
				return false;
			}

			/**
			 * Faster version of standard forEach method in array
	 		 * Confirmed that this method is 20 times faster then native
			 * @method forEach
			 * @param {Array} array
			 * @param {Function} callback
			 * @member ns.util.array
			 * @static
			 */
			function forEach(array, callback) {
				var i,
					length;
				if (!(array instanceof Array)) {
					array = [].slice.call(array);
				}
				length = array.length;
				for (i = 0; i < length; i++) {
					callback(array[i], i, array);
				}
			}


			/**
			 * Faster version of standard filter method in array
			 * @method filter
			 * @param {Array} array
			 * @param {Function} callback
			 * @member ns.util.array
			 * @static
			 */
			function filter(array, callback) {
				var result = [],
					i,
					length,
					value;
				if (!(array instanceof Array)) {
					array = [].slice.call(array);
				}
				length = array.length;
				for (i = 0; i < length; i++) {
					value = array[i];
					if (callback(value, i, array)) {
						result.push(value);
					}
				}
				return result;
			}

			/**
			 * Faster version of standard map method in array
			 * Confirmed that this method is 60% faster then native
			 * @method map
			 * @param {Array} array
			 * @param {Function} callback
			 * @member ns.util.array
			 * @static
			 */
			function map(array, callback) {
				var result = [],
					i,
					length;
				if (!(array instanceof Array)) {
					array = [].slice.call(array);
				}
				length = array.length;
				for (i = 0; i < length; i++) {
					result.push(callback(array[i], i, array));
				}
				return result;
			}

			/**
			 * Faster version of standard reduce method in array
			 * Confirmed that this method is 60% faster then native
			 * @method reduce
			 * @param {Array} array
			 * @param {Function} callback
			 * @param {*} [initialValue]
			 * @member ns.util.array
			 * @return {*}
			 * @static
			 */
			function reduce(array, callback, initialValue) {
				var i,
					length,
					value,
					result = initialValue;
				if (!(array instanceof Array)) {
					array = [].slice.call(array);
				}
				length = array.length;
				for (i = 0; i < length; i++) {
					value = array[i];
					if (result === undefined && i === 0) {
						result = value;
					} else {
						result = callback(result, value, i, array);
					}
				}
				return result;
			}

			ns.util.array = {
				range: range,
				isArrayLike: isArrayLike,
				forEach: forEach,
				filter: filter,
				map: map,
				reduce: reduce
			};

			}(window, window.document, ns));

/*global window, ns, define, CustomEvent */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Events
 *
 * The Tizen Advanced UI (TAU) framework provides events optimized for the Tizen
 * Web application. The following table displays the events provided by the TAU
 * framework.
 * @class ns.event
 */
(function (window, ns) {
	
	
			/**
			* Checks if specified variable is a array or not
			* @method isArray
			* @return {boolean}
			* @member ns.event
			* @private
			* @static
			*/
		var isArray = Array.isArray,
			isArrayLike = ns.util.array.isArrayLike,
			/**
			 * @property {RegExp} SPLIT_BY_SPACES_REGEXP
			 */
			SPLIT_BY_SPACES_REGEXP = /\s+/g,

			/**
			 * Returns trimmed value
			 * @method trim
			 * @param {string} value
			 * @return {string} trimmed string
			 * @static
			 * @private
			 * @member ns.event
			 */
			trim = function (value) {
				return value.trim();
			},

			/**
			 * Split string to array
			 * @method getEventsListeners
			 * @param {string|Array|Object} names string with one name of event, many names of events divided by spaces, array with names of widgets or object in which keys are names of events and values are callbacks
			 * @param {Function} globalListener
			 * @return {Array}
			 * @static
			 * @private
			 * @member ns.event
			 */
			getEventsListeners = function (names, globalListener) {
				var name,
					result = [],
					i;

				if (typeof names === 'string') {
					names = names.split(SPLIT_BY_SPACES_REGEXP).map(trim);
				}

				if (isArray(names)) {
					for (i=0; i<names.length; i++) {
						result.push({type: names[i], callback: globalListener});
					}
				} else {
					for (name in names) {
						if (names.hasOwnProperty(name)) {
							result.push({type: name, callback: names[name]});
						}
					}
				}
				return result;
			};

			ns.event = {

				/**
				* Triggers custom event fastOn element
				* The return value is false, if at least one of the event
				* handlers which handled this event, called preventDefault.
				* Otherwise it returns true.
				* @method trigger
				* @param {HTMLElement} element
				* @param {string} type
				* @param {?*} [data=null]
				* @param {boolean=} [bubbles=true]
				* @param {boolean=} [cancelable=true]
				* @return {boolean=}
				* @member ns.event
				* @static
				*/
				trigger: function (element, type, data, bubbles, cancelable) {
					var evt = new CustomEvent(type, {
							"detail": data,
							//allow event to bubble up, required if we want to allow to listen fastOn document etc
							bubbles: typeof bubbles === "boolean" ? bubbles : true,
							cancelable: typeof cancelable === "boolean" ? cancelable : true
						});
										return element.dispatchEvent(evt);
				},

				/**
				 * Prevent default on original event
				 * @method preventDefault
				 * @param {CustomEvent} event
				 * @member ns.event
				 * @static
				 */
				preventDefault: function (event) {
					var originalEvent = event._originalEvent;
					// @todo this.isPropagationStopped = returnTrue;
					if (originalEvent && originalEvent.preventDefault) {
						originalEvent.preventDefault();
					}
					event.preventDefault();
				},

				/**
				* Stop event propagation
				* @method stopPropagation
				* @param {CustomEvent} event
				* @member ns.event
				* @static
				*/
				stopPropagation: function (event) {
					var originalEvent = event._originalEvent;
					// @todo this.isPropagationStopped = returnTrue;
					if (originalEvent && originalEvent.stopPropagation) {
						originalEvent.stopPropagation();
					}
					event.stopPropagation();
				},

				/**
				* Stop event propagation immediately
				* @method stopImmediatePropagation
				* @param {CustomEvent} event
				* @member ns.event
				* @static
				*/
				stopImmediatePropagation: function (event) {
					var originalEvent = event._originalEvent;
					// @todo this.isPropagationStopped = returnTrue;
					if (originalEvent && originalEvent.stopImmediatePropagation) {
						originalEvent.stopImmediatePropagation();
					}
					event.stopImmediatePropagation();
				},

				/**
				 * Return document relative cords for event
				 * @method documentRelativeCoordsFromEvent
				 * @param {Event} event
				 * @return {Object}
				 * @return {number} return.x
				 * @return {number} return.y
				 * @member ns.event
				 * @static
				 */
				documentRelativeCoordsFromEvent: function(event) {
					var _event = event ? event : window.event,
							client = {
								x: _event.clientX,
								y: _event.clientY
							},
							page = {
								x: _event.pageX,
								y: _event.pageY
							},
							posX = 0,
							posY = 0,
							touch0,
							body = document.body,
							documentElement = document.documentElement;

						if (event.type.match(/^touch/)) {
							touch0 = _event.targetTouches[0] || _event.originalEvent.targetTouches[0];
							page = {
								x: touch0.pageX,
								y: touch0.pageY
							};
							client = {
								x: touch0.clientX,
								y: touch0.clientY
							};
						}

						if (page.x || page.y) {
							posX = page.x;
							posY = page.y;
						}
						else if (client.x || client.y) {
							posX = client.x + body.scrollLeft + documentElement.scrollLeft;
							posY = client.y + body.scrollTop  + documentElement.scrollTop;
						}

						return { x: posX, y: posY };
				},

				/**
				 * Return target relative cords for event
				 * @method targetRelativeCoordsFromEvent
				 * @param {Event} event
				 * @return {Object}
				 * @return {number} return.x
				 * @return {number} return.y
				 * @member ns.event
				 * @static
				 */
				targetRelativeCoordsFromEvent: function(event) {
					var target = event.target,
						cords = {
							x: event.offsetX,
							y: event.offsetY
						};

					if (cords.x === undefined || isNaN(cords.x) ||
						cords.y === undefined || isNaN(cords.y)) {
						cords = ns.event.documentRelativeCoordsFromEvent(event);
						cords.x -= target.offsetLeft;
						cords.y -= target.offsetTop;
					}

					return cords;
				},

				/**
				 * Add event listener to element
				 * @method fastOn
				 * @param {HTMLElement} element
				 * @param {string} type
				 * @param {Function} listener
				 * @param {boolean} [useCapture=false]
				 * @member ns.event
				 * @static
				 */
				fastOn: function(element, type, listener, useCapture) {
					element.addEventListener(type, listener, useCapture || false);
				},

				/**
				 * Remove event listener to element
				 * @method fastOff
				 * @param {HTMLElement} element
				 * @param {string} type
				 * @param {Function} listener
				 * @param {boolean} [useCapture=false]
				 * @member ns.event
				 * @static
				 */
				fastOff: function(element, type, listener, useCapture) {
					element.removeEventListener(type, listener, useCapture || false);
				},

				/**
				 * Add event listener to element with prefixes for all browsers
				 * @method fastPrefixedOn
				 * @param {HTMLElement} element
				 * @param {string} type
				 * @param {Function} listener
				 * @param {boolean} [useCapture=false]
				 * @member ns.event
				 * @static
				 */
				prefixedFastOn: function(element, type, listener, useCapture) {
					var nameForPrefix = type.charAt(0).toLocaleUpperCase() + type.substring(1);

					element.addEventListener(type.toLowerCase(), listener, useCapture || false);
					element.addEventListener("webkit" + nameForPrefix, listener, useCapture || false);
					element.addEventListener("moz" + nameForPrefix, listener, useCapture || false);
					element.addEventListener("ms" + nameForPrefix, listener, useCapture || false);
					element.addEventListener("o" + nameForPrefix.toLowerCase(), listener, useCapture || false);
				},

				/**
				 * Remove event listener to element with prefixes for all browsers
				 * @method fastPrefixedOff
				 * @param {HTMLElement} element
				 * @param {string} type
				 * @param {Function} listener
				 * @param {boolean} [useCapture=false]
				 * @member ns.event
				 * @static
				 */
				prefixedFastOff: function(element, type, listener, useCapture) {
					var nameForPrefix = type.charAt(0).toLocaleUpperCase() + type.substring(1);

					element.removeEventListener(type.toLowerCase(), listener, useCapture || false);
					element.removeEventListener("webkit" + nameForPrefix, listener, useCapture || false);
					element.removeEventListener("moz" + nameForPrefix, listener, useCapture || false);
					element.removeEventListener("ms" + nameForPrefix, listener, useCapture || false);
					element.removeEventListener("o" + nameForPrefix.toLowerCase(), listener, useCapture || false);
				},

				/**
				 * Add event listener to element that can be added addEventListner
				 * @method on
				 * @param {HTMLElement|HTMLDocument|Window} element
				 * @param {string|Array|Object} type
				 * @param {Function} listener
				 * @param {boolean} [useCapture=false]
				 * @member ns.event
				 * @static
				 */
				on: function(element, type, listener, useCapture) {
					var i,
						j,
						elementsLength,
						typesLength,
						elements,
						listeners;

					if (isArrayLike(element)) {
						elements = element;
					} else {
						elements = [element];
					}
					elementsLength = elements.length;
					listeners = getEventsListeners(type, listener);
					typesLength = listeners.length;
					for (i = 0; i < elementsLength; i++) {
						if (typeof elements[i].addEventListener === "function") {
							for (j = 0; j < typesLength; j++) {
								ns.event.fastOn(elements[i], listeners[j].type, listeners[j].callback, useCapture);
							}
						}
					}
				},

				/**
				 * Remove event listener to element
				 * @method off
				 * @param {HTMLElement|HTMLDocument|Window} element
				 * @param {string|Array|Object} type
				 * @param {Function} listener
				 * @param {boolean} [useCapture=false]
				 * @member ns.event
				 * @static
				 */
				off: function(element, type, listener, useCapture) {
					var i,
						j,
						elementsLength,
						typesLength,
						elements,
						listeners;
					if (isArrayLike(element)) {
						elements = element;
					} else {
						elements = [element];
					}
					elementsLength = elements.length;
					listeners = getEventsListeners(type, listener);
					typesLength = listeners.length;
					for (i = 0; i < elementsLength; i++) {
						if (typeof elements[i].addEventListener === "function") {
							for (j = 0; j < typesLength; j++) {
								ns.event.fastOff(elements[i], listeners[j].type, listeners[j].callback, useCapture);
							}
						}
					}
				},

				/**
				 * Add event listener to element only for one trigger
				 * @method one
				 * @param {HTMLElement|HTMLDocument|window} element
				 * @param {string|Array|Object} type
				 * @param {Function} listener
				 * @param {boolean} [useCapture=false]
				 * @member ns.event
				 * @static
				 */
				one: function(element, type, listener, useCapture) {
					var arraySlice = [].slice,
						i,
						j,
						elementsLength,
						typesLength,
						elements,
						types,
						listeners,
						callbacks = [];
					if (isArrayLike(element)) {
						elements = arraySlice.call(element);
					} else {
						elements = [element];
					}
					elementsLength = elements.length;
					listeners = getEventsListeners(type, listener);
					typesLength = listeners.length;
					for (i = 0; i < elementsLength; i++) {
						if (typeof elements[i].addEventListener === "function") {
							callbacks[i] = [];
							for (j = 0; j < typesLength; j++) {
								callbacks[i][j] = (function(i, j) {
									var args = arraySlice.call(arguments);
									ns.event.fastOff(elements[i], listeners[j].type, callbacks[i][j], useCapture);
									args.shift(); // remove the first argument of binding function
									args.shift(); // remove the second argument of binding function
									listeners[j].callback.apply(this, args);
								}).bind(null, i, j);
								ns.event.fastOn(elements[i], listeners[j].type, callbacks[i][j], useCapture);
							}
						}
					}
				}

			};

			}(window, ns));

/*global window, ns, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Info
 *
 * Various TAU information
 * @class ns.info
 */
(function (window, document, ns) {
	
				/**
			 * @property {Object} info
			 * @property {string} [info.profile="default"] Current runtime profile
			 * @property {string} [info.theme="default"] Current runtime theme
			 * @property {string} info.version Current runtime version
			 * @member ns.info
			 * @static
			 */
			var eventUtils = ns.event,
				info = {
					profile: "default",
					theme: "default",
					version: ns.version,

					/**
					 * Refreshes information about runtime
					 * @method refreshTheme
					 * @param {Function} done Callback run when the theme is discovered
					 * @member ns.info
					 * @return {null|String}
					 * @static
					 */
					refreshTheme: function (done) {
						var el = document.createElement("span"),
							parent = document.body,
							themeName = null;

						if (document.readyState !== "interactive" && document.readyState !== "complete") {
							eventUtils.fastOn(document, "DOMContentLoaded", this.refreshTheme.bind(this, done));
							return null;
						}
						el.classList.add("tau-info-theme");

						parent.appendChild(el);
						themeName = window.getComputedStyle(el, ":after").content;
						parent.removeChild(el);

						if (themeName && themeName.length > 0) {
							this.theme = themeName;
						}

						themeName = themeName || null;

						if (done) {
							done(themeName);
						}

						return themeName;
					}
				};

			info.refreshTheme();

			ns.info = info;
			}(window, window.document, ns));

/*global define: true, window: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Selectors Utility
 * Object contains functions to get HTML elements by different selectors.
 * @class ns.util.selectors
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Jadwiga Sosnowska <j.sosnowska@partner.samsung.com>
 * @author Damian Osipiuk <d.osipiuk@samsung.com>
 */
(function (document, ns) {
	
				/**
			 * @method slice Alias for array slice method
			 * @member ns.util.selectors
			 * @private
			 * @static
			 */
			var slice = [].slice,
				/**
				 * @method matchesSelectorType
				 * @return {string|boolean}
				 * @member ns.util.selectors
				 * @private
				 * @static
				 */
				matchesSelectorType = (function () {
					var el = document.createElement("div");

					if (typeof el.webkitMatchesSelector === "function") {
						return "webkitMatchesSelector";
					}

					if (typeof el.mozMatchesSelector === "function") {
						return "mozMatchesSelector";
					}

					if (typeof el.msMatchesSelector === "function") {
						return "msMatchesSelector";
					}

					if (typeof el.matchesSelector === "function") {
						return "matchesSelector";
					}

					if (typeof el.matches === "function") {
						return "matches";
					}

					return false;
				}());

			/**
			 * Prefix selector with 'data-' and namespace if present
			 * @method getDataSelector
			 * @param {string} selector
			 * @return {string}
			 * @member ns.util.selectors
			 * @private
			 * @static
			 */
			function getDataSelector(selector) {
				var namespace = ns.getConfig('namespace');
				return '[data-' + (namespace ? namespace + '-' : '') + selector + ']';
			}

			/**
			 * Runs matches implementation of matchesSelector
			 * method on specified element
			 * @method matchesSelector
			 * @param {HTMLElement} element
			 * @param {string} selector
			 * @return {boolean}
			 * @static
			 * @member ns.util.selectors
			 */
			function matchesSelector(element, selector) {
				if (matchesSelectorType && element[matchesSelectorType]) {
					return element[matchesSelectorType](selector);
				}
				return false;
			}

			/**
			 * Return array with all parents of element.
			 * @method parents
			 * @param {HTMLElement} element
			 * @return {Array}
			 * @member ns.util.selectors
			 * @private
			 * @static
			 */
			function parents(element) {
				var items = [],
					current = element.parentNode;
				while (current && current !== document) {
					items.push(current);
					current = current.parentNode;
				}
				return items;
			}

			/**
			 * Checks if given element and its ancestors matches given function
			 * @method closest
			 * @param {HTMLElement} element
			 * @param {Function} testFunction
			 * @return {?HTMLElement}
			 * @member ns.util.selectors
			 * @static
			 * @private
			 */
			function closest(element, testFunction) {
				var current = element;
				while (current && current !== document) {
					if (testFunction(current)) {
						return current;
					}
					current = current.parentNode;
				}
				return null;
			}

			/**
			 * @method testSelector
			 * @param {string} selector
			 * @param {HTMLElement} node
			 * @return {boolean}
			 * @member ns.util.selectors
			 * @static
			 * @private
			 */
			function testSelector(selector, node) {
				return matchesSelector(node, selector);
			}

			/**
			 * @method testClass
			 * @param {string} className
			 * @param {HTMLElement} node
			 * @return {boolean}
			 * @member ns.util.selectors
			 * @static
			 * @private
			 */
			function testClass(className, node) {
				return node && node.classList && node.classList.contains(className);
			}

			/**
			 * @method testTag
			 * @param {string} tagName
			 * @param {HTMLElement} node
			 * @return {boolean}
			 * @member ns.util.selectors
			 * @static
			 * @private
			 */
			function testTag(tagName, node) {
				return node.tagName.toLowerCase() === tagName;
			}

			/**
			 * @class ns.util.selectors
			 */
			ns.util.selectors = {
				matchesSelector: matchesSelector,

				/**
				* Return array with children pass by given selector.
				* @method getChildrenBySelector
				* @param {HTMLElement} context
				* @param {string} selector
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getChildrenBySelector: function (context, selector) {
					return slice.call(context.children).filter(testSelector.bind(null, selector));
				},

				/**
				* Return array with children pass by given data-namespace-selector.
				* @method getChildrenByDataNS
				* @param {HTMLElement} context
				* @param {string} dataSelector
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getChildrenByDataNS: function (context, dataSelector) {
					return slice.call(context.children).filter(testSelector.bind(null, getDataSelector(dataSelector)));
				},

				/**
				* Return array with children with given class name.
				* @method getChildrenByClass
				* @param {HTMLElement} context
				* @param {string} className
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getChildrenByClass: function (context, className) {
					return slice.call(context.children).filter(testClass.bind(null, className));
				},

				/**
				* Return array with children with given tag name.
				* @method getChildrenByTag
				* @param {HTMLElement} context
				* @param {string} tagName
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getChildrenByTag: function (context, tagName) {
					return slice.call(context.children).filter(testTag.bind(null, tagName));
				},

				/**
				* Return array with all parents of element.
				* @method getParents
				* @param {HTMLElement} context
				* @param {string} selector
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getParents: parents,

				/**
				* Return array with all parents of element pass by given selector.
				* @method getParentsBySelector
				* @param {HTMLElement} context
				* @param {string} selector
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getParentsBySelector: function (context, selector) {
					return parents(context).filter(testSelector.bind(null, selector));
				},

				/**
				* Return array with all parents of element pass by given selector with namespace.
				* @method getParentsBySelectorNS
				* @param {HTMLElement} context
				* @param {string} selector
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getParentsBySelectorNS: function (context, selector) {
					return parents(context).filter(testSelector.bind(null, getDataSelector(selector)));
				},

				/**
				* Return array with all parents of element with given class name.
				* @method getParentsByClass
				* @param {HTMLElement} context
				* @param {string} className
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getParentsByClass: function (context, className) {
					return parents(context).filter(testClass.bind(null, className));
				},

				/**
				* Return array with all parents of element with given tag name.
				* @method getParentsByTag
				* @param {HTMLElement} context
				* @param {string} tagName
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getParentsByTag: function (context, tagName) {
					return parents(context).filter(testTag.bind(null, tagName));
				},

				/**
				* Return first element from parents of element pass by selector.
				* @method getClosestBySelector
				* @param {HTMLElement} context
				* @param {string} selector
				* @return {HTMLElement}
				* @static
				* @member ns.util.selectors
				*/
				getClosestBySelector: function (context, selector) {
					return closest(context, testSelector.bind(null, selector));
				},

				/**
				* Return first element from parents of element pass by selector with namespace.
				* @method getClosestBySelectorNS
				* @param {HTMLElement} context
				* @param {string} selector
				* @return {HTMLElement}
				* @static
				* @member ns.util.selectors
				*/
				getClosestBySelectorNS: function (context, selector) {
					return closest(context, testSelector.bind(null, getDataSelector(selector)));
				},

				/**
				* Return first element from parents of element with given class name.
				* @method getClosestByClass
				* @param {HTMLElement} context
				* @param {string} selector
				* @return {HTMLElement}
				* @static
				* @member ns.util.selectors
				*/
				getClosestByClass: function (context, selector) {
					return closest(context, testClass.bind(null, selector));
				},

				/**
				* Return first element from parents of element with given tag name.
				* @method getClosestByTag
				* @param {HTMLElement} context
				* @param {string} selector
				* @return {HTMLElement}
				* @static
				* @member ns.util.selectors
				*/
				getClosestByTag: function (context, selector) {
					return closest(context, testTag.bind(null, selector));
				},

				/**
				* Return array of elements from context with given data-selector
				* @method getAllByDataNS
				* @param {HTMLElement} context
				* @param {string} dataSelector
				* @return {Array}
				* @static
				* @member ns.util.selectors
				*/
				getAllByDataNS: function (context, dataSelector) {
					return slice.call(context.querySelectorAll(getDataSelector(dataSelector)));
				},

				/**
				 * Get scrollable parent elmenent
				 * @method getScrollableParent
				 * @param {HTMLElement} element
				 * @return {HTMLElement}
				 * @static
				 * @member ns.util.selectors
				 */
				getScrollableParent:  function (element) {
					var overflow,
						style;

					while (element && element != document.body) {
						style = window.getComputedStyle(element);

						if (style) {
							overflow = style.getPropertyValue("overflow-y");
							if (overflow === "scroll" || (overflow === "auto" && element.scrollHeight > element.clientHeight)) {
								return element;
							}
						}
						element = element.parentNode;
					}
					return null;
				}
			};
			}(window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Object Utility
 * Object contains functions help work with objects.
 * @class ns.util.object
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (ns) {
	
	
			var object = {
				/**
				* Copy object to new object
				* @method copy
				* @param {Object} orgObject
				* @return {Object}
				* @static
				* @member ns.util.object
				*/
				copy: function (orgObject) {
					return object.merge({}, orgObject);
				},

				/**
				* Attach fields from second object to first object.
				* @method fastMerge
				* @param {Object} newObject
				* @param {Object} orgObject
				* @return {Object}
				* @static
				* @member ns.util.object
				*/
				fastMerge: function (newObject, orgObject) {
					var key;
					for (key in orgObject) {
						if (orgObject.hasOwnProperty(key)) {
							newObject[key] = orgObject[key];
						}
					}
					return newObject;
				},

				/**
				* Attach fields from second and next object to first object.
				* @method merge
				* @param {Object} newObject
				* @param {...Object} orgObject
				* @param {?boolean} [override=true]
				* @return {Object}
				* @static
				* @member ns.util.object
				*/
				merge: function ( /* newObject, orgObject, override */ ) {
					var newObject, orgObject, override,
						key,
						args = [].slice.call(arguments),
						argsLength = args.length,
						i;
					newObject = args.shift();
					override = true;
					if (typeof arguments[argsLength-1] === "boolean") {
						override = arguments[argsLength-1];
						argsLength--;
					}
					for (i = 0; i < argsLength; i++) {
						orgObject = args.shift();
						if (orgObject !== null) {
							for (key in orgObject) {
								if (orgObject.hasOwnProperty(key) && ( override || newObject[key] === undefined )) {
									newObject[key] = orgObject[key];
								}
							}
						}
					}
					return newObject;
				},

				/**
				 * Function add to Constructor prototype Base object and add to prototype properties and methods from
				 * prototype object.
				 * @method inherit
				 * @param {Function} Constructor
				 * @param {Function} Base
				 * @param {Object} prototype
				 * @static
				 * @member ns.util.object
				 */
				/* jshint -W083 */
				inherit: function( Constructor, Base, prototype ) {
					var basePrototype = new Base(),
						property,
						value;
					for (property in prototype) {
						if (prototype.hasOwnProperty(property)) {
							value = prototype[property];
							if ( typeof value === "function" ) {
								basePrototype[property] = (function createFunctionWithSuper(Base, property, value) {
									var _super = function() {
										var superFunction = Base.prototype[property];
										if (superFunction) {
											return superFunction.apply(this, arguments);
										}
										return null;
									};
									return function() {
										var __super = this._super,
											returnValue;

										this._super = _super;
										returnValue = value.apply(this, arguments);
										this._super = __super;
										return returnValue;
									};
								}(Base, property, value));
							} else {
								basePrototype[property] = value;
							}
						}
					}

					Constructor.prototype = basePrototype;
					Constructor.prototype.constructor = Constructor;
				},

				/**
				 * Returns true if every property value corresponds value from 'value' argument
				 * @method hasPropertiesOfValue
				 * @param {Object} obj
				 * @param {*} [value=undefined]
				 * @return {boolean}
				 */
				hasPropertiesOfValue: function (obj, value) {
					var keys = Object.keys(obj),
						i = keys.length;

					// Empty array should return false
					if (i === 0) {
						return false;
					}

					while (--i >= 0) {
						if (obj[keys[i]] !== value) {
							return false;
						}
					}

					return true;
				},

				/**
				 * Remove properties from object.
				 * @method removeProperties
				 * @param {Object} object
				 * @param {Array} propertiesToRemove
				 * @return {Object}
				 */
				removeProperties: function (object, propertiesToRemove) {
					var length = propertiesToRemove.length,
						property,
						i;

					for (i = 0; i < length; i++) {
						property = propertiesToRemove[i];
						if (object.hasOwnProperty(property)) {
							delete object[property];
						}
					}
					return object;
				}
			};
			ns.util.object = object;
			}(ns));

/*global window, define, ns, Node, HTMLElement */
/*jslint nomen: true, plusplus: true, bitwise: false */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Engine
 * Main class with engine of library which control communication
 * between parts of framework.
 * @class ns.engine
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Michal Szepielak <m.szepielak@samsung.com>
 * @author Jadwiga Sosnowska <j.sosnowska@partner.samsung.com>
 * @author Maciej Moczulski <m.moczulski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 * @author Tomasz Lukawski <t.lukawski@samsung.com>
 * @author Przemyslaw Ciezkowski <p.ciezkowski@samsung.com>
 */
(function (window, document, ns) {
	
				/**
			 * @method slice Array.slice
			 * @private
			 * @static
			 * @member ns.engine
			 */
			var slice = [].slice,
				/**
				 * @property {Object} eventUtils {@link ns.event}
				 * @private
				 * @static
				 * @member ns.engine
				 */
				eventUtils = ns.event,
				objectUtils = ns.util.object,
				selectors = ns.util.selectors,
				/**
				 * @property {Object} widgetDefs Object with widgets definitions
				 * @private
				 * @static
				 * @member ns.engine
				 */
				widgetDefs = {},
				/**
				 * @property {Object} widgetBindingMap Object with widgets bindings
				 * @private
				 * @static
				 * @member ns.engine
				 */
				widgetBindingMap = {},
				location = window.location,
				/**
				 * engine mode, if true then engine only builds widgets
				 * @property {boolean} justBuild
				 * @private
				 * @static
				 * @member ns.engine
				 */
				justBuild = location.hash === "#build",
				/**
				 * @property {string} [TYPE_STRING="string"] local cache of string type name
				 * @private
				 * @static
				 * @readonly
				 * @member ns.engine
				 */
				TYPE_STRING = "string",
				/**
				 * @property {string} [TYPE_FUNCTION="function"] local cache of function type name
				 * @private
				 * @static
				 * @readonly
				 * @member ns.engine
				 */
				TYPE_FUNCTION = "function",
				/**
				 * @property {string} [DATA_BUILT="data-tau-built"] attribute informs that widget id build
				 * @private
				 * @static
				 * @readonly
				 * @member ns.engine
				 */
				DATA_BUILT = "data-tau-built",
				/**
				 * @property {string} [DATA_NAME="data-tau-name"] attribute contains widget name
				 * @private
				 * @static
				 * @readonly
				 * @member ns.engine
				 */
				DATA_NAME = "data-tau-name",
				/**
				 * @property {string} [DATA_BOUND="data-tau-bound"] attribute informs that widget id bound
				 * @private
				 * @static
				 * @readonly
				 * @member ns.engine
				 */
				DATA_BOUND = "data-tau-bound",
				/**
				 * @property {string} NAMES_SEPARATOR
				 * @private
				 * @static
				 * @readonly
				 */
				NAMES_SEPARATOR = ",",
				/**
				 * @property {string} [querySelectorWidgets="*[data-tau-built][data-tau-name]:not([data-tau-bound])"] query selector for all widgets which are built but not bound
				 * @private
				 * @static
				 * @member ns.engine
				 */
					// @TODO this selector is not valid ...
				querySelectorWidgets = "*[" + DATA_BUILT + "][" + DATA_NAME + "]:not([" + DATA_BOUND + "])",
				/**
				 * @method excludeBuildAndBound
				 * @private
				 * @static
				 * @member ns.engine
				 * @return {string} :not([data-tau-built*='widgetName']):not([data-tau-bound*='widgetName'])
				 */
				excludeBuiltAndBound = function (widgetType) {
					return ":not([" + DATA_BUILT + "*='" + widgetType +"']):not([" + DATA_BOUND + "*='" + widgetType +"'])";
				},

				/**
				 * Engine event types
				 * @property {Object} eventType
				 * @property {string} eventType.INIT="tauinit" INIT of framework init event
				 * @property {string} eventType.WIDGET_BOUND="widgetbound" WIDGET_BOUND of widget bound event
				 * @property {string} eventType.WIDGET_DEFINED="widgetdefined" WIDGET_DEFINED of widget built event
				 * @property {string} eventType.WIDGET_BUILT="widgetbuilt" WIDGET_BUILT of widget built event
				 * @property {string} eventType.BOUND="bound" BOUND of bound event
				 * @static
				 * @readonly
				 * @member ns.engine
				 */
				eventType = {
					INIT: "tauinit",
					WIDGET_BOUND: "widgetbound",
					WIDGET_DEFINED: "widgetdefined",
					WIDGET_BUILT: "widgetbuilt",
					BOUND: "bound"
				},
				engine,
				/**
				 * @property {Object} router Router object
				 * @private
				 * @static
				 * @member ns.engine
				 */
				router;

			/**
			 * This function prepares selector for widget' definition
			 * @method selectorChange
			 * @param {string} selectorName
			 * @return {string} new selector
			 * @member ns.engine
			 * @static
			 */
			function selectorChange (selectorName) {
				if (selectorName.match(/\[data-role=/) && !selectorName.match(/:not\(\[data-role=/)) {
					return selectorName.trim();
				}
				return selectorName.trim() + ":not([data-role='none'])";
			}

			/**
			 * Function to define widget
			 * @method defineWidget
			 * @param {string} name
			 * @param {string} selector
			 * @param {Array} methods
			 * @param {Object} widgetClass
			 * @param {string} [namespace]
			 * @param {boolean} [redefine]
			 * @param {boolean} [widgetNameToLowercase = true]
			 * @return {boolean}
			 * @member ns.engine
			 * @static
			 */
			function defineWidget(name, selector, methods, widgetClass, namespace, redefine, widgetNameToLowercase) {
				var definition;
				// Widget name is absolutely required
				if (name) {
					if (!widgetDefs[name] || redefine) {
												methods = methods || [];
						methods.push("destroy", "disable", "enable", "option", "refresh", "value");
						definition = {
							name: name,
							methods: methods,
							selector: selector || "",
							selectors: selector ? selector.split(",").map(selectorChange) : [],
							widgetClass: widgetClass || null,
							namespace: namespace || "",
							widgetNameToLowercase: widgetNameToLowercase === undefined ? true : !!widgetNameToLowercase
						};

						widgetDefs[name] = definition;
						eventUtils.trigger(document, "widgetdefined", definition, false);
						return true;
					}
									} else {
					ns.error("Widget with selector [" + selector + "] defined without a name, aborting!");
				}
				return false;
			}

			/**
			 * Get binding for element
			 * @method getBinding
			 * @static
			 * @param {HTMLElement|string} element
			 * @param {string} [type] widget name
			 * @return {?Object}
			 * @member ns.engine
			 */
			function getBinding(element, type) {
				var id = !element || typeof element === TYPE_STRING ? element : element.id,
					binding,
					widgetInstance,
					bindingElement,
					storedWidgetNames;

				if (typeof element === TYPE_STRING) {
					element = document.getElementById(id);
				}

				// Fetch group of widget defined for this element
				binding = widgetBindingMap[id];

				if (binding && typeof binding === "object") {
					// If name is defined it's possible to fetch it instantly
					if (type) {
						widgetInstance = binding.instances[type];
					} else {
						storedWidgetNames = Object.keys(binding.instances);
						widgetInstance = binding.instances[storedWidgetNames[0]];
					}

					// Return only it instance of the proper widget exists
					if (widgetInstance) {
						
						// Check if widget instance has that same object referenced
						if (widgetInstance.element === element) {
							return widgetInstance;
						}
					}
				}

				return null;
			}

			/**
			 * Set binding of widget
			 * @method setBinding
			 * @param {ns.widget.BaseWidget} widgetInstance
			 * @static
			 * @member ns.engine
			 */
			function setBinding(widgetInstance) {
				var id = widgetInstance.element.id,
					type = widgetInstance.name,
					widgetBinding = widgetBindingMap[id];

				
				// If the HTMLElement never had a widget declared create an empty object
				if(!widgetBinding) {
					widgetBinding = {
						elementId: id,
						element: widgetInstance.element,
						instances: {}
					};
				}

				widgetBinding.instances[type] = widgetInstance;
				widgetBindingMap[id] = widgetBinding;
			}

			/**
			 * Returns all bindings for element or id gives as parameter
			 * @method getAllBindings
			 * @param {HTMLElement|string} element
			 * @return {?Object}
			 * @static
			 * @member ns.engine
			 */
			function getAllBindings(element) {
				var id = !element || typeof element === TYPE_STRING ? element : element.id;

				return (widgetBindingMap[id] && widgetBindingMap[id].instances) || null;
			}

			/**
			 * Removes given name from attributeValue string.
			 * Names should be separated with a NAMES_SEPARATOR
			 * @param {string} name
			 * @param {string} attributeValue
			 * @private
			 * @static
			 * @return {string}
			 */
			function _removeWidgetNameFromAttribute(name, attributeValue) {
				var widgetNames,
					searchResultIndex;

				// Split attribute value by separator
				widgetNames = attributeValue.split(NAMES_SEPARATOR);
				searchResultIndex = widgetNames.indexOf(name);

				if (searchResultIndex > -1) {
					widgetNames.splice(searchResultIndex, 1);
					attributeValue = widgetNames.join(NAMES_SEPARATOR);
				}

				return attributeValue;
			}

			function _removeAllBindingAttributes(element) {
				element.removeAttribute(DATA_BUILT);
				element.removeAttribute(DATA_BOUND);
				element.removeAttribute(DATA_NAME);
			}

			/**
			 * Remove binding data attributes for element.
			 * @method _removeBindingAttributes
			 * @param {HTMLElement} element
			 * @param {string} type widget type (name)
			 * @private
			 * @static
			 * @member ns.engine
			 */
			function _removeWidgetFromAttributes(element, type) {
				var dataBuilt,
					dataBound,
					dataName;

				// Most often case is that name is not defined
				if (!type) {
					_removeAllBindingAttributes(element);
				} else {
					dataBuilt = _removeWidgetNameFromAttribute(type, element.getAttribute(DATA_BUILT) || "");
					dataBound = _removeWidgetNameFromAttribute(type, element.getAttribute(DATA_BOUND) || "");
					dataName = _removeWidgetNameFromAttribute(type, element.getAttribute(DATA_NAME) || "");

					// Check if all attributes have at least one widget
					if (dataBuilt && dataBound && dataName) {
						element.setAttribute(DATA_BUILT, dataBuilt);
						element.setAttribute(DATA_BOUND, dataBound);
						element.setAttribute(DATA_NAME, dataName);
					} else {
						// If something is missing remove everything
						_removeAllBindingAttributes(element);
					}
				}
			}

			/**
			 * Method removes binding for single widget.
			 * @method _removeSingleBinding
			 * @param {Object} bindingGroup
			 * @param {string} type
			 * @return {boolean}
			 * @private
			 * @static
			 */
			function _removeSingleBinding(bindingGroup, type) {
				var widgetInstance = bindingGroup[type];

				if (widgetInstance){
					if (widgetInstance.element && typeof widgetInstance.element.setAttribute === TYPE_FUNCTION) {
						_removeWidgetFromAttributes(widgetInstance.element, type);
					}

					bindingGroup[type] = null;

					return true;
				}

				return false;
			}

			/**
			 * Remove binding for widget based on element.
			 * @method removeBinding
			 * @param {HTMLElement|string} element
			 * @param {string} type widget name
			 * @return {boolean}
			 * @static
			 * @member ns.engine
			 */
			function removeBinding(element, type) {
				var id = (typeof element === TYPE_STRING) ? element : element.id,
					binding = widgetBindingMap[id],
					bindingGroup,
					widgetName,
					partialSuccess,
					fullSuccess = false;

				// [NOTICE] Due to backward compatibility calling removeBinding
				// with one parameter should remove all bindings

				if (binding) {
					if (typeof element === TYPE_STRING) {
						// Search based on current document may return bad results,
						// use previously defined element if it exists
						element = binding.element;
					}

					if (element) {
						_removeWidgetFromAttributes(element, type);
					}

					bindingGroup = widgetBindingMap[id] && widgetBindingMap[id].instances;

					if (bindingGroup) {
						if (!type) {
							fullSuccess = true;

							// Iterate over group of created widgets
							for (widgetName in bindingGroup) {
								if (bindingGroup.hasOwnProperty(widgetName)) {
									partialSuccess = _removeSingleBinding(bindingGroup, widgetName);
									
									// As we iterate over keys we are sure we want to remove this element
									// NOTE: Removing property by delete is slower than assigning null value
									bindingGroup[widgetName] = null;

									fullSuccess = (fullSuccess && partialSuccess);
								}
							}

							// If the object bindingGroup is empty or every key has a null value
							if (objectUtils.hasPropertiesOfValue(bindingGroup, null)) {
								// NOTE: Removing property by delete is slower than assigning null value
								widgetBindingMap[id] = null;
							}

							return fullSuccess;
						}

						partialSuccess = _removeSingleBinding(bindingGroup, type);

						if (objectUtils.hasPropertiesOfValue(bindingGroup, null)) {
							widgetBindingMap[id] = null;
						}

						return partialSuccess;
					}
				}

				return false;
			}

			/**
			 * Removes all bindings of widgets.
			 * @method removeAllBindings
			 * @param {HTMLElement|string} element
			 * @return {boolean}
			 * @static
			 * @member ns.engine
			 */
			function removeAllBindings(element) {
				// @TODO this should be coded in the other way around, removeAll should loop through all bindings and inside call removeBinding
				// but due to backward compatibility that code should be more readable
				return removeBinding(element);
			}

			/**
			 * If element not exist create base element for widget.
			 * @method ensureElement
			 * @param {HTMLElement} element
			 * @param {ns.widget.BaseWidget} Widget
			 * @return {HTMLElement}
			 * @static
			 * @private
			 * @member ns.engine
			 */
			function ensureElement(element, Widget) {
				if (!element || !element instanceof HTMLElement) {
					if (typeof Widget.createEmptyElement === TYPE_FUNCTION) {
						element = Widget.createEmptyElement();
					} else {
						element = document.createElement("div");
					}
				}
				return element;
			}

			/**
			 * Load widget
			 * @method processWidget
			 * @param {HTMLElement} element base element of widget
			 * @param {Object} definition definition of widget
			 * @param {ns.widget.BaseWidget} definition.widgetClass
			 * @param {string} definition.name
			 * @param {Object} [options] options for widget
			 * @private
			 * @static
			 * @member ns.engine
			 */
			function processWidget(element, definition, options) {
				var widgetOptions = options || {},
					createFunction = widgetOptions.create,
					Widget = definition.widgetClass,
					/**
					 * @type {ns.widget.BaseWidget} widgetInstance
					 */
					widgetInstance,
					buildAttribute,
					parentEnhance,
					existingBinding;

				element = ensureElement(element, Widget);
				widgetInstance = Widget ? new Widget(element) : false;
				// if any parent has attribute data-enhance=false then stop building widgets
				parentEnhance = selectors.getParentsBySelectorNS(element, 'enhance=false');

				// While processing widgets queue other widget may built this one before
				// it reaches it's turn
				existingBinding = getBinding(element, definition.name);
				if (existingBinding && existingBinding.element === element) {
					return existingBinding.element;
				}

				if (widgetInstance && !parentEnhance.length) {
										widgetInstance.configure(definition, element, options);

					// Run .create method from widget options when a [widgetName]create event is triggered
					if (typeof createFunction === TYPE_FUNCTION) {
						eventUtils.one(element, definition.name.toLowerCase() + "create", createFunction);
					}

					if (element.id) {
						widgetInstance.id = element.id;
					}

					// Check if this type of widget was build for this element before
					buildAttribute = element.getAttribute(DATA_BUILT);
					if (!buildAttribute || (buildAttribute && buildAttribute.split(NAMES_SEPARATOR).indexOf(widgetInstance.name) === -1)) {
						element = widgetInstance.build(element);
					}

					if (element) {
						widgetInstance.element = element;

						setBinding(widgetInstance);

						widgetInstance.trigger(eventType.WIDGET_BUILT, widgetInstance, false);

						if (!justBuild) {
							widgetInstance.init(element);
						}

						widgetInstance.bindEvents(element, justBuild);

						eventUtils.trigger(element, eventType.WIDGET_BOUND, widgetInstance, false);
						eventUtils.trigger(document, eventType.WIDGET_BOUND, widgetInstance);
					} else {
											}
				}
				return widgetInstance.element;
			}

			/**
			 * Destroys widget of given 'type' for given HTMLElement.
			 * [NOTICE] This method won't destroy any children widgets.
			 * @method destroyWidget
			 * @param {HTMLElement|string} element
			 * @param {string} type
			 * @static
			 * @member ns.engine
			 */
			function destroyWidget(element, type) {
				var widgetInstance;

				if (typeof element === TYPE_STRING) {
					element = document.getElementById(element);
				}

				
				// If type is not defined all widgets should be removed
				// this is for backward compatibility
				widgetInstance = getBinding(element, type);

				if (widgetInstance) {
					//Destroy widget
					widgetInstance.destroy();
					widgetInstance.trigger("widgetdestroyed");

					removeBinding(element, type);
				}
			}

			/**
			 * Calls destroy on widget (or widgets) connected with given HTMLElement
			 * Removes child widgets as well.
			 * @method destroyAllWidgets
			 * @param {HTMLElement|string} element
			 * @param {boolean} [childOnly=false] destroy only widgets on children elements
			 * @static
			 * @member ns.engine
			 */
			function destroyAllWidgets(element, childOnly) {
				var widgetName,
					widgetInstance,
					widgetGroup,
					childWidgets,
					i;

				if (typeof element === TYPE_STRING) {
					element = document.getElementById(element);
				}

				
				if (!childOnly) {
					// If type is not defined all widgets should be removed
					// this is for backward compatibility
					widgetGroup = getAllBindings(element);
					for (widgetName in widgetGroup) {
						if (widgetGroup.hasOwnProperty(widgetName)) {
							widgetInstance = widgetGroup[widgetName];

							//Destroy widget
							if (widgetInstance) {
								widgetInstance.destroy();
								widgetInstance.trigger("widgetdestroyed");
							}
						}
					}
				}

				//Destroy child widgets, if something left.
				childWidgets = slice.call(element.querySelectorAll("[" + DATA_BOUND + "]"));
				for (i = childWidgets.length - 1; i >= 0; i -= 1) {
					if (childWidgets[i]) {
						destroyAllWidgets(childWidgets[i], false);
					}
				}

				removeAllBindings(element);
			}

			/**
			 * Load widgets from data-* definition
			 * @method processHollowWidget
			 * @param {HTMLElement} element base element of widget
			 * @param {Object} definition widget definition
			 * @param {Object} [options] options for create widget
			 * @return {HTMLElement} base element of widget
			 * @private
			 * @static
			 * @member ns.engine
			 */
			function processHollowWidget(element, definition, options) {
				var name = (element && element.getAttribute(DATA_NAME)) ||
						(definition && definition.name);
								definition = definition || (name && widgetDefs[name]) || {
					"name": name
				};
				return processWidget(element, definition, options);
			}

			/**
			 * Compare function for nodes on build queue
			 * @param {Object} nodeA
			 * @param {Object} nodeB
			 * @return {number}
			 * @private
			 * @static
			 */
			function compareByDepth(nodeA, nodeB) {
				var mask = Node.DOCUMENT_POSITION_CONTAINS | Node.DOCUMENT_POSITION_PRECEDING;

				if (nodeA.element === nodeB.element) {
					return 0;
				}

				if (nodeA.element.compareDocumentPosition(nodeB.element) & mask) {
					return 1;
				}

				return -1;
			}

			/**
			 * Processes one build queue item. Runs processHollowWidget
			 * underneath
			 * @method processBuildQueueItem
			 * @param {Object|HTMLElement} queueItem
			 * @private
			 * @static
			 */
			function processBuildQueueItem(queueItem) {
				// HTMLElement doesn't have .element property
				// widgetDefs will return undefined when called widgetDefs[undefined]
				processHollowWidget(queueItem.element || queueItem, widgetDefs[queueItem.widgetName]);
			}

			/**
			 * Build widgets on all children of context element
			 * @method createWidgets
			 * @static
			 * @param {HTMLElement} context base html for create children
			 * @member ns.engine
			 */
			function createWidgets(context) {
				var builtWithoutTemplates = slice.call(context.querySelectorAll(querySelectorWidgets)),
					normal = [],
					buildQueue = [],
					selectorKeys = Object.keys(widgetDefs),
					excludeSelector,
					i,
					j,
					len = selectorKeys.length,
					definition,
					widgetName,
					definitionSelectors;

				
				
				// @TODO EXPERIMENTAL WIDGETS WITHOUT TEMPLATE DEFINITION
				builtWithoutTemplates.forEach(processBuildQueueItem);

				/* NORMAL */
				for (i = 0; i < len; ++i) {
					widgetName = selectorKeys[i];
					definition = widgetDefs[widgetName];
					definitionSelectors = definition.selectors;
					if (definitionSelectors.length) {
						excludeSelector = excludeBuiltAndBound(widgetName);

						normal = slice.call(context.querySelectorAll(definitionSelectors.join(excludeSelector + ",") + excludeSelector));
						j = normal.length;

						while (--j >= 0) {
							buildQueue.push({
								element: normal[j],
								widgetName: widgetName
							});
						}
					}
				}

				// Sort queue by depth, on every DOM branch outer most element go first
				buildQueue.sort(compareByDepth);

				// Build all widgets from queue
				buildQueue.forEach(processBuildQueueItem);

				
				eventUtils.trigger(document, "built");
				eventUtils.trigger(document, eventType.BOUND);
							}

			/**
			 * Handler for event create
			 * @method createEventHandler
			 * @param {Event} event
			 * @static
			 * @member ns.engine
			 */
			function createEventHandler(event) {
				createWidgets(event.target);
			}

			function setViewport() {
				/**
				 * Sets viewport tag if not exists
				 */
				var documentHead = document.head,
					metaTagListLength,
					metaTagList,
					metaTag,
					i;

				metaTagList = documentHead.querySelectorAll('[name="viewport"]');
				metaTagListLength = metaTagList.length;

				if (metaTagListLength > 0) {
					// Leave the last viewport tag
					--metaTagListLength;

					// Remove duplicated tags
					for (i = 0; i < metaTagListLength; ++i) {
						// Remove meta tag from DOM
						documentHead.removeChild(metaTagList[i]);
					}
				} else {
					// Create new HTML Element
					metaTag = document.createElement('meta');

					// Set required attributes
					metaTag.setAttribute('name', 'viewport');
					metaTag.setAttribute('content', 'width=device-width, user-scalable=no');

					// Force that viewport tag will be first child of head
					if (documentHead.firstChild) {
						documentHead.insertBefore(metaTag, documentHead.firstChild);
					} else {
						documentHead.appendChild(metaTag);
					}
				}
			}

			/**
			 * Build first page
			 * @method build
			 * @static
			 * @member ns.engine
			 */
			function build() {
				if (router) {
					// @TODO: Consider passing viewport options via script tag arguments (web-ui-fw style).
					setViewport();

					eventUtils.trigger(document, "beforerouterinit", router, false);
					router.init(justBuild);
					eventUtils.trigger(document, "routerinit", router, false);
				}
			}

			/**
			 * Method to remove all listeners bound in run
			 * @method stop
			 * @static
			 * @member ns.engine
			 */
			function stop() {
				if (router) {
					router.destroy();
				}
			}

			/**
			 * Add to object value at index equal to type of arg.
			 * @method getType
			 * @param {Object} result
			 * @param {*} arg
			 * @return {Object}
			 * @static
			 * @private
			 * @member ns.engine
			 */
			function getType(result, arg) {
				var type = arg instanceof HTMLElement ? "HTMLElement" : typeof arg;
				result[type] = arg;
				return result;
			}

			/**
			 * Convert args array to object with keys being types and arguments mapped by values
			 * @method getArgumentsTypes
			 * @param {Arguments[]} args
			 * @return {Object}
			 * @static
			 * @private
			 * @member ns.engine
			 */
			function getArgumentsTypes(args) {
				return tau.util.array.reduce(args, getType, {});
			}

			/*
			 document.addEventListener(eventType.BOUND, function () {
			 //@TODO dump it to file for faster binding by ids
			 nsWidgetBindingMap = widgetBindingMap;
			 }, false);
			 */
			ns.widgetDefinitions = {};
			engine = {
				justBuild: location.hash === "#build",
				/**
				 * object with names of engine attributes
				 * @property {Object} dataTau
				 * @property {string} [dataTau.built="data-tau-built"] attribute inform that widget id build
				 * @property {string} [dataTau.name="data-tau-name"] attribute contains widget name
				 * @property {string} [dataTau.bound="data-tau-bound"] attribute inform that widget id bound
				 * @property {string} [dataTau.separator=","] separation string for widget names
				 * @static
				 * @member ns.engine
				 */
				dataTau: {
					built: DATA_BUILT,
					name: DATA_NAME,
					bound: DATA_BOUND,
					separator: NAMES_SEPARATOR
				},
				destroyWidget: destroyWidget,
				destroyAllWidgets: destroyAllWidgets,
				createWidgets: createWidgets,

				/**
				 * Method to get all definitions of widgets
				 * @method getDefinitions
				 * @return {Object}
				 * @static
				 * @member ns.engine
				 */
				getDefinitions: function () {
					return widgetDefs;
				},
				/**
				 * Returns definition of widget
				 * @method getWidgetDefinition
				 * @param {string} name
				 * @static
				 * @member ns.engine
				 * @returns {Object}
				 */
				getWidgetDefinition: function (name) {
					return widgetDefs[name];
				},
				defineWidget: defineWidget,
				getBinding: getBinding,
				getAllBindings: getAllBindings,
				setBinding: setBinding,
				// @TODO either rename or fix functionally because
				// this method does not only remove binding but
				// actually destroys widget
				removeBinding: removeBinding,
				removeAllBindings: removeAllBindings,

				/**
				 * Clear bindings of widgets
				 * @method _clearBindings
				 * @static
				 * @member ns.engine
				 */
				_clearBindings: function () {
					//clear and set references to the same object
					widgetBindingMap = {};
				},

				build: build,

				/**
				 * Run engine
				 * @method run
				 * @static
				 * @member ns.engine
				 */
				run: function () {
										stop();

					eventUtils.fastOn(document, "create", createEventHandler);

					eventUtils.trigger(document, eventType.INIT, {tau: ns});

					switch (document.readyState) {
					case "interactive":
					case "complete":
						build();
						break;
					default:
						eventUtils.fastOn(document, "DOMContentLoaded", build.bind(engine));
						break;
					}
				},

				/**
				 * Return router
				 * @method getRouter
				 * @return {Object}
				 * @static
				 * @member ns.engine
				 */
				getRouter: function () {
					return router;
				},

				/**
				 * Initialize router. This method should be call in file with router class definition.
				 * @method initRouter
				 * @param {Function} RouterClass Router class
				 * @static
				 * @member ns.engine
				 */
				initRouter: function (RouterClass) {
					router = new RouterClass();
				},

				/**
				 * Build instance of widget and binding events
				 * Returns error when empty element is passed
				 * @method instanceWidget
				 * @param {HTMLElement} [element]
				 * @param {string} name
				 * @param {Object} [options]
				 * @return {?Object}
				 * @static
				 * @member ns.engine
				 */
				instanceWidget: function (element, name, options) {
					var binding,
						definition,
						argumentsTypes = getArgumentsTypes(arguments);

					// Map arguments with specific types to correct variables
					// Only name is required argument
					element = argumentsTypes.HTMLElement;
					name = argumentsTypes.string;
					options = argumentsTypes.object;
					// If element exists try to find existing binding
					if (element) {
						binding = getBinding(element, name);
					}
					// If didn't found binding build new widget
					if (!binding && widgetDefs[name]) {
						definition = widgetDefs[name];
						element = processHollowWidget(element, definition, options);
						binding = getBinding(element, name);
					}
					return binding;
				},

				stop: stop,

				/**
				 * Method to change build mode
				 * @method setJustBuild
				 * @param {boolean} newJustBuild
				 * @static
				 * @member ns.engine
				 */
				setJustBuild: function (newJustBuild) {
					// Set location hash to have a consistent behavior
					if(newJustBuild){
						location.hash = "build";
					} else {
						location.hash = "";
					}

					justBuild = newJustBuild;
				},

				/**
				 * Method to get build mode
				 * @method getJustBuild
				 * @return {boolean}
				 * @static
				 * @member ns.engine
				 */
				getJustBuild: function () {
					return justBuild;
				},
				_createEventHandler : createEventHandler
			};

			engine.eventType = eventType;
			ns.engine = engine;
			}(window, window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Anchor Highlight Utility
 * Utility enables highlight links.
 * @class ns.util.anchorHighlight
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Damian Osipiuk <d.osipiuk@samsung.com>
 * @author Konrad Lipner <k.lipner@samsung.com>
 */
(function (document, window, ns) {
	
				/* anchorHighlightController.js
			To prevent perfomance regression when scrolling,
			do not apply hover class in anchor.
			Instead, this code checks scrolling for time threshold and
			decide how to handle the color.
			When scrolling with anchor, it checks flag and decide to highlight anchor.
			While it helps to improve scroll performance,
			it lowers responsiveness of the element for 50msec.
			*/

			/**
			 * Touch start x
			 * @property {number} startX
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			var startX,
				/**
				 * Touch start y
				 * @property {number} startY
				 * @member ns.util.anchorHighlight
				 * @private
				 * @static
				 */
				startY,
				/**
				 * Did page scrolled
				 * @property {boolean} didScroll
				 * @member ns.util.anchorHighlight
				 * @private
				 * @static
				 */
				didScroll,
				/**
				 * Touch target element
				 * @property {HTMLElement} target
				 * @member ns.util.anchorHighlight
				 * @private
				 * @static
				 */
				target,
				/**
				 * Timer id of adding activeClass delay
				 * @property {number} addActiveClassTimerID
				 * @member ns.util.anchorHighlight
				 * @private
				 * @static
				 */
				addActiveClassTimerID,
				/**
				 * Object with default options
				 * @property {Object} options
				 * Treshold after which didScroll will be set
				 * @property {number} [options.scrollThreshold=5]
				 * Time to wait before adding activeClass
				 * @property {number} [options.addActiveClassDelay=10]
				 * Time to stay activeClass after touch end
				 * @property {number} [options.keepActiveClassDelay=100]
				 * @member ns.util.anchorHighlight
				 * @private
				 * @static
				 */
				options = {
					scrollThreshold: 30,
					addActiveClassDelay: 10,
					keepActiveClassDelay: 100
				},
				/**
				 * Class used to mark element as active
				 * @property {string} [activeClassLI="ui-li-active"] activeClassLI
				 * @member ns.util.anchorHighlight
				 * @private
				 * @static
				 */
				activeClassLI = "ui-li-active",
				/**
				 * Function invoked after touch move ends
				 * @method removeTouchMove
				 * @member ns.util.anchorHighlight
				 * @private
				 * @static
				 */
				removeTouchMove,
				/**
				 * Alias for class {@link ns.util.selectors}
				 * @property {Object} selectors
				 * @member ns.util.anchorHighlight
				 * @private
				 * @static
				 */
				selectors = ns.util.selectors;


			/**
			 * Get closest highlightable element
			 * @method detectHighlightTarget
			 * @param {HTMLElement} target
			 * @return {HTMLElement}
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function detectHighlightTarget(target) {
				target = selectors.getClosestBySelector(target, 'a, label');
				return target;
			}

			/**
			 * Get closest li element
			 * @method detectLiElement
			 * @param {HTMLElement} target
			 * @return {HTMLElement}
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function detectLiElement(target) {
				target = selectors.getClosestByTag(target, 'li');
				return target;
			}

			/**
			 * Add active class to touched element
			 * @method addActiveClass
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function addActiveClass() {
				var liTarget;
				target = detectHighlightTarget(target);
				if (!didScroll && target && (target.tagName === "A" || target.tagName === "LABEL")) {
					liTarget = detectLiElement(target);
					if( liTarget ) {
						liTarget.classList.add(activeClassLI);
					}
				}
			}

			/**
			 * Get all active elements
			 * @method getActiveElements
			 * @return {Array}
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function getActiveElements() {
				return document.getElementsByClassName(activeClassLI);
			}

			/**
			 * Remove active class from active elements
			 * @method removeActiveClass
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function removeActiveClass() {
				var activeA = getActiveElements(),
					activeALength = activeA.length,
					i;
				for (i = 0; i < activeALength; i++) {
					if (activeA[i]) {
						activeA[i].classList.remove(activeClassLI);
					}
				}
			}

			/**
			 * Function invoked during touch move
			 * @method touchmoveHandler
			 * @param {Event} event
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function touchmoveHandler(event) {
				var touch = event.touches[0];
				didScroll = didScroll ||
					(Math.abs(touch.clientX - startX) > options.scrollThreshold || Math.abs(touch.clientY - startY) > options.scrollThreshold);

				if (didScroll) {
					removeTouchMove();
					removeActiveClass();
				}
			}

			/**
			 * Function invoked after touch start
			 * @method touchstartHandler
			 * @param {Event} event
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function touchstartHandler(event) {
				var touches = event.touches,
					touch = touches[0];

				if (touches.length === 1) {
					didScroll = false;
					startX = touch.clientX;
					startY = touch.clientY;
					target = event.target;

					document.addEventListener("touchmove", touchmoveHandler, false);
					clearTimeout(addActiveClassTimerID);
					addActiveClassTimerID = setTimeout(addActiveClass, options.addActiveClassDelay);
				}
			}

			removeTouchMove = function () {
				document.removeEventListener("touchmove", touchmoveHandler, false);
			};

			/**
			 * Function invoked after touch
			 * @method touchendHandler
			 * @param {Event} event
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function touchendHandler(event) {
				if (event.touches.length === 0) {
					clearTimeout(addActiveClassTimerID);
					addActiveClassTimerID = null;
					if (!didScroll) {
						setTimeout(removeActiveClass, options.keepActiveClassDelay);
					}
					didScroll = false;
				}
			}

			/**
			 * Function invoked after visibilitychange event
			 * @method checkPageVisibility
			 * @member ns.util.anchorHighlight
			 * @private
			 * @static
			 */
			function checkPageVisibility() {
				if (document.visibilityState === "hidden") {
					removeActiveClass();
				}
			}

			/**
			 * Bind events to document
			 * @method enable
			 * @member ns.util.anchorHighlight
			 * @static
			 */
			function enable() {
				document.addEventListener("touchstart", touchstartHandler, false);
				document.addEventListener("touchend", touchendHandler, false);
				document.addEventListener("visibilitychange", checkPageVisibility, false);
				window.addEventListener("pagehide", removeActiveClass, false);
			}

			/**
			 * Unbinds events from document.
			 * @method disable
			 * @member ns.util.anchorHighlight
			 * @static
			 */
			function disable() {
				document.removeEventListener("touchstart", touchstartHandler, false);
				document.removeEventListener("touchend", touchendHandler, false);
				window.removeEventListener("pagehide", removeActiveClass, false);
			}

			enable();

			ns.util.anchorHighlight = {
				enable: enable,
				disable: disable
			};

			}(document, window, ns));

/*global window, define */
/*jslint plusplus: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Utility DOM
 * Utility object with function to DOM manipulation, CSS properties support
 * and DOM attributes support.
 *
 * # How to replace jQuery methods  by ns methods
 * ## append vs appendNodes
 *
 * #### HTML code before manipulation
 *
 *     @example
 *     <div>
 *         <div id="first">Hello</div>
 *         <div id="second">And</div>
 *         <div id="third">Goodbye</div>
 *     </div>
 *
 * #### jQuery manipulation
 *
 *     @example
 *     $( "#second" ).append( "<span>Test</span>" );

 * #### ns manipulation
 *
 *     @example
 *     var context = document.getElementById("second"),
 *         element = document.createElement("span");
 *     element.innerHTML = "Test";
 *     ns.util.DOM.appendNodes(context, element);
 *
 * #### HTML code after manipulation
 *
 *     @example
 *     <div>
 *         <div id="first">Hello</div>
 *         <div id="second">And
 *             <span>Test</span>
 *         </div>
 *        <div id="third">Goodbye</div>
 *     </div>
 *
 * ## replaceWith vs replaceWithNodes
 *
 * #### HTML code before manipulation
 *
 *     @example
 *     <div>
 *         <div id="first">Hello</div>
 *         <div id="second">And</div>
 *         <div id="third">Goodbye</div>
 *     </div>
 *
 * #### jQuery manipulation
 *
 *     @example
 *     $('#second').replaceWith("<span>Test</span>");
 *
 * #### ns manipulation
 *
 *     @example
 *     var context = document.getElementById("second"),
 *         element = document.createElement("span");
 *     element.innerHTML = "Test";
 *     ns.util.DOM.replaceWithNodes(context, element);
 *
 * #### HTML code after manipulation
 *
 *     @example
 *     <div>
 *         <div id="first">Hello</div>
 *         <span>Test</span>
 *         <div id="third">Goodbye</div>
 *     </div>
 *
 * ## before vs insertNodesBefore
 *
 * #### HTML code before manipulation
 *
 *     @example
 *     <div>
 *         <div id="first">Hello</div>
 *         <div id="second">And</div>
 *         <div id="third">Goodbye</div>
 *     </div>
 *
 * #### jQuery manipulation
 *
 *     @example
 *     $( "#second" ).before( "<span>Test</span>" );
 *
 * #### ns manipulation
 *
 *     @example
 *     var context = document.getElementById("second"),
 *         element = document.createElement("span");
 *     element.innerHTML = "Test";
 *     ns.util.DOM.insertNodesBefore(context, element);
 *
 * #### HTML code after manipulation
 *
 *     @example
 *     <div>
 *         <div id="first">Hello</div>
 *         <span>Test</span>
 *         <div id="second">And</div>
 *         <div id="third">Goodbye</div>
 *     </div>
 *
 * ## wrapInner vs wrapInHTML
 *
 * #### HTML code before manipulation
 *
 *     @example
 *     <div>
 *         <div id="first">Hello</div>
 *         <div id="second">And</div>
 *         <div id="third">Goodbye</div>
 *     </div>
 *
 * #### jQuery manipulation
 *
 *     @example
 *     $( "#second" ).wrapInner( "<span class="new"></span>" );
 *
 * #### ns manipulation
 *
 *     @example
 *     var element = document.getElementById("second");
 *     ns.util.DOM.wrapInHTML(element, "<span class="new"></span>");
 *
 * #### HTML code after manipulation
 *
 *     @example
 *     <div>
 *         <div id="first">Hello</div>
 *         <div id="second">
 *             <span class="new">And</span>
 *         </div>
 *         <div id="third">Goodbye</div>
 *     </div>
 *
 * @class ns.util.DOM
 * @author Jadwiga Sosnowska <j.sosnowska@partner.samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Maciej Moczulski <m.moczulski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (ns) {
	
				ns.util.DOM = ns.util.DOM || {};
			}(ns));

/*global window, define */
/*jslint plusplus: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author Jadwiga Sosnowska <j.sosnowska@partner.samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Maciej Moczulski <m.moczulski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (window, document, ns) {
	
	

			var selectors = ns.util.selectors,
				DOM = ns.util.DOM,
				namespace = "namespace";

			/**
			 * Returns given attribute from element or the closest parent,
			 * which matches the selector.
			 * @method inheritAttr
			 * @member ns.util.DOM
			 * @param {HTMLElement} element
			 * @param {string} attr
			 * @param {string} selector
			 * @return {?string}
			 * @static
			 */
			DOM.inheritAttr = function (element, attr, selector) {
				var value = element.getAttribute(attr),
					parent;
				if (!value) {
					parent = selectors.getClosestBySelector(element, selector);
					if (parent) {
						return parent.getAttribute(attr);
					}
				}
				return value;
			};

			/**
			 * Returns Number from properties described in html tag
			 * @method getNumberFromAttribute
			 * @member ns.util.DOM
			 * @param {HTMLElement} element
			 * @param {string} attribute
			 * @param {string=} [type] auto type casting
			 * @param {number} [defaultValue] default returned value
			 * @static
			 * @return {number}
			 */
			DOM.getNumberFromAttribute = function (element, attribute, type, defaultValue) {
				var value = element.getAttribute(attribute),
					result = defaultValue;

				if (!isNaN(value)) {
					if (type === "float") {
						value = parseFloat(value);
						if (!isNaN(value)) {
							result = value;
						}
					} else {
						value = parseInt(value, 10);
						if (!isNaN(value)) {
							result = value;
						}
					}
				}
				return result;
			};

			function getDataName(name) {
				var namespace = ns.getConfig(namespace);
				return "data-" + (namespace ? namespace + "-" : "") + name;
			}

			/**
			 * Special function to set attribute and property in the same time
			 * @method setAttribute
			 * @param {HTMLElement} element
			 * @param {string} name
			 * @param {Mixed} value
			 * @member ns.util.DOM
			 * @static
			 */
			function setAttribute(element, name, value) {
				element[name] = value;
				element.setAttribute(name, value);
			}

			/**
			 * This function sets value of attribute data-{namespace}-{name} for element.
			 * If the namespace is empty, the attribute data-{name} is used.
			 * @method setNSData
			 * @param {HTMLElement} element Base element
			 * @param {string} name Name of attribute
			 * @param {string|number|boolean} value New value
			 * @member ns.util.DOM
			 * @static
			 */
			DOM.setNSData = function (element, name, value) {
				element.setAttribute(getDataName(name), value);
			};

			/**
			 * This function returns value of attribute data-{namespace}-{name} for element.
			 * If the namespace is empty, the attribute data-{name} is used.
			 * Method may return boolean in case of 'true' or 'false' strings as attribute value.
			 * @method getNSData
			 * @param {HTMLElement} element Base element
			 * @param {string} name Name of attribute
			 * @member ns.util.DOM
			 * @return {?string|boolean}
			 * @static
			 */
			DOM.getNSData = function (element, name) {
				var value = element.getAttribute(getDataName(name));

				if (value === "true") {
					return true;
				}

				if (value === "false") {
					return false;
				}

				return value;
			};

			/**
			 * This function returns true if attribute data-{namespace}-{name} for element is set
			 * or false in another case. If the namespace is empty, attribute data-{name} is used.
			 * @method hasNSData
			 * @param {HTMLElement} element Base element
			 * @param {string} name Name of attribute
			 * @member ns.util.DOM
			 * @return {boolean}
			 * @static
			 */
			DOM.hasNSData = function (element, name) {
				return element.hasAttribute(getDataName(name));
			};

			/**
			 * Get or set value on data attribute.
			 * @method nsData
			 * @param {HTMLElement} element
			 * @param {string} name
			 * @param {?Mixed} [value]
			 * @static
			 * @member ns.util.DOM
			 */
			DOM.nsData = function (element, name, value) {
				// @TODO add support for object in value
				if (value === undefined) {
					return DOM.getNSData(element, name);
				} else {
					return DOM.setNSData(element, name, value);
				}
			};

			/**
			 * This function removes attribute data-{namespace}-{name} from element.
			 * If the namespace is empty, attribute data-{name} is used.
			 * @method removeNSData
			 * @param {HTMLElement} element Base element
			 * @param {string} name Name of attribute
			 * @member ns.util.DOM
			 * @static
			 */
			DOM.removeNSData = function (element, name) {
				element.removeAttribute(getDataName(name));
			};

			/**
			 * Returns object with all data-* attributes of element
			 * @method getData
			 * @param {HTMLElement} element Base element
			 * @member ns.util.DOM
			 * @return {Object}
			 * @static
			 */
			DOM.getData = function (element) {
				var dataPrefix = "data-",
					data = {},
					attrs = element.attributes,
					attr,
					nodeName,
					value,
					i,
					length = attrs.length;

				for (i = 0; i < length; i++) {
					attr = attrs.item(i);
					nodeName = attr.nodeName;
					if (nodeName.indexOf(dataPrefix) > -1) {
						value = attr.value;
						data[nodeName.replace(dataPrefix, "")] = value.toLowerCase() === "true" ? true : value.toLowerCase() === "false" ? false : value;
					}
				}

				return data;
			};

			/**
			 * Special function to remove attribute and property in the same time
			 * @method removeAttribute
			 * @param {HTMLElement} element
			 * @param {string} name
			 * @member ns.util.DOM
			 * @static
			 */
			DOM.removeAttribute = function (element, name) {
				element.removeAttribute(name);
				element[name] = false;
			};

			DOM.setAttribute = setAttribute;
			/**
			 * Special function to set attributes and propertie in the same time
			 * @method setAttribute
			 * @param {HTMLElement} element
			 * @param {Object} name
			 * @param {Mixed} value
			 * @member ns.util.DOM
			 * @static
			 */
			DOM.setAttributes = function (element, values) {
				var i,
					names = Object.keys(values),
					name,
					len;

				for (i = 0, len = names.length; i < len; i++) {
					name = names[i];
					setAttribute(element, name, values[name]);
				}
			};
			}(window, window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Namespace For Widgets
 * Namespace For Widgets
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @class ns.widget
 */
(function (document, ns) {
	
				var engine = ns.engine,
				widget = {
					/**
					 * Get bound widget for element
					 * @method getInstance
					 * @static
					 * @param {HTMLElement|string} element
					 * @param {string} type widget name
					 * @return {?Object}
					 * @member ns.widget
					 */
					getInstance: engine.getBinding,
					/**
					 * Returns Get all bound widget for element or id gives as parameter
					 * @method getAllInstances
					 * @param {HTMLElement|string} element
					 * @return {?Object}
					 * @static
					 * @member ns.widget
					 */
					getAllInstances: engine.getAllBindings
				};

			function widgetConstructor(name, element, options) {
				return engine.instanceWidget(element, name, options);
			}

			document.addEventListener(engine.eventType.WIDGET_DEFINED, function (evt) {
				var definition = evt.detail,
					name = definition.name;

				ns.widget[name] = widgetConstructor.bind(null, name);

			}, true);

			/** @namespace ns.widget */
			ns.widget = widget;
			}(window.document, ns));

/*global window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/*
 */
/**
 * #BaseWidget
 * Prototype class of widget
 *
 * ## How to invoke creation of widget from JavaScript
 *
 * To build and initialize widget in JavaScript you have to use method {@link ns.engine#instanceWidget} . First argument for method
 * is HTMLElement, which specifies the element of widget. Second parameter is name of widget to create.
 *
 * If you load jQuery before initializing tau library, you can use standard jQuery UI Widget notation.
 *
 * ### Examples
 * #### Build widget from JavaScript
 *
 *		@example
 *		var element = document.getElementById("id"),
 *			ns.engine.instanceWidget(element, "Button");
 *
 * #### Build widget from jQuery
 *
 *		@example
 *		var element = $("#id").button();
 *
 * ## How to create new widget
 *
 *		@example
 *		(function (ns) {
 *			
 *			 *					var BaseWidget = ns.widget.BaseWidget, // create alias to main objects
 *						...
 *						arrayOfElements, // example of private property, common for all instances of widget
 *						Button = function () { // create local object with widget
 *							...
 *						},
 *						prototype = new BaseWidget(); // add ns.widget.BaseWidget as prototype to widget's object, for better minification this should be assign to local variable and next variable should be assign to prototype of object
 *
 *					function closestEnabledButton(element) { // example of private method
 *						...
 *					}
 *					...
 *
 *					prototype.options = { //add default options to be read from data- attributes
 *						theme: "s",
 *						...
 *					};
 *
 *					prototype._build = function (template, element) { // method called when the widget is being built, should contain all HTML manipulation actions
 *						...
 *						return element;
 *					};
 *
 *					prototype._init = function (element) { // method called during initialization of widget, should contain all actions necessary fastOn application start
 *						...
 *						return element;
 *					};
 *
 *					prototype._bindEvents = function (element) { // method to bind all events, should contain all event bindings
 *						...
 *					};
 *
 *					prototype._enable = function (element) { // method called during invocation of enable() method
 *						...
 *					};
 *
 *					prototype._disable = function (element) { // method called during invocation of disable() method
 *						...
 *					};
 *
 *					prototype.refresh = function (element) { // example of public method
 *						...
 *					};
 *
 *					prototype._refresh = function () { // example of protected method
 *						...
 *					};
 *
 *					Button.prototype = prototype;
 *
 *					engine.defineWidget( // define widget
 *						"Button", //name of widget
 *						"[data-role='button'],button,[type='button'],[type='submit'],[type='reset']",  //widget's selector
 *						[ // public methods, here should be list all public method, without that method will not be available
 *							"enable",
 *							"disable",
 *							"refresh"
 *						],
 *						Button, // widget's object
 *						"mobile" // widget's namespace
 *					);
 *					ns.widget.Button = Button;
 *					 *		}(ns));
 * @author Jadwiga Sosnowska <j.sosnowska@samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Tomasz Lukawski <t.lukawski@samsung.com>
 * @author Przemyslaw Ciezkowski <p.ciezkowski@samsung.com>
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 * @author Michał Szepielak <m.szepielak@samsung.com>
 * @class ns.widget.BaseWidget
 */
(function (document, ns, undefined) {
	
				/**
			 * Alias to Array.slice function
			 * @method slice
			 * @member ns.widget.BaseWidget
			 * @private
			 * @static
			 */
			var slice = [].slice,
				/**
				 * Alias to ns.engine
				 * @property {ns.engine} engine
				 * @member ns.widget.BaseWidget
				 * @private
				 * @static
				 */
				engine = ns.engine,
				engineDataTau = engine.dataTau,
				util = ns.util,
				/**
				 * Alias to {@link ns.event}
				 * @property {Object} eventUtils
				 * @member ns.widget.BaseWidget
				 * @private
				 * @static
				 */
				eventUtils = ns.event,
				/**
				 * Alias to {@link ns.util.DOM}
				 * @property {Object} domUtils
				 * @private
				 * @static
				 */
				domUtils = util.DOM,
				/**
				 * Alias to {@link ns.util.object}
				 * @property {Object} objectUtils
				 * @private
				 * @static
				 */
				objectUtils = util.object,
				BaseWidget = function () {
					return this;
				},
				prototype = {},
				/**
				 * Property with string represent function type 
				 * (for better minification)
				 * @property {string} [TYPE_FUNCTION="function"]
				 * @private
				 * @static
				 * @readonly
				 */
				TYPE_FUNCTION = "function",
				disableClass = "ui-state-disabled",
				ariaDisabled = "aria-disabled";

			BaseWidget.classes = {
				disable: disableClass
			};

			/**
			 * Protected method configuring the widget
			 * @method _configure
			 * @member ns.widget.BaseWidget
			 * @protected
			 * @template
			 * @internal
			 */
			/**
			 * Configures widget object from definition.
			 *
			 * It calls such methods as #\_getCreateOptions and #\_configure.
			 * @method configure
			 * @param {Object} definition
			 * @param {string} definition.name Name of the widget
			 * @param {string} definition.selector Selector of the widget
			 * @param {HTMLElement} element Element of widget
			 * @param {Object} options Configure options
			 * @member ns.widget.BaseWidget
			 * @chainable
			 * @internal
			 */
			prototype.configure = function (definition, element, options) {
				var self = this,
					definitionName,
					definitionNamespace;
				/**
				 * Object with options for widget
				 * @property {Object} [options={}]
				 * @member ns.widget.BaseWidget
				 */
				self.options = self.options || {};
				/**
				 * Base element of widget
				 * @property {?HTMLElement} [element=null]
				 * @member ns.widget.BaseWidget
				 */
				self.element = self.element || null;
				if (definition) {
					definitionName = definition.name;
					definitionNamespace = definition.namespace;
					/**
					 * Name of the widget
					 * @property {string} name
					 * @member ns.widget.BaseWidget
					 */
					self.name = definitionName;

					/**
					 * Name of the widget (in lower case)
					 * @property {string} widgetName
					 * @member ns.widget.BaseWidget
					 */
					self.widgetName = definitionName;

					/**
					 * Namespace of widget events
					 * @property {string} widgetEventPrefix
					 * @member ns.widget.BaseWidget
					 */
					self.widgetEventPrefix = definitionName.toLowerCase();

					/**
					 * Namespace of the widget
					 * @property {string} namespace 
					 * @member ns.widget.BaseWidget
					 */
					self.namespace = definitionNamespace;

					/**
					 * Full name of the widget
					 * @property {string} widgetFullName
					 * @member ns.widget.BaseWidget
					 */
					self.widgetFullName = ((definitionNamespace ? definitionNamespace + "-" : "") + definitionName).toLowerCase();
					/**
					 * Id of widget instance
					 * @property {string} id
					 * @member ns.widget.BaseWidget
					 */
					self.id = ns.getUniqueId();

					/**
					 * Widget's selector
					 * @property {string} selector
					 * @member ns.widget.BaseWidget
					 */
					self.selector = definition.selector;
				}

				if (typeof self._configure === TYPE_FUNCTION) {
					self._configure(element);
				}

				self._getCreateOptions(element);

				objectUtils.fastMerge(self.options, options);
			};

			/**
			 * Reads data-* attributes and save to options object.
			 * @method _getCreateOptions
			 * @param {HTMLElement} element Base element of the widget
			 * @return {Object}
			 * @member ns.widget.BaseWidget
			 * @protected
			 */
			prototype._getCreateOptions = function (element) {
				var options = this.options,
					bigRegexp = /[A-Z]/g;
				if (options !== undefined) {
					Object.keys(options).forEach(function (option) {
						// Get value from data-{namespace}-{name} element's attribute
						// based on widget.options property keys
						var value = domUtils.getNSData(element, (option.replace(bigRegexp, function (c) {
							return "-" + c.toLowerCase();
						})));

						if (value !== null) {
							options[option] = value;
						}
					});
				}
				return options;
			};
			/**
			 * Protected method building the widget
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement} widget's element
			 * @member ns.widget.BaseWidget
			 * @protected
			 * @template
			 */
			/**
			 * Builds widget.
			 *
			 * It calls method #\_build.
			 *
			 * Before starting building process, the event beforecreate with
			 * proper prefix defined in variable widgetEventPrefix is triggered.
			 * @method build
			 * @param {HTMLElement} element Element of widget before building process
			 * @return {HTMLElement} Element of widget after building process
			 * @member ns.widget.BaseWidget
			 * @internal
			 */
			prototype.build = function (element) {
				var self = this,
					id,
					node,
					dataBuilt = element.getAttribute(engineDataTau.built),
					dataName = element.getAttribute(engineDataTau.name);

				eventUtils.trigger(element, self.widgetEventPrefix + "beforecreate");

				id = element.id;
				if (id) {
					self.id = id;
				} else {
					element.id = self.id;
				}

				if (typeof self._build === TYPE_FUNCTION) {
					node = self._build(element);
				} else {
					node = element;
				}

				// Append current widget name to data-tau-built and data-tau-name attributes
				dataBuilt = !dataBuilt ? self.name : dataBuilt + engineDataTau.separator + self.name;
				dataName = !dataName ? self.name : dataName + engineDataTau.separator + self.name;

				element.setAttribute(engineDataTau.built, dataBuilt);
				element.setAttribute(engineDataTau.name, dataName);

				return node;
			};

			/**
			 * Protected method initializing the widget
			 * @method _init
			 * @param {HTMLElement} element
			 * @member ns.widget.BaseWidget
			 * @template
			 * @protected
			 */
			/**
			 * Initializes widget.
			 *
			 * It calls method #\_init.
			 * @method init
			 * @param {HTMLElement} element Element of widget before initialization
			 * @member ns.widget.BaseWidget
			 * @chainable
			 * @internal
			 */
			prototype.init = function (element) {
				var self = this;
				self.id = element.id;

				if (typeof self._init === TYPE_FUNCTION) {
					self._init(element);
				}

				if (element.getAttribute("disabled") || self.options.disabled === true) {
					self.disable();
				} else {
					self.enable();
				}

				return self;
			};

			/**
			 * Returns base element widget
			 * @member ns.widget.BaseWidget
			 * @return {HTMLElement|null}
			 * @instance
			 */
			prototype.getContainer = function () {
				var self = this;
				if (typeof self._getContainer === TYPE_FUNCTION) {
					return self._getContainer();
				}
				return self.element;
			};

			/**
			 * Bind widget events attached in init mode
			 * @method _bindEvents
			 * @param {HTMLElement} element Base element of widget
			 * @member ns.widget.BaseWidget
			 * @template
			 * @protected
			 */
			/**
			 * Binds widget events.
			 *
			 * It calls such methods as #\_buildBindEvents and #\_bindEvents.
			 * At the end of binding process, the event "create" with proper
			 * prefix defined in variable widgetEventPrefix is triggered.
			 * @method bindEvents
			 * @param {HTMLElement} element Base element of the widget
			 * @param {boolean} onlyBuild Inform about the type of bindings: build/init
			 * @member ns.widget.BaseWidget
			 * @chainable
			 * @internal
			 */
			prototype.bindEvents = function (element, onlyBuild) {
				var self = this,
					dataBound = element.getAttribute(engineDataTau.bound);

				if (!onlyBuild) {
					dataBound = !dataBound ? self.name : dataBound + engineDataTau.separator + self.name;
					element.setAttribute(engineDataTau.bound, dataBound);
				}
				if (typeof self._buildBindEvents === TYPE_FUNCTION) {
					self._buildBindEvents(element);
				}
				if (!onlyBuild && typeof self._bindEvents === TYPE_FUNCTION) {
					self._bindEvents(element);
				}

				self.trigger(self.widgetEventPrefix + "create", self);

				return self;
			};

			/**
			 * Focus widget's element.
			 *
			 * This function calls function focus on element and if it is known
			 * the direction of event, the proper css classes are added/removed.
			 * @method focus
			 * @param {object} options The options of event.
			 * @param {"up"|"down"|"left"|"right"} direction
			 * For example, if this parameter has value "down", it means that the movement
			 * comes from the top (eg. down arrow was pressed on keyboard).
			 * @param {HTMLElement} previousElement Element to blur
			 * @member ns.widget.BaseWidget
			 */
			prototype.focus = function (options) {
				var self = this,
					element = self.element,
					blurElement,
					blurWidget;

				options = options || {};

				blurElement = options.previousElement;
				// we try to blur element, which has focus previously
				if (blurElement) {
					blurWidget = engine.getBinding(blurElement);
					// call blur function on widget
					if (blurWidget) {
						options = objectUtils.merge({}, options, {element: blurElement});
						blurWidget.blur(options);
					} else {
						// or on element, if widget does not exist
						blurElement.blur();
					}
				}

				options = objectUtils.merge({}, options, {element: element});

				// set focus on element
				eventUtils.trigger(document, "taufocus", options);
				element.focus();

				return true;
			};

			/**
			 * Blur widget's element.
			 *
			 * This function calls function blur on element and if it is known
			 * the direction of event, the proper css classes are added/removed.
			 * @method blur
			 * @param {object} options The options of event.
			 * @param {"up"|"down"|"left"|"right"} direction
			 * @member ns.widget.BaseWidget
			 */
			prototype.blur = function (options) {
				var self = this,
					element = self.element;

				options = objectUtils.merge({}, options, {element: element});

				// blur element
				eventUtils.trigger(document, "taublur", options);
				element.blur();
				return true;
			};

			/**
			 * Protected method destroying the widget
			 * @method _destroy
			 * @template
			 * @protected
			 * @member ns.widget.BaseWidget
			 */
			/**
			 * Destroys widget.
			 *
			 * It calls method #\_destroy.
			 *
			 * At the end of destroying process, the event "destroy" with proper
			 * prefix defined in variable widgetEventPrefix is triggered and
			 * the binding set in engine is removed.
			 * @method destroy
			 * @param {HTMLElement} element Base element of the widget
			 * @member ns.widget.BaseWidget
			 */
			prototype.destroy = function (element) {
				var self = this;
				element = element || self.element;
				if (typeof self._destroy === TYPE_FUNCTION) {
					self._destroy(element);
				}
				if (self.element) {
					self.trigger(self.widgetEventPrefix + "destroy");
				}
				if (element) {
					engine.removeBinding(element, self.name);
				}
			};

			/**
			 * Protected method disabling the widget
			 * @method _disable
			 * @protected
			 * @member ns.widget.BaseWidget
			 * @template
			 */
			/**
			 * Disables widget.
			 *
			 * It calls method #\_disable.
			 * @method disable
			 * @member ns.widget.BaseWidget
			 * @chainable
			 */
			prototype.disable = function () {
				var self = this,
					args = slice.call(arguments),
					element = self.element;

				element.classList.add(disableClass);
				element.setAttribute(ariaDisabled, true);

				if (typeof self._disable === TYPE_FUNCTION) {
					args.unshift(element);
					self._disable.apply(self, args);
				}
				return this;
			};

			/**
			 * Check if widget is disabled.
			 * @method isDisabled
			 * @member ns.widget.BaseWidget
			 * @return {boolean} Returns true if widget is disabled
			 */
			prototype.isDisabled = function () {
				var self = this;
				return self.element.getAttribute("disabled") || self.options.disabled === true;
			};

			/**
			 * Protected method enabling the widget
			 * @method _enable
			 * @protected
			 * @member ns.widget.BaseWidget
			 * @template
			 */
			/**
			 * Enables widget.
			 *
			 * It calls method #\_enable.
			 * @method enable
			 * @member ns.widget.BaseWidget
			 * @chainable
			 */
			prototype.enable = function () {
				var self = this,
					args = slice.call(arguments),
					element = self.element;

				element.classList.remove(disableClass);
				element.setAttribute(ariaDisabled, false);

				if (typeof self._enable === TYPE_FUNCTION) {
					args.unshift(element);
					self._enable.apply(self, args);
				}
				return this;
			};

			/**
			 * Protected method causing the widget to refresh
			 * @method _refresh
			 * @protected
			 * @member ns.widget.BaseWidget
			 * @template
			 */
			/**
			 * Refreshes widget.
			 *
			 * It calls method #\_refresh.
			 * @method refresh
			 * @member ns.widget.BaseWidget
			 * @chainable
			 */
			prototype.refresh = function () {
				var self = this;
				if (typeof self._refresh === TYPE_FUNCTION) {
					self._refresh.apply(self, arguments);
				}
				return self;
			};


			/**
			 * Gets or sets options of the widget.
			 *
			 * This method can work in many context.
			 *
			 * If first argument is type of object them, method set values for options given in object. Keys of object are names of options and values from object are values to set.
			 *
			 * If you give only one string argument then method return value for given option.
			 *
			 * If you give two arguments and first argument will be a string then second argument will be intemperate as value to set.
			 *
			 * @method option
			 * @param {string|Object} [name] name of option
			 * @param {*} [value] value to set
			 * @member ns.widget.BaseWidget
			 * @return {*} return value of option or undefined if method is called in setter context
			 */
			prototype.option = function (/*name, value*/) {
				var self = this,
					args = slice.call(arguments),
					firstArgument = args.shift(),
					secondArgument = args.shift(),
					key,
					result,
					partResult,
					refresh = false;
				if (typeof firstArgument === "string") {
					result = self._oneOption(firstArgument, secondArgument);
					if (firstArgument !== undefined && secondArgument !== undefined) {
						refresh = result;
						result = undefined;
					}
				} else if (typeof firstArgument === "object") {
					for (key in firstArgument) {
						if (firstArgument.hasOwnProperty(key)) {
							partResult = self._oneOption(key, firstArgument[key]);
							if (key !== undefined && firstArgument[key] !== undefined) {
								refresh = refresh || partResult;
							}
						}
					}
				}
				if (refresh) {
					self.refresh();
				}
				return result;
			};

			/**
			 * Gets or sets one option of the widget.
			 *
			 * @method _oneOption
			 * @param {string} field
			 * @param {*} value
			 * @member ns.widget.BaseWidget
			 * @return {*}
			 * @protected
			 */
			prototype._oneOption = function (field, value) {
				var self = this,
					methodName,
					refresh = false;
				if (value === undefined) {
					methodName = "_get" + (field[0].toUpperCase() + field.slice(1));
					if (typeof self[methodName] === TYPE_FUNCTION) {
						return self[methodName]();
					}
					return self.options[field];
				}
				methodName = "_set" + (field[0].toUpperCase() + field.slice(1));
				if (typeof self[methodName] === TYPE_FUNCTION) {
					self[methodName](self.element, value);
				} else {
					self.options[field] = value;
					if (self.element) {
						self.element.setAttribute("data-" + (field.replace(/[A-Z]/g, function (c) {
							return "-" + c.toLowerCase();
						})), value);
						refresh = true;
					}
				}
				return refresh;
			};

			/**
			 * Returns true if widget has bounded events.
			 *
			 * This methods enables to check if the widget has bounded 
			 * events through the {@link ns.widget.BaseWidget#bindEvents} method.
			 * @method isBound
			 * @param {string} [type] Type of widget
			 * @member ns.widget.BaseWidget
			 * @internal
			 * @return {boolean} true if events are bounded
			 */
			prototype.isBound = function (type) {
				var element = this.element;
				type = type || this.name;
				return element && element.hasAttribute(engineDataTau.bound) && element.getAttribute(engineDataTau.bound).indexOf(type) > -1;
			};

			/**
			 * Returns true if widget is built.
			 *
			 * This methods enables to check if the widget was built 
			 * through the {@link ns.widget.BaseWidget#build} method.
			 * @method isBuilt
			 * @param {string} [type] Type of widget
			 * @member ns.widget.BaseWidget
			 * @internal
			 * @return {boolean} true if the widget was built
			 */
			prototype.isBuilt = function (type) {
				var element = this.element;
				type = type || this.name;
				return element && element.hasAttribute(engineDataTau.built) && element.getAttribute(engineDataTau.built).indexOf(type) > -1;
			};

			/**
			 * Protected method getting the value of widget
			 * @method _getValue
			 * @return {*}
			 * @member ns.widget.BaseWidget
			 * @template
			 * @protected
			 */
			/**
			 * Protected method setting the value of widget
			 * @method _setValue
			 * @param {*} value
			 * @return {*}
			 * @member ns.widget.BaseWidget
			 * @template
			 * @protected
			 */
			/**
			 * Gets or sets value of the widget.
			 *
			 * @method value
			 * @param {*} [value] New value of widget
			 * @member ns.widget.BaseWidget
			 * @return {*}
			 */
			prototype.value = function (value) {
				var self = this;
				if (value !== undefined) {
					if (typeof self._setValue === TYPE_FUNCTION) {
						return self._setValue(value);
					}
					return self;
				}
				if (typeof self._getValue === TYPE_FUNCTION) {
					return self._getValue();
				}
				return self;
			};

			/**
			 * Triggers an event on widget's element.
			 *
			 * @method trigger
			 * @param {string} eventName The name of event to trigger
			 * @param {?*} [data] additional Object to be carried with the event
			 * @param {boolean} [bubbles=true] Indicating whether the event
			 * bubbles up through the DOM or not
			 * @param {boolean} [cancelable=true] Indicating whether
			 * the event is cancelable
			 * @member ns.widget.BaseWidget
			 * @return {boolean} False, if any callback invoked preventDefault on event object
			 */
			prototype.trigger = function (eventName, data, bubbles, cancelable) {
				return eventUtils.trigger(this.element, eventName, data, bubbles, cancelable);
			};

			/**
			 * Adds event listener to widget's element.
			 * @method on
			 * @param {string} eventName The name of event
			 * @param {Function} listener Function called after event will be trigger
			 * @param {boolean} [useCapture=false] useCapture Parameter of addEventListener
			 * @member ns.widget.BaseWidget
			 */
			prototype.on = function (eventName, listener, useCapture) {
				eventUtils.on(this.element, eventName, listener, useCapture);
			};

			/**
			 * Removes event listener from  widget's element.
			 * @method off
			 * @param {string} eventName The name of event
			 * @param {Function} listener Function call after event will be trigger
			 * @param {boolean} [useCapture=false] useCapture Parameter of addEventListener
			 * @member ns.widget.BaseWidget
			 */
			prototype.off = function (eventName, listener, useCapture) {
				eventUtils.off(this.element, eventName, listener, useCapture);
			};

			BaseWidget.prototype = prototype;

			// definition
			ns.widget.BaseWidget = BaseWidget;

			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * #Namespace For Widgets
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @class ns.widget
 */
(function (document, ns) {
	
				ns.widget.core = ns.widget.core || {};
			}(window.document, ns));

/*global window, define */
/*jslint plusplus: true */
/*jshint -W069 */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author Jadwiga Sosnowska <j.sosnowska@partner.samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Maciej Moczulski <m.moczulski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (window, document, ns) {
	
	
			var DOM = ns.util.DOM,
				DASH_TO_UPPER_CASE_REGEXP = /-([a-z])/gi;

			/**
			 * Returns css property for element
			 * @method getCSSProperty
			 * @param {HTMLElement} element
			 * @param {string} property
			 * @param {string|number|null} [def=null] default returned value
			 * @param {"integer"|"float"|null} [type=null] auto type casting
			 * @return {string|number|null}
			 * @member ns.util.DOM
			 * @static
			 */
			function getCSSProperty(element, property, def, type) {
				var style = window.getComputedStyle(element),
					value = null,
					result = def;
				if (style) {
					value = style.getPropertyValue(property);
					if (value) {
						switch (type) {
						case "integer":
							value = parseInt(value, 10);
							if (!isNaN(value)) {
								result = value;
							}
							break;
						case "float":
							value = parseFloat(value);
							if (!isNaN(value)) {
								result = value;
							}
							break;
						default:
							result = value;
							break;
						}
					}
				}
				return result;
			}

			/**
			 * Extracts css properties from computed css for an element.
			 * The properties values are applied to the specified
			 * properties list (dictionary)
			 * @method extractCSSProperties
			 * @param {HTMLElement} element
			 * @param {Object} properties
			 * @param {?string} [pseudoSelector=null]
			 * @param {boolean} [noConversion=false]
			 * @member ns.util.DOM
			 * @static
			 */
			function extractCSSProperties (element, properties, pseudoSelector, noConversion) {
				var style = window.getComputedStyle(element, pseudoSelector),
					property,
					value = null,
					utils = ns.util;

				// @TODO extractCSSProperties should rather return raw values (with units)
				for (property in properties) {
					if (properties.hasOwnProperty(property)) {
						value = style.getPropertyValue(property);
						if (utils.isNumber(value) && !noConversion) {
							if (value.match(/\./gi)) {
								properties[property] = parseFloat(value);
							} else {
								properties[property] = parseInt(value, 10);
							}
						} else {
							properties[property] = value;
						}
					}
				}
			}

			/**
			 * Returns elements height from computed style
			 * @method getElementHeight
			 * @param {HTMLElement} element
			 * if null then the "inner" value is assigned
			 * @param {"outer"|null} [type=null]
			 * @param {boolean} [includeOffset=false]
			 * @param {boolean} [includeMargin=false]
			 * @param {?string} [pseudoSelector=null]
			 * @param {boolean} [force=false] check even if element is hidden
			 * @return {number}
			 * @member ns.util.DOM
			 * @static
			 */
			function getElementHeight(element, type, includeOffset, includeMargin, pseudoSelector, force) {
				var height = 0,
					style,
					value,
					originalDisplay = null,
					originalVisibility = null,
					originalPosition = null,
					outer = (type && type === "outer") || false,
					offsetHeight = 0,
					property,
					props = {
						"height": 0,
						"margin-top": 0,
						"margin-bottom": 0,
						"padding-top": 0,
						"padding-bottom": 0,
						"border-top-width": 0,
						"border-bottom-width": 0,
						"box-sizing": ""
					};
				if (element) {
					style = element.style;

					if (style.display !== "none") {
						extractCSSProperties(element, props, pseudoSelector, true);
						offsetHeight = element.offsetHeight;
					} else if (force) {
						originalDisplay = style.display;
						originalVisibility = style.visibility;
						originalPosition = style.position;

						style.display = "block";
						style.visibility = "hidden";
						style.position = "relative";

						extractCSSProperties(element, props, pseudoSelector, true);
						offsetHeight = element.offsetHeight;

						style.display = originalDisplay;
						style.visibility = originalVisibility;
						style.position = originalPosition;
					}

					// We are extracting raw values to be able to check the units
					if(typeof props["height"] === "string" && props["height"].indexOf("px") === -1){
						//ignore non px values such as auto or %
						props["height"] = 0;
					}

					for (property in props) {
						if (props.hasOwnProperty(property) && property !== "box-sizing"){
							value = parseFloat(props[property]);
							if (isNaN(value)) {
								value = 0;
							}
							props[property] = value;
						}
					}

					height += props["height"];

					if (props["box-sizing"] !== 'border-box') {
						height += props["padding-top"] + props["padding-bottom"];
					}

					if (includeOffset) {
						height = offsetHeight;
					} else if (outer && props["box-sizing"] !== 'border-box') {
						height += props["border-top-width"] + props["border-bottom-width"];
					}

					if (includeMargin) {
						height += Math.max(0, props["margin-top"]) + Math.max(0, props["margin-bottom"]);
					}
				}
				return height;
			}

			/**
			 * Returns elements width from computed style
			 * @method getElementWidth
			 * @param {HTMLElement} element
			 * if null then the "inner" value is assigned
			 * @param {"outer"|null} [type=null]
			 * @param {boolean} [includeOffset=false]
			 * @param {boolean} [includeMargin=false]
			 * @param {?string} [pseudoSelector=null]
			 * @param {boolean} [force=false] check even if element is hidden
			 * @return {number}
			 * @member ns.util.DOM
			 * @static
			 */
			function getElementWidth(element, type, includeOffset, includeMargin, pseudoSelector, force) {
				var width = 0,
					style,
					value,
					originalDisplay = null,
					originalVisibility = null,
					originalPosition = null,
					offsetWidth = 0,
					property,
					outer = (type && type === "outer") || false,
					props = {
						"width": 0,
						"margin-left": 0,
						"margin-right": 0,
						"padding-left": 0,
						"padding-right": 0,
						"border-left-width": 0,
						"border-right-width": 0,
						"box-sizing": ""
					};

				if (element) {
					style = element.style;

					if (style.display !== "none") {
						extractCSSProperties(element, props, pseudoSelector, true);
						offsetWidth = element.offsetWidth;
					} else if (force) {
						originalDisplay = style.display;
						originalVisibility = style.visibility;
						originalPosition = style.position;

						style.display = "block";
						style.visibility = "hidden";
						style.position = "relative";

						extractCSSProperties(element, props, pseudoSelector, true);

						style.display = originalDisplay;
						style.visibility = originalVisibility;
						style.position = originalPosition;
					}

					if(typeof props["width"] === 'string' && props["width"].indexOf("px") === -1) {
						//ignore non px values such as auto or %
						props["width"] = 0;
					}
					for (property in props) {
						if (props.hasOwnProperty(property) && property !== "box-sizing"){
							value = parseFloat(props[property]);
							if (isNaN(value)) {
								value = 0;
							}
							props[property] = value;
						}
					}

					width += props["width"];
					if (props["box-sizing"] !== 'border-box') {
						width += props["padding-left"] + props["padding-right"];
					}

					if (includeOffset) {
						width = offsetWidth;
					} else if (outer && props["box-sizing"] !== 'border-box') {
						width += props["border-left-width"] + props["border-right-width"];
					}

					if (includeMargin) {
						width += Math.max(0, props["margin-left"]) + Math.max(0, props["margin-right"]);
					}
				}
				return width;
			}

			/**
			 * Returns offset of element
			 * @method getElementOffset
			 * @param {HTMLElement} element
			 * @return {Object}
			 * @member ns.util.DOM
			 * @static
			 */
			function getElementOffset(element) {
				var left = 0,
					top = 0;
				do {
					top += element.offsetTop;
					left += element.offsetLeft;
					element = element.offsetParent;
				} while (element !== null);

				return {
					top: top,
					left: left
				};
			}

			/**
			 * Check if element occupies place at view
			 * @method isOccupiedPlace
			 * @param {HTMLElement} element
			 * @return {boolean}
			 * @member ns.util.DOM
			 * @static
			 */
			function isOccupiedPlace(element) {
				return !(element.offsetWidth <= 0 && element.offsetHeight <= 0);
			}

			function toUpperCaseFn(match, value) {
				return value.toLocaleUpperCase();
			}

			function dashesToCamelCase(str) {
				return str.replace(DASH_TO_UPPER_CASE_REGEXP, toUpperCaseFn);
			}

			function firstToUpperCase(str) {
				return str.charAt(0).toLocaleUpperCase() + str.substring(1);
			}

			/**
			 * Set values for element with prefixes for browsers
			 * @method setPrefixedStyle
			 * @param {HTMLElement} element
			 * @param {string} property
			 * @param {string|Object|null} value
			 * @member ns.util.DOM
			 * @static
			 */
			function setPrefixedStyle(element, property, value) {
				var style = element.style,
					propertyForPrefix = firstToUpperCase(dashesToCamelCase(property)),
					values = (typeof value === "string") ? {
						webkit: value,
						moz: value,
						o: value,
						ms: value,
						normal: value
					} : value;

				style[property] = values.normal;
				style["webkit" + propertyForPrefix] = values.webkit;
				style["moz" + propertyForPrefix] = values.moz;
				style["o" + propertyForPrefix] = values.o;
				style["ms" + propertyForPrefix] = values.ms;
			}

			/**
			 * Get value from element with prefixes for browsers
			 * @method getCSSProperty
			 * @param {string} value
			 * @return {Object}
			 * @member ns.util.DOM
			 * @static
			 */
			function getPrefixedValue(value) {
				return {
					webkit: "-webkit-" + value,
					moz: "-moz-" + value,
					o: "-ms-" + value,
					ms: "-o-" + value,
					normal: value
				};
			}

			/**
			 * Returns style value for css property with browsers prefixes
			 * @method getPrefixedStyle
			 * @param {HTMLStyle} styles
			 * @param {string} property
			 * @return {Object}
			 * @member ns.util.DOM
			 * @static
			 */
			function getPrefixedStyleValue(styles, property) {
				var prefixedProperties = getPrefixedValue(property),
					value,
					key;

				for (key in prefixedProperties) {
					value = styles[prefixedProperties[key]];
					if (value && value !== "none") {
						return value;
					}
				}
				return value;
			}


			// assign methods to namespace
			DOM.getCSSProperty = getCSSProperty;
			DOM.extractCSSProperties = extractCSSProperties;
			DOM.getElementHeight = getElementHeight;
			DOM.getElementWidth = getElementWidth;
			DOM.getElementOffset = getElementOffset;
			DOM.isOccupiedPlace = isOccupiedPlace;
			DOM.setPrefixedStyle = setPrefixedStyle;
			DOM.getPrefixedValue = getPrefixedValue;
			DOM.getPrefixedStyleValue = getPrefixedStyleValue;

			}(window, window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/**
 * # Page Widget
 * Page is main element of application's structure.
 *
 * ## Default selectors
 * In the Tizen Web UI framework the application page structure is based on a header, content and footer elements:
 *
 * - **The header** is placed at the top, and displays the page title and optionally buttons.
 * - **The content** is the section below the header, showing the main content of the page.
 * - **The footer** is a bottom part of page which can display for example buttons
 *
 * The following table describes the specific information for each section.
 *
 * <table>
 *     <tr>
 *         <th>Section</th>
 *         <th>Class</th>
 *         <th>Mandatory</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *         <td rowspan="2">Page</td>
 *         <td>ui-page</td>
 *         <td>Yes</td>
 *         <td>Defines the element as a page.
 *
 * The page widget is used to manage a single item in a page-based architecture.
 *
 * A page is composed of header (optional), content (mandatory), and footer (optional) elements.</td>
 *      </tr>
 *      <tr>
 *          <td>ui-page-active</td>
 *          <td>No</td>
 *          <td>If an application has a static start page, insert the ui-page-active class in the page element to speed up the application launch. The start page with the ui-page-active class can be displayed before the framework is fully loaded.
 *
 *If this class is not used, the framework inserts the class automatically to the first page of the application. However, this has a slowing effect on the application launch, because the page is displayed only after the framework is fully loaded.</td>
 *      </tr>
 *      <tr>
 *          <td>Header</td>
 *          <td>ui-header</td>
 *          <td>No</td>
 *          <td>Defines the element as a header.</td>
 *      </tr>
 *      <tr>
 *          <td>Content</td>
 *          <td>ui-content</td>
 *          <td>Yes</td>
 *          <td>Defines the element as content.</td>
 *      </tr>
 *      <tr>
 *          <td>Footer</td>
 *          <td>ui-footer</td>
 *          <td>No</td>
 *          <td>Defines the element as a footer.
 *
 * The footer section is mostly used to include option buttons.</td>
 *      </tr>
 *  </table>
 *
 * All elements with class=ui-page will be become page widgets
 *
 *      @example
 *         <!--Page layout-->
 *         <div class="ui-page ui-page-active">
 *             <header class="ui-header"></header>
 *             <div class="ui-content"></div>
 *             <footer class="ui-footer"></footer>
 *         </div>
 *
 *         <!--Page layout with more button in header-->
 *         <div class="ui-page ui-page-active">
 *             <header class="ui-header ui-has-more">
 *                 <h2 class="ui-title">Call menu</h2>
 *                 <button type="button" class="ui-more ui-icon-overflow">More Options</button>
 *             </header>
 *             <div class="ui-content">Content message</div>
 *             <footer class="ui-footer">
 *                 <button type="button" class="ui-btn">Footer Button</button>
 *             </footer>
 *         </div>
 *
 * ## Manual constructor
 * For manual creation of page widget you can use constructor of widget from **tau** namespace:
 *
 *		@example
 *		var pageElement = document.getElementById("page"),
 *			page = tau.widget.page(buttonElement);
 *
 * Constructor has one require parameter **element** which are base **HTMLElement** to create widget. We recommend get this element by method *document.getElementById*.
 *
 * ## Multi-page Layout
 *
 * You can implement a template containing multiple page containers in the application index.html file.
 *
 * In the multi-page layout, the main page is defined with the ui-page-active class. If no page has the ui-page-active class, the framework automatically sets up the first page in the source order as the main page. You can improve the launch performance by explicitly defining the main page to be displayed first. If the application has to wait for the framework to set up the main page, the page is displayed with some delay only after the framework is fully loaded.
 *
 * You can link to internal pages by referring to the ID of the page. For example, to link to the page with an ID of two, the link element needs the href="#two" attribute in the code, as in the following example.
 *
 *      @example
 *         <!--Main page-->
 *         <div id="one" class="ui-page ui-page-active">
 *             <header class="ui-header"></header>
 *             <div class="ui-content"></div>
 *             <footer class="ui-footer"></footer>
 *         </div>
 *
 *         <!--Secondary page-->
 *         <div id="two" class="ui-page">
 *             <header class="ui-header"></header>
 *             <div class="ui-content"></div>
 *             <footer class="ui-footer"></footer>
 *         </div>
 *
 * To find the currently active page, use the ui-page-active class.
 *
 * ## Changing Pages
 * ### Go to page in JavaScript
 * To change page use method *tau.changePage*
 *
 *      @example
 *      tau.changePage("page-two");
 *
 * ### Back in JavaScript
 * To back to previous page use method *tau.back*
 *
 *      @example
 *      tau.back();
 *
 * ## Transitions
 *
 * When changing the active page, you can use a page transition.
 *
 * Tizen Web UI Framework does not apply transitions by default. To set a custom transition effect, you must add the data-transition attribute to a link:
 *
 *      @example
 *      <a href="index.html" data-transition="slideup">I'll slide up</a>
 *
 * To set a default custom transition effect for all pages, use the pageTransition property:
 *
 *      @example
 *      tau.defaults.pageTransition = "slideup";
 *
 * ### Transitions list
 *
 *  - **none** no transition.
 *  - **slideup** Makes the content of the next page slide up, appearing to conceal the content of the previous page.
 *
 * ## Handling Page Events
 *
 * With page widget we have connected many of events.
 *
 * To handle page events, use the following code:
 *
 *      @example
 *        <div id="page" class="ui-page">
 *             <header class="ui-header"></header>
 *             <div class="ui-content"></div>
 *         </div>
 *
 *         <script>
 *             var page = document.getElementById("page");
 *             page.addEventListener("Event", function(event) {
 *                 // Your code
 *             });
 *         </script>
 *
 * To bind an event callback on the Back key, use the following code:
 *
 * Full list of available events is in [events list section](#events-list).
 *
 * To bind an event callback on the Back key, use the following code:
 *
 *      @example
 *         <script>
 *             window.addEventListener("tizenhwkey", function (event) {
 *                 if (event.keyName == "back") {
 *                     // Call window.history.back() to go to previous browser window
 *                     // Call tizen.application.getCurrentApplication().exit() to exit application
 *                     // Add script to add another behavior
 *                 }
 *             });
 *         </script>
 *
 * ## Options for Page Widget
 *
 * Page widget hasn't any options.
 *
 * ## Methods
 *
 * To call method on widget you can use tau API:
 *
 *		@example
 *		var pageElement = document.getElementById("page"),
 *			page = tau.widget.page(buttonElement);
 *
 *		page.methodName(methodArgument1, methodArgument2, ...);
 *
 * @class ns.widget.core.Page
 * @extends ns.widget.BaseWidget
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 * @author Damian Osipiuk <d.osipiuk@samsung.com>
 */
(function (document, ns) {
	
				/**
			 * Alias for {@link ns.widget.BaseWidget}
			 * @property {Object} BaseWidget
			 * @member ns.widget.core.Page
			 * @private
			 * @static
			 */
			var BaseWidget = ns.widget.BaseWidget,
				/**
				 * Alias for {@link ns.util}
				 * @property {Object} util
				 * @member ns.widget.core.Page
				 * @private
				 * @static
				 */
				util = ns.util,
				/**
				 * Alias for {@link ns.util.DOM}
				 * @property {Object} doms
				 * @member ns.widget.core.Page
				 * @private
				 * @static
				 */
				doms = util.DOM,
				/**
				 * Alias for {@link ns.util.selectors}
				 * @property {Object} utilSelectors
				 * @member ns.widget.core.Page
				 * @private
				 * @static
				 */
				utilSelectors = util.selectors,
				/**
				 * Alias for {@link ns.engine}
				 * @property {Object} engine
				 * @member ns.widget.core.Page
				 * @private
				 * @static
				 */
				engine = ns.engine,

				Page = function () {
					var self = this;
					/**
					 * Callback on resize
					 * @property {?Function} contentFillAfterResizeCallback
					 * @private
					 * @member ns.widget.core.Page
					 */
					self.contentFillAfterResizeCallback = null;
					self._initialContentStyle = {};
					/**
					 * Options for widget.
					 * It is empty object, because widget Page does not have any options.
					 * @property {Object} options
					 * @member ns.widget.core.Page
					 */
					self.options = {};

					self._contentStyleAttributes = ["height", "width", "minHeight", "marginTop", "marginBottom"];

					self._ui = {};
				},
				/**
				 * Dictionary for page related event types
				 * @property {Object} EventType
				 * @member ns.widget.core.Page
				 * @static
				 */
				EventType = {
					/**
					 * Triggered on the page we are transitioning to,
					 * after the transition animation has completed.
					 * @event pageshow
					 * @member ns.widget.core.Page
					 */
					SHOW: "pageshow",
					/**
					 * Triggered on the page we are transitioning away from,
					 * after the transition animation has completed.
					 * @event pagehide
					 * @member ns.widget.core.Page
					 */
					HIDE: "pagehide",
					/**
					 * Triggered when the page has been created in the DOM
					 * (for example, through Ajax) but before all widgets
					 * have had an opportunity to enhance the contained markup.
					 * @event pagecreate
					 * @member ns.widget.core.Page
					 */
					CREATE: "pagecreate",
					/**
					 * Triggered when the page is being initialized,
					 * before most plugin auto-initialization occurs.
					 * @event pagebeforecreate
					 * @member ns.widget.core.Page
					 */
					BEFORE_CREATE: "pagebeforecreate",
					/**
					 * Triggered on the page we are transitioning to,
					 * before the actual transition animation is kicked off.
					 * @event pagebeforeshow
					 * @member ns.widget.core.Page
					 */
					BEFORE_SHOW: "pagebeforeshow",
					/**
					 * Triggered on the page we are transitioning away from,
					 * before the actual transition animation is kicked off.
					 * @event pagebeforehide
					 * @member ns.widget.core.Page
					 */
					BEFORE_HIDE: "pagebeforehide"
				},
				/**
				 * Dictionary for page related css class names
				 * @property {Object} classes
				 * @member ns.widget.core.Page
				 * @static
				 * @readonly
				 */
				classes = {
					uiPage: "ui-page",
					uiPageActive: "ui-page-active",
					uiSection: "ui-section",
					uiHeader: "ui-header",
					uiFooter: "ui-footer",
					uiContent: "ui-content"
				},

				prototype = new BaseWidget();

			Page.classes = classes;
			Page.events = EventType;

			/**
			 * Configure default options for widget
			 * @method _configure
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._configure = function() {
				var options = this.options || {};
				/**
				 * Object with default options
				 * @property {Object} options
				 * @property {boolean|string|null} [options.header=false] Sets content of header.
				 * @property {boolean|string|null} [options.footer=false] Sets content of footer.
				 * @property {string} [options.content=null] Sets content of popup.
				 * @member ns.widget.core.Page
				 * @static
				 */
				options.header = null;
				options.footer = null;
				options.content = null;
				this.options = options;
			};

			/**
			 * Sets top-bottom css attributes for content element
			 * to allow it to fill the page dynamically
			 * @method _contentFill
			 * @member ns.widget.core.Page
			 */
			prototype._contentFill = function () {
				var self = this,
					element = self.element,
					screenWidth = window.innerWidth,
					screenHeight = window.innerHeight,
					contentSelector = classes.uiContent,
					headerSelector = classes.uiHeader,
					footerSelector = classes.uiFooter,
					extraHeight = 0,
					children = [].slice.call(element.children),
					childrenLength = children.length,
					elementStyle = element.style,
					i,
					node,
					contentStyle,
					marginTop,
					marginBottom,
					nodeStyle;

				elementStyle.width = screenWidth + "px";
				elementStyle.height = screenHeight + "px";

				for (i = 0; i < childrenLength; i++) {
					node = children[i];
					if (node.classList.contains(headerSelector) ||
						node.classList.contains(footerSelector)) {
						extraHeight += doms.getElementHeight(node);
					}
				}
				for (i = 0; i < childrenLength; i++) {
					node = children[i];
					nodeStyle = node.style;
					if (node.classList.contains(contentSelector)) {
						contentStyle = window.getComputedStyle(node);
						marginTop = parseFloat(contentStyle.marginTop);
						marginBottom = parseFloat(contentStyle.marginBottom);
						nodeStyle.height = (screenHeight - extraHeight - marginTop - marginBottom) + "px";
						nodeStyle.width = screenWidth + "px";
					}
				}
			};

			prototype._storeContentStyle = function () {
				var initialContentStyle = this._initialContentStyle,
					contentStyleAttributes = this._contentStyleAttributes,
					content = this.element.querySelector("." + classes.uiContent),
					contentStyle = content ? content.style : {};

				contentStyleAttributes.forEach(function(name) {
					initialContentStyle[name] = contentStyle[name];
				});
			};

			prototype._restoreContentStyle = function () {
				var initialContentStyle = this._initialContentStyle,
					contentStyleAttributes = this._contentStyleAttributes,
					content = this.element.querySelector("." + classes.uiContent),
					contentStyle = content ? content.style : {};

				contentStyleAttributes.forEach(function(name) {
					contentStyle[name] = initialContentStyle[name];
				});
			};

			/**
			 * Setter for footer option
			 * @method _setFooter
			 * @param {HTMLElement} element
			 * @param {string} value
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._setFooter = function(element, value) {
				var self = this,
					ui = self._ui,
					footer = ui.footer;

				// footer element if footer does not exist and value is true or string
				if (!footer && value) {
					footer = document.createElement("footer");
					element.appendChild(footer);
					ui.footer = footer;
				}
				if (footer) {
					// remove child if footer does not exist and value is set to false
					if (value === false) {
						element.removeChild(footer);
					} else {
						// if options is set to true, to string or not is set
						// add class
						footer.classList.add(classes.uiFooter);
						// if is string fill content by string value
						if (typeof value === "string") {
							ui.footer.textContent = value;
						}
					}
					// and remember options
					self.options.footer = value;
				}
			};

			/**
			 * Setter for header option
			 * @method _setHeader
			 * @param {HTMLElement} element
			 * @param {string} value
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._setHeader = function(element, value) {
				var self = this,
					ui = self._ui,
					header = ui.header;

				// header element if header does not exist and value is true or string
				if (!header && value) {
					header = document.createElement("header");
					element.appendChild(header);
					ui.header = header;
				}
				if (header) {
					// remove child if header does not exist and value is set to false
					if (value === false) {
						element.removeChild(header);
					} else {
						// if options is set to true, to string or not is set
						// add class
						header.classList.add(classes.uiHeader);
						// if is string fill content by string value
						if (typeof value === "string") {
							ui.header.textContent = value;
						}
					}
					// and remember options
					self.options.header = value;
				}
			};

			/**
			 * Setter for content option
			 * @method _setContent
			 * @param {HTMLElement} element
			 * @param {string} value
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._setContent = function(element, value) {
				if (typeof value === "string") {
					this.options.content =
						this._ui.content.textContent = value;
				}
			};

			/**
			 * Method creates empty page header. It also checks for additional
			 * content to be added in header.
			 * @method _buildHeader
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._buildHeader = function(element) {
				var self = this;
				self._ui.header = utilSelectors.getChildrenBySelector(element, "header,[data-role='header'],." + classes.uiHeader)[0];
				self._setHeader(element, self.options.header);
			};

			/**
			 * Method creates empty page footer.
			 * @method _buildFooter
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._buildFooter = function(element) {
				var self = this;

				self._ui.footer = utilSelectors.getChildrenBySelector(element, "footer,[data-role='footer'],." + classes.uiFooter)[0];
				self._setFooter(element, self.options.footer);
			};

			/**
			 * Method creates empty page content.
			 * @method _buildContent
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._buildContent = function(element) {
				var self = this,
					content = utilSelectors.getChildrenBySelector(element, "[data-role='content'],." + classes.uiContent)[0],
					next,
					child = element.firstChild,
					ui = self._ui;
				// content must always exists
				if (!content) {
					content = document.createElement("div");
					while (child) {
						next = child.nextSibling;
						if (child !== ui.footer && child !== ui.header) {
							content.appendChild(child);
						}
						child = next;
					}
				}

				// we put it before footer or if footer not exists as last child of element
				element.insertBefore(content, ui.footer);
				content.classList.add(classes.uiContent);
				ui.content = content;
				// we set content text if is set in options.content
				self._setContent(element, self.options.content);
			};

			/**
			 * Build page
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._build = function (element) {
				var self = this;
				element.classList.add(classes.uiPage);
				self._buildHeader(element);
				self._buildFooter(element);
				self._buildContent(element);
				return element;
			};

			/**
			 * This method sets page active or inactive.
			 * @method setActive
			 * @param {boolean} value If true, then page will be active. Otherwise, page will be inactive.
			 * @member ns.widget.core.Page
			 */
			prototype.setActive = function (value) {
				var elementClassList = this.element.classList;
				if (value) {
					this.focus();
					elementClassList.add(classes.uiPageActive);
				} else {
					this.blur();
					elementClassList.remove(classes.uiPageActive);
				}
			};

			/**
			 * Return current status of page.
			 * @method isActive
			 * @member ns.widget.core.Page
			 * @instance
			 */
			prototype.isActive = function () {
				return this.element.classList.contains(classes.uiPageActive);
			};

			/**
			 * Sets the focus to page
			 * @method focus
			 * @member ns.widget.core.Page
			 */
			prototype.focus = function () {
				var element = this.element,
					focusable = element.querySelector("[autofocus]") || element;
				focusable.focus();
			};

			/**
			 * Removes focus from page and all descendants
			 * @method blur
			 * @member ns.widget.core.Page
			 */
			prototype.blur = function () {
				var element = this.element,
					focusable = element.querySelector(":focus") || element;
				focusable.blur();
			};

			/**
			 * Bind events to widget
			 * @method _bindEvents
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._bindEvents = function (element) {
				var self = this;
				self.contentFillAfterResizeCallback = self._contentFill.bind(self);
				window.addEventListener("resize", self.contentFillAfterResizeCallback, false);
			};

			/**
			 * Refresh widget structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._refresh = function () {
				this._restoreContentStyle();
				this._contentFill();
			};

			/**
			 * Layouting page structure
			 * @method layout
			 * @member ns.widget.core.Page
			 */
			prototype.layout = function () {
				this._storeContentStyle();
				this._contentFill();
			};

			/**
			 * This method triggers BEFORE_SHOW event.
			 * @method onBeforeShow
			 * @member ns.widget.core.Page
			 */
			prototype.onBeforeShow = function () {
				this.trigger(EventType.BEFORE_SHOW);
			};

			/**
			 * This method triggers SHOW event.
			 * @method onShow
			 * @member ns.widget.core.Page
			 */
			prototype.onShow = function () {
								this.trigger(EventType.SHOW);
			};

			/**
			 * This method triggers BEFORE_HIDE event.
			 * @method onBeforeHide
			 * @member ns.widget.core.Page
			 */
			prototype.onBeforeHide = function () {
				this.trigger(EventType.BEFORE_HIDE);
			};

			/**
			 * This method triggers HIDE event.
			 * @method onHide
			 * @member ns.widget.core.Page
			 */
			prototype.onHide = function () {
				this._restoreContentStyle();
				this.trigger(EventType.HIDE);
			};

			/**
			 * Destroy widget
			 * @method _destroy
			 * @protected
			 * @member ns.widget.core.Page
			 */
			prototype._destroy = function () {
				var self = this,
					element = self.element;

				element = element || self.element;
				
				window.removeEventListener("resize", self.contentFillAfterResizeCallback, false);
				// destroy widgets on children
				engine.destroyAllWidgets(element, true);
			};

			Page.prototype = prototype;

			Page.createEmptyElement = function() {
				var div = document.createElement("div");
				div.classList.add(classes.uiPage);
				doms.setNSData(div, "role", "page");
				return div;
			};

			// definition
			ns.widget.core.Page = Page;
			engine.defineWidget(
				"Page",
				"[data-role=page],.ui-page",
				[
					"layout",
					"focus",
					"blur",
					"setActive",
					"isActive"
				],
				Page,
				"core"
			);

			engine.defineWidget(
				"page",
				"",
				[
					"layout",
					"focus",
					"blur",
					"setActive",
					"isActive"
				],
				Page,
				"core"
			);

			// @remove
			// THIS IS ONLY FOR COMPATIBILITY
			ns.widget.page = ns.widget.Page;

			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * # PageContainer Widget
 * PageContainer is a widget, which is supposed to have multiple child pages but display only one at a time.
 *
 * It allows for adding new pages, switching between them and displaying progress bars indicating loading process.
 *
 * @class ns.widget.core.PageContainer
 * @extends ns.widget.BaseWidget
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 * @author Krzysztof Głodowski <k.glodowski@samsung.com>
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				Page = ns.widget.core.Page,
				util = ns.util,
				eventUtils = ns.event,
				DOM = util.DOM,
				engine = ns.engine,
				classes = {
					pageContainer: "ui-page-container",
					uiViewportTransitioning: "ui-viewport-transitioning",
					out: "out",
					in: "in",
					reverse: "reverse",
					uiPreIn: "ui-pre-in",
					uiBuild: "ui-page-build"
				},
				PageContainer = function () {
					/**
					 * Active page.
					 * @property {ns.widget.core.Page} [activePage]
					 * @member ns.widget.core.PageContainer
					 */
					this.activePage = null;
				},
				EventType = {
					/**
					 * Triggered before the changePage() request
					 * has started loading the page into the DOM.
					 * @event pagebeforechange
					 * @member ns.widget.core.PageContainer
					 */
					PAGE_BEFORE_CHANGE: "pagebeforechange",
					/**
					 * Triggered after the changePage() request
					 * has finished loading the page into the DOM and
					 * all page transition animations have completed.
					 * @event pagechange
					 * @member ns.widget.core.PageContainer
					 */
					PAGE_CHANGE: "pagechange"
				},
				animationend = "animationend",
				webkitAnimationEnd = "webkitAnimationEnd",
				mozAnimationEnd = "mozAnimationEnd",
				msAnimationEnd = "msAnimationEnd",
				oAnimationEnd = "oAnimationEnd",
				prototype = new BaseWidget();

			/**
			 * Dictionary for PageContainer related event types.
			 * @property {Object} events
			 * @property {string} [events.PAGE_CHANGE="pagechange"]
			 * @member ns.router.route.popup
			 * @static
			 */
			PageContainer.events = EventType;

			/**
			 * Dictionary for PageContainer related css class names
			 * @property {Object} classes
			 * @member ns.widget.core.Page
			 * @static
			 * @readonly
			 */
			PageContainer.classes = classes;

			/**
			 * This method changes active page to specified element.
			 * @method change
			 * @param {HTMLElement} toPage The element to set
			 * @param {Object} [options] Additional options for the transition
			 * @param {string} [options.transition=none] Specifies the type of transition
			 * @param {boolean} [options.reverse=false] Specifies the direction of transition
			 * @member ns.widget.core.PageContainer
			 */
			prototype.change = function (toPage, options) {
				var self = this,
					fromPageWidget = self.getActivePage(),
					toPageWidget;

				options = options || {};
				options.widget = options.widget || "Page";

				// The change should be made only if no active page exists
				// or active page is changed to another one.
				if (!fromPageWidget || (fromPageWidget.element !== toPage)) {
					if (toPage.parentNode !== self.element) {
						toPage = self._include(toPage);
					}

					self.trigger(EventType.PAGE_BEFORE_CHANGE);

					toPage.classList.add(classes.uiBuild);

					toPageWidget = engine.instanceWidget(toPage, options.widget);

					// set sizes of page for correct display
					toPageWidget.layout();

					if (ns.getConfig("autoBuildOnPageChange", false)) {
						engine.createWidgets(toPage);
					}

					if (fromPageWidget) {
						fromPageWidget.onBeforeHide();
					}
					toPageWidget.onBeforeShow();

					toPage.classList.remove(classes.uiBuild);

					options.deferred = {
						resolve: function () {
							if (fromPageWidget) {
								fromPageWidget.onHide();
								self._removeExternalPage(fromPageWidget, options);
							}
							toPageWidget.onShow();
														self.trigger(EventType.PAGE_CHANGE);
													}
					};
					self._transition(toPageWidget, fromPageWidget, options);
				}
			};

			/**
			 * This method performs transition between the old and a new page.
			 * @method _transition
			 * @param {ns.widget.core.Page} toPageWidget The new page
			 * @param {ns.widget.core.Page} fromPageWidget The page to be replaced
			 * @param {Object} [options] Additional options for the transition
			 * @param {string} [options.transition=none] The type of transition
			 * @param {boolean} [options.reverse=false] Specifies transition direction
			 * @param {Object} [options.deferred] Deferred object
			 * @member ns.widget.core.PageContainer
			 * @protected
			 */
			prototype._transition = function (toPageWidget, fromPageWidget, options) {
				var self = this,
					element = self.element,
					elementClassList = element.classList,
					transition = !fromPageWidget || !options.transition ? "none" : options.transition,
					deferred = options.deferred,
					clearClasses = [classes.in, classes.out, classes.uiPreIn, transition],
					oldDeferredResolve,
					classlist,
					oneEvent;

				if (options.reverse) {
					clearClasses.push(classes.reverse);
				}
				elementClassList.add(classes.uiViewportTransitioning);
				oldDeferredResolve = deferred.resolve;
				deferred.resolve = function () {
					var fromPageWidgetClassList = fromPageWidget && fromPageWidget.element.classList,
						toPageWidgetClassList = toPageWidget.element.classList;

					self._setActivePage(toPageWidget);

					elementClassList.remove(classes.uiViewportTransitioning);
					clearClasses.forEach(function (className) {
						toPageWidgetClassList.remove(className);
					});
					if (fromPageWidgetClassList) {
						clearClasses.forEach(function (className) {
							fromPageWidgetClassList.remove(className);
						});
					}
					oldDeferredResolve();
				};

				if (transition !== "none") {
					oneEvent = function () {
						eventUtils.off(
							toPageWidget.element,
							[
								animationend,
								webkitAnimationEnd,
								mozAnimationEnd,
								msAnimationEnd,
								oAnimationEnd
							],
							oneEvent,
							false
						);
						deferred.resolve();
					};
					eventUtils.on(
						toPageWidget.element,
						[
							animationend,
							webkitAnimationEnd,
							mozAnimationEnd,
							msAnimationEnd,
							oAnimationEnd
						],
						oneEvent,
						false
					);

					if (fromPageWidget) {
						classlist = fromPageWidget.element.classList;
						classlist.add(transition);
						classlist.add(classes.out);
						if (options.reverse) {
							classlist.add(classes.reverse);
						}
					}

					classlist = toPageWidget.element.classList;
					classlist.add(transition);
					classlist.add(classes.in);
					classlist.add(classes.uiPreIn);
					if (options.reverse) {
						classlist.add(classes.reverse);
					}
				} else {
					window.setTimeout(deferred.resolve, 0);
				}
			};
			/**
			 * This method adds an element as a page.
			 * @method _include
			 * @param {HTMLElement} page an element to add
			 * @return {HTMLElement}
			 * @member ns.widget.core.PageContainer
			 * @protected
			 */
			prototype._include = function (page) {
				var element = this.element;
				if (page.parentNode !== element) {
					page = util.importEvaluateAndAppendElement(page, element);
				}
				return page;
			};
			/**
			 * This method sets currently active page.
			 * @method _setActivePage
			 * @param {ns.widget.core.Page} page a widget to set as the active page
			 * @member ns.widget.core.PageContainer
			 * @protected
			 */
			prototype._setActivePage = function (page) {
				var self = this;
				if (self.activePage) {
					self.activePage.setActive(false);
				}
				self.activePage = page;
				page.setActive(true);
			};
			/**
			 * This method returns active page widget.
			 * @method getActivePage
			 * @member ns.widget.core.PageContainer
			 * @return {ns.widget.core.Page} Currently active page
			 */
			prototype.getActivePage = function () {
				return this.activePage;
			};

			/**
			 * This method displays a progress bar indicating loading process.
			 * @method showLoading
			 * @member ns.widget.core.PageContainer
			 * @return {null}
			 */
			prototype.showLoading = function () {
								return null;
			};
			/**
			 * This method hides any active progress bar.
			 * @method hideLoading
			 * @member ns.widget.core.PageContainer
			 * @return {null}
			 */
			prototype.hideLoading = function () {
								return null;
			};
			/**
			 * This method removes page element from the given widget and destroys it.
			 * @method _removeExternalPage
			 * @param {ns.widget.core.Page} fromPageWidget the widget to destroy
			 * @param {Object} [options] transition options
			 * @param {boolean} [options.reverse=false] specifies transition direction
			 * @member ns.widget.core.PageContainer
			 * @protected
			 */
			prototype._removeExternalPage = function ( fromPageWidget, options) {
				var fromPage = fromPageWidget.element;
				options = options || {};
				if (options.reverse && DOM.hasNSData(fromPage, "external")) {
					fromPageWidget.destroy();
					if (fromPage.parentNode) {
						fromPage.parentNode.removeChild(fromPage);
					}
				}
			};

			PageContainer.prototype = prototype;

			// definition
			ns.widget.core.PageContainer = PageContainer;

			engine.defineWidget(
				"pagecontainer",
				"",
				["change", "getActivePage", "showLoading", "hideLoading"],
				PageContainer,
				"core"
			);
			}(window.document, ns));

/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,
				Button = function () {
					var self = this;
					self.options = {};
				},
				classes = {
					BTN: "ui-btn",
					DISABLED: "ui-state-disabled",
					INLINE: "ui-inline",
					BTN_ICON: "ui-btn-icon",
					ICON_PREFIX: "ui-icon-",
					BTN_CIRCLE: "ui-btn-circle",
					BTN_NOBG: "ui-btn-nobg",
					BTN_ICON_ONLY: "ui-btn-icon-only",
					BTN_ICON_POSITION_PREFIX: "ui-btn-icon-",
					MULTILINE: "ui-multiline"
				},
				buttonStyle = {
					CIRCLE: "circle",
					NOBG: "nobg"
				},

				prototype = new BaseWidget();

			Button.classes = classes;
			Button.prototype = prototype;

			/**
			 * Configure button
			 * @method _configre
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._configure = function (element) {
				this.options = {
					// common options
					inline: false,
					icon: null,
					disabled: false,
					// mobile options
					style: null,
					iconpos: "left",
					// wearable options
					multiline: false,
				};
			};

			/**
			 * Set style option
			 * @method _setStyle
			 * @param {HTMLElement} element
			 * @param {string} style
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._setStyle = function (element, style) {
				var options = this.options,
					buttonClassList = element.classList,
					innerTextLength = element.textContent.length || (element.value ? element.value.length : 0);

				style = style || options.style;

				switch (style) {
					case buttonStyle.CIRCLE:
						if (innerTextLength == 0) {
							buttonClassList.remove(classes.BTN_NOBG);
							buttonClassList.add(classes.BTN_CIRCLE);
						}
						break;
					case buttonStyle.NOBG:
						if (innerTextLength == 0) {
							buttonClassList.remove(classes.BTN_CIRCLE);
							buttonClassList.add(classes.BTN_NOBG);
						}
						break;
					default:
				}
			};

			/**
			 * Set multiline option
			 * @method _setMultiline
			 * @param {HTMLElement} element
			 * @param {boolean} multiline
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._setMultiline = function (element, multiline) {
				var options = this.options;

				multiline = multiline || options.multiline;

				if (multiline) {
					element.classList.add(classes.MULTILINE);
				}
			};

			/**
			 * Set inline option
			 * @method _setInline
			 * @param {HTMLElement} element
			 * @param {boolean} inline
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._setInline = function (element, inline) {
				var options = this.options;

				inline = inline || options.inline;

				if (inline) {
					element.classList.add(classes.INLINE);
				}
			};

			/**
			 * Set icon option
			 * @method _setIcon
			 * @param {HTMLElement} element
			 * @param {string} icon
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._setIcon = function (element, icon) {
				var self = this,
					options = self.options;

				icon = icon || options.icon;

				if (icon) {
					element.classList.add(classes.BTN_ICON);
					element.classList.add(classes.ICON_PREFIX + icon);
					self._setTitleForIcon(element);
				}
			};

			/**
			 * Set iconpos option
			 * @method _setIconpos
			 * @param {HTMLElement} element
			 * @param {string} iconpos
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._setIconpos = function (element, iconpos) {
				var options = this.options,
					innerTextLength = element.textContent.length || (element.value ? element.value.length : 0);

				iconpos = iconpos || options.iconpos;

				if (options.icon) {
					if (innerTextLength > 0) {
						element.classList.add(classes.BTN_ICON_POSITION_PREFIX + iconpos);
					} else {
						element.classList.add(classes.BTN_ICON_ONLY);
					}
				}
			};

			/**
			 * Set title for button without showing text
			 * @method _setTitleForIcon
			 * @param {HTMLElement|HTMLInputElement|HTMLButtonElement} element
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._setTitleForIcon = function (element) {
				var self = this,
					elementTagName = element.tagName.toLowerCase(),
					options = self.options,
					buttonText = element.textContent;

				// Add title to element if button not has text.
				if (options.iconpos === "notext" && !element.getAttribute("title")) {
					element.setAttribute("title", buttonText);
				}
				if (!buttonText.length && elementTagName !== "label") {
					element.textContent = options.icon.replace("naviframe-", "");
				}
			};

			prototype._setDisabled = function (element) {
				var self = this,
					options = self.options,
					buttonClassList = element.classList;

				if (options.disabled === true || element.disabled || buttonClassList.contains(classes.DISABLED)) {
					self._disable(element);
				}
			};

			/**
			* Build Button
			* @method _build
			* @protected
			* @param {HTMLElement} element
			* @return {HTMLElement}
			* @member ns.widget.core.Button
			*/
			prototype._build = function (element) {
				var self = this,
					buttonClassList = element.classList;

				if (!buttonClassList.contains(classes.BTN)) {
					buttonClassList.add(classes.BTN);
				}

				self._setStyle(element);
				self._setMultiline(element);
				self._setInline(element);
				self._setIconpos(element);
				self._setIcon(element);
				self._setDisabled(element);

				return element;
			};

			/**
			 * Refresh structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._refresh = function () {
				var self = this,
					element = this.element;

				self._setStyle(element);
				self._setMultiline(element);
				self._setInline(element);
				self._setIconpos(element);
				self._setIcon(element);
				self._setDisabled(element);

				return null;
			};

			/* Get value of button
			 * @method _getValue
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._getValue = function () {
				return this.element.textContent;
			};

			/* Set value of button
			 * @method _setValue
			 * @param {string} value
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._setValue = function (value) {
				this.element.textContent = value;
			};

			/**
			 * Enable button
			 * @method _enable
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._enable = function (element) {
				var options = this.options,
					tagName = element.tagName.toLowerCase();

				if (element) {
					if (element.tagName.toLowerCase() === "button") {
						element.removeAttribute("disabled");
					}
					element.classList.remove(classes.DISABLED);
					options.disabled = false;
				}
			};

			/**
			 * Disable button
			 * @method _disable
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Button
			 */
			prototype._disable = function (element) {
				var options = this.options;

				if (element) {
					if (element.tagName.toLowerCase() === "button") {
						element.disabled = true;
					}
					element.classList.add(classes.DISABLED);
					options.disabled = true;
				}
			};

			ns.widget.core.Button = Button;

			engine.defineWidget(
				"Button",
				"button, [data-role='button'], .ui-btn, input[type='button']",
				[],
				Button,
				"core"
			);
			}(window.document, ns));

/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function (document, ns) {
	
				var BaseWidget  = ns.widget.BaseWidget,
				engine = ns.engine,
				selectors = ns.util.selectors,
				slice = [].slice,
				Checkboxradio = function () {
					var self = this;

					self._label = null;
					self._inputtype = null;
				},
				classes = {
					DISABLED: "ui-state-disabled",
					UI_PREFIX: "ui-"
				},
				prototype = new BaseWidget();

			Checkboxradio.prototype = prototype;

			prototype._build = function (element) {
				var inputtype = element.getAttribute("type"),
					elementClassList = element.classList;

				if (inputtype !== "checkbox" && inputtype !== "radio") {
					//_build should always return element
					return element;
				}

				elementClassList.add(classes.UI_PREFIX + inputtype);

				return element;
			};

			/**
			* Inits widget
			* @method _init
			* @param {HTMLElement} element
			* @protected
			* @member ns.widget.Checkboxradio
			* @instance
			*/
			prototype._init = function (element) {
				var self = this;

				self._label = element.labels ? element.labels[0] : null;
				self._inputtype = element.getAttribute("type");
			};

			/**
			* Returns either a set of radios with the same name attribute or a single checkbox
			* @method getInputSet
			* @return {Array}
			* @protected
			* @member ns.widget.Checkboxradio
			* @instance
			*/
			prototype._getInputSet = function () {
				var self = this,
					element = self.element,
					parent;

				if (self._inputtype === "checkbox") {
					return [element];
				}

				parent = selectors.getClosestBySelector(element, "form, fieldset, .ui-page, [data-role='page'], [data-role='dialog']");

				if (parent) {
					return slice.call(parent.querySelectorAll("input[name='" + element.name + "'][type='" + self._inputtype + "']"));
				}

				return [];
			};

			/**
			* Refreshes widget
			* @method _refresh
			* @member ns.widget.Checkboxradio
			* @instance
			*/
			prototype._refresh = function () {
				var self = this,
					element = this.element;

				if (element.getAttribute("disabled")) {
					self._disable();
				} else {
					self._enable();
				}
			};

			/**
			* Enables widget
			* @method _enable
			* @member ns.widget.Checkboxradio
			* @protected
			* @instance
			*/
			prototype._enable = function (element) {
				if (element) {
					element.classList.remove(classes.DISABLED);
					element.removeAttribute("disabled");
				}
			};

			/**
			* Disables widget
			* @method _disable
			* @protected
			* @member ns.widget.Checkboxradio
			* @instance
			*/
			prototype._disable = function (element) {
				if (element) {
					element.classList.add(classes.DISABLED);
					element.setAttribute("disabled", true);
				}
			};

			/**
			* Return checked checkboxradio element
			* @method getCheckedElement
			* @return {?HTMLElement}
			* @member ns.widget.Checkboxradio
			* @new
			*/
			prototype.getCheckedElement = function () {
				var radios = this._getInputSet(),
					i,
					max = radios.length;
				for (i = 0; i < max; i++) {
					if (radios[i].checked) {
						return radios[i];
					}
				}
				return null;
			};

			/**
			* Returns value of checkbox if it is checked or value of radios with the same name
			* @method _getValue
			* @member ns.widget.Checkboxradio
			* @return {?string}
			* @protected
			* @instance
			* @new
			*/
			prototype._getValue = function () {
				var checkedElement = this.getCheckedElement();

				if (checkedElement) {
					return checkedElement.value;
				}
				return null;
			};

			/**
			* Check element with value
			* @method _setValue
			* @param {string} value
			* @member ns.widget.Checkboxradio
			* @chainable
			* @instance
			* @protected
			* @new
			*/
			prototype._setValue = function (value) {
				var self = this,
					radios = self._getInputSet(),
					checkedElement,
					i,
					max = radios.length;

				for (i = 0; i < max; i++) {
					if (radios[i].value === value) {
						checkedElement = self.getCheckedElement();
						if (checkedElement) {
							checkedElement.checked = false;
						}
						radios[i].checked = true;
						return self;
					}
				}
				return self;
			};

			/**
			* Cleans widget's resources
			* @method _destroy
			* @protected
			* @member ns.widget.Checkboxradio
			* @instance
			*/
			prototype._destroy = function () {
				var self = this;

				self._label = null;
				self._inputtype = null;
			};

			// definition
			ns.widget.core.Checkboxradio = Checkboxradio;
			engine.defineWidget(
				"Checkboxradio",
				"input[type='checkbox']:not(.ui-slider-switch-input):not([data-role='toggleswitch']):not(.ui-toggleswitch), " +
				"input[type='radio'], " +
				"input.ui-checkbox, " +
				"input.ui-radio",
				[],
				Checkboxradio,
				""
			);
			}(window.document, ns));

/*global window, define, console */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * # Marquee Text
 * It makes <div> element with text move horizontally like legacy <marquee> tag
 *
 * ## Make Marquee Element
 * If you want to use Marquee widget, you have to declare below attributes in <div> element and make Marquee widget in JS code.
 * To use a Marquee widget in your application, use the following code:
 *
 *	@example
 *	<div class="ui-content">
 *		<ul class="ui-listview">
 *			<li><div class="ui-marquee" id="marquee">Marquee widget code sample</div></li>
 *		</ul>
 *	</div>
 *	<script>
 *		var marqueeEl = document.getElementById("marquee"),
 *			marqueeWidget = new tau.widget.Marquee(marqueeEl, {marqueeStyle: "scroll", delay: "3000"});
 *	</script>
 *
 * @author Heeju Joo <heeju.joo@samsung.com>
 * @class ns.widget.core.Marquee
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
	
			var BaseWidget = ns.widget.BaseWidget,
				/**
				 * Alias for class ns.engine
				 * @property {ns.engine} engine
				 * @member ns.widget.core.Marquee
				 * @private
				 */
				engine = ns.engine,
				/**
				 * Alias for class ns.event
				 * @property {ns.event} event
				 * @member ns.widget.core.Marquee
				 * @private
				 */
				utilEvent = ns.event,
				/**
				 * Alias for class ns.util.object
				 * @property {Object} objectUtils
				 * @member ns.widget.core.Marquee
				 * @private
				 */
				objectUtils = ns.util.object,
				/**
				 * Alias for class ns.util.DOM
				 * @property {Object} domUtil
				 * @member ns.widget.core.Marquee
				 * @private
				 */
				domUtil = ns.util.DOM,

				Marquee = function() {
					this._ui = {};
					this._ui.marqueeInnerElement = null;
					this._ui.styleSheelElement = null;

					this._state = states.STOPPED;
					this._hasEllipsisText = false;

					this.options = objectUtils.merge({}, Marquee.defaults);

					// event callbacks
					this._callbacks = {};
				},

				prototype = new BaseWidget(),

				CLASSES_PREFIX = "ui-marquee",

				states = {
					RUNNING: "running",
					STOPPED: "stopped",
					IDLE: "idle"
				},

				eventType = {
					/**
					 * Triggered when the marquee animation end.
					 * @event marqueeend
					 * @memeber ns.widget.core.Marquee
					 */
					MARQUEE_START: "marqueestart",
					MARQUEE_END: "marqueeend",
					MARQUEE_STOPPED: "marqueestopped"
				},
				/**
				 * Dictionary for CSS class of marquee play state
				 * @property {Object} classes
				 * @member ns.widget.core.Marquee
				 * @static
				 */
				classes = {
					MARQUEE_CONTENT: CLASSES_PREFIX + "-content",
					MARQUEE_GRADIENT: CLASSES_PREFIX + "-gradient",
					MARQUEE_ELLIPSIS: CLASSES_PREFIX + "-ellipsis",
					ANIMATION_RUNNING: CLASSES_PREFIX + "-anim-running",
					ANIMATION_STOPPED: CLASSES_PREFIX + "-anim-stopped",
					ANIMATION_IDLE: CLASSES_PREFIX + "-anim-idle"
				},

				selector = {
					MARQUEE_CONTENT: "." + CLASSES_PREFIX + "-content"
				},

				/**
				 * Dictionary for marquee style
				 */
				style = {
					SCROLL: "scroll",
					SLIDE: "slide",
					ALTERNATE: "alternate",
					ENDTOEND: "endToEnd"
				},

				ellipsisEffect = {
					GRADIENT: "gradient",
					ELLIPSIS: "ellipsis",
					NONE: "none"
				},

				/**
				 * Options for widget
				 * @property {Object} options
				 * @property {string|"slide"|"scroll"|"alternate"} [options.marqueeStyle="slide"] Sets the default style for the marquee
				 * @property {number} [options.speed=60] Sets the speed(px/sec) for the marquee
				 * @property {number|"infinite"} [options.iteration=1] Sets the iteration count number for marquee
				 * @property {number} [options.delay=2000] Sets the delay(ms) for marquee
				 * @property {"linear"|"ease"|"ease-in"|"ease-out"|"cubic-bezier(n,n,n,n)"} [options.timingFunction="linear"] Sets the timing function for marquee
				 * @property {"gradient"|"ellipsis"|"none"} [options.ellipsisEffect="gradient"] Sets the end-effect(gradient) of marquee
				 * @property {boolean} [options.autoRun=true] Sets the status of autoRun
				 * @member ns.widget.core.Marquee
				 * @static
				 */
				defaults = {
					marqueeStyle: style.SLIDE,
					speed: 60,
					iteration: 1,
					delay: 0,
					timingFunction: "linear",
					ellipsisEffect: "gradient",
					runOnlyOnEllipsisText: true,
					autoRun: true
				};

			Marquee.classes = classes;
			Marquee.defaults = defaults;

			/* Marquee AnimationEnd callback */
			function marqueeEndHandler(self) {
				self.reset();
			}

			function getAnimationDuration(self, speed) {
				var marqueeInnerElement = self._ui.marqueeInnerElement,
					textWidth = marqueeInnerElement.scrollWidth,
					duration = textWidth / speed;

				return duration;
			}

			function setMarqueeKeyFrame(self, marqueeStyle) {
				var marqueeInnerElement = self._ui.marqueeInnerElement,
					marqueeContainer = self.element,
					containerWidth = marqueeContainer.offsetWidth,
					textWidth = marqueeInnerElement.scrollWidth,
					styleElement = document.createElement("style"),
					keyFrameName = marqueeStyle + "-" + self.id,
					customKeyFrame,
					returnTimeFrame;

				switch (marqueeStyle) {
					case style.SLIDE:
						customKeyFrame = "@-webkit-keyframes " + keyFrameName + " {"
										+ "0% { -webkit-transform: translate3d(0, 0, 0);}"
										+ "95%, 100% { -webkit-transform: translate3d(-" + (textWidth - containerWidth) + "px, 0, 0);} }";
						break;
					case style.SCROLL:
						customKeyFrame = "@-webkit-keyframes " + keyFrameName + " {"
										+ "0% { -webkit-transform: translate3d(0, 0, 0);}"
										+ "95%, 100% { -webkit-transform: translate3d(-100%, 0, 0);} }";
						break;
					case style.ALTERNATE:
						customKeyFrame = "@-webkit-keyframes " + keyFrameName + " {"
										+ "0% { -webkit-transform: translate3d(0, 0, 0);}"
										+ "50% { -webkit-transform: translate3d(-" + (textWidth - containerWidth) + "px, 0, 0);}"
										+ "100% { -webkit-transform: translate3d(0, 0, 0);} }";
						break;
					case style.ENDTOEND:
						returnTimeFrame = parseInt((textWidth / (textWidth + containerWidth)) * 100, 10);
						customKeyFrame = "@-webkit-keyframes " + keyFrameName + " {"
										+ "0% { -webkit-transform: translate3d(0, 0, 0);}"
										+ returnTimeFrame + "% { -webkit-transform: translate3d(-100%, 0, 0); opacity: 1;}"
										+ (returnTimeFrame+1) + "% { -webkit-transform: translate3d(-100%, 0, 0); opacity: 0; }"
										+ (returnTimeFrame+2) + "% { -webkit-transform: translate3d(" + containerWidth + "px, 0, 0); opacity: 0; }"
										+ (returnTimeFrame+3) + "% { -webkit-transform: translate3d(" + containerWidth + "px, 0, 0); opacity: 1; }"
										+ "100% { -webkit-transform: translate3d(0, 0, 0);} }";
						break;
					default:
						customKeyFrame = null;
						break;
				}

				if (customKeyFrame) {
					self.element.appendChild(styleElement);
					styleElement.sheet.insertRule(customKeyFrame, 0);

					self._ui.styleSheelElement = styleElement;
				}

				return keyFrameName;
			}

			function setAnimationStyle(self, options) {
				var marqueeInnerElement = self._ui.marqueeInnerElement,
					marqueeInnerElementStyle = marqueeInnerElement.style,
					duration = getAnimationDuration(self, isNaN(parseInt(options.speed))? defaults.speed : options.speed ),
					marqueeKeyFrame = setMarqueeKeyFrame(self, options.marqueeStyle),
					iteration;

				// warning when option value is not correct.
				if (isNaN(parseInt(options.speed))) {
					ns.warn("speed value must be number(px/sec)");
				}
				if ((options.iteration !== "infinite") && isNaN(options.iteration)) {
					ns.warn("iteration count must be number or 'infinite'");
				}
				if (isNaN(options.delay)) {
					ns.warn("delay value must be number");
				}

				marqueeInnerElementStyle.webkitAnimationName = marqueeKeyFrame;
				marqueeInnerElementStyle.webkitAnimationDuration = duration + "s";
				marqueeInnerElementStyle.webkitAnimationIterationCount = options.iteration;
				marqueeInnerElementStyle.webkitAnimationTimingFunction = options.timingFunction;
				marqueeInnerElementStyle.webkitAnimationDelay = options.delay + "ms";
			}

			function setEllipsisEffectStyle(self, ellipsisEffectOption, hasEllipsisText) {
				var marqueeElement = self.element;

				switch (ellipsisEffectOption) {
					case ellipsisEffect.GRADIENT:
						if (hasEllipsisText) {
							marqueeElement.classList.add(classes.MARQUEE_GRADIENT);
						}
						break;
					case ellipsisEffect.ELLIPSIS:
						marqueeElement.classList.add(classes.MARQUEE_ELLIPSIS);
						break;
					default :
						break;
				}

			}

			function setAutoRunState(self, autoRunOption) {
				if (autoRunOption) {
					self.start();
				} else {
					self.stop();
				}
			}

			/**
			 * Build Marquee DOM
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.core.Marquee
			 */
			prototype._build = function(element) {
				var marqueeInnerElement = document.createElement("div");

				while (element.hasChildNodes()) {
					marqueeInnerElement.appendChild(element.removeChild(element.firstChild));
				}
				marqueeInnerElement.classList.add(classes.MARQUEE_CONTENT);
				element.appendChild(marqueeInnerElement);

				this._ui.marqueeInnerElement = marqueeInnerElement;

				return element;
			};

			/**
			 * Init Marquee Style
			 * @method _init
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.core.Marquee
			 */
			prototype._init = function(element) {
				var self = this;

				self._ui.marqueeInnerElement = self._ui.marqueeInnerElement || element.querySelector(selector.MARQUEE_CONTENT);
				self._hasEllipsisText = element.offsetWidth - domUtil.getCSSProperty(element, "padding-right", null, "float") < self._ui.marqueeInnerElement.scrollWidth;

				if (!(self.options.runOnlyOnEllipsisText && !self._hasEllipsisText)) {
					setEllipsisEffectStyle(self, self.options.ellipsisEffect, self._hasEllipsisText);
					setAnimationStyle(self, self.options);
					setAutoRunState(self, self.options.autoRun);
				}

				return element;
			};

			/**
			 * Bind events
			 * @method _bindEvents
			 * @protected
			 * @member ns.widget.core.Marquee
			 */
			prototype._bindEvents = function() {
				var self = this,
					marqueeInnerElement = self._ui.marqueeInnerElement,
					animationEndCallback = marqueeEndHandler.bind(null, self);

				self._callbacks.animationEnd = animationEndCallback;

				utilEvent.one(marqueeInnerElement, "webkitAnimationEnd", animationEndCallback)
			};

			/**
			 * Refresh styles
			 * @method _refresh
			 * @protected
			 * @memeber ns.widget.core.Marquee
			 */
			prototype._refresh = function() {
				var self = this;

				self._resetStyle();
				self._hasEllipsisText = self.element.offsetWidth < self._ui.marqueeInnerElement.scrollWidth;

				if (self.options.runOnlyOnEllipsisText && !self._hasEllipsisText) {
					return;
				}

				setEllipsisEffectStyle(self, self.options.ellipsisEffect, self._hasEllipsisText);
				setAnimationStyle(self, self.options);
				setAutoRunState(self, self.options.autoRun);
			};

			/**
			 * Reset style of Marquee elements
			 * @method _resetStyle
			 * @protected
			 * @memeber ns.widget.core.Marquee
			 */
			prototype._resetStyle = function() {
				var self = this,
					marqueeContainer = self.element,
					marqueeKeyframeStyleSheet = self._ui.styleSheelElement,
					marqueeInnerElementStyle = self._ui.marqueeInnerElement.style;

				if (marqueeContainer.contains(marqueeKeyframeStyleSheet)) {
					marqueeContainer.removeChild(marqueeKeyframeStyleSheet);
				}

				marqueeInnerElementStyle.webkitAnimationName = "";
				marqueeInnerElementStyle.webkitAnimationDuration = "";
				marqueeInnerElementStyle.webkitAnimationDelay = "";
				marqueeInnerElementStyle.webkitAnimationIterationCount = "";
				marqueeInnerElementStyle.webkitAnimationTimingFunction = "";
			};

			/**
			 * Remove marquee object and Reset DOM structure
			 * @method _resetDOM
			 * @protected
			 * @memeber ns.widget.core.Marquee
			 */
			prototype._resetDOM = function() {
				var ui = this._ui;

				while (ui.marqueeInnerElement.hasChildNodes()) {
					this.element.appendChild(ui.marqueeInnerElement.removeChild(ui.marqueeInnerElement.firstChild));
				}
				this.element.removeChild(ui.marqueeInnerElement);
				return null;
			};

			/**
			 * Destroy widget
			 * @method _destroy
			 * @protected
			 * @member ns.widget.core.Marquee
			 */
			prototype._destroy = function() {
				var self = this;

				self._resetStyle();
				self._resetDOM();
				self._callbacks = null;
				self._ui = null;

				return null;
			};

			/**
			 * Set Marquee animation status Running
			 * @method _animationStart
			 * @memeber ns.widget.core.Marquee
			 */
			prototype._animationStart = function() {
				var self = this,
					marqueeElementClassList = self.element.classList,
					marqueeInnerElementClassList = self._ui.marqueeInnerElement.classList;

				self._state = states.RUNNING;

				if (marqueeElementClassList.contains(classes.MARQUEE_ELLIPSIS)) {
					marqueeElementClassList.remove(classes.MARQUEE_ELLIPSIS);
				}

				marqueeInnerElementClassList.remove(classes.ANIMATION_IDLE, classes.ANIMATION_STOPPED);
				marqueeInnerElementClassList.add(classes.ANIMATION_RUNNING);
				self.trigger(eventType.MARQUEE_START);
			};

			/**
			 * Start Marquee animation
			 *
			 * #####Running example in pure JavaScript:
			 *
			 *	@example
			 *	<div class="ui-marquee" id="marquee">
			 *		<p>MarqueeTEST TEST message TEST for marquee</p>
			 *	</div>
			 *	<script>
			 *		var marqueeWidget = tau.widget.Marquee(document.getElementById("marquee"));
			 *		marqueeWidget.start();
			 *	</script>
			 *
			 * @method start
			 * @memeber ns.widget.core.Marquee
			 */
			prototype.start = function() {
				var self = this;

				if (self.options.runOnlyOnEllipsisText && !self._hasEllipsisText) {
					return;
				}

				switch (self._state) {
					case states.IDLE:
						setAnimationStyle(self, self.options);
						self._bindEvents();
						self._animationStart();
						break;
					case states.STOPPED:
						self._state = states.RUNNING;
						self._animationStart();
						break;
					case states.RUNNING:
						break;
				}
			};

			/**
			 * Pause Marquee animation
			 *
			 * #####Running example in pure JavaScript:
			 *	@example
			 *	<div class="ui-marquee" id="marquee">
			 *		<p>MarqueeTEST TEST message TEST for marquee</p>
			 *	</div>
			 *	<script>
			 *		var marqueeWidget = tau.widget.Marquee(document.getElementById("marquee"));
			 *		marqueeWidget.stop();
			 *	</script>
			 *
			 * @method stop
			 * @member ns.widget.core.Marquee
			 */
			prototype.stop = function() {
				var self = this,
					marqueeInnerElementClassList = self._ui.marqueeInnerElement.classList;

				if (self.options.runOnlyOnEllipsisText && !self._hasEllipsisText) {
					return;
				}

				if (self._state == states.IDLE) {
					return;
				}

				self._state = states.STOPPED;
				marqueeInnerElementClassList.remove(classes.ANIMATION_RUNNING);
				marqueeInnerElementClassList.add(classes.ANIMATION_STOPPED);
				self.trigger(eventType.MARQUEE_STOPPED);
			};

			/**
			 * Reset Marquee animation
			 *
			 * #####Running example in pure JavaScript:
			 *	@example
			 *	<div class="ui-marquee" id="marquee">
			 *		<p>MarqueeTEST TEST message TEST for marquee</p>
			 *	</div>
			 *	<script>
			 *		var marqueeWidget = tau.widget.Marquee(document.getElementById("marquee"));
			 *		marqueeWidget.reset();
			 *	</script>
			 *
			 * @method reset
			 * @member ns.widget.core.Marquee
			 */
			prototype.reset = function() {
				var self = this,
					marqueeElementClassList = self.element.classList,
					marqueeInnerElementClassList = self._ui.marqueeInnerElement.classList;

				if (self.options.runOnlyOnEllipsisText && !self._hasEllipsisText) {
					return;
				}

				if (self._state == states.IDLE) {
					return;
				}

				self._state = states.IDLE;
				marqueeInnerElementClassList.remove(classes.ANIMATION_RUNNING, classes.ANIMATION_STOPPED);
				marqueeInnerElementClassList.add(classes.ANIMATION_IDLE);
				if (self.options.ellipsisEffect == ellipsisEffect.ELLIPSIS) {
					marqueeElementClassList.add(classes.MARQUEE_ELLIPSIS);
				}

				self._resetStyle();
				self.trigger(eventType.MARQUEE_END);
			};

			Marquee.prototype = prototype;
			ns.widget.core.Marquee = Marquee;

			engine.defineWidget(
				"Marquee",
				".ui-marquee",
				["start", "stop", "reset"],
				Marquee,
				"core"
			);
			}(window.document, ns));

/*global window, define, XMLHttpRequest */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Load Utility
 * Object contains function to load external resources.
 * @class ns.util.load
 */
(function (document, ns) {
	
	
			/**
			 * Local alias for document HEAD element
			 * @property {HTMLHeadElement} head
			 * @static
			 * @private
			 * @member ns.util.load
			 */
			var head = document.head,
				/**
				 * Local alias for document styleSheets element
				 * @property {HTMLStyleElement} styleSheets
				 * @static
				 * @private
				 * @member ns.util.load
				 */
				styleSheets = document.styleSheets,
				/**
				 * Local alias for ns.util.DOM
				 * @property {Object} utilsDOM Alias for {@link ns.util.DOM}
				 * @member ns.util.load
				 * @static
				 * @private
				 */
				utilDOM = ns.util.DOM,
				getNSData = utilDOM.getNSData,
				setNSData = utilDOM.setNSData,
				load = ns.util.load || {},
				/**
				 * Regular expression for extracting path to the image
				 * @property {RegExp} IMAGE_PATH_REGEXP
				 * @static
				 * @private
				 * @member ns.util.load
				 */
				IMAGE_PATH_REGEXP = /url\((\.\/)?images/gm,
				/**
				 * Regular expression for extracting path to the css
				 * @property {RegExp} CSS_FILE_REGEXP
				 * @static
				 * @private
				 * @member ns.util.load
				 */
				CSS_FILE_REGEXP = /[^/]+\.css$/;

			/**
			 * Load file
			 * (synchronous loading)
			 * @method loadFileSync
			 * @param {string} scriptPath
			 * @param {?Function} successCB
			 * @param {?Function} errorCB
			 * @static
			 * @private
			 * @member ns.util.load
			 */
			 function loadFileSync(scriptPath, successCB, errorCB) {
				var xhrObj = new XMLHttpRequest();

				// open and send a synchronous request
				xhrObj.open('GET', scriptPath, false);
				xhrObj.send();
				// add the returned content to a newly created script tag
				if (xhrObj.status === 200 || xhrObj.status === 0) {
					if (typeof successCB === 'function') {
						successCB(xhrObj, xhrObj.status);
					}
				} else {
					if (typeof errorCB === 'function') {
						errorCB(xhrObj, xhrObj.status, new Error(xhrObj.statusText));
					}
				}
			}

			/**
			 * Callback function on javascript load success
			 * @method scriptSyncSuccess
			 * @private
			 * @static
			 * @param {?Function} successCB
			 * @param {?Function} xhrObj
			 * @param {?string} status
			 * @member ns.util.load
			 */
			function scriptSyncSuccess(successCB, xhrObj, status) {
				var script = document.createElement('script');
				script.type = 'text/javascript';
				script.text = xhrObj.responseText;
				document.body.appendChild(script);
				if (typeof successCB === 'function') {
					successCB(xhrObj, status);
				}
			}


			/**
			 * Add script to document
			 * (synchronous loading)
			 * @method scriptSync
			 * @param {string} scriptPath
			 * @param {?Function} successCB
			 * @param {?Function} errorCB
			 * @static
			 * @member ns.util.load
			 */
			function scriptSync(scriptPath, successCB, errorCB) {
				loadFileSync(scriptPath, scriptSyncSuccess.bind(null, successCB), errorCB);
			}

			/**
			 * Callback function on css load success
			 * @method cssSyncSuccess
			 * @param {string} cssPath
			 * @param {?Function} successCB
			 * @param {?Function} xhrObj
			 * @member ns.util.load
			 * @static
			 * @private
			 */
			function cssSyncSuccess(cssPath, successCB, xhrObj) {
				var css = document.createElement('style');
				css.type = 'text/css';
				css.textContent = xhrObj.responseText.replace(
					IMAGE_PATH_REGEXP,
					'url(' + cssPath.replace(CSS_FILE_REGEXP, 'images')
				);
				if (typeof successCB === 'function') {
					successCB(css);
				}
			}

			/**
			 * Add css to document
			 * (synchronous loading)
			 * @method cssSync
			 * @param {string} cssPath
			 * @param {?Function} successCB
			 * @param {?Function} errorCB
			 * @static
			 * @private
			 * @member ns.util.load
			 */
			function cssSync(cssPath, successCB, errorCB) {
				loadFileSync(cssPath, cssSyncSuccess.bind(null, cssPath, successCB), errorCB);
			}

			/**
			 * Add element to head tag
			 * @method addElementToHead
			 * @param {HTMLElement} element
			 * @param {boolean} [asFirstChildElement=false]
			 * @member ns.util.load
			 * @static
			 */
			function addElementToHead(element, asFirstChildElement) {
				var firstElement;
				if (head) {
					if (asFirstChildElement) {
						firstElement = head.firstElementChild;
						if (firstElement) {
							head.insertBefore(element, firstElement);
							return;
						}
					}
					head.appendChild(element);
				}
			}

			/**
			 * Create HTML link element with href
			 * @method makeLink
			 * @param {string} href
			 * @returns {HTMLLinkElement}
			 * @member ns.util.load
			 * @static
			 */
			function makeLink(href) {
				var cssLink = document.createElement('link');
				cssLink.setAttribute('rel', 'stylesheet');
				cssLink.setAttribute('href', href);
				cssLink.setAttribute('name', 'tizen-theme');
				return cssLink;
			}

			/**
			 * Adds the given node to document head or replaces given 'replaceElement'.
			 * Additionally adds 'name' and 'theme-name' attribute
			 * @param {HTMLElement} node Element to be placed as theme link
			 * @param {string} themeName Theme name passed to the element
			 * @param {HTMLElement} [replaceElement=null] If replaceElement is given it gets replaced by node
			 */
			function addNodeAsTheme(node, themeName, replaceElement) {
				setNSData(node, 'name', 'tizen-theme');
				setNSData(node, 'theme-name', themeName);

				if (replaceElement) {
					replaceElement.parentNode.replaceChild(node, replaceElement);
				} else {
					addElementToHead(node, true);
				}
			}

			/**
			 * Add css link element to head if not exists
			 * @method themeCSS
			 * @param {string} path
			 * @param {string} themeName
			 * @param {boolean} [embed=false] Embeds the CSS content to the document
			 * @member ns.util.load
			 * @static
			 */
			function themeCSS(path, themeName, embed) {
				var i,
					styleSheetsLength = styleSheets.length,
					ownerNode,
					previousElement = null,
					linkElement;
				// Find css link or style elements
				for (i = 0; i < styleSheetsLength; i++) {
					ownerNode = styleSheets[i].ownerNode;

					// We try to find a style / link node that matches current style or is linked to
					// the proper theme. We cannot use ownerNode.href because this returns the absolute path
					if (getNSData(ownerNode, 'name') === 'tizen-theme' || ownerNode.getAttribute("href") === path) {
						if (getNSData(ownerNode, 'theme-name') === themeName) {
							// Nothing to change
							return;
						}
						previousElement = ownerNode;
						break;
					}
				}

				if (embed){
					// Load and replace old styles or append new styles
					cssSync(path, function onSuccess(styleElement) {
						addNodeAsTheme(styleElement, themeName, previousElement);
					}, function onFailure(xhrObj, xhrStatus, errorObj) {
						ns.warn("There was a problem when loading '" + themeName + "', status: " + xhrStatus);
					});
				} else {
					linkElement = makeLink(path);
					addNodeAsTheme(linkElement, themeName, previousElement);
				}
			}

			/**
			 * In debug mode add time to url to disable cache
			 * @property {string} cacheBust
			 * @member ns.util.load
			 * @static
			 */
			load.cacheBust = (document.location.href.match(/debug=true/)) ? '?cacheBust=' + (new Date()).getTime() : '';
			// the binding a local methods with the namespace
			load.scriptSync = scriptSync;
			load.addElementToHead = addElementToHead;
			load.makeLink = makeLink;
			load.themeCSS = themeCSS;

			ns.util.load = load;
			}(window.document, ns));

/*global window, define, Math, ns*/
/*jslint bitwise: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Theme object
 * Class with functions to set theme of application.
 * @class ns.theme
 */
(function (window, document, ns) {
	
			/**
			 * Local alias for document HEAD element
			 * @property {HTMLHeadElement} head
			 * @static
			 * @private
			 * @member ns.theme
			 */
			var head = document.head,
				documentElement = document.documentElement,
				frameworkData = ns.frameworkData,
				util = ns.util,
				DOM = util.DOM,
				load = util.load,
				support = ns.support,

				stopEvent = function (event) {
					var element = event.target,
						tag = element.tagName.toLowerCase(),
						type = element.type;
					if ((tag !== "input" ||
							(type !== "text" && type !== "email" && type !== "url" && type !== "search" && type !== "tel")) &&
							tag !== "textarea") {
						event.stopPropagation();
						event.preventDefault();
					}
				},

				THEME_JS_FILE_NAME = "theme.js",
				THEME_CSS_FILE_NAME = "tau",

				themeRegex =  /ui-(bar|body|overlay)-([a-z])\b/,
				deviceWidthRegex = /.*width=(device-width|\d+)\s*,?.*$/gi;

			ns.theme = {
				/**
				 * Standard theme
				 * @property {string} theme="s"
				 * @member ns.theme
				 */
				theme: "s",

				_activeTheme: null,

				/**
				 * This function inits theme.
				 * @method init
				 * @param {HTMLElement} container
				 * @member ns.theme
				 */
				init: function (container) {
					var self = this,
						containerClassList = container.classList;

					if (frameworkData) {
						frameworkData.getParams();
					}

					if (support && support.gradeA()) {
						documentElement.classList.add("ui-mobile");
						containerClassList.add("ui-mobile-viewport");
					}

					if (frameworkData) {
						self.loadTheme(frameworkData.theme);
					}
				},

				/**
				 * This function scales font size.
				 * @method scaleBaseFontSize
				 * @param {number} themeDefaultFontSize Default font size
				 * @param {number} ratio Scaling ration
				 * @member ns.theme
				 */
				scaleBaseFontSize : function (themeDefaultFontSize, ratio) {
					var scaledFontSize = Math.max(themeDefaultFontSize * ratio | 0, 4);
					documentElement.style.fontSize = scaledFontSize + "px";
					document.body.style.fontSize = scaledFontSize + "px";
				},

				/**
				 * This function searches theme, which is inherited
				 * from parents by element.
				 * @method getInheritedTheme
				 * @param {HTMLElement} element Element for which theme is looking for.
				 * @param {string} defaultTheme Default theme.
				 * It is used if no theme, which can be inherited, is found.
				 * @return {string} Inherited theme
				 * @member ns.theme
				 */
				getInheritedTheme: function (element, defaultTheme) {
					var theme,
						parentElement = element.parentNode,
						parentClasses,
						parentTheme;

					theme = DOM.getNSData(element, "theme");

					if (!theme) {
						while (parentElement) {
							parentClasses = parentElement.className || "";
							parentTheme = themeRegex.exec(parentClasses);
							if (parentClasses && parentTheme && parentTheme.length > 2) {
								theme = parentTheme[2];
								break;
							}
							parentElement = parentElement.parentNode;
						}
					}
					return theme || defaultTheme;
				},

				/**
				 * This function sets selection behavior for the element.
				 * @method enableSelection
				 * @param {HTMLElement} element Element for which selection behavior is set.
				 * @param {"text"|"auto"|"none"} value="auto" Selection behavior.
				 * @return {HTMLElement} Element with set styles.
				 * @member ns.theme
				 */
				enableSelection: function (element, value) {
					var val,
						elementStyle;

					switch (value) {
					case "text":
					case "auto":
					case "none":
						val = value;
						break;
					default:
						val = "auto";
						break;
					}

					if (element === document) {
						element = document.body;
					}

					elementStyle = element.style;
					elementStyle.MozUserSelect = elementStyle.webkitUserSelect = elementStyle.userSelect = val;

					return element;
				},

				/**
				 * This function disables event "contextmenu".
				 * @method disableContextMenu
				 * @param {HTMLElement} element Element for which event "contextmenu"
				 * is disabled.
				 * @member ns.theme
				 */
				disableContextMenu: function (element) {
					element.addEventListener("contextmenu", stopEvent, true);
				},

				/**
				 * This function enables event "contextmenu".
				 * @method enableContextMenu
				 * @param {HTMLElement} element Element for which event "contextmenu"
				 * is enabled.
				 * @member ns.theme
				 */
				enableContextMenu: function (element) {
					element.removeEventListener("contextmenu", stopEvent, true);
				},

				/**
				 * This function loads files with proper theme.
				 * @method loadTheme
				 * @param {string} theme Choosen theme.
				 * @member ns.theme
				 */
				loadTheme: function(theme) {
					var self = this,
						themePath = frameworkData.themePath,
						themeName = THEME_CSS_FILE_NAME,
						cssPath,
						isMinified = frameworkData.minified,
						jsPath;

					// If the theme has been loaded do not repeat that process
					if (frameworkData.themeLoaded) {
												return;
					}

					if (frameworkData.frameworkName !== "tau") {
						themeName = "tizen-web-ui-fw-theme";
					}
					if (isMinified) {
						cssPath = themePath + "/" + themeName + ".min.css";
					} else {
						cssPath = themePath + "/" + themeName + ".css";
					}

					
					load.themeCSS(cssPath, theme);
					jsPath = themePath + "/" + THEME_JS_FILE_NAME;
										load.scriptSync(jsPath);

					if (support.gradeA()) {
						self.setScaling();
					}

					frameworkData.themeLoaded = true;
				},

				/**
				 * This function sets viewport.
				 * If custom viewport is found, its width will be returned.
				 * Otherwise, the new viewport will be created.
				 * @method setViewport
				 * @param {number|string} viewportWidth Width of the new viewport.
				 * If no viewport is found, the new viewport with this
				 * width is created.
				 * @return {string} Width of custom viewport.
				 * @member ns.theme
				 */
				setViewport: function(viewportWidth) {
					var metaViewport = document.querySelector("meta[name=viewport]"),
						content;

					if (metaViewport) {
						// Found custom viewport!
						content = metaViewport.getAttribute("content");
						viewportWidth = content.replace(deviceWidthRegex, "$1");
					} else {
						// Create a meta tag
						metaViewport = document.createElement("meta");
						metaViewport.name = "viewport";
						content = "width=" + viewportWidth + ", user-scalable=no";
						metaViewport.content = content;
						head.insertBefore(metaViewport, head.firstChild);
					}
					return viewportWidth;
				},

				/**
				 * This function checks if application is run
				 * in the mobile browser.
				 * @method isMobileBrowser
				 * @return {boolean} Returns true, if application
				 * is run in mobile browser. Otherwise, false is returned.
				 * @member ns.theme
				 */
				isMobileBrowser: function() {
					return window.navigator.appVersion.indexOf("Mobile") > -1;
				},

				/**
				 * This function sets scaling of viewport.
				 * @method setScaling
				 * @member ns.theme
				 */
				setScaling: function () {
					var self = this,
						viewportWidth = frameworkData.viewportWidth,
						themeDefaultFontSize = frameworkData.defaultFontSize, // comes from theme.js
						ratio = 1;

					// Keep original font size
					document.body.setAttribute("data-tizen-theme-default-font-size", themeDefaultFontSize);

					if (ns.theme.isMobileBrowser()) {
						// Legacy support: tizen.frameworkData.viewportScale
						if (frameworkData.viewportScale === true) {
							viewportWidth = "screen-width";
						}

						// screen-width support
						if ("screen-width" === viewportWidth) {
							if (window.self === window.top) {
								// Top frame: for target. Use window.outerWidth.
								viewportWidth = window.outerWidth;
							} else {
								// iframe: for web simulator. Use clientWidth.
								viewportWidth = document.documentElement.clientWidth;
							}
						}

						// set viewport meta tag
						// If custom viewport setting exists, get viewport width
						viewportWidth = self.setViewport(viewportWidth);

						if (viewportWidth !== "device-width") {
							ratio = parseFloat(viewportWidth / ns.frameworkData.defaultViewportWidth);
							self.scaleBaseFontSize(themeDefaultFontSize, ratio);
						}
					}
				}
			};

			document.addEventListener("themeinit", function (evt) {
				var router = evt.detail;
				if (router && ns.getConfig("autoInitializePage", true)) {
					ns.theme.init(router.getContainer().element);
				}
			}, false);

			}(window, window.document, ns));

/*global ns, window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Gesture Namespace
 * Core object enables multi gesture support.
 *
 * @class ns.event.gesture
 */
(function ( ns, window, undefined ) {
	
	
			var Gesture = function( elem, options ) {
				return new ns.event.gesture.Instance( elem, options );
			};

			/**
			 * Default values for Gesture feature
			 * @property {Object} defaults
			 * @property {boolean} [defaults.triggerEvent=false]
			 * @property {number} [defaults.updateVelocityInterval=16]
			 * Interval in which Gesture recalculates current velocity in ms
			 * @property {number} [defaults.estimatedPointerTimeDifference=15]
			 * pause time threshold.. tune the number to up if it is slow
			 * @member ns.event.gesture
			 * @static
			 */
			Gesture.defaults = {
				triggerEvent: false,
				updateVelocityInterval: 16,
				estimatedPointerTimeDifference: 15
			};

			/**
			 * Dictionary of orientation
			 * @property {Object} Orientation
			 * @property {1} Orientation.VERTICAL vertical orientation
			 * @property {2} Orientation.HORIZONTAL horizontal orientation
			 * @member ns.event.gesture
			 * @static
			 */
			Gesture.Orientation = {
				VERTICAL: "vertical",
				HORIZONTAL: "horizontal"
			};

			/**
			 * Dictionary of direction
			 * @property {Object} Direction
			 * @property {1} Direction.UP up
			 * @property {2} Direction.DOWN down
			 * @property {3} Direction.LEFT left
			 * @property {4} Direction.RIGHT right
			 * @member ns.event.gesture
			 * @static
			 */
			Gesture.Direction = {
				UP: "up",
				DOWN: "down",
				LEFT: "left",
				RIGHT: "right"
			};

			/**
			 * Dictionary of gesture events state
			 * @property {Object} Event
			 * @property {"start"} Event.START start
			 * @property {"move"} Event.MOVE move
			 * @property {"end"} Event.END end
			 * @property {"cancel"} Event.CANCEL cancel
			 * @property {"blocked"} Event.BLOCKED blocked
			 * @member ns.event.gesture
			 * @static
			 */
			Gesture.Event = {
				START: "start",
				MOVE: "move",
				END: "end",
				CANCEL: "cancel",
				BLOCKED: "blocked"
			};

			/**
			 * Dictionary of gesture events flags
			 * @property {Object} Result
			 * @property {number} [Result.PENDING=1] is pending
			 * @property {number} [Result.RUNNING=2] is running
			 * @property {number} [Result.FINISHED=4] is finished
			 * @property {number} [Result.BLOCK=8] is blocked
			 * @member ns.event.gesture
			 * @static
			 */
			Gesture.Result = {
				PENDING: 1,
				RUNNING: 2,
				FINISHED: 4,
				BLOCK: 8
			};

			/**
			 * Create plugin namespace.
			 * @property {Object} plugin
			 * @member ns.event.gesture
			 * @static
			 */
			Gesture.plugin = {};

			/**
			 * Create object of Detector
			 * @method createDetector
			 * @param {string} gesture
			 * @param {HTMLElement} eventSender
			 * @param {Object} options
			 * @return {ns.event.gesture.Gesture}
			 * @member ns.event.gesture
			 * @static
			 */
			Gesture.createDetector = function( gesture, eventSender, options ) {
				if ( !Gesture.plugin[gesture] ) {
					throw gesture + " gesture is not supported";
				}
				return new Gesture.plugin[gesture]( eventSender, options );
			};

			ns.event.gesture = Gesture;
			} ( ns, window ) );

/*global ns, window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Gesture Utilities
 * Contains helper function to gesture support.
 * @class ns.event.gesture.utils
 */
(function (ns, Math, undefined) {
	
	
				/**
				 * Local alias for {@link ns.event.gesture}
				 * @property {Object}
				 * @member ns.event.gesture.utils
				 * @private
				 * @static
				 */
			var Gesture = ns.event.gesture;

			Gesture.utils = {

				/**
				 * Get center from array of touches
				 * @method getCenter
				 * @param {Event[]} touches description
				 * @member ns.event.gesture.utils
				 * @return {Object} position
				 * @return {number} return.clientX position X
				 * @return {number} return.clientY position Y
				 */
				getCenter: function (touches) {
					var valuesX = [], valuesY = [];

					[].forEach.call(touches, function(touch) {
						// I prefer clientX because it ignore the scrolling position
						valuesX.push(!isNaN(touch.clientX) ? touch.clientX : touch.pageX);
						valuesY.push(!isNaN(touch.clientY) ? touch.clientY : touch.pageY);
					});

					return {
						clientX: (Math.min.apply(Math, valuesX) + Math.max.apply(Math, valuesX)) / 2,
						clientY: (Math.min.apply(Math, valuesY) + Math.max.apply(Math, valuesY)) / 2
					};
				},

				/**
				 * Get velocity
				 * @method getVelocity
				 * @param {number} delta_time Delta of time
				 * @param {number} delta_x Position change on x axis
				 * @param {number} delta_y Position change on y axis
				 * @return {Object} velocity
				 * @return {number} return.x velocity on X axis
				 * @return {number} return.y velocity on Y axis
				 * @member ns.event.gesture.utils
				 */
				getVelocity: function (delta_time, delta_x, delta_y) {
					return {
						x: Math.abs(delta_x / delta_time) || 0,
						y: Math.abs(delta_y / delta_time) || 0
					};
				},

				/**
				 * Get angel between position of two touches
				 * @method getAngle
				 * @param {Event} touch1 first touch
				 * @param {Event} touch2 second touch
				 * @return {number} angel (deg)
				 * @member ns.event.gesture.utils
				 */
				getAngle: function (touch1, touch2) {
					var y = touch2.clientY - touch1.clientY,
						x = touch2.clientX - touch1.clientX;
					return Math.atan2(y, x) * 180 / Math.PI;
				},

				/**
				 * Get direction indicated by position of two touches
				 * @method getDirectiqon
				 * @param {Event} touch1 first touch
				 * @param {Event} touch2 second touch
				 * @return {ns.event.gesture.Direction.LEFT|ns.event.gesture.Direction.RIGHT|ns.event.gesture.Direction.UP|ns.event.gesture.Direction.DOWN}
				 * @member ns.event.gesture.utils
				 */
				getDirection: function (touch1, touch2) {
					var x = Math.abs(touch1.clientX - touch2.clientX),
						y = Math.abs(touch1.clientY - touch2.clientY);

					if (x >= y) {
						return touch1.clientX - touch2.clientX > 0 ? Gesture.Direction.LEFT : Gesture.Direction.RIGHT;
					}
					return touch1.clientY - touch2.clientY > 0 ? Gesture.Direction.UP : Gesture.Direction.DOWN;
				},

				/**
				 * Get distance indicated by position of two touches
				 * @method getDistance
				 * @param {Event} touch1 first touch
				 * @param {Event} touch2 second touch
				 * @return {number} distance
				 * @member ns.event.gesture.utils
				 */
				getDistance: function (touch1, touch2) {
					var x = touch2.clientX - touch1.clientX,
						y = touch2.clientY - touch1.clientY;
					return Math.sqrt((x * x) + (y * y));
				},

				/**
				 * Get scale indicated by position of the first and the last touch
				 * @method getScale
				 * @param {Event} start start touch
				 * @param {Event} end end touch
				 * @return {number} scale
				 * @member ns.event.gesture.utils
				 */
				getScale: function (start, end) {
					// need two fingers...
					if (start.length >= 2 && end.length >= 2) {
						return this.getDistance(end[0], end[1]) / this.getDistance(start[0], start[1]);
					}
					return 1;
				},

				/**
				 * Get value of rotation indicated by position
				 * of the first and the last touch
				 * @method getRotation
				 * @param {Event} start start touch
				 * @param {Event} end end touch
				 * @return {number} angle (deg)
				 * @member ns.event.gesture.utils
				 */
				getRotation: function (start, end) {
					// need two fingers
					if (start.length >= 2 && end.length >= 2) {
						return this.getAngle(end[1], end[0]) -
							this.getAngle(start[1], start[0]);
					}
					return 0;
				},

				/**
				 * Check if the direction is vertical
				 * @method isVertical
				 * @param {ns.event.gesture.Direction.LEFT|ns.event.gesture.Direction.RIGHT|ns.event.gesture.Direction.UP|ns.event.gesture.Direction.DOWN} direction start touch
				 * @return {boolean}
				 * @member ns.event.gesture.utils
				 */
				isVertical: function (direction) {
					return direction === Gesture.Direction.UP || direction === Gesture.Direction.DOWN;
				},

				/**
				 * Check if the direction is horizontal
				 * @method isHorizontal
				 * @param {ns.event.gesture.Direction.LEFT|ns.event.gesture.Direction.RIGHT|ns.event.gesture.Direction.UP|ns.event.gesture.Direction.DOWN} direction start touch
				 * @return {boolean}
				 * @member ns.event.gesture.utils
				 */
				isHorizontal: function (direction) {
					return direction === Gesture.Direction.LEFT || direction === Gesture.Direction.RIGHT;
				},

				/**
				 * Check if the direction is horizontal
				 * @method getOrientation
				 * @param {ns.event.gesture.Direction.LEFT|ns.event.gesture.Direction.RIGHT|ns.event.gesture.Direction.UP|ns.event.gesture.Direction.DOWN} direction
				 * @return {boolean}
				 * @member ns.event.gesture.utils
				 */
				getOrientation: function (direction) {
					return this.isVertical(direction) ? Gesture.Orientation.VERTICAL : Gesture.Orientation.HORIZONTAL;
				}
			};
			} (ns, window.Math));

/*global ns, window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Gesture.Detector class
 * Base class for create detectors in gestures.
 *
 * @class ns.event.gesture.Detector
 */
( function ( ns, window, undefined ) {
	
					/**
				 * Local alias for {@link ns.event.gesture}
				 * @property {Object}
				 * @member ns.event.gesture.Manager
				 * @private
				 * @static
				 */
			var Gesture = ns.event.gesture,
				/**
				 * Alias for method {@link ns.util.object.merge}
				 * @property {Function} objectMerge
				 * @member ns.event.gesture.Detector
				 * @private
				 * @static
				 */
				objectMerge = ns.util.object.merge,

				Detector = function( strategy, sender ) {
					this.sender = sender;
					this.strategy = strategy.create();
					this.name = this.strategy.name;
					this.index = this.strategy.index || 100;
					this.options = this.strategy.options || {};
				};

			/**
			 * Start of gesture detection of given type
			 * @method detect
			 * @param {string} gestureEvent
			 * @return {Object}
			 * @member ns.event.gesture.Detector
			 */
			Detector.prototype.detect = function( gestureEvent ) {
				return this.strategy.handler( gestureEvent, this.sender, this.strategy.options );
			};

			Detector.Sender = {
				sendEvent: function(/* eventName, detail */) {}
			};

			/**
			 * Create plugin namespace.
			 * @property {Object} plugin
			 * @member ns.event.gesture.Detector
			 */
			Detector.plugin = {};

			/**
			 * Methods creates plugin
			 * @method create
			 * @param {Object} gestureHandler
			 * @return {ns.event.gesture.Detector} gestureHandler
			 * @member ns.event.gesture.Detector.plugin
			 */
			Detector.plugin.create = function( gestureHandler ) {

				if ( !gestureHandler.types ) {
					gestureHandler.types = [ gestureHandler.name ];
				}

				var detector = Detector.plugin[ gestureHandler.name ] = function( options ) {
					this.options = objectMerge({}, gestureHandler.defaults, options);
				};

				detector.prototype.create = function() {
					return objectMerge({
						options: this.options
					}, gestureHandler);
				};

				return detector;
			};

			// definition
			Gesture.Detector = Detector;

			} ( ns, window ));

/*global ns, window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Gesture.Manager class
 * Main class controls all gestures.
 * @class ns.event.gesture.Manager
 */
( function ( ns, window, document) {
	
	
				/**
				 * Local alias for {@link ns.event.gesture}
				 * @property {Object}
				 * @member ns.event.gesture.Manager
				 * @private
				 * @static
				 */
			var Gesture = ns.event.gesture,

				/**
				 * Alias for method {@link ns.util.object.merge}
				 * @property {Function} objectMerge
				 * @member ns.event.gesture.Manager
				 * @private
				 * @static
				 */
				objectMerge = ns.util.object.merge,

				/**
				 * Device has touchable interface
				 * @property {boolean} TOUCH_DEVICE
				 * @member ns.event.gesture.Manager
				 * @private
				 * @static
				 */
				TOUCH_DEVICE = "ontouchstart" in window;

			Gesture.Manager = (function() {
				var instance = null,

				startEvent = null,
				isReadyDetecting = false,
				blockMouseEvent = false,

				Manager = function() {

					this.instances = [];
					this.gestureDetectors = [];
					this.runningDetectors = [];
					this.detectorRequestedBlock = null;

					this.unregisterBlockList = [];

					this.gestureEvents = null;
					this.velocity = null;
				};

				Manager.prototype = {
					/**
					 * Bind start events
					 * @method _bindStartEvents
					 * @param {ns.event.gesture.Instance} instance gesture instance
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_bindStartEvents: function( instance ) {
						var element = instance.getElement();
						if ( TOUCH_DEVICE ) {
							element.addEventListener( "touchstart", this);
						}

						element.addEventListener( "mousedown", this);
					},

					/**
					 * Bind move, end and cancel events
					 * @method _bindEvents
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_bindEvents: function( ) {
						if ( TOUCH_DEVICE ) {
							document.addEventListener( "touchmove", this);
							document.addEventListener( "touchend", this);
							document.addEventListener( "touchcancel", this);
						}

						document.addEventListener( "mousemove", this);
						document.addEventListener( "mouseup", this);
					},

					/**
					 * Unbind start events
					 * @method _unbindStartEvents
					 * @param {ns.event.gesture.Instance} instance gesture instance
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_unbindStartEvents: function( instance ) {
						var element = instance.getElement();
						if ( TOUCH_DEVICE ) {
							element.removeEventListener( "touchstart", this);
						}

						element.removeEventListener( "mousedown", this);
					},

					/**
					 * Unbind move, end and cancel events
					 * @method _bindEvents
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_unbindEvents: function() {
						if ( TOUCH_DEVICE ) {
							document.removeEventListener( "touchmove", this);
							document.removeEventListener( "touchend", this);
							document.removeEventListener( "touchcancel", this);
						}

						document.removeEventListener( "mousemove", this);
						document.removeEventListener( "mouseup", this);
					},

					/**
					 * Handle event
					 * @method handleEvent
					 * @param {Event} event
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					/* jshint -W086 */
					handleEvent: function( event ) {
						var eventType = event.type.toLowerCase();

						if ( eventType.match(/touch/) ) {
							blockMouseEvent = true;
						}

						if ( eventType.match(/mouse/) &&
							( blockMouseEvent || event.which !== 1 ) ) {
							return;
						}

						switch ( event.type ) {
							case "mousedown":
							case "touchstart":
								this._start( event );
								break;
							case "mousemove":
							case "touchmove":
								this._move( event );
								break;
							case "mouseup":
							case "touchend":
								this._end( event );
								break;
							case "touchcancel":
								this._cancel( event );
								break;
						}
					},

					/**
					 * Handler for gesture start
					 * @method _start
					 * @param {Event} event
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_start: function( event ) {
						var elem = event.currentTarget,
							startEvent = {},
							detectors = [];

						if ( !isReadyDetecting ) {
							this._resetDetecting();
							this._bindEvents();

							startEvent = this._createDefaultEventData( Gesture.Event.START, event );

							this.gestureEvents = {
								start: startEvent,
								last: startEvent
							};

							this.velocity = {
								event: startEvent,
								x: 0,
								y: 0
							};

							startEvent = objectMerge(startEvent, this._createGestureEvent(Gesture.Event.START, event));
							isReadyDetecting = true;
						}

						this.instances.forEach(function( instance ) {
							if ( instance.getElement() === elem ) {
								detectors = detectors.concat( instance.getGestureDetectors() );
							}
						}, this);

						detectors.sort(function(a, b) {
							if(a.index < b.index) {
								return -1;
							} else if(a.index > b.index) {
								return 1;
							}
							return 0;
						});

						this.gestureDetectors = this.gestureDetectors.concat( detectors );

						this._detect(detectors, startEvent);
					},

					/**
					 * Handler for gesture move
					 * @method _move
					 * @param {Event} event
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_move: function( event ) {
						if ( !isReadyDetecting ) {
							return;
						}

						event = this._createGestureEvent(Gesture.Event.MOVE, event);
						this._detect(this.gestureDetectors, event);

						this.gestureEvents.last = event;
					},

					/**
					 * Handler for gesture end
					 * @method _end
					 * @param {Event} event
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_end: function( event ) {

						event = objectMerge(
							{},
							this.gestureEvents.last,
							this._createDefaultEventData(Gesture.Event.END, event)
						);

						if ( event.pointers.length > 0 ) {
							return;
						}

						this._detect(this.gestureDetectors, event);

						this.unregisterBlockList.forEach(function( instance ) {
							this.unregist( instance );
						}, this);

						this._resetDetecting();
						blockMouseEvent = false;
					},

					/**
					 * Handler for gesture cancel
					 * @method _cancel
					 * @param {Event} event
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_cancel: function( event ) {

						event = objectMerge(
							{},
							this.gestureEvents.last,
							this._createDefaultEventData(Gesture.Event.CANCEL, event)
						);

						this._detect(this.gestureDetectors, event);

						this.unregisterBlockList.forEach(function( instance ) {
							this.unregist( instance );
						}, this);

						this._resetDetecting();
						blockMouseEvent = false;
					},

					/**
					 * Detect gesture
					 * @method _detect
					 * @param {Event} event
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_detect: function( detectors, event ) {
						var finishedDetectors = [];

						detectors.forEach(function( detector ) {
							var result;

							if ( this.detectorRequestedBlock ) {
								return;
							}

							result = detector.detect( event );
							if ( result & Gesture.Result.RUNNING ) {
								if ( this.runningDetectors.indexOf( detector ) < 0 ) {
									this.runningDetectors.push( detector );
								}
							}

							if ( result & Gesture.Result.FINISHED ) {
								finishedDetectors.push( detector );
							}

							if ( result & Gesture.Result.BLOCK ) {
								this.detectorRequestedBlock = detector;
							}

						}, this);

						// remove finished detectors.
						finishedDetectors.forEach(function( detector ) {
							var idx;

							idx = this.gestureDetectors.indexOf( detector );
							if ( idx > -1 ) {
								this.gestureDetectors.splice(idx, 1);
							}

							idx = this.runningDetectors.indexOf( detector );
							if ( idx > -1 ) {
								this.runningDetectors.splice(idx, 1);
							}
						}, this);

						// remove all detectors except the detector that return block result
						if ( this.detectorRequestedBlock ) {
							// send to cancel event.
							this.runningDetectors.forEach(function( detector ) {
								var cancelEvent = objectMerge({}, event, {
									eventType: Gesture.Event.BLOCKED
								});
								detector.detect( cancelEvent );
							});
							this.runningDetectors.length = 0;

							// remove all detectors.
							this.gestureDetectors.length = 0;
							if ( finishedDetectors.indexOf( this.detectorRequestedBlock ) < 0 ) {
								this.gestureDetectors.push( this.detectorRequestedBlock );
							}
						}
					},

					/**
					 * Reset of gesture manager detector
					 * @method _resetDetecting
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_resetDetecting: function() {
						isReadyDetecting = false;
						startEvent = null

						this.gestureDetectors.length = 0;
						this.runningDetectors.length = 0;
						this.detectorRequestedBlock = null;

						this.gestureEvents = null;
						this.velocity = null;

						this._unbindEvents();
					},

					/**
					 * Create default event data
					 * @method _createDefaultEventData
					 * @param {string} type event type
					 * @param {Event} event source event
					 * @return {Object} default event data
					 * @return {string} return.eventType
					 * @return {number} return.timeStamp
					 * @return {Touch} return.pointer
					 * @return {TouchList} return.pointers
					 * @return {Event} return.srcEvent
					 * @return {Function} return.preventDefault
					 * @return {Function} return.stopPropagation
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_createDefaultEventData: function( type, event ) {
						var pointers = event.touches ?
								event.touches :
									event.type === "mouseup" ? [] : ( event.identifier=1 && [event] ),
							pointer = pointers[0],
							timeStamp = new Date().getTime();

						return {
							eventType: type,
							timeStamp: timeStamp,
							pointer: pointer,
							pointers: pointers,

							srcEvent: event,
							preventDefault: function() {
								this.srcEvent.preventDefault();
							},
							stopPropagation: function() {
								this.srcEvent.stopPropagation();
							}
						};
					},

					/**
					 * Create gesture event
					 * @method _createGestureEvent
					 * @param {string} type event type
					 * @param {Event} event source event
					 * @return {Object} gesture event consist from Event class and additional properties
					 * @return {number} return.deltaTime
					 * @return {number} return.deltaX
					 * @return {number} return.deltaY
					 * @return {number} return.velocityX
					 * @return {number} return.velocityY
					 * @return {number} return.estimatedX
					 * @return {number} return.estimatedY
					 * @return {number} return.estimatedDeltaX
					 * @return {number} return.estimatedDeltaY
					 * @return {number} return.distance
					 * @return {number} return.angle
					 * @return {ns.event.gesture.Direction.LEFT|ns.event.gesture.Direction.RIGHT|ns.event.gesture.Direction.UP|ns.event.gesture.Direction.DOWN} return.direction
					 * @return {number} return.scale
					 * @return {number} return.rotation (deg)
					 * @return {Event} return.startEvent
					 * @return {Event} return.lastEvent
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_createGestureEvent: function( type, event ) {
						var ev = this._createDefaultEventData( type, event ),
							startEvent = this.gestureEvents.start,
							lastEvent = this.gestureEvents.last,
							velocityEvent = this.velocity.event,
							delta = {
								time: ev.timeStamp - startEvent.timeStamp,
								x: ev.pointer.clientX - startEvent.pointer.clientX,
								y: ev.pointer.clientY - startEvent.pointer.clientY
							},
							deltaFromLast = {
								x: ev.pointer.clientX - lastEvent.pointer.clientX,
								y: ev.pointer.clientY - lastEvent.pointer.clientY
							},
							velocity = this.velocity,
							timeDifference = Gesture.defaults.estimatedPointerTimeDifference, /* pause time threshold.util. tune the number to up if it is slow */
							estimated;

						// reset start event for multi touch
						if( startEvent && ev.pointers.length !== startEvent.pointers.length ) {
							startEvent.pointers = [];
							[].forEach.call(ev.pointers, function( pointer ) {
								startEvent.pointers.push( objectMerge({}, pointer) );
							});
						}

						if ( ev.timeStamp - velocityEvent.timeStamp > Gesture.defaults.updateVelocityInterval ) {
							velocity = Gesture.utils.getVelocity(
									ev.timeStamp - velocityEvent.timeStamp,
									ev.pointer.clientX - velocityEvent.pointer.clientX,
									ev.pointer.clientY - velocityEvent.pointer.clientY
							);

							objectMerge(this.velocity, velocity, {
								event: ev
							});
						}

						estimated = {
							x: Math.round( ev.pointer.clientX + ( timeDifference * velocity.x * (deltaFromLast.x < 0 ? -1 : 1) ) ),
							y: Math.round( ev.pointer.clientY + ( timeDifference * velocity.y * (deltaFromLast.y < 0 ? -1 : 1) ) )
						};

						// Prevent that point goes back even though direction is not changed.
						if ( (deltaFromLast.x < 0 && estimated.x > lastEvent.estimatedX) ||
							(deltaFromLast.x > 0 && estimated.x < lastEvent.estimatedX) ) {
							estimated.x = lastEvent.estimatedX;
						}

						if ( (deltaFromLast.y < 0 && estimated.y > lastEvent.estimatedY) ||
							(deltaFromLast.y > 0 && estimated.y < lastEvent.estimatedY) ) {
							estimated.y = lastEvent.estimatedY;
						}

						objectMerge(ev, {
							deltaTime: delta.time,
							deltaX: delta.x,
							deltaY: delta.y,

							velocityX: velocity.x,
							velocityY: velocity.y,

							estimatedX: estimated.x,
							estimatedY: estimated.y,
							estimatedDeltaX: estimated.x - startEvent.pointer.clientX,
							estimatedDeltaY: estimated.y - startEvent.pointer.clientY,

							distance: Gesture.utils.getDistance(startEvent.pointer, ev.pointer),

							angle: Gesture.utils.getAngle(startEvent.pointer, ev.pointer),

							direction: Gesture.utils.getDirection(startEvent.pointer, ev.pointer),

							scale: Gesture.utils.getScale(startEvent.pointers, ev.pointers),
							rotation: Gesture.utils.getRotation(startEvent.pointers, ev.pointers),

							startEvent: startEvent,
							lastEvent: lastEvent
						});

						return ev;
					},

					/**
					 * Register instance of gesture
					 * @method register
					 * @param {ns.event.gesture.Instance} instance gesture instance
					 * @member ns.event.gesture.Manager
					 */
					register: function( instance ) {
						var idx = this.instances.indexOf( instance );
						if ( idx < 0 ) {
							this.instances.push( instance );
							this._bindStartEvents( instance );
						}
					},

					/**
					 * Unregister instance of gesture
					 * @method unregister
					 * @param {ns.event.gesture.Instance} instance gesture instance
					 * @member ns.event.gesture.Manager
					 */
					unregister: function( instance ) {
						var idx;

						if ( !!this.gestureDetectors.length ) {
							this.unregisterBlockList.push( instance );
							return;
						}

						idx = this.instances.indexOf( instance );
						if ( idx > -1 ) {
							this.instances.splice( idx, 1 );
							this._unbindStartEvents( instance );
						}

						if ( !this.instances.length ) {
							this._destroy();
						}
					},

					/**
					 * Destroy instance of Manager
					 * @method _destroy
					 * @member ns.event.gesture.Manager
					 * @protected
					 */
					_destroy: function() {
						this._resetDetecting();

						this.instances.length = 0;
						this.unregisterBlockList.length = 0;

						blockMouseEvent = false;
						instance = null;
					}

				};

				return {
					getInstance: function() {
						return instance ? instance : ( instance = new Manager() );
					}
				};
			})();
			} ( ns, window, window.document ) );

/*global ns, window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Gesture.Instance class
 * Creates instance of gesture manager on element.
 * @class ns.event.gesture.Instance
 */
( function ( ns, window, undefined ) {
	
					/**
				 * Local alias for {@link ns.event.gesture}
				 * @property {Object}
				 * @member ns.event.gesture.Instance
				 * @private
				 * @static
				 */
			var Gesture = ns.event.gesture,
				/**
				 * Local alias for {@link ns.event.gesture.Detector}
				 * @property {Object}
				 * @member ns.event.gesture.Instance
				 * @private
				 * @static
				 */
				Detector = ns.event.gesture.Detector,
				/**
				 * Local alias for {@link ns.event.gesture.Manager}
				 * @property {Object}
				 * @member ns.event.gesture.Instance
				 * @private
				 * @static
				 */
				Manager = ns.event.gesture.Manager,
				/**
				 * Local alias for {@link ns.event}
				 * @property {Object}
				 * @member ns.event.gesture.Instance
				 * @private
				 * @static
				 */
				events = ns.event,
				/**
				 * Alias for method {@link ns.util.object.merge}
				 * @property {Function} merge
				 * @member ns.event.gesture.Instance
				 * @private
				 * @static
				 */
				merge = ns.util.object.merge;

			Gesture.Instance = function( element, options ) {

				this.element = element;
				this.eventDetectors = [];

				this.options = merge({}, Gesture.defaults, options);
				this.gestureManager = null;

				this._init();
			};

			Gesture.Instance.prototype = {
				/**
				 * Initialize gesture instance
				 * @method _init
				 * @member ns.event.gesture.Instance
				 * @protected
				 */
				_init: function() {
					this.gestureManager = Manager.getInstance();
					this.eventSender = merge({}, Detector.Sender, {
						sendEvent: this.trigger.bind(this)
					});
				},

				/**
				 * Find gesture detector
				 * @method _findGestureDetector
				 * @param {string} gesture gesture
				 * @member ns.event.gesture.Instance
				 * @protected
				 */
				_findGestureDetector: function( gesture ) {
					var detectors = Detector.plugin,
						detector, name;
					for ( name in detectors ) {
						if ( detectors.hasOwnProperty( name ) ) {
							detector = detectors[ name ];
							if ( detector.prototype.types.indexOf( gesture ) > -1 ) {
								return detector;
							}
						}
					}
				},

				/**
				 * Set options
				 * @method setOptions
				 * @param {Object} options options
				 * @chainable
				 * @member ns.event.gesture.Instance
				 */
				setOptions: function( options ) {
					merge(this.options, options);
					return this;
				},

				/**
				 * Add detector
				 * @method addDetector
				 * @param {Object} detectorStrategy strategy
				 * @chainable
				 * @member ns.event.gesture.Instance
				 */
				addDetector: function( detectorStrategy ) {
					var detector = new Detector( detectorStrategy, this.eventSender ),
						alreadyHasDetector = !!this.eventDetectors.length;

					this.eventDetectors.push(detector);

					if ( !!this.eventDetectors.length && !alreadyHasDetector ) {
						this.gestureManager.register(this);
					}

					return this;
				},

				/**
				 * Remove detector
				 * @method removeDetector
				 * @param {Object} detectorStrategy strategy
				 * @chainable
				 * @member ns.event.gesture.Instance
				 */
				removeDetector: function( detectorStrategy ) {
					var idx = this.eventDetectors.indexOf( detectorStrategy );

					if ( idx > -1 ) {
						this.eventDetectors.splice(idx, 1);
					}

					if ( !this.eventDetectors.length ) {
						this.gestureManager.unregister(this);
					}

					return this;
				},

				/**
				 * Triggers the gesture event
				 * @method trigger
				 * @param {string} gesture gesture name
				 * @param {Object} eventInfo data provided to event object
				 * @member ns.event.gesture.Instance
				 */
				trigger: function( gesture, eventInfo ) {
					return events.trigger(this.element, gesture, eventInfo, false);
				},

				/**
				 * Get HTML element assigned to gesture event instance
				 * @method getElement
				 * @member ns.event.gesture.Instance
				 */
				getElement: function() {
					return this.element;
				},

				/**
				 * Get gesture event detectors assigned to instance
				 * @method getGestureDetectors
				 * @member ns.event.gesture.Instance
				 */
				getGestureDetectors: function() {
					return this.eventDetectors;
				},

				/**
				 * Destroy instance
				 * @method destroy
				 * @member ns.event.gesture.Instance
				 */
				destroy: function( ) {
					this.element = null;
					this.eventHandlers = {};
					this.gestureManager = null;
					this.eventSender = null;
					this.eventDetectors.length = 0;
				}
			};
			} ( ns, window ) );

/*global ns, window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * # Gesture Plugin: drag
 * Plugin enables drag event.
 *
 * @class ns.event.gesture.Drag
 */
( function ( ns, window, undefined ) {
	
	
				/**
				 * Local alias for {@link ns.event.gesture}
				 * @property {Object}
				 * @member ns.event.gesture.Drag
				 * @private
				 * @static
				 */
			var Gesture = ns.event.gesture,
				/**
				 * Local alias for {@link ns.event.gesture.Detector}
				 * @property {Object}
				 * @member ns.event.gesture.Drag
				 * @private
				 * @static
				 */
				Detector = ns.event.gesture.Detector,
				/**
				 * Alias for method {@link ns.util.object.merge}
				 * @property {Function} merge
				 * @member ns.event.gesture.Drag
				 * @private
				 * @static
				 */
				merge = ns.util.object.merge,

				// TODO UA test will move to support.
				tizenBrowser = !!window.navigator.userAgent.match(/tizen/i);

			ns.event.gesture.Drag = Detector.plugin.create({

				/**
				 * Gesture name
				 * @property {string} [name="drag"]
				 * @member ns.event.gesture.Drag
				 */
				name: "drag",

				/**
				 * Gesture Index
				 * @property {number} [index=400]
				 * @member ns.event.gesture.Drag
				 */
				index: 500,

				/**
				 * Array of posible drag events
				 * @property {string[]} types
				 * @member ns.event.gesture.Drag
				 */
				types: ["drag", "dragstart", "dragend", "dragcancel"],

				/**
				 * Default values for drag gesture
				 * @property {Object} defaults
				 * @property {boolean} [defaults.blockHorizontal=false]
				 * @property {boolean} [defaults.blockVertical=false]
				 * @property {number} [defaults.threshold=10]
				 * @property {number} [defaults.delay=0]
				 * @member ns.event.gesture.Drag
				 */
				defaults: {
					blockHorizontal: false,
					blockVertical: false,
					threshold: 20,
					delay: 0
				},

				/**
				 * Triggered
				 * @property {boolean} [triggerd=false]
				 * @member ns.event.gesture.Drag
				 */
				triggerd: false,

				/**
				 * Handler for drag gesture
				 * @method handler
				 * @param {Event} gestureEvent gesture event
				 * @param {Object} sender event's sender
				 * @param {Object} options options
				 * @return {ns.event.gesture.Result.PENDING|ns.event.gesture.Result.END|ns.event.gesture.Result.FINISHED|ns.event.gesture.Result.BLOCK}
				 * @member ns.event.gesture.Drag
				 */
				handler: function( gestureEvent, sender, options ) {
					var ge = gestureEvent,
						threshold = options.threshold,
						result = Gesture.Result.PENDING,
						event = {
							drag: this.types[0],
							start: this.types[1],
							end: this.types[2],
							cancel: this.types[3]
						},
						direction = ge.direction;

					if ( !this.triggerd && ge.eventType === Gesture.Event.MOVE ) {
						if ( Math.abs(ge.deltaX) < threshold && Math.abs(ge.deltaY) < threshold ) {
							if ( !tizenBrowser ) {
								ge.preventDefault();
							}
							return Gesture.Result.PENDING;
						}

						if ( options.delay && ge.deltaTime < options.delay ) {
							if ( !tizenBrowser ) {
								ge.preventDefault();
							}
							return Gesture.Result.PENDING;
						}
						if ( options.blockHorizontal && Gesture.utils.isHorizontal( ge.direction ) ||
							options.blockVertical && Gesture.utils.isVertical( ge.direction ) ) {
							return Gesture.Result.FINISHED;
						}
						this.fixedStartPointX = 0;
						this.fixedStartPointY = 0;
						if ( Gesture.utils.isHorizontal( ge.direction ) ) {
							this.fixedStartPointX = ( ge.deltaX < 0 ? 1 : -1 ) * threshold;
						} else {
							this.fixedStartPointY = ( ge.deltaY < 0 ? 1 : -1 ) * threshold;
						}
					}

					if ( options.blockHorizontal ) {
						direction = ge.deltaY < 0 ? Gesture.Direction.UP : Gesture.Direction.DOWN;
					}

					if ( options.blockVertical ) {
						direction = ge.deltaX < 0 ? Gesture.Direction.LEFT : Gesture.Direction.RIGHT;
					}

					ge = merge({}, ge, {
						deltaX: ge.deltaX + this.fixedStartPointX,
						deltaY: ge.deltaY + this.fixedStartPointY,
						estimatedDeltaX: ge.estimatedDeltaX + this.fixedStartPointX,
						estimatedDeltaY: ge.estimatedDeltaY + this.fixedStartPointY,

						direction: direction
					});

					switch( ge.eventType ) {
						case Gesture.Event.START:
							this.triggerd = false;
							break;
						case Gesture.Event.MOVE:
							if ( !this.triggerd ) {
								sender.sendEvent( event.start, ge );
							}
							result = sender.sendEvent( event.drag, ge ) ? Gesture.Result.RUNNING : Gesture.Result.FINISHED;
							ge.preventDefault();
							this.triggerd = true;
							break;

						case Gesture.Event.BLOCKED:
						case Gesture.Event.END:
							result = Gesture.Result.FINISHED;
							if ( this.triggerd ) {
								sender.sendEvent( event.end, ge );
								ge.preventDefault();
								this.triggerd = false;
							}
							break;

						case Gesture.Event.CANCEL:
							result = Gesture.Result.FINISHED;
							if ( this.triggerd ) {
								sender.sendEvent( event.cancel, ge );
								ge.preventDefault();
								this.triggerd = false;
							}
							break;

					}

					return result;
				}
			});
			} ( ns, window ) );

/*global ns, window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Gesture Plugin: swipe
 * Plugin enables swipe event.
 *
 * @class ns.event.gesture.Swipe
 */
( function ( ns, window, undefined ) {
	
    
				/**
				 * Local alias for {@link ns.event.gesture}
				 * @property {Object}
				 * @member ns.event.gesture.Swipe
				 * @private
				 * @static
				 */
			var Gesture = ns.event.gesture,
				/**
				 * Local alias for {@link ns.event.gesture.Detector}
				 * @property {Object}
				 * @member ns.event.gesture.Swipe
				 * @private
				 * @static
				 */
				Detector = ns.event.gesture.Detector;

			ns.event.gesture.Swipe = Detector.plugin.create({
				/**
				 * Gesture name
				 * @property {string} [name="swipe"]
				 * @member ns.event.gesture.Swipe
				 */
				name: "swipe",

				/**
				 * Gesture Index
				 * @property {number} [index=400]
				 * @member ns.event.gesture.Swipe
				 */
				index: 400,

				/**
				 * Default values for swipe gesture
				 * @property {Object} defaults
				 * @property {number} [defaults.timeThreshold=400]
				 * @property {number} [defaults.velocity=0.6]
				 * @property {ns.event.gesture.HORIZONTAL|ns.event.gesture.VERTICAL} [defaults.orientation=ns.event.gesture.HORIZONTAL]
				 * @member ns.event.gesture.Swipe
				 */
				defaults: {
					timeThreshold: 400,
					velocity: 0.6,
					orientation: Gesture.Orientation.HORIZONTAL
				},

				/**
				 * Handler for swipe gesture
				 * @method handler
				 * @param {Event} gestureEvent gesture event
				 * @param {Object} sender event's sender
				 * @param {Object} options options
				 * @return {ns.event.gesture.Result.PENDING|ns.event.gesture.Result.END|ns.event.gesture.Result.FINISHED|ns.event.gesture.Result.BLOCK}
				 * @member ns.event.gesture.Swipe
				 */
				handler: function( gestureEvent, sender, options ) {
					var ge = gestureEvent,
						result = Gesture.Result.PENDING;

					if ( ge.eventType !== Gesture.Event.END ) {
						return result;
					}

					if ( ( ge.deltaTime > options.timeThreshold ) ||
						( options.orientation !== Gesture.utils.getOrientation( ge.direction ) ) ) {
						result = Gesture.Result.FINISHED;
						return result;
					}

					if( ge.velocityX > options.velocity || ge.velocityY > options.velocity ) {
						sender.sendEvent( this.name, gestureEvent );
						result = Gesture.Result.FINISHED | Gesture.Result.BLOCK;
					}

					return result;
				}
			});
			} ( ns, window ) );

/*global window, define, CustomEvent */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @class ns.event.gesture
 */
(function (ns) {
	
				var instances = [],
				gesture = ns.event.gesture || {};

			/**
			 * Find instance by element
			 * @method findInstance
			 * @param {HTMLElement} element
			 * @return {ns.event.gesture.Instance}
			 * @member ns.event
			 * @static
			 * @private
			 */
			function findInstance(element) {
				var instance;
				instances.forEach(function(item) {
					if (item.element === element) {
						instance = item.instance;
					}
				});
				return instance;
			}

			/**
			 * Remove instance from instances by element
			 * @method removeInstance
			 * @param {HTMLElement} element
			 * @member ns.event
			 * @static
			 * @private
			 */
			function removeInstance(element) {
				instances.forEach(function(item, key) {
					if (item.element === element) {
						instances.splice(key, 1);
					}
				});
			}

			/**
			 * Enable gesture handlingo on given HTML element or object
			 * @method enableGesture
			 * @param {HTMLElement} element
			 * @param {...Object} [gesture] Gesture object {@link ns.event.gesture}
			 * @member ns.event
			 */
			ns.event.enableGesture = function() {
				var element = arguments[0],
					gestureInstance = findInstance( element ),
					length = arguments.length,
					i = 1;

				if ( !gestureInstance ) {
					gestureInstance = new gesture.Instance(element);
					instances.push({element: element, instance: gestureInstance});
				}

				for ( ; i < length; i++ ) {
					gestureInstance.addDetector( arguments[i] );
				}
			};

			/**
			 * Disable gesture handling from given HTML element or object
			 * @method disableGesture
			 * @param {HTMLElement} element
			 * @param {...Object} [gesture] Gesture object {@link ns.event.gesture}
			 * @member ns.event
			 */
			ns.event.disableGesture = function() {
				var element = arguments[0],
					gestureInstance = findInstance( element ),
					length = arguments.length,
					i = 1;

				if ( !gestureInstance ) {
					return;
				}

				if ( length > 1 ) {
					gestureInstance.removeDetector( arguments[i] );
				} else {
					gestureInstance.destroy();
					removeInstance( element );
				}
			};

			ns.event.gesture = gesture;
			}(ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 */
(function (ns) {
	
				ns.widget.core.viewswitcher = ns.widget.core.viewswitcher || {};
			}(ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * #type namespace
 * ViewSwitcher animation
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 * @class ns.widget.core.ViewSwitcher.animation
 */
(function (window, ns) {
	
				/** @namespace ns.widget.wearable */
			ns.widget.core.viewswitcher.animation = ns.widget.core.viewswitcher.animation || {};
			}(window, ns));

/*global window, define, Event, console, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * #Animation Interface
 * Interface for animation for used viewswitcher
 * @class ns.widget.core.viewswitcher.animation.interface
 */
(function (document, ns) {
	
	
			ns.widget.core.viewswitcher.animation.interface = {
				/**
				 * Init views position
				 * @method initPosition
				 * @param views array
				 * @param active index
				 * @static
				 * @member ns.widget.core.viewswitcher.animation.interface
				 */
				initPosition: function (/* views array, active index */) {
				},
				/**
				 * Animate views
				 * @method animate
				 * @param views array
				 * @param active index
				 * @param position
				 * @static
				 * @member ns.widget.core.viewswitcher.animation.interface
				 */
				animate: function (/* views array, active index, position */) {
				},
				/**
				 * Reset views position
				 * @method resetPosition
				 * @param views array
				 * @param active index
				 * @static
				 * @member ns.widget.core.viewswitcher.animation.interface
				 */
				resetPosition: function (/* views array, active index */) {
				}
			};
			}(window.document, ns));

/*global window, define, Event, console, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * #Animation carousel
 *
 * carousel is animation type of ViewSwitcher
 *
 * @class ns.widet.core.ViewSwitcher.animation.carousel
 * @extends ns.widget.core.ViewSwitcher.animation.interface
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 */
(function (document, ns) {
	
	
			var object = ns.util.object,
				utilDOM = ns.util.DOM,
				animation = ns.widget.core.viewswitcher.animation,
				animationInterface = animation.interface,
				DEFAULT = {
					PERSPECTIVE: 280,
					ZINDEX_TOP: 3,
					ZINDEX_MIDDLE: 2,
					ZINDEX_BOTTOM: 1,
					DIM_LEVEL: 6
				},
				options = {
					useDim: true,
					dimLevel: DEFAULT.DIM_LEVEL
				},
				classes = {
					CAROUSEL: "ui-view-carousel",
					CAROUSEL_ACTIVE: "ui-view-carousel-active",
					CAROUSEL_LEFT: "ui-view-carousel-left",
					CAROUSEL_RIGHT: "ui-view-carousel-right",
					CAROUSEL_DIM: "ui-view-carousel-dim"
				};

			function translate(element, x, y, z, duration) {
				if (duration) {
					utilDOM.setPrefixedStyle(element, "transition", utilDOM.getPrefixedValue("transform " + duration / 1000 + "s ease-out"));
				}

				utilDOM.setPrefixedStyle(element, "transform", "translate3d(" + x + "px, " + y + "px, " + z + "px)");
			}

			function resetStyle(element) {
				element.style.left = "";
				element.style.right = "";
				element.style.zIndex = DEFAULT.ZINDEX_MIDDLE;
				element.style.transform = "translateZ(" + -element.parentNode.offsetWidth / 2 + "px)";
				element.style.webkitTransform = "translateZ(" + -element.parentNode.offsetWidth / 2 + "px)";
			}

			animation.carousel = object.merge({}, animationInterface, {
				/**
				 * Init views position
				 * @method initPosition
				 * @param views array
				 * @param active index
				 * @static
				 * @member ns.widget.core.ViewSwitcher.animation.interface
				 */
				initPosition: function (views, index) {
					var viewSwitcher = views[0].parentNode,
						vsOffsetWidth = viewSwitcher.offsetWidth,
						dimElement,
						i, len;

					viewSwitcher.classList.add(classes.CAROUSEL);
					viewSwitcher.style.webkitPerspective = DEFAULT.PERSPECTIVE;
					if (options.useDim) {
						len = views.length;
						for (i = 0; i < len; i++) {
							dimElement = document.createElement("DIV");
							dimElement.classList.add(classes.CAROUSEL_DIM);
							views[i].appendChild(dimElement);
						}
					}
					views[index].classList.add(classes.CAROUSEL_ACTIVE);
					if (index > 0) {
						views[index - 1].classList.add(classes.CAROUSEL_LEFT);
						views[index - 1].style.transform = "translateZ(" + -vsOffsetWidth / 2 + "px)";
					}
					if (index < views.length - 1) {
						views[index + 1].classList.add(classes.CAROUSEL_RIGHT);
						views[index + 1].style.transform = "translateZ(" + -vsOffsetWidth / 2 + "px)";
					}
				},
				/**
				 * Animate views
				 * @method animate
				 * @param views array
				 * @param active index
				 * @param position [0 - 100 or -100 - 0]
				 * @static
				 * @member ns.widget.core.ViewSwitcher.animation.interface
				 */
				animate: function (views, index, position) {
					var viewSwitcher = views[0].parentNode,
						vsWidth = viewSwitcher.offsetWidth,
						vsHalfWidth = vsWidth / 2,
						left = index > 0 ? views[index - 1] : undefined,
						right = index < views.length - 1 ? views[index + 1] : undefined,
						active = views[index],
						ex = position / 100 * vsWidth,
						halfEx = ex / 2,
						centerPosition = (vsHalfWidth - active.offsetWidth / 2),
						adjPosition = (centerPosition/ (vsHalfWidth * 0.6)),
						absEx = Math.abs(ex),
						absPosition = Math.abs(position),
						mark = position < 0 ? 1 : -1,
						edge = vsHalfWidth * 0.2 * mark,
						// edgeDeltaX -> -mark * (2 * (0.8 * vsHalfWidth)) - halfEx
						edgeDeltaX = -mark * 1.6 * vsHalfWidth - halfEx,
						minusDeltaX = -vsHalfWidth - halfEx,
						plusDeltaX = -vsHalfWidth + halfEx,
						hidingDeltaX = -halfEx * 0.2,
						prev, next, beforePrev, afterNext;

					active.style.left = (vsWidth - active.offsetWidth) / 2 + "px";
					active.style.zIndex = DEFAULT.ZINDEX_TOP;

					next = ex < 0 ? right : left;
					afterNext = ex < 0 ? (next && next.nextElementSibling) : (next && next.previousElementSibling);
					prev = ex < 0 ? left : right;
					beforePrev = ex < 0 ? (prev && prev.previousElementSibling) : (prev && prev.nextElementSibling);

					if (next) {
						if (absEx < vsWidth * 0.2) {
							next.style.zIndex = DEFAULT.ZINDEX_MIDDLE;
							translate(next, -halfEx * adjPosition, 0, ex < 0 ? minusDeltaX : plusDeltaX);
						} else {
							active.style.zIndex = DEFAULT.ZINDEX_MIDDLE;
							next.style.zIndex = DEFAULT.ZINDEX_TOP;
							translate(next, (2 * edge + halfEx) * adjPosition, 0, ex < 0 ? minusDeltaX : plusDeltaX);
						}
						if (afterNext) {
							afterNext.classList.add(ex < 0 ? classes.CAROUSEL_RIGHT : classes.CAROUSEL_LEFT);
							translate(afterNext, (ex < 0 ? minusDeltaX : -plusDeltaX) * 0.6, 0, -vsWidth - halfEx * mark);
						}
					}
					if (prev) {
						if (beforePrev) {
							beforePrev.classList.remove(ex < 0 ? classes.CAROUSEL_LEFT : classes.CAROUSEL_RIGHT);
						}
						prev.style.zIndex = DEFAULT.ZINDEX_BOTTOM;
						translate(prev, hidingDeltaX, 0, ex < 0 ? plusDeltaX : minusDeltaX);
					}
					if (absEx < vsWidth * 0.8) {
						translate(active, halfEx * adjPosition, 0, halfEx * mark);
					} else {
						translate(active, edgeDeltaX * adjPosition, 0, halfEx * mark);
					}
					if (options.useDim) {
						active.querySelector("." + classes.CAROUSEL_DIM).style.opacity = absPosition * options.dimLevel / 1000;
						if (next) {
							next.querySelector("." + classes.CAROUSEL_DIM).style.opacity = options.dimLevel / 10 * (1 - absPosition / 100);
						}
					}
				},
				/**
				 * Reset views position
				 * @method resetPosition
				 * @param views array
				 * @param active index
				 * @static
				 * @member ns.widget.core.ViewSwitcher.animation.interface
				 */
				resetPosition: function (views, index) {
					var viewSwitcher = views[0].parentNode,
						active = views[index],
						rightElements = viewSwitcher.querySelectorAll("." + classes.CAROUSEL_RIGHT),
						leftElements = viewSwitcher.querySelectorAll("." + classes.CAROUSEL_LEFT),
						i, len;

					viewSwitcher.querySelector("." + classes.CAROUSEL_ACTIVE).classList.remove(classes.CAROUSEL_ACTIVE);
					active.classList.add(classes.CAROUSEL_ACTIVE);
					active.style.transform = "";
					active.style.webkitTransform = "";
					len = rightElements.length;
					for (i = 0; i < len; i++) {
						rightElements[i].classList.remove(classes.CAROUSEL_RIGHT);
					}
					if (index < views.length - 1) {
						views[index + 1].classList.add(classes.CAROUSEL_RIGHT);
						resetStyle(views[index + 1]);
					}
					len = leftElements.length;
					for (i = 0; i < len; i++) {
						leftElements[i].classList.remove(classes.CAROUSEL_LEFT);
					}
					if (index > 0) {
						views[index - 1].classList.add(classes.CAROUSEL_LEFT);
						resetStyle(views[index - 1]);
					}
				}
			});

			animation.carousel.options = options;
			}(window.document, ns));

/*global window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #ViewSwitcher Component
 * ViewSwitcher component is controller for each view elements is changing position.
 * This component managed to animation, views position, events and get/set active view index.
 * If you want to change the view as various animating, you should wrap views as the ViewSwitcher element then
 * ViewSwitcher would set views position and start to manage to gesture event.
 *
 * ##Set and Get the active index
 * You can set or get the active index as the setActiveIndex() and getActiveIndex()
 *
 * @class ns.widget.core.viewswitcher.ViewSwitcher
 * @extends ns.widget.BaseWidget
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 */
(function (document, ns) {
	
				/**
			 * @property {Object} Widget Alias for {@link ns.widget.BaseWidget}
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @private
			 * @static
			 */
			var BaseWidget = ns.widget.BaseWidget,
				events = ns.event,
				engine = ns.engine,
				utilsObject = ns.util.object,
				Gesture = ns.event.gesture,
				/**
				 * Default values
				 */
				DEFAULT = {
					ACTIVE_INDEX: 0,
					ANIMATION_TYPE: "carousel",
					ANIMATION_SPEED: 30,
					ANIMATION_TIMING_FUNCTION: "ease-out"
				},
				/**
				 * ViewSwitcher triggered some customEvents
				 * viewchangestart : This event has been triggerred when view changing started.
				 * viewchangeend : This event has been triggerred when view changing ended.
				 * viewchange: This event has been triggerred when view changing complete to user.
				 */
				EVENT_TYPE = {
					CHANGE_START: "viewchangestart",
					CHANGE_END: "viewchangeend",
					CHANGE: "viewchange"
				},
				/**
				 * ViewSwitcher constructor
				 * @method ViewSwitcher
				 */
				ViewSwitcher = function () {
					var self = this;

					self.options = {};
					self._ui = {};
				},
				/**
				 * Dictionary object containing commonly used widget classes
				 * @property {Object} classes
				 * @member ns.widget.core.viewswitcher.ViewSwitcher
				 * @private
				 * @static
				 * @readonly
				 */
				classes = {
					VIEW: "ui-view",
					VIEW_ACTIVE: "ui-view-active",
					ANIMATION_TYPE: "ui-animation-"
				},
				/**
				 * {Object} ViewSwitcher widget prototype
				 * @member ns.widget.core.viewswitcher.ViewSwitcher
				 * @private
				 * @static
				 */
				prototype = new BaseWidget();

			ViewSwitcher.prototype = prototype;
			ViewSwitcher.classes = classes;

			/**
			 * Configure of ViewSwitcher component
			 * @method _configure
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._configure = function () {
				var self = this;

				/**
				 * ViewSwitcher containing some options
				 * @property {number} ViewSwitcher default active index (Default is 0)
				 * @property {string} ViewSwitcher animation type (Default is "carousel")
				 * @property {number} ViewSwitcher animation speed (Default is 18)
				 */
				self.options = utilsObject.merge(self.options, {
					active: DEFAULT.ACTIVE_INDEX,
					animationType: DEFAULT.ANIMATION_TYPE,
					animationSpeed: DEFAULT.ANIMATION_SPEED
				});
			};
			/**
			 * Build structure of ViewSwitcher component
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement} Returns built element
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._build = function (element) {
				var self = this,
					ui = self._ui;

				ui._views = element.querySelectorAll("." + classes.VIEW);
				return element;
			};

			/**
			 * Initialization of ViewSwitcher component
			 * @method _init
			 * @param {HTMLElement} element
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._init = function (element) {
				var self = this;

				self._elementOffsetWidth = element.offsetWidth;
				self._initPosition();

				return element;
			};

			/**
			 * Init position of Views inner ViewSwitcher
			 * @method _initPosition
			 * @param {HTMLElement} element
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._initPosition = function () {
				var self = this,
					views = self._ui._views,
					options = self.options,
					activeIndex = self._getActiveIndex();

				self._type = ns.widget.core.viewswitcher.animation[options.animationType];
				self._type.initPosition(views, activeIndex);
				self._activeIndex = activeIndex;
			};

			/**
			 * Get the active index as view has the "ui-view-active" or not
			 * @method _getActiveIndex
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._getActiveIndex = function() {
				var self = this,
					ui = self._ui,
					views = ui._views,
					i, len;

				len = views.length;
				for (i = 0; i < len; i++) {
					if (views[i].classList.contains(classes.VIEW_ACTIVE)) {
						return i;
					}
				}
				return self.options.active;
			};

			/**
			 * Binds events to a ViewSwitcher component
			 * @method _bindEvents
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._bindEvents = function() {
				var self = this,
					element = self.element;

				events.enableGesture(
					element,
					new events.gesture.Drag({
						blockVertical: true,
						threshold: 0
					}),
					new events.gesture.Swipe({
						orientation: Gesture.Orientation.HORIZONTAL
					})
				);
				events.on(element, "drag dragstart dragend swipe", self, false);

			};

			/**
			 * Handle events
			 * @method handleEvent
			 * @param {Event} event
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 */
			prototype.handleEvent = function(event) {
				var self = this;
				switch (event.type) {
					case "drag":
						self._onDrag(event);
						break;
					case "dragstart":
						self._onDragStart(event);
						break;
					case "dragend":
					case "swipe":
						self._onDragEnd(event);
						break;
				}
			};

			/**
			 * Drag event handler
			 * @method _onDrag
			 * @param {Event} event
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._onDrag = function(event) {
				var self = this,
					direction = event.detail.direction,
					ex = event.detail.estimatedDeltaX,
					deltaX = ex / self._elementOffsetWidth * 100,
					ui = self._ui,
					active = ui._views[self._activeIndex];

				if ((direction === "left" && !active.nextElementSibling) || (direction === "right" && !active.previousElementSibling)) {
					return;
				}
				if (self._dragging && !self._isAnimating && Math.abs(deltaX) < 100) {
					self._type.animate(ui._views, self._activeIndex, deltaX);
					self._triggerChange(deltaX);
				}
			};

			/**
			 * DragStart event handler
			 * @method _onDragStart
			 * @param {Event} event
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._onDragStart = function(event) {
				var self = this,
					direction = event.detail.direction,
					ui = self._ui,
					active = ui._views[self._activeIndex];

				if ((direction === "left" && !active.nextElementSibling) || (direction === "right" && !active.previousElementSibling) || self._dragging) {
					return;
				}
				self._dragging = true;
			};

			/**
			 * DragEnd event handler
			 * @method _onDragEnd
			 * @param {Event} event
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._onDragEnd = function(event) {
				var self = this,
					ui = self._ui,
					active = ui._views[self._activeIndex],
					direction = event.detail.direction,
					estimatedDeltaX = event.detail.estimatedDeltaX;

				if (!self._dragging || self._isAnimating
					|| (direction === "left" && !active.nextElementSibling) || (direction === "right" && !active.previousElementSibling)) {
					return;
				}
				self._lastDirection = direction;
				if (event.type === "dragend" && Math.abs(estimatedDeltaX) < self._elementOffsetWidth / 2) {
					direction = "backward";
				}
				self.trigger(EVENT_TYPE.CHANGE_START);
				self._requestFrame(estimatedDeltaX, direction);
			};

			prototype._triggerChange = function(estimatedDeltaX) {
				var self = this,
					absEx = Math.abs(estimatedDeltaX);
				if (absEx > 50 && !self._changed) {
					self.trigger(EVENT_TYPE.CHANGE, {
						index: self._activeIndex + (estimatedDeltaX < 0 ? 1 : -1)
					});
					self._changed = true;
				} else if (absEx < 50 && self._changed){
					self.trigger(EVENT_TYPE.CHANGE, {
						index: self._activeIndex
					});
					self._changed = false;
				}
			};
			/**
			 * Animate views as the requestAnimationFrame.
			 * @method _requestFrame
			 * @param {string} animation direction
			 * @param {string} animation timing type (ease-out|linear)
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._requestFrame = function(estimatedDeltaX, direction, animationTiming) {
				var self = this,
					elementOffsetWidth = self._elementOffsetWidth,
					animationTimingFunction = animationTiming ? animationTiming : DEFAULT.ANIMATION_TIMING_FUNCTION,
					isStop = false,
					lastDirection = self._lastDirection,
					ui = self._ui,
					ex = estimatedDeltaX,
					deltaX =  ex / elementOffsetWidth * 100,
					animationFrame,
					validDirection,
					stopPosition,
					mark;

				if (direction === "backward") {
					validDirection = lastDirection === "left" ? "right" : "left";
					if (lastDirection === "left" && ex > 0
						|| lastDirection === "right" && ex < 0) {
						isStop = true;
						stopPosition = 0;
					}
				} else {
					validDirection = direction;
					if (Math.abs(ex) > elementOffsetWidth) {
						isStop = true;
						stopPosition = 100;
					}
				}
				mark = validDirection === "left" ? -1 : 1;
				if (isStop) {
					self._type.animate(ui._views, self._activeIndex, stopPosition * mark);
					webkitCancelRequestAnimationFrame(animationFrame);
					if (direction !== "backward") {
						ui._views[self._activeIndex].classList.remove(classes.VIEW_ACTIVE);
						self._activeIndex = self._activeIndex - mark;
						self._type.resetPosition(ui._views, self._activeIndex);
						ui._views[self._activeIndex].classList.add(classes.VIEW_ACTIVE);
					}
					self._dragging = false;
					self._isAnimating = false;
					self._changed = false;
					self.trigger(EVENT_TYPE.CHANGE_END);
					return;
				}
				self._type.animate(ui._views, self._activeIndex, deltaX);
				self._triggerChange(deltaX);
				self._isAnimating = true;

				if (animationTimingFunction === "ease-out") {
					if (Math.abs(ex) > elementOffsetWidth * 0.95) {
						ex = ex + mark;
					} else {
						ex = ex + self.options.animationSpeed * mark;
					}
				} else if (animationTimingFunction === "linear") {
					ex = ex + self.options.animationSpeed * mark;
				}
				animationFrame = webkitRequestAnimationFrame(self._requestFrame.bind(self, ex, direction, animationTiming));
			};
			/**
			 * Set the active view
			 * @method setActiveIndex
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @public
			 */
			prototype.setActiveIndex = function(index) {
				var self = this,
					latestActiveIndex = self._activeIndex,
					interval = latestActiveIndex - index,
					direction, i, len;

				if (!self._isAnimating && index < self._ui._views.length && index >= 0) {
					self._lastDeltaX = 0;
					if (interval < 0) {
						direction = "left";
					} else {
						direction = "right";
					}
					len = Math.abs(interval);
					self._lastDirection = direction;
					for (i = 0; i < len; i++) {
						self.trigger(EVENT_TYPE.CHANGE_START);
						self._requestFrame(0, direction, "linear");
					}
				}
			};

			/**
			 * Get the active view index
			 * @method getActiveIndex
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @public
			 */
			prototype.getActiveIndex = function() {
				return this._activeIndex;
			};
			/**
			 * Destroys ViewSwitcher widget
			 * @method _destroy
			 * @member ns.widget.core.viewswitcher.ViewSwitcher
			 * @protected
			 */
			prototype._destroy = function() {
				var element = this.element;
				events.disableGesture(element);
				events.off(element, "drag dragstart dragend", this, false);
				this.options = null;
				this._ui = null;
			};

			ns.widget.core.viewswitcher.ViewSwitcher = ViewSwitcher;

			engine.defineWidget(
				"ViewSwitcher",
				"[data-role='viewSwitcher'], .ui-view-switcher",
				[
					"setActiveIndex",
					"getActiveIndex"
				],
				ViewSwitcher
			);
			}(window.document, ns));

/*global window, define, NodeList, HTMLCollection */
/*jslint plusplus: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author Jadwiga Sosnowska <j.sosnowska@partner.samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Maciej Moczulski <m.moczulski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (window, document, ns) {
	
	
			/**
			 * @property {DocumentFragment} fragment
			 * @member ns.util.DOM
			 * @private
			 * @static
			 */
			/*
			 * @todo maybe can be moved to function scope?
			 */
			var fragment = document.createDocumentFragment(),
				/**
				 * @property {DocumentFragment} fragment2
				 * @member ns.util.DOM
				 * @private
				 * @static
				 */
				/*
				 * @todo maybe can be moved to function scope?
				 */
				fragment2 = document.createDocumentFragment(),
				/**
				 * @property {number} [containerCounter=0]
				 * @member ns.util.DOM
				 * @private
				 * @static
				 */
				/*
				 * @todo maybe can be moved to function scope?
				 */
				containerCounter = 0,
				/**
				 * Alias to Array.slice method
				 * @method slice
				 * @member ns.util.DOM
				 * @private
				 * @static
				 */
				slice = [].slice,
				DOM = ns.util.DOM;

			/**
			 * Appends node or array-like node list array to context
			 * @method appendNodes
			 * @member ns.util.DOM
			 * @param {HTMLElement} context
			 * @param {HTMLElement|HTMLCollection|NodeList|Array} elements
			 * @return {HTMLElement|Array|null}
			 * @static
			 * @throws {string}
			 */
			DOM.appendNodes = function (context, elements) {
				var i,
					len;
				if (context) {
					if (elements instanceof Array || elements instanceof NodeList || elements instanceof HTMLCollection) {
						elements = slice.call(elements);
						for (i = 0, len = elements.length; i < len; ++i) {
							context.appendChild(elements[i]);
						}
					} else {
						context.appendChild(elements);
					}
					return elements;
				}

				throw "Context empty!";
			};

			/**
			 * Replaces context with node or array-like node list
			 * @method replaceWithNodes
			 * @member ns.util.DOM
			 * @param {HTMLElement} context
			 * @param {HTMLElement|HTMLCollection|NodeList|Array} elements
			 * @return {HTMLElement|Array|null}
			 * @static
			 */
			DOM.replaceWithNodes = function (context, elements) {
				if (elements instanceof Array || elements instanceof NodeList || elements instanceof HTMLCollection) {
					elements = this.insertNodesBefore(context, elements);
					context.parentNode.removeChild(context);
				} else {
					context.parentNode.replaceChild(elements, context);
				}
				return elements;
			};

			/**
			 * Remove all children
			 * @method removeAllChildren
			 * @member ns.util.DOM
			 * @param {HTMLElement} context
			 * @static
			 */
			DOM.removeAllChildren = function (context) {
				context.innerHTML = "";
			};

			/**
			 * Inserts node or array-like node list before context
			 * @method insertNodesBefore
			 * @member ns.util.DOM
			 * @param {HTMLElement} context
			 * @param {HTMLElement|HTMLCollection|NodeList|Array} elements
			 * @return {HTMLElement|Array|null}
			 * @static
			 * @throws {string}
			 */
			DOM.insertNodesBefore = function (context, elements) {
				var i,
					len,
					parent;
				if (context) {
					parent = context.parentNode;
					if (elements instanceof Array || elements instanceof NodeList || elements instanceof HTMLCollection) {
						elements = slice.call(elements);
						for (i = 0, len = elements.length; i < len; ++i) {
							parent.insertBefore(elements[i], context);
						}
					} else {
						parent.insertBefore(elements, context);
					}
					return elements;
				}

				throw "Context empty!";

			};

			/**
			 * Inserts node after context
			 * @method insertNodeAfter
			 * @member ns.util.DOM
			 * @param {HTMLElement} context
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @static
			 * @throws {string}
			 */
			DOM.insertNodeAfter = function (context, element) {
				if (context) {
					context.parentNode.insertBefore(element, context.nextSibling);
					return element;
				}
				throw "Context empty!";
			};

			/**
			 * Wraps element or array-like node list in html markup
			 * @method wrapInHTML
			 * @param {HTMLElement|NodeList|HTMLCollection|Array} elements
			 * @param {string} html
			 * @return {HTMLElement|NodeList|Array} wrapped element
			 * @member ns.util.DOM
			 * @static
			 */
			DOM.wrapInHTML = function (elements, html) {
				var container = document.createElement("div"),
					contentFlag = false,
					elementsLen = elements.length,
					//if elements is nodeList, retrieve parentNode of first node
					originalParentNode = elementsLen ? elements[0].parentNode : elements.parentNode,
					next = elementsLen ? elements[elementsLen - 1].nextSibling : elements.nextSibling,
					innerContainer;

				fragment.appendChild(container);
				html = html.replace(/(\$\{content\})/gi, function () {
					contentFlag = true;
					return "<span id='temp-container-" + (++containerCounter) + "'></span>";
				});
				container.innerHTML = html;

				if (contentFlag === true) {
					innerContainer = container.querySelector("span#temp-container-" + containerCounter);
					elements = this.replaceWithNodes(innerContainer, elements);
				} else {
					innerContainer = container.children[0];
					elements = this.appendNodes(innerContainer || container, elements);
				}

				// move the nodes
				while (fragment.firstChild.firstChild) {
					fragment2.appendChild(fragment.firstChild.firstChild);
				}

				// clean up
				while (fragment.firstChild) {
					fragment.removeChild(fragment.firstChild);
				}

				if (originalParentNode) {
					if (next) {
						originalParentNode.insertBefore(fragment2, next);
					} else {
						originalParentNode.appendChild(fragment2);
					}
				} else {
					// clean up
					while (fragment2.firstChild) {
						fragment2.removeChild(fragment2.firstChild);
					}
				}
				return elements;
			};
			}(window, window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * #Wearable Widget Reference
 * The Tizen Web UI service provides rich Tizen widgets that are optimized for the Tizen Web browser. You can use the widgets for:
 *
 * - CSS animation
 * - Rendering
 *
 * The following table displays the widgets provided by the Tizen Web UI service.
 * @class ns.widget.wearable
 * @seeMore https://developer.tizen.org/dev-guide/2.2.1/org.tizen.web.uiwidget.apireference/html/web_ui_framework.htm "Web UI Framework Reference"
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 */
(function (window, ns) {
	
				ns.widget.wearable = ns.widget.wearable || {};
			}(window, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/**
 * # Page Widget
 * Page is main element of application's structure.
 *
 * ## Default selectors
 * In the Tizen Web UI framework the application page structure is based on a header, content and footer elements:
 *
 * - **The header** is placed at the top, and displays the page title and optionally buttons.
 * - **The content** is the section below the header, showing the main content of the page.
 * - **The footer** is a bottom part of page which can display for example buttons
 *
 * The following table describes the specific information for each section.
 *
 * <table>
 *     <tr>
 *         <th>Section</th>
 *         <th>Class</th>
 *         <th>Mandatory</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *         <td rowspan="2">Page</td>
 *         <td>ui-page</td>
 *         <td>Yes</td>
 *         <td>Defines the element as a page.
 *
 * The page widget is used to manage a single item in a page-based architecture.
 *
 * A page is composed of header (optional), content (mandatory), and footer (optional) elements.</td>
 *      </tr>
 *      <tr>
 *          <td>ui-page-active</td>
 *          <td>No</td>
 *          <td>If an application has a static start page, insert the ui-page-active class in the page element to speed up the application launch. The start page with the ui-page-active class can be displayed before the framework is fully loaded.
 *
 *If this class is not used, the framework inserts the class automatically to the first page of the application. However, this has a slowing effect on the application launch, because the page is displayed only after the framework is fully loaded.</td>
 *      </tr>
 *      <tr>
 *          <td>Header</td>
 *          <td>ui-header</td>
 *          <td>No</td>
 *          <td>Defines the element as a header.</td>
 *      </tr>
 *      <tr>
 *          <td>Content</td>
 *          <td>ui-content</td>
 *          <td>Yes</td>
 *          <td>Defines the element as content.</td>
 *      </tr>
 *      <tr>
 *          <td>Footer</td>
 *          <td>ui-footer</td>
 *          <td>No</td>
 *          <td>Defines the element as a footer.
 *
 * The footer section is mostly used to include option buttons.</td>
 *      </tr>
 *  </table>
 *
 * All elements with class=ui-page will be become page widgets
 *
 *      @example
 *         <!--Page layout-->
 *         <div class="ui-page ui-page-active">
 *             <header class="ui-header"></header>
 *             <div class="ui-content"></div>
 *             <footer class="ui-footer"></footer>
 *         </div>
 *
 *         <!--Page layout with more button in header-->
 *         <div class="ui-page ui-page-active">
 *             <header class="ui-header ui-has-more">
 *                 <h2 class="ui-title">Call menu</h2>
 *                 <button type="button" class="ui-more ui-icon-overflow">More Options</button>
 *             </header>
 *             <div class="ui-content">Content message</div>
 *             <footer class="ui-footer">
 *                 <button type="button" class="ui-btn">Footer Button</button>
 *             </footer>
 *         </div>
 *
 * ## Manual constructor
 * For manual creation of page widget you can use constructor of widget from **tau** namespace:
 *
 *		@example
 *		var pageElement = document.getElementById("page"),
 *			page = tau.widget.page(buttonElement);
 *
 * Constructor has one require parameter **element** which are base **HTMLElement** to create widget. We recommend get this element by method *document.getElementById*.
 *
 * ## Multi-page Layout
 *
 * You can implement a template containing multiple page containers in the application index.html file.
 *
 * In the multi-page layout, the main page is defined with the ui-page-active class. If no page has the ui-page-active class, the framework automatically sets up the first page in the source order as the main page. You can improve the launch performance by explicitly defining the main page to be displayed first. If the application has to wait for the framework to set up the main page, the page is displayed with some delay only after the framework is fully loaded.
 *
 * You can link to internal pages by referring to the ID of the page. For example, to link to the page with an ID of two, the link element needs the href="#two" attribute in the code, as in the following example.
 *
 *      @example
 *         <!--Main page-->
 *         <div id="one" class="ui-page ui-page-active">
 *             <header class="ui-header"></header>
 *             <div class="ui-content"></div>
 *             <footer class="ui-footer"></footer>
 *         </div>
 *
 *         <!--Secondary page-->
 *         <div id="two" class="ui-page">
 *             <header class="ui-header"></header>
 *             <div class="ui-content"></div>
 *             <footer class="ui-footer"></footer>
 *         </div>
 *
 * To find the currently active page, use the ui-page-active class.
 *
 * ## Changing Pages
 * ### Go to page in JavaScript
 * To change page use method *tau.changePage*
 *
 *      @example
 *      tau.changePage("page-two");
 *
 * ### Back in JavaScript
 * To back to previous page use method *tau.back*
 *
 *      @example
 *      tau.back();
 *
 * ## Transitions
 *
 * When changing the active page, you can use a page transition.
 *
 * Tizen Web UI Framework does not apply transitions by default. To set a custom transition effect, you must add the data-transition attribute to a link:
 *
 *      @example
 *      <a href="index.html" data-transition="slideup">I'll slide up</a>
 *
 * To set a default custom transition effect for all pages, use the pageTransition property:
 *
 *      @example
 *      tau.defaults.pageTransition = "slideup";
 *
 * ### Transitions list
 *
 *  - **none** no transition.
 *  - **slideup** Makes the content of the next page slide up, appearing to conceal the content of the previous page.
 *
 * ## Handling Page Events
 *
 * With page widget we have connected many of events.
 *
 * To handle page events, use the following code:
 *
 *      @example
 *        <div id="page" class="ui-page">
 *             <header class="ui-header"></header>
 *             <div class="ui-content"></div>
 *         </div>
 *
 *         <script>
 *             var page = document.getElementById("page");
 *             page.addEventListener("Event", function(event) {
 *                 // Your code
 *             });
 *         </script>
 *
 * To bind an event callback on the Back key, use the following code:
 *
 * Full list of available events is in [events list section](#events-list).
 *
 * To bind an event callback on the Back key, use the following code:
 *
 *      @example
 *         <script>
 *             window.addEventListener("tizenhwkey", function (event) {
 *                 if (event.keyName == "back") {
 *                     // Call window.history.back() to go to previous browser window
 *                     // Call tizen.application.getCurrentApplication().exit() to exit application
 *                     // Add script to add another behavior
 *                 }
 *             });
 *         </script>
 *
 * ## Options for Page Widget
 *
 * Page widget hasn't any options.
 *
 * ## Methods
 *
 * To call method on widget you can use tau API:
 *
 *		@example
 *		var pageElement = document.getElementById("page"),
 *			page = tau.widget.page(buttonElement);
 *
 *		page.methodName(methodArgument1, methodArgument2, ...);
 *
 * @class ns.widget.wearable.Page
 * @extends ns.widget.core.Page
 * @author hyunkook cho <hk0713.cho@samsung.com>
 */
(function (document, ns) {
	
				/**
			 * Alias for {@link ns.widget.BaseWidget}
			 * @property {Object} BaseWidget
			 * @member ns.widget.core.Page
			 * @private
			 * @static
			 */
			var CorePage = ns.widget.core.Page,
				/**
				 * Alias for {@link ns.util}
				 * @property {Object} util
				 * @member ns.widget.wearable.Page
				 * @private
				 * @static
				 */
				util = ns.util,
				/**
				 * Alias for {@link ns.util.DOM}
				 * @property {Object} doms
				 * @member ns.widget.wearable.Page
				 * @private
				 * @static
				 */
				doms = util.DOM,
				/**
				 * Alias for {@link ns.util.selectors}
				 * @property {Object} selectors
				 * @member ns.widget.wearable.Page
				 * @private
				 * @static
				 */
				selectors = util.selectors,
				/**
				 * Alias for {@link ns.util.object}
				 * @property {Object} object
				 * @member ns.widget.wearable.Page
				 * @private
				 * @static
				 */
				object = ns.util.object,
				/**
				 * Alias for {@link ns.event}
				 * @property {Object} object
				 * @member ns.widget.wearable.Page
				 * @private
				 * @static
				 */
				utilsEvents = ns.event,
				/**
				 * Alias for {@link ns.event.gesture}
				 * @property {Object} object
				 * @member ns.widget.wearable.Page
				 * @private
				 * @static
				 */
				Gesture = utilsEvents.gesture,
				/**
				 * Alias for {@link ns.engine}
				 * @property {Object} engine
				 * @member ns.widget.wearable.Page
				 * @private
				 * @static
				 */
				engine = ns.engine,

				scrollBarType = {
					CIRCLE: "tizen-circular-scrollbar"
				},

				Page = function () {
					var self = this;
					CorePage.call(self);
					self._contentStyleAttributes = ["height", "width", "minHeight", "marginTop", "marginBottom"];
				},
				/**
				 * Dictionary for page related css class names
				 * @property {Object} classes
				 * @member ns.widget.core.Page
				 * @static
				 * @readonly
				 */
				classes = object.merge({
					uiHeader: "ui-header",
					uiContent: "ui-content",
					uiPageScroll: "ui-scroll-on",
					uiScroller: "ui-scroller",
					uiFixed: "ui-fixed"
				}, CorePage.classes),

				prototype = new CorePage();

			/**
			 * Configure Page Widget
			 * @method _configure
			 * @member ns.widget.wearable.Page
			 */
			prototype._configure = function () {
				CorePage.prototype._configure.call(this);
				this.options.enablePageScroll = ns.getConfig("enablePageScroll");
			};
			/**
			 * Sets top-bottom css attributes for content element
			 * to allow it to fill the page dynamically
			 * @method _contentFill
			 * @member ns.widget.wearable.Page
			 */
			prototype._contentFill = function () {
				var self = this,
					option = self.options,
					element = self.element,
					screenWidth = window.innerWidth,
					screenHeight = window.innerHeight,
					pageScrollSelector = classes.uiPageScroll,
					children = [].slice.call(element.children),
					elementStyle = element.style,
					scroller,
					content,
					fragment,
					firstChild;

				elementStyle.width = screenWidth + "px";
				elementStyle.height = screenHeight + "px";

				if (option.enablePageScroll === true && !element.querySelector("." + classes.uiScroller)) {
					element.classList.add(pageScrollSelector);
					scroller = document.createElement("div");
					scroller.classList.add(classes.uiScroller);
					fragment = document.createDocumentFragment();

					children.forEach( function(value) {
						if ( selectors.matchesSelector(value, ".ui-header:not(.ui-fixed), .ui-content, .ui-footer:not(.ui-fixed)")) {
							fragment.appendChild(value);
						}
					});

					if (element.children.length > 0 && element.children[0].classList.contains(classes.uiHeader)) {
						doms.insertNodeAfter(element.children[0], scroller);
					} else {
						element.insertBefore(scroller, element.firstChild);
					}

					firstChild = fragment.firstChild;

					scroller.appendChild(fragment);
				}

				if (tau.support.shape.circle) {
					if (scroller) {
						scroller.setAttribute(scrollBarType.CIRCLE, "");
					}
					content = element.querySelector("." + classes.uiContent);
					if (content) {
						content.setAttribute(scrollBarType.CIRCLE, "");
					}
				}
			};

			prototype.getScroller = function() {
				var element = this.element,
					scroller = element.querySelector("." + classes.uiScroller);
				return scroller || element.querySelector("." + classes.uiContent) || element;
			};

			prototype._destroy = function () {
				CorePage.prototype._destroy.call(this);
			};

			Page.prototype = prototype;

			// definition
			ns.widget.wearable.Page = Page;
			engine.defineWidget(
				"Page",
				"[data-role=page],.ui-page",
				[
					"layout",
					"focus",
					"blur",
					"setActive",
					"isActive"
				],
				Page,
				"wearable",
				true
			);

			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Callback Utility
 * Class creates a callback list
 *
 * Create a callback list using the following parameters:
 *  options: an optional list of space-separated options that will change how
 *			the callback list behaves or a more traditional option object
 *
 * By default a callback list will act like an event callback list and can be
 * "fired" multiple times.
 *
 * Possible options:
 *
 *	once:			will ensure the callback list can only be fired once (like a Deferred)
 *
 *	memory:			will keep track of previous values and will call any callback added
 *					after the list has been fired right away with the latest "memorized"
 *					values (like a Deferred)
 *
 *	unique:			will ensure a callback can only be added once (no duplicate in the list)
 *
 *	stopOnFalse:	interrupt callings when a callback returns false
 * @class ns.util.callbacks
 */
(function (window, document, ns) {
	
				ns.util.callbacks = function (orgOptions) {

				var object = ns.util.object,
					options = object.copy(orgOptions),
					/**
					 * Alias to Array.slice function
					 * @method slice
					 * @member ns.util.callbacks
					 * @private
					 */
					slice = [].slice,
					/**
					 * Last fire value (for non-forgettable lists)
					 * @property {Object} memory
					 * @member ns.util.callbacks
					 * @private
					 */
					memory,
					/**
					 * Flag to know if list was already fired
					 * @property {boolean} fired
					 * @member ns.util.callbacks
					 * @private
					 */
					fired,
					/**
					 * Flag to know if list is currently firing
					 * @property {boolean} firing
					 * @member ns.util.callbacks
					 * @private
					 */
					firing,
					/**
					 * First callback to fire (used internally by add and fireWith)
					 * @property {number} [firingStart=0]
					 * @member ns.util.callbacks
					 * @private
					 */
					firingStart,
					/**
					 * End of the loop when firing
					 * @property {number} firingLength
					 * @member ns.util.callbacks
					 * @private
					 */
					firingLength,
					/**
					 * Index of currently firing callback (modified by remove if needed)
					 * @property {number} firingIndex
					 * @member ns.util.callbacks
					 * @private
					 */
					firingIndex,
					/**
					 * Actual callback list
					 * @property {Array} list
					 * @member ns.util.callbacks
					 * @private
					 */
					list = [],
					/**
					 * Stack of fire calls for repeatable lists
					 * @property {Array} stack
					 * @member ns.util.callbacks
					 * @private
					 */
					stack = !options.once && [],
					fire,
					add,
					self = {
						/**
						 * Add a callback or a collection of callbacks to the list
						 * @method add
						 * @param {..Function} list
						 * @return {ns.util.callbacks} self
						 * @chainable
						 * @member ns.util.callbacks
						 */
						add: function () {
							if (list) {
								// First, we save the current length
								var start = list.length;
								add(arguments);
								// Do we need to add the callbacks to the
								// current firing batch?
								if (firing) {
									firingLength = list.length;
								// With memory, if we're not firing then
								// we should call right away
								} else if (memory) {
									firingStart = start;
									fire(memory);
								}
							}
							return this;
						},
						/**
						 * Remove a callback from the list
						 * @method remove
						 * @param {..Function} list
						 * @return {ns.util.callbacks} self
						 * @chainable
						 * @member ns.util.callbacks
						 */
						remove: function () {
							if (list) {
								slice.call(arguments).forEach(function (arg) {
									var index = list.indexOf(arg);
									while (index > -1) {
										list.splice(index, 1);
										// Handle firing indexes
										if (firing) {
											if (index <= firingLength) {
												firingLength--;
											}
											if (index <= firingIndex) {
												firingIndex--;
											}
										}
										index = list.indexOf(arg, index);
									}
								});
							}
							return this;
						},
						/**
						 * Check if a given callback is in the list. 
						 * If no argument is given,
						 * return whether or not list has callbacks attached.
						 * @method has
						 * @param {Funciton} fn
						 * @return {boolean}
						 * @member ns.util.callbacks
						 */
						has: function (fn) {
							return fn ? !!list && list.indexOf(fn) > -1 : !!(list && list.length);
						},
						/**
						 * Remove all callbacks from the list
						 * @method empty
						 * @return {ns.util.callbacks} self
						 * @chainable
						 * @member ns.util.callbacks
						 */
						empty: function () {
							list = [];
							firingLength = 0;
							return this;
						},
						/**
						 * Have the list do nothing anymore
						 * @method disable
						 * @return {ns.util.callbacks} self
						 * @chainable
						 * @member ns.util.callbacks
						 */
						disable: function () {
							list = stack = memory = undefined;
							return this;
						},
						/**
						 * Is it disabled?
						 * @method disabled
						 * @return {boolean}
						 * @member ns.util.callbacks
						 */
						disabled: function () {
							return !list;
						},
						/**
						 * Lock the list in its current state
						 * @method lock
						 * @return {ns.util.callbacks} self
						 * @chainable
						 * @member ns.util.callbacks
						 */
						lock: function () {
							stack = undefined;
							if (!memory) {
								self.disable();
							}
							return this;
						},
						/**
						 * Is it locked?
						 * @method locked
						 * @return {boolean} stack
						 * @member ns.util.callbacks
						 */
						locked: function () {
							return !stack;
						},
						/**
						 * Call all callbacks with the given context and
						 * arguments
						 * @method fireWith
						 * @param {Object} context
						 * @param {Array} args
						 * @return {ns.util.callbacks} self
						 * @chainable
						 * @member ns.util.callbacks
						 */
						fireWith: function (context, args) {
							if (list && (!fired || stack)) {
								args = args || [];
								args = [context, args.slice ? args.slice() : args];
								if (firing) {
									stack.push(args);
								} else {
									fire(args);
								}
							}
							return this;
						},
						/**
						 * Call all the callbacks with the given arguments
						 * @method fire
						 * @param {...*} argument
						 * @return {ns.util.callbacks} self
						 * @chainable
						 * @member ns.util.callbacks
						 */
						fire: function () {
							self.fireWith(this, arguments);
							return this;
						},
						/**
						 * To know if the callbacks have already been called at
						 * least once
						 * @method fired
						 * @return {booblean}
						 * @chainable
						 * @member ns.util.callbacks
						 */
						fired: function () {
							return !!fired;
						}
					};
				/**
				 * Adds functions to the callback list
				 * @method add
				 * @param {...*} argument
				 * @member ns.util.bezierCurve
				 * @private
				 */
				add = function (args) {
					slice.call(args).forEach(function (arg) {
						var type = typeof arg;
						if (type === "function") {
							if (!options.unique || !self.has(arg)) {
								list.push(arg);
							}
						} else if (arg && arg.length && type !== "string") {
							// Inspect recursively
							add(arg);
						}
					});
				};
				/**
				 * Fire callbacks
				 * @method fire
				 * @param {Array} data
				 * @member ns.util.bezierCurve
				 * @private
				 */
				fire = function (data) {
					memory = options.memory && data;
					fired = true;
					firingIndex = firingStart || 0;
					firingStart = 0;
					firingLength = list.length;
					firing = true;
					while (list && firingIndex < firingLength) {
						if (list[firingIndex].apply(data[0], data[1]) === false && options.stopOnFalse) {
							memory = false; // To prevent further calls using add
							break;
						}
						firingIndex++;
					}
					firing = false;
					if (list) {
						if (stack) {
							if (stack.length) {
								fire(stack.shift());
							}
						} else if (memory) {
							list = [];
						} else {
							self.disable();
						}
					}
				};

				return self;
			};

			}(window, window.document, ns));

/*global window, define, RegExp */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Deferred Utility
 * Class creates object which can call registered callback depend from
 * state of object..
 * @class ns.util.deferred
 * @author Tomasz Lukawski <t.lukawski@samsung.com>
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */(function (window, document, ns) {
	
	
			var Deferred = function (callback) {
				var callbacks = ns.util.callbacks,
					object = ns.util.object,
					/**
					 * Register additional action for deferred object
					 * @property {Array} tuples
					 * @member ns.util.deferred
					 * @private
					 */
					tuples = [
						// action, add listener, listener list, final state
						["resolve", "done", callbacks({once: true, memory: true}), "resolved"],
						["reject", "fail", callbacks({once: true, memory: true}), "rejected"],
						["notify", "progress", callbacks({memory: true})]
					],
					state = "pending",
					deferred = {},
					promise = {
						/**
						 * Determine the current state of a Deferred object.
						 * @method state
						 * @return {"pending" | "resolved" | "rejected"} representing the current state
						 * @member ns.util.deferred
						 */
						state: function () {
							return state;
						},
						/**
						 * Add handlers to be called when the Deferred object
						 * is either resolved or rejected.
						 * @method always
						 * @param {...Function}
						 * @return {ns.util.deferred} self
						 * @member ns.util.deferred
						 */
						always: function () {
							deferred.done(arguments).fail(arguments);
							return this;
						},
						/**
						 * Add handlers to be called when the Deferred object
						 * is resolved, rejected, or still in progress.
						 * @method then
						 * @param {?Function} callback assign when done
						 * @param {?Function} callback assign when fail
						 * @param {?Function} callback assign when progress
						 * @return {Object} returns a new promise
						 * @member ns.util.deferred
						 */
						then: function () {/* fnDone, fnFail, fnProgress */
							var functions = arguments;
							return new Deferred(function (newDefer) {
								tuples.forEach(function (tuple, i) {
									var fn = (typeof functions[i] === 'function') && functions[i];
									// deferred[ done | fail | progress ] for forwarding actions to newDefer
									deferred[tuple[1]](function () {
										var returned = fn && fn.apply(this, arguments);
										if (returned && (typeof returned.promise === 'function')) {
											returned.promise()
												.done(newDefer.resolve)
												.fail(newDefer.reject)
												.progress(newDefer.notify);
										} else {
											newDefer[tuple[0] + "With"](this === promise ? newDefer.promise() : this, fn ? [returned] : arguments);
										}
									});
								});
								functions = null;
							}).promise();
						},
						/**
						 * Get a promise for this deferred. If obj is provided,
						 * the promise aspect is added to the object
						 * @method promise
						 * @param {Object} obj
						 * @return {Object} return a Promise object
						 * @member ns.util.deferred
						 */
						promise: function (obj) {
							if (obj) {
								return object.merge(obj, promise);
							}
							return promise;
						}
					};

				/**
				 * alias for promise.then, Keep pipe for back-compat
				 * @method pipe
				 * @member ns.util.deferred
				 */
				promise.pipe = promise.then;

				// Add list-specific methods

				tuples.forEach(function (tuple, i) {
					var list = tuple[2],
						stateString = tuple[3];

					// promise[ done | fail | progress ] = list.add
					promise[tuple[1]] = list.add;

					// Handle state
					if (stateString) {
						list.add(function () {
							// state = [ resolved | rejected ]
							state = stateString;

						// [ reject_list | resolve_list ].disable; progress_list.lock
						}, tuples[i ^ 1][2].disable, tuples[2][2].lock);
					}

					// deferred[ resolve | reject | notify ]
					deferred[tuple[0]] = function () {
						deferred[tuple[0] + "With"](this === deferred ? promise : this, arguments);
						return this;
					};
					deferred[tuple[0] + "With"] = list.fireWith;
				});

				// Make the deferred a promise
				promise.promise(deferred);

				// Call given func if any
				if (callback) {
					callback.call(deferred, deferred);
				}

				// All done!
				return deferred;
			};
			ns.util.deferred = Deferred;
			}(window, window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * # Popup Widget
 *
 * @author Hyunkook Cho <hk0713.cho@samsung.com>
 * @class ns.widget.core.Popup
 * @extends ns.widget.Popup
 */
(function (ns) {
	
					/**
				 * Alias for {@link ns.widget.BaseWidget}
				 * @property {Function} BaseWidget
				 * @member ns.widget.core.Popup
				 * @private
				 */
			var BaseWidget = ns.widget.BaseWidget,
				/**
				 * Alias for class ns.engine
				 * @property {ns.engine} engine
				 * @member ns.widget.core.Popup
				 * @private
				 */
				engine = ns.engine,
				/**
				 * Alias for class ns.util.object
				 * @property {Object} objectUtils
				 * @member ns.widget.core.Popup
				 * @private
				 */
				objectUtils = ns.util.object,
				/**
				 * Alias for class ns.util.deferred
				 * @property {Object} UtilDeferred
				 * @member ns.widget.core.Popup
				 * @private
				 */
				UtilDeferred = ns.util.deferred,
				/**
				 * Alias for class ns.util.selectors
				 * @property {Object} utilSelector
				 * @member ns.widget.core.Popup
				 * @private
				 */
				utilSelector = ns.util.selectors,
				/**
				 * Alias for class ns.event
				 * @property {Object} eventUtils
				 * @member ns.widget.core.Popup
				 * @private
				 */
				eventUtils = ns.event,

				Popup = function () {
					var self = this,
						ui = {};

					self.selectors = selectors;
					self.options = objectUtils.merge({}, Popup.defaults);
					self.storedOptions = null;
					/**
					 * Popup state flag
					 * @property {0|1|2|3} [state=null]
					 * @member ns.widget.core.Popup
					 * @private
					 */
					self.state = states.CLOSED;

					ui.overlay = null;
					ui.header = null;
					ui.footer = null;
					ui.content = null;
					ui.container = null;
					ui.wrapper = null;
					self._ui = ui;

					// event callbacks
					self._callbacks = {};
				},
				/**
				 * Object with default options
				 * @property {Object} defaults
				 * @property {string} [options.transition="none"] Sets the default transition for the popup.
				 * @property {string} [options.positionTo="window"] Sets the element relative to which the popup will be centered.
				 * @property {boolean} [options.dismissible=true] Sets whether to close popup when a popup is open to support the back button.
				 * @property {boolean} [options.overlay=true] Sets whether to show overlay when a popup is open.
				 * @property {boolean|string} [options.header=false] Sets content of header.
				 * @property {boolean|string} [options.footer=false] Sets content of footer.
				 * @property {string} [options.content=null] Sets content of popup.
				 * @property {string} [options.overlayClass=""] Sets the custom class for the popup background, which covers the entire window.
				 * @property {string} [options.closeLinkSelector="a[data-rel='back']"] Sets selector for close buttons in popup.
				 * @property {boolean} [options.history=true] Sets whether to alter the url when a popup is open to support the back button.
				 * @member ns.widget.core.Popup
				 * @static
				 */
				defaults = {
					transition: "none",
					dismissible: true,
					overlay: true,
					header: false,
					footer: false,
					content: null,
					overlayClass: "",
					closeLinkSelector: "[data-rel='back']",
					history: true
				},
				states = {
					DURING_OPENING: 0,
					OPENED: 1,
					DURING_CLOSING: 2,
					CLOSED: 3
				},
				CLASSES_PREFIX = "ui-popup",
				/**
				 * Dictionary for popup related css class names
				 * @property {Object} classes
				 * @member ns.widget.core.Popup
				 * @static
				 */
				classes = {
					popup: CLASSES_PREFIX,
					active: CLASSES_PREFIX + "-active",
					overlay: CLASSES_PREFIX + "-overlay",
					header: CLASSES_PREFIX + "-header",
					footer: CLASSES_PREFIX + "-footer",
					content: CLASSES_PREFIX + "-content",
					wrapper: CLASSES_PREFIX + "-wrapper",
					build: "ui-build"
				},
				/**
				 * Dictionary for popup related selectors
				 * @property {Object} selectors
				 * @member ns.widget.core.Popup
				 * @static
				 */
				selectors = {
					header: "." + classes.header,
					content: "." + classes.content,
					footer: "." + classes.footer
				},
				EVENTS_PREFIX = "popup",
				/**
				 * Dictionary for popup related events
				 * @property {Object} events
				 * @member ns.widget.core.Popup
				 * @static
				 */
				events = {
					/**
					 * Triggered when the popup has been created in the DOM (via ajax or other) but before all widgets have had an opportunity to enhance the contained markup.
					 * @event popupshow
					 * @member ns.widget.core.Popup
					 */
					show: EVENTS_PREFIX + "show",
					/**
					 * Triggered on the popup after the transition animation has completed.
					 * @event popuphide
					 * @member ns.widget.core.Popup
					 */
					hide: EVENTS_PREFIX + "hide",
					/**
					 * Triggered on the popup we are transitioning to, before the actual transition animation is kicked off.
					 * @event popupbeforeshow
					 * @member ns.widget.core.Popup
					 */
					before_show: EVENTS_PREFIX + "beforeshow",
					/**
					 * Triggered on the popup we are transitioning away from, before the actual transition animation is kicked off.
					 * @event popupbeforehide
					 * @member ns.widget.core.Popup
					 */
					before_hide: EVENTS_PREFIX + "beforehide"
				},

				prototype = new BaseWidget();

			Popup.classes = classes;
			Popup.events = events;
			Popup.defaults = defaults;

			/**
			 * Build the content of popup
			 * @method _buildContent
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._buildContent = function (element) {
				var self = this,
					ui = self._ui,
					selectors = self.selectors,
					options = self.options,
					content = ui.content || element.querySelector(selectors.content),
					footer = ui.footer || element.querySelector(selectors.footer),
					elementChildren = [].slice.call(element.childNodes),
					elementChildrenLength = elementChildren.length,
					i,
					node;

				if (!content) {
					content = document.createElement("div");
					content.className = classes.content;
					for (i = 0; i < elementChildrenLength; ++i) {
						node = elementChildren[i];
						if (node !== ui.footer && node !== ui.header) {
							content.appendChild(node);
						}
					}
					if (typeof options.content === "string") {
						content.innerHTML = options.content;
					}
					element.insertBefore(content, footer);
				}
				content.classList.add(classes.content);
				ui.content = content;
			};

			/**
			 * Build the header of popup
			 * @method _buildHeader
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._buildHeader = function (element) {
				var self = this,
					ui = self._ui,
					options = self.options,
					selectors = self.selectors,
					content = ui.content || element.querySelector(selectors.content),
					header = ui.header || element.querySelector(selectors.header);
				if (!header && options.header !== false) {
					header = document.createElement("div");
					header.className = classes.header;
					if (typeof options.header !== "boolean") {
						header.innerHTML = options.header;
					}
					element.insertBefore(header, content);
				}
				if (header) {
					header.classList.add(classes.header);
				}
				ui.header = header;
			};

			/**
			 * Set the header of popup.
			 * This function is called by function "option" when the option "header" is set.
			 * @method _setHeader
			 * @param {HTMLElement} element
			 * @param {boolean|string} value
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._setHeader = function (element, value) {
				var self = this,
					ui = self._ui,
					header = ui.header;
				if (header) {
					header.parentNode.removeChild(header);
					ui.header = null;
				}
				self.options.header = value;
				self._buildHeader(ui.container);
			};

			/**
			 * Build the footer of popup
			 * @method _buildFooter
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._buildFooter = function (element) {
				var self = this,
					ui = self._ui,
					options = self.options,
					footer = ui.footer || element.querySelector(self.selectors.footer);
				if (!footer && options.footer !== false) {
					footer = document.createElement("div");
					footer.className = classes.footer;
					if (typeof options.footer !== "boolean") {
						footer.innerHTML = options.footer;
					}
					element.appendChild(footer);
				}
				if (footer) {
					footer.classList.add(classes.footer);
				}
				ui.footer = footer;
			};

			/**
			 * Set the footer of popup.
			 * This function is called by function "option" when the option "footer" is set.
			 * @method _setFooter
			 * @param {HTMLElement} element
			 * @param {boolean|string} value
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._setFooter = function (element, value) {
				var self = this,
					ui = self._ui,
					footer = ui.footer;
				if (footer) {
					footer.parentNode.removeChild(footer);
					ui.footer = null;
				}
				self.options.footer = value;
				self._buildFooter(ui.container);
			};

			/**
			 * Build structure of Popup widget
			 * @method _build
			 * @param {HTMLElement} element of popup
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.Popup
			 */
			prototype._build = function (element) {
				var self = this,
					ui = self._ui,
					wrapper,
					child = element.firstChild;

				// set class for element
				element.classList.add(classes.popup);

				// create wrapper
				wrapper = document.createElement("div");
				wrapper.classList.add(classes.wrapper);
				ui.wrapper = wrapper;
				ui.container = wrapper;
				// move all children to wrapper
				while (child) {
					wrapper.appendChild(child);
					child = element.firstChild;
				}
				// add wrapper and arrow to popup element
				element.appendChild(wrapper);

				// build header, footer and content
				this._buildHeader(ui.container);
				this._buildFooter(ui.container);
				this._buildContent(ui.container);

				// set overlay
				this._setOverlay(element, this.options.overlay);

				return element;
			};

			/**
			 * Set overlay
			 * @method _setOverlay
			 * @param {HTMLElement} element
			 * @param {boolean} enable
			 * @protected
			 * @member ns.widget.Popup
			 */
			prototype._setOverlay = function(element, enable) {
				var self = this,
					overlayClass = self.options.overlayClass,
					ui = self._ui,
					overlay = ui.overlay;

				// if this popup is not connected with slider,
				// we create overlay, which is invisible when
				// the value of option overlay is false
				/// @TODO: get class from widget
				if (!element.classList.contains("ui-slider-popup")) {
					// create overlay
					if (!overlay) {
						overlay = document.createElement("div");
						element.parentNode.insertBefore(overlay, element);
						ui.overlay = overlay;
					}
					overlay.className = classes.overlay + (overlayClass ? " " + overlayClass : "");
					if (enable) {
						overlay.style.opacity = "";
					} else {
						// if option is set on "false", the overlay is not visible
						overlay.style.opacity = 0;
					}
				}
			};

			/**
			 * Returns the state of the popup
			 * @method _isActive
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._isActive = function () {
				var state = this.state;
				return state === states.DURING_OPENING || state === states.OPENED;
			};

			/**
			 * Returns true if popup is already opened and visible
			 * @method _isActive
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._isOpened = function () {
				return this.state === states.OPENED;
			};

			/**
			 * Init widget
			 * @method _init
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._init = function(element) {
				var self = this,
					selectors = self.selectors,
					ui = self._ui;

				ui.header = ui.header || element.querySelector(selectors.header);
				ui.footer = ui.footer || element.querySelector(selectors.footer);
				ui.content = ui.content || element.querySelector(selectors.content);
				ui.wrapper = ui.wrapper || element.querySelector("." + classes.wrapper);
				ui.container = ui.wrapper || element;

				// @todo - use selector from page's definition in engine
				ui.page = utilSelector.getClosestByClass(element, "ui-page") || window;
			};

			/**
			 * Set the state of the popup
			 * @method _setActive
			 * @param {boolean} active
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._setActive = function (active) {
				var self = this,
					activeClass = classes.active,
					elementClassList = self.element.classList,
					route = engine.getRouter().getRoute("popup"),
					options;

				// NOTE: popup's options object is stored in window.history at the router module,
				// and this window.history can't store DOM element object.
				options =  objectUtils.merge({}, self.options, {positionTo: null, link: null});

				// set state of popup and add proper class
				if (active) {
					// set global variable
					route.setActive(self, options);
					// add proper class
					elementClassList.add(activeClass);
					// set state of popup 	358
					self.state = states.OPENED;
				} else {
					// no popup is opened, so set global variable on "null"
					route.setActive(null, options);
					// remove proper class
					elementClassList.remove(activeClass);
					// set state of popup
					self.state = states.CLOSED;
				}
			};

			/**
			 * Bind events
			 * @method _bindEvents
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._bindEvents = function () {
				var self = this,
					closeButtons = self.element.querySelectorAll(self.options.closeLinkSelector);

				self._ui.page.addEventListener("pagebeforehide", self, false);
				window.addEventListener("resize", self, false);
				eventUtils.on(closeButtons, "click", self, false);
				self._bindOverlayEvents();
			};

			/**
			 * Bind "click" event for overlay
			 * @method _bindOverlayEvents
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._bindOverlayEvents = function () {
				var overlay = this._ui.overlay;
				if (overlay) {
					overlay.addEventListener("click", this, false);
				}
			};

			/**
			 * Unbind "click" event for overlay
			 * @method _bindOverlayEvents
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._unbindOverlayEvents = function () {
				var overlay = this._ui.overlay;
				if (overlay) {
					overlay.removeEventListener("click", this, false);
				}
			};

			/**
			 * Unbind events
			 * @method _bindEvents
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._unbindEvents = function () {
				var self = this;

				self._ui.page.removeEventListener("pagebeforehide", self, false);
				window.removeEventListener("resize", self, false);
				self._unbindOverlayEvents();
			};

			/**
			 * Layouting popup structure
			 * @method layout
			 * @member ns.widget.core.Popup
			 */
			prototype._layout = function (element) {
			};

			/**
			 * Open the popup
			 * @method open
			 * @param {Object=} [options]
			 * @param {string=} [options.transition] options.transition
			 * @member ns.widget.core.Popup
			 */
			prototype.open = function (options) {
				var self = this,
					newOptions;

				if (!self._isActive()) {
					/*
					 * Some passed options on open need to be kept until popup closing.
					 * For example, trasition parameter should be kept for closing animation.
					 * On the other hand, fromHashChange or x, y parameter should be removed.
					 * We store options and restore them on popup closing.
					 */
					self._storeOpenOptions(options);

					newOptions = objectUtils.merge(self.options, options);
					if (!newOptions.dismissible) {
						engine.getRouter().lock();
					}
					self._show(newOptions);
				}
			};

			/**
			 * Close the popup
			 * @method close
			 * @param {Object=} [options]
			 * @param {string=} [options.transition]
			 * @member ns.widget.core.Popup
			 */
			prototype.close = function (options) {
				var self = this,
					newOptions = objectUtils.merge(self.options, options);

				if (self._isActive()) {
					if (!newOptions.dismissible) {
						engine.getRouter().unlock();
					}
					self._hide(newOptions);
				}
			};

			/**
			 * Store Open options.
			 * @method _storeOpenOptions
			 * @param {object} options
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._storeOpenOptions = function (options) {
				var self = this,
					oldOptions = self.options,
					storedOptions = {},
					key;

				for (key in options) {
					if (options.hasOwnProperty(key)) {
						storedOptions[key] = oldOptions[key];
					}
				}

				self.storedOptions = storedOptions;
			};

			/**
			 * Restore Open options and remove some unnecessary ones.
			 * @method _storeOpenOptions
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._restoreOpenOptions = function () {
				var self = this,
					options = self.options,
					propertiesToRemove = ["x", "y", "fromHashChange"];

				// we restore opening values of all options
				options = objectUtils.merge(options, self.storedOptions);
				// and remove all values which should not be stored
				objectUtils.removeProperties(options, propertiesToRemove);
			};

			/**
			 * Show popup.
			 * @method _show
			 * @param {object} options
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._show = function (options) {
				var self = this,
					transitionOptions = objectUtils.merge({}, options),
					overlay = self._ui.overlay,
					deferred;

				// layouting
				self._layout(self.element);

				// change state of popup
				self.state = states.DURING_OPENING;
				// set transiton
				transitionOptions.ext = " in ";

				self.trigger(events.before_show);
				// show overlay
				if (overlay) {
					overlay.style.display = "block";
				}
				// start opening animation
				self._transition(transitionOptions, self._onShow.bind(self));
			};

			/**
			 * Show popup
			 * @method _onShow
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._onShow = function() {
				var self = this;
				self._setActive(true);
				self.trigger(events.show);
			};

			/**
			 * Hide popup
			 * @method _hide
			 * @param {object} options
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._hide = function (options) {
				var self = this,
					isOpened = self._isOpened(),
					callbacks = self._callbacks;

				// change state of popup
				self.state = states.DURING_CLOSING;

				self.trigger(events.before_hide);

				if (isOpened) {
					// popup is opened, so we start closing animation
					options.ext = " out ";
					self._transition(options, self._onHide.bind(self));
				} else {
					// popup is active, but not opened yet (DURING_OPENING), so
					// we stop opening animation
					if (callbacks.transitionDeferred) {
						callbacks.transitionDeferred.reject();
					}
					if (callbacks.animationEnd) {
						callbacks.animationEnd();
					}
					// and set popup as inactive
					self._onHide();
				}
			};

			/**
			 * Hide popup
			 * @method _onHide
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._onHide = function() {
				var self = this,
					overlay = self._ui.overlay;

				if (overlay) {
					overlay.style.display = "";
				}
				self._setActive(false);
				self._restoreOpenOptions();
				self.trigger(events.hide);
			};

			/**
			 * Handle events
			 * @method handleEvent
			 * @param {Event} event
			 * @member ns.widget.core.Popup
			 */
			prototype.handleEvent = function(event) {
				var self = this;
				switch(event.type) {
					case "pagebeforehide":
						// we need close active popup if exists
						engine.getRouter().close(null, {transition: "none", rel: "popup"});
						break;
					case "resize":
						self._onResize(event);
						break;
					case "click":
						if ( event.target === self._ui.overlay ) {
							self._onClickOverlay(event);
						}
						break;
				}
			};

			/**
			 * Refresh structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._refresh = function() {
				var self = this;
				self._unbindOverlayEvents();
				self._setOverlay(self.element, self.options.overlay);
				self._bindOverlayEvents();
			};

			/**
			 * Callback function fires after clicking on overlay.
			 * @method _onClickOverlay
			 * @param {Event} event
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._onClickOverlay = function(event) {
				var options = this.options;

				event.preventDefault();
				event.stopPropagation();

				if (options.dismissible) {
					engine.getRouter().close();
				}
			};

			/**
			 * Callback function fires on resizing
			 * @method _onResize
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._onResize = function() {
				if (this._isOpened()) {
					this._refresh();
				}
			};

			function clearAnimation(self, transitionClass, deferred) {
				var element = self.element,
					elementClassList = element.classList,
					overlay = self._ui.overlay,
					animationEndCallback = self._callbacks.animationEnd;

				// remove callbacks on animation events
				element.removeEventListener("animationend", animationEndCallback, false);
				element.removeEventListener("webkitAnimationEnd", animationEndCallback, false);
				element.removeEventListener("mozAnimationEnd", animationEndCallback, false);
				element.removeEventListener("oAnimationEnd", animationEndCallback, false);
				element.removeEventListener("msAnimationEnd", animationEndCallback, false);

				// clear classes
				transitionClass.split(" ").forEach(function (currentClass) {
					currentClass = currentClass.trim();
					if (currentClass.length > 0) {
						elementClassList.remove(currentClass);
						if (overlay) {
							overlay.classList.remove(currentClass);
						}
					}
				});
				if (deferred.state() === "pending") {
					// we resolve only pending (not rejected) deferred
					deferred.resolve();
				}
			}

			function setTransitionDeferred(self, resolve) {
				var deferred = new UtilDeferred();

				deferred.then(function() {
					if (deferred === self._callbacks.transitionDeferred) {
						resolve();
					}
				});

				self._callbacks.transitionDeferred = deferred;
				return deferred;
			}
			/**
			 * Animate popup opening/closing
			 * @method _transition
			 * @protected
			 * @param {Object} [options]
			 * @param {string=} [options.transition]
			 * @param {string=} [options.ext]
			 * @param {?Function} [resolve]
			 * @member ns.widget.core.Popup
			 */
			prototype._transition = function (options, resolve) {
				var self = this,
					transition = options.transition || self.options.transition || "none",
					transitionClass = transition + options.ext,
					element = self.element,
					elementClassList = element.classList,
					deferred,
					animationEndCallback;

				deferred = setTransitionDeferred(self, resolve);

				if (transition !== "none") {
					// set animationEnd callback
					animationEndCallback = clearAnimation.bind(null, self, transitionClass, deferred);
					self._callbacks.animationEnd = animationEndCallback;

					// add animation callbacks
					element.addEventListener("animationend", animationEndCallback, false);
					element.addEventListener("webkitAnimationEnd", animationEndCallback, false);
					element.addEventListener("mozAnimationEnd", animationEndCallback, false);
					element.addEventListener("oAnimationEnd", animationEndCallback, false);
					element.addEventListener("msAnimationEnd", animationEndCallback, false);
					// add transition classes
					transitionClass.split(" ").forEach(function (currentClass) {
						currentClass = currentClass.trim();
						if (currentClass.length > 0) {
							elementClassList.add(currentClass);
						}
					});
				} else {
					window.setTimeout(deferred.resolve, 0);
				}
				return deferred;
			};

			/**
			 * Destroy popup
			 * @method _destroy
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._destroy = function() {
				var self = this,
					element = self.element,
					ui = self._ui,
					wrapper = ui.wrapper,
					child;

				if (wrapper) {
					// restore all children from wrapper
					child = wrapper.firstChild;
					while (child) {
						element.appendChild(child);
						child = wrapper.firstChild;
					}

					if (wrapper.parentNode) {
						wrapper.parentNode.removeChild(wrapper);
					}
				}

				self._unbindEvents(element);
				self._setOverlay(element, false);

				ui.wrapper = null;
			};

			Popup.prototype = prototype;

			ns.widget.core.Popup = Popup;

			engine.defineWidget(
				"Popup",
				"[data-role='popup'], .ui-popup",
				[
					"open",
					"close",
					"reposition"
				],
				Popup,
				"core"
			);
			}(ns));

/*global window, define */
/*
* Copyright (c) 2015 Samsung Electronics Co., Ltd
*
* Licensed under the Flora License, Version 1.1 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://floralicense.org/license/
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*jslint nomen: true, plusplus: true */

/**
 * # Popup Widget
 * Shows a pop-up window.
 *
 * The popup widget shows in the middle of the screen a list of items in a pop-up window. It automatically optimizes the pop-up window size within the screen. The following table describes the supported popup classes.
 *
 * ## Default selectors
 * All elements with class *ui-popup* will be become popup widgets.
 *
 * The pop-up window can contain a header, content, and footer area like the page element.
 *
 * To open a pop-up window from a link, use the data-rel attribute in HTML markup as in the following code:
 *
 *      @example
 *      <a href="#popup" class="ui-btn" data-rel="popup">Open popup when clicking this element.</a>
 *
 * The following table shows examples of various types of popups.
 *
 * The popup contains header, content and footer area
 *
 * ###HTML Examples
 *
 * #### Basic popup with header, content, footer
 *
 *		@example
 *		<div class="ui-page">
 *		    <div class="ui-popup">
 *		        <div class="ui-popup-header">Power saving mode</div>
 *		        <div class="ui-popup-content">
 *		            Turning on Power
 *		            saving mode will
 *		            limit the maximum
 *		            per
 *		        </div>
 *		        <div class="ui-popup-footer">
 *		            <button id="cancel" class="ui-btn">Cancel</button>
 *		        </div>
 *		    </div>
 *		</div>
 *
 * #### Popup with 2 buttons in the footer
 *
 *      @example
 *         <div id="2btnPopup" class="ui-popup">
 *             <div class="ui-popup-header">Delete</div>
 *             <div class="ui-popup-content">
 *                 Delete the image?
 *             </div>
 *             <div class="ui-popup-footer ui-grid-col-2">
 *                 <button id="2btnPopup-cancel" class="ui-btn">Cancel</button>
 *                 <button id="2btnPopup-ok" class="ui-btn">OK</button>
 *             </div>
 *         </div>
 *
 * #### Popup with checkbox/radio
 *
 * If you want make popup with list checkbox(or radio) just include checkbox (radio) to popup and add class *ui-popup-checkbox-label* to popup element.
 *
 *		@example
 *         <div id="listBoxPopup" class="ui-popup">
 *             <div class="ui-popup-header">When?</div>
 *             <div class="ui-popup-content" style="height:243px; overflow-y:scroll">
 *                 <ul class="ui-listview">
 *                     <li>
 *                         <label for="check-1" class="ui-popup-checkbox-label">Yesterday</label>
 *                         <input type="checkbox" name="checkset" id="check-1" />
 *                     </li>
 *                     <li>
 *                         <label for="check-2" class="ui-popup-checkbox-label">Today</label>
 *                         <input type="checkbox" name="checkset" id="check-2" />
 *                     </li>
 *                     <li>
 *                         <label for="check-3" class="ui-popup-checkbox-label">Tomorrow</label>
 *                         <input type="checkbox" name="checkset" id="check-3" />
 *                     </li>
 *                 </ul>
 *                 <ul class="ui-listview">
 *                     <li>
 *                         <label for="radio-1" class="ui-popup-radio-label">Mandatory</label>
 *                         <input type="radio" name="radioset" id="radio-1" />
 *                     </li>
 *                     <li>
 *                         <label for="radio-2" class="ui-popup-radio-label">Optional</label>
 *                         <input type="radio" name="radioset" id="radio-2" />
 *                     </li>
 *                 </ul>
 *             </div>
 *             <div class="ui-popup-footer">
 *                 <button id="listBoxPopup-close" class="ui-btn">Close</button>
 *             </div>
 *         </div>
 *     </div>
 *
 * #### Popup with no header and footer
 *
 *      @example
 *         <div id="listNoTitleNoBtnPopup" class="ui-popup">
 *             <div class="ui-popup-content" style="height:294px; overflow-y:scroll">
 *                 <ul class="ui-listview">
 *                     <li><a href="">Ringtones 1</a></li>
 *                     <li><a href="">Ringtones 2</a></li>
 *                     <li><a href="">Ringtones 3</a></li>
 *                 </ul>
 *             </div>
 *         </div>
 *
 * #### Toast popup
 *
 *      @example
 *         <div id="PopupToast" class="ui-popup ui-popup-toast">
 *             <div class="ui-popup-content">Saving contacts to sim on Samsung</div>
 *         </div>
 *
 * ### Create Option popup
 *
 * Popup inherits value of option positionTo from property data-position-to set in link.
 *
 *		@example
 *		<!--definition of link, which opens popup and sets its position-->
 *		<a href="#popupOptionText" data-rel="popup"  data-position-to="origin">Text</a>
 *		<!--definition of popup, which inherites property position from link-->
 *		<div id="popupOptionText" class="ui-popup">
 *			<div class="ui-popup-content">
 *				<ul class="ui-listview">
 *				<li><a href="#">Option 1</a></li>
 *				<li><a href="#">Option 2</a></li>
 *				<li><a href="#">Option 3</a></li>
 *				<li><a href="#">Option 4</a></li>
 *				</ul>
 *			</div>
 *		</div>
 *
 * ### Opening and closing popup
 *
 * To open popup from "a" link using html markup, use the following code:
 *
 *		@example
 *      <div class="ui-page">
 *          <header class="ui-header">
 *              <h2 class="ui-title">Call menu</h2>
 *          </header>
 *          <div class="ui-content">
 *              <a href="#popup" class="ui-btn" data-rel="popup" >Open Popup</a>
 *          </div>
 *
 *          <div id="popup" class="ui-popup">
 *               <div class="ui-popup-header">Power saving mode</div>
 *                   <div class="ui-popup-content">
 *                       Turning on Power
 *                       saving mode will
 *                       limit the maximum
 *                       per
 *                   </div>
 *               <div class="ui-popup-footer">
 *               <button id="cancel" class="ui-btn">Cancel</button>
 *           </div>
 *       </div>
 *
 *  To open the popup widget from JavaScript use method *tau.openPopup(to)*
 *
 *          @example
 *          tau.openPopup("popup")
 *
 *  To close the popup widget from JavaScript use method *tau.openPopup(to)*
 *
 *          @example
 *          tau.closePopup("popup")
 *
 * To find the currently active popup, use the ui-popup-active class.
 *
 * To bind the popup to a button, use the following code:
 *
 *      @example
 *         <!--HTML code-->
 *         <div id="1btnPopup" class="ui-popup">
 *             <div class="ui-popup-header">Power saving mode</div>
 *             <div class="ui-popup-content">
 *             </div>
 *             <div class="ui-popup-footer">
 *                 <button id="1btnPopup-cancel" class="ui-btn">Cancel</button>
 *             </div>
 *         </div>
 *         <script>
 *             // Popup opens with button click
 *             var button = document.getElementById("button");
 *             button.addEventListener("click", function() {
 *                 tau.openPopup("#1btnPopup");
 *             });
 *
 *             // Popup closes with Cancel button click
 *             document.getElementById("1btnPopup-cancel").addEventListener("click", function() {
 *                 tau.closePopup();
 *             });
 *         </script>
 *
 * ## Manual constructor
 * For manual creation of popup widget you can use constructor of widget from **tau** namespace:
 *
 *		@example
 *		var popupElement = document.getElementById("popup"),
 *			popup = tau.widget.popup(buttonElement);
 *
 * Constructor has one require parameter **element** which are base **HTMLElement** to create widget. We recommend get this element by method *document.getElementById*.
 *
 * ## Options for Popup Widget
 *
 * Options for widget can be defined as _data-..._ attributes or give as parameter in constructor.
 *
 * You can change option for widget using method **option**.
 *
 * ## Methods
 *
 * To call method on widget you can use tau API:
 *
 *		@example
 *		var popupElement = document.getElementById("popup"),
 *			popup = tau.widget.popup(buttonElement);
 *
 *		popup.methodName(methodArgument1, methodArgument2, ...);
 *
 * ## Transitions
 *
 * By default, the framework doesn't apply transition. To set a custom transition effect, add the data-transition attribute to the link.
 *
 *		@example
 *		<a href="index.html" data-rel="popup" data-transition="slideup">I'll slide up</a>
 *
 * Global configuration:
 *
 *		@example
 *		gear.ui.defaults.popupTransition = "slideup";
 *
 * ### Transitions list
 *
 * - **none** Default value, no transition.
 * - **slideup** Makes the content of the pop-up slide up.
 *
 * ## Handling Popup Events
 *
 * To use popup events, use the following code:
 *
 *      @example
 *         <!--Popup html code-->
 *         <div id="popup" class="ui-popup">
 *             <div class="ui-popup-header"></div>
 *             <div class="ui-popup-content"></div>
 *         </div>
 *         </div>
 *         <script>
 *             // Use popup events
 *             var popup = document.getElementById("popup");
 *             popup.addEventListener("popupbeforecreate", function() {
 *                 // Implement code for popupbeforecreate event
 *             });
 *         </script>
 *
 * Full list of available events is in [events list section](#events-list).
 *
 * @author Hyunkook Cho <hk0713.cho@samsung.com>
 * @class ns.widget.core.Popup
 * @extends ns.widget.core.BasePopup
 */
(function (window, document, ns) {
	
	
			var Popup = ns.widget.core.Popup,

				PopupPrototype = Popup.prototype,

				engine = ns.engine,

				objectUtils = ns.util.object,

				domUtils = ns.util.DOM,

				/**
				 * Object with default options
				 * @property {Object} defaults
				 * @property {string} [options.transition="none"] Sets the default transition for the popup.
				 * @property {string} [options.positionTo="window"] Sets the element relative to which the popup will be centered.
				 * @property {boolean} [options.dismissible=true] Sets whether to close popup when a popup is open to support the back button.
				 * @property {boolean} [options.overlay=true] Sets whether to show overlay when a popup is open.
				 * @property {string} [overlayClass=""] Sets the custom class for the popup background, which covers the entire window.
				 * @property {boolean} [options.history=true] Sets whether to alter the url when a popup is open to support the back button.
				 * @property {string} [options.arrow="l,t,r,b"] Sets directions of popup's arrow by priority ("l" for left, "t" for top,
				 * "r" for right, and "b" for bottom). The first one has the highest priority, the last one - the lowest. If you set arrow="t",
				 * then arrow will be placed at the top of popup container and the whole popup will be placed under cliced element.
				 * @property {string} [options.positionTo="window"] Sets the element relative to which the popup will be centered.
				 * @property {number} [options.distance=0] Sets the extra distance in px from clicked element.
				 * @property {HTMLElement|string} [options.link=null] Set the element or its id, under which popup should be placed.
				 * It only works with option positionTo="origin".
				 * @member ns.widget.core.ContextPopup
				 * @static
				 * @private
				 */
				defaults = {
					arrow: "l,b,r,t",
					positionTo: "window",
					positionOriginCenter: false,
					distance: 0,
					link: null
				},

				ContextPopup = function () {
					var self = this,
						ui;

					Popup.call(self);

					// set options
					self.options = objectUtils.merge(self.options, defaults);

					// set ui
					ui = self._ui || {};
					ui.arrow = null;
					self._ui = ui;
				},

				/**
				 * @property {Object} classes Dictionary for popup related css class names
				 * @member ns.widget.core.Popup
				 * @static
				 */
				CLASSES_PREFIX = "ui-popup",
				classes = objectUtils.merge({}, Popup.classes, {
					context: "ui-ctxpopup",
					arrow: "ui-arrow",
					arrowDir: CLASSES_PREFIX + "-arrow-"
				}),

				/**
				 * @property {Object} events Dictionary for popup related events
				 * @member ns.widget.core.Popup
				 * @static
				 */
				events = objectUtils.merge({}, Popup.events, {
					before_position: "beforeposition"
				}),

				positionTypes = {
					WINDOW: "window",
					ORIGIN: "origin",
					ABSOLUTE: "absolute"
				},

				prototype = new Popup();

			ContextPopup.defaults = objectUtils.merge({}, Popup.defaults, defaults);
			ContextPopup.classes = classes;
			ContextPopup.events = events;
			ContextPopup.positionTypes = positionTypes;

			/**
			 * Build structure of Popup widget
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.core.Popup
			 */
			prototype._build = function (element) {
				var self = this,
					ui = self._ui,
					arrow;

				// build elements of popup
				PopupPrototype._build.call(self, element);

				// set class for element
				element.classList.add(classes.popup);

				// create arrow
				arrow = document.createElement("div");
				arrow.appendChild(document.createElement("span"));
				arrow.classList.add(classes.arrow);
				ui.arrow = arrow;

				// add arrow to popup element
				element.appendChild(arrow);

				return element;
			};

			/**
			 * Init widget
			 * @method _init
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._init = function(element) {
				var self = this,
					ui = self._ui;

				PopupPrototype._init.call(this, element);

				ui.arrow = ui.arrow || element.querySelector("." + classes.arrow);
			};

			/**
			 * Layouting popup structure
			 * @method layout
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._layout = function (element) {
				var self = this;
				this._reposition();
				PopupPrototype._layout.call(self, element);
			};

			/**
			 * Set positon and size of popup.
			 * @method _reposition
			 * @param {object} options
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._reposition = function(options) {
				var self = this,
					element = self.element,
					elementClassList = element.classList;

				options = objectUtils.merge({}, self.options, options);

				self.trigger(events.before_position, null, false);

				elementClassList.add(classes.build);

				// set height of content
				self._setContentHeight();

				// set position of popup
				self._placementCoords(options);

				elementClassList.remove(classes.build);

			};

			/**
			 * Find the best positon of context popup.
			 * @method findBestPosition
			 * @param {ns.widget.core.ContextPopup} self
			 * @param {HTMLElement} clickedElement
			 * @private
			 * @member ns.widget.core.ContextPopup
			 */
			function findBestPosition(self, clickedElement) {
				var options = self.options,
					arrowsPriority = options.arrow.split(","),
					element = self.element,
					windowWidth = window.innerWidth,
					windowHeight = window.innerHeight,
					popupWidth = domUtils.getElementWidth(element, "outer"),
					popupHeight = domUtils.getElementHeight(element, "outer"),
					// offset coordinates of clicked element
					clickElementRect = clickedElement.getBoundingClientRect(),
					clickElementOffsetX = clickElementRect.left,
					clickElementOffsetY = clickElementRect.top,
					// width of visible part of clicked element
					clickElementOffsetWidth = Math.min(clickElementRect.width,
							windowWidth - clickElementOffsetX),
					// height of visible part of clicked element
					clickElementOffsetHeight = Math.min(clickElementRect.height,
							windowHeight - clickElementOffsetY),
					// params for all types of popup
					// "l" - popup with arrow on the left side, "r" - right, "b" - bottom, "t" - top
					// dir - this letter is added as a suffix of class to popup's element
					// fixedPositionField - specifies which coordinate is changed for this type of popup
					// fixedPositionFactor - factor, which specifies if size should be added or subtracted
					// size - available size, which is needed for this type of popup (width or height)
					// max - maximum size of available place
					params = {
						"l": {dir: "l", fixedPositionField: "x", fixedPositionFactor: 1,
							size: popupWidth, max: clickElementOffsetX},
						"r": {dir: "r", fixedPositionField: "x", fixedPositionFactor: -1,
							size: popupWidth, max: windowWidth - clickElementOffsetX - clickElementOffsetWidth},
						"b": {dir: "b", fixedPositionField: "y", fixedPositionFactor: -1,
							size: popupHeight, max: clickElementOffsetY},
						"t": {dir: "t", fixedPositionField: "y", fixedPositionFactor: 1,
							size: popupHeight, max: windowHeight - clickElementOffsetY - clickElementOffsetHeight}
					},
					bestDirection,
					direction,
					bestOffsetInfo;

				// set value of bestDirection on the first possible type or top
				bestDirection = params[arrowsPriority[0]] || params.t;

				arrowsPriority.forEach(function(key){
					var param = params[key],
						paramMax = param.max;
					if (!direction) {
						if (param.size < paramMax) {
							direction = param;
						} else if (paramMax > bestDirection.max) {
							bestDirection = param;
						}
					}
				});

				if (!direction) {
					direction = bestDirection;
					if (direction.fixedPositionField === "x") {
						popupWidth = direction.max;
					} else {
						popupHeight = direction.max;
					}
				}

				// info about the best position without taking into account type of popup
				bestOffsetInfo = {
					x: clickElementOffsetX + clickElementOffsetWidth / 2 - popupWidth / 2,
					y: clickElementOffsetY + clickElementOffsetHeight / 2 - popupHeight / 2,
					w: popupWidth,
					h: popupHeight,
					dir: direction.dir
				};

				// check type of popup and correct value for "fixedPositionField" coordinate
				bestOffsetInfo[direction.fixedPositionField] +=
					(direction.fixedPositionField === "x" ?
						(popupWidth + clickElementOffsetWidth) * direction.fixedPositionFactor :
						(popupHeight + clickElementOffsetHeight) * direction.fixedPositionFactor)
						/ 2 + options.distance * direction.fixedPositionFactor;

				// fix min/max position
				bestOffsetInfo.x = bestOffsetInfo.x < 0 ? 0 : bestOffsetInfo.x + bestOffsetInfo.w > windowWidth ? windowWidth - bestOffsetInfo.w : bestOffsetInfo.x;
				bestOffsetInfo.y = bestOffsetInfo.y < 0 ? 0 : bestOffsetInfo.y + bestOffsetInfo.h > windowHeight ? windowHeight - bestOffsetInfo.h : bestOffsetInfo.y;

				return bestOffsetInfo;
			}

			/**
			 * Find the best positon of arrow.
			 * @method adjustedPositionAndPlacementArrow
			 * @param {ns.widget.core.ContextPopup} self
			 * @param {Object} bestRectangle
			 * @param {number} x
			 * @param {number} y
			 * @private
			 * @member ns.widget.core.ContextPopup
			 */
			function adjustedPositionAndPlacementArrow(self, bestRectangle, x, y) {
				var ui = self._ui,
					wrapper = ui.wrapper,
					arrow = ui.arrow,
					popupElement = self.element,
					arrowStyle = arrow.style,
					windowWidth = window.innerWidth,
					windowHeight = window.innerHeight,
					wrapperRect = wrapper.getBoundingClientRect(),
					arrowHalfWidth = arrow.offsetWidth / 2,
					popupProperties = {
						"padding-top": 0,
						"padding-bottom": 0,
						"padding-left": 0,
						"padding-right": 0,
						"border-top-width": 0,
						"border-left-width": 0,
						"box-sizing": null
					},
					wrapperProperties = {
						"margin-top": 0,
						"margin-bottom": 0,
						"margin-left": 0,
						"margin-right": 0,
						"padding-top": 0,
						"padding-bottom": 0,
						"padding-left": 0,
						"padding-right": 0
					},
					margins,
					params = {
						"t": {pos: x, min: "left", max: "right", posField: "x", valField: "w", styleField: "left"},
						"b": {pos: x, min: "left", max: "right", posField: "x", valField: "w", styleField: "left"},
						"l": {pos: y, min: "top", max: "bottom", posField: "y", valField: "h", styleField: "top"},
						"r": {pos: y, min: "top", max: "bottom", posField: "y", valField: "h", styleField: "top"}
					},
					param = params[bestRectangle.dir],
					surplus,
					addPadding;

				domUtils.extractCSSProperties(popupElement, popupProperties);
				domUtils.extractCSSProperties(wrapper, wrapperProperties);
				addPadding = popupProperties["box-sizing"] === "border-box";
				margins	= {
					"t": popupProperties["padding-top"] + wrapperProperties["margin-top"] + wrapperProperties["padding-top"],
					"b": popupProperties["padding-bottom"] + wrapperProperties["margin-bottom"] + wrapperProperties["padding-bottom"],
					"l": popupProperties["padding-left"] + wrapperProperties["margin-left"] + wrapperProperties["padding-left"],
					"r": popupProperties["padding-right"] + wrapperProperties["margin-right"] + wrapperProperties["padding-right"]
				};

				// value of coordinates of proper edge of wrapper
				wrapperRect = {
					// x-coordinate of left edge
					left: margins.l + bestRectangle.x,
					// x-coordinate of right edge
					right: margins.l + wrapperRect.width + bestRectangle.x,
					// y-coordinate of top edge
					top: margins.t + bestRectangle.y,
					// y-coordinate of bottom edge
					bottom: wrapperRect.height + margins.t + bestRectangle.y
				};

				if (wrapperRect[param.min] > param.pos - arrowHalfWidth) {
					surplus = bestRectangle[param.posField];
					if (surplus > 0) {
						bestRectangle[param.posField] = Math.max(param.pos - arrowHalfWidth, 0);
						param.pos = bestRectangle[param.posField] + arrowHalfWidth;
					} else {
						param.pos = wrapperRect[param.min] + arrowHalfWidth;
					}
				} else if (wrapperRect[param.max] < param.pos + arrowHalfWidth) {
					surplus = (param.valField === "w" ? windowWidth : windowHeight)
						- (bestRectangle[param.posField] + bestRectangle[param.valField]);
					if (surplus > 0) {
						bestRectangle[param.posField] += Math.min(surplus, (param.pos + arrowHalfWidth) - wrapperRect[param.max]);
						param.pos = bestRectangle[param.posField] + bestRectangle[param.valField] - arrowHalfWidth;
					} else {
						param.pos = wrapperRect[param.max] - arrowHalfWidth;
					}
				}

				arrowStyle[param.styleField] = (param.pos - arrowHalfWidth - bestRectangle[param.posField] - (addPadding ? popupProperties["border-" + param.styleField + "-width"] : 0)) + "px";

				return bestRectangle;
			}

			/**
			 * Set top, left and margin for popup's container.
			 * @method _placementCoordsWindow
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._placementCoordsWindow = function(element) {
				var elementStyle = element.style,
					elementWidth = element.offsetWidth,
					elementHeight = element.offsetHeight;

				elementStyle.top = (window.innerHeight - elementHeight) + "px";
				elementStyle.left = "50%";
				elementStyle.marginLeft = -(elementWidth / 2) + "px";
			};

			/**
			 * Set top, left and margin for popup's container.
			 * @method _placementCoordsAbsolute
			 * @param {HTMLElement} element
			 * @param {number} x
			 * @param {number} y
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._placementCoordsAbsolute = function(element, x, y) {
				var elementStyle = element.style,
					elementWidth = element.offsetWidth,
					elementHeight = element.offsetHeight;

				elementStyle.top = y + "px";
				elementStyle.left = x + "px";
				elementStyle.marginTop = -(elementHeight / 2) + "px";
				elementStyle.marginLeft = -(elementWidth / 2) + "px";
			};

			/**
			 * Find clicked element.
			 * @method _findClickedElement
			 * @param {number} x
			 * @param {number} y
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._findClickedElement = function(x, y) {
				return document.elementFromPoint(x, y);
			};

			/**
			 * Emulate position of event for clicked element.
			 * @method emulatePositionOfClick
			 * @param {string} bestDirection direction of arrow
			 * @param {HTMLElement} clickedElement
			 * @private
			 * @member ns.widget.core.ContextPopup
			 */
			function emulatePositionOfClick(bestDirection, clickedElement) {
				var clickedElementRect = clickedElement.getBoundingClientRect(),
					position = {};

				switch(bestDirection) {
					case "l":
						// the arrow will be on the left edge of container, so x-coordinate
						// should have value equals to the position of right edge of clicked element
						position.x = clickedElementRect.right;
						// y-coordinate should have value equals to the position of top edge of clicked
						// element plus half of its height
						position.y = clickedElementRect.top + clickedElementRect.height / 2;
						break;
					case "r":
						// the arrow will be on the right edge of container
						position.x = clickedElementRect.left;
						position.y =  clickedElementRect.top + clickedElementRect.height / 2;
						break;
					case "t":
						// the arrow will be on the top edge of container
						position.x = clickedElementRect.left + clickedElementRect.width / 2;
						position.y = clickedElementRect.bottom;
						break;
					case "b":
						// the arrow will be on the bottom edge of container
						position.x = clickedElementRect.left + clickedElementRect.width / 2;
						position.y = clickedElementRect.top;
						break;
				}
				return position;
			}

			prototype._placementCoordsOrigin = function (clickedElement, options) {
				var self = this,
					element = self.element,
					elementStyle = element.style,
					elementClassList = element.classList,
					x = options.x,
					y = options.y,
					bestRectangle,
					emulatedPosition,
					arrowType,
					elementHeight;

				elementClassList.add(classes.context);

				elementHeight = element.offsetHeight;
				bestRectangle = findBestPosition(self, clickedElement);

				arrowType = bestRectangle.dir;
				elementClassList.add(classes.arrowDir + arrowType);
				self._ui.arrow.setAttribute("type", arrowType);

				if ((typeof x !== "number" && typeof y !== "number") || self.options.positionOriginCenter) {
					// if we found element, which was clicked, but the coordinates of event
					// was not available, we have to count these coordinates to the center of proper edge of element.
					emulatedPosition = emulatePositionOfClick(arrowType, clickedElement);
					x = emulatedPosition.x;
					y = emulatedPosition.y;
				}
				bestRectangle = adjustedPositionAndPlacementArrow(self, bestRectangle, x, y);

				if (elementHeight > bestRectangle.h) {
					self._setContentHeight(bestRectangle.h);
				}

				elementStyle.left = bestRectangle.x + "px";
				elementStyle.top = bestRectangle.y + "px";
			};

			prototype._placementCoordsElement = function (clickedElement, options) {
				var self = this,
					element = self.element,
					elementStyle = element.style,
					bestRectangle,
					elementHeight;

				element.classList.add(classes.context);

				elementHeight = element.offsetHeight;
				bestRectangle = findBestPosition(self, clickedElement);

				if (elementHeight > bestRectangle.h) {
					self._setContentHeight(bestRectangle.h);
				}

				elementStyle.left = bestRectangle.x + "px";
				elementStyle.top = bestRectangle.y + "px";
			};

			/**
			 * Find and set the best position for popup.
			 * @method _placementCoords
			 * @param {object} options
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._placementCoords = function(options) {
				var self = this,
					positionTo = options.positionTo,
					x = options.x,
					y = options.y,
					element = self.element,
					elementHeight,
					clickedElement,
					link;

				switch (positionTo) {
					case positionTypes.ORIGIN:
						// if we know x-coord and y-coord, we open the popup with arrow
						link = options.link;
						if (link) {
							if (typeof link === "string") {
								clickedElement = document.getElementById(link);
							} else if (typeof link === "object") {
								clickedElement = link;
							}
						} else if (typeof x === "number" && typeof y === "number") {
							clickedElement = self._findClickedElement(x, y);
						}
						if (clickedElement) {
							self._placementCoordsOrigin(clickedElement, options);
							return;
						}
						break;
					case positionTypes.WINDOW:
						self._placementCoordsWindow(element);
						return;
						break;
					case positionTypes.ABSOLUTE:
						if (typeof x === "number" && typeof y === "number") {
							self._placementCoordsAbsolute(element, x, y);
							return;
						}
						break;
					default:
						// there is posible, that element or its id was given
						if (typeof positionTo === "string") {
							try {
								clickedElement = document.querySelector(options.positionTo);
							} catch(e) {}
						} else if (typeof positionTo === "object") {
							clickedElement = positionTo;
						}
						if (clickedElement) {
							self._placementCoordsElement(clickedElement, options);
							return;
						}
						break;
				}

				// if there was problem with setting position of popup, we set its position to window
				self._placementCoordsWindow(element);
			};

			/**
			 * Set height for popup's container.
			 * @method _setContentHeight
			 * @param {number} maxHeight
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._setContentHeight = function(maxHeight) {
				var self = this,
					element = self.element,
					content = self._ui.content,
					contentStyle,
					contentHeight,
					elementOffsetHeight;

				if (content) {
					contentStyle = content.style;

					if (contentStyle.height || contentStyle.minHeight) {
						contentStyle.height = "";
						contentStyle.minHeight = "";
					}

					maxHeight = maxHeight || window.innerHeight;

					contentHeight = content.offsetHeight;
					elementOffsetHeight = element.offsetHeight;

					if (elementOffsetHeight > maxHeight) {
						contentHeight -= (elementOffsetHeight - maxHeight);
						contentStyle.height = contentHeight + "px";
						contentStyle.minHeight = contentHeight + "px";
					}
				}
			};

			/**
			 * Hide popup.
			 * @method _onHide
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._onHide = function() {
				var self = this,
					ui = self._ui,
					element = self.element,
					elementClassList = element.classList,
					content = ui.content,
					arrow = ui.arrow;

				elementClassList.remove(classes.context);
				["l", "r", "b", "t"].forEach(function(key) {
					elementClassList.remove(classes.arrowDir + key);
				});

				// we remove styles for element, which are changed
				// styles for container, header and footer are left unchanged
				element.removeAttribute("style");
				arrow.removeAttribute("style");

				PopupPrototype._onHide.call(self);
			};

			/**
			 * Destroy popup.
			 * @method _destroy
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._destroy = function() {
				var self = this,
					element = self.element,
					ui = self._ui,
					arrow = ui.arrow;

				PopupPrototype._destroy.call(self);

				if (arrow && arrow.parentNode) {
					arrow.parentNode.removeChild(arrow);
				}

				ui.arrow = null;
			};

			/**
			 * Set new position for popup.
			 * @method reposition
			 * @param options
			 * @param options.x
			 * @param options.y
			 * @param options.positionTo
			 * @member ns.widget.core.ContextPopup
			 */
			prototype.reposition = function(options) {
				if (this._isActive()) {
					this._reposition(options);
				}
			};

			/**
			 * Refresh structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.core.ContextPopup
			 */
			prototype._refresh = function() {
				if (this._isActive()) {
					PopupPrototype._refresh.call(this);
					this.reposition(this.options);
				}
			};

			ContextPopup.prototype = prototype;
			ns.widget.core.ContextPopup = ContextPopup;

			engine.defineWidget(
				"Popup",
				"[data-role='popup'], .ui-popup",
				[
					"open",
					"close",
					"reposition"
				],
				ContextPopup,
				"core",
				true
			);

			// @remove
			// THIS IS ONLY FOR COMPATIBILITY
			ns.widget.popup = ns.widget.Popup;

			}(window, window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2013 - 2014 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */

/**
 * # Popup Widget
 * Shows a pop-up window.
 *
 * The popup widget shows in the middle of the screen a list of items in a pop-up window. It automatically optimizes the pop-up window size within the screen. The following table describes the supported popup classes.
 *
 * ## Default selectors
 * All elements with class *ui-popup* will be become popup widgets.
 *
 * The pop-up window can contain a header, content, and footer area like the page element.
 *
 * To open a pop-up window from a link, use the data-rel attribute in HTML markup as in the following code:
 *
 *      @example
 *      <a href="#popup" class="ui-btn" data-rel="popup">Open popup when clicking this element.</a>
 *
 * The following table shows examples of various types of popups.
 *
 * The popup contains header, content and footer area
 *
 * ###HTML Examples
 *
 * #### Basic popup with header, content, footer
 *
 *		@example
 *		<div class="ui-page">
 *		    <div class="ui-popup">
 *		        <div class="ui-popup-header">Power saving mode</div>
 *		        <div class="ui-popup-content">
 *		            Turning on Power
 *		            saving mode will
 *		            limit the maximum
 *		            per
 *		        </div>
 *		        <div class="ui-popup-footer">
 *		            <button id="cancel" class="ui-btn">Cancel</button>
 *		        </div>
 *		    </div>
 *		</div>
 *
 * #### Popup with 2 buttons in the footer
 *
 *      @example
 *         <div id="2btnPopup" class="ui-popup">
 *             <div class="ui-popup-header">Delete</div>
 *             <div class="ui-popup-content">
 *                 Delete the image?
 *             </div>
 *             <div class="ui-popup-footer ui-grid-col-2">
 *                 <button id="2btnPopup-cancel" class="ui-btn">Cancel</button>
 *                 <button id="2btnPopup-ok" class="ui-btn">OK</button>
 *             </div>
 *         </div>
 *
 * #### Popup with checkbox/radio
 *
 * If you want make popup with list checkbox(or radio) just include checkbox (radio) to popup and add class *ui-popup-checkbox-label* to popup element.
 *
 *		@example
 *         <div id="listBoxPopup" class="ui-popup">
 *             <div class="ui-popup-header">When?</div>
 *             <div class="ui-popup-content" style="height:243px; overflow-y:scroll">
 *                 <ul class="ui-listview">
 *                     <li>
 *                         <label for="check-1" class="ui-popup-checkbox-label">Yesterday</label>
 *                         <input type="checkbox" name="checkset" id="check-1" />
 *                     </li>
 *                     <li>
 *                         <label for="check-2" class="ui-popup-checkbox-label">Today</label>
 *                         <input type="checkbox" name="checkset" id="check-2" />
 *                     </li>
 *                     <li>
 *                         <label for="check-3" class="ui-popup-checkbox-label">Tomorrow</label>
 *                         <input type="checkbox" name="checkset" id="check-3" />
 *                     </li>
 *                 </ul>
 *                 <ul class="ui-listview">
 *                     <li>
 *                         <label for="radio-1" class="ui-popup-radio-label">Mandatory</label>
 *                         <input type="radio" name="radioset" id="radio-1" />
 *                     </li>
 *                     <li>
 *                         <label for="radio-2" class="ui-popup-radio-label">Optional</label>
 *                         <input type="radio" name="radioset" id="radio-2" />
 *                     </li>
 *                 </ul>
 *             </div>
 *             <div class="ui-popup-footer">
 *                 <button id="listBoxPopup-close" class="ui-btn">Close</button>
 *             </div>
 *         </div>
 *     </div>
 *
 * #### Popup with no header and footer
 *
 *      @example
 *         <div id="listNoTitleNoBtnPopup" class="ui-popup">
 *             <div class="ui-popup-content" style="height:294px; overflow-y:scroll">
 *                 <ul class="ui-listview">
 *                     <li><a href="">Ringtones 1</a></li>
 *                     <li><a href="">Ringtones 2</a></li>
 *                     <li><a href="">Ringtones 3</a></li>
 *                 </ul>
 *             </div>
 *         </div>
 *
 * #### Toast popup
 *
 *      @example
 *         <div id="PopupToast" class="ui-popup ui-popup-toast">
 *             <div class="ui-popup-content">Saving contacts to sim on Samsung</div>
 *         </div>
 *
 * ### Create Option popup
 *
 * Popup inherits value of option positionTo from property data-position-to set in link.
 *
 *		@example
 *		<!--definition of link, which opens popup and sets its position-->
 *		<a href="#popupOptionText" data-rel="popup"  data-position-to="origin">Text</a>
 *		<!--definition of popup, which inherites property position from link-->
 *		<div id="popupOptionText" class="ui-popup">
 *			<div class="ui-popup-content">
 *				<ul class="ui-listview">
 *				<li><a href="#">Option 1</a></li>
 *				<li><a href="#">Option 2</a></li>
 *				<li><a href="#">Option 3</a></li>
 *				<li><a href="#">Option 4</a></li>
 *				</ul>
 *			</div>
 *		</div>
 *
 * ### Opening and closing popup
 *
 * To open popup from "a" link using html markup, use the following code:
 *
 *		@example
 *      <div class="ui-page">
 *          <header class="ui-header">
 *              <h2 class="ui-title">Call menu</h2>
 *          </header>
 *          <div class="ui-content">
 *              <a href="#popup" class="ui-btn" data-rel="popup" >Open Popup</a>
 *          </div>
 *
 *          <div id="popup" class="ui-popup">
 *               <div class="ui-popup-header">Power saving mode</div>
 *                   <div class="ui-popup-content">
 *                       Turning on Power
 *                       saving mode will
 *                       limit the maximum
 *                       per
 *                   </div>
 *               <div class="ui-popup-footer">
 *               <button id="cancel" class="ui-btn">Cancel</button>
 *           </div>
 *       </div>
 *
 *  To open the popup widget from JavaScript use method *tau.openPopup(to)*
 *
 *          @example
 *          tau.openPopup("popup")
 *
 *  To close the popup widget from JavaScript use method *tau.openPopup(to)*
 *
 *          @example
 *          tau.closePopup("popup")
 *
 * To find the currently active popup, use the ui-popup-active class.
 *
 * To bind the popup to a button, use the following code:
 *
 *      @example
 *         <!--HTML code-->
 *         <div id="1btnPopup" class="ui-popup">
 *             <div class="ui-popup-header">Power saving mode</div>
 *             <div class="ui-popup-content">
 *             </div>
 *             <div class="ui-popup-footer">
 *                 <button id="1btnPopup-cancel" class="ui-btn">Cancel</button>
 *             </div>
 *         </div>
 *         <script>
 *             // Popup opens with button click
 *             var button = document.getElementById("button");
 *             button.addEventListener("click", function() {
 *                 tau.openPopup("#1btnPopup");
 *             });
 *
 *             // Popup closes with Cancel button click
 *             document.getElementById("1btnPopup-cancel").addEventListener("click", function() {
 *                 tau.closePopup();
 *             });
 *         </script>
 *
 * ## Manual constructor
 * For manual creation of popup widget you can use constructor of widget from **tau** namespace:
 *
 *		@example
 *		var popupElement = document.getElementById("popup"),
 *			popup = tau.widget.popup(buttonElement);
 *
 * Constructor has one require parameter **element** which are base **HTMLElement** to create widget. We recommend get this element by method *document.getElementById*.
 *
 * ## Options for Popup Widget
 *
 * Options for widget can be defined as _data-..._ attributes or give as parameter in constructor.
 *
 * You can change option for widget using method **option**.
 *
 * ## Methods
 *
 * To call method on widget you can use tau API:
 *
 *		@example
 *		var popupElement = document.getElementById("popup"),
 *			popup = tau.widget.popup(buttonElement);
 *
 *		popup.methodName(methodArgument1, methodArgument2, ...);
 *
 * ## Transitions
 *
 * By default, the framework doesn't apply transition. To set a custom transition effect, add the data-transition attribute to the link.
 *
 *		@example
 *		<a href="index.html" data-rel="popup" data-transition="slideup">I'll slide up</a>
 *
 * Global configuration:
 *
 *		@example
 *		gear.ui.defaults.popupTransition = "slideup";
 *
 * ### Transitions list
 *
 * - **none** Default value, no transition.
 * - **slideup** Makes the content of the pop-up slide up.
 *
 * ## Handling Popup Events
 *
 * To use popup events, use the following code:
 *
 *      @example
 *         <!--Popup html code-->
 *         <div id="popup" class="ui-popup">
 *             <div class="ui-popup-header"></div>
 *             <div class="ui-popup-content"></div>
 *         </div>
 *         </div>
 *         <script>
 *             // Use popup events
 *             var popup = document.getElementById("popup");
 *             popup.addEventListener("popupbeforecreate", function() {
 *                 // Implement code for popupbeforecreate event
 *             });
 *         </script>
 *
 * Full list of available events is in [events list section](#events-list).
 *
 * @author Hyunkook Cho <hk0713.cho@samsung.com>
 * @class ns.widget.core.Popup
 * @extends ns.widget.core.ContextPopup
 */
(function (window, document, ns) {
	
	
			var CorePopup = ns.widget.core.ContextPopup,

				CorePopupPrototype = CorePopup.prototype,

				engine = ns.engine,

				objectUtils = ns.util.object,

				domUtils = ns.util.DOM,

				defaults = {
					fullSize: false,
					enablePopupScroll: false
				},

				classes = objectUtils.merge({}, CorePopup.classes, {
					popupScroll: "ui-scroll-on",
					fixed: "ui-fixed",
					sideButton: "ui-side-button",
					hasSideButtons: "ui-has-side-buttons",
					toast: "ui-popup-toast",
					ctx: "ui-ctxpopup"
				}),

				Popup = function () {
					var self = this;

					CorePopup.call(self);
					self.options = objectUtils.merge(self.options, {
						fullSize: ns.getConfig("popupFullSize", defaults.fullSize),
						enablePopupScroll: ns.getConfig("enablePopupScroll", defaults.enablePopupScroll)
					});
				},

				prototype = new CorePopup();

			/**
			 * Layouting popup structure
			 * @method layout
			 * @member ns.widget.wearable.Popup
			 */
			prototype._layout = function (element) {
				var self = this,
					elementClassList = element.classList,
					ui = self._ui,
					wrapper = ui.wrapper,
					header = ui.header,
					footer = ui.footer,
					content = ui.content,
					headerHeight = 0,
					footerHeight = 0;

				self._blockPageScroll();

				CorePopupPrototype._layout.call(self, element);

				if (self.options.enablePopupScroll === true) {
					element.classList.add(classes.popupScroll);
				} else {
					element.classList.remove(classes.popupScroll);
				}

				if (elementClassList.contains(classes.popupScroll)) {
					elementClassList.add(classes.build);

					if (header) {
						headerHeight = header.offsetHeight;
						if (header.classList.contains(classes.fixed)) {
							content.style.marginTop = headerHeight + "px";
						}
					}
					if (footer) {
						footerHeight = footer.offsetHeight;
						if (footer.classList.contains(classes.fixed)) {
							content.style.marginBottom = footerHeight + "px";
						}
						if (footer.classList.contains(classes.sideButton)) {
							elementClassList.add(classes.hasSideButtons);
						}
					}

					wrapper.style.height = Math.min(content.offsetHeight + headerHeight + footerHeight, element.offsetHeight) + "px";

					elementClassList.remove(classes.build);
				}

				if (self.options.fullSize && !elementClassList.contains(classes.toast) && !elementClassList.contains(classes.ctx)) {
					wrapper.style.height = window.innerHeight + "px";
				}
			};

			/**
			 * Hide popup.
			 * @method _onHide
			 * @protected
			 * @member ns.widget.wearable.Popup
			 */
			prototype._onHide = function() {
				var self = this,
					ui = self._ui,
					wrapper = ui.wrapper;

				wrapper.removeAttribute("style");
				self._unblockPageScroll();
				CorePopupPrototype._onHide.call(self);
			};

			prototype._blockPageScroll = function() {
				var page = ns.widget.Page(this._ui.page);
				if (page.getScroller) {
					page.getScroller().style.overflow = "hidden";
				}
			};

			prototype._unblockPageScroll = function() {
				var page = ns.widget.Page(this._ui.page);
				if (page.getScroller) {
					page.getScroller().style.overflow = "";
				}
			};

			Popup.prototype = prototype;
			ns.widget.wearable.Popup = Popup;

			engine.defineWidget(
				"Popup",
				"[data-role='popup'], .ui-popup",
				[
					"open",
					"close",
					"reposition"
				],
				Popup,
				"wearable",
				true
			);

			}(window, window.document, ns));

/*global define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Router
 * Namespace for routers
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @class ns.router
 */
(function (ns) {
	
				ns.router = ns.router || {};
			}(ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #History
 * Object controls history changes.
 *
 * @class ns.router.history
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 */
(function (window, ns) {
	
				var historyVolatileMode,
				object = ns.util.object,
				historyUid = 0,
				historyActiveIndex = 0,
				windowHistory = window.history,
				history = {
					/**
					 * Property contains active state in history.
					 * @property {Object} activeState
					 * @static
					 * @member ns.router.history
					 */
					activeState : null,

					/**
					 * This method replaces or pushes state to history.
					 * @method replace
					 * @param {Object} state The state object
					 * @param {string} stateTitle The title of state
					 * @param {string} url The new history entry's URL
					 * @static
					 * @member ns.router.history
					 */
					replace: function (state, stateTitle, url) {
						var newState = object.merge({}, state, {
								uid: historyVolatileMode ? historyActiveIndex : ++historyUid,
								stateUrl: url,
								stateTitle: stateTitle
							});
						windowHistory[historyVolatileMode ? "replaceState" : "pushState"](newState, stateTitle, url);
						history.setActive(newState);
					},

					/**
					 * This method moves backward through history.
					 * @method back
					 * @static
					 * @member ns.router.history
					 */
					back: function () {
						windowHistory.back();
					},

					/**
					 * This method sets active state.
					 * @method setActive
					 * @param {Object} state Activated state
					 * @static
					 * @member ns.router.history
					 */
					setActive: function (state) {
						if (state) {
							history.activeState = state;
							historyActiveIndex = state.uid;

							if (state.volatileRecord) {
								history.enableVolatileRecord();
								return;
							}
						}

						history.disableVolatileMode();
					},

					/**
					 * This method returns "back" if state is in history or "forward" if it is new state.
					 * @method getDirection
					 * @param {Object} state Checked state
					 * @return {"back"|"forward"}
					 * @static
					 * @member ns.router.history
					 */
					getDirection: function (state) {
						if (state) {
							return state.uid < historyActiveIndex ? "back" : "forward";
						}
						return "back";
					},

					/**
					 * This method sets volatile mode to true.
					 * @method enableVolatileRecord
					 * @static
					 * @member ns.router.history
					 */
					enableVolatileRecord: function () {
						historyVolatileMode = true;
					},

					/**
					 * This method sets volatile mode to false.
					 * @method disableVolatileMode
					 * @static
					 * @member ns.router.history
					 */
					disableVolatileMode: function () {
						historyVolatileMode = false;
					}
				};
			ns.router.history = history;
			}(window, ns));

/*global window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Drawer Widget
 * Core Drawer widget is a base for creating Drawer widgets for profiles. It
 * provides drawer functionality - container with ability to open and close with
 * an animation.
 *
 * ##Positioning Drawer left / right
 * To change position of a Drawer please set data-position attribute of Drawer
 * element to:
 * - left (left position, default)
 * - right (right position)
 *
 * ##Opening / Closing Drawer
 * To open / close Drawer one can use open() and close() methods.
 *
 * ##Checking if Drawer is opened.
 * To check if Drawer is opened use widget`s isOpen() method.
 *
 * ##Creating widget
 * Core drawer is a base class - examples of creating widgets are described in
 * documentation of profiles
 *
 * @class ns.widget.core.Drawer
 * @extends ns.widget.BaseWidget
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 */
(function (document, ns) {
	
				/**
			 * @property {Object} Widget Alias for {@link ns.widget.BaseWidget}
			 * @member ns.widget.core.Drawer
			 * @private
			 * @static
			 */
			var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,
				/**
				 * @property {Object} selectors Alias for class ns.util.selectors
				 * @member ns.widget.core.Drawer
				 * @private
				 * @static
				 * @readonly
				 */
				selectors = ns.util.selectors,
				utilDOM = ns.util.DOM,
				events = ns.event,
				history = ns.router.history,
				Gesture = ns.event.gesture,
				Page = ns.widget.core.Page,
				STATE = {
					CLOSED: "closed",
					OPENED: "opened",
					SLIDING: "sliding",
					SETTLING: "settling"
				},
				CUSTOM_EVENTS = {
					OPEN: "draweropen",
					CLOSE: "drawerclose"
				},
				/**
				 * Default values
				 */
				DEFAULT = {
					WIDTH: 240,
					DURATION: 300,
					POSITION: "left"
				},
				/**
				 * Drawer constructor
				 * @method Drawer
				 */
				Drawer = function () {
					var self = this;
					/**
					 * Drawer field containing options
					 * @property options.position {string} Position of Drawer ("left" or "right")
					 * @property options.width {number} Width of Drawer
					 * @property options.duration {number} Duration of Drawer entrance animation
					 * @property options.closeOnClick {boolean} If true Drawer will be closed on overlay
					 * @property options.overlay {boolean} Sets whether to show an overlay when Drawer is open.
					 * @property options.drawerTarget {string} Set drawer target element as the css selector
					 * @property options.enable {boolean} Enable drawer component
					 * @property options.dragEdge {number} Set the area that can open the drawer as drag gesture in drawer target element
					 */
					self.options = {
						position : DEFAULT.POSITION,
						width : DEFAULT.WIDTH,
						duration : DEFAULT.DURATION,
						closeOnClick: true,
						overlay: true,
						drawerTarget: "." + Page.classes.uiPage,
						enable: true,
						dragEdge: 1
					};

					self._pageSelector = null;

					self._isDrag = false;
					self._state = STATE.CLOSED;
					self._settlingType = STATE.CLOSED;
					self._traslatedX = 0;

					self._ui = {};

					self._eventBoundElement = null;
					self._drawerOverlay = null;
				},
				/**
				 * Dictionary object containing commonly used widget classes
				 * @property {Object} classes
				 * @member ns.widget.core.Drawer
				 * @private
				 * @static
				 * @readonly
				 */
				classes = {
					page : Page.classes.uiPage,
					drawer : "ui-drawer",
					left : "ui-drawer-left",
					right : "ui-drawer-right",
					overlay : "ui-drawer-overlay",
					open : "ui-drawer-open",
					close : "ui-drawer-close"
				},
				/**
				 * {Object} Drawer widget prototype
				 * @member ns.widget.core.Drawer
				 * @private
				 * @static
				 */
				prototype = new BaseWidget();

			Drawer.prototype = prototype;
			Drawer.classes = classes;

			/**
			 * Unbind drag events
			 * @method unbindDragEvents
			 * @param {Object} self
			 * @param {HTMLElement} element
			 * @member ns.widget.core.Drawer
			 * @private
			 * @static
			 */
			function unbindDragEvents(self, element) {
				var overlayElement = self._ui.drawerOverlay;

				events.disableGesture(element);
				events.off(element, "drag dragstart dragend dragcancel swipe swipeleft swiperight vmouseup", self, false);
				events.prefixedFastOff(self.element, "transitionEnd", self, false);
				events.off(window, "resize", self, false);
				if (overlayElement) {
					events.off(overlayElement, "vclick", self, false);
				}
			}

			/**
			 * Bind drag events
			 * @method bindDragEvents
			 * @param {Object} self
			 * @param {HTMLElement} element
			 * @member ns.widget.core.Drawer
			 * @private
			 * @static
			 */
			function bindDragEvents(self, element) {
				var overlayElement = self._ui.drawerOverlay;
				self._eventBoundElement = element;

				events.enableGesture(
					element,

					new Gesture.Drag(),
					new Gesture.Swipe({
						orientation: Gesture.Orientation.HORIZONTAL
					})
				);

				events.on(element, "drag dragstart dragend dragcancel swipe swipeleft swiperight vmouseup", self, false);
				events.prefixedFastOn(self.element, "transitionEnd", self, false);
				events.on(window, "resize", self, false);
				if (overlayElement) {
					events.on(overlayElement, "vclick", self, false);
				}
			}
			/**
			 * Handle events
			 * @method handleEvent
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 */
			prototype.handleEvent = function (event) {
				var self = this;
				switch (event.type) {
					case "drag":
						self._onDrag(event);
						break;
					case "dragstart":
						self._onDragStart(event);
						break;
					case "dragend":
						self._onDragEnd(event);
						break;
					case "dragcancel":
						self._onDragCancel(event);
						break;
					case "vmouseup":
						self._onMouseup(event);
						break;
					case "swipe":
					case "swipeleft":
					case "swiperight":
						self._onSwipe(event);
						break;
					case "vclick":
						self._onClick(event);
						break;
					case "transitionend":
					case "webkitTransitionEnd":
					case "mozTransitionEnd":
					case "oTransitionEnd":
					case "msTransitionEnd":
						self._onTransitionEnd(event);
						break;
					case "resize":
						self._onResize(event);
						break;
				}
			};

			/**
			 * MouseUp event handler
			 * @method _onMouseup
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._onMouseup = function (event) {
				var self = this;
				if (self._state === STATE.SLIDING) {
					self.close();
				}
			};
			/**
			 * Click event handler
			 * @method _onClick
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._onClick = function (event) {
				var self = this;
				if (self._state === STATE.OPENED) {
					self.close();
				}
			};

			/**
			 * Resize event handler
			 * @method _onResize
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._onResize = function (event) {
				var self = this;
				// resize event handler
				self._refresh();
			};

			/**
			 * webkitTransitionEnd event handler
			 * @method _onTransitionEnd
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._onTransitionEnd = function (event) {
				var self = this,
					position = self.options.position,
					drawerOverlay = self._drawerOverlay;

				if (self._state === STATE.SETTLING) {
					if (self._settlingType === STATE.OPENED) {
						self.trigger(CUSTOM_EVENTS.OPEN, {
							position: position
						});
						self._setActive(true);
						self._state = STATE.OPENED;
					} else {
						self.close();
						self.trigger(CUSTOM_EVENTS.CLOSE, {
							position: position
						});
						self._setActive(false);
						self._state = STATE.CLOSED;
						if (drawerOverlay) {
							drawerOverlay.style.visibility = "hidden";
						}
					}
				}
			};

			/**
			 * Swipe event handler
			 * @method _onSwipe
			 * @protected
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 */
			prototype._onSwipe = function (event) {
				var self = this,
					direction,
					options = self.options;

				// Now mobile has two swipe event
				if (event.detail) {
					direction = event.detail.direction === "left" ? "right" : "left";
				} else if (event.type === "swiperight") {
					direction = "left";
				} else if (event.type === "swipeleft") {
					direction = "right";
				}
				if (options.enable && self._isDrag && options.position === direction) {
					self.open();
					self._isDrag = false;
				}
			};
			/**
			 * Dragstart event handler
			 * @method _onDragStart
			 * @protected
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 */
			prototype._onDragStart = function (event) {
				var self = this;
				if (self._state === STATE.OPENED) {
					return;
				}
				if (self.options.enable && !self._isDrag && self._state !== STATE.SETTLING && self._checkSideEdge(event)) {
					self._isDrag = true;
				} else {
					self.close();
				}
			};
			/**
			 * Drag event handler
			 * @method _onDrag
			 * @protected
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 */
			prototype._onDrag = function (event) {
				var self = this,
					deltaX = event.detail.deltaX,
					options = self.options,
					translatedX = self._traslatedX,
					movedX;

				if (options.enable && self._isDrag && self._state !== STATE.SETTLING) {
					if (options.position === "left") {
						movedX = -options.width + deltaX + translatedX;
						if (movedX < 0) {
							self._translate(movedX, 0);
						}
					} else {
						movedX = window.innerWidth + deltaX - translatedX;
						if (movedX > 0 && movedX > window.innerWidth - options.width) {
							self._translate(movedX, 0);
						}
					}
				}
			};
			/**
			 * DragEnd event handler
			 * @method _onDragEnd
			 * @protected
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 */
			prototype._onDragEnd = function (event) {
				var self = this,
					options = self.options,
					detail = event.detail;
				if (options.enable && self._isDrag) {
					if (Math.abs(detail.deltaX) > options.width / 2) {
						self.open();
					} else if (self._state !== STATE.SETTLING) {
						self.close();
					}
				}
				self._isDrag = false;
			};

			/**
			 * DragCancel event handler
			 * @method _onDragCancel
			 * @protected
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 */
			prototype._onDragCancel = function (event) {
				var self = this;
				if (self.options.enable && self._isDrag) {
					self.close();
				}
				self._isDrag = false;
			};
			/**
			 * Drawer translate function
			 * @method _translate
			 * @param {number} x
			 * @param {number} duration
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._translate = function (x, duration) {
				var self = this,
					element = self.element;

				if (self._state !== STATE.SETTLING) {
					self._state = STATE.SLIDING;
				}

				if (duration) {
					utilDOM.setPrefixedStyle(element, "transition", utilDOM.getPrefixedValue("transform " + duration / 1000 + "s ease-out"));
				}

				// there should be a helper for this :(
				utilDOM.setPrefixedStyle(element, "transform", "translate3d(" + x + "px, 0px, 0px)");
				if (self.options.overlay) {
					self._setOverlay(x);
				}
				if (!duration) {
					self._onTransitionEnd();
				}

			};

			/**
			 * Set overlay opacity and visibility
			 * @method _setOverlay
			 * @param {number} x
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._setOverlay = function (x) {
				var self = this,
					options = self.options,
					overlay = self._ui.drawerOverlay,
					overlayStyle = overlay.style,
					absX = Math.abs(x),
					ratio = options.position === "right" ? absX / window.innerWidth : absX / options.width;

				if (ratio < 1) {
					overlayStyle.visibility = "visible";
				} else {
					overlayStyle.visibility = "hidden";
				}
				overlayStyle.opacity = 1 - ratio;
			};

			/**
			 * Set active status in drawer router
			 * @method _setActive
			 * @param {boolean} active
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._setActive = function (active) {
				var self = this,
					route = engine.getRouter().getRoute("drawer");

				if (active) {
					route.setActive(self);
				} else {
					route.setActive(null);
				}
			};

			/**
			 * Build structure of Drawer widget
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement} Returns built element
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._build = function (element) {
				var self = this,
					ui = self._ui,
					options = self.options,
					targetElement;
				element.classList.add(classes.drawer);
				element.style.top = 0;
				targetElement = selectors.getClosestBySelector(element, options.drawerTarget);

				if (targetElement) {
					targetElement.appendChild(element);
					targetElement.style.overflowX = "hidden";
				}

				if (self.options.overlay) {
					ui.drawerOverlay = self._createOverlay(element);
					ui.drawerOverlay.style.visibility = "hidden";
				}

				if (!ui.placeholder) {
					ui.placeholder = document.createComment(element.id + "-placeholder");
					element.parentNode.insertBefore(ui.placeholder, element);
				}
				ui.targetElement = targetElement;
				return element;
			};

			/**
			 * Initialization of Drawer widget
			 * @method _init
			 * @param {HTMLElement} element
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._init = function (element) {
				var self = this,
					ui = self._ui;
				ui.drawerPage = selectors.getClosestByClass(element, classes.page);
				ui.drawerPage.style.overflowX = "hidden";
				self._initLayout();
				return element;
			};

			/**
			 * init Drawer widget layout
			 * @method _initLayout
			 * @protected
			 * @member ns.widget.core.Drawer
			 */
			prototype._initLayout = function () {
				var self = this,
					options = self.options,
					element = self.element,
					elementStyle = element.style,
					ui = self._ui,
					overlayStyle = ui.drawerOverlay ? ui.drawerOverlay.style : false;

				options.width = options.width || ui.targetElement.offsetWidth;

				elementStyle.width = options.width + "px";
				elementStyle.height = ui.targetElement.offsetHeight + "px";

				if (overlayStyle) {
					overlayStyle.width = window.innerWidth + "px";
					overlayStyle.height = window.innerHeight + "px";
					overlayStyle.top = 0;
				}
				if (options.position === "right") {
					element.classList.add(classes.right);
					self._translate(window.innerWidth, 0);
				} else {
					// left or default
					element.classList.add(classes.left);
					self._translate(-options.width, 0);
				}
				self._state = STATE.CLOSED;
			};

			/**
			 * Provides translation if position is set to right
			 * @method _translateRight
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._translateRight = function () {
				var self = this,
					options = self.options;
				if (options.position === "right") {
					// If drawer position is right, drawer should be moved right side
					if (self._state === STATE.OPENED) {
						// drawer opened
						self._translate(window.innerWidth - options.width, 0);
					} else {
						// drawer closed
						self._translate(window.innerWidth, 0);
					}
				}
			};

			/**
			 * Check dragstart event whether triggerred on side edge area or not
			 * @method _checkSideEdge
			 * @protected
			 * @param {Event} event
			 * @member ns.widget.core.Drawer
			 */
			prototype._checkSideEdge = function (event) {
				var self = this,
					detail = event.detail,
					eventClientX = detail.pointer.clientX - detail.estimatedDeltaX,
					options = self.options,
					position = options.position,
					boundElement = self._eventBoundElement,
					boundElementOffsetWidth = boundElement.offsetWidth,
					boundElementRightEdge = boundElement.offsetLeft + boundElementOffsetWidth,
					dragStartArea = boundElementOffsetWidth * options.dragEdge;

				return ((position === "left" && eventClientX > 0 && eventClientX < dragStartArea) ||
				(position === "right" && eventClientX > boundElementRightEdge - dragStartArea &&
				eventClientX < boundElementRightEdge));
			};
			/**
			 * Refreshes Drawer widget
			 * @method _refresh
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._refresh = function () {
				// Drawer layout has been set by parent element layout
				var self = this;

				self._translateRight();
				self._initLayout();
			};
			/**
			 * Creates Drawer overlay element
			 * @method _createOverlay
			 * @param {HTMLElement} element
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._createOverlay = function (element) {
				var overlayElement = document.createElement("div");

				overlayElement.classList.add(classes.overlay);
				element.parentNode.insertBefore(overlayElement, element);

				return overlayElement;
			};

			/**
			 * Binds events to a Drawer widget
			 * @method _bindEvents
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._bindEvents = function () {
				var self = this,
					targetElement = self._ui.targetElement;

				bindDragEvents(self, targetElement);
			};

			/**
			 * Enable Drawer widget
			 * @method _enable
			 * @protected
			 * @member ns.widget.core.Drawer
			 */
			prototype._enable = function () {
				this._oneOption("enable", true);
			};

			/**
			 * Disable Drawer widget
			 * @method _disable
			 * @protected
			 * @member ns.widget.core.Drawer
			 */
			prototype._disable = function () {
				this._oneOption("enable", false);
			};

			/**
			 * Checks Drawer status
			 * @method isOpen
			 * @member ns.widget.core.Drawer
			 * @return {boolean} Returns true if Drawer is open
			 */
			prototype.isOpen = function () {
				return (this._state === STATE.OPENED);
			};

			/**
			 * Opens Drawer widget
			 * @method open
			 * @param {number} [duration] Duration for opening, if is not set then method take value from options
			 * @member ns.widget.core.Drawer
			 */
			prototype.open = function (duration) {
				var self = this,
					options = self.options,
					drawerClassList = self.element.classList,
					drawerOverlay = self._ui.drawerOverlay;
				if (self._state !== STATE.OPENED) {
					self._state = STATE.SETTLING;
					self._settlingType = STATE.OPENED;
					duration = duration !== undefined ? duration : options.duration;
					if (drawerOverlay) {
						drawerOverlay.style.visibility = "visible";
					}
					drawerClassList.remove(classes.close);
					drawerClassList.add(classes.open);
					if (options.position === "left") {
						self._translate(0, duration);
					} else {
						self._translate(window.innerWidth - options.width, duration);
					}
				}
			};

			/**
			 * Closes Drawer widget
			 * @method close
			 * @param {object} options This value is router options whether reverse or not.
			 * @param {number} [duration] Duration for closing, if is not set then method take value from options
			 * @member ns.widget.core.Drawer
			 */
			prototype.close = function (options, duration) {
				var self = this,
					reverse = options ? options.reverse : false,
					selfOptions = self.options,
					drawerClassList = self.element.classList;
				if (self._state !== STATE.CLOSED) {
					if (!reverse && self._state === STATE.OPENED) {
						// This method was fired by JS code or this widget.
						history.back();
						return;
					}
					self._state = STATE.SETTLING;
					self._settlingType = STATE.CLOSED;
					duration = duration !== undefined ? duration : selfOptions.duration;
					drawerClassList.remove(classes.open);
					drawerClassList.add(classes.close);
					if (selfOptions.position === "left") {
						self._translate(-selfOptions.width, duration);
					} else {
						self._translate(window.innerWidth, duration);
					}
				}
			};

			/**
			 * Set Drawer drag handler.
			 * If developer use handler, drag event is bound at handler only.
			 * @method setDragHandler
			 * @param {HTMLElement} element
			 * @member ns.widget.core.Drawer
			 */
			prototype.setDragHandler = function (element) {
				var self = this;
				self.options.dragEdge = 1;
				unbindDragEvents(self, self._eventBoundElement);
				bindDragEvents(self, element);
			};

			/**
			 * Transition Drawer widget.
			 * This method use only positive integer number.
			 * @method transition
			 * @param {number} position
			 * @member ns.widget.core.Drawer
			 */
			prototype.transition = function (position) {
				var self = this,
					options = self.options;
				if (options.position === "left"){
					self._translate(-options.width + position, options.duration);
				} else {
					self._translate(options.width - position , options.duration);
				}
				self._traslatedX = position;
			};

			/**
			 * Get state of Drawer widget.
			 */
			prototype.getState = function () {
				return this._state;
			};
			/**
			 * Destroys Drawer widget
			 * @method _destroy
			 * @member ns.widget.core.Drawer
			 * @protected
			 */
			prototype._destroy = function () {
				var self = this,
					ui = self._ui,
					drawerOverlay = ui.drawerOverlay,
					placeholder = ui.placeholder,
					placeholderParent = placeholder.parentNode,
					element = self.element;

				placeholderParent.insertBefore(element, placeholder);
				placeholderParent.removeChild(placeholder);

				if (drawerOverlay) {
					drawerOverlay.removeEventListener("vclick", self._onClickBound, false);
				}
				unbindDragEvents(self, self._eventBoundElement);
				ui = null;
			};

			ns.widget.core.Drawer = Drawer;

			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Drawer Widget in Wearable
 * The drawer component is a panel that the application's sub layout on the left or right edge of the screen.
 * This component is hidden most of the time, but user can be opened as swipe gesture from the edge of the screen or click the element that is added event handler,
 * handler has drawer.open() method.
 *
 * Note!
 * We recommend to make handler element.
 * Because if you didn't set the handler, handler was set page element automatically.
 * If you really want to make handler as the page element, you should notice data-drag-edge or dragEdge option value
 * because default value, '1', is whole area of handler element.
 *
 * ## HTML Examples
 *
 *        @example
 *        <div id="drawerPage" class="ui-page">
 *          <header id="contentHeader" class="ui-header">
 *              <h2 class="ui-title">Drawer</h2>
 *          </header>
 *          <div id = "content" class="ui-content">
 *            Drawer
 *          </div>
 *
 *          <!-- Drawer Handler -->
 *          <a id="drawerHandler" href="#Drawer" class="drawer-handler">Drawer Button</a>
 *          <!-- Drawer Widget -->
 *          <div id="drawer" class="ui-drawer" data-drawer-target="#drawerPage" data-position="left" data-enable="true" data-drag-edge="1">
 *              <header class="ui-header">
 *                  <h2 class="ui-title">Left Drawer</h2>
 *              </header>
 *              <div class="ui-content">
 *                  <p>CONTENT</p>
 *              </div>
 *          </div>
 *        </div>
 *
 * ## Manual constructor
 *
 *         @example
 *             (function() {
 *                 var handler = document.getElementById("drawerHandler"),
 *                     page = document.getElementById("drawerPage"),
 *                     drawerElement = document.querySelector(handler.getAttribute("href")),
 *                     drawer = tau.widget.Drawer(drawerElement);
 *
 *                 page.addEventListener( "pagebeforeshow", function() {
 *                         drawer.setDragHandler(handler);
 *                         tau.event.on(handler, "mousedown touchstart", function(e) {
 *                             switch (e.type) {
 *                             case "touchstart":
 *                             case "mousedown":
 *                             // open drawer
 *                             drawer.transition(60);
 *                         }
 *                 }, false);
 *             })();
 *
 * ##Drawer state
 * Drawer has four state type.
 * - "closed" - Drawer closed state.
 * - "opened" - Drawer opened state.
 * - "sliding" - Drawer is sliding state. This state does not mean that will operate open or close.
 * - "settling" - drawer is settling state. 'Settle' means open or close status. So, this state means that drawer is animating for opened or closed state.
 *
 * ##Drawer positioning
 * You can declare to drawer position manually. (Default is left)
 *
 * If you implement data-position attributes value is 'left', drawer appear from left side.
 *
 *        @example
 *        <div class="ui-drawer" data-position="left" id="leftdrawer">
 *
 * - "left" - drawer appear from left side
 * - "right" - drawer appear from right side
 *
 * ##Drawer targeting
 * You can declare to drawer target manually. (Default is Page)
 *
 * If you implement data-drawer-target attribute value at CSS selector type, drawer widget will be appended to target.
 *
 *        @example
 *        <div class="ui-drawer" data-drawer-target="#drawerPage">
 *
 * ##Drawer enable
 * You can declare for whether drawer gesture used or not. (Default is true)
 *
 * If you implement data-enable attribute value is 'true', you can use the drawer widget.
 * This option can be changed by 'enable' or 'disable' method.
 *
 *        @example
 *        <div class="ui-drawer" data-enable="true">
 *
 * ##Drawer drag gesture start point
 * You can declare to drag gesture start point. (Default is 1)
 *
 * If you implement data-drag-edge attribute value is '0.5', you can drag gesture start in target width * 0.5 width area.
 *
 *        @example
 *        <div class="ui-drawer" data-drag-edge="1">
 *
 * @class ns.widget.wearable.Drawer
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 */
(function (document, ns) {
	
				var CoreDrawer = ns.widget.core.Drawer,
				engine = ns.engine,
				object = ns.util.object,
				Drawer = function () {
					var self = this;
					CoreDrawer.call(self);
				},
				prototype = new CoreDrawer();

			Drawer.prototype = prototype;

			/**
			 * Configure Drawer widget
			 * @method _configure
			 * @protected
			 * @param {HTMLElement} element
			 * @member ns.widget.wearable.Drawer
			 */
			prototype._configure = function() {
				var self = this;
				/**
				 * Widget options
				 * @property {number} [options.width=0] If you set width is 0, drawer width will set as the css style.
				 */
				self.options.width = 0;
			};
			/**
			 * Set Drawer drag handler.
			 * If developer use handler, drag event is bound at handler only.
			 *
			 * #####Running example in pure JavaScript:
			 *
			 * @example
			 * <!-- Drawer Handlers -->
			 * <a id="leftDrawerHandler" href="#leftDrawer" class="drawer-handler">Left Handler</a>
			 *
			 * <div id="leftDrawer" class="ui-drawer" data-drawer-target="#drawerSinglePage" data-position="left" data-enable="true" data-drag-edge="1">
			 *    <header class="ui-header">
			 *        <h2 class="ui-title">Left Drawer</h2>
			 *    </header>
			 *    <div id="leftClose" class="ui-content">
			 *        <p>Click Close</p>
			 *    </div>
			 * </div>
			 *
			 * <script>
			 *     var handler = document.getElementById("leftDrawerHandler"),
			 *         drawer = tau.widget.Drawer(document.querySelector(handler.getAttribute("href"));
			 *
			 *     drawer.setDragHandler(handler);
			 * </script>
			 *
			 * @method setDragHandler
			 * @public
			 * @param {Element} element
			 * @member ns.widget.wearable.Drawer
			 */

			/**
			 * Transition Drawer widget.
			 * This method use only positive integer number.
			 *
			 * #####Running example in pure JavaScript:
			 *
			 * @example
			 * <!-- Drawer Handlers -->
			 * <a id="leftDrawerHandler" href="#leftDrawer" class="drawer-handler">Left Handler</a>
			 *
			 * <div id="leftDrawer" class="ui-drawer" data-drawer-target="#drawerSinglePage" data-position="left" data-enable="true" data-drag-edge="1">
			 *    <header class="ui-header">
			 *        <h2 class="ui-title">Left Drawer</h2>
			 *    </header>
			 *    <div id="leftClose" class="ui-content">
			 *        <p>Click Close</p>
			 *    </div>
			 * </div>
			 *
			 * <script>
			 *     var handler = document.getElementById("leftDrawerHandler"),
			 *         drawer = tau.widget.Drawer(document.querySelector(handler.getAttribute("href"));
			 *
			 *     drawer.Transition(60);
			 * </script>
			 *
			 * @method transition
			 * @public
			 * @param {Integer} position
			 * @member ns.widget.wearable.Drawer
			 */
			/**
			 * Open Drawer widget.
			 *
			 * #####Running example in pure JavaScript:
			 *
			 * @example
			 * <!-- Drawer Handlers -->
			 * <a id="leftDrawerHandler" href="#leftDrawer" class="drawer-handler">Left Handler</a>
			 *
			 * <div id="leftDrawer" class="ui-drawer" data-drawer-target="#drawerSinglePage" data-position="left" data-enable="true" data-drag-edge="1">
			 *    <header class="ui-header">
			 *        <h2 class="ui-title">Left Drawer</h2>
			 *    </header>
			 *    <div id="leftClose" class="ui-content">
			 *        <p>Click Close</p>
			 *    </div>
			 * </div>
			 *
			 * <script>
			 *     var handler = document.getElementById("leftDrawerHandler"),
			 *         drawer = tau.widget.Drawer(document.querySelector(handler.getAttribute("href"));
			 *
			 *     drawer.open();
			 * </script>
			 *
			 * @method open
			 * @public
			 * @member ns.widget.wearable.Drawer
			 */
			/**
			 * Close Drawer widget.
			 *
			 * @example
			 * <!-- Drawer Handlers -->
			 * <a id="leftDrawerHandler" href="#leftDrawer" class="drawer-handler">Left Handler</a>
			 *
			 * <div id="leftDrawer" class="ui-drawer" data-drawer-target="#drawerSinglePage" data-position="left" data-enable="true" data-drag-edge="1">
			 *    <header class="ui-header">
			 *        <h2 class="ui-title">Left Drawer</h2>
			 *    </header>
			 *    <div id="leftClose" class="ui-content">
			 *        <p>Click Close</p>
			 *    </div>
			 * </div>
			 *
			 * <script>
			 *     var handler = document.getElementById("leftDrawerHandler"),
			 *         drawer = tau.widget.Drawer(document.querySelector(handler.getAttribute("href"));
			 *
			 *     drawer.close();
			 * </script>
			 *
			 * @method close
			 * @public
			 * @member ns.widget.wearable.Drawer
			 */
			/**
			 * Refresh Drawer widget.
			 * @method refresh
			 * @protected
			 * @member ns.widget.wearable.Drawer
			 */
			/**
			 * Get state of Drawer widget.
			 *
			 * @example
			 * <!-- Drawer Handlers -->
			 * <a id="leftDrawerHandler" href="#leftDrawer" class="drawer-handler">Left Handler</a>
			 *
			 * <div id="leftDrawer" class="ui-drawer" data-drawer-target="#drawerSinglePage" data-position="left" data-enable="true" data-drag-edge="1">
			 *    <header class="ui-header">
			 *        <h2 class="ui-title">Left Drawer</h2>
			 *    </header>
			 *    <div id="leftClose" class="ui-content">
			 *        <p>Click Close</p>
			 *    </div>
			 * </div>
			 *
			 * <script>
			 *     var handler = document.getElementById("leftDrawerHandler"),
			 *         drawer = tau.widget.Drawer(document.querySelector(handler.getAttribute("href")),
			 *         state;
			 *
			 *     state = drawer.getState();
			 * </script>
			 * @method getState
			 * @return {String} Drawer state {"closed"|"opened"|"sliding"|"settling"}
			 * @public
			 * @member ns.widget.wearable.Drawer
			 */
			ns.widget.wearable.Drawer = Drawer;
			engine.defineWidget(
				"Drawer",
				".ui-drawer",
				[
					"transition",
					"setDragHandler",
					"open",
					"close",
					"isOpen",
					"getState"
				],
				Drawer,
				"wearable"
			);

			}(window.document, ns));

/*global window, ns, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/**
 * # Circle Progress Widget
 * Shows a control that indicates the progress percentage of an on-going operation by circular shape.
 *
 * The circle progress widget shows a control that indicates the progress percentage of an on-going operation. This widget can be scaled to be fit inside a parent container.
 *
 * ### Simple progress bar
 * If you don't make any widget "circleprogress" with <progress> element, you can show default progress style.
 * To add a circular shape(page size) progressbar in your application, you have to declare <progress> tag in "ui-page" element.
 * To add a CircleProgressBar widget to the application, use the following code:
 *
 *      @example
 *	<div class="ui-page" id="pageCircleProgressBar">
 *	    <header class="ui-header"></header>
 *	    <div class="ui-content"></div>
 *          <progress class="ui-circle-progress" id="circleprogress" max="20" value="2"></progress>
 *	</div>
 *	<script>
 *		(function(){
 *
 *		    var page = document.getElementById( "pageCircleProgressBar" ),
 *		        progressBar = document.getElementById("circleprogress"),
 *		        progressBarWidget;
 *
 *		    page.addEventListener( "pageshow", function() {
 *		        var i=0;
 *		        // make Circle Progressbar object
 *		        progressBarWidget = new tau.widget.CircleProgressBar(progressBar);
 *
 *	            });
 *
 *	            page.addEventListener( "pagehide", function() {
 *		        // release object
 *		        progressBarWidget.destroy();
 *		    });
 *              }());
 *	</script>
 *
 *
 * @class ns.widget.wearable.CircleProgressBar
 * @since 2.3
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,
				utilEvent = ns.event,
				doms = ns.util.DOM,

				eventType = {
					/**
					 * Triggered when the section is changed.
					 * @event progresschange
					 * @member ns.widget.wearable.CircleProgressBar
					 */
					CHANGE: "progresschange"
				},

				CircleProgressBar = function () {
					var self = this,
						ui = {};

					ui.progressContainer = null;
					ui.progressValue = null;
					ui.progressValueLeft = null;
					ui.progressValueRight = null;
					ui.progressValueBg = null;

					self.options = {};
					self._ui = ui;

					self._maxValue = null;
					self._value = null;

				},

				prototype = new BaseWidget(),

				CLASSES_PREFIX = "ui-progressbar",

				classes = {
					uiProgressbar: CLASSES_PREFIX,
					uiProgressbarBg: CLASSES_PREFIX + "-bg",
					uiProgressbarValue: CLASSES_PREFIX + "-value",
					uiProgressbarValueLeft: CLASSES_PREFIX + "-value-left",
					uiProgressbarValueRight: CLASSES_PREFIX + "-value-right",
					uiProgressbarHalf: CLASSES_PREFIX + "-half"
				},

				selectors = {
					progressContainer: "." + classes.uiProgressbar,
					progressBg: "." + classes.uiProgressbarBg,
					progressValue: "." + classes.uiProgressbarValue,
					progressValueLeft: "." + classes.uiProgressbarValueLeft,
					progressValueRight: "." + classes.uiProgressbarValueRight
				},

				size = {
					FULL: "full",
					LARGE: "large",
					MEDIUM: "medium",
					SMALL: "small"
				};

			CircleProgressBar.classes = classes;

			/* make widget refresh with new value */
			function refreshProgressBar (self, value) {
				var percentValue = value / self._maxValue * 100,
					rotateValue,
					ui = self._ui;

				if (percentValue >= 50) {
					ui.progressValue.classList.add(classes.uiProgressbarHalf);
				} else {
					ui.progressValue.classList.remove(classes.uiProgressbarHalf);
				}

				rotateValue = 360 * (percentValue/100);
				ui.progressValueLeft.style.webkitTransform = "rotate(" + rotateValue + "deg)";
			}

			function setThicknessStyle (self, value) {
				var ui = self._ui;

				ui.progressValueLeft.style.borderWidth = value +"px";
				ui.progressValueRight.style.borderWidth = value +"px";
				ui.progressValueBg.style.borderWidth = value +"px";
			}

			function setProgressBarSize (self, progressSize) {
				var sizeToNumber = parseFloat(progressSize),
					ui = self._ui;

				if (!isNaN(sizeToNumber)) {
					ui.progressContainer.style.fontSize = progressSize + "px";
					ui.progressContainer.style.width = progressSize + "px";
					ui.progressContainer.style.height = progressSize + "px";
				} else {
					switch(progressSize) {
						case size.FULL:
						case size.LARGE:
						case size.MEDIUM:
						case size.SMALL:
							ui.progressContainer.classList.add(CLASSES_PREFIX + "-" + progressSize);
							break;
					}
					ui.progressContainer.style.fontSize = doms.getCSSProperty(ui.progressContainer, "width", 0, "float") + "px";
				}
			}

			function checkOptions (self, option) {
				if (option.thickness) {
					setThicknessStyle(self, option.thickness);
				}

				if (option.size) {
					setProgressBarSize(self, option.size);
				}

				if (option.containerClassName) {
					self._ui.progressContainer.classList.add(option.containerClassName);
				}
			}

			prototype._configure = function () {
				/**
				 * Options for widget
				 * @property {Object} options Options for widget
				 * @property {number} [options.thickness=null] Sets the border width of CircleProgressBar.
				 * @property {number|"full"|"large"|"medium"|"small"} [options.size="full"] Sets the size of CircleProgressBar.
				 * @property {string} [options.containerClassName=null] Sets the class name of CircleProgressBar container.
				 * @member ns.widget.wearable.CircleProgressBar
				 */
				this.options = {
					thickness: null,
					size: size.MEDIUM,
					containerClassName: null
				};
			};
			/**
			 * Build CircleProgressBar
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.wearable.CircleProgressBar
			 */
			prototype._build = function (element) {
				var self = this,
					ui = self._ui,
					progressElement = element,
					progressbarContainer, progressbarBg, progressbarValue, progressbarValueLeft, progressbarValueRight;

				ui.progressContainer = progressbarContainer = document.createElement("div"),
				ui.progressValueBg = progressbarBg = document.createElement("div"),
				ui.progressValue = progressbarValue = document.createElement("div"),
				ui.progressValueLeft = progressbarValueLeft = document.createElement("div"),
				ui.progressValueRight = progressbarValueRight = document.createElement("div");

				// set classNames of progressbar DOMs.
				progressbarContainer.className = classes.uiProgressbar;
				progressbarBg.className = classes.uiProgressbarBg;
				progressbarValue.className = classes.uiProgressbarValue;
				progressbarValueLeft.className = classes.uiProgressbarValueLeft;
				progressbarValueRight.className = classes.uiProgressbarValueRight;

				// set id for progress container using "container" prefix
				progressbarContainer.id = progressElement.id? progressElement.id + "-container" : "";

				progressbarValue.appendChild(progressbarValueLeft);
				progressbarValue.appendChild(progressbarValueRight);
				progressbarContainer.appendChild(progressbarValue);
				progressbarContainer.appendChild(progressbarBg);
				progressElement.parentNode.appendChild(progressbarContainer);
				progressElement.parentNode.insertBefore(progressElement, progressbarContainer);

				return element;
			};
			/**
			 * Init CircleProgressBar
			 * @method _init
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.wearable.CircleProgressBar
			 */
			prototype._init = function (element) {
				var self = this,
					ui = self._ui,
					progressElement = element,
					elementParent = element.parentNode,
					options = self.options;

				ui.progressContainer = ui.progressContainer || elementParent.querySelector(selectors.progressContainer);
				ui.progressValueBg = ui.progressValueBg || elementParent.querySelector(selectors.progressValueBg);
				ui.progressValue = ui.progressValue || elementParent.querySelector(selectors.progressValue);
				ui.progressValueLeft = ui.progressValueLeft || elementParent.querySelector(selectors.progressValueLeft);
				ui.progressValueRight = ui.progressValueRight || elementParent.querySelector(selectors.progressValueRight);

				self._maxValue = doms.getNumberFromAttribute(progressElement, "max", null, 100);

				// max value must be positive number bigger than 0
				if (self._maxValue <= 0) {
					ns.error("max value of progress must be positive number that bigger than zero!");
					self._maxValue = 100;
				}

				self._value = doms.getNumberFromAttribute(progressElement, "value", null, 50);

				checkOptions(self, options);
				refreshProgressBar(self, self._value);

				return element;
			};

			/**
			 * Get or Set value of the widget
			 *
			 * Return element value or set the value
			 *
			 *		@example
			 * 		<progress class="ui-circle-progress" id="circleprogress" max="20" value="2"></progress>
			 *		<script>
			 *			var progressbar = document.getElementById("circleprogress"),
							progressbarWidget = tau.widget.CircleProgressBar(progressbar),
			 *			// return value in progress tag
			 * 			value = progressbarWidget.value();
			 *			// sets the value for the progress
			 *			progressbarWidget.value("15");
			 *		</script>
			 * @method value
			 * return {string} In get mode return element value
			 * @since 2.3
			 * @member ns.widget.wearable.CircleProgressBar
			 */

			/**
			 * Get value of Circle Progressbar
			 * @method _getValue
			 * @protected
			 * @memeber ns.widget.wearable.CircleProgressBar
			 */
			prototype._getValue = function () {
				return this.element.getAttribute("value");
			};
			/**
			 * Set value of Circle Progressbar
			 * @method _setValue
			 * @param {string} value
			 * @protected
			 * @member ns.widget.wearable.CircleProgressBar
			 */
			prototype._setValue = function (inputValue) {
				var self = this,
					value,
					selfElementValue;

				if (inputValue > self._maxValue) {
					value = self._maxValue;
 				} else if (inputValue < 0) {
					value = 0;
				} else if (isNaN(inputValue)) {
					value = 0;
				} else {
					value = inputValue;
				}

				doms.setAttribute(self.element, "value", value);

				if (self._value !== value) {
					self._value = value;
					utilEvent.trigger(self.element, eventType.CHANGE);
					refreshProgressBar(self, value);
				}
			};

			/**
			 * Refresh structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.wearable.CircleProgressBar
			 */
			prototype._refresh = function () {
				var self = this;

				self._reset();
				checkOptions(self, self.options);
				refreshProgressBar(self, self._getValue());
				return null;
			};

			/**
			 * Reset style of Value elements
			 * @method _reset
			 * @protected
			 * @member ns.widget.wearable.CircleProgressBar
			 */
			prototype._reset = function () {
				var self = this,
					ui = self._ui;

				ui.progressValue.classList.remove(classes.uiProgressbarHalf);
				ui.progressValueLeft.style.webkitTransform = "";
				if (self.options.thickness) {
					ui.progressValueLeft.style.borderWidth = "";
					ui.progressValueRight.style.borderWidth = "";
					ui.progressValueBg.style.borderWidth = "";
				}
			};

			/**
			 * Destroy widget
			 * @method _destroy
			 * @protected
			 * @member ns.widget.wearable.CircleProgressBar
			 */
			prototype._destroy = function () {
				var self = this;

				self._reset();

				// remove doms
				self.element.parentNode.removeChild(self._ui.progressContainer);

				// clear variables
				self.element = null;
				self._ui = null;
				self._maxValue = null;
				self._value = null;

				return null;
			};

			CircleProgressBar.prototype = prototype;
			ns.widget.wearable.CircleProgressBar = CircleProgressBar;

			engine.defineWidget(
				"CircleProgressBar",
				".ui-circle-progress",
				[],
				CircleProgressBar,
				"wearable"
			);
			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/**
 * # Listview Widget
 * Shows a list view.
 *
 * The list widget is used to display, for example, navigation data, results, and data entries. The following table describes the supported list classes.
 *
 * ## Default selectors
 *
 * Default selector for listview widget is class *ui-listview*.
 *
 * To add a list widget to the application, use the following code:
 *
 * ### List with basic items
 *
 * You can add a basic list widget as follows:
 *
 *      @example
 *         <ul class="ui-listview">
 *             <li>1line</li>
 *             <li>2line</li>
 *             <li>3line</li>
 *             <li>4line</li>
 *             <li>5line</li>
 *         </ul>
 *
 * ### List with link items
 *
 * You can add a list widget with a link and press effect that allows the user to click each list item as follows:
 *
 *      @example
 *         <ul class="ui-listview">
 *             <li>
 *                 <a href="#">1line</a>
 *             </li>
 *             <li>
 *                 <a href="#">2line</a>
 *             </li>
 *             <li>
 *                 <a href="#">3line</a>
 *             </li>
 *             <li>
 *                 <a href="#">4line</a>
 *             </li>
 *             <li>
 *                 <a href="#">5line</a>
 *             </li>
 *         </ul>
 *
 * ## JavaScript API
 *
 * Listview widget hasn't JavaScript API.
 *
 * @class ns.widget.wearable.Listview
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,
				Listview = function () {
				},
				prototype = new BaseWidget();

			/**
			 * Dictionary for listview related events.
			 * For listview, it is an empty object.
			 * @property {Object} events
			 * @member ns.widget.wearable.Listview
			 * @static
			 */
			Listview.events = {};

			/**
			 * Build Listview
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.wearable.Listview
			 */
			prototype._build = function (element) {
				return element;
			};

			prototype._init = function (element) {
				return element;
			};

			prototype._bindEvents = function (element) {
				return element;
			};

			/**
			 * Refresh structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.wearable.Listview
			 */
			prototype._refresh = function () {
				return null;
			};

			/**
			 * Destroy widget
			 * @method _destroy
			 * @protected
			 * @member ns.widget.wearable.Listview
			 */
			prototype._destroy = function () {
				return null;
			};

			Listview.prototype = prototype;
			ns.widget.wearable.Listview = Listview;

			engine.defineWidget(
				"Listview",
				".ui-listview",
				[],
				Listview,
				"wearable"
			);
			}(window.document, ns));

/*global CustomEvent, define, window, ns */
/*jslint plusplus: true, nomen: true, bitwise: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Virtual Mouse Events
 * Reimplementation of jQuery Mobile virtual mouse events.
 *
 * ##Purpose
 * It will let for users to register callbacks to the standard events like bellow,
 * without knowing if device support touch or mouse events
 * @class ns.event.vmouse
 */
/**
 * Triggered after mouse-down or touch-started.
 * @event vmousedown
 * @member ns.event.vmouse
 */
/**
 * Triggered when mouse-click or touch-end when touch-move didn't occur
 * @event vclick
 * @member ns.event.vmouse
 */
/**
 * Triggered when mouse-up or touch-end
 * @event vmouseup
 * @member ns.event.vmouse
 */
/**
 * Triggered when mouse-move or touch-move
 * @event vmousemove
 * @member ns.event.vmouse
 */
/**
 * Triggered when mouse-over or touch-start if went over coordinates
 * @event vmouseover
 * @member ns.event.vmouse
 */
/**
 * Triggered when mouse-out or touch-end
 * @event vmouseout
 * @member ns.event.vmouse
 */
/**
 * Triggered when mouse-cancel or touch-cancel and when scroll occur during touchmove
 * @event vmousecancel
 * @member ns.event.vmouse
 */
(function (window, document, ns) {
	
				/**
			 * Object with default options
			 * @property {Object} vmouse
			 * @member ns.event.vmouse
			 * @static
			 * @private
			 **/
			var vmouse,
				/**
				 * @property {Object} eventProps Contains the properties which are copied from the original event to custom v-events
				 * @member ns.event.vmouse
				 * @static
				 * @private
				 **/
				eventProps,
				/**
				 * Indicates if the browser support touch events
				 * @property {boolean} touchSupport
				 * @member ns.event.vmouse
				 * @static
				 **/
				touchSupport = window.hasOwnProperty("ontouchstart"),
				/**
				 * @property {boolean} didScroll The flag tell us if the scroll event was triggered
				 * @member ns.event.vmouse
				 * @static
				 * @private
				 **/
				didScroll,
				/** @property {HTMLElement} lastOver holds reference to last element that touch was over
				 * @member ns.event.vmouse
				 * @private
				 */
				lastOver = null,
				/**
				 * @property {Number} [startX=0] Initial data for touchstart event
				 * @member ns.event.vmouse
				 * @static
				 * @private
				 **/
				startX = 0,
				/**
				 * @property {Number} [startY=0] Initial data for touchstart event
				 * @member ns.event.vmouse
				 * @private
				 * @static
				 **/
				startY = 0,
				touchEventProps = ["clientX", "clientY", "pageX", "pageY", "screenX", "screenY"],
				KEY_CODES = {
					enter: 13
				};

			/**
			 * Extends objects with other objects
			 * @method copyProps
			 * @param {Object} from Sets the original event
			 * @param {Object} to Sets the new event
			 * @param {Object} properties Sets the special properties for position
			 * @param {Object} propertiesNames Describe parameters which will be copied from Original to To event
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function copyProps(from, to, properties, propertiesNames) {
				var i,
					length,
					descriptor,
					property;

				for (i = 0, length = propertiesNames.length; i < length; ++i) {
					property = propertiesNames[i];
					if (property !== "detail" && (isNaN(properties[property]) === false || isNaN(from[property]) === false)) {
						descriptor = Object.getOwnPropertyDescriptor(to, property);
						if (!descriptor || descriptor.writable) {
							to[property] = properties[property] || from[property];
						}
					}
				}
			}

			/**
			 * Create custom event
			 * @method createEvent
			 * @param {string} newType gives a name for the new Type of event
			 * @param {Event} original Event which trigger the new event
			 * @param {Object} properties Sets the special properties for position
			 * @return {Event}
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function createEvent(newType, original, properties) {
				var evt = new CustomEvent(newType, {
						"bubbles": original.bubbles,
						"cancelable": original.cancelable,
						"detail": original.detail
					}),
					orginalType = original.type,
					changeTouches,
					touch,
					j = 0,
					len,
					prop;

				copyProps(original, evt, properties, eventProps);
				evt._originalEvent = original;

				if (orginalType.indexOf("touch") !== -1) {
					orginalType = original.touches;
					changeTouches = original.changedTouches;

					if (orginalType && orginalType.length) {
						touch = orginalType[0];
					} else {
						touch = (changeTouches && changeTouches.length) ? changeTouches[0] : null;
					}

					if (touch) {
						for (len = touchEventProps.length; j < len; j++) {
							prop = touchEventProps[j];
							evt[prop] = touch[prop];
						}
					}
				}

				return evt;
			}

			/**
			 * Dispatch Events
			 * @method fireEvent
			 * @param {string} eventName event name
			 * @param {Event} evt original event
			 * @param {Object} [properties] Sets the special properties for position
			 * @return {boolean}
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function fireEvent(eventName, evt, properties) {
				return evt.target.dispatchEvent(createEvent(eventName, evt, properties || {}));
			}

			eventProps = [
				"currentTarget",
				"detail",
				"button",
				"buttons",
				"clientX",
				"clientY",
				"offsetX",
				"offsetY",
				"pageX",
				"pageY",
				"screenX",
				"screenY",
				"toElement",
				"which"
			];

			vmouse = {
				/**
				 * Sets the distance of pixels after which the scroll event will be successful
				 * @property {number} [eventDistanceThreshold=10]
				 * @member ns.event.vmouse
				 * @static
				 */
				eventDistanceThreshold: 10,

				touchSupport: touchSupport
			};

			/**
			 * Handle click down
			 * @method handleDown
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleDown(evt) {
				fireEvent("vmousedown", evt);
			}

			/**
			 * Prepare position of event for keyboard events.
			 * @method preparePositionForClick
			 * @param {Event} event
			 * @return {?Object} options
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function preparePositionForClick(event) {
				var x = event.clientX,
					y = event.clientY;
				// event comes from keyboard
				if (!x && !y) {
					return preparePositionForEvent(event);
				}
			}

			/**
			 * Handle click
			 * @method handleClick
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleClick(evt) {
				fireEvent("vclick", evt, preparePositionForClick(evt));
			}

			/**
			 * Handle click up
			 * @method handleUp
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleUp(evt) {
				fireEvent("vmouseup", evt);
			}

			/**
			 * Handle click move
			 * @method handleMove
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleMove(evt) {
				fireEvent("vmousemove", evt);
			}

			/**
			 * Handle click over
			 * @method handleOver
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleOver(evt) {
				fireEvent("vmouseover", evt);
			}

			/**
			 * Handle click out
			 * @method handleOut
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleOut(evt) {
				fireEvent("vmouseout", evt);
			}

			/**
			 * Handle touch start
			 * @method handleTouchStart
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleTouchStart(evt) {
				var touches = evt.touches,
					firstTouch,
					over;
				//if touches are registered and we have only one touch
				if (touches && touches.length === 1) {
					didScroll = false;
					firstTouch = touches[0];
					startX = firstTouch.pageX;
					startY = firstTouch.pageY;

					// Check if we have touched something on our page
					// @TODO refactor for multi touch
					over = document.elementFromPoint(startX, startY);
					if (over) {
						lastOver = over;
						fireEvent("vmouseover", evt);
					}
					fireEvent("vmousedown", evt);
				}

			}

			/**
			 * Handle touch end
			 * @method handleTouchEnd
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleTouchEnd(evt) {
				var touches = evt.touches;
				if (touches && touches.length === 0) {
					fireEvent("vmouseup", evt);
					fireEvent("vmouseout", evt);
					// Reset flag for last over element
					lastOver = null;
				}
			}

			/**
			 * Handle touch move
			 * @method handleTouchMove
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleTouchMove(evt) {
				var over,
					firstTouch = evt.touches && evt.touches[0],
					didCancel = didScroll,
				//sets the threshold, based on which we consider if it was the touch-move event
					moveThreshold = vmouse.eventDistanceThreshold;

				/**
				 * Ignore the touch which has identifier other than 0.
				 * Only first touch has control others are ignored.
				 * Patch for webkit behaviour where touchmove event
				 * is triggered between touchend events
				 * if there is multi touch.
				 */
				if (firstTouch.identifier > 0) {
					evt.preventDefault();
					evt.stopPropagation();
					return;
				}

				didScroll = didScroll ||
					//check in both axes X,Y if the touch-move event occur
					(Math.abs(firstTouch.pageX - startX) > moveThreshold ||
						Math.abs(firstTouch.pageY - startY) > moveThreshold);

				// detect over event
				// for compatibility with mouseover because "touchenter" fires only once
				// @TODO Handle many touches
				over = document.elementFromPoint(firstTouch.pageX, firstTouch.pageY);
				if (over && lastOver !== over) {
					lastOver = over;
					fireEvent("vmouseover", evt);
				}

				//if didscroll occur and wasn't canceled then trigger touchend otherwise just touchmove
				if (didScroll && !didCancel) {
					fireEvent("vmousecancel", evt);
					lastOver = null;
				}
				fireEvent("vmousemove", evt);
			}

			/**
			 * Handle Scroll
			 * @method handleScroll
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleScroll(evt) {
				if (!didScroll) {
					fireEvent("vmousecancel", evt);
				}
				didScroll = true;
			}

			/**
			 * Handle touch cancel
			 * @method handleTouchCancel
			 * @param {Event} evt
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleTouchCancel(evt) {
				fireEvent("vmousecancel", evt);
				lastOver = null;
			}

			/**
			 * Prepare position of event for keyboard events.
			 * @method preparePositionForEvent
			 * @param {Event} event
			 * @return {Object} properties
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function preparePositionForEvent(event) {
				var targetRect = event.target && event.target.getBoundingClientRect(),
					properties = {};
				if (targetRect) {
					properties = {
						"clientX": targetRect.left + targetRect.width / 2,
						"clientY": targetRect.top + targetRect.height / 2,
						"which": 1
					};
				}
				return properties;
			}

			/**
			 * Handle key up
			 * @method handleKeyUp
			 * @param {Event} event
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleKeyUp(event) {
				var properties;
				if (event.keyCode === KEY_CODES.enter) {
					properties = preparePositionForEvent(event);
					fireEvent("vmouseup", event, properties);
					fireEvent("vclick", event, properties);
				}
			}

			/**
			 * Handle key down
			 * @method handleKeyDown
			 * @param {Event} event
			 * @private
			 * @static
			 * @member ns.event.vmouse
			 */
			function handleKeyDown(event) {
				if (event.keyCode === KEY_CODES.enter) {
					fireEvent("vmousedown", event, preparePositionForEvent(event));
				}
			}

			/**
			 * Binds events common to mouse and touch to support virtual mouse.
			 * @method bindCommonEvents
			 * @static
			 * @member ns.event.vmouse
			 */
			vmouse.bindCommonEvents = function () {
				document.addEventListener("keyup", handleKeyUp, true);
				document.addEventListener("keydown", handleKeyDown, true);
				document.addEventListener("scroll", handleScroll, true);
				document.addEventListener("click", handleClick, true);
			};

			// @TODO delete touchSupport flag and attach touch and mouse listeners,
			// @TODO check if v-events are not duplicated if so then called only once

			/**
			 * Binds touch events to support virtual mouse.
			 * @method bindTouch
			 * @static
			 * @member ns.event.vmouse
			 */
			vmouse.bindTouch = function () {
				document.addEventListener("touchstart", handleTouchStart, true);
				document.addEventListener("touchend", handleTouchEnd, true);
				document.addEventListener("touchmove", handleTouchMove, true);
				document.addEventListener("touchcancel", handleTouchCancel, true);

				// touchenter and touchleave are removed from W3C spec
				// No need to listen to touchover as it has never exited
				// document.addEventListener("touchenter", handleTouchOver, true);
				// document.addEventListener("touchleave", callbacks.out, true);
				document.addEventListener("touchcancel", handleTouchCancel, true);
			};

			/**
			 * Binds mouse events to support virtual mouse.
			 * @method bindMouse
			 * @static
			 * @member ns.event.vmouse
			 */
			vmouse.bindMouse = function () {
				document.addEventListener("mousedown", handleDown, true);

				document.addEventListener("mouseup", handleUp, true);
				document.addEventListener("mousemove", handleMove, true);
				document.addEventListener("mouseover", handleOver, true);
				document.addEventListener("mouseout", handleOut, true);
			};

			ns.event.vmouse = vmouse;

			if (touchSupport) {
				vmouse.bindTouch();
			} else {
				vmouse.bindMouse();
			}
			vmouse.bindCommonEvents();

			}(window, window.document, ns));
/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 */
(function (ns) {
	
				/** @namespace ns.widget.wearable */
			ns.widget.core.indexscrollbar = ns.widget.core.indexscrollbar || {};
			}(ns));

/*global define, ns, document, window */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * #IndexBar widget
 * Widget creates bar with index.
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Jadwiga Sosnowska <j.sosnowska@samsung.com>
 * @class ns.widget.wearable.indexscrollbar.IndexBar
 */
(function (document, ns) {
	
				var utilsObject = ns.util.object,
				utilsDOM = ns.util.DOM;

			function IndexBar(element, options) {
				this.element = element;
				this.options = utilsObject.merge(options, this._options, false);
				this.container = this.options.container;

				this.indices = {
					original: this.options.index,
					merged: []
				};

				this._init();

				return this;
			}
			IndexBar.prototype = {
				_options: {
					container: null,
					offsetLeft: 0,
					index: [],
					verticalCenter: false,
					moreChar: "*",
					indexHeight: 41,
					selectedClass: "ui-state-selected",
					ulClass: null,
					maxIndexLen : 0
				},
				_init: function() {
					this.indices.original = this.options.index;
					this.indexLookupTable = [];
					this.indexElements = null;
					this.selectedIndex = -1;
					this.visiblity = "hidden";

					this._setMaxIndexLen();
					this._makeMergedIndices();
					this._drawDOM();
					this._appendToContainer();
					if(this.options.verticalCenter) {
						this._adjustVerticalCenter();
					}
					this._setIndexCellInfo();
				},

				_clear: function() {
					while(this.element.firstChild) {
						this.element.removeChild(this.element.firstChild);
					}

					this.indices.merged.length = 0;
					this.indexLookupTable.length = 0;
					this.indexElements = null;
					this.selectedIndex = -1;
					this.visiblity = null;
				},

				/**
				 * Refreshes widget.
				 * @method refresh
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				refresh: function() {
					this._clear();
					this._init();
				},

				/**
				 * Destroys widget.
				 * @method destroy
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				destroy: function() {
					this._clear();
				},

				/**
				 * Shows widget.
				 * @method show
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				show: function() {
					this.visibility = "visible";
					this.element.style.visibility = this.visibility;
				},

				/**
				 * Hides widget.
				 * @method hide
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				hide: function() {
					this.visibility = "hidden";
					this.element.style.visibility = this.visibility;
				},

				/**
				 * Get if the visibility status is shown or not
				 * @method isShown
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				isShown: function() {
					return "visible" === this.visibility;
				},

				_setMaxIndexLen: function() {
					var maxIndexLen,
						containerHeight = this.container.offsetHeight;
					maxIndexLen = Math.floor( containerHeight / this.options.indexHeight );
					if(maxIndexLen > 0 && maxIndexLen%2 === 0) {
						maxIndexLen -= 1;	// Ensure odd number
					}
					this.options.maxIndexLen = this.options.maxIndexLen > 0 ? Math.min(maxIndexLen, this.options.maxIndexLen) : maxIndexLen;
				},

				_makeMergedIndices: function() {
					var origIndices = this.indices.original,
						origIndexLen = origIndices.length,
						visibleIndexLen = Math.min(this.options.maxIndexLen, origIndexLen),
						totalLeft = origIndexLen - visibleIndexLen,
						nIndexPerItem = parseInt(totalLeft / parseInt(visibleIndexLen/2, 10), 10),
						leftItems = totalLeft % parseInt(visibleIndexLen/2, 10),
						indexItemSize = [],
						mergedIndices = [],
						i, len, position=0;

					for(i = 0, len = visibleIndexLen; i < len; i++) {
						indexItemSize[i] = 1;
						if (i % 2) {	// even number: omitter
							indexItemSize[i] += nIndexPerItem + (leftItems-- > 0 ? 1 : 0);
						}
						position +=  indexItemSize[i];
						mergedIndices.push( {
							start: position-1,
							length: indexItemSize[i]
						});
					}
					this.indices.merged = mergedIndices;
				},

				_drawDOM: function() {
					var origIndices = this.indices.original,
						indices = this.indices.merged,
						indexLen = indices.length,
					//container = this.container,
					//containerHeight = container.offsetHeight,
						indexHeight = this.options.indexHeight,
					//maxIndexLen = Math.min(this.maxIndexLen, indices.length),
						moreChar = this.options.moreChar,
						addMoreCharLineHeight = 9,
						text,
						frag,
						li,
						i,
						m;

					frag = document.createDocumentFragment();
					for(i=0; i < indexLen; i++) {
						m = indices[i];
						text = m.length === 1 ? origIndices[m.start] : moreChar;
						li = document.createElement("li");
						li.innerText = text.toUpperCase();
						li.style.height = indexHeight + "px";
						li.style.lineHeight = text === moreChar ? indexHeight + addMoreCharLineHeight + "px" : indexHeight + "px";
						frag.appendChild(li);
					}
					this.element.appendChild(frag);

					if(this.options.ulClass) {
						this.element.classList.add( this.options.ulClass );
					}
				},

				_adjustVerticalCenter: function() {
					var nItem = this.indices.merged.length,
						totalIndexLen = nItem * this.options.indexHeight,
						vPadding = parseInt((this.container.offsetHeight - totalIndexLen) / 2, 10);
					this.element.style.paddingTop = vPadding + "px";
				},

				_appendToContainer: function() {
					this.container.appendChild(this.element);
					this.element.style.left = this.options.offsetLeft + "px";
				},

				/**
				 * Sets padding top for element.
				 * @method setPaddingTop
				 * @param {number} paddingTop
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				setPaddingTop: function(paddingTop) {
					var height = this.element.clientHeight,
						oldPaddingTop = this.element.style.paddingTop,
						containerHeight = this.container.clientHeight;

					if(oldPaddingTop === "") {
						oldPaddingTop = 0;
					} else {
						oldPaddingTop = parseInt(oldPaddingTop, 10);
					}

					height = height - oldPaddingTop;

					if(height > containerHeight) {
						paddingTop -= (paddingTop + height - containerHeight);
					}
					this.element.style.paddingTop = paddingTop + "px";

					this._setIndexCellInfo();	// update index cell info
				},

				/**
				 * Returns element's offsetTop of given index.
				 * @method getOffsetTopByIndex
				 * @param {number} index
				 * @return {number}
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				getOffsetTopByIndex: function(index) {
					var cellIndex = this.indexLookupTable[index].cellIndex,
						el = this.indexElements[cellIndex],
						offsetTop = el.offsetTop;

					return offsetTop;
				},

				_setIndexCellInfo: function() {
					var element = this.element,
						mergedIndices = this.indices.merged,
						containerOffsetTop = utilsDOM.getElementOffset(this.container).top,
						listitems = this.element.querySelectorAll("LI"),
						lookupTable = [];

					[].forEach.call(listitems, function(node, idx) {
						var m = mergedIndices[idx],
							i = m.start,
							len = i + m.length,
							top = containerOffsetTop + node.offsetTop,
							height = node.offsetHeight / m.length;

						for ( ; i < len; i++ ) {
							lookupTable.push({
								cellIndex: idx,
								top: top,
								range: height
							});
							top += height;
						}
					});
					this.indexLookupTable = lookupTable;
					this.indexElements = element.children;
				},

				/**
				 * Returns index for given position.
				 * @method getIndexByPosition
				 * @param {number} posY
				 * @return {number}
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				getIndexByPosition: function(posY) {
					var table = this.indexLookupTable,
						info,
						i, len, range;

					// boundary check
					if( table[0] ) {
						info = table[0];
						if(posY < info.top) {
							return 0;
						}
					}
					if( table[table.length -1] ) {
						info = table[table.length -1];
						if(posY >= info.top + info.range) {
							return table.length - 1;
						}
					}
					for ( i=0, len=table.length; i < len; i++) {
						info = table[i];
						range = posY - info.top;
						if ( range >= 0 && range < info.range ) {
							return i;
						}
					}
					return 0;
				},

				/**
				 * Returns value for given index.
				 * @method getValueByIndex
				 * @param {number} idx
				 * @return {number}
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				getValueByIndex: function(idx) {
					if(idx < 0) { idx = 0; }
					return this.indices.original[idx];
				},

				/**
				 * Select given index
				 * @method select
				 * @param {number} idx
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				select: function(idx) {
					var cellIndex,
						eCell;

					this.clearSelected();

					if(this.selectedIndex === idx) {
						return;
					}
					this.selectedIndex = idx;

					cellIndex = this.indexLookupTable[idx].cellIndex;
					eCell = this.indexElements[cellIndex];
					eCell.classList.add(this.options.selectedClass);
				},

				/**
				 * Clears selected class.
				 * @method clearSelected
				 * @member ns.widget.wearable.indexscrollbar.IndexBar
				 */
				clearSelected: function() {
					var el = this.element,
						selectedClass = this.options.selectedClass,
						selectedElement = el.querySelectorAll("."+selectedClass);

					[].forEach.call(selectedElement, function(node) {
						node.classList.remove(selectedClass);
					});
					this.selectedIndex = -1;
				}
			};

			ns.widget.core.indexscrollbar.IndexBar = IndexBar;

			}(window.document, ns));

/*global define, ns, document, window */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * #IndexIndicator widget
 * Class creates index indicator.
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Jadwiga Sosnowska <j.sosnowska@samsung.com>
 * @class ns.widget.wearable.indexscrollbar.IndexIndicator
 */
(function (document, ns) {
	
				var utilsObject = ns.util.object,
				events = ns.event;

			/**
			 * @brief block 'unexpected bouncing effect' on indexscroller indicator.
			 */
			function blockEvent (event) {
				event.preventDefault();
				event.stopPropagation();
			}

			function IndexIndicator(element, options) {
				this.element = element;
				this.options = utilsObject.merge(options, this._options, false);
				this.value = null;

				this._init();

				return this;
			}

			IndexIndicator.prototype = {
				_options: {
					className: "ui-indexscrollbar-indicator",
					selectedClass: "ui-selected",
					container: null
				},
				_init: function() {
					var self = this,
						options = self.options,
						element = self.element;
					element.className = options.className;
					element.innerHTML = "<span></span>";
					events.on(element, ["touchstart", "touchmove"], blockEvent, false);


					// Add to DOM tree
					options.referenceElement.parentNode.insertBefore(element, options.referenceElement);
					self.fitToContainer();
				},

				/**
				 * Fits size to container.
				 * @method fitToContainer
				 * @member ns.widget.wearable.indexscrollbar.IndexIndicator
				 */
				fitToContainer: function() {
					var element = this.element,
						container = this.options.container,
						containerPosition = window.getComputedStyle(container).position;

					element.style.width = container.offsetWidth + "px";
					element.style.height = container.offsetHeight + "px";

					if ( containerPosition !== "absolute" && containerPosition !== "relative" ) {
						element.style.top = container.offsetTop + "px";
						element.style.left = container.offsetLeft + "px";
					}
				},

				/**
				 * Sets value of widget.
				 * @method setValue
				 * @param {string} value
				 * @member ns.widget.wearable.indexscrollbar.IndexIndicator
				 */
				setValue: function( value ) {
					this.value = value;	// remember value
					value = value.toUpperCase();

					var selected = value.substr(value.length - 1),
						remained = value.substr(0, value.length - 1),
						inner = "<span>" + remained + "</span><span class=\"ui-selected\">" + selected + "</span>";
					this.element.firstChild.innerHTML = inner;	// Set indicator text
				},

				/**
				 * Shows widget.
				 * @method show
				 * @member ns.widget.wearable.indexscrollbar.IndexIndicator
				 */
				show: function() {
					//this.element.style.visibility="visible";
					this.element.style.display="block";
				},

				/**
				 * Hides widget.
				 * @method hide
				 * @member ns.widget.wearable.indexscrollbar.IndexIndicator
				 */
				hide: function() {
					this.element.style.display="none";
				},

				/**
				 * Destroys widget.
				 * @method destroy
				 * @member ns.widget.wearable.indexscrollbar.IndexIndicator
				 */
				destroy: function() {
					var element = this.element;

					while(element.firstChild) {
						element.removeChild(element.firstChild);
					}
					events.off(element, ["touchstart", "touchmove"], blockEvent, false);
					this.element = null;	// unreference element

				}
			};
			ns.widget.core.indexscrollbar.IndexIndicator = IndexIndicator;
			}(window.document, ns));

/*global define, ns, document, window */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * #IndexScrollbar Widget
 * Shows an index scroll bar with indices, usually for the list.
 *
 * The index scroll bar widget shows on the screen a scrollbar with indices,
 * and fires a select event when the index characters are clicked.
 * The following table describes the supported index scroll bar APIs.
 *
 * ## Manual constructor
 * For manual creation of widget you can use constructor of widget from **tau** namespace:
 *
 *		@example
 *		var indexscrollbarElement = document.getElementById('indexscrollbar'),
 *			indexscrollbar = tau.widget.IndexScrollbar(IndexScrollbar, {index: "A,B,C"});
 *
 * Constructor has one require parameter **element** which are base **HTMLElement** to create widget.
 * We recommend get this element by method *document.getElementById*. Second parameter is **options**
 * and it is a object with options for widget.
 *
 * To add an IndexScrollbar widget to the application, use the following code:
 *
 *      @example
 *      <div id="foo" class="ui-indexscrollbar" data-index="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z"></div>
 *      <script>
 *          (function() {
 *              var elem = document.getElementById("foo");
 *              tau.widget.IndexScrollbar(elem);
 *              elem.addEventListener("select", function( event ) {
 *                  var index = event.detail.index;
 *                  console.log(index);
 *              });
 *          }());
 *      </script>
 *
 * The index value can be retrieved by accessing event.detail.index property.
 *
 * In the following example, the list scrolls to the position of the list item defined using
 * the li-divider class, selected by the index scroll bar:
 *
 *      @example
 *         <div id="pageIndexScrollbar" class="ui-page">
 *             <header class="ui-header">
 *                 <h2 class="ui-title">IndexScrollbar</h2>
 *             </header>
 *             <section class="ui-content">
 *                 <div style="overflow-y:scroll;">
 *                     <div id="indexscrollbar1"
 *                          class="ui-indexscrollbar"
 *                          data-index="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z">
 *                     </div>
 *                     <ul class="ui-listview" id="list1">
 *                         <li class="li-divider">A</li>
 *                         <li>Anton</li>
 *                         <li>Arabella</li>
 *                         <li>Art</li>
 *                         <li class="li-divider">B</li>
 *                         <li>Barry</li>
 *                         <li>Bibi</li>
 *                         <li>Billy</li>
 *                         <li>Bob</li>
 *                         <li class="li-divider">D</li>
 *                         <li>Daisy</li>
 *                         <li>Derek</li>
 *                         <li>Desmond</li>
 *                     </ul>
 *                 </div>
 *             </section>
 *             <script>
 *                 (function () {
 *                     var page = document.getElementById("pageIndexScrollbar");
 *                     page.addEventListener("pagecreate", function () {
 *                         var elem = document.getElementById("indexscrollbar1"), // Index scroll bar element
 *                                 elList = document.getElementById("list1"), // List element
 *                                 elDividers = elList.getElementsByClassName("li-divider"), // List items (dividers)
 *                                 elScroller = elList.parentElement, // List's parent item (overflow-y:scroll)
 *                                 dividers = {}, // Collection of list dividers
 *                                 indices = [], // List of index
 *                                 elDivider,
 *                                 i, idx;
 *
 *                         // For all list dividers
 *                         for (i = 0; i < elDividers.length; i++) {
 *                             // Add the list divider elements to the collection
 *                             elDivider = elDividers[i];
 *                             // li element having the li-divider class
 *                             idx = elDivider.innerText;
 *                             // Get a text (index value)
 *                             dividers[idx] = elDivider;
 *                             // Remember the element
 *
 *                             // Add the index to the index list
 *                             indices.push(idx);
 *                         }
 *
 *                         // Change the data-index attribute to the indexscrollbar element
 *                         // before initializing IndexScrollbar widget
 *                         elem.setAttribute("data-index", indices.join(","));
 *
 *                         // Create index scroll bar
 *                         tau.IndexScrollbar(elem);
 *
 *                         // Bind the select callback
 *                         elem.addEventListener("select", function (ev) {
 *                             var elDivider,
 *                                     idx = ev.detail.index;
 *                             elDivider = dividers[idx];
 *                             if (elDivider) {
 *                                 // Scroll to the li-divider element
 *                                 elScroller.scrollTop = elDivider.offsetTop - elScroller.offsetTop;
 *                             }
 *                         });
 *                     });
 *                 }());
 *             </script>
 *         </div>
 *
 * The following example uses the supplementScroll argument, which shows a level 2 index scroll bar.
 * The application code must contain a level 2 index array for each level 1 index character.
 * The example shows a way to analyze list items and create a dictionary (secondIndex) for level 1
 * indices for the index scroll bar, and a dictionary (keyItem) for moving list items at runtime:
 *
 *      @example
 *         <div id="indexscrollbar2" class="ui-indexscrollbar"
 *              data-index="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z">
 *         </div>
 *         <ul class="ui-listview" id="ibar2_list2">
 *             <li>Anton</li>
 *             <li>Arabella</li>
 *             <li>Art</li>
 *             <li>Barry</li>
 *             <li>Bibi</li>
 *             <li>Billy</li>
 *             <li>Bob</li>
 *             <li>Carry</li>
 *             <li>Cibi</li>
 *             <li>Daisy</li>
 *             <li>Derek</li>
 *             <li>Desmond</li>
 *         </ul>
 *
 *         <script>
 *             (function () {
 *                 var page = document.getElementById("pageIndexScrollbar2"),
 *                         isb,
 *                         index = [],
 *                         supIndex = {},
 *                         elIndex = {};
 *                 page.addEventListener("pageshow", function () {
 *                     var elisb = document.getElementById("indexscrollbar2"),
 *                             elList = document.getElementById("ibar2_list2"), // List element
 *                             elItems = elList.children,
 *                             elScroller = elList.parentElement, // Scroller (overflow-y:hidden)
 *                             indexData = getIndexData(
 *                                     {
 *                                         array: elItems,
 *                                         getTextValue: function (array, i) {
 *                                             return array[i].innerText;
 *                                         }
 *                                     });
 *
 *                     function getIndexData(options) {
 *                         var array = options.array,
 *                                 getTextValue = options.getTextValue,
 *                                 item,
 *                                 text,
 *                                 firstIndex = [],
 *                                 secondIndex = {},
 *                                 keyItem = {},
 *                                 c1 = null,
 *                                 c2 = null,
 *                                 i;
 *
 *                         for (i = 0; i < array.length; i++) {
 *                             item = array[i];
 *                             text = getTextValue(array, i);
 *                             if (text.length > 0) {
 *                                 if (!c1 || c1 !== text[0]) {
 *                                     // New c1
 *                                     c1 = text[0];
 *                                     firstIndex.push(c1);
 *                                     keyItem[c1] = item;
 *                                     secondIndex[c1] = [];
 *                                     c2 = text[1];
 *                                     if (c2) {
 *                                         secondIndex[c1].push(c2);
 *                                     }
 *                                     else {
 *                                         c2 = '';
 *                                     }
 *                                     keyItem[c1 + c2] = item;
 *                                 }
 *                                 else {
 *                                     // Existing c1
 *                                     if (c2 !== text[1]) {
 *                                         c2 = text[1];
 *                                         secondIndex[c1].push(c2);
 *                                         keyItem[c1 + c2] = item;
 *                                     }
 *                                 }
 *                             }
 *                         }
 *                         return {
 *                             firstIndex: firstIndex,
 *                             secondIndex: secondIndex,
 *                             keyItem: keyItem
 *                         };
 *                     }
 *
 *                     // Update the data-index attribute to the indexscrollbar element, with the index list above
 *                     elisb.setAttribute("data-index", indexData.firstIndex);
 *                     // Create IndexScrollbar
 *                     isb = new tau.IndexScrollbar(elisb, {
 *                         index: indexData.firstIndex,
 *                         supplementaryIndex: function (firstIndex) {
 *                             return indexData.secondIndex[firstIndex];
 *                         }
 *                     });
 *                     // Bind the select callback
 *                     elisb.addEventListener("select", function (ev) {
 *                         var el,
 *                             index = ev.detail.index;
 *                         el = indexData.keyItem[index];
 *                         if (el) {
 *                             // Scroll to the li-divider element
 *                             elScroller.scrollTop = el.offsetTop - elScroller.offsetTop;
 *                         }
 *                     });
 *                 });
 *                 page.addEventListener("pagehide", function () {
 *                     console.log('isb2:destroy');
 *                     isb.destroy();
 *                     index.length = 0;
 *                     supIndex = {};
 *                     elIndex = {};
 *                 });
 *             }());
 *         </script>
 *
 * ##Options for widget
 *
 * Options for widget can be defined as _data-..._ attributes or give as parameter in constructor.
 *
 * You can change option for widget using method **option**.
 *
 * ##Methods
 *
 * To call method on widget you can use tau API:
 *
 * First API is from tau namespace:
 *
 *		@example
 *		var indexscrollbarElement = document.getElementById('indexscrollbar'),
 *			indexscrollbar = tau.widget.IndexScrollbar(indexscrollbarElement);
 *
 *		indexscrollbar.methodName(methodArgument1, methodArgument2, ...);
 *
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Jadwiga Sosnowska <j.sosnowska@samsung.com>
 * @author Tomasz Lukawski <t.lukawski@samsung.com>
 * @class ns.widget.core.IndexScrollbar
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var IndexScrollbar = function() {
				// Support calling without 'new' keyword
				this.element = null;
				this.indicator = null;
				this.indexBar1 = null;	// First IndexBar. Always shown.
				this.indexBar2 = null;	// 2-depth IndexBar. shown if needed.


				this.index = null;
				this.touchAreaOffsetLeft = 0;
				this.indexElements = null;
				this.selectEventTriggerTimeoutId = null;
				this.ulMarginTop = 0;

				this.eventHandlers = {};

			},
				BaseWidget = ns.widget.BaseWidget,
				/**
				 * Alias for class {@link ns.engine}
				 * @property {Object} engine
				 * @member ns.widget.core.IndexScrollbar
				 * @private
				 * @static
				 */
				engine = ns.engine,
				/**
				 * Alias for class {@link ns.event}
				 * @property {Object} events
				 * @member ns.widget.core.IndexScrollbar
				 * @private
				 * @static
				 */
				events = ns.event,
				/**
				 * Alias for class {@link ns.util.object}
				 * @property {Object} utilsObject
				 * @member ns.widget.core.IndexScrollbar
				 * @private
				 * @static
				 */
				utilsObject = ns.util.object,
				/**
				 * Alias for class ns.util.DOM
				 * @property {ns.util.DOM} doms
				 * @member ns.widget.wearable.IndexScrollbar
				 * @private
				 * @static
				 */
				doms = ns.util.DOM,

				IndexBar = ns.widget.core.indexscrollbar.IndexBar,
				IndexIndicator = ns.widget.core.indexscrollbar.IndexIndicator,
				EventType = {
					/**
					 * Event triggered after select index by user
					 * @event select
					 * @member ns.widget.core.IndexScrollbar
					 */
					SELECT: "select"
				},

				POINTER_START = 'vmousedown',
				POINTER_MOVE = 'vmousemove',
				POINTER_END = 'vmouseup',

				pointerIsPressed = false,
				prototype = new BaseWidget();

			IndexScrollbar.prototype = prototype;

			utilsObject.merge(prototype, {
				widgetName: "IndexScrollbar",
				widgetClass: "ui-indexscrollbar",

				_configure: function () {
					/**
					 * All possible widget options
					 * @property {Object} options
					 * @property {string} [options.moreChar="*"] more character
					 * @property {string} [options.selectedClass="ui-state-selected"] disabled class name
					 * @property {string} [options.delimiter=","] delimiter in index
					 * @property {string|Array} [options.index=["A","B","C","D","E","F","G","H","I",
					 * "J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","1"]]
					 * String with list of letters separate be delimiter or array of letters
					 * @property {boolean} [options.maxIndexLen=0]
					 * @property {boolean} [options.indexHeight=41]
					 * @property {boolean} [options.keepSelectEventDelay=50]
					 * @property {?boolean} [options.container=null]
					 * @property {?boolean} [options.supplementaryIndex=null]
					 * @property {number} [options.supplementaryIndexMargin=1]
					 * @member ns.widget.core.IndexScrollbar
					 */
					this.options = {
						moreChar: "*",
						indexScrollbarClass: "ui-indexscrollbar",
						selectedClass: "ui-state-selected",
						indicatorClass: "ui-indexscrollbar-indicator",
						delimiter: ",",
						index: [
							"A", "B", "C", "D", "E", "F", "G", "H",
							"I", "J", "K", "L", "M", "N", "O", "P", "Q",
							"R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1"
						],
						maxIndexLen: 0,
						indexHeight: 41,
						keepSelectEventDelay: 50,
						container: null,
						supplementaryIndex: null,
						supplementaryIndexMargin: 1
					};
				},

				/**
				 * This method builds widget.
				 * @method _build
				 * @protected
				 * @param {HTMLElement} element
				 * @return {HTMLElement}
				 * @member ns.widget.core.IndexScrollbar
				 */
				_build: function (element) {
					return element;
				},

				/**
				 * This method inits widget.
				 * @method _init
				 * @protected
				 * @param {HTMLElement} element
				 * @return {HTMLElement}
				 * @member ns.widget.core.IndexScrollbar
				 */
				_init: function (element) {
					var self = this,
						options = self.options;

					element.classList.add(options.indexScrollbarClass);

					self._setIndex(element, options.index);
					self._setMaxIndexLen(element, options.maxIndexLen);
					self._setInitialLayout();	// This is needed for creating sub objects
					self._createSubObjects();

					self._updateLayout();

					// Mark as extended
					self._extended(true);
					return element;
				},

				/**
				 * This method refreshes widget.
				 * @method _refresh
				 * @protected
				 * @return {HTMLElement}
				 * @member ns.widget.core.IndexScrollbar
				 */
				_refresh: function () {
					if( this._isExtended() ) {
						this._unbindEvent();
						this.indicator.hide();
						this._extended( false );
					}

					this._updateLayout();
					this.indexBar1.options.index = this.options.index;
					this.indexBar1.refresh();
					this._bindEvents();
					this._extended( true );
				},

				/**
				 * This method destroys widget.
				 * @method _destroy
				 * @protected
				 * @param {HTMLElement} element
				 * @return {HTMLElement}
				 * @member ns.widget.core.IndexScrollbar
				 */
				_destroy: function() {
					var self = this;
					if (self.isBound()) {
						self._unbindEvent();
						self._extended(false);
						self._destroySubObjects();
						self.indicator = null;
						self.index = null;
						self.eventHandlers = {};
					}
				},

				/**
				 * This method creates indexBar1 and indicator in the indexScrollbar
				 * @method _createSubObjects
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_createSubObjects: function() {
					var self =  this,
						options = self.options,
						element = self.element;
					// indexBar1
					self.indexBar1 = new IndexBar( document.createElement("UL"), {
						container: element,
						offsetLeft: 0,
						index: options.index,
						verticalCenter: true,
						indexHeight: options.indexHeight,
						maxIndexLen: options.maxIndexLen
					});

					// indexBar2
					if (typeof options.supplementaryIndex === "function") {
						self.indexBar2 = new IndexBar( document.createElement("UL"), {
							container: element,
							offsetLeft: -element.clientWidth - options.supplementaryIndexMargin,
							index: [],	// empty index
							indexHeight: options.indexHeight,
							ulClass: "ui-indexscrollbar-supplementary"
						});
						self.indexBar2.hide();
					}

					// indicator
					self.indicator = new IndexIndicator(document.createElement("DIV"), {
						container: self._getContainer(),
						referenceElement: self.element,
						className: options.indicatorClass
					});

				},

				/**
				 * This method destroys sub-elements: index bars and indicator.
				 * @method _destroySubObjects
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_destroySubObjects: function() {
					var subObjs = {
							iBar1: this.indexBar1,
							iBar2: this.indexBar2,
							indicator: this.indicator
						},
						subObj,
						el,
						i;
					for(i in subObjs) {
						subObj = subObjs[i];
						if(subObj) {
							el = subObj.element;
							subObj.destroy();
							el.parentNode.removeChild(el);
						}
					}
				},

				/**
				 * This method sets initial layout.
				 * @method _setInitialLayout
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_setInitialLayout: function () {
					var indexScrollbar = this.element,
						container = this._getContainer(),
						containerPosition = window.getComputedStyle(container).position,
						indexScrollbarStyle = indexScrollbar.style;

					// Set the indexScrollbar's position, if needed
					if (containerPosition !== "absolute" && containerPosition !== "relative") {
						indexScrollbarStyle.top = container.offsetTop + "px";
						indexScrollbarStyle.height = container.offsetHeight + "px";
					}
				},

				/**
				 * This method calculates maximum index length.
				 * @method _setMaxIndexLen
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_setMaxIndexLen: function(element, value) {
					var self = this,
						options = self.options,
						container = self._getContainer(),
						containerHeight = container.offsetHeight;

					if (value <= 0) {
						value = Math.floor( containerHeight / options.indexHeight );
					}
					if (value > 0 && value%2 === 0) {
						value -= 1;	// Ensure odd number
					}
					options.maxIndexLen = value;
				},

				/**
				 * This method updates layout.
				 * @method _updateLayout
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_updateLayout: function() {
					this._setInitialLayout();
					this._draw();

					this.touchAreaOffsetLeft = this.element.offsetLeft - 10;
				},

				/**
				 * This method draws additional sub-elements
				 * @method _draw
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_draw: function () {
					this.indexBar1.show();
					return this;
				},

				/**
				 * This method removes indicator.
				 * @method _removeIndicator
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_removeIndicator: function() {
					var indicator = this.indicator,
						parentElem = indicator.element.parentNode;

					parentElem.removeChild(indicator.element);
					indicator.destroy();
					this.indicator = null;
				},

				/**
				 * This method returns the receiver of event by position.
				 * @method _getEventReceiverByPosition
				 * @param {number} posX The position relative to the left edge of the document.
				 * @return {?ns.widget.core.indexscrollbar.IndexBar} Receiver of event
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_getEventReceiverByPosition: function(posX) {
					var windowWidth = window.innerWidth,
						elementWidth = this.element.clientWidth,
						receiver;

					if( this.options.supplementaryIndex ) {
						if( windowWidth - elementWidth <= posX && posX <= windowWidth) {
							receiver = this.indexBar1;
						} else {
							receiver = this.indexBar2;
						}
					} else {
						receiver = this.indexBar1;
					}
					return receiver;
				},

				/**
				 * This method updates indicator.
				 * It sets new value of indicator and triggers event "select".
				 * @method _updateIndicatorAndTriggerEvent
				 * @param {number} val The value of indicator
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_updateIndicatorAndTriggerEvent: function(val) {
					this.indicator.setValue( val );
					this.indicator.show();
					if(this.selectEventTriggerTimeoutId) {
						window.clearTimeout(this.selectEventTriggerTimeoutId);
					}
					this.selectEventTriggerTimeoutId = window.setTimeout(function() {
						this.trigger(EventType.SELECT, {index: val});
						this.selectEventTriggerTimeoutId = null;
					}.bind(this), this.options.keepSelectEventDelay);
				},

				/**
				 * This method is executed on event "touchstart"
				 * @method _onTouchStartHandler
				 * @param {Event} event Event
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_onTouchStartHandler: function(event) {
					pointerIsPressed = true;
					var touches = event.touches || event._originalEvent && event._originalEvent.touches;
					if (touches && (touches.length > 1)) {
						event.preventDefault();
						event.stopPropagation();
						return;
					}
					var pos = this._getPositionFromEvent(event),
					// At touchstart, only indexbar1 is shown.
						iBar1 = this.indexBar1,
						idx = iBar1.getIndexByPosition( pos.y ),
						val = iBar1.getValueByIndex( idx );

					iBar1.select( idx );	// highlight selected value

					document.addEventListener(POINTER_MOVE, this.eventHandlers.touchMove);
					document.addEventListener(POINTER_END, this.eventHandlers.touchEnd);
					document.addEventListener("touchcancel", this.eventHandlers.touchEnd);

					this._updateIndicatorAndTriggerEvent( val );
				},

				/**
				 * This method is executed on event "touchmove"
				 * @method _onTouchMoveHandler
				 * @param {Event} event Event
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_onTouchMoveHandler: function(event) {
					var touches = event._originalEvent && event._originalEvent.touches;
					if (touches && (touches.length > 1) || !pointerIsPressed) {
						events.preventDefault(event);
						events.stopPropagation(event);
						return;
					}

					var pos = this._getPositionFromEvent( event ),
						iBar1 = this.indexBar1,
						iBar2 = this.indexBar2,
						idx,
						iBar,
						val;

					// Check event receiver: ibar1 or ibar2
					iBar = this._getEventReceiverByPosition( pos.x );
					if( iBar === iBar2 ) {
						iBar2.options.index = this.options.supplementaryIndex(iBar1.getValueByIndex(iBar1.selectedIndex));
						iBar2.refresh();
					}

					// get index and value from ibar1 or ibar2
					idx = iBar.getIndexByPosition( pos.y );
					val = iBar.getValueByIndex( idx );
					if(iBar === iBar2) {
						// Update val to make a concatenated string for indexIndicator
						val = iBar1.getValueByIndex(iBar1.selectedIndex) + val;
					} else if(iBar2 && !iBar2.isShown()) {
						// iBar1 is selected.
						// Set iBar2's paddingTop, only when the iBar2 isn't shown
						iBar2.setPaddingTop(iBar1.getOffsetTopByIndex(iBar1.selectedIndex));
					}

					// update ibars
					iBar.select(idx);	// highlight selected value
					iBar.show();
					if( iBar1 === iBar && iBar2 ) {
						iBar2.hide();
					}

					// update indicator
					this._updateIndicatorAndTriggerEvent( val );

					events.preventDefault(event);
					events.stopPropagation(event);
				},

				/**
				 * This method is executed on event "touchend"
				 * @method _onTouchEndHandler
				 * @param {Event} event Event
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_onTouchEndHandler: function( event ) {
					var self = this,
						touches = event._originalEvent && event._originalEvent.touches;

					if (touches && (touches.length === 0) ||
							!touches) {
						pointerIsPressed = false;
					}
					self.indicator.hide();
					self.indexBar1.clearSelected();
					if(self.indexBar2) {
						self.indexBar2.clearSelected();
						self.indexBar2.hide();
					}

					document.removeEventListener(POINTER_MOVE, self.eventHandlers.touchMove);
					document.removeEventListener(POINTER_END, self.eventHandlers.touchEnd);
					document.removeEventListener("touchcancel", self.eventHandlers.touchEnd);
				},

				/**
				 * This method binds events to widget.
				 * @method _bindEvents
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_bindEvents: function() {
					this._bindResizeEvent();
					this._bindEventToTriggerSelectEvent();
				},

				/**
				 * This method unbinds events to widget.
				 * @method _unbindEvent
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_unbindEvent: function() {
					this._unbindResizeEvent();
					this._unbindEventToTriggerSelectEvent();
				},

				/**
				 * This method binds event "resize".
				 * @method _bindResizeEvent
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_bindResizeEvent: function() {
					this.eventHandlers.onresize = function(/* ev */) {
						this.refresh();
					}.bind(this);

					window.addEventListener( "resize", this.eventHandlers.onresize );
				},

				/**
				 * This method unbinds event "resize".
				 * @method _bindResizeEvent
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_unbindResizeEvent: function() {
					if ( this.eventHandlers.onresize ) {
						window.removeEventListener( "resize", this.eventHandlers.onresize );
					}
				},

				/**
				 * This method binds touch events.
				 * @method _bindEventToTriggerSelectEvent
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_bindEventToTriggerSelectEvent: function() {
					var self = this;
					self.eventHandlers.touchStart = self._onTouchStartHandler.bind(self);
					self.eventHandlers.touchEnd = self._onTouchEndHandler.bind(self);
					self.eventHandlers.touchMove = self._onTouchMoveHandler.bind(self);

					self.element.addEventListener(POINTER_START, self.eventHandlers.touchStart);
				},

				/**
				 * This method unbinds touch events.
				 * @method _unbindEventToTriggerSelectEvent
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_unbindEventToTriggerSelectEvent: function() {
					var self = this;
					self.element.removeEventListener(POINTER_START, self.eventHandlers.touchStart);
				},

				/**
				 * This method sets or gets data from widget.
				 * @method _data
				 * @param {string|Object} key
				 * @param {*} val
				 * @return {*} Return value of data or widget's object
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_data: function (key, val) {
					var el = this.element,
						d = el.__data,
						idx;
					if(!d) {
						d = el.__data = {};
					}
					if(typeof key === "object") {
						// Support data collection
						for(idx in key) {
							this._data(idx, key[idx]);
						}
						return this;
					} else {
						if("undefined" === typeof val) {	// Getter
							return d[key];
						} else {	// Setter
							d[key] = val;
							return this;
						}
					}
				},

				/**
				 * This method checks if element is valid element of widget IndexScrollbar.
				 * @method _isValidElement
				 * @param {HTMLElement} el
				 * @return {boolean} True, if element is valid.
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_isValidElement: function (el) {
					return el.classList.contains(this.widgetClass);
				},

				/**
				 * This method checks if widget is extended.
				 * @method _isExtended
				 * @return {boolean} True, if element is extended.
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_isExtended: function () {
					return !!this._data("extended");
				},

				/**
				 * This method sets value of "extended" to widget.
				 * @method _extended
				 * @param {boolean} flag Value for extended
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_extended: function (flag) {
					this._data("extended", flag);
					return this;
				},

				/**
				 * This method gets indices prepared from parameter
				 * or index of widget.
				 * @method _setIndex
				 * @param {HTMLElement} element element
				 * @param {string} value Indices to prepared
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_setIndex: function (element, value) {
					var options = this.options;
					if (typeof value === "string") {
						value = value.split(options.delimiter);	// delimiter
					}
					options.index = value;
				},

				/**
				 * This method gets offset of element.
				 * @method _getOffset
				 * @param {HTMLElement} el Element
				 * @return {Object} Offset with "top" and "left" properties
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_getOffset: function( el ) {
					var left=0, top=0 ;
					do {
						top += el.offsetTop;
						left += el.offsetLeft;
						el = el.offsetParent;
					} while (el);

					return {
						top: top,
						left: left
					};
				},

				/**
				 * This method returns container of widget.
				 * @method _getContainer
				 * @return {HTMLElement} Container
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_getContainer: function() {
					var container = this.options.container,
						element = this.element,
						parentElement = element.parentNode,
						overflow;

					if (!container) {
						while (parentElement && parentElement != document.body) {
							overflow = doms.getCSSProperty(parentElement, "overflow-y");
							if (overflow === "scroll" || (overflow === "auto" && parentElement.scrollHeight > parentElement.clientHeight)) {
								return parentElement;
							}
							parentElement = parentElement.parentNode;
						}
						container = element.parentNode;
					}

					return container || element.parentNode;
				},

				/**
				 * Returns position of event.
				 * @method _getPositionFromEvent
				 * @return {Object} Position of event with properties "x" and "y"
				 * @protected
				 * @member ns.widget.core.IndexScrollbar
				 */
				_getPositionFromEvent: function( ev ) {
					return ev.type.search(/^touch/) !== -1 ?
					{x: ev.touches[0].clientX, y: ev.touches[0].clientY} :
					{x: ev.clientX, y: ev.clientY};
				},

				/**
				 * Adds event listener to element of widget.
				 * @method addEventListener
				 * @param {string} type Name of event
				 * @param {Function} listener Function to be executed
				 * @member ns.widget.core.IndexScrollbar
				 */
				addEventListener: function (type, listener) {
					this.element.addEventListener(type, listener);
				},

				/**
				 * Removes event listener from element of widget.
				 * @method removeEventListener
				 * @param {string} type Name of event
				 * @param {Function} listener Function to be removed
				 * @member ns.widget.core.IndexScrollbar
				 */
				removeEventListener: function (type, listener) {
					this.element.removeEventListener(type, listener);
				}

			});

			// definition
			ns.widget.core.IndexScrollbar = IndexScrollbar;
			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/*
 *
 * @class ns.widget.wearable.IndexScrollbar
 * @extends ns.widget.core.IndexScrollbar
 * @since 2.0
 */
(function (document, ns) {
	
	
			var engine = ns.engine,
				CoreIndexScrollbar = ns.widget.core.IndexScrollbar,
				prototype = new CoreIndexScrollbar(),
				IndexScrollbar = function () {
					CoreIndexScrollbar.call(this);
				};

			// definition
			IndexScrollbar.prototype = prototype;
			ns.widget.wearable.IndexScrollbar = IndexScrollbar;

			engine.defineWidget(
				"IndexScrollbar",
				".ui-indexscrollbar",
				[],
				IndexScrollbar,
				"wearable"
			);

			}(window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * # CircularIndexScrollbar UI Component
 * Shows a circularindexscrollbar with indices, usually for the list.
 *
 * The circularindexscrollbar component shows on the screen a circularscrollbar with indices.
 * The indices can be selected by moving the rotary.
 * And it fires a select event when the index characters are selected.
 *
 * ## Manual constructor
 * For manual creation of UI Component you can use constructor of component from **tau** namespace:
 *
 *              @example
 *              var circularindexElement = document.getElementById('circularindex'),
 *                  circularindexscrollbar = tau.widget.CircularIndexScrollbar(circularindexElement, {index: "A,B,C"});
 *
 * Constructor has one require parameter **element** which are base **HTMLElement** to create component.
 * We recommend get this element by method *document.getElementById*. Second parameter is **options**
 * and it is a object with options for component.
 *
 * To add an CircularIndexScrollbar component to the application, use the following code:
 *
 *      @example
 *      <div id="foo" class="ui-circularindexscrollbar" data-index="A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z"></div>
 *      <script>
 *          (function() {
 *              var elem = document.getElementById("foo");
 *              tau.widget.CircularIndexScrollbar(elem);
 *              elem.addEventListener("select", function( event ) {
 *                  var index = event.detail.index;
 *                  console.log(index);
 *              });
 *          }());
 *      </script>
 *
 * The index value can be retrieved by accessing event.detail.index property.
 *
 * In the following example, the list scrolls to the position of the list item defined using
 * the li-divider class, selected by the circularindexscrollbar:
 *
 *      @example
 *         <div id="pageCircularIndexScrollbar" class="ui-page">
 *             <header class="ui-header">
 *                 <h2 class="ui-title">CircularIndexScrollbar</h2>
 *             </header>
 *             <div id="circularindexscrollbar"class="ui-circularindexscrollbar" data-index="A,B,C,D,E"></div>
 *             <section class="ui-content">
 *                 <ul class="ui-listview" id="list1">
 *                     <li class="li-divider">A</li>
 *                     <li>Anton</li>
 *                     <li>Arabella</li>
 *                     <li>Art</li>
 *                     <li class="li-divider">B</li>
 *                     <li>Barry</li>
 *                     <li>Bibi</li>
 *                     <li>Billy</li>
 *                     <li>Bob</li>
 *                     <li class="li-divider">D</li>
 *                     <li>Daisy</li>
 *                     <li>Derek</li>
 *                     <li>Desmond</li>
 *                 </ul>
 *             </section>
 *             <script>
 *                 (function () {
 *                     var page = document.getElementById("pageIndexScrollbar"),
                           circularindexscrollbar;
 *                     page.addEventListener("pagecreate", function () {
 *                         var elisb = document.getElementById("circularindexscrollbar"), // CircularIndexscrollbar element
 *                                 elList = document.getElementById("list1"), // List element
 *                                 elDividers = elList.getElementsByClassName("li-divider"), // List items (dividers)
 *                                 elScroller = elList.parentElement, // List's parent item
 *                                 dividers = {}, // Collection of list dividers
 *                                 indices = [], // List of index
 *                                 elDivider,
 *                                 i, idx;
 *
 *                         // For all list dividers
 *                         for (i = 0; i < elDividers.length; i++) {
 *                             // Add the list divider elements to the collection
 *                             elDivider = elDividers[i];
 *                             // li element having the li-divider class
 *                             idx = elDivider.innerText;
 *                             // Get a text (index value)
 *                             dividers[idx] = elDivider;
 *                             // Remember the element
 *
 *                             // Add the index to the index list
 *                             indices.push(idx);
 *                         }
 *
 *                         // Create CircularIndexScrollbar
 *                         circularindexscrollbar = new tau.widget.CircularIndexScrollbar(elisb, {index: indices});
 *
 *                         // Bind the select callback
 *                         elisb.addEventListener("select", function (ev) {
 *                             var elDivider,
 *                                     idx = ev.detail.index;
 *                             elDivider = dividers[idx];
 *                             if (elDivider) {
 *                                 // Scroll to the li-divider element
 *                                 elScroller.scrollTop = elDivider.offsetTop - elScroller.offsetTop;
 *                             }
 *                         });
 *                     });
 *                 }());
 *             </script>
 *         </div>
 *
 * @author Junyoung Park <jy-.park@samsung.com>
 * @author Hagun Kim <hagun.kim@samsung.com>
 * @class ns.widget.wearable.CircularIndexScrollbar
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,
				utilsEvents = ns.event,
				eventTrigger = utilsEvents.trigger,
				prototype = new BaseWidget(),

				CircularIndexScrollbar = function() {
					this._phase = null;
					this._tid = {
						phaseOne: 0,
						phaseThree: 0
					};
					this._detent = {
						phaseOne: 0
					};
					this.options = {};
					this._activeIndex = 0;
				},

				rotaryDirection = {
					// right rotary direction
					CW: "CW",
					// left rotary direction
					CCW: "CCW"
				},

				EventType = {
					/**
					 * Event triggered after select index by user
					 * @event select
					 * @member ns.widget.wearable.CircularIndexScrollbar
					 */
					SELECT: "select"
				},

				classes = {
					INDEXSCROLLBAR: "ui-circularindexscrollbar",
					INDICATOR: "ui-circularindexscrollbar-indicator",
					INDICATOR_TEXT: "ui-circularindexscrollbar-indicator-text",
					SHOW: "ui-circularindexscrollbar-show"
				};

			CircularIndexScrollbar.prototype = prototype;

			/**
			 * This method configure component.
			 * @method _configure
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._configure = function() {
				/**
				 * All possible component options
				 * @property {Object} options
				 * @property {string} [options.delimiter=","] delimiter in index
				 * @property {string|Array} [options.index=["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","1"]] indices list
				 * String with list of letters separate be delimiter or array of letters
				 * @property {number} [options.maxVisibleIndex=30] maximum length of visible indices
				 * @property {number} [options.duration=500] duration of show/hide animation time
				 * @member ns.widget.wearable.CircularIndexScrollbar
				 */
				this.options = {
					delimiter: ",",
					index: [
						"A", "B", "C", "D", "E", "F", "G", "H",
						"I", "J", "K", "L", "M", "N", "O", "P", "Q",
						"R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1"
					]
				};
			};

			/**
			 * This method build component.
			 * @method _build
			 * @protected
			 * @param {HTMLElement} element
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._build = function(element) {
				var indicator,
					indicatorText;

				indicator = document.createElement("div");
				indicator.classList.add(classes.INDICATOR);
				indicatorText = document.createElement("span");
				indicatorText.classList.add(classes.INDICATOR_TEXT);
				indicator.appendChild(indicatorText);
				element.appendChild(indicator);
				element.classList.add(classes.INDEXSCROLLBAR);

				return element;
			};

			/**
			 * This method inits component.
			 * @method _init
			 * @protected
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._init = function(element) {
				var self = this,
					options = self.options;

				self._phase = 1;

				self._setIndices(options.index);
				self._setValueByPosition(self._activeIndex, true);

				return element;
			};

			/**
			 * This method set indices prepared from parameter
			 * or index of component.
			 * @method _setIndices
			 * @param {string} [value] Indices to prepared
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._setIndices = function(value) {
				var self = this,
					options = self.options;

				if (value === null) {
					ns.warn("CircularIndexScrollbar must have indices.");
					options.index = null;
					return;
				}

				if (typeof value === "string") {
					value = value.split(options.delimiter); // delimiter
				}

				options.index = value;
			};

			/**
			 * This method select the index
			 * @method _setValueByPosition
			 * @protected
			 * @param {string} index index number
			 * @param {boolean} isFireEvent whether "select" event is fired or not
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._setValueByPosition = function(index, isFireEvent) {
				var self = this,
					indicatorText;

				if (!self.options.index) {
					return;
				}

				indicatorText = self.element.querySelector("." + classes.INDICATOR_TEXT);

				self._activeIndex = index;
				indicatorText.innerHTML = self.options.index[index];
				if (isFireEvent) {
					eventTrigger(self.element, EventType.SELECT, {index: self.options.index[index]});
				}
			};

			/**
			 * This method select next index
			 * @method _nextIndex
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._nextIndex = function() {
				var self = this,
					activeIndex = self._activeIndex,
					indexLen = self.options.index.length,
					nextIndex;

				if (activeIndex < indexLen -1 ) {
					nextIndex = activeIndex + 1;
				} else {
					return;
				}
				self._setValueByPosition(nextIndex, true);
			};

			/**
			 * This method select previos index
			 * @method _prevIndex
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._prevIndex = function() {
				var self = this,
					activeIndex = self._activeIndex,
					prevIndex;

				if (activeIndex > 0) {
					prevIndex = activeIndex - 1;
				} else {
					return;
				}

				self._setValueByPosition(prevIndex, true);
			};

			/**
			 * Get or Set index of the CircularIndexScrollbar
			 *
			 * Return current index or set the index
			 *
			 *		@example
			 *		<progress class="ui-circularindexscrollbar" id="circularindexscrollbar"></progress>
			 *		<script>
			 *			var circularindexElement = document.getElementById("circularindex"),
			 *				circularIndexScrollbar = tau.widget.CircleProgressBar(circularindexElement),
			 *			// return current index value
			 *			value = circularIndexScrollbar.value();
			 *			// sets the index value
			 *			circularIndexScrollbar.value("C");
			 *		</script>
			 * @method value
			 * return {string} In get mode return current index value
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			/**
			 * This method select the index
			 * @method _setValue
			 * @protected
			 * @param {string} value of index
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._setValue = function(value) {
				var self = this,
					index = self.options.index,
					indexNumber;

				if (index && (indexNumber = index.indexOf(value)) >= 0) {
					self._setValueByPosition(indexNumber, false);
				}
			};

			/**
			 * This method gets current index
			 * @method _getValue
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._getValue = function() {
				var self = this,
					index = self.options.index;

				if (index) {
					return index[self._activeIndex];
				} else {
					return null;
				}
			};

			/**
			 * This method is a "rotarydetent" event handler
			 * @method _rotary
			 * @protected
			 * @param {Event} event Event
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._rotary = function(event) {
				var self = this,
					direction = event.detail.direction;

				if (!self.options.index) {
					return;
				}

				if (self._phase === 1) {
					self._rotaryPhaseOne();
				} else if (self._phase === 3) {
					event.stopPropagation();
					self._rotaryPhaseThree(direction);
				}
			};

			/**
			 * This method is for phase 1 operation.
			 * @method _rotaryPhaseOne
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._rotaryPhaseOne = function() {
				var self = this;
				clearTimeout(self._tid.phaseOne);
				self._tid.phaseOne = setTimeout(function(){
					if (self._phase === 1) {
						self._detent.phaseOne = 0;
					}
				}, 100);

				if (self._detent.phaseOne > 3) {
					self._phase = 3;
					clearTimeout(self._tid.phaseOne);
					self._detent.phaseOne = 0;
				} else {
					self._detent.phaseOne++;
				}
			};

			/**
			 * This method is for phase 3 operation.
			 * @method _rotaryPhaseThree
			 * @protected
			 * @param {string} direction direction of rotarydetent event
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._rotaryPhaseThree = function(direction) {
				var self = this;
				clearTimeout(self._tid.phaseThree);
				self._tid.phaseThree = setTimeout(function(){
					self.element.classList.remove(classes.SHOW);
					self._phase = 1;
				}, 1000);

				if(self._phase === 3) {
					self.element.classList.add(classes.SHOW);
					if (direction === rotaryDirection.CW) {
						self._nextIndex();
					} else {
						self._prevIndex();
					}
				}
			};

			/**
			 * This method handles events
			 * @method handleEvent
			 * @public
			 * @param {Event} event Event
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype.handleEvent = function(event) {
				var self = this;

				switch (event.type) {
					case "rotarydetent":
						self._rotary(event);
						break;
				}
			};

			/**
			 * This method binds events to component.
			 * method _bindEvents
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._bindEvents = function() {
				var self = this;

				utilsEvents.on(document, "rotarydetent", self);
			};

			/**
			 * This method unbinds events to component.
			 * method _unbindEvents
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._unbindEvents = function() {
				var self = this;

				utilsEvents.off(document, "rotarydetent", self);
			};

			/**
			 * This method refreshes component.
			 * @method _refresh
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._refresh = function() {
				var self = this,
					options = self.options;

				self._unbindEvents();
				self._setIndices(options.index);
				self._setValueByPosition(self._activeIndex, true);
				self._bindEvents();
			};

			/**
			 * This method detroys component.
			 * @method _destroy
			 * @protected
			 * @member ns.widget.wearable.CircularIndexScrollbar
			 */
			prototype._destroy = function() {
				var self = this;
				self._unbindEvents();
			};

			// definition
			ns.widget.wearable.CircularIndexScrollbar = CircularIndexScrollbar;
			engine.defineWidget(
				"CircularIndexScrollbar",
				".ui-circularindexscrollbar",
				[],
				CircularIndexScrollbar,
				"wearable"
			);
			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/**
 * # Progress Widget
 * Shows a control that indicates the progress percentage of an on-going operation.
 *
 * The progress widget shows a control that indicates the progress percentage of an on-going operation. This widget can be scaled to fit inside a parent container.
 *
 * ## Default selectors
 *
 * This widget provide three style progress.
 *
 * ### Simple progress bar
 * If you don't implement any class, you can show default progress style
 * To add a progress widget to the application, use the following code:
 *
 *      @example
 *      <progress max="100" value="90"></progress>
 *
 * ### Infinite progress bar
 * If you implement class (*ui-progress-indeterminate*), you can show image looks like infinite move.
 *
 * To add a progress widget to the application, use the following code:
 *      @example
 *      <progress class="ui-progress-indeterminate" max="100" value="100"></progress>
 *
 * ### Progress bar with additional information
 * If you implement div tag that can choose two classes (*ui-progress-proportion* or *ui-progress-ratio*) at progress tag same level, you can show two information (proportion information is located left below and ratio information is located right below)
 *
 * To add a progress widget to the application, use the following code:
 *
 *      @example
 *      <progress max="100" value="50"></progress>
 *      <div class="ui-progress-proportion">00/20</div>
 *      <div class="ui-progress-ratio">50%</div>
 *
 * ## JavaScript API
 *
 * Progress widget hasn't JavaScript API.
 *
 * @class ns.widget.wearable.Progress
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,

				Progress = function () {
					return this;
				},
				prototype = new BaseWidget();

			Progress.events = {};

			/**
			 * Build Progress
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.wearable.Progress
			 */
			prototype._build = function (element) {
				return element;
			};

			prototype._init = function (element) {
				return element;
			};

			prototype._bindEvents = function (element) {
				return element;
			};

			/**
			 * Refresh structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.wearable.Progress
			 */
			prototype._refresh = function () {
				return null;
			};

			/**
			 * Destroy widget
			 * @method _destroy
			 * @protected
			 * @member ns.widget.wearable.Progress
			 */
			prototype._destroy = function () {
				return null;
			};

			Progress.prototype = prototype;
			ns.widget.wearable.Progress = Progress;

			engine.defineWidget(
				"Progress",
				"progress",
				[],
				Progress,
				"wearable"
			);
			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/**
 * # Processing Widget
 * Shows that an operation is in progress.
 *
 * The processing widget shows that an operation is in progress.
 *
 * ## Default selectors
 *
 * To add a processing widget to the application, use the following code:
 *
 *      @example
 *      <div class="ui-processing"></div>
 *      <div class="ui-processing-text">
 *          Description about progress
 *      </div>
 *
 * ## JavaScript API
 *
 * Processing widget hasn't JavaScript API.
 *
 * @class ns.widget.wearable.Progressing
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,
				Progressing = function () {
					return this;
				},
				prototype = new BaseWidget();

			Progressing.events = {};

			/**
			 * Build Progressing
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.wearable.Progressing
			 */
			prototype._build = function (element) {
				return element;
			};

			prototype._init = function (element) {
				return element;
			};

			prototype._bindEvents = function (element) {
				return element;
			};

			/**
			 * Refresh structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.wearable.Progressing
			 */
			prototype._refresh = function () {
				return null;
			};

			/**
			 * Destroy widget
			 * @method _destroy
			 * @protected
			 * @member ns.widget.wearable.Progressing
			 */
			prototype._destroy = function () {
				return null;
			};

			Progressing.prototype = prototype;
			ns.widget.wearable.Progressing = Progressing;

			engine.defineWidget(
				"Progressing",
				".ui-progress",
				[],
				Progressing,
				"wearable"
			);
			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/**
 * # Toggle Switch Widget
 * Shows a 2-state switch.
 *
 * The toggle switch widget shows a 2-state switch on the screen.
 *
 * ## Default selectors
 *
 * To add a toggle switch widget to the application, use the following code:
 *
 *      @example
 *      <div class="ui-switch">
 *          <div class="ui-switch-text">
 *              Toggle Switch
 *          </div>
 *          <label class="ui-toggleswitch">
 *              <input type="checkbox" class="ui-switch-input">
 *              <div class="ui-switch-activation">
 *                   <div class="ui-switch-inneroffset">
 *                       <div class="ui-switch-handler"></div>
 *                   </div>
 *              </div>
 *           </label>
 *      </div>
 *
 * ## JavaScript API
 *
 * ToggleSwitch widget hasn't JavaScript API.
 *
 * @class ns.widget.wearable.ToggleSwitch
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,

				ToggleSwitch = function () {
					/**
					 * Options for widget
					 * @property {Object} options
					 * @property {?string} [options.text=null] Shown text
					 * @member ns.widget.wearable.ToggleSwitch
					 */
					this.options = {
						text: null
					};
				},
				events = {},
				classesPrefix = "ui-switch",
				classes = {
					handler: classesPrefix + "-handler",
					inneroffset: classesPrefix + "-inneroffset",
					activation: classesPrefix + "-activation",
					input: classesPrefix + "-input",
					text: classesPrefix + "-text"
				},
				prototype = new BaseWidget();

			function getClass(name) {
				return classes[name];
			}

			function addClass(element, classId) {
				element.classList.add(getClass(classId));
			}

			function createElement(name) {
				return document.createElement(name);
			}

			/**
			 * Dictionary for ToggleSwitch related events.
			 * For ToggleSwitch, it is an empty object.
			 * @property {Object} events
			 * @member ns.widget.wearable.ToggleSwitch
			 * @static
			 */
			ToggleSwitch.events = events;

			/**
			 * Dictionary for ToggleSwitch related css class names
			 * @property {Object} classes
			 * @member ns.widget.wearable.ToggleSwitch
			 * @static
			 * @readonly
			 */
			ToggleSwitch.classes = classes;

			/**
			 * Build ToggleSwitch
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.wearable.ToggleSwitch
			 */
			prototype._build = function (element) {
				var options = this.options,
					text = options.text,
					divText,
					label = createElement("label"),
					input = createElement("input"),
					divActivation = createElement("div"),
					divInneroffset = createElement("div"),
					divHandler = createElement("div");

				if (text) {
					divText = createElement("div");
					addClass(divText, "text");
					divText.innerHTML = text;
					element.appendChild(divText);
				}
				addClass(divHandler, "handler");
				divInneroffset.appendChild(divHandler);
				addClass(divInneroffset, "inneroffset");
				divActivation.appendChild(divInneroffset);
				addClass(divActivation, "activation");
				label.classList.add("ui-toggleswitch");
				input.type = "checkbox";
				addClass(input, "input");
				label.appendChild(input);
				label.appendChild(divActivation);
				element.appendChild(label);
				return element;
			};

			ToggleSwitch.prototype = prototype;
			ns.widget.wearable.ToggleSwitch = ToggleSwitch;

			engine.defineWidget(
				"ToggleSwitch",
				".ui-switch",
				[],
				ToggleSwitch,
				"wearable"
			);
			}(window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, white: true, plusplus: true*/
(function (document, ns) {
	
				/**
			 * @property {Object} Widget Alias for {@link ns.widget.BaseWidget}
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			var BaseWidget = ns.widget.BaseWidget,
				// Constants definition
				/**
				 * Defines index of scroll `{@link ns.widget.core.VirtualListview#_scroll}.direction`
				 * @property {number} SCROLL_UP
				 * to retrive if user is scrolling up
				 * @private
				 * @static
				 * @member ns.widget.core.VirtualListview
				 */
				SCROLL_UP = 0,
				/**
				 * Defines index of scroll `{@link ns.widget.core.VirtualListview#_scroll}.direction`
				 * @property {number} SCROLL_RIGHT
				 * to retrive if user is scrolling right
				 * @private
				 * @static
				 * @member ns.widget.core.VirtualListview
				 */
				SCROLL_RIGHT = 1,
				/**
				 * Defines index of scroll {@link ns.widget.core.VirtualListview#_scroll}
				 * @property {number} SCROLL_DOWN
				 * to retrive if user is scrolling down
				 * @private
				 * @static
				 * @member ns.widget.core.VirtualListview
				 */
				SCROLL_DOWN = 2,
				/**
				 * Defines index of scroll {@link ns.widget.core.VirtualListview#_scroll}
				 * @property {number} SCROLL_LEFT
				 * to retrive if user is scrolling left
				 * @private
				 * @static
				 * @member ns.widget.core.VirtualListview
				 */
				SCROLL_LEFT = 3,
				/**
				 * Defines vertical scrolling orientation. It's default orientation.
				 * @property {string} VERTICAL
				 * @private
				 * @static
				 */
				VERTICAL = "y",
				/**
				 * Defines horizontal scrolling orientation.
				 * @property {string} HORIZONTAL
				 * @private
				 * @static
				 */
				HORIZONTAL = "x",
				/**
				 * Determines that scroll event should not be taken into account if scroll event accurs.
				 * @property {boolean} blockEvent
				 * @private
				 * @static
				 */
				blockEvent = false,
				/**
				 * Handle window timeout ID.
				 * @property {number} timeoutHandler
				 * @private
				 * @static
				 */
				timeoutHandler,
				/**
				 * Reference to original target object from touch event.
				 * @property {Object} origTarget
				 * @private
				 * @static
				 */
				origTarget,
				/**
				 * Number of miliseconds to determine if tap event occured.
				 * @property {number} tapholdThreshold
				 * @private
				 * @static
				 */
				tapholdThreshold = 250,
				/**
				 * Handler for touch event listener to examine tap occurance.
				 * @property {Object} tapHandlerBound
				 * @private
				 * @static
				 */
				tapHandlerBound = null,
				/**
				 * Stores last touch position to examine tap occurance.
				 * @property {Object} lastTouchPos
				 * @private
				 * @static
				 */
				lastTouchPos =	{},

				selectors = ns.util.selectors,

				utilEvent = ns.event,

				/**
				 * Local constructor function
				 * @method VirtualListview
				 * @private
				 * @member ns.widget.core.VirtualListview
				 */
				VirtualListview = function() {
					var self = this;
					/**
					 * VirtualListview widget's properties associated with
					 * @property {Object} ui
					 * User Interface
					 * @property {?HTMLElement} [ui.scrollview=null] Scroll element
					 * @property {?HTMLElement} [ui.spacer=null] HTML element which makes scrollbar proper size
					 * @property {number} [ui.itemSize=0] Size of list element in pixels. If scrolling is
					 * vertically it's item width in other case it"s height of item element
					 * @member ns.widget.core.VirtualListview
					 */
					self._ui = {
						scrollview: null,
						spacer: null,
						itemSize: 0
					};

					/**
					 * Holds information about scrolling state
					 * @property {Object} _scroll
					 * @property {Array} [_scroll.direction=[0,0,0,0]] Holds current direction of scrolling.
					 * Indexes suit to following order: [up, left, down, right]
					 * @property {number} [_scroll.lastPositionX=0] Last scroll position from top in pixels.
					 * @property {number} [_scroll.lastPositionY=0] Last scroll position from left in pixels.
					 * @property {number} [_scroll.lastJumpX=0] Difference between last and current
					 * position of horizontal scroll.
					 * @property {number} [_scroll.lastJumpY=0] Difference between last and current
					 * position of vertical scroll.
					 * @property {number} [_scroll.clipWidth=0] Width of clip - visible area for user.
					 * @property {number} [_scroll.clipHeight=0] Height of clip - visible area for user.
					 * @member ns.widget.core.VirtualListview
					 */
					self._scroll = {
						direction: [0, 0, 0, 0],
						lastPositionX: 0,
						lastPositionY: 0,
						lastJumpX: 0,
						lastJumpY: 0,
						clipWidth: 0,
						clipHeight: 0
					};

					/**
					 * Name of widget
					 * @property {string} name
					 * @member ns.widget.core.VirtualListview
					 * @static
					 */
					self.name = "VirtualListview";

					/**
					 * Current zero-based index of data set.
					 * @property {number} _currentIndex
					 * @member ns.widget.core.VirtualListview
					 * @protected
					 */
					self._currentIndex = 0;

					/**
					 * VirtualListview widget options.
					 * @property {Object} options
					 * @property {number} [options.bufferSize=100] Number of items of result set. The default value is 100.
					 * As the value gets higher, the loading time increases while the system performance
					 * improves. So you need to pick a value that provides the best performance
					 * without excessive loading time. It's recomended to set bufferSize at least 3 times bigger than number
					 * of visible elements.
					 * @property {number} [options.dataLength=0] Total number of items.
					 * @property {string} [options.orientation="y"] Scrolling orientation. Default vertical scrolling enabled.
					 * @property {Object} options.listItemUpdater Holds reference to method which modifies list item, depended
					 * at specified index from database. **Method should be overridden by developer using
					 * {@link ns.widget.core.VirtualListview#setListItemUpdater} method.** or defined as a config
					 * object. Method takes two parameters:
					 *  -  element {HTMLElement} List item to be modified
					 *  -  index {number} Index of data set
					 * @member ns.widget.core.VirtualListview
					 */
					self.options = {
						bufferSize: 100,
						dataLength: 0,
						orientation: VERTICAL,
						listItemUpdater: null,
						scrollElement: null
					};

					/**
					 * Binding for scroll event listener.
					 * @method _scrollEventBound
					 * @member ns.widget.core.VirtualListview
					 * @protected
					 */
					self._scrollEventBound = null;
					/**
					 * Binding for touch start event listener.
					 * @method _touchStartEventBound
					 * @member ns.widget.core.VirtualListview
					 * @protected
					 */
					self._touchStartEventBound = null;

					return self;
				},
				POINTER_START = 'vmousedown',
				POINTER_MOVE = 'vmousemove',
				POINTER_END = 'vmouseup',

			// Cached prototype for better minification
				prototype = new BaseWidget();

			/**
			 * Dictionary object containing commonly used wiget classes
			 * @property {Object} classes
			 * @static
			 * @readonly
			 * @member ns.widget.core.VirtualListview
			 */
			VirtualListview.classes = {
				uiVirtualListContainer: "ui-virtual-list-container",
				uiListviewActive: "ui-listview-active"
			};

			/**
			 * Remove highlight from items.
			 * @method _removeHighlight
			 * @param {ns.widget.core.VirtualListview} self Reference to VirtualListview object.
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			function _removeHighlight (self) {
				var children = self.element.children,
					i = children.length;
				while (--i >= 0) {
					children[i].classList.remove(VirtualListview.classes.uiListviewActive);
				}
			}

			/**
			 * Checks if tap meet the condition.
			 * @method _tapHandler
			 * @param {ns.widget.core.VirtualListview} self Reference to VirtualListview object.
			 * @param {Event} event Received Touch event
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			function _tapHandler (self, event) {
				var changedTouches = event.changedTouches ||
						(event._originalEvent &&
							event._originalEvent.changedTouches),
					eventTouch = (changedTouches && changedTouches.length) ?
						changedTouches[0] :
						event;

				if (event.type === POINTER_MOVE) {
					if (Math.abs(lastTouchPos.clientX - eventTouch.clientX) > 10 && Math.abs(lastTouchPos.clientY - eventTouch.clientY) > 10) {
						_removeHighlight(self);
						window.clearTimeout(timeoutHandler);
					}
				} else {
					_removeHighlight(self);
					window.clearTimeout(timeoutHandler);
				}

			}

			/**
			 * Adds highlight
			 * @method tapholdListener
			 * @param {ns.widget.core.VirtualListview} self Reference to VirtualListview object.
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			function tapholdListener(self) {
				var liElement;

				liElement = origTarget.tagName === "LI" ? origTarget : origTarget.parentNode;

				origTarget.removeEventListener(POINTER_MOVE, tapHandlerBound, false);
				origTarget.removeEventListener(POINTER_END, tapHandlerBound, false);
				tapHandlerBound = null;

				_removeHighlight(self);
				liElement.classList.add(VirtualListview.classes.uiListviewActive);
				lastTouchPos = {};
			}

			/**
			 * Binds touching events to examine tap event.
			 * @method _touchStartHandler
			 * @param {ns.widget.core.VirtualListview} self Reference to VirtualListview object.
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			function _touchStartHandler (self, event) {
				var eventData;

				origTarget = event.target;

				// Clean up
				window.clearTimeout(timeoutHandler);
				origTarget.removeEventListener(POINTER_MOVE, tapHandlerBound, false);
				origTarget.removeEventListener(POINTER_END, tapHandlerBound, false);

				timeoutHandler = window.setTimeout(tapholdListener.bind(null, self), tapholdThreshold);
				eventData = (event.touches && event.touches.length) ? event.touches[0] : event;
				lastTouchPos.clientX = eventData.clientX;
				lastTouchPos.clientY = eventData.clientY;

				//Add touch listeners
				tapHandlerBound = _tapHandler.bind(null, self);
				origTarget.addEventListener(POINTER_MOVE, tapHandlerBound, false);
				origTarget.addEventListener(POINTER_END, tapHandlerBound, false);

			}


			/**
			 * Updates scroll information about position, direction and jump size.
			 * @method _updateScrollInfo
			 * @param {ns.widget.core.VirtualListview} self VirtualListview widget reference
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			function _updateScrollInfo(self) {
				var scrollInfo = self._scroll,
					scrollDirection = scrollInfo.direction,
					scrollViewElement = self._ui.scrollview,
					scrollLastPositionX = scrollInfo.lastPositionX,
					scrollLastPositionY = scrollInfo.lastPositionY,
					scrollviewPosX = scrollViewElement.scrollLeft,
					scrollviewPosY = scrollViewElement.scrollTop;

				self._refreshScrollbar();
				//Reset scroll matrix
				scrollDirection = [0, 0, 0, 0];

				//Scrolling UP
				if (scrollviewPosY < scrollLastPositionY) {
					scrollDirection[SCROLL_UP] = 1;
				}

				//Scrolling RIGHT
				if (scrollviewPosX < scrollLastPositionX) {
					scrollDirection[SCROLL_RIGHT] = 1;
				}

				//Scrolling DOWN
				if (scrollviewPosY > scrollLastPositionY) {
					scrollDirection[SCROLL_DOWN] = 1;
				}

				//Scrolling LEFT
				if (scrollviewPosX > scrollLastPositionX) {
					scrollDirection[SCROLL_LEFT] = 1;
				}

				scrollInfo.lastJumpY = Math.abs(scrollviewPosY - scrollLastPositionY);
				scrollInfo.lastJumpX = Math.abs(scrollviewPosX - scrollLastPositionX);
				scrollInfo.lastPositionX = scrollviewPosX;
				scrollInfo.lastPositionY = scrollviewPosY;
				scrollInfo.direction = scrollDirection;
				scrollInfo.clipHeight = scrollViewElement.clientHeight;
				scrollInfo.clipWidth = scrollViewElement.clientWidth;
			}

			/**
			 * Computes list element size according to scrolling orientation
			 * @method _computeElementSize
			 * @param {HTMLElement} element Element whose size should be computed
			 * @param {string} orientation Scrolling orientation
			 * @return {number} Size of element in pixels
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			function _computeElementSize(element, orientation) {
				// @TODO change to util method if it will work perfectly
				return parseInt(orientation === VERTICAL ? element.clientHeight : element.clientWidth, 10) + 1;
			}

			/**
			 * Scrolls and manipulates DOM element to destination index. Element at destination
			 * index is the first visible element on the screen. Destination index can
			 * be different from Virtual List's current index, because current index points
			 * to first element in the buffer.
			 * @member ns.widget.core.VirtualListview
			 * @param {ns.widget.core.VirtualListview} self VirtualListview widget reference
			 * @param {number} toIndex Destination index.
			 * @method _orderElementsByIndex
			 * @private
			 * @static
			 */
			function _orderElementsByIndex(self, toIndex) {
				var element = self.element,
					options = self.options,
					scrollInfo = self._scroll,
					scrollClipSize = 0,
					dataLength = options.dataLength,
					indexCorrection = 0,
					bufferedElements = 0,
					avgListItemSize = 0,
					bufferSize = options.bufferSize,
					i,
					offset = 0,
					index,
					isLastBuffer = false;

				//Get size of scroll clip depended on scroll direction
				scrollClipSize = options.orientation === VERTICAL ? scrollInfo.clipHeight : scrollInfo.clipWidth;

				//Compute average list item size
				avgListItemSize = _computeElementSize(element, options.orientation) / bufferSize;

				//Compute average number of elements in each buffer (before and after clip)
				bufferedElements = Math.floor((bufferSize - Math.floor(scrollClipSize / avgListItemSize)) / 2);

				if (toIndex - bufferedElements <= 0) {
					index = 0;
					indexCorrection = 0;
				} else {
					index = toIndex - bufferedElements;
				}

				if (index + bufferSize >= dataLength) {
					index = dataLength - bufferSize;
					if (index < 0) {
						index = 0;
					}
					isLastBuffer = true;
				}
				indexCorrection = toIndex - index;

				self._loadData(index);
				blockEvent = true;
				offset = index * avgListItemSize;
				if (options.orientation === VERTICAL) {
					if (isLastBuffer) {
						offset = self._ui.spacer.clientHeight;
					}
					element.style.top = offset + "px";
				} else {
					if (isLastBuffer) {
						offset = self._ui.spacer.clientWidth;
					}
					element.style.left = offset + "px";
				}

				for (i = 0; i < indexCorrection; i += 1) {
					offset += _computeElementSize(element.children[i], options.orientation);
				}

				if (options.orientation === VERTICAL) {
					//MOBILE: self._ui.scrollview.element.scrollTop = offset;
					self._ui.scrollview.scrollTop = offset;
				} else {
					//MOBILE: self._ui.scrollview.element.scrollLeft = offset;
					self._ui.scrollview.scrollLeft = offset;
				}
				blockEvent = false;
				self._currentIndex = index;
			}

			/**
			 * Orders elements. Controls resultset visibility and does DOM manipulation. This
			 * method is used during normal scrolling.
			 * @method _orderElements
			 * @param {ns.widget.core.VirtualListview} self VirtualListview widget reference
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			function _orderElements(self) {
				var element = self.element,
					scrollInfo = self._scroll,
					options = self.options,
					elementStyle = element.style,
				//Current index of data, first element of resultset
					currentIndex = self._currentIndex,
				//Number of items in resultset
					bufferSize = parseInt(options.bufferSize, 10),
				//Total number of items
					dataLength = options.dataLength,
				//Array of scroll direction
					scrollDirection = scrollInfo.direction,
					scrolledVertically = (scrollDirection[SCROLL_UP] || scrollDirection[SCROLL_DOWN]),
					scrolledHorizontally = (scrollDirection[SCROLL_LEFT] || scrollDirection[SCROLL_RIGHT]),
					scrollClipWidth = scrollInfo.clipWidth,
					scrollClipHeight = scrollInfo.clipHeight,
					scrollLastPositionY = scrollInfo.lastPositionY,
					scrollLastPositionX = scrollInfo.lastPositionX,
					elementPositionTop = parseInt(elementStyle.top, 10) || 0,
					elementPositionLeft = parseInt(elementStyle.left, 10) || 0,
					elementsToLoad = 0,
					bufferToLoad = 0,
					elementsLeftToLoad = 0,
					temporaryElement = null,
					avgListItemSize = 0,
					resultsetSize = 0,
					childrenNodes,
					i = 0,
					jump = 0,
					hiddenPart = 0,
					direction,
					newPosition;


				childrenNodes = element.children;
				for (i = childrenNodes.length - 1; i > 0; i -= 1) {
					if (options.orientation === VERTICAL) {
						resultsetSize += childrenNodes[i].clientHeight;
					} else {
						resultsetSize += childrenNodes[i].clientWidth;
					}
				}

				//Compute average list item size
				avgListItemSize = _computeElementSize(element, options.orientation) / bufferSize;

				//Compute hidden part of result set and number of elements, that needed to be loaded, while user is scrolling DOWN
				if (scrollDirection[SCROLL_DOWN]) {
					hiddenPart = scrollLastPositionY - elementPositionTop;
					elementsLeftToLoad = dataLength - currentIndex - bufferSize;
				}

				//Compute hidden part of result set and number of elements, that needed to be loaded, while user is scrolling UP
				if (scrollDirection[SCROLL_UP]) {
					hiddenPart = (elementPositionTop + resultsetSize) - (scrollLastPositionY + scrollClipHeight);
					elementsLeftToLoad = currentIndex;
				}

				//Compute hidden part of result set and number of elements, that needed to be loaded, while user is scrolling RIGHT
				if (scrollDirection[SCROLL_RIGHT]) {
					hiddenPart = scrollLastPositionX - elementPositionLeft;
					elementsLeftToLoad = dataLength - currentIndex - bufferSize;
				}

				//Compute hidden part of result set and number of elements, that needed to be loaded, while user is scrolling LEFT
				if (scrollDirection[SCROLL_LEFT]) {
					hiddenPart = (elementPositionLeft + resultsetSize) - (scrollLastPositionX - scrollClipWidth);
					elementsLeftToLoad = currentIndex;
				}

				//manipulate DOM only, when at least 2/3 of result set is hidden
				//NOTE: Result Set should be at least 3x bigger then clip size
				if (hiddenPart > 0 && (resultsetSize / hiddenPart) <= 1.5) {

					//Left half of hidden elements still hidden/cached
					elementsToLoad = Math.floor(hiddenPart / avgListItemSize) - Math.floor((bufferSize - scrollClipHeight / avgListItemSize) / 2);
					elementsToLoad = elementsLeftToLoad < elementsToLoad ? elementsLeftToLoad : elementsToLoad;
					bufferToLoad = Math.floor(elementsToLoad / bufferSize);
					elementsToLoad = elementsToLoad % bufferSize;

					// Scrolling more then buffer
					if (bufferToLoad > 0) {
						if (scrollDirection[SCROLL_DOWN] || scrollDirection[SCROLL_RIGHT]) {
							direction = 1;
						}

						if (scrollDirection[SCROLL_UP] || scrollDirection[SCROLL_LEFT]) {
							direction = -1;
						}

						// Load data to buffer according to jumped index
						self._loadData(currentIndex + direction * bufferToLoad * bufferSize);

						// Refresh current index after buffer jump
						currentIndex = self._currentIndex;

						jump += direction * bufferToLoad * bufferSize * avgListItemSize;
					}

					if (scrollDirection[SCROLL_DOWN] || scrollDirection[SCROLL_RIGHT]) {
						//Switch currentIndex to last
						currentIndex = currentIndex + bufferSize - 1;
					}
					for (i = elementsToLoad; i > 0; i -= 1) {
						if (scrollDirection[SCROLL_DOWN] || scrollDirection[SCROLL_RIGHT]) {
							temporaryElement = element.appendChild(element.firstElementChild);
							++currentIndex;

							//Updates list item using template
							self._updateListItem(temporaryElement, currentIndex);
							jump += temporaryElement.clientHeight;
						}

						if (scrollDirection[SCROLL_UP] || scrollDirection[SCROLL_LEFT]) {
							temporaryElement = element.insertBefore(element.lastElementChild, element.firstElementChild);
							--currentIndex;

							//Updates list item using template
							self._updateListItem(temporaryElement, currentIndex);
							jump -= temporaryElement.clientHeight;
						}
					}
					if (scrolledVertically) {
						newPosition = elementPositionTop + jump;

						if (newPosition < 0 || currentIndex <= 0) {
							newPosition = 0;
							currentIndex = 0;
						}

						if (currentIndex >= (dataLength - 1)) {
							newPosition = self._ui.spacer.clientHeight;
						}

						elementStyle.top = newPosition + "px";
					}

					if (scrolledHorizontally) {
						newPosition = elementPositionLeft + jump;

						if (newPosition < 0 || currentIndex <= 0) {
							newPosition = 0;
						}

						if (currentIndex >= (dataLength - 1)) {
							newPosition = self._ui.spacer.clientWidth;
						}

						elementStyle.left = newPosition + "px";
					}

					if (scrollDirection[SCROLL_DOWN] || scrollDirection[SCROLL_RIGHT]) {
						//Switch currentIndex to first
						currentIndex = currentIndex - bufferSize + 1;
					}
					//Save current index
					self._currentIndex = currentIndex;
				}
			}

			/**
			 * Check if scrolling position is changed and updates list if it needed.
			 * @method _updateList
			 * @param {ns.widget.core.VirtualListview} self VirtualListview widget reference
			 * @member ns.widget.core.VirtualListview
			 * @private
			 * @static
			 */
			function _updateList(self) {
				var _scroll = self._scroll;
				_updateScrollInfo.call(null, self);
				if (_scroll.lastJumpY > 0 || _scroll.lastJumpX > 0) {
					if (!blockEvent) {
						_orderElements(self);
						utilEvent.trigger(self.element, "vlistupdate");
					}
				}
			}

			/**
			 * Updates list item using user defined listItemUpdater function.
			 * @method _updateListItem
			 * @param {HTMLElement} element List element to update
			 * @param {number} index Data row index
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._updateListItem = function (element, index) {
				this.options.listItemUpdater(element, index);
			};

			/**
			 * Build widget structure
			 * @method _build
			 * @param {HTMLElement} element Widget's element
			 * @return {HTMLElement} Element on which built is widget
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._build = function(element) {
				var classes = VirtualListview.classes;

				element.classList.add(classes.uiVirtualListContainer);
				return element;
			};

			prototype._setupScrollview = function (element, orientation) {
				var scrollview,
					scrollviewStyle;
				//Get scrollview instance
				scrollview = element.parentElement;
				scrollviewStyle = scrollview.style;

				if (orientation === HORIZONTAL) {
					// @TODO check if whiteSpace: nowrap is better for vertical listes
					scrollviewStyle.overflowX = "scroll";
					scrollviewStyle.overflowY = "hidden";
				} else {
					scrollviewStyle.overflowX = "hidden";
					scrollviewStyle.overflowY = "scroll";
				}

				return scrollview;
			};

			/**
			 * Initialize widget on an element.
			 * @method _init
			 * @param {HTMLElement} element Widget's element
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._init = function(element) {
				var self = this,
					ui = self._ui,
					options = self.options,
					orientation,
					scrollview,
					scrollviewStyle,
					spacer,
					spacerStyle;

				//Prepare element
				element.style.position = "relative";

				//Set orientation, default vertical scrolling is allowed
				orientation = options.orientation.toLowerCase() === HORIZONTAL ? HORIZONTAL : VERTICAL;
				if (options.scrollElement) {
					if (typeof options.scrollElement === "string") {
						scrollview = selectors.getClosestBySelector(element, "." + options.scrollElement);
					} else {
						scrollview = options.scrollElement;
					}
				}
				if(!scrollview) {
					scrollview = self._setupScrollview(element, orientation);
				}

				// Prepare spacer (element which makes scrollbar proper size)
				spacer = document.createElement("div");
				spacerStyle = spacer.style;
				spacerStyle.display = "block";
				spacerStyle.position = "static";
				if (orientation === HORIZONTAL) {
					spacerStyle.float = "left";
				}
				element.parentNode.appendChild(spacer);

				if (options.dataLength < options.bufferSize) {
					options.bufferSize = options.dataLength;
				}

				if (options.bufferSize < 1) {
					options.bufferSize = 1;
				}

				// Assign variables to members
				ui.spacer = spacer;
				ui.scrollview = scrollview;
				self.element = element;
				options.orientation = orientation;
			};

			/**
			 * Builds Virtual List structure
			 * @method _buildList
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._buildList = function() {
				var self = this,
					listItem,
					list = self.element,
					options = self.options,
					childElementType = (list.tagName === "UL" || list.tagName === "OL") ? "li" : "div",
					numberOfItems = options.bufferSize,
					documentFragment = document.createDocumentFragment(),
					touchStartEventBound = _touchStartHandler.bind(null, self),
					orientation = options.orientation,
					i;

				for (i = 0; i < numberOfItems; ++i) {
					listItem = document.createElement(childElementType);

					if (orientation === HORIZONTAL) {
						// TODO: check if whiteSpace: nowrap is better for vertical listes
						// NOTE: after rebuild this condition check possible duplication from _init method
						listItem.style.float = "left";
					}

					self._updateListItem(listItem, i);
					documentFragment.appendChild(listItem);
					listItem.addEventListener(POINTER_START, touchStartEventBound, false);
				}

				list.appendChild(documentFragment);
				this._touchStartEventBound = touchStartEventBound;
				this._refresh();
			};

			/**
			 * Refresh list
			 * @method _refresh
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._refresh = function() {
				//Set default value of variable create
				this._refreshScrollbar();
			};

			/**
			 * Loads data from specified index to result set.
			 * @method _loadData
			 * @param {number} index Index of first row
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._loadData = function(index) {
				var self = this,
					children = self.element.firstElementChild;

				if (self._currentIndex !== index) {
					self._currentIndex = index;
					do {
						self._updateListItem(children, index);
						++index;
						children = children.nextElementSibling;
					} while (children);
				}
			};

			/**
			 * Sets proper scrollbar size: height (vertical), width (horizontal)
			 * @method _refreshScrollbar
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._refreshScrollbar = function() {
				var self = this,
					element = self.element,
					options = self.options,
					ui = self._ui,
					spacerStyle = ui.spacer.style,
					bufferSizePx;

				if (options.orientation === VERTICAL) {
					//Note: element.clientHeight is variable
					bufferSizePx = parseFloat(element.clientHeight) || 0;
					spacerStyle.height = (bufferSizePx / options.bufferSize * options.dataLength - bufferSizePx) + "px";
				} else {
					//Note: element.clientWidth is variable
					bufferSizePx = parseFloat(element.clientWidth) || 0;
					spacerStyle.width = (bufferSizePx / options.bufferSize * (options.dataLength - 1) - 4 / 3 * bufferSizePx) + "px";
				}
			};

			/**
			 * Binds VirtualListview events
			 * @method _bindEvents
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._bindEvents = function() {
				var scrollEventBound = _updateList.bind(null, this),
					//MOBILE: scrollviewClip = self._ui.scrollview && self._ui.scrollview.element;
					scrollviewClip = this._ui.scrollview;

				if (scrollviewClip) {
					scrollviewClip.addEventListener("scroll", scrollEventBound, false);
					this._scrollEventBound = scrollEventBound;
				}

				//MOBILE: parent_bindEvents.call(self, self.element);
			};

			/**
			 * Cleans widget's resources
			 * @method _destroy
			 * @member ns.widget.core.VirtualListview
			 * @protected
			 */
			prototype._destroy = function() {
				var self = this,
					scrollviewClip = self._ui.scrollview,
					uiSpacer = self._ui.spacer,
					element = self.element,
					elementStyle = element.style,
					listItem;

				// Restore start position
				elementStyle.position = "static";
				if (self.options.orientation === VERTICAL) {
					elementStyle.top = "auto";
				} else {
					elementStyle.left = "auto";
				}

				if (scrollviewClip) {
					scrollviewClip.removeEventListener("scroll", self._scrollEventBound, false);
				}

				//Remove spacer element
				if (uiSpacer.parentNode) {
					uiSpacer.parentNode.removeChild(uiSpacer);
				}

				//Remove li elements.
				while (element.firstElementChild) {
					listItem = element.firstElementChild;
					listItem.removeEventListener(POINTER_START, self._touchStartEventBound, false);
					element.removeChild(listItem);
				}

			};

			/**
			 * This method scrolls list to defined position in pixels.
			 * @method scrollTo
			 * @param {number} position Scroll position expressed in pixels.
			 * @member ns.widget.core.VirtualListview
			 */
			prototype.scrollTo = function(position) {
				this._ui.scrollview.scrollTop = position;
			};

			/**
			 * This method scrolls list to defined index.
			 * @method scrollToIndex
			 * @param {number} index Scroll Destination index.
			 * @member ns.widget.core.VirtualListview
			 */
			prototype.scrollToIndex = function(index) {
				if (index < 0) {
					index = 0;
				}
				if (index >= this.options.dataLength) {
					index = this.options.dataLength - 1;
				}
				_updateScrollInfo(this);
				_orderElementsByIndex(this, index);
			};

			/**
			 * This method builds widget and trigger event "draw".
			 * @method draw
			 * @member ns.widget.core.VirtualListview
			 */
			prototype.draw = function() {
				this._buildList();
				this.trigger("draw");
			};

			/**
			 * This method sets list item updater function.
			 * To learn how to create list item updater function please
			 * visit Virtual List User Guide.
			 * @method setListItemUpdater
			 * @param {Object} updateFunction Function reference.
			 * @member ns.widget.core.VirtualListview
			 */
			prototype.setListItemUpdater = function(updateFunction) {
				this.options.listItemUpdater = updateFunction;
			};

			// Assign prototype
			VirtualListview.prototype = prototype;
			ns.widget.core.VirtualListview = VirtualListview;

			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, white: true, plusplus: true*/
/**
 *#Virtual ListView Widget
 * Shows a list view for large amounts of data.
 *
 * In the Web environment, it is challenging to display a large amount of data in a list, such as
 * displaying a contact list of over 1000 list items. It takes time to display the entire list in
 * HTML and the DOM manipulation is complex.
 *
 * The virtual list widget is used to display a list of unlimited data elements on the screen
 * for better performance. This widget provides easy access to databases to retrieve and display data.
 * It based on **result set** which is fixed size defined by developer by data-row attribute. Result
 * set should be **at least 3 times bigger** then size of clip (number of visible elements).
 *
 * To add a virtual list widget to the application follow these steps:
 *
 * ##Create widget container - list element
 *

   &lt;ul id=&quot;vlist&quot; class=&quot;ui-listview ui-virtuallistview&quot;&gt;&lt;/ul&gt;

 *
 * ##Initialize widget
 *
	// Get HTML Element reference
	var elList = document.getElementById("vlist"),
		// Set up config. All config options can be found in virtual list reference
		vListConfig = {
		dataLength: 2000,
		bufferSize: 40,
		listItemUpdater: function(elListItem, newIndex){
			// NOTE: JSON_DATA is global object with all data rows.
			var data = JSON_DATA["newIndex"];
			elListItem.innerHTML = '<span class="ui-li-text-main">' +
												data.NAME + '</span>';
			}
		};
	vlist = tau.widget.VirtualListview(elList, vListConfig);
 *
 * More config options can be found in {@link ns.widget.wearable.VirtualListview#options}
 *
 * ##Set list item update function
 *
 * List item update function is responsible to update list element depending on data row index. If you didn’t
 * pass list item update function by config option, you have to do it using following method.
 * Otherwise you will see an empty list.
 *
 *
	vlist.setListItemUpdater(function(elListItem, newIndex){
		// NOTE: JSON_DATA is global object with all data rows.
		var data = JSON_DATA["newIndex"];
		elListItem.innerHTML = '<span class="ui-li-text-main">' +
									data.NAME + '</span>';
	});
 *
 * **Attention:** Virtual List manipulates DOM elements to be more efficient. It doesn’t remove or create list
 * elements before calling list item update function. It means that, you have to take care about list element
 * and keep it clean from custom classes an attributes, because order of li elements is volatile.
 *
 * ##Draw child elements
 * If all configuration options are set, call draw method to draw child elements and make virtual list work.
 *
	vlist.draw();
 *
 * ##Destroy Virtual List
 * It’s highly recommended to destroy widgets, when they aren’t necessary. To destroy Virtual List call destroy method.
 *
	vlist.destroy();
 *
 * ##Full working code
 *
	var page = document.getElementById("pageTestVirtualList"),
		vlist,
		// Assing data.
		JSON_DATA = [
			{NAME:"Abdelnaby, Alaa", ACTIVE:"1990 - 1994", FROM:"College - Duke", TEAM_LOGO:"../test/1_raw.jpg"},
			{NAME:"Abdul-Aziz, Zaid", ACTIVE:"1968 - 1977", FROM:"College - Iowa State", TEAM_LOGO:"../test/2_raw.jpg"}
			// A lot of records.
			// These database can be found in Gear Sample Application Winset included to Tizen SDK
			];

		page.addEventListener("pageshow", function() {
			var elList = document.getElementById("vlist");

			vlist = tau.widget.VirtualListview(elList, {
					dataLength: JSON_DATA.length,
					bufferSize: 40
			});

			// Set list item updater
			vlist.setListItemUpdater(function(elListItem, newIndex) {
				//TODO: Update listitem here
				var data =  JSON_DATA[newIndex];
				elListItem.innerHTML = '<span class="ui-li-text-main">' +
											data.NAME + '</span>';
			});
			// Draw child elements
			vlist.draw();
		});
		page.addEventListener("pagehide", function() {
			// Remove all children in the vlist
			vlist.destroy();
		});
 *
 * @class ns.widget.wearable.VirtualListview
 * @since 2.2
 * @extends ns.widget.BaseWidget
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 * @author Michał Szepielak <m.szepielak@samsung.com>
 * @author Tomasz Lukawski <t.lukawski@samsung.com>
 */
(function(document, ns) {
	
					var VirtualListview = ns.widget.core.VirtualListview,
					prototype = new VirtualListview(),
					parent_init = prototype._init;


				VirtualListview.prototype = prototype;
				ns.widget.wearable.VirtualListview = VirtualListview;

				ns.engine.defineWidget(
						"VirtualListview",
						"",
						["draw", "setListItemUpdater", "scrollTo", "scrollToIndex"],
						VirtualListview,
						"wearable"
						);
				}(window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Scroller namespace
 * Namespace contains classes and objects connected with scroller widget.
 * @class ns.widget.wearable.scroller
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 */
(function (window, ns) {
	
				ns.widget.wearable.scroller = ns.widget.wearable.scroller || {};
			}(window, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * #Effect namespace
 * Namespace with effects for scroller widget.
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @class ns.widget.wearable.scroller.effect
 */
(function (window, ns) {
	
				ns.widget.wearable.scroller.effect = ns.widget.wearable.scroller.effect || {};
			}(window, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * # Bouncing effect
 * Bouncing effect for scroller widget.
 * @class ns.widget.wearable.scroller.effect.Bouncing
 * @since 2.3
 */
(function (document, ns) {
	
				// scroller.start event trigger when user try to move scroller
			var utilsObject = ns.util.object,
				selectors = ns.util.selectors,
				Bouncing = function (scrollerElement, options) {
					var self = this;
					self._orientation = null;
					self._maxValue = null;

					self._container = null;
					self._minEffectElement = null;
					self._maxEffectElement = null;

					self.options = utilsObject.merge({}, Bouncing.defaults, {scrollEndEffectArea: ns.getConfig("scrollEndEffectArea", Bouncing.defaults.scrollEndEffectArea)});
				/**
				 * target element for bouncing effect
				 * @property {HTMLElement} targetElement
				 * @member ns.widget.wearable.scroller.effect.Bouncing
				 */
					self._targetElement = null;

					self._isShow = false;
					self._isDrag = false;
					self._isShowAnimating = false;
					self._isHideAnimating = false;

					self._create(scrollerElement, options);
				},
				endEffectAreaType = {
					content: "content",
					screen: "screen"
				},
				defaults = {
					duration: 500,
					scrollEndEffectArea : "content"
				},
				classes = {
					bouncingEffect: "ui-scrollbar-bouncing-effect",
					page: "ui-page",
					left: "ui-left",
					right: "ui-right",
					top: "ui-top",
					bottom: "ui-bottom",
					hide: "ui-hide",
					show: "ui-show"
				};

			Bouncing.defaults = defaults;

			Bouncing.prototype = {
				_create: function (scrollerElement, options) {
					var self = this;
					if( self.options.scrollEndEffectArea === endEffectAreaType.content ){
						self._container = scrollerElement;
					} else {
						self._container = selectors.getClosestByClass(scrollerElement, classes.page);
					}

					self._orientation = options.orientation;
					self._maxValue = self._getValue( options.maxScrollX, options.maxScrollY );

					self._initLayout();
				},

				_initLayout: function() {
					var self = this,
						minElement = self._minEffectElement = document.createElement("DIV"),
						maxElement = self._maxEffectElement = document.createElement("DIV"),
						className = classes.bouncingEffect;

					if ( self._orientation === ns.widget.wearable.scroller.Scroller.Orientation.HORIZONTAL ) {
						minElement.className = className + " " + classes.left;
						maxElement.className = className + " " + classes.right;
					} else {
						minElement.className = className + " " + classes.top;
						maxElement.className = className + " " + classes.bottom;
					}

					self._container.appendChild( minElement );
					self._container.appendChild( maxElement );

					minElement.addEventListener("animationEnd", this);
					minElement.addEventListener("webkitAnimationEnd", this);
					minElement.addEventListener("mozAnimationEnd", this);
					minElement.addEventListener("msAnimationEnd", this);
					minElement.addEventListener("oAnimationEnd", this);

					maxElement.addEventListener("animationEnd", this);
					maxElement.addEventListener("webkitAnimationEnd", this);
					maxElement.addEventListener("mozAnimationEnd", this);
					maxElement.addEventListener("msAnimationEnd", this);
					maxElement.addEventListener("oAnimationEnd", this);
				},

				/**
				 * ...
				 * @method drag
				 * @param x
				 * @param y
				 * @member ns.widget.wearable.scroller.effect.Bouncing
				 */
				drag: function( x, y ) {
					this._isDrag = true;
					this._checkAndShow( x, y );
				},

				/**
				 * ...
				 * @method dragEnd
				 * @member ns.widget.wearable.scroller.effect.Bouncing
				 */
				dragEnd: function() {
					var self = this;
					if ( self._isShow && !self._isShowAnimating && !self._isHideAnimating ) {
						self._beginHide();
					}

					self._isDrag = false;
				},

				/**
				 * Shows effect.
				 * @method show
				 * @member ns.widget.wearable.scroller.effect.Bouncing
				 */
				show: function() {
					var self = this;
					if ( self._targetElement ) {
						self._isShow = true;
						self._beginShow();
					}
				},

				/**
				 * Hides effect.
				 * @method hide
				 * @member ns.widget.wearable.scroller.effect.Bouncing
				 */
				hide: function() {
					var self = this;
					if ( self._isShow ) {
						self._minEffectElement.style.display = "none";
						self._maxEffectElement.style.display = "none";
						self._targetElement.classList.remove(classes.hide);
						self._targetElement.classList.remove(classes.show);
					}
					self._isShow = false;
					self._isShowAnimating = false;
					self._isHideAnimating = false;
					self._targetElement = null;
				},

				_checkAndShow: function( x, y ) {
					var self = this,
						val = self._getValue(x, y);
					if ( !self._isShow ) {
						if ( val >= 0 ) {
							self._targetElement = self._minEffectElement;
							self.show();
						} else if ( val <= self._maxValue ) {
							self._targetElement = self._maxEffectElement;
							self.show();
						}

					} else if ( self._isShow && !self._isDrag && !self._isShowAnimating && !self._isHideAnimating ) {
						self._beginHide();
					}
				},

				_getValue: function(x, y) {
					return this._orientation === ns.widget.wearable.scroller.Scroller.Orientation.HORIZONTAL ? x : y;
				},

				_beginShow: function() {
					var self = this;
					if ( !self._targetElement || self._isShowAnimating ) {
						return;
					}

					self._targetElement.style.display = "block";

					self._targetElement.classList.remove(classes.hide);
					self._targetElement.classList.add(classes.show);

					self._isShowAnimating = true;
					self._isHideAnimating = false;
				},

				_finishShow: function() {
					var self = this;
					self._isShowAnimating = false;
					if ( !self._isDrag ) {
						self._targetElement.classList.remove(classes.show);
						self._beginHide();
					}
				},

				_beginHide: function() {
					var self = this;
					if ( self._isHideAnimating ) {
						return;
					}

					self._targetElement.classList.remove(classes.show);
					self._targetElement.classList.add(classes.hide);

					self._isHideAnimating = true;
					self._isShowAnimating = false;
				},

				_finishHide: function() {
					var self = this;
					self._isHideAnimating = false;
					self._targetElement.classList.remove(classes.hide);
					self.hide();
					self._checkAndShow();
				},

				/**
				 * Supports events.
				 * @method handleEvent
				 * @member ns.widget.wearable.scroller.effect.Bouncing
				 */
				handleEvent: function( event ) {
					if (event.type.toLowerCase().indexOf("animationend") > -1) {
						if ( this._isShowAnimating ) {
							this._finishShow();
						} else if ( this._isHideAnimating ) {
							this._finishHide();
						}
					}
				},

				/**
				 * Destroys effect.
				 * @method destroy
				 * @member ns.widget.wearable.scroller.effect.Bouncing
				 */
				destroy: function() {
					var self = this,
						maxEffectElement = this._maxEffectElement,
						minEffectElement = this._minEffectElement;

					minEffectElement.removeEventListener("animationEnd", this);
					minEffectElement.removeEventListener("webkitAnimationEnd", this);
					minEffectElement.removeEventListener("mozAnimationEnd", this);
					minEffectElement.removeEventListener("msAnimationEnd", this);
					minEffectElement.removeEventListener("oAnimationEnd", this);

					maxEffectElement.removeEventListener("animationEnd", this);
					maxEffectElement.removeEventListener("webkitAnimationEnd", this);
					maxEffectElement.removeEventListener("mozAnimationEnd", this);
					maxEffectElement.removeEventListener("msAnimationEnd", this);
					maxEffectElement.removeEventListener("oAnimationEnd", this);

					self._container.removeChild( minEffectElement );
					self._container.removeChild( maxEffectElement );

					self._container = null;
					self._minEffectElement = null;
					self._maxEffectElement = null;
					self._targetElement = null;

					self._isShow = null;
					self._orientation = null;
					self._maxValue = null;
				}
			};

			ns.widget.wearable.scroller.effect.Bouncing = Bouncing;
			}(window.document, ns));

/*global window, define, Event, console, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * # Scroller Widget
 * Widget creates scroller on content.
 * @class ns.widget.wearable.scroller.Scroller
 * @since 2.3
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				// scroller.start event trigger when user try to move scroller
			var BaseWidget = ns.widget.BaseWidget,
				Gesture = ns.event.gesture,
				engine = ns.engine,
				utilsObject = ns.util.object,
				utilsEvents = ns.event,
				eventTrigger = utilsEvents.trigger,
				prototype = new BaseWidget(),
				EffectBouncing = ns.widget.wearable.scroller.effect.Bouncing,
				eventType = {
					/**
					 * event trigger when scroller start
					 * @event scrollstart
					 */
					START: "scrollstart",
					/**
					 * event trigger when scroller move
					 * @event scrollmove
					 */
					MOVE: "scrollmove",
					/**
					 * event trigger when scroller end
					 * @event scrollend
					 */
					END: "scrollend",
					/**
					 * event trigger when scroll is cancel
					 * @event scrollcancel
					 */
					CANCEL: "scrollcancel"
				},

				/*
				 * this option is related operation of scroll bar.
				 * the value is true, scroll bar is shown during touching screen even if content doesn't scroll.
				 * the value is false, scroll bar disappear when there is no movement of the scroll bar.
				 */
				_keepShowingScrollbarOnTouch = false,

				Scroller = function () {
				};

			Scroller.Orientation = {
				VERTICAL: "vertical",
				HORIZONTAL: "horizontal"
			};

			Scroller.eventType = eventType;

			prototype._build = function (element) {
				if (element.children.length !== 1) {
					throw "scroller has only one child.";
				}

				this.scroller = element.children[0];
				this.scrollerStyle = this.scroller.style;

				this.bouncingEffect = null;
				this.scrollbar = null;

				this.scrollerWidth = 0;
				this.scrollerHeight = 0;
				this.scrollerOffsetX = 0;
				this.scrollerOffsetY = 0;

				this.maxScrollX = 0;
				this.maxScrollY = 0;

				this.startScrollerOffsetX = 0;
				this.startScrollerOffsetY = 0;

				this.orientation = null;

				this.enabled = true;
				this.scrolled = false;
				this.dragging = false;
				this.scrollCanceled = false;

				return element;
			};

			prototype._configure = function () {
				/**
				 * @property {Object} options Options for widget
				 * @property {number} [options.scrollDelay=0]
				 * @property {number} [options.threshold=10]
				 * @property {""|"bar"|"tab"} [options.scrollbar=""]
				 * @property {boolean} [options.useBouncingEffect=false]
				 * @property {"vertical"|"horizontal"} [options.orientation="vertical"]
				 * @member ns.widget.wearable.Scroller
				 */
				this.options = utilsObject.merge({}, this.options, {
					scrollDelay: 0,
					threshold: 30,
					scrollbar: "",
					useBouncingEffect: false,
					orientation: "vertical"	// vertical or horizontal,
				});
			};

			prototype._init = function () {
				var options = this.options,
					scrollerChildren = this.scroller.children,
					elementStyle = this.element.style,
					scrollerStyle = this.scroller.style,
					elementHalfWidth =  this.element.offsetWidth / 2,
					elementHalfHeight = this.element.offsetHeight / 2;

				this.orientation = this.orientation ? this.orientation :
					(options.orientation === "horizontal" ? Scroller.Orientation.HORIZONTAL : Scroller.Orientation.VERTICAL);
				this.scrollerWidth = this.scroller.offsetWidth;
				this.scrollerHeight = this.scroller.offsetHeight;

				this.maxScrollX = elementHalfWidth - this.scrollerWidth + scrollerChildren[scrollerChildren.length - 1].offsetWidth / 2;
				this.maxScrollY = elementHalfHeight - this.scrollerHeight + scrollerChildren[scrollerChildren.length - 1].offsetHeight / 2;
				this.minScrollX = elementHalfWidth - scrollerChildren[0].offsetWidth / 2;
				this.minScrollY = elementHalfHeight - scrollerChildren[0].offsetHeight / 2;

				this.scrolled = false;
				this.touching = true;
				this.scrollCanceled = false;

				if ( this.orientation === Scroller.Orientation.HORIZONTAL ) {
					this.maxScrollY = 0;
				} else {
					this.maxScrollX = 0;
				}
				elementStyle.overflow = "hidden";
				elementStyle.position = "relative";
				scrollerStyle.position = "absolute";
				scrollerStyle.top = "0px";
				scrollerStyle.left = "0px";
				scrollerStyle.width = this.scrollerWidth + "px";
				scrollerStyle.height = this.scrollerHeight + "px";
				this._initScrollbar();
				this._initBouncingEffect();
			};

			prototype._initScrollbar = function () {
				var type = this.options.scrollbar,
					scrollbarType;

				if ( type ) {
					scrollbarType = ns.widget.wearable.scroller.scrollbar.type[type];
					if ( scrollbarType ) {
						this.scrollbar = engine.instanceWidget(this.element, "ScrollBar", {
							type: scrollbarType,
							orientation: this.orientation
						});
					}
				}
			};

			prototype._initBouncingEffect = function () {
				var o = this.options;
				if ( o.useBouncingEffect ) {
					this.bouncingEffect = new EffectBouncing(this.element, {
						maxScrollX: this.maxScrollX,
						maxScrollY: this.maxScrollY,
						orientation: this.orientation
					});
				}
			};

			prototype._resetLayout = function () {
				var elementStyle = this.element.style,
					scrollerStyle = this.scrollerStyle;

				elementStyle.overflow = "";
				elementStyle.position = "";

				elementStyle.overflow = "hidden";
				elementStyle.position = "relative";

				if (scrollerStyle) {
					scrollerStyle.position = "";
					scrollerStyle.top = "";
					scrollerStyle.left = "";
					scrollerStyle.width = "";
					scrollerStyle.height = "";

					scrollerStyle["-webkit-transform"] = "";
					scrollerStyle["-moz-transition"] = "";
					scrollerStyle["-ms-transition"] = "";
					scrollerStyle["-o-transition"] = "";
					scrollerStyle["transition"] = "";
				}
			};

			prototype._bindEvents = function () {
				ns.event.enableGesture(
					this.scroller,

					new ns.event.gesture.Drag({
						threshold: this.options.threshold,
						delay: this.options.scrollDelay,
						blockVertical: this.orientation === Scroller.Orientation.HORIZONTAL,
						blockHorizontal: this.orientation === Scroller.Orientation.VERTICAL
					})
				);

				utilsEvents.on( this.scroller, "drag dragstart dragend dragcancel", this );
				window.addEventListener("resize", this);
			};

			prototype._unbindEvents = function () {
				if (this.scroller) {
					ns.event.disableGesture( this.scroller );
					utilsEvents.off( this.scroller, "drag dragstart dragend dragcancel", this );
					window.removeEventListener("resize", this);
				}
			};

			/* jshint -W086 */
			prototype.handleEvent = function (event) {
				switch (event.type) {
					case "dragstart":
						this._start( event );
						break;
					case "drag":
						this._move( event );
						break;
					case "dragend":
						this._end( event );
						break;
					case "dragcancel":
						this.cancel( event );
						break;
					case "resize":
						this.refresh();
						break;
				}
			};

			/**
			 * Set options for widget.
			 * @method setOptions
			 * @param {Object} options
			 * @member ns.widget.wearable.scroller.Scroller
			 */
			prototype.setOptions = function (options) {
				var name;
				for ( name in options ) {
					if ( options.hasOwnProperty(name) && !!options[name] ) {
						this.options[name] = options[name];
					}
				}
			};

			prototype._refresh = function () {
				this._clear();
				this._unbindEvents();
				this._init();
				this._bindEvents();
			};

			/**
			 * Scrolls to new position.
			 * @method scrollTo
			 * @param x
			 * @param y
			 * @param duration
			 * @member ns.widget.wearable.scroller.Scroller
			 */
			prototype.scrollTo = function (x, y, duration) {
				this._translate(x, y, duration);
				this._translateScrollbar(x, y, duration);
			};

			prototype._translate = function (x, y, duration) {
				var translate,
					transition = {
						normal: "none",
						webkit: "none",
						moz: "none",
						ms: "none",
						o: "none"
					},
					scrollerStyle = this.scrollerStyle;

				if (duration) {
					transition.normal = "transform " + duration / 1000 + "s ease-out";
					transition.webkit = "-webkit-transform " + duration / 1000 + "s ease-out";
					transition.moz = "-moz-transform " + duration / 1000 + "s ease-out";
					transition.ms = "-ms-transform " + duration / 1000 + "s ease-out";
					transition.o = "-o-transform " + duration / 1000 + "s ease-out";
				}
				translate = "translate3d(" + x + "px," + y + "px, 0)";

				scrollerStyle["-webkit-transform"] =
						scrollerStyle["-moz-transform"] =
						scrollerStyle["-ms-transform"] =
						scrollerStyle["-o-transform"] =
						scrollerStyle.transform = translate;
				scrollerStyle.transition = transition.normal;
				scrollerStyle["-webkit-transition"] = transition.webkit;
				scrollerStyle["-moz-transition"] = transition.moz;
				scrollerStyle["-ms-transition"] = transition.ms;
				scrollerStyle["-o-transition"] = transition.o;

				this.scrollerOffsetX = window.parseInt(x, 10);
				this.scrollerOffsetY = window.parseInt(y, 10);
			};

			prototype._translateScrollbar = function (x, y, duration, autoHidden) {
				if (!this.scrollbar) {
					return;
				}

				this.scrollbar.translate(this.orientation === Scroller.Orientation.HORIZONTAL ? -x : -y, duration, autoHidden);
			};

			prototype._start = function(/* e */) {
				this.scrolled = false;
				this.dragging = true;
				this.scrollCanceled = false;
				this.startScrollerOffsetX = this.scrollerOffsetX;
				this.startScrollerOffsetY = this.scrollerOffsetY;
			};

			prototype._move = function (e, pos) {
				var newX = this.startScrollerOffsetX,
					newY = this.startScrollerOffsetY,
					autoHide = !_keepShowingScrollbarOnTouch;

				if ( !this.enabled || this.scrollCanceled || !this.dragging ) {
					return;
				}

				if ( this.orientation === Scroller.Orientation.HORIZONTAL ) {
					newX += e.detail.estimatedDeltaX;
				} else {
					newY += e.detail.estimatedDeltaY;
				}

				if ( newX > this.minScrollX || newX < this.maxScrollX ) {
					newX = newX > this.minScrollX ? this.minScrollX : this.maxScrollX;
				}
				if ( newY > this.minScrollY || newY < this.maxScrollY ) {
					newY = newY > this.minScrollY ? this.minScrollY : this.maxScrollY;
				}

				if ( newX !== this.scrollerOffsetX || newY !== this.scrollerOffsetY ) {
					if ( !this.scrolled ) {
						this._fireEvent( eventType.START );
					}
					this.scrolled = true;

					this._translate( newX, newY );
					this._translateScrollbar( newX, newY, 0, autoHide );
					// TODO to dispatch move event is too expansive. it is better to use callback.
					this._fireEvent( eventType.MOVE );

					if ( this.bouncingEffect ) {
						this.bouncingEffect.hide();
					}
				} else {
					if ( this.bouncingEffect ) {
						this.bouncingEffect.drag( newX, newY );
					}
					this._translateScrollbar( newX, newY, 0, autoHide );
				}
			};

			prototype._end = function (/* e */) {
				if ( !this.dragging ) {
					return;
				}

				// bouncing effect
				if ( this.bouncingEffect ) {
					this.bouncingEffect.dragEnd();
				}

				if ( this.scrollbar ) {
					this.scrollbar.end();
				}

				this._endScroll();
				this.dragging = false;
			};

			prototype._endScroll = function () {
				if (this.scrolled) {
					this._fireEvent(eventType.END);
				}

				this.scrolled = false;
			};

			/**
			 * Cancels scroll.
			 * @method cancel
			 * @member ns.widget.wearable.scroller.Scroller
			 */
			prototype.cancel = function () {
				this.scrollCanceled = true;

				if ( this.scrolled ) {
					this._translate( this.startScrollerOffsetX, this.startScrollerOffsetY );
					this._translateScrollbar( this.startScrollerOffsetX, this.startScrollerOffsetY );
					this._fireEvent( eventType.CANCEL );
				}

				if ( this.scrollbar ) {
					this.scrollbar.end();
				}

				this.scrolled = false;
				this.dragging = false;
			};

			prototype._fireEvent = function (eventName, detail) {
				eventTrigger( this.element, eventName, detail );
			};

			prototype._clear = function () {
				this.scrolled = false;
				this.scrollCanceled = false;

				this._resetLayout();
				this._clearScrollbar();
				this._clearBouncingEffect();
			};

			prototype._clearScrollbar = function () {
				if ( this.scrollbar ) {
					this.scrollbar.destroy();
				}
				this.scrollbar = null;
			};

			prototype._clearBouncingEffect = function () {
				if (this.bouncingEffect) {
					this.bouncingEffect.destroy();
				}
				this.bouncingEffect = null;
			};

			prototype._disable = function () {
				this.enabled = false;
			};

			prototype._enable = function () {
				this.enabled = true;
			};

			prototype._destroy = function () {
				this._clear();
				this._unbindEvents();
				this.scrollerStyle = null;
				this.scroller = null;
			};

			Scroller.prototype = prototype;

			ns.widget.wearable.scroller.Scroller = Scroller;

			engine.defineWidget(
				"Scroller",
				".scroller",
				["scrollTo", "cancel"],
				Scroller
			);
			}(window.document, ns));

/*global window, define, Event, console */
/*
* Copyright (c) 2015 Samsung Electronics Co., Ltd
*
* Licensed under the Flora License, Version 1.1 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://floralicense.org/license/
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*jslint nomen: true, plusplus: true */
/**
 * #TabIndicator Widget
 * Widget create tabs indicator.
 * @class ns.widget.wearable.TabIndicator
 * @since 2.3
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var engine = ns.engine,
				object = ns.util.object,
				TabIndicator = function() {
				this.tabSize = 0;
				this.activeIndex = 0;
				this.width = 0;
			};

			TabIndicator.EventType = {
				/**
				 * Triggered when tab is changing
				 * @event tabchange
				 * @member ns.widget.wearable.TabIndicator
				 */
				change: "tabchange"
			};

			TabIndicator.prototype = new ns.widget.BaseWidget();

			object.fastMerge(TabIndicator.prototype, {
				_init: function(element) {
					var o = this.options;

					this.width = element.offsetWidth;
					element.classList.add( o.wrapperClass );
				},

				_configure: function( ) {
					/**
					 * @property {Object} options Options for widget
					 * @property {number} [options.margin=2]
					 * @property {boolean} [options.triggerEvent=false]
					 * @property {string} [options.wrapperClass="ui-tab-indicator]
					 * @property {string} [options.itemClass="ui-tab-item"]
					 * @property {string} [options.activeClass="ui-tab-active"]
					 * @member ns.widget.wearable.TabIndicator
					 */
					this.options = {
						margin: 4,
						triggerEvent: false,
						wrapperClass: "ui-tab-indicator",
						itemClass: "ui-tab-item",
						activeClass: "ui-tab-active"
					};
				},

				_createIndicator: function() {
					var o = this.options,
						activeIndex = this.activeIndex,
						wrap = document.createDocumentFragment(),
						widthTable = [],
						margin = o.margin,
						i = 0,
						len = this.tabSize,
						width = this.width-margin*(len-1),
						std = Math.floor(width / len),
						remain = width % len,
						span, offset=0;

					for (i=0; i < len; i++) {
						widthTable[i] = std;
					}

					for ( i= Math.floor((len-remain)/2); remain > 0; i++, remain-- ) {
						widthTable[i] += 1;
					}

					for (i=0; i < len; i++) {
						span = document.createElement("span");
						span.classList.add( o.itemClass );
						span.style.width = widthTable[i] + "px";
						span.style.left = offset + "px";
						offset += widthTable[i] + margin;

						if ( i === activeIndex ) {
							span.classList.add( o.activeClass );
						}
						wrap.appendChild(span);
					}

					this.element.appendChild( wrap );
				},

				_removeIndicator: function() {
					this.element.innerHTML = "";
				},

				_fireEvent: function(eventName, detail) {
					ns.fireEvent( this.element, eventName, detail );
				},

				_refresh: function() {
					this._removeIndicator();
					this._createIndicator();
				},

				/**
				 * @method setActive
				 * @param position
				 * @member ns.widget.wearable.TabIndicator
				 */
				setActive: function ( position ) {
					var o = this.options,
						nodes = this.element.children;

					this.activeIndex = position;

					[].forEach.call(nodes, function( element ) {
						element.classList.remove( o.activeClass );
					});

					if ( position < nodes.length ) {
						nodes[position].classList.add( o.activeClass );

						if ( o.triggerEvent ) {
							this._fireEvent(TabIndicator.EventType.change, {
								active: position
							});
						}
					}
				},

				/**
				 * @method setSize
				 * @param size
				 * @member ns.widget.wearable.TabIndicator
				 */
				setSize: function( size ) {
					var needRefresh = this.tabSize !== size;

					this.tabSize = size;
					if ( needRefresh ) {
						this.refresh();
					}
				},

				_destroy: function() {
					var o = this.options;

					this._removeIndicator();

					this.element.classList.remove( o.wrapperClass );
				}
			});

			ns.widget.wearable.TabIndicator = TabIndicator;

			engine.defineWidget(
				"TabIndicator",
				".ui-tab",
				["setActive", "setSize"],
				TabIndicator
			);
			}(window.document, ns));

/*global window, define, Event, console */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * # SectionChanger Widget
 * Shows a control that you can use to scroll through multiple *section*
 * elements.
 *
 * The section changer widget provides an application architecture, which has
 * multiple sections on a page and enables scrolling through the *section* elements.
 *
 * ## Manual constructor
 *
 *      @example
 *         <div id="hasSectionchangerPage" class="ui-page">
 *             <header class="ui-header">
 *                 <h2 class="ui-title">SectionChanger</h2>
 *             </header>
 *             <div id="sectionchanger" class="ui-content">
 *                 <!--Section changer has only one child-->
 *                 <div>
 *                     <section>
 *                         <h3>LEFT1 PAGE</h3>
 *                     </section>
 *                     <section class="ui-section-active">
 *                         <h3>MAIN PAGE</h3>
 *                     </section>
 *                     <section>
 *                         <h3>RIGHT1 PAGE</h3>
 *                     </section>
 *                 </div>
 *             </div>
 *         </div>
 *         <script>
 *             (function () {
 *                 var page = document.getElementById("hasSectionchangerPage"),
 *                     element = document.getElementById("sectionchanger"),
 *                     sectionChanger;
 *
 *                 page.addEventListener("pageshow", function () {
 *                     // Create the SectionChanger object
 *                     sectionChanger = new tau.SectionChanger(element, {
 *                         circular: true,
 *                         orientation: "horizontal",
 *                         useBouncingEffect: true
 *                     });
 *                 });
 *
 *                 page.addEventListener("pagehide", function () {
 *                     // Release the object
 *                     sectionChanger.destroy();
 *                 });
 *             })();
 *         </script>
 *
 * ## Handling Events
 *
 * To handle section changer events, use the following code:
 *
 *      @example
 *         <script>
 *             (function () {
 *                 var changer = document.getElementById("sectionchanger");
 *                 changer.addEventListener("sectionchange", function (event) {
 *                     console.debug(event.detail.active + " section is active.");
 *                 });
 *             })();
 *         </script>
 *
 * @class ns.widget.wearable.SectionChanger
 * @since 2.2
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var Scroller = ns.widget.wearable.scroller.Scroller,
				Gesture = ns.event.gesture,
				engine = ns.engine,
				utilsObject = ns.util.object,
				utilsEvents = ns.event,
				eventType = ns.util.object.merge({
					/**
					 * Triggered when the section is changed.
					 * @event sectionchange
					 * @member ns.widget.wearable.SectionChanger
					 */
					CHANGE: "sectionchange"
				}, Scroller.eventType),
				classes = {
					uiSectionChanger: "ui-section-changer"
				};


			function SectionChanger() {
				this.options = {};
			}

			function calculateCustomLayout(direction, elements, lastIndex) {
				var len = lastIndex !== undefined ? lastIndex : elements.length,
					result = 0,
					i;
				for (i = 0; i < len; i++) {
					result += direction === Scroller.Orientation.HORIZONTAL ? elements[i].offsetWidth : elements[i].offsetHeight;
				}
				return result;
			}
			function calculateCenter(direction, elements, index) {
				var result = calculateCustomLayout(direction, elements, index + 1);
				result -= direction === Scroller.Orientation.HORIZONTAL ? elements[index].offsetWidth / 2 : elements[index].offsetHeight / 2;
				return result;
			}
			utilsObject.inherit(SectionChanger, Scroller, {
				_build: function (element) {

					this.tabIndicatorElement = null;
					this.tabIndicator = null;

					this.sections = null;
					this.sectionPositions = [];

					this.activeIndex = 0;
					this.beforeIndex = 0;

					this._super(element);
					element.classList.add(classes.uiSectionChanger);
					return element;
				},

				_configure : function () {
					this._super();
					/**
					 * Options for widget
					 * @property {Object} options
					 * @property {"horizontal"|"vertical"} [options.orientation="horizontal"] Sets the section changer orientation:
					 * @property {boolean} [options.circular=false] Presents the sections in a circular scroll fashion.
					 * @property {boolean} [options.useBouncingEffect=false] Shows a scroll end effect on the scroll edge.
					 * @property {string} [options.items="section"] Defines the section element selector.
					 * @property {string} [options.activeClass="ui-section-active"] Specifies the CSS classes which define the active section element. Add the specified class (ui-section-active) to a *section* element to indicate which section must be shown first. By default, the first section is shown first.
					 * @property {boolean} [options.fillContent=true] declare to section tag width to fill content or not.
					 * @member ns.widget.wearable.SectionChanger
					 */
					this.options = utilsObject.merge(this.options, {
						items: "section",
						activeClass: "ui-section-active",
						circular: false,
						animate: true,
						animateDuration: 100,
						orientation: "horizontal",
						changeThreshold: -1,
						useTab: false,
						fillContent: true
					});
				},

				_init: function (element) {
					var o = this.options,
						scroller = this.scroller,
						sectionLength, i, className;

					scroller.style.position = "absolute";
					this._sectionChangerWidth = element.offsetWidth;
					this._sectionChangerHeight = element.offsetHeight;
					this._sectionChangerHalfWidth = this._sectionChangerWidth / 2;
					this._sectionChangerHalfHeight = this._sectionChangerHeight / 2;
					this.orientation = o.orientation === "horizontal" ? Scroller.Orientation.HORIZONTAL : Scroller.Orientation.VERTICAL;

					if (o.scrollbar === "tab") {
						o.scrollbar = false;
						o.useTab = true;
					}

					this.sections = typeof o.items === "string" ?
						scroller.querySelectorAll(o.items) :
						o.items;
					sectionLength = this.sections.length;

					if (o.circular && sectionLength < 3) {
						throw "if you use circular option, you must have at least three sections.";
					}

					if (this.activeIndex >= sectionLength) {
						this.activeIndex = sectionLength - 1;
					}

					for (i = 0; i < sectionLength; i++) {
						className = this.sections[i].className;
						if (className && className.indexOf(o.activeClass) > -1) {
							this.activeIndex = i;
						}

						this.sectionPositions[i] = i;
					}

					this._prepareLayout();
					this._initLayout();
					this._super();
					this._repositionSections(true);
					this.setActiveSection(this.activeIndex);

					// set corret options values.
					if (!o.animate) {
						o.animateDuration = 0;
					}
					if (o.changeThreshold < 0) {
						o.changeThreshold = this._sectionChangerHalfWidth;
					}

					if (this.enabled && sectionLength > 1) {
						this.enable();
					} else {
						this.disable();
					}
					return element;
				},

				_prepareLayout: function () {
					var o = this.options,
						sectionLength = this.sections.length,
						width = this._sectionChangerWidth,
						height = this._sectionChangerHeight,
						orientation = this.orientation,
						scrollerStyle = this.scroller.style,
						tabHeight;

					if (o.useTab) {
						this._initTabIndicator();
						tabHeight = this.tabIndicatorElement.offsetHeight;
						height -= tabHeight;
						this._sectionChangerHalfHeight = height / 2;
						this.element.style.height = height + "px";
						this._sectionChangerHeight = height;
					}

					if (orientation === Scroller.Orientation.HORIZONTAL) {
						scrollerStyle.width = (o.fillContent ? width * sectionLength : calculateCustomLayout(orientation, this.sections)) + "px";
						scrollerStyle.height = height + "px"; //set Scroller width
					} else {
						scrollerStyle.width = width + "px"; //set Scroller width
						scrollerStyle.height = (o.fillContent ? height * sectionLength : calculateCustomLayout(orientation, this.sections)) + "px";
					}

				},

				_initLayout: function () {
					var sectionStyle = this.sections.style,
						left = 0,
						top = 0,
						i, sectionLength;

					//section element has absolute position
					for (i = 0, sectionLength = this.sections.length; i < sectionLength; i++) {
						//Each section set initialize left position
						sectionStyle = this.sections[i].style;
						sectionStyle.position = "absolute";
						if (this.options.fillContent) {
							sectionStyle.width = this._sectionChangerWidth + "px";
							sectionStyle.height = this._sectionChangerHeight + "px";
						}

						if (this.orientation === Scroller.Orientation.HORIZONTAL) {
							top = 0;
							left = calculateCustomLayout(this.orientation, this.sections, i);
						} else {
							top = calculateCustomLayout(this.orientation, this.sections, i);
							left = 0;
						}

						sectionStyle.top = top + "px";
						sectionStyle.left = left + "px";
					}

				},

				_initBouncingEffect: function () {
					var o = this.options;
					if (!o.circular) {
						this._super();
					}
				},

				_translateScrollbar: function (x, y, duration, autoHidden) {
					var offset;

					if (!this.scrollbar) {
						return;
					}

					if (this.orientation === Scroller.Orientation.HORIZONTAL) {
						offset = (-x + this.minScrollX);
					} else {
						offset = (-y + this.minScrollY);
					}

					this.scrollbar.translate(offset, duration, autoHidden);
				},

				_translateScrollbarWithPageIndex: function (pageIndex, duration) {
					var offset;

					if (!this.scrollbar) {
						return;
					}

					offset = calculateCustomLayout(this.orientation, this.sections, this.activeIndex);

					this.scrollbar.translate(offset, duration);
				},

				_initTabIndicator: function () {
					var elem = this.tabIndicatorElement = document.createElement("div");
					this.element.parentNode.insertBefore(elem, this.element);

					this.tabIndicator = new engine.instanceWidget(elem, "TabIndicator");
					this.tabIndicator.setSize(this.sections.length);
					this.tabIndicator.setActive(this.activeIndex);
					this.tabIndicatorHandler = function (e) {
						this.tabIndicator.setActive(e.detail.active);
					}.bind(this);
					this.element.addEventListener(eventType.CHANGE, this.tabIndicatorHandler, false);
				},

				_clearTabIndicator: function () {
					if (this.tabIndicator) {
						this.element.parentNode.removeChild(this.tabIndicatorElement);
						this.element.removeEventListener(eventType.CHANGE, this.tabIndicatorHandler, false);
						this.tabIndicator.destroy();
						this.tabIndicator = null;
						this.tabIndicatorElement = null;
						this.tabIndicatorHandler = null;
					}
				},

				_resetLayout: function () {
					var //scrollerStyle = this.scroller.style,
						sectionStyle = this.sections.style,
						i, sectionLength;

					//scrollerStyle.width = "";
					//scrollerStyle.height = "";
					//this.scroller || this.scroller._resetLayout();

					for (i = 0, sectionLength = this.sections.length; i < sectionLength; i++) {
						sectionStyle = this.sections[i].style;

						sectionStyle.position = "";
						sectionStyle.width = "";
						sectionStyle.height = "";
						sectionStyle.top = "";
						sectionStyle.left = "";
					}

					this._super();
				},

				_bindEvents: function () {
					this._super();

					ns.event.enableGesture(
						this.scroller,

						new ns.event.gesture.Swipe({
							orientation: this.orientation === Scroller.Orientation.HORIZONTAL ?
								Gesture.Orientation.HORIZONTAL :
								Gesture.Orientation.VERTICAL
						})
					);

					utilsEvents.on(this.scroller,
							"swipe transitionEnd webkitTransitionEnd mozTransitionEnd msTransitionEnd oTransitionEnd", this);
				},

				_unbindEvents: function () {
					this._super();

					if (this.scroller) {
						ns.event.disableGesture(this.scroller);
						utilsEvents.off(this.scroller,
							"swipe transitionEnd webkitTransitionEnd mozTransitionEnd msTransitionEnd oTransitionEnd", this);
					}
				},

				/**
				 * This method manages events.
				 * @method handleEvent
				 * @returns {Event} event
				 * @member ns.widget.wearable.SectionChanger
				 */
				handleEvent: function (event) {
					this._super(event);

					switch (event.type) {
						case "swipe":
							this._swipe(event);
							break;
						case "webkitTransitionEnd":
						case "mozTransitionEnd":
						case "msTransitionEnd":
						case "oTransitionEnd":
						case "transitionEnd":
							if (event.target === this.scroller) {
								this._endScroll();
							}
							break;
					}
				},

				_notifyChanagedSection: function (index) {
					var activeClass = this.options.activeClass,
						sectionLength = this.sections.length,
						i=0, section;

					for (i=0; i < sectionLength; i++) {
						section = this.sections[i];
						section.classList.remove(activeClass);
						if (i === this.activeIndex) {
							section.classList.add(activeClass);
						}
					}

					this._fireEvent(eventType.CHANGE, {
						active: index
					});
				},

				/**
				 * Changes the currently active section element.
				 * @method setActiveSection
				 * @param {number} index
				 * @param {number} duration For smooth scrolling,
				 * the duration parameter must be in milliseconds.
				 * @member ns.widget.wearable.SectionChanger
				 */
				setActiveSection: function (index, duration, direct) {
					var position = this.sectionPositions[ index ],
						scrollbarDuration = duration,
						oldActiveIndex = this.activeIndex,
						newX=0,
						newY= 0,
						centerX = 0,
						centerY = 0;

					if (this.orientation === Scroller.Orientation.HORIZONTAL) {
						newX = this._sectionChangerHalfWidth - calculateCenter(this.orientation, this.sections, position);
					} else {
						newY = this._sectionChangerHalfHeight - calculateCenter(this.orientation, this.sections, position);
					}

					if (this.beforeIndex - index > 1 || this.beforeIndex - index < -1) {
						scrollbarDuration = 0;
					}

					this.activeIndex = index;
					this.beforeIndex = this.activeIndex;

					if (newX !== this.scrollerOffsetX || newY !== this.scrollerOffsetY) {
						
						if (direct !== false) {
							this._fireEvent( eventType.START );
							this.scrolled = true;
						}

						this._translate(newX, newY, duration);
						this._translateScrollbarWithPageIndex(index, scrollbarDuration);
					} else {
						this._endScroll();
					}

					// notify changed section.
					if (this.activeIndex !== oldActiveIndex) {
						this._notifyChanagedSection(this.activeIndex);
					}
				},

				/**
				 * Gets the currently active section element's index.
				 * @method getActiveSectionIndex
				 * @returns {number}
				 * @member ns.widget.wearable.SectionChanger
				 */
				getActiveSectionIndex: function () {
					return this.activeIndex;
				},

				_start: function (e) {
					this._super(e);

					this.beforeIndex = this.activeIndex;
				},

				_move: function (e) {
					var changeThreshold = this.options.changeThreshold,
						delta = this.orientation === Scroller.Orientation.HORIZONTAL ? e.detail.deltaX : e.detail.deltaY,
						oldActiveIndex = this.activeIndex;

					this._super(e);

					if (!this.scrolled) {
						return;
					}

					if (delta > changeThreshold) {
						this.activeIndex = this._calculateIndex(this.beforeIndex - 1);
					} else if (delta < -changeThreshold) {
						this.activeIndex = this._calculateIndex(this.beforeIndex + 1);
					} else {
						this.activeIndex = this.beforeIndex;
					}

					// notify changed section.
					if (this.activeIndex !== oldActiveIndex) {
						this._notifyChanagedSection(this.activeIndex);
					}
				},

				_end: function (/* e */) {
					if ( this.scrollbar ) {
						this.scrollbar.end();
					}

					if (!this.enabled || this.scrollCanceled || !this.dragging) {
						return;
					}

					// bouncing effect
					if (this.bouncingEffect) {
						this.bouncingEffect.dragEnd();
					}

					this.setActiveSection(this.activeIndex, this.options.animateDuration, false);
					this.dragging = false;
				},

				_swipe: function (e) {
					var offset = e.detail.direction === Gesture.Direction.UP || e.detail.direction === Gesture.Direction.LEFT ? 1 : -1,
						newIndex = this._calculateIndex(this.beforeIndex + offset);

					if (!this.enabled || this.scrollCanceled || !this.dragging) {
						return;
					}

					// bouncing effect
					if (this.bouncingEffect) {
						this.bouncingEffect.dragEnd();
					}

					if (this.activeIndex !== newIndex) {
						this.activeIndex = newIndex;
						this._notifyChanagedSection(newIndex);
					}

					this.setActiveSection(newIndex, this.options.animateDuration, false);
					this.dragging = false;
				},

				_endScroll: function () {
					if (!this.enabled || !this.scrolled || this.scrollCanceled) {
						return;
					}

					this._repositionSections();
					this._super();
				},

				_repositionSections: function (init) {
					// if developer set circular option is true, this method used when webkitTransitionEnd event fired
					var sectionLength = this.sections.length,
						curPosition = this.sectionPositions[this.activeIndex],
						centerPosition = window.parseInt(sectionLength/2, 10),
						circular = this.options.circular,
						centerX = 0,
						centerY = 0,
						i, sectionStyle, sIdx, top, left, newX, newY;

					if (this.orientation === Scroller.Orientation.HORIZONTAL) {
						newX = -(calculateCenter(this.orientation, this.sections, (circular ? centerPosition : this.activeIndex)));
						newY = 0;
					} else {
						newX = 0;
						newY = -(calculateCenter(this.orientation, this.sections, (circular ? centerPosition : this.activeIndex)));
					}

					this._translateScrollbarWithPageIndex(this.activeIndex);

					if (init || (curPosition === 0 || curPosition === sectionLength - 1)) {

						if (this.orientation === Scroller.Orientation.HORIZONTAL) {
							centerX = this._sectionChangerHalfWidth + newX;
						} else {
							centerY = this._sectionChangerHalfHeight + newY;
						}
						this._translate(centerX, centerY);

						if (circular) {
							for (i = 0; i < sectionLength; i++) {
								sIdx = (sectionLength + this.activeIndex - centerPosition + i) % sectionLength;
								sectionStyle = this.sections[ sIdx ].style;

								this.sectionPositions[sIdx] = i;

								if (this.orientation === Scroller.Orientation.HORIZONTAL) {
									top = 0;
									left = calculateCustomLayout(this.orientation, this.sections, i);
								} else {
									top = calculateCustomLayout(this.orientation, this.sections, i);
									left = 0;
								}

								sectionStyle.top = top + "px";
								sectionStyle.left = left + "px";
							}
						}
					}
				},

				_calculateIndex: function (newIndex) {
					var sectionLength = this.sections.length;

					if (this.options.circular) {
						newIndex = (sectionLength + newIndex) % sectionLength;
					} else {
						newIndex = newIndex < 0 ? 0 : (newIndex > sectionLength - 1 ? sectionLength - 1 : newIndex);
					}

					return newIndex;
				},

				_clear: function () {
					this._clearTabIndicator();
					this._super();
					this.sectionPositions.length = 0;
				}
			});

			ns.widget.wearable.SectionChanger = SectionChanger;

			engine.defineWidget(
				"SectionChanger",
				".scroller",
				["getActiveSectionIndex", "setActiveSection"],
				SectionChanger
			);
			}(window.document, ns));

/*global window, define */
/*jslint nomen: true, plusplus: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #VirtualGrid Widget
 * Widget creates special grid which can contain big number of items.
 *
 * @class ns.widget.wearable.VirtualGrid
 * @since 2.3
 * @extends ns.widget.wearable.VirtualListview
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (window, document, ns) {
	
				/**
			 * Alias for {@link ns.widget.wearable.VirtualListview}
			 * @property {Object} VirtualList
			 * @member ns.widget.wearable.VirtualGrid
			 * @private
			 * @static
			 */
			var VirtualList = ns.widget.wearable.VirtualListview,
				/**
				 * Alias for class {@link ns.engine}
				 * @property {Object} engine
				 * @member ns.widget.wearable.VirtualGrid
				 * @private
				 * @static
				 */
				engine = ns.engine,
				/**
				 * Alias for class {@link ns.util.DOM}
				 * @property {Object} DOM
				 * @member ns.widget.wearable.VirtualGrid
				 * @private
				 * @static
				 */
				DOM = ns.util.DOM,
				/**
				 * Constans for horizontal virtual grid
				 * @property {string} HORIZONTAL="x"
				 * @private
				 * @member ns.widget.wearable.VirtualGrid
				 * @static
				 */
				HORIZONTAL = "x",
				/**
				 * Constans for vertical virtual grid
				 * @property {string} VERTICAL="y"
				 * @private
				 * @member ns.widget.wearable.VirtualGrid
				 * @static
				 */
				VERTICAL = "y",
				FOCUS_SELECTOR = "::virtualgrid",
				FOCUS_SELECTOR_PATTERN = /(::virtualgrid\((\d+)\))/gi,
				/**
				 * Alias for class VirtualGrid
				 * @method VirtualGrid
				 * @member ns.widget.wearable.VirtualGrid
				 * @private
				 * @static
				 */
				VirtualGrid = function () {
					/**
					 * Object with default options
					 * @property {Object} options
					 * @property {number} [options.bufferSize=100] Element count in buffer
					 * @property {number} [options.dataLength=0] Element count in list
					 * @property {"x"|"y"} [options.orientation="y"] Orientation : horizontal ("x"), vertical ("y")
					 * @member ns.widget.wearable.VirtualGrid
					 */
					this.options = {
						bufferSize: 100,
						dataLength: 0,
						orientation: VERTICAL,
						/**
						 * Method which modifies list item, depended at specified index from database.
						 * @method options.listItemUpdater
						 * @param {HTMLElement} element List item to be modified.
						 * @param {number} index Index of data set.
						 * @member ns.widget.wearable.VirtualGrid
						 */
						listItemUpdater: function () {
							return null;
						}
					};
					this._onFocusQuery = null;
					return this;
				},

				prototype = new VirtualList(),
				/**
				 * Alias for VirtualList prototype
				 * @property {Object} VirtualListPrototype
				 * @member ns.widget.wearable.VirtualGrid
				 * @private
				 * @static
				 */
				VirtualListPrototype = VirtualList.prototype,
				/**
				 * Alias for {@link ns.widget.wearable.VirtualListview#draw VirtualList.draw}
				 * @method parent_draw
				 * @member ns.widget.wearable.VirtualGrid
				 * @private
				 * @static
				 */
				parent_draw = VirtualListPrototype.draw,
				/**
				 * Alias for {@link ns.widget.wearable.VirtualListview#_refreshScrollbar VirtualList.\_refreshScrollbar}
				 * @method parent_refreshScrollbar
				 * @member ns.widget.wearable.VirtualGrid
				 * @private
				 * @static
				 */
				parent_refreshScrollbar = VirtualListPrototype._refreshScrollbar,
				parent_bindEvents = VirtualListPrototype._bindEvents,
				parent_destroy = VirtualListPrototype._destroy;

			/**
			 * This method draws item.
			 * @method draw
			 * @member ns.widget.wearable.VirtualGrid
			 */
			prototype.draw = function () {
				var self = this,
					element = self.element,
					ui = self._ui,
					newDiv = null,
					newDivStyle = null;

				if (self.options.orientation === HORIZONTAL) {
					newDiv = document.createElement("div");
					newDivStyle = newDiv.style;
					element.parentNode.appendChild(newDiv);
					newDiv.appendChild(element);
					newDiv.appendChild(ui.spacer);
					newDivStyle.width = "10000px";
					newDivStyle.height = "100%";
					ui.container = newDiv;
				}
				self._initListItem();
				parent_draw.call(self);
			};

			function onFocusQuery(self, event) {
				var data = event.detail,
					selector = data.selector,
					index = -1;
				if (selector.indexOf(FOCUS_SELECTOR) > -1) {
					data.selector = selector = selector.replace(FOCUS_SELECTOR_PATTERN,
							function (match, widgetMatch, indexMatch) {
						if (widgetMatch && indexMatch) {
							index = indexMatch | 0;
							return "#" + self.id + " [data-index='" + index + "']";
						}
						return match;
					});

					if (index > -1) {
						self.scrollToIndex(index);
						data.nextElement = document.querySelector(selector);
						event.preventDefault(); // consume
					}
				}
			}

			prototype._bindEvents = function (element) {
				var self = this;
				parent_bindEvents.call(self, element);
				self._onFocusQuery = onFocusQuery.bind(null, self);
				self.element.addEventListener("focusquery", self._onFocusQuery);
			};

			prototype._destroy = function (element) {
				var self = this;
				parent_destroy.call(self, element);
				self.element.removeEventListener("focusquery", self._onFocusQuery);
			};

			/**
			 * Sets proper scrollbar size: width (horizontal)
			 * @method _refreshScrollbar
			 * @protected
			 * @member ns.widget.wearable.VirtualGrid
			 */
			prototype._refreshScrollbar = function () {
				var width = 0,
					ui = this._ui;
				parent_refreshScrollbar.call(this);
				if (ui.container) {
					width = this.element.clientWidth + ui.spacer.clientWidth;
					ui.container.style.width = width + "px";
				}
			};

			/**
			 * Initializes list item
			 * @method _initListItem
			 * @protected
			 * @member ns.widget.wearable.VirtualGrid
			 */
			prototype._initListItem = function () {
				var self = this,
					thisElement = self.element,
					element = document.createElement("div"),
					rowElement = document.createElement("div"),
					elementStyle = element.style,
					orientation = self.options.orientation,
					thisElementStyle = thisElement.style,
					rowElementStyle = rowElement.style;

				elementStyle.overflow = "hidden";
				rowElement.style.overflow = "hidden";
				thisElement.appendChild(rowElement);
				rowElement.appendChild(element);
				self.options.listItemUpdater(element, 0);

				if (orientation === VERTICAL) {
					thisElementStyle.overflowY = "auto";
					thisElementStyle.overflowX = "hidden";
					rowElementStyle.overflow = "hidden";
					element.style.float = "left";
					self._cellSize = DOM.getElementWidth(element);
					self._columnsCount = Math.floor(DOM.getElementWidth(thisElement) / self._cellSize);
				} else {
					thisElementStyle.overflowX = "auto";
					thisElementStyle.overflowY = "hidden";
					rowElementStyle.overflow = "hidden";
					rowElementStyle.float = "left";
					thisElementStyle.height = "100%";
					rowElementStyle.height = "100%";
					self._cellSize = DOM.getElementHeight(element);
					self._columnsCount = Math.floor(DOM.getElementHeight(thisElement) / self._cellSize);
				}
				thisElement.removeChild(rowElement);
				self.options.originalDataLength = self.options.dataLength;
				self.options.dataLength /= self._columnsCount;
			};

			/**
			 * Updates list item with data using defined template
			 * @method _updateListItem
			 * @param {HTMLElement} element List element to update
			 * @param {number} index Data row index
			 * @protected
			 * @member ns.widget.wearable.VirtualGrid
			 */
			prototype._updateListItem = function (element, index) {
				var elementI,
					i,
					count,
					elementStyle = element.style,
					options = this.options,
					elementIStyle,
					size;
				element.innerHTML = "";
				elementStyle.overflow = "hidden";
				elementStyle.position = "relative";
				if (options.orientation === HORIZONTAL) {
					elementStyle.height = "100%";
				}
				count = this._columnsCount;
				size = (100 / count);
				for (i = 0; i < count; i++) {
					elementI = document.createElement("div");
					elementIStyle = elementI.style;
					elementIStyle.overflow = "hidden";
					elementI.setAttribute("data-index", count * index + i);

					if (options.orientation === VERTICAL) {
						elementI.style.float = "left";
						elementI.style.width = size + "%";
					} else {
						elementI.style.height = size + "%";
					}

					if (count * index + i < options.originalDataLength) {
						this.options.listItemUpdater(elementI, count * index + i, count);
					}
					element.appendChild(elementI);
				}
			};

			VirtualGrid.prototype = prototype;

			ns.widget.wearable.VirtualGrid = VirtualGrid;

			engine.defineWidget(
				"VirtualGrid",
				".ui-virtualgrid",
				[],
				VirtualGrid
			);

			}(window, window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true */
/**
 * # SnapListview Widget
 * Shows a snap list view.
 * It detects center-positioned list item when scroll end. When scroll event started, SnapListview trigger *scrollstart* event, and scroll event ended, it trigger *scrollend* event.
 * When scroll ended and it attach class to detected item.
 *
 * ## Default selectors
 *
 * Default selector for snap listview widget is class *ui-snap-listview*.
 *
 * To add a list widget to the application, use the following code:
 *
 * ### List with basic items
 *
 * You can add a basic list widget as follows:
 *
 *      @example
 *         <ul class="ui-listview ui-snap-listview">
 *             <li>1line</li>
 *             <li>2line</li>
 *             <li>3line</li>
 *             <li>4line</li>
 *             <li>5line</li>
 *         </ul>
 *
 * ## JavaScript API
 *
 * There is no JavaScript API.
 *
 * @author Heeju Joo <heeju.joo@samsung.com>
 * @class ns.widget.wearable.SnapListview
 * @extends ns.widget.BaseWidget
 */
(function(document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				/**
				 * Alias for class ns.engine
				 * @property {ns.engine} engine
				 * @member ns.widget.wearable.SnapListview
				 * @private
				 */
				engine = ns.engine,
				/**
				 * Alias for class ns.event
				 * @property {ns.event} utilEvent
				 * @member ns.widget.wearable.SnapListview
				 * @private
				 */
				utilEvent = ns.event,
				/**
				 * Alias for class ns.util.DOM
				 * @property {ns.util.DOM} doms
				 * @member ns.widget.wearable.SnapListview
				 * @private
				 */
				doms = ns.util.DOM,
				/**
				 * Alias for class ns.util.selectors
				 * @property {ns.util.selectors} utilSelector
				 * @member ns.widget.wearable.SnapListview
				 * @private
				 */
				utilSelector = ns.util.selectors,


				eventType = {
					/**
					 * Dictionary for SnapListview related events.
					 * @event scrollstart
					 * @event scrollend
					 * @event selected
					 * @member ns.widget.wearable.SnapListview
					 */
					SCROLL_START: "scrollstart",
					SCROLL_END: "scrollend",
					SELECTED: "selected"
				},

				animationTimer = null,

				SnapListview = function() {
					var self = this;

					self._ui = {
						page: null,
						scrollableParent: {
							element: null,
							height: 0
						},
						childItems: {}
					};

					self.options = {
						selector: "li:not(.ui-listview-divider)",
						animate: "none",
						scale: {
							from: 0.77,
							to: 1
						},
						opacity: {
							from: 0.7,
							to: 1
						}
					};

					self._listItems = [];
					self._callbacks = {};
					self._scrollEndTimeoutId = null;
					self._isScrollStarted = false;
					self._selectedIndex = null;
					self._enabled = true;
					self._isTouched = false;
					self._scrollEventCount = 0;
				},

				prototype = new BaseWidget(),

				CLASSES_PREFIX = "ui-snap-listview",

				classes = {
					SNAP_CONTAINER: "ui-snap-container",
					SNAP_DISABLED: "ui-snap-disabled",
					SNAP_LISTVIEW: CLASSES_PREFIX,
					SNAP_LISTVIEW_SELECTED: CLASSES_PREFIX + "-selected",
					SNAP_LISTVIEW_ITEM: CLASSES_PREFIX + "-item"
				},

				// time threshold for detect scroll end
				SCROLL_END_TIME_THRESHOLD = 150;

			SnapListview.classes = classes;

			SnapListview.ListItem = function(element, visiableOffset) {
				var offsetTop = element.offsetTop,
					height = element.offsetHeight;

				this.element = element;
				this.rate = -1;

				this.coord = {
					top: offsetTop,
					height: height
				};

				this.position = {
					begin: offsetTop - visiableOffset,
					start: offsetTop - visiableOffset + height,
					stop: offsetTop,
					end: offsetTop + height
				};

				element.classList.add(classes.SNAP_LISTVIEW_ITEM);
			};

			SnapListview.ListItem.prototype = {
				animate: function(offset, callback) {
					var element = this.element,
						p = this.position,
						begin = p.begin,
						end = p.end,
						start = p.start,
						stop = p.stop,
						rate;

					if (offset >= start && offset <= stop) {
						rate = Math.min(1, Math.abs((offset - start) / (stop - start)));
					} else if ((offset > begin && offset < start) || (offset < end && offset > stop)) {
						rate = 0;
					} else {
						rate = -1;
					}

					if (this.rate !== rate) {
						callback(element, rate);
						this.rate = rate;
					}
				}
			};

			function removeSelectedClass(self) {
				var selectedIndex = self._selectedIndex;

				if (selectedIndex !== null) {
					self._listItems[selectedIndex].element.classList.remove(classes.SNAP_LISTVIEW_SELECTED);
				}
			}

			function setSelection(self) {
				var ui = self._ui,
					listItems = self._listItems,
					scrollableParent = ui.scrollableParent,
					scrollCenter = scrollableParent.element.scrollTop + scrollableParent.height / 2,
					listItemLength = listItems.length,
					tempListItem, tempListItemCoord, i;

				for (i=0 ; i < listItemLength; i++) {
					tempListItem = listItems[i];
					tempListItemCoord = tempListItem.coord;

					if ((tempListItemCoord.top < scrollCenter) && (tempListItemCoord.top + tempListItemCoord.height >= scrollCenter)) {
						removeSelectedClass(self);
						self._selectedIndex = i;
						tempListItem.element.classList.add(classes.SNAP_LISTVIEW_SELECTED);
						utilEvent.trigger(tempListItem.element, eventType.SELECTED);
						return;
					}
				}
			}

			function listItemAnimate(self) {
				var anim = self.options.animate,
					animateCallback = self._callbacks[anim],
					scrollPosition;

				if (animateCallback) {
					scrollPosition = self._ui.scrollableParent.element.scrollTop;
					self._listItems.forEach(function(item) {
						item.animate(scrollPosition, animateCallback);
					});
				}
			}

			function scrollEndCallback(self) {
				if(self._isTouched === false) {
					self._isScrollStarted = false;
					// trigger "scrollend" event
					utilEvent.trigger(self.element, eventType.SCROLL_END);

					setSelection(self);
				}
			}

			function scrollHandler(self) {
				var callbacks = self._callbacks,
					scrollEndCallback = callbacks.scrollEnd;

				if (!self._isScrollStarted) {
					self._isScrollStarted = true;
					utilEvent.trigger(self.element, eventType.SCROLL_START);
					self._scrollEventCount = 0;
				}

				self._scrollEventCount++;

				if (self._scrollEventCount > 2 || self._isTouched === true) {
					removeSelectedClass(self);
				}

				listItemAnimate(self);

				// scrollend handler can be run only when all touches are released.
				if(self._isTouched === false) {
					window.clearTimeout(self._scrollEndTimeoutId);
					self._scrollEndTimeoutId = window.setTimeout(scrollEndCallback, SCROLL_END_TIME_THRESHOLD);
				}
			}

			function onTouchStart(self) {
				self._isTouched = true;
			}

			function onTouchEnd(self) {
				var scrollElement = self._ui.scrollableParent.element;
				self._isTouched = false;
				if (scrollElement.scrollTop === 0 || scrollElement.scrollTop === scrollElement.scrollHeight - scrollElement.offsetHeight) {
					setSelection(self);
				}
			}

			function getScrollableParent(element) {
				var overflow;

				while (element !== document.body) {
					overflow = doms.getCSSProperty(element, "overflow-y");
					if (overflow === "scroll" || (overflow === "auto" && element.scrollHeight > element.clientHeight)) {
						return element;
					}
					element = element.parentNode;
				}

				return null;
			}

			function initSnapListview(listview) {
				var self = this,
					ui = self._ui,
					options = self.options,
					listItems = [],
					scroller, visiableOffset;


				ui.page = utilSelector.getClosestByClass(listview, "ui-page") || window;
				scroller = getScrollableParent(listview) || ui.page;
				scroller.classList.add(classes.SNAP_CONTAINER);
				visiableOffset = scroller.clientHeight || ui.page.offsetHeight;

				ui.scrollableParent.element = scroller;
				ui.scrollableParent.height = visiableOffset;

				[].slice.call(listview.querySelectorAll(options.selector)).forEach(function(element) {
					listItems.push(new SnapListview.ListItem(element, visiableOffset));
				});

				self._listItems = listItems;
				listItemAnimate(self);
			}

			prototype._build = function(element) {
				if (!element.classList.contains(classes.SNAP_LISTVIEW)) {
					element.classList.add(classes.SNAP_LISTVIEW);
				}

				return element;
			};

			/**
			 * Init SnapListview
			 * @method _init
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.wearable.SnapListview
			 */
			prototype._init = function(element) {
				var self = this,
					options = this.options,
					scaleForm = options.scale.from,
					scaleTo = options.scale.to,
					opacityForm = options.opacity.from,
					opacityTo = options.opacity.to;

				self._callbacks = {
					scroll: scrollHandler.bind(null, self),
					scrollEnd: scrollEndCallback.bind(null, self),
					scale : function(listItemElement, rate) {
						var scale = 1,
							opacity = 1;

						if (rate < 0) {
							listItemElement.style.webkitTransform = "";
							listItemElement.style.opacity = "";
							return;
						}

						rate = rate > 0.5 ? 1 - rate : rate;

						scale = scaleForm + ((scaleTo - scaleForm) * rate*2);
						opacity = opacityForm + ((opacityTo - opacityForm) * rate*2);

						listItemElement.style.webkitTransform = "scale3d("+scale+","+scale+","+scale+")";
						listItemElement.style.opacity = opacity;
					}
				};

				initSnapListview.call(self, element);
				setSelection(self);

				return element;
			};

			/**
			 * Refresh structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.wearable.SnapListview
			 */
			prototype._refresh = function() {
				var self = this,
					element = self.element;

				self._unbindEvents();

				initSnapListview.call(self, element);
				setSelection(self);

				self._bindEvents();

				return null;
			};

			prototype._bindEvents = function() {
				var self = this,
					element = self.element,
					scrollableElement = self._ui.scrollableParent.element;

				self._callbacks.touchstart = onTouchStart.bind(null, self);
				self._callbacks.touchend = onTouchEnd.bind(null, self);
				if (scrollableElement) {
					utilEvent.on(scrollableElement, "scroll", this._callbacks.scroll, false);
				}
				element.addEventListener("touchstart", self._callbacks.touchstart);
				element.addEventListener("touchend", self._callbacks.touchend);
			};

			prototype._unbindEvents = function() {
				var self = this,
					element = self.element,
					scrollableElement = self._ui.scrollableParent.element;

				if (scrollableElement) {
					utilEvent.off(scrollableElement, "scroll", this._callbacks.scroll, false);
				}
				element.removeEventListener("touchstart", self._callbacks.touchstart);
				element.removeEventListener("touchend", self._callbacks.touchend);
			};

			/**
			 * Destroy widget
			 * @method _destroy
			 * @protected
			 * @member ns.widget.wearable.SnapListview
			 */
			prototype._destroy = function() {
				var self = this;

				self._unbindEvents();

				self._ui = null;
				self._callbacks = null;
				self._listItems = null;
				self._isScrollStarted = null;

				if (self._scrollEndTimeoutId) {
					window.clearTimeout(self._scrollEndTimeoutId);
				}
				self._scrollEndTimeoutId = null;
				self._selectedIndex = null;

				return null;
			};

			prototype._enable = function() {
				var self = this,
					scrollableParent = self._ui.scrollableParent.element;

				scrollableParent.classList.remove(classes.SNAP_DISABLED);
				if (!self._enabled) {
					self._enabled = true;
					self._refresh();
				}
			};

			prototype._disable = function() {
				var self = this,
					scrollableParent = self._ui.scrollableParent.element;

				scrollableParent.classList.add(classes.SNAP_DISABLED);
				self._enabled = false;
			};

			/**
			 * Get selectedIndex
			 * @method getSelectedIndex
			 * @return {number} index
			 * @public
			 * @member ns.widget.wearable.SnapListview
			 */
			prototype.getSelectedIndex = function() {
				return this._selectedIndex;
			};

			/**
			 * Scroll SnapList by index
			 * @method scrollToPosition
			 * @param {number} index
			 * @public
			 * @member ns.widget.wearable.SnapListview
			 */
			prototype.scrollToPosition = function(index) {
				var self = this,
					ui = self._ui,
					enabled = self._enabled,
					listItems = self._listItems,
					scrollableParent = ui.scrollableParent,
					listItemLength = listItems.length,
					indexItem,
					dest;

				if (!enabled || index < 0 || index >= listItemLength || self._selectedIndex === index) {
					return;
				}

				removeSelectedClass(self);
				
				indexItem = listItems[index].coord;
				dest = indexItem.top - scrollableParent.height / 2 + indexItem.height / 2;

				self._selectedIndex = index;

				if(animationTimer !== null) {
					window.cancelAnimationFrame(animationTimer);
					animationTimer = null;
				}
				scrollAnimation(scrollableParent.element, scrollableParent.element.scrollTop, dest, 450);
			};

			function cubicBezier (x1, y1, x2, y2) {
				return function (t) {
					var rp = 1 - t, rp3 = 3 * rp, p2 = t * t, p3 = p2 * t, a1 = rp3 * t * rp, a2 = rp3 * p2;
					return a1 * y1 + a2 * y2 + p3;
				};
			}

			function scrollAnimation(element, from, to, duration) {
				var easeOut = cubicBezier(0.25, 0.46, 0.45, 1),
					startTime = 0,
					currentTime = 0,
					progress = 0,
					easeProgress = 0,
					distance = to - from,
					scrollTop = element.scrollTop;

				startTime = window.performance.now();
				animationTimer = window.requestAnimationFrame(function animation() {
					var gap;
					currentTime = window.performance.now();
					progress = (currentTime - startTime) / duration;
					easeProgress = easeOut(progress);
					gap = distance * easeProgress;
					element.scrollTop = scrollTop + gap;
					if (progress <= 1 && progress >= 0) {
						animationTimer = window.requestAnimationFrame(animation);
					} else {
						animationTimer = null;
					}
				});
			}

			SnapListview.prototype = prototype;
			ns.widget.wearable.SnapListview = SnapListview;

			engine.defineWidget(
				"SnapListview",
				".ui-snap-listview",
				[],
				SnapListview,
				"wearable"
			);
			}(window.document, ns));

/*global window, define, Event, console */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * # Swipe List
 * Shows a list where you can swipe horizontally through a list item to perform a specific task.
 *
 * The swipe list widget shows on the screen a list where you can swipe horizontally through a list item to activate a specific feature or perform a specific task. For example, you can swipe a contact in a contact list to call them or to open a message editor in order to write them a message.
 *
 * The following table describes the supported swipe list options.
 *
 *      @example
 *         <div class="ui-content">
 *             <!--List items that can be swiped-->
 *             <ul class="ui-listview ui-swipelist-list">
 *                 <li>Andrew</li>
 *                 <li>Bill</li>
 *                 <li>Christina</li>
 *                 <li>Daniel</li>
 *                 <li>Edward</li>
 *                 <li>Peter</li>
 *                 <li>Sam</li>
 *                 <li>Tom</li>
 *             </ul>
 *             <!--Swipe actions-->
 *             <div class="ui-swipelist">
 *                 <div class="ui-swipelist-left">
 *                     <div class="ui-swipelist-icon"></div>
 *                     <div class="ui-swipelist-text">Calling</div>
 *                 </div>
 *                 <div class="ui-swipelist-right">
 *                     <div class="ui-swipelist-icon"></div>
 *                     <div class="ui-swipelist-text">Message</div>
 *                 </div>
 *             </div>
 *         </div>
 *         <script>
 *             (function () {
 *                 var page = document.getElementById("swipelist"),
 *                         listElement = page.getElementsByClassName("ui-swipelist-list", "ul")[0],
 *                         swipeList;
 *                 page.addEventListener("pageshow", function () {
 *                     // Make swipe list object
 *                     var options = {
 *                         left: true,
 *                         right: true
 *                     };
 *                     swipeList = new tau.widget.SwipeList(listElement, options);
 *                 });
 *                 page.addEventListener("pagehide", function () {
 *                     // Release object
 *                     swipeList.destroy();
 *                 });
 *             })();
 *         </script>
 * @class ns.widget.wearable.SwipeList
 * @since 2.2
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var Gesture = ns.event.gesture,
				utilsEvents = ns.event,
				engine = ns.engine,
				dom = ns.util.DOM,
				selectors = ns.util.selectors,

				eventType = {
					/**
					 * Triggered when a left-to-right swipe is completed.
					 * @event swipelist.left
					 * @member ns.widget.wearable.SwipeList
					 */
					LEFT: "swipelist.left",
					/**
					 * Triggered when a right-to-left swipe is completed.
					 * @event swipelist.right
					 * @member ns.widget.wearable.SwipeList
					 */
					RIGHT: "swipelist.right"
				},

				SwipeList = function () {
					/**
					 * SwipeList's container.
					 * @property {?HTMLElement} [container=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.container = null;

					/**
					 * SwipeList's element.
					 * @property {?HTMLElement} [swipeElement=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.swipeElement = null;
					/**
					 * Left element of widget.
					 * @property {?HTMLElement} [swipeLeftElement=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.swipeLeftElement = null;
					/**
					 * Right element of widget.
					 * @property {?HTMLElement} [swipeRightElement=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.swipeRightElement = null;

					/**
					 * Style of SwipeList's element.
					 * @property {?Object} [swipeElementStyle=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.swipeElementStyle = null;
					/**
					 * Style of left element of widget.
					 * @property {?Object} [swipeLeftElementStyle=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.swipeLeftElementStyle = null;
					/**
					 * Style of right element of widget.
					 * @property {?Object} [swipeRightElementStyle=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.swipeRightElementStyle = null;

					/**
					 * Active element of widget.
					 * @property {?HTMLElement} [activeElement=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.activeElement = null;
					/**
					 * Target of swipe event.
					 * @property {?HTMLElement} [activeTarget=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.activeTarget = null;

					/**
					 * Function calls on destroying.
					 * @property {?Function} [resetLayoutCallback=null]
					 * @member ns.widget.wearable.SwipeList
					 */
					this.resetLayoutCallback = null;
					this.options = {};

					this._interval = 0;

					this._cancelled = false;
					this._dragging = false;
					this._animating = false;

				},
				prototype = new ns.widget.BaseWidget(),

				blockEvent = function(event) {
					event.preventDefault();
				};

			prototype._configure = function () {

				/**
				 * Options for widget
				 * @property {Object} options
				 * @property {boolean} [options.left=false] Set to true to allow swiping from left to right.
				 * @property {boolean} [options.right=false] Set to true to allow swiping from right to left.
				 * @property {number} [options.threshold=10] Define the threshold (in pixels) for the minimum swipe movement which allows the swipe action to appear.
				 * @property {number} [options.animationThreshold=150] Define the threshold (in pixels) for the minimum swipe movement that allows a swipe animation (with a color change) to be shown. The animation threshold is usually the threshold for the next operation after the swipe.
				 * @property {number} [options.animationDuration=200] Define the swipe list animation duration. Do not change the default value, since it has been defined to show a complete color change.
				 * @property {number} [options.animationInterval=8] Define the swipe list animation interval. The animation is called with the requestAnimationFrame() method once every 1/60 seconds. The interval determines how many coordinates the animation proceeds between each call. The animation ends when the coordinates reach the value defined as animationDuration. This option basically allows you to control the speed of the animation.
				 * @property {string} [options.ltrStartColor=""] Define the start color for the left-to-right swipe.
				 * @property {string} [options.ltrEndColor=""] Define the end color for the left-to-right swipe.
				 * @property {string} [options.rtlStartColor=""] Define the start color for the right-to-left swipe.
				 * @property {string} [options.rtlEndColor=""] Define the end color for the right-to-left swipe.
				 * @property {?HTMLElement} [options.container=null] Define container of widget.
				 * @property {string} [options.swipeTarget="li"] Selector for swipe list
				 * @property {string} [options.swipeElement=".ui-swipelist"] Selector for swipe list container
				 * @property {string} [options.swipeLeftElement=".ui-swipelist-left"] Selector for swipe left container
				 * @property {string} [options.swipeRightElement=".ui-swipelist-right"] Selector for swipe right container
				 * @member ns.widget.wearable.SwipeList
				 */
				this.options = {
					threshold: 10,
					animationThreshold: 150,
					animationDuration: 200,
					animationInterval: 8,

					container: null,

					swipeTarget: "li",
					swipeElement: ".ui-swipelist",
					swipeLeftElement: ".ui-swipelist-left",
					swipeRightElement: ".ui-swipelist-right",

					ltrStartColor: "",
					ltrEndColor: "",
					rtlStartColor: "",
					rtlEndColor: ""
				};
			};

			prototype._init = function (element) {
				var page = selectors.getClosestBySelector(element, "." + ns.widget.core.Page.classes.uiPage),
					options = this.options,
					swipeLeftElementBg,
					swipeRightElementBg,
					rgbStringRgExp = /rgb\(([0-9]+), ([0-9]+), ([0-9]+)\)/g;

				if (options.container) {
					this.container = page.querySelector(options.container);
				} else {
					this.container = element.parentNode;
				}

				this.scrollableElement = selectors.getScrollableParent(element);
				if (!this.scrollableElement) {
					this.scrollableElement = this.container;
				}
				this.swipeElement = page.querySelector(options.swipeElement);
				this.swipeLeftElement = options.swipeLeftElement ? page.querySelector(options.swipeLeftElement) : undefined;
				this.swipeRightElement = options.swipeRightElement ? page.querySelector(options.swipeRightElement) : undefined;

				if (this.swipeElement) {
					this.swipeElementStyle = this.swipeElement.style;
					this.swipeElementStyle.display = "none";
					this.swipeElementStyle.background = "transparent";
					this.swipeElementStyle.width = this.scrollableElement.offsetWidth + "px";
					this.swipeElementStyle.height = this.scrollableElement.offsetHeight + "px";
				}

				if (this.swipeLeftElement) {
					this.swipeLeftElementStyle = this.swipeLeftElement.style;
					this.swipeLeftElementStyle.display = "none";
					// Get background-color value for swipe left element
					swipeLeftElementBg = this.swipeLeftElement ? dom.getCSSProperty(this.swipeLeftElement, "background-image").match(rgbStringRgExp) : undefined;
				}

				if (this.swipeRightElement) {
					this.swipeRightElementStyle = this.swipeRightElement.style;
					this.swipeRightElementStyle.display = "none";
					// Get background-color value for swipe right element
					swipeRightElementBg = this.swipeRightElement ? dom.getCSSProperty(this.swipeRightElement, "background-image").match(rgbStringRgExp) : undefined;
				}

				
				// Set start/end color: If user set color as option, that color will be used. If not, css based color of swipe will be used.
				options.ltrStartColor = options.ltrStartColor || swipeLeftElementBg[0];
				options.ltrEndColor = options.ltrEndColor || swipeLeftElementBg[1];
				options.rtlStartColor = options.rtlStartColor || swipeRightElementBg[0];
				options.rtlEndColor = options.rtlEndColor || swipeRightElementBg[1];

				this.resetLayoutCallback = null;
				if (this.swipeElement.parentNode !== this.container) {
					this.resetLayoutCallback = (function (parent, nextSibling, element) {
						return function () {
							try {
								if (nextSibling) {
									parent.insertBefore(element, nextSibling);
								} else {
									parent.appendChild(element);
								}
							} catch (e) {
								element.parentNode.removeChild(element);
							}
						};
					}(this.swipeElement.parentNode, this.swipeElement.nextElementSibling, this.swipeElement));
					this.container.appendChild(this.swipeElement);
				}
			};

			prototype._reset = function () {
				this.container.style.position = "";

				this.swipeElementStyle.display = "";
				this.swipeElementStyle.background = "";
				this.swipeElementStyle.width = "";
				this.swipeElementStyle.height = "";

				this.swipeLeftElementStyle.display = "";
				this.swipeLeftElementStyle.background = "";

				this.swipeRightElementStyle.display = "";
				this.swipeRightElementStyle.background = "";

				if (this.resetLayoutCallback) {
					this.resetLayoutCallback();
				}
				this._unbindEvents();
			};

			prototype._bindEvents = function () {

				ns.event.enableGesture(
					this.element,

					new Gesture.Drag({
						threshold: this.options.threshold,
						blockVertical: true
					}),

					new Gesture.Swipe({
						orientation: Gesture.Orientation.HORIZONTAL
					})
				);

				utilsEvents.on(this.element, "drag dragstart dragend dragcancel swipe", this);
				utilsEvents.on(document, "scroll touchcancel", this);
				utilsEvents.on(this.swipeElement, "touchstart touchmove touchend", blockEvent, false);
			};

			prototype._unbindEvents = function () {
				ns.event.disableGesture(this.element);

				utilsEvents.off(this.element, "drag dragstart dragend dragcancel swipe", this);
				utilsEvents.off(document, "scroll touchcancel", this);
				utilsEvents.off(this.swipeElement, "touchstart touchmove touchend", blockEvent, false);
			};

			prototype.handleEvent = function (event) {
				switch (event.type) {
					case "dragstart":
						this._start(event);
						break;
					case "drag":
						this._move(event);
						break;
					case "dragend":
						this._end(event);
						break;
					case "swipe":
						this._swipe(event);
						break;
					case "dragcancel":
					case "scroll":
						this._cancel();
						break;
				}
			};

			prototype._translate = function (activeElementStyle, translateX, anim) {
				var deltaX = translateX / window.innerWidth * 100,
					self = this,
					fromColor, toColor, prefix;

				if (this.swipeLeftElement && translateX >= 0) {
					// left
					fromColor = self.options.ltrStartColor;
					toColor = self.options.ltrEndColor;
					prefix = "left";
				} else if (this.swipeRightElement && translateX < 0) {
					fromColor = self.options.rtlStartColor;
					toColor = self.options.rtlEndColor;
					prefix = "right";
					deltaX = Math.abs(deltaX);
				}

				(function animate() {
					activeElementStyle.background = "-webkit-linear-gradient(" + prefix + ", " + fromColor + " 0%, " + toColor + " " + deltaX + "%)";
					if (anim && deltaX < self.options.animationDuration) {
						self._animating = true;
						deltaX += self.options.animationInterval;
						window.webkitRequestAnimationFrame(animate);
					} else if (anim && deltaX >= self.options.animationDuration) {
						self._animating = false;
						self._transitionEnd();
					}
				}());
			};

			prototype._findSwipeTarget = function (element) {
				var selector = this.options.swipeTarget;

				while (element && element.webkitMatchesSelector && !element.webkitMatchesSelector(selector)) {
					element = element.parentNode;
				}
				return element;
			};

			prototype._fireEvent = function (eventName, detail) {
				var target = this.activeTarget || this.listElement;
				utilsEvents.trigger(target, eventName, detail);
			};

			prototype._start = function (e) {
				var gesture = e.detail,
					width, height, top;

				this._dragging = false;
				this._cancelled = false;

				this.activeTarget = this._findSwipeTarget(gesture.srcEvent.target);

				if (this.activeTarget) {

					width = this.activeTarget.offsetWidth;
					height = this.activeTarget.offsetHeight;
					top = this.activeTarget.offsetTop - this.scrollableElement.scrollTop;

					if (this.swipeLeftElementStyle) {
						this.swipeLeftElementStyle.width = width + "px";
						this.swipeLeftElementStyle.height = height + "px";
						this.swipeLeftElementStyle.top = top + "px";
					}
					if (this.swipeRightElementStyle) {
						this.swipeRightElementStyle.width = width + "px";
						this.swipeRightElementStyle.height = height + "px";
						this.swipeRightElementStyle.top = top + "px";
					}

					this._dragging = true;
				}
			};

			prototype._move = function (e) {
				var gesture = e.detail,
					translateX = gesture.estimatedDeltaX,
					activeElementStyle;

				if (!this._dragging || this._cancelled) {
					return;
				}

				if (this.swipeLeftElement && (gesture.direction === Gesture.Direction.RIGHT) && translateX >= 0) {
					if (this.swipeRightElementStyle) {
						this.swipeRightElementStyle.display = "none";
					}
					this.activeElement = this.swipeLeftElement;
					activeElementStyle = this.swipeLeftElementStyle;

				} else if (this.swipeRightElement && (gesture.direction === Gesture.Direction.LEFT) && translateX < 0) {
					if (this.swipeLeftElementStyle) {
						this.swipeLeftElementStyle.display = "none";
					}
					this.activeElement = this.swipeRightElement;
					activeElementStyle = this.swipeRightElementStyle;
				}

				if (!activeElementStyle) {
					return;
				}

				activeElementStyle.display = "block";
				this.swipeElementStyle.display = "block"; // wrapper element

				this._translate(activeElementStyle, translateX, false);
			};

			prototype._end = function (e) {
				var gesture = e.detail;

				if (!this._dragging || this._cancelled) {
					return;
				}

				if (this.swipeLeftElement && (gesture.estimatedDeltaX > this.options.animationThreshold)) {
					this._fire(eventType.LEFT, e);
				} else if (this.swipeRightElement && (gesture.estimatedDeltaX < -this.options.animationThreshold)) {
					this._fire(eventType.RIGHT, e);
				} else {
					this._hide();
				}

				this._dragging = false;
			};

			prototype._swipe = function (e) {
				var gesture = e.detail;

				if (!this._dragging || this._cancelled) {
					return;
				}

				if (this.swipeLeftElement && (gesture.direction === Gesture.Direction.RIGHT)) {
					this._fire(eventType.LEFT, e);
				} else if (this.swipeRightElement && (gesture.direction === Gesture.Direction.LEFT)) {
					this._fire(eventType.RIGHT, e);
				} else {
					this._hide();
				}

				this._dragging = false;
			};

			prototype._fire = function (type, e) {
				var gesture = e.detail;

				if (type === eventType.LEFT) {
					this._translate(this.swipeLeftElementStyle, gesture.estimatedDeltaX, true);
				} else if (type === eventType.RIGHT) {
					this._translate(this.swipeRightElementStyle, gesture.estimatedDeltaX, true);
				}
			};

			prototype._transitionEnd = function () {
				this._hide();

				if (this.activeElement === this.swipeLeftElement) {
					this._fireEvent(eventType.LEFT);
				} else if (this.activeElement === this.swipeRightElement) {
					this._fireEvent(eventType.RIGHT);
				}
			};

			prototype._cancel = function () {
				this._dragging = false;
				this._cancelled = true;
				this._hide();
			};

			prototype._hide = function () {
				if (this.swipeElementStyle) {
					this.swipeElementStyle.display = "none";
				}

				if (this.activeElement) {
					this.activeElement.style.display = "none";
				}
			};

			prototype._destroy = function () {
				this._reset();

				this.element = null;
				this.container = null;
				this.swipeElement = null;
				this.swipeLeftElement = null;
				this.swipeRightElement = null;

				this.swipeElementStyle = null;
				this.swipeLeftElementStyle = null;
				this.swipeRightElementStyle = null;

				this.activeElement = null;
				this.activeTarget = null;

				this.startX = null;
				this.options = null;
				this.gesture = null;

				this._cancelled = null;
				this._dragging = null;
				this._animating = null;
			};

			SwipeList.prototype = prototype;

			ns.widget.wearable.SwipeList = SwipeList;

			engine.defineWidget(
				"SwipeList",
				".ui-swipe",
				[],
				SwipeList
			);
			}(window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * #Scrollbar namespace
 * Namespace with scrollbar for scroller widget.
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @class ns.widget.wearable.scroller.scrollbar
 */
(function (window, ns) {
	
				ns.widget.wearable.scroller.scrollbar = ns.widget.wearable.scroller.scrollbar || {};
			}(window, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * #type namespace
 * Namespace with types of scroll bars..
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @class ns.widget.wearable.scroller.scrollbar.type
 */
(function (window, ns) {
	
				/** @namespace ns.widget.wearable */
			ns.widget.wearable.scroller.scrollbar.type = ns.widget.wearable.scroller.scrollbar.type || {};
			}(window, ns));

/*global window, define, Event, console, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * #Type Interface
 * Interface for types used in scroll bar widget.
 * @class ns.widget.wearable.scroller.scrollbar.type.interface
 */
(function (document, ns) {
	
				// scroller.start event trigger when user try to move scroller

			ns.widget.wearable.scroller.scrollbar.type.interface = {
				/**
				 * Inserts elements end decorate.
				 * @method insertAndDecorate
				 * @param options
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.interface
				 */
				setScrollbarLayout: function (/* options */) {
				},
				/**
				 * Removes element.
				 * @method remove
				 * @param options
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.interface
				 */
				remove: function (/* options */) {
				},
				/**
				 * ...
				 * @method start
				 * @param scrollbarElement
				 * @param barElement
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.interface
				 */
				start: function (/* scrollbarElement, barElement */) {
				},
				/**
				 * ...
				 * @method end
				 * @param scrollbarElement
				 * @param barElement
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.interface
				 */
				end: function (/* scrollbarElement, barElement */) {
				},
				/**
				 * ...
				 * @method offset
				 * @param orientation
				 * @param offset
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.interface
				 */
				offset: function (/* orientation, offset  */) {
				}
			};
			}(window.document, ns));

/*global window, define, Event, console, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * #Bar Type
 * Bar type support for scroll bar widget.
 * @class ns.widget.wearable.scroller.scrollbar.type.bar
 * @extends ns.widget.wearable.scroller.scrollbar.type.interface
 */
(function (document, ns) {
	
				// scroller.start event trigger when user try to move scroller
			var utilsObject = ns.util.object,
				type = ns.widget.wearable.scroller.scrollbar.type,
				typeInterface = type.interface,
				Scroller = ns.widget.wearable.scroller.Scroller;

			type.bar = utilsObject.merge({}, typeInterface, {
				options: {
					animationDuration: 500
				},

				/**
				 * @method setScrollbar
				 * @param viewLayout
				 * @param firstChildLayout
				 * @param clipLayout
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.bar
				 */

				setScrollbar: function(viewLayout, firstChildLayout, clipLayout) {
					this._viewLayout = viewLayout;
					this._clipLayout = clipLayout;
					this._firstChildLayout = firstChildLayout;
					this._ratio = clipLayout / firstChildLayout;
				},

				/**
				 * @method getScrollbarSize
				 * @return scrollbar size
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.bar
				 */
				getScrollbarSize: function() {
					return this._firstChildLayout / this._viewLayout * this._firstChildLayout * this._ratio;
				},
				/**
				 * @method offset
				 * @param orientation
				 * @param offset
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.bar
				 */
				offset: function( orientation, offset ) {
					var x, y;

					offset = offset * this._clipLayout / this._viewLayout;

					if ( orientation === Scroller.Orientation.VERTICAL ) {
						x = 0;
						y = offset;
					} else {
						x = offset;
						y = 0;
					}

					return {
						x: x,
						y: y
					};
				},

				/**
				 * @method start
				 * @param scrollbarElement
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.bar
				 */
				start: function( scrollbarElement/*, barElement */) {
					var style = scrollbarElement.style,
						duration = this.options.animationDuration;
					style["-webkit-transition"] =
							style["-moz-transition"] =
							style["-ms-transition"] =
							style["-o-transition"] =
							style.transition = "opacity " + duration / 1000 + "s ease";
					style.opacity = 1;
				},

				/**
				 * @method end
				 * @param scrollbarElement
				 * @static
				 * @member ns.widget.wearable.scroller.scrollbar.type.bar
				 */
				end: function( scrollbarElement/*, barElement */) {
					var style = scrollbarElement.style,
						duration = this.options.animationDuration;
					style["-webkit-transition"] =
							style["-moz-transition"] =
							style["-ms-transition"] =
							style["-o-transition"] =
							style.transition = "opacity " + duration / 1000 + "s ease";
					style.opacity = 0;
				}
			});

			}(window.document, ns));

/*global window, define, Event, console, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint nomen: true, plusplus: true */
/**
 * #Scroll Bar Widget
 * Widget creates scroll bar.
 * @class ns.widget.wearable.scroller.scrollbar.ScrollBar
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				// scroller.start event trigger when user try to move scroller
			var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,
				prototype = new BaseWidget(),
				utilsObject = ns.util.object,
				selectors = ns.util.selectors,
				scrollbarType = ns.widget.wearable.scroller.scrollbar.type,
				Classes = {
					wrapperClass: "ui-scrollbar-bar-type",
					barClass: "ui-scrollbar-indicator",
					orientationClass: "ui-scrollbar-"
				},

				Scroller = ns.widget.wearable.scroller.Scroller,
				ScrollerScrollBar = function () {

					this.wrapper = null;
					this.barElement = null;

					this.container = null;
					this.view = null;

					this.options = {};
					this.type = null;

					this.maxScroll = null;
					this.started = false;
					this.displayDelayTimeoutId = null;

					this.lastScrollPosition = 0;
				};

			prototype._build = function (scrollElement) {
				this.clip = scrollElement;
				this.view = scrollElement.children[0];
				this.firstChild = this.view.children[0];
				return scrollElement;
			};

			prototype._configure = function () {
				/**
				 * @property {Object} options Options for widget
				 * @property {boolean} [options.type=false]
				 * @property {number} [options.displayDelay=700]
				 * @property {"vertical"|"horizontal"} [options.orientation="vertical"]
				 * @member ns.widget.wearable.scroller.scrollbar.ScrollBar
				 */
				this.options = utilsObject.merge({}, this.options, {
					type: false,
					displayDelay: 700,
					orientation: Scroller.Orientation.VERTICAL
				});
			};

			prototype._init = function () {
				this.type = this.options.type;

				if ( !this.type ) {
					return;
				}
				this._createScrollbar();
			};

			prototype._bindEvents = function() {
				document.addEventListener("visibilitychange", this);
			};

			prototype._createScrollbar = function () {
				var orientation = this.options.orientation,
					wrapper = document.createElement("DIV"),
					bar = document.createElement("span"),
					view = this.view,
					clip = this.clip,
					firstChild = this.firstChild,
					type = this.type;

				clip.appendChild(wrapper);
				wrapper.appendChild(bar);
				wrapper.classList.add(Classes.wrapperClass);
				bar.className = Classes.barClass;

				if (orientation === Scroller.Orientation.HORIZONTAL) {
					type.setScrollbar(view.offsetWidth, firstChild.offsetWidth, clip.offsetWidth);
					bar.style.width = type.getScrollbarSize() + "px";
					wrapper.classList.add(Classes.orientationClass + "horizontal");
				} else {
					type.setScrollbar(view.offsetHeight, firstChild.offsetHeight, clip.offsetHeight);
					bar.style.height = type.getScrollbarSize() + "px";
					wrapper.classList.add(Classes.orientationClass + "vertical");
				}

				this.wrapper = wrapper;
				this.barElement = bar;
			};

			prototype._removeScrollbar = function () {
				this.clip.removeChild(this.wrapper);

				this.wrapper = null;
				this.barElement = null;
			};

			prototype._refresh = function () {
				var self = this;
				self._clear();
				self._init();
				self.translate(self.lastScrollPosition);
			};

			/**
			 * Translates widget.
			 * @method translate
			 * @param offset
			 * @param duration
			 * @member ns.widget.wearable.scroller.scrollbar.ScrollBar
			 */
			prototype.translate = function (offset, duration, autoHidden) {
				var orientation = this.options.orientation,
					translate,
					transition = {
						normal: "none",
						webkit: "none",
						moz: "none",
						ms: "none",
						o: "none"
					},
					barStyle,
					endDelay;

				if ( !this.wrapper || !this.type || this.lastScrollPosition === offset ) {
					return;
				}

				autoHidden = autoHidden !== false;

				this.lastScrollPosition = offset;

				offset = this.type.offset( orientation, offset );

				barStyle = this.barElement.style;
				if (duration) {
					transition.normal = "transform " + duration / 1000 + "s ease-out";
					transition.webkit = "-webkit-transform " + duration / 1000 + "s ease-out";
					transition.moz = "-moz-transform " + duration / 1000 + "s ease-out";
					transition.ms = "-ms-transform " + duration / 1000 + "s ease-out";
					transition.o = "-o-transform " + duration / 1000 + "s ease-out";
				}

				translate = "translate3d(" + offset.x + "px," + offset.y + "px, 0)";

				barStyle["-webkit-transform"] =
					barStyle["-moz-transform"] =
					barStyle["-ms-transform"] =
					barStyle["-o-transform"] =
					barStyle.transform = translate;
				barStyle["-webkit-transition"] = transition.webkit;
				barStyle["-moz-transition"] = transition.moz;
				barStyle["-ms-transition"] = transition.ms;
				barStyle["-o-transition"] = transition.o;
				barStyle.transition = transition.normal;

				if ( !this.started ) {
					this._start();
				}

				if ( this.displayDelayTimeoutId !== null ) {
					window.clearTimeout( this.displayDelayTimeoutId );
					this.displayDelayTimeoutId = null;
				}

				if ( autoHidden ) {
					endDelay = ( duration || 0 ) + this.options.displayDelay;
					this.displayDelayTimeoutId = window.setTimeout(this._end.bind(this), endDelay);
				}
			};

			prototype.end = function () {
				if ( !this.displayDelayTimeoutId ) {
					this.displayDelayTimeoutId = window.setTimeout(this._end.bind(this), this.options.displayDelay);
				}
			};

			prototype._start = function () {
				this.type.start(this.wrapper, this.barElement);
				this.started = true;
			};

			prototype._end = function () {
				this.started = false;
				this.displayDelayTimeoutId = null;

				if ( this.type ) {
					this.type.end(this.wrapper, this.barElement);
				}
			};

			/**
			 * Supports events.
			 * @method handleEvent
			 * @param event
			 * @member ns.widget.wearable.scroller.scrollbar.ScrollBar
			 */
			prototype.handleEvent = function(event) {
				var page;

				switch(event.type) {
				case "visibilitychange":
					page = selectors.getClosestBySelector(this.container, "." + ns.widget.core.Page.classes.uiPage);
					if (document.visibilityState === "visible" && page === ns.activePage) {
						this.refresh();
					}
					break;
				}
			};

			prototype._clear = function () {
				this._removeScrollbar();

				this.started = false;
				this.type = null;
				this.barElement = null;
				this.displayDelayTimeoutId = null;
			};

			prototype._destroy = function () {
				this._clear();
				document.removeEventListener("visibilitychange", this);

				this.options = null;
				this.clip = null;
				this.view = null;
			};

			ScrollerScrollBar.prototype = prototype;

			ns.widget.wearable.scroller.scrollbar.ScrollBar = ScrollerScrollBar;

			engine.defineWidget(
				"ScrollBar",
				"",
				["translate"],
				ScrollerScrollBar
			);
			}(window.document, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #PageIndicator Widget
 * Widget create dots page indicator.
 * @class ns.widget.wearable.PageIndicator
 * @extends ns.widget.BaseWidget
 */
(function (document, ns) {
	
				var BaseWidget = ns.widget.BaseWidget,
				engine = ns.engine,

				PageIndicator = function () {
					var self = this;
					self._activeIndex = null;
					self.options = {};
				},
				classes = {
					indicator: "ui-page-indicator",
					indicatorActive: "ui-page-indicator-active",
					indicatorItem: "ui-page-indicator-item",
					linearIndicator: "ui-page-indicator-linear",
					circularIndicator: "ui-page-indicator-circular"
				},
				maxDots = {
					IN_CIRCLE: 60,
					IN_LINEAR: 5
				},
				layoutType = {
					LINEAR: "linear",
					CIRCULAR: "circular"
				},
				DISTANCE_FROM_EDGE = 15,

				prototype = new BaseWidget();

			PageIndicator.classes = classes;

			prototype._configure = function () {
				/**
				 * Options for widget.
				 * @property {Object} options
				 * @property {number} [options.maxPage=null] Maximum number of dots(pages) in indicator.
				 * @property {number} [options.numberOfPages=null] Number of pages to be linked to PageIndicator.
				 * @property {string} [options.layout="linear"] Layout type of page indicator.
				 * @property {number} [options.intervalAngle=6] angle between each dot in page indicator.
				 * @member ns.widget.wearable.PageIndicator
				 */
				this.options = {
					maxPage: null,
					numberOfPages: null,
					layout: "linear",
					intervalAngle: 6
				};
			};

			/**
			 * Build PageIndicator
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @protected
			 * @member ns.widget.wearable.PageIndicator
			 */
			prototype._build = function (element) {
				var self = this;
				self._createIndicator(element);
				if (self.options.layout === layoutType.CIRCULAR) {
					self._circularPositioning(element);
				}
				return element;
			};

			/**
			 * Create HTML elements for PageIndicator
			 * @method _createIndicator
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.wearable.PageIndicator
			 */
			prototype._createIndicator = function (element) {
				var self = this,
					i,
					len,
					maxPage,
					span,
					numberOfPages = self.options.numberOfPages;

				if(numberOfPages === null) {
					ns.error("build error: numberOfPages is null");
					return;
				}

				self.options.layout = self.options.layout.toLowerCase();

				if (self.options.layout === layoutType.CIRCULAR) {
					element.classList.remove(classes.linearIndicator);
					element.classList.add(classes.circularIndicator);
				} else {
					element.classList.remove(classes.circularIndicator);
					element.classList.add(classes.linearIndicator);
				}

				maxPage = self._getMaxPage();

				len = numberOfPages < maxPage ? numberOfPages : maxPage;

				for(i = 0; i < len; i++) {
					span = document.createElement("span");
					span.classList.add(classes.indicatorItem);

					element.appendChild(span);
				}
			};

			/**
			 * Make circular positioned indicator
			 * @method _circularPositioning
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.wearable.PageIndicator
			 */
			prototype._circularPositioning = function (element) {
				var self = this,
					items = element.children,
					numberOfDots = items.length,
					intervalAngle = self.options.intervalAngle - "0",
					translatePixel,
					style,
					i;

				translatePixel = element.offsetWidth / 2 - DISTANCE_FROM_EDGE;

				for(i=0;i<numberOfDots;i++) {
					style = "rotate(" + (i * intervalAngle - 90 - (numberOfDots-1) * intervalAngle * 0.5) + "deg) translate(" +
						translatePixel + "px) ";

					items[i].style.transform = style;
				}

			};

			/**
			 * Return maximum number of dots(pages) in indicator
			 * @method _getMaxPage
			 * @protected
			 * @member ns.widget.wearable.PageIndicator
			 */
			prototype._getMaxPage = function() {
				var self = this,
					options = self.options,
					maxPage;
				if (options.layout === layoutType.CIRCULAR) {
					maxPage = options.maxPage || maxDots.IN_CIRCLE;
				} else {
					maxPage = options.maxPage || maxDots.IN_LINEAR;
				}
				return maxPage;
			};

			/**
			 * Remove contents of HTML elements for PageIndicator
			 * @method _removeIndicator
			 * @param {HTMLElement} element
			 * @protected
			 * @member ns.widget.wearable.PageIndicator
			 */
			prototype._removeIndicator =  function (element) {
				element.textContent = "";
			};

			/**
			 * This method sets a dot to active state.
			 * @method setActive
			 * @param {number} position index to be active state.
			 * @member ns.widget.wearable.PageIndicator
			 */
			prototype.setActive = function (position) {
				var self = this,
					dotIndex = position,
					elPageIndicatorItems = self.element.children,
					maxPage,
					numberOfPages = self.options.numberOfPages,
					middle,
					numberOfCentralDotPages = 0,
					indicatorActive = classes.indicatorActive,
					previousActive;

				if(position === null || position === undefined) {
					return;
				}

				self._activeIndex = position;
				maxPage = self._getMaxPage();
				middle = window.parseInt(maxPage/2, 10);

				if(numberOfPages > maxPage) {
					numberOfCentralDotPages = numberOfPages - maxPage;
				} else if(numberOfPages === null) {
					ns.error("setActive error: numberOfPages is null");
					return;
				} else if(numberOfPages === 0) {
					return;
				}

				previousActive = self.element.querySelector("." + indicatorActive);
				if(previousActive) {
					previousActive.classList.remove(indicatorActive);
				}

				if ((middle < position) && (position <= (middle + numberOfCentralDotPages))) {
					dotIndex = middle;
				} else if (position > (middle + numberOfCentralDotPages)) {
					dotIndex = position - numberOfCentralDotPages;
				}

				elPageIndicatorItems[dotIndex].classList.add(indicatorActive);
			};

			/**
			 * Refresh widget structure
			 * @method _refresh
			 * @protected
			 * @member ns.widget.wearable.PageIndicator
			 */
			prototype._refresh = function () {
				var self = this,
					element = self.element;
				self._removeIndicator(element);
				self._createIndicator(element);
				if (self.options.layout === layoutType.CIRCULAR) {
					self._circularPositioning(element);
				}
			};

			/**
			 * Destroy widget
			 * @method _destroy
			 * @protected
			 * @member ns.widget.wearable.PageIndicator
			 */
			prototype._destroy = function () {
				this._removeIndicator(this.element);
			};

			PageIndicator.prototype = prototype;

			// definition
			ns.widget.wearable.PageIndicator = PageIndicator;
			engine.defineWidget(
				"PageIndicator",
				".ui-page-indicator",
				["setActive"],
				PageIndicator,
				"wearable"
			);
			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * # Selector Component
 *
 * Selector component is special component that has unique UX of Tizen wearable profile.
 * Selector component has been used in more options commonly but If you want to use other situation then you can use
 * this component as standalone component in everywhere.
 * Selector component was consisted as selector element and item elements. You can set the item selector, each items locate degree and radius.
 * Selector component has made layers automatically. Layer has items and you can set items number on one layer.
 * Indicator is indicator that located center of Selector. We provide default indicator style and function.
 * But, If you want to change indicator style and function, you can make the custom indicator and set your indicator for operate with Selector.
 * Indicator arrow is special indicator style that has the arrow. That was used for provide more correct indicate information for user.
 * Also, you can make the custom indicator arrow and set your custom indicator arrow for operate with Selector.
 * Selector provide to control for arrow indicate active item position.
 *
 * ## HTML example
 *
 *          @example
 *              <div class="ui-page ui-page-active" id="main">
 *                  <div id="selector" class="ui-selector">
 *                      <div class="ui-item ui-show-icon" data-title="Show"></div>
 *                      <div class="ui-item ui-human-icon" data-title="Human"></div>
 *                      <div class="ui-item ui-delete-icon" data-title="Delete"></div>
 *                      <div class="ui-item ui-show-icon" data-title="Show"></div>
 *                      <div class="ui-item ui-human-icon" data-title="Human"></div>
 *                      <div class="ui-item ui-delete-icon" data-title="Delete"></div>
 *                      <div class="ui-item ui-x-icon" data-title="X Icon"></div>
 *                      <div class="ui-item ui-fail-icon" data-title="Fail"></div>
 *                      <div class="ui-item ui-show-icon" data-title="Show"></div>
 *                      <div class="ui-item ui-human-icon" data-title="Human"></div>
 *                      <div class="ui-item ui-delete-icon" data-title="Delete"></div>
 *                  </div>
 *              </div>
 *
 * ## Manual constructor
 *
 *          @example
 *              (function() {
 *                  var page = document.getElementById("selectorPage"),
 *                      selector = document.getElementById("selector"),
 *                      clickBound;
 *
 *                  function onClick(event) {
 *                      var activeItem = selector.querySelector(".ui-item-active");
 *                      //console.log(activeItem.getAttribute("data-title"));
 *                  }
 *                  page.addEventListener("pagebeforeshow", function() {
 *                      clickBound = onClick.bind(null);
 *                      tau.widget.Selector(selector);
 *                      selector.addEventListener("click", clickBound, false);
 *                  });
 *                  page.addEventListener("pagebeforehide", function() {
 *                      selector.removeEventListener("click", clickBound, false);
 *                  });
 *              })();
 *
 * ## Options
 * Selector component options
 *
 * {String} itemSelector [options.itemSelector=".ui-item"] or You can set attribute on tag [data-item-selector=".ui-item] Selector item selector that style is css selector.
 * {String} indicatorSelector [options.indicatorSelector=".ui-selector-indicator"] or You can set attribute on tag [data-indicator-selector=".ui-selector-indicator"] Selector indicator selector that style is css selector.
 * {String} indicatorArrowSelector [options.indicatorArrowSelector=".ui-selector-indicator-arrow"] or You can set attribute on tag [data-indicator-arrow-selector=".ui-selector-indicator-arrow"] Selector indicator arrow selector that style is css style.
 * {Number} itemDegree [options.itemDegree=30] or You can set attribute on tag [data-item-degree=30] Items degree each other.
 * {Number} itemRadius [options.itemRadius=140] or You can set attribute on tag [data-item-radius=140] Items radius between center and it.
 * {Number} maxItemNumber [options.maxItemNumber=11] or You can set attribute on tag [data-max-item-number=11] Max item number on one layer. If you change the itemDegree, we recommend to consider to modify this value for fit your Selector layout.
 * {boolean} indicatorAutoControl [options.indicatorAutoControl=true] or You can set attribute on tag [data-indicator-auto-control=true] Indicator auto control switch. If you want to control your indicator manually, change this options to false.
 *
 * @class ns.widget.wearable.Selector
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 */
(function (document, ns) {
	
				var engine = ns.engine,
				utilDom = ns.util.DOM,
				Gesture = ns.event.gesture,
				events = ns.event,
				utilsObject = ns.util.object,
				Selector = function () {
					var self = this;
					self._ui = {};
					self.options = {};
				},
				classes = {
					SELECTOR: "ui-selector",
					LAYER: "ui-layer",
					LAYER_ACTIVE: "ui-layer-active",
					LAYER_PREV: "ui-layer-prev",
					LAYER_PREV_PREV: "ui-layer-prev-prev",
					LAYER_NEXT: "ui-layer-next",
					LAYER_NEXT_NEXT: "ui-layer-next-next",
					LAYER_HIDE: "ui-layer-hide",
					ITEM: "ui-item",
					ITEM_ACTIVE: "ui-item-active",
					INDICATOR: "ui-selector-indicator",
					INDICATOR_NEXT_END: "ui-selector-indicator-next-end",
					INDICATOR_PREV_END: "ui-selector-indicator-prev-end",
					INDICATOR_ARROW: "ui-selector-indicator-arrow"
				},
				STATIC = {
					RADIUS_RATIO: 0.78
				},
				DEFAULT = {
					ITEM_SELECTOR: "."+ classes.ITEM,
					INDICATOR_SELECTOR: "." + classes.INDICATOR,
					INDICATOR_ARROW_SELECTOR: "." + classes.INDICATOR_ARROW,
					ITEM_DEGREE: 30,
					MAX_ITEM_NUMBER: 11,
					ITEM_RADIUS: -1,
					ITEM_START_DEGREE: 30,
					ITEM_END_DEGREE: 330,
					ITEM_NORMAL_SCALE: "scale(1)",
					ITEM_ACTIVE_SCALE: "scale(1.18)"
				},
				EVENT_TYPE = {
					/**
					 * Triggered when the active item is changed. Target is active item element.
					 * This event has detail information.
					 * - layer: Layer element on active item
					 * - layerIndex: Layer's index on active item
					 * - index: Item index on layer.
					 * - title: If Item has 'data-title' attribute, this value is that.
					 * @event selectoritemchange
					 * @member ns.widget.wearable.Selector
					 */
					ITEM_CHANGE: "selectoritemchange",
					/**
					 * Triggered when the active layer is changed. Target is active layer element.
					 * This event has detail information.
					 * - index: Layer index.
					 * @event selectorlayerchange
					 * @member ns.widget.wearable.Selector
					 */
					LAYER_CHANGE: "selectorlayerchange"
				},
				BaseWidget = ns.widget.BaseWidget,
				prototype = new BaseWidget();

			Selector.prototype = prototype;

			function buildLayers(element, items, options) {
				var layers = [],
					layer,
					i, len;

				removeLayers(element, options);
				len = items.length;
				for (i = 0; i < len; i++) {
					if (!(i % options.maxItemNumber)) {
						layer = document.createElement("div");
						layer.classList.add(classes.LAYER);
						element.appendChild(layer);
						layers.push(layer);
					}
					items[i].classList.add(classes.ITEM);
					layer.appendChild(items[i]);
					if (utilDom.getNSData(items[i], "active")) {
						items[i].classList.add(classes.ITEM_ACTIVE);
						layer.classList.add(classes.LAYER_ACTIVE);
					}
				}
				return layers;
			}

			function removeLayers(element, options) {
				var layers = element.getElementsByClassName(classes.LAYER),
					items,
					i, len, j, itemLength;
				if (layers.length) {
					// Delete legacy layers
					len = layers.length;
					for (i = 0; i < len; i++) {
						items = layers[0].querySelectorAll(options.itemSelector);
						itemLength = items.length;
						for (j = 0; j < itemLength; j++) {
							element.appendChild(items[j]);
						}
						element.removeChild(layers[0]);
					}
				}
			}

			/**
			 * Bind events
			 * @method bindEvents
			 * @param {Object} self
			 * @private
			 * @member ns.widget.wearable.Selector
			 */
			function bindEvents(self) {
				var element= self.element;
				events.enableGesture(
					element,

					new Gesture.Drag()
				);
				events.on(document, "rotarydetent", self, false);
				events.on(element, "dragstart drag dragend click", self, false);
			}

			/**
			 * Unbind events
			 * @method bindEvents
			 * @param {Object} self
			 * @private
			 * @member ns.widget.wearable.Selector
			 */
			function unbindEvents(self) {
				var element= self.element;
				events.disableGesture(
					element
				);
				events.off(document, "rotarydetent", self, false);
				events.off(element, "dragstart drag dragend click", self, false);
			}
			/**
			 * Remove ordering classes of layers base on parameter.
			 * @method removeLayerClasses
			 * @param {HTMLElement} activeLayer
			 * @private
			 * @member ns.widget.wearable.Selector
			 */
			function removeLayerClasses(activeLayer) {
				var activePrevLayer = activeLayer.previousElementSibling,
					activeNextLayer = activeLayer.nextElementSibling,
					ppLayer, nnLayer;

				if (activePrevLayer) {
					activePrevLayer.classList.remove(classes.LAYER_PREV);
					ppLayer = activePrevLayer.previousElementSibling;
					ppLayer && ppLayer.classList.remove(classes.LAYER_PREV_PREV);
				}
				if (activeNextLayer) {
					activeNextLayer.classList.remove(classes.LAYER_NEXT);
					nnLayer = activeNextLayer.nextElementSibling;
					nnLayer && nnLayer.classList.remove(classes.LAYER_NEXT_NEXT);
				}
				activeLayer.classList.remove(classes.LAYER_ACTIVE);
			}

			/**
			 * Add ordering classes of layers base on parameter.
			 * @method addLayerClasses
			 * @param {HTMLElement} activeLayer
			 * @private
			 * @member ns.widget.wearable.Selector
			 */
			function addLayerClasses(self, validLayer) {
				var options = self.options,
					validPrevLayer = validLayer.previousElementSibling,
					validNextLayer = validLayer.nextElementSibling,
					radius = options.itemRadius,
					prevLayerDeg, ppLayerDeg, nextLayerDeg, nnLayerDeg,
					ppLayer, nnLayer;

				if (validPrevLayer && validPrevLayer.classList.contains(classes.LAYER)) {
					validPrevLayer.classList.add(classes.LAYER_PREV);
					prevLayerDeg = DEFAULT.ITEM_END_DEGREE + DEFAULT.ITEM_DEGREE / 6;
					setItemTransform(validPrevLayer, prevLayerDeg, radius, -prevLayerDeg, DEFAULT.ITEM_NORMAL_SCALE);
					ppLayer = validPrevLayer.previousElementSibling;
					ppLayerDeg = DEFAULT.ITEM_END_DEGREE + DEFAULT.ITEM_DEGREE / 4;
					if (ppLayer && ppLayer.classList.contains(classes.LAYER)) {
						ppLayer.classList.add(classes.LAYER_PREV_PREV);
						setItemTransform(ppLayer, ppLayerDeg, radius, -ppLayerDeg, DEFAULT.ITEM_NORMAL_SCALE);
					}
				}

				if (validNextLayer && validNextLayer.classList.contains(classes.LAYER)) {
					validNextLayer.classList.add(classes.LAYER_NEXT);
					nextLayerDeg = DEFAULT.ITEM_START_DEGREE - DEFAULT.ITEM_DEGREE / 6;
					setItemTransform(validNextLayer, nextLayerDeg, radius, -nextLayerDeg, DEFAULT.ITEM_NORMAL_SCALE);
					nnLayer = validNextLayer.nextElementSibling;
					nnLayerDeg = DEFAULT.ITEM_START_DEGREE - DEFAULT.ITEM_DEGREE / 4;
					if (nnLayer && nnLayer.classList.contains(classes.LAYER)) {
						nnLayer.classList.add(classes.LAYER_NEXT_NEXT);
						setItemTransform(nnLayer, nnLayerDeg, radius, -nnLayerDeg, DEFAULT.ITEM_NORMAL_SCALE);
					}
				}
				validLayer.classList.add(classes.LAYER_ACTIVE);
				validLayer.style.transform = "none";
			}

			function setItemTransform(element, degree, radius, selfDegree, scale) {
				element.style.transform = "rotate(" + degree + "deg) " +
					"translate3d(0, " + -radius + "px, 0) " +
					"rotate(" + selfDegree + "deg) " +
					scale;
			}

			function setIndicatorTransform(element, selfDegree) {
				element.style.transform = "rotate(" + selfDegree + "deg) ";
				element.style.transition = "transform 300ms";
			}

			prototype._configure = function() {
				var self = this;
				/**
				 * Selector component options
				 * @property {string} itemSelector [options.itemSelector=".ui-item"] Selector item selector that style is css selector.
				 * @property {string} indicatorSelector [options.indicatorSelector=".ui-selector-indicator"] Selector indicator selector that style is css selector.
				 * @property {string} indicatorArrowSelector [options.indicatorArrowSelector=".ui-selector-indicator-arrow"] Selector indicator arrow selector that style is css style.
				 * @property {Number} itemDegree [options.itemDegree=30] Each items locate degree.
				 * @property {Number} itemRadius [options.itemRadius=-1] Items locate radius between center to it. Default value is determined by Selector element layout.
				 * @property {Number} maxItemNumber [options.maxItemNumber=11] Max item number on one layer. If you change the itemDegree, we recommend to consider to modify this value for fit your Selector layout.
				 * @property {boolean} indicatorAutoControl [options.indicatorAutoControl=true] Indicator auto control switch. If you want to control your indicator manually, change this options to false.
				 */
				self.options = utilsObject.merge(self.options, {
					itemSelector: DEFAULT.ITEM_SELECTOR,
					indicatorSelector: DEFAULT.INDICATOR_SELECTOR,
					indicatorArrowSelector: DEFAULT.INDICATOR_ARROW_SELECTOR,
					itemDegree: DEFAULT.ITEM_DEGREE,
					itemRadius: DEFAULT.ITEM_RADIUS,
					maxItemNumber: DEFAULT.MAX_ITEM_NUMBER,
					indicatorAutoControl: true
				});
			};

			/**
			 * Build Selector component
			 * @method _build
			 * @param {HTMLElement} element
			 * @return {HTMLElement} element
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._build = function(element) {
				var self = this,
					ui = self._ui,
					options = self.options,
					items = element.querySelectorAll(self.options.itemSelector),
					indicator,
					indicatorArrow,
					queryIndicator,
					queryIndicatorArrow,
					layers;

				if (items && items.length) {

					layers = buildLayers(element, items, options);
					element.classList.add(classes.SELECTOR);

					if (options.indicatorAutoControl) {
						queryIndicator = element.querySelector(options.indicatorSelector);
						queryIndicatorArrow = element.querySelector(options.indicatorArrowSelector);

						if (queryIndicator) {
							ui.indicator = queryIndicator;
						} else {
							indicator = document.createElement("div");
							indicator.classList.add(classes.INDICATOR);
							ui.indicator = indicator;
							element.appendChild(ui.indicator);
						}
						if (queryIndicatorArrow) {
							ui.indicatorArrow = queryIndicatorArrow;
						} else {
							indicatorArrow = document.createElement("div");
							indicatorArrow.classList.add(classes.INDICATOR_ARROW);
							ui.indicatorArrow = indicatorArrow;
							element.appendChild(ui.indicatorArrow);
						}
					}
					ui.items = items;
					ui.layers = layers;
				} else {
					console.warn("Please check your item selector option. Default value is '.ui-item'");
					return;
				}

				return element;
			};

			/**
			 * Init Selector component
			 * @method _init
			 * @param {HTMLElement} element
			 * @return {HTMLElement} element
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._init = function(element) {
				var self = this,
					options = self.options,
					items = self._ui.items,
					activeLayerIndex = self._getActiveLayer(),
					activeItemIndex = self._getActiveItem(),
					validLayout = element.offsetWidth > element.offsetHeight ? element.offsetHeight : element.offsetWidth,
					i, len;

				self._started = false;
				self._enabled = true;
				self._activeItemIndex = activeItemIndex === null ? 0 : activeItemIndex;
				options.itemRadius = options.itemRadius < 0 ? validLayout / 2 * STATIC.RADIUS_RATIO : options.itemRadius;
				len = items.length;
				for (i = 0; i < len; i++) {
					utilDom.setNSData(items[i], "index", i);
					setItemTransform(items[i], DEFAULT.ITEM_END_DEGREE, options.itemRadius, -DEFAULT.ITEM_END_DEGREE, DEFAULT.ITEM_NORMAL_SCALE);
				}
				if (activeLayerIndex === null) {
					self._activeLayerIndex = 0;
					self._setActiveLayer(0);
				} else {
					self._activeLayerIndex = activeLayerIndex;
					self._setActiveLayer(activeLayerIndex);
				}
				return element;
			};

			/**
			 * Init items on layer
			 * @method _initItems
			 * @param {HTMLElement} layer
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._initItems = function(layer) {
				var self = this,
					options = self.options,
					items = layer.querySelectorAll(options.itemSelector),
					degree,
					i, len;

				len = items.length > options.maxItemNumber ? options.maxItemNumber : items.length;
				for (i = 0; i < len; i++) {
					degree = DEFAULT.ITEM_START_DEGREE + (options.itemDegree * i);
					setItemTransform(items[i], degree, options.itemRadius, -degree, DEFAULT.ITEM_NORMAL_SCALE);
				}

				self._setActiveItem(self._activeItemIndex);
			};

			/**
			 * Bind events on Selector component
			 * @method _bindEvents
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._bindEvents = function() {
				bindEvents(this);
			};

			/**
			 * Handle events on Selector component
			 * @method handleEvent
			 * @param {Event} event
			 * @public
			 * @member ns.widget.wearable.Selector
			 */
			prototype.handleEvent = function(event) {
				var self = this;
				switch (event.type) {
					case "dragstart":
						self._onDragstart(event);
						break;
					case "drag":
						self._onDrag(event);
						break;
					case "dragend":
						self._onDragend(event);
						break;
					case "click":
						self._onClick(event);
						break;
					case "rotarydetent":
						self._onRotary(event);
						break;
				}
			};

			/**
			 * Get the active layer
			 * @method _getActiveLayer
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._getActiveLayer = function() {
				var self = this,
					ui = self._ui,
					i, len;

				len = ui.layers.length;
				for (i = 0; i < len; i++) {
					if (ui.layers[i].classList.contains(classes.LAYER_ACTIVE)) {
						return i;
					}
				}
				return null;
			};

			/**
			 * Set the active layer
			 * @method _setActiveLayer
			 * @param {Number} index
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._setActiveLayer = function(index) {
				var self = this,
					ui = self._ui,
					active = self._activeLayerIndex,
					activeLayer = ui.layers[active],
					validLayer = ui.layers[index];
				if (activeLayer) {
					removeLayerClasses(activeLayer);
				}
				if (validLayer) {
					addLayerClasses(self, validLayer);
				}
				self._activeLayerIndex = index;
				self._initItems(validLayer);
				events.trigger(validLayer, EVENT_TYPE, {
					index: index
				})
			};

			/**
			 * Get the active item
			 * @method _getActiveItem
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._getActiveItem = function() {
				var self = this,
					ui = self._ui,
					i, len;

				len = ui.items.length;
				for (i = 0; i < len; i++) {
					if (ui.items[i].classList.contains(classes.ITEM_ACTIVE)) {
						return i;
					}
				}
				return null;
			};

			/**
			 * Set the active item
			 * @method _setActiveItem
			 * @param {Number} index
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._setActiveItem = function(index) {
				var self = this,
					element = self.element,
					ui = self._ui,
					options = self.options,
					items = ui.items,
					index = index !== undefined ? index : 0,
					active = element.querySelector("." + classes.ITEM_ACTIVE);

				if (active) {
					active.style.transform = active.style.transform.replace(DEFAULT.ITEM_ACTIVE_SCALE, DEFAULT.ITEM_NORMAL_SCALE);
					active.classList.remove(classes.ITEM_ACTIVE);
				}
				if (items.length) {
					items[index].classList.add(classes.ITEM_ACTIVE);
					items[index].style.transform = items[index].style.transform.replace(DEFAULT.ITEM_NORMAL_SCALE, DEFAULT.ITEM_ACTIVE_SCALE);
					if (self.options.indicatorAutoControl) {
						self._setIndicatorIndex(index);
					}
					self._activeItemIndex = index;
					events.trigger(items[index], EVENT_TYPE.ITEM_CHANGE, {
						layer: ui.layers[self._activeLayerIndex],
						layerIndex: self._activeLayerIndex,
						index: index,
						title: utilDom.getNSData(items[index], "title")
					});
				}
			};

			/**
			 * Set indicator index. Handler direction was set by index value.
			 * @method _setIndicatorIndex
			 * @param {Number} index
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._setIndicatorIndex = function(index) {
				var self = this,
					ui = self._ui,
					item = ui.items[index],
					title = utilDom.getNSData(item, "title"),
					indicator = ui.indicator,
					indicatorArrow = ui.indicatorArrow,
					idcIndex = index % self.options.maxItemNumber;

				if (indicator.children.length === 0) {
					indicator.textContent = title ? title : "ITEM";
				}

				utilDom.setNSData(indicator, "index", index);

				setIndicatorTransform(indicatorArrow, DEFAULT.ITEM_START_DEGREE + self.options.itemDegree * idcIndex);
			};

			/**
			 * Dragstart event handler
			 * @method _onDragstart
			 * @param {Event} event
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._onDragstart = function(event) {
				this._started = true;
			};

			/**
			 * Drag event handler
			 * @method _onDrag
			 * @param {Event} event
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._onDrag = function(event) {
				var self = this,
					ex = event.detail.estimatedX,
					ey = event.detail.estimatedY,
					pointedElement = document.elementFromPoint(ex, ey),
					index;

				if (this._started) {
					if (pointedElement && pointedElement.classList.contains(classes.ITEM)) {
						index = parseInt(utilDom.getNSData(pointedElement, "index"), 10);
						self._setActiveItem(index);
					}
				}
			};

			/**
			 * Dragend event handler
			 * @method _onDragend
			 * @param {Event} event
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._onDragend = function(event) {
				var self = this,
					ex = event.detail.estimatedX,
					ey = event.detail.estimatedY,
					pointedElement = document.elementFromPoint(ex, ey),
					index;

				if (pointedElement && pointedElement.classList.contains(classes.ITEM)) {
					index = parseInt(utilDom.getNSData(pointedElement, "index"), 10);
					self._setActiveItem(index);
				}

				this._started = false;
			};

			/**
			 * Click event handler
			 * @method _onClick
			 * @param {Event} event
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._onClick = function(event) {
				var self = this,
					pointedElement = document.elementFromPoint(event.pageX, event.pageY),
					index;

				if (!self._enabled) {
					return;
				}
				if (pointedElement && pointedElement.classList.contains(classes.ITEM)) {
					index = parseInt(utilDom.getNSData(pointedElement, "index"), 10);
					self._setActiveItem(index);
				}
			};

			/**
			 * Rotary event handler
			 * @method _onRotary
			 * @param {Event} event
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._onRotary = function(event) {
				var self = this,
					ui = self._ui,
					options = self.options,
					direction = event.detail.direction,
					activeLayer = ui.layers[self._activeLayerIndex],
					activeLayerItemsLength = activeLayer.querySelectorAll(options.itemSelector).length,
					prevLayer = activeLayer.previousElementSibling,
					nextLayer = activeLayer.nextElementSibling,
					bounceDegree;

				if (!options.indicatorAutoControl || !self._enabled) {
					return;
				}
				event.stopPropagation();

				if (direction === "CW") {
					// check length
					if (self._activeItemIndex === (activeLayerItemsLength + self._activeLayerIndex * options.maxItemNumber) - 1) {
						if (prevLayer && prevLayer.classList.contains(classes.LAYER_PREV)) {
							self._activeItemIndex = self._activeItemIndex - activeLayerItemsLength - prevLayer.querySelectorAll(options.itemSelector).length + 1;
							self._changeLayer(self._activeLayerIndex - 1);
						} else {
							bounceDegree = DEFAULT.ITEM_START_DEGREE + options.itemDegree * (self._activeItemIndex % options.maxItemNumber);
							setIndicatorTransform(ui.indicatorArrow, bounceDegree + options.itemDegree / 3);
							//setIndicatorTransform(ui.indicatorArrow, options.itemDegree * self._activeItemIndex + options.itemDegree / 3);
							setTimeout(function() {
								setIndicatorTransform(ui.indicatorArrow, bounceDegree);
							}, 100)
						}
					} else {
						self._changeItem(self._activeItemIndex + 1);
					}
				} else {
					// check 0
					if (self._activeItemIndex % options.maxItemNumber === 0) {
						if (nextLayer && nextLayer.classList.contains(classes.LAYER_NEXT)) {
							self._activeItemIndex = self._activeItemIndex + activeLayerItemsLength + nextLayer.querySelectorAll(options.itemSelector).length - 1;
							self._changeLayer(self._activeLayerIndex + 1);
						} else {
							setIndicatorTransform(ui.indicatorArrow, DEFAULT.ITEM_START_DEGREE - DEFAULT.ITEM_START_DEGREE / 3);
							setTimeout(function() {
								setIndicatorTransform(ui.indicatorArrow, DEFAULT.ITEM_START_DEGREE);
							}, 100)
						}
					} else {
						self._changeItem(self._activeItemIndex - 1);
					}
				}
			};

			/**
			 * Hide items on layer
			 * @method _hideItems
			 * @param {HTMLElement} layer
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._hideItems = function(layer) {
				var self = this,
					options = self.options,
					items = layer.getElementsByClassName(classes.ITEM),
					i, len;
				layer.classList.add(classes.LAYER_HIDE);
				len = items.length;
				for (i = 0; i < len; i++) {
					setItemTransform(items[i], DEFAULT.ITEM_START_DEGREE, self.options.itemRadius, -DEFAULT.ITEM_START_DEGREE, DEFAULT.ITEM_NORMAL_SCALE);
				}

				setTimeout(function() {
					len = items.length;
					for (i = 0; i < len; i++) {
						setItemTransform(items[i], DEFAULT.ITEM_END_DEGREE, self.options.itemRadius, -DEFAULT.ITEM_END_DEGREE, DEFAULT.ITEM_NORMAL_SCALE);
					}
					layer.classList.remove(classes.LAYER_HIDE);
				}, 150);
			};

			/**
			 * Refresh Selector component
			 * @method _refresh
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._refresh = function() {
				var self = this,
					ui = self._ui,
					options = self.options,
					element = self.element;

				ui.layers = buildLayers(element, ui.items, options);
				self._setActiveLayer(self._activeLayerIndex);
			};

			/**
			 * Change active layer
			 * @method _changeLayer
			 * @param {Number} index
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._changeLayer = function(index) {
				var self = this,
					layers = self._ui.layers,
					activeLayer = layers[self._activeLayerIndex];

				if (index < 0 || index > layers.length - 1) {
					console.warn("Please insert index between 0 to layers number");
					return;
				}
				self._enabled = false;
				self._hideItems(activeLayer);
				setTimeout(function() {
					self._setActiveLayer(index);
					self._enabled = true;
				}, 150);

			};

			/**
			 * Change active item on active layer
			 * @method _changeItem
			 * @param {Number} index
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._changeItem = function(index) {
				this._setActiveItem(index);
			};

			/**
			 * Change active item on active layer
			 * @method changeItem
			 * @param {Number} index
			 * @public
			 * @member ns.widget.wearable.Selector
			 */
			prototype.changeItem = function(index) {
				this._changeItem(index);
			};

			/**
			 * Add new item
			 * @method addItem
			 * @param {HTMLElement} item
			 * @param {Number} index
			 * @public
			 * @member ns.widget.wearable.Selector
			 */
			prototype.addItem = function(item, index) {
				var self = this,
					element = self.element,
					items = element.querySelectorAll(self.options.itemSelector),
					ui = self._ui;

				removeLayers(self.element, self.options);
				if (index >= 0 && index < ui.items.length) {
					element.insertBefore(item, items[index]);
				} else {
					element.appendChild(item);
				}
				ui.items = element.querySelectorAll(self.options.itemSelector);
				self._refresh();
			};

			/**
			 * Remove item on specific layer
			 * @method removeItem
			 * @param {Number} index
			 * @public
			 * @member ns.widget.wearable.Selector
			 */
			prototype.removeItem = function(index) {
				var self = this,
					ui = self._ui,
					element = self.element;

				removeLayers(self.element, self.options);
				element.removeChild(ui.items[index]);
				ui.items = element.querySelectorAll(self.options.itemSelector);
				self._refresh();
			};

			prototype._destroy = function() {
				var self = this;
				unbindEvents(self);
				self._ui = null;
			};

			/**
			 * Disable Selector
			 * @method _disable
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._disable = function() {
				this._enabled = false;
			};

			/**
			 * Enable Selector
			 * @method _enable
			 * @protected
			 * @member ns.widget.wearable.Selector
			 */
			prototype._enable = function() {
				this._enabled = true;
			};

			ns.widget.wearable.Selector = Selector;
			engine.defineWidget(
				"Selector",
				".ui-selector",
				[
					"changeItem",
					"addItem",
					"removeItem",
					"enable",
					"disable"
				],
				Selector,
				"wearable"
			);

			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint plusplus: true, nomen: true */
/**
 * @class tau.helper
 * @author Heeju Joo <heeju.joo@samsung.com>
 */
(function (ns) {
    
                ns.helper = ns.helper || {};
            }(ns));
/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #SnapListStyle Helper Script
 * Helper script using SnapListview.
 * @class ns.helper.SnapListStyle
 * @author Junyoung Park <jy-.park@samsung.com>
 */
(function (document, window, ns) {
	
				var engine = ns.engine,
				objectUtils = ns.util.object,
				selectors = ns.util.selectors,

				SnapListStyle = function (listDomElement, options) {
					var self = this;

					self._snapListviewWidget = null;
					self._callbacks = {};
					self.init(listDomElement, options);
				},

				prototype = SnapListStyle.prototype;

			function rotaryDetentHandler(e) {
				var snapListviewWidget = this._snapListviewWidget,
					selectedIndex = snapListviewWidget.getSelectedIndex(),
					direction = e.detail.direction;

				if (direction === "CW" && selectedIndex !== null) {
					 snapListviewWidget.scrollToPosition(++selectedIndex);
				} else if (direction === "CCW" && selectedIndex !== null) {
					 snapListviewWidget.scrollToPosition(--selectedIndex);
				}
			}

			prototype.init = function(listDomElement, options) {
				var self = this;

				// create SnapListview widget
				self._snapListviewWidget = engine.instanceWidget(listDomElement, "SnapListview", options);
				self.bindEvents();
			};

			prototype.bindEvents = function() {
				var self = this,
					rotaryDetentCallback;

				rotaryDetentCallback = rotaryDetentHandler.bind(self);

				self._callbacks.rotarydetent = rotaryDetentCallback;

				window.addEventListener("rotarydetent", rotaryDetentCallback);
			};

			prototype.unbindEvents = function() {
				var self = this;

				window.removeEventListener("rotarydetent", self._callbacks.rotarydetent);

				self._callbacks.rotarydetent = null;
			};

			prototype.destroy = function() {
				var self = this;

				self.unbindEvents();
				self._snapListviewWidget.destroy();

				self._snapListviewWidget = null;
				self._callbacks = null;
			};

			prototype.getSnapList = function() {
				return this._snapListviewWidget;
			};

			SnapListStyle.create = function(listDomElement, options) {
				return new SnapListStyle(listDomElement, options);
			};

			ns.helper.SnapListStyle = SnapListStyle;
			}(document, window, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #SnapListMarqueeStyle Helper Script
 * Helper script using SnapListview and Marquee.
 * @class ns.helper.SnapListMarqueeStyle
 * @author Heeju Joo <heeju.joo@samsung.com>
 */
(function (document, window, ns) {
	
				var engine = ns.engine,
				objectUtils = ns.util.object,
				defaults = {
					marqueeDelay: 0,
					marqueeStyle: "slide",
					speed: 60,
					iteration: 1,
					timingFunction: "linear",
					ellipsisEffect: "gradient",
					runOnlyOnEllipsisText: true,
					autoRun: false
				},

				SnapListMarqueeStyle = function (listDomElement, options) {
					var self = this;

					self.options = objectUtils.merge({}, defaults);
					self._snapListStyleHelper = null;
					self._selectedMarqueeWidget = null;
					self._callbacks = {};

					self.init(listDomElement, options);
				},

				prototype = SnapListMarqueeStyle.prototype;

			function destroyMarqueeWidget(self) {
				if (self._selectedMarqueeWidget) {
					self._selectedMarqueeWidget.destroy();
					self._selectedMarqueeWidget = null;
				}
			}

			function touchStartHandler() {
				if (this._selectedMarqueeWidget) {
					this._selectedMarqueeWidget.reset();
				}
			}

			function scrollEndHandler() {
				destroyMarqueeWidget(this);
			}

			function selectedHandler(e) {
				var self = this,
					marquee = e.target.querySelector(".ui-marquee");

				destroyMarqueeWidget(self);

				if (marquee) {
					self._selectedMarqueeWidget = engine.instanceWidget(marquee, "Marquee", {
						delay: self.options.marqueeDelay,
						marqueeStyle: self.options.marqueeStyle,
						speed: self.options.speed,
						iteration: self.options.iteration,
						timingFunction: self.options.timingFunction,
						ellipsisEffect: self.options.ellipsisEffect,
						runOnlyOnEllipsisText: self.options.runOnlyOnEllipsisText,
						autoRun: self.options.autoRun
					});
					self._selectedMarqueeWidget.start();
				}
			}

			prototype.init = function(listDomElement, options) {
				var self = this;

				objectUtils.fastMerge(self.options, options);

				self.bindEvents();
				// create SnapListStyle helper
				self._snapListStyleHelper = tau.helper.SnapListStyle.create(listDomElement);
			};

			prototype.bindEvents = function() {
				var self = this,
					touchStartCallback,
					scrollEndCallback,
					selectedCallback;

				touchStartCallback = touchStartHandler.bind(self);
				scrollEndCallback = scrollEndHandler.bind(self);
				selectedCallback = selectedHandler.bind(self);

				self._callbacks.touchStart = touchStartCallback;
				self._callbacks.scrollEnd = scrollEndCallback;
				self._callbacks.selected = selectedCallback;

				document.addEventListener("touchstart", touchStartCallback, false);
				document.addEventListener("scrollend", scrollEndCallback, false);
				document.addEventListener("rotarydetent", touchStartCallback, false);
				document.addEventListener("selected", selectedCallback, false);
			};

			prototype.unbindEvents = function() {
				var self = this;

				document.removeEventListener("touchstart", self._callbacks.touchStart, false);
				document.removeEventListener("scrollend", self._callbacks.scrollEnd, false);
				document.removeEventListener("rotarydetent", self._callbacks.touchStart, false);
				document.removeEventListener("selected", self._callbacks.selected, false);

				self._callbacks.touchStart = null;
				self._callbacks.selected = null;
			};

			prototype.destroy = function() {
				var self = this;

				self.unbindEvents();
				destroyMarqueeWidget(self);
				self._snapListStyleHelper.destroy();

				self.options = null;
				self._snapListStyleHelper = null;
				self._selectedMarqueeWidget = null;
				self._callbacks = null;
			};

			SnapListMarqueeStyle.create = function(listDomElement, options) {
				return new SnapListMarqueeStyle(listDomElement, options);
			};

			ns.helper.SnapListMarqueeStyle = SnapListMarqueeStyle;
			}(document, window, ns));

/*global window, define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #DrawerMoreStyle Helper Script
 * Helper script using drawer, sectionChanger.
 * @class ns.helper.DrawerMoreStyle
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 */
(function (document, window, ns) {
	
				var engine = ns.engine,
				objectUtils = ns.util.object,
				events = ns.event,
				selectors = ns.util.selectors,
				Drawer = ns.widget.wearable.Drawer,
				Selector = ns.widget.wearable.Selector,
				defaults = {
					more: ".ui-more",
					selector: ".ui-selector"
				},
				classes = {
					page: "ui-page"
				},

				DrawerMoreStyle = function (element, options) {
					var self = this;

					self.options = objectUtils.merge({}, defaults);
					self._drawerWidget = null;
					self._handlerElement = null;
					self._selectorWidget = null;

					self.init(element, options);
				},

				prototype = DrawerMoreStyle.prototype;

			function bindDragEvents(element) {

				events.on(element, "touchstart touchend mousedown mouseup" , this, false);
			}

			function unBindDragEvents(element) {

				events.off(element, "touchstart touchend mousedown mouseup" , this, false);
			}

			prototype.handleEvent = function(event) {
				var self = this;
				switch (event.type) {
					case "touchstart":
					case "mousedown":
						self._onTouchStart(event);
						break;
					case "touchend":
					case "mouseup":
						self._onTouchEnd(event);
						break;
				}
			};

			prototype._onTouchStart = function(event) {
				event.preventDefault();
				event.stopPropagation();
			};

			prototype._onTouchEnd = function(event) {
				this._drawerWidget.close();
			};

			prototype.init = function(element, options) {
				var self = this,
					pageElement = selectors.getClosestByClass(element, classes.page),
					handlerElement,
					selectorElement;

				objectUtils.fastMerge(self.options, options);

				handlerElement = pageElement.querySelector(self.options.handler);
				selectorElement = element.querySelector(self.options.selector);

				self._drawerWidget = engine.instanceWidget(element, "Drawer");
				if (handlerElement) {
					self._drawerWidget.setDragHandler(handlerElement);
					self._handlerElement = handlerElement;
					self._bindEvents();
				}
				if (selectorElement) {
					self._selectorWidget = engine.instanceWidget(selectorElement, "Selector");
				}
			};

			prototype._bindEvents = function() {
				var self = this;

				bindDragEvents.call(self, self._handlerElement);
			};

			prototype._unbindEvents = function() {
				var self = this;

				unBindDragEvents.call(self, self._handlerElement);
			};

			prototype.destroy = function() {
				var self = this;

				if (self._handlerElement) {
					self._unbindEvents();
				}
				self._drawerWidget = null;
				self._handlerElement = null;
				self._selectorWidget = null;
			};

			DrawerMoreStyle.create = function(element, options) {
				return new DrawerMoreStyle(element, options);
			};

			ns.helper.DrawerMoreStyle = DrawerMoreStyle;
			}(document, window, ns));

/*global window, define, RegExp */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Path Utility
 * Object helps work with paths.
 * @class ns.util.path
 * @static
 * @author Tomasz Lukawski <t.lukawski@samsung.com>
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 */
(function (window, document, ns) {
	
					/**
				* Local alias for ns.engine
				* @property {Object} engine Alias for {@link ns.engine}
				* @member ns.util.path
				* @static
				* @private
				*/
			var engine = ns.engine,
				/**
				* Local alias for ns.util.object
				* @property {Object} utilsObject Alias for {@link ns.util.object}
				* @member ns.util.path
				* @static
				* @private
				*/
				utilsObject = ns.util.object,
				/**
				* Local alias for ns.util.selectors
				* @property {Object} utilsSelectors Alias for {@link ns.util.selectors}
				* @member ns.util.path
				* @static
				* @private
				*/
				utilsSelectors = ns.util.selectors,
				/**
				* Local alias for ns.util.DOM
				* @property {Object} utilsDOM Alias for {@link ns.util.DOM}
				* @member ns.util.path
				* @static
				* @private
				*/
				utilsDOM = ns.util.DOM,
				/**
				* Cache for document base element
				* @member ns.util.path
				* @property {HTMLBaseElement} base
				* @static
				* @private
				*/
				base,
				/**
				 * location object
				 * @property {Object} location
				 * @static
				 * @private
				 * @member ns.util.path
				 */
				location = {},
				path = {
					/**
					 * href part for mark state
					 * @property {string} [uiStateKey="&ui-state"]
					 * @static
					 * @member ns.util.path
					 */
					uiStateKey: "&ui-state",

					// This scary looking regular expression parses an absolute URL or its relative
					// variants (protocol, site, document, query, and hash), into the various
					// components (protocol, host, path, query, fragment, etc that make up the
					// URL as well as some other commonly used sub-parts. When used with RegExp.exec()
					// or String.match, it parses the URL into a results array that looks like this:
					//
					//	[0]: http://jblas:password@mycompany.com:8080/mail/inbox?msg=1234&type=unread#msg-content?param1=true&param2=123
					//	[1]: http://jblas:password@mycompany.com:8080/mail/inbox?msg=1234&type=unread
					//	[2]: http://jblas:password@mycompany.com:8080/mail/inbox
					//	[3]: http://jblas:password@mycompany.com:8080
					//	[4]: http:
					//	[5]: //
					//	[6]: jblas:password@mycompany.com:8080
					//	[7]: jblas:password
					//	[8]: jblas
					//	[9]: password
					//	[10]: mycompany.com:8080
					//	[11]: mycompany.com
					//	[12]: 8080
					//	[13]: /mail/inbox
					//	[14]: /mail/
					//	[15]: inbox
					//	[16]: ?msg=1234&type=unread
					//	[17]: #msg-content?param1=true&param2=123
					//	[18]: #msg-content
					//	[19]: ?param1=true&param2=123
					//
					/**
					* @property {RegExp} urlParseRE Regular expression for parse URL
					* @member ns.util.path
					* @static
					*/
					urlParseRE: /^(((([^:\/#\?]+:)?(?:(\/\/)((?:(([^:@\/#\?]+)(?:\:([^:@\/#\?]+))?)@)?(([^:\/#\?\]\[]+|\[[^\/\]@#?]+\])(?:\:([0-9]+))?))?)?)?((\/?(?:[^\/\?#]+\/+)*)([^\?#]*)))?(\?[^#]+)?)((#[^\?]*)(\?.*)?)?/,

					/**
					* Abstraction to address xss (Issue #4787) by removing the authority in
					* browsers that auto decode it. All references to location.href should be
					* replaced with a call to this method so that it can be dealt with properly here
					* @method getLocation
					* @param {string|Object} url
					* @return {string}
					* @member ns.util.path
					*/
					getLocation: function (url) {
						var uri = this.parseUrl(url || window.location.href),
							hash = uri.hash,
							search = uri.hashSearch;
						// mimic the browser with an empty string when the hash and hashSearch are empty
						hash = hash === "#" && !search ? "" : hash;
						location = uri;
						// Make sure to parse the url or the location object for the hash because using location.hash
						// is autodecoded in firefox, the rest of the url should be from the object (location unless
						// we're testing) to avoid the inclusion of the authority
						return uri.protocol + "//" + uri.host + uri.pathname + uri.search + hash + search;
					},

					/**
					* Return the original document url
					* @method getDocumentUrl
					* @member ns.util.path
					* @param {boolean} [asParsedObject=false]
					* @return {string|Object}
					* @static
					*/
					getDocumentUrl: function (asParsedObject) {
						return asParsedObject ? utilsObject.copy(path.documentUrl) : path.documentUrl.href;
					},

					/**
					* Parse a location into a structure
					* @method parseLocation
					* @return {Object}
					* @member ns.util.path
					*/
					parseLocation: function () {
						return this.parseUrl(this.getLocation());
					},

					/**
					* Parse a URL into a structure that allows easy access to
					* all of the URL components by name.
					* If we're passed an object, we'll assume that it is
					* a parsed url object and just return it back to the caller.
					* @method parseUrl
					* @member ns.util.path
					* @param {string|Object} url
					* @return {Object} uri record
					* @return {string} return.href
					* @return {string} return.hrefNoHash
					* @return {string} return.hrefNoSearch
					* @return {string} return.domain
					* @return {string} return.protocol
					* @return {string} return.doubleSlash
					* @return {string} return.authority
					* @return {string} return.username
					* @return {string} return.password
					* @return {string} return.host
					* @return {string} return.hostname
					* @return {string} return.port
					* @return {string} return.pathname
					* @return {string} return.directory
					* @return {string} return.filename
					* @return {string} return.search
					* @return {string} return.hash
					* @return {string} return.hashSearch
					* @static
					*/
					parseUrl: function (url) {
						var matches;
						if (typeof url === "object") {
							return url;
						}
						matches = path.urlParseRE.exec(url || "") || [];

							// Create an object that allows the caller to access the sub-matches
							// by name. Note that IE returns an empty string instead of undefined,
							// like all other browsers do, so we normalize everything so its consistent
							// no matter what browser we're running on.
						return {
							href: matches[0] || "",
							hrefNoHash: matches[1] || "",
							hrefNoSearch: matches[2] || "",
							domain: matches[3] || "",
							protocol: matches[4] || "",
							doubleSlash: matches[5] || "",
							authority: matches[6] || "",
							username: matches[8] || "",
							password: matches[9] || "",
							host: matches[10] || "",
							hostname: matches[11] || "",
							port: matches[12] || "",
							pathname: matches[13] || "",
							directory: matches[14] || "",
							filename: matches[15] || "",
							search: matches[16] || "",
							hash: matches[18] || "",
							hashSearch: matches[19] || ""
						};
					},

					/**
					* Turn relPath into an absolute path. absPath is
					* an optional absolute path which describes what
					* relPath is relative to.
					* @method makePathAbsolute
					* @member ns.util.path
					* @param {string} relPath
					* @param {string} [absPath=""]
					* @return {string}
					* @static
					*/
					makePathAbsolute: function (relPath, absPath) {
						var absStack,
							relStack,
							directory,
							i;
						if (relPath && relPath.charAt(0) === "/") {
							return relPath;
						}

						relPath = relPath || "";
						absPath = absPath ? absPath.replace(/^\/|(\/[^\/]*|[^\/]+)$/g, "") : "";

						absStack = absPath ? absPath.split("/") : [];
						relStack = relPath.split("/");
						for (i = 0; i < relStack.length; i++) {
							directory = relStack[i];
							switch (directory) {
							case ".":
								break;
							case "..":
								if (absStack.length) {
									absStack.pop();
								}
								break;
							default:
								absStack.push(directory);
								break;
							}
						}
						return "/" + absStack.join("/");
					},

					/**
					* Returns true if both urls have the same domain.
					* @method isSameDomain
					* @member ns.util.path
					* @param {string|Object} absUrl1
					* @param {string|Object} absUrl2
					* @return {boolean}
					* @static
					*/
					isSameDomain: function (absUrl1, absUrl2) {
						return path.parseUrl(absUrl1).domain === path.parseUrl(absUrl2).domain;
					},

					/**
					* Returns true for any relative variant.
					* @method isRelativeUrl
					* @member ns.util.path
					* @param {string|Object} url
					* @return {boolean}
					* @static
					*/
					isRelativeUrl: function (url) {
						// All relative Url variants have one thing in common, no protocol.
						return path.parseUrl(url).protocol === "";
					},

					/**
					 * Returns true for an absolute url.
					 * @method isAbsoluteUrl
					 * @member ns.util.path
					 * @param {string} url
					 * @return {boolean}
					 * @static
					 */
					isAbsoluteUrl: function (url) {
						return path.parseUrl(url).protocol !== "";
					},

					/**
					* Turn the specified realtive URL into an absolute one. This function
					* can handle all relative variants (protocol, site, document, query, fragment).
					* @method makeUrlAbsolute
					* @member ns.util.path
					* @param {string} relUrl
					* @param {string} absUrl
					* @return {string}
					* @static
					*/
					makeUrlAbsolute: function (relUrl, absUrl) {
						if (!path.isRelativeUrl(relUrl)) {
							return relUrl;
						}

						var relObj = path.parseUrl(relUrl),
							absObj = path.parseUrl(absUrl),
							protocol = relObj.protocol || absObj.protocol,
							doubleSlash = relObj.protocol ? relObj.doubleSlash : (relObj.doubleSlash || absObj.doubleSlash),
							authority = relObj.authority || absObj.authority,
							hasPath = relObj.pathname !== "",
							pathname = path.makePathAbsolute(relObj.pathname || absObj.filename, absObj.pathname),
							search = relObj.search || (!hasPath && absObj.search) || "",
							hash = relObj.hash;

						return protocol + doubleSlash + authority + pathname + search + hash;
					},

					/**
					* Add search (aka query) params to the specified url.
					* If page is embedded page, search query will be added after
					* hash tag. It's allowed to add query content for both external
					* pages and embedded pages.
					* Examples:
					* http://domain/path/index.html#embedded?search=test
					* http://domain/path/external.html?s=query#embedded?s=test
					* @method addSearchParams
					* @member ns.util.path
					* @param {string|Object} url
					* @param {Object|string} params
					* @return {string}
					*/
					addSearchParams: function (url, params) {
						var urlObject = path.parseUrl(url),
							paramsString = (typeof params === "object") ? this.getAsURIParameters(params) : params,
							searchChar = '',
							urlObjectHash = urlObject.hash;

						if (path.isEmbedded(url) && paramsString.length > 0) {
							searchChar = urlObject.hashSearch || "?";
							return urlObject.hrefNoHash + (urlObjectHash || "") + searchChar + (searchChar.charAt(searchChar.length - 1) === "?" ? "" : "&") + paramsString ;
						}

						searchChar = urlObject.search || "?";
						return urlObject.hrefNoSearch + searchChar + (searchChar.charAt(searchChar.length - 1) === "?" ? "" : "&") + paramsString + (urlObjectHash || "");
					},

					/**
					 * Add search params to the specified url with hash
					 * @method addHashSearchParams
					 * @member ns.util.path
					 * @param {string|Object} url
					 * @param {Object|string} params
					 * @returns {string}
					 */
					addHashSearchParams: function (url, params) {
						var urlObject = path.parseUrl(url),
							paramsString = (typeof params === "object") ? path.getAsURIParameters(params) : params,
							hash = urlObject.hash,
							searchChar = hash ? (hash.indexOf("?") < 0 ? hash + "?" : hash + "&") : "#?";
						return urlObject.hrefNoHash + searchChar + (searchChar.charAt(searchChar.length - 1) === "?" ? "" : "&") + paramsString;
					},

					/**
					* Convert absolute Url to data Url
					* - for embedded pages strips parameters
					* - for the same domain as document base remove domain
					* otherwise returns decoded absolute Url
					* @method convertUrlToDataUrl
					* @member ns.util.path
					* @param {string} absUrl
					* @param {string} dialogHashKey
					* @param {Object} documentBase uri structure
					* @return {string}
					* @static
					*/
					convertUrlToDataUrl: function (absUrl, dialogHashKey, documentBase) {
						var urlObject = path.parseUrl(absUrl);

						if (path.isEmbeddedPage(urlObject, !!dialogHashKey)) {
							// Keep hash and search data for embedded page
							return path.getFilePath(urlObject.hash + urlObject.hashSearch, dialogHashKey);
						}
						documentBase = documentBase || path.documentBase;
						if (path.isSameDomain(urlObject, documentBase)) {
							return urlObject.hrefNoHash.replace(documentBase.domain, "");
						}

						return window.decodeURIComponent(absUrl);
					},

					/**
					* Get path from current hash, or from a file path
					* @method get
					* @member ns.util.path
					* @param {string} newPath
					* @return {string}
					*/
					get: function (newPath) {
						if (newPath === undefined) {
							newPath = this.parseLocation().hash;
						}
						return this.stripHash(newPath).replace(/[^\/]*\.[^\/*]+$/, '');
					},

					/**
					* Test if a given url (string) is a path
					* NOTE might be exceptionally naive
					* @method isPath
					* @member ns.util.path
					* @param {string} url
					* @return {boolean}
					* @static
					*/
					isPath: function (url) {
						return (/\//).test(url);
					},

					/**
					* Return a url path with the window's location protocol/hostname/pathname removed
					* @method clean
					* @member ns.util.path
					* @param {string} url
					* @param {Object} documentBase  uri structure
					* @return {string}
					* @static
					*/
					clean: function (url, documentBase) {
						return url.replace(documentBase.domain, "");
					},

					/**
					* Just return the url without an initial #
					* @method stripHash
					* @member ns.util.path
					* @param {string} url
					* @return {string}
					* @static
					*/
					stripHash: function (url) {
						return url.replace(/^#/, "");
					},

					/**
					* Return the url without an query params
					* @method stripQueryParams
					* @member ns.util.path
					* @param {string} url
					* @return {string}
					* @static
					*/
					stripQueryParams: function (url) {
						return url.replace(/\?.*$/, "");
					},

					/**
					* Validation proper hash
					* @method isHashValid
					* @member ns.util.path
					* @param {string} hash
					* @static
					*/
					isHashValid: function (hash) {
						return (/^#[^#]+$/).test(hash);
					},

					/**
					* Check whether a url is referencing the same domain, or an external domain or different protocol
					* could be mailto, etc
					* @method isExternal
					* @member ns.util.path
					* @param {string|Object} url
					* @param {Object} documentUrl uri object
					* @return {boolean}
					* @static
					*/
					isExternal: function (url, documentUrl) {
						var urlObject = path.parseUrl(url);
						return urlObject.protocol && urlObject.domain !== documentUrl.domain ? true : false;
					},

					/**
					* Check if the url has protocol
					* @method hasProtocol
					* @member ns.util.path
					* @param {string} url
					* @return {boolean}
					* @static
					*/
					hasProtocol: function (url) {
						return (/^(:?\w+:)/).test(url);
					},

					/**
					 * Check if the url refers to embedded content
					 * @method isEmbedded
					 * @member ns.util.path
					 * @param {string} url
					 * @returns {boolean}
					 * @static
					 */
					isEmbedded: function (url) {
						var urlObject = path.parseUrl(url);

						if (urlObject.protocol !== "") {
							return (!path.isPath(urlObject.hash) && !!urlObject.hash && (urlObject.hrefNoHash === path.parseLocation().hrefNoHash));
						}
						return (/\?.*#|^#/).test(urlObject.href);
					},

					/**
					* Get the url as it would look squashed on to the current resolution url
					* @method squash
					* @member ns.util.path
					* @param {string} url
					* @param {string} [resolutionUrl=undefined]
					* @return {string}
					*/
					squash: function (url, resolutionUrl) {
						var href,
							cleanedUrl,
							search,
							stateIndex,
							isPath = this.isPath(url),
							uri = this.parseUrl(url),
							preservedHash = uri.hash,
							uiState = "";

						// produce a url against which we can resole the provided path
						resolutionUrl = resolutionUrl || (path.isPath(url) ? path.getLocation() : path.getDocumentUrl());

						// If the url is anything but a simple string, remove any preceding hash
						// eg #foo/bar -> foo/bar
						//	#foo -> #foo
						cleanedUrl = isPath ? path.stripHash(url) : url;

						// If the url is a full url with a hash check if the parsed hash is a path
						// if it is, strip the #, and use it otherwise continue without change
						cleanedUrl = path.isPath(uri.hash) ? path.stripHash(uri.hash) : cleanedUrl;

						// Split the UI State keys off the href
						stateIndex = cleanedUrl.indexOf(this.uiStateKey);

						// store the ui state keys for use
						if (stateIndex > -1) {
							uiState = cleanedUrl.slice(stateIndex);
							cleanedUrl = cleanedUrl.slice(0, stateIndex);
						}

						// make the cleanedUrl absolute relative to the resolution url
						href = path.makeUrlAbsolute(cleanedUrl, resolutionUrl);

						// grab the search from the resolved url since parsing from
						// the passed url may not yield the correct result
						search = this.parseUrl(href).search;

						// @TODO all this crap is terrible, clean it up
						if (isPath) {
							// reject the hash if it's a path or it's just a dialog key
							if (path.isPath(preservedHash) || preservedHash.replace("#", "").indexOf(this.uiStateKey) === 0) {
								preservedHash = "";
							}

							// Append the UI State keys where it exists and it's been removed
							// from the url
							if (uiState && preservedHash.indexOf(this.uiStateKey) === -1) {
								preservedHash += uiState;
							}

							// make sure that pound is on the front of the hash
							if (preservedHash.indexOf("#") === -1 && preservedHash !== "") {
								preservedHash = "#" + preservedHash;
							}

							// reconstruct each of the pieces with the new search string and hash
							href = path.parseUrl(href);
							href = href.protocol + "//" + href.host + href.pathname + search + preservedHash;
						} else {
							href += href.indexOf("#") > -1 ? uiState : "#" + uiState;
						}

						return href;
					},

					/**
					* Check if the hash is preservable
					* @method isPreservableHash
					* @member ns.util.path
					* @param {string} hash
					* @return {boolean}
					*/
					isPreservableHash: function (hash) {
						return hash.replace("#", "").indexOf(this.uiStateKey) === 0;
					},

					/**
					* Escape weird characters in the hash if it is to be used as a selector
					* @method hashToSelector
					* @member ns.util.path
					* @param {string} hash
					* @return {string}
					* @static
					*/
					hashToSelector: function (hash) {
						var hasHash = (hash.substring(0, 1) === "#");
						if (hasHash) {
							hash = hash.substring(1);
						}
						return (hasHash ? "#" : "") + hash.replace(new RegExp('([!"#$%&\'()*+,./:;<=>?@[\\]^`{|}~])', 'g'), "\\$1");
					},

					/**
					* Check if the specified url refers to the first page in the main application document.
					* @method isFirstPageUrl
					* @member ns.util.path
					* @param {string} url
					* @param {Object} documentBase uri structure
					* @param {boolean} documentBaseDiffers
					* @param {Object} documentUrl uri structure
					* @return {boolean}
					* @static
					*/
					isFirstPageUrl: function (url, documentBase, documentBaseDiffers, documentUrl) {
						var urlStructure,
							samePath,
							firstPage,
							firstPageId,
							hash;

						documentBase = documentBase === undefined ? path.documentBase : documentBase;
						documentBaseDiffers = documentBaseDiffers === undefined ? path.documentBaseDiffers : documentBaseDiffers;
						documentUrl = documentUrl === undefined ? path.documentUrl : documentUrl;

						// We only deal with absolute paths.
						urlStructure = path.parseUrl(path.makeUrlAbsolute(url, documentBase));

						// Does the url have the same path as the document?
						samePath = urlStructure.hrefNoHash === documentUrl.hrefNoHash || (documentBaseDiffers && urlStructure.hrefNoHash === documentBase.hrefNoHash);

						// Get the first page element.
						firstPage = engine.getRouter().firstPage;

						// Get the id of the first page element if it has one.
						firstPageId = firstPage ? firstPage.id : undefined;
						hash = urlStructure.hash;

						// The url refers to the first page if the path matches the document and
						// it either has no hash value, or the hash is exactly equal to the id of the
						// first page element.
						return samePath && (!hash || hash === "#" || (firstPageId && hash.replace(/^#/, "") === firstPageId));
					},

					/**
					* Some embedded browsers, like the web view in Phone Gap, allow cross-domain XHR
					* requests if the document doing the request was loaded via the file:// protocol.
					* This is usually to allow the application to "phone home" and fetch app specific
					* data. We normally let the browser handle external/cross-domain urls, but if the
					* allowCrossDomainPages option is true, we will allow cross-domain http/https
					* requests to go through our page loading logic.
					* @method isPermittedCrossDomainRequest
					* @member ns.util.path
					* @param {Object} docUrl
					* @param {string} reqUrl
					* @return {boolean}
					* @static
					*/
					isPermittedCrossDomainRequest: function (docUrl, reqUrl) {
						return ns.getConfig('allowCrossDomainPages', false) &&
							docUrl.protocol === "file:" &&
							reqUrl.search(/^https?:/) !== -1;
					},

					/**
					* Convert a object data to URI parameters
					* @method getAsURIParameters
					* @member ns.util.path
					* @param {Object} data
					* @return {string}
					* @static
					*/
					getAsURIParameters: function (data) {
						var url = '',
							key;
						for (key in data) {
							if (data.hasOwnProperty(key)) {
								url += encodeURIComponent(key) + '=' + encodeURIComponent(data[key]) + '&';
							}
						}
						return url.substring(0, url.length - 1);
					},

					/**
					* Document Url
					* @member ns.util.path
					* @property {string|null} documentUrl
					*/
					documentUrl: null,

					/**
					* The document base differs
					* @member ns.util.path
					* @property {boolean} documentBaseDiffers
					*/
					documentBaseDiffers: false,

					/**
					* Set location hash to path
					* @method set
					* @member ns.util.path
					* @param {string} path
					* @static
					*/
					set: function (path) {
						location.hash = path;
					},

					/**
					* Return the substring of a file path before the sub-page key,
					* for making a server request
					* @method getFilePath
					* @member ns.util.path
					* @param {string} path
					* @param {string} dialogHashKey
					* @return {string}
					* @static
					*/
					getFilePath: function (path, dialogHashKey) {
						var splitkey = '&' + ns.getConfig('subPageUrlKey', '');
						return path && path.split(splitkey)[0].split(dialogHashKey)[0];
					},

					/**
					* Remove the preceding hash, any query params, and dialog notations
					* @method cleanHash
					* @member ns.util.path
					* @param {string} hash
					* @param {string} dialogHashKey
					* @return {string}
					* @static
					*/
					cleanHash: function (hash, dialogHashKey) {
						return path.stripHash(hash.replace(/\?.*$/, "").replace(dialogHashKey, ""));
					},

					/**
					* Check if url refers to the embedded page
					* @method isEmbeddedPage
					* @member ns.util.path
					* @param {string} url
					* @param {boolean} allowEmbeddedOnlyBaseDoc
					* @return {boolean}
					* @static
					*/
					isEmbeddedPage: function (url, allowEmbeddedOnlyBaseDoc) {
						var urlObject = path.parseUrl(url);

						//if the path is absolute, then we need to compare the url against
						//both the documentUrl and the documentBase. The main reason for this
						//is that links embedded within external documents will refer to the
						//application document, whereas links embedded within the application
						//document will be resolved against the document base.
						if (urlObject.protocol !== "") {
							return (urlObject.hash &&
									( allowEmbeddedOnlyBaseDoc ?
											urlObject.hrefNoHash === path.documentUrl.hrefNoHash :
											urlObject.hrefNoHash === path.parseLocation().hrefNoHash ));
						}
						return (/^#/).test(urlObject.href);
					}
				};

			path.documentUrl = path.parseLocation();

			base = document.querySelector('base');

			/**
			* The document base URL for the purposes of resolving relative URLs,
			* and the name of the default browsing context for the purposes of
			* following hyperlinks
			* @member ns.util.path
			* @property {Object} documentBase uri structure
			* @static
			*/
			path.documentBase = base ? path.parseUrl(path.makeUrlAbsolute(base.getAttribute("href"), path.documentUrl.href)) : path.documentUrl;

			path.documentBaseDiffers = (path.documentUrl.hrefNoHash !== path.documentBase.hrefNoHash);

			/**
			* Get document base
			* @method getDocumentBase
			* @member ns.util.path
			* @param {boolean} [asParsedObject=false]
			* @return {string|Object}
			* @static
			*/
			path.getDocumentBase = function (asParsedObject) {
				return asParsedObject ? utilsObject.copy(path.documentBase) : path.documentBase.href;
			};

			/**
			* Find the closest page and extract out its url
			* @method getClosestBaseUrl
			* @member ns.util.path
			* @param {HTMLElement} element
			* @param {string} selector
			* @return {string}
			* @static
			*/
			path.getClosestBaseUrl = function (element, selector) {
				// Find the closest page and extract out its url.
				var url = utilsDOM.getNSData(utilsSelectors.getClosestBySelector(element, selector), "url"),
					baseUrl = path.documentBase.hrefNoHash;

				if (!ns.getConfig('dynamicBaseEnabled', true) || !url || !path.isPath(url)) {
					url = baseUrl;
				}

				return path.makeUrlAbsolute(url, baseUrl);
			};

			ns.util.path = path;
			}(window, window.document, ns));

/*global define, ns */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Route Namespace
 * Object contains rules for router.
 *
 * @class ns.router.route
 */
/*
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 */
(function (ns) {
	
				ns.router.route = ns.router.route || {};
			}(ns));

/*global window, define, XMLHttpRequest, Node, HTMLElement, ns */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Router
 * Main class to navigate between pages and popups in profile Wearable.
 *
 * @class ns.router.Router
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Piotr Karny <p.karny@samsung.com>
 * @author Tomasz Lukawski <t.lukawski@samsung.com>
 */
(function (window, document, ns) {
	
					/**
				 * Local alias for ns.util
				 * @property {Object} util Alias for {@link ns.util}
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
			var util = ns.util,
				/**
				 * Local alias for ns.event
				 * @property {Object} eventUtils Alias for {@link ns.event}
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				eventUtils = ns.event,
				/**
				 * Alias for {@link ns.util.DOM}
				 * @property {Object} DOM
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				DOM = util.DOM,
				/**
				 * Local alias for ns.util.path
				 * @property {Object} path Alias for {@link ns.util.path}
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				path = util.path,
				/**
				 * Local alias for ns.util.selectors
				 * @property {Object} selectors Alias for {@link ns.util.selectors}
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				selectors = util.selectors,
				/**
				 * Local alias for ns.util.object
				 * @property {Object} object Alias for {@link ns.util.object}
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				object = util.object,
				/**
				 * Local alias for ns.engine
				 * @property {Object} engine Alias for {@link ns.engine}
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				engine = ns.engine,
				/**
				 * Local alias for ns.router
				 * @property {Object} routerMicro Alias for namespace ns.router
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				routerMicro = ns.router,
				/**
				 * Local alias for ns.router.history
				 * @property {Object} history Alias for {@link ns.router.history}
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				history = routerMicro.history,
				/**
				 * Local alias for ns.router.route
				 * @property {Object} route Alias for namespace ns.router.route
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				route = routerMicro.route,
				/**
				 * Local alias for document body element
				 * @property {HTMLElement} body
				 * @member ns.router.Router
				 * @static
				 * @private
				 */
				body = document.body,
				/**
				 * Alias to Array.slice method
				 * @method slice
				 * @member ns.router.Router
				 * @private
				 * @static
				 */
				slice = [].slice,

				/**
				 * Router locking flag
				 * @property {boolean} [_isLock]
				 * @member ns.router.Router
				 * @private
				 */
				_isLock = false,

				Page = ns.widget.core.Page,

				Router = function () {
					var self = this;

					/**
					 * Element of the page opened as first.
					 * @property {?HTMLElement} [firstPage]
					 * @member ns.router.Router
					 */
					self.firstPage = null;
					/**
					 * The container of widget.
					 * @property {?ns.widget.core.PageContainer} [container]
					 * @member ns.router.Router
					 */
					self.container = null;
					/**
					 * Settings for last open method
					 * @property {Object} [settings]
					 * @member ns.router.Router
					 */
					self.settings = {};
				};

			/**
			 * Default values for router
			 * @property {Object} defaults
			 * @property {boolean} [defaults.fromHashChange = false] Sets if will be changed after hashchange.
			 * @property {boolean} [defaults.reverse = false] Sets the direction of change.
			 * @property {boolean} [defaults.showLoadMsg = true] Sets if message will be shown during loading.
			 * @property {number} [defaults.loadMsgDelay = 0] Sets delay time for the show message during loading.
			 * @property {boolean} [defaults.volatileRecord = false] Sets if the current history entry will be modified or a new one will be created.
			 * @member ns.router.Router
			 */
			Router.prototype.defaults = {
				fromHashChange: false,
				reverse: false,
				showLoadMsg: true,
				loadMsgDelay: 0,
				volatileRecord: false
			};

			/**
			 * Find the closest link for element
			 * @method findClosestLink
			 * @param {HTMLElement} element
			 * @return {HTMLElement}
			 * @private
			 * @static
			 * @member ns.router.Router
			 */
			function findClosestLink(element) {
				while (element) {
					if (element.nodeType === Node.ELEMENT_NODE && element.nodeName && element.nodeName === "A") {
						break;
					}
					element = element.parentNode;
				}
				return element;
			}

			/**
			 * Handle event link click
			 * @method linkClickHandler
			 * @param {ns.router.Router} router
			 * @param {Event} event
			 * @private
			 * @static
			 * @member ns.router.Router
			 */
			function linkClickHandler(router, event) {
				var link = findClosestLink(event.target),
					href,
					useDefaultUrlHandling,
					options;

				if (link && event.which === 1) {
					href = link.getAttribute("href");
					useDefaultUrlHandling = (link.getAttribute("rel") === "external") || link.hasAttribute("target");
					if (!useDefaultUrlHandling) {
						options = DOM.getData(link);
						router.open(href, options, event);
						eventUtils.preventDefault(event);
					}
				}
			}

			/**
			 * Handle event for pop state
			 * @method popStateHandler
			 * @param {ns.router.Router} router
			 * @param {Event} event
			 * @private
			 * @static
			 * @member ns.router.Router
			 */
			function popStateHandler(router, event) {
				var state = event.state,
					prevState = history.activeState,
					rules = routerMicro.route,
					ruleKey,
					options,
					to,
					url,
					isContinue = true,
					reverse,
					transition;

				if (_isLock) {
					history.disableVolatileMode();
					history.replace(prevState, prevState.stateTitle, prevState.stateUrl);
					return;
				}

				if (state) {
					to = state.url;
					reverse = history.getDirection(state) === "back";
					transition = reverse ? ((prevState && prevState.transition) || "none") : state.transition;
					options = object.merge({}, state, {
						reverse: reverse,
						transition: transition,
						fromHashChange: true
					});

					url = path.getLocation();

					for (ruleKey in rules) {
						if (rules.hasOwnProperty(ruleKey) && rules[ruleKey].onHashChange(url, options, prevState.stateUrl)) {
							isContinue = false;
						}
					}

					history.setActive(state);

					if (isContinue) {
						router.open(to, options);
					}
				} else {
					url = path.getLocation();
					if (prevState) {
						if (prevState.absUrl !== url && prevState.stateUrl !== url) {
							history.enableVolatileRecord();
							router.open(url);
						}
					}
				}
			}

			/**
			 * Detect rel attribute from HTMLElement
			 * @param {HTMLElement} to
			 * @member ns.router.Router
			 * @method detectRel
			 */
			Router.prototype.detectRel = function (to) {
				var rule,
					i;

				for (i in route) {
					rule = route[i];
					if (selectors.matchesSelector(to, rule.filter)) {
						return i;
					}
				}
			};

			/**
			 * Change page to page given in parameter "to".
			 * @method open
			 * @param {string|HTMLElement} to Id of page or file url or HTMLElement of page
			 * @param {Object} [options]
			 * @param {"page"|"popup"|"external"} [options.rel = "page"] Represents kind of link as "page" or "popup" or "external" for linking to another domain.
			 * @param {string} [options.transition = "none"] Sets the animation used during change of page.
			 * @param {boolean} [options.reverse = false] Sets the direction of change.
			 * @param {boolean} [options.fromHashChange = false] Sets if will be changed after hashchange.
			 * @param {boolean} [options.showLoadMsg = true] Sets if message will be shown during loading.
			 * @param {number} [options.loadMsgDelay = 0] Sets delay time for the show message during loading.
			 * @param {boolean} [options.volatileRecord = false] Sets if the current history entry will be modified or a new one will be created.
			 * @param {boolean} [options.dataUrl] Sets if page has url attribute.
			 * @param {?string} [options.container = null] It is used in RoutePopup as selector for container.
			 * @member ns.router.Router
			 */
			Router.prototype.open = function (to, options, event) {
				var rel,
					rule,
					deferred = {},
					filter,
					stringId,
					toElement,
					self = this;

				to = getHTMLElement(to);
				rel = ((options && options.rel) || (to instanceof HTMLElement && this.detectRel(to)) || "page");
					rule = route[rel];
				if (_isLock) {
					return;
				}

				if (rel === "back") {
					history.back();
					return;
				}

				if (rule) {
					options = object.merge(
						{
							rel: rel
						},
						this.defaults,
						rule.option(),
						options
					);
					filter = rule.filter;
					deferred.resolve = function (options, content) {
						rule.open(content, options, event);
					};
					deferred.reject = function (options) {
						eventUtils.trigger(self.container.element, "changefailed", options);
					};
					if (typeof to === "string") {
						if (to.replace(/[#|\s]/g, "")) {
							this._loadUrl(to, options, rule, deferred);
						}
					} else {
						if (to && selectors.matchesSelector(to, filter)) {
							deferred.resolve(options, to);
						} else {
							deferred.reject(options);
						}
					}
				} else {
					throw new Error("Not defined router rule [" + rel + "]");
				}
			};

			/**
			 * Method initializes page container and builds the first page if flag autoInitializePage is set.
			 * @method init
			 * @param {boolean} justBuild
			 * @member ns.router.Router
			 */
			Router.prototype.init = function (justBuild) {
				var page,
					containerElement,
					container,
					firstPage,
					pages,
					activePages,
					ruleKey,
					rules = routerMicro.route,
					location = window.location,
					PageClasses = Page.classes,
					uiPageActiveClass = PageClasses.uiPageActive,
					pageDefinition = ns.engine.getWidgetDefinition("Page"),
					pageSelector = pageDefinition.selector,
					self = this;

				body = document.body;
				containerElement = ns.getConfig("pageContainer") || body;
				pages = slice.call(containerElement.querySelectorAll(pageSelector));
				if (!ns.getConfig("pageContainerBody", false)) {
					containerElement = pages.length ? pages[0].parentNode : containerElement;
				}
				self.justBuild = justBuild;

				if (ns.getConfig("autoInitializePage", true)) {
					firstPage = containerElement.querySelector("." + uiPageActiveClass);
					if (!firstPage) {
						firstPage = pages[0];
					}

					if (firstPage) {
						activePages = containerElement.querySelectorAll("." + uiPageActiveClass);
						slice.call(activePages).forEach(function (page) {
							page.classList.remove("." + uiPageActiveClass);
						});
					}

					if (location.hash) {
						//simple check to determine if we should show firstPage or other
						page = document.getElementById(location.hash.replace("#", ""));
						if (page && selectors.matchesSelector(page, pageSelector)) {
							firstPage = page;
						}
					}

					if (!firstPage && ns.getConfig("addPageIfNotExist", true)) {
						firstPage = Page.createEmptyElement();
						while(containerElement.firstChild) {
							firstPage.appendChild(containerElement.firstChild);
						}
						containerElement.appendChild(firstPage);
					}

					if (justBuild) {
												//engine.createWidgets(containerElement, true);
						container = engine.instanceWidget(containerElement, "pagecontainer");
						if (firstPage) {
							self.register(container, firstPage);
						}
						return;
					}
				}

				for (ruleKey in rules) {
					if (rules.hasOwnProperty(ruleKey) && rules[ruleKey].init) {
						rules[ruleKey].init();
					}
				}

				container = engine.instanceWidget(containerElement, "pagecontainer");
				self.register(container, firstPage);
			};

			/**
			 * Method removes all events listners set by router.
			 * @method destroy
			 * @member ns.router.Router
			 */
			Router.prototype.destroy = function () {
				var self = this;
				window.removeEventListener("popstate", self.popStateHandler, false);
				if (body) {
					body.removeEventListener("pagebeforechange", self.pagebeforechangeHandler, false);
					body.removeEventListener("vclick", self.linkClickHandler, false);
				}
			};

			/**
			 * Method sets container.
			 * @method setContainer
			 * @param {ns.widget.core.PageContainer} container
			 * @member ns.router.Router
			 */
			Router.prototype.setContainer = function (container) {
				this.container = container;
			};

			/**
			 * Method returns container.
			 * @method getContainer
			 * @return {ns.widget.core.PageContainer} container of widget
			 * @member ns.router.Router
			 */
			Router.prototype.getContainer = function () {
				return this.container;
			};

			/**
			 * Method returns ths first page.
			 * @method getFirstPage
			 * @return {HTMLElement} the first page
			 * @member ns.router.Router
			 */
			Router.prototype.getFirstPage = function () {
				return this.firstPage;
			};

			/**
			 * Method registers page container and the first page.
			 * @method register
			 * @param {ns.widget.core.PageContainer} container
			 * @param {HTMLElement} firstPage
			 * @member ns.router.Router
			 */
			Router.prototype.register = function (container, firstPage) {
				var self = this;
				self.container = container;
				self.firstPage = firstPage;

				self.linkClickHandler = linkClickHandler.bind(null, self);
				self.popStateHandler = popStateHandler.bind(null, self);

				document.addEventListener("vclick", self.linkClickHandler, false);
				window.addEventListener("popstate", self.popStateHandler, false);

				eventUtils.trigger(document, "themeinit", self);

				if (ns.getConfig("loader", false)) {
					container.element.appendChild(self.getLoader().element);
				}
				history.enableVolatileRecord();
				if (firstPage) {
					self.open(firstPage, { transition: "none" });
				}
				this.getRoute("popup").setActive(null);
			};

			/**
			 * Convert string id to HTMLElement or return HTMLElement if is given
			 * @method getHTMLElement
			 * @param {string|HTMLElement} idOrElement
			 * @returns {HTMLElement}
			 */
			function getHTMLElement(idOrElement) {
				var stringId,
					toElement;
				if (typeof idOrElement === "string") {
					if (idOrElement[0] === "#") {
						stringId = idOrElement.substr(1);
					} else {
						stringId = idOrElement;
					}
					toElement = document.getElementById(stringId);
					if (toElement) {
						idOrElement = toElement;
					}
				}
				return idOrElement;
			}

			/*
			* Method close route element, eg page or popup.
			* @method close
			* @param {string|HTMLElement} to Id of page or file url or HTMLElement of page
			* @param {Object} [options]
			* @param {"page"|"popup"|"external"} [options.rel = "page"] Represents kind of link as "page" or "popup" or "external" for linking to another domain
			* @member ns.router.Router
			*/
			Router.prototype.close = function (to, options) {
				var rel = (options && options.rel) || "back",
					rule = route[rel];

				if (rel === "back") {
					history.back();
				} else {
					if (rule) {
						rule.close(getHTMLElement(to), options);
					} else {
						throw new Error("Not defined router rule [" + rel + "]");
					}
				}
			};

			/**
			 * Method opens popup.
			 * @method openPopup
			 * @param {HTMLElement|string} to Id or HTMLElement of popup.
			 * @param {Object} [options]
			 * @param {string} [options.transition = "none"] Sets the animation used during change of page.
			 * @param {boolean} [options.reverse = false] Sets the direction of change.
			 * @param {boolean} [options.fromHashChange = false] Sets if will be changed after hashchange.
			 * @param {boolean} [options.showLoadMsg = true] Sets if message will be shown during loading.
			 * @param {number} [options.loadMsgDelay = 0] Sets delay time for the show message during loading.
			 * @param {boolean} [options.volatileRecord = false] Sets if the current history entry will be modified or a new one will be created.
			 * @param {boolean} [options.dataUrl] Sets if page has url attribute.
			 * @param {?string} [options.container = null] It is used in RoutePopup as selector for container.
			 * @member ns.router.Router
			 */
			Router.prototype.openPopup = function (to, options) {
				this.open(to, object.fastMerge({rel: "popup"}, options));
			};

			/**
			 * Method closes popup.
			 * @method closePopup
			 * @param {Object} options
			 * @param {string=} [options.transition]
			 * @param {string=} [options.ext= in ui-pre-in] options.ext
			 * @member ns.router.Router
			 */
			Router.prototype.closePopup = function (options) {
				var popupRoute = this.getRoute("popup");

				if (popupRoute) {
					popupRoute.close(null, options);
				}
			};

			Router.prototype.lock = function () {
				_isLock = true;
			};

			Router.prototype.unlock = function () {
				_isLock = false;
			};

			/**
			 * Load content from url
			 * @method _loadUrl
			 * @param {string} url
			 * @param {Object} options
			 * @param {"page"|"popup"|"external"} [options.rel = "page"] Represents kind of link as "page" or "popup" or "external" for linking to another domain.
			 * @param {string} [options.transition = "none"] Sets the animation used during change of page.
			 * @param {boolean} [options.reverse = false] Sets the direction of change.
			 * @param {boolean} [options.fromHashChange = false] Sets if will be changed after hashchange.
			 * @param {boolean} [options.showLoadMsg = true] Sets if message will be shown during loading.
			 * @param {number} [options.loadMsgDelay = 0] Sets delay time for the show message during loading.
			 * @param {boolean} [options.volatileRecord = false] Sets if the current history entry will be modified or a new one will be created.
			 * @param {boolean} [options.dataUrl] Sets if page has url attribute.
			 * @param {?string} [options.container = null] It is used in RoutePopup as selector for container.
			 * @param {string} [options.absUrl] Absolute Url for content used by deferred object.
			 * @param {Object} rule
			 * @param {Object} deferred
			 * @param {Function} deferred.reject
			 * @param {Function} deferred.resolve
			 * @member ns.router.Router
			 * @protected
			 */
			Router.prototype._loadUrl = function (url, options, rule, deferred) {
				var absUrl = path.makeUrlAbsolute(url, path.getLocation()),
					content,
					request,
					detail = {},
					self = this;

				// If the caller provided data append the data to the URL.
				if (options.data) {
					absUrl = path.addSearchParams(absUrl, options.data);
					options.data = undefined;
				}

				content = rule.find(absUrl);

				if (!content && path.isEmbedded(absUrl)) {
					deferred.reject(detail);
					return;
				}
				// If the content we are interested in is already in the DOM,
				// and the caller did not indicate that we should force a
				// reload of the file, we are done. Resolve the deferrred so that
				// users can bind to .done on the promise
				if (content) {
					detail = object.fastMerge({absUrl: absUrl}, options);
					deferred.resolve(detail, content);
					return;
				}

				if (options.showLoadMsg) {
					self._showLoading(options.loadMsgDelay);
				}

				// Load the new content.
				request = new XMLHttpRequest();
				request.responseType = "document";
				request.overrideMimeType("text/html");
				request.open("GET", absUrl);
				request.addEventListener("error", self._loadError.bind(self, absUrl, options, deferred));
				request.addEventListener("load", function (event) {
					var request = event.target;
					if (request.readyState === 4) {
						if (request.status === 200 || (request.status === 0 && request.responseXML)) {
							self._loadSuccess(absUrl, options, rule, deferred, request.responseXML);
						} else {
							self._loadError(absUrl, options, deferred);
						}
					}
				});
				request.send();
			};

			/**
			 * Error handler for loading content by AJAX
			 * @method _loadError
			 * @param {string} absUrl
			 * @param {Object} options
			 * @param {"page"|"popup"|"external"} [options.rel = "page"] Represents kind of link as "page" or "popup" or "external" for linking to another domain.
			 * @param {string} [options.transition = "none"] Sets the animation used during change of page.
			 * @param {boolean} [options.reverse = false] Sets the direction of change.
			 * @param {boolean} [options.fromHashChange = false] Sets if will be changed after hashchange.
			 * @param {boolean} [options.showLoadMsg = true] Sets if message will be shown during loading.
			 * @param {number} [options.loadMsgDelay = 0] Sets delay time for the show message during loading.
			 * @param {boolean} [options.volatileRecord = false] Sets if the current history entry will be modified or a new one will be created.
			 * @param {boolean} [options.dataUrl] Sets if page has url attribute.
			 * @param {?string} [options.container = null] It is used in RoutePopup as selector for container.
			 * @param {string} [options.absUrl] Absolute Url for content used by deferred object.
			 * @param {Object} deferred
			 * @param {Function} deferred.reject
			 * @member ns.router.Router
			 * @protected
			 */
			Router.prototype._loadError = function (absUrl, options, deferred) {
				var detail = object.fastMerge({url: absUrl}, options),
					self = this;
				// Remove loading message.
				if (options.showLoadMsg) {
					self._showError(absUrl);
				}

				eventUtils.trigger(self.container.element, "loadfailed", detail);
				deferred.reject(detail);
			};

			// TODO it would be nice to split this up more but everything appears to be "one off"
			//	or require ordering such that other bits are sprinkled in between parts that
			//	could be abstracted out as a group
			/**
			 * Success handler for loading content by AJAX
			 * @method _loadSuccess
			 * @param {string} absUrl
			 * @param {Object} options
			 * @param {"page"|"popup"|"external"} [options.rel = "page"] Represents kind of link as "page" or "popup" or "external" for linking to another domain.
			 * @param {string} [options.transition = "none"] Sets the animation used during change of page.
			 * @param {boolean} [options.reverse = false] Sets the direction of change.
			 * @param {boolean} [options.fromHashChange = false] Sets if will be changed after hashchange.
			 * @param {boolean} [options.showLoadMsg = true] Sets if message will be shown during loading.
			 * @param {number} [options.loadMsgDelay = 0] Sets delay time for the show message during loading.
			 * @param {boolean} [options.volatileRecord = false] Sets if the current history entry will be modified or a new one will be created.
			 * @param {boolean} [options.dataUrl] Sets if page has url attribute.
			 * @param {?string} [options.container = null] It is used in RoutePopup as selector for container.
			 * @param {string} [options.absUrl] Absolute Url for content used by deferred object.
			 * @param {Object} rule
			 * @param {Object} deferred
			 * @param {Function} deferred.reject
			 * @param {Function} deferred.resolve
			 * @param {string} html
			 * @member ns.router.Router
			 * @protected
			 */
			Router.prototype._loadSuccess = function (absUrl, options, rule, deferred, html) {
				var detail = object.fastMerge({url: absUrl}, options),
					content = rule.parse(html, absUrl);

				// Remove loading message.
				if (options.showLoadMsg) {
					this._hideLoading();
				}

				if (content) {
					deferred.resolve(detail, content);
				} else {
					deferred.reject(detail);
				}
			};

			// TODO the first page should be a property set during _create using the logic
			//	that currently resides in init
			/**
			 * Get initial content
			 * @method _getInitialContent
			 * @member ns.router.Router
			 * @return {HTMLElement} the first page
			 * @protected
			 */
			Router.prototype._getInitialContent = function () {
				return this.firstPage;
			};

			/**
			 * Show the loading indicator
			 * @method _showLoading
			 * @param {number} delay
			 * @member ns.router.Router
			 * @protected
			 */
			Router.prototype._showLoading = function (delay) {
				this.container.showLoading(delay);
			};

			/**
			 * Report an error loading
			 * @method _showError
			 * @param {string} absUrl
			 * @member ns.router.Router
			 * @protected
			 */
			Router.prototype._showError = function (absUrl) {
				ns.error("load error, file: ", absUrl);
			};

			/**
			 * Hide the loading indicator
			 * @method _hideLoading
			 * @member ns.router.Router
			 * @protected
			 */
			Router.prototype._hideLoading = function () {
				this.container.hideLoading();
			};

			/**
			 * Returns true if popup is active.
			 * @method hasActivePopup
			 * @return {boolean}
			 * @member ns.router.Router
			 */
			Router.prototype.hasActivePopup = function () {
				var popup = this.getRoute("popup");
				return popup && popup.hasActive();
			};

			/**
			 * This function returns proper route.
			 * @method getRoute
			 * @param {string} Type of route
			 * @return {?ns.router.route.interface}
			 * @member ns.router.Router
			 */
			Router.prototype.getRoute = function (type) {
				return route[type];
			};


			/**
			 * Returns loader widget
			 * @return {ns.widget.mobile.Loader}
			 * @member ns.router.Page
			 * @method getLoader
			 */
			Router.prototype.getLoader = function () {
				var loaderElement = document.querySelector("[data-role=loader],.ui-loader");

				if (!loaderElement) {
					loaderElement = document.createElement("div");
					DOM.setNSData(loaderElement, "role", "loader");
				}

				return engine.instanceWidget(loaderElement, "Loader");
			};

			routerMicro.Router = Router;

			engine.initRouter(Router);
			}(window, window.document, ns));

/*global window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Route Page
 * Support class for router to control changing pages.
 * @class ns.router.route.page
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 */
(function (document, ns) {
	
				var util = ns.util,
				path = util.path,
				DOM = util.DOM,
				object = util.object,
				utilSelector = util.selectors,
				history = ns.router.history,
				engine = ns.engine,
				Page = ns.widget.core.Page,
				baseElement,
				routePage = {},
				head;

			/**
			 * Tries to find a page element matching id and filter (selector).
			 * Adds data url attribute to found page, sets page = null when nothing found
			 * @method findPageAndSetDataUrl
			 * @param {string} dataUrl DataUrl of searching element
			 * @param {string} filter Query selector for searching page
			 * @return {?HTMLElement}
			 * @private
			 * @static
			 * @member ns.router.route.page
			 */
			function findPageAndSetDataUrl(dataUrl, filter) {
				var id = path.stripQueryParams(dataUrl).replace("#", ""),
					page = document.getElementById(id);

				if (page && utilSelector.matchesSelector(page, filter)) {
					if (dataUrl === id) {
						DOM.setNSData(page, "url", "#" + id);
					} else {
						DOM.setNSData(page, "url", dataUrl);
					}

				} else {
					// if we matched any element, but it doesn't match our filter
					// reset page to null
					page = null;
				}
				// @TODO ... else
				// probably there is a need for running onHashChange while going back to a history entry
				// without state, eg. manually entered #fragment. This may not be a problem on target device
				return page;
			}

			/**
			 * Property containing default properties
			 * @property {Object} defaults
			 * @property {string} defaults.transition="none"
			 * @static
			 * @member ns.router.route.page
			 */
			routePage.defaults = {
				transition: "none"
			};

			/**
			 * Property defining selector for filtering only page elements
			 * @property {string} filter
			 * @member ns.router.route.page
			 * @static
			 */
			routePage.filter = engine.getWidgetDefinition("Page").selector;

			/**
			 * Returns default route options used inside Router.
			 * @method option
			 * @static
			 * @member ns.router.route.page
			 * @return {Object} default route options
			 */
			routePage.option = function () {
				var defaults = object.merge({}, routePage.defaults);
				defaults.transition = ns.getConfig('pageTransition', defaults.transition);
				return defaults;
			};

			routePage.init = function() {
				var pages = [].slice.call(document.querySelectorAll(this.filter));
				pages.forEach(function (page) {
					if (!DOM.getNSData(page, "url")) {
						DOM.setNSData(page, "url", (page.id && "#" + page.id) || location.pathname + location.search);
					}
				});
			};

			/**
			 * This method changes page. It sets history and opens page passed as a parameter.
			 * @method open
			 * @param {HTMLElement|string} toPage The page which will be opened.
			 * @param {Object} [options]
			 * @param {boolean} [options.fromHashChange] Sets if call was made on hash change.
			 * @param {string} [options.dataUrl] Sets if page has url attribute.
			 * @member ns.router.route.page
			 */
			routePage.open = function (toPage, options) {
				var pageTitle = document.title,
					url,
					state = {},
					router = engine.getRouter();

				if (toPage === router.getFirstPage() && !options.dataUrl) {
					url = path.documentUrl.hrefNoHash;
				} else {
					url = DOM.getNSData(toPage, "url");
				}

				pageTitle = DOM.getNSData(toPage, "title") || utilSelector.getChildrenBySelector(toPage, ".ui-header > .ui-title").textContent || pageTitle;
				if (!DOM.getNSData(toPage, "title")) {
					DOM.setNSData(toPage, "title", pageTitle);
				}

				if (url && !options.fromHashChange) {
					if (!path.isPath(url) && url.indexOf("#") < 0) {
						url = path.makeUrlAbsolute("#" + url, path.documentUrl.hrefNoHash);
					}

					state = object.merge(
						{},
						options,
						{
							url: url
						}
					);

					history.replace(state, pageTitle, url);
				}

				// write base element
				this._setBase(url);

				//set page title
				document.title = pageTitle;
				this.getContainer().change(toPage, options);
			};

			/**
			 * This method determines target page to open.
			 * @method find
			 * @param {string} absUrl Absolute path to opened page
			 * @member ns.router.route.page
			 * @return {?HTMLElement} Element of page to open.
			 */
			routePage.find = function (absUrl) {
				var self = this,
					router = engine.getRouter(),
					dataUrl = self._createDataUrl(absUrl),
					initialContent = router.getFirstPage(),
					pageContainer = router.getContainer(),
					page,
					selector = "[data-url='" + dataUrl + "']",
					filterRegexp = /,/gm;

				if (/#/.test(absUrl) && path.isPath(dataUrl)) {
					return null;
				}

				// Check to see if the page already exists in the DOM.
				// NOTE do _not_ use the :jqmData pseudo selector because parenthesis
				//      are a valid url char and it breaks on the first occurence
				// prepare selector for new page
				selector += self.filter.replace(filterRegexp, ",[data-url='" + dataUrl + "']");
				page = pageContainer.element.querySelector(selector);

				// If we failed to find the page, check to see if the url is a
				// reference to an embedded page. If so, it may have been dynamically
				// injected by a developer, in which case it would be lacking a
				// data-url attribute and in need of enhancement.
				if (!page && dataUrl && !path.isPath(dataUrl)) {
					//Remove search data
					page = findPageAndSetDataUrl(dataUrl, self.filter);
				}

				// If we failed to find a page in the DOM, check the URL to see if it
				// refers to the first page in the application. Also check to make sure
				// our cached-first-page is actually in the DOM. Some user deployed
				// apps are pruning the first page from the DOM for various reasons.
				// We check for this case here because we don't want a first-page with
				// an id falling through to the non-existent embedded page error case.
				if (!page &&
						path.isFirstPageUrl(dataUrl) &&
						initialContent) {
					page = initialContent;
				}

				return page;
			};

			/**
			 * This method parses HTML and runs scripts from parsed code.
			 * Fetched external scripts if required.
			 * Sets document base to parsed document absolute path.
			 * @method parse
			 * @param {string} html HTML code to parse
			 * @param {string} absUrl Absolute url for parsed page
			 * @member ns.router.route.page
			 * @return {?HTMLElement} Element of page in parsed document.
			 */
			routePage.parse = function (html, absUrl) {
				var self = this,
					page,
					dataUrl = self._createDataUrl(absUrl);

				// write base element
				// @TODO shouldn't base be set if a page was found?
				self._setBase(absUrl);

				// Finding matching page inside created element
				page = html.querySelector(self.filter);

				// If a page exists...
				if (page) {
					// TODO tagging a page with external to make sure that embedded pages aren't
					// removed by the various page handling code is bad. Having page handling code
					// in many places is bad. Solutions post 1.0
					DOM.setNSData(page, "url", dataUrl);
					DOM.setNSData(page, "external", true);
				}
				return page;
			};

			/**
			 * This method handles hash change, **currently does nothing**.
			 * @method onHashChange
			 * @static
			 * @member ns.router.route.page
			 * @return {null}
			 */
			routePage.onHashChange = function (/* url, options */) {
				return null;
			};

			/**
			 * This method creates data url from absolute url given as argument.
			 * @method _createDataUrl
			 * @param {string} absoluteUrl
			 * @protected
			 * @static
			 * @member ns.router.route.page
			 * @return {string}
			 */
			routePage._createDataUrl = function (absoluteUrl) {
				return path.convertUrlToDataUrl(absoluteUrl, true);
			};

			/**
			 * On open fail, currently never used
			 * @method onOpenFailed
			 * @member ns.router.route.page
			 */
			routePage.onOpenFailed = function (/* options */) {
				this._setBase(path.parseLocation().hrefNoSearch);
			};

			/**
			 * This method returns base element from document head.
			 * If no base element is found, one is created based on current location.
			 * @method _getBaseElement
			 * @protected
			 * @static
			 * @member ns.router.route.page
			 * @return {HTMLElement}
			 */
			routePage._getBaseElement = function () {
				// Fetch document head if never cached before
				if (!head) {
					head = document.querySelector("head");
				}
				// Find base element
				if (!baseElement) {
					baseElement = document.querySelector("base");
					if (!baseElement) {
						baseElement = document.createElement("base");
						baseElement.href = path.documentBase.hrefNoHash;
						head.appendChild(baseElement);
					}
				}
				return baseElement;
			};

			/**
			 * Sets document base to url given as argument
			 * @method _setBase
			 * @param {string} url
			 * @protected
			 * @member ns.router.route.page
			 */
			routePage._setBase = function (url) {
				var base = this._getBaseElement(),
					baseHref = base.href;

				if (path.isPath(url)) {
					url = path.makeUrlAbsolute(url, path.documentBase);
					if (path.parseUrl(baseHref).hrefNoSearch !== path.parseUrl(url).hrefNoSearch) {
						base.href = url;
						path.documentBase = path.parseUrl(path.makeUrlAbsolute(url, path.documentUrl.href));
					}
				}
			};

			/**
			 * Returns container of pages
			 * @method getContainer
			 * @return {?ns.widget.core.Page}
			 * @member ns.router.route.page
			 * @static
			 */
			routePage.getContainer = function () {
				return engine.getRouter().getContainer();
			};

			/**
			 * Returns active page.
			 * @method getActive
			 * @return {?ns.widget.core.Page}
			 * @member ns.router.route.page
			 * @static
			 */
			routePage.getActive = function () {
				return this.getContainer().getActivePage();
			};

			/**
			 * Returns element of active page.
			 * @method getActiveElement
			 * @return {HTMLElement}
			 * @member ns.router.route.page
			 * @static
			 */
			routePage.getActiveElement = function () {
				return this.getActive().element;
			};
			ns.router.route.page = routePage;

			}(window.document, ns));

/*global window, define, ns */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Route Popup
 * Support class for router to control changing pupups.
 * @class ns.router.route.popup
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Damian Osipiuk <d.osipiuk@samsung.com>
 */
(function (window, document, ns) {
	
				var
			/**
			 * @property {Object} Popup Alias for {@link ns.widget.Popup}
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			Popup = ns.widget.core.Popup,
			util = ns.util,
			routePopup = {
				/**
				 * Object with default options
				 * @property {Object} defaults
				 * @property {string} [defaults.transition='none'] Sets the animation used during change of popup.
				 * @property {?HTMLElement} [defaults.container=null] Sets container of element.
				 * @property {boolean} [defaults.volatileRecord=true] Sets if the current history entry will be modified or a new one will be created.
				 * @member ns.router.route.popup
				 * @static
				 */
				defaults: {
					transition: "none",
					container: null,
					volatileRecord: true
				},
				/**
				 * Popup Element Selector
				 * @property {string} filter
				 * @member ns.router.route.popup
				 * @static
				 */
				filter: "." + Popup.classes.popup,
				/**
				 * Storage variable for active popup
				 * @property {?HTMLElement} activePopup
				 * @member ns.router.route.popup
				 * @static
				 */
				activePopup: null,
				/**
				 * Dictionary for popup related event types
				 * @property {Object} events
				 * @property {string} [events.POPUP_HIDE='popuphide']
				 * @member ns.router.route.popup
				 * @static
				 */
				events: {
					POPUP_HIDE: "popuphide"
				}
			},
			/**
			 * Alias for {@link ns.engine}
			 * @property {Object} engine
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			engine = ns.engine,
			/**
			 * Alias for {@link ns.util.path}
			 * @property {Object} path
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			path = ns.util.path,
			/**
			 * Alias for {@link ns.util.selectors}
			 * @property {Object} utilSelector
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			utilSelector = ns.util.selectors,
			/**
			 * Alias for {@link ns.router.history}
			 * @property {Object} history
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			history = ns.router.history,
			/**
			 * Alias for {@link ns.util.DOM}
			 * @property {Object} DOM
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			DOM = ns.util.DOM,
			/**
			 * Alias for Object utils
			 * @method slice
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			object = ns.util.object,
			/**
			 * Popup's hash added to url
			 * @property {string} popupHashKey
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			popupHashKey = "popup=true",
			/**
			 * Regexp for popup's hash
			 * @property {RegExp} popupHashKeyReg
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			popupHashKeyReg = /([&|\?]popup=true)/;

			/**
			 * Tries to find a popup element matching id and filter (selector).
			 * Adds data url attribute to found page, sets page = null when nothing found.
			 * @method findPopupAndSetDataUrl
			 * @param {string} id
			 * @param {string} filter
			 * @return {HTMLElement}
			 * @member ns.router.route.popup
			 * @private
			 * @static
			 */
			function findPopupAndSetDataUrl(id, filter) {
				var popup,
					hashReg = /^#/;

				id = id.replace(hashReg,"");
				popup = document.getElementById(id);

				if (popup && utilSelector.matchesSelector(popup, filter)) {
					DOM.setNSData(popup, "url", "#" + id);
				} else {
					// if we matched any element, but it doesn't match our filter
					// reset page to null
					popup = null;
				}
				// @TODO ... else
				// probably there is a need for running onHashChange while going back to a history entry
				// without state, eg. manually entered #fragment. This may not be a problem on target device
				return popup;
			}

			/**
			 * This method returns default options for popup router.
			 * @method option
			 * @return {Object}
			 * @member ns.router.route.popup
			 * @static
			 */
			routePopup.option = function () {
				var defaults = object.merge({}, routePopup.defaults);
				defaults.transition = ns.getConfig("popupTransition", defaults.transition);
				return defaults;
			};

			/**
			 * This method sets active popup and manages history.
			 * @method setActive
			 * @param {?ns.widget.core.popup} activePopup
			 * @param {Object} options
			 * @member ns.router.route.popup
			 * @static
			 */
			routePopup.setActive = function (activePopup, options) {
				var url,
					pathLocation = path.getLocation(),
					documentUrl = pathLocation.replace(popupHashKeyReg, "");

				this.activePopup = activePopup;

				if (activePopup) {
					// If popup is being opened, the new state is added to history.
					if (options && !options.fromHashChange && options.history) {
						url = path.addHashSearchParams(documentUrl, popupHashKey);
						history.replace(options, "", url);
					}
				} else if (pathLocation !== documentUrl) {
					// If popup is being closed, the history.back() is called
					// but only if url has special hash.
					// Url is changed after opening animation and in some cases,
					// the popup is closed before this animation and then the history.back
					// could cause undesirable change of page.
					history.back();
				}
			};

			/**
			 * This method opens popup if no other popup is opened.
			 * It also changes history to show that popup is opened.
			 * If there is already active popup, it will be closed.
			 * @method open
			 * @param {HTMLElement|string} toPopup
			 * @param {Object} options
			 * @param {"page"|"popup"|"external"} [options.rel = 'popup'] Represents kind of link as 'page' or 'popup' or 'external' for linking to another domain.
			 * @param {string} [options.transition = 'none'] Sets the animation used during change of popup.
			 * @param {boolean} [options.reverse = false] Sets the direction of change.
			 * @param {boolean} [options.fromHashChange = false] Sets if will be changed after hashchange.
			 * @param {boolean} [options.showLoadMsg = true] Sets if message will be shown during loading.
			 * @param {number} [options.loadMsgDelay = 0] Sets delay time for the show message during loading.
			 * @param {boolean} [options.dataUrl] Sets if page has url attribute.
			 * @param {string} [options.container = null] Selector for container.
			 * @param {boolean} [options.volatileRecord=true] Sets if the current history entry will be modified or a new one will be created.
			 * @param {Event} event
			 * @member ns.router.route.popup
			 * @static
			 */
			routePopup.open = function (toPopup, options, event) {
				var self = this,
					popup,
					router = engine.getRouter(),
					events = self.events,
					removePopup = function () {
						document.removeEventListener(events.POPUP_HIDE, removePopup, false);
						toPopup.parentNode.removeChild(toPopup);
						self.activePopup = null;
					},
					openPopup = function () {
						var positionTo = options["position-to"];
						// add such option only if it exists
						if (positionTo) {
							options.positionTo = positionTo;
						}
						if (event && event.touches) {
							options.x = event.touches[0].clientX;
							options.y = event.touches[0].clientY;
						} else if (event){
							options.x = event.clientX;
							options.y = event.clientY;
						}

						document.removeEventListener(events.POPUP_HIDE, openPopup, false);
						popup = engine.instanceWidget(toPopup, "Popup", options);
						popup.open(options);
						self.activePopup = popup;
					},
					activePage = router.container.getActivePage(),
					container;

				if (DOM.getNSData(toPopup, "external") === true) {
					container = options.container ? activePage.element.querySelector(options.container) : activePage.element;
					if (toPopup.parentNode !== container) {
						toPopup = util.importEvaluateAndAppendElement(toPopup, container);
					}
					document.addEventListener(routePopup.events.POPUP_HIDE, removePopup, false);
				}

				if (self.hasActive()) {
					document.addEventListener(events.POPUP_HIDE, openPopup, false);
					self.close();
				} else {
					openPopup();
				}
			};

			/**
			 * This method closes active popup.
			 * @method close
			 * @param {ns.widget.core.Popup} [activePopup]
			 * @param {string=} [options.transition]
			 * @param {string=} [options.ext= in ui-pre-in] options.ext
			 * @param {Object} options
			 * @member ns.router.route.popup
			 * @protected
			 * @static
			 */
			routePopup.close = function (activePopup, options) {
				var popupOptions,
					pathLocation = path.getLocation(),
					documentUrl = pathLocation.replace(popupHashKeyReg, "");

				options = options || {};

				if (activePopup && !(activePopup instanceof Popup)) {
					activePopup = engine.instanceWidget(activePopup, "Popup", options);
				}
				activePopup = activePopup || this.activePopup;

				// if popup is active
				if (activePopup) {
					popupOptions = activePopup.options;
					// we check if it changed the history
					if (popupOptions.history && pathLocation !== documentUrl) {
						// and then set new options for popup
						popupOptions.transition = options.transition || popupOptions.transition;
						popupOptions.ext = options.ext || popupOptions.ext;
						// unlock the router if it was locked
						if (!popupOptions.dismissible) {
							engine.getRouter().unlock();
						}
						// and call history.back()
						history.back();
					} else {
						// if popup did not change the history, we close it normally
						activePopup.close(options || {});
					}
					return true;
				}
				return false;
			};

			/**
			 * This method handles hash change.
			 * It closes opened popup.
			 * @method onHashChange
			 * @param {string} url
			 * @param {object} options
			 * @return {boolean}
			 * @member ns.router.route.popup
			 * @static
			 */
			routePopup.onHashChange = function (url, options) {
				var activePopup = this.activePopup;

				if (activePopup) {
					activePopup.close(options);
					// Default routing setting cause to rewrite further window history
					// even if popup has been closed
					// To prevent this onHashChange after closing popup we need to change
					// disable volatile mode to allow pushing new history elements
					return true;
				}
				return false;
			};

			/**
			 * On open fail, currently never used
			 * @method onOpenFailed
			 * @member ns.router.route.popup
			 * @return {null}
			 * @static
			 */
			routePopup.onOpenFailed = function (/* options */) {
				return null;
			};

			/**
			 * This method finds popup by data-url.
			 * @method find
			 * @param {string} absUrl Absolute path to opened popup
			 * @return {HTMLElement} Element of popup
			 * @member ns.router.route.popup
			 */
			routePopup.find = function (absUrl) {
				var self = this,
					dataUrl = self._createDataUrl(absUrl),
					activePage = engine.getRouter().getContainer().getActivePage(),
					popup;

				popup = activePage.element.querySelector("[data-url='" + dataUrl + "']" + self.filter);

				if (!popup && dataUrl && !path.isPath(dataUrl)) {
					popup = findPopupAndSetDataUrl(dataUrl, self.filter);
				}

				return popup;
			};

			/**
			 * This method parses HTML and runs scripts from parsed code.
			 * Fetched external scripts if required.
			 * @method parse
			 * @param {string} html HTML code to parse
			 * @param {string} absUrl Absolute url for parsed popup
			 * @return {HTMLElement}
			 * @member ns.router.route.popup
			 */
			routePopup.parse = function (html, absUrl) {
				var self = this,
					popup,
					dataUrl = self._createDataUrl(absUrl);

				popup = html.querySelector(self.filter);

				if (popup) {
					// TODO tagging a popup with external to make sure that embedded popups aren't
					// removed by the various popup handling code is bad. Having popup handling code
					// in many places is bad. Solutions post 1.0
					DOM.setNSData(popup, "url", dataUrl);
					DOM.setNSData(popup, "external", true);
				}

				return popup;
			};

			/**
			 * Convert url to data-url
			 * @method _createDataUrl
			 * @param {string} absoluteUrl
			 * @return {string}
			 * @member ns.router.route.popup
			 * @protected
			 * @static
			 */
			routePopup._createDataUrl = function (absoluteUrl) {
				return path.convertUrlToDataUrl(absoluteUrl);
			};

			/**
			 * Return true if active popup exists.
			 * @method hasActive
			 * @return {boolean}
			 * @member ns.router.route.popup
			 * @static
			 */
			routePopup.hasActive = function () {
				return !!this.activePopup;
			};

			/**
			 * Returns active popup.
			 * @method getActive
			 * @return {?ns.widget.core.Popup}
			 * @member ns.router.route.popup
			 * @static
			 */
			routePopup.getActive = function () {
				return this.activePopup;
			};

			/**
			 * Returns element of active popup.
			 * @method getActiveElement
			 * @return {HTMLElement}
			 * @member ns.router.route.popup
			 * @static
			 */
			routePopup.getActiveElement = function () {
				var active = this.getActive();
				return active && active.element;
			};

			ns.router.route.popup = routePopup;

			}(window, window.document, ns));

/*global window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Route Drawer
 * Support class for router to control drawer widget in profile Wearable.
 * @class ns.router.route.drawer
 * @author Hyeoncheol Choi <hc7.choi@samsung.com>
 */
(function (document, ns) {
	
				var CoreDrawer = ns.widget.core.Drawer,
				path = ns.util.path,
				history = ns.router.history,
				engine = ns.engine,
				routeDrawer = {},
				drawerHashKey = "drawer=true",
				drawerHashKeyReg = /([&|\?]drawer=true)/;

			/**
			 * Property containing default properties
			 * @property {Object} defaults
			 * @property {string} defaults.transition="none"
			 * @static
			 * @member ns.router.route.drawer
			 */
			routeDrawer.defaults = {
				transition: "none"
			};

			/**
			 * Property defining selector for filtering only drawer elements
			 * @property {string} filter
			 * @member ns.router.route.drawer
			 * @static
			 */
			routeDrawer.filter = "." + CoreDrawer.classes.drawer;


			/**
			 * Returns default route options used inside Router.
			 * But, drawer router has not options.
			 * @method option
			 * @static
			 * @member ns.router.route.drawer
			 * @return null
			 */
			routeDrawer.option = function () {
				return null;
			};

			/**
			 * This method opens the drawer.
			 * @method open
			 * @param {HTMLElement} drawerElement
			 * @member ns.router.route.drawer
			 */
			routeDrawer.open = function (drawerElement) {
				var drawer = engine.instanceWidget(drawerElement, "Drawer");
				drawer.open();
			};

			/**
			 * This method determines target drawer to open.
			 * @method find
			 * @param {string} absUrl Absolute path to opened drawer widget
			 * @member ns.router.route.drawer
			 * @return {?HTMLElement} drawerElement
			 */
			routeDrawer.find = function (absUrl) {
				var dataUrl = path.convertUrlToDataUrl(absUrl),
					activePage = engine.getRouter().getContainer().getActivePage(),
					drawer;

				drawer = activePage.element.querySelector("#" + dataUrl);

				return drawer;
			};

			/**
			 * This method parses HTML and runs scripts from parsed code.
			 * But, drawer router doesn't need to that.
			 * @method parse
			 * @param {string} html HTML code to parse
			 * @param {string} absUrl Absolute url for parsed page
			 * @member ns.router.route.drawer
			 */
			routeDrawer.parse = function (html, absUrl) {
				return null;
			};

			/**
			 * This method sets active drawer and manages history.
			 * @method setActive
			 * @param {Object} activeDrawer
			 * @member ns.router.route.drawer
			 * @static
			 */
			routeDrawer.setActive = function (activeDrawer) {
				var url,
					pathLocation = path.getLocation(),
					documentUrl = pathLocation.replace(drawerHashKeyReg, "");

				this._activeDrawer = activeDrawer;

				if(activeDrawer) {
					url = path.addHashSearchParams(documentUrl, drawerHashKey);
					history.replace({}, "", url);
				} else if (pathLocation !== documentUrl) {
					history.back();
				}
			};

			/**
			 * This method handles hash change.
			 * @method onHashChange
			 * @param {String} url
			 * @param {Object} options
			 * @param {String} prev Previous url string
			 * @static
			 * @member ns.router.route.drawer
			 * @return {null}
			 */
			routeDrawer.onHashChange = function (url, options, prev) {
				var self = this,
					activeDrawer = self._activeDrawer;

				if (activeDrawer && prev.search(drawerHashKey) > 0 && url.search(drawerHashKey) < 0) {
					activeDrawer.close(options);
				}
				return null;
			};

			ns.router.route.drawer = routeDrawer;

			}(window.document, ns));

/*global window, define */
/*jslint nomen: true */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * #Route CircularIndexScrollbar
 * Support class for router to control circularindexscrollbar widget in profile Wearable.
 * @class ns.router.route.circularindexscrollbar
 * @author Junyoung Park <jy-.park@samsung.com>
 */
(function (document, ns) {
	
				var CircularIndexScrollbar = ns.widget.wearable.CircularIndexScrollbar,
				CircularIndexScrollbarPrototype = CircularIndexScrollbar.prototype,
				util = ns.util,
				path = util.path,
				DOM = util.DOM,
				object = util.object,
				utilSelector = util.selectors,
				history = ns.router.history,
				engine = ns.engine,
				slice = [].slice,
				routeCircularIndexScrollbar = {},
				circularindexscrollbarHashKey = "circularindexscrollbar=true",
				circularindexscrollbarHashKeyReg = /([&|\?]circularindexscrollbar=true)/,
				INDEXSCROLLBAR_SELECTOR = ".ui-circularindexscrollbar";

			/**
			 * Property defining selector for filtering only circularIndexScrollbar elements
			 * @property {string} filter
			 * @member ns.router.route.circularindexscrollbar
			 * @static
			 */
			routeCircularIndexScrollbar.filter = INDEXSCROLLBAR_SELECTOR;

			/**
			 * Returns default route options used inside Router.
			 * But, circularindexscrollbar router has not options.
			 * @method option
			 * @static
			 * @member ns.router.route.circularindexscrollbar
			 * @return null
			 */
			routeCircularIndexScrollbar.option = function () {
				return null;
			};

			/**
			 * This method opens the circularindexscrollbar.
			 * @method open
			 * @param {HTMLElement} element
			 * @param {Object} [options]
			 * @member ns.router.route.circularindexscrollbar
			 */
			routeCircularIndexScrollbar.open = function (element, options) {
				return null;
			};

			/**
			 * This method determines target circularIndexScrollbar to open.
			 * @method find
			 * @param {string} absUrl Absolute path to opened circularIndexScrollbar widget
			 * @member ns.router.route.circularindexscrollbar
			 * @return {?HTMLElement} circularIndexScrollbarElement
			 */
			routeCircularIndexScrollbar.find = function (absUrl) {
				var self = this,
					dataUrl = path.convertUrlToDataUrl(absUrl),
					activePage = engine.getRouter().getContainer().getActivePage(),
					circularIndexScrollbar;

				circularIndexScrollbar = activePage.element.querySelector("#" + dataUrl);

				return circularIndexScrollbar;
			};

			/**
			 * This method parses HTML and runs scripts from parsed code.
			 * But, circularIndexScrollbar router doesn't need to that.
			 * @method parse
			 * @param {string} html HTML code to parse
			 * @param {string} absUrl Absolute url for parsed page
			 * @member ns.router.route.circularindexscrollbar
			 * @return {?HTMLElement} Element of page in parsed document.
			 */
			routeCircularIndexScrollbar.parse = function (html, absUrl) {
				return null;
			};

			/**
			 * This method sets active circularIndexScrollbar and manages history.
			 * @method setActive
			 * @param {Object} activeWidget
			 * @member ns.router.route.circularindexscrollbar
			 * @static
			 */
			routeCircularIndexScrollbar.setActive = function (activeWidget) {
				var url,
					pathLocation = path.getLocation(),
					documentUrl = pathLocation.replace(circularindexscrollbarHashKeyReg, "");

				this._activeWidget = activeWidget;

				if(activeWidget) {
					url = path.addHashSearchParams(documentUrl, circularindexscrollbarHashKey);
					history.replace({}, "", url);
				} else if (pathLocation !== documentUrl) {
					history.back();
				}
			};

			/**
			 * This method handles hash change.
			 * @method onHashChange
			 * @param {String} url
			 * @param {Object} options
			 * @static
			 * @member ns.router.route.circularindexscrollbar
			 * @return {null}
			 */
			routeCircularIndexScrollbar.onHashChange = function (url, options, prev) {
				var self = this,
					activeWidget = self._activeWidget;

				if (activeWidget && prev.search(circularindexscrollbarHashKey) > 0 && url.search(circularindexscrollbarHashKey) < 0) {
					activeWidget.hide(options);
				}
				return null;
			};

			ns.router.route.circularindexscrollbar = routeCircularIndexScrollbar;

			}(window.document, ns));

/*global window, define */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*jslint plusplus: true, nomen: true */
/**
 * @class tau.navigator
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 */
//  * @TODO add support of $.mobile.buttonMarkup.hoverDelay
(function (document, ns) {
	
	
			document.addEventListener("beforerouterinit", function () {
				ns.setConfig('autoInitializePage', ns.autoInitializePage);
			}, false);

			document.addEventListener("routerinit", function (evt) {
				var router = evt.detail,
					routePage = router.getRoute("page"),
					history = ns.router.history,
					back = history.back.bind(router),
					classes = ns.widget.core.Page.classes,
					pageActiveClass = classes.uiPageActive;
				/**
				 * @method changePage
				 * @inheritdoc ns.router.Router#open
				 * @member tau
				 */
				ns.changePage = router.open.bind(router);
				document.addEventListener('pageshow', function () {
					/**
					 * Current active page
					 * @property {HTMLElement} activePage
					 * @member tau
					 */
					ns.activePage = document.querySelector('.' + pageActiveClass);
				});
				/**
				 * First page element
				 * @inheritdoc ns.router.Router#firstPage
				 * @property {HTMLElement} firstPage
				 * @member tau
				 */
				ns.firstPage = router.getFirstPage();
				/**
				 * Returns active page element
				 * @inheritdoc ns.router.Router#getActivePageElement
				 * @method getActivePage
				 * @member tau
				 */
				ns.getActivePage = routePage.getActiveElement.bind(routePage);
				/**
				 * @inheritdoc ns.router.history#back
				 * @method back
				 * @member tau
				 */
				ns.back = back;
				/**
				 * @inheritdoc ns.router.Router#init
				 * @method initializePage
				 * @member tau
				 */
				ns.initializePage = router.init.bind(router);
				/**
				 * Page Container widget
				 * @property {HTMLElement} pageContainer
				 * @inheritdoc ns.router.Router#container
				 * @member tau
				 */
				ns.pageContainer = router.container;
				/**
				 * @method openPopup
				 * @inheritdoc ns.router.Router#openPopup
				 * @member tau
				 */
				ns.openPopup = function(to, options) {
					var htmlElementTo;
					if (to && to.length !== undefined && typeof to === 'object') {
						htmlElementTo = to[0];
					} else {
						htmlElementTo = to;
					}
					router.openPopup(htmlElementTo, options);
				};
				/**
				 * @method closePopup
				 * @inheritdoc ns.router.Router#closePopup
				 * @member tau
				 */
				ns.closePopup = router.closePopup.bind(router);

			}, false);

			}(window.document, ns));

(function (ns) {
	
	
			var engine = ns.engine;

			ns.IndexScrollbar = function (element, options) {
				ns.warn("tau.IndexScrollbar is deprecated. you have to use tau.widget.IndexScrollbar.");
				return engine.instanceWidget(element, "IndexScrollbar", options);
			};

			ns.SectionChanger = function (element, options) {
				ns.warn("tau.SectionChanger is deprecated. you have to use tau.widget.SectionChanger.");
				return engine.instanceWidget(element, "SectionChanger", options);
			};

			ns.SwipeList = function (element, options) {
				ns.warn("tau.SwipeList is deprecated. you have to use tau.widget.SwipeList.");
				return engine.instanceWidget(element, "SwipeList", options);
			};

			ns.VirtualListview = function (element, options) {
				ns.warn("tau.VirtualListview is deprecated. you have to use tau.widget.VirtualListview.");
				return engine.instanceWidget(element, "VirtualListview", options);
			};

			}(ns));
/*global define, window */
/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd
 *
 * Licensed under the Flora License, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://floralicense.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author Maciej Urbanski <m.urbanski@samsung.com>
 * @author Krzysztof Antoszek <k.antoszek@samsung.com>
 */
(function (ns) {
	
				if (ns.getConfig("autorun", true) === true) {
				ns.engine.run();
			}
			}(ns));

/*global define, ns */
			ns.info.profile = "wearable";
			
}(window, window.document));
