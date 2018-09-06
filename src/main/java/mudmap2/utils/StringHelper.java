package mudmap2.utils;

public class StringHelper {

    /**
     * Concatenates the given objects into a single String
     *
     * @param objects The objects to join
     * @return
     */
    public static String join(final Object... objects) {
        final StringBuilder b = new StringBuilder();
        for (final Object object : objects) {
            b.append(object);
        }
        return b.toString();
    }

}
