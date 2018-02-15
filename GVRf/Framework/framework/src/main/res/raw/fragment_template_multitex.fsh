
#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
#endif
precision highp float;
precision lowp int;

layout(location = 0) out vec4 fragColor;

@MATRIX_UNIFORMS

layout(location = 1) in vec3 viewspace_position;
layout(location = 2) in vec3 viewspace_normal;
layout(location = 3) in vec4 local_position;
layout(location = 0) in vec3 view_direction;
layout(location = 4) in vec2 diffuse_coord;

#ifdef HAS_SHADOWS
layout(set = 0, binding = 3) uniform highp sampler2DArray u_shadow_maps;
#endif

struct Radiance
{
   vec3 ambient_intensity;
   vec3 diffuse_intensity;
   vec3 specular_intensity;
   vec3 direction; // view space direction from light to surface
   float attenuation;
};

float unpackFloatFromVec4i(const vec4 value)
{
    const vec4 unpackFactors = vec4(1.0 / (256.0 * 256.0 * 256.0), 1.0 / (256.0 * 256.0), 1.0 / 256.0, 1.0);
    return dot(value, unpackFactors);
}


@FragmentSurface

@FragmentAddLight

@LIGHTSOURCES

void main()
{
	Surface s = @ShaderName();
#if defined(HAS_LIGHTSOURCES)
	vec4 color = LightPixel(s);
	color = clamp(color, vec4(0), vec4(1));
	fragColor = color;
#else
    float t = s.emission.x + s.emission.y + s.emission.z;
    if (t != 0.0)
    {
        fragColor = s.emission; // X3D uses emissive color when not lit
    }
    else
    {
	    fragColor = s.diffuse;  // JASSIMP uses diffuse color when not lit
	}
#endif
}
