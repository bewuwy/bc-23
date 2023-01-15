package deathbot1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

public strictfp class RobotPlayer {

    static void dfs(RobotController rc, Direction dir) throws GameActionException {
        Direction[] dirs = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        for (Direction d : dirs) {
            if (rc.canMove(d)) {
                rc.move(d);
                break;
            }
        }
    }

    static int turnCount = 0;

    static Direction robotDirection;
    static List<MapLocation> searchPath = new ArrayList<MapLocation>();

    static MapLocation ownHQ;

    // HQ VARS
    static int numCarriers = 0;
    static int numLaunchers = 0;
    static int numAnchorsBuilt = 0;

    static final Random rng = new Random(6147);

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

    static char[] internalMap = new char[3600];

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

        @Override
        public String toString() {
            return "Island{" +
                    "loc=" + loc +
                    ", index=" + index +
                    '}';
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

    // Convert an Island to an integer for use in the shared array
    private static int IslandToInt(Island island) {
        return (island.index << 11) + (island.loc.x << 5) + island.loc.y%32;
    }

    // Convert an integer to a Island for use in the internal map
    private static Island intToIsland(int loc) {
        return new Island(new MapLocation(loc >> 5 & 0x3F, loc & 0x20), loc >> 12);
    }

    // Download islands from shared array
    private static void downloadIslands(RobotController rc) throws GameActionException {
        int i = 1;
        while (sharedIslands.size()+i < 35) {

            int island = rc.readSharedArray(sharedIslands.size()+4+i);
            if (island == 0) {
                break;
            }

            if (sharedIslands.contains(intToIsland(island))) {
                i++;
                continue;
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
        
        if (rc.canWriteSharedArray(0, 0)) { // testing if we can write to the shared array
            for (int i = 0; i < newIslands.size(); i++) {
                Island island = newIslands.get(0);

                rc.writeSharedArray(sharedIslands.size()+4, IslandToInt(island)); // +4 because the first 5 are reserved for the HQs and symmetry type
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
                internalMap[loc.x*60 + loc.y] = 'c';
            }
            else if(!loc_info.isPassable()) {
                internalMap[loc.x*60 + loc.y] = 'b';
            } else switch(loc_info.getCurrentDirection()) {
                case NORTH:
                internalMap[loc.x*60 + loc.y] = 'n';
                    break;
                case SOUTH:
                internalMap[loc.x*60 + loc.y] = 's';
                    break;
                case EAST:
                internalMap[loc.x*60 + loc.y] = 'e';
                    break;
                case WEST:
                internalMap[loc.x*60 + loc.y] = 'w';
                    break;
                case CENTER:
                internalMap[loc.x*60 + loc.y] = 'n';
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

        //! Init internalMap
        

        while (true) {
            turnCount += 1;

            try {                
                // Init the bots
                if (turnCount == 1) {
                    // set ownHQ
                    if (rc.getType() != RobotType.HEADQUARTERS) {
                    
                        RobotInfo[] l = rc.senseNearbyRobots(2);
                        for (RobotInfo r : l) {
                            if (r.type == RobotType.HEADQUARTERS) {
                                ownHQ = r.location;
                            }
                        }
                    }

                    switch (rc.getType()) {
                        case HEADQUARTERS: Headquarters.initHeadquarters(rc); break;
                        case CARRIER:  Carrier.initCarrier(rc); break;
                        case LAUNCHER: Launcher.initLauncher(rc); break;
                        case BOOSTER: break; // TODO
                        case DESTABILIZER: break; // TODO
                        case AMPLIFIER: break; // TODO
                    }
                }

                // Run the bots
                else {
                    switch (rc.getType()) {
                        case HEADQUARTERS: Headquarters.runHeadquarters(rc); break;
                        case CARRIER: Carrier.runCarrier(rc); break;
                        case LAUNCHER: Launcher.runLauncher(rc); break;
                        case BOOSTER: break; // TODO
                        case DESTABILIZER: break; // TODO
                        case AMPLIFIER: break; // TODO
                    }

                    // End of turn actions

                    // download new islands
                    if(turnCount == 1){
                        Arrays.fill(internalMap, 'u');
                    }
                    downloadIslands(rc);

                    // gather and share information
                    if (turnCount % 3 == 1) {
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
}
