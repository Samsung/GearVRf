precision mediump float;
varying vec2  coord;
uniform sampler2D texture;
uniform float tile;
void main() {
	
	
	
	vec4 color;
    color = texture2D(texture, coord);
		
	gl_FragColor = color;
}