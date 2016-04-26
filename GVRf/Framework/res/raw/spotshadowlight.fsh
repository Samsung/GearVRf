Radiance @LightType(Surface s, in Uniform@LightType data, Vertex@LightType vertex)
{
     vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
     vec3 lightdir = normalize(lightpos.xyz - viewspace_position.xyz);
          
     // Attenuation
    float distance    = length(lightpos.xyz - viewspace_position);
    float attenuation = 1.0 / (data.attenuation_constant + data.attenuation_linear * distance + 
    					data.attenuation_quadratic * (distance * distance));
     
     vec4 spotDir =  normalize(u_view * vec4(data.world_direction.xyz, 0.0));    
     float cosSpotAngle = dot(spotDir.xyz, -lightdir);
     float outer = data.outer_cone_angle;
     float inner = data.inner_cone_angle;
     float inner_minus_outer = inner - outer;  
     float spot = clamp((cosSpotAngle - outer) / inner_minus_outer , 0.0, 1.0);
	
     if (data.shadow_map_index >= 0.0)
	 {
   		vec4 ShadowCoord = vertex.shadow_position;
  		ShadowCoord = ShadowCoord.xyzw / ShadowCoord.w;
		ShadowCoord = ShadowCoord * 0.5 + 0.5;
  		vec3 texcoord = vec3(ShadowCoord.x, ShadowCoord.y, data.shadow_map_index);
  		vec4 t = texture(u_shadow_maps, texcoord);
  
   		if (t.z < ShadowCoord.z - 0.0009)
      		spot = 0.5;
	}
    return Radiance(data.ambient_intensity.xyz,
                    data.diffuse_intensity.xyz,
                    data.specular_intensity.xyz,
                    lightdir.xyz,
                    spot);  
                   
}