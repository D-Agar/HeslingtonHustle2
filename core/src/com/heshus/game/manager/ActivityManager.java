package com.heshus.game.manager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

/**

 * Manages all activities in the game that the player can perform
 * by introducing property tags to the tiled map the player interacts with each property tag
 * and according to what the type of activity is the player's energy and time is incremented or decremented
 * Manages how the activities are performed, why they are performed and energy/time constraints
 * Displays text whenever a task is completed
 */

public class ActivityManager {

    private final TiledMapTileLayer layer;
    private Player player;
    private DayManager dayManager;

    private String activityText = "";
    private Vector2 textPosition = new Vector2();

    GlyphLayout layout = new GlyphLayout();

    /**
     * Constructor for ActivityManager
     * @param layer layer that controls collision and activity logic
     */
    public ActivityManager(TiledMapTileLayer layer, DayManager dayManager) {
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

//        // Convert avatar position to tile coordinates
//        int x = (int) avatarX;
//        int y = (int) avatarY;
//        // checking for the property tag
//        TiledMapTileLayer.Cell cell = layer.getCell(x/ layer.getTileWidth(), y/ layer.getTileHeight());
//        if (cell != null) {
//            if (cell.getTile().getProperties().containsKey("eat") && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
//                performEatingActivity();
//            } else if (cell.getTile().getProperties().containsKey("study") && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
//                performStudyingActivity();
//            } else if (cell.getTile().getProperties().containsKey("recreation") && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
//                performRecreationalActivity();
//            } else if (cell.getTile().getProperties().containsKey("sleep") && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
//                performSleepingActivity();
//            }
//        }

        // Check all activities
        MapObjects objects = layer.getObjects();
        for (int i = 0; i < objects.getCount(); i++) {
            RectangleMapObject rectActivity = (RectangleMapObject) objects.get(i);
            // In activity area and they press E
            if (player.getBoundingRectangle().overlaps(rectActivity.getRectangle()) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                MapProperties activityProperties = rectActivity.getProperties();
                if (activityProperties.get("activity", String.class) == "sleep") {
                    performSleepingActivity();
                } else { performActivity(activityProperties); }
            }
        }
    }

    private void performActivity(MapProperties activityProperties) {
        if(!(dayManager.currentDay.getEnergy() <= 0) && !(dayManager.currentDay.getTime() >= 24)) {
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
            }
            String holdText = "You " + activityProperties.get("description", String.class);
            layout.setText(Play.getFont(), holdText);
            setText(holdText, Math.round(player.getX() / 16) * 16 + 8 - (layout.width / 2), Math.round(player.getY() / 16) * 16);
        } else { noEnergyOrSleep(); }
    }


    /**
     * If available, controls variables indicating eating has been performed
     */
    private void performEatingActivity() {

        if(!(dayManager.currentDay.getEnergy() <= 0) && !(dayManager.currentDay.getTime() >= 24)) {
            decrementEnergy(10);
            incrementTime(2);
            dayManager.currentDay.incrementEatScore();

            //Holds the message to be displayed

            String holdText = "You feel refreshed";
            layout.setText(Play.getFont(), holdText);

        }
        else{
            noEnergyOrSleep();
        }
    }

    // incrementing each property tag activity, if time and have not run out it will decrement energy with 20 from 100 and increment time with 4
  

    /**
     * If available, controls variables indicating studying has been performed
     */
    private void performStudyingActivity() {

        if(!(dayManager.currentDay.getEnergy() <= 0) && !(dayManager.currentDay.getTime() >= 24)) {
            decrementEnergy(20);
            incrementTime(4);
            dayManager.currentDay.incrementStudyScore();

            //Holds the message to be displayed

            String holdText = "You feel smarter";
            layout.setText(Play.getFont(), holdText);
            setText(holdText, Math.round(player.getX() / 16) * 16 + 8 - (layout.width / 2), Math.round(player.getY() / 16) * 16);
        }
        else{
            noEnergyOrSleep();
        }
    }


    // incrementing each property tag activity, if time and have not run out it will decrement energy with 20 from 100 and increment time with 3
    

    /**
     * If available, controls variables indicating recreation has been performed
     */
    private void performRecreationalActivity() {

        if(!(dayManager.currentDay.getEnergy() <= 0) && !(dayManager.currentDay.getTime() >= 24)){

            decrementEnergy(20);
            incrementTime(3);
            dayManager.currentDay.incrementRecreationalScore();

            //Holds the message to be displayed

            String holdText = "You have recreationed";
            layout.setText(Play.getFont(), holdText);
            setText(holdText, Math.round(player.getX() / 16) * 16 + 8 - (layout.width/2), Math.round(player.getY() / 16) * 16);
        }
        else{
            noEnergyOrSleep();
        }

    }


    // incrementing each property tag activity, the player can only sleep when they have ran out of energy

    /**
     * Checks whether the player can sleep and if so, creates a new day
     */

    private void performSleepingActivity() {
        // decided to define day over with reaching 840 time
        if (dayManager.currentDay.getTime() >= 24 || dayManager.currentDay.getEnergy() <= 0) {
            //Holds the message to be displayed
            String holdText = "You feel well rested";
            layout.setText(Play.getFont(), holdText);
            setText(holdText, Math.round(player.getX() / 16) * 16 + 8 - (layout.width/2), Math.round(player.getY() / 16) * 16);

            // if the game is not over the avatar will move to the next day and reset their energy
            if (!dayManager.getGameOver()) {
                dayManager.incrementDay();
            }
        }
    }

    /**
     /**
     * @param setTime
     * accepts as parameter a setTime for different activities
     * Decreases current day's energy
     * @param energy value to decrease energy by
     */
    private void decrementEnergy(int energy) {
        dayManager.currentDay.setEnergy(Math.max(0, dayManager.currentDay.getEnergy() - energy));
    }

    /**
     * Increases the current day's time
     * @param setTime value to increase time by
     */
    private void incrementTime(int setTime) {
        float newTime = dayManager.currentDay.getTime() + setTime;
        if (newTime >= 24) {
            //"You need to sleep" we display a message to the player, move to next day
        } else {
            dayManager.currentDay.setTime(newTime);
        }
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


    /**
     * Manages text when no energy or time is available
     */
    public void noEnergyOrSleep(){
        String holdText = "You should get some sleep";
        layout.setText(Play.getFont(), holdText);
        setText(holdText, Math.round(player.getX() / 16) * 16 + 8 - (layout.width/2), Math.round(player.getY() / 16) * 16);
    }
}