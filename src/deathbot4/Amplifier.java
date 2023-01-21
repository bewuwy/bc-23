package deathbot4;

import battlecode.common.*;

public class Amplifier extends RobotPlayer {

    public static void initAmplifier(RobotController rc) throws GameActionException{

        MapLocation myLoc = rc.getLocation();
        robotDirection = ownHQ.directionTo(myLoc);

    }

    static void runAmplifier(RobotController rc) throws GameActionException {
        if(searchPath.isEmpty()){
            searchPath.add(new MapLocation(rng.nextInt(mapSize[0]),rng.nextInt(mapSize[1])));
        }
        
        rc.setIndicatorString("Amplifier: " + searchPath.get(0).toString());

        Direction dir = rc.getLocation().directionTo(searchPath.get(0));
        dfs.go(rc, dir);

        if(rc.getLocation().isAdjacentTo(searchPath.get(0))){
            searchPath.remove(0);
        }
    }
}
