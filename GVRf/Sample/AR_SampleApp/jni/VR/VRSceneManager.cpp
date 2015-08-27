#include "VRShaderUtil.h"
#include "VRSceneManager.h"
#include "VRLogger.h"
#include <SkImageDecoder.h>
#include <SkCompressedImageRef.h>


#define  LOG_TAG    "VRSCENE"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,__VA_ARGS__)

using namespace GVR;

#define NOANIMATION -1
#define SKELETALANIM 1
#define KEYFRMANIM 2


VRSceneRenderer :: VRSceneRenderer(char *assetsPath) {
	if(assetsPath){
		int len = strlen(assetsPath) + 1;
		mAssetsPath = new char[len];
		strncpy(mAssetsPath, assetsPath, len);
		VR_LOGI("VRSceneRenderer() : assetsPath = %s", mAssetsPath);
	} else {
		VR_LOGE("Invalid assets path, assetsPath = %s", assetsPath);
	}
	mCinemaHall = mChairs = mSpaceShip = mSpace = mSphere =0;
	mSceneType = VR_SCENE_MAX;
}

VRSceneRenderer :: ~VRSceneRenderer() {
	VR_LOGI("Deleting the VR Scene Objects");
	for(unsigned int i = 0; i < mScnObjs.size(); i++) {

		VRSceneObj *model = mScnObjs.at(i);
		if(model) {
			delete model;
			model = 0;
		}
	}
	mScnObjs.clear();

	mChairs = mCinemaHall = mSpace = mSpaceShip = mSphere = 0;

	if(mAssetsPath) {
		delete mAssetsPath;
		mAssetsPath = 0;
	}

}



