package mudmap2.frontend.GUIElement.WorldPanel;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * redraws the world tab after the popup is closed
 */
public class TabContextPopMenuListener implements PopupMenuListener {

    private final WorldPanel worldPanel;

    public TabContextPopMenuListener(final WorldPanel worldPanel) {
        this.worldPanel = worldPanel;
    }

    @Override
    public void popupMenuWillBecomeVisible(final PopupMenuEvent arg0) {
        worldPanel.setContextMenu(true);
    }

    @Override
    public void popupMenuWillBecomeInvisible(final PopupMenuEvent arg0) {
        worldPanel.setContextMenu(false);
        worldPanel.repaint();
    }

    @Override
    public void popupMenuCanceled(final PopupMenuEvent arg0) {
        worldPanel.setContextMenu(false);
        worldPanel.repaint();
    }

}
