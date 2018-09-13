package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mudmap2.backend.Layer;
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.frontend.dialog.PathConnectDialog;
import mudmap2.frontend.dialog.PlaceCommentDialog;
import mudmap2.frontend.dialog.PlaceDialog;
import mudmap2.frontend.dialog.PlaceRemoveDialog;
import mudmap2.frontend.dialog.placeGroup.PlaceGroupDialog;
import mudmap2.utils.StringHelper;

/**
 * This listener contains actions, that modify the world
 */
public class TabKeyListener implements KeyListener {

    WorldPanel parent;

    public TabKeyListener(final WorldPanel parent) {
        this.parent = parent;
    }

    @Override
    public void keyTyped(final KeyEvent e) {
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(parent);
        if (e.isControlDown()) { // ctrl key pressed
            Place place, other;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_A: // select all places
                    parent.placeGroupSet(parent.getWorld().getLayer(parent.getPosition().getLayer()).getPlaces());
                    break;
                case KeyEvent.VK_X: // cut selected places
                    if (!parent.placeGroupGetSelection().isEmpty()) { // cut group selection
                        mudmap2.CopyPaste.cut(parent.getPlaceGroup(), parent.getCursorX(), parent.getCursorY());
                        parent.callMessageListeners(StringHelper.join(parent.getPlaceGroup().size(), " places cut"));
                        parent.placeGroupReset();
                    } else if (parent.getSelectedPlace() != null) { // cut cursor selection
                        final HashSet<Place> tmp_selection = new HashSet<>();
                        tmp_selection.add(parent.getSelectedPlace());
                        mudmap2.CopyPaste.cut(tmp_selection, parent.getCursorX(), parent.getCursorY());
                        parent.callMessageListeners("1 place cut");
                    } else {
                        parent.callMessageListeners("No places cut: selection empty");
                    }
                    break;
                case KeyEvent.VK_C: // copy selected places
                    if (!parent.placeGroupGetSelection().isEmpty()) { // copy group selection
                        mudmap2.CopyPaste.copy(parent.getPlaceGroup(), parent.getCursorX(), parent.getCursorY());
                        parent.callMessageListeners(StringHelper.join(parent.getPlaceGroup().size(), " places copied"));
                        parent.placeGroupReset();
                    } else if (parent.getSelectedPlace() != null) { // copy cursor selection
                        final HashSet<Place> tmp_selection = new HashSet<>();
                        tmp_selection.add(parent.getSelectedPlace());
                        mudmap2.CopyPaste.copy(tmp_selection, parent.getCursorX(), parent.getCursorY());
                        parent.callMessageListeners("1 place copied");
                    } else {
                        mudmap2.CopyPaste.resetCopy();
                        parent.callMessageListeners("No places copied: selection empty");
                    }
                    break;
                case KeyEvent.VK_V: // paste copied / cut places
                    if (mudmap2.CopyPaste.hasCopyPlaces()) {
                        if (mudmap2.CopyPaste.canPaste(parent.getCursorX(), parent.getCursorY(), parent.getWorld().getLayer(parent.getPosition().getLayer()))) {
                            final int paste_num = mudmap2.CopyPaste.getCopyPlaces().size();
                            if (mudmap2.CopyPaste.paste(parent.getCursorX(), parent.getCursorY(), parent.getWorld().getLayer(parent.getPosition().getLayer()))) {
                                parent.callMessageListeners(StringHelper.join(paste_num, " places pasted"));
                            } else {
                                parent.callMessageListeners("No places pasted");
                            }
                        } else {
                            parent.callMessageListeners("Can't paste: not enough free space on map");
                        }
                    } else {
                        mudmap2.CopyPaste.resetCopy();
                        parent.callMessageListeners("Can't paste: no places cut or copied");
                    }
                    break;

                case KeyEvent.VK_NUMPAD8:
                case KeyEvent.VK_UP:
                    //case KeyEvent.VK_W: // add path to direction 'n'
                    place = parent.getSelectedPlace();
                    other = parent.getWorld().getLayer(parent.getPosition().getLayer()).get(parent.getCursorX(), parent.getCursorY() + 1);
                    if (place != null && other != null) { // if places exist
                        if (place.getExit("n") == null && other.getExit("s") == null) { // if exits aren't occupied
                            place.connectPath(new Path(place, "n", other, "s"));
                        }
                    }
                    break;
                case KeyEvent.VK_NUMPAD9: // add path to direction 'ne'
                    place = parent.getSelectedPlace();
                    other = parent.getWorld().getLayer(parent.getPosition().getLayer()).get(parent.getCursorX() + 1, parent.getCursorY() + 1);
                    if (place != null && other != null) { // if places exist
                        if (place.getExit("ne") == null && other.getExit("sw") == null) { // if exits aren't occupied
                            place.connectPath(new Path(place, "ne", other, "sw"));
                        }
                    }
                    break;
                case KeyEvent.VK_NUMPAD6:
                case KeyEvent.VK_RIGHT:
                    //case KeyEvent.VK_D: // add path to direction 'e'
                    place = parent.getSelectedPlace();
                    other = parent.getWorld().getLayer(parent.getPosition().getLayer()).get(parent.getCursorX() + 1, parent.getCursorY());
                    if (place != null && other != null) { // if places exist
                        if (place.getExit("e") == null && other.getExit("w") == null) { // if exits aren't occupied
                            place.connectPath(new Path(place, "e", other, "w"));
                        }
                    }
                    break;
                case KeyEvent.VK_NUMPAD3: // add path to direction 'se'
                    place = parent.getSelectedPlace();
                    other = parent.getWorld().getLayer(parent.getPosition().getLayer()).get(parent.getCursorX() + 1, parent.getCursorY() - 1);
                    if (place != null && other != null) { // if places exist
                        if (place.getExit("se") == null && other.getExit("nw") == null) { // if exits aren't occupied
                            place.connectPath(new Path(place, "se", other, "nw"));
                        }
                    }
                    break;
                case KeyEvent.VK_NUMPAD2:
                case KeyEvent.VK_DOWN:
                    //case KeyEvent.VK_S: // add path to direction 's'
                    place = parent.getSelectedPlace();
                    other = parent.getWorld().getLayer(parent.getPosition().getLayer()).get(parent.getCursorX(), parent.getCursorY() - 1);
                    if (place != null && other != null) { // if places exist
                        if (place.getExit("s") == null && other.getExit("n") == null) { // if exits aren't occupied
                            place.connectPath(new Path(place, "s", other, "n"));
                        }
                    }
                    break;
                case KeyEvent.VK_NUMPAD1: // add path to direction 'sw'
                    place = parent.getSelectedPlace();
                    other = parent.getWorld().getLayer(parent.getPosition().getLayer()).get(parent.getCursorX() - 1, parent.getCursorY() - 1);
                    if (place != null && other != null) { // if places exist
                        if (place.getExit("sw") == null && other.getExit("ne") == null) { // if exits aren't occupied
                            place.connectPath(new Path(place, "sw", other, "ne"));
                        }
                    }
                    break;
                case KeyEvent.VK_NUMPAD4:
                case KeyEvent.VK_LEFT:
                    //case KeyEvent.VK_A: // add path to direction 'w'
                    place = parent.getSelectedPlace();
                    other = parent.getWorld().getLayer(parent.getPosition().getLayer()).get(parent.getCursorX() - 1, parent.getCursorY());
                    if (place != null && other != null) { // if places exist
                        if (place.getExit("w") == null && other.getExit("e") == null) { // if exits aren't occupied
                            place.connectPath(new Path(place, "w", other, "e"));
                        }
                    }
                    break;
                case KeyEvent.VK_NUMPAD7: // add path to direction 'nw'
                    place = parent.getSelectedPlace();
                    other = parent.getWorld().getLayer(parent.getPosition().getLayer()).get(parent.getCursorX() - 1, parent.getCursorY() + 1);
                    if (place != null && other != null) { // if places exist
                        if (place.getExit("nw") == null && other.getExit("se") == null) { // if exits aren't occupied
                            place.connectPath(new Path(place, "nw", other, "se"));
                        }
                    }
                    break;
                case KeyEvent.VK_NUMPAD5: // open add path dialog
                    new PathConnectDialog(parent.getParentFrame(), parent.getSelectedPlace()).setVisible(true);
                    break;
            }
        } else if (e.isShiftDown()) { // shift key pressed -> modify selection
            final int x_bef = parent.getCursorX();
            final int y_bef = parent.getCursorY();

            switch (e.getKeyCode()) {
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

                case KeyEvent.VK_SPACE: // add or removePlace single place to place group selection
                    final Place place = parent.getSelectedPlace();
                    if (place != null) {
                        parent.placeGroupAdd(place);
                    }
                    break;
            }
            final int x_sel = parent.getCursorX();
            final int y_sel = parent.getCursorY();

            // change group selection, if place selection changed
            if (x_sel != x_bef || y_sel != y_bef) {
                if (parent.getPlaceGroupBoxStart() == null) {
                    parent.placeGroupBoxModifySelection(x_bef, y_bef);
                }
                parent.placeGroupBoxModifySelection(x_sel, y_sel);
            }
        } else if (e.isAltDown() || e.isAltGraphDown()) { // alt or altgr key pressed
            final Place place = parent.getSelectedPlace();
            Path path;

            if (place != null) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_NUMPAD8:
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W: // removePlace path to direction 'n'
                        path = place.getPathTo("n");
                        if (path != null) {
                            place.removePath(path);
                        }
                        break;
                    case KeyEvent.VK_NUMPAD9: // removePlace path to direction 'ne'
                        path = place.getPathTo("ne");
                        if (path != null) {
                            place.removePath(path);
                        }
                        break;
                    case KeyEvent.VK_NUMPAD6:
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D: // removePlace path to direction 'e'
                        path = place.getPathTo("e");
                        if (path != null) {
                            place.removePath(path);
                        }
                        break;
                    case KeyEvent.VK_NUMPAD3: // removePlace path to direction 'se'
                        path = place.getPathTo("se");
                        if (path != null) {
                            place.removePath(path);
                        }
                        break;
                    case KeyEvent.VK_NUMPAD2:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S: // removePlace path to direction 's'
                        path = place.getPathTo("s");
                        if (path != null) {
                            place.removePath(path);
                        }
                        break;
                    case KeyEvent.VK_NUMPAD1: // removePlace path to direction 'sw'
                        path = place.getPathTo("sw");
                        if (path != null) {
                            place.removePath(path);
                        }
                        break;
                    case KeyEvent.VK_NUMPAD4:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A: // removePlace path to direction 'w'
                        path = place.getPathTo("w");
                        if (path != null) {
                            place.removePath(path);
                        }
                        break;
                    case KeyEvent.VK_NUMPAD7: // removePlace path to direction 'nw'
                        path = place.getPathTo("nw");
                        if (path != null) {
                            place.removePath(path);
                        }
                        break;
                }
            }
        } else { // ctrl, shift and alt not pressed
            switch (e.getKeyCode()) {
                // show context menu
                case KeyEvent.VK_CONTEXT_MENU:
                    if (parent.isCursorEnabled()) {
                        final ContextMenu context_menu = new ContextMenu(parent, parent.getCursorX(), parent.getCursorY());
                        context_menu.show(e.getComponent(), parent.getScreenPosX(parent.getCursorX()) + (int) parent.getTileSize() / 2, parent.getScreenPosY(parent.getCursorY()) + (int) parent.getTileSize() / 2);
                    }
                    break;

                // edit / add place
                case KeyEvent.VK_INSERT:
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_E:
                    if (parent.isCursorEnabled()) {
                        final Place place = parent.getSelectedPlace();
                        PlaceDialog dlg;

                        Layer layer = null;
                        if (parent.getPosition() != null) {
                            layer = parent.getWorld().getLayer(parent.getPosition().getLayer());
                        }

                        if (place != null) {
                            dlg = new PlaceDialog(rootFrame, parent.getWorld(), place);
                        } else {
                            dlg = new PlaceDialog(rootFrame, parent.getWorld(), parent.getWorld().getLayer(parent.getPosition().getLayer()), parent.getCursorX(), parent.getCursorY());
                        }
                        dlg.setVisible(true);

                        if (layer == null) {
                            parent.pushPosition(dlg.getPlace().getCoordinate());
                        }
                    }
                    break;
                // create placeholder
                case KeyEvent.VK_F:
                    if (parent.isCursorEnabled()) {
                        final Place place = parent.getSelectedPlace();
                        // create placeholder or removePlace one
                        if (place == null) {
                            parent.getWorld().putPlaceholder(parent.getPosition().getLayer(), parent.getCursorX(), parent.getCursorY());
                        } else if (place.getName().equals(Place.PLACEHOLDER_NAME)) {
                            try {
                                place.getLayer().remove(place);
                            } catch (final RuntimeException ex) {
                                Logger.getLogger(TabKeyListener.class.getName()).log(Level.SEVERE, null, ex);
                                JOptionPane.showMessageDialog(parent, StringHelper.join("Could not remove place: ", ex.getMessage()));
                            } catch (final PlaceNotFoundException ex) {
                                Logger.getLogger(TabKeyListener.class.getName()).log(Level.SEVERE, null, ex);
                                JOptionPane.showMessageDialog(parent, "Could not remove place: Place not found.");
                            }
                        }
                    }
                    parent.repaint();
                    break;
                // removePlace place
                case KeyEvent.VK_DELETE:
                case KeyEvent.VK_R:
                    if (!parent.placeGroupHasSelection()) { // no places selected
                        if (parent.isCursorEnabled()) {
                            final Place place = parent.getSelectedPlace();
                            if (place != null) {
                                new PlaceRemoveDialog(rootFrame, parent.getWorld(), place).show();
                            }
                        }
                    } else { // places selected
                        final HashSet<Place> place_group = parent.placeGroupGetSelection();
                        if (place_group != null) {
                            final PlaceRemoveDialog dlg = new PlaceRemoveDialog(rootFrame, parent.getWorld(), place_group);
                            dlg.show();
                            // reset selection, if places were removed
                            if (dlg.getPlacesRemoved()) {
                                parent.placeGroupReset();
                            }
                        }
                    }
                    break;
                // edit place comments
                case KeyEvent.VK_C:
                    if (parent.isCursorEnabled()) {
                        final Place place = parent.getSelectedPlace();
                        if (place != null) {
                            new PlaceCommentDialog(rootFrame, place).setVisible(true);
                            parent.callStatusUpdateListeners();
                        }
                    }
                    break;
                // modify place group
                case KeyEvent.VK_Q:
                    Place place = parent.getSelectedPlace();

                    if (!parent.placeGroupHasSelection()) {
                        // no place selected
                        if (place == null) {
                            new PlaceGroupDialog(rootFrame, parent.getWorld()).setVisible(true);
                        } else {
                            new PlaceGroupDialog(rootFrame, parent.getWorld(), place).setVisible(true);
                        }
                    }
                    break;

                case KeyEvent.VK_SPACE: // add or removePlace single place to place group selection
                    place = parent.getSelectedPlace();
                    if (place != null) {
                        parent.placeGroupAdd(place);
                    }
                    break;
            }
        }
        parent.repaint();
    }

    @Override
    public void keyReleased(final KeyEvent arg0) {
    }
}
