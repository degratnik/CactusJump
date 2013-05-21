package org.game.cactusjump;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;



/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class Player extends AnimatedSprite
{
	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------
	private boolean canRun = false;
	private Body body;
	private int footContacts = 0;
    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------
    
    public Player(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld)
    {
        super(pX, pY, ResourceManager.getInstance().boy_region, vbo);
        createPhysics(camera, physicsWorld);
        camera.setChaseEntity(this);
    }
    
    public abstract void onDie();
    
    private void createPhysics(final Camera camera, PhysicsWorld physicsWorld)
    {        
        body = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0, 0));

        body.setUserData("player");
        body.setFixedRotation(true);
        
        physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, false)
        {
            @Override
            public void onUpdate(float pSecondsElapsed)
            {
                super.onUpdate(pSecondsElapsed);
                camera.onUpdate(0.1f);
                
                if (getY() <= 0)
                {                    
                    onDie();
                }
                
                if (canRun)
                {    
                    body.setLinearVelocity(new Vector2(3, body.getLinearVelocity().y)); 
                }
            }
        });
    }
    
    public void setRunning()
    {
        canRun = true;
            
        final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
            
        animate(PLAYER_ANIMATE, 0, 2, true);
    }
    
    public void setStoped()
    {
        canRun = false;
            
        final long[] PLAYER_ANIMATE = new long[] { 100 };
            
        animate(PLAYER_ANIMATE, 0, 0, true);
    }
    
    public void jump()
    {
    	if (footContacts < 1) 
        {
            return; 
        }
        body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, 14)); 
    }
    
    public void increaseFootContacts()
    {
        footContacts++;
    }

    public void decreaseFootContacts()
    {
        footContacts--;
    }
}