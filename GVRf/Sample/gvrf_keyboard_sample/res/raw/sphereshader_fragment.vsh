precision mediump float;
varying vec2  coord;
varying vec3  normal;
varying vec3  view;
varying vec3  light;
uniform sampler2D HDRI_texture;
uniform sampler2D texture;
uniform sampler2D second_texture;
uniform vec3 trans_color;
uniform mat4 u_mvp;
vec2  animOffset;
uniform float animTexture;
uniform float blur;
varying vec3  n;
varying vec3  v;
varying vec3  l;
varying vec3  p;
uniform float u_radius;
void main() {
	
	vec2 reflect_coord;
	float reflect_intensity = 0.1;
	float division = 7.0;
	
	vec3  r = normalize(reflect(v,n));
    float b = dot(r,p);
    float c = dot(p,p)-u_radius*u_radius;
    float t = sqrt(b*b-c);
    
    if( -b + t > 0.0 ) t = -b + t;
    else t = -b - t;
    
    vec3 ray = normalize(p+t*r);
    ray.z = ray.z/sqrt(ray.x*ray.x+ray.z*ray.z);
   
    if( ray.x > 0.0 ) reflect_coord.x =  ray.z + 1.0;
    else              reflect_coord.x = -ray.z - 1.0;
   
    reflect_coord.x /= 2.0;
    reflect_coord.y  = ray.y;
    reflect_coord.x = 0.5 + 0.6*asin(reflect_coord.x)/1.57079632675;
    reflect_coord.y = 0.5 + 0.6*asin(reflect_coord.y)/1.57079632675;
	
	animOffset = vec2(animTexture,0.0);
	
	vec4 reflect = texture2D(HDRI_texture, reflect_coord);
	vec4 color = texture2D(texture, coord) / division;
	vec3 color2 = texture2D(texture, coord ).rgb;
	vec4 color3 = texture2D(second_texture, coord + animOffset - vec2(1,0.0));
	
	color += texture2D(texture, (coord * (0.9)) + vec2(0.05,0.05)) / division;
	color += texture2D(texture, (coord * (0.85)) + vec2(0.075,0.075)) / division;
	color += texture2D(texture, (coord * (0.8)) + vec2(0.1,0.1)) / division;
	color += texture2D(texture, (coord * (0.75)) + vec2(0.125,0.125)) / division;
	color += texture2D(texture, (coord * (0.7)) + vec2(0.15,0.15)) / division;
	color += texture2D(texture, (coord * (0.65)) + vec2(0.175,0.175)) / division;
	
	vec3 finalColor = (color.rgb * blur) + (color2 * (1.0-blur));
	if(color3.w == 0.0){
		finalColor = (  trans_color * animTexture + (finalColor * (1.0 -animTexture))) + color3.rgb;
	}
	
	else{
		finalColor = color3.rgb;
	}
	
	vec3  h = normalize(v+l);
	float diffuse  = max ( dot(l,n), 0.2 );
	
	float specular = max ( dot(h,n), 0.0 );
	specular = pow (specular, 300.0);
	
	finalColor *= diffuse;
	finalColor += 0.5*(1.0- finalColor)*specular;
	if(reflect.r > 0.5)
		finalColor = (finalColor + reflect.rgb * reflect_intensity);
	
	gl_FragColor = vec4( finalColor * 1.0 - blur/2.0, 1.0 );
	gl_FragColor.a = 1.0 - min(0.9,blur * dot(v,n));
}