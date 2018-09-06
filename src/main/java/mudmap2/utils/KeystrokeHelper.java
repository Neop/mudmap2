package mudmap2.utils;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

public class KeystrokeHelper {

    public static KeyStroke ctrl(final int keyCode) {
        return KeyStroke.getKeyStroke(keyCode, ActionEvent.CTRL_MASK);
    }

    public static KeyStroke ctrlAlt(final int keyCode) {
        return KeyStroke.getKeyStroke(keyCode, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK);
    }

}
