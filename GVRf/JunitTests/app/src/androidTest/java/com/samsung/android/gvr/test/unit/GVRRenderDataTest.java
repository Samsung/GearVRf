package com.samsung.android.gvr.test.unit;

import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by santhyago on 2/26/15.
 */
public class GVRRenderDataTest  {

//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//    }
//
//    public void testGetAlphaBlend() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        assertTrue(renderDataLeftScreen.getAlphaBlend());
//    }
//
//    public void testGetDepthTest() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        assertTrue(renderDataLeftScreen.getDepthTest());
//    }
//
//    public void testGetOffsetUnits() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        assertEquals(0.0f, renderDataLeftScreen.getOffsetUnits());
//    }
//
//    public void testGetOffsetFactor() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        assertEquals(0.0f, renderDataLeftScreen.getOffsetFactor());
//    }
//
//    public void testGetOffset() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        assertTrue(!renderDataLeftScreen.getOffset());
//    }
//
//    public void testGetCullTest() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        assertTrue(renderDataLeftScreen.getCullTest());
//    }
//
//    public void testGetRenderingOrder() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        assertTrue(renderDataLeftScreen.getRenderingOrder() > 0);
//    }
//
//    public void testGetRenderMask() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setRenderMask(2);
//        assertTrue(renderDataLeftScreen.getRenderMask() > 0);
//    }
//
//    public void testSetAlphaBlend() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setAlphaBlend(false);
//        assertTrue(!renderDataLeftScreen.getAlphaBlend());
//    }
//
//    public void testSetDepthTest() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setDepthTest(false);
//        assertTrue(!renderDataLeftScreen.getDepthTest());
//    }
//
//    public void testSetOffsetUnits() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setOffsetUnits(2.0f);
//        assertEquals(2.0f, renderDataLeftScreen.getOffsetUnits());
//    }
//
//    public void testSetOffsetFactor() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setOffsetFactor(2.0f);
//        assertEquals(2.0f, renderDataLeftScreen.getOffsetFactor());
//    }
//
//    public void testSetOffset() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setOffset(true);
//        assertTrue(renderDataLeftScreen.getOffset());
//    }
//
//    public void testSetCullTest() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setCullTest(false);
//        assertTrue(!renderDataLeftScreen.getCullTest());
//    }
//
//    public void testSetRenderingOrder() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setRenderingOrder(2);
//        assertEquals(2, renderDataLeftScreen.getRenderingOrder());
//    }
//
//    public void testSetRenderMask() {
//        SampleViewManager renderDataScript = (SampleViewManager) getActivity().getViewManager();
//        GVRRenderData renderDataLeftScreen = renderDataScript.leftSceneObject.getRenderData();
//
//        assertNotNull(renderDataScript);
//        assertNotNull(renderDataLeftScreen);
//        renderDataLeftScreen.setRenderMask(2);
//        assertEquals(2, renderDataLeftScreen.getRenderMask());
//    }
//
//    @Override
//    public void setApplicationMode() {
//        MainActivity.activityMode = MainActivity.GVR_RENDERDATA_TEST;
//    }
}
