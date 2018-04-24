// This calculates the specular geometric attenuation (aka G()),
// where rougher material will reflect less light back to the viewer.
// This implementation is based on [1] Equation 4, and we adopt their modifications to
// alphaRoughness as input as originally proposed in [2].
float geometricOcclusion(Surface s, float NdotL, float NdotV)
{
    float r = s.alphaRoughness;
    float attenuationL = 2.0 * NdotL / (NdotL + sqrt(r * r + (1.0 - r * r) * (NdotL * NdotL)));
    float attenuationV = 2.0 * NdotV / (NdotV + sqrt(r * r + (1.0 - r * r) * (NdotV * NdotV)));
    return attenuationL * attenuationV;
}

// The following equation(s) model the distribution of microfacet normals across the area being drawn (aka D())
// Implementation from "Average Irregularity Representation of a Roughened Surface for Ray Reflection" by T. S. Trowbridge, and K. P. Reitz
// Follows the distribution function recommended in the SIGGRAPH 2013 course notes from EPIC Games [1], Equation 3.
float microfacetDistribution(Surface s, float NdotH)
{
    float roughnessSq = s.alphaRoughness * s.alphaRoughness;
    float f = (NdotH * roughnessSq - NdotH) * NdotH + 1.0;
    return roughnessSq / (M_PI * f * f);
}

vec4 AddLight(Surface s, Radiance r)
{
	vec3 L = r.direction.xyz;	// From surface to light, unit length, view-space
    vec3 n = s.viewspaceNormal;                // normal at surface point
    vec3 v = view_direction;                   // Vector from surface point to camera
    vec3 h = normalize(L + v);                 // Half vector between both l and v
    vec3 reflection = -normalize(reflect(v, n));
    float NdotL = clamp(dot(n, l), 0.001, 1.0);
    float NdotV = abs(dot(n, v)) + 0.001;
    float NdotH = clamp(dot(n, h), 0.0, 1.0);
    float LdotH = clamp(dot(l, h), 0.0, 1.0);
    float VdotH = clamp(dot(v, h), 0.0, 1.0);

    vec3 F = s.reflectance0 + (s.reflectance90 - s.reflectance0) * pow(clamp(1.0 - VdotH, 0.0, 1.0), 5.0);
    float G = geometricOcclusion(s, NDotL, NdotV);
    float D = microfacetDistribution(s, NdotH);

    // Calculation of analytical lighting contribution
    vec3 kD = (1.0 - F) * s.diffuse / M_PI;
    vec3 kS = F * G * D / (4.0 * NdotL * NdotV);

    // Obtain final intensity as reflectance (BRDF) scaled by the energy of the light (cosine law)
    vec3 color = NdotL * r.diffuse_intensity * (kD + kS) + s.emission.xyz;

#ifdef HAS_occlusionTexture
    float ao = texture2D(occlusionTexture, v_UV).r;
    color = mix(color, color * ao, u_occlusionStrength);
#endif

    // This section uses mix to override final color for reference app visualization
    // of various parameters in the lighting equation.
    color = mix(color, F, scaleFGD.x);
    color = mix(color, vec3(G), scaleFGD.y);
    color = mix(color, vec3(D), scaleFGD.z);
    color = mix(color, kS, scaleFGD.w);
    color = mix(color, kD, scaleDiffBaseMR.x);
    color = mix(color, s.diffuse.rgb, scaleDiffBaseMR.y);
    color = mix(color, vec3(s.metallic), scaleDiffBaseMR.z);
    color = mix(color, vec3(s.perceptualRoughness), scaleDiffBaseMR.w);
    return vec4(pow(color, vec3(1.0 / 2.2)), alpha);
}
