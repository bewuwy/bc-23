package deathbot1;

import battlecode.common.*;

public class Launcher extends RobotPlayer {
    public static void initLauncher(RobotController rc) throws GameActionException {
        System.out.println("Initiating carrier");

        MapLocation myLoc = rc.getLocation();
        robotDirection = ownHQ.directionTo(myLoc);

        // Create a zigzag search path
        class ZigZagger {
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

        ZigZagger zg = new ZigZagger();
        
        switch (robotDirection) {
            case NORTH:
                zg.createZigZagSearchPath(0, 3, -9, 9, 9, 9);
                break;
            case NORTHEAST:
                zg.createZigZagSearchPath(3, 3, 0, 12, 12, 0);
                break;
            case EAST:
                zg.createZigZagSearchPath(3, 0, 9, 9, 9, -9);
                break;
            case SOUTHEAST:
                zg.createZigZagSearchPath(3, -3, 12, 0, 0, -12);
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
            if (rc.canMove(dir)) {
                dfs(rc, dir);
            }
        } else {
            // If there are no enemies, move away from own HQ
            // try to go in the set path
            rc.setIndicatorString("Moving to " + searchPath.get(0).toString());
            if(searchPath.get(0).isAdjacentTo(myLocation)) {
                searchPath.remove(0);
            }
            Direction dir = myLocation.directionTo(searchPath.get(0));
            if (rc.canMove(dir)) {
                dfs(rc, dir);
            }
        }
    }

}
