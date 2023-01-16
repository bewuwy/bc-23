package deathbot1;

import battlecode.common.*;

public class Launcher extends RobotPlayer {

    public static final int LAUNCHERS_TO_ATTACK = 4;

    public static void initLauncher(RobotController rc) throws GameActionException {

        MapLocation myLoc = rc.getLocation();
        robotDirection = ownHQ.directionTo(myLoc);
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

            // move only if there are many launchers nearby
            int numLaunchersNearby = 0;

            for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
                if (robot.getType() == RobotType.LAUNCHER) {
                    numLaunchersNearby++;
                }
            }

            // if too few launchers nearby, don't move
            if (numLaunchersNearby < LAUNCHERS_TO_ATTACK) {
                return;
            }

            MapLocation launcherTargetLoc = new MapLocation(mapSize[0] - ownHQ.x, mapSize[1] - ownHQ.y);
            Direction dir = myLocation.directionTo(launcherTargetLoc);
            dfs(rc, dir);
        }
    }

}
