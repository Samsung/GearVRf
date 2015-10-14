precision mediump float;
varying vec2  coord;
uniform sampler2D grayScaleTexture;
uniform sampler2D detailsTexture;
uniform vec4 color;
uniform float opacity;

void main() {
	
	vec4 colorGrayScale;
	vec4 colorDetails;
	
    colorGrayScale = texture2D(grayScaleTexture, coord);
    colorDetails = texture2D(detailsTexture, coord);
    
    vec4 colorResult = colorGrayScale * color; 

	if(colorDetails.a != 0.0){
	
		colorResult = colorResult * (1.0-colorDetails.a) + colorDetails * (colorDetails.a);
        
	}
    		
	gl_FragColor = colorResult * opacity;
}