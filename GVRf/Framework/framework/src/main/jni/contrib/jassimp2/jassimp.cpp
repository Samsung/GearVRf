#include "../jassimp2/jassimp.h"
#include "memory_file.h"

#include <assimp/cfileio.h>
#include <assimp/cimport.h>
#include <assimp/scene.h>

#include "android/asset_manager_jni.h"

#ifdef JNI_LOG
#ifdef ANDROID
#include <android/log.h>
#define lprintf(...) __android_log_print(ANDROID_LOG_VERBOSE, __func__, __VA_ARGS__)
#else
#define lprintf(...) printf (__VA_ARGS__)
#endif /* ANDROID */
#else
#define lprintf
#endif

class DeleteLocalRef {
private:
    JNIEnv* mEnv;
    jobject& mObject;
public:
    DeleteLocalRef(JNIEnv* env, jobject& object) : mEnv(env), mObject(object) {};
    DeleteLocalRef(JNIEnv* env, jclass& clazz) : mEnv(env), mObject((jobject&) clazz) {};
    DeleteLocalRef(JNIEnv* env, jbyteArray& barray) : mEnv(env), mObject((jobject&) barray) {};
    DeleteLocalRef(JNIEnv* env, jfloatArray& farray) : mEnv(env), mObject((jobject&) farray) {};
    DeleteLocalRef(JNIEnv* env, jintArray& iarray) : mEnv(env), mObject((jobject&) iarray) {};
    DeleteLocalRef(JNIEnv* env, jstring& str) : mEnv(env), mObject((jobject&) str) {};
    ~DeleteLocalRef() {
        if (mObject != NULL) {
            mEnv->DeleteLocalRef(mObject);
        }
    }
};

static bool createInstance(JNIEnv *env, const char* className, jobject& newInstance)
{
	jclass clazz = env->FindClass(className);
    DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not find class %s\n", className);
		return false;
	}

	jmethodID ctr_id = env->GetMethodID(clazz, "<init>", "()V");

	if (NULL == ctr_id)
	{
		lprintf("could not find no-arg constructor for class %s\n", className);
		return false;
	}

	newInstance = env->NewObject(clazz, ctr_id);
	if (NULL == newInstance) 
	{
		lprintf("error calling no-arg constructor for class %s\n", className);
		return false;
	}

	return true;
}


static bool createInstance(JNIEnv *env, const char* className, const char* signature,/* const*/ jvalue* params, jobject& newInstance)
{
	jclass clazz = env->FindClass(className);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not find class %s\n", className);
		return false;
	}

	jmethodID ctr_id = env->GetMethodID(clazz, "<init>", signature);

	if (NULL == ctr_id)
	{
		lprintf("could not find no-arg constructor for class %s\n", className);
		return false;
	}

	newInstance = env->NewObjectA(clazz, ctr_id, params);

	if (NULL == newInstance) 
	{
		lprintf("error calling  constructor for class %s, signature %s\n", className, signature);
		return false;
	}

	return true;
}


static bool getField(JNIEnv *env, jobject object, const char* fieldName, const char* signature, jobject& field)
{
	jclass clazz = env->GetObjectClass(object);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not get class for object\n");
		return false;
	}

	jfieldID fieldId = env->GetFieldID(clazz, fieldName, signature);

	if (NULL == fieldId)
	{
		lprintf("could not get field %s with signature %s\n", fieldName, signature);
		return false;
	}

	field = env->GetObjectField(object, fieldId);

	return true;
}


static bool setIntField(JNIEnv *env, jobject object, const char* fieldName, jint value)
{
	jclass clazz = env->GetObjectClass(object);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not get class for object\n");
		return false;
	}

	jfieldID fieldId = env->GetFieldID(clazz, fieldName, "I");

	if (NULL == fieldId)
	{
		lprintf("could not get field %s with signature I\n", fieldName);
		return false;
	}

	env->SetIntField(object, fieldId, value);

	return true;
}


static bool setFloatField(JNIEnv *env, jobject object, const char* fieldName, jfloat value)
{
	jclass clazz = env->GetObjectClass(object);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not get class for object\n");
		return false;
	}

	jfieldID fieldId = env->GetFieldID(clazz, fieldName, "F");

	if (NULL == fieldId)
	{
		lprintf("could not get field %s with signature F\n", fieldName);
		return false;
	}

	env->SetFloatField(object, fieldId, value);

	return true;
}


static bool setObjectField(JNIEnv *env, jobject object, const char* fieldName, const char* signature, jobject value)
{
	jclass clazz = env->GetObjectClass(object);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not get class for object\n");
		return false;
	}

	jfieldID fieldId = env->GetFieldID(clazz, fieldName, signature);

	if (NULL == fieldId)
	{
		lprintf("could not get field %s with signature %s\n", fieldName, signature);
		return false;
	}

	env->SetObjectField(object, fieldId, value);

	return true;
}


static bool getStaticField(JNIEnv *env, const char* className, const char* fieldName, const char* signature, jobject& field)
{
	jclass clazz = env->FindClass(className);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not find class %s\n", className);
		return false;
	}

	jfieldID fieldId = env->GetFieldID(clazz, fieldName, signature);

	if (NULL == fieldId)
	{
		lprintf("could not get field %s with signature %s\n", fieldName, signature);
		return false;
	}

	field = env->GetStaticObjectField(clazz, fieldId);

	return true;
}


static bool call(JNIEnv *env, jobject object, const char* typeName, const char* methodName, 
	const char* signature,/* const*/ jvalue* params)
{
	jclass clazz = env->FindClass(typeName);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not find class %s\n", typeName);
		return false;
	}

	jmethodID mid = env->GetMethodID(clazz, methodName, signature);

	if (NULL == mid)
	{
		lprintf("could not find method %s with signature %s in type %s\n", methodName, signature, typeName);
		return false;
	}

	jboolean jReturnValue = env->CallBooleanMethod(object, mid, params[0].l);

	return (bool)jReturnValue;
}

static bool callv(JNIEnv *env, jobject object, const char* typeName,
		const char* methodName, const char* signature,/* const*/ jvalue* params) {
	jclass clazz = env->FindClass(typeName);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz) {
		lprintf("could not find class %s\n", typeName);
		return false;
	}

	jmethodID mid = env->GetMethodID(clazz, methodName, signature);

	if (NULL == mid) {
		lprintf("could not find method %s with signature %s in type %s\n", methodName, signature, typeName);
		return false;
	}

	env->CallVoidMethodA(object, mid, params);

	return true;
}

