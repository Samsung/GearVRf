package com.samsung.android.gvr.app.base.viewmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.Log;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import com.samsung.android.gvr.app.R;
import com.samsung.android.gvr.app.base.misc.GVRTextBitmapFactory;
import com.samsung.android.gvr.app.base.misc.Gyroscope;
import com.samsung.android.gvr.app.base.utils.UtilResource;


public class TestViewManager extends TestDefaultGVRViewManager {
	
    private enum State {
        Idle, Ready, Rotating, Pass, Fail
    };

    private State mState = State.Idle;
    private GVRSceneObject mDegreeBoard = null;
    private GVRSceneObject mAngularVelocityBoard = null;
    private GVRSceneObject mValueBoard = null;
    private GVRSceneObject mStateBoard = null;

    private double mPreviousDegree = 0.0f;
    private long mTimeStamp = 0l;

    private float mAValue = 3.0f;
    private float mBValue = 10.0f;

    private double mFinalDegree = 0.0f;

    private Gyroscope mGyroscope = null;

    @Override
    public void onInitTest() {

        mGyroscope = new Gyroscope(mGVRContext.getContext());

        mGVRContext.getMainScene().getMainCameraRig()
                .setCameraRigType(GVRCameraRig.GVRCameraRigType.YawOnly.ID);

        GVRMesh cylinderMesh = mGVRContext.loadMesh(UtilResource.androidResource(mGVRContext, R.mipmap.cylinder));
        Bitmap cylinderBitmap = BitmapFactory.decodeResource(mGVRContext
                .getContext().getResources(), R.drawable.cylinder2);
        GVRSceneObject cylinder = new GVRSceneObject(mGVRContext, cylinderMesh,
                new GVRBitmapTexture(mGVRContext, cylinderBitmap));

        mGVRContext.getMainScene().addSceneObject(cylinder);

        Bitmap cursorBitmap = BitmapFactory.decodeResource(mGVRContext
                .getContext().getResources(), R.drawable.cursor);

        GVRSceneObject cursor = new GVRSceneObject(mGVRContext, 0.05f, 0.5f,
                new GVRBitmapTexture(mGVRContext, cursorBitmap));
        cursor.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        mGVRContext.getMainScene().getMainCameraRig().getOwnerObject()
                .addChildObject(cursor);

        Bitmap degreeBitmap = GVRTextBitmapFactory.create(1024, 128,
                "degree : 0.00", 40, Align.LEFT, Color.YELLOW,
                Color.TRANSPARENT);
        mDegreeBoard = new GVRSceneObject(mGVRContext, 2.0f, 0.5f,
                new GVRBitmapTexture(mGVRContext, degreeBitmap));
        mDegreeBoard.getTransform().setPosition(-0.5f, 0.7f, -2.0f);
        degreeBitmap.recycle();
        mGVRContext.getMainScene().getMainCameraRig().getOwnerObject()
                .addChildObject(mDegreeBoard);

        Bitmap angularVelocityBitmap = GVRTextBitmapFactory.create(1024, 128,
                "velocity : 0.00", 50, Align.LEFT, Color.YELLOW,
                Color.TRANSPARENT);
        mAngularVelocityBoard = new GVRSceneObject(mGVRContext, 2.0f, 0.5f,
                new GVRBitmapTexture(mGVRContext, angularVelocityBitmap));
        mAngularVelocityBoard.getTransform().setPosition(-0.5f, -0.7f, -2.0f);
        angularVelocityBitmap.recycle();
        mGVRContext.getMainScene().getMainCameraRig().getOwnerObject()
                .addChildObject(mAngularVelocityBoard);

        Bitmap aValueBitmap = GVRTextBitmapFactory.create(1024, 128, String
                .format("ZRO : %.2f, Spec degree : %.2f", mAValue, mBValue),
                30, Align.LEFT, Color.YELLOW, Color.TRANSPARENT);
        mValueBoard = new GVRSceneObject(mGVRContext, 2.0f, 0.5f,
                new GVRBitmapTexture(mGVRContext, aValueBitmap));
        mValueBoard.getTransform().setPosition(-0.5f, 0.5f, -2.0f);
        aValueBitmap.recycle();
        mGVRContext.getMainScene().getMainCameraRig().getOwnerObject()
                .addChildObject(mValueBoard);

        Bitmap stateBitmap = GVRTextBitmapFactory.create(1024, 128, "", 50,
                Align.LEFT, Color.TRANSPARENT, Color.TRANSPARENT);
        mStateBoard = new GVRSceneObject(mGVRContext, 2.5f, 0.625f,
                new GVRBitmapTexture(mGVRContext, stateBitmap));
        mStateBoard.getTransform().setPosition(-0.5f, -0.7f, -5.0f);
        stateBitmap.recycle();
        mGVRContext.getMainScene().getMainCameraRig().getOwnerObject()
                .addChildObject(mStateBoard);

        mTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void onStep() {
        float[] lookAt = mGVRContext.getMainScene().getMainCameraRig()
                .getLookAt();
        double degree = Math.atan2(lookAt[0], -lookAt[2]) * 180.0 / Math.PI;
        if (degree < 0.0) {
            degree += 360.0;
        }

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - mTimeStamp) / 1000.0f;
        mTimeStamp = currentTime;

        double deltaDegree = degree - mPreviousDegree;
        mPreviousDegree = degree;

        if (deltaDegree > 180.0f) {
            deltaDegree -= 360.0f;
        } else if (deltaDegree < -180.0f) {
            deltaDegree += 360.0f;
        }

        float angularVelocity = mGyroscope.getMagnitude();

        Log.v("", String.format("degree : %f", degree));
        Log.v("", String.format("angularVelocity : %f", angularVelocity));

        if (mState == State.Idle) {
            if (angularVelocity < mAValue && angularVelocity > -mAValue) {
                mState = State.Ready;
            }
        } else if (mState == State.Ready) {
            if (angularVelocity > mAValue || angularVelocity < -mAValue) {
                mState = State.Rotating;
            }
        } else if (mState == State.Rotating) {
            if (angularVelocity < mAValue || angularVelocity > -mAValue) {
                if ((degree >= 0.0f && degree < mBValue)
                        || (degree > 360.0f - mBValue && degree < 360.0f)) {
                    mState = State.Pass;
                } else {
                    mState = State.Fail;
                }
                mFinalDegree = degree;
            }
        }

        Bitmap degreeBitmap = GVRTextBitmapFactory.create(1024, 128,
                String.format("degree : %.2f", degree), 50, Align.LEFT,
                Color.YELLOW, Color.TRANSPARENT);
        GVRMaterial degreeMaterial = mDegreeBoard.getRenderData().getMaterial();
        degreeMaterial.setMainTexture(new GVRBitmapTexture(mGVRContext,
                degreeBitmap));
        degreeBitmap.recycle();

        Bitmap angularVelocityBitmap = GVRTextBitmapFactory.create(1024, 128,
                String.format("velocity : %.2f", angularVelocity), 50,
                Align.LEFT, Color.YELLOW, Color.TRANSPARENT);
        GVRMaterial angularVelocityMaterial = mAngularVelocityBoard
                .getRenderData().getMaterial();
        angularVelocityMaterial.setMainTexture(new GVRBitmapTexture(
                mGVRContext, angularVelocityBitmap));
        angularVelocityBitmap.recycle();

        Bitmap aValueBitmap = GVRTextBitmapFactory.create(1024, 128, String
                .format("ZRO : %.2f, Spec degree : %.2f", mAValue, mBValue),
                30, Align.LEFT, Color.YELLOW, Color.TRANSPARENT);
        GVRMaterial aValueMaterial = mValueBoard.getRenderData().getMaterial();
        aValueMaterial.setMainTexture(new GVRBitmapTexture(mGVRContext,
                aValueBitmap));
        aValueBitmap.recycle();

        Bitmap stateBitmap = null;

        switch (mState) {
        case Idle:
            stateBitmap = GVRTextBitmapFactory.create(1024, 128, "", 50,
                    Align.LEFT, Color.TRANSPARENT, Color.TRANSPARENT);
            break;
        case Ready:
            stateBitmap = GVRTextBitmapFactory.create(1024, 128, "Ready", 50,
                    Align.LEFT, Color.BLACK, Color.WHITE);
            break;
        case Rotating:
            stateBitmap = GVRTextBitmapFactory.create(1024, 128, "", 50,
                    Align.LEFT, Color.TRANSPARENT, Color.TRANSPARENT);
            break;
        case Pass:
            stateBitmap = GVRTextBitmapFactory.create(1024, 128,
                    String.format("PASS degree : %.2f", mFinalDegree), 50,
                    Align.LEFT, Color.BLACK, Color.GREEN);
            break;
        case Fail:
            stateBitmap = GVRTextBitmapFactory.create(1024, 128,
                    String.format("FAIL degree : %.2f", mFinalDegree), 50,
                    Align.LEFT, Color.BLACK, Color.RED);
            break;
        }

        GVRMaterial stateMaterial = mStateBoard.getRenderData().getMaterial();
        stateMaterial.setMainTexture(new GVRBitmapTexture(mGVRContext,
                stateBitmap));
        stateBitmap.recycle();
    }

    public void onDoubleTap() {
        mState = State.Idle;
    }

    public void addAValue(float a) {
        mAValue += a;
        if (mAValue < 0.0f) {
            mAValue = 0.0f;
        }
    }

    public void addBValue(float b) {
        mBValue += b;
        if (mBValue < 0.0f) {
            mBValue = 0.0f;
        }
    }

}
