Radiance @LightType(Surface s, in Struct@LightType data)
{
     vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
     vec3 lightdir = lightpos.xyz - viewspace_position.xyz;
          
     // Attenuation
    float distance    = length(lightpos - viewspace_position);
    float attenuation = 1.0 / (data.attenuation_constant + data.attenuation_linear * distance + 
    data.attenuation_quadratic * (distance * distance));
     

     vec4 spotDir =  normalize(u_view * vec4(data.world_direction.xyz,0.0));    
     float cosSpotAngle = max(dot(normalize(lightdir), normalize(-spotDir.xyz)), 0.0);
     
     float inner_minus_outer = data.inner_cone_angle - data.outer_cone_angle;  
     float spot = clamp((cosSpotAngle - data.outer_cone_angle) / 
                    inner_minus_outer , 0.0, 1.0);
     
     
     return Radiance(data.ambient_intensity.xyz,
                         data.diffuse_intensity.xyz,
                         data.specular_intensity.xyz,
                         normalize(lightdir.xyz),
                         spot);                        

}
