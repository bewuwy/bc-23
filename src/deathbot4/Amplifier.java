package deathbot4;

import battlecode.common.*;

public class Amplifier extends RobotPlayer {

    public static void initAmplifier(RobotController rc) throws GameActionException{

        MapLocation myLoc = rc.getLocation();
        robotDirection = ownHQ.directionTo(myLoc);

    }

    static void runAmplifier(RobotController rc) throws GameActionException {
        
        //* find launchers nearby and follow one
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

        MapLocation target = rc.getLocation();

        for (RobotInfo robot : nearbyRobots) {
            if (robot.type == RobotType.LAUNCHER) {
                
                target = robot.location;
            }
        }

        Direction dir = rc.getLocation().directionTo(target);
        dfs.go(rc, dir);
    }
}
