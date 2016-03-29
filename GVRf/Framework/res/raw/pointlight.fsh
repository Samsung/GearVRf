Radiance @LightType(Surface s, in Struct@LightType data)
{
	vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
	vec4 lightdir = lightpos - viewspace_position;
	return Radiance(normalize(data.ambient_intensity.xyz),
				    normalize(data.diffuse_intensity.xyz),
				    normalize(data.specular_intensity.xyz),
				    normalize(lightdir.xyz),
					1.0f);

}
