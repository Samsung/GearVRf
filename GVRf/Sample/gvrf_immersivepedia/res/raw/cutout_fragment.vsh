precision mediump float;
varying vec2  coord;
uniform sampler2D texture;
uniform float cutout;
void main() {
	
	vec4 color;	
	color = texture2D(texture, coord);
	
	if(color.r < cutout){
		gl_FragColor = vec4(0,0,0,color.a);
	}else{
		gl_FragColor = vec4(0,0,0,0);
	}
	if(color.a < 1.0)
		gl_FragColor = vec4(0,0,0,0);	
}