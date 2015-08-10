/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia.misc;

import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vec4F;
import com.qualcomm.vuforia.VideoBackgroundConfig;


public class VuforiaMath
{
    
    private static final String LOGTAG = "VuforiaMath";
    
    private static float temp[] = new float[16];
    private static Vec3F mLineStart = new Vec3F();
    private static Vec3F mLineEnd = new Vec3F();
    private static Vec3F mIntersection = new Vec3F();
    
    
    public static Vec2F Vec2FSub(Vec2F v1, Vec2F v2)
    {
        temp[0] = v1.getData()[0] - v2.getData()[0];
        temp[1] = v1.getData()[1] - v2.getData()[1];
        return new Vec2F(temp[0], temp[1]);
    }
    
    
    public static float Vec2FDist(Vec2F v1, Vec2F v2)
    {
        float dx = v1.getData()[0] - v2.getData()[0];
        float dy = v1.getData()[1] - v2.getData()[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    
    public static Vec3F Vec3FAdd(Vec3F v1, Vec3F v2)
    {
        temp[0] = v1.getData()[0] + v2.getData()[0];
        temp[1] = v1.getData()[1] + v2.getData()[1];
        temp[2] = v1.getData()[2] + v2.getData()[2];
        return new Vec3F(temp[0], temp[1], temp[2]);
    }
    
    
    public static Vec3F Vec3FSub(Vec3F v1, Vec3F v2)
    {
        temp[0] = v1.getData()[0] - v2.getData()[0];
        temp[1] = v1.getData()[1] - v2.getData()[1];
        temp[2] = v1.getData()[2] - v2.getData()[2];
        return new Vec3F(temp[0], temp[1], temp[2]);
    }
    
    
    public static Vec3F Vec3FScale(Vec3F v, float s)
    {
        temp[0] = v.getData()[0] * s;
        temp[1] = v.getData()[1] * s;
        temp[2] = v.getData()[2] * s;
        return new Vec3F(temp[0], temp[1], temp[2]);
    }
    
    
    public static float Vec3FDot(Vec3F v1, Vec3F v2)
    {
        return v1.getData()[0] * v2.getData()[0] + v1.getData()[1]
            * v2.getData()[1] + v1.getData()[2] * v2.getData()[2];
    }
    
    
    public static Vec3F Vec3FCross(Vec3F v1, Vec3F v2)
    {
        temp[0] = v1.getData()[1] * v2.getData()[2] - v1.getData()[2]
            * v2.getData()[1];
        temp[1] = v1.getData()[2] * v2.getData()[0] - v1.getData()[0]
            * v2.getData()[2];
        temp[2] = v1.getData()[0] * v2.getData()[1] - v1.getData()[1]
            * v2.getData()[0];
        return new Vec3F(temp[0], temp[1], temp[2]);
    }
    
    
    public static Vec3F Vec3FNormalize(Vec3F v)
    {
        float length = (float) Math
            .sqrt(v.getData()[0] * v.getData()[0] + v.getData()[1]
                * v.getData()[1] + v.getData()[2] * v.getData()[2]);
        if (length != 0.0f)
            length = 1.0f / length;
        
        temp[0] = v.getData()[0] * length;
        temp[1] = v.getData()[1] * length;
        temp[2] = v.getData()[2] * length;
        
        return new Vec3F(temp[0], temp[1], temp[2]);
    }
    
    
    public static Vec3F Vec3FTransform(Vec3F v, Matrix44F m)
    {
        float lambda;
        lambda = m.getData()[12] * v.getData()[0] + m.getData()[13]
            * v.getData()[1] + m.getData()[14] * v.getData()[2]
            + m.getData()[15];
        
        temp[0] = m.getData()[0] * v.getData()[0] + m.getData()[1]
            * v.getData()[1] + m.getData()[2] * v.getData()[2] + m.getData()[3];
        temp[1] = m.getData()[4] * v.getData()[0] + m.getData()[5]
            * v.getData()[1] + m.getData()[6] * v.getData()[2] + m.getData()[7];
        temp[2] = m.getData()[8] * v.getData()[0] + m.getData()[9]
            * v.getData()[1] + m.getData()[10] * v.getData()[2]
            + m.getData()[11];
        
        temp[0] /= lambda;
        temp[1] /= lambda;
        temp[2] /= lambda;
        
        return new Vec3F(temp[0], temp[1], temp[2]);
    }
    
    
    public static Vec3F Vec3FTransformNormal(Vec3F v, Matrix44F m)
    {
        temp[0] = m.getData()[0] * v.getData()[0] + m.getData()[1]
            * v.getData()[1] + m.getData()[2] * v.getData()[2];
        temp[1] = m.getData()[4] * v.getData()[0] + m.getData()[5]
            * v.getData()[1] + m.getData()[6] * v.getData()[2];
        temp[2] = m.getData()[8] * v.getData()[0] + m.getData()[9]
            * v.getData()[1] + m.getData()[10] * v.getData()[2];
        
        return new Vec3F(temp[0], temp[1], temp[2]);
    }
    
    
    public static Vec4F Vec4FTransform(Vec4F v, Matrix44F m)
    {
        temp[0] = m.getData()[0] * v.getData()[0] + m.getData()[1]
            * v.getData()[1] + m.getData()[2] * v.getData()[2] + m.getData()[3]
            * v.getData()[3];
        temp[1] = m.getData()[4] * v.getData()[0] + m.getData()[5]
            * v.getData()[1] + m.getData()[6] * v.getData()[2] + m.getData()[7]
            * v.getData()[3];
        temp[2] = m.getData()[8] * v.getData()[0] + m.getData()[9]
            * v.getData()[1] + m.getData()[10] * v.getData()[2]
            + m.getData()[11] * v.getData()[3];
        temp[3] = m.getData()[12] * v.getData()[0] + m.getData()[13]
            * v.getData()[1] + m.getData()[14] * v.getData()[2]
            + m.getData()[15] * v.getData()[3];
        
        return new Vec4F(temp[0], temp[1], temp[2], temp[3]);
    }
    
    
    public static Vec4F Vec4FDiv(Vec4F v, float s)
    {
        temp[0] = v.getData()[0] / s;
        temp[1] = v.getData()[1] / s;
        temp[2] = v.getData()[2] / s;
        temp[3] = v.getData()[3] / s;
        return new Vec4F(temp[0], temp[1], temp[2], temp[3]);
    }
    
    
    public static Matrix44F Matrix44FIdentity()
    {
        Matrix44F r = new Matrix44F();
        
        for (int i = 0; i < 16; i++)
            temp[i] = 0.0f;
        
        temp[0] = 1.0f;
        temp[5] = 1.0f;
        temp[10] = 1.0f;
        temp[15] = 1.0f;
        
        r.setData(temp);
        
        return r;
    }
    
    
    public static Matrix44F Matrix44FTranspose(Matrix44F m)
    {
        Matrix44F r = new Matrix44F();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                temp[i * 4 + j] = m.getData()[i + 4 * j];
        
        r.setData(temp);
        return r;
    }
    
    
    public static float Matrix44FDeterminate(Matrix44F m)
    {
        return m.getData()[12] * m.getData()[9] * m.getData()[6]
            * m.getData()[3] - m.getData()[8] * m.getData()[13]
            * m.getData()[6] * m.getData()[3] - m.getData()[12]
            * m.getData()[5] * m.getData()[10] * m.getData()[3]
            + m.getData()[4] * m.getData()[13] * m.getData()[10]
            * m.getData()[3] + m.getData()[8] * m.getData()[5]
            * m.getData()[14] * m.getData()[3] - m.getData()[4]
            * m.getData()[9] * m.getData()[14] * m.getData()[3]
            - m.getData()[12] * m.getData()[9] * m.getData()[2]
            * m.getData()[7] + m.getData()[8] * m.getData()[13]
            * m.getData()[2] * m.getData()[7] + m.getData()[12]
            * m.getData()[1] * m.getData()[10] * m.getData()[7]
            - m.getData()[0] * m.getData()[13] * m.getData()[10]
            * m.getData()[7] - m.getData()[8] * m.getData()[1]
            * m.getData()[14] * m.getData()[7] + m.getData()[0]
            * m.getData()[9] * m.getData()[14] * m.getData()[7]
            + m.getData()[12] * m.getData()[5] * m.getData()[2]
            * m.getData()[11] - m.getData()[4] * m.getData()[13]
            * m.getData()[2] * m.getData()[11] - m.getData()[12]
            * m.getData()[1] * m.getData()[6] * m.getData()[11]
            + m.getData()[0] * m.getData()[13] * m.getData()[6]
            * m.getData()[11] + m.getData()[4] * m.getData()[1]
            * m.getData()[14] * m.getData()[11] - m.getData()[0]
            * m.getData()[5] * m.getData()[14] * m.getData()[11]
            - m.getData()[8] * m.getData()[5] * m.getData()[2]
            * m.getData()[15] + m.getData()[4] * m.getData()[9]
            * m.getData()[2] * m.getData()[15] + m.getData()[8]
            * m.getData()[1] * m.getData()[6] * m.getData()[15]
            - m.getData()[0] * m.getData()[9] * m.getData()[6]
            * m.getData()[15] - m.getData()[4] * m.getData()[1]
            * m.getData()[10] * m.getData()[15] + m.getData()[0]
            * m.getData()[5] * m.getData()[10] * m.getData()[15];
    }
    
    
    public static Matrix44F Matrix44FInverse(Matrix44F m)
    {
        Matrix44F r = new Matrix44F();
        
        float det = 1.0f / Matrix44FDeterminate(m);
        
        temp[0] = m.getData()[6] * m.getData()[11] * m.getData()[13]
            - m.getData()[7] * m.getData()[10] * m.getData()[13]
            + m.getData()[7] * m.getData()[9] * m.getData()[14]
            - m.getData()[5] * m.getData()[11] * m.getData()[14]
            - m.getData()[6] * m.getData()[9] * m.getData()[15]
            + m.getData()[5] * m.getData()[10] * m.getData()[15];
        
        temp[4] = m.getData()[3] * m.getData()[10] * m.getData()[13]
            - m.getData()[2] * m.getData()[11] * m.getData()[13]
            - m.getData()[3] * m.getData()[9] * m.getData()[14]
            + m.getData()[1] * m.getData()[11] * m.getData()[14]
            + m.getData()[2] * m.getData()[9] * m.getData()[15]
            - m.getData()[1] * m.getData()[10] * m.getData()[15];
        
        temp[8] = m.getData()[2] * m.getData()[7] * m.getData()[13]
            - m.getData()[3] * m.getData()[6] * m.getData()[13]
            + m.getData()[3] * m.getData()[5] * m.getData()[14]
            - m.getData()[1] * m.getData()[7] * m.getData()[14]
            - m.getData()[2] * m.getData()[5] * m.getData()[15]
            + m.getData()[1] * m.getData()[6] * m.getData()[15];
        
        temp[12] = m.getData()[3] * m.getData()[6] * m.getData()[9]
            - m.getData()[2] * m.getData()[7] * m.getData()[9] - m.getData()[3]
            * m.getData()[5] * m.getData()[10] + m.getData()[1]
            * m.getData()[7] * m.getData()[10] + m.getData()[2]
            * m.getData()[5] * m.getData()[11] - m.getData()[1]
            * m.getData()[6] * m.getData()[11];
        
        temp[1] = m.getData()[7] * m.getData()[10] * m.getData()[12]
            - m.getData()[6] * m.getData()[11] * m.getData()[12]
            - m.getData()[7] * m.getData()[8] * m.getData()[14]
            + m.getData()[4] * m.getData()[11] * m.getData()[14]
            + m.getData()[6] * m.getData()[8] * m.getData()[15]
            - m.getData()[4] * m.getData()[10] * m.getData()[15];
        
        temp[5] = m.getData()[2] * m.getData()[11] * m.getData()[12]
            - m.getData()[3] * m.getData()[10] * m.getData()[12]
            + m.getData()[3] * m.getData()[8] * m.getData()[14]
            - m.getData()[0] * m.getData()[11] * m.getData()[14]
            - m.getData()[2] * m.getData()[8] * m.getData()[15]
            + m.getData()[0] * m.getData()[10] * m.getData()[15];
        
        temp[9] = m.getData()[3] * m.getData()[6] * m.getData()[12]
            - m.getData()[2] * m.getData()[7] * m.getData()[12]
            - m.getData()[3] * m.getData()[4] * m.getData()[14]
            + m.getData()[0] * m.getData()[7] * m.getData()[14]
            + m.getData()[2] * m.getData()[4] * m.getData()[15]
            - m.getData()[0] * m.getData()[6] * m.getData()[15];
        
        temp[13] = m.getData()[2] * m.getData()[7] * m.getData()[8]
            - m.getData()[3] * m.getData()[6] * m.getData()[8] + m.getData()[3]
            * m.getData()[4] * m.getData()[10] - m.getData()[0]
            * m.getData()[7] * m.getData()[10] - m.getData()[2]
            * m.getData()[4] * m.getData()[11] + m.getData()[0]
            * m.getData()[6] * m.getData()[11];
        
        temp[2] = m.getData()[5] * m.getData()[11] * m.getData()[12]
            - m.getData()[7] * m.getData()[9] * m.getData()[12]
            + m.getData()[7] * m.getData()[8] * m.getData()[13]
            - m.getData()[4] * m.getData()[11] * m.getData()[13]
            - m.getData()[5] * m.getData()[8] * m.getData()[15]
            + m.getData()[4] * m.getData()[9] * m.getData()[15];
        
        temp[6] = m.getData()[3] * m.getData()[9] * m.getData()[12]
            - m.getData()[1] * m.getData()[11] * m.getData()[12]
            - m.getData()[3] * m.getData()[8] * m.getData()[13]
            + m.getData()[0] * m.getData()[11] * m.getData()[13]
            + m.getData()[1] * m.getData()[8] * m.getData()[15]
            - m.getData()[0] * m.getData()[9] * m.getData()[15];
        
        temp[10] = m.getData()[1] * m.getData()[7] * m.getData()[12]
            - m.getData()[3] * m.getData()[5] * m.getData()[12]
            + m.getData()[3] * m.getData()[4] * m.getData()[13]
            - m.getData()[0] * m.getData()[7] * m.getData()[13]
            - m.getData()[1] * m.getData()[4] * m.getData()[15]
            + m.getData()[0] * m.getData()[5] * m.getData()[15];
        
        temp[14] = m.getData()[3] * m.getData()[5] * m.getData()[8]
            - m.getData()[1] * m.getData()[7] * m.getData()[8] - m.getData()[3]
            * m.getData()[4] * m.getData()[9] + m.getData()[0] * m.getData()[7]
            * m.getData()[9] + m.getData()[1] * m.getData()[4]
            * m.getData()[11] - m.getData()[0] * m.getData()[5]
            * m.getData()[11];
        
        temp[3] = m.getData()[6] * m.getData()[9] * m.getData()[12]
            - m.getData()[5] * m.getData()[10] * m.getData()[12]
            - m.getData()[6] * m.getData()[8] * m.getData()[13]
            + m.getData()[4] * m.getData()[10] * m.getData()[13]
            + m.getData()[5] * m.getData()[8] * m.getData()[14]
            - m.getData()[4] * m.getData()[9] * m.getData()[14];
        
        temp[7] = m.getData()[1] * m.getData()[10] * m.getData()[12]
            - m.getData()[2] * m.getData()[9] * m.getData()[12]
            + m.getData()[2] * m.getData()[8] * m.getData()[13]
            - m.getData()[0] * m.getData()[10] * m.getData()[13]
            - m.getData()[1] * m.getData()[8] * m.getData()[14]
            + m.getData()[0] * m.getData()[9] * m.getData()[14];
        
        temp[11] = m.getData()[2] * m.getData()[5] * m.getData()[12]
            - m.getData()[1] * m.getData()[6] * m.getData()[12]
            - m.getData()[2] * m.getData()[4] * m.getData()[13]
            + m.getData()[0] * m.getData()[6] * m.getData()[13]
            + m.getData()[1] * m.getData()[4] * m.getData()[14]
            - m.getData()[0] * m.getData()[5] * m.getData()[14];
        
        temp[15] = m.getData()[1] * m.getData()[6] * m.getData()[8]
            - m.getData()[2] * m.getData()[5] * m.getData()[8] + m.getData()[2]
            * m.getData()[4] * m.getData()[9] - m.getData()[0] * m.getData()[6]
            * m.getData()[9] - m.getData()[1] * m.getData()[4]
            * m.getData()[10] + m.getData()[0] * m.getData()[5]
            * m.getData()[10];
        
        for (int i = 0; i < 16; i++)
            temp[i] *= det;
        
        r.setData(temp);
        return r;
    }
    
    
    public static Vec3F linePlaneIntersection(Vec3F lineStart, Vec3F lineEnd,
        Vec3F pointOnPlane, Vec3F planeNormal)
    {
        Vec3F lineDir = Vec3FSub(lineEnd, lineStart);
        lineDir = Vec3FNormalize(lineDir);
        
        Vec3F planeDir = Vec3FSub(pointOnPlane, lineStart);
        
        float n = Vec3FDot(planeNormal, planeDir);
        float d = Vec3FDot(planeNormal, lineDir);
        
        if (Math.abs(d) < 0.00001)
        {
            // Line is parallel to plane
            return null;
        }
        
        float dist = n / d;
        
        Vec3F offset = Vec3FScale(lineDir, dist);
        
        return Vec3FAdd(lineStart, offset);
    }
    
    
    private static void projectScreenPointToPlane(Matrix44F inverseProjMatrix,
        Matrix44F modelViewMatrix, float screenWidth, float screenHeight,
        Vec2F point, Vec3F planeCenter, Vec3F planeNormal)
    {
        // Window Coordinates to Normalized Device Coordinates
        VideoBackgroundConfig config = Renderer.getInstance()
            .getVideoBackgroundConfig();
        
        float halfScreenWidth = screenWidth / 2.0f;
        float halfScreenHeight = screenHeight / 2.0f;
        
        float halfViewportWidth = config.getSize().getData()[0] / 2.0f;
        float halfViewportHeight = config.getSize().getData()[1] / 2.0f;
        
        float x = (point.getData()[0] - halfScreenWidth) / halfViewportWidth;
        float y = (point.getData()[1] - halfScreenHeight) / halfViewportHeight
            * -1;
        
        Vec4F ndcNear = new Vec4F(x, y, -1, 1);
        Vec4F ndcFar = new Vec4F(x, y, 1, 1);
        
        // Normalized Device Coordinates to Eye Coordinates
        Vec4F pointOnNearPlane = Vec4FTransform(ndcNear, inverseProjMatrix);
        Vec4F pointOnFarPlane = Vec4FTransform(ndcFar, inverseProjMatrix);
        pointOnNearPlane = Vec4FDiv(pointOnNearPlane,
            pointOnNearPlane.getData()[3]);
        pointOnFarPlane = Vec4FDiv(pointOnFarPlane,
            pointOnFarPlane.getData()[3]);
        
        // Eye Coordinates to Object Coordinates
        Matrix44F inverseModelViewMatrix = Matrix44FInverse(modelViewMatrix);
        
        Vec4F nearWorld = Vec4FTransform(pointOnNearPlane,
            inverseModelViewMatrix);
        Vec4F farWorld = Vec4FTransform(pointOnFarPlane, inverseModelViewMatrix);
        
        mLineStart = new Vec3F(nearWorld.getData()[0], nearWorld.getData()[1],
            nearWorld.getData()[2]);
        mLineEnd = new Vec3F(farWorld.getData()[0], farWorld.getData()[1],
            farWorld.getData()[2]);
        mIntersection = linePlaneIntersection(mLineStart, mLineEnd,
            planeCenter, planeNormal);
        
        if (mIntersection == null)
            Log.e(LOGTAG, "No intersection with the plane");
    }
    
    
    public static Vec3F getPointToPlaneIntersection(
        Matrix44F inverseProjMatrix, Matrix44F modelViewMatrix,
        float screenWidth, float screenHeight, Vec2F point, Vec3F planeCenter,
        Vec3F planeNormal)
    {
        projectScreenPointToPlane(inverseProjMatrix, modelViewMatrix,
            screenWidth, screenHeight, point, planeCenter, planeNormal);
        return mIntersection;
    }
    
    
    public static Vec3F getPointToPlaneLineStart(Matrix44F inverseProjMatrix,
        Matrix44F modelViewMatrix, float screenWidth, float screenHeight,
        Vec2F point, Vec3F planeCenter, Vec3F planeNormal)
    {
        projectScreenPointToPlane(inverseProjMatrix, modelViewMatrix,
            screenWidth, screenHeight, point, planeCenter, planeNormal);
        return mLineStart;
    }
    
    
    public static Vec3F getPointToPlaneLineEnd(Matrix44F inverseProjMatrix,
        Matrix44F modelViewMatrix, float screenWidth, float screenHeight,
        Vec2F point, Vec3F planeCenter, Vec3F planeNormal)
    {
        projectScreenPointToPlane(inverseProjMatrix, modelViewMatrix,
            screenWidth, screenHeight, point, planeCenter, planeNormal);
        return mLineEnd;
    }
}
