#ifndef _VRSceneManager_h
#define _VRSceneManager_h

#include <vector>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "kSensor/math/matrix.hpp"
#include "VRCamera.h"
#include "VRShaderUtil.h"

namespace GVR {

	enum VRSceneType {
		VR_SCENE_CINEMAHALL,
		VR_SCENE_SPACESHIP,
		VR_SCENE_SPHERE,
		VR_SCENE_MAX,
	};
	
	typedef struct
	{
		int numEfxNode;
		int numTexture;
		int numGeometry;
		int numAnim;
		int numMesh;
		int maxBoneReq;
		int animType;
		int mMaxAnimFrame;
		long int fileSize;
	}FileHeader;

	typedef struct
	{
		int numMesh;
		int contrIndx;
		int mNumBone;
	}GeometryHeader;


	typedef struct
	{
		int mImgObjListIndex;
		int numVertex;
		int numNorm;
		int numUV;
		int numBoneIdWt;
	}MeshHeader;

	typedef struct
	{
		float v1[3];
		float v2[3];
		float v3[3];
		float v4[3];
		float v5[3];
		float v6[3];
		float v7[3];
		float v8[3];
	}BoundingBox;


	class BoneMatAnim
	{

	public:

		BoneMatAnim() {
			mAnimMat = 0;
			mTimekey = 0;
		}
		~BoneMatAnim() {
			
			if(mTimekey != 0) {
				delete[] mTimekey;
				mTimekey = 0;
			}

			if(mAnimMat) {
				delete[] mAnimMat;
				mAnimMat = 0;
			}
		}

		int mSTIndx;
		int mEndIndx;
		int mCount;
		mat4* mAnimMat;
		float *mTimekey;
	};


	class Mesh
	{
	public:
		int mHasVert ;
		float *mVertex;
		int mHasNorm;
		float *mNormal;
		int mHasUV;
		float *mUV;
		int mHasBone;
		float *mBoneIdWt;
		int mTriangleCount;
		int mImgObjIndex;
		int mTexId;
		GLuint mPosVBO;
		GLuint mTexCordVBO;
		GLuint mBoneIdWtHandle;

		Mesh()
		{
			mHasVert = 0;
			mHasNorm = 0;
			mHasUV = 0;
			mImgObjIndex = -1;

			mVertex = 0;
			mNormal = 0;
			mUV = 0;
			mBoneIdWt = 0;

			mPosVBO = 0;
			mTexCordVBO = 0;

		}

		~Mesh() {


			if(mVertex) {
				delete mVertex;
				mVertex = 0;
			}
			if(mNormal) {
				//delete mNormal;
				//mNormal = 0;
			}
			if(mUV) {
				delete mUV;
				mUV = 0;
			}
			if(mBoneIdWt) {
				delete mBoneIdWt;
				mBoneIdWt = 0;
			}

			if(mPosVBO) {
				glDeleteBuffers(1, &mPosVBO);
				mPosVBO = 0;
			}

			if(mTexCordVBO) {
				glDeleteBuffers(1, &mTexCordVBO);
				mTexCordVBO = 0;
			}

		}
	};


	class EFXNodebin
	{
	public:

		EFXNodebin() {
			mBindShapeMat = 0; 
			mJoinGeomIndx = 0;
			mBoneMat = 0; 
			mChildList = 0;
		}

		~EFXNodebin() {

			if(mIsJoinNode) {
				if(mJoinGeomIndx != 0) {
					delete[] mJoinGeomIndx;
					mJoinGeomIndx = 0;
				}

				if(mBoneMat) {
					for(int i =0; i < mNumEffCntr; i++) {
						if(mBoneMat[i]) {
							delete mBoneMat[i];
						}
					}
					mBoneMat = 0;
				}

				if(mBindShapeMat) {
					for(int i =0; i < mNumEffCntr; i++) {
						if(mBindShapeMat[i]) {
							delete mBindShapeMat[i];
						}
					}
					mBindShapeMat = 0;
				}
			}

			if(mNumChild > 0) {
				if(mChildList != 0) {
					delete[] mChildList;
					mChildList = 0;
				}
			}
		}

		int mIndex;
		int mParentIndx;
		int mAnimIndx;
		int mIsJoinNode;
		int mBoneIndx;
		int mCntIndx;
		int mNumBone;
		int mNumEffCntr;
		int mNumChild;
		int mGeomIndx;
		mat4 mTransMat;
		mat4 **mBindShapeMat; 
		int *mJoinGeomIndx;
		mat4 **mBoneMat; 
		int *mChildList;
	};

	class Geometry
	{
	public:

		std::vector<Mesh*> mMeshList;
		int mCntIndx;
		int mNumBone;
		BoundingBox* mBBox;
		GVR::mat4 mModelMat;
		int mCollider;
		Geometry() {
			mCollider = 0;
		}

