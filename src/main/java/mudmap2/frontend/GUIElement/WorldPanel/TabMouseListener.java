package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import mudmap2.backend.Place;
import mudmap2.frontend.dialog.PlaceDialog;

/**
 * This listener contains actions that modify the world
 */
public class TabMouseListener implements MouseListener {

    private final WorldPanel worldPanel;

    public TabMouseListener(final WorldPanel worldPanel) {
        this.worldPanel = worldPanel;
    }

    public WorldPanel getWorldPanel() {
        return worldPanel;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(e.getComponent());

        if (e.getButton() == MouseEvent.BUTTON1) { // left click
            final Place place = worldPanel.getWorld().getLayer(worldPanel.getPosition().getLayer()).get(worldPanel.getPlacePosX(e.getX()), worldPanel.getPlacePosY(e.getY()));
            if (e.isControlDown()) { // left click + ctrl
                if (place != null) {
                    worldPanel.placeGroupAdd(place);
                }
            } else if (!e.isShiftDown()) { // left click and not shift
                worldPanel.placeGroupReset();
                if (e.getClickCount() > 1) { // double click
                    if (place != null) {
                        new PlaceDialog(rootFrame, worldPanel.getWorld(), place).setVisible(true);
                    } else {
                        new PlaceDialog(rootFrame, worldPanel.getWorld(), worldPanel.getWorld().getLayer(worldPanel.getPosition().getLayer()), worldPanel.getPlacePosX(e.getX()), worldPanel.getPlacePosY(e.getY())).setVisible(true);
                    }
                }
            } else {
                if (!worldPanel.placeGroupHasSelection()) {
                    worldPanel.placeGroupBoxModifySelection(worldPanel.getCursorX(), worldPanel.getCursorY());
                }
                worldPanel.placeGroupBoxModifySelection(worldPanel.getPlacePosX(e.getX()), worldPanel.getPlacePosY(e.getY()));
                // cursor has to be set after the selection -> not handled by passive listener
                worldPanel.setCursor(worldPanel.getPlacePosX(e.getX()), worldPanel.getPlacePosY(e.getY()));
            }
        }
        worldPanel.repaint();
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        worldPanel.requestFocusInWindow();
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        worldPanel.setMouseInPanel(true);
        worldPanel.setMouseXPrevious(e.getX());
        worldPanel.setMouseYPrevious(e.getY());
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        worldPanel.setMouseInPanel(false);
    }

}