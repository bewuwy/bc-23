package deathbot1;

import battlecode.common.*;

public class Headquarters extends RobotPlayer {
    
    public  static void initHeadquarters(RobotController rc) {
        System.out.println("Initiating headquarters");
        ownHQ = rc.getLocation();
    }
    
    public static void runHeadquarters(RobotController rc) throws GameActionException {
        // 4 starting carriers 
        int wantedCarriers = 4;
        if (numCarriers <= wantedCarriers) {
            rc.setIndicatorString("Building starter bots");

            Direction dir = directions[(numCarriers * 2) % 8];
            MapLocation loc = rc.getLocation().add(dir);
            
            rc.buildRobot(RobotType.CARRIER, loc);
            numCarriers++;
        }

        // spawning launchers
        Direction launcher_dir = directions[rng.nextInt(8)];
        MapLocation launcher_loc = rc.getLocation().add(launcher_dir);

        int wantedLaunchers = (int)(3 * Math.log(rc.getRoundNum()) - 1);
        if (numLaunchers < wantedLaunchers && rc.getRoundNum() > wantedCarriers + 1 && rc.canBuildRobot(RobotType.LAUNCHER, launcher_loc)) {
            rc.setIndicatorString("Building launchers");
            
            rc.buildRobot(RobotType.LAUNCHER, launcher_loc);
            numLaunchers++;
        }

        if (sharedIslands.size() > numAnchorsBuilt && rc.canBuildAnchor(Anchor.STANDARD)) {
            rc.setIndicatorString("Building anchor! " + Anchor.STANDARD);
        
            rc.buildAnchor(Anchor.STANDARD);
            numAnchorsBuilt++;
        }
    }
}
