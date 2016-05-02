Radiance @LightType(Surface s, in Uniform@LightType data, Vertex@LightType vertex)
{
	vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
	float attenuation = 1.0;
 
 #ifdef HAS_SHADOWS	   
    if (data.shadow_map_index >= 0.0)
	{
   		vec4 ShadowCoord = vertex.shadow_position;
   		float bias = 0.0015;
 		ShadowCoord = ShadowCoord.xyzw / ShadowCoord.w;
  		vec3 texcoord = vec3(ShadowCoord.x, ShadowCoord.y, data.shadow_map_index);
  
   		if (texture(u_shadow_maps, texcoord).r < ShadowCoord.z - bias)
      		attenuation = 0.5;
	}
#endif	
 	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					attenuation);
		
					
					
}