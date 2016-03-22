Radiance @LightType(Surface s, in Data@LightType data)
{
   vec3 L = normalize(data.world_direction.xyz);
   float nDotL = max(dot(world_normal, L), 0.0);
   vec3 diffuse = nDotL * data.diffuse_intensity.xyz;
   vec3 specular = nDotL * data.specular_intensity.xyz;
   vec3 reflect = normalize(reflect(data.world_direction.xyz, world_normal));
   
   Radiance r = Radiance(data.ambient_intensity.xyz, diffuse, specular, L, reflect);
   return r;
}
