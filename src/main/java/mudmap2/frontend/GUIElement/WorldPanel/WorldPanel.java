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

import java.awt.Component;
import java.awt.Event;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import mudmap2.backend.Layer;
import mudmap2.backend.LayerElement;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldChangeListener;
import mudmap2.backend.WorldCoordinate;
import mudmap2.frontend.dialog.placeGroup.PlaceGroupDialog;
import mudmap2.frontend.dialog.PathConnectDialog;
import mudmap2.frontend.dialog.PathConnectNeighborsDialog;
import mudmap2.frontend.dialog.PlaceCommentDialog;
import mudmap2.frontend.dialog.PlaceDialog;
import mudmap2.frontend.dialog.PlaceRemoveDialog;
import mudmap2.frontend.dialog.PlaceSelectionDialog;

/**
 *
 * @author neop
 */
public class WorldPanel extends JPanel implements WorldChangeListener {
    private static final long serialVersionUID = 1L;

    // tile size in pixel
    public static final int TILE_SIZE_MIN = 10;
    public static final int TILE_SIZE_MAX = 200;

    JFrame parentFrame;

    MapPainter mappainter;

    // passive worldpanels don't modify the world
    final boolean passive;

    World world;

    // tile size in pixel
    double tileSize;

    int cursorX, cursorY;
    boolean cursorEnabled, cursorForced;

    HashSet<PlaceSelectionListener> placeSelectionListeners;
    HashSet<MapCursorListener> mapCursorListeners;
    HashSet<StatusListener> statusListeners;
    HashSet<WorldPanelListener> tileSizeListeners;

    /**
     * positionsTail contains all previously visited positions up to the current
     * position, positionsHead contains all positions visited after the current
     * one. In case the 'previous button' gets used the top position of head
     * gets popped and pushed to tail. At least one position needs to remain in
     * tail.
     */
    LinkedList<WorldCoordinate> positionsHead, positionsTail;

    // true, if the mouse is in the panel, for relative motion calculation
    boolean mouseInPanel;
    // previous position of the mouse
    int mouseXPrevious, mouseYPrevious;

    // place (group) selection
    WorldCoordinate placeGroupBoxStart, placeGroupBoxEnd;
    HashSet<Place> placeGroup;

    // true, if a context menu is shown (to disable forced focus)
    boolean isContextMenuShown;
    boolean forcedFocus;

