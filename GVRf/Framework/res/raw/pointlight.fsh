Radiance @LightType(Surface s, in Struct@LightType data)
{
	vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
	vec4 lightdir = lightpos - viewspace_position;
	return Radiance(data.ambient_intensity.xyz,
				    data.diffuse_intensity.xyz,
				    data.specular_intensity.xyz,
				    normalize(lightdir.xyz),
					1.0f);

}
