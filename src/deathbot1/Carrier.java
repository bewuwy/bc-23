package deathbot1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;


public class Carrier extends RobotPlayer {
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

    static enum courierStatus {
        ADAMANTIUM,
        MANA,
        GATHERING,
        ANCHOR,
        RETURNING,
        // NOTHING
    };
    static courierStatus currentCourierStatus = courierStatus.GATHERING;
    static MapLocation myWell = null;

    static boolean isAnchorCourier = false;
    static int targetIslandID = 0;
    static MapLocation targetIslandLoc = null;

    public static void initCarrier(RobotController rc) throws GameActionException {
        System.out.println("Initiating carrier");
        targetIslandID = rc.readSharedArray(Consts.CARRIER_ANCHOR_ARRAY_INDEX);

        if (targetIslandID != 0) {
            isAnchorCourier = true;
            rc.takeAnchor(ownHQ, Anchor.STANDARD);
        }

        MapLocation myLoc = rc.getLocation();
        robotDirection = ownHQ.directionTo(myLoc);

        // Create a zigzag search path
        

        ZigZagger zg = new ZigZagger(myLoc);
        
        switch (robotDirection) {
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
        
        // // Draw the search path on the map
        // for (int i = 0; i < searchPath.size()-1; i++) {
        //     MapLocation m = searchPath.get(i);
        //     MapLocation m2 = searchPath.get(i + 1);
        //     rc.setIndicatorLine(m, m2, 255, 255, 255);
        //     rc.setIndicatorString("searchPath: " + m + " " + m2);
        // }

        currentCourierStatus = courierStatus.GATHERING;
    }
    
    public static void runCarrier(RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        if(myWell != null){
            rc.setIndicatorString(currentCourierStatus.toString() + " " + myLocation.toString() + " is adjacent to well " + myLocation.isAdjacentTo(myWell) + " " + myWell.toString());
        }

        // ANCHOR BEHAVIOUR
        if (rc.getAnchor() != null) { // TODO

            if (targetIslandLoc == null && turnCount >= 2) {
                
                // targetIslandLoc = sharedIslands.get(sharedIslands.indexOf(new Island(new MapLocation(0, 0), targetIslandID))).loc;
                for (Island island : sharedIslands) {
                    if (island.index == targetIslandID) {
                        targetIslandLoc = island.loc;
                    }
                }
            }

            Direction dir = myLocation.directionTo(targetIslandLoc);

            // check if adjacent to target island
            boolean inTargetIsland = false;
            for (MapLocation islandLoc : rc.senseNearbyIslandLocations(targetIslandID)) {
                if(islandLoc.equals(myLocation)){
                    inTargetIsland = true;
                }
            }

            if (inTargetIsland) {
                rc.setIndicatorString("Adjacent to target island");

                rc.placeAnchor();
                
            } else if (dir != null) {

                dfs(rc, dir);
            }

            return;
        }

        // GATHERING BEHAVIOUR
        if (currentCourierStatus == courierStatus.RETURNING) {
            Direction dir = myLocation.directionTo(ownHQ);
            dfs(rc, dir);
            if (myLocation.isAdjacentTo(ownHQ)){
                for (ResourceType t : ResourceType.values()) {
                    int r =rc.getResourceAmount(t);
                    if (r > 0) {
                        rc.transferResource(ownHQ, t, r);;
                    }
                }
                currentCourierStatus = courierStatus.GATHERING;
                rc.setIndicatorString("Switching to GATHERING");
            }
        } else {
        
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
                            currentCourierStatus = courierStatus.RETURNING; // If we can't collect from the well, return to HQ
                        }
                    } else{
                        // go towards myWell
                        Direction dir = myLocation.directionTo(myWell);
                        if (currentCourierStatus == courierStatus.GATHERING) {
                            dfs(rc, dir);
                        }
                    }
                }
                else {
                    WellInfo[] wells = rc.senseNearbyWells();
                    for (WellInfo well : wells) {
                        Direction dir = myLocation.directionTo(well.getMapLocation());
                        if (dir == robotDirection || dir == robotDirection.rotateRight() || dir == robotDirection.rotateRight().rotateRight() || !well.getMapLocation().isWithinDistanceSquared(ownHQ, 20)){
                            myWell = well.getMapLocation();
                        }
                    }
                    // try to go in the set path
                    if(searchPath.get(0).isAdjacentTo(myLocation)) {
                        searchPath.remove(0);
                    }
                    if (searchPath.size() == 0) {

                        // if we are at the end of the path, go to the center of the map
                        robotDirection = myLocation.directionTo(new MapLocation(mapSize[0]/2, mapSize[1]/2));

                        ZigZagger zg = new ZigZagger(myLocation);

                        switch (robotDirection) {
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
                    }
                    Direction dir = myLocation.directionTo(searchPath.get(0));
                    if (currentCourierStatus == courierStatus.GATHERING) {
                        dfs(rc, dir);
                    }
                
                }
        }
    }

}
