package org.gearvrf.mixedreality.arcore;

import com.google.ar.core.Anchor;

import org.gearvrf.GVRContext;
import org.gearvrf.mixedreality.GVRAnchor;

public class ARCoreAnchor extends GVRAnchor {
    private Anchor anchor;

    public ARCoreAnchor(GVRContext gvrContext) {
        super(gvrContext);
    }

    public void setAnchorAR(Anchor anchor) {
        this.anchor = anchor;
    }

    public Anchor getAnchorAR() {
        return this.anchor;
    }
}
