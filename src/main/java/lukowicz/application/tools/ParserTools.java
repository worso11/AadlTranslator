package lukowicz.application.tools;

import java.math.BigInteger;
import java.util.UUID;

public class ParserTools {

    public static String generateUUID(){
        return String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16));
    }

}
