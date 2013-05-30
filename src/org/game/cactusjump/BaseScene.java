package org.game.cactusjump;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.game.cactusjump.SceneManager.SceneType;

import android.app.Activity;

public abstract class BaseScene extends Scene
{
    //global variables that are useful for any scene. Will be initialized in constructor.
    protected Engine engine;
    protected Activity activity;
    protected ResourceManager resourcesManager;
    protected VertexBufferObjectManager vbom;
    protected Camera camera;
    
    /**
     * Constructor. Init variables and call createScene.
     */
    public BaseScene()
    {
        this.resourcesManager = ResourceManager.getInstance();
        this.engine = resourcesManager.engine;
        this.activity = resourcesManager.activity;
        this.vbom = resourcesManager.vbom;
        this.camera = resourcesManager.camera;
        createScene();
    }  

    /**
     * Create scene.
     */
    public abstract void createScene();
    /**
     * Called if back is pressed.
     */
    public abstract void onBackKeyPressed();
    /**
     * Return scene type.
     * @return SceneType
     */
    public abstract SceneType getSceneType();
    /**
     * Destroy scene.
     */
    public abstract void disposeScene();
}
