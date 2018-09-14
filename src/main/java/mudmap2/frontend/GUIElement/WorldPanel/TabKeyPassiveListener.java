package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.KeyEvent;

/**
 * This listener only contains actions that don't modify the world
 */
public class TabKeyPassiveListener extends TabKeyListener {
    public TabKeyPassiveListener(final WorldPanel parent) {
        super(parent);
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown()) { // ctrl, shift and alt not pressed
            final int xBef = parent.getCursorX();
            final int yBef = parent.getCursorY();

            switch (e.getKeyCode()) {
                // zoom the map
                case KeyEvent.VK_PLUS:
                case KeyEvent.VK_ADD:
                case KeyEvent.VK_PAGE_UP:
                    parent.tileSizeIncrement();
                    break;
                case KeyEvent.VK_MINUS:
                case KeyEvent.VK_SUBTRACT:
                case KeyEvent.VK_PAGE_DOWN:
                    parent.tileSizeDecrement();
                    break;

                // enable / disable cursor
                case KeyEvent.VK_P:
                    parent.setCursorEnabled(!parent.isCursorEnabled());
                    break;

                // shift place selection - wasd
                case KeyEvent.VK_NUMPAD8:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (parent.isCursorEnabled()) {
                        parent.moveCursor(0, +1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD4:
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (parent.isCursorEnabled()) {
                        parent.moveCursor(-1, 0);
                    }
                    break;
                case KeyEvent.VK_NUMPAD2:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (parent.isCursorEnabled()) {
                        parent.moveCursor(0, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD6:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (parent.isCursorEnabled()) {
                        parent.moveCursor(+1, 0);
                    }
                    break;

                // diagonal movement
                case KeyEvent.VK_NUMPAD1:
                    if (parent.isCursorEnabled()) {
                        parent.moveCursor(-1, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD3:
                    if (parent.isCursorEnabled()) {
                        parent.moveCursor(+1, -1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD7:
                    if (parent.isCursorEnabled()) {
                        parent.moveCursor(-1, +1);
                    }
                    break;
                case KeyEvent.VK_NUMPAD9:
                    if (parent.isCursorEnabled()) {
                        parent.moveCursor(+1, +1);
                    }
                    break;

                // goto home
                case KeyEvent.VK_NUMPAD5:
                case KeyEvent.VK_H:
                case KeyEvent.VK_HOME:
                    parent.gotoHome();
                    break;

                // reset place group selection
                case KeyEvent.VK_ESCAPE:
                    parent.placeGroupReset();
                    break;
            }

            final int xSel = parent.getCursorX();
            final int ySel = parent.getCursorY();

            // change group selection, if place selection changed
            if (xSel != xBef || ySel != yBef) {
                if (parent.getPlaceGroupBoxStart() != null) {
                    parent.placeGroupBoxSelectionToList();
                }
            }
        }
    }
}
