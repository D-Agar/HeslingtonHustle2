package com.heshus.game.engine;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.heshus.game.entities.Player;
import com.heshus.game.manager.ActivityManager;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.audio.Sound;

import com.heshus.game.screens.states.GameOverScreen;
import com.heshus.game.screens.states.PauseMenu;
import com.heshus.game.screens.states.SettingsMenu;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class Play implements Screen {

    public static final int GAME_RUNNING = 0;
    public static final int GAME_PAUSED = 1;
    public static final int GAME_SETTINGS = 2;
    public static final int GAME_OVER = 3;
    public static final int GAME_MAINMENU = 4;
    public static final int GAME_LEADERBOARD = 5;

    public static int state;
    private final HesHusGame game;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Player player;
    private static BitmapFont font;
    private TiledMapTileLayer collisionLayer;
    private ActivityManager activityManager;
    private float volume = 0.5f;

    private Sprite blankTexture, textBubble, dimTexture;
    private Texture TblankTexture, textBubbleTexture;
    private ExtendViewport extendViewport;

    private PauseMenu pauseMenu;
    private SettingsMenu settingsMenu;

    private Texture counterBoxTexture;
    private Texture verticalBarTexture;
    private Sprite verticalBarSprite;

    private float bubbleTimer, bubblePeriod = 3;

    // Walking sounds
    private Sound walkingSound1;
    private Sound walkingSound2;
    private Sound walkingSound3;
    private Sound walkingSound4;
    private int currentWalkingSoundIndex = 0;
    private boolean isWalking = false;

    private float walkingSoundTimer = 0;

    // 1/4th of a second delay between sounds, because our avatar is running everywhere
    private final float WALKING_SOUND_DELAY = 0.25f;
    private Music backgroundMusic;

    private Texture increaseVolumeTexture;
    private Texture lowerVolumeTexture;
    private Texture volumeOffTexture;
    private Texture volumeOnTexture;
    private Stage stage;

    private Sprite moonSprite;
    private Texture moonTexture;

    private Sound clickSound;
    private Texture playerTexture;
    private InputMultiplexer inputMultiplexer;
    private Button increaseVolumeButton;
    private Button lowerVolumeButton;
    private Button volumeOffButton;
    private Button volumeOnButton;





    /**
     * The Play class represents the game screen and manages some of the states functionalities
     * as well as handling user input and game sprites
     * It's also responsible for drawing the game world, the player character,
     * and the UI elements like the pause and settings menus. It also handles sound effects
     * and music playback based on game events.
     * creates new instance of the Play screen with specific game and player sprite settings.
     * @param playerSpriteSelection The texture for the player sprite to be used in the game.* Create the Play instance
     * @param game the game instance
     * @param playerSpriteSelection the texture which will be used as the player sprite
     */
    public Play(HesHusGame game, Texture playerSpriteSelection) {
        this.game = game;
        this.playerTexture = playerSpriteSelection;
    }
    /**
     * Renders the game world, player, and UI elements. This method is called every frame.
     *
     * @param delta The time in seconds since the last render call.
     */
    @Override
    public void render(float delta) {
        update();

        draw();
    }

    /**
     * Draws the game world, including the map, player, and HUD elements.
     */
    private void draw(){
        ScreenUtils.clear(0,0,0,1);
        //CAMERA
        int cameraSmoothness = 4; //higher looks smoother! makes it take longer for camera to reach player pos
        camera.position.set(((player.getX() + player.getWidth() / 2)+(camera.position.x *(cameraSmoothness-1)))/cameraSmoothness, ((player.getY() + player.getHeight() / 2)+(camera.position.y *(cameraSmoothness-1)))/cameraSmoothness, 0);
        lockCameraInTiledMapLayer(camera,(TiledMapTileLayer) map.getLayers().get(2)); //locks camera position so it cannot show out of bounds
        camera.position.set(Math.round(camera.position.x) ,Math.round(camera.position.y),0);//This is needed to stop black lines between tiles. I think something to do with the tilemaprenderer and floats causes this
        camera.viewportWidth = Math.round(camera.viewportWidth);
        camera.viewportHeight = Math.round(camera.viewportHeight);
        camera.update();


        float padding = 10; // Adjust padding as needed
        float buttonSize = 50; // The size of the buttons, adjust as needed
        // Calculate the positions based on the updated camera position
        float baseX = camera.position.x + camera.viewportWidth / 2 - buttonSize - padding;
        float baseY = camera.position.y + camera.viewportHeight / 2 - padding - buttonSize;

        // Set the position for each volume button to always stay in one place on screen

        increaseVolumeButton.setPosition(baseX - 2 * buttonSize, baseY);
        lowerVolumeButton.setPosition(baseX - 3 * buttonSize, baseY);
        volumeOffButton.setPosition(baseX - buttonSize, baseY);
        volumeOnButton.setPosition(baseX, baseY);

        //Tilemap
        renderer.setView(camera);
       //takes a layers[] argument if we want to specifically render certain layers
        renderer.render(); 
        renderer.getBatch().begin();
        //Player
        player.draw(renderer.getBatch());


        switch (state) {
            case(GAME_RUNNING):
                activityManager.checkActivity();
                //HUD
                //Drawing energy bar, can be replaced for a standard energy bar with comments
                renderer.getBatch().setColor(Color.GRAY);

                renderer.getBatch().draw(blankTexture, (camera.position.x - camera.viewportWidth/2), (camera.position.y - camera.viewportHeight/2), camera.viewportWidth, 14);
                //renderer.getBatch().draw(blankTexture, (camera.position.x - camera.viewportWidth/2) + 3, (camera.position.y - camera.viewportHeight/2) + 3, 204, 44);
                renderer.getBatch().setColor(Color.YELLOW);
                renderer.getBatch().draw(blankTexture, (camera.position.x - camera.viewportWidth/2), (camera.position.y - camera.viewportHeight/2), camera.viewportWidth * ((float) game.dayManager.getEnergy() /100), 12);
                //renderer.getBatch().draw(blankTexture, (camera.position.x - camera.viewportWidth/2) + 5, (camera.position.y - camera.viewportHeight/2) + 5, 200 * ((float) DayManager.currentDay.getEnergy() /100), 40);

                renderer.getBatch().setColor(Color.WHITE);

                //Draw activity text
                if(!activityManager.getText().isEmpty()){
                    //logic for drawing bubble
                    font.getData().setScale(1f);
                    //Get text length
                    GlyphLayout layout = new GlyphLayout();
                    layout.setText(font, activityManager.getText());
                    renderer.getBatch().draw(textBubble, activityManager.getTextPosition().x - 2, activityManager.getTextPosition().y, layout.width + 4, 50);
                    activityManager.drawTextBubble((SpriteBatch) renderer.getBatch(), font);
                    font.getData().setScale(2f);
                    //changes timer
                    bubbleTimer += Gdx.graphics.getDeltaTime();
                    //removes bubble after timer ends
                    if(bubbleTimer > bubblePeriod){
                        bubbleTimer -= bubblePeriod;
                        activityManager.setText("", 0, 0);
                    }


                }

                //Dims screen when energy lost
                dimTexture.setAlpha((float)0.4 + game.dayManager.getEnergy());
                dimTexture.draw(renderer.getBatch());


                /**
                 * The counterbox setup, it stays in the upper left corner and allows through
                 * using the LibGDX BitMapFont class to increment written integers over the white
                 * counter box on the screen
                 */

                ///////////////////////////////////////////////////////////////////////////
                // The Counter and Counter Icons                                         //
                // Upper-left corner position for the counter box set and will not move //
                float counterBoxX = camera.position.x - camera.viewportWidth / 2;
                float counterBoxY = (camera.position.y + camera.viewportHeight / 2) - counterBoxTexture.getHeight();
                // drawing the counterbox texture
                renderer.getBatch().draw(counterBoxTexture, counterBoxX, counterBoxY);

                // help space and structure the strings on top of the box

                float iconSize = 20;
                float iconSpacingX = 2;
                float iconSpacingY = 8;
                // this is the verticalbar, that represents a day and increments to 7 during the game
                float verticalBarStartX = counterBoxX + iconSpacingX + 24;
                float verticalBarStartY = counterBoxY + counterBoxTexture.getHeight() - iconSpacingY - iconSize - iconSpacingY + 13;

                // setting up the font size and colour
                font.getData().setScale(1f);
                font.setColor(Color.BLACK);

                // Defining the Y position for each row
                float firstRowY = counterBoxY + counterBoxTexture.getHeight() - iconSpacingY - iconSize - 20;
                float secondRowY = firstRowY - iconSize - iconSpacingY;
                float thirdRowY = secondRowY - iconSize - iconSpacingY;

                // draw the player's score for the three activity types

                font.draw(renderer.getBatch(), String.valueOf(game.dayManager.overallEatCount), counterBoxX + 43, firstRowY+18);
                font.draw(renderer.getBatch(), String.valueOf(game.dayManager.overallStudyCount), counterBoxX + 43, secondRowY+27);
                font.draw(renderer.getBatch(), String.valueOf(game.dayManager.overallRecreationalCount), counterBoxX + 43, thirdRowY+36);

                // Draw the Day icon in the first row
                for (int i = 0; i < game.dayManager.getDayNumber(); i++) {
                    renderer.getBatch().draw(verticalBarSprite, verticalBarStartX+15 + (5 + iconSpacingX) * i, verticalBarStartY, 5, 20);
                }
                //End of main renderer
                renderer.getBatch().end();
                //Draw sound buttons. (currently can't have buttons get input while other menus have input)
                stage.act(Gdx.graphics.getDeltaTime());
                stage.draw();
                break;
            case (GAME_PAUSED):
                //Dims screen when energy lost
                dimTexture.setAlpha((float)0.4 + game.dayManager.currentDay.getEnergy());
                dimTexture.draw(renderer.getBatch());
   
                //Pause menu
                renderer.getBatch().end();
                pauseMenu.update(camera);
                pauseMenu.draw();
                break;
            case (GAME_SETTINGS):
                //Dims screen when energy lost
                dimTexture.setAlpha((float)0.4 + game.dayManager.getEnergy());
                dimTexture.draw(renderer.getBatch());

                //Settings menu
                renderer.getBatch().end();
                settingsMenu.update();
                break;
        }

        // updates and draws the stage
        // the stage is a container for all the actors
        //coordinates user inputs
        /**
         * Updates the {@link Stage} with the time passed since the last frame and draws all {@link Actor}s added to it.
         * This method should be called every frame to ensure the {@link Stage} accurately represents the current game state,
         * including processing any input events and executing any actions on {@link Actor}s.
         *
         * @param delta The time in seconds since the last frame, used to update animations and process input.
         */


    }
    /**
     * the method should be called every frame, updates the game state
     * checks for the player's movement sounds
     *  handles movement and game over scenario
     *  as parameter it has time delta that is time in seconds
     *  since last frame
     */
    private void update(){
        //Detect if game should be paused or not
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)||Gdx.input.isKeyJustPressed(Input.Keys.P)){
            if (state!=GAME_PAUSED) {
                state = GAME_PAUSED;
            }
            else{
                state = GAME_RUNNING;
            }
        }
        //logic/physics - anything that moves
        switch (state){
            case (GAME_RUNNING):
                Gdx.input.setInputProcessor(inputMultiplexer);
                player.update(Gdx.graphics.getDeltaTime());
                break;
            case (GAME_SETTINGS)://we do the same settings or paused
            case (GAME_LEADERBOARD):
            case (GAME_PAUSED):
                player.update(0);
                player.setVelocity(new Vector2(0,0));
                pauseMenu.update(camera);
                break;
        }

        // check walking is happening, by first checking if the player has a velocity 0
        if (Math.abs(player.getVelocity().x) > 0 || Math.abs(player.getVelocity().y) > 0) {
            if (!isWalking) {
                isWalking = true;
                currentWalkingSoundIndex = 0;
                walkingSoundTimer = WALKING_SOUND_DELAY;
            }
        } else {
            isWalking = false;
        }

        if(game.dayManager.getGameOver()){
            game.score = game.dayManager.calculateScore();
            game.setScreen(new GameOverScreen(game));
        }

        playWalkingSound(Gdx.graphics.getDeltaTime());
        stage.act(Gdx.graphics.getDeltaTime());
    }

    /**
     * Show is called the first time this object is drawn
     * Used here to set up variables like sprites and textures, and assign references
     * to the camera, renderer, collision layer, and our manager classes.
     */
    @Override
    public void show() {

        // Initialize the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 450);
        extendViewport = new ExtendViewport(camera.viewportWidth, camera.viewportHeight, camera);
        // Load the map and set up the renderer
        map = new TmxMapLoader().load("MapRelated/testmap.tmx");
        collisionLayer = (TiledMapTileLayer) map.getLayers().get(0);

        renderer = new OrthogonalTiledMapRenderer(map, 1 / 1f);

        // Set up the player
        //int playerSpriteNumber = 5;
        //Texture playerTexture = new Texture("Icons/player-" + Integer.toString(playerSpriteNumber) + ".png");
        Sprite playerSprite = new Sprite(playerTexture);
        player = new Player(playerSprite, collisionLayer);
        float startX = 30 * collisionLayer.getTileWidth();
        float startY = (collisionLayer.getHeight() - 26) * collisionLayer.getTileHeight();
        player.setPosition(startX, startY);
        //Gdx.input.setInputProcessor(player);

        // Set up the activity manager

        activityManager = new ActivityManager(map.getLayers().get("Activities"), game.dayManager);
        activityManager.setPlayer(player);

        // Set up the font
        font = new BitmapFont();
        font.getData().setScale(2);

        // Set up blank texture (used for energy bar)
        TblankTexture = new Texture("Icons/WhiteSquare.png");
        blankTexture = new Sprite(TblankTexture);

        // Set up text bubble
        textBubbleTexture = new Texture("Icons/textBubble.png");
        textBubble = new Sprite(textBubbleTexture);

        //setup menu
        pauseMenu = new PauseMenu(extendViewport, camera);
        settingsMenu = new SettingsMenu(GAME_PAUSED,camera,extendViewport,2);

        //set state
        state = GAME_RUNNING;
        // Set up the counter and counter components
        counterBoxTexture = new Texture("Icons/counter-box.png");

        // set up for the vertical bar that counts the days
        verticalBarTexture = new Texture("Icons/vertical-bar.png");
        verticalBarSprite = new Sprite(verticalBarTexture);
        // set up and connect the audio for the steps of the player
        walkingSound1 = Gdx.audio.newSound(Gdx.files.internal("Sounds/tile1.mp3"));
        walkingSound2 = Gdx.audio.newSound(Gdx.files.internal("Sounds/tile2.mp3"));
        walkingSound3 = Gdx.audio.newSound(Gdx.files.internal("Sounds/tile3.mp3"));
        walkingSound4 = Gdx.audio.newSound(Gdx.files.internal("Sounds/tile4.mp3"));
        // set up and connect the audio for the background music to always loop
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Sounds/background-music.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(volume);
        backgroundMusic.play();

        dimTexture = new Sprite(blankTexture);
        dimTexture.setColor(Color.BLACK);
        dimTexture.setSize(collisionLayer.getWidth() * 16, collisionLayer.getHeight() * 16);
        // set up the click sound for the buttons ui
        clickSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/switch2.ogg"));



        /**
         * stage is initialised and the setup creates a stage for UI elements and input processing
         * and preparing the buttons for volume control
         */
        stage = new Stage(extendViewport, renderer.getBatch());

        // a Multiplexer is needed in the play screen to be able to manage inputs from the UI and the player
        inputMultiplexer = new InputMultiplexer();
        // Stage first to ensure UI input is prioritized
        inputMultiplexer.addProcessor(stage);
        // Then player
        inputMultiplexer.addProcessor(player);
        // Set the global input processor to receive input from touch keys to the input multiplexer

        Gdx.input.setInputProcessor(inputMultiplexer);


        // Prepare textures for buttons
        increaseVolumeTexture = new Texture(Gdx.files.internal("Icons/increase-volume.png"));
        lowerVolumeTexture = new Texture(Gdx.files.internal("Icons/lower-volume.png"));
        volumeOffTexture = new Texture(Gdx.files.internal("Icons/volume-off.png"));
        volumeOnTexture = new Texture(Gdx.files.internal("Icons/volume-on.png"));



        // Create and set up the 4 volume buttons
        // increases the volume of the game if not already to the max

        Button.ButtonStyle increaseVolumeStyle = new Button.ButtonStyle();
        increaseVolumeStyle.up = new TextureRegionDrawable(new TextureRegion(increaseVolumeTexture));
        increaseVolumeButton = new Button(increaseVolumeStyle);
        //increaseVolumeButton.setPosition(900, 670); // Example position

        increaseVolumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                volume = Math.min(volume + 0.1f, 1.0f);
                backgroundMusic.setVolume(volume);
            }
        });

        // lowers the volume, checks the volume and subtracts from it
        Button.ButtonStyle lowerVolumeStyle = new Button.ButtonStyle();
        lowerVolumeStyle.up = new TextureRegionDrawable(new TextureRegion(lowerVolumeTexture));
        lowerVolumeButton = new Button(lowerVolumeStyle);
        //lowerVolumeButton.setPosition(950, 670); // Example position
        lowerVolumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                volume = Math.max(volume - 0.1f, 0.0f);
                backgroundMusic.setVolume(volume);
            }
        });
        // turns off the volume
        Button.ButtonStyle volumeOffStyle = new Button.ButtonStyle();
        volumeOffStyle.up = new TextureRegionDrawable(new TextureRegion(volumeOffTexture));
        volumeOffButton = new Button(volumeOffStyle);
        // volumeOffButton.setPosition(1000, 670); // Example position
        volumeOffButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                if (backgroundMusic.isPlaying()) {
                    backgroundMusic.pause();

                    volume = 0;

                }
            }
        });

        // Play the music if it is not already playing
        Button.ButtonStyle volumeOnStyle = new Button.ButtonStyle();
        volumeOnStyle.up = new TextureRegionDrawable(new TextureRegion(volumeOnTexture));
        volumeOnButton = new Button(volumeOnStyle);
        //volumeOnButton.setPosition(1050, 670); // Example position
        volumeOnButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                if (!backgroundMusic.isPlaying()) {
                    backgroundMusic.play();
                    volume = 1.0f;
                }
            }
        });
        // makes the buttons interactive
        stage.addActor(increaseVolumeButton);
        stage.addActor(lowerVolumeButton);
        stage.addActor(volumeOffButton);
        stage.addActor(volumeOnButton);




    }
    @Override
    public void hide() {
        dispose();
    }

    /**
     * Gets width and height screen resized to and updates viewport
     * @param width width changed to
     * @param height height changed to
     */
    @Override
    public void resize(int width, int height) {
        extendViewport.update(width,height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose() {
        //if we forget to dispose something it causes a memory leak!
        //if this is a big concern we should change our approach to assets
        //using the AssetManager means we have to load assets in a different way
        //BUT we can just call AssetManager.dispose() and it will for sure dispose all our assets correctly

        map.dispose();
        player.getTexture().dispose();
        font.dispose();
        counterBoxTexture.dispose();
        verticalBarTexture.dispose();
        walkingSound1.dispose();
        walkingSound2.dispose();
        walkingSound3.dispose();
        walkingSound4.dispose();
        settingsMenu.dispose();
        pauseMenu.dispose();

        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        stage.dispose();
        increaseVolumeTexture.dispose();
        lowerVolumeTexture.dispose();
        volumeOffTexture.dispose();
        volumeOnTexture.dispose();
        TblankTexture.dispose();
        textBubbleTexture.dispose();
        backgroundMusic.dispose();
        increaseVolumeTexture.dispose();
        volumeOffTexture.dispose();
        volumeOnTexture.dispose();
        lowerVolumeTexture.dispose();
    }
    // checks when to play the walking sound
    private void playWalkingSound(float delta) {
        if (!isWalking || walkingSoundTimer < WALKING_SOUND_DELAY) {
            walkingSoundTimer += delta;
            return;
        }

        // when the player is moving alternates between different walking sounds
        walkingSoundTimer = 0;

        Sound soundToPlay = null;
        switch (currentWalkingSoundIndex) {
            case 0:
                soundToPlay = walkingSound1;
                break;
            case 1:
                soundToPlay = walkingSound2;
                break;
            case 2:
                soundToPlay = walkingSound3;
                break;
            case 3:
                soundToPlay = walkingSound4;
                break;
        }
        if (soundToPlay != null) {
            soundToPlay.play(volume);
            currentWalkingSoundIndex = (currentWalkingSoundIndex + 1) % 4;
        }
    }

    /**
     *If camera would show out of map, move it in
     * @param cam camera to keep position within layer
     * @param layer TiledMapTile layer to lock
     */
    private void lockCameraInTiledMapLayer(OrthographicCamera cam, TiledMapTileLayer layer){
        //get variables needed to find edges of map!
        int mapPixelOffsetY =(int) layer.getOffsetY();
        int mapPixelOffsetX =(int) layer.getOffsetX();
        int mapPixelWidth = layer.getWidth() * layer.getTileWidth() + mapPixelOffsetX;
        int mapPixelHeight = layer.getHeight() * layer.getTileHeight() + mapPixelOffsetY;
        //if the camera viewport is large enough to see outside the map on both sides at once, there is no useful way to lock it. this shouldn't happen
        if (cam.viewportWidth>mapPixelWidth || cam.viewportHeight>mapPixelHeight){
            return;
        }

        //check if camera would show out of bounds, lock it in bounds if it would
        if ((cam.position.x- (cam.viewportWidth/2)< mapPixelOffsetX))//is the camera too far left.
        {
            cam.position.x = mapPixelOffsetX + cam.viewportWidth/2;
        }
        else if ((cam.position.x+ (cam.viewportWidth/2)> mapPixelWidth))//is the camera too far right.
        {
            cam.position.x = mapPixelWidth - cam.viewportWidth/2;
        }
        if ((cam.position.y- (cam.viewportHeight/2)< mapPixelOffsetY))//is the camera too low.
        {
            cam.position.y = mapPixelOffsetY + cam.viewportHeight/2;
        }
        else if ((cam.position.y+ (cam.viewportHeight/2)> mapPixelHeight))//is the camera too high.
        {
            cam.position.y = mapPixelHeight - cam.viewportHeight/2;
        }
    }

    /**
     * Get font in another class
     * @return games value of font
     */
    public static BitmapFont getFont(){
        return font;
    }

}

