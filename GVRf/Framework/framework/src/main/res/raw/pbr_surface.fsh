@MATERIAL_UNIFORMS

layout ( set = 0, binding = 10 ) uniform sampler2D diffuseTexture;
layout ( set = 0, binding = 11 ) uniform sampler2D metalRoughnessTexture;
layout ( set = 0, binding = 12 ) uniform sampler2D occlusionTexture;
layout ( set = 0, binding = 13 ) uniform sampler2D emissiveTexture;
layout ( set = 0, binding = 14 ) uniform sampler2D normalTexture;

struct Surface
{
    vec3 diffuse;                 // color contribution from diffuse lighting
    vec3 specular;                // color contribution from specular lighting
    vec3 viewspaceNormal;
    vec3 reflectance0;            // full reflectance color (normal incidence angle)
    vec3 reflectance90;           // reflectance color at grazing angle
    float perceptualRoughness;    // roughness value, as authored by the model creator (input to shader)
    float metallic;               // metallic value at the surface
    float alphaRoughness;         // roughness mapped to a more linear change in the roughness (proposed by [2])
};

vec4 SRGBtoLINEAR(vec4 srgbIn)
{
#ifdef MANUAL_SRGB
#ifdef SRGB_FAST_APPROXIMATION
    vec3 linOut = pow(srgbIn.xyz, vec3(2.2));
#else //SRGB_FAST_APPROXIMATION
    vec3 bLess = step(vec3(0.04045), srgbIn.xyz);
    vec3 linOut = mix(srgbIn.xyz / vec3(12.92), pow((srgbIn.xyz + vec3(0.055)) / vec3(1.055), vec3(2.4)), bLess);
#endif //SRGB_FAST_APPROXIMATION
    return vec4(linOut,srgbIn.w);;
#else //MANUAL_SRGB
    return srgbIn;
#endif //MANUAL_SRGB
}

Surface @ShaderName()
{
	vec4 basecolor = diffuse_color;
	vec4 emission = emissive_color;
    float perceptualRoughness = u_perceptualRoughness;
    float metal = metallic;

#ifdef HAS_metalRoughnessTexture
    // Roughness is stored in the 'g' channel, metallic is stored in the 'b' channel.
    // This layout intentionally reserves the 'r' channel for (optional) occlusion map data
    vec4 mrSample = texture2D(u_metalRoughnessTexture, a_texcoord);
    perceptualRoughness = mrSample.g * perceptualRoughness;
    metal = mrSample.b * metal;
#endif
    perceptualRoughness = clamp(perceptualRoughness, c_MinRoughness, 1.0);
    metal = clamp(m, 0.0, 1.0);
    // Roughness is authored as perceptual roughness; as is convention,
    // convert to material roughness by squaring the perceptual roughness [2].
    float alphaRoughness = perceptualRoughness * perceptualRoughness;

#ifdef HAS_diffuseTexture
	basecolor *= texture(diffuseTexture, diffuse_coord.xy);
#endif
    vec3 f0 = vec3(0.04);
    vec3 diffuseColor = basecolor.rgb * (vec3(1.0) - f0);
    diffuseColor *= 1.0 - metal;
    vec4 specular = vec4(mix(f0, basecolor.rgb, metal), 1.0f);

    // Compute reflectance.
    float reflectance = max(max(specular.r, specular.g), specular.b);

    // For typical incident reflectance range (between 4% to 100%) set the grazing reflectance to 100% for typical fresnel effect.
    // For very low reflectance range on highly diffuse objects (below 4%), incrementally reduce grazing reflecance to 0%.
    float reflectance90 = clamp(reflectance * 25.0, 0.0, 1.0);
    vec3 specularEnvironmentR0 = specular.rgb;
    vec3 specularEnvironmentR90 = vec3(1.0, 1.0, 1.0) * reflectance90;

#ifdef HAS_emissiveTexture
	emission = texture(emissiveTexture, diffuse_coord.xy);
#endif
#ifdef HAS_normalTexture
	viewspaceNormal = texture(normalTexture, diffuse_coord.xy).xyz * 2.0 - 1.0;
#else
	viewspaceNormal = viewspace_normal;
#endif
    return Surface(diffuse, specular, viewspaceNormal,
                   specularEnvironmentR0, specularEnvironmentR90, perceptualRoughness,
                   metal, alphaRoughness);
}
