Radiance @LightType(Surface s, in Uniform@LightType data, Vertex@LightType vertex)
{
#ifdef HAS_MULTIVIEW
	vec4 L = u_view_[int(view_id)] * vec4(data.world_direction.xyz, 0.0);
#else
    vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
#endif
	
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
        vec3 shadowMapPosition = ShadowCoord.xyz / ShadowCoord.w;
        vec3 texcoord = vec3(shadowMapPosition.x, shadowMapPosition.y, data.shadow_map_index);
        vec4 depth = texture(u_shadow_maps, texcoord);
        float distanceFromLight = unpackFloatFromVec4i(depth);

        if (distanceFromLight + bias < shadowMapPosition.z)
            attenuation = 0.5;
	}
#endif
 	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					attenuation);
		
					
					
}