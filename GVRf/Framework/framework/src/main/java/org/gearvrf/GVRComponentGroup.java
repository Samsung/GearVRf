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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A container component for components of the same type
 * @param <T> class of component the group contains
 */
public final class GVRComponentGroup<T extends GVRComponent> extends GVRComponent implements IComponentGroup<T>
{
    /**
     *
     * @param gvrContext
     * @param type a unique type id
     */
    public GVRComponentGroup(GVRContext gvrContext, long type) {
        super(gvrContext, 0);
        mType = type;
    }

    private final List<T> mComponents = new ArrayList<T>();

    @Override
    public Iterator<T> iterator()
    {
        Iterator<T> iter = new Iterator<T>()
        {
            int mIndex = 0;

            public boolean hasNext()
            {
                return mIndex < getSize();
            }

            public T next()
            {
                synchronized (mComponents) {
                    if (mIndex < getSize()) {
                        return mComponents.get(mIndex++);
                    }
                }
                return null;
            }
        };
        return iter;
    }

    @Override
    public void addChildComponent(T child) {
        synchronized (mComponents) {
            mComponents.add(child);
        }
    }

    @Override
    public void removeChildComponent(T child) {
        synchronized (mComponents) {
            mComponents.remove(child);
        }
    }

    @Override
    public int getSize()
    {
        synchronized (mComponents) {
            return mComponents.size();
        }
    }

    @Override
    public T getChildAt(int index)
    {
        synchronized (mComponents) {
            return mComponents.get(index);
        }
    }

     @Override
    public void onAttach(GVRSceneObject newOwner) {
         synchronized (mComponents) {
             for (T t : mComponents) {
                 t.onAttach(newOwner);
             }
         }
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        synchronized (mComponents) {
            for (T t : mComponents) {
                t.onDetach(oldOwner);
            }
        }
    }

    @Override
    public void onNewOwnersParent(GVRSceneObject newOwnersParent) {
        synchronized (mComponents) {
            for (T t : mComponents) {
                t.onNewOwnersParent(newOwnersParent);
            }
        }
    }

    @Override
    public void onRemoveOwnersParent(GVRSceneObject oldOwnersParent) {
        synchronized (mComponents) {
            for (T t : mComponents) {
                t.onRemoveOwnersParent(oldOwnersParent);
            }
        }
    }

    @Override
    public void onEnable() {
        synchronized (mComponents) {
            for (T t : mComponents) {
                t.onEnable();
            }
        }
    }

    @Override
    public void onDisable() {
        synchronized (mComponents) {
            for (T t : mComponents) {
                t.onDisable();
            }
        }
    }
}
