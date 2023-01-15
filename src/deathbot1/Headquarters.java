package deathbot1;

import battlecode.common.*;

public class Headquarters extends RobotPlayer {

    

    static boolean buyCarrierNextRound = false;
    static Island islandToAttack = null;
    
    public  static void initHeadquarters(RobotController rc) throws GameActionException {
        System.out.println("Initiating headquarters");
        ownHQ = rc.getLocation();
    }
    
    public static void runHeadquarters(RobotController rc) throws GameActionException {
        // buy carrier with an anchor
        if (buyCarrierNextRound) {
            Island islandToAttack = sharedIslands.get(numAnchorsBuilt - 1);

            Direction dir = rc.getLocation().directionTo(islandToAttack.loc);
            for (int i = 0; i < 8; i++) {
                if (rc.canBuildRobot(RobotType.CARRIER, rc.getLocation().add(dir))) {
                    rc.buildRobot(RobotType.CARRIER, rc.getLocation().add(dir));
                    buyCarrierNextRound = false;
                    break;
                }
                dir = dir.rotateLeft();
            }
            
            rc.writeSharedArray(Consts.CARRIER_ANCHOR_ARRAY_INDEX, islandToAttack.index);

            numCarriers++;
            buyCarrierNextRound = false;
        }

        // 4 starting carriers 
        Direction dir_carrier = directions[(numCarriers * 2) % 8];
        MapLocation loc_carrier = rc.getLocation().add(dir_carrier);

        int wantedCarriers = 4;
        if (numCarriers <= wantedCarriers && rc.canBuildRobot(RobotType.CARRIER, loc_carrier)) {
            rc.setIndicatorString("Building starter bots");
            
            rc.buildRobot(RobotType.CARRIER, loc_carrier);
            numCarriers++;
        }
        // building anchors
        if (sharedIslands.size() > numAnchorsBuilt &&
                rc.canBuildAnchor(Anchor.STANDARD) && 
                rc.getResourceAmount(ResourceType.ADAMANTIUM) >= (50 + 100) && 
                rc.getResourceAmount(ResourceType.MANA) >= 100) {

            System.out.println("Shared islands size " + sharedIslands.size() + " num anchors built " + numAnchorsBuilt);
            
            rc.setIndicatorString("Building anchor! " + Anchor.STANDARD);
            
            rc.buildAnchor(Anchor.STANDARD);

            buyCarrierNextRound = true;

            numAnchorsBuilt++;
        }
        
        // spawning launchers
        // MapLocation enemyHQ = ownHQ.translate((mapSize[0]/2 - ownHQ.x)* 2, (mapSize[1]/2 - ownHQ.y)* 2);
        MapLocation launcherTargetLoc = new MapLocation(mapSize[0] - ownHQ.x, mapSize[1] - ownHQ.y);

        Direction launcher_dir = ownHQ.directionTo(launcherTargetLoc);
        MapLocation launcher_loc = rc.getLocation().add(launcher_dir);
        
        if (rc.getResourceAmount(ResourceType.MANA) >= 160 && rc.getRoundNum() > wantedCarriers + 1 && rc.canBuildRobot(RobotType.LAUNCHER, launcher_loc)) {
            rc.setIndicatorString("Building launchers");
            
            rc.buildRobot(RobotType.LAUNCHER, launcher_loc); // TODO: make them useful
            numLaunchers++;
        }
    }

}
