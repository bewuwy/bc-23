package deathbot1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.HashSet;
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
    static List<MapLocation> searchPath = new ArrayList<MapLocation>();
    // static MapLocation[] wellsFound;
    static MapLocation myWell = null;

    // HQ VARS
    static int numCarriers = 0;
    static int numLaunchers = 0;
    static int numAnchorsBuilt = 0;

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

    //! Map Code goes here ----------------------------------------------

    static enum terrainTypes{
        EMPTY,
        WALL,
        CLOUD,
        CURRENT_NORTH,
        CURRENT_EAST,
        CURRENT_SOUTH,
        CURRENT_WEST,
        ISLAND,
        WELL,
        HQ,
        NORMAL,
        UNKNOWN
    }

    static terrainTypes[][] internalMap = new terrainTypes[64][64];

    static List<Island> sharedIslands = new ArrayList<>();

    static List<Island> newIslands = new ArrayList<>();


    // Wrapper class for island(map location and index)
    static class Island {
        MapLocation loc;
        int index;

        public Island(MapLocation loc, int index) {
            this.loc = loc;
            this.index = index;
        }

        //equals method on index
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Island)) {
                return false;
            }
            Island i = (Island) o;
            return i.index == this.index;
        }
    }

    //Convert an Island to an integer for use in the shared array
    private static int IslandToInt(Island island) {
        return (island.index << 11) + (island.loc.x << 5) + island.loc.y%32;
    }

    //Convert an integer to a Island for use in the internal map
    private static Island intToIsland(int loc) {
        return new Island(new MapLocation(loc >> 5 & 0x3F, loc & 0x20), loc >> 12);
    }

    // download islands from shared array
    private static void downloadIslands(RobotController rc) throws GameActionException {
        int i = 0;
        while (sharedIslands.size()+i < 35) {

            int island = rc.readSharedArray(sharedIslands.size()+4+i);
            if (island == 0) {
                break;
            }
            sharedIslands.add(intToIsland(island));
            if(newIslands.contains(intToIsland(island))){
                newIslands.remove(intToIsland(island));
            }
            i++;
        }
    }

    // put newIslands into shared array - should work now
    private static void shareIslands(RobotController rc) throws GameActionException {
        
         if(rc.canWriteSharedArray(0, 0)) { // testing if we can write to the shared array
             for (int i = 0; i < newIslands.size(); i++) {
                 Island island = newIslands.get(0);
                 rc.writeSharedArray(sharedIslands.size()+4, IslandToInt(island)); //+4 because the first 5 are reserved for the HQs and symmetry type
                 newIslands.remove(0);
                 sharedIslands.add(island);
             }
         }
    }

    // look at the terrain around you and save it to the internal map
    // save any islands to newIslands
    private static void scout(RobotController rc) throws GameActionException {
        MapInfo[] visibleMap = rc.senseNearbyMapInfos();
        for (int i = 0; i < visibleMap.length; i++) {
            MapInfo loc_info = visibleMap[i];
            MapLocation loc = loc_info.getMapLocation();

            if(loc_info.hasCloud()) {
                internalMap[loc.x][loc.y] = terrainTypes.CLOUD;
            }
            else if(!loc_info.isPassable()) {
                internalMap[loc.x][loc. y] = terrainTypes.WALL;
            } else switch(loc_info.getCurrentDirection()) {
                case NORTH:
                    internalMap[loc.x][loc. y] = terrainTypes.CURRENT_NORTH;
                    break;
                case SOUTH:
                    internalMap[loc.x][loc. y] = terrainTypes.CURRENT_SOUTH;
                    break;
                case EAST:
                    internalMap[loc.x][loc. y] = terrainTypes.CURRENT_EAST;
                    break;
                case WEST:
                    internalMap[loc.x][loc. y] = terrainTypes.CURRENT_WEST;
                    break;
                case CENTER:
                    internalMap[loc.x][loc. y] = terrainTypes.NORMAL;
                    break;
                case NORTHEAST:
                    break;
                case NORTHWEST:
                    break;
                case SOUTHEAST:
                    break;
                case SOUTHWEST:
                    break;
            }
        }
        // check for islands
        int[] islands_index = rc.senseNearbyIslands();
        for (int i : islands_index) {
            Island island = new Island(rc.senseNearbyIslandLocations(i)[0], i);
            if (!sharedIslands.contains(island)) {
                newIslands.add(island); //add islands to newIslands
            }
        }
    }

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

        //init internalMap
        for (int i = 0; i < 60; i++) {
            for (int j = 0; j < 60; j++) {
                internalMap[i][j] = terrainTypes.UNKNOWN;
            }
        }


        while (true) {
            turnCount += 1;

            try {                
                // Init the bots
                if (turnCount == 1) {
                    switch (rc.getType()) {
                        case HEADQUARTERS: initHeadquarters(rc); break;
                        case CARRIER: initCarrier(rc); break;
                        case LAUNCHER: initLauncher(rc); break;
                        case BOOSTER: break; // TODO
                        case DESTABILIZER: break; // TODO
                        case AMPLIFIER: break; // TODO
                    }
                }

                // Run the bots
                else {
                    switch (rc.getType()) {
                        case HEADQUARTERS: runHeadquarters(rc); break;
                        case CARRIER: runCarrier(rc); break;
                        case LAUNCHER: runLauncher(rc); break;
                        case BOOSTER: break; // TODO
                        case DESTABILIZER: break; // TODO
                        case AMPLIFIER: break; // TODO
                    }

                    // End of turn actions

                    // download new islands
                    downloadIslands(rc);

                    // gather and share information
                    if (turnCount % 10 == 0) {
                        scout(rc);
                        shareIslands(rc);
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
        System.out.println("Initiating headquarters");
        ownHQ = rc.getLocation();
    }

    private static void initCarrier(RobotController rc) throws GameActionException {
        System.out.println("Initiating carrier");

        RobotInfo[] l = rc.senseNearbyRobots(1);
        for (RobotInfo r : l) {
            if (r.type == RobotType.HEADQUARTERS) {
                ownHQ = r.location;
            }
        }

        MapLocation myLoc = rc.getLocation();

        courierDirection = ownHQ.directionTo(myLoc);

        //create a zigzag search path
        class ZigZagger {
            void createZigZagSearchPath(int x, int y, int xStep, int xStep2, int yStep, int yStep2) {
                searchPath.add(myLoc.translate(x, y));
                searchPath.add(searchPath.get(0));
                for (int i = 1; i < 20; i += 4) {
                    searchPath.add(searchPath.get(i).translate(xStep, yStep));
                    searchPath.add(searchPath.get(i - 1).translate(xStep2, yStep2));
                    searchPath.add(searchPath.get(i + 2).translate(xStep2, yStep2));
                    searchPath.add(searchPath.get(i + 1).translate(xStep, yStep));
                }
            }
        }

        ZigZagger zg = new ZigZagger();
        
        switch (courierDirection) {
            case NORTH:
                zg.createZigZagSearchPath(0, 3, 9, -9, 9, 9);
                break;
            case NORTHEAST:
                zg.createZigZagSearchPath(3, 3, 12, 0, 0, 12);
                break;
            case EAST:
                zg.createZigZagSearchPath(3, 0, 9, 9, -9, 9);
                break;
            case SOUTHEAST:
                zg.createZigZagSearchPath(3, -3, 0, 12, -12, 0);
                break;
            case SOUTH:
                zg.createZigZagSearchPath(0, -3, -9, 9, -9, -9);
                break;
            case SOUTHWEST:
                zg.createZigZagSearchPath(-3, -3, -12, 0, 0, -12);
                break;
            case WEST:
                zg.createZigZagSearchPath(-3, 0, -9, -9, 9, -9);
                break;
            case NORTHWEST:
                zg.createZigZagSearchPath(-3, 3, 0, -12, 12, 0);
                break;
            case CENTER:
                break;
        }

        searchPath.remove(0);
        
        //draw the search path on the map
        for (int i = 0; i < searchPath.size()-1; i++) {
            MapLocation m = searchPath.get(i);
            MapLocation m2 = searchPath.get(i + 1);
            rc.setIndicatorLine(m, m2, 255, 255, 255);
            rc.setIndicatorString("searchPath: " + m + " " + m2);
        }

        currentCourierStatus = courierStatus.GATHERING;
    }

    private static void initLauncher(RobotController rc) {

    }

    static void runHeadquarters(RobotController rc) throws GameActionException {
        // 4 starting carriers 
        int wantedCarriers = 4;
        if (numCarriers <= wantedCarriers) {
            rc.setIndicatorString("Building starter bots");

            Direction dir = directions[(numCarriers * 2) % 8];
            MapLocation loc = rc.getLocation().add(dir);
            
            rc.buildRobot(RobotType.CARRIER, loc);
            numCarriers++;
        }

        // spawning launchers
        Direction launcher_dir = directions[rng.nextInt(8)];
        MapLocation launcher_loc = rc.getLocation().add(launcher_dir);

        int wantedLaunchers = (int)(3 * Math.log(rc.getRoundNum()) - 1);
        if (numLaunchers < wantedLaunchers && rc.getRoundNum() > wantedCarriers + 1 && rc.canBuildRobot(RobotType.LAUNCHER, launcher_loc)) {
            rc.setIndicatorString("Building launchers");
            
            rc.buildRobot(RobotType.LAUNCHER, launcher_loc);
            numLaunchers++;
        }

        if (sharedIslands.size() > numAnchorsBuilt && rc.canBuildAnchor(Anchor.STANDARD)) {
            rc.setIndicatorString("Building anchor! " + Anchor.STANDARD);
        
            rc.buildAnchor(Anchor.STANDARD);
            numAnchorsBuilt++;
        }
    }

    static void runCarrier(RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        rc.setIndicatorString(currentCourierStatus.toString());
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
                while (!myLocation.equals(islandLocation)) {
                    Direction dir = myLocation.directionTo(islandLocation);
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
            Direction dir = myLocation.directionTo(ownHQ);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
            if (myLocation.isAdjacentTo(ownHQ)){
                for (ResourceType t : ResourceType.values()) {
                    int r =rc.getResourceAmount(t);
                    if (r > 0) {
                        rc.transferResource(ownHQ, t, r);;
                    }
                }
                currentCourierStatus = courierStatus.GATHERING;
            }
        }
        
        // // Occasionally try out the carriers attack
        // if (rng.nextInt(20) == 1) {
        //     RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        //     if (enemyRobots.length > 0) {
        //         if (rc.canAttack(enemyRobots[0].location)) {
        //             rc.attack(enemyRobots[0].location);
        //         }
        //     }
        // }
        
        // If know see a well, collect from there
        if (myWell != null) {
            if(myLocation.isAdjacentTo(myWell)) { // If we are adjacent to the well
                if (rc.canCollectResource(myWell, -1)) { // and we can collect from the well
                    
                    rc.collectResource(myWell, -1);
                    rc.setIndicatorString("Collecting, now have, AD:" + 
                        rc.getResourceAmount(ResourceType.ADAMANTIUM) + 
                        " MN: " + rc.getResourceAmount(ResourceType.MANA) + 
                        " EX: " + rc.getResourceAmount(ResourceType.ELIXIR)); 
                } else {
                    rc.setIndicatorString("Can't collect from well");
                    currentCourierStatus = courierStatus.RETURNING; // If we can't collect from the well, return to HQ
                    myWell = rc.getLocation();
                }
            } else{
                // go towards myWell
                Direction dir = myLocation.directionTo(myWell);
                if (rc.canMove(dir) && currentCourierStatus == courierStatus.GATHERING) {
                    rc.move(dir);
                }
            }
        }
        else {
            WellInfo[] wells = rc.senseNearbyWells();
            for (WellInfo well : wells) {
                Direction dir = myLocation.directionTo(well.getMapLocation());
                if (dir == courierDirection || dir == courierDirection.rotateRight() || !well.getMapLocation().isWithinDistanceSquared(ownHQ, 20)){
                    myWell = well.getMapLocation();
                }
            }
            MapLocation m = searchPath.get(0);
            MapLocation m2 = searchPath.get(1);
            rc.setIndicatorLine(m, m2, 255, 255, 255);
            rc.setIndicatorString("searchPath: " + m + " " + m2);
            // try to go in the set path
            if(searchPath.get(0).isAdjacentTo(myLocation)) {
                searchPath.remove(0);
            }
            Direction dir = myLocation.directionTo(searchPath.get(0));
            if (rc.canMove(dir) && currentCourierStatus == courierStatus.GATHERING) {
                rc.move(dir);
            }
            
        }
        
    }

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
