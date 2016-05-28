/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2014  Neop (email: mneop@web.de)
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

/*  File description
 *
 *  This class displays a world and the GUI elements that belong to it. It also
 *  processes keyboard commands. The class is supposed to be a tab in Mainwindow.
 *  It reads and writes the world meta (*_meta) files
 */

package mudmap2.frontend;

import mudmap2.frontend.GUIElement.MapPainter;
import mudmap2.frontend.GUIElement.MapPainterDefault;
import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import mudmap2.Paths;
import mudmap2.backend.Layer;
import mudmap2.backend.Layer.PlaceNotFoundException;
import mudmap2.backend.LayerElement;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;
import mudmap2.backend.WorldFileReader.WorldFile;
import mudmap2.backend.WorldFileReader.current.WorldFileDefault;
import mudmap2.backend.WorldManager;
import mudmap2.frontend.GUIElement.ScrollLabel;
import mudmap2.frontend.dialog.AreaDialog;
import mudmap2.frontend.dialog.OpenWorldDialog;
import mudmap2.frontend.dialog.PathConnectDialog;
import mudmap2.frontend.dialog.PathConnectNeighborsDialog;
import mudmap2.frontend.dialog.PlaceCommentDialog;
import mudmap2.frontend.dialog.PlaceDialog;
import mudmap2.frontend.dialog.PlaceRemoveDialog;
import mudmap2.frontend.dialog.PlaceSelectionDialog;
import mudmap2.frontend.sidePanel.LayerPanelListener;
import mudmap2.frontend.sidePanel.PlacePanelListener;
import mudmap2.frontend.sidePanel.SidePanel;

/**
 * A tab in the main window that displays a world
 *
 * @author neop
 */
public class WorldTab extends JPanel implements LayerPanelListener,PlacePanelListener {
    private static final long serialVersionUID = 1L;

    World world;
    String filename;

    // GUI elements
    JFrame parent;
    WorldPanel worldpanel;
    SidePanel sidePanel;
    JSlider sliderZoom;
    JPanel panelSouth;
    ScrollLabel labelInfobar;

    // history of shown position
    LinkedList<WorldCoordinate> positions;
    int positionsCurIndex; // index of currently shown position
    // max amount of elements in the list
    static final int HISTORY_MAX_LENGTH = 25;

    // true, if the mouse is in the panel, for relative motion calculation
    boolean mouseInPanel;
    // previous position of the mouse
    int mouseXPrevious, mouseYPrevious;

    // the position of the selected place (selected by mouse or keyboard)
    static boolean placeSelectionEnabledDefault = true; // default value
    boolean cursorEnabled;
    int cursorX, cursorY;
    boolean forceSelection;

    // world_meta file version supported by this WorldTab
    static final int META_FILE_VER_MAJOR = 1;
    static final int META_FILE_VER_MINOR = 1;

    // tile size in pixel
    double tileSize;
    public static final int TILE_SIZE_MIN = 10;
    public static final int TILE_SIZE_MAX = 200;

    // true, if a context menu is shown (to disable forced focus)
    boolean isContextMenuShown;
    boolean forcedFocusDisabled;

    // passive worldtabs don't modify the world
    final boolean passive;

    LinkedList<CursorListener> cursorListeners;

    // place (group) selection
    WorldCoordinate placeGroupBoxStart, placeGroupBoxEnd;
    HashSet<Place> placeGroup;

    // ============================= Methods ===================================

    /**
     * Constructs the world tab, opens the world if necessary
     * @param parent parent frame
     * @param world world
     * @param file
     * @param passive world won't be changed, if true
     */
    public WorldTab(JFrame parent, World world, String file, boolean passive){
        this.parent = parent;
        this.world = world;
        this.filename = file;
        this.passive = passive;
        create();
    }

    /**
     * Constructs the world tab, opens the world if necessary
     * @param parent parent frame
     * @param world world
     * @param passive world won't be changed, if true
     */
    public WorldTab(JFrame parent, World world, boolean passive){
        this.parent = parent;
        this.world = world;
        this.passive = passive;
        create();
    }

    /**
     * Copies a WorldTab and creates a new passive one
     * @param wt
     */
    public WorldTab(WorldTab wt){
        parent = wt.get_parent();
        passive = true;
        world = wt.getWorld();
        createVariables();

        tileSize = wt.tileSize;
        cursorEnabled = wt.cursorEnabled;
        // copy positions
        for(WorldCoordinate pos: wt.positions) positions.add(pos);

        createGui();
    }

    /**
     * Clones the WorldTab
     * @return
     */
    @Override
    public Object clone(){
        return new WorldTab(this);
    }

    /**
     * Creates the WorldTab from scratch
     */
    private void create(){
        createVariables();
        loadMeta();
        createGui();
    }

    /**
     * Sets the initial values of the member variables
     */
    private void createVariables(){
        positions = new LinkedList<>();
        tileSize = 120;

        isContextMenuShown = false;
        forcedFocusDisabled = true;

        mouseInPanel = false;
        mouseXPrevious = mouseYPrevious = 0;

        forceSelection = false;
        cursorEnabled = placeSelectionEnabledDefault;

        placeGroup = new HashSet<>();
    }

