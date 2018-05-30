package assignment5;
/* CRITTERS Critter.java
 * EE422C Project 5 submission by
 * Replace <...> with your actual data.
 * Zachary Williams
 * zw3622
 * 15470
 * Slip days used: <0>
 * Fall 2016
 */


import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

/* see the PDF for descriptions of the methods and fields in this class
 * you may add fields, methods or inner classes to Critter ONLY if you make your additions private
 * no new public, protected or default-package code or data can be added to Critter
 */


public abstract class Critter {

    /**
     * Defines the shape of a critter when created by the {@link #displayWorld(Object)}
     */
    public enum CritterShape {
        CIRCLE,
        SQUARE,
        TRIANGLE,
        DIAMOND,
        STAR
    }

    /**
     * Defines the dimensions of the {@link CritterShape} to draw (square)
     */
    private static double pixelwidth;

    /**
     * Base color getter method for all Critters
     * @return
     */
    public javafx.scene.paint.Color viewColor() {
        return javafx.scene.paint.Color.WHITE;
    }

    /**
     * Defualt method that returns White ({@link #viewColor()}) as the Critter's outline color if not overridden by a subclass of Critter
     * @return a Color White is not overridden
     */
    public javafx.scene.paint.Color viewOutlineColor() { return viewColor(); }

    /**
     * Defualt method that returns White ({@link #viewColor()}) as the Critter's Fill color if not overridden by a subclass of Critter
     * @return a Color White is not overridden
     */
    public javafx.scene.paint.Color viewFillColor() { return viewColor(); }

    /**
     * Getter method for viewing Critter's shape
     * @return {@link CritterShape}
     */
    public abstract CritterShape viewShape();

    private static String myPackage;

    /**
     * Contains all Critters
     */
    private static List<Critter> population = new java.util.ArrayList<Critter>();

    /**
     * Temporarily stores reproduced critters
     */
    private static List<Critter> babies = new java.util.ArrayList<Critter>();

    /**
     * Stores all Critters in population with reference to where they are in a 2D grid
     */
    private static List<List<Critter>> world = new ArrayList<>(Params.world_width*Params.world_height);

    /**
     * Stores whether each Critter has walked or not
     */
    private static List<List<Boolean>> hasWalked = new ArrayList<>(Params.world_width*Params.world_height);

    /**
     * Used for {@link #look(int, boolean)} method as it relies on all Critters moving simultaneously
     */
    private static List<PrevLoc> prevLocs = new ArrayList<>();

    /**
     * Stores the current critters that are fighting
     */
    private static Critter[] fighters = new Critter[2];

    /**
     * Gets the package name.  This assumes that Critter and its subclasses are all in the same package.
     */
    static {
        for (int i=0; i<Params.world_width*Params.world_height; i++){
            world.add(new ArrayList<Critter>());
            hasWalked.add(new ArrayList<Boolean>());
        }
        myPackage = Critter.class.getPackage().toString().split(" ")[1];
        if (Params.world_width >= Params.world_height){
            pixelwidth = 480.0/Params.world_width;
        } else{
            pixelwidth = 480.0/Params.world_height;
        }
    }

    private static java.util.Random rand = new java.util.Random();

    /**
     * Random number generator function to allow reproducibility
     * @param max defines the range of numbers that can be returned from 0 - (max-1)
     * @return a randomly generated int
     */
    public static int getRandomInt(int max) {
        return rand.nextInt(max);
    }

    /**
     * Sets the seed of the random number generator
     * @param new_seed is the seed to set
     */
    public static void setSeed(long new_seed) {
        rand = new java.util.Random(new_seed);
    }


    /**
     * Normally Overrided method of its subclasses
     * @return a blank string
     */
    public String toString() {
        return "";
    }

    private int energy = 0;

    protected int getEnergy() {
        return energy;
    }

    private static int firstBuild = 0;

    private int x_coord;
    private int y_coord;

    /**
     * Will determine if a critter is in the specified grid-spot based on the direction and distance.
     * @param direction is a number 0-7 that corresponds to E, NE, N etc..
     * @param steps is equivalent to distance. False = 1 step, True = 2 steps
     * @return null if empty, occupying Critter's toString if occupied
     */
    protected final String look(int direction, boolean steps) {
        int prevX = x_coord;
        int prevY = y_coord;
        int distance = steps ? 2:1;
        String repres = null;
        move(this, direction, distance);
        for(PrevLoc prevLoc: prevLocs){
            if (prevLoc !=null && x_coord == prevLoc.prevX && y_coord == prevLoc.prevY){
                repres = prevLoc.stringRep;
                break;
            }
        }
        x_coord = prevX;
        y_coord = prevY;
        energy-=Params.look_energy_cost;
        return repres;
    }

