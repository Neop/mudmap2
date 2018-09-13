package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class TabMouseMotionListener implements MouseMotionListener {

    private final WorldPanel worldPanel;

    public TabMouseMotionListener(final WorldPanel worldPanel) {
        this.worldPanel = worldPanel;
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (worldPanel.isMouseInPanel()) {
            final double dx = (e.getX() - worldPanel.getMouseXPrevious()) / worldPanel.getTileSize();
            final double dy = (e.getY() - worldPanel.getMouseYPrevious()) / worldPanel.getTileSize();
            if (!e.isShiftDown()) {
                worldPanel.getPosition().move(-dx, dy);
            } else { // shift pressed: box selection
                worldPanel.placeGroupBoxModifySelection(worldPanel.getPlacePosX(e.getX()), worldPanel.getPlacePosY(e.getY()));
            }
            worldPanel.repaint();
        }
        worldPanel.setMouseXPrevious(e.getX());
        worldPanel.setMouseYPrevious(e.getY());
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        worldPanel.setMouseXPrevious(e.getX());
        worldPanel.setMouseYPrevious(e.getY());
    }
}
