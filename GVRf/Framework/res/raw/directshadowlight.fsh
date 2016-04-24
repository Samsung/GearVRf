Radiance @LightType(Surface s, in Uniform@LightType data)
{
	vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
	float attenuation = 1.0;
    
    if (data.shadow_map_index >= 0.0)
	{
   	//	vec4 ShadowCoord = data.shadow_matrix * u_model * local_position;
   		vec4 ShadowCoord = local_position;
  		ShadowCoord = ShadowCoord.xyzw / ShadowCoord.w;
		ShadowCoord = ShadowCoord * 0.5 + 0.5;
  		vec3 texcoord = vec3(ShadowCoord.x, ShadowCoord.y, data.shadow_map_index);
  
   		if (texture(u_shadow_maps, texcoord).r < ShadowCoord.z - 0.0015)
      		attenuation = 0.0;
    //	data.diffuse_intensity.xyz = texture(u_shadow_maps, texcoord.xy).xyz;
    
	}
		
 	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					attenuation);
		
					
					
}