Radiance @LightType(Surface s, in Uniform@LightType data, Vertex@LightType vertex)
{
	vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
	float attenuation = 1.0;
 
 #ifdef HAS_SHADOWS	  
 	float cosTheta = dot(normalize(data.world_direction.xyz), normalize(viewspace_normal.xyz));
	float biasFixes = 0.0002*tan(acos(cosTheta));
	biasFixes = clamp(biasFixes, 0.0, 0.01);
	 vec2 poissonDisk[4] = vec2[](
	  vec2( -0.94201624, -0.39906216 ),
	  vec2( 0.94558609, -0.76890725 ),
	  vec2( -0.094184101, -0.92938870 ),
	  vec2( 0.34495938, 0.29387760 )
	);
    if (data.shadow_map_index >= 0.0)
	{
   		vec4 ShadowCoord = vertex.shadow_position;
   		float bias = 0.0015;
 		ShadowCoord = ShadowCoord.xyzw / ShadowCoord.w;
  		for (int i=0;i<4;i++){
    		vec3 texcoord = vec3(ShadowCoord.x, ShadowCoord.y, data.shadow_map_index);
  			texcoord.xy = texcoord.xy + poissonDisk[i]/700.0 ;  			
   			if (texture(u_shadow_maps, texcoord).r < ShadowCoord.z - bias)
      			attenuation -= 0.2;
       	}
	}
#endif
 	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					attenuation);
		
					
					
}