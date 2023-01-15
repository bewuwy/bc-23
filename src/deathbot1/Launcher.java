package deathbot1;

import battlecode.common.*;

public class Launcher extends RobotPlayer {
    public static void initLauncher(RobotController rc) throws GameActionException {
        // System.out.println("Initiating launcher");

        MapLocation myLoc = rc.getLocation();
        robotDirection = ownHQ.directionTo(myLoc);

        // // Create a zigzag search path

        // ZigZagger zg = new ZigZagger(myLoc);
        
        // switch (robotDirection) {
        //     case NORTH:
        //         zg.createZigZagSearchPath(0, 3, -9, 9, 9, 9);
        //         break;
        //     case NORTHEAST:
        //         zg.createZigZagSearchPath(3, 3, 0, 12, 12, 0);
        //         break;
        //     case EAST:
        //         zg.createZigZagSearchPath(3, 0, 9, 9, 9, -9);
        //         break;
        //     case SOUTHEAST:
        //         zg.createZigZagSearchPath(3, -3, 12, 0, 0, -12);
        //         break;
        //     case SOUTH:
        //         zg.createZigZagSearchPath(0, -3, -9, 9, -9, -9);
        //         break;
        //     case SOUTHWEST:
        //         zg.createZigZagSearchPath(-3, -3, -12, 0, 0, -12);
        //         break;
        //     case WEST:
        //         zg.createZigZagSearchPath(-3, 0, -9, -9, 9, -9);
        //         break;
        //     case NORTHWEST:
        //         zg.createZigZagSearchPath(-3, 3, 0, -12, 12, 0);
        //         break;
        //     case CENTER:
        //         break;
        // }

        // searchPath.remove(0);
    }

    public static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            // MapLocation toAttack = enemies[0].location;
            MapLocation toAttack = enemies[0].getLocation();

            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");        
                rc.attack(toAttack);
            }
        }

        // Launcher movement
        MapLocation myLocation = rc.getLocation();
        if (enemies.length > 0) {
            // If there are enemies, move towards them
            Direction dir = myLocation.directionTo(enemies[0].location);
            dfs(rc, dir);
        } else if (returnToHQ) {

            // If we are returning to HQ, move towards HQ
            Direction dir = myLocation.directionTo(ownHQ);
            dfs(rc, dir);      
        } else {

            // // try to go in the set path
            // rc.setIndicatorString("Moving to " + searchPath.get(0).toString());
            // if(searchPath.get(0).isAdjacentTo(myLocation)) {
            //     searchPath.remove(0);
            // }
            // Direction dir = myLocation.directionTo(searchPath.get(0));
            
            // Direction dir = myLocation.directionTo(ownHQ).opposite();
            MapLocation launcherTargetLoc = new MapLocation(mapSize[0] - ownHQ.x, mapSize[1] - ownHQ.y);

            Direction dir = myLocation.directionTo(launcherTargetLoc);

            dfs(rc, dir);
        }
    }

}
