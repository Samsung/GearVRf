precision mediump float;
out vec4 color;

void main()
{
	//color = vec4(0.5,0.5,0.5,1.0);
	color = vec4(gl_FragCoord.z,gl_FragCoord.z,gl_FragCoord.z,1.0);
}
