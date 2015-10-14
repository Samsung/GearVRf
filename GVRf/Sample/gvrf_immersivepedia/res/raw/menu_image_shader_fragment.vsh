precision mediump float;
varying vec2  coord;
uniform sampler2D state1;
uniform sampler2D state2;
uniform float textureSwitch;
uniform float opacity;

void main() {
	vec4 texture;
	if(textureSwitch == 0.0) {
		texture = texture2D(state1, coord);
	} else {
		texture = texture2D(state2, coord);
	}
		
	gl_FragColor = texture;
	gl_FragColor.a = gl_FragColor.a * opacity;
}