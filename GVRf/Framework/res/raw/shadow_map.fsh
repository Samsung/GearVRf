#ifdef HAS_ShadowMap
float calculateShadow(vec4 texCords)
{
	vec3 shadowCords = texCords.xyz/texCords.w;
	shadowCords.x = shadowCords.x * 0.5 + 0.5;
	shadowCords.y = shadowCords.y * 0.5 + 0.5;
	shadowCords.z = shadowCords.z * 0.5 + 0.5;
	
	float visibility = 1.0;
	if ( texture( shadowTexture, shadowCords.xy ).z  <  shadowCords.z){
    	visibility = 0.3;
    	
    return visibility;
}

#endif