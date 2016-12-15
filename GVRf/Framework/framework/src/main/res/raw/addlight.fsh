vec4 AddLight(Surface s, Radiance r)
{
	vec3 L = r.direction.xyz;	// From surface to light, unit length, view-space
	float nDotL = max(dot(s.viewspaceNormal, L), 0.0);
	
	vec3 kE = s.emission.xyz;
	float alpha = s.diffuse.a;
	vec3 kA = s.ambient.xyz * clamp(r.ambient_intensity.xyz, 0.0, 1.0);
	vec3 kS = vec3(0, 0, 0);
	vec3 kD = nDotL * s.diffuse.xyz * clamp(r.diffuse_intensity.xyz, 0.0, 1.0);
	if (nDotL > 0.0)
	{
		vec3 reflect = normalize(reflect(-L, s.viewspaceNormal));
		float cosAngle = dot(view_direction, reflect);
		if (cosAngle > 0.0)
		{
			kS = s.specular.xyz * clamp(r.specular_intensity, 0.0, 1.0);
			kS *= pow(cosAngle, specular_exponent);
		}
	}
	return vec4(kE + r.attenuation * (kA + kD + kS), alpha);
}