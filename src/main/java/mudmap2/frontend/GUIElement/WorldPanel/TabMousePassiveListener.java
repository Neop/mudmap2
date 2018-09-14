package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * This listener only contains actions, that don't modify the world
 */
public class TabMousePassiveListener extends TabMouseListener implements MouseListener {

    public TabMousePassiveListener(final WorldPanel worldPanel) {
        super(worldPanel);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) { // right click
            // show context menu
            final ContextMenu context_menu = new ContextMenu(getWorldPanel(), getWorldPanel().getPlacePosX(e.getX()), getWorldPanel().getPlacePosY(e.getY()));
            context_menu.show(e.getComponent(), e.getX(), e.getY());
        } else if (e.getButton() == MouseEvent.BUTTON1) { // left click
            if (!e.isShiftDown()) { // left click + hift gets handled in active listener
                // set place selection to coordinates if keyboard selection is enabled
                getWorldPanel().setCursor(getWorldPanel().getPlacePosX(e.getX()), getWorldPanel().getPlacePosY(e.getY()));
            }
        }
    }
}