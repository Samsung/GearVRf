Radiance @LightType(Surface s, in Struct@LightType data)
{
	vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
	vec4 lightdir = lightpos - viewspace_position;
		
	
	// Attenuation
    float distance    = length(lightpos.xyz - viewspace_position);
    float attenuation = 1.0 / (data.attenuation_constant + data.attenuation_linear * distance + 
    data.attenuation_quadratic * (distance * distance));

	vec4 spotDir =  normalize(u_view * vec4(0.0, 0.0, 1.0, 0.0));
	
	float cosSpotAngle = max(dot(normalize(-lightdir), spotDir), 0.0);

	float coneAngle = (data.outer_cone_angle  + data.inner_cone_angle)/2.0;
	
	if (acos(cosSpotAngle) < degrees(coneAngle)) {	
		return Radiance(data.ambient_intensity.xyz,
					    data.diffuse_intensity.xyz,
					    data.specular_intensity.xyz,
					    normalize(lightdir.xyz),
						attenuation);					
	}
}