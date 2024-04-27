package com.heshus.game.screens.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.heshus.game.editor.CustomiseSprite;
import com.heshus.game.engine.HesHusGame;
import com.heshus.game.engine.Play;

/**
 * Allows the player to enter their name into a {@link TextField},
 * which will then be used by the leaderboard after.
 */
public class PlayerNameScreen {
    private BitmapFont font;
    private final Stage stage;
    private final Camera camera;
    private final Table table;
    private final Sound clickSound;
    private Texture buttonTexture;
    private TextButton.TextButtonStyle textButtonStyle;
    private final TextField enterName;
    private final HesHusGame game;
    private final Skin skin;
    private Label title;
    private TextButton button;

    /**
     * Create a playerNameScreen instance, setting up the table layout also
     * @param game The base game instance
     * @param camera The camera used to view the starting main menu
     * @param viewport The viewport associated with the menu camera
     */
    public PlayerNameScreen(HesHusGame game, Camera camera, ExtendViewport viewport) {
        this.camera = camera;
        this.stage = new Stage(viewport);
        this.game = game;

        skin = new Skin(Gdx.files.internal("UI/uiskin.json"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/switch2.ogg"));
        setupFont(.5f);
        setButtonStyle();

        title = new Label("Enter your name:", new Label.LabelStyle(font, Color.WHITE));
        button = new TextButton("PLAY", textButtonStyle);
        enterName = new TextField("", skin);
        enterName.setMessageText("Max 10 characters");
        enterName.setScale(0.8f);

        table = new Table();
        stage.addActor(table);

        // Arrange table
        table.add(title).fillX().center();
        table.row().pad(10, 0, 10, 0);
        table.add(enterName).center();
        table.row().pad(0, 0, 10, 0);
        table.add(button).center().width(buttonTexture.getWidth()).height(buttonTexture.getHeight());

        // Add input listener to the button
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                clickSound.play();
                // Only continue if they have entered a name and it is no more than 10 characters long
                if ((!enterName.getText().isEmpty()) && (enterName.getText().length() <= 10)) {
                    game.playerName = enterName.getText();
                    Play.state = Play.GAME_PLAYER_SELECT;
                    dispose();
                    return true;
                } else { return false; }
            }
        });
    }

    private void setButtonStyle() {
        buttonTexture = new Texture("UI/button_up.png");
        TextureRegion buttonTextureRegion = new TextureRegion(buttonTexture, buttonTexture.getWidth(), buttonTexture.getHeight());
        TextureRegionDrawable buttonTextureRegionDrawable = new TextureRegionDrawable(buttonTextureRegion);
        textButtonStyle = new TextButton.TextButtonStyle(buttonTextureRegionDrawable, buttonTextureRegionDrawable, buttonTextureRegionDrawable, this.font);
    }

    /**
     * Set up the font for the UI, with a given scale
     * @param scale The scale of the font
     */
    private void setupFont(float scale) {
        font = new BitmapFont(Gdx.files.internal("Fonts/monogram/pixel.fnt"), false);
        font.getData().setScale(scale);
        font.setColor(Color.BLACK);
    }

    /**
     * Update the current view to focus on this class' instance
     */
    public void update() {
        Gdx.input.setInputProcessor(stage);
        table.setPosition(camera.position.x, camera.position.y);
        stage.draw();
    }

    /**
     * Dispose of all textures
     */
    public void dispose() {
        stage.dispose();
        skin.dispose();
        font.dispose();
        buttonTexture.dispose();
        clickSound.dispose();
    }
}