static jobject callj(JNIEnv *env, jobject object, const char* typeName, const char* methodName,
    const char* signature,/* const*/ jvalue* params)
{
    jclass clazz = env->FindClass(typeName);
    DeleteLocalRef clazzRef(env, clazz);

    if (NULL == clazz)
    {
        lprintf("could not find class %s\n", typeName);
        return NULL;
    }

    jmethodID mid = env->GetMethodID(clazz, methodName, signature);

    if (NULL == mid)
    {
        lprintf("could not find method %s with signature %s in type %s\n", methodName, signature, typeName);
        return NULL;
    }

    jobject jReturnValue = env->CallObjectMethodA(object, mid, params);

    return jReturnValue;
}

static bool callStaticObject(JNIEnv *env, const char* typeName, const char* methodName, 
	const char* signature,/* const*/ jvalue* params, jobject& returnValue)
{
	jclass clazz = env->FindClass(typeName);
	DeleteLocalRef clazzRef(env, clazz);

	if (NULL == clazz)
	{
		lprintf("could not find class %s\n", typeName);
		return false;
	}

	jmethodID mid = env->GetStaticMethodID(clazz, methodName, signature);

	if (NULL == mid)
	{
		lprintf("could not find method %s with signature %s in type %s\n", methodName, signature, typeName);
		return false;
	}

	returnValue = env->CallStaticObjectMethodA(clazz, mid, params);

	return true;
}


static bool copyBuffer(JNIEnv *env, jobject jMesh, const char* jBufferName, void* cData, size_t size)
{
	jobject jBuffer = NULL;
	DeleteLocalRef bufferRef(env, jBuffer);

	if (!getField(env, jMesh, jBufferName, "Ljava/nio/ByteBuffer;", jBuffer))
	{
		return false;
	}

	if (env->GetDirectBufferCapacity(jBuffer) != size)
	{
		lprintf("invalid direct buffer, expected %u, got %llu\n", (unsigned)size, env->GetDirectBufferCapacity(jBuffer));
		return false;
	}

	void* jBufferPtr = env->GetDirectBufferAddress(jBuffer);

	if (NULL == jBufferPtr)
	{
		lprintf("could not access direct buffer\n");
		return false;
	}

	memcpy(jBufferPtr, cData, size);

	return true;
}


static bool copyBufferArray(JNIEnv *env, jobject jMesh, const char* jBufferName, int index, void* cData, size_t size)
{
	jobject jBufferArray = NULL;
	DeleteLocalRef bufferArrayRef(env, jBufferArray);

	if (!getField(env, jMesh, jBufferName, "[Ljava/nio/ByteBuffer;", jBufferArray))
	{
		return false;
	}

	jobject jBuffer = env->GetObjectArrayElement((jobjectArray) jBufferArray, index);
	DeleteLocalRef bufferRef(env, jBuffer);

	if (env->GetDirectBufferCapacity(jBuffer) != size)
	{
		lprintf("invalid direct buffer, expected %u, got %llu\n", (unsigned)size, env->GetDirectBufferCapacity(jBuffer));
		return false;
	}

	void* jBufferPtr = env->GetDirectBufferAddress(jBuffer);

	if (NULL == jBufferPtr)
	{
		lprintf("could not access direct buffer\n");
		return false;
	}

	memcpy(jBufferPtr, cData, size);

	return true;
}



