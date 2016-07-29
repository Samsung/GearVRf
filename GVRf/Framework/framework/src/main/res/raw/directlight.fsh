Radiance @LightType(Surface s, in Uniform@LightType data)
{
#ifdef HAS_MULTIVIEW
	vec4 L = u_view_[int(view_id)] * vec4(data.world_direction.xyz, 0.0);
#else
     vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
#endif

	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					1.0);
}
