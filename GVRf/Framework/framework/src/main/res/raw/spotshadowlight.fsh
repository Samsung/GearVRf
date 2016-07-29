Radiance @LightType(Surface s, in Uniform@LightType data, Vertex@LightType vertex)
{
#ifdef HAS_MULTIVIEW
	vec4 lightpos = u_view_[view_id] * vec4(data.world_position.xyz, 1.0);
#else
    vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
#endif
     vec3 lightdir = normalize(lightpos.xyz - viewspace_position.xyz);
          
     // Attenuation
     float distance    = length(lightpos.xyz - viewspace_position);
     float attenuation = 1.0 / (data.attenuation_constant + data.attenuation_linear * distance + 
    					data.attenuation_quadratic * (distance * distance));
     
#ifdef HAS_MULTIVIEW 
	vec4 spotDir =  normalize(u_view_[view_id] * vec4(data.world_direction.xyz, 0.0));
#else
	vec4 spotDir =  normalize(u_view * vec4(data.world_direction.xyz, 0.0));  
#endif        
     float cosSpotAngle = dot(spotDir.xyz, -lightdir);
     float outer = data.outer_cone_angle;
     float inner = data.inner_cone_angle;
     float inner_minus_outer = inner - outer;  
     float spot = clamp((cosSpotAngle - outer) / inner_minus_outer , 0.0, 1.0);

#ifdef HAS_SHADOWS	
    if (data.shadow_map_index >= 0.0)
	{
   		vec4 ShadowCoord = vertex.shadow_position;
   		float bias = 0.0015;
 		vec3 temp = ShadowCoord.xyz / ShadowCoord.w;
  		vec3 texcoord = vec3(temp.x, temp.y, data.shadow_map_index);
  
   		if (texture(u_shadow_maps, texcoord).r < (ShadowCoord.z - bias) / ShadowCoord.w)
      		attenuation *= 0.5;
	}
#endif
    return Radiance(data.ambient_intensity.xyz,
                    data.diffuse_intensity.xyz,
                    data.specular_intensity.xyz,
                    lightdir.xyz,
                    spot * attenuation);  
                   
}