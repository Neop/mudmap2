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
 *  This class describes a place in a world
 */

package mudmap2.backend;

import java.util.HashSet;
import java.util.TreeMap;

import mudmap2.backend.sssp.BreadthSearch;

/**
 * A place in the world
 * @author neop
 */
public class Place extends LayerElement implements Comparable<Place>, BreadthSearch {

    public static final String PLACEHOLDER_NAME = "?";

    // next id to be assigned
    static int nextID;

    int id;
    String name;
    PlaceGroup placeGroup = null;
    int recLevelMin = -1;
    int recLevelMax = -1;
    InformationColor infoRing = null;
    String comments = "";

    HashSet<Place> children = new HashSet<>();
    HashSet<Place> parents = new HashSet<>();
    HashSet<Path> paths = new HashSet<>();
    TreeMap<String, Boolean> flags = new TreeMap<>();

    BreadthSearchData breadthSearchData = null;

    public Place(final int id, final String name, final int posX, final int posY, final Layer l) {
        super(posX, posY, l);
        this.name = name;
        this.id = id;
        if (id >= nextID) {
            nextID = id + 1;
        }
    }

    /**
     * Constructs new a place a certain position
     * @param name name
     * @param posX x coordinate
     * @param posY y coordinate
     * @param l
     */
    public Place(final String name, final int posX, final int posY, final Layer l) {
        super(posX, posY, l);
        this.name = name;
        id = nextID++;
    }

    /**
     * Gets the place id
     * @return place id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the name
     * @return name of the place
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     * @param name new name
     */
    public void setName(final String name) {
        this.name = name;
        callWorldChangeListeners();
    }

    /**
     * Gets the position of a place as world coordinate
     * @return place coordinate
     */
    public WorldCoordinate getCoordinate() {
        return new WorldCoordinate(getLayer().getId(), getX(), getY());
    }

    /**
     * Gets the PlaceGroup
     * @return placeGroup
     */
    public PlaceGroup getPlaceGroup() {
        return placeGroup;
    }

    /**
     * Sets the PlaceGroup
     * @param placeGroup
     */
    public void setPlaceGroup(final PlaceGroup placeGroup) {
        this.placeGroup = placeGroup;
        if (placeGroup != null && getLayer() != null && getLayer().getWorld() != null) {
            getLayer().getWorld().addPlaceGroup(placeGroup);
        }
        callWorldChangeListeners();
    }

    /**
     * Gets the minimal recommended level
     * @return minimal recommended level
     */
    public int getRecLevelMin() {
        return recLevelMin;
    }

    /**
     * Sets the minimal recommended level
     * @param recLevelMin
     */
    public void setRecLevelMin(final int recLevelMin) {
        this.recLevelMin = recLevelMin;
        callWorldChangeListeners();
    }

    /**
     * Gets the maximal recommended level
     * @return maximal recommended level
     */
    public int getRecLevelMax() {
        return recLevelMax;
    }

    /**
     * Sets the maximal recommended level
     * @param recLevelMax
     */
    public void setRecLevelMax(final int recLevelMax) {
        this.recLevelMax = recLevelMax;
        callWorldChangeListeners();
    }

    /**
     * Gets the information ring (colored ring on place tile)
     * @return InformationColor
     */
    public InformationColor getInfoRing() {
        return infoRing;
    }

    /**
     * sets the information ring
     * @param infoRing
     */
    public void setInfoRing(final InformationColor infoRing) {
        this.infoRing = infoRing;
        callWorldChangeListeners();
    }

    /**
     * Gets the comments
     * @return comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the comments
     * @param comments
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Gets the paths connected to an exit
     * @param dir exit direction
     * @return set of paths connected to that exit
     */
    public HashSet<Path> getExit(final String dir) {
        HashSet<Path> ret = new HashSet<>();
        for (final Path path : paths) {
            if (path.getExit(this).equals(dir)) {
                ret.add(path);
            }
        }
        return ret;
    }

    /**
     * Gets the paths to a place
     * @param place a place that this place is connected to
     * @return paths to place
     */
    public HashSet<Path> getPaths(final Place place) {
        final HashSet<Path> ret = new HashSet<>();
        for (final Path path : paths) {
            if (path.hasPlace(place)) {
                ret.add(path);
            }
        }
        return ret;
    }

    /**
     * Gets all paths
     * @return all paths
     */
    public HashSet<Path> getPaths() {
        return paths;
    }

    /**
     * Removes a path
     * @param path
     */
    public void removePath(final Path path) {
        paths.remove(path);
        path.getOtherPlace(this).paths.remove(path);
        callWorldChangeListeners();
    }

    /**
     * Removes a set of paths
     * @param paths set of paths
     */
    public void removePaths(final HashSet<Path> paths){
        paths.removeAll(paths);
        for(Path path: paths){
            path.getOtherPlace(this).paths.remove(path);
        }
        callWorldChangeListeners();
    }

    /**
     * Connects a place to another one tht is specified in path
     * If 'this place' is not in path an exception will be thrown
     * @param path
     */
    public void connectPath(final Path path) throws RuntimeException, NullPointerException {
        final Place[] pp = path.getPlaces();
        Place other;

        if (pp[0] == this) {
            other = pp[1];
        } else if (pp[1] == this) {
            other = pp[0];
        } else {
            throw new RuntimeException("Wrong place in given path");
        }

        // check whether other place is null
        if (other == null) {
            throw new NullPointerException();
        }

        // check whether the path connects a place with itself on the same exit
        if (other == this && path.getExitDirections()[0].equals(path.getExitDirections()[1])) {
            throw new RuntimeException("Can not connect path to the same exit of one place");
        }

        paths.add(path);
        other.paths.add(path);

        callWorldChangeListeners();
    }

    /**
     * Get a set of paths connected to exit
     * @param dir exit direction
     * @return set of paths or empty set
     */
    public HashSet<Path> getPathsTo(final String dir) {
        HashSet<Path> ret = new HashSet<>();
        for (final Path pa : paths) {
            if (pa.getExit(this).equals(dir)) {
                ret.add(pa);
            }
        }
        return ret;
    }

    /**
     * Gets a flag value
     * @param key flag name
     * @return flag value
     */
    public boolean getFlag(final String key) {
        if (key != null && flags.containsKey(key)) {
            return flags.get(key);
        }
        return false;
    }

    /**
     * Sets a string flag
     * @param key flag name
     * @param state value
     */
    public void setFlag(final String key, final boolean state) {
        if (key != null) {
            flags.put(key, state);
            callWorldChangeListeners();
        }
    }

    /**
     * Gets the flags of a place
     * @return
     */
    public TreeMap<String, Boolean> getFlags() {
        return flags;
    }

    /**
     * Connects a place as child, this place will be added to the child as parent
     * @param place
     */
    public void connectChild(final Place place) {
        if (place != null) {
            children.add(place);
            place.parents.add(this);
            callWorldChangeListeners();
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Removes a parent - child connection
     * @param place child to be removed
     */
    public void removeChild(final Place place) {
        if (place != null) {
            children.remove(place);
            place.parents.remove(this);
            callWorldChangeListeners();
        } // don't throw
    }

    /**
     * Gets the child places
     * @return child places
     */
    public HashSet<Place> getChildren() {
        return children;
    }

    /**
     * Gets the parent places
     * @return parent places
     */
    public HashSet<Place> getParents() {
        return parents;
    }

    /**
     * Gets the name
     * @return name of he place
     */
    @Override
    public String toString() {
        return name + " (ID: " + getId() + ")";
    }

    /**
     * Compares two places by their name
     * @param arg0
     * @return
     */
    @Override
    public int compareTo(final Place arg0) {
        if (arg0 == null) {
            throw new NullPointerException();
        }
        return getName().compareTo(arg0.getName());
    }

    /**
     * Removes all connections to other places (paths, child-connections)
     */
    public void removeConnections() {
        // remove place paths (buffer, since connected_places will be modified)
        final HashSet<Path> cp_buffer = (HashSet<Path>) paths.clone();
        for (final Path p : cp_buffer) {
            p.remove();
        }
        // remove place connection to children / parents
        for (final Place pl : children) {
            pl.parents.remove(this);
        }
        children.clear();
        for (final Place pl : parents) {
            pl.children.remove(this);
        }
        parents.clear();

        callWorldChangeListeners();
    }

    /**
     * Returns true, if the keyword is found in any of the places data
     * it searches in name, comments and flags
     * @param keyword
     * @return true, if the keyword is found
     */
    private boolean matchKeyword(String keyword) {
        keyword = keyword.toLowerCase();
        // search in name
        if (name.toLowerCase().contains(keyword)) {
            return true;
        }
        // search in comments
        return comments.toLowerCase().contains(keyword);
    }

    /**
     * Returns true, if a keyword is found in any of the places data
     * it searches in name, comments and flags
     * @param keywords
     * @return true, if a keyword is found
     */
    public boolean matchKeywords(final String[] keywords) {
        if (keywords == null) {
            return false;
        }

        for (final String kw : keywords) {
            if (!matchKeyword(kw)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new place from this place
     * a new id will be created and connections to other places not copied
     * @return
     */
    public Place duplicate() {
        final Place place = new Place(name, getX(), getY(), null);

        place.placeGroup = placeGroup;
        place.recLevelMax = recLevelMax;
        place.recLevelMin = recLevelMin;
        place.infoRing = infoRing;
        place.flags = (TreeMap<String, Boolean>) flags.clone();
        place.comments = comments;

        return place;
    }

    /**
     * Resets the breadth search data
     */
    @Override
    public void breadthSearchReset() {
        getBreadthSearchData().reset();
    }

    /**
     * Gets the breadth search data
     * @return
     */
    @Override
    public BreadthSearchData getBreadthSearchData() {
        if (breadthSearchData == null) {
            breadthSearchData = new BreadthSearchData();
        }
        return breadthSearchData;
    }

    /**
     * Call world change listeners on place changes
     */
    private void callWorldChangeListeners() {
        if (getLayer() != null && getLayer().getWorld() != null) {
            getLayer().getWorld().callListeners(this);
        }
    }

}
