package org.game.cactusjump;

import java.io.IOException;

import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.handler.timer.ITimerCallback;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.JumpModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSCounter;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.game.cactusjump.LevelCompleteWindow.StarsCount;
import org.game.cactusjump.SceneManager.SceneType;

import org.xml.sax.Attributes;

public class GameScene extends BaseScene implements IOnSceneTouchListener 
{
	private HUD gameHUD;
	FPSCounter fpsCounter;
	private Text scoreText;
	private int score = 0;
	private PhysicsWorld physicsWorld;
	
	private float playerPosX = 0;
	
	
	//XML parsing
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	    
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN = "coin";
	
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE = "levelComplete";
    
	private Player boy;
	private boolean firstTouch = false;
	
	private Text gameOverText;
	private boolean gameOverDisplayed = false;
	
	private LevelCompleteWindow levelCompleteWindow;
	
    @Override
    public void createScene()
    {
    	createBackground();
        createHUD();
        createPhysics();
        loadLevel(1);
        setOnSceneTouchListener(this);
        createGameOverText();
        levelCompleteWindow = new LevelCompleteWindow(vbom);
    }

    @Override
    public void onBackKeyPressed()
    {
        SceneManager.getInstance().loadMenuScene(engine);
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene()
    {
        camera.setHUD(null);
        camera.setChaseEntity(null);
        // TODO code responsible for disposing scene
        // removing all game scene objects.
    }
    
    private void createBackground()
    {
    	ResourceManager rm = resourcesManager;
        setBackground(new Background(Color.BLUE));

		//creates background. Set color for something and speed.
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		
		//attach texture to background and object manager
		Sprite backSprite = new Sprite(MainActivity.LB_X, MainActivity.LB_Y, rm.mParallaxLayerBack, vbom);
		backSprite.setAnchorCenter(0, 0);
		backSprite.setSize(MainActivity.CAMERA_WIDTH, MainActivity.CAMERA_HEIGHT);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, backSprite));
		Sprite midSprite = new Sprite(MainActivity.LT_X, MainActivity.LT_Y - 200, rm.mParallaxLayerMid, vbom);
		midSprite.setAnchorCenter(0, 0);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, midSprite));
		Sprite frontSprite = new Sprite(MainActivity.LB_X, MainActivity.LB_Y, rm.mParallaxLayerFront, vbom);
		frontSprite.setAnchorCenter(0, 0);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, frontSprite));
		//set back
		this.setBackground(autoParallaxBackground);

		// Calculate the coordinates for the face, so its centered on the camera. 
		final float playerX = MainActivity.CR_X + playerPosX;
		final float playerY = 0;

		// Create two spritess and add it to the scene. 
		final AnimatedSprite player = new AnimatedSprite(playerX, playerY, rm.mPlayerTextureRegion, vbom);
		player.setScaleCenterY(rm.mPlayerTextureRegion.getHeight());
		player.setScale(4);
		player.setAnchorCenter(0, 0);
		player.animate(new long[]{400, 400, 400}, 3, 5, true);
		
		this.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback()
	    {
	        @Override
	        public void onTimePassed(final TimerHandler pTimerHandler)
	        {
	        	ResourceManager rm = resourcesManager;
	        	player.setPosition(MainActivity.CR_X + playerPosX, 0);
	        }
	    }));
		
		final AnimatedSprite enemy = new AnimatedSprite(playerX - 80, playerY, ResourceManager.getInstance().mEnemyTextureRegion, vbom);
		enemy.setScaleCenterY(rm.mEnemyTextureRegion.getHeight());
		enemy.setScale(2);
		enemy.setAnchorCenter(0, 0);
		enemy.animate(new long[]{200, 200, 200}, 3, 5, true);
		enemy.registerEntityModifier(new LoopEntityModifier(new JumpModifier(5, playerX - 80, playerX - 80, playerY, playerY + 160, 160)));
		final AnimatedSprite enemy2 = new AnimatedSprite(playerX - 160, playerY, rm.mEnemyTextureRegion, vbom);
		enemy2.setScaleCenterY(rm.mEnemyTextureRegion.getHeight());
		enemy2.setScale(2);
		enemy2.setAnchorCenter(0, 0);
		enemy2.animate(new long[]{200, 200, 200}, 3, 5, true);
		enemy2.registerEntityModifier(new LoopEntityModifier(new RotationModifier(5, 0, 360)));
		
		//camera.setChaseEntity(enemy);
		//camera.setBounds(0, 0, 2000, 2000);
		//camera.setBoundsEnabled(true);

		this.attachChild(player);
		this.attachChild(enemy);
		this.attachChild(enemy2);
		
		/*
		final Text centerText = new Text(100, 40, rm.font, "Hello AndEngine!\nYou can even have multilined text!", new TextOptions(HorizontalAlign.CENTER), vbom);
		final Text leftText = new Text(100, 170, rm.font, "Also left aligned!\nLorem ipsum dolor sit amat...", new TextOptions(HorizontalAlign.LEFT), vbom);
		final Text rightText = new Text(100, 300, rm.font, "And right aligned!\nLorem ipsum dolor sit amat...", new TextOptions(HorizontalAlign.RIGHT), vbom);

		this.attachChild(centerText);
		this.attachChild(leftText);
		this.attachChild(rightText);
		*/
		
	    
	    fpsCounter = new FPSCounter();
	    engine.registerUpdateHandler(fpsCounter);
	     
        /*
	    final Text fpsText2 = new Text(300, 200, rm.font, "FPS2:", "FPS: XXXXXXXXXXXXXXXXXXXXXX".length(), vbom);

	    this.attachChild(fpsText2);
	     

	    
	    this.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback()
	    {
	        @Override
	        public void onTimePassed(final TimerHandler pTimerHandler)
	        {
	            fpsText2.setText("FPS: " + fpsCounter.getFPS());
	        }
	    }));
	    */

        
    }
    
    private void createHUD()
    {
    	gameHUD = new HUD();
        
        // CREATE SCORE TEXT
        scoreText = new Text(100, 560, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
        scoreText.setAnchorCenter(0, 0);    
        scoreText.setText("Score: 0");
        gameHUD.attachChild(scoreText);
        

	    
		final Rectangle left = new Rectangle(60, 200, 60, 60, vbom)
	    {
	    	private boolean touched = false;
	    	
	        public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
	        {
	            if (touchEvent.isActionDown())
	            {
	            	touched = true;

	            }
	            if (touchEvent.isActionUp())
	            {
	            	touched = false;
	            }
	            return true;
	        };
	        
	        @Override
	    	protected void onManagedUpdate(float pSecondsElapsed)
	    	{
	    		if (touched)
	    		{
	            	Log.i("MainActivity", "Left pSecondsElapsed = " + pSecondsElapsed);
	            	playerPosX -= 20*pSecondsElapsed;
	    		}
	    		super.onManagedUpdate(pSecondsElapsed);
	    	}
	    };
	    
	    final Rectangle right = new Rectangle(camera.getWidth() - 120, 200, 60, 60, vbom)
	    {
	    	private boolean touched = false;
	    	
	        public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
	        {
	            if (touchEvent.isActionDown())
	            {
	            	touched = true;

	            }
	            if (touchEvent.isActionUp())
	            {
	            	touched = false;
	            }
	            return true;
	        };
	        
	        @Override
	    	protected void onManagedUpdate(float pSecondsElapsed)
	    	{
	    		if (touched)
	    		{
	            	Log.i("MainActivity", "Right pSecondsElapsed = " + pSecondsElapsed);
	            	playerPosX += 10*pSecondsElapsed;
	    		}
	    		super.onManagedUpdate(pSecondsElapsed);
	    	}
	    };
	    left.setColor(Color.BLUE);
	    left.setAnchorCenter(0, 0);  
	    right.setColor(Color.RED);
	    right.setAnchorCenter(0, 0);
	    
	    gameHUD.registerTouchArea(left);
	    gameHUD.registerTouchArea(right);
	    gameHUD.attachChild(left);
	    gameHUD.attachChild(right);
	    
	    final Text fpsText = new Text(100, 20, resourcesManager.font, "FPS:", "FPS: XXXXXXXXXXXXXXXXXXXXXX".length(), vbom);
	    fpsText.setAnchorCenter(0, 0);

	    gameHUD.attachChild(fpsText);
	    gameHUD.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback()
	    {
	        @Override
	        public void onTimePassed(final TimerHandler pTimerHandler)
	        {
	            fpsText.setText("FPS: " + fpsCounter.getFPS());
	        }
	    }));
        
        camera.setHUD(gameHUD);
    }
    
    private void addToScore(int i)
    {
        score += i;
        scoreText.setText("Score: " + score);
    }
    
    private void createPhysics()
    {
        physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false); 
        physicsWorld.setContactListener(contactListener());
        registerUpdateHandler(physicsWorld);
    }
    
    
    private void loadLevel(int levelID)
    {
        final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
        
        final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
            {
                final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
                final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
                
                // TODO later we will specify camera BOUNDS and create invisible walls
                // on the beginning and on the end of the level.
                ((BoundCamera) camera).setBounds(0, 0, width, height); // here we set camera bounds
                ((BoundCamera) camera).setBoundsEnabled(true);

                return GameScene.this;
            }
        });
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
            {
                final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
                final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
                final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
                
                final Sprite levelObject;
                
                if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
                {
                    levelObject = new Sprite(x, y, resourcesManager.platform1_region, vbom);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
                } 
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
                {
                    levelObject = new Sprite(x, y, resourcesManager.platform2_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("platform2");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3))
                {
                    levelObject = new Sprite(x, y, resourcesManager.platform3_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("platform3");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN))
                {
                    levelObject = new Sprite(x, y, resourcesManager.coin_region, vbom)
                    {
                        @Override
                        protected void onManagedUpdate(float pSecondsElapsed) 
                        {
                            super.onManagedUpdate(pSecondsElapsed);
                            
                            /** 
                             * TODO
                             * we will later check if player collide with this (coin)
                             * and if it does, we will increase score and hide coin
                             * it will be completed in next articles (after creating player code)
                             */
                            if (boy.collidesWith(this))
                            {
                                addToScore(10);
                                this.setVisible(false);
                                this.setIgnoreUpdate(true);
                            }
                        }
                    };
                    levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
                {
                    boy = new Player(x, y, vbom, camera, physicsWorld)
                    {
                        @Override
                        public void onDie()
                        {
                            // TODO Latter we will handle it.
                        	if (!gameOverDisplayed)
                            {
                        		displayGameOverText();
                            }
                        }
                    };
                    levelObject = boy;
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE))
                {
                    levelObject = new Sprite(x, y, resourcesManager.complete_stars_region, vbom)
                    {
                        @Override
                        protected void onManagedUpdate(float pSecondsElapsed) 
                        {
                            super.onManagedUpdate(pSecondsElapsed);

                            if (boy.collidesWith(this))
                            {
                            	if (score > 30) {
                            		levelCompleteWindow.display(StarsCount.THREE, GameScene.this, camera);
                                } else if (score >15) {
                                	levelCompleteWindow.display(StarsCount.TWO, GameScene.this, camera);
                                } else {
                                	levelCompleteWindow.display(StarsCount.ONE, GameScene.this, camera);
                                }
                                this.setVisible(false);
                                this.setIgnoreUpdate(true);
                            }
                        }
                    };
                    levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
                }
                else
                {
                    throw new IllegalArgumentException();
                }

                levelObject.setCullingEnabled(true);

                return levelObject;
            }
        });

        levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
    }

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionDown())
	    {
			if (pSceneTouchEvent.isActionDown())
		    {
		        if (!firstTouch)
		        {
		            boy.setRunning();
		            firstTouch = true;
		        }
		        else
		        {
		            boy.jump();
		        }
		    }
	    }
		return false;
	}
	
	private void createGameOverText()
	{
	    gameOverText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);
	}

	private void displayGameOverText()
	{
	    camera.setChaseEntity(null);
	    gameOverText.setPosition(camera.getCenterX(), camera.getCenterY());
	    attachChild(gameOverText);
	    gameOverDisplayed = true;
	}
	
	private ContactListener contactListener()
	{
	    ContactListener contactListener = new ContactListener()
	    {
	        public void beginContact(Contact contact)
	        {
	            final Fixture x1 = contact.getFixtureA();
	            final Fixture x2 = contact.getFixtureB();

	            if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
	            {
	                if (x2.getBody().getUserData().equals("player"))
	                {
	                    boy.increaseFootContacts();
	                }
	            }
	            
	            if (x1.getBody().getUserData().equals("platform3") && x2.getBody().getUserData().equals("player"))
	            {
	            	//this is not update thread and you cannot edit sprite
	            	//x1.getBody().setType(BodyType.DynamicBody);
	            	//you can use runOnUpdateThread(Runnable runnable); instead engine.registerUpdateHandler
	                engine.registerUpdateHandler(new TimerHandler(0.6f, new ITimerCallback()
	                {                                    
	                    public void onTimePassed(final TimerHandler pTimerHandler)
	                    {
	                    	//this is update thread
	                    	Log.i("GameScene", "Platform 3 down now");
	                        pTimerHandler.reset();
	                        Log.i("GameScene", "Platform 3 time handler killed");
	                        engine.unregisterUpdateHandler(pTimerHandler);
	                        Log.i("GameScene", "Platform 3 update handler unregistered");
	                        Log.i("GameScene", "Platform 3 down now");
	    	                x1.getBody().setType(BodyType.DynamicBody);
	    	                Log.i("GameScene", "Platform 3 is dynamic");
	                    }
	                }));
	            }
	            
	            if (x1.getBody().getUserData().equals("platform2") && x2.getBody().getUserData().equals("player"))
	            {
	            	Log.i("GameScene", "Platform 2 down after 0.2f");
	                engine.registerUpdateHandler(new TimerHandler(1.2f, new ITimerCallback()
	                {                                    
	                    public void onTimePassed(final TimerHandler pTimerHandler)
	                    {
	                    	Log.i("GameScene", "Platform 2 down now");
	                        pTimerHandler.reset();
	                        Log.i("GameScene", "Platform 2 time handler killed");
	                        engine.unregisterUpdateHandler(pTimerHandler);
	                        Log.i("GameScene", "Platform 2 update handler unregistered");
	                        x1.getBody().setType(BodyType.DynamicBody);
	                        Log.i("GameScene", "Platform 2 is dynamic");
	                    }
	                }));
	            }
	        }

	        public void endContact(Contact contact)
	        {
	            final Fixture x1 = contact.getFixtureA();
	            final Fixture x2 = contact.getFixtureB();

	            if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
	            {
	                if (x2.getBody().getUserData().equals("player"))
	                {
	                    boy.decreaseFootContacts();
	                }
	            }
	        }


			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub
				
			}
	    };
	    return contactListener;
	}

    
}


