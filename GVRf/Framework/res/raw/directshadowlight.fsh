Radiance @LightType(Surface s, in Struct@LightType data)
{
	vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
	float attenuation = 1.0f;
    
/*	if (data.shadow_map_index >= 0.0)
	{
   		vec4 ShadowCoord = shadow_matrix * u_model * local_position;
  		ShadowCoord.xyz = ShadowCoord.xyz / ShadowCoord.w;
  		ShadowCoord.x = ShadowCoord.x * 0.5 + 0.5;
  		ShadowCoord.y = ShadowCoord.y * 0.5 + 0.5;
  		ShadowCoord.z = ShadowCoord.z * 0.5 + 0.5;
  		vec3 texcoord = vec3(ShadowCoord.x, ShadowCoord.y, data.shadow_map_index);
   		
		if (texture(u_shadow_maps, texcoord).x < ShadowCoord.z)
    		attenuation = 0.3;
	}
 */	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					attenuation);
}