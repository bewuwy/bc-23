package deathbot1;

import battlecode.common.*;

public class Launcher extends RobotPlayer {
    public static void initLauncher(RobotController rc) {

    }

    public static void runLauncher(RobotController rc) throws GameActionException {
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

        // Launcher movement
        if (enemies.length > 0) {
            // If there are enemies, move towards them
            Direction dir = rc.getLocation().directionTo(enemies[0].location);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        } else {
            // If there are no enemies, move away from own HQ
            Direction dir = rc.getLocation().directionTo(ownHQ);

            dir = dir.opposite();

            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

}
