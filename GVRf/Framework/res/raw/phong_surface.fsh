#ifdef HAS_diffuseTexture
   uniform sampler2D diffuseTexture;
#endif
uniform vec4 ambientColor;
uniform vec4 diffuseColor;
uniform vec4 specularColor;
uniform float specularExponent;

struct Surface
{
   vec4 ambient;
   vec4 diffuse;
   vec4 specular;
};

Surface @ShaderName()
{
	Surface surface = Surface(ambientColor, diffuseColor, specularColor);                                                                         
#ifdef HAS_diffuseTexture
	surface.diffuse *= texture2D(diffuseTexture, diffuse_coord.xy);
#endif
	return surface;
}