bool VRSceneObj :: loadFromFile(char *filePath, char *fileName) {
	FILE *fin = 0;
	char *fileBuf = NULL, *freeBuf = NULL;
	FileHeader *gloHeader = NULL;

	int l1 = strlen(filePath);
	int l2 = strlen(fileName);
	int total_len = l1 + l2 + 2;
	VR_LOGI("l1 = %d, l2 = %d", l1, l2);
	char fileFullPath[total_len];
	strcpy(fileFullPath, "");
	strncpy(fileFullPath, filePath, l1+1);
	strcat(fileFullPath, fileName);

	VR_LOGI("Going to read the file, filePath = %s, fileName = %s, fileFullPath = %s", filePath, fileName, fileFullPath);
	fin = fopen(fileFullPath, "rb");
	int temp;

	if(fin == NULL) {
		VR_LOGE("fail to load %s\n", fileFullPath);
		return false;
	}
	else
	{
		int bytesRd = 0;
		gloHeader = new FileHeader();

		size_t size_header = sizeof(FileHeader);

		bytesRd = fread(gloHeader, size_header, 1, fin);

		if(bytesRd ==0) {
			VR_LOGE("VRSceneRenderer::loadSceneObject() : Failed to read Header data ");
			delete gloHeader;
			gloHeader = 0;
			fclose(fin);
			fin = 0;
			return false;
		} else {
			// Validate the gloHeader
			/*if(gloHeader->numEfxNode > 3000) {
				VR_LOGE("gloHeader->numEfxNode > 3000 so clamping it to 3000");
				gloHeader->numEfxNode = 3000;
			}
			if(gloHeader->numTexture > 100) {
				VR_LOGE("gloHeader->numTexture > 100 so clamping it to 100");
				gloHeader->numTexture = 100;
			}
			if(gloHeader->numGeometry > 50000) {
				VR_LOGE("gloHeader->numGeometry > 50000 so clamping it to 50000");
				gloHeader->numGeometry  = 50000;
			}
			if(gloHeader->numAnim > 10000) {
				VR_LOGE("gloHeader->numAnim > 10000 so clamping it to 10000");
				gloHeader->numAnim = 10000;
			}
			if(gloHeader->numMesh > 50000) {
				VR_LOGE("gloHeader->numMesh > 50000 so clamping it to 50000");
				gloHeader->numMesh = 50000;
			}
			if(gloHeader->maxBoneReq > 1000) {
				VR_LOGE("gloHeader->maxBoneReq > 1000 so clamping it to 1000");
				gloHeader->maxBoneReq = 1000;
			}
			if(gloHeader->mMaxAnimFrame > 50000) {
				VR_LOGE("gloHeader->mMaxAnimFrame > 50000 so clamping it to 50000");
				gloHeader->mMaxAnimFrame = 50000;
			}
			if(gloHeader->fileSize == 0) {
				VR_LOGE("gloHeader->fileSize = 0 so clamping it to 3000");
				return false;
			}*/
		}

		fileBuf = new char[gloHeader->fileSize];		   
		freeBuf = fileBuf;


		bytesRd = fread(fileBuf, sizeof(char)*gloHeader->fileSize, 1, fin);

		// read geometry header
		GeometryHeader *geomHead = new GeometryHeader[gloHeader->numGeometry];
		memcpy(geomHead, fileBuf, sizeof(GeometryHeader)*gloHeader->numGeometry);
		fileBuf += sizeof(GeometryHeader)*gloHeader->numGeometry;

		//read all mesh header
		MeshHeader *meshHead = new MeshHeader[gloHeader->numMesh]; 
		memcpy(meshHead, fileBuf, sizeof(MeshHeader)*gloHeader->numMesh);
		fileBuf += sizeof(MeshHeader)*gloHeader->numMesh;

		mMaxAnimFrame = gloHeader->mMaxAnimFrame;

		if(gloHeader->numTexture > 100) {
			gloHeader->numTexture = 0;
			VR_LOGE("Number of textures are greater than 100, reduce scene textures use texture atlas");
		}

		for(int i1 = 0; i1 < gloHeader->numTexture; i1++)
		{
			ImageTex *imgObj = new ImageTex();
			char imageFileName[32];
			memcpy(imageFileName, fileBuf, sizeof(char)*32);
			strcpy(imgObj->fileName, filePath);
			strcat(imgObj->fileName, imageFileName);
			fileBuf += sizeof(char)*32;
			mImageList.push_back(imgObj);
		}

		mGeometrySize = gloHeader->numGeometry;

		if(gloHeader->maxBoneReq != 0)
		{
			mHasBones = 1;// replace with scene max bone
		}
		else
		{
			mHasBones = 0;// replace with scene max bone
		}


		if(mHasBones)
			mBoneAnimMat =  new mat4*[gloHeader->numGeometry]; //new float*[gloHeader->numGeometry]; 

		int meshIndx = 0;

		for(int i = 0; i < gloHeader->numGeometry; i++)
		{
			int meshSize;
			Geometry *geomObj;
			geomObj = new Geometry();
			geomObj->mCntIndx = geomHead[i].contrIndx;

			geomObj->mNumBone = geomHead[i].mNumBone;

			if(geomObj->mNumBone > 1000) {
				geomObj->mNumBone = 1000;
				VR_LOGE("geomObj->mNumBone > 1000 so clamping it to 1000");
			}
			
			if(geomObj->mNumBone != 0)
			{
				//mBoneAnimMat[geomObj->mCntIndx] = new float[geomObj->mNumBone * 16] ;
				mBoneAnimMat[geomObj->mCntIndx] = new mat4[geomObj->mNumBone] ;
			}

			//read bounding mBBox 
			BoundingBox *mBBox = new BoundingBox();
			float tmpbox[24];

			memcpy(tmpbox, fileBuf, sizeof(float)*24);

			fileBuf += sizeof(float)*24;
			geomObj->mBBox = mBBox;
			geomObj->mBBox->v1[0] = tmpbox[0];
			geomObj->mBBox->v1[1] = tmpbox[1];
			geomObj->mBBox->v1[2] = tmpbox[2];
			geomObj->mBBox->v2[0] = tmpbox[3];
			geomObj->mBBox->v2[1] = tmpbox[4];
			geomObj->mBBox->v2[2] = tmpbox[5];
			geomObj->mBBox->v3[0] = tmpbox[6];
			geomObj->mBBox->v3[1] = tmpbox[7];
			geomObj->mBBox->v3[2] = tmpbox[8];
			geomObj->mBBox->v4[0] = tmpbox[9];
			geomObj->mBBox->v4[1] = tmpbox[10];
			geomObj->mBBox->v4[2] = tmpbox[11];
			geomObj->mBBox->v5[0] = tmpbox[12];
			geomObj->mBBox->v5[1] = tmpbox[13];
			geomObj->mBBox->v5[2] = tmpbox[14];
			geomObj->mBBox->v6[0] = tmpbox[15];
			geomObj->mBBox->v6[1] = tmpbox[16];
			geomObj->mBBox->v6[2] = tmpbox[17];
			geomObj->mBBox->v7[0] = tmpbox[18];
			geomObj->mBBox->v7[1] = tmpbox[19];
			geomObj->mBBox->v7[2] = tmpbox[20];
			geomObj->mBBox->v8[0] = tmpbox[21];
			geomObj->mBBox->v8[1] = tmpbox[22];
			geomObj->mBBox->v8[2] = tmpbox[23];

			for(int j = 0; j < geomHead[i].numMesh; j++)
			{
				Mesh *meshObj;
				meshObj = new Mesh();
				float *vert = NULL, *norm = NULL, *mUV = NULL;
				meshObj->mImgObjIndex = meshHead[meshIndx].mImgObjListIndex;

				if(meshHead[meshIndx].numVertex != 0)
				{
					vert = new float[meshHead[meshIndx].numVertex]; //(float*)malloc(sizeof(float)*meshHead[meshIndx].numVertex);
					memcpy(vert, fileBuf, sizeof(float)*meshHead[meshIndx].numVertex);
					fileBuf += sizeof(float)*meshHead[meshIndx].numVertex;
					meshObj->mVertex = vert;
					meshObj->mHasVert = 1;
					meshObj->mTriangleCount = meshHead[meshIndx].numVertex/9;
					glGenBuffers(1, &(meshObj->mPosVBO));
					glBindBuffer(GL_ARRAY_BUFFER, meshObj->mPosVBO);
					glBufferData(GL_ARRAY_BUFFER, sizeof(float)*meshObj->mTriangleCount*9, meshObj->mVertex, GL_STATIC_DRAW);
					glBindBuffer(GL_ARRAY_BUFFER, 0);

				}

				if(meshHead[meshIndx].numNorm != 0)
				{
					norm = new float[meshHead[meshIndx].numNorm]; //(float*)malloc(sizeof(float)*meshHead[meshIndx].numNorm);
					memcpy(norm, fileBuf, sizeof(float)*meshHead[meshIndx].numNorm);
					fileBuf += sizeof(float)*meshHead[meshIndx].numNorm;
					meshObj->mNormal = norm;
					meshObj->mHasNorm = 1;
				}

				if(meshHead[meshIndx].numUV != 0)
				{
					mUV =  new float[meshHead[meshIndx].numUV]; //(float*)malloc(sizeof(float)*meshHead[meshIndx].numUV);
					memcpy(mUV, fileBuf, sizeof(float)*meshHead[meshIndx].numUV);
					fileBuf += sizeof(float)*meshHead[meshIndx].numUV;
					meshObj->mUV = mUV;
					meshObj->mHasUV = 1;
					glGenBuffers(1, &(meshObj->mTexCordVBO));
					glBindBuffer(GL_ARRAY_BUFFER, meshObj->mTexCordVBO);
					glBufferData(GL_ARRAY_BUFFER, sizeof(float)*meshObj->mTriangleCount*6, meshObj->mUV, GL_STATIC_DRAW);
					glBindBuffer(GL_ARRAY_BUFFER, 0);


				}

				if(meshHead[meshIndx].numBoneIdWt != 0)
				{
					meshObj->mHasBone = 1;
				}
				else
				{
					meshObj->mHasBone = 0;
				}

				if(meshObj->mHasBone)
				{
					meshObj->mHasBone = 1;
					meshObj->mBoneIdWt = new float[meshHead[meshIndx].numBoneIdWt];
					memcpy(meshObj->mBoneIdWt, fileBuf, sizeof(float)*meshHead[meshIndx].numBoneIdWt);
					fileBuf += sizeof(float)*24*meshObj->mTriangleCount;
					glGenBuffers(1, &(meshObj->mBoneIdWtHandle));
					glBindBuffer(GL_ARRAY_BUFFER, meshObj->mBoneIdWtHandle);
					glBufferData(GL_ARRAY_BUFFER, sizeof(float)*meshObj->mTriangleCount*24, meshObj->mBoneIdWt, GL_STATIC_DRAW);
					glBindBuffer(GL_ARRAY_BUFFER, 0);


				}

				geomObj->mMeshList.push_back(meshObj);
				meshIndx++;
			}
			mGeometryList.push_back(geomObj);
		}

		delete[] meshHead;
		delete[] geomHead;

	}


	mEfxNodeSize = gloHeader->numEfxNode;
	//read tree structure
	for(int m = 0; m < gloHeader->numEfxNode; m++)
	{
		EFXNodebin *efxNode = new EFXNodebin(); 
		memcpy(efxNode, fileBuf, sizeof(EFXNodebin));
		fileBuf += sizeof(EFXNodebin);
		if(efxNode->mNumChild != 0)
		{
			efxNode->mChildList = new int[efxNode->mNumChild] ; 
			memcpy(efxNode->mChildList, fileBuf, sizeof(int)*efxNode->mNumChild);
			fileBuf += sizeof(int)*efxNode->mNumChild;
		}
		if(efxNode->mIsJoinNode)
		{
			efxNode->mJoinGeomIndx = new int[efxNode->mNumEffCntr * 2] ; 
			memcpy(efxNode->mJoinGeomIndx, fileBuf, sizeof(int)*efxNode->mNumEffCntr*2);
			fileBuf += sizeof(int)*efxNode->mNumEffCntr*2;

			efxNode->mBoneMat = new mat4*[efxNode->mNumEffCntr]; 
			memcpy(*efxNode->mBoneMat, fileBuf, sizeof(float)*efxNode->mNumEffCntr*16);
			fileBuf += sizeof(float)*efxNode->mNumEffCntr*16;

			efxNode->mBindShapeMat = new mat4*[efxNode->mNumEffCntr]; 
			memcpy(*efxNode->mBindShapeMat, fileBuf, sizeof(float)*efxNode->mNumEffCntr*16);
			fileBuf += sizeof(float)*efxNode->mNumEffCntr*16;
		}

		mEfxNodeList.push_back(efxNode);
	}


	mRootNode = mEfxNodeList.at(0);
	mMaxBoneSize = gloHeader->maxBoneReq;


	if(gloHeader->animType == SKELETALANIM)
	{
		mAnimSize = gloHeader->numAnim;
		for(int anim = 0; anim < gloHeader->numAnim; anim++)
		{
			BoneMatAnim *boneAnim = new BoneMatAnim();
			memcpy(boneAnim, fileBuf, sizeof(BoneMatAnim));
			fileBuf += sizeof(BoneMatAnim);
			boneAnim->mTimekey =  new float[boneAnim->mCount]; 
			boneAnim->mAnimMat =  new mat4[boneAnim->mCount]; //new float[boneAnim->mCount * 16]; 
			memcpy(boneAnim->mTimekey, fileBuf, sizeof(float)*boneAnim->mCount);
			fileBuf += sizeof(float)*boneAnim->mCount;
			memcpy((float*)boneAnim->mAnimMat[0].Pointer(), fileBuf, sizeof(float)*boneAnim->mCount*16);
			fileBuf += sizeof(float)*boneAnim->mCount*16;
			mBoneMatData.push_back(boneAnim);
		}
	}
	else if(gloHeader->animType == KEYFRMANIM)
	{
		mAnimSize = gloHeader->numAnim;

		for(int anim = 0; anim < mAnimSize; anim++)
		{
			AnimationBin *animObj = new AnimationBin(); 

			memcpy(animObj, fileBuf, sizeof(AnimationBin));
			fileBuf += sizeof(AnimationBin);
			animObj->mKeyTrans = new float[animObj->mCount*16]; 
			memcpy(animObj->mKeyTrans, fileBuf, sizeof(float)*animObj->mCount*16);
			fileBuf += sizeof(float)*animObj->mCount*16;
			mAnimBinList.push_back(animObj);
		}
	}
	else if(gloHeader->animType == NOANIMATION)
	{
		//cout<<"no animation found"<<endl;
	}
	fclose(fin);
	delete[] freeBuf;
	delete gloHeader; 



	// Load textures using skia
	SkImageDecoder::Mode mode = SkImageDecoder::kDecodePixels_Mode;
	SkBitmap::Config prefConfig = SkBitmap::kARGB_8888_Config;

	for(unsigned int i = 0; i < mImageList.size();i++) {
		ImageTex *imgObj = mImageList.itemAt(i);

		SkBitmap* bm = new SkBitmap();
		if(NULL == bm) {
			VR_LOGE("Skia bitmap allocation failed for scene textures\n");
		}

		if(SkImageDecoder::DecodeFile(imgObj->fileName, bm, prefConfig, mode, NULL))
		{

			GLuint texId;
			glGenTextures(1, &texId);
			glBindTexture(GL_TEXTURE_2D, texId);

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

			/* load texture */

#ifdef USE_COMPRESSED_TEXTURE	
			SkCompressedImageRef *ref = static_cast<SkCompressedImageRef *>(bm->pixelRef());
			glCompressedTexImage2D(GL_TEXTURE_2D, 0,/*GL_COMPRESSED_RGB8_ETC2*/0x9274, ref->getWidth(), ref->getHeight(), 0, ref->getPixelsByteSize(), ref->pixels());

#else
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bm->width(), bm->height(), 0, GL_RGBA, GL_UNSIGNED_BYTE, (unsigned char *)bm->getPixels());
#endif
			/*load compressed texture */


			imgObj->mTexId = texId;

		}else {
			VR_LOGE("Loading the scene texture file [%s] : failed\n", imgObj->fileName);
		}

		if(bm){
			delete bm;
			bm = 0;
		}

	}

	for(unsigned int i = 0; i < mGeometryList.size(); i++)
	{
		Geometry *geom = mGeometryList.itemAt(i);
		for(unsigned int j = 0; j < geom->mMeshList.size(); j++)
		{
			Mesh *mesh = geom->mMeshList.itemAt(j);
			ImageTex *imgObj = mImageList.itemAt(mesh->mImgObjIndex);
			mesh->mTexId= imgObj->mTexId;
			imgObj = 0;

		}
		geom = 0;
	}

	

	return true;

}