    /**
     * Constructs a world panel
     * @param parent
     * @param world
     * @param passive
     */
    public WorldPanel(JFrame parent, World world, boolean passive) {
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
            public void focusLost(FocusEvent arg0) {
                if(isFocusForced()) requestFocusInWindow();
            }
        });

        addKeyListener(new TabKeyPassiveListener(this));
        addMouseListener(new TabMousePassiveListener());
        if(!passive){
            addKeyListener(new TabKeyListener(this));
            addMouseListener(new TabMouseListener());
        }
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double ts = getTileSize();
                int delta = -e.getWheelRotation();

                ts = Math.exp(Math.log(ts / 10) + delta * 0.05) * 10;
                if(delta > 0) ts = Math.max(ts, getTileSize() + 1);
                else if(delta < 0) ts = Math.min(ts, getTileSize() - 1);
                setTileSize(ts);
            }
        });
        addMouseMotionListener((MouseMotionListener) new TabMouseMotionListener());

        if(!passive) world.addChangeListener(this);
    }

    public MapPainter getMappainter() {
        return mappainter;
    }

    public boolean isPassive(){
        return passive;
    }

    /**
     * Returs true, if forced focus can be enabled
     * @return
     */
    public boolean isFocusForced(){
        return forcedFocus && !hasContextMenu();
    }

    public void setFocusForced(Boolean b){
        forcedFocus = b;
    }

   /**
     * Remove integer part, the part after the point remains
     * @param val
     * @return
     */
    private static double remint(double val){
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
    public void gotoHome(){
        pushPosition(getWorld().getHome());
        setCursor((int) Math.round(getPosition().getX()), (int) Math.round(getPosition().getY()));
    }

    /**
     * Set a new home position
     */
    public void setHome(){
        getWorld().setHome(new WorldCoordinate(getPosition()));
    }

    /**
     * Get the selected place or null
     * @return place or null
     */
    public Place getSelectedPlace(){
        Layer layer = getWorld().getLayer(getPosition().getLayer());
        Place ret = null;
        if(layer != null){
            ret = layer.get(getCursorX(), getCursorY());
        }
        return ret;
    }

    // ========================= context menu ==================================

    /**
     * Set whether a context menu is shown, to disable forced focus
     * @param b
     */
    private void setContextMenu(boolean b) {
        isContextMenuShown = b;
    }

    /**
     * Returns true, if a context menu is shown and forced focus is disabled
     * @return
     */
    private boolean hasContextMenu(){
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
    public void setTileSize(double tileSize) {
        this.tileSize = Math.min(Math.max(tileSize, WorldPanel.TILE_SIZE_MIN), WorldPanel.TILE_SIZE_MAX);
        callTileSizeListeners();
        repaint();
    }

    /**
     * increases the tile size
     */
    public void tileSizeIncrement(){
        double ts = getTileSize();
        ts = Math.exp(Math.log(ts / 10) + 0.03) * 10;
        ts = Math.min(ts, TILE_SIZE_MAX);
        setTileSize(Math.min(Math.max(ts, getTileSize() + 1), TILE_SIZE_MAX));
    }

    /**
     * decreases the tile size
     */
    public void tileSizeDecrement(){
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
    public void pushPosition(WorldCoordinate coord){
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
        if(positionsTail.size() > 1){
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
    public void restorePosition(){
        if(!positionsHead.isEmpty()){
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
    public void resetHistory(WorldCoordinate pos){
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
    public WorldCoordinate getPosition(){
        WorldCoordinate ret;
        if(positionsTail.isEmpty()){
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
    public LinkedList<WorldCoordinate> getHistory(){
        return new LinkedList<>(positionsTail);
    }

    /**
     * Replace history, clears list of positions ahead
     * @param list
     */
    public void setHistory(LinkedList<WorldCoordinate> list){
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

    public void setCursorForced(boolean cursorForced) {
        this.cursorForced = cursorForced;
    }

    public boolean isCursorEnabled() {
        return cursorEnabled || cursorForced;
    }

    public void setCursorEnabled(boolean cursorEnabled) {
        this.cursorEnabled = cursorEnabled;
        callCursorListeners();
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public void setCursor(int x, int y){
        cursorX = x;
        cursorY = y;
        callCursorListeners();
    }

    public void moveCursor(int dx, int dy){
        cursorX += dx;
        cursorY += dy;
        moveScreenToCursor();
    }

    /**
     * moves the shown places so the selection is on the screen
     */
    public void moveScreenToCursor(){
        final double screenX = getScreenPosX(cursorX);
        final double screenY = getScreenPosY(cursorY);
        final double ts = getTileSize();

        double dx = 0, dy = 0;

        if(screenX < 0){
            dx = screenX / ts;
        } else if(screenX > getWidth() - ts){
            dx = (screenX - getWidth()) / ts + 1;
        }

        if(screenY < 0){
            dy = -screenY / ts;
        } else if(screenY > getHeight() - ts){
            dy = -(screenY - getHeight()) / ts - 1;
        }

        if(dx != 0 || dy != 0) getPosition().move((int) dx, (int) dy);

        callCursorListeners();
    }

    // ========================= map coordinate maths ==========================

    /**
     * Converts screen coordinates to world coordinates
     * @param screen_x a screen coordinate (x-axis)
     * @return world coordinate x
     */
    private int getPlacePosX(int screen_x){
        return (int) Math.ceil((screen_x - getWidth() / 2) / getTileSize() + getPosition().getX()) - 1;
    }

    /**
     * Converts screen coordinates to world coordinates
     * @param mouse_y a screen coordinate (y-axis)
     * @return world coordinate y
     */
    private int getPlacePosY(int screen_y){
        return (int) -Math.ceil((screen_y - getHeight() / 2) / getTileSize() - getPosition().getY()) + 1;
    }

    /**
     * Converts world coordinates to screen coordinates
     * @param placeX a world (place) coordinate (x axis)
     * @return a screen coordinate x
     */
    private int getScreenPosX(int placeX){
        double ts = getTileSize();
        double screenCenterX = (getWidth() / ts) / 2; // note: wdtwd2
        int placeXOffset = (int) (Math.round(getPosition().getX()) - Math.round(screenCenterX));
        return (int)((placeX - placeXOffset + remint(screenCenterX) - remint(getPosition().getX())) * ts);
    }

    /**
     * Converts world coordinates to screen coordinates
     * @param placeY a world (place) coordinate (y axis)
     * @return a screen coordinate y
     */
    private int getScreenPosY(int placeY){
        double ts = getTileSize();
        double screenCenterY = (getHeight() / ts) / 2;
        int placeYOffset = (int) (Math.round(getPosition().getY()) - Math.round(screenCenterY));
        return (int)((-placeY + placeYOffset - remint(screenCenterY) + remint(getPosition().getY())) * ts + getHeight());
    }

    // ========================= place (group) selection =======================
    /**
     * Clears the box/shift selection box
     */
    private void placeGroupBoxResetSelection(){
        placeGroupBoxEnd = placeGroupBoxStart = null;
    }

    /**
     * Modifies the box/shift selection box (eg on shift + direction key)
     * @param x new coordinate
     * @param y new coordinate
     */
    private void placeGroupBoxModifySelection(int x, int y){
        placeGroup.clear();
        placeGroupBoxEnd = new WorldCoordinate(getPosition().getLayer(), x, y);
        // reset if layer changed
        if(placeGroupBoxStart != null && placeGroupBoxStart.getLayer() != placeGroupBoxEnd.getLayer()) placeGroupBoxStart = null;
        // set start, if not set
        if(placeGroupBoxStart == null) placeGroupBoxStart = placeGroupBoxEnd;
    }

    /**
     * Moves the box/shift selection to the selected places list
     */
    private void placeGroupBoxSelectionToList(){
        if(placeGroupBoxEnd != null && placeGroupBoxStart != null){
            int x1 = (int) Math.round(placeGroupBoxEnd.getX());
            int x2 = (int) Math.round(placeGroupBoxStart.getX());
            int y1 = (int) Math.round(placeGroupBoxEnd.getY());
            int y2 = (int) Math.round(placeGroupBoxStart.getY());

            int x_min = Math.min(x1, x2);
            int x_max = Math.max(x1, x2);
            int y_min = Math.min(y1, y2);
            int y_max = Math.max(y1, y2);

            Layer layer = getWorld().getLayer(placeGroupBoxEnd.getLayer());

            for(int x = x_min; x <= x_max; ++x){
                for(int y = y_min; y <= y_max; ++y){
                    Place pl = layer.get(x, y);
                    if(pl != null) placeGroup.add(pl);
                }
            }
        }
        placeGroupBoxResetSelection();
    }

    /**
     * adds a place to the place selection list (eg on ctrl + click)
     * @param pl
     */
    private void placeGroupAdd(Place pl){
        placeGroupBoxSelectionToList();
        // clear list, if new place is on a different layer
        if(!placeGroup.isEmpty() && placeGroup.iterator().next().getLayer() != pl.getLayer()) placeGroup.clear();
        if(pl != null){
            if(placeGroup.contains(pl)) placeGroup.remove(pl);
            else placeGroup.add(pl);
        }
    }

    /**
     * Sets the selection to a new set
     * @param set
     */
    private void placeGroupSet(HashSet<Place> set){
        placeGroup.clear();
        placeGroup = set;
    }

    /**
     * Clears the selected places list and the shift selection
     */
    private void placeGroupReset(){
        placeGroup.clear();
        placeGroupBoxResetSelection();
    }

    /**
     * Returns true, if places are selected
     * @return
     */
    public boolean placeGroupHasSelection(){
        return (placeGroupBoxStart != null && placeGroupBoxEnd != null) || !placeGroup.isEmpty();
    }

    /**
     * gets all selected places
     * @return
     */
    public HashSet<Place> placeGroupGetSelection(){
        if(placeGroupBoxStart != null) placeGroupBoxSelectionToList();
        return placeGroup;
    }

    // ======================= DRAW WORLD HERE =============================

    @Override
    public void paintComponent(Graphics g){
        mappainter.setSelectedPlaces(placeGroup, placeGroupBoxStart, placeGroupBoxEnd);
        mappainter.selectPlaceAt(getCursorX(), getCursorY());
        mappainter.setSelectionVisible(isCursorEnabled());

        Layer layer = getWorld().getLayer(getPosition().getLayer());

        if(layer == null || layer.isEmpty()){
            FontMetrics fm = g.getFontMetrics();

            String strAddPLace = "Do a right click to add and change places";
            g.drawString(strAddPLace, (getWidth() - fm.stringWidth(strAddPLace)) / 2, getHeight() / 2 - fm.getHeight() * 2);

            String strEditWorld = "Change world settings in the World menu";
            g.drawString(strEditWorld, (getWidth() - fm.stringWidth(strEditWorld)) / 2, getHeight() / 2);

            String strSidebar = "Use the side bar to go to other maps and places";
            g.drawString(strSidebar, (getWidth() - fm.stringWidth(strSidebar)) / 2, getHeight() / 2 + fm.getHeight() * 2);
        } else {
            mappainter.paint(g, (int) getTileSize(), getWidth(), getHeight(), layer, getPosition());
        }
    }

    // ========================= Listeners and context menu ================

    public void addPlaceSelectionListener(PlaceSelectionListener listener){
        if(!placeSelectionListeners.contains(listener)){
            placeSelectionListeners.add(listener);
        }
    }

    public void removePlaceSelectionListener(PlaceSelectionListener listener){
        if(placeSelectionListeners.contains(listener)){
            placeSelectionListeners.remove(listener);
        }
    }

    public void callPlaceSelectionListeners(Place place){
        for(PlaceSelectionListener listener: placeSelectionListeners){
            listener.placeSelected(place);
        }
    }

    public void callPlaceDeselectionListeners(Place place){
        for(PlaceSelectionListener listener: placeSelectionListeners){
            listener.placeDeselected(place);
        }
    }

    /**
     * Adds a place selection listener
     * @param listener
     */
    public void addCursorListener(MapCursorListener listener){
        if(!mapCursorListeners.contains(listener))
            mapCursorListeners.add(listener);
    }

    /**
     * Removes a place selection listener
     * @param listener
     */
    public void removeCursorListener(MapCursorListener listener){
        mapCursorListeners.remove(listener);
    }

    /**
     * calls all place selection listeners
     */
    private void callCursorListeners(){
        Layer layer = getWorld().getLayer(getPosition().getLayer());
        Place place = null;

        if(layer != null){
            place = layer.get(getCursorX(), getCursorY());
        }

        if(place != null){
            for(MapCursorListener listener: mapCursorListeners)
                listener.placeSelected(place);
            callPlaceSelectionListeners(place);
        } else {
            for(MapCursorListener listener: mapCursorListeners)
                listener.placeDeselected(layer, getCursorX(), getCursorY());
        }

        repaint();
    }

    /**
     * Adds a tileSize listener
     * @param listener
     */
    public void addTileSizeListener(WorldPanelListener listener){
        if(!tileSizeListeners.contains(listener))
            tileSizeListeners.add(listener);
    }

    /**
     * Removes a tileSize listener
     * @param listener
     */
    public void removeTileSizeListener(WorldPanelListener listener){
        tileSizeListeners.remove(listener);
    }

    /**
     * Adds a status listener
     * @param listener
     */
    public void addStatusListener(StatusListener listener){
        if(!statusListeners.contains(listener))
            statusListeners.add(listener);
    }

    public void callTileSizeListeners(){
        for(WorldPanelListener listener: tileSizeListeners){
            listener.TileSizeChanged();
        }
    }

    public void callLayerChangeListeners(Layer l){
        for(WorldPanelListener listener: tileSizeListeners){
            listener.LayerChanged(l);
        }
    }

    /**
     * Removes a status listener
     * @param listener
     */
    public void removeStatusListener(StatusListener listener){
        statusListeners.remove(listener);
    }

    public void callStatusUpdateListeners(){
        for(StatusListener listener: statusListeners){
            listener.statusUpdate();
        }
    }

    public void callMessageListeners(String message){
        for(StatusListener listener: statusListeners){
            listener.messageReceived(message);
        }
    }

    @Override
    public void worldChanged(Object source) {
        repaint();
    }

    /**
     * This listener only contains actions, that don't modify the world
     */
    private class TabMousePassiveListener extends TabMouseListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON3){ // right click
                // show context menu
                ContextMenu context_menu = new ContextMenu(WorldPanel.this, getPlacePosX(e.getX()), getPlacePosY(e.getY()));
                context_menu.show(e.getComponent(), e.getX(), e.getY());
            } else if(e.getButton() == MouseEvent.BUTTON1){ // left click
                if(!e.isShiftDown()){ // left click + hift gets handled in active listener
                    // set place selection to coordinates if keyboard selection is enabled
                    setCursor(getPlacePosX(e.getX()), getPlacePosY(e.getY()));
                }
            }
        }
    }

    /**
     * This listener contains actions that modify the world
     */
    private class TabMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            JFrame rootFrame = (JFrame) SwingUtilities.getRoot(e.getComponent());

            if(e.getButton() == MouseEvent.BUTTON1){ // left click
                Place place = getWorld().getLayer(getPosition().getLayer()).get(getPlacePosX(e.getX()), getPlacePosY(e.getY()));
                if(e.isControlDown()){ // left click + ctrl
                    if(place != null) placeGroupAdd(place);
                } else if(!e.isShiftDown()) { // left click and not shift
                    placeGroupReset();
                    if(e.getClickCount() > 1){ // double click
                        if(place != null) (new PlaceDialog(rootFrame, getWorld(), place)).setVisible(true);
                        else (new PlaceDialog(rootFrame, getWorld(), getWorld().getLayer(getPosition().getLayer()), getPlacePosX(e.getX()), getPlacePosY(e.getY()))).setVisible(true);
                    }
                } else {
                    if(!placeGroupHasSelection())
                        placeGroupBoxModifySelection(getCursorX(), getCursorY());
                    placeGroupBoxModifySelection(getPlacePosX(e.getX()), getPlacePosY(e.getY()));
                    // cursor has to be set after the selection -> not handled by passive listener
                    setCursor(getPlacePosX(e.getX()), getPlacePosY(e.getY()));
                }
            }
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            requestFocusInWindow();
        }

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {
            mouseInPanel = true;
            mouseXPrevious = e.getX();
            mouseYPrevious = e.getY();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            mouseInPanel = false;
        }
    }

    private class TabMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
            if(mouseInPanel){
                double dx = (double) (e.getX() - mouseXPrevious) / getTileSize();
                double dy = (double) (e.getY() - mouseYPrevious) / getTileSize();
                if(!e.isShiftDown()) // shift not pressed: move view
                    getPosition().move(-dx , dy);
                else { // shift pressed: box selection
                    placeGroupBoxModifySelection(getPlacePosX(e.getX()), getPlacePosY(e.getY()));
                }
                repaint();
            }
            mouseXPrevious = e.getX();
            mouseYPrevious = e.getY();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseXPrevious = e.getX();
            mouseYPrevious = e.getY();
        }
    }

    /**
     * This listener only contains actions that don't modify the world
     */
    private class TabKeyPassiveListener extends TabKeyListener {
        public TabKeyPassiveListener(WorldPanel parent){
            super(parent);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(!e.isShiftDown() && !e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown()){ // ctrl, shift and alt not pressed
                int xBef = getCursorX();
                int yBef = getCursorY();

                switch(e.getKeyCode()){
                    // zoom the map
                    case KeyEvent.VK_PLUS:
                    case KeyEvent.VK_ADD:
                    case KeyEvent.VK_PAGE_UP:
                        tileSizeIncrement();
                        break;
                    case KeyEvent.VK_MINUS:
                    case KeyEvent.VK_SUBTRACT:
                    case KeyEvent.VK_PAGE_DOWN:
                        tileSizeDecrement();
                        break;

                    // enable / disable cursor
                    case KeyEvent.VK_P:
                        setCursorEnabled(!isCursorEnabled());
                        break;

                    // shift place selection - wasd
                    case KeyEvent.VK_NUMPAD8:
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        if(isCursorEnabled()) moveCursor(0, +1);
                        break;
                    case KeyEvent.VK_NUMPAD4:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        if(isCursorEnabled()) moveCursor(-1, 0);
                        break;
                    case KeyEvent.VK_NUMPAD2:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        if(isCursorEnabled()) moveCursor(0, -1);
                        break;
                    case KeyEvent.VK_NUMPAD6:
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        if(isCursorEnabled()) moveCursor(+1, 0);
                        break;

                    // diagonal movement
                    case KeyEvent.VK_NUMPAD1:
                        if(isCursorEnabled()) moveCursor(-1, -1);
                        break;
                    case KeyEvent.VK_NUMPAD3:
                        if(isCursorEnabled()) moveCursor(+1, -1);
                        break;
                    case KeyEvent.VK_NUMPAD7:
                        if(isCursorEnabled()) moveCursor(-1, +1);
                        break;
                    case KeyEvent.VK_NUMPAD9:
                        if(isCursorEnabled()) moveCursor(+1, +1);
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

                int xSel = getCursorX();
                int ySel = getCursorY();

                // change group selection, if place selection changed
                if(xSel != xBef || ySel != yBef){
                    if(parent.placeGroupBoxStart != null) parent.placeGroupBoxSelectionToList();
                }
            }
        }
    }

    /**
     * This listener contains actions, that modify the world
     */
    private class TabKeyListener implements KeyListener {

        WorldPanel parent;

        public TabKeyListener(WorldPanel parent){
            this.parent = parent;
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            JFrame rootFrame = (JFrame) SwingUtilities.getRoot(parent);
            if(e.isControlDown()){ // ctrl key pressed
                Place place, other;

                switch(e.getKeyCode()){
                    case KeyEvent.VK_A: // select all places
                        parent.placeGroupSet(getWorld().getLayer(getPosition().getLayer()).getPlaces());
                        break;
                    case KeyEvent.VK_X: // cut selected places
                        if(!parent.placeGroupGetSelection().isEmpty()){ // cut group selection
                            mudmap2.CopyPaste.cut(parent.placeGroup, getCursorX(), getCursorY());
                            callMessageListeners(parent.placeGroup.size() + " places cut");
                            parent.placeGroupReset();
                        } else if(parent.getSelectedPlace() != null){ // cut cursor selection
                            HashSet<Place> tmp_selection = new HashSet<>();
                            tmp_selection.add(parent.getSelectedPlace());
                            mudmap2.CopyPaste.cut(tmp_selection, getCursorX(), getCursorY());
                            callMessageListeners("1 place cut");
                        } else callMessageListeners("No places cut: selection empty");
                        break;
                    case KeyEvent.VK_C: // copy selected places
                        if(!parent.placeGroupGetSelection().isEmpty()){ // copy group selection
                            mudmap2.CopyPaste.copy(parent.placeGroup, getCursorX(), getCursorY());
                            callMessageListeners(parent.placeGroup.size() + " places copied");
                            parent.placeGroupReset();
                        } else if(parent.getSelectedPlace() != null){ // copy cursor selection
                            HashSet<Place> tmp_selection = new HashSet<>();
                            tmp_selection.add(parent.getSelectedPlace());
                            mudmap2.CopyPaste.copy(tmp_selection, getCursorX(), getCursorY());
                            callMessageListeners("1 place copied");
                        } else {
                            mudmap2.CopyPaste.resetCopy();
                            callMessageListeners("No places copied: selection empty");
                        }
                        break;
                    case KeyEvent.VK_V: // paste copied / cut places
                        if(mudmap2.CopyPaste.hasCopyPlaces()){
                            if(mudmap2.CopyPaste.canPaste(getCursorX(), getCursorY(), getWorld().getLayer(getPosition().getLayer()))){
                                int paste_num = mudmap2.CopyPaste.getCopyPlaces().size();
                                if(mudmap2.CopyPaste.paste(getCursorX(), getCursorY(), getWorld().getLayer(getPosition().getLayer()))){
                                    callMessageListeners(paste_num + " places pasted");
                                } else {
                                    callMessageListeners("No places pasted");
                                }
                            } else {
                                callMessageListeners("Can't paste: not enough free space on map");
                            }
                        } else {
                            mudmap2.CopyPaste.resetCopy();
                            callMessageListeners("Can't paste: no places cut or copied");
                        }
                        break;

                    case KeyEvent.VK_NUMPAD8:
                    case KeyEvent.VK_UP:
                    //case KeyEvent.VK_W: // add path to direction 'n'
                        place = getSelectedPlace();
                        other = getWorld().getLayer(getPosition().getLayer()).get(getCursorX(), getCursorY() + 1);
                        if(place != null && other != null){ // if places exist
                            if(place.getExit("n") == null && other.getExit("s") == null){ // if exits aren't occupied
                                place.connectPath(new Path(place, "n", other, "s"));
                            }
                        }
                        break;
                    case KeyEvent.VK_NUMPAD9: // add path to direction 'ne'
                        place = getSelectedPlace();
                        other = getWorld().getLayer(getPosition().getLayer()).get(getCursorX() + 1, getCursorY() + 1);
                        if(place != null && other != null){ // if places exist
                            if(place.getExit("ne") == null && other.getExit("sw") == null){ // if exits aren't occupied
                                place.connectPath(new Path(place, "ne", other, "sw"));
                            }
                        }
                        break;
                    case KeyEvent.VK_NUMPAD6:
                    case KeyEvent.VK_RIGHT:
                    //case KeyEvent.VK_D: // add path to direction 'e'
                        place = getSelectedPlace();
                        other = getWorld().getLayer(getPosition().getLayer()).get(getCursorX() + 1, getCursorY());
                        if(place != null && other != null){ // if places exist
                            if(place.getExit("e") == null && other.getExit("w") == null){ // if exits aren't occupied
                                place.connectPath(new Path(place, "e", other, "w"));
                            }
                        }
                        break;
                    case KeyEvent.VK_NUMPAD3: // add path to direction 'se'
                        place = getSelectedPlace();
                        other = getWorld().getLayer(getPosition().getLayer()).get(getCursorX() + 1, getCursorY() - 1);
                        if(place != null && other != null){ // if places exist
                            if(place.getExit("se") == null && other.getExit("nw") == null){ // if exits aren't occupied
                                place.connectPath(new Path(place, "se", other, "nw"));
                            }
                        }
                        break;
                    case KeyEvent.VK_NUMPAD2:
                    case KeyEvent.VK_DOWN:
                    //case KeyEvent.VK_S: // add path to direction 's'
                        place = getSelectedPlace();
                        other = getWorld().getLayer(getPosition().getLayer()).get(getCursorX(), getCursorY() - 1);
                        if(place != null && other != null){ // if places exist
                            if(place.getExit("s") == null && other.getExit("n") == null){ // if exits aren't occupied
                                place.connectPath(new Path(place, "s", other, "n"));
                            }
                        }
                        break;
                    case KeyEvent.VK_NUMPAD1: // add path to direction 'sw'
                        place = getSelectedPlace();
                        other = getWorld().getLayer(getPosition().getLayer()).get(getCursorX() - 1, getCursorY() - 1);
                        if(place != null && other != null){ // if places exist
                            if(place.getExit("sw") == null && other.getExit("ne") == null){ // if exits aren't occupied
                                place.connectPath(new Path(place, "sw", other, "ne"));
                            }
                        }
                        break;
                    case KeyEvent.VK_NUMPAD4:
                    case KeyEvent.VK_LEFT:
                    //case KeyEvent.VK_A: // add path to direction 'w'
                        place = getSelectedPlace();
                        other = getWorld().getLayer(getPosition().getLayer()).get(getCursorX() - 1, getCursorY());
                        if(place != null && other != null){ // if places exist
                            if(place.getExit("w") == null && other.getExit("e") == null){ // if exits aren't occupied
                                place.connectPath(new Path(place, "w", other, "e"));
                            }
                        }
                        break;
                    case KeyEvent.VK_NUMPAD7: // add path to direction 'nw'
                        place = getSelectedPlace();
                        other = getWorld().getLayer(getPosition().getLayer()).get(getCursorX() - 1, getCursorY() + 1);
                        if(place != null && other != null){ // if places exist
                            if(place.getExit("nw") == null && other.getExit("se") == null){ // if exits aren't occupied
                                place.connectPath(new Path(place, "nw", other, "se"));
                            }
                        }
                        break;
                    case KeyEvent.VK_NUMPAD5: // open add path dialog
                        (new PathConnectDialog(parentFrame, getSelectedPlace())).setVisible(true);
                        break;
                }
            } else if(e.isShiftDown()){ // shift key pressed -> modify selection
                int x_bef = getCursorX();
                int y_bef = getCursorY();

                switch(e.getKeyCode()){
                    case KeyEvent.VK_NUMPAD8:
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        if(isCursorEnabled()) moveCursor(0, +1);
                        break;
                    case KeyEvent.VK_NUMPAD4:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        if(isCursorEnabled()) moveCursor(-1, 0);
                        break;
                    case KeyEvent.VK_NUMPAD2:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        if(isCursorEnabled()) moveCursor(0, -1);
                        break;
                    case KeyEvent.VK_NUMPAD6:
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        if(isCursorEnabled()) moveCursor(+1, 0);
                        break;

                    // diagonal movement
                    case KeyEvent.VK_NUMPAD1:
                        if(isCursorEnabled()) moveCursor(-1, -1);
                        break;
                    case KeyEvent.VK_NUMPAD3:
                        if(isCursorEnabled()) moveCursor(+1, -1);
                        break;
                    case KeyEvent.VK_NUMPAD7:
                        if(isCursorEnabled()) moveCursor(-1, +1);
                        break;
                    case KeyEvent.VK_NUMPAD9:
                        if(isCursorEnabled()) moveCursor(+1, +1);
                        break;

                    case KeyEvent.VK_SPACE: // add or removePlace single place to place group selection
                        Place place = getSelectedPlace();
                        if(place != null) parent.placeGroupAdd(place);
                        break;
                }
                int x_sel = getCursorX();
                int y_sel = getCursorY();

                // change group selection, if place selection changed
                if(x_sel != x_bef || y_sel != y_bef){
                    if(parent.placeGroupBoxStart == null) parent.placeGroupBoxModifySelection(x_bef, y_bef);
                    parent.placeGroupBoxModifySelection(x_sel, y_sel);
                }
            } else if(e.isAltDown() || e.isAltGraphDown()){ // alt or altgr key pressed
                Place place = getSelectedPlace();
                Place other;
                Path path;

                if(place != null){
                    switch(e.getKeyCode()){
                        case KeyEvent.VK_NUMPAD8:
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W: // removePlace path to direction 'n'
                                path = place.getPathTo("n");
                                if(path != null) place.removePath(path);
                            break;
                        case KeyEvent.VK_NUMPAD9: // removePlace path to direction 'ne'
                                path = place.getPathTo("ne");
                                if(path != null) place.removePath(path);
                            break;
                        case KeyEvent.VK_NUMPAD6:
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D: // removePlace path to direction 'e'
                                path = place.getPathTo("e");
                                if(path != null) place.removePath(path);
                            break;
                        case KeyEvent.VK_NUMPAD3: // removePlace path to direction 'se'
                                path = place.getPathTo("se");
                                if(path != null) place.removePath(path);
                            break;
                        case KeyEvent.VK_NUMPAD2:
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S: // removePlace path to direction 's'
                                path = place.getPathTo("s");
                                if(path != null) place.removePath(path);
                            break;
                        case KeyEvent.VK_NUMPAD1: // removePlace path to direction 'sw'
                                path = place.getPathTo("sw");
                                if(path != null) place.removePath(path);
                            break;
                        case KeyEvent.VK_NUMPAD4:
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A: // removePlace path to direction 'w'
                                path = place.getPathTo("w");
                                if(path != null) place.removePath(path);
                            break;
                        case KeyEvent.VK_NUMPAD7: // removePlace path to direction 'nw'
                                path = place.getPathTo("nw");
                                if(path != null) place.removePath(path);
                            break;
                    }
                }
            } else { // ctrl, shift and alt not pressed
                switch(e.getKeyCode()){
                    // show context menu
                    case KeyEvent.VK_CONTEXT_MENU:
                        if(isCursorEnabled()){
                            ContextMenu context_menu = new ContextMenu(parent, getCursorX(), getCursorY());
                            context_menu.show(e.getComponent(), getScreenPosX(getCursorX()) + (int) getTileSize() / 2, getScreenPosY(getCursorY()) + (int) getTileSize() / 2);
                        }
                        break;

                    // edit / add place
                    case KeyEvent.VK_INSERT:
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_E:
                        if(isCursorEnabled()){
                            Place place = getSelectedPlace();
                            PlaceDialog dlg;

                            Layer layer = null;
                            if(getPosition() != null) layer = getWorld().getLayer(getPosition().getLayer());

                            if(place != null) dlg = new PlaceDialog(rootFrame, getWorld(), place);
                            else dlg = new PlaceDialog(rootFrame, getWorld(), getWorld().getLayer(getPosition().getLayer()), parent.getCursorX(), parent.getCursorY());
                            dlg.setVisible(true);

                            if(layer == null) pushPosition(dlg.getPlace().getCoordinate());
                        }
                        break;
                    // create placeholder
                    case KeyEvent.VK_F:
                        if(isCursorEnabled()){
                            Place place = getSelectedPlace();
                            // create placeholder or removePlace one
                            if(place == null){
                                getWorld().putPlaceholder(getPosition().getLayer(), parent.getCursorX(), parent.getCursorY());
                            } else if(place.getName().equals(Place.PLACEHOLDER_NAME)){
                                try {
                                    place.getLayer().remove(place);
                                } catch (RuntimeException ex) {
                                    Logger.getLogger(TabKeyListener.class.getName()).log(Level.SEVERE, null, ex);
                                    JOptionPane.showMessageDialog(parent, "Could not remove place: " + ex.getMessage());
                                }
                            }
                        }
                        repaint();
                        break;
                    // removePlace place
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_R:
                        if(!parent.placeGroupHasSelection()){ // no places selected
                            if(isCursorEnabled()){
                                Place place = getSelectedPlace();
                                if(place != null) (new PlaceRemoveDialog(rootFrame, getWorld(), place)).show();
                            }
                        } else { // places selected
                            HashSet<Place> place_group = parent.placeGroupGetSelection();
                            if(place_group != null){
                                PlaceRemoveDialog dlg = new PlaceRemoveDialog(rootFrame, getWorld(), place_group);
                                dlg.show();
                                // reset selection, if places were removed
                                if(dlg.getPlacesRemoved()) parent.placeGroupReset();
                            }
                        }
                        break;
                    // edit place comments
                    case KeyEvent.VK_C:
                        if(isCursorEnabled()){
                            Place place = getSelectedPlace();
                            if(place != null){
                                (new PlaceCommentDialog(rootFrame, place)).setVisible(true);
                                callStatusUpdateListeners();
                            }
                        }
                        break;
                    // modify place group
                    case KeyEvent.VK_Q:
                        Place place = getSelectedPlace();

                        if(!parent.placeGroupHasSelection()){
                            // no place selected
                            if(place == null) (new PlaceGroupDialog(rootFrame, getWorld())).setVisible(true);
                            // place selected
                            else (new PlaceGroupDialog(rootFrame, getWorld(), place)).setVisible(true);
                        }
                        break;

                    case KeyEvent.VK_SPACE: // add or removePlace single place to place group selection
                        place = getSelectedPlace();
                        if(place != null) parent.placeGroupAdd(place);
                        break;
                }
            }
            repaint();
        }

        @Override
        public void keyReleased(KeyEvent arg0) {}
    }

    // constructs the context menu (on right click)
    private class ContextMenu extends JPopupMenu implements ActionListener {

        private static final long serialVersionUID = 1L;

        final Layer layer;
        final Place place;
        final Integer posX;
        final Integer posY;

        /**
         * Constructs a context menu at position (x,y)
         * @param px screen / panel coordinate x
         * @param py screen / panel coordinate y
         */
        public ContextMenu(WorldPanel parent, Integer px, Integer py) {
            addPopupMenuListener(new TabContextPopMenuListener());
            JFrame rootFrame = (JFrame) SwingUtilities.getRoot(parent);

            this.posX = px;
            this.posY = py;
            layer = getWorld().getLayer(getPosition().getLayer());
            place = (layer != null ? layer.get(posX, posY) : null);

            parent.setCursor(posX, posY);

            if(layer != null && place != null){ // if place exists
                if(!passive){
                    JMenuItem miEdit = new JMenuItem("Edit place");
                    PlaceDialog pdlg = new PlaceDialog(rootFrame, getWorld(), place);
                    miEdit.addActionListener(pdlg);

                    add(miEdit);
                    miEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));

                    HashSet<Place> placeGroup = placeGroupGetSelection();

                    JMenuItem miRemove;
                    if(placeGroup.isEmpty()){
                        miRemove = new JMenuItem("Remove place");
                        miRemove.addActionListener(new PlaceRemoveDialog(rootFrame, getWorld(), place));
                        miRemove.setToolTipText("Remove this place");
                    } else {
                        miRemove = new JMenuItem("*Remove places");
                        miRemove.addActionListener(new PlaceRemoveDialog(rootFrame, getWorld(), placeGroup));
                        miRemove.setToolTipText("Remove all selected places");
                    }
                    add(miRemove);
                    miRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

                    JMenuItem miComments = new JMenuItem("Edit comments");
                    miComments.addActionListener(new PlaceCommentDialog(rootFrame, place));
                    miComments.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
                    add(miComments);

                    JMenuItem miPlaceGroup;
                    if(placeGroup.isEmpty()){
                        miPlaceGroup = new JMenuItem("Edit place group");
                        miPlaceGroup.addActionListener(new PlaceGroupDialog(rootFrame, getWorld(), place));
                        miPlaceGroup.setToolTipText("Edit the place group of this place");
                        add(miPlaceGroup);
                    }
                }

                // ------------- Paths ------------------
                JMenu mPaths = new JMenu("Paths / Exits");
                if(!passive || !place.getPaths().isEmpty())
                    add(mPaths);

                if(!passive){
                    JMenu mPathConnect = new JMenu("Connect");
                    mPaths.add(mPathConnect);
                    mPathConnect.setToolTipText("Connect a path from this place to another one");

                    JMenuItem miPathConnectSelect = new JMenuItem("Select");
                    mPathConnect.add(miPathConnectSelect);
                    miPathConnectSelect.setToolTipText("Select any place from the map");
                    miPathConnectSelect.addActionListener(new PathConnectDialog(parentFrame, place));
                    miPathConnectSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, Event.CTRL_MASK));

                    JMenuItem miPathConnectNeighbors = new JMenuItem("Neighbors");
                    mPathConnect.add(miPathConnectNeighbors);
                    miPathConnectNeighbors.setToolTipText("Choose from surrounding places");
                    miPathConnectNeighbors.addActionListener(new PathConnectNeighborsDialog(rootFrame, place));

                    LinkedList<Place> places = layer.getNeighbors(posX, posY, 1);
                    if(!places.isEmpty()){
                        mPathConnect.add(new JSeparator());

                        for(LayerElement neighbor: places){
                            // only show, if no connection exists, yet
                            if(place.getPaths((Place) neighbor).isEmpty()){
                                String dir1 = "", dir2 = "";

                                if(neighbor.getY() > place.getY())
                                    {dir1 = "n"; dir2 = "s";}
                                else if(neighbor.getY() < place.getY())
                                    {dir1 = "s"; dir2 = "n";}
                                if(neighbor.getX() > place.getX())
                                    {dir1 = dir1 + "e"; dir2 = dir2 + "w";}
                                else if(neighbor.getX() < place.getX())
                                    {dir1 = dir1 + "w"; dir2 = dir2 + "e";}

                                // if exits aren't occupied yet -> add menu item
                                if(place.getPathTo(dir1) == null && ((Place) neighbor).getPathTo(dir2) == null){
                                    JMenuItem mi_path_connect = new JMenuItem("[" + dir1 + "] " + ((Place) neighbor).getName());
                                    mPathConnect.add(mi_path_connect);
                                    mi_path_connect.addActionListener(new ConnectPathActionListener(place, ((Place) neighbor), dir1, dir2));

                                    // add accelerator
                                    int dirnum = Path.getDirNum(dir1);
                                    if(dirnum > 0 & dirnum <= 9)
                                        mi_path_connect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + dirnum, Event.CTRL_MASK));
                                }
                            }
                        }
                    }
                }

                // getPlace all connected places
                HashSet<Path> paths = place.getPaths();

                if(!paths.isEmpty()){
                    JMenu mPathRemove = null;
                    if(!parent.passive){
                        mPathRemove = new JMenu("Remove");
                        mPaths.add(mPathRemove);
                        mPathRemove.setToolTipText("Remove a path");

                        mPaths.add(new JSeparator());
                    }

                    for(Path path: paths){
                        Place otherPlace = path.getOtherPlace(place);
                        JMenuItem miPathGoto = new JMenuItem("Go to [" + path.getExit(place) + "] " + otherPlace.getName());
                        mPaths.add(miPathGoto);
                        miPathGoto.addActionListener(new GotoPlaceActionListener(parent, otherPlace));

                        if(!parent.passive){
                            String dir = path.getExit(place);
                            JMenuItem miPathRemove = new JMenuItem("Remove [" + dir + "] " + otherPlace.getName());
                            miPathRemove.addActionListener(new RemovePathActionListener(path));
                            mPathRemove.add(miPathRemove);

                            // add accelerator
                            int dirnum = Path.getDirNum(dir);
                            if(dirnum > 0 & dirnum <= 9)
                                miPathRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + dirnum, Event.ALT_MASK));
                        }
                    }

                    if(!parent.passive){
                        JMenuItem miShortestPath = new JMenuItem("Find shortest path");
                        miShortestPath.setActionCommand("find_path");
                        miShortestPath.addActionListener(this);
                        mPaths.add(new JSeparator());
                        mPaths.add(miShortestPath);
                    }
                }

                // ------------- layers / maps ------------------
                JMenu mChildren = new JMenu("Maps");
                mChildren.setToolTipText("Related places, eg. for maps within maps");
                if(!parent.passive || !place.getChildren().isEmpty())
                    add(mChildren);

                if(!parent.passive){
                    JMenuItem miChildConnect = new JMenuItem("Connect with existing place");
                    miChildConnect.setToolTipText("Connect another place with \"" + place.getName() + "\"");
                    miChildConnect.setActionCommand("connect_child");
                    miChildConnect.addActionListener(this);
                    mChildren.add(miChildConnect);

                    JMenuItem miChildNewLayer = new JMenuItem("New place on new map");
                    miChildNewLayer.setToolTipText("Creates a new place on a new map layer and connects it with \"" + place.getName() + "\"");
                    miChildNewLayer.setActionCommand("create_child_new_layer");
                    miChildNewLayer.addActionListener(this);
                    mChildren.add(miChildNewLayer);
                }

                HashSet<Place> children = place.getChildren();
                if(!children.isEmpty()){
                    if(!parent.passive){
                        JMenu m_sa_remove = new JMenu("Remove");
                        mChildren.add(m_sa_remove);

                        for(Place child: children){
                            JMenuItem mi_sa_remove = new JMenuItem("Remove " + child.getName() + " (" + child.getLayer().getName() + ")");
                            m_sa_remove.add(mi_sa_remove);
                            mi_sa_remove.addActionListener(new RemoveChildrenActionListener(place, child));
                        }
                    }

                    mChildren.add(new JSeparator());

                    for(Place child: children){
                        JMenuItem mi_sa_goto = new JMenuItem("Go to " + child.getName() + " (" + child.getLayer().getName() + ")");
                        mChildren.add(mi_sa_goto);
                        mi_sa_goto.addActionListener(new GotoPlaceActionListener(parent, child));
                    }
                }

                HashSet<Place> parents = place.getParents();
                if(!parents.isEmpty()){
                    mChildren.add(new JSeparator());

                    for(Place child: parents){
                        JMenuItem mi_sa_goto = new JMenuItem("Go to " + child.getName() + " (" + child.getLayer().getName() + ")");
                        mChildren.add(mi_sa_goto);
                        mi_sa_goto.addActionListener(new GotoPlaceActionListener(parent, child));
                    }
                }

            }  else { // if layer doesn't exist or no place exists at position x,y
                JMenuItem miNewPlace = new JMenuItem("New place");
                miNewPlace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
                miNewPlace.addActionListener(new PlaceDialog(rootFrame, parent.getWorld(), layer, posX, posY));
                add(miNewPlace);
                JMenuItem miNewPlaceholder = new JMenuItem("New placeholder");
                add(miNewPlaceholder);
                miNewPlaceholder.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));
                miNewPlaceholder.setActionCommand("create_placeholder");
                miNewPlaceholder.addActionListener(this);
            }

            // cut / copy / paste for selected places
            final boolean can_paste = layer != null && mudmap2.CopyPaste.canPaste(posX, posY, layer);
            final boolean has_paste_places = layer != null && mudmap2.CopyPaste.hasCopyPlaces();
            final boolean has_selection = parent.placeGroupHasSelection();

            if((layer != null && place != null) || has_selection || has_paste_places)
                add(new JSeparator());

            if((layer != null && place != null) || has_selection){
                JMenuItem miCutPlace = new JMenuItem("Cut" + (has_selection ? " selection" : " place"));
                miCutPlace.setActionCommand("cut");
                miCutPlace.addActionListener(this);
                miCutPlace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
                add(miCutPlace);

                JMenuItem miCopyPlace = new JMenuItem("Copy" + (has_selection ? " selection" : " place"));
                miCopyPlace.setActionCommand("copy");
                miCopyPlace.addActionListener(this);
                miCopyPlace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
                add(miCopyPlace);
            }

            if(has_paste_places){
                JMenuItem miPastePlace = new JMenuItem("Paste");
                miPastePlace.setActionCommand("paste");
                miPastePlace.addActionListener(this);
                if(!can_paste) miPastePlace.setEnabled(false);
                miPastePlace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
                add(miPastePlace);
            }

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame rootFrame = (JFrame) SwingUtilities.getRoot((Component) e.getSource());

            switch(e.getActionCommand()){
                case "create_placeholder":
                    getWorld().putPlaceholder(getPosition().getLayer(), posX, posY);
                    repaint();
                    break;
                case "create_child_new_layer":
                    // create new place
                    PlaceDialog dlg = new PlaceDialog(rootFrame, getWorld(), null, 0, 0);
                    dlg.setVisible(true);

                    Place place_new = dlg.getPlace();
                    if(place_new != null){
                        // connect new place with place as a child
                        place.connectChild(place_new);
                        // go to new place
                        pushPosition(place_new.getCoordinate());
                    }
                    break;
                case "connect_child":
                    {
                        PlaceSelectionDialog psdlg = new PlaceSelectionDialog(rootFrame, getWorld(), getPosition(), true);
                        psdlg.setVisible(true);
                        Place child = psdlg.getSelection();
                        if(psdlg.getSelected() && child != null && child != place){
                            int ret = JOptionPane.showConfirmDialog(rootFrame, "Connect \"" + child.getName() + "\" to \"" + place.getName() + "\"?", "Connect child place", JOptionPane.YES_NO_OPTION);
                            if(ret == JOptionPane.YES_OPTION){
                                place.connectChild(child);
                                repaint();
                            }
                        }
                    }
                    break;
                case "copy":
                    if(placeGroupHasSelection()){
                        mudmap2.CopyPaste.copy(placeGroupGetSelection(), posX, posY);
                    } else {
                        HashSet<Place> set = new HashSet<>();
                        set.add(place);
                        mudmap2.CopyPaste.copy(set, posX, posY);
                    }
                    repaint();
                    break;
                case "cut":
                    if(placeGroupHasSelection()){
                        mudmap2.CopyPaste.cut(placeGroupGetSelection(), posX, posY);
                    } else {
                        HashSet<Place> set = new HashSet<>();
                        set.add(place);
                        mudmap2.CopyPaste.cut(set, posX, posY);
                    }
                    repaint();
                    break;
                case "paste":
                    mudmap2.CopyPaste.paste(posX, posY, layer);
                    repaint();
                    break;
                case "find_path":
                    {
                        PlaceSelectionDialog psdlg = new PlaceSelectionDialog(rootFrame, getWorld(), getPosition(), true);
                        psdlg.setVisible(true);
                        Place end = psdlg.getSelection();
                        if(end != null){
                            placeGroupReset();
                            Place place_it = getWorld().breadthSearch(place, end);
                            if(place_it == null){
                                callMessageListeners("No Path found");
                                JOptionPane.showMessageDialog(this, "Could not find path to " + end.getName());
                            } else {
                                int path_length = 0;
                                while(place_it != null){
                                    placeGroup.add(place_it);
                                    place_it = place_it.getBreadthSearchData().predecessor;
                                    ++path_length;
                                }
                                //repaint();
                                worldChanged(place); // workaround: why doesn't repaint work?
                                callMessageListeners("Path found, length: " + (path_length - 1));
                            }

                        }
                    }
                    break;
                default:
                    System.out.println("Invalid action command " + e.getActionCommand());
                    JOptionPane.showMessageDialog(this, "Runtime Error: Invalid action command " + e.getActionCommand());
            }
        }

        /**
         * redraws the world tab after the popup is closed
         */
        private class TabContextPopMenuListener implements PopupMenuListener {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                setContextMenu(true);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                setContextMenu(false);
                repaint();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
                setContextMenu(false);
                repaint();
            }
        }

        /**
         * Moves the map to the place, if action is performed
         */
        private class GotoPlaceActionListener implements ActionListener {
            WorldPanel parent;
            Place place;

            public GotoPlaceActionListener(WorldPanel worldpanel, Place place){
                this.parent = worldpanel;
                this.place = place;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if(place != null) parent.pushPosition(place.getCoordinate());
            }
        }

        /**
         * Removes a child from a place, if action performed
         */
        private class RemoveChildrenActionListener implements ActionListener {

            Place place, child;

            public RemoveChildrenActionListener(Place place, Place child) {
                this.place = place;
                this.child = child;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if(place != null && child != null) place.removeChild(child);
            }
        }

        /**
         * Connects a new path, if called
         */
        private class ConnectPathActionListener implements ActionListener {

            Place pl1, pl2;
            String dir1, dir2;

            public ConnectPathActionListener(Place pl1, Place pl2, String dir1, String dir2) {
                this.pl1 = pl1;
                this.pl2 = pl2;
                this.dir1 = dir1;
                this.dir2 = dir2;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                pl1.connectPath(new Path(pl1, dir1, pl2, dir2));
            }
        }

        /**
         * removes a path, if called
         */
        private class RemovePathActionListener implements ActionListener {

            Path path;

            private RemovePathActionListener(Path path) {
                this.path = path;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                path.remove();
            }
        }

    }
}
