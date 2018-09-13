/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2016  Neop (email: mneop@web.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldChangeListener;
import mudmap2.backend.WorldCoordinate;

/**
 *
 * @author neop
 */
public class WorldPanel extends JPanel implements WorldChangeListener {
    private static final long serialVersionUID = 1L;

    // tile size in pixel
    public static final int TILE_SIZE_MIN = 10;
    public static final int TILE_SIZE_MAX = 200;

    private final JFrame parentFrame;

    private final MapPainter mappainter;

    // passive worldpanels don't modify the world
    private final boolean passive;

    private final World world;

    // tile size in pixel
    private double tileSize;

    private int cursorX;
    private int cursorY;
    private boolean cursorEnabled;
    private boolean cursorForced;

    private final HashSet<PlaceSelectionListener> placeSelectionListeners;
    private final HashSet<MapCursorListener> mapCursorListeners;
    private final HashSet<StatusListener> statusListeners;
    private final HashSet<WorldPanelListener> tileSizeListeners;

    /**
     * positionsTail contains all previously visited positions up to the current
     * position, positionsHead contains all positions visited after the current
     * one. In case the 'previous button' gets used the top position of head
     * gets popped and pushed to tail. At least one position needs to remain in
     * tail.
     */
    private final LinkedList<WorldCoordinate> positionsHead;
    private final LinkedList<WorldCoordinate> positionsTail;

    // true, if the mouse is in the panel, for relative motion calculation
    private boolean mouseInPanel;
    // previous position of the mouse
    private int mouseXPrevious;
    private int mouseYPrevious;

    // place (group) selection
    private WorldCoordinate placeGroupBoxStart;
    private WorldCoordinate placeGroupBoxEnd;
    private HashSet<Place> placeGroup;

    // true, if a context menu is shown (to disable forced focus)
    private boolean isContextMenuShown;
    private boolean forcedFocus;

    /**
     * Constructs a world panel
     * @param parent
     * @param world
     * @param passive
     */
    public WorldPanel(final JFrame parent, final World world, final boolean passive) {
        parentFrame = parent;
        this.world = world;
        this.passive = passive;

        tileSize = 120; // default tile size

        placeSelectionListeners = new HashSet<>();
        mapCursorListeners = new HashSet<>();
        tileSizeListeners = new HashSet<>();
        statusListeners = new HashSet<>();

        positionsHead = new LinkedList<>();
        positionsTail = new LinkedList<>();

        placeGroup = new HashSet<>();

        mappainter = new MapPainterDefault();

        cursorX = cursorY = 0;
        cursorEnabled = true;
        cursorForced = false;

        isContextMenuShown = false;
        forcedFocus = false;

        mouseInPanel = false;
        mouseXPrevious = mouseYPrevious = 0;

        setFocusable(true);
        requestFocusInWindow();
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent arg0) {
                if (isFocusForced()) {
                    requestFocusInWindow();
                }
            }
        });

        addKeyListener(new TabKeyPassiveListener(this));
        addMouseListener(new TabMousePassiveListener(this));
        if (!passive) {
            addKeyListener(new TabKeyListener(this));
            addMouseListener(new TabMouseListener(this));
        }
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                double ts = getTileSize();
                final int delta = -e.getWheelRotation();

                ts = Math.exp(Math.log(ts / 10) + delta * 0.05) * 10;
                if (delta > 0) {
                    ts = Math.max(ts, getTileSize() + 1);
                } else if (delta < 0) {
                    ts = Math.min(ts, getTileSize() - 1);
                }
                setTileSize(ts);
            }
        });
        addMouseMotionListener(new TabMouseMotionListener(this));

        if (!passive) {
            world.addChangeListener(this);
        }
    }

    public MapPainter getMappainter() {
        return mappainter;
    }

    public boolean isPassive() {
        return passive;
    }

    /**
     * Returs true, if forced focus can be enabled
     * @return
     */
    public boolean isFocusForced() {
        return forcedFocus && !hasContextMenu();
    }

    public void setFocusForced(final Boolean b) {
        forcedFocus = b;
    }

    /**
     * Remove integer part, the part after the point remains
     * @param val
     * @return
     */
    private static double remint(final double val) {
        return val - Math.round(val);
    }

    /**
     * Get world associated to this world panel
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * Go to the home position
     */
    public void gotoHome() {
        pushPosition(getWorld().getHome());
        setCursor((int) Math.round(getPosition().getX()), (int) Math.round(getPosition().getY()));
    }

    /**
     * Set a new home position
     */
    public void setHome() {
        getWorld().setHome(new WorldCoordinate(getPosition()));
    }

    /**
     * Get the selected place or null
     * @return place or null
     */
    public Place getSelectedPlace() {
        final Layer layer = getWorld().getLayer(getPosition().getLayer());
        Place ret = null;
        if (layer != null) {
            ret = layer.get(getCursorX(), getCursorY());
        }
        return ret;
    }

    // ========================= context menu ==================================

    /**
     * Set whether a context menu is shown, to disable forced focus
     * @param b
     */
    void setContextMenu(final boolean b) {
        isContextMenuShown = b;
    }

    /**
     * Returns true, if a context menu is shown and forced focus is disabled
     * @return
     */
    private boolean hasContextMenu() {
        return isContextMenuShown;
    }

    // ========================= tile size =====================================

    /**
     * Get tile size in pixels
     * @return
     */
    public double getTileSize() {
        return tileSize;
    }

    /**
     * Set tile size in pixels
     * @param tileSize
     */
    public void setTileSize(final double tileSize) {
        this.tileSize = Math.min(Math.max(tileSize, WorldPanel.TILE_SIZE_MIN), WorldPanel.TILE_SIZE_MAX);
        callTileSizeListeners();
        repaint();
    }

    /**
     * increases the tile size
     */
    public void tileSizeIncrement() {
        double ts = getTileSize();
        ts = Math.exp(Math.log(ts / 10) + 0.03) * 10;
        ts = Math.min(ts, TILE_SIZE_MAX);
        setTileSize(Math.min(Math.max(ts, getTileSize() + 1), TILE_SIZE_MAX));
    }

    /**
     * decreases the tile size
     */
    public void tileSizeDecrement() {
        double ts = getTileSize();
        ts = Math.exp(Math.log(ts / 10) - 0.02) * 10;
        ts = Math.max(ts, TILE_SIZE_MIN);
        setTileSize(Math.max(Math.min(ts, getTileSize() - 1), TILE_SIZE_MIN));
    }

    // ========================= position history ==============================

    /**
     * Add new position to history, discard positions ahead, go to new position
     * @param coord
     */
    public void pushPosition(final WorldCoordinate coord) {
        positionsTail.push(new WorldCoordinate(coord));
        positionsHead.clear();

        callLayerChangeListeners(getWorld().getLayer(coord.getLayer()));

        // move place selection
        setCursor((int) coord.getX(), (int) coord.getY());
    }

    /**
     * Goes to previous position while moving the current position to the list
     * of positions ahead. Goes to home position if history is empty
     */
    public void popPosition() {
        if (positionsTail.size() > 1) {
            positionsHead.push(positionsTail.pop());
        }

        final WorldCoordinate position = getPosition();
        setCursor((int) position.getX(), (int) position.getY());
        callLayerChangeListeners(getWorld().getLayer(position.getLayer()));
    }

    /**
    * Moves position from list of positions ahead to current position.
    * Does nothing if list of positions ahead is empty.
    */
    public void restorePosition() {
        if (!positionsHead.isEmpty()) {
            positionsTail.push(positionsHead.pop());

            final WorldCoordinate position = getPosition();
            setCursor((int) position.getX(), (int) position.getY());
            callLayerChangeListeners(getWorld().getLayer(position.getLayer()));
        }
    }

    /**
     * Clears history
     * @param pos new position
     */
    public void resetHistory(final WorldCoordinate pos) {
        positionsHead.clear();
        positionsTail.clear();
        positionsTail.push(new WorldCoordinate(pos));

        setCursor((int) Math.round(pos.getX()), (int) Math.round(pos.getY()));
        callLayerChangeListeners(getWorld().getLayer(pos.getLayer()));
    }

    /**
     * Get current position or home if history is empty
     * @return current position or home position
     */
    public WorldCoordinate getPosition() {
        WorldCoordinate ret;
        if (positionsTail.isEmpty()) {
            ret = new WorldCoordinate(world.getHome());
        } else {
            ret = positionsTail.peek();
        }
        return ret;
    }

    /**
     * Get copy of history
     * @return
     */
    public LinkedList<WorldCoordinate> getHistory() {
        return new LinkedList<>(positionsTail);
    }

    /**
     * Replace history, clears list of positions ahead
     * @param list
     */
    public void setHistory(final LinkedList<WorldCoordinate> list) {
        positionsHead.clear();
        positionsTail.clear();
        positionsTail.addAll(list);

        final WorldCoordinate position = getPosition();
        setCursor((int) Math.round(position.getX()), (int) Math.round(position.getY()));
        callLayerChangeListeners(getWorld().getLayer(position.getLayer()));
    }

    // ========================= map cursor ====================================

    public boolean isCursorForced() {
        return cursorForced;
    }

    public void setCursorForced(final boolean cursorForced) {
        this.cursorForced = cursorForced;
    }

    public boolean isCursorEnabled() {
        return cursorEnabled || cursorForced;
    }

    public void setCursorEnabled(final boolean cursorEnabled) {
        this.cursorEnabled = cursorEnabled;
        callCursorListeners();
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public void setCursor(final int x, final int y) {
        cursorX = x;
        cursorY = y;
        callCursorListeners();
    }

    public void moveCursor(final int dx, final int dy) {
        cursorX += dx;
        cursorY += dy;
        moveScreenToCursor();
    }

    public JFrame getParentFrame() {
        return parentFrame;
    }

    /**
     * moves the shown places so the selection is on the screen
     */
    public void moveScreenToCursor() {
        final double screenX = getScreenPosX(cursorX);
        final double screenY = getScreenPosY(cursorY);
        final double ts = getTileSize();

        double dx = 0, dy = 0;

        if (screenX < 0) {
            dx = screenX / ts;
        } else if (screenX > getWidth() - ts) {
            dx = (screenX - getWidth()) / ts + 1;
        }

        if (screenY < 0) {
            dy = -screenY / ts;
        } else if (screenY > getHeight() - ts) {
            dy = -(screenY - getHeight()) / ts - 1;
        }

        if (dx != 0 || dy != 0) {
            getPosition().move((int) dx, (int) dy);
        }

        callCursorListeners();
    }

    // ========================= map coordinate maths ==========================

    /**
     * Converts screen coordinates to world coordinates
     * @param screen_x a screen coordinate (x-axis)
     * @return world coordinate x
     */
    int getPlacePosX(final int screen_x) {
        return (int) Math.ceil((screen_x - getWidth() / 2) / getTileSize() + getPosition().getX()) - 1;
    }

    /**
     * Converts screen coordinates to world coordinates
     * @param mouse_y a screen coordinate (y-axis)
     * @return world coordinate y
     */
    int getPlacePosY(final int screen_y) {
        return (int) -Math.ceil((screen_y - getHeight() / 2) / getTileSize() - getPosition().getY()) + 1;
    }

    /**
     * Converts world coordinates to screen coordinates
     * @param placeX a world (place) coordinate (x axis)
     * @return a screen coordinate x
     */
    int getScreenPosX(final int placeX) {
        final double ts = getTileSize();
        final double screenCenterX = getWidth() / ts / 2; // note: wdtwd2
        final int placeXOffset = (int) (Math.round(getPosition().getX()) - Math.round(screenCenterX));
        return (int) ((placeX - placeXOffset + remint(screenCenterX) - remint(getPosition().getX())) * ts);
    }

    /**
     * Converts world coordinates to screen coordinates
     * @param placeY a world (place) coordinate (y axis)
     * @return a screen coordinate y
     */
    int getScreenPosY(final int placeY) {
        final double ts = getTileSize();
        final double screenCenterY = getHeight() / ts / 2;
        final int placeYOffset = (int) (Math.round(getPosition().getY()) - Math.round(screenCenterY));
        return (int) ((-placeY + placeYOffset - remint(screenCenterY) + remint(getPosition().getY())) * ts + getHeight());
    }

    // ========================= place (group) selection =======================
    /**
     * Clears the box/shift selection box
     */
    private void placeGroupBoxResetSelection() {
        placeGroupBoxEnd = placeGroupBoxStart = null;
    }

    /**
     * Modifies the box/shift selection box (eg on shift + direction key)
     * @param x new coordinate
     * @param y new coordinate
     */
    void placeGroupBoxModifySelection(final int x, final int y) {
        placeGroup.clear();
        placeGroupBoxEnd = new WorldCoordinate(getPosition().getLayer(), x, y);
        // reset if layer changed
        if (placeGroupBoxStart != null && placeGroupBoxStart.getLayer() != placeGroupBoxEnd.getLayer()) {
            placeGroupBoxStart = null;
        }
        // set start, if not set
        if (placeGroupBoxStart == null) {
            placeGroupBoxStart = placeGroupBoxEnd;
        }
    }

    /**
     * Moves the box/shift selection to the selected places list
     */
    void placeGroupBoxSelectionToList() {
        if (placeGroupBoxEnd != null && placeGroupBoxStart != null) {
            final int x1 = (int) Math.round(placeGroupBoxEnd.getX());
            final int x2 = (int) Math.round(placeGroupBoxStart.getX());
            final int y1 = (int) Math.round(placeGroupBoxEnd.getY());
            final int y2 = (int) Math.round(placeGroupBoxStart.getY());

            final int x_min = Math.min(x1, x2);
            final int x_max = Math.max(x1, x2);
            final int y_min = Math.min(y1, y2);
            final int y_max = Math.max(y1, y2);

            final Layer layer = getWorld().getLayer(placeGroupBoxEnd.getLayer());

            for (int x = x_min; x <= x_max; ++x) {
                for (int y = y_min; y <= y_max; ++y) {
                    final Place pl = layer.get(x, y);
                    if (pl != null) {
                        placeGroup.add(pl);
                    }
                }
            }
        }
        placeGroupBoxResetSelection();
    }

    /**
     * adds a place to the place selection list (eg on ctrl + click)
     * @param pl
     */
    void placeGroupAdd(final Place pl) {
        placeGroupBoxSelectionToList();
        // clear list, if new place is on a different layer
        if (!placeGroup.isEmpty() && placeGroup.iterator().next().getLayer() != pl.getLayer()) {
            placeGroup.clear();
        }
        if (pl != null) {
            if (placeGroup.contains(pl)) {
                placeGroup.remove(pl);
            } else {
                placeGroup.add(pl);
            }
        }
    }

    /**
     * Sets the selection to a new set
     * @param set
     */
    void placeGroupSet(final HashSet<Place> set) {
        placeGroup.clear();
        placeGroup = set;
    }

    /**
     * Clears the selected places list and the shift selection
     */
    void placeGroupReset() {
        placeGroup.clear();
        placeGroupBoxResetSelection();
    }

    /**
     * Returns true, if places are selected
     * @return
     */
    public boolean placeGroupHasSelection() {
        return placeGroupBoxStart != null && placeGroupBoxEnd != null || !placeGroup.isEmpty();
    }

    /**
     * gets all selected places
     * @return
     */
    public HashSet<Place> placeGroupGetSelection() {
        if (placeGroupBoxStart != null) {
            placeGroupBoxSelectionToList();
        }
        return placeGroup;
    }

    // ======================= DRAW WORLD HERE =============================

    @Override
    public void paintComponent(final Graphics g) {
        mappainter.setSelectedPlaces(placeGroup, placeGroupBoxStart, placeGroupBoxEnd);
        mappainter.selectPlaceAt(getCursorX(), getCursorY());
        mappainter.setSelectionVisible(isCursorEnabled());

        final Layer layer = getWorld().getLayer(getPosition().getLayer());

        if (layer == null || layer.isEmpty()) {
            final FontMetrics fm = g.getFontMetrics();

            final String strAddPLace = "Do a right click to add and change places";
            g.drawString(strAddPLace, (getWidth() - fm.stringWidth(strAddPLace)) / 2, getHeight() / 2 - fm.getHeight() * 2);

            final String strEditWorld = "Change world settings in the World menu";
            g.drawString(strEditWorld, (getWidth() - fm.stringWidth(strEditWorld)) / 2, getHeight() / 2);

            final String strSidebar = "Use the side bar to go to other maps and places";
            g.drawString(strSidebar, (getWidth() - fm.stringWidth(strSidebar)) / 2, getHeight() / 2 + fm.getHeight() * 2);
        } else {
            mappainter.paint(g, (int) getTileSize(), getWidth(), getHeight(), layer, getPosition());
        }
    }

    // ========================= Listeners and context menu ================

    public void addPlaceSelectionListener(final PlaceSelectionListener listener) {
        if (!placeSelectionListeners.contains(listener)) {
            placeSelectionListeners.add(listener);
        }
    }

    public void removePlaceSelectionListener(final PlaceSelectionListener listener) {
        if (placeSelectionListeners.contains(listener)) {
            placeSelectionListeners.remove(listener);
        }
    }

    public void callPlaceSelectionListeners(final Place place) {
        for (final PlaceSelectionListener listener : placeSelectionListeners) {
            listener.placeSelected(place);
        }
    }

    public void callPlaceDeselectionListeners(final Place place) {
        for (final PlaceSelectionListener listener : placeSelectionListeners) {
            listener.placeDeselected(place);
        }
    }

    /**
     * Adds a place selection listener
     * @param listener
     */
    public void addCursorListener(final MapCursorListener listener) {
        if (!mapCursorListeners.contains(listener)) {
            mapCursorListeners.add(listener);
        }
    }

    /**
     * Removes a place selection listener
     * @param listener
     */
    public void removeCursorListener(final MapCursorListener listener) {
        mapCursorListeners.remove(listener);
    }

    /**
     * calls all place selection listeners
     */
    private void callCursorListeners() {
        final Layer layer = getWorld().getLayer(getPosition().getLayer());
        Place place = null;

        if (layer != null) {
            place = layer.get(getCursorX(), getCursorY());
        }

        if (place != null) {
            for (final MapCursorListener listener : mapCursorListeners) {
                listener.placeSelected(place);
            }
            callPlaceSelectionListeners(place);
        } else {
            for (final MapCursorListener listener : mapCursorListeners) {
                listener.placeDeselected(layer, getCursorX(), getCursorY());
            }
        }

        repaint();
    }

    /**
     * Adds a tileSize listener
     * @param listener
     */
    public void addTileSizeListener(final WorldPanelListener listener) {
        if (!tileSizeListeners.contains(listener)) {
            tileSizeListeners.add(listener);
        }
    }

    /**
     * Removes a tileSize listener
     * @param listener
     */
    public void removeTileSizeListener(final WorldPanelListener listener) {
        tileSizeListeners.remove(listener);
    }

    /**
     * Adds a status listener
     * @param listener
     */
    public void addStatusListener(final StatusListener listener) {
        if (!statusListeners.contains(listener)) {
            statusListeners.add(listener);
        }
    }

    public void callTileSizeListeners() {
        for (final WorldPanelListener listener : tileSizeListeners) {
            listener.TileSizeChanged();
        }
    }

    public void callLayerChangeListeners(final Layer l) {
        for (final WorldPanelListener listener : tileSizeListeners) {
            listener.LayerChanged(l);
        }
    }

    /**
     * Removes a status listener
     * @param listener
     */
    public void removeStatusListener(final StatusListener listener) {
        statusListeners.remove(listener);
    }

    public void callStatusUpdateListeners() {
        for (final StatusListener listener : statusListeners) {
            listener.statusUpdate();
        }
    }

    public void callMessageListeners(final String message) {
        for (final StatusListener listener : statusListeners) {
            listener.messageReceived(message);
        }
    }

    @Override
    public void worldChanged(final Object source) {
        repaint();
    }

    public WorldCoordinate getPlaceGroupBoxStart() {
        return placeGroupBoxStart;
    }

    public HashSet<Place> getPlaceGroup() {
        return placeGroup;
    }

    public boolean isMouseInPanel() {
        return mouseInPanel;
    }

    public void setMouseInPanel(final boolean mouseInPanel) {
        this.mouseInPanel = mouseInPanel;
    }

    public int getMouseXPrevious() {
        return mouseXPrevious;
    }

    public void setMouseXPrevious(final int mouseXPrevious) {
        this.mouseXPrevious = mouseXPrevious;
    }

    public int getMouseYPrevious() {
        return mouseYPrevious;
    }

    public void setMouseYPrevious(final int mouseYPrevious) {
        this.mouseYPrevious = mouseYPrevious;
    }

}
