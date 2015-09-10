precision mediump float;
varying vec2  coord;
uniform sampler2D state1Background;
uniform sampler2D state1Text;
uniform sampler2D state2Background;
uniform sampler2D state2Text;
uniform sampler2D state3Background;
uniform sampler2D state3Text;
uniform float textureSwitch;
uniform float opacity;

void main() {
	vec4 background;
	vec4 text;
	if(textureSwitch == 0.0) {
		background = texture2D(state1Background, coord);
		text = texture2D(state1Text, coord);
	} else if (textureSwitch == 1.0) {
		background = texture2D(state2Background, coord);
		text = texture2D(state2Text, coord);
	} else if (textureSwitch == 2.0) {
		background = texture2D(state3Background, coord);
		text = texture2D(state3Text, coord);
	} else {
		background = vec4(0.0, 1.0, 0.0, 1.0);
		text = vec4(0.0, 0.0, 1.0, 1.0);
	}
		
	gl_FragColor = background + text;
	gl_FragColor.a = gl_FragColor.a * opacity;
}