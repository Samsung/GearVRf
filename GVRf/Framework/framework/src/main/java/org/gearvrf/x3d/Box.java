package org.gearvrf.x3d;

public class Box extends mesh {
	public static final float BoxVertices[] = {
			1, 1, -1,     1, -1, -1,	// back right
			-1, -1, -1,		-1, 1, -1,	// back left
			1, 1, 1,		 1, -1, 1,	// front right
			-1, -1, 1,    -1, 1, 1 };	// front left
	public static final float BoxVertexNormals[] = {
				0, 0, -1,		0, 0, 1, // back, front
				-1, 0, 0,	1, 0, 0,	// left, right
				0, 1, 0,	0, -1, 0 };	// top, bottom
	
	public static final float TextureCoordinates[] = {0, 0,     0, 1,     1, 0,     1, 1};
	
	public static final short BoxIndexedFaceSet[] = {
			0, 1, 2,   0, 2, 3,	//back
			4, 7, 6,   4, 6, 5, //front
			3, 2, 6,   3, 6, 7, //left
			0, 4, 5,   5, 1, 0, //right
			3, 7, 4,   3, 4, 0, //top
			2, 1, 6,   1, 5, 6 // bottom
			};
	
	public static final short BoxVertexNormalIndex[] = {
			0, 0, 0, 0, 0, 0,	//back
			1, 1, 1, 1, 1, 1, // front
			2, 2, 2, 2, 2, 2, // left
			3, 3, 3, 3, 3, 3, // right
			4, 4, 4, 4, 4, 4, // top
			5, 5, 5, 5, 5, 5  // bottom
		};
	
	public static final short BoxTextureCoordinateIndex[] = {
			1, 0, 2,  0, 2, 3,	//back
			3, 1, 0,  3, 0, 2, // front
			1, 0, 2,  1, 2, 3, // left
			3, 1, 0,  0, 2, 3, // right
			1, 0, 2,  1, 2, 3, // top
			2, 0, 3,  0, 1, 3  // bottom
		};
	

	public Box () {
		vertices = new float[BoxVertices.length];
		for (int i = 0; i < vertices.length; i++) vertices[i] = BoxVertices[i];
			
		vertexNormals = new float[BoxVertexNormals.length];
		for (int i = 0; i < vertexNormals.length; i++) vertices[i] = BoxVertexNormals[i];
		
		textureCoordinates = new float[TextureCoordinates.length];
		for (int i = 0; i < textureCoordinates.length; i++) textureCoordinates[i] = TextureCoordinates[i];
		
		indexedFaceSet = new short[BoxIndexedFaceSet.length];
		for (int i = 0; i < indexedFaceSet.length; i++) indexedFaceSet[i] = BoxIndexedFaceSet[i];
		
		indexedVertexNormals = new short[BoxVertexNormalIndex.length];
		for (int i = 0; i < indexedVertexNormals.length; i++) indexedVertexNormals[i] = BoxVertexNormalIndex[i];
		
		indexedTextureCoordinates = new short[BoxTextureCoordinateIndex.length];
		for (int i = 0; i < indexedTextureCoordinates.length; i++) indexedTextureCoordinates[i] = BoxTextureCoordinateIndex[i];

	}

}
