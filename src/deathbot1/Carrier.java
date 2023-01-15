package deathbot1;

import battlecode.common.*;

public class Carrier extends RobotPlayer {

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
    static Island targetIsland;

    public static void initCarrier(RobotController rc) throws GameActionException {
        // System.out.println("Initiating carrier");

        int targetIslandIndex = rc.readSharedArray(Consts.CARRIER_ANCHOR_ARRAY_INDEX);
        int anchorHQID = rc.readSharedArray(Consts.CARRIER_ANCHOR_HQ_ID);
    
        int myHQID = rc.senseRobotAtLocation(ownHQ).getID();

        if (targetIslandIndex != 0 && anchorHQID == myHQID) {

            // targetIslandID loaded, clear it
            targetIsland = intToIsland(rc.readSharedArray(targetIslandIndex), targetIslandIndex);

            isAnchorCourier = true;
            rc.takeAnchor(ownHQ, Anchor.STANDARD);

            rc.writeSharedArray(Consts.CARRIER_ANCHOR_ARRAY_INDEX, 0);
            rc.writeSharedArray(Consts.CARRIER_ANCHOR_HQ_ID, 0);
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
        // MapLocation m = searchPath.get(i);
        // MapLocation m2 = searchPath.get(i + 1);
        // rc.setIndicatorLine(m, m2, 255, 255, 255);
        // rc.setIndicatorString("searchPath: " + m + " " + m2);
        // }

        currentCourierStatus = courierStatus.GATHERING;
    }

    public static void runCarrier(RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        if (myWell != null) {
            rc.setIndicatorString(currentCourierStatus.toString() + " " + myLocation.toString()
                    + " is adjacent to well " + myLocation.isAdjacentTo(myWell) + " " + myWell.toString());
        }

        // ANCHOR BEHAVIOUR
        if (rc.getAnchor() != null) {

            rc.setIndicatorString("si size: " + sharedIslands.size() + " island: " + targetIsland);

            Direction dir = myLocation.directionTo(targetIsland.loc);

            // check if adjacent to target island
            boolean inTargetIsland = false;
            for (MapLocation islandLoc : rc.senseNearbyIslandLocations(targetIsland.index)) {
                if (islandLoc.equals(myLocation)) {
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
            if (myLocation.isAdjacentTo(ownHQ)) {
                for (ResourceType t : ResourceType.values()) {
                    int r = rc.getResourceAmount(t);
                    if (r > 0) {
                        rc.transferResource(ownHQ, t, r);
                        ;
                    }
                }
                currentCourierStatus = courierStatus.GATHERING;
                rc.setIndicatorString("Switching to GATHERING");
            }
        } else {

            // // Occasionally try out the carriers attack
            // if (rng.nextInt(20) == 1) {
            // RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            // if (enemyRobots.length > 0) {
            // if (rc.canAttack(enemyRobots[0].location)) {
            // rc.attack(enemyRobots[0].location);
            // }
            // }
            // }

            // If know see a well, collect from there
            if (myWell != null) {
                if (myLocation.isAdjacentTo(myWell)) { // If we are adjacent to the well
                    if (rc.canCollectResource(myWell, -1)) { // and we can collect from the well

                        rc.collectResource(myWell, -1);
                        rc.setIndicatorString("Collecting, now have, AD:" +
                                rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                                " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                                " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                    } else {
                        currentCourierStatus = courierStatus.RETURNING; // If we can't collect from the well, return to
                                                                        // HQ
                    }
                } else {
                    // go towards myWell
                    Direction dir = myLocation.directionTo(myWell);
                    if (currentCourierStatus == courierStatus.GATHERING) {
                        dfs(rc, dir);
                        dir = myLocation.directionTo(myWell);
                        dfs(rc, dir);
                    }
                }
            } else {
                WellInfo[] wells = rc.senseNearbyWells();
                for (WellInfo well : wells) {
                    Direction dir = myLocation.directionTo(well.getMapLocation());
                    if (true) {
                        myWell = well.getMapLocation();
                    }
                }

                // try to go in the set path
                if (searchPath.size() == 0) {

                    // if we are at the end of the path, go to the center of the map
                    robotDirection = myLocation.directionTo(new MapLocation(mapSize[0] / 2, mapSize[1] / 2));

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
                dir = myLocation.directionTo(searchPath.get(0));
                if (currentCourierStatus == courierStatus.GATHERING) {
                    dfs(rc, dir);
                }

                if (searchPath.get(0).isAdjacentTo(myLocation)) {
                    searchPath.remove(0);
                }

            }
        }
    }

}