bool VRSceneObj :: setShader()
{

	if(mShaderPgrmId == -1){	 
		/*if(mHasBones)
		{

			mShaderPgrmId = new ghProgram(gSceneBoneAnimVShader, gSceneBoneAnimFShader);
		}
		else*/
		{
		//	mShaderPgrmId = new ghProgram(gSceneVShader, gSceneFShader);
			mShaderPgrmId = VRShaderUtil::instance().createProgram(gSceneVShader, gSceneFShader);
			VR_LOGI("Creating shader for rendering scene environment.. mShaderPgrmId = %d", mShaderPgrmId);
		}
	}

	if (!mShaderPgrmId) {
		VR_LOGE("VRSceneRenderer :: Could not create program.");
		return false;
	}
	int programId = mShaderPgrmId;

	glUseProgram(programId);

	if(mHasBones)
	{

		mBoneIdAttribLoc = glGetAttribLocation(programId, "boneId");
		mBoneWtAttribLoc = glGetAttribLocation(programId, "boneWt");
		mFinalMatLoc = glGetUniformLocation(programId, "finalMat");
	}

	mPosAttribLoc = glGetAttribLocation(programId, "pos");
	mTexCordAttribLoc = glGetAttribLocation(programId, "a_TexCoordinate");
	mMVPMatrixHandle = glGetUniformLocation(programId, "u_MVPMatrix");
	return true;
}