static bool loadMeshes(JNIEnv *env, const aiScene* cScene, jobject& jScene)
{
	for (unsigned int meshNr = 0; meshNr < cScene->mNumMeshes; meshNr++)
	{
		const aiMesh *cMesh = cScene->mMeshes[meshNr];

		lprintf("converting mesh %s ...\n", cMesh->mName.C_Str());

		/* create mesh */
		jobject jMesh = NULL;
		DeleteLocalRef refMesh(env, jMesh);

		if (!createInstance(env, "org/gearvrf/jassimp2/AiMesh", jMesh))
		{
			return false;
		}

		/* add mesh to m_meshes java.util.List */
		jobject jMeshes = NULL;
		DeleteLocalRef refMeshes(env, jMeshes);

		if (!getField(env, jScene, "m_meshes", "Ljava/util/List;", jMeshes))
		{
			return false;
		}

		jvalue addParams[1];
		addParams[0].l = jMesh;
		if (!call(env, jMeshes, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addParams))
		{
			return false;
		}


		/* set general mesh data in java */
		jvalue setTypesParams[1];
		setTypesParams[0].i = cMesh->mPrimitiveTypes;
		if (!callv(env, jMesh, "org/gearvrf/jassimp2/AiMesh", "setPrimitiveTypes", "(I)V", setTypesParams))
		{
			return false;
		}


		if (!setIntField(env, jMesh, "m_materialIndex", cMesh->mMaterialIndex))
		{
			return false;
		}

		jstring nameString = env->NewStringUTF(cMesh->mName.C_Str());
		DeleteLocalRef refNameString(env, nameString);
		if (!setObjectField(env, jMesh, "m_name", "Ljava/lang/String;", nameString))
		{
			return false;
		}

		/* determine face buffer size */
		bool isPureTriangle = cMesh->mPrimitiveTypes == aiPrimitiveType_TRIANGLE;
		size_t faceBufferSize;
		if (isPureTriangle) 
		{
			faceBufferSize = cMesh->mNumFaces * 3 * sizeof(unsigned int);
		}
		else
		{
			int numVertexReferences = 0;
			for (unsigned int face = 0; face < cMesh->mNumFaces; face++)
			{
				numVertexReferences += cMesh->mFaces[face].mNumIndices;
			}

			faceBufferSize = numVertexReferences * sizeof(unsigned int);
		}


		/* allocate buffers - we do this from java so they can be garbage collected */
		jvalue allocateBuffersParams[4];
		allocateBuffersParams[0].i = cMesh->mNumVertices;
		allocateBuffersParams[1].i = cMesh->mNumFaces;
		allocateBuffersParams[2].z = isPureTriangle;
		allocateBuffersParams[3].i = (jint) faceBufferSize;
		if (!callv(env, jMesh, "org/gearvrf/jassimp2/AiMesh", "allocateBuffers", "(IIZI)V", allocateBuffersParams))
		{
			return false;
		}


		if (cMesh->mNumVertices > 0)
		{
			/* push vertex data to java */
			if (!copyBuffer(env, jMesh, "m_vertices", cMesh->mVertices, cMesh->mNumVertices * sizeof(aiVector3D)))
			{
				lprintf("could not copy vertex data\n");
				return false;
			}

			lprintf("    with %u vertices\n", cMesh->mNumVertices);
		}


		/* push face data to java */
		if (cMesh->mNumFaces > 0)
		{
			if (isPureTriangle) 
			{
				char* faceBuffer = (char*) malloc(faceBufferSize);

				size_t faceDataSize = 3 * sizeof(unsigned int);
				for (unsigned int face = 0; face < cMesh->mNumFaces; face++)
				{
					memcpy(faceBuffer + face * faceDataSize, cMesh->mFaces[face].mIndices, faceDataSize);
				}

				bool res = copyBuffer(env, jMesh, "m_faces", faceBuffer, faceBufferSize);

				free(faceBuffer);

				if (!res) 
				{
					lprintf("could not copy face data\n");
					return false;
				}
			}
			else
			{
				char* faceBuffer = (char*) malloc(faceBufferSize);
				char* offsetBuffer = (char*) malloc(cMesh->mNumFaces * sizeof(unsigned int));

				size_t faceBufferPos = 0;
				for (unsigned int face = 0; face < cMesh->mNumFaces; face++)
				{
					size_t faceBufferOffset = faceBufferPos / sizeof(unsigned int);
					memcpy(offsetBuffer + face * sizeof(unsigned int), &faceBufferOffset, sizeof(unsigned int));

					size_t faceDataSize = cMesh->mFaces[face].mNumIndices * sizeof(unsigned int);
					memcpy(faceBuffer + faceBufferPos, cMesh->mFaces[face].mIndices, faceDataSize);
					faceBufferPos += faceDataSize;
				}
		
				if (faceBufferPos != faceBufferSize)
				{
					/* this should really not happen */
					lprintf("faceBufferPos %u, faceBufferSize %u\n", (unsigned)faceBufferPos, (unsigned)faceBufferSize);
					env->FatalError("error copying face data");
					exit(-1);
				}


				bool res = copyBuffer(env, jMesh, "m_faces", faceBuffer, faceBufferSize);
				res &= copyBuffer(env, jMesh, "m_faceOffsets", offsetBuffer, cMesh->mNumFaces * sizeof(unsigned int));

				free(faceBuffer);
				free(offsetBuffer);

				if (!res) 
				{
					lprintf("could not copy face data\n");
					return false;
				}
			}

			lprintf("    with %u faces\n", cMesh->mNumFaces);
		}


		/* push normals to java */
		if (cMesh->HasNormals())
		{
			jvalue allocateDataChannelParams[2];
			allocateDataChannelParams[0].i = 0;
			allocateDataChannelParams[1].i = 0;
			if (!callv(env, jMesh, "org/gearvrf/jassimp2/AiMesh", "allocateDataChannel", "(II)V", allocateDataChannelParams))
			{
				lprintf("could not allocate normal data channel\n");
				return false;
			}
			if (!copyBuffer(env, jMesh, "m_normals", cMesh->mNormals, cMesh->mNumVertices * 3 * sizeof(float)))
			{
				lprintf("could not copy normal data\n");
				return false;
			}

			lprintf("    with normals\n");
		}


		/* push tangents to java */
		if (cMesh->mTangents != NULL)
		{
			jvalue allocateDataChannelParams[2];
			allocateDataChannelParams[0].i = 1;
			allocateDataChannelParams[1].i = 0;
			if (!callv(env, jMesh, "org/gearvrf/jassimp2/AiMesh", "allocateDataChannel", "(II)V", allocateDataChannelParams))
			{
				lprintf("could not allocate tangents data channel\n");
				return false;
			}
			if (!copyBuffer(env, jMesh, "m_tangents", cMesh->mTangents, cMesh->mNumVertices * 3 * sizeof(float)))
			{
				lprintf("could not copy tangents data\n");
				return false;
			}

			lprintf("    with tangents\n");
		}


		/* push bitangents to java */
		if (cMesh->mBitangents != NULL)
		{
			jvalue allocateDataChannelParams[2];
			allocateDataChannelParams[0].i = 2;
			allocateDataChannelParams[1].i = 0;
			if (!callv(env, jMesh, "org/gearvrf/jassimp2/AiMesh", "allocateDataChannel", "(II)V", allocateDataChannelParams))
			{
				lprintf("could not allocate bitangents data channel\n");
				return false;
			}
			if (!copyBuffer(env, jMesh, "m_bitangents", cMesh->mBitangents, cMesh->mNumVertices * 3 * sizeof(float)))
			{
				lprintf("could not copy bitangents data\n");
				return false;
			}

			lprintf("    with bitangents\n");
		}


		/* push color sets to java */
		for (int c = 0; c < AI_MAX_NUMBER_OF_COLOR_SETS; c++)
		{
			if (cMesh->mColors[c] != NULL)
			{
				jvalue allocateDataChannelParams[2];
				allocateDataChannelParams[0].i = 3;
				allocateDataChannelParams[1].i = c;
				if (!callv(env, jMesh, "org/gearvrf/jassimp2/AiMesh", "allocateDataChannel", "(II)V", allocateDataChannelParams))
				{
					lprintf("could not allocate colorset data channel\n");
					return false;
				}
				if (!copyBufferArray(env, jMesh, "m_colorsets", c, cMesh->mColors[c], cMesh->mNumVertices * 4 * sizeof(float)))
				{
					lprintf("could not copy colorset data\n");
					return false;
				}

				lprintf("    with colorset[%d]\n", c);
			}
		}


		/* push tex coords to java */
		for (int c = 0; c < AI_MAX_NUMBER_OF_TEXTURECOORDS; c++)
		{
			if (cMesh->mTextureCoords[c] != NULL)
			{
				jvalue allocateDataChannelParams[2];

				switch (cMesh->mNumUVComponents[c])
				{
				case 1:
					allocateDataChannelParams[0].i = 4;
					break;
				case 2:
					allocateDataChannelParams[0].i = 5;
					break;
				case 3:
					allocateDataChannelParams[0].i = 6;
					break;
				default:
					return false;
				}

				allocateDataChannelParams[1].i = c;
				if (!callv(env, jMesh, "org/gearvrf/jassimp2/AiMesh", "allocateDataChannel", "(II)V", allocateDataChannelParams))
				{
					lprintf("could not allocate texture coordinates data channel\n");
					return false;
				}

				/* gather data */
				size_t coordBufferSize = cMesh->mNumVertices * cMesh->mNumUVComponents[c] * sizeof(float);
				char* coordBuffer = (char*) malloc(coordBufferSize);
				size_t coordBufferOffset = 0;

				for (unsigned int v = 0; v < cMesh->mNumVertices; v++)
				{
					memcpy(coordBuffer + coordBufferOffset, &cMesh->mTextureCoords[c][v], cMesh->mNumUVComponents[c] * sizeof(float));
					coordBufferOffset += cMesh->mNumUVComponents[c] * sizeof(float);
				}

				if (coordBufferOffset != coordBufferSize)
				{
					/* this should really not happen */
					lprintf("coordBufferPos %u, coordBufferSize %u\n", (unsigned)coordBufferOffset, (unsigned)coordBufferSize);
					env->FatalError("error copying coord data");
					exit(-1);
				}

				bool res = copyBufferArray(env, jMesh, "m_texcoords", c, coordBuffer, coordBufferSize);

				free(coordBuffer);

				if (!res)
				{
					lprintf("could not copy texture coordinates data\n");
					return false;
				}

				lprintf("    with %uD texcoord[%d]\n", cMesh->mNumUVComponents[c], c);
			}
		}


		for (unsigned int b = 0; b < cMesh->mNumBones; b++)
		{
			aiBone *cBone = cMesh->mBones[b];

			jobject jBone;
			DeleteLocalRef refBone(env, jBone);
			if (!createInstance(env, "org/gearvrf/jassimp2/AiBone", jBone))
			{
				return false;
			}

			/* add bone to bone list */
			jobject jBones = NULL;
			DeleteLocalRef refBones(env, jBones);
			if (!getField(env, jMesh, "m_bones", "Ljava/util/List;", jBones))
			{
				return false;
			}

			jvalue addParams[1];
			addParams[0].l = jBone;
			if (!call(env, jBones, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addParams))
			{
				return false;
			}

			/* set bone data */
			jstring boneNameString = env->NewStringUTF(cBone->mName.C_Str());
			DeleteLocalRef refNameString(env, boneNameString);
			if (!setObjectField(env, jBone, "m_name", "Ljava/lang/String;", boneNameString))
			{
				return false;
			}

			/* add bone weights */
			for (unsigned int w = 0; w < cBone->mNumWeights; w++)
			{
				jobject jBoneWeight;
				DeleteLocalRef refBoneWeight(env, jBoneWeight);
				if (!createInstance(env, "org/gearvrf/jassimp2/AiBoneWeight", jBoneWeight))
				{
					return false;
				}

				/* add boneweight to bone list */
				jobject jBoneWeights = NULL;
				DeleteLocalRef refBoneWeights(env, jBoneWeights);
				if (!getField(env, jBone, "m_boneWeights", "Ljava/util/List;", jBoneWeights))
				{
					return false;
				}

				/* copy offset matrix */
				jfloatArray jMatrixArr = env->NewFloatArray(16);
				DeleteLocalRef refMatrixArr(env, jMatrixArr);
				env->SetFloatArrayRegion(jMatrixArr, 0, 16, (jfloat*) &cBone->mOffsetMatrix);

				jvalue wrapParams[1];
				wrapParams[0].l = jMatrixArr;
				jobject jMatrix;
				DeleteLocalRef refMatrix(env, jMatrix);
				
				if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapMatrix", "([F)Ljava/lang/Object;", wrapParams, jMatrix))
				{
					return false;
				}

				if (!setObjectField(env, jBone, "m_offsetMatrix", "Ljava/lang/Object;", jMatrix))
				{
					return false;
				}


				jvalue addBwParams[1];
				addBwParams[0].l = jBoneWeight;
				if (!call(env, jBoneWeights, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addBwParams))
				{
					return false;
				}


				if (!setIntField(env, jBoneWeight, "m_vertexId", cBone->mWeights[w].mVertexId))
				{
					return false;
				}
				
				if (!setFloatField(env, jBoneWeight, "m_weight", cBone->mWeights[w].mWeight))
				{
					return false;
				}
			}
		}
	}

	return true;
}


static bool loadSceneNode(JNIEnv *env, const aiNode *cNode, jobject parent, jobject* loadedNode = NULL) 
{
	lprintf("   converting node %s ...\n", cNode->mName.C_Str());

	/* wrap matrix */
	jfloatArray jMatrixArr = env->NewFloatArray(16);
	DeleteLocalRef refMatrixArr(env, jMatrixArr);
	env->SetFloatArrayRegion(jMatrixArr, 0, 16, (jfloat*) &cNode->mTransformation);

	jvalue wrapMatParams[1];
	wrapMatParams[0].l = jMatrixArr;
	jobject jMatrix;
	DeleteLocalRef refMatrix(env, jMatrix);
				
	if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapMatrix", "([F)Ljava/lang/Object;", wrapMatParams, jMatrix))
	{
		return false;
	}

	/* create mesh references array */
	jintArray jMeshrefArr = env->NewIntArray(cNode->mNumMeshes);
	DeleteLocalRef refMeshrefArr(env, jMeshrefArr);

	jint *temp = (jint*) malloc(sizeof(jint) * cNode->mNumMeshes);

	for (unsigned int i = 0; i < cNode->mNumMeshes; i++)
	{
		temp[i] = cNode->mMeshes[i];
	}
	env->SetIntArrayRegion(jMeshrefArr, 0, cNode->mNumMeshes, (jint*) temp);

	free(temp);


	/* convert name */
	jstring jNodeName = env->NewStringUTF(cNode->mName.C_Str());
	DeleteLocalRef refNodeName(env, jNodeName);

	/* wrap scene node */
	jvalue wrapNodeParams[4];
	wrapNodeParams[0].l = parent;
	wrapNodeParams[1].l = jMatrix;
	wrapNodeParams[2].l = jMeshrefArr;
	wrapNodeParams[3].l = jNodeName;
	jobject jNode;

	if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapSceneNode",
		"(Ljava/lang/Object;Ljava/lang/Object;[ILjava/lang/String;)Ljava/lang/Object;", wrapNodeParams, jNode)) 
	{
		return false;
	}

	/* and recurse */
	for (unsigned int c = 0; c < cNode->mNumChildren; c++)
	{
		if (!loadSceneNode(env, cNode->mChildren[c], jNode))
		{
			return false;
		}
	}

	if (NULL != loadedNode)
	{
		*loadedNode = jNode;
	}

	return true;
}


