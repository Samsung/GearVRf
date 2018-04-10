package org.gearvrf.mixedreality.arcore;

import com.google.ar.core.Anchor;

import org.gearvrf.GVRContext;
import org.gearvrf.mixedreality.GVRAnchor;

public class ARCoreAnchor extends GVRAnchor {
    private Anchor mAnchor;
    ARCoreSession mSession;

    public ARCoreAnchor(GVRContext gvrContext, ARCoreSession session) {
        super(gvrContext);
        this.mSession = session;
    }

    public void setAnchorAR(Anchor anchor) {
        this.mAnchor = anchor;
    }

    public Anchor getAnchorAR() {
        return this.mAnchor;
    }

    @Override
    protected void onDetachAnchor() {
        this.mSession.onDetachAnchor(this);
        this.mAnchor.detach();
    }
}
