package org.game.cactusjump;

import java.io.IOException;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.JumpModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSCounter;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import org.game.cactusjump.SceneManager.SceneType;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
/**
 * (c) 2013 degratnik
 *
 * @author Oleksandr Reshetnik
 * @since 08.05.2013
 */
public class MainActivity extends SimpleBaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================
    private final static int FPS = 60;
	
	// ===========================================================
	// Fields
	// ===========================================================
    
    private ResourceManager resourcesManager;
    
	static int CAMERA_WIDTH = 1000;
	static int CAMERA_HEIGHT = 600;
	/**
	 * Center X
	 */
	static int CR_X = CAMERA_WIDTH/2;
	/**
	 * Center Y
	 */
	static int CR_Y = CAMERA_HEIGHT/2;
	
	/**
	 * Left-Bottom X
	 */
	static int LB_X = 0;
	/**
	 * Left-Bottom Y
	 */
	static int LB_Y = 0;
	/**
	 * Left-Top X
	 */
	static int LT_X = 0;
	/**
	 * Left-Top Y
	 */
	static int LT_Y = CAMERA_HEIGHT;
	
	/**
	 * Right-Bottom X
	 */
	static int RB_X = CAMERA_WIDTH;
	/**
	 * Right-Bottom Y
	 */
	static int RB_Y = 0;
	/**
	 * Right-Top X
	 */
	static int RT_X = CAMERA_WIDTH;
	/**
	 * Right-Top Y
	 */
	static int RT_Y = CAMERA_HEIGHT;
	
	private BoundCamera camera;
	//slash
	private Scene splashScene;
	private BitmapTextureAtlas splashTextureAtlas;
	private ITextureRegion splashTextureRegion;
	private Sprite splash;
	//main
	private Scene mainScene;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mPlayerTextureRegion;
	private TiledTextureRegion mEnemyTextureRegion;

	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;

	private ITextureRegion mParallaxLayerBack;
	private ITextureRegion mParallaxLayerMid;
	private ITextureRegion mParallaxLayerFront;
	private Font mFont;
	
	private float playerPosX = 0;
		

	
	
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	
	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) 
	{
		/*
	    We want to make our game work at similar speeds on various devices, we will use the LimitedFPSEngine class to do it.
		*/
		Log.i("MainActivity", String.format("Create Engine with limited FPS = %d", FPS));
	    return new LimitedFPSEngine(pEngineOptions, FPS);
	}
	

	@SuppressLint("NewApi")
	@Override
	//Is called when engine is loaded.
	public EngineOptions onCreateEngineOptions() {
		//Get screen size.
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		//real size values in not important ration is important
		//engine will render in will screen but you world dimensions will be defined there
		//I will set world width as 1000 and
		//get screen ratio and set height
		//CAMERA_WIDTH = 1000;
		CAMERA_HEIGHT = CAMERA_WIDTH*size.y/size.x;
		CR_Y = CAMERA_HEIGHT/2;
		LT_Y = CAMERA_HEIGHT;
		RT_Y = CAMERA_HEIGHT;
		//Creates camera for game on full screen. We set start point (x,y) and size
		//form camera start point depens player position in next method
		Log.i("MainActivity", String.format("Create camera %d x %d", CAMERA_WIDTH, CAMERA_HEIGHT));
		camera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		//Creates options: camera, orientation, fullscreeen
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		//the picture wil be better
		engineOptions.getRenderOptions().setDithering(true);
		engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
	    engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
		return engineOptions;
	}

	@Override
	public void onCreateResources() {
		Log.i("MainActivity", "initialize resource manager");
		ResourceManager.prepareManager(mEngine, this, camera, getVertexBufferObjectManager());
	    resourcesManager = ResourceManager.getInstance();
		/*
		//Loads textures from assets/gfx
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		//splash
		splashTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
		splashTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.splashTextureAtlas, this, "parallax_background_layer_back.png", 0, 188);
		splashTextureAtlas.load();
		Log.i("MainActivity", "onCreateResources - finished");
		*/
	}
	
	public void loadResources() {
		/*
		//Loads textures from assets/gfx
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	
		//Texture atlas creation
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
		//Factory creates region. player.png is 72 x 128 and has 12 tiles 3 x 4. Tile size 24x32. We set start point (0,0) and 3 columns and 4 rows.
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "player.png", 0, 0, 3, 4);
		this.mEnemyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "enemy.png", 73, 0, 3, 4);
		//atlas is loaded after creation
		this.mBitmapTextureAtlas.load();

		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_front.png", 0, 0);
		this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_back.png", 0, 188);
		this.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_mid.png", 0, 669);
		this.mAutoParallaxBackgroundTexture.load();
		
		this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, Color.WHITE);
		this.mFont.load();
		*/
	}

	private void initSplashScene()
	{
		/*
	    splashScene = new Scene();
	    final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
	    splash = new Sprite(0, 0, splashTextureRegion, vertexBufferObjectManager)
	    {
	        @Override
	        protected void preDraw(GLState pGLState, Camera pCamera)
	        {
	            super.preDraw(pGLState, pCamera);
	            pGLState.enableDither();
	        }
	    };

	    splash.setScale(1.5f);
	    splash.setPosition((CAMERA_WIDTH - splash.getWidth()) * 0.5f, (CAMERA_HEIGHT - splash.getHeight()) * 0.5f);
	    splashScene.attachChild(splash);
	    */
	}
	
	private void initMainScene() {
		/*
		//creates scene
		mainScene = new Scene();
		//creates background. Set color for something and speed.
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		//creates object manager
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		//attach texture to background and object manager
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerMid, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
		//set back
		mainScene.setBackground(autoParallaxBackground);

		// Calculate the coordinates for the face, so its centered on the camera. 
		final float playerX = (CAMERA_WIDTH - this.mPlayerTextureRegion.getWidth()) / 2 + playerPosX;
		final float playerY = CAMERA_HEIGHT - this.mPlayerTextureRegion.getHeight() - 5;
		Log.i("MainActivity", String.format("Player x = %f y = %f", playerX, playerY));
		// Create two spritess and add it to the scene. 
		final AnimatedSprite player = new AnimatedSprite(playerX, playerY, this.mPlayerTextureRegion, vertexBufferObjectManager);
		player.setScaleCenterY(this.mPlayerTextureRegion.getHeight());
		player.setScale(4);
		player.animate(new long[]{400, 400, 400}, 3, 5, true);
		
		mainScene.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback()
	    {
	        @Override
	        public void onTimePassed(final TimerHandler pTimerHandler)
	        {
	        	player.setPosition((CAMERA_WIDTH - mPlayerTextureRegion.getWidth()) / 2 + playerPosX, CAMERA_HEIGHT - mPlayerTextureRegion.getHeight() - 5);
	        }
	    }));
		
		final AnimatedSprite enemy = new AnimatedSprite(playerX - 80, playerY, this.mEnemyTextureRegion, vertexBufferObjectManager);
		enemy.setScaleCenterY(this.mEnemyTextureRegion.getHeight());
		enemy.setScale(2);
		enemy.animate(new long[]{200, 200, 200}, 3, 5, true);
		enemy.registerEntityModifier(new LoopEntityModifier(new JumpModifier(5, playerX - 80, playerX - 80, playerY, playerY + 160, 160)));
		final AnimatedSprite enemy2 = new AnimatedSprite(playerX - 160, playerY, this.mEnemyTextureRegion, vertexBufferObjectManager);
		enemy2.setScaleCenterY(this.mEnemyTextureRegion.getHeight());
		enemy2.setScale(2);
		enemy2.animate(new long[]{200, 200, 200}, 3, 5, true);
		enemy2.registerEntityModifier(new LoopEntityModifier(new RotationModifier(5, 0, 360)));
		
		camera.setChaseEntity(enemy);
		camera.setBounds(0, 0, 2000, 2000);
		camera.setBoundsEnabled(true);

		mainScene.attachChild(player);
		mainScene.attachChild(enemy);
		mainScene.attachChild(enemy2);
		
		
		final Text centerText = new Text(100, 40, this.mFont, "Hello AndEngine!\nYou can even have multilined text!", new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
		final Text leftText = new Text(100, 170, this.mFont, "Also left aligned!\nLorem ipsum dolor sit amat...", new TextOptions(HorizontalAlign.LEFT), vertexBufferObjectManager);
		final Text rightText = new Text(100, 300, this.mFont, "And right aligned!\nLorem ipsum dolor sit amat...", new TextOptions(HorizontalAlign.RIGHT), vertexBufferObjectManager);

		mainScene.attachChild(centerText);
		mainScene.attachChild(leftText);
		mainScene.attachChild(rightText);
		
		HUD yourHud = new HUD();
	    
		final Rectangle left = new Rectangle(80, 300, 60, 60, vertexBufferObjectManager)
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
	    
	    final Rectangle right = new Rectangle(CAMERA_WIDTH - 80, 300, 60, 60, vertexBufferObjectManager)
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
	    
	    yourHud.registerTouchArea(left);
	    yourHud.registerTouchArea(right);
	    yourHud.attachChild(left);
	    yourHud.attachChild(right);
	    
	    final FPSCounter fpsCounter = new FPSCounter();
	    this.mEngine.registerUpdateHandler(fpsCounter);
	     
	    final Text fpsText = new Text(10, 10, this.mFont, "FPS:", "FPS: XXXXXXXXXXXXXXXXXXXXXX".length(),vertexBufferObjectManager);
	    final Text fpsText2 = new Text(10, 200, this.mFont, "FPS2:", "FPS: XXXXXXXXXXXXXXXXXXXXXX".length(),vertexBufferObjectManager);
	    yourHud.attachChild(fpsText); 
	    mainScene.attachChild(fpsText2);
	     
	    yourHud.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback()
	    {
	        @Override
	        public void onTimePassed(final TimerHandler pTimerHandler)
	        {
	            fpsText.setText("FPS: " + fpsCounter.getFPS());
	        }
	    }));
	    
	    mainScene.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback()
	    {
	        @Override
	        public void onTimePassed(final TimerHandler pTimerHandler)
	        {
	            fpsText2.setText("FPS: " + fpsCounter.getFPS());
	        }
	    }));
	    
	    camera.setHUD(yourHud);
	    */
	}
	/*
	@Override
	public Scene onCreateScene() {
				Log.i("MainActivity", "onCreateScene");
		//log fps values
		this.mEngine.registerUpdateHandler(new FPSLogger());
		initSplashScene();
		
		mEngine.registerUpdateHandler(new TimerHandler(3f, new ITimerCallback() 
		{
		    public void onTimePassed(final TimerHandler pTimerHandler) 
		    {
		        mEngine.unregisterUpdateHandler(pTimerHandler);
		        loadResources();
		        initMainScene();        
		        splash.detachSelf();
		        mEngine.setScene(mainScene);
		        Log.i("MainActivity", "Main show");
		    }
		}));
		Log.i("MainActivity", "Splash show");
		Log.i("MainActivity", "onCreateScene - finished");
		return splashScene;
	}
	*/
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
/*
	public void onPopulateScene(Scene pScene) throws IOException
	{
	    mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() 
	    {
	            public void onTimePassed(final TimerHandler pTimerHandler) 
	            {
	                mEngine.unregisterUpdateHandler(pTimerHandler);
	                // load menu resources, create menu scene
	                // set menu scene using scene manager
	                // disposeSplashScene();
	                // READ NEXT ARTICLE FOR THIS PART.
	                SceneManager.getInstance().createMenuScene();
	            }
	    }));

	}*/
	
	@Override
	protected void onDestroy()
	{
	    super.onDestroy();
	        
	    if (this.isGameLoaded())
	    {
	        System.exit(0);    
	    }
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{  
	    if (keyCode == KeyEvent.KEYCODE_BACK)
	    {
	        SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
	    }
	    return false; 
	}


	@Override
	protected Scene onCreateScene() {
		Log.i("MainActivity", "Create and set splash scene");
		SceneManager.getInstance().createSplashScene();
		SceneManager.getInstance().setScene(SceneType.SCENE_SPLASH);
		 mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() 
		    {
		            public void onTimePassed(final TimerHandler pTimerHandler) 
		            {
		                mEngine.unregisterUpdateHandler(pTimerHandler);
		                // load menu resources, create menu scene
		                // set menu scene using scene manager
		                // disposeSplashScene();
		                // READ NEXT ARTICLE FOR THIS PART.
		                SceneManager.getInstance().createMenuScene();
		                Log.i("MainActivity", "Create and set menu scene");
		            }
		    }));
		return SceneManager.getInstance().getCurrentScene();
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
