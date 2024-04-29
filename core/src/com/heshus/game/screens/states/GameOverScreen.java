package com.heshus.game.screens.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.heshus.game.engine.HesHusGame;
import com.heshus.game.manager.DayManager;
import com.heshus.game.manager.Save;
import com.heshus.game.manager.Score;
import static com.heshus.game.manager.Save.gd;

public class GameOverScreen implements Screen {

    private final HesHusGame game;
    // New: added the player's score as a Score instance
    private Score playerScore;
    private BitmapFont font;
    private Table table;
    private TextButton menuBtn;
    private Label title;
    private Label scoreLabel;
    private Texture buttonTexture;
    private TextureRegion buttonTextureRegion;
    private TextureRegionDrawable buttonTextureRegionDrawable;
    private TextButton.TextButtonStyle textButtonStyle;
    private Stage stage;


    /**
     * Create an instance of the game over screen
     *
     * @param game The base game class
     */
    public GameOverScreen(final HesHusGame game) {
        this.game = game;
        stage = new Stage(new ExtendViewport(400, 225));

        // New: Create an instance of Score for the player
        game.score = game.dayManager.endGame();
        playerScore = new Score(game.playerName, game.score);

        // Set up font
        font = new BitmapFont(Gdx.files.internal("Fonts/monogram/pixel.fnt"), false);
        font.getData().setScale(.5F);
        font.setColor(Color.BLACK);

        title = new Label("Game Over!", new Label.LabelStyle(font, Color.WHITE));
        scoreLabel = new Label("Score: " + playerScore.getScore(), new Label.LabelStyle(font, Color.WHITE));

        setButtonStyle();
        menuBtn = new TextButton("Main Menu", textButtonStyle);

        table = new Table();
        stage.addActor(table);
        table.setFillParent(true);
    }

    // New: modified the show method to save the player's score if it qualifies to be on the leaderboard
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        table.add(title).center();
        table.row().pad(20, 0, 10, 0);
        if (gd.isHighScore(playerScore)) {
            gd.addHighScore(playerScore);
            Save.save();
            Label highScore = new Label("High Score!", new Label.LabelStyle(font, Color.WHITE));
            table.add(highScore).center();
        }
        table.add(scoreLabel).center();
        table.row().pad(20, 0, 10, 0);
        table.add(menuBtn).center();

        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.dayManager = new DayManager();
                game.score = 0;
                game.playerName = "";
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.draw();
    }

    private void setButtonStyle() {
        buttonTexture = new Texture("UI/button_up.png");
        buttonTextureRegion = new TextureRegion(buttonTexture, buttonTexture.getWidth(), buttonTexture.getHeight());
        buttonTextureRegionDrawable = new TextureRegionDrawable(buttonTextureRegion);
        textButtonStyle = new TextButton.TextButtonStyle(buttonTextureRegionDrawable, buttonTextureRegionDrawable, buttonTextureRegionDrawable, this.font);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        font.dispose();
        buttonTexture.dispose();
        stage.dispose();
    }
}
