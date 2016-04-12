Radiance @LightType(Surface s, in Struct@LightType data)
{
	vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
	float attenuation = 1.0f;
    
	if (data.has_shadow_map != 0)
	{
   		vec4 ShadowCoord = data.shadow_matrix * vec4(local_position, 1);
		if (texture(data.shadow_map, ShadowCoord.xy).z < ShadowCoord.z)
    		attenuation = 0.5;
	}
 	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					attenuation);
}