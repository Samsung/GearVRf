package com.samsung.android.gvr.app.base.utils;

import com.samsung.android.gvr.app.R;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;

/**
 * Created by danielnogueira on 4/16/15.
 */
public class UtilResource {

    public static GVRAndroidResource androidResource(GVRContext context, int resource){
       GVRAndroidResource result = null;

        if(context != null ){

            result  = new GVRAndroidResource(context, resource);
        }

        return result;
    }
}
