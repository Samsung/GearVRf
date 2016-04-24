Radiance @LightType(Surface s, in Uniform@LightType data)
{
	vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
     
	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					1.0);
}