static bool loadSceneGraph(JNIEnv *env, const aiScene* cScene, jobject& jScene)
{
	lprintf("converting scene graph ...\n");

	if (NULL != cScene->mRootNode)
	{
		jobject jRoot;

		if (!loadSceneNode(env, cScene->mRootNode, NULL, &jRoot))
		{
			return false;
		}

		if (!setObjectField(env, jScene, "m_sceneRoot", "Ljava/lang/Object;", jRoot))
		{
			return false;
		}
	}

	lprintf("converting scene graph finished\n");

	return true;
}


static bool loadMaterials(JNIEnv *env, const aiScene* cScene, jobject& jScene) 
{
	for (unsigned int m = 0; m < cScene->mNumMaterials; m++)
	{
		const aiMaterial* cMaterial = cScene->mMaterials[m];

		lprintf("converting material %d ...\n", m);

		jobject jMaterial = NULL;
		DeleteLocalRef refMaterial(env, jMaterial);

		if (!createInstance(env, "org/gearvrf/jassimp2/AiMaterial", jMaterial))
		{
			return false;
		}

		/* add material to m_materials java.util.List */
		jobject jMaterials = NULL;
		DeleteLocalRef refMaterials(env, jMaterials);

		if (!getField(env, jScene, "m_materials", "Ljava/util/List;", jMaterials))
		{
			return false;
		}

		jvalue addMatParams[1];
		addMatParams[0].l = jMaterial;
		if (!call(env, jMaterials, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addMatParams))
		{
			return false;
		}

		/* set texture numbers */
		for (int ttInd = aiTextureType_DIFFUSE; ttInd < aiTextureType_UNKNOWN; ttInd++) 
		{
			aiTextureType tt = static_cast<aiTextureType>(ttInd);

			unsigned int num = cMaterial->GetTextureCount(tt);

			lprintf("   found %d textures of type %d ...\n", num, ttInd);

			jvalue setNumberParams[2];
			setNumberParams[0].i = ttInd;
			setNumberParams[1].i = num;

			if (!callv(env, jMaterial, "org/gearvrf/jassimp2/AiMaterial", "setTextureNumber", "(II)V", setNumberParams))
			{
				return false;
			}
		}


		for (unsigned int p = 0; p < cMaterial->mNumProperties; p++)
		{
			//printf("%s - %u - %u\n", cScene->mMaterials[m]->mProperties[p]->mKey.C_Str(), 
			//	cScene->mMaterials[m]->mProperties[p]->mSemantic,
			//	cScene->mMaterials[m]->mProperties[p]->mDataLength);

			const aiMaterialProperty* cProperty = cMaterial->mProperties[p];

			lprintf("   converting property %s ...\n", cProperty->mKey.C_Str());

			jobject jProperty = NULL;
			DeleteLocalRef refProperty(env, jProperty);

			jvalue constructorParams[5];
			jstring keyString = env->NewStringUTF(cProperty->mKey.C_Str());
			DeleteLocalRef refKeyString(env, keyString);
			constructorParams[0].l = keyString;
			constructorParams[1].i = cProperty->mSemantic;
			constructorParams[2].i = cProperty->mIndex;
			constructorParams[3].i = cProperty->mType;


			/* special case conversion for color3 */
			if (NULL != strstr(cProperty->mKey.C_Str(), "clr") && 
				cProperty->mType == aiPTI_Float &&
				cProperty->mDataLength == 3 * sizeof(float)) 
			{
				jobject jData = NULL;
				DeleteLocalRef refData(env, jData);

				/* wrap color */
				jvalue wrapColorParams[3];
				wrapColorParams[0].f = ((float*) cProperty->mData)[0];
				wrapColorParams[1].f = ((float*) cProperty->mData)[1];
				wrapColorParams[2].f = ((float*) cProperty->mData)[2];
				if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapColor3", "(FFF)Ljava/lang/Object;", wrapColorParams, jData))
				{
					return false;
				}

				constructorParams[4].l = jData;
				if (!createInstance(env, "org/gearvrf/jassimp2/AiMaterial$Property", "(Ljava/lang/String;IIILjava/lang/Object;)V",
					constructorParams, jProperty))
				{
					return false;
				}
			}
			/* special case conversion for color4 */
			else if (NULL != strstr(cProperty->mKey.C_Str(), "clr") && 
				cProperty->mType == aiPTI_Float &&
				cProperty->mDataLength == 4 * sizeof(float)) 
			{
				jobject jData = NULL;
				DeleteLocalRef refData(env, jData);

				/* wrap color */
				jvalue wrapColorParams[4];
				wrapColorParams[0].f = ((float*) cProperty->mData)[0];
				wrapColorParams[1].f = ((float*) cProperty->mData)[1];
				wrapColorParams[2].f = ((float*) cProperty->mData)[2];
				wrapColorParams[3].f = ((float*) cProperty->mData)[3];
				if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapColor4", "(FFFF)Ljava/lang/Object;", wrapColorParams, jData))
				{
					return false;
				}

				constructorParams[4].l = jData;
				if (!createInstance(env, "org/gearvrf/jassimp2/AiMaterial$Property", "(Ljava/lang/String;IIILjava/lang/Object;)V",
					constructorParams, jProperty))
				{
					return false;
				}

			}
			else if (cProperty->mType == aiPTI_Float && cProperty->mDataLength == sizeof(float)) 
			{
				jobject jData = NULL;
				DeleteLocalRef refData(env, jData);

				jvalue newFloatParams[1];
				newFloatParams[0].f = ((float*) cProperty->mData)[0];
				if (!createInstance(env, "java/lang/Float", "(F)V", newFloatParams, jData))
				{
					return false;
				}

				constructorParams[4].l = jData;
				if (!createInstance(env, "org/gearvrf/jassimp2/AiMaterial$Property", "(Ljava/lang/String;IIILjava/lang/Object;)V",
					constructorParams, jProperty))
				{
					return false;
				}
			}
			else if (cProperty->mType == aiPTI_Integer && cProperty->mDataLength == sizeof(int)) 
			{
				jobject jData = NULL;
				DeleteLocalRef refData(env, jData);

				jvalue newIntParams[1];
				newIntParams[0].i = ((int*) cProperty->mData)[0];
				if (!createInstance(env, "java/lang/Integer", "(I)V", newIntParams, jData))
				{
					return false;
				}

				constructorParams[4].l = jData;
				if (!createInstance(env, "org/gearvrf/jassimp2/AiMaterial$Property", "(Ljava/lang/String;IIILjava/lang/Object;)V",
					constructorParams, jProperty))
				{
					return false;
				}
			}
			else if (cProperty->mType == aiPTI_String) 
			{
				/* skip length prefix */
				jobject jData = env->NewStringUTF(cProperty->mData + 4);
				DeleteLocalRef refData(env, jData);

				constructorParams[4].l = jData;
				if (!createInstance(env, "org/gearvrf/jassimp2/AiMaterial$Property", "(Ljava/lang/String;IIILjava/lang/Object;)V",
					constructorParams, jProperty))
				{
					return false;
				}
			}
			else 
			{
				constructorParams[4].i = cProperty->mDataLength;

				/* generic copy code, uses dump ByteBuffer on java side */
				if (!createInstance(env, "org/gearvrf/jassimp2/AiMaterial$Property", "(Ljava/lang/String;IIII)V", constructorParams, jProperty))
				{
					return false;
				}

				jobject jBuffer = NULL;
				DeleteLocalRef refBuffer(env, jBuffer);
				if (!getField(env, jProperty, "m_data", "Ljava/lang/Object;", jBuffer))
				{
					return false;
				}

				if (env->GetDirectBufferCapacity(jBuffer) != cProperty->mDataLength)
				{
					lprintf("invalid direct buffer\n");
					return false;
				}

				void* jBufferPtr = env->GetDirectBufferAddress(jBuffer);

				if (NULL == jBufferPtr)
				{
					lprintf("could not access direct buffer\n");
					return false;
				}

				memcpy(jBufferPtr, cProperty->mData, cProperty->mDataLength);
			}


			/* add property */
			jobject jProperties = NULL;
			DeleteLocalRef refProperties(env, jProperties);
			if (!getField(env, jMaterial, "m_properties", "Ljava/util/List;", jProperties))
			{
				return false;
			}

			jvalue addPropParams[1];
			addPropParams[0].l = jProperty;
			if (!call(env, jProperties, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addPropParams))
			{
				return false;
			}
		}
	}

	lprintf("materials finished\n");

	return true;
}


