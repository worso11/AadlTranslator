package lukowicz.application.memory;

public class ElementsPosition {

    private static Double PLACE_X_POSITION = -504.000000;
    private static Double PLACE_Y_POSITION = 376.000000;
    private static Double TRANSITION_X_POSITION = 504.000000;
    private static Double TRANSITION_Y_POSITION = 376.000000;

    private static Double ARC_X_POSITION = 0.000000;
    private static Double ARC_Y_POSITION = 0.000000;

    private static Double INDENT = 64.0000;

    public static Double getPLACE_X_POSITION() {
        return PLACE_X_POSITION;
    }

    public static Double getPLACE_Y_POSITION() {
        PLACE_Y_POSITION = PLACE_Y_POSITION - INDENT;
        return PLACE_Y_POSITION;
    }

    public static Double getTRANSITION_X_POSITION() {
        return TRANSITION_X_POSITION;
    }

    public static Double getTRANSITION_Y_POSITION() {
        TRANSITION_Y_POSITION = TRANSITION_Y_POSITION - INDENT;
        return TRANSITION_Y_POSITION;
    }

    public static String getArcXPosition() {
        return ARC_X_POSITION.toString();
    }

    public static String getArcYPosition() {
       return ARC_Y_POSITION.toString();
    }

    public static void resetPositions(){
        PLACE_Y_POSITION = 376.000000;
        TRANSITION_Y_POSITION = 376.000000;

    }
}
