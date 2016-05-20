package org.gearvrf;

/**
 * Interface for processing asset load events.
 * The asset loader raises an event whenever a texture
 * or a model is loaded. It also raises an event when
 * the entire asset (models and textures) is loaded.
 * This interface lets you observe  these events.
 */
public interface IAssetEvents extends IEvents
{
    /**
     * Called when a model is successfully loaded.
     * This event will not be raised until the model and all of
     * its textures are loaded.
     * @param context   GVRContext used to load the model
     * @param model     GVRSceneObject root of the model scene graph, null if model did not load.
     * @param filePath  File path or URL of the model.
     * @param errors    String with loading errors or null if successful
     */
    public void onAssetLoaded(GVRContext context, GVRSceneObject model, String filePath, String errors);
    
    /**
     * Called when a model is successfully loaded.
     * This event will not be raised until the model and all of
     * its textures are loaded.
     * @param context   GVRContext used to load the model
     * @param model root of the scene graph representing the model.
     * @param filePath  File path or URL of the model.
     */
    public void onModelLoaded(GVRContext context, GVRSceneObject model, String filePath);
    
    /**
     * Called when a texture is successfully loaded.
     * @param context   GVRContext used to load the model
     * @param texture   GVRTexture created from loading the texture file.
     * @param filePath  File path or URL of the texture file.
     */
    public void onTextureLoaded(GVRContext context, GVRTexture texture, String filePath);
    
    /**
     * Called when a model cannot be loaded.
     * @param context   GVRContext used to load the model
     * @param error     error message
     * @param filePath  File path or URL of the model that failed to load.
     */
    public void onModelError(GVRContext context, String error, String filePath);
    
    /**
     * Called when a texture cannot be loaded.
     * @param context   GVRContext used to load the model
     * @param error     error message
     * @param filePath  File path or URL of the texture that failed to load.
     */
    public void onTextureError(GVRContext context, String error, String filePath);
}
