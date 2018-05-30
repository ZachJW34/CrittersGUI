package assignment5;

import javafx.scene.paint.Color;

import java.util.List;



/**
 * Critter that acts kind of like a plague as it can devastate the algae population quickly. The Critter will move
 * about half of the time but when it does it will always run. It doesn't move half of the time so that it can easily
 * run away from opponents as it has no interest in fighting except for other members of the same species.
 * @author ZachJ
 */
public class Critter1 extends Critter {

    private int tilesMoved;
    private int prevDir;

    /**
     * Sets the Critters initial parameters to zero
     */
    public Critter1(){
        prevDir = 0;
        tilesMoved = 0;
    }

    /**
     * Will either sit still or run randomly but always in a direction that was not it's previous direction (maximum
     * spread).
     */
    @Override
    public void doTimeStep() {
        int roll = getRandomInt(2);
        if (roll == 0){
            int dir = getRandomInt(8);
            while (dir == prevDir){
                dir = getRandomInt(8);
            }
            int futuremove = dir;
            String lookAround = look(dir, true);
            if (lookAround == null || !lookAround.equals("@") || !lookAround.equals("1")){
                dir = getRandomInt(8);
                while (dir == prevDir || dir == futuremove){
                    dir = getRandomInt(8);
                }
            }
            run(dir);
            tilesMoved ++;
            prevDir = dir;
        }

        if (getEnergy() >= Params.min_reproduce_energy){
            Critter1 child = new Critter1();
            reproduce(child, prevDir);
        }

    }

    /**
     * Will run away from all opponents except for itself and in a direction that is different from where it last moved.
     * @param opponent is the string representation of it's opponent
     * @return true if it wants to fight, false if it tries to run away.
     */
    @Override
    public boolean fight(String opponent) {
        if (opponent.equals("C") || opponent.equals("2")){
            int dir = getRandomInt(8);
            while (prevDir == dir){
                dir = getRandomInt(8);
            }
            run(dir);
            prevDir = dir;
            tilesMoved+=2;
            return false;
        }
        return true;
    }

    /**
     * Returns string representation of Critter
     * @return "1"
     */
    @Override
    public String toString() {
        return "1";
    }

    /**
     * Stats of Critter1 is defined by how many tiles the Critter has moved. Since this is a plague-like creature, it is
     * interesting to see how far they have spread.
     * @param critter1List is the list of all Critter2 critters
     */
    public static String runStats(List<Critter> critter1List){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Total Critter1s: ").append(critter1List.size()).append("\n");
        int totalmoved = 0;
        for (Critter critter: critter1List){
            Critter1 critter1 = (Critter1) critter;
            totalmoved+=critter1.getTilesMoved();
        }
        stringBuilder.append(totalmoved + " total tiles moved\n");
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    @Override
    public CritterShape viewShape() { return CritterShape.TRIANGLE; }

    @Override
    public javafx.scene.paint.Color viewFillColor() { return Color.ORANGE; }

    public int getTilesMoved(){
        return tilesMoved;
    }

}