void VRSceneRenderer :: addToRenderer(VRSceneObj *scn) {
	mScnObjs.push_back(scn);
}

void VRSceneRenderer :: unloadModel(VRSceneObj *scn) {
	//
}

void VRSceneRenderer :: renderScene(float fElapsedTime, VRCamera *camRef) {

	VR_LOGI("VRSCenerenderer::renderScene this = %p, camRef = %p, no of scenes to render = %d", this, camRef, mScnObjs.size());

	mCamRef = camRef;

	for(unsigned int i = 0; i < mScnObjs.size(); i++)
	{
		VRSceneObj *scn = mScnObjs.at(i);

		scn->setShader();

		if(scn->mMaxBoneSize == 0)
		{
			mat4 parentMat;

			traverseNodeTree(scn->mRootNode, scn, parentMat);

		}
		else
		{
			//computeFinalMat(mAnimIndx, scn);
			drawSkeletal(scn);
		}

	}
}

void VRSceneRenderer :: computeFinalMat(int curIndx, VRSceneObj *scn)
{
    if(scn->mRootNode != NULL)
    {
        /*float parentMat[16];
        float parentAnim[16];
        setIdentity(parentAnim);
        setIdentity(parentMat);*/
        mat4 parentMat, parentAnim;
        mAnimIndex = curIndx;
        recurFinalMat(scn->mRootNode, parentMat, scn);
    }
}