		~Geometry() {

			for(unsigned int i = 0; i < mMeshList.size(); i++)
			{
				Mesh *mesh = mMeshList.at(i);
				if(mesh != 0) {
					delete mesh;
					mesh = 0;
				}
			}
		}

	};


	class ImageTex
	{
	public:
		ImageTex() {

		}
		~ImageTex() {

			glDeleteTextures(1, &mTexId);

		}
		char fileName[100];
		unsigned int mTexId;
	};

	class AnimationBin
	{

	public:
		AnimationBin() {
			mKeyTrans = 0;
		}

		~AnimationBin() {


			if(mKeyTrans) {
				delete mKeyTrans;
				mKeyTrans = 0;
			}
		}

		int mSTIndx;
		int mEndIndx;
		int mCount;
		float *mKeyTrans;


	};


	class VRSceneObj
	{
	public:

		VRSceneObj(mat4 &modelMat) {
			mBoneAnimMat = 0;
			mRootNode = 0;
			mMaxAnimFrame = 0;
			mShaderPgrmId = -1;
			mModelMat = modelMat;
		}

		~VRSceneObj() {
		
			for(unsigned int i = 0; i < mGeometryList.size(); i++) {
				Geometry *geom = mGeometryList.at(i);
				if(geom != 0) {
					delete geom;
					geom = 0;
				}
			}

			for(unsigned int i = 0; i < mImageList.size(); i++) {
				ImageTex *img = mImageList.at(i);
				if(img != 0) {
					delete img;
					img = 0;
				}
			}

			for(unsigned int i = 0; i < mAnimBinList.size(); i++) {
				AnimationBin *anim = mAnimBinList.at(i);
				if(anim != 0) {
					delete anim;
					anim = 0;
				}
			}
			for(unsigned int i = 0; i < mBoneMatData.size(); i++) {
				BoneMatAnim *mBoneMat = mBoneMatData.at(i);
				if(mBoneMat != 0) {
					delete mBoneMat;
					mBoneMat = 0;
				}
			}

			if(mBoneAnimMat) {
				delete[] mBoneAnimMat;
				mBoneAnimMat = 0;
			}

			/*if(mRootNode != 0) {
				delete mRootNode;
				mRootNode = 0;
			}*/
			for(unsigned int i = 0; i < mEfxNodeList.size(); i++) {
				EFXNodebin *node = mEfxNodeList.at(i);
				if(node != 0) {
					delete node;
					node = 0;
				}
			}

			if(mShaderPgrmId) {
				VRShaderUtil::instance().deleteProgram(mShaderPgrmId);
				mShaderPgrmId = -1;
			}
		}
		bool setShader();
		bool loadFromFile(char *filePath, char *fileName);

	public:
		GLuint mVertexShader, mFragmentShader, mMVPMatrixHandle;
		int mShaderPgrmId;
		
		GLuint mTexCordAttribLoc, mPosAttribLoc;
		GLuint mFinalMatLoc, mBoneIdAttribLoc, mBoneWtAttribLoc;
		GLuint mWorldHandle;
		GLuint mAnimHandle;
		
		int mGeometrySize;
		int mEfxNodeSize;
		int mAnimSize;
		int mMaxAnimFrame;
		int mMaxBoneSize;
		int mHasBones;
		float mBindShapeMat[16];

		std::vector <Geometry*> mGeometryList;
		std::vector <ImageTex*> mImageList;
		std::vector <EFXNodebin*> mEfxNodeList;
		std::vector <AnimationBin*> mAnimBinList;
		std::vector <BoneMatAnim*> mBoneMatData;

		mat4** mBoneAnimMat;
		EFXNodebin *mRootNode;
		GVR::mat4 mModelMat;
	};

	class VRSceneRenderer
	{
	public:
		VRSceneRenderer(char *assetsPath);
		~VRSceneRenderer();

		void init(VRSceneType type);
		void addToRenderer(VRSceneObj *scn);
		void unloadModel(VRSceneObj *scn);	
		void renderScene(float fElapsedTime, VRCamera *cam);

	private:
		void getCurAnim(BoneMatAnim *boneAnim, int curIndx, mat4 &curAnimMat);
		void recurFinalMat(EFXNodebin *efxNode, mat4 &parentMat, VRSceneObj *scn);
		void computeFinalMat(int curIndx, VRSceneObj *scn);
		void drawGeometry(mat4 &mat, int mGeomIndx , VRSceneObj *scn);
		void drawSkeletal(VRSceneObj *scn);
		void traverseNodeTree(EFXNodebin *efxNode, VRSceneObj *scn, mat4 &parentMat);

	private :
		std::vector<VRSceneObj*> mScnObjs;
		VRCamera *mCamRef;
		
		int mAnimIndex;
		char *mAssetsPath;

		// Default VR Models
		VRSceneType mSceneType;
		VRSceneObj *mCinemaHall,  *mChairs;
		VRSceneObj *mSpaceShip, *mSpace;
	};
};

#endif //_VRSceneManager_h

