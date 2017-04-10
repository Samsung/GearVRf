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
var enableLogs = true;

/**
 * Log to console using sprintf.
 * <p>
 * Even though console.log does accept formatting parameters, it does not work
 * in all consoles such as the Tizen Web IDE console. This should work on all
 * consoles.
 */
function log() {
	if(enableLogs) {
		console.log(sprintf.apply(this, arguments));
	}
}

/**
 * Exit the application
 */
function exit() {
	try {
		tizen.application.getCurrentApplication().exit();
	} catch (err) {
		console.log(err);
	}
}