    /**
     * Creates the GUI elements
     */
    private void createGui(){
        setLayout(new BorderLayout());

        worldpanel = new WorldPanel(this, passive);
        add(worldpanel, BorderLayout.CENTER);

        sidePanel = new SidePanel(world);
        add(sidePanel, BorderLayout.EAST);
        sidePanel.addLayerPanelListener(this);
        sidePanel.addPlacePanelListener(this);

        add(panelSouth = new JPanel(), BorderLayout.SOUTH);
        panelSouth.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(1, 2, 0, 2);

        // add bottom panel elements
        // previous / next buttons for the history
        JButton button_prev = new JButton("Prev");
        constraints.gridx++;
        panelSouth.add(button_prev, constraints);
        button_prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                popPosition();
            }
        });

        JButton button_next = new JButton("Next");
        constraints.gridx++;
        panelSouth.add(button_next, constraints);
        button_next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                restorePosition();
            }
        });

        constraints.gridx++;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        panelSouth.add(labelInfobar = new ScrollLabel(), constraints);
        labelInfobar.startThread();

        // set default selected place to hte center place
        cursorX = (int) Math.round(getCurPosition().getX());
        cursorY = (int) Math.round(getCurPosition().getY());

        sliderZoom = new JSlider(0, 100, (int) (100.0 / TILE_SIZE_MAX * tileSize));
        constraints.gridx++;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        panelSouth.add(sliderZoom, constraints);
        sliderZoom.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                setTileSize((int) ((double) TILE_SIZE_MAX * ((JSlider) arg0.getSource()).getValue() / 100.0));
            }
        });

        cursorListeners = new LinkedList<>();
    }

    /**
     * Gets the parent frame
     * @return
     */
    public JFrame get_parent(){
        return parent;
    }

    /**
     * Closes the tab
     */
    public void close(){
        if(parent instanceof Mainwindow){
            int ret = JOptionPane.showConfirmDialog(this, "Save world \"" + getWorld().getName() + "\"?", "Save world", JOptionPane.YES_NO_OPTION);
            if(ret == JOptionPane.YES_OPTION) save();
            WorldManager.closeFile(filename);
            ((Mainwindow) parent).removeTab(this);
        }
    }

    /**
     * Get the world
     * @return world
     */
    public World getWorld(){
        return world;
    }

    /**
     * Get the worlds file name
     * @return
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Set the worlds file name
     * @param filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Get the panel width / width of the actually drawn map
     * @return
     */
    public int getPanelWidth(){
        return (int) worldpanel.getScreenWidth();
    }

    /**
     * Get the panel height / height of the actually drawn map
     * @return
     */
    public int getPanelHeight(){
        return (int) worldpanel.getScreenHeight();
    }

    /**
     * Set visibility of the side panel
     * @param b
     */
    public void setSidePanelVisible(Boolean b){
        sidePanel.setVisible(b);
    }

    /**
     * Returns true if curved path lines are enabled
     * @return
     */
    public boolean getPathsCurved(){
        return ((MapPainterDefault) this.worldpanel.mappainter).getPathsCurved();
    }

    /**
     * Enables or disables curved path lines
     * @param b
     */
    public void setPathsCurved(boolean b){
        ((MapPainterDefault) this.worldpanel.mappainter).setPathsCurved(b);
    }

    /**
     * Saves the changes in the world
     */
    public void save(){
        if(!passive){
            writeMeta();

            WorldFile worldFile = world.getWorldFile();

            if(worldFile == null){
                if(filename == null || filename.isEmpty() || (new File(filename)).exists()){
                    // TODO: create new filename
                    throw new UnsupportedOperationException("no filename or file exists");
                } else {
                    worldFile = new WorldFileDefault(filename);
                    world.setWorldFile(worldFile);
                }
            }

            try {
                worldFile.writeFile(world);
            } catch (IOException ex) {
                Logger.getLogger(WorldTab.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(parent,
                        "Could not save world file " + worldFile.getFilename(),
                        "Saving world file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        showMessage("World saved");
    }

    /**
     * Show message in infobar
     * @param message
     */
    public void showMessage(String message){
        labelInfobar.showMessage(message);
    }

    // ========================== Place selection ==============================
    /**
     * Get the x coordinate of the selected place
     * @return x coordinate
     */
    public int getCursorX(){
        return cursorX;
    }

    /**
     * Get the y coordinate of the selected place
     * @return y coordinate
     */
    public int getCursorY(){
        return cursorY;
    }

    /**
     * Set the coordinates of the selected place
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setCursor(int x, int y){
        cursorX = x;
        cursorY = y;
        updateInfobar();
        moveScreenToCursor();
        repaint();
        callCursorListeners();
    }

    /**
     * Moves the place selection coordinates
     * @param dx x movement
     * @param dy y movement
     */
    private void moveCursor(int dx, int dy){
        cursorX += dx;
        cursorY += dy;
        updateInfobar();
        moveScreenToCursor();
        repaint();
        callCursorListeners();
    }

    /**
     * moves the shown places so the selection is on the screen
     */
    private void moveScreenToCursor(){
        if(worldpanel != null){
            int screen_x = worldpanel.getScreenPosX(cursorX);
            int screen_y = worldpanel.getScreenPosY(cursorY);
            int tilesize = getTileSize();

            double dx = 0, dy = 0;

            if(screen_x < 0) dx = (double) screen_x / tilesize;
            else if(screen_x > worldpanel.screen_width - tilesize) dx = (screen_x - worldpanel.screen_width) / tilesize + 1;
            if(screen_y < 0) dy = (double) -screen_y / tilesize;
            else if(screen_y > worldpanel.screen_height - tilesize) dy = -(screen_y - worldpanel.screen_height) / tilesize - 1;

            if(dx != 0 || dy != 0) getCurPosition().move(dx, dy);
            repaint();
        }
    }

    public void updateCursorEnabled(){
        updateInfobar();
        repaint();
    }

    /**
     * Set the cursor state (if true, the selection will be shown)
     * @param b
     */
    public void setCursorEnabled(boolean b){
        cursorEnabled = b || forceSelection;
        updateCursorEnabled();
    }

    /**
     * Toggles the cursor enabled state
     */
    private void setCursorToggle(){
        if(!forceSelection){
            cursorEnabled = !cursorEnabled;
            updateCursorEnabled();
        }
    }

    /**
     * Get the cursor state
     * @return
     */
    public boolean getCursorEnabled(){
        return cursorEnabled || forceSelection;
    }

    /**
     * Forces the cursor to be enabled, if true
     * @param b
     */
    public void setCursorForced(boolean b){
        if(forceSelection = b) setCursorEnabled(true);
        updateCursorEnabled();
    }

    /**
     * Get the currently shown position
     * @return current position
     */
    public WorldCoordinate getCurPosition(){
        if(positions.isEmpty()) return world.getHome().clone();
        return positions.get(positionsCurIndex);
        //return positions.getFirst();
    }

    /**
     * Pushes a new position on the position stack ("goto")
     * @param _pos new position
     */
    public void pushPosition(WorldCoordinate _pos){
        WorldCoordinate pos = _pos.clone();
        // removePlace all entries after the current one
        while(positionsCurIndex > 0){
            positions.pop();
            positionsCurIndex--;
        }
        // add new position
        positions.push(pos);

        // move place selection
        setCursor((int) pos.getX(), (int) pos.getY());
        while(positions.size() > HISTORY_MAX_LENGTH) positions.removeLast();
        repaint();
    }

    /**
     * Removes the first position from the position stack,
     * go to home position if the stack is empty
     */
    public void popPosition(){
        // if end not reached
        if(positionsCurIndex < positions.size() - 1) positionsCurIndex++;
        // add home coord at list end (unlike gotoHome())
        else positions.addLast(getWorld().getHome());

        //if(positions.size() > 0) positions.removeFirst();
        //if(positions.size() == 0) gotoHome();

        setCursor((int) getCurPosition().getX(), (int) getCurPosition().getY());
        repaint();
    }

    public void restorePosition(){
        if(positionsCurIndex > 0){
            positionsCurIndex--;
            setCursor((int) getCurPosition().getX(), (int) getCurPosition().getY());
            repaint();
        }
    }

    /**
     * Updates the infobar
     */
    private void updateInfobar(){
        if(labelInfobar != null ){
            if(getCursorEnabled()){
                Layer layer = world.getLayer(getCurPosition().getLayer());
                if(layer != null && layer.exist(getCursorX(), getCursorY())){
                    Place pl;
                        pl = layer.get(getCursorX(), getCursorY());

                        boolean has_area = pl.getArea() != null;
                        boolean has_comments = !pl.getComments().isEmpty();

                        String infotext = pl.getName();
                        if(has_area || has_comments) infotext += " (";
                        if(has_area) infotext += pl.getArea().getName();
                        if(has_comments) infotext += (has_area ? ", " : "") + pl.getCommentsString(false);
                        if(has_area || has_comments) infotext += ")";

                        labelInfobar.setText(infotext);
                } else {
                    labelInfobar.setText("");
                }
            } else labelInfobar.setText("");
        }
    }

    /**
     * Removes all previously visited positions from history and sets pos
     * @param pos new position
     */
    public void resetHistory(WorldCoordinate pos){
        positions.clear();
        positions.add(pos);
        cursorX = (int) Math.round(pos.getX());
        cursorY = (int) Math.round(pos.getY());
        updateInfobar();
        repaint();
    }

    /**
     * Go to the home position
     */
    public void gotoHome(){
        pushPosition(world.getHome());
        setCursor((int) Math.round(getCurPosition().getX()), (int) Math.round(getCurPosition().getY()));
    }

    /**
     * Set a new home position
     */
    public void setHome(){
        world.setHome(getCurPosition().clone());
    }

    /**
     * Get a place on the current layer
     * @param x x coordinate
     * @param y y coordinate
     * @return place or null
     */
    public Place getPlace(int x, int y){
        Place ret = null;
        Layer layer = world.getLayer(getCurPosition().getLayer());
        if(layer != null) ret = layer.get(x, y);
        return ret;
    }

    /**
     * Get the selected place or null
     * @return place or null
     */
    public Place getSelectedPlace(){
        return getPlace(getCursorX(), getCursorY());
    }

    /**
     * Get the current tile size
     * @return tile size
     */
    public int getTileSize(){
        return (int) tileSize;
    }

    /**
     * set the tile size
     * @param ts new tile size
     */
    public void setTileSize(double ts){
        tileSize = Math.min(Math.max(ts, TILE_SIZE_MIN), TILE_SIZE_MAX);
        sliderZoom.setValue((int) (100.0 / TILE_SIZE_MAX * tileSize));
        repaint();
    }

    /**
     * increases the tile size
     */
    public void tileSizeIncrement(){
        double ts = tileSize;
        ts = Math.exp(Math.log(ts / 10) + 0.03) * 10;
        ts = Math.min(ts, TILE_SIZE_MAX);
        tileSize = Math.min(Math.max(ts, tileSize + 1), TILE_SIZE_MAX);

        //if(tileSize < TILE_SIZE_MAX) tileSize++;
        sliderZoom.setValue((int) (100.0 / TILE_SIZE_MAX * tileSize));
        repaint();
    }

    /**
     * decreases the tile size
     */
    public void tileSizeDecrement(){
        double ts = tileSize;
        ts = Math.exp(Math.log(ts / 10) - 0.02) * 10;
        ts = Math.max(ts, TILE_SIZE_MIN);
        tileSize = Math.max(Math.min(ts, tileSize - 1), TILE_SIZE_MIN);

        //if(tileSize > TILE_SIZE_MIN) tileSize--;

        sliderZoom.setValue((int) (100.0 / TILE_SIZE_MAX * tileSize));
        repaint();
    }


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

    /**
     * Manually disables the forced focus
     * @param b
     */
    public void setForcedFocusDisabled(boolean b){
        forcedFocusDisabled = b;
    }

    /**
     * Returns true, if forced focus is disabled manually
     * @return
     */
    public boolean getForcedFocusDisabled(){
        return forcedFocusDisabled;
    }

    /**
     * Returs true, if forced focus can be enabled
     * @return
     */
    private boolean getForcedFocus(){
        return !forcedFocusDisabled && !isContextMenuShown;
    }

    // ========================= selection listener ============================
    /**
     * Adds a place selection listener
     * @param listener
     */
    public void addCursorListener(CursorListener listener){
        if(!cursorListeners.contains(listener))
            cursorListeners.add(listener);
    }

    /**
     * Removes a place selection listener
     * @param listener
     */
    public void removeCursorListener(CursorListener listener){
        cursorListeners.remove(listener);
    }

    /**
     * calls all place selection listeners
     */
    private void callCursorListeners(){
        Place place = getPlace(getCursorX(), getCursorY());

        if(cursorListeners != null) {
            if(place != null)
                for(CursorListener listener: cursorListeners)
                    listener.placeSelected(place);
            else {
                Layer layer = getWorld().getLayer(getCurPosition().getLayer());
                for(CursorListener listener: cursorListeners)
                    listener.placeDeselected(layer, getCursorX(), getCursorY());
            }
        }
    }

    @Override
    public void layerSelected(Layer layer, MouseEvent event) {
        switch(event.getButton()){
            case MouseEvent.BUTTON1: // left click -> go to layer
                pushPosition(new WorldCoordinate(layer.getId(), layer.getCenterX(), layer.getCenterY()));
                break;
            case MouseEvent.BUTTON3: // right click -> set name
                if(!passive){
                    String name;
                    if(layer.hasName()){
                        name = JOptionPane.showInputDialog(this, "Map name", layer.getName());
                        if(name != null){
                            if(name.isEmpty()){
                                layer.setName(null);
                                System.out.println("set null");
                            } else {
                                layer.setName(name);
                            }
                            sidePanel.update();
                        } else System.out.println("null");
                    } else {
                        name = JOptionPane.showInputDialog(this, "Map name");
                        if(name != null && !name.isEmpty()){
                            layer.setName(name);
                            sidePanel.update();
                        }
                    }
                }
                break;
        }
        repaint();
    }

    @Override
    public void createLayer() {
        pushPosition(new WorldCoordinate(world.getNewLayer().getId(), 0, 0));
        repaint();
    }

    @Override
    public void placeSelected(Place place) {
        pushPosition(place.getCoordinate());
        repaint();
    }

    public interface CursorListener{
        // gets called, when the cursor moves to another place
        public void placeSelected(Place p);
        // gets called, when the cursor changes to null
        public void placeDeselected(Layer layer, int x, int y);
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
        placeGroupBoxEnd = new WorldCoordinate(getCurPosition().getLayer(), x, y);
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

            Layer layer = world.getLayer(placeGroupBoxEnd.getLayer());

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

    /**
     * Loads the world meta data file
     * this file describes the coordinates of the last shown positions
     *
     * important: call this after creation of worldpanel!
     */
    private void loadMeta(){
        if(getFilename() != null){
            String file = getFilename() + "_meta";
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String line;
                int layer_id = world.getHome().getLayer();
                double pos_x = world.getHome().getX();
                double pos_y = world.getHome().getY();

                try {
                    while((line = reader.readLine()) != null){
                        line = line.trim();

                        if(!line.isEmpty() && !line.startsWith("//") && !line.startsWith("#")){
                            if(line.startsWith("lp")){ // last position
                                String[] tmp = line.split(" ");
                                layer_id = Integer.parseInt(tmp[1]);
                                // the x coordinate has to be negated for backward compatibility to mudmap 1.x
                                pos_x = -Double.parseDouble(tmp[2]);
                                pos_y = Double.parseDouble(tmp[3]);


                            } else if(line.startsWith("pcv")){ // previously shown places
                                String[] tmp = line.split(" ");
                                int tmp_layer_id = Integer.parseInt(tmp[1]);

                                // the x coordinate has to be negated for backward compatibility to mudmap 1.x
                                double tmp_pos_x = -Double.parseDouble(tmp[2]);
                                double tmp_pos_y = Double.parseDouble(tmp[3]);

                                WorldCoordinate newcoord = new WorldCoordinate(tmp_layer_id, tmp_pos_x, tmp_pos_y);
                                if(positions.isEmpty() || !getCurPosition().equals(newcoord)) pushPosition(newcoord);
                            } else if(line.startsWith("tile_size")){
                                String[] tmp = line.split(" ");
                                tileSize = Double.parseDouble(tmp[1]);
                            } else if(line.startsWith("enable_place_selection")){
                                String[] tmp = line.split(" ");
                                cursorEnabled = Boolean.parseBoolean(tmp[1]) || forceSelection;
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(WorldManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                pushPosition(new WorldCoordinate(layer_id, pos_x, pos_y));

            } catch (FileNotFoundException ex) {
                System.out.println("Couldn't open world meta file \"" + file + "\", file not found");
                //Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, null, ex);

                pushPosition(world.getHome());
            }
        }
    }

    /**
     * Saves the world meta file
     */
    public void writeMeta(){
        if(!passive){
            try {
                // open file
                if(!Paths.isDirectory(Paths.getWorldsDir())) Paths.createDirectory(Paths.getWorldsDir());
                File file = new File(getFilename() + "_meta");
                file.getParentFile().mkdirs();
                try (PrintWriter outstream = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
                    outstream.println("# MUD Map (v2) world meta data file");
                    outstream.println("ver " + META_FILE_VER_MAJOR + "." + META_FILE_VER_MINOR);

                    // tile size
                    outstream.println("tile_size " + (int) tileSize);

                    // write whether the place selection is shown
                    outstream.println("enable_place_selection " + getCursorEnabled());

                    // write current position and position history
                    outstream.println("lp " + getCurPosition().getMetaString());

                    // shown place history
                    for(Iterator<WorldCoordinate> wcit = positions.descendingIterator(); wcit.hasNext();){
                        WorldCoordinate next = wcit.next();
                        if(next != getCurPosition()) outstream.println("pcv " + next.getMetaString());
                    }
                }
            } catch (IOException ex) {
                System.out.printf("Couldn't write world meta file " + getFilename()+ "_meta");
                Logger.getLogger(WorldTab.class.getName()).log(Level.WARNING, null, ex);
            }
        }
    }

    private static class WorldPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        double screen_width, screen_height;

        WorldTab parent;
        MapPainter mappainter;

        // passive worldpanels don't modify the world
        final boolean passive;

        /**
         * Constructs a world panel
         * @param _parent parent world tab
         */
        public WorldPanel(WorldTab _parent, boolean passive) {
            this.parent = _parent;
            this.passive = passive;
            mappainter = new MapPainterDefault();

            setFocusable(true);
            requestFocusInWindow();
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent arg0) {
                    if(parent.getForcedFocus()) requestFocusInWindow();
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
                    double ts = parent.getTileSize();
                    ts = Math.exp(Math.log(ts / 10) + e.getWheelRotation() * 0.05) * 10;
                    if(e.getWheelRotation() > 0) ts = Math.max(ts, parent.getTileSize() + 1);
                    else if(e.getWheelRotation() < 0) ts = Math.min(ts, parent.getTileSize() - 1);
                    parent.setTileSize(ts);
                    //parent.setTileSize(parent.getTileSize() + e.getWheelRotation());
                }
            });
            addMouseMotionListener(new TabMouseMotionListener());
        }

        /**
         * Gets the screen width
         * @return screen width
         */
        public double getScreenWidth(){
            return screen_width;
        }

        /**
         * Gets the screen height
         * @return screen height
         */
        public double getScreenHeight(){
            return screen_height;
        }

        /**
         * Remove integer part, the part after the point remains
         * @param val
         * @return
         */
        private double remint(double val){
            return val - Math.round(val);
        }

        /**
         * Converts screen coordinates to world coordinates
         * @param screen_x a screen coordinate (x-axis)
         * @return world coordinate x
         */
        private int getPlacePosX(int screen_x){
            return (int) Math.ceil((screen_x - screen_width / 2) / parent.getTileSize() + parent.getCurPosition().getX()) - 1;
        }

        /**
         * Converts screen coordinates to world coordinates
         * @param mouse_y a screen coordinate (y-axis)
         * @return world coordinate y
         */
        private int getPlacePosY(int screen_y){
            return (int) -Math.ceil((screen_y - screen_height / 2) / parent.getTileSize() - parent.getCurPosition().getY()) + 1;
        }

        /**
         * Converts world coordinates to screen coordinates
         * @param place_x a world (place) coordinate (x axis)
         * @return a screen coordinate x
         */
        private int getScreenPosX(int place_x){
            int tileSize = parent.getTileSize();
            double screen_center_x = (screen_width / tileSize) / 2; // note: wdtwd2
            int place_x_offset = (int) (Math.round(parent.getCurPosition().getX()) - Math.round(screen_center_x));
            return (int)((place_x - place_x_offset + remint(screen_center_x) - remint(parent.getCurPosition().getX())) * tileSize);
        }

        /**
         * Converts world coordinates to screen coordinates
         * @param place_y a world (place) coordinate (y axis)
         * @return a screen coordinate y
         */
        private int getScreenPosY(int place_y){
            int tileSize = parent.getTileSize();
            double screen_center_y = (screen_height / tileSize) / 2;
            int place_y_offset = (int) (Math.round(parent.getCurPosition().getY()) - Math.round(screen_center_y));
            return (int)((-place_y + place_y_offset - remint(screen_center_y) + remint(parent.getCurPosition().getY())) * tileSize + screen_height);
        }

        // ======================= DRAW WORLD HERE =============================

        @Override
        public void paintComponent(Graphics g){
            mappainter.setSelectedPlaces(parent.placeGroup, parent.placeGroupBoxStart, parent.placeGroupBoxEnd);
            mappainter.selectPlaceAt(parent.getCursorX(), parent.getCursorY());
            mappainter.setSelectionVisible(parent.getCursorEnabled());

            mappainter.paint(g, parent.getTileSize(), screen_width = getWidth(), screen_height = getHeight(), parent.getWorld().getLayer(parent.getCurPosition().getLayer()), parent.getCurPosition());
        }

        // ========================= Listeners and context menu ================

        /**
         * This listener only contains actions, that don't modify the world
         */
        private class TabMousePassiveListener extends TabMouseListener implements MouseListener {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if(arg0.getButton() == MouseEvent.BUTTON3){ // right click
                    // show context menu
                    TabContextMenu context_menu = new TabContextMenu(parent, getPlacePosX(arg0.getX()), getPlacePosY(arg0.getY()));
                    context_menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
                } else if(arg0.getButton() == MouseEvent.BUTTON1){ // left click
                    if(!arg0.isShiftDown()){ // left click + hift gets handled in active listener
                        // set place selection to coordinates if keyboard selection is enabled
                        parent.setCursor(getPlacePosX(arg0.getX()), getPlacePosY(arg0.getY()));
                    }
                }
            }
        }

        /**
         * This listener contains actions that modify the world
         */
        private class TabMouseListener implements MouseListener {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if(arg0.getButton() == MouseEvent.BUTTON1){ // left click
                    Place place = parent.getPlace(getPlacePosX(arg0.getX()), getPlacePosY(arg0.getY()));
                    if(arg0.isControlDown()){ // left click + ctrl
                        if(place != null) parent.placeGroupAdd(place);
                    } else if(!arg0.isShiftDown()) { // left click and not shift
                        parent.placeGroupReset();
                        if(arg0.getClickCount() > 1){ // double click
                            if(place != null) (new PlaceDialog(parent.parent, parent.getWorld(), place)).setVisible(true);
                            else (new PlaceDialog(parent.parent, parent.world, parent.getWorld().getLayer(parent.getCurPosition().getLayer()), getPlacePosX(arg0.getX()), getPlacePosY(arg0.getY()))).setVisible(true);
                        }
                    } else {
                        if(!parent.placeGroupHasSelection())
                            parent.placeGroupBoxModifySelection(parent.getCursorX(), parent.getCursorY());
                        parent.placeGroupBoxModifySelection(getPlacePosX(arg0.getX()), getPlacePosY(arg0.getY()));
                        // cursor has to be set after the selection -> not handled by passive listener
                        parent.setCursor(getPlacePosX(arg0.getX()), getPlacePosY(arg0.getY()));
                    }
                }
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {}

            @Override
            public void mouseEntered(MouseEvent arg0) {
                parent.mouseInPanel = true;
                parent.mouseXPrevious = arg0.getX();
                parent.mouseYPrevious = arg0.getY();
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                parent.mouseInPanel = false;
            }
        }

        private class TabMouseMotionListener implements MouseMotionListener {

            @Override
            public void mouseDragged(MouseEvent arg0) {
                if(parent.mouseInPanel){
                    double dx = (double) (arg0.getX() - parent.mouseXPrevious) / parent.getTileSize();
                    double dy = (double) (arg0.getY() - parent.mouseYPrevious) / parent.getTileSize();
                    if(!arg0.isShiftDown()) // shift not pressed: move view
                        parent.getCurPosition().move(-dx , dy);
                    else { // shift pressed: box selection
                        parent.placeGroupBoxModifySelection(getPlacePosX(arg0.getX()), getPlacePosY(arg0.getY()));
                    }
                    parent.repaint();
                }
                parent.mouseXPrevious = arg0.getX();
                parent.mouseYPrevious = arg0.getY();
            }

            @Override
            public void mouseMoved(MouseEvent arg0) {
                parent.mouseXPrevious = arg0.getX();
                parent.mouseYPrevious = arg0.getY();
            }
        }

        /**
         * This listener only contains actions, that don't modify the world
         */
        private class TabKeyPassiveListener extends TabKeyListener {
            public TabKeyPassiveListener(WorldPanel parent){
                super(parent);
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
                if(!arg0.isShiftDown() && !arg0.isControlDown() && !arg0.isAltDown() && !arg0.isAltGraphDown()){ // ctrl, shift and alt not pressed
                    int x_bef = parent.getCursorX();
                    int y_bef = parent.getCursorY();

                    switch(arg0.getKeyCode()){
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
                            parent.setCursorToggle();
                            break;

                        // shift place selection - wasd
                        case KeyEvent.VK_NUMPAD8:
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W:
                            if(parent.getCursorEnabled()) parent.moveCursor(0, +1);
                            break;
                        case KeyEvent.VK_NUMPAD4:
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A:
                            if(parent.getCursorEnabled()) parent.moveCursor(-1, 0);
                            break;
                        case KeyEvent.VK_NUMPAD2:
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S:
                            if(parent.getCursorEnabled()) parent.moveCursor(0, -1);
                            break;
                        case KeyEvent.VK_NUMPAD6:
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D:
                            if(parent.getCursorEnabled()) parent.moveCursor(+1, 0);
                            break;

                        // diagonal movement
                        case KeyEvent.VK_NUMPAD1:
                            if(parent.getCursorEnabled()) parent.moveCursor(-1, -1);
                            break;
                        case KeyEvent.VK_NUMPAD3:
                            if(parent.getCursorEnabled()) parent.moveCursor(+1, -1);
                            break;
                        case KeyEvent.VK_NUMPAD7:
                            if(parent.getCursorEnabled()) parent.moveCursor(-1, +1);
                            break;
                        case KeyEvent.VK_NUMPAD9:
                            if(parent.getCursorEnabled()) parent.moveCursor(+1, +1);
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

                    int x_sel = parent.getCursorX();
                    int y_sel = parent.getCursorY();

                    // change group selection, if place selection changed
                    if(x_sel != x_bef || y_sel != y_bef){
                        if(parent.placeGroupBoxStart != null) parent.placeGroupBoxSelectionToList();
                    }
                }
            }
        }

        /**
         * This listener contains actions, that modify the world
         */
        private class TabKeyListener implements KeyListener {

            WorldPanel worldpanel;

            public TabKeyListener(WorldPanel parent){
                worldpanel = parent;
            }

            @Override
            public void keyTyped(KeyEvent arg0) {}

            @Override
            public void keyPressed(KeyEvent arg0) {
                if(arg0.isControlDown()){ // ctrl key pressed
                    Place place, other;

                    switch(arg0.getKeyCode()){
                        case KeyEvent.VK_S: // save world
                            parent.save();
                            break;
                        case KeyEvent.VK_O: // open world
                            (new OpenWorldDialog((Mainwindow) parent.parent)).setVisible();
                            break;

                        case KeyEvent.VK_A: // select all places
                            parent.placeGroupSet(parent.getWorld().getLayer(parent.getCurPosition().getLayer()).getPlaces());
                            break;
                        case KeyEvent.VK_X: // cut selected places
                            if(!parent.placeGroupGetSelection().isEmpty()){ // cut group selection
                                mudmap2.CopyPaste.cut(parent.placeGroup, parent.getCursorX(), parent.getCursorY());
                                parent.showMessage(parent.placeGroup.size() + " places cut");
                                parent.placeGroupReset();
                            } else if(parent.getSelectedPlace() != null){ // cut cursor selection
                                HashSet<Place> tmp_selection = new HashSet<>();
                                tmp_selection.add(parent.getSelectedPlace());
                                mudmap2.CopyPaste.cut(tmp_selection, parent.getCursorX(), parent.getCursorY());
                                parent.showMessage("1 place cut");
                            } else parent.showMessage("No places cut: selection empty");
                            break;
                        case KeyEvent.VK_C: // copy selected places
                            if(!parent.placeGroupGetSelection().isEmpty()){ // copy group selection
                                mudmap2.CopyPaste.copy(parent.placeGroup, parent.getCursorX(), parent.getCursorY());
                                parent.showMessage(parent.placeGroup.size() + " places copied");
                                parent.placeGroupReset();
                            } else if(parent.getSelectedPlace() != null){ // copy cursor selection
                                HashSet<Place> tmp_selection = new HashSet<>();
                                tmp_selection.add(parent.getSelectedPlace());
                                mudmap2.CopyPaste.copy(tmp_selection, parent.getCursorX(), parent.getCursorY());
                                parent.showMessage("1 place copied");
                            } else {
                                mudmap2.CopyPaste.resetCopy();
                                parent.showMessage("No places copied: selection empty");
                            }
                            break;
                        case KeyEvent.VK_V: // paste copied / cut places
                            if(mudmap2.CopyPaste.hasCopyPlaces()){
                                if(mudmap2.CopyPaste.canPaste(parent.getCursorX(), parent.getCursorY(), parent.getWorld().getLayer(parent.getCurPosition().getLayer()))){
                                    int paste_num = mudmap2.CopyPaste.getCopyPlaces().size();
                                    if(mudmap2.CopyPaste.paste(parent.getCursorX(), parent.getCursorY(), parent.getWorld().getLayer(parent.getCurPosition().getLayer()))){
                                        parent.showMessage(paste_num + " places pasted");
                                    } else {
                                        parent.showMessage("No places pasted");
                                    }
                                } else {
                                    parent.showMessage("Can't paste: not enough free space on map");
                                }
                            } else {
                                mudmap2.CopyPaste.resetCopy();
                                parent.showMessage("Can't paste: no places cut or copied");
                            }
                            break;

                        case KeyEvent.VK_NUMPAD8:
                        case KeyEvent.VK_UP:
                        //case KeyEvent.VK_W: // add path to direction 'n'
                            place = parent.getSelectedPlace();
                            other = parent.getPlace(parent.getCursorX(), parent.getCursorY() + 1);
                            if(place != null && other != null){ // if places exist
                                if(place.getExit("n") == null && other.getExit("s") == null){ // if exits aren't occupied
                                    place.connectPath(new Path(place, "n", other, "s"));
                                }
                            }
                            break;
                        case KeyEvent.VK_NUMPAD9: // add path to direction 'ne'
                            place = parent.getSelectedPlace();
                            other = parent.getPlace(parent.getCursorX() + 1, parent.getCursorY() + 1);
                            if(place != null && other != null){ // if places exist
                                if(place.getExit("ne") == null && other.getExit("sw") == null){ // if exits aren't occupied
                                    place.connectPath(new Path(place, "ne", other, "sw"));
                                }
                            }
                            break;
                        case KeyEvent.VK_NUMPAD6:
                        case KeyEvent.VK_RIGHT:
                        //case KeyEvent.VK_D: // add path to direction 'e'
                            place = parent.getSelectedPlace();
                            other = parent.getPlace(parent.getCursorX() + 1, parent.getCursorY());
                            if(place != null && other != null){ // if places exist
                                if(place.getExit("e") == null && other.getExit("w") == null){ // if exits aren't occupied
                                    place.connectPath(new Path(place, "e", other, "w"));
                                }
                            }
                            break;
                        case KeyEvent.VK_NUMPAD3: // add path to direction 'se'
                            place = parent.getSelectedPlace();
                            other = parent.getPlace(parent.getCursorX() + 1, parent.getCursorY() - 1);
                            if(place != null && other != null){ // if places exist
                                if(place.getExit("se") == null && other.getExit("nw") == null){ // if exits aren't occupied
                                    place.connectPath(new Path(place, "se", other, "nw"));
                                }
                            }
                            break;
                        case KeyEvent.VK_NUMPAD2:
                        case KeyEvent.VK_DOWN:
                        //case KeyEvent.VK_S: // add path to direction 's'
                            place = parent.getSelectedPlace();
                            other = parent.getPlace(parent.getCursorX(), parent.getCursorY() - 1);
                            if(place != null && other != null){ // if places exist
                                if(place.getExit("s") == null && other.getExit("n") == null){ // if exits aren't occupied
                                    place.connectPath(new Path(place, "s", other, "n"));
                                }
                            }
                            break;
                        case KeyEvent.VK_NUMPAD1: // add path to direction 'sw'
                            place = parent.getSelectedPlace();
                            other = parent.getPlace(parent.getCursorX() - 1, parent.getCursorY() - 1);
                            if(place != null && other != null){ // if places exist
                                if(place.getExit("sw") == null && other.getExit("ne") == null){ // if exits aren't occupied
                                    place.connectPath(new Path(place, "sw", other, "ne"));
                                }
                            }
                            break;
                        case KeyEvent.VK_NUMPAD4:
                        case KeyEvent.VK_LEFT:
                        //case KeyEvent.VK_A: // add path to direction 'w'
                            place = parent.getSelectedPlace();
                            other = parent.getPlace(parent.getCursorX() - 1, parent.getCursorY());
                            if(place != null && other != null){ // if places exist
                                if(place.getExit("w") == null && other.getExit("e") == null){ // if exits aren't occupied
                                    place.connectPath(new Path(place, "w", other, "e"));
                                }
                            }
                            break;
                        case KeyEvent.VK_NUMPAD7: // add path to direction 'nw'
                            place = parent.getSelectedPlace();
                            other = parent.getPlace(parent.getCursorX() - 1, parent.getCursorY() + 1);
                            if(place != null && other != null){ // if places exist
                                if(place.getExit("nw") == null && other.getExit("se") == null){ // if exits aren't occupied
                                    place.connectPath(new Path(place, "nw", other, "se"));
                                }
                            }
                            break;
                        case KeyEvent.VK_NUMPAD5: // open add path dialog
                            (new PathConnectDialog(parent, parent.getSelectedPlace())).setVisible(true);
                            break;
                    }
                } else if(arg0.isShiftDown()){ // shift key pressed -> modify selection
                    int x_bef = parent.getCursorX();
                    int y_bef = parent.getCursorY();

                    switch(arg0.getKeyCode()){
                        case KeyEvent.VK_NUMPAD8:
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W:
                            if(parent.getCursorEnabled()) parent.moveCursor(0, +1);
                            break;
                        case KeyEvent.VK_NUMPAD4:
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A:
                            if(parent.getCursorEnabled()) parent.moveCursor(-1, 0);
                            break;
                        case KeyEvent.VK_NUMPAD2:
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S:
                            if(parent.getCursorEnabled()) parent.moveCursor(0, -1);
                            break;
                        case KeyEvent.VK_NUMPAD6:
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D:
                            if(parent.getCursorEnabled()) parent.moveCursor(+1, 0);
                            break;

                        // diagonal movement
                        case KeyEvent.VK_NUMPAD1:
                            if(parent.getCursorEnabled()) parent.moveCursor(-1, -1);
                            break;
                        case KeyEvent.VK_NUMPAD3:
                            if(parent.getCursorEnabled()) parent.moveCursor(+1, -1);
                            break;
                        case KeyEvent.VK_NUMPAD7:
                            if(parent.getCursorEnabled()) parent.moveCursor(-1, +1);
                            break;
                        case KeyEvent.VK_NUMPAD9:
                            if(parent.getCursorEnabled()) parent.moveCursor(+1, +1);
                            break;

                        case KeyEvent.VK_SPACE: // add or removePlace single place to place group selection
                            Place place = parent.getSelectedPlace();
                            if(place != null) parent.placeGroupAdd(place);
                            break;
                    }
                    int x_sel = parent.getCursorX();
                    int y_sel = parent.getCursorY();

                    // change group selection, if place selection changed
                    if(x_sel != x_bef || y_sel != y_bef){
                        if(parent.placeGroupBoxStart == null) parent.placeGroupBoxModifySelection(x_bef, y_bef);
                        parent.placeGroupBoxModifySelection(x_sel, y_sel);
                    }
                } else if(arg0.isAltDown() || arg0.isAltGraphDown()){ // alt or altgr key pressed
                    Place place = parent.getSelectedPlace();
                    Place other;
                    Path path;

                    if(place != null){
                        switch(arg0.getKeyCode()){
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
                    switch(arg0.getKeyCode()){
                        // show context menu
                        case KeyEvent.VK_CONTEXT_MENU:
                            if(parent.getCursorEnabled()){
                                TabContextMenu context_menu = new TabContextMenu(parent, parent.getCursorX(), parent.getCursorY());
                                context_menu.show(arg0.getComponent(), getScreenPosX(parent.getCursorX()) + worldpanel.parent.getTileSize() / 2, getScreenPosY(parent.getCursorY()) + worldpanel.parent.getTileSize() / 2);
                            }
                            break;

                        // edit / add place
                        case KeyEvent.VK_INSERT:
                        case KeyEvent.VK_ENTER:
                        case KeyEvent.VK_E:
                            if(parent.getCursorEnabled()){
                                Place place = parent.getSelectedPlace();
                                PlaceDialog dlg;

                                Layer layer = null;
                                if(parent.getCurPosition() != null) layer = parent.getWorld().getLayer(parent.getCurPosition().getLayer());

                                if(place != null) dlg = new PlaceDialog(parent.parent, parent.world, place);
                                else dlg = new PlaceDialog(parent.parent, parent.world, parent.world.getLayer(parent.getCurPosition().getLayer()), parent.getCursorX(), parent.getCursorY());
                                dlg.setVisible(true);

                                if(layer == null) parent.pushPosition(dlg.getPlace().getCoordinate());
                            }
                            break;
                        // create placeholder
                        case KeyEvent.VK_F:
                            if(parent.getCursorEnabled()){
                                Place place = parent.getSelectedPlace();
                                // create placeholder or removePlace one
                                if(place == null){
                                    parent.world.putPlaceholder(parent.getCurPosition().getLayer(), parent.getCursorX(), parent.getCursorY());
                                } else if(place.getName().equals(Place.PLACEHOLDER_NAME)){
                                    try {
                                        place.remove();
                                    } catch (RuntimeException ex) {
                                        Logger.getLogger(WorldTab.class.getName()).log(Level.SEVERE, null, ex);
                                        JOptionPane.showMessageDialog(parent, "Could not remove place: " + ex.getMessage());
                                    } catch (PlaceNotFoundException ex) {
                                        Logger.getLogger(WorldTab.class.getName()).log(Level.SEVERE, null, ex);
                                        JOptionPane.showMessageDialog(parent, "Could not remove place: Place not found.");
                                    }
                                }
                            }
                            parent.repaint();
                            break;
                        // removePlace place
                        case KeyEvent.VK_DELETE:
                        case KeyEvent.VK_R:
                            if(!parent.placeGroupHasSelection()){ // no places selected
                                if(parent.getCursorEnabled()){
                                    Place place = parent.getSelectedPlace();
                                    if(place != null) (new PlaceRemoveDialog(parent.parent, parent.world, place)).show();
                                }
                            } else { // places selected
                                HashSet<Place> place_group = parent.placeGroupGetSelection();
                                if(place_group != null){
                                    PlaceRemoveDialog dlg = new PlaceRemoveDialog(parent.parent, parent.world, place_group);
                                    dlg.show();
                                    // reset selection, if places were removed
                                    if(dlg.getPlacesRemoved()) parent.placeGroupReset();
                                }
                            }
                            break;
                        // edit place comments
                        case KeyEvent.VK_C:
                            if(parent.getCursorEnabled()){
                                Place place = parent.getSelectedPlace();
                                if(place != null){
                                    (new PlaceCommentDialog(parent.parent, place)).setVisible(true);
                                    parent.updateInfobar();
                                }
                            }
                            break;
                        // modify area
                        case KeyEvent.VK_Q:
                            Place place = parent.getSelectedPlace();

                            if(!parent.placeGroupHasSelection()){
                                // no place selected
                                if(place == null) (new AreaDialog(parent.parent, parent.world)).setVisible(true);
                                // place selected
                                else (new AreaDialog(parent.parent, parent.world, place)).setVisible(true);
                            } else { // place group selection
                                (new AreaDialog(parent.parent, parent.world, parent.placeGroupGetSelection(), place)).setVisible(true);
                            }
                            break;

                        case KeyEvent.VK_SPACE: // add or removePlace single place to place group selection
                            place = parent.getSelectedPlace();
                            if(place != null) parent.placeGroupAdd(place);
                            break;
                    }
                }
                parent.repaint();
            }

            @Override
            public void keyReleased(KeyEvent arg0) {}
        }

        // constructs the context menu (on right click)
        private static class TabContextMenu extends JPopupMenu implements ActionListener {

            private static final long serialVersionUID = 1L;

            final WorldTab parent;
            final Layer layer;
            final Place place;
            final Integer posX;
            final Integer posY;

            /**
             * Constructs a context menu at position (x,y)
             * @param px screen / panel coordinate x
             * @param py screen / panel coordinate y
             */
            public TabContextMenu(WorldTab parent, Integer px, Integer py) {
                addPopupMenuListener(new TabContextPopMenuListener());

                this.parent = parent;
                this.posX = px;
                this.posY = py;
                layer = parent.world.getLayer(parent.getCurPosition().getLayer());
                place = (layer != null ? layer.get(posX, posY) : null);

                if(layer != null && place != null){ // if place exists
                    if(!parent.passive){
                        JMenuItem miEdit = new JMenuItem("Edit place");
                        PlaceDialog pdlg = new PlaceDialog(parent.parent, parent.world, place);
                        miEdit.addActionListener(pdlg);
                        if(layer == null) parent.pushPosition(pdlg.getPlace().getCoordinate());

                        add(miEdit);
                        miEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));

                        HashSet<Place> placeGroup = parent.placeGroupGetSelection();

                        JMenuItem miRemove;
                        if(placeGroup.isEmpty()){
                            miRemove = new JMenuItem("Remove place");
                            miRemove.addActionListener(new PlaceRemoveDialog(parent.parent, parent.world, place));
                            miRemove.setToolTipText("Remove this place");
                        } else {
                            miRemove = new JMenuItem("*Remove places");
                            miRemove.addActionListener(new PlaceRemoveDialog(parent.parent, parent.world, placeGroup));
                            miRemove.setToolTipText("Remove all selected places");
                        }
                        add(miRemove);
                        miRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

                        JMenuItem miComments = new JMenuItem("Edit comments");
                        miComments.addActionListener(new PlaceCommentDialog(parent.parent, place));
                        miComments.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
                        add(miComments);

                        JMenuItem miArea;
                        if(placeGroup.isEmpty()){
                            miArea = new JMenuItem("Edit area");
                            miArea.addActionListener(new AreaDialog(parent.parent, parent.world, place));
                            miArea.setToolTipText("Edit the area of this place");
                        } else {
                            miArea = new JMenuItem("*Edit area");
                            miArea.addActionListener(new AreaDialog(parent.parent, parent.world, placeGroup, place));
                            miArea.setToolTipText("Sets a common area for all selected places");
                        }
                        add(miArea);
                    }

                    // ------------- Paths ------------------
                    JMenu mPaths = new JMenu("Paths / Exits");
                    if(!parent.passive || !place.getPaths().isEmpty())
                        add(mPaths);

                    if(!parent.passive){
                        JMenu mPathConnect = new JMenu("Connect");
                        mPaths.add(mPathConnect);
                        mPathConnect.setToolTipText("Connect a path from this place to another one");

                        JMenuItem miPathConnectSelect = new JMenuItem("Select");
                        mPathConnect.add(miPathConnectSelect);
                        miPathConnectSelect.setToolTipText("Select any place from the map");
                        miPathConnectSelect.addActionListener(new PathConnectDialog(parent, place));
                        miPathConnectSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, Event.CTRL_MASK));

                        JMenuItem miPathConnectNeighbors = new JMenuItem("Neighbors");
                        mPathConnect.add(miPathConnectNeighbors);
                        miPathConnectNeighbors.setToolTipText("Choose from surrounding places");
                        miPathConnectNeighbors.addActionListener(new PathConnectNeighborsDialog(parent.parent, place));

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
                    JMenu mChildren = new JMenu("Children");
                    mChildren.setToolTipText("Child relationships, eg. for maps within maps");
                    if(!parent.passive || !place.getChildren().isEmpty())
                        add(mChildren);

                    if(!parent.passive){
                        JMenuItem miChildConnect = new JMenuItem("Connect with place");
                        miChildConnect.setToolTipText("Connect another place as child place");
                        miChildConnect.setActionCommand("connect_child");
                        miChildConnect.addActionListener(this);
                        mChildren.add(miChildConnect);

                        JMenuItem miChildNewLayer = new JMenuItem("Add child on new map layer");
                        miChildNewLayer.setToolTipText("Creates a new place on a new map layer and connects it with \"" + place.getName() + "\" as it'S parent place");
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
                                JMenuItem mi_sa_remove = new JMenuItem("Remove " + child.getName());
                                m_sa_remove.add(mi_sa_remove);
                                mi_sa_remove.addActionListener(new RemoveSubAreaActionListener(place, child));
                            }
                        }

                        mChildren.add(new JSeparator());

                        for(Place child: children){
                            JMenuItem mi_sa_goto = new JMenuItem("Go to " + child.getName());
                            mChildren.add(mi_sa_goto);
                            mi_sa_goto.addActionListener(new GotoPlaceActionListener(parent, child));
                        }
                    }

                    HashSet<Place> parents = place.getParents();
                    if(!parents.isEmpty()){
                        mChildren.add(new JSeparator());

                        for(Place child: parents){
                            JMenuItem mi_sa_goto = new JMenuItem("Go to parent " + child.getName());
                            mChildren.add(mi_sa_goto);
                            mi_sa_goto.addActionListener(new GotoPlaceActionListener(parent, child));
                        }
                    }

                }  else { // if layer doesn't exist or no place exists at position x,y
                    JMenuItem miNewPlace = new JMenuItem("New place");
                    miNewPlace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
                    miNewPlace.addActionListener(new PlaceDialog(parent.parent, parent.world, layer, posX, posY));
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
                switch(e.getActionCommand()){
                    case "create_placeholder":
                        parent.world.putPlaceholder(parent.getCurPosition().getLayer(), posX, posY);
                        parent.repaint();
                        break;
                    case "create_child_new_layer":
                        // create new place
                        PlaceDialog dlg = new PlaceDialog(parent.parent, parent.getWorld(), null, 0, 0);
                        dlg.setVisible(true);

                        Place place_new = dlg.getPlace();
                        if(place_new != null){
                            // connect new place with place as a child
                            place.connectChild(place_new);
                            // go to new place
                            parent.pushPosition(place_new.getCoordinate());
                        }
                        break;
                    case "connect_child":
                        {
                            PlaceSelectionDialog psdlg = new PlaceSelectionDialog(parent.parent, parent.world, parent.getCurPosition(), true);
                            psdlg.setVisible(true);
                            Place child = psdlg.getSelection();
                            if(child != null && child != place){
                                int ret = JOptionPane.showConfirmDialog(parent, "Connect \"" + child.getName() + "\" to \"" + place.getName() + "\"?", "Connect child place", JOptionPane.YES_NO_OPTION);
                                if(ret == JOptionPane.YES_OPTION){
                                    place.connectChild(child);
                                    parent.repaint();
                                }
                            }
                        }
                        break;
                    case "copy":
                        if(parent.placeGroupHasSelection()){
                            mudmap2.CopyPaste.copy(parent.placeGroupGetSelection(), posX, posY);
                        } else {
                            HashSet<Place> set = new HashSet<>();
                            set.add(place);
                            mudmap2.CopyPaste.copy(set, posX, posY);
                        }
                        parent.repaint();
                        break;
                    case "cut":
                        if(parent.placeGroupHasSelection()){
                            mudmap2.CopyPaste.cut(parent.placeGroupGetSelection(), posX, posY);
                        } else {
                            HashSet<Place> set = new HashSet<>();
                            set.add(place);
                            mudmap2.CopyPaste.cut(set, posX, posY);
                        }
                        parent.repaint();
                        break;
                    case "paste":
                        mudmap2.CopyPaste.paste(posX, posY, layer);
                        parent.repaint();
                        break;
                    case "find_path":
                        {
                            PlaceSelectionDialog psdlg = new PlaceSelectionDialog(parent.parent, parent.world, parent.getCurPosition(), true);
                            psdlg.setVisible(true);
                            Place end = psdlg.getSelection();
                            if(end != null){
                                parent.placeGroupReset();
                                Place place_it = parent.world.breadthSearch(place, end);
                                if(place_it == null) parent.labelInfobar.showMessage("No Path found");
                                else {
                                    int path_length = 0;
                                    while(place_it != null){
                                        parent.placeGroup.add(place_it);
                                        place_it = place_it.getBreadthSearchData().predecessor;
                                        ++path_length;
                                    }
                                    parent.labelInfobar.showMessage("Path found, length: " + (path_length - 1));
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
                    parent.setContextMenu(true);
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                    parent.setContextMenu(false);
                    parent.repaint();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent arg0) {
                    parent.setContextMenu(false);
                    parent.repaint();
                }
            }

            /**
             * Moves the map to the place, if action is performed
             */
            private class GotoPlaceActionListener implements ActionListener{
                WorldTab worldtab;
                Place place;

                public GotoPlaceActionListener(WorldTab worldtab, Place place){
                    this.worldtab = worldtab;
                    this.place = place;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(place != null) worldtab.pushPosition(place.getCoordinate());
                }
            }

            /**
             * Removes a subarea child from a place, if action performed
             */
            private class RemoveSubAreaActionListener implements ActionListener{
                Place place, child;

                public RemoveSubAreaActionListener(Place place, Place child) {
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
            private class ConnectPathActionListener implements ActionListener{

                Place pl1, pl2;
                String dir1, dir2;

                public ConnectPathActionListener(Place _pl1, Place _pl2, String _dir1, String _dir2) {
                    pl1 = _pl1;
                    pl2 = _pl2;
                    dir1 = _dir1;
                    dir2 = _dir2;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    pl1.connectPath(new Path(pl1, dir1, pl2, dir2));
                }
            }

            /**
             * removes a path, if called
             */
            private class RemovePathActionListener implements ActionListener{
                Path path;

                private RemovePathActionListener(Path _path) {
                    path = _path;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    path.remove();
                }
            }

        }
    }
}