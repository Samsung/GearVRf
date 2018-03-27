Radiance @LightType(Surface s, in U@LightType data, int index)
{
#ifdef HAS_MULTIVIEW
	vec4 L = u_view_[gl_ViewID_OVR] * data.world_direction;
#else
    vec4 L = u_view * data.world_direction;
#endif
	float attenuation = 1.0;
    vec4 ShadowCoord = @LightType_shadow_position[index];
    vec3 lightdir = normalize(L.xyz);

 #ifdef HAS_SHADOWS
    if ((data.shadow_map_index >= 0.0) && (ShadowCoord.w > 0.0))
	{
        float nDotL = max(dot(s.viewspaceNormal, lightdir), 0.0);
        float bias = 0.001 * tan(acos(nDotL));
        bias = clamp(bias, 0.0, 0.01);

        vec3 shadowMapPosition = ShadowCoord.xyz / ShadowCoord.w;
        vec3 texcoord = vec3(shadowMapPosition.x, shadowMapPosition.y, data.shadow_map_index);
        vec4 depth = texture(u_shadow_maps, texcoord);
        float distanceFromLight = unpackFloatFromVec4i(depth);

        if (distanceFromLight < shadowMapPosition.z - bias)
            attenuation = 0.5;
	}
#endif
 	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					-lightdir,
					attenuation);
		
					
					
}