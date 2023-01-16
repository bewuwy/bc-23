package deathbot1;

import battlecode.common.*;

public class Headquarters extends RobotPlayer {

    static boolean buyCarrierNextRound = false;
    static Island islandToAttack = null;

    public static boolean spawnBot(RobotController rc, MapLocation hq_loc, Direction dir, RobotType type) throws GameActionException {
        rc.setIndicatorString("Building" + type);
        
        MapLocation robotSpawnLocation = hq_loc.add(dir);
        boolean canBuild = rc.canBuildRobot(type, robotSpawnLocation);
        
        MapInfo[] nearby = rc.senseNearbyMapInfos(robotSpawnLocation, 1);

        MapLocation mapLoc;

        for (MapInfo mapInfo : nearby) {
            
            mapLoc = mapInfo.getMapLocation();

            canBuild = rc.canBuildRobot(type, mapLoc);

            if (canBuild) {                
                rc.buildRobot(type, mapLoc);

                switch (type) {
                    case LAUNCHER:
                        numLaunchers++;
                        break;
                    case CARRIER:
                        numCarriers++;

                        int carrierType = 1;
                        if (numCarriers % 2 == 1) {
                            carrierType = Consts.CARRIER_TYPE_AD;
                        }
                        else {
                            carrierType = Consts.CARRIER_TYPE_MN;
                        }

                        // System.out.println("im building a carrier type " + carrierType + " writing to shared array index " + 
                        //     (Consts.HQ_CARRIER_TYPE_ARRAY_INDEX_0 + Consts.hq_id_to_array_index(rc.getID())));

                        rc.writeSharedArray(Consts.HQ_CARRIER_TYPE_ARRAY_INDEX_0 + Consts.hq_id_to_array_index(rc.getID()), 
                            Consts.hq_carrier_type_encode(rc.getID(), carrierType));

                        break;
                    default:
                        break;
                }

                return true;
            }
        }

        return false;
    }
    
    public  static void initHeadquarters(RobotController rc) throws GameActionException {
        // System.out.println("Initiating headquarters");
        ownHQ = rc.getLocation();
    }
    
    public static void runHeadquarters(RobotController rc) throws GameActionException {

        // System.out.println("turn " + rc.getRoundNum() + " HQ " + rc.getLocation());
        // // print shared array
        // for (int i = 0; i < 64; i++) {
        //     System.out.println("Shared array " + i + " " + rc.readSharedArray(i));
        // }

        // print shared islands
        String sharedIslandsString = "";
        for (Island island : sharedIslands) {
            sharedIslandsString += island.index + ";";
        }
        rc.setIndicatorString("shared islands: " +  sharedIslandsString);

        //! buy carrier with an anchor
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
            rc.writeSharedArray(Consts.CARRIER_ANCHOR_HQ_ID, rc.getID());

            numCarriers++;
            buyCarrierNextRound = false;
        }

        //! 4 starting carriers 
        Direction dir_carrier = directions[(numCarriers * 2) % 8];
        // MapLocation loc_carrier = rc.getLocation().add(dir_carrier).add(dir_carrier);

        int wantedCarriers = 4;
        if (numCarriers <= wantedCarriers) {
            // rc.setIndicatorString("Building starter bots");
            
            spawnBot(rc, ownHQ, dir_carrier, RobotType.CARRIER);
        }
        
        //! building anchors
        if (sharedIslands.size() > numAnchorsBuilt &&
                rc.canBuildAnchor(Anchor.STANDARD) &&
                rc.getResourceAmount(ResourceType.ADAMANTIUM) >= (50 + 100) && 
                rc.getResourceAmount(ResourceType.MANA) >= 100) {

            // System.out.println("Building anchor nr" + numAnchorsBuilt + "; Shared islands size: " + sharedIslands.size());
            
            rc.setIndicatorString("Building anchor! " + Anchor.STANDARD);
            
            rc.buildAnchor(Anchor.STANDARD);

            buyCarrierNextRound = true;
            numAnchorsBuilt++;
        }
        
        //! spamming launchers and carriers
        MapLocation launcherTargetLoc = new MapLocation(mapSize[0] - ownHQ.x, mapSize[1] - ownHQ.y);

        Direction dir_launcher = ownHQ.directionTo(launcherTargetLoc);

        if (numCarriers > 4 && (rc.getRoundNum() % 2 == 0 || rc.getRoundNum() < 300)) { // build launchers on even rounds or in early game
            spawnBot(rc, ownHQ, dir_launcher, RobotType.LAUNCHER);
        } else if (rc.getRoundNum() % 4 == 1 && numCarriers <= Consts.MAX_CARRIERS) {
            spawnBot(rc, ownHQ, dir_carrier, RobotType.CARRIER);
        }

        //! too much adamantium, change carrier type to mana
        if (rc.getResourceAmount(ResourceType.ADAMANTIUM) > 1000) {

            rc.writeSharedArray(Consts.HQ_CARRIER_TYPE_ARRAY_INDEX_0 + Consts.hq_id_to_array_index(rc.getID()), 
                Consts.hq_carrier_type_encode(rc.getID(), Consts.CARRIER_TYPE_MN));

            // System.out.println("pls change carrier type to mana");
        }
    }
}
