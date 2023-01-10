package deathbot1;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.lang.Math;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    static MapLocation ownHQ;

    // CARRIER VARS
    static enum courierStatus {
        ADAMANTIUM,
        MANA,
        GATHERING,
        ANCHOR,
        RETURNING,
        // NOTHING
    };
    static courierStatus currentCourierStatus = courierStatus.GATHERING;
    static Direction courierDirection;
    // static MapLocation[] wellsFound;
    static MapLocation myWell = null;

    // HQ VARS
    static int numCarriers = 0;
    static int numLaunchers = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.
            turnCount += 1;  // We have now been alive for one more turn!

            try {                
                // Init the bots
                if (turnCount == 1) {
                    switch (rc.getType()) {
                        case HEADQUARTERS:     initHeadquarters(rc);  break;
                        case CARRIER:      initCarrier(rc);   break;
                        case LAUNCHER: initLauncher(rc); break;
                        case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                        case DESTABILIZER: // You might want to give them a try!
                        case AMPLIFIER:       break;
                    }
                }

                // Run the bots
                else {
                    switch (rc.getType()) {
                        case HEADQUARTERS:     runHeadquarters(rc);  break;
                        case CARRIER:      runCarrier(rc);   break;
                        case LAUNCHER: runLauncher(rc); break;
                        case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                        case DESTABILIZER: // You might want to give them a try!
                        case AMPLIFIER:       break;
                    }
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
        }
    }

    private static void initHeadquarters(RobotController rc) {
        
    }

    private static void initCarrier(RobotController rc) throws GameActionException {
        System.out.println("Initiating carrier");

        RobotInfo[] l = rc.senseNearbyRobots(1);
        for (RobotInfo r : l) {
            if (r.type == RobotType.HEADQUARTERS) {
                ownHQ = r.location;
            }
        }

        courierDirection = ownHQ.directionTo(rc.getLocation());
        currentCourierStatus = courierStatus.GATHERING;
    }

    private static void initLauncher(RobotController rc) {

    }

    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        // 4 starting carriers 
        int wantedCarriers = 4;
        if (rc.getRoundNum() <= wantedCarriers) {
            rc.setIndicatorString("Building starter bots");

            Direction dir = directions[(rc.getRoundNum()-1)*2];
            MapLocation loc = rc.getLocation().add(dir);
            
            rc.buildRobot(RobotType.CARRIER, loc);
            numCarriers++;
        }

        int wantedLaunchers = (int)(3 * Math.log(rc.getRoundNum()) - 1);
        if (numLaunchers < wantedLaunchers && rc.getRoundNum() > wantedCarriers + 1 && rc.getResourceAmount(ResourceType.MANA) >= 100) {
            rc.setIndicatorString("Building launchers");

            Direction dir = directions[rng.nextInt(8)];
            MapLocation loc = rc.getLocation().add(dir);
            
            rc.buildRobot(RobotType.LAUNCHER, loc);
            numLaunchers++;
        }

        /*
        if (rc.canBuildAnchor(Anchor.STANDARD)) {
            // If we can build an anchor do it!
            rc.buildAnchor(Anchor.STANDARD);
            rc.setIndicatorString("Building anchor! " + rc.getAnchor());
        }
        */

        // if (rng.nextBoolean()) {
        //     // Let's try to build a carrier.
        //     rc.setIndicatorString("Trying to build a carrier");
        //     if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
        //         rc.buildRobot(RobotType.CARRIER, newLoc);
        //     }
        // } else {
        //     // Let's try to build a launcher.
        //     rc.setIndicatorString("Trying to build a launcher");
        //     if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
        //         rc.buildRobot(RobotType.LAUNCHER, newLoc);
        //     }
        // }
    }

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        if (rc.getAnchor() != null) {
            // If I have an anchor singularly focus on getting it to the first island I see
            int[] islands = rc.senseNearbyIslands();
            Set<MapLocation> islandLocs = new HashSet<>();
            for (int id : islands) {
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                islandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                rc.setIndicatorString("Moving my anchor towards " + islandLocation);
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    rc.setIndicatorString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }

        if (currentCourierStatus == courierStatus.RETURNING) {
            Direction dir = rc.getLocation().directionTo(ownHQ);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
            if (rc.getLocation().isAdjacentTo(ownHQ)){
                for (ResourceType t : ResourceType.values()) {
                    int r =rc.getResourceAmount(t);
                    if (r > 0) {
                        rc.transferResource(ownHQ, t, r);;
                    }
                }
                currentCourierStatus = courierStatus.GATHERING;
            }
        }

        // Try to gather from squares around us.
        // if(rc.senseNearbyWells(1))
         MapLocation myLocation = rc.getLocation();
        // for (int dx = -1; dx <= 1; dx++) {
        //     for (int dy = -1; dy <= 1; dy++) {
        //         MapLocation wellLocation = new MapLocation(myLocation.x + dx, myLocation.y + dy);
        //         if (rc.canCollectResource(wellLocation, -1)) {
        //             rc.collectResource(wellLocation, -1);
        //             rc.setIndicatorString("Collecting, now have, AD:" + 
        //                 rc.getResourceAmount(ResourceType.ADAMANTIUM) + 
        //                 " MN: " + rc.getResourceAmount(ResourceType.MANA) + 
        //                 " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
        //         } else {
        //             currentCourierStatus = courierStatus.RETURNING;
        //             myWell = rc.getLocation();
        //         }
        //     }
        //}
        
        // // Occasionally try out the carriers attack // TODO: is this needed?
        // if (rng.nextInt(20) == 1) {
        //     RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        //     if (enemyRobots.length > 0) {
        //         if (rc.canAttack(enemyRobots[0].location)) {
        //             rc.attack(enemyRobots[0].location);
        //         }
        //     }
        // }
        
        // If we can see a well, move towards it
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 1 && currentCourierStatus == courierStatus.GATHERING) {
            WellInfo well_one = wells[1];
            Direction dir = myLocation.directionTo(well_one.getMapLocation());

            if(myLocation.isAdjacentTo(well_one.getMapLocation())) { // If we are adjacent to the well
                if (rc.canCollectResource(well_one.getMapLocation(), -1)) { // and we can collect from the well
                    
                    rc.collectResource(well_one.getMapLocation(), -1);
                    rc.setIndicatorString("Collecting, now have, AD:" + 
                        rc.getResourceAmount(ResourceType.ADAMANTIUM) + 
                        " MN: " + rc.getResourceAmount(ResourceType.MANA) + 
                        " EX: " + rc.getResourceAmount(ResourceType.ELIXIR)); 
                } else {
                    currentCourierStatus = courierStatus.RETURNING; // If we can't collect from the well, return to HQ
                    myWell = rc.getLocation();
                }
            } else {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }
        }
        else { // No well nearby
            if (myWell != null) {
                // go towards myWell
                Direction dir = myLocation.directionTo(myWell);
                if (rc.canMove(dir) && currentCourierStatus == courierStatus.GATHERING) {
                    rc.move(dir);
                }
            }
            else {
                // try to go in the initial direction

                Direction dir = courierDirection;
                if (rc.canMove(dir) && currentCourierStatus == courierStatus.GATHERING) {
                    rc.move(dir);
                }
                
            }
        }
    }

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length >= 0) {
            // MapLocation toAttack = enemies[0].location;
            MapLocation toAttack = rc.getLocation().add(Direction.EAST);

            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");        
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
