package ie.itcarlow.box2ddemo;


import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.BaseGameActivity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;


public class Box2DSpriteCollisions extends BaseGameActivity implements IUpdateHandler {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mTextureAustrianBear;
	private BitmapTextureAtlas mTexturePiglet;
	private ITextureRegion mAustrianBearTextureRegion;
	private ITextureRegion mPigletTextureRegion;
	private Scene mScene;
	private Sprite mPiglet;	
	private boolean destroyPiglet = false;

	private PhysicsWorld mPhysicsWorld;
	
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
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

    @Override
	public void onCreateResources(
       OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {

    	 loadGfx();
		 pOnCreateResourcesCallback.onCreateResourcesFinished();

    }

    private void loadGfx() {     
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");  
        mTextureAustrianBear = new BitmapTextureAtlas(getTextureManager(), 46, 54);  
        mAustrianBearTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAustrianBear, this, "austrian_bear.png", 0, 0);
        mTextureAustrianBear.load();
        
        mTexturePiglet = new BitmapTextureAtlas(getTextureManager(), 46, 54);  
        mPigletTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexturePiglet, this, "piglet.png", 0, 0);
        mTexturePiglet.load();
    }

    @Override
  	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
  			throws Exception {
    	
  		this.mScene = new Scene();
  		this.mScene.setBackground(new Background(0, 125, 58));
  	    pOnCreateSceneCallback.onCreateSceneFinished(this.mScene);  		
  	}


    @Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
    	setUpBox2DWorld();
    	mPhysicsWorld.setContactListener(createContactListener());
    	
    	// Setup coordinates for the sprite in order that it will
    	//  be centered on the camera.
    	final float centerX = (CAMERA_WIDTH - this.mAustrianBearTextureRegion.getWidth()) / 2;
    	final float centerY = (CAMERA_HEIGHT - this.mAustrianBearTextureRegion.getHeight()) / 2;
 
    	// Create the austrian bear and add it to the scene.
    	final Sprite austrianBear = new Sprite(centerX, centerY, this.mAustrianBearTextureRegion, this.getVertexBufferObjectManager())
    	{
           @Override
           public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
                                        final float pTouchAreaLocalX,
                                        final float pTouchAreaLocalY) {
               //setBodyPosition(this, pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
               //Body pigBody = (Body) mPiglet.getUserData();
               //applyLinearImpulse(2, pigBody.getPosition()));
        	   Body bearBody = (Body) this.getUserData();
        	   bearBody.applyForce(-500, -500, bearBody.getPosition().x, bearBody.getPosition().y);
        	   //float distance = (float) Math.sqrt(  (Math.pow(bearBody.getPosition().x - pigBody.getPosition().x, 2))  + 
        	   //											(Math.pow(bearBody.getPosition().y - pigBody.getPosition().y, 2)) );
               //Vector2 direction = ((bearBody.getPosition() - pigBody.getPosition())/distance);
        	   return true;
           }
    	};
	   
    	mPiglet = new Sprite(centerX - 100, centerY - 100,  mPigletTextureRegion, this.mEngine.getVertexBufferObjectManager());
    	createPhysicsBodies(austrianBear);  
    	
    	// The bear sprite (unlike the piglet sprite) is a local variable, 
    	//  so it must be passed to method createPhysicsBodies
    	this.mEngine.registerUpdateHandler(this);
    	pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

	// ===========================================================
	// Methods
	// ===========================================================

    private void setUpBox2DWorld() {
    	final Vector2 v = Vector2Pool.obtain(0, 0);
    	mPhysicsWorld = new PhysicsWorld(v, false);
    	Vector2Pool.recycle(v);
    	this.mScene.registerUpdateHandler(mPhysicsWorld);
    }
    
    private void createPhysicsBodies(final Sprite austrianBear) {
    	final FixtureDef fixDef = PhysicsFactory.createFixtureDef(1.5f,0.45f, 0.3f);
    	
    	Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, austrianBear, BodyType.DynamicBody, fixDef);
    	body.setUserData("austrianBear");
    	austrianBear.setUserData(body);
    	
    	Body body2 = PhysicsFactory.createBoxBody(mPhysicsWorld, mPiglet, BodyType.DynamicBody, fixDef);
    	body2.setUserData("piglet");
    	mPiglet.setUserData(body2);
    	
    	mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mPiglet, body2, true, true));
    	mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(austrianBear, body, true, true));
    	
    	mScene.attachChild(austrianBear);
    	mScene.attachChild(mPiglet);	 
    	mScene.registerTouchArea(austrianBear);
    	
    }
    
    /*
     * Helper method that translates the associated physics body to the specified coordinates.
     * 
	 * @param pX The desired x coordinate for this sprite.
	 * @param pY The desired y coordinate for this sprite.
     */
    private void setBodyPosition(final Sprite sprite, final float pX, final float pY) {
    	
    	final Body body = (Body) sprite.getUserData();
        final float widthD2 = sprite.getWidth() / 2;
        final float heightD2 = sprite.getHeight() / 2;
        final float angle = body.getAngle(); // keeps the body angle       
        final Vector2 v2 = Vector2Pool.obtain((pX + widthD2) / 
        					PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, (pY + heightD2) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
        body.setTransform(v2, angle);
        Vector2Pool.recycle(v2);
    }

    private ContactListener createContactListener() {
        ContactListener levelContactListener = new ContactListener() {
        	
        	@Override
        	public void beginContact(Contact contact) {
        		Body body = contact.getFixtureA().getBody();
        		if(body.getUserData().equals("austrianBear")) {
        			//mPhysicsWorld.destroyBody((Body)mPiglet.getUserData());
        			destroyPiglet = true;
        		} 
        	}
        	
        	@Override
        	public void endContact(Contact contact) {            
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
        return levelContactListener;
    }

	@Override
	public void onUpdate(float pSecondsElapsed) {
		if (destroyPiglet){
    		this.mEngine.runOnUpdateThread(new Runnable(){
			public void run() {
					// Find the physics connector associated with the sprite mPiglet
					PhysicsConnector physicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(mPiglet);
					// Unregister the physics connector
					mPhysicsWorld.unregisterPhysicsConnector(physicsConnector);
					// Destroy the body
					mPhysicsWorld.destroyBody(physicsConnector.getBody());
					destroyPiglet = false;
				}
			});
    	}
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	    
    // ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
    
}
