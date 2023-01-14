package deathbot1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;


public class Carrier extends RobotPlayer {
    public static void initCarrier(RobotController rc) throws GameActionException {
        System.out.println("Initiating carrier");

        MapLocation myLoc = rc.getLocation();
        robotDirection = ownHQ.directionTo(myLoc);

        // Create a zigzag search path
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
        
        // Draw the search path on the map
        for (int i = 0; i < searchPath.size()-1; i++) {
            MapLocation m = searchPath.get(i);
            MapLocation m2 = searchPath.get(i + 1);
            rc.setIndicatorLine(m, m2, 255, 255, 255);
            rc.setIndicatorString("searchPath: " + m + " " + m2);
        }

        currentCourierStatus = courierStatus.GATHERING;
    }
    
    public static void runCarrier(RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        if(myWell != null){
            rc.setIndicatorString(currentCourierStatus.toString() + " " + myLocation.toString() + " is adjacent to well " + myLocation.isAdjacentTo(myWell) + " " + myWell.toString());
        }
        if (rc.getAnchor() != null) { // TODO
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
                        if (rc.canMove(dir) && currentCourierStatus == courierStatus.GATHERING) {
                            rc.move(dir);
                        }
                    }
                }
                else {
                    WellInfo[] wells = rc.senseNearbyWells();
                    for (WellInfo well : wells) {
                        Direction dir = myLocation.directionTo(well.getMapLocation());
                        if (dir == robotDirection || dir == robotDirection.rotateRight() || !well.getMapLocation().isWithinDistanceSquared(ownHQ, 20)){
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
    }

}
