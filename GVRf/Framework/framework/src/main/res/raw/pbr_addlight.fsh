
// Schlick Implementation of microfacet occlusion from
// "An Inexpensive BRDF Model for Physically based Rendering" by Christophe Schlick.
//
float geometricOcclusionSchlick(float LdotH, float NdotH, float perceptualRoughness)
{
    float k = perceptualRoughness * 0.79788; // 0.79788 = sqrt(2.0/3.1415);
    // alternately, k can be defined with
    // float k = (perceptualRoughness + 1) * (perceptualRoughness + 1) / 8;

    float l = LdotH / (LdotH * (1.0 - k) + k);
    float n = NdotH / (NdotH * (1.0 - k) + k);
    return l * n;
}

//
// Cook Torrance Implementation from
// "A Reflectance Model for Computer Graphics" by Robert Cook and Kenneth Torrance
//
float geometricOcclusionCT(float NdotL, float NdotV, float NdotH, float VdotH)
{
    return min(min(2.0 * NdotV * NdotH / VdotH, 2.0 * NdotL * NdotH / VdotH), 1.0);
}

//
// The following implementation is from
// "Geometrical Shadowing of a Random Rough Surface" by Bruce G. Smith
//
float geometricOcclusionSmith(float NdotL, float NdotV, float alphaRoughness)
{
  float NdotL2 = NdotL * NdotL;
  float NdotV2 = NdotV * NdotV;
  float v = ( -1.0 + sqrt ( alphaRoughness * (1.0 - NdotL2 ) / NdotL2 + 1.)) * 0.5;
  float l = ( -1.0 + sqrt ( alphaRoughness * (1.0 - NdotV2 ) / NdotV2 + 1.)) * 0.5;
  return (1.0 / max((1.0 + v + l ), 0.000001));
}

//
// The following equation(s) model the distribution of microfacet normals across the area being drawn (aka D())
// Implementation from "Average Irregularity Representation of a Roughened Surface for Ray Reflection" by T. S. Trowbridge, and K. P. Reitz
// Follows the distribution function recommended in the SIGGRAPH 2013 course notes from EPIC Games [1], Equation 3.
//
float microfacetDistribution(float NdotH, float alphaRoughness)
{
    float roughnessSq = alphaRoughness * alphaRoughness;
    float f = (NdotH * roughnessSq - NdotH) * NdotH + 1.0;
    return roughnessSq / (M_PI * f * f);
}




// Calculation of the lighting contribution from an optional Image Based Light source.
// Precomputed Environment Maps are required uniform inputs.
vec3 getIBLContribution(float perceptualRoughness, float NdotV, vec3 n, vec3 reflection, vec3 specularColor,
                        vec3 diffuseColor)
{

    vec3 diffuse = vec3(0);
    vec3 specular = vec3(0);

    #ifdef HAS_brdfLUTTexture
        vec3 brdf = SRGBtoLINEAR(texture(brdfLUTTexture, vec2(NdotV, 1.0 - perceptualRoughness)).rgb);
        #ifdef HAS_diffuseEnvTex
            vec3 diffuseLight = SRGBtoLINEAR(texture(diffuseEnvTex, n).rgb);
            diffuse = diffuseLight * diffuseColor;
        #endif

        #ifdef HAS_specularEnvTexture
            vec3 specularLight = SRGBtoLINEAR(texture(specularEnvTexture, reflection).rgb);
            specular = specularLight * (specularColor * brdf.x + brdf.y);
        #endif

    #endif
    return diffuse + specular;

}


vec4 AddLight(Surface s, Radiance r)
{
	vec3 l = r.direction.xyz;                  // From surface to light, unit length, view-space
    vec3 n = s.viewspaceNormal;                // normal at surface point
    vec3 v = -viewspace_position;               // Vector from surface point to camera
    vec3 h = normalize(l + v);                 // Half vector between both l and v
    vec3 reflection = reflect(-v, normalize(n));
    float NdotL = clamp(dot(n, l), 0.001, 1.0);
    float NdotV = abs(dot(n, v)) + 0.001;
    float NdotH = clamp(dot(n, h), 0.0, 1.0);
    float LdotH = clamp(dot(l, h), 0.0, 1.0);
    float VdotH = clamp(dot(v, h), 0.0, 1.0);
    float alphaRoughness = s.roughness * s.roughness;

    //
    // Calculate surface reflection
    // Fresnel Schlick Simplified implementation of fresnel from
    // "An Inexpensive BRDF Model for Physically based Rendering" by Christophe Schlick.
    //
    vec3 specularEnvironmentR0 = s.specular.rgb;
    vec3 specularEnvironmentR90 = vec3(1.0, 1.0, 1.0) * s.brdf.y;
    vec3 F = specularEnvironmentR0 + (specularEnvironmentR90 - specularEnvironmentR0) * pow(clamp(1.0 - VdotH, 0.0, 1.0), 5.0);

    float G = geometricOcclusionSchlick(NdotL, NdotV, s.roughness); // Schlick implementation
//  float G = geometricOcclusionSmith(NdotL, NdotV, alphaRoughess); // Smith implementation
//  float G = geometricOcclusionCT(NdotL, NdotV, NdotH, VdotH);     // Cook Torrance implementation
    float D = microfacetDistribution(NdotH, alphaRoughness);

    //
    // calculate diffuse and specular contribution
    // From Schlick BRDF model from "An Inexpensive BRDF Model for Physically-based Rendering"
    //
    vec3 kD = (1.0 - F) * s.diffuse.xyz / M_PI;
    vec3 kS = F * G * D / (4.0 * NdotL * NdotV);

    // Obtain final intensity as reflectance (BRDF) scaled by the energy of the light (cosine law)
    vec3 color = NdotL * ((r.diffuse_intensity * kD) + (r.specular_intensity * kS)) + s.emission.xyz;

    mat4 view_i;
#ifdef HAS_MULTIVIEW
    view_i = u_view_i_[gl_ViewID_OVR];
#else
    view_i = u_view_i;
#endif
    color += getIBLContribution(s.roughness, NdotV, (view_i * vec4(n, 1.0)).xyz,
                                (view_i * vec4(reflection, 1.0)).xyz, s.specular, s.diffuse.xyz);

#ifdef HAS_lightmapTexture
    float ao = texture(lightmapTexture, lightmap_coord).r;
    color = mix(color, color * ao, lightmapStrength);
#endif

    // This section uses mix to override final color for reference app visualization
    // of various parameters in the lighting equation. Might need this if we have an app for demo.

    //#ifdef HAS_scaleFGD
    //    color = mix(color, F, scaleFGD.x);
    //    color = mix(color, vec3(G), scaleFGD.y);
    //    color = mix(color, vec3(D), scaleFGD.z);
    //    color = mix(color, kS, scaleFGD.w);
    //#endif
    //#ifdef HAS_scaleDiffBaseMR
    //    color = mix(color, kD, scaleDiffBaseMR.x);
    //    color = mix(color, s.diffuse.rgb, scaleDiffBaseMR.y);
    //    color = mix(color, vec3(s.metallic), scaleDiffBaseMR.z);
    //    color = mix(color, vec3(s.roughness), scaleDiffBaseMR.w);
    //#endif

    return vec4(pow(color, vec3(1.0 / 2.2)), s.diffuse.w);
}