static bool loadAnimations(JNIEnv *env, const aiScene* cScene, jobject& jScene) 
{
	lprintf("converting %d animations ...\n", cScene->mNumAnimations);

	for (unsigned int a = 0; a < cScene->mNumAnimations; a++)
	{
		const aiAnimation *cAnimation = cScene->mAnimations[a];

		lprintf("   converting animation %s ...\n", cAnimation->mName.C_Str());

		jobject jAnimation;
		DeleteLocalRef refAnimation(env, jAnimation);

		jvalue newAnimParams[3];
		jstring nameString = env->NewStringUTF(cAnimation->mName.C_Str());
		DeleteLocalRef refNameString(env, nameString);
		newAnimParams[0].l = nameString;
		newAnimParams[1].d = cAnimation->mDuration;
		newAnimParams[2].d = cAnimation->mTicksPerSecond;

		if (!createInstance(env, "org/gearvrf/jassimp2/AiAnimation", "(Ljava/lang/String;DD)V", newAnimParams, jAnimation))
		{
			return false;
		}

		/* add animation to m_animations java.util.List */
		jobject jAnimations = NULL;
		DeleteLocalRef refAnimations(env, jAnimations);

		if (!getField(env, jScene, "m_animations", "Ljava/util/List;", jAnimations))
		{
			return false;
		}

		jvalue addParams[1];
		addParams[0].l = jAnimation;
		if (!call(env, jAnimations, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addParams))
		{
			return false;
		}


		for (unsigned int c = 0; c < cAnimation->mNumChannels; c++)
		{
			const aiNodeAnim *cNodeAnim = cAnimation->mChannels[c];

			jobject jNodeAnim;
			DeleteLocalRef refNodeAnim(env, jNodeAnim);

			jvalue newNodeAnimParams[6];
			jstring animationName = env->NewStringUTF(cNodeAnim->mNodeName.C_Str());
			DeleteLocalRef refAnimationName(env, animationName);
			newNodeAnimParams[0].l = animationName;
			newNodeAnimParams[1].i = cNodeAnim->mNumPositionKeys;
			newNodeAnimParams[2].i = cNodeAnim->mNumRotationKeys;
			newNodeAnimParams[3].i = cNodeAnim->mNumScalingKeys;
			newNodeAnimParams[4].i = cNodeAnim->mPreState;
			newNodeAnimParams[5].i = cNodeAnim->mPostState;

			if (!createInstance(env, "org/gearvrf/jassimp2/AiNodeAnim", "(Ljava/lang/String;IIIII)V", newNodeAnimParams, jNodeAnim))
			{
				return false;
			}


			/* add nodeanim to m_animations java.util.List */
			jobject jNodeAnims = NULL;
			DeleteLocalRef refNodeAnims(env, jNodeAnims);

			if (!getField(env, jAnimation, "m_nodeAnims", "Ljava/util/List;", jNodeAnims))
			{
				return false;
			}

			jvalue addParams[1];
			addParams[0].l = jNodeAnim;
			if (!call(env, jNodeAnims, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addParams))
			{
				return false;
			}

			/* copy keys */
			if (!copyBuffer(env, jNodeAnim, "m_posKeys", cNodeAnim->mPositionKeys, 
				cNodeAnim->mNumPositionKeys * sizeof(aiVectorKey)))
			{
				return false;
			}

			if (!copyBuffer(env, jNodeAnim, "m_rotKeys", cNodeAnim->mRotationKeys, 
				cNodeAnim->mNumRotationKeys * sizeof(aiQuatKey)))
			{
				return false;
			}

			if (!copyBuffer(env, jNodeAnim, "m_scaleKeys", cNodeAnim->mScalingKeys, 
				cNodeAnim->mNumScalingKeys * sizeof(aiVectorKey)))
			{
				return false;
			}
		}
	}

	lprintf("converting animations finished\n");

	return true;
}


static bool loadLights(JNIEnv *env, const aiScene* cScene, jobject& jScene) 
{
	lprintf("converting %d lights ...\n", cScene->mNumLights);

	for (unsigned int l = 0; l < cScene->mNumLights; l++)
	{
		const aiLight *cLight = cScene->mLights[l];

		lprintf("converting light %s ...\n", cLight->mName.C_Str());

		/* wrap color nodes */
		jvalue wrapColorParams[3];
		wrapColorParams[0].f = cLight->mColorDiffuse.r;
		wrapColorParams[1].f = cLight->mColorDiffuse.g;
		wrapColorParams[2].f = cLight->mColorDiffuse.b;
		jobject jDiffuse;
		DeleteLocalRef refDiffuse(env, jDiffuse);
		if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapColor3", "(FFF)Ljava/lang/Object;", wrapColorParams, jDiffuse))
		{
			return false;
		}

		wrapColorParams[0].f = cLight->mColorSpecular.r;
		wrapColorParams[1].f = cLight->mColorSpecular.g;
		wrapColorParams[2].f = cLight->mColorSpecular.b;
		jobject jSpecular;
		DeleteLocalRef refSpecular(env, jSpecular);
		if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapColor3", "(FFF)Ljava/lang/Object;", wrapColorParams, jSpecular))
		{
			return false;
		}

		wrapColorParams[0].f = cLight->mColorAmbient.r;
		wrapColorParams[1].f = cLight->mColorAmbient.g;
		wrapColorParams[2].f = cLight->mColorAmbient.b;
		jobject jAmbient;
		DeleteLocalRef refAmbient(env, jAmbient);
		if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapColor3", "(FFF)Ljava/lang/Object;", wrapColorParams, jAmbient))
		{
			return false;
		}


		/* wrap vec3 nodes */
		jvalue wrapVec3Params[3];
		wrapVec3Params[0].f = cLight->mPosition.x;
		wrapVec3Params[1].f = cLight->mPosition.y;
		wrapVec3Params[2].f = cLight->mPosition.z;
		jobject jPosition;
		DeleteLocalRef refPosition(env, jPosition);
		if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapVec3", "(FFF)Ljava/lang/Object;", wrapVec3Params, jPosition))
		{
			return false;
		}

		wrapVec3Params[0].f = cLight->mPosition.x;
		wrapVec3Params[1].f = cLight->mPosition.y;
		wrapVec3Params[2].f = cLight->mPosition.z;
		jobject jDirection;
		DeleteLocalRef refDirection(env, jDirection);
		if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapVec3", "(FFF)Ljava/lang/Object;", wrapVec3Params, jDirection))
		{
			return false;
		}


		jobject jLight;
		DeleteLocalRef refLight(env, jLight);
		jvalue params[12];
		jstring lightName = env->NewStringUTF(cLight->mName.C_Str());
		DeleteLocalRef refLightName(env, lightName);
		params[0].l = lightName;
		params[1].i = cLight->mType;
		params[2].l = jPosition;
		params[3].l = jDirection;
		params[4].f = cLight->mAttenuationConstant;
		params[5].f = cLight->mAttenuationLinear;
		params[6].f = cLight->mAttenuationQuadratic;
		params[7].l = jDiffuse;
		params[8].l = jSpecular;
		params[9].l = jAmbient;
		params[10].f = cLight->mAngleInnerCone;
		params[11].f = cLight->mAngleOuterCone;
		
		if (!createInstance(env, "org/gearvrf/jassimp2/AiLight", "(Ljava/lang/String;ILjava/lang/Object;Ljava/lang/Object;FFFLjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;FF)V",
			params, jLight))
		{
			return false;
		}

		/* add light to m_lights java.util.List */
		jobject jLights = NULL;
		DeleteLocalRef refLights(env, jLights);

		if (!getField(env, jScene, "m_lights", "Ljava/util/List;", jLights))
		{
			return false;
		}

		jvalue addParams[1];
		addParams[0].l = jLight;
		if (!call(env, jLights, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addParams))
		{
			return false;
		}
	}

	lprintf("converting lights finished ...\n"); 

	return true;
}


static bool loadCameras(JNIEnv *env, const aiScene* cScene, jobject& jScene) 
{
	lprintf("converting %d cameras ...\n", cScene->mNumCameras);

	for (unsigned int c = 0; c < cScene->mNumCameras; c++)
	{
		const aiCamera *cCamera = cScene->mCameras[c];

		lprintf("converting camera %s ...\n", cCamera->mName.C_Str());

		/* wrap color nodes */
		jvalue wrapPositionParams[3];
		wrapPositionParams[0].f = cCamera->mPosition.x;
		wrapPositionParams[1].f = cCamera->mPosition.y;
		wrapPositionParams[2].f = cCamera->mPosition.z;
		jobject jPosition;
		DeleteLocalRef refPosition(env, jPosition);
		if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapVec3", "(FFF)Ljava/lang/Object;", wrapPositionParams, jPosition))
		{
			return false;
		}

		wrapPositionParams[0].f = cCamera->mUp.x;
		wrapPositionParams[1].f = cCamera->mUp.y;
		wrapPositionParams[2].f = cCamera->mUp.z;
		jobject jUp;
		DeleteLocalRef refUp(env, jUp);
		if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapVec3", "(FFF)Ljava/lang/Object;", wrapPositionParams, jUp))
		{
			return false;
		}

		wrapPositionParams[0].f = cCamera->mLookAt.x;
		wrapPositionParams[1].f = cCamera->mLookAt.y;
		wrapPositionParams[2].f = cCamera->mLookAt.z;
		jobject jLookAt;
		DeleteLocalRef refLookAt(env, jLookAt);
		if (!callStaticObject(env, "org/gearvrf/jassimp2/Jassimp", "wrapVec3", "(FFF)Ljava/lang/Object;", wrapPositionParams, jLookAt))
		{
			return false;
		}


		jobject jCamera;
		DeleteLocalRef refCamera(env, jCamera);

		jvalue params[8];
		jstring cameraName = env->NewStringUTF(cCamera->mName.C_Str());
		DeleteLocalRef refCameraName(env, cameraName);
		params[0].l = cameraName;
		params[1].l = jPosition;
		params[2].l = jUp;
		params[3].l = jLookAt;
		params[4].f = cCamera->mHorizontalFOV;
		params[5].f = cCamera->mClipPlaneNear;
		params[6].f = cCamera->mClipPlaneFar;
		params[7].f = cCamera->mAspect;
		
		if (!createInstance(env, "org/gearvrf/jassimp2/AiCamera", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;FFFF)V",
			params, jCamera))
		{
			return false;
		}

		/* add camera to m_cameras java.util.List */
		jobject jCameras = NULL;
		DeleteLocalRef refCameras(env, jCameras);
		if (!getField(env, jScene, "m_cameras", "Ljava/util/List;", jCameras))
		{
			return false;
		}

		jvalue addParams[1];
		addParams[0].l = jCamera;
		if (!call(env, jCameras, "java/util/Collection", "add", "(Ljava/lang/Object;)Z", addParams))
		{
			return false;
		}
	}

	lprintf("converting cameras finished\n");

	return true;
}


JNIEXPORT jint JNICALL Java_org_gearvrf_jassimp2_Jassimp_getVKeysize
  (JNIEnv *env, jclass jClazz)
{
	const int res = sizeof(aiVectorKey);
	return res;
}

JNIEXPORT jint JNICALL Java_org_gearvrf_jassimp2_Jassimp_getQKeysize
  (JNIEnv *env, jclass jClazz)
{
	const int res = sizeof(aiQuatKey);
	return res;
}

JNIEXPORT jint JNICALL Java_org_gearvrf_jassimp2_Jassimp_getV3Dsize
  (JNIEnv *env, jclass jClazz)
{
	const int res = sizeof(aiVector3D);
	return res;
}

JNIEXPORT jint JNICALL Java_org_gearvrf_jassimp2_Jassimp_getfloatsize
  (JNIEnv *env, jclass jClazz)
{
	const int res = sizeof(float);
	return res;
}

JNIEXPORT jint JNICALL Java_org_gearvrf_jassimp2_Jassimp_getintsize
  (JNIEnv *env, jclass jClazz)
{
	const int res = sizeof(int);
	return res;
}

JNIEXPORT jint JNICALL Java_org_gearvrf_jassimp2_Jassimp_getuintsize
  (JNIEnv *env, jclass jClazz)
{
	const int res = sizeof(unsigned int);
	return res;
}

JNIEXPORT jint JNICALL Java_org_gearvrf_jassimp2_Jassimp_getdoublesize
  (JNIEnv *env, jclass jClazz)
{
	const int res = sizeof(double);
	return res;
}

JNIEXPORT jint JNICALL Java_org_gearvrf_jassimp2_Jassimp_getlongsize
  (JNIEnv *env, jclass jClazz)
{
	const int res = sizeof(long);
	return res;
}

JNIEXPORT jstring JNICALL Java_org_gearvrf_jassimp2_Jassimp_getErrorString
  (JNIEnv *env, jclass jClazz)
{
	const char *err = aiGetErrorString();

	if (NULL == err)
	{
		return env->NewStringUTF("");
	}

	return env->NewStringUTF(err);
}

