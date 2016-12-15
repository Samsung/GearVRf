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

package org.gearvrf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Opaque type that specifies a material shader.
 */
public class GVRShaderId {
    final Class<? extends GVRShader> ID;
    protected GVRShader mShaderTemplate;
    protected int mNativeShader;

    public GVRShaderId(Class<? extends GVRShader> id)
    {
        ID = id;
        mShaderTemplate = null;
        mNativeShader = 0;
    }

    protected GVRShaderId(GVRShader template)
    {
        ID = template.getClass();
        mShaderTemplate = template;
    }

    GVRShader getTemplate(GVRContext ctx)
    {
        if (mShaderTemplate == null)
        {
            mShaderTemplate = makeTemplate(ID, ctx);
        }
        return mShaderTemplate;
    }

    void setTemplate(GVRShader shader)
    {
        mShaderTemplate = shader;
    }

    int getNativeShader(GVRContext ctx, GVRShaderManager manager)
    {
        if (mNativeShader == 0)
        {
            GVRShader shader = getTemplate(ctx);
            if ((shader != null) && !shader.hasVariants())
            {
                mNativeShader = shader.bindShader(ctx, manager);
            }
        }
        return mNativeShader;
    }

    GVRShader makeTemplate(Class<? extends GVRShader> id, GVRContext ctx)
    {
        try
        {
            Constructor<? extends GVRShader> maker = id.getDeclaredConstructor(GVRContext.class);
            return maker.newInstance(ctx);
        }
        catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex)
        {
            try
            {
                Constructor<? extends GVRShader> maker = id.getDeclaredConstructor();
                return maker.newInstance();
            }
            catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex2)
            {
                ctx.getEventManager().sendEvent(ctx, IErrorEvents.class, "onError", new Object[] {ex2.getMessage(), this});
                return null;
            }
        }
    }
}