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
package mudmap2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import mudmap2.backend.Layer;
import mudmap2.backend.LayerElement;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.utils.Pair;

/**
 *
 * @author neop
 */
public final class CopyPaste {

    // places to be copied or cut
    static HashSet<Place> copyPlaces;
    static int copydx, copydy;
    // if true, the places will be copied instead of cut
    static boolean copyMode; // copy or cut
    // locations of copied places relative to cursor (places will be inserted here)
    static HashSet<Pair<Integer, Integer>> copyPlaceLocations;

    private CopyPaste() {
    };

    /**
     * Cuts the places relatively to x, y
     * @param places
     * @param x
     * @param y
     */
    public static void copy(final HashSet<Place> places, final int x, final int y) {
        resetCopy();

        copyPlaces = (HashSet<Place>) places.clone();
        copyMode = true;
        copydx = x;
        copydy = y;
        generateLocations(x, y);
    }

    /**
     * Copies the places relatively to x, y
     * @param places
     * @param x
     * @param y
     */
    public static void cut(final HashSet<Place> places, final int x, final int y) {
        resetCopy();

        copyPlaces = (HashSet<Place>) places.clone();
        copyMode = false;
        copydx = x;
        copydy = y;
        generateLocations(x, y);
    }

    /**
     * Calculates copyPlaceLocations relatively to x, y
     */
    private static void generateLocations(final int x, final int y) {
        copyPlaceLocations = new HashSet<>();
        for (final Place place : copyPlaces) {
            copyPlaceLocations.add(new Pair<>(place.getX() - x, place.getY() - y));
        }
    }

    /**
     * Checks whether the places can be pasted relatively to x,y on layer
     * @param x
     * @param y
     * @param layer
     * @return
     */
    public static boolean canPaste(final int x, final int y, final Layer layer) {
        if (copyPlaces == null || copyPlaces.isEmpty()) {
            return false;
        }
        if (copyPlaceLocations != null && layer != null) {
            for (final Pair<Integer, Integer> coordinate : copyPlaceLocations) {
                final LayerElement collision = layer.get(x + coordinate.first, y + coordinate.second);
                if (collision != null) {
                    if (copyMode) {
                        return false;
                    } else { // if places are moved: checkif the colliding place is in the movied group, too
                        if (!copyPlaces.contains(collision)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Pastes the cut / copied places to layer, if possible
     * @param x
     * @param y
     * @param layer
     * @return false on error or user abort
     */
    public static boolean paste(final int x, final int y, final Layer layer) {
        if (!canPaste(x, y, layer)) {
            return false;
        }

        // ask user
        final String title = (copyMode ? "Copy " : "Paste ") + "place(s)";
        final String message = title + "? This can not be undone!" + (copyPlaces.iterator().next().getLayer().getWorld() != layer.getWorld() ? " Pasting to another world might cause problems!" : "");
        final int ret = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
        if (ret == JOptionPane.YES_OPTION) {
            // map to translate from old to new place
            final HashMap<Place, Place> place_to_new_place = new HashMap<Place, Place>();

            Place[] places;

            if (copyMode) {
                places = copyPlaces.toArray(new Place[copyPlaces.size()]);
            } else {
                // getPlace movement direction
                final int fact_x = x <= copydx ? 1 : -1;
                final int fact_y = y <= copydy ? 1 : -1;

                // sort places
                final ArrayList<Place> ordered_places = new ArrayList<>(copyPlaces);
                Collections.sort(ordered_places, new Comparator<Place>() {
                    @Override
                    public int compare(final Place t, final Place t1) {
                        // order by movement direction:
                        // places that might collide with other places in the
                        // list will be moved first
                        if (fact_x * t.getX() > fact_x * t1.getX()) {
                            return 1;
                        } else if (t.getX() == t1.getX()) {
                            if (fact_y * t.getY() > fact_y * t1.getY()) {
                                return 1;
                            } else if (t.getY() == t1.getY()) {
                                return 0;
                            }
                        }
                        return -1;
                    }
                });

                places = ordered_places.toArray(new Place[ordered_places.size()]);
            }

            // copy places
            for (final Place place : places) {
                try {
                    if (place.getLayer().getWorld() != layer.getWorld()) {
                        if (place.getPlaceGroup() != null && !layer.getWorld().getPlaceGroups().contains(place.getPlaceGroup())) {
                            layer.getWorld().addPlaceGroup(place.getPlaceGroup());
                        }
                    }
                    if (copyMode) { // copy places -> duplicate on new layer
                        final Place new_place = place.duplicate();
                        place_to_new_place.put(place, new_place);
                        layer.put(new_place, place.getX() - copydx + x, place.getY() - copydy + y);
                    } else {
                        // remove place from old layer
                        final Layer layerOld = place.getLayer();
                        layerOld.remove(place);

                        // add place to new layer
                        place.setLayer(layer);
                        layer.put(place, place.getX() - copydx + x, place.getY() - copydy + y);
                    }
                } catch (final Exception ex) {
                    Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }

            // recreate paths and child connections after copy-paste
            if (copyMode) {
                for (final Place place : copyPlaces) {
                    final Place new_place = place_to_new_place.get(place);
                    // connect paths
                    for (final Path path : place.getPaths()) {
                        // only check first place, because the other side will
                        // check itself
                        final Place path_end_place = path.getPlaces()[0];
                        // if end place is not this place and is also copied
                        if (path_end_place != place && copyPlaces.contains(path_end_place)) {
                            final Place other_new_place = place_to_new_place.get(path_end_place);
                            new_place.connectPath(new Path(other_new_place, path.getExitDirections()[0], new_place, path.getExitDirections()[1]));
                        }
                    }
                    // connect children
                    for (final Place child : place.getChildren()) {
                        // if child is copied, too
                        if (copyPlaces.contains(child)) {
                            final Place new_child = place_to_new_place.get(child);
                            new_place.connectChild(new_child);
                        }
                    }
                }
                // moving places modifies their coordinates so that they cant be pasted again
            } else {
                resetCopy();
            }
        }

        // cleanup
        //copy_places = null;
        //copy_place_locations = null;
        // don't clean up but change cut to copy
        return true;
    }

    /**
     * Moves the given place to the new x/y coordinates on the place's layer.
     * @param place
     * @param x
     * @param y
     * @return True if the place was moved
     */
    public static boolean move(final Place place, final int x, final int y) {
        try {
            final Layer layer = place.getLayer();
            if (layer.get(x, y) != null) {
                return false;
            }
            layer.remove(place);
            layer.put(place, x, y);
            return true;
        } catch (final Exception e) {
            Logger.getLogger(Mudmap2.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

    /**
     * Returns true, if there are places to paste
     * @return
     */
    public static boolean hasCopyPlaces() {
        return copyPlaces != null;
    }

    /**
     * Gets the places to cut / copy
     * @return places or null
     */
    public static HashSet<Place> getCopyPlaces() {
        return copyPlaces;
    }

    /**
     * Gets the locations of the copied places relatively to the cursor
     * @return locations or null
     */
    public static HashSet<Pair<Integer, Integer>> getCopyPlaceLocations() {
        return copyPlaceLocations;
    }

    /**
     * Gets the layer of the copied places
     * @return layer or null
     */
    public static Layer getCopyPlaceLayer() {
        if (copyPlaces.isEmpty()) {
            return null;
        }
        return copyPlaces.iterator().next().getLayer();
    }

    /**
     * Removes all places from the copy/cut list
     */
    public static void resetCopy() {
        // cleanup
        copyPlaces = null;
        copyPlaceLocations = null;
    }

    /**
     * Has plac(es) for cut operation
     * @return
     */
    public static Boolean isCut() {
        return hasCopyPlaces() && !copyMode;
    }

    public static Boolean isMarked(final Place place) {
        return copyPlaces.contains(place);
    }
}
