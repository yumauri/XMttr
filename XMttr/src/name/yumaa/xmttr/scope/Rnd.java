package name.yumaa.xmttr.scope;

import java.util.Random;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 30.10.2014
 */
public class Rnd {

    /**
     * Random generator
     */
    public static final Random random = new Random();

    private static final String SMALL_LETTERS   = "abcdefghijklmnopqrstuvwxyz";
    private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS          = "1234567890";
    private static final String CHARS           = SMALL_LETTERS + CAPITAL_LETTERS + DIGITS;

    /**
     * Get random value within range [min; max]
     * @param min    minimal value
     * @param max    maximal value
     * @return random value within range
     */
    public static int getRandom(final int min, final int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    /**
     * Get random value within range [0; max]
     * @param max    maximal value
     * @return random value within range
     */
    public static int getRandom(final int max) {
        return getRandom(0, max);
    }

    /**
     * Get random character from CHARS
     * @return random char
     */
    public static char randomChar() {
        return CHARS.charAt(random.nextInt(CHARS.length()));
    }

    /**
     * Get random digit [0;9]
     * @return digit
     */
    public static int randomDigit() {
        return Rnd.random.nextInt(10);
    }

    /**
     * Generate random string
     * @param length    string length
     * @return random string
     */
    public static String randomString(int length) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buffer.append(randomChar());
        }
        return buffer.toString();
    }

}
