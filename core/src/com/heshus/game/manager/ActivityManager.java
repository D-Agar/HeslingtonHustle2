package com.heshus.game.manager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.heshus.game.engine.Play;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import java.util.Objects;

/**
 * Manages all activities in the game that the player can perform
 * by introducing property tags to the tiled map the player interacts with each property tag
 * and according to what the type of activity is the player's energy and time is incremented or decremented
 * Manages how the activities are performed, why they are performed and energy/time constraints
 * Displays text whenever a task is completed
 */
public class ActivityManager {
    private final MapLayer layer;
    private final DayManager dayManager;

    private String activityText = "";
    private final Vector2 textPosition = new Vector2();

    private int studied;

    GlyphLayout layout = new GlyphLayout();

    /**
     * Constructor for ActivityManager
     * @param layer layer that controls collision and activity logic
     */
    public ActivityManager(MapLayer layer, DayManager dayManager) {
        this.layer = layer;
        this.dayManager = dayManager;
        this.studied = 0;
    }

    // todo: Modification - method now checks the newly added activities layer and gets a selected activity's properties
    /**
     * Checks whether and which activity is performed based on location of player
     */
    public void checkActivity(Rectangle playerBoundRect, boolean justPressedE, float x, float y) {

        MapProperties activityProperties = null;
        // Check all activities
        MapObjects objects = layer.getObjects();
        // In activity area and they press E
        for (RectangleMapObject rectActivity : objects.getByType(RectangleMapObject.class)) {

            if (playerBoundRect.overlaps(rectActivity.getRectangle()) && justPressedE) {
                activityProperties = rectActivity.getProperties();
                performActivity(activityProperties, x, y);
            }
        }

        // New: Dev buttons, (m , . /) perform eat/recreation/study/sleep
        // Eat
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            decrementEnergy(10);
            dayManager.incrementEatScore("cafe");
            incrementTime(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.COMMA)) {
            // Recreation
            decrementEnergy(35);
            dayManager.incrementRecreationalScore("ducks");
            incrementTime(2);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) {
            // Study
            decrementEnergy(40);
            dayManager.incrementStudyScore("library");
            incrementTime(4);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SLASH)) {
            // Sleep
            // if the game is not over the avatar will move to the next day and reset their energy
            if (!dayManager.getGameOver()) {
                dayManager.incrementDay();
            }
        }
    }
    //We can also use this to tinker with different locations having different effects
    private void performActivity(MapProperties activityProperties, float x, float y) {
        String holdText = "";
        if(validActivity(activityProperties)) {
            decrementEnergy(activityProperties.get("energy", int.class));
            incrementTime(activityProperties.get("time", int.class));
            switch (activityProperties.get("activity", String.class)) {
                case "eat":
                    dayManager.incrementEatScore((String) activityProperties.get("place"));
                    break;
                case "study":
                    studied++;
                    dayManager.incrementStudyScore((String) activityProperties.get("place")); //Pass in description tile attribute when thats implemented
                    break;
                case "recreation":
                    dayManager.incrementRecreationalScore((String) activityProperties.get("place"));
                    break;
                case "sleep":
                    // if the game is not over the avatar will move to the next day and reset their energy
                    if (!dayManager.getGameOver()) {
                        dayManager.incrementDay();
                        studied = 0;
                    }
                    break;
            }
            holdText = "You " + activityProperties.get("description", String.class);
        } else if (dayManager.getEnergy() - activityProperties.get("energy", int.class) < 0) {
            holdText = "You're too tired for that, you should sleep";
        } else if (dayManager.getTime() + activityProperties.get("time", int.class) > 24) {
            holdText = "It's getting late, you should go to bed";
        } else if (cantStudy()) {
            holdText = "You cannot study any more today";
        } else { holdText = "You should get some sleep"; }
        layout.setText(Play.getFont(), holdText);
        setText(holdText, Math.round(x / 16) * 16 + 8 - (layout.width / 2), Math.round(y / 16) * 16);
    }

    private boolean validActivity(MapProperties activityProperties) {
        // Can only study twice if they didn't study previous day
        if (cantStudy() && Objects.equals(activityProperties.get("activity", String.class), "study")) {
            return false;
        }
        boolean enoughEnergy = dayManager.getEnergy() - activityProperties.get("energy", int.class) >= 0;
        boolean enoughTime = dayManager.getTime() + activityProperties.get("time", int.class) <= 24;

        return enoughEnergy && enoughTime;
    }

    /**
     * @return boolean indicating whether the player can study or not
     */
    private boolean cantStudy() {
        return (dayManager.getDaysOfNoStudy() <= 0 || studied > 1) &&
                (dayManager.getDaysOfNoStudy() != 0 || studied != 0);
    }

    /**
     * Decrement the player's energy
     * @param energy value to decrease energy by
     */
    private void decrementEnergy(int energy) {
        dayManager.setEnergy(dayManager.getEnergy() - energy);
    }

    /**
     * Increases the current day's time
     * @param setTime value to increase time by
     */
    private void incrementTime(int setTime) {
        float newTime = dayManager.getTime() + setTime;
        dayManager.setTime(newTime);
    }

    /**
     *
     * @param text to be displayed
     * @param x horizontal position of text
     * @param y vertical position of text
     */

    public void setText(String text, float x, float y){
        activityText = text;
        textPosition.set(x, y + 40);
    }

    /**
     * Gets the current text needing to be displayed
     * @return text
     */
    public String getText(){
        return activityText;
    }

    /**
     * Gets the position to display the text
     * @return position to display text
     */
    public Vector2 getTextPosition() {
        return textPosition;
    }

    /**
     * Draws a text bubble above current text position (usually the current cell)
     * @param batch instance of spritebatch
     * @param font instance of font
     */
    public void drawTextBubble(SpriteBatch batch, BitmapFont font){
        font.setColor(new Color(Color.BLACK));
        font.draw(batch, activityText, textPosition.x, textPosition.y + 37);
        font.setColor(new Color(Color.WHITE));
    }
}