package mudmap2.utils;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

public class KeystrokeHelper {

    /**
     * Returns a KeyStroke for the given keyCode using the CTRL key modifier
     *
     * @param keyCode an int specifying the numeric code for a keyboard key
     * @return
     */
    public static KeyStroke ctrl(final int keyCode) {
        return KeyStroke.getKeyStroke(keyCode, ActionEvent.CTRL_MASK);
    }

    /**
     * Returns a KeyStroke for the given keyCode using the ALT key modifier
     *
     * @param keyCode an int specifying the numeric code for a keyboard key
     * @return
     */
    public static KeyStroke alt(final int keyCode) {
        return KeyStroke.getKeyStroke(keyCode, ActionEvent.ALT_MASK);
    }

    /**
     * Returns a KeyStroke for the given keyCode using the CTRL ALT key modifier
     *
     * @param keyCode an int specifying the numeric code for a keyboard key
     * @return
     */
    public static KeyStroke ctrlAlt(final int keyCode) {
        return KeyStroke.getKeyStroke(keyCode, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK);
    }

}
