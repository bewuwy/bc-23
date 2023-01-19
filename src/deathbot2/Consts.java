package deathbot2;

public class Consts {

    //! HQ carrier type - shared array
        public static final int HQ_CARRIER_TYPE_ARRAY_INDEX_0 = 40;
        
        public static final int CARRIER_TYPE_AD = 1;
        public static final int CARRIER_TYPE_MN = 2;

        public static int hq_carrier_type_encode(int hq_id, int carrier_type) {
            return hq_id * 100 + carrier_type;
        }

        public static int[] hq_carrier_type_decode(int hq_carrier_input) {
            int[] result = new int[2];
            result[0] = hq_carrier_input / 100;
            result[1] = hq_carrier_input % 100;
            return result;
        }

    // hq ids are either 1,3,5,7 or 2,4,6,8
    public static int hq_id_to_array_index(int hq_id) {
        // if id is odd
        if (hq_id % 2 == 1) {
            return (hq_id - 1) / 2;
        }
        else {
            return (hq_id / 2) - 1;
        }
    }

    // public static final int HQ_AD_AMOUNT_ARRAY_INDEX_0 = 40;
    // public static final int HQ_AD_TURN_ARRAY_INDEX_0 = 43;

    // Shared array indices for HQ
    public static final int CARRIER_ANCHOR_ARRAY_INDEX = 59;
    public static final int CARRIER_ANCHOR_HQ_ID = 60;

    // public static int islandIdToSharedArrayIndex(int islandId) {
    //     return islandId + 4; // +4 because the first 5 are reserved for the HQs and symmetry type
    // }
}
