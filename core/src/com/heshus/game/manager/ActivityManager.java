package com.heshus.game.manager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.heshus.game.engine.Play;
import com.heshus.game.entities.Player;
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
    private Player player;
    private DayManager dayManager;

    private String activityText = "";
    private Vector2 textPosition = new Vector2();

    GlyphLayout layout = new GlyphLayout();

    /**
     * Constructor for ActivityManager
     * @param layer layer that controls collision and activity logic
     */
    public ActivityManager(MapLayer layer, DayManager dayManager) {
        this.layer = layer;
        this.dayManager = dayManager;
    }


    /**
     * Checks whether and which activity is performed based on location of player
     */
    public void checkActivity() {
        // based on the x, y coordinates of the player
        float avatarX = player.getX();
        float avatarY = player.getY();

        // Check all activities
        MapObjects objects = layer.getObjects();
        // In activity area and they press E
        for (RectangleMapObject rectActivity : objects.getByType(RectangleMapObject.class)) {
            if (player.getBoundingRectangle().overlaps(rectActivity.getRectangle()) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                MapProperties activityProperties = rectActivity.getProperties();
                performActivity(activityProperties);
            }
        }
    }

    private void performActivity(MapProperties activityProperties) {
        String holdText = "";
        if(validActivity(activityProperties)) {
            decrementEnergy(activityProperties.get("energy", int.class));
            incrementTime(activityProperties.get("time", int.class));
            switch (activityProperties.get("activity", String.class)) {
                case "eat":
                    dayManager.currentDay.incrementEatScore();
                    break;
                case "study":
                    dayManager.currentDay.incrementStudyScore();
                    break;
                case "recreation":
                    dayManager.currentDay.incrementRecreationalScore();
                    break;
                case "sleep":
                    // if the game is not over the avatar will move to the next day and reset their energy
                    if (!dayManager.getGameOver()) {
                        dayManager.incrementDay();
                    }
                    break;
            }
            holdText = "You " + activityProperties.get("description", String.class);
        } else if (dayManager.currentDay.getEnergy() - activityProperties.get("energy", int.class) < 0) {
            holdText = "You're too tired for that, you should sleep";
        } else if (dayManager.currentDay.getTime() + activityProperties.get("time", int.class) > 24) {
            holdText = "It's getting late, you should go to bed";
        } else { holdText = "You should get some sleep"; }
        layout.setText(Play.getFont(), holdText);
        setText(holdText, Math.round(player.getX() / 16) * 16 + 8 - (layout.width / 2), Math.round(player.getY() / 16) * 16);
    }

    private boolean validActivity(MapProperties activityProperties) {
        return (dayManager.currentDay.getEnergy() - activityProperties.get("energy", int.class) >= 0) &&
                (dayManager.currentDay.getTime() + activityProperties.get("time", int.class) <= 24);
    }

    /**
     /**
     * @param setTime
     * accepts as parameter a setTime for different activities
     * Decreases current day's energy
     * @param energy value to decrease energy by
     */
    private void decrementEnergy(int energy) {
        dayManager.currentDay.setEnergy(dayManager.currentDay.getEnergy() - energy);
    }

    /**
     * Increases the current day's time
     * @param setTime value to increase time by
     */
    private void incrementTime(int setTime) {
        float newTime = dayManager.currentDay.getTime() + setTime;
        dayManager.currentDay.setTime(newTime);
    }


    /**
     * Sets another class' instance of 'player' to ActivityManager's player
     * @param player
     */
    public void setPlayer(Player player) {
        this.player = player;
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