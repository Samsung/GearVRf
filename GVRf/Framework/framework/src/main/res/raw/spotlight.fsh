Radiance @LightType(Surface s, in Uniform@LightType data)
{
#ifdef HAS_MULTIVIEW
	vec4 lightpos = u_view_[view_id] * vec4(data.world_position.xyz, 1.0);
#else
    vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
#endif
     vec3 lightdir = normalize(lightpos.xyz - viewspace_position.xyz);
          
     // Attenuation
    float distance    = length(lightdir);
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
     float spot = clamp((cosSpotAngle - outer) / 
                    inner_minus_outer , 0.0, 1.0);
     return Radiance(data.ambient_intensity.xyz,
                     data.diffuse_intensity.xyz,
                     data.specular_intensity.xyz,
                     lightdir,
                     spot);  
                   
}
