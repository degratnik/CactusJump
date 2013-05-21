package org.game.cactusjump;

import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.util.adt.color.Color;



import org.game.cactusjump.SceneManager.SceneType;



public class LoadingScene extends BaseScene
{
    @Override
    public void createScene()
    {
    	setBackground(new Background(Color.WHITE));
        attachChild(new Text(800, 560, resourcesManager.font, "Loading...", vbom));
        //attachChild(new Text(400, 240, resourcesManager.font, "400,240", vbom));
    }

    @Override
    public void onBackKeyPressed()
    {
        return;
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_LOADING;
    }

    @Override
    public void disposeScene()
    {

    }
}