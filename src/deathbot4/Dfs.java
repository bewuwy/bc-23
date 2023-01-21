package deathbot4;

import battlecode.common.*;

public class Dfs {


    Direction lastDfs;

    void go(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            lastDfs = dir;
        } else {
            if (lastDfs != null) {
                Direction[] dirs = {lastDfs, lastDfs.rotateLeft(), lastDfs.rotateRight(), lastDfs.rotateLeft().rotateLeft(), lastDfs.rotateRight().rotateRight()};
                for (Direction d : dirs) {
                    if (rc.canMove(d)) {
                        rc.move(d);
                        lastDfs = d;
                        break;
                    }
                }
            } else{
                Direction[] dirs = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(), dir.opposite().rotateRight(), dir.opposite().rotateLeft(), dir.opposite()};
                for (Direction d : dirs) {
                    if (rc.canMove(d)) {
                        rc.move(d);
                        lastDfs = d;
                        break;
                    }
                }
            }
        }
    }


}