void VRSceneRenderer :: recurFinalMat(EFXNodebin *efxNode, mat4 &parentMat, VRSceneObj *scn)
{
  /*  float globMat[16];
    float nodeMat[16];

    setIdentity(globMat);
    setIdentity(nodeMat);*/

    mat4 globMat, nodeMat;

    if(efxNode->mAnimIndx != -1 &&  efxNode->mAnimIndx < scn->mBoneMatData.size())
    {
        BoneMatAnim *boneAnim = scn->mBoneMatData.at(efxNode->mAnimIndx);
        if(boneAnim != NULL)
        	getCurAnim(boneAnim, mAnimIndex, nodeMat);
    }
    else
    {
        memcpy((float*)nodeMat.Pointer(), (float*)efxNode->mTransMat.Pointer(), sizeof(float)*16);
    }

    //matMul(globMat, nodeMat, parentMat);
    globMat = nodeMat * parentMat;


    if(efxNode->mIsJoinNode)
    {
       // float tmpMat[16];
       //float *boneMat = efxNode->mBoneMat;
       // float *bindShapeMat = efxNode->mBindShapeMat;

        mat4 tmpMat;
        mat4 boneMat = *efxNode->mBoneMat[0];
        mat4 bindShapeMat = *efxNode->mBindShapeMat[0];

        for(int p = 0; p < efxNode->mNumEffCntr * 2; p)
        {
            int geomIndx = efxNode->mJoinGeomIndx[p];
            int boneIndx = efxNode->mJoinGeomIndx[p + 1];
           /* float *tmp = scn->mBoneAnimMat[geomIndx];           
            tmp = tmp + 16 * boneIndx;
            matMul(tmpMat, boneMat, globMat);
            matMul(tmp, bindShapeMat, tmpMat);
            boneMat += 16;
            bindShapeMat += 16;*/
	     tmpMat = boneMat * globMat;
	     scn->mBoneAnimMat[geomIndx][boneIndx] = bindShapeMat  * tmpMat;
            p += 2;
        }
    }

    for(int i = 0; i < efxNode->mNumChild; i++)
    {
        EFXNodebin *efxChildNode = scn->mEfxNodeList.at(efxNode->mChildList[i]);
        recurFinalMat(efxChildNode, globMat, scn);
    }
}

void VRSceneRenderer :: getCurAnim(BoneMatAnim *boneAnim, int curIndx, mat4 &curAnimMat)
{

    //compute animation without any interpolation
    int useIndx = -1;
    if(curIndx < boneAnim->mSTIndx)
        useIndx = 0;
    else if (curIndx > boneAnim->mEndIndx)
        useIndx = boneAnim->mCount - 1;
    else
        useIndx = curIndx - boneAnim->mSTIndx;

    if(useIndx < boneAnim->mCount)
    {
        /*float *tmp;
        tmp = boneAnim->animMat;
        tmp = tmp + 16*useIndx;*/

	 curAnimMat = boneAnim->mAnimMat[useIndx];

    }
    else
    {
        //LOGI("FATAL ERROR: animation index going out of bound\n");
        curAnimMat = mat4::Identity(); //setIdentity(curAnimMat);
        
    }
}


void VRSceneRenderer :: traverseNodeTree(EFXNodebin *efxNode, VRSceneObj *scn, mat4 &parentMat)
{
	mat4 globMat;
	mat4 nodeMat;
	int mAnimIndx = 0;

	if(efxNode->mAnimIndx != -1 && scn->mMaxBoneSize == 0)
	{
		int useIndx = 0;
		AnimationBin *animObj = scn->mAnimBinList.at(efxNode->mAnimIndx);
		int testindx = mAnimIndx;//123;
		if(testindx < animObj->mSTIndx)
			useIndx = 0;
		else if(testindx > animObj->mEndIndx)
			useIndx = animObj->mCount - 1;
		else
			useIndx = testindx - animObj->mSTIndx;

		if(useIndx < animObj->mCount)
		{
			float *tmp;
			tmp =  animObj->mKeyTrans;
			tmp = tmp + 16*useIndx;
			memcpy((void*)nodeMat.Pointer(), tmp, sizeof(float)*16);
		}
	}
	else
	{
		memcpy((float*)nodeMat.Pointer(), efxNode->mTransMat.Pointer(), sizeof(float)*16);

	}

	globMat = parentMat * nodeMat;

	if(efxNode->mGeomIndx != -1)
	{
		Geometry *geomObj = scn->mGeometryList.at(efxNode->mGeomIndx);
		drawGeometry(globMat, efxNode->mGeomIndx, scn);
	}


	for(int i = 0; i < efxNode->mNumChild; i++)
	{
		EFXNodebin *efxChildNode = scn->mEfxNodeList.at(efxNode->mChildList[i]);
		traverseNodeTree(efxChildNode, scn, globMat);
	}


}

void VRSceneRenderer :: drawSkeletal(VRSceneObj *scn)
{

	mat4 mat;

	for(unsigned int i = 0; i < scn->mGeometryList.size(); i++)
	{
		drawGeometry(mat, i, scn);
	}
}

void VRSceneRenderer :: drawGeometry(mat4 &mat, int mGeomIndx , VRSceneObj *scn)
{
	mat4 resMat;
	Geometry *geom = scn->mGeometryList.at(mGeomIndx);

	resMat =   scn->mModelMat * mCamRef->getViewMat() * mCamRef->getProjMat();


	glEnable(GL_BLEND);
	glEnable(GL_DEPTH_TEST);

	glDepthFunc(GL_LESS); // GL_LEQUAL
	glEnable(GL_CULL_FACE); // glDisable(GL_CULLFACE)

	int texLoc = glGetUniformLocation(scn->mShaderPgrmId, "u_Texture");

	glUniformMatrix4fv(scn->mMVPMatrixHandle, 1, false, resMat.Pointer());


	for(unsigned int k = 0; k < geom->mMeshList.size(); k++)
	{
		Mesh *mesh = geom->mMeshList.itemAt(k);

	
		//vbo draw
		if(mesh->mHasVert )
		{

			 glBindTexture(GL_TEXTURE_2D, mesh->mTexId);
			//glBindTexture(GL_TEXTURE_2D, scn->mImageList.itemAt(0)->mTexId);
			glActiveTexture(GL_TEXTURE0);
			glUniform1i(texLoc, 0);


			glBindBuffer(GL_ARRAY_BUFFER, mesh->mPosVBO);
			glVertexAttribPointer(scn->mPosAttribLoc, 3, GL_FLOAT, GL_FALSE, 0, 0); //mesh->mVertex
			glEnableVertexAttribArray(scn->mPosAttribLoc);

			glBindBuffer(GL_ARRAY_BUFFER, mesh->mTexCordVBO);
			glVertexAttribPointer(scn->mTexCordAttribLoc, 2, GL_FLOAT, GL_FALSE, 0,  0); //mesh->mUV 
			glEnableVertexAttribArray(scn->mTexCordAttribLoc);

			if(0 /*scn->mHasBones*/)
			{
				mat4* boneAnimMatArray = scn->mBoneAnimMat[geom->mCntIndx];
				glUniformMatrix4fv(scn->mFinalMatLoc, geom->mNumBone, GL_FALSE, (float*)boneAnimMatArray[0].Pointer());
				glEnableVertexAttribArray(scn->mBoneIdAttribLoc);
				glBindBuffer(GL_ARRAY_BUFFER, mesh->mBoneIdWtHandle);
				glVertexAttribPointer(scn->mBoneIdAttribLoc, 4, GL_FLOAT, GL_FALSE, sizeof(float)*8, 0);
				glEnableVertexAttribArray(scn->mBoneWtAttribLoc);
				glBindBuffer(GL_ARRAY_BUFFER,mesh->mBoneIdWtHandle);
				glVertexAttribPointer(scn->mBoneWtAttribLoc, 4, GL_FLOAT, GL_FALSE, sizeof(float)*8, (GLvoid*)(sizeof(float)*4));
			}

			glDrawArrays(GL_TRIANGLES, 0, mesh->mTriangleCount*3);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glDisableVertexAttribArray(scn->mTexCordAttribLoc);
			glDisableVertexAttribArray(scn->mPosAttribLoc);

		}
	}
	glDisable(GL_DEPTH_TEST);
	glDisable(GL_BLEND);
}