    /**
     * Method to have Critter run.
     * @param direction is the direction to move the critter in the world.
     */
    protected final void walk(int direction) {
        moveConditionals(direction, 1, Params.walk_energy_cost);
    }

    /**
     * Method to have Critter run.
     * @param direction is the direction to move the critter in the world.
     */
    protected final void run(int direction) {
        moveConditionals(direction, 2, Params.run_energy_cost);
    }

    /**
     * Walk/run method for a Critter. Will call {@link #move(Critter, int, int)} to move the Critter one/two spaces. Conditionals
     * to check to see if the Critter:
     *      <ul>
     *          <li> If the Critter is a fighter and has not moved (thus they can move)</li>
     *          <li> If the Critter is a fighter and HAS moved (can't move and get deducted for trying)</li>
     *          <li> If the Critter is a fighter and has not moved, but tries to move into an occupied position</li>
     *      </ul>
     * @param direction is the direction to move in
     */
    private void moveConditionals(int direction, int distance, int energyCost){
        boolean isFighter = (fighters[0] == this || fighters[1] == this);
        if ( isFighter && checkIfWalked(this)){
            energy-=energyCost;
            return;
        } else if (isFighter){
            int prevX = x_coord;
            int prevY = y_coord;
            move(this, direction, distance);
            if (world.get(convertTo1D(x_coord, y_coord)).isEmpty()){
                x_coord = prevX;
                y_coord = prevY;
            } else{
                x_coord = prevX;
                y_coord = prevY;
                energy-=energyCost;
                return;
            }
        }
        removeFromWorld(this);
        move(this, direction, distance);
        energy-=energyCost;
        addToWorld(this);
        markAsWalked(this);
    }

    /**
     * Moves the critter in the specified direction with specified distance. Critter can move in 8 different directions
     * with 0 being E, 1 = NE, 2 = N, 3 = NW, 4 = W, 5 = SW, 6 = S, 7 = SE.
     * @param critter is the Critter to move
     * @param direction is the direction to move the critter
     * @param distance the distance to move the critter ( 1 for walk, 2 for run)
     */
    private static void move(Critter critter, int direction, int distance){
        if (direction == 7 || direction == 0 || direction == 1){
            critter.x_coord = (critter.x_coord +distance)%Params.world_width;
        }
        if (direction == 3 || direction ==4 || direction == 5){
            critter.x_coord-=distance;
            if (critter.x_coord < 0){
                critter.x_coord+=Params.world_width;
            }
        }
        if (direction == 5 || direction == 6 || direction == 7){
            critter.y_coord = (critter.y_coord + distance)%Params.world_height;
        }
        if (direction == 1 || direction == 2 || direction == 3){
            critter.y_coord-=distance;
            if (critter.y_coord<0){
                critter.y_coord += Params.world_height;
            }
        }

    }

    /**
     * New Critter offspring is given an x and y coordinate based on the direction passed and can be placed on top of
     * another pre-existing critter. Added to babies list which will be added to population after {@link #worldTimeStep()}
     * @param offspring is the new offspring with attributes based off the parent. Child will have 1/2 of parent's energy rounded down.
     *                  Parent will have its energy reduced by 1/2 rounded up.
     * @param direction is the direction to place the child Critter
     */
    protected final void reproduce(Critter offspring, int direction) {
        offspring.energy = this.energy/2;
        this.energy = (int) Math.ceil((double)this.energy/2);
        offspring.x_coord = x_coord;
        offspring.y_coord = y_coord;
        move(offspring, direction, 1);
        babies.add(offspring);
    }

    /**
     * Overriden by subclass. Method defines how creature behaves in the world but not encounters
     */
    public abstract void doTimeStep();

    /**
     * Overridedn by subclass
     * @param oponent is the toString of an opposing Critter
     * @return true if the Critters wants to fight, false if not
     */
    public abstract boolean fight(String oponent);

