package com.heshus.tests.manager;


import com.heshus.game.manager.DayManager;
import com.heshus.tests.GdxTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(GdxTestRunner.class)
public class DayManagerTests {

    //tests that the day counter increments when the method is called
    @Test
    public void dayIncrements() {
        DayManager dm = new DayManager();
        int prevDayNum = dm.getDayNumber();
        dm.incrementDay();
        assertEquals(prevDayNum + 1, dm.getDayNumber());

    }

    //tests that gameover is set to true after the 7th day
    @Test
    public void gameOverAfterLastDay(){
        DayManager dm = new DayManager();
        for(int i = 0; i<6;i++){
            runDay(dm, 0, 0, 0);
        }
        assertFalse(dm.getGameOver());
        dm.incrementDay();
        assertTrue(dm.getGameOver());
    }

    @Test
    public void studyEachDay(){
        int[] eatArray = {3,3,3,3,3,3,3};
        int[] studyArray = {1,0,0,1,1,1,1};
        int[] recArray = {1,1,1,1,1,1,1};
        DayManager dm = new DayManager();
        for (int i = 0; i < eatArray.length; i++) {
            runDay(dm, eatArray[i], studyArray[i], recArray[i]);
        }
        assertEquals("Passes when missing 2 days of study fails the player",0,dm.calculateScore());

        eatArray = new int[]{3, 3, 3, 3, 3, 3, 3};
        studyArray = new int[]{1, 0, 1, 1, 1, 1, 1};
        recArray = new int[]{1, 1, 1, 1, 1, 1, 1};
        dm = new DayManager();
        for (int i = 0; i < eatArray.length; i++) {
            runDay(dm, eatArray[i], studyArray[i], recArray[i]);
        }
        assertTrue("Passes when missing one day of study does not fail the player,",dm.calculateScore() > 0);

    }

    //
    @Test
    public void overStudying(){
        //represents a base case where one of each activity takes place per day
        DayManager dm1 = new DayManager();
        //represents a case of over-studying where the score should be decreased
        DayManager dm2 = new DayManager();

        int[] studyArray1 = {2,2,1,1,1,1,1};
        int[] studyArray2 = {3,2,3,2,3,2,3};
        int[] eatArray = {1,1,1,1,1,1,1};
        int[] recArray = {1,1,1,1,1,1,1};

        for (int i = 0; i < 7; i++){
            runDay(dm1, eatArray[i], studyArray1[i], recArray[i]);
            runDay(dm2, eatArray[i], studyArray2[i], recArray[i]);
        }
        assertTrue(dm1.calculateScore() > dm2.calculateScore());
    }


    /**
     * Simulates a day being played with
     */
    private void runDay(DayManager dm, int eat, int study, int rec){
        boolean dayover = false;
        while (!dayover){
            if (eat > 0){
                dm.incrementEatScore();
                eat--;
            }
            if (study > 0){
                dm.incrementStudyScore("placeholder");
                study--;
            }
            if (rec > 0){
                dm.incrementRecreationalScore("placeholder");
                rec--;
            }
            dayover = (rec==0)&&(study==0)&&(eat==0);
        }
        dm.incrementDay();
    }
}