void VRSceneRenderer :: init(VRSceneType type)
{
	bool isSceneLoaded = false;	
	mat4 modelMat;

	if(type == VR_SCENE_CINEMAHALL) {

		//modelMat = mat4::RotateY(-90) *  mat4::RotateZ(-90);
		modelMat = mat4::RotateY(-90);

		if(mCinemaHall == 0)
			mCinemaHall = new VRSceneObj(modelMat);

		if(mChairs == 0)
			mChairs = new VRSceneObj(modelMat);

		char fileName[100];
	#ifdef USE_COMPRESSED_TEXTURE		
		strcpy(fileName, "cinema_pkm.bin");
		isSceneLoaded =mCinemaHall->loadFromFile(mAssetsPath, fileName);
	#else 
	       strcpy(fileName, "cinema.bin");
		isSceneLoaded = mCinemaHall->loadFromFile(mAssetsPath, fileName); //cinema.bin //Space.bin
	#endif 
		if(isSceneLoaded) {
			this->addToRenderer(mCinemaHall);
		} else {
			VR_LOGE("VR Error :: VRSceneRenderer::init() , Couldn't load the cinema [%p] scene !!\n", (void*)mCinemaHall);
		}
/*		
	#ifdef USE_COMPRESSED_TEXTURE		
		isSceneLoaded = mChairs->loadFromFile("/data/log/vr/chair_pkm.bin");
	#else 
		isSceneLoaded = mChairs->loadFromFile("/data/log/vr/chair.bin"); // chair.bin //SpaceShip.bin
	#endif 
		if(isSceneLoaded) {
			this->addToRenderer(mChairs);
		} else {

		VR_LOGE("VR Error :: VRSceneRenderer::int() , Couldn't load the chair [%x] scene !!\n", mChairs);

		}*/

	} else if(type  == VR_SCENE_SPACESHIP) {

		//modelMat = mat4::RotateY(180) * mat4::RotateZ(-90);
		modelMat = mat4::RotateY(180) ;

		if(mSpace == 0)
			mSpace = new VRSceneObj(modelMat);
		if(mSpaceShip == 0)
			mSpaceShip = new VRSceneObj(modelMat);

		char fileName[100];
		
		strcpy(fileName, "Space.bin");
		isSceneLoaded = mSpace->loadFromFile(mAssetsPath, fileName); //cinema.bin //Space.bin

		if(isSceneLoaded) {
			this->addToRenderer(mSpace);
		} else {
			VR_LOGE("VR Error :: VRSceneRenderer::init() , Couldn't load the space [%p] scene !!\n", (void*)mSpace);
		}

		strcpy(fileName, "SpaceShip.bin");
		isSceneLoaded = mSpaceShip->loadFromFile(mAssetsPath, fileName); // chair.bin //SpaceShip.bin

		if(isSceneLoaded) {
			this->addToRenderer(mSpaceShip);
		} else {
			VR_LOGE("VR Error :: VRSceneRenderer::int() , Couldn't load the spaceship [%p] scene !!\n", (void*)mSpaceShip);
		}	
	
	}else if(type  == VR_SCENE_SPHERE) {

		//modelMat = mat4::RotateY(180) * mat4::RotateZ(-90);
		modelMat = mat4::RotateY(180) ;

		if(mSphere == 0)
			mSphere = new VRSceneObj(modelMat);

		char fileName[100];
		
		strcpy(fileName, "sphere.bin");
		isSceneLoaded = mSphere->loadFromFile(mAssetsPath, fileName); //cinema.bin //Space.bin

		if(isSceneLoaded) {
			this->addToRenderer(mSphere);
		} else {
			VR_LOGE("VR Error :: VRSceneRenderer::init() , Couldn't load the space [%p] scene !!\n", (void*)mSphere);
		}
	
	}
}


