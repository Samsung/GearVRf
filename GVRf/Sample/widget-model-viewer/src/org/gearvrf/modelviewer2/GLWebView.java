/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.modelviewer2;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by user on 3/15/15.
 */
public class GLWebView extends ListView {

    private ViewerScript mViewToGLRenderer;

    // default constructors

    public GLWebView(Context context) {
        super(context);
    }

    public GLWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // draw magic
    @Override
    protected void onDraw(Canvas canvas) {
        // returns canvas attached to gl texture to draw on
        /*
         * Canvas c = canvas; Canvas glAttachedCanvas =
         * mViewToGLRenderer.refresh(); if(glAttachedCanvas != null) {
         * //translate canvas to reflect view scrolling // //draw the view to
         * provided canvas super.onDraw(glAttachedCanvas);
         * //super.draw(glAttachedCanvas); // draw(glAttachedCanvas);
         * mViewToGLRenderer.postrefresh(glAttachedCanvas); }
         */
        // scrollTo(-getScrollX(), -getScrollY());
        // canvas.translate(-getScrollX(), -getScrollY());
        // Canvas c = new Canvas();
        // super.onDraw(c);
    }

    public void setViewToGLRenderer(ViewerScript viewTOGLRenderer) {
        mViewToGLRenderer = viewTOGLRenderer;
    }
}