static char *extractAsset(AAssetManager* mgr, const char *name, int *pBufferSize)
{
	// Open file
	AAsset* asset = AAssetManager_open(mgr, name, AASSET_MODE_UNKNOWN);

	char *pBuffer = NULL;

	if (asset != NULL) {
		// Find size
		off_t assetSize = AAsset_getLength(asset);

		// Prepare input buffer
		if (pBufferSize)
			*pBufferSize = assetSize;
		pBuffer = new char[assetSize];

		if (pBuffer) {
			// Store input buffer
			AAsset_read(asset, pBuffer, assetSize);

			lprintf("Assimp", "Asset extracted");
		}

        // Close
        AAsset_close(asset);
	} else {
		lprintf("Asset not found: %s", name);
		return 0;
	}
	return pBuffer;
}

// Memory File IO

struct FileOpsData {
    jobject jFileIO;
    JNIEnv *env;
};

static aiFile* aiFileOpen(C_STRUCT aiFileIO* fio, const char* name, const char* mode) {
    FileOpsData &opsData(*reinterpret_cast<FileOpsData*>(fio->UserData));
    JNIEnv *env = opsData.env;

    jstring jNameString = env->NewStringUTF(name);
    DeleteLocalRef refNameString(env, jNameString);

    jvalue readParams[1];
    readParams[0].l = jNameString;

    jbyteArray jByteArray = static_cast<jbyteArray>(callj(opsData.env, opsData.jFileIO, "org/gearvrf/jassimp2/JassimpFileIO", "read",
            "(Ljava/lang/String;)[B", readParams));
    DeleteLocalRef refByteArray(env, jByteArray);

    if (!jByteArray) {
        lprintf("JassimpFileIO.read returns null");
        return nullptr;
    }

    int len = env->GetArrayLength(jByteArray);
    unsigned char* buf = (unsigned char*)malloc(len);
    env->GetByteArrayRegion(jByteArray, 0, len, reinterpret_cast<jbyte*>(buf));

    // Wrap data as a memory-backed file
    aiFile *file = (aiFile *)calloc(1, sizeof(aiFile));
    MemoryFileData *fileData = (MemoryFileData *)calloc(1, sizeof(MemoryFileData));
    fileData->buf = buf;
    fileData->size = len;

    lprintf("ASSIMP before memcpy");
    memcpy(file, &memoryFilePrototype, sizeof(memoryFilePrototype));
    file->UserData = reinterpret_cast<char*>(fileData);
    lprintf("ASSIMP after memcpy");

    return file;
}

static void aiFileClose(C_STRUCT aiFileIO* fio, C_STRUCT aiFile* file) {
    MemoryFileData *fileData(reinterpret_cast<MemoryFileData *>(file->UserData));
    free(fileData->buf);
    free(file->UserData);
    free(file);
}

static jobject importHelper(JNIEnv *env, jclass jClazz, jstring jFilename, jlong postProcess,
                            jobject assetManager, jobject jFileIO)
{
	jobject jScene = NULL;

	/* convert params */
	const char* cFilename = env->GetStringUTFChars(jFilename, NULL);

	lprintf("opening file: %s%s\n", cFilename,
	        assetManager ? " from android assets"
	                     : (jFileIO ? " from custom fileIO" : ""));

	/* do import */
	const aiScene *cScene;
	if (assetManager) {
	    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

	    int assetSize;
	    char *pBuffer = extractAsset(mgr, cFilename, &assetSize);
	    if (!pBuffer)
	        return NULL;

	    char* extension = 0;
	    if (cFilename != 0) {
	        extension = strrchr(cFilename, '.');
	        if (extension && extension != cFilename) {
	            extension++;
	        }
	    }

	    cScene = aiImportFileFromMemory(pBuffer, assetSize, (unsigned int) postProcess,
	            extension);

	    delete pBuffer;
	} else if (jFileIO) {
	    FileOpsData fileOpsData {
	        .jFileIO = jFileIO,
	        .env = env
	    };

	    aiFileIO fileIO = {
	            .OpenProc = aiFileOpen,
	            .CloseProc = aiFileClose,
	            .UserData = reinterpret_cast<char*>(&fileOpsData)
	    };

	    cScene = aiImportFileEx(cFilename, (unsigned int) postProcess, &fileIO);
	} else {
	    cScene = aiImportFile(cFilename, (unsigned int) postProcess);
	}

	lprintf("jassimp aiImportFile done");
	if (!cScene)
	{
		lprintf("import file returned null\n");
		goto error;
	}

	if (!createInstance(env, "org/gearvrf/jassimp2/AiScene", jScene))
	{
		goto error;
	}
	lprintf("jassimp createInstance");

	if (!loadMeshes(env, cScene, jScene))
	{
		goto error;
	}
	lprintf("jassimp loadMeshes");

	if (!loadMaterials(env, cScene, jScene))
	{
		goto error;
	}
	lprintf("jassimp loadMaterials");

	if (!loadAnimations(env, cScene, jScene))
	{
		goto error;
	}
	lprintf("jassimp loadAnimations");

	if (!loadLights(env, cScene, jScene))
	{
		goto error;
	}
	lprintf("jassimp loadLights");

	if (!loadCameras(env, cScene, jScene))
	{
		goto error;
	}
	lprintf("jassimp loadCameras");

	if (!loadSceneGraph(env, cScene, jScene))
	{
		goto error;
	}
	lprintf("jassimp loadSceneGraph");

	/* jump over error handling section */
	goto end;

error:
	{
	jclass exception = env->FindClass("java/io/IOException");
	DeleteLocalRef refException(env, exception);

	if (NULL == exception)
	{
		/* thats really a problem because we cannot throw in this case */
		env->FatalError("could not throw java.io.IOException");
	}

	env->ThrowNew(exception, aiGetErrorString());

	lprintf("problem detected\n");
	}

end:
	/* 
	 * NOTE: this releases all memory used in the native domain.
	 * Ensure all data has been passed to java before!
	 */
	aiReleaseImport(cScene);


	/* free params */
	env->ReleaseStringUTFChars(jFilename, cFilename);

	lprintf("return from native\n");

	return jScene;
}

JNIEXPORT jobject JNICALL Java_org_gearvrf_jassimp2_Jassimp_aiImportAssetFile
  (JNIEnv *env, jclass jClazz, jstring jFilename, jlong postProcess, jobject assetManager)
{
	return importHelper(env, jClazz, jFilename, postProcess, assetManager, NULL);
}

JNIEXPORT jobject JNICALL Java_org_gearvrf_jassimp2_Jassimp_aiImportFile
  (JNIEnv *env, jclass jClazz, jstring jFilename, jlong postProcess)
{
	return importHelper(env, jClazz, jFilename, postProcess, NULL, NULL);
}

JNIEXPORT jobject JNICALL Java_org_gearvrf_jassimp2_Jassimp_aiImportFileEx
  (JNIEnv *env, jclass jClazz, jstring jFilename, jlong postProcess, jobject jFileIO)
{
    return importHelper(env, jClazz, jFilename, postProcess, NULL, jFileIO);
}
