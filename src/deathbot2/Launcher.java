package deathbot2;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.*;

public class Launcher extends RobotPlayer {

    public static final int LAUNCHERS_TO_ATTACK = 5;

    public static void initLauncher(RobotController rc) throws GameActionException {

        MapLocation myLoc = rc.getLocation();
        robotDirection = ownHQ.directionTo(myLoc);
    }

    public static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] robotsNearby = rc.senseNearbyRobots(-1);
        
        List<RobotInfo> enemies = new ArrayList<RobotInfo>();

        for (RobotInfo robot : robotsNearby) {
            if (robot.getTeam() == opponent && robot.getType() != RobotType.HEADQUARTERS) {

                enemies.add(robot);
                break;
            }
        }

        if (enemies.size() > 0) {
            MapLocation toAttack = enemies.get(0).getLocation();

            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");        
                rc.attack(toAttack);
            }
        }

        // Launcher movement
        MapLocation myLocation = rc.getLocation();
        if (returnToHQ) {

            // If we are returning to HQ, move towards HQ
            Direction dir = myLocation.directionTo(ownHQ);
            dfs(rc, dir);      
        } else {

            //! move only towards the enemy HQ if there are many launchers nearby, otherwise go to the center
            int numAllyLaunchersNearby = 0;
            MapLocation enemyHQ = new MapLocation(mapSize[0] - ownHQ.x, mapSize[1] - ownHQ.y); // fail-safe

            for (RobotInfo robot : robotsNearby) {
                if (robot.getType() == RobotType.LAUNCHER && robot.getTeam() == rc.getTeam()) {
                    numAllyLaunchersNearby++;
                }
                if (robot.getType() == RobotType.HEADQUARTERS && robot.getTeam() == rc.getTeam().opponent()) {
                    enemyHQ = robot.getLocation();
                }
            }

            // boolean move = true;
            boolean opposite = false;
            MapLocation launcherTargetLoc;

            // attack
            if (numAllyLaunchersNearby >= LAUNCHERS_TO_ATTACK) {   
                launcherTargetLoc = enemyHQ;
            }
            else {
                // go to center
                launcherTargetLoc = new MapLocation(mapSize[0] / 2, mapSize[1] / 2);
            }

            // if (enemies.size() > 0) {
                //? If there are enemies, move towards them -- stupid?
            //     launcherTargetLoc = enemies.get(0).location;
            // }

            if (myLocation.distanceSquaredTo(enemyHQ) <= 9 && enemies.size() == 0) {
                // If we are close to the enemy HQ, move away from it
                launcherTargetLoc = enemyHQ;
                opposite = true;
            }

            Direction dir = myLocation.directionTo(launcherTargetLoc);

            if (opposite) {
                dir = dir.opposite();
            }

            dfs(rc, dir);
        }
    }

}
