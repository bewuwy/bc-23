package deathbot1;

import battlecode.common.*;

public class Headquarters extends RobotPlayer {

    static boolean buyCarrierNextRound = false;
    static Island islandToAttack = null;

    static int max_carriers = 12;

    static int numCarriers = 0;
    static int numLaunchers = 0;
    static int numAnchorsBuilt = 0;

    // static RobotType last_built = null;

    public static boolean spawnBot(RobotController rc, MapLocation hq_loc, Direction dir, RobotType type) throws GameActionException {        
        MapLocation robotSpawnLocation = hq_loc.add(dir);
        boolean canBuild = rc.canBuildRobot(type, robotSpawnLocation);
        
        MapInfo[] nearby = rc.senseNearbyMapInfos(robotSpawnLocation, 1);

        MapLocation mapLoc;

        for (MapInfo mapInfo : nearby) {
            mapLoc = mapInfo.getMapLocation();
            canBuild = rc.canBuildRobot(type, mapLoc);

            if (canBuild) {                
                rc.setIndicatorString("Building " + type);
                rc.buildRobot(type, mapLoc);
                // last_built = type;

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

        int ad_amount = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        int mn_amount = rc.getResourceAmount(ResourceType.MANA);

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
                ad_amount >= (50 + 100) && 
                mn_amount >= 100) {

            // System.out.println("Building anchor nr" + numAnchorsBuilt + "; Shared islands size: " + sharedIslands.size());
            
            rc.setIndicatorString("Building anchor! " + Anchor.STANDARD);
            
            rc.buildAnchor(Anchor.STANDARD);

            buyCarrierNextRound = true;
            numAnchorsBuilt++;
        }
        
        //! spamming launchers and carriers
        MapLocation launcherTargetLoc = new MapLocation(mapSize[0] - ownHQ.x, mapSize[1] - ownHQ.y);

        Direction dir_launcher = ownHQ.directionTo(launcherTargetLoc);

        if ((numCarriers > 4 || ad_amount < 50 ) && (rc.getRoundNum() % 4 == 0 || rc.getRoundNum() < 300)) { // build launchers every 4th round or in early game
            spawnBot(rc, ownHQ, dir_launcher, RobotType.LAUNCHER);
        } else if (rc.getRoundNum() % 4 == 1 && numCarriers <= max_carriers) {
            spawnBot(rc, ownHQ, dir_carrier, RobotType.CARRIER);
        }

        //! too much adamantium, change carrier type to mana
        if (ad_amount > 500 && turnCount > 50 && (mn_amount == 0 || (ad_amount / (mn_amount)) > 3)) {
            max_carriers += 2;

            rc.writeSharedArray(Consts.HQ_CARRIER_TYPE_ARRAY_INDEX_0 + Consts.hq_id_to_array_index(rc.getID()), 
                Consts.hq_carrier_type_encode(rc.getID(), Consts.CARRIER_TYPE_MN));

            // System.out.println("pls change carrier type to mana");
        }
    }
}
