#if defined(HAS_blendshapeTexture)

// positions, normals, tangents, bitangents
#if defined(HAS_a_normal) && defined(HAS_a_tangent)
vertex.local_tangent = a_tangent;
    vertex.local_bitangent = a_bitangent;  
	for (int i = 0; i < u_numblendshapes; ++i)
	{
	    int ofs = i * 4;

	    vertex.local_position.xyz += u_blendweights[i] * texelFetch(blendshapeTexture, ivec2(ofs, gl_VertexID), 0).rgb;
	    vertex.local_normal.xyz += u_blendweights[i] * texelFetch(blendshapeTexture, ivec2(ofs + 1, gl_VertexID), 0).rgb;
	    vertex.local_tangent.xyz += u_blendweights[i] * texelFetch(blendshapeTexture, ivec2(ofs + 2, gl_VertexID), 0).rgb;
	    vertex.local_bitangent.xyz += u_blendweights[i] * texelFetch(blendshapeTexture, ivec2(ofs + 3, gl_VertexID), 0).rgb;
	}


// positions and normals
#elif defined(HAS_a_normal)
for (int i = 0; i < u_numblendshapes; ++i)
	{
	    int ofs = i * 2;
	    vertex.local_position.xyz += u_blendweights[i] * texelFetch(blendshapeTexture, ivec2(ofs, gl_VertexID), 0).rgb;
	    vertex.local_normal.xyz += u_blendweights[i] * texelFetch(blendshapeTexture, ivec2(ofs + 1, gl_VertexID), 0).rgb;
	}



// only positions
#else
	for (int i = 0; i < u_numblendshapes; ++i)
	{
	    vertex.local_normal.xyz += u_blendweights[i] * texelFetch(blendshapeTexture, ivec2(i, gl_VertexID), 0).rgb;
	}
#endif
#endif
