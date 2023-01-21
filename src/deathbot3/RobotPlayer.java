package deathbot3;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public strictfp class RobotPlayer {

    static class ZigZagger {

        MapLocation myLoc;

        public ZigZagger(MapLocation myLoc) {
            this.myLoc = myLoc;
        }

        void createZigZagSearchPath(int x, int y, int xStep, int xStep2, int yStep, int yStep2) {
            searchPath.add(myLoc.translate(x, y));
            searchPath.add(searchPath.get(0));
            MapLocation nextLoc;
            boolean isDone = false;
            for (int i = 1; i < 20; i += 4) {
                nextLoc = searchPath.get(i).translate(xStep, yStep);
                if (nextLoc.x > mapSize[0]){
                    nextLoc = new MapLocation(mapSize[0], nextLoc.y);
                    isDone = true;
                }

                if (nextLoc.y > mapSize[1]){
                    nextLoc = new MapLocation(nextLoc.x, mapSize[1]);
                    isDone = true;
                }

                if(nextLoc.x < 0){
                    nextLoc = new MapLocation(0, nextLoc.y);
                    isDone = true;
                }
                    
                if(nextLoc.y < 0){
                    nextLoc = new MapLocation(nextLoc.x, 0);
                    isDone = true;
                }
                searchPath.add(nextLoc);
                if (isDone){
                    break;
                }
                
                nextLoc  = searchPath.get(i - 1).translate(xStep2, yStep2);
                if (nextLoc.x > mapSize[0]){
                    nextLoc = new MapLocation(mapSize[0], nextLoc.y);
                    isDone = true;
                }

                if (nextLoc.y > mapSize[1]){
                    nextLoc = new MapLocation(nextLoc.x, mapSize[1]);
                    isDone = true;
                }

                if(nextLoc.x < 0){
                    nextLoc = new MapLocation(0, nextLoc.y);
                    isDone = true;
                }

                if(nextLoc.y < 0){
                    nextLoc = new MapLocation(nextLoc.x, 0);
                    isDone = true;
                }

                searchPath.add(nextLoc);
                if (isDone){
                    break;
                }
                nextLoc = searchPath.get(i + 2).translate(xStep2, yStep2);

                if (nextLoc.x > mapSize[0]){
                    nextLoc = new MapLocation(mapSize[0], nextLoc.y);
                    isDone = true;
                }

                if (nextLoc.y > mapSize[1]){
                    nextLoc = new MapLocation(nextLoc.x, mapSize[1]);
                    isDone = true;
                }

                if(nextLoc.x < 0){
                    nextLoc = new MapLocation(0, nextLoc.y);
                    isDone = true;
                }

                if(nextLoc.y < 0){
                    nextLoc = new MapLocation(nextLoc.x, 0);
                    isDone = true;
                }

                searchPath.add(nextLoc);

                if (isDone){
                    break;
                }

                nextLoc = searchPath.get(i + 1).translate(xStep, yStep);

                if (nextLoc.x > mapSize[0]){
                    nextLoc = new MapLocation(mapSize[0], nextLoc.y);
                    isDone = true;
                }

                if (nextLoc.y > mapSize[1]){
                    nextLoc = new MapLocation(nextLoc.x, mapSize[1]);
                    isDone = true;
                }

                if(nextLoc.x < 0){
                    nextLoc = new MapLocation(0, nextLoc.y);
                    isDone = true;
                }

                if(nextLoc.y < 0){
                    nextLoc = new MapLocation(nextLoc.x, 0);
                    isDone = true;
                }

                searchPath.add(nextLoc);

                if (isDone){
                    break;
                }
                
            }
        }
    }

    static Direction lastDfs;

    static void dfs(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            lastDfs = dir;
        } else {
            if (lastDfs != null) {
                Direction[] dirs = {lastDfs, lastDfs.rotateLeft(), lastDfs.rotateRight(), lastDfs.rotateLeft().rotateLeft(), lastDfs.rotateRight().rotateRight()};
                for (Direction d : dirs) {
                    if (rc.canMove(d)) {
                        rc.move(d);
                        lastDfs = d;
                        break;
                    }
                }
            } else{
                Direction[] dirs = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
                for (Direction d : dirs) {
                    if (rc.canMove(d)) {
                        rc.move(d);
                        lastDfs = d;
                        break;
                    }
                }
            }
        }
    }

    
    static Direction robotDirection;
    static List<MapLocation> searchPath = new ArrayList<MapLocation>();
    
    static int turnCount = 0;
    static MapLocation ownHQ;
    static int[] mapSize = new int[2];

    static boolean returnToHQ = false;

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

    // static char[] internalMap = new char[3600];

    static List<Island> sharedIslands = new ArrayList<>();

    static List<Island> newIslands = new ArrayList<>();

    enum IslandOwner{
        NEUTRAL,
        ENEMY,
        FRIENDLY
    }

    // Wrapper class for island(map location and index)
    static class Island {
        MapLocation loc;
        int index;
        IslandOwner owner;

        public Island(MapLocation loc, int index, IslandOwner owner) {
            this.loc = loc;
            this.index = index;
            this.owner = owner;
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
    static int islandToInt(Island island) {
        return  (island.owner.ordinal() << 12) + (island.loc.x << 6) + island.loc.y;
    }

    // Convert an integer to a Island for use in the internal map
    static Island intToIsland(int int_val, int index) {
        return new Island(new MapLocation(int_val >>> 6 & 63, int_val & 63), index, IslandOwner.values()[(int_val >>> 12) & 3]);
    }

    static IslandOwner islandOwner(RobotController rc, int index) throws GameActionException {
        // TODO: make this work somehow
        // return rc.senseAnchor(index).team == rc.getTeam() ? IslandOwner.FRIENDLY : IslandOwner.ENEMY;

        return IslandOwner.NEUTRAL;
    }

    // Download islands from shared array
    private static void downloadIslands(RobotController rc) throws GameActionException {
        int i = 1;
        while (i < 36) {
            
            int island = rc.readSharedArray(i);
            if (island == 0) {
                break;
            }

            // System.out.println("Downloading islands: " + island + " " + i);
            // //print shared islands size
            // System.out.println("Shared Islands Size: " + sharedIslands.size());
            
            if (sharedIslands.contains(intToIsland(island, i))) {
                i++;
                continue;
            }

            sharedIslands.add(intToIsland(island, i));
            if(newIslands.contains(intToIsland(island, i))){
                newIslands.remove(intToIsland(island, i));
            }

            i++;
        }
    }

    // put newIslands into shared array - should work *now*
    private static void shareIslands(RobotController rc) throws GameActionException {
        
        if (rc.canWriteSharedArray(0, 0)) { // testing if we can write to the shared array

            for (int i = 0; i < newIslands.size(); i++) {
                Island island = newIslands.get(0);

                // if (sharedIslands.contains(island)) {
                //     newIslands.remove(0);
                //     continue;
                // }

                rc.writeSharedArray(island.index, islandToInt(island));
                newIslands.remove(0);
                sharedIslands.add(island);
            }
        }
    }

    // look at the terrain around you and save it to the internal map
    // save any islands to newIslands
    private static void scout(RobotController rc) throws GameActionException {
        // MapInfo[] visibleMap = rc.senseNearbyMapInfos();
        // for (int i = 0; i < visibleMap.length; i++) {
        //     MapInfo loc_info = visibleMap[i];
        //     MapLocation loc = loc_info.getMapLocation();

        //     if(loc_info.hasCloud()) {
        //         internalMap[loc.x*60 + loc.y] = 'c';
        //     }
        //     else if(!loc_info.isPassable()) {
        //         internalMap[loc.x*60 + loc.y] = 'b';
        //     } else switch(loc_info.getCurrentDirection()) {
        //         case NORTH:
        //         internalMap[loc.x*60 + loc.y] = 'n';
        //             break;
        //         case SOUTH:
        //         internalMap[loc.x*60 + loc.y] = 's';
        //             break;
        //         case EAST:
        //         internalMap[loc.x*60 + loc.y] = 'e';
        //             break;
        //         case WEST:
        //         internalMap[loc.x*60 + loc.y] = 'w';
        //             break;
        //         case CENTER:
        //         internalMap[loc.x*60 + loc.y] = 'n';
        //             break;
        //         case NORTHEAST:
        //             break;
        //         case NORTHWEST:
        //             break;
        //         case SOUTHEAST:
        //             break;
        //         case SOUTHWEST:
        //             break;
        //     }
        // }

        // check for islands
        int[] islands_index = rc.senseNearbyIslands();
        for (int i : islands_index) {
            Island island = new Island(rc.senseNearbyIslandLocations(i)[0], i, islandOwner(rc, i));
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
        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        mapSize[0] = rc.getMapWidth();
        mapSize[1] = rc.getMapHeight();

        while (true) {
            turnCount += 1;

            try {                
                // Init the bots
                if (turnCount == 1) {
                    // set ownHQ
                    if (rc.getType() != RobotType.HEADQUARTERS) {
                    
                        RobotInfo[] l = rc.senseNearbyRobots(-1);
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
                        case AMPLIFIER: Amplifier.initAmplifier(rc); break; // TODO
                    }
                }

                // Run the bots
                // else {
                    switch (rc.getType()) {
                        case HEADQUARTERS: Headquarters.runHeadquarters(rc); break;
                        case CARRIER: Carrier.runCarrier(rc); break;
                        case LAUNCHER: Launcher.runLauncher(rc); break;
                        case BOOSTER: break; // TODO
                        case DESTABILIZER: break; // TODO
                        case AMPLIFIER: Amplifier.runAmplifier(rc); break; // TODO
                    }

                    // End of turn actions

                    // if(turnCount == 1){
                    //     Arrays.fill(internalMap, 'u');
                    // }
                    
                    // download new islands
                    downloadIslands(rc);

                    // gather and share information
                    if (turnCount % 3 == 1) {
                        scout(rc);

                        // if can't share, go back to HQ
                        if (!rc.canWriteSharedArray(0, 0) && newIslands.size() > 0 && rc.getType() == RobotType.LAUNCHER) {

                            rc.setIndicatorString("Going back to HQ to share islands");

                            returnToHQ = true;
                        } else {
                            returnToHQ = false;
                        }

                        shareIslands(rc);
                    }
                // }
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
