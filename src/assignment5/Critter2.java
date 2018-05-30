package assignment5;

import javafx.scene.paint.Color;

import java.util.List;

/**
 * This Critter was implemented so that it formed cool patterns when the world is full of algae. The critter will move
 * in a circle of a randomly generated radius, and once it completes its circle it will pick a new radius and a new
 * direction that is different from the previous. This creature will always fight so as not to disturb it's arc.
 * @author ZachJ
 */
public class Critter2 extends Critter {

    private int dir;
    private int totalMoves;
    private int moves;
    private int radius;
    private int circles;

    /**
     * Generates a random direction and random radius size, and sets the rest of the parameters to zero
     */
    public Critter2(){
        circles = 0;
        moves = 0;
        dir = getRandomInt(8);
        radius = getRandomInt(8);
        while (radius == 0){
            radius = getRandomInt(8);
        }
        totalMoves = 0;
    }

    /**
     * Will always cause the Critter to walk. The critter's walking behavior is defined as a circle based on its radius.
     * Once the Critter has moved a complete circle, the critter will choose a different radius and a different direction
     * to begin its circle.
     */
    @Override
    public void doTimeStep() {
        walk(dir);
        moves++;
        totalMoves++;

        if (moves%radius == 0){
            dir = (dir+1)%8;
            moves = 0;
        }
        if (totalMoves >= radius*8){
            dir = getRandomInt(8);
            int roll = getRandomInt(8);
            while (radius == roll || roll == 0){
                roll = getRandomInt(8);
            }
            radius = roll;
            moves = 0;
            totalMoves = 0;
            circles++;
        }
    }

    /**
     * Will always fight so as not to disturb it's arc
     * @param opponent is the string representation of the critter's opponent
     * @return a boolean true always
     */
    @Override
    public boolean fight(String opponent) {
        return true;
    }

    /**
     * String representation of critter
     * @return "2"
     */
    @Override
    public String toString() {
        return "2";
    }

    /**
     * Interesting thing about these critters is the amount of full circles the critters have made.
     * @param critter2List is the list of all critters of type Critter2
     */
    public static String runStats(List<Critter> critter2List){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Total Critter2s: ").append(critter2List.size()).append("\n");
        int totalCircles = 0;
        for (Critter critter: critter2List){
            Critter2 critter2 = (Critter2) critter;
            totalCircles+=critter2.getCircles();
        }
        stringBuilder.append("Total Circles: ").append(totalCircles);
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    @Override
    public CritterShape viewShape() { return CritterShape.DIAMOND; }

    @Override
    public javafx.scene.paint.Color viewOutlineColor() { return Color.YELLOW; }

    public int getCircles(){
        return circles;
    }

}