    /**
     * create and initialize a Critter subclass.
     * critter_class_name must be the unqualified name of a concrete subclass of Critter, if not,
     * an InvalidCritterException must be thrown.
     * (Java weirdness: Exception throwing does not work properly if the parameter has lower-case instead of
     * upper. For example, if craig is supplied instead of Craig, an error is thrown instead of
     * an Exception.)
     *
     * @param critter_class_name is the simple class name for a Critter.
     * @throws InvalidCritterException if the class doesn't exist an other errors
     */
    public static void makeCritter(String critter_class_name) throws InvalidCritterException {
        if (critter_class_name.length() != 0 && Character.isLowerCase(critter_class_name.charAt(0))){
            throw new InvalidCritterException(critter_class_name);
        }
        try {
            Class critterClass = Class.forName(myPackage + '.' + critter_class_name);
            Critter critter = (Critter) critterClass.newInstance();
            critter.x_coord = getRandomInt(Params.world_width);
            critter.y_coord = getRandomInt(Params.world_height);
            critter.energy = Params.start_energy;
            population.add(critter);
            addToWorld(critter);
            hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord));
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoClassDefFoundError e) {
            throw new InvalidCritterException(critter_class_name);
        }
    }

    /**
     * Gets a list of critters of a specific type.
     *
     * @param critter_class_name What kind of Critter is to be listed.  Unqualified class name.
     * @return List of Critters.
     * @throws InvalidCritterException if simple name doesn't match a defined class
     */
    public static List<Critter> getInstances(String critter_class_name) throws InvalidCritterException {
        List<Critter> result = new java.util.ArrayList<Critter>();
        try {
            Class critterClass = Class.forName(myPackage + "." + critter_class_name);
            for (Critter critter : population) {
                if (critterClass.equals(critter.getClass())) {
                    result.add(critter);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidCritterException(critter_class_name);
        }
        return result;
    }

    /**
     * Prints out how many Critters of each type there are on the board.
     *
     * @param critters List of Critters.
     */
    public static String runStats(List<Critter> critters) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("" + critters.size() + " critters as follows -- ");
        java.util.Map<String, Integer> critter_count = new java.util.HashMap<String, Integer>();
        for (Critter crit : critters) {
            String crit_string = crit.toString();
            Integer old_count = critter_count.get(crit_string);
            if (old_count == null) {
                critter_count.put(crit_string,  1);
            } else {
                critter_count.put(crit_string, old_count.intValue() + 1);
            }
        }
        String prefix = "";
        for (String s : critter_count.keySet()) {
            stringBuilder.append(prefix + s + ":" + critter_count.get(s));
            prefix = ", ";
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    /* the TestCritter class allows some critters to "cheat". If you want to
     * create tests of your Critter model, you can create subclasses of this class
     * and then use the setter functions contained here.
     *
     * NOTE: you must make sure that the setter functions work with your implementation
     * of Critter. That means, if you're recording the positions of your critters
     * using some sort of external grid or some other data structure in addition
     * to the x_coord and y_coord functions, then you MUST update these setter functions
     * so that they correctly update your grid/data structure.
     */
    static abstract class TestCritter extends Critter {
        protected void setEnergy(int new_energy_value) {
            super.energy = new_energy_value;
        }

        protected void setX_coord(int new_x_coord) {
            Critter.removeFromWorld(this);
            super.x_coord = new_x_coord;
            Critter.addToWorld(this);
        }

        protected void setY_coord(int new_y_coord) {
            Critter.removeFromWorld(this);
            super.y_coord = new_y_coord;
            Critter.addToWorld(this);
        }

        protected int getX_coord() {
            return super.x_coord;
        }

        protected int getY_coord() {
            return super.y_coord;
        }


        /*
         * This method getPopulation has to be modified by you if you are not using the population
         * ArrayList that has been provided in the starter code.  In any case, it has to be
         * implemented for grading tests to work.
         */
        protected static List<Critter> getPopulation() {
            return population;
        }

        /*
         * This method getBabies has to be modified by you if you are not using the babies
         * ArrayList that has been provided in the starter code.  In any case, it has to be
         * implemented for grading tests to work.  Babies should be added to the general population
         * at either the beginning OR the end of every timestep.
         */
        protected static List<Critter> getBabies() {
            return babies;
        }
    }

    /**
     * Clear the world of all critters, dead and alive
     */
    public static void clearWorld() {
        population.clear();
        for (int i = 0; i < world.size(); i++){
            world.get(i).clear();
            hasWalked.get(i).clear();
        }
    }

    /**
     * Store information regarding a Critter's previous location rather than storing the full Critter
     */
    private static class PrevLoc{
        private int prevX;
        private int prevY;
        private String stringRep;

        /**
         * Constructor for PrevLoc and only interface with the class
         * @param prevX is the previous x location before doTimeStep
         * @param prevY is the previous y location before doTimeStep
         * @param stringRep is the Critter's toString value
         */
        PrevLoc(int prevX, int prevY, String stringRep){
            this.prevX = prevX;
            this.prevY = prevY;
            this.stringRep = stringRep;
        }
    }

    /**
     * Handles all of the logic of a Critter interacting with its environment. Flow is:
     *      <ul>
     *          <li>Calling {@link #doTimeStep()} on every Critter in population</li>
     *          <li>Removing creatures that died after {@link #doTimeStep()}</li>
     *          <li>Searching through the grid to check if any Critters occupy the same site.
     *          The Critters in this spot will then fight until only one (or none) Critter remains</li>
     *          <li>Adding the reproduced babies to the population</li>
     *          <li>Adding more {@link Algae}</li> critters based on {@link Params}
     *          <li>Reducing all the Critters in population's energy for resting</li>
     *          <li>Culling any Critters whose energy fell below zero</li>
     *      </ul>
     */
    public static void worldTimeStep() {
        prevLocs.clear();
        for (List<Critter> critterList: world){
            if (critterList.isEmpty()){
                prevLocs.add(null);
            } else{
                Critter critter = critterList.get(0);
                prevLocs.add(new PrevLoc(critter.x_coord, critter.y_coord, critter.toString()));
            }
        }

        for (Critter critter : population) {
            critter.doTimeStep();
        }

        removeDead();

        for (List<Critter> critterList: world){
            while (critterList.size() >= 2){
                fighters[0] = critterList.get(0);
                fighters[1] = critterList.get(1);
                Critter loser = battle(fighters[0], fighters[1]);
                if (loser != null) {
                    removeFromWorld(loser);
                    population.remove(loser);
                }
                fighters[0] = null;
                fighters[1] = null;
            }
        }

        for (Critter critter: population){
            critter.energy-=Params.rest_energy_cost;
            int index = world.get(convertTo1D(critter.x_coord, critter.y_coord)).indexOf(critter);
            hasWalked.get(convertTo1D(critter.x_coord,critter.y_coord)).set(index, false);
        }

        for (int i = 0; i < Params.refresh_algae_count; i++){
            try {
                makeCritter(Algae.class.getSimpleName());
            } catch (InvalidCritterException e){
                e.printStackTrace();
            }
        }

        for (Critter critter: babies){
            addToWorld(critter);
            population.add(critter);
        }
        babies.clear();

        removeDead();

    }

    /**
     * Battle between two Critters. Critters that do not want to fight can try to run away if they have not moved yet.
     * If a Critter tries to run away and fails, it will fight the opposing Critter with a roll of 0. If any of the Critters
     * decide to fight, then they roll a number between 0 - (Critter.energy-1). Whichever Critter has the higher roll will be
     * rewarded with 1/2 of the opposing Critter's energy rounded down, and the loser will be removed from the simulation. If
     * both Critters roll the same number, or both 0, then the winner will be chosen randomly.
     * @param A is a {@link Critter} that will be fighting
     * @param B is a {@link Critter} that will be fighting
     * @return a Critter that has lost and needs to be removed, or null if a Critter either died for trying to run away or successfully ran away
     */
    private static Critter battle(Critter A, Critter B){
        boolean action1 = A.fight(B.toString());
        boolean action2 = B.fight(A.toString());
        boolean didOneDie = removeDead();
        if ((A.x_coord != B.x_coord) || (A.y_coord != B.y_coord) || didOneDie){
            return null;
        }

        int rollA = action1 ? getRandomInt(A.energy) : 0;
        int rollB = action2 ? getRandomInt(B.energy) : 0;

        if (rollA > rollB){
            A.energy+=B.energy/2;
            return B;
        } else if (rollA < rollB){
            B.energy+=A.energy/2;
            return A;
        } else{
            int random = getRandomInt(2);
            if (random == 0){
                A.energy += B.energy/2;
                return B;
            } else{
                B.energy += A.energy/2;
                return A;
            }
        }
    }

    /**
     * Searches through the population to see if any of the Critter's energy fell below zero.
     * @return a true if a Critter died, false if not.
     */
    private static boolean removeDead(){
        boolean didOneDie = false;
        List<Critter> toRemove = new ArrayList<>();
        for (Critter critter: population){
            if (critter.energy <= 0) {
                didOneDie = true;
                removeFromWorld(critter);
                toRemove.add(critter);
            }
        }
        for (Critter critter: toRemove){
            population.remove(critter);
        }
        return didOneDie;
    }

    /**
     * Method for filling in the grid ({@link GridPane}) of the JavaFX GUI. The contents of {@link #world} is looped through
     * and the Critter (or no Critter) that exists at each x, y position is placed into the grid based upon the Critter's
     * view characteristics
     * @param grid is the grid to place the graphical representations of the critters
     */
    public static void displayWorld(Object grid) {
        GridPane gridPane = (GridPane) grid;
        int x = 0;
        int y = 0;

        /*
        int param = Params.world_width >= Params.world_height ? Params.world_width: Params.world_height;
        for (int i = 0; i < param*param; i++) {
            if (x >= param) {
                y++;
                x = 0;
            }
            Rectangle rectangle = new Rectangle(pixelwidth, pixelwidth);
            rectangle.setFill(Color.WHITE);
            gridPane.add(rectangle, x, y);
            x++;
        }
        x=0;
        y=0;
        */

        for (List<Critter> list: world){
            if (x >= Params.world_width){
                y++;
                x=0;
            }
            Rectangle rectangle = new Rectangle(pixelwidth, pixelwidth);
            rectangle.setFill(Color.LIGHTSLATEGREY);
            gridPane.add(rectangle, x, y);
            if (!list.isEmpty()){
                gridPane.add(getShape(list.get(0)), x, y);
            }
            x++;
        }
    }

    /**
     * Helper method to bridge the {@link CritterShape} value and the actual graphical representation. Depending on the
     * Critter's view characteristics, a shape will be constructed and colored.
     * @param critter is the critter to graphically represent
     * @return is a {@link Shape} that will represent the critter
     */
    private static Shape getShape(Critter critter){
        Shape shape = null;
        switch (critter.viewShape()){
            case SQUARE:
                shape = new Rectangle(pixelwidth, pixelwidth);
                break;

            case CIRCLE:
                shape = new Circle(pixelwidth/2);
                break;

            case TRIANGLE: {
                double width = (double) pixelwidth;
                Polygon polygon = new Polygon();
                polygon.getPoints().addAll(0.0, 0.0, width / 2, width, width, 0.0); //1.0, width, width/2+0.5, 1.0, width, width
                shape = polygon;
                break;
            }
            case DIAMOND:{
                double width = (double) pixelwidth;
                Polygon polygon = new Polygon();
                polygon.getPoints().addAll(0.0, width/2, width/2, width, width, width/2, width/2, 0.0);
                shape = polygon;
                break;
            }
            case STAR:{
                double width = (double) pixelwidth/6;
                Polygon polygon = new Polygon();
                polygon.getPoints().addAll(0.0, width * 3,
                        width * 2, width * 2,
                        width * 3, 0.0,
                        width * 4, width * 2,
                        width * 6, width * 3,
                        width * 4, width * 4,
                        width * 3, width * 6,
                        width * 2, width * 4);
                shape = polygon;
                break;
            }
        }
        shape.setFill(critter.viewFillColor());
        shape.setStrokeType(StrokeType.INSIDE);
        if (critter.viewOutlineColor() != Color.WHITE) {
            shape.setStroke(critter.viewOutlineColor());
        }
        return  shape;
    }

    /**
     * Converts a 2D position to 1D
     * @param x is the x-coordinate
     * @param y is the y-coordinate
     * @return y*(world width) + x
     */
    private static int convertTo1D(int x, int y){
        int result = y*Params.world_width+x;
        return result;
    }

    /**
     * Adds a Critter to the world map according to it's (x,y) position as well as a corresponding boolean false to the
     * hasWalked list to denote that the critter has not moved
     * @param critter is the Critter to add to the world
     */
    private static void addToWorld(Critter critter){
        world.get(convertTo1D(critter.x_coord, critter.y_coord)).add(critter);
        hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord)).add(false);
    }

    /**
     * Removes a Critter from the world as well as its corresponding walk boolean from the hasWalked list
     * @param critter is the Critter to remove from the world
     */
    private static void removeFromWorld(Critter critter){
        int index = world.get(convertTo1D(critter.x_coord, critter.y_coord)).indexOf(critter);
        world.get(convertTo1D(critter.x_coord, critter.y_coord)).remove(index);
        hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord)).remove(index);
    }

    /**
     * Mark that a critter has walked by setting it's boolean flag to true
     * @param critter
     */
    private static void markAsWalked(Critter critter){
        int index = world.get(convertTo1D(critter.x_coord, critter.y_coord)).indexOf(critter);
        hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord)).set(index, true);
    }

    /**
     * Checks the Critters boolean flag to see if it has walked
     * @param critter is the Critter to check
     * @return a true if walked, false if not
     */
    private static boolean checkIfWalked(Critter critter){
        int index = world.get(convertTo1D(critter.x_coord, critter.y_coord)).indexOf(critter);
        boolean result = hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord)).get(index);
        return result;
    }

}