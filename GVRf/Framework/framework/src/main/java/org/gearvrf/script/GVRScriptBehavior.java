package org.gearvrf.script;

import java.io.IOException;
import java.util.HashMap;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IErrorEvents;
import org.gearvrf.IPickEvents;
import org.gearvrf.ISceneEvents;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.GVRPicker;
import org.gearvrf.IPickEvents;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;

/**
 * Attaches a Java or Lua script to a scene object.
 * 
 * These script callbacks are invoked if they are present:
 *      onEarlyInit(GVRContext) called after script is loaded
 *      onAfterInit()           called when the script becomes active
 *                              (this component is attached to a scene object and enabled)
 *      onStep()                called every frame if this component is enabled
 *                              and attached to a scene object
 *      onPickEnter(GVRSceneObject, GVRPicker.GVRPickedObject)
 *                              called when picking ray enters an object
 *      onPickExit(GVRSceneObject)
 *                              called when picking ray exits an object
 *      onPickInside(GVRSceneObject, GVRPicker.GVRPickedObject)
 *                              called when picking ray is inside an object
 *      onPick(GVRPicker)       called when picking selection changes
 *      onNoPick(GVRPicker)     called when nothing is picked
 *
 */
public class GVRScriptBehavior extends GVRBehavior implements IPickEvents, ISensorEvents, ISceneEvents
{
    static private long TYPE_SCRIPT_BEHAVIOR = newComponentType(GVRScriptBehavior.class);
    static private Object[] noargs = new Object[0];
    protected GVRScriptFile mScriptFile = null;
    protected boolean mIsAttached = false;
    protected int mPickEvents = 0xF;
    protected String mLanguage = GVRScriptManager.LANG_JAVASCRIPT;
    private String mLastError;
    private GVRScene mScene = null;
    
    private final int ON_ENTER = 1;
    private final int ON_EXIT = 2;
    private final int ON_PICK = 4;
    private final int ON_NOPICK = 8;
    private final int PICK_EVENTS = (ON_ENTER | ON_EXIT | ON_PICK | ON_NOPICK);
    
    /**
     * Constructor for a script behavior component.
     * @param gvrContext    The current GVRF context
     */
    public GVRScriptBehavior(GVRContext gvrContext)
    {
        super(gvrContext);
        mHasFrameCallback = false;
        mType = TYPE_SCRIPT_BEHAVIOR;
        mIsAttached = false;
        mLanguage = GVRScriptManager.LANG_JAVASCRIPT;
        gvrContext.getEventReceiver().addListener(this);
    }

    /**
     * Constructor for a script behavior component.
     * @param gvrContext    The current GVRF context
     * @param scriptFile    Path to the script file.
     */
    public GVRScriptBehavior(GVRContext gvrContext, String scriptFile) throws IOException, GVRScriptException
    {
        super(gvrContext);
        mHasFrameCallback = false;
        mType = TYPE_SCRIPT_BEHAVIOR;
        mIsAttached = false;
        mLanguage = GVRScriptManager.LANG_JAVASCRIPT;
        gvrContext.getEventReceiver().addListener(this);
        setFilePath(scriptFile);
    }

    public GVRScriptFile getScriptFile()
    {
        return mScriptFile;
    }
    
    public static long getComponentType() { return TYPE_SCRIPT_BEHAVIOR; }
    
    /**
     * Sets the path to the script file to load and loads the script.
     * 
     * @param filePath path to script file
     * @throws IOException
     * @throws GVRScriptException
     */
    public void setFilePath(String filePath) throws IOException, GVRScriptException
    {
        GVRResourceVolume.VolumeType volumeType = GVRResourceVolume.VolumeType.ANDROID_ASSETS;
        String fname = filePath.toLowerCase();
        
        mLanguage = FileNameUtils.getExtension(fname);        
        if (fname.startsWith("sd:"))
        {
            volumeType = GVRResourceVolume.VolumeType.ANDROID_SDCARD;
        }
        else if (fname.startsWith("http:") || fname.startsWith("https:"))
        {
            volumeType = GVRResourceVolume.VolumeType.NETWORK;            
        }
        GVRResourceVolume volume = new GVRResourceVolume(getGVRContext(), volumeType,
                FileNameUtils.getParentDirectory(filePath));
        GVRAndroidResource resource = volume.openResource(filePath);
         
        setScriptFile(getGVRContext().getScriptManager().loadScript(resource, mLanguage));
    }
    
    /**
     * Loads the script from a text string.
     * @param scriptText text string containing script to execute.
     * @param language language ("js" or "lua")
     */
    public void setScriptText(String scriptText, String language)
    {
        GVRScriptFile newScript;
        
        if (language.equals(GVRScriptManager.LANG_LUA))
        {
            newScript = new GVRLuaScriptFile(getGVRContext(), scriptText);
            mLanguage = language;
        }
        else
        {
            newScript = new GVRJavascriptScriptFile(getGVRContext(), scriptText);
            mLanguage = GVRScriptManager.LANG_JAVASCRIPT;
        }
        setScriptFile(newScript);        
    }
    
    /**
     * Set the GVRScriptFile to execute.
     * @param scriptFile GVRScriptFile with script already loaded.
     * If the script contains a function called "onEarlyInit"
     * it is called if the script file is valid.
     */
    public void setScriptFile(GVRScriptFile scriptFile)
    {
        if (mScriptFile != scriptFile)
        {
            detachScript();
            mScriptFile = scriptFile;
        }
    }
    
    /**
     * Invokes the script associated with this component.
     * This function invokes the script even if the
     * component is not enabled and not attached to
     * a scene object.
     * @see GVRScriptFile.invoke
     */
    public void invoke()
    {
        if (mScriptFile != null)
        {
            mScriptFile.invoke();
        }
    }

    public void onInit(GVRContext context, GVRScene scene)
    {
        mScene = scene;
        startPicking();
    }

    public void onAfterInit() { }

    public void onStep() { }

    public void onAttach(GVRSceneObject owner)
    {
        super.onAttach(owner);
        attachScript(owner);
    }

    public void onEnable()
    {
        super.onEnable();
        attachScript(null);
    }
    
    public void onDetach(GVRSceneObject owner)
    {
        detachScript();
        super.onDetach(owner);
    }
    
    public void onDrawFrame(float frameTime)
    {
        invokeFunction("onStep", noargs);
    }

    public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject hit)
    {
         if ((sceneObj == getOwnerObject()) && !invokeFunction("onPickEnter", new Object[] { sceneObj, hit }))
         {
             mPickEvents &= ~ON_ENTER;
             if (mPickEvents == 0)
             {
                 stopPicking();
             }
         }
    }

    public void onExit(GVRSceneObject sceneObj)
    {
        if ((sceneObj == getOwnerObject()) && !invokeFunction("onPickExit", new Object[] { sceneObj }))
        {
            mPickEvents &= ~ON_EXIT;
            if (mPickEvents == 0)
            {
                stopPicking();
            }
        }
    }

    public void onPick(GVRPicker picker)
    {
        if (!invokeFunction("onPick", new Object[] { picker }))
        {
            mPickEvents &= ~ON_PICK;
            if (mPickEvents == 0)
            {
                stopPicking();
            }
        }
    }

    public void onNoPick(GVRPicker picker)
    {
       if (!invokeFunction("onNoPick", new Object[] { picker }))
       {
           mPickEvents &= ~ON_NOPICK;
           if (mPickEvents == 0)
           {
               stopPicking();
           }
       }
    }

    public void onSensorEvent(SensorEvent event)
    {
        invokeFunction("onSensorEvent", new Object[] { event });
    }

    public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject hit) { }

    protected void attachScript(GVRSceneObject owner)
    {
        if (owner == null)
        {
            owner = getOwnerObject();
        }
        if (!mIsAttached && (mScriptFile != null) && isEnabled() && (owner != null) && owner.isEnabled())
        {
            getGVRContext().getScriptManager().attachScriptFile(owner, mScriptFile);
            mIsAttached = true;
            owner.getEventReceiver().addListener(this);
            if (invokeFunction("onStep", noargs))
            {
                mHasFrameCallback = true;
                startListening();
            }
            startPicking();
        }
    }

    protected void startPicking()
    {
        GVRScene scene = mScene;
        mPickEvents = PICK_EVENTS;
        if (mScene == null)
        {
            scene = getGVRContext().getMainScene();
        }
        scene.getEventReceiver().addListener(this);
    }

    protected void stopPicking()
    {
        GVRScene scene = mScene;
        if (mScene == null)
        {
            scene = getGVRContext().getMainScene();
        }
        scene.getEventReceiver().removeListener(this);
    }

    protected void detachScript()
    {
        GVRSceneObject owner = getOwnerObject();
        
        if (mIsAttached && (owner != null))
        {
            getGVRContext().getScriptManager().detachScriptFile(owner);
            owner.getEventReceiver().removeListener(this);
            mIsAttached = false;
            mHasFrameCallback = true;
            stopPicking();
            stopListening();
        }
    }

    /**
     * Calls a function script associated with this component.
     * The function is called even if the component
     * is not enabled and not attached to a scene object.
     * @param funcName name of script function to call.
     * @param params function parameters as an array of objects.
     * @return true if function was called, false if no such function
     * @see GVRScriptFile.invokeFunction
     */
    public boolean invokeFunction(String funcName, Object[] args)
    {
        mLastError = null;
        if (mScriptFile != null)
        {
            if (mScriptFile.invokeFunction(funcName, args))
            {
                return true;
            }
        }
        mLastError = mScriptFile.getLastError();
        if ((mLastError != null) && !mLastError.contains("is not defined"))
        {
            getGVRContext().logError(mLastError, this);
        }
        return false;
    }
}
