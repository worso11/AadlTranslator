package lukowicz.application.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

public class ParserTools {

    public static String generateUUID(){
        return String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16));
    }

    public static ArrayList<Integer> preparePorts(String source) {
        String[] sourceSplitted = source.split(" ");
        ArrayList<Integer> sourceList = new ArrayList<>();
        for (String element : sourceSplitted) {
            sourceList.add(Integer.valueOf(element));
        }
        return sourceList;

    }

}
