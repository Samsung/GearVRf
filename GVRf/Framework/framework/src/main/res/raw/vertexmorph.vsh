#if defined(HAS_blendshapeTexture)
	for (int i = 0; i < u_numblendshapes; ++i)
	{
	    vertex.local_position.xyz += u_blendweights[i] * texelFetch(blendshapeTexture, ivec2(i, gl_VertexID), 0).rgb;
	}

#endif