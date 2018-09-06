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
 *  This class describes a pr-quadtree
 */

package mudmap2.backend.prquadtree;

import java.util.HashSet;

/**
 * A point-region quadtree
 * @author Neop
 * @param <T> element class type
 */
public class Quadtree<T> {

    /// NOTE: increase the last parameter to support larger maps (is already max for int)
    private static final int DEFAULT_LENGTH = 1 << 30;

    private QuadtreeNode root;

    /** Constructs a quadtree */
    public Quadtree(){
        root = null;
    }

    /**
     * Constructs a quadtree, manually balanced
     * @param centerX center x coordinate
     * @param centerY center y coordinate
     */
    public Quadtree(int centerX, int centerY){
        root = new QuadtreeNode(null, centerX, centerY, DEFAULT_LENGTH);
    }

    /**
     * Gets the element data at position x, y
     * @param x
     * @param y
     * @return element data or null
     */
    public T get(int x, int y){
        if(root == null){
            return null;
        }

        QuadtreeElement ret = root.get(x, y);
        if(ret == null){
            return null;
        } else {
            return ((QuadtreeLeaf<T>) ret).getData();
        }
    }

    /**
     * Returns true, if an element exists at x, y
     * @param x
     * @param y
     * @return
     */
    public boolean exist(int x, int y){
        if(root == null){
            return false;
        }
        return root.get(x, y) != null;
    }

    public boolean isEmpty(){
        if(root == null){
            return true;
        } else {
            return root.isEmpty();
        }
    }

    /**
     * Inserts obj at x, y
     * @param obj
     * @param x
     * @param y
     * @throws Exception throws an exception, if the element couldn't be inserted
     */
    public void insert(T obj, int x, int y) throws Exception{
        if(root == null){
            root = new QuadtreeNode(null, x, y, DEFAULT_LENGTH);
        }
        root.insert(new QuadtreeLeaf(obj, x, y));
    }

    /**
     * Removes el from the quadtree
     * @param el
     * @throws Exception throws an exception, if e couldn't be removed
     */
    private void remove(QuadtreeElement el) throws Exception {
        el.remove();
    }

    /**
     * Removes element at x, y, if there is one
     * @param x
     * @param y
     */
    public void remove(int x, int y){
        if(root != null) {
            root.remove(x, y);
        }
    }

    /**
     * Removes all elements that contain object
     * @param object
     */
    public void remove(T object){
        if(root != null) {
            root.remove(object);
        }
    }

    /**
     * Moves an element
     * @param xOld previous x coordinate
     * @param yOld previous y coordinate
     * @param xNew new x coordinate
     * @param yNew new y coordinate
     * @throws Exception Throws an exception, if the element couldn'T be moved
     */
    public void move(int xOld, int yOld, int xNew, int yNew) throws Exception {
        if(root == null){
            throw new Exception("Couldn't move element, quadtree is empty");
        }
        T obj = get(xOld, yOld);
        remove(xOld, yOld);
        insert(obj, xNew, yNew);
    }

    /**
     * Gets the data of all elements
     * @return
     */
    public HashSet<T> values(){
        HashSet<T> ret = new HashSet<>();
        if(root != null){
            root.values(ret);
        }
        return ret;
    }

    /**
     * Gets a String that represents the tree structure
     * @return
     */
    @Override
    public String toString(){
        if(root == null){
            return "root: {null}";
        } else {
            return "root: " + root.toString();
        }
    }

    /**
     * Checks whether the quadtree contains object
     * @param object
     * @return
     */
    public boolean contains(T object){
        return (root != null && root.contains(object));
    }

    /**
     * QuadtreeElement interface class
     * @param <T> element data type
     */
    private interface QuadtreeElement<T>{
        /** Gets the x (center) coordinate of the element */
        public int getX();
        /** Gets the y (center) coordinate of the element */
        public int getY();

        /**
         * Gets the element at x, y or null
         * @param x
         * @param y
         * @return element at x, y or null
         */
        public QuadtreeElement<T> get(int x, int y);

        /** gets the parent node or null */
        public QuadtreeElement<T> getParent();
        /** sets the parent node */
        public void setParent(QuadtreeElement<T> parent);

        /** removes the node from the quadtree */
        public void remove();

        public void remove(T object);

        /**
         * Gets the element data of each child
         * @param set element data will be inserted in this set
         */
        public void values(HashSet<T> set);

        /** checks whether the element is empty */
        public boolean isEmpty();

        /** checks whether the element or one of its children contains object*/
        public boolean contains(T object);
    }

    /**
     * A quadtree node with up to four children, but no own data
     * @param <T> element data type
     */
    private class QuadtreeNode<T> implements QuadtreeElement<T>{

        QuadtreeElement<T> parent;
        int length; // size in each direction
        // max amount of children below this node: (2 * length)^2
        final int x, y;
        private final QuadtreeElement<T> elements[] = new QuadtreeElement[4];

        // fake enum (no not-static enums in Java?)
        private static final int NW = 0, NE = 1, SW = 2, SE = 3;

        /**
         * Constructs a new node
         * @param _parent parent node or null (root)
         * @param _x coordinate next to the center (-0.5, -0.5)
         * @param _y coordinate next to the center (-0.5, -0.5)
         * @param _length size in each direction
         */
        public QuadtreeNode(QuadtreeNode<T> parent, int x, int y, int length) {
            this.parent = parent;
            this.x = x;
            this.y = y;
            this.length = length;
            for(int i = 0; i < 4; ++i){
                elements[i] = null;
            }
        }

        /**
         * Gets the index of the child at x, y, or -1 if outside of this node
         * @param x
         * @param y
         * @return index or -1
         */
        private int getChildNum(int x, int y){
            // check whether the child is in this node's range
            if(x < (this.x - length + 1) || y < (this.y - length + 1) ||
                x > (this.x + length) || y > (this.y + length)){
                return -1;
            }
            // calculate child num
            int id = 0;
            if(x > this.x){
                id = 1;
            }
            if(y > this.y){
                id |= 2;
            }
            return id;
        }

        /**
         * Gets the index of the child at x, y, or -1 if outside of this node
         * for "static class simulation"
         * @param x
         * @param y
         * @return index or -1
         */
        private int getChildNum(int x, int y, int centerX, int centerY, int length){
            // check whether the child is in this node's range
            if(Math.abs(centerX - x) > length || Math.abs(centerY - y) > length){
                return -1;
            }
            // calculate child num
            int id = 0;
            if(x > centerX){
                id = 1;
            }
            if(y > centerY){
                id |= 2;
            }
            return id;
        }

        /**
         * Gets the element data at x, y or null
         * @param x
         * @param y
         * @return element data or null
         */
        @Override
        public QuadtreeElement<T> get(int x, int y){
            int num = getChildNum(x, y);
            if(num != -1){
                QuadtreeElement<T> ret = elements[num];
                if(ret != null) return ret.get(x, y);
            }
            return null;
        }

        /**
         * Inserts a new element (the element has to know it's position)
         * @param newelement
         * @throws Exception if node couldn't be splitted (shouldn't occur)
         */
        public void insert(QuadtreeElement<T> newelement) throws Exception{
            int childnum = getChildNum(newelement.getX(), newelement.getY());
            QuadtreeElement<T> predecessor = elements[childnum];

            if(predecessor != null){ // child node exists
                // child node is a node and newelement's position is in that node
                if(predecessor instanceof QuadtreeNode &&
                   ((QuadtreeNode) predecessor).getChildNum(newelement.getX(), newelement.getY()) != -1)
                    ((QuadtreeNode) predecessor).insert(newelement);

                else { // child node is a leaf -> create split node
                    if(length < 2){
                        throw new Exception("Can't split quadtree node");
                    } // shouldn't occur

                    int newx = x, newy = y, newlength = length;
                    final int compx = Math.min(newelement.getX(), predecessor.getX());
                    final int compy = Math.min(newelement.getY(), predecessor.getY());
                    // calculate new center and length
                    do {
                        newlength /= 2;
                        if(compx > newx){
                            newx += newlength;
                        } else {
                            newx -= newlength;
                        }
                        if(compy > newy){
                            newy += newlength;
                        } else {
                            newy -= newlength;
                        }
                    } while(newlength > 1 && getChildNum(newelement.getX(), newelement.getY(), newx, newy, newlength) == getChildNum(predecessor.getX(), predecessor.getY(), newx, newy, newlength));

                    // insert new node
                    QuadtreeNode newnode = new QuadtreeNode(this, newx, newy, newlength);
                    elements[childnum] = newnode;
                    newnode.setParent(this);

                    newnode.insert(predecessor);
                    newnode.insert(newelement);
                }
            } else { // child node doesn't exist
                // insert new element
                elements[childnum] = newelement;
                newelement.setParent(this);
            }
        }

        /**
         * Remove element by reference
         * @param element
         */
        public void remove(QuadtreeElement element){
            // remove node
            int id = getChildNum(element.getX(), element.getY());
            if(elements[id] instanceof QuadtreeLeaf || elements[id] == element){
                elements[id] = null;
            } else {
                ((QuadtreeNode) elements[id]).remove(element);
            }

            if(isEmpty()){
                remove();
            }
        }

        /**
         * Remove elements by reference of wrapped object
         * @param object wrapped object
         */
        @Override
        public void remove(T object){
            for(int i = 0; i < 4; ++i){
                if(elements[i] != null){
                    elements[i].remove(object);
                }
            }
        }

        /**
         * Remove by coordinate
         * @param x
         * @param y
         */
        public void remove(int x, int y){
            QuadtreeElement el = get(x, y);
            if(el != null) el.remove();
        }

        /**
         * Remove this element
         */
        @Override
        public void remove() {
            if(parent != null){
                if(!(parent instanceof QuadtreeNode)){
                    throw new Error("wrong parent class in quadtree (this shouldn't occur)");
                }
                ((QuadtreeNode<T>) parent).remove(this);
            }
        }

        /**
         * Gets the center x coordinate
         * @return
         */
        @Override
        public int getX() {
            return x;
        }

        /**
         * Gets the center y coordinate
         * @return
         */
        @Override
        public int getY() {
            return y;
        }

        /**
         * Sets the parent node
         * @param _parent
         */
        @Override
        public void setParent(QuadtreeElement<T> parent) {
            this.parent = parent;
        }

        /**
         * Gets the parent node or null if root
         * @return parent node or null
         */
        @Override
        public QuadtreeElement<T> getParent() {
            return parent;
        }

        /**
         * Checks whether the node is empty
         * @return
         */
        @Override
        public boolean isEmpty() {
            for(int i = 0; i < 4; ++i){
                if(elements[i] != null && !elements[i].isEmpty()){
                    return false;
                }
            }
            return true;
        }

        /**
         * Gets the partial tree as a string
         * @return
         */
        @Override
        public String toString(){
            return "{(" + getX() + ", " + getY() + ", " + length + "), NW: " + (elements[NW] != null ? elements[NW].toString() : "null") +
                    ", NE: " + (elements[NE] != null ? elements[NE].toString() : "null") +
                    ", SW: " + (elements[SW] != null ? elements[SW].toString() : "null") +
                    ", SE: " + (elements[SE] != null ? elements[SE].toString() : "null") + "}";
        }

        /**
         * Gets the element data of the children
         * @param set inserts tghe data into this set
         */
        @Override
        public void values(HashSet<T> set) {
            for(int i = 0; i < 4; ++i){
                if(elements[i] != null){
                    elements[i].values(set);
                }
            }
        }

        /**
         * Checks whether the element or one of its children contains object
         * @param object
         * @return
         */
        @Override
        public boolean contains(T object) {
            if(elements[NE] != null && elements[NE].contains(object)) return true;
            else if(elements[NW] != null && elements[NW].contains(object)) return true;
            else if(elements[SE] != null && elements[SE].contains(object)) return true;
            else if(elements[SW] != null && elements[SW].contains(object)) return true;
            return false;
        }
    }

    /**
     * A quadtree leaf
     * @param <T>
     */
    private class QuadtreeLeaf<T> implements QuadtreeElement<T>{

        private QuadtreeElement<T> parent = null;
        private final T data;
        private final int x, y;

        /**
         * Constructs a new quadtree leaf
         * @param object element data
         * @param _x element x coordinate
         * @param _y element y coordinate
         */
        public QuadtreeLeaf(T data, int x, int y){
            this.data = data;
            this.x = x;
            this.y = y;
        }

        /**
         * Gets the x coordinate
         * @return
         */
        @Override
        public int getX(){
            return x;
        }

        /**
         * Gets the y coordinate
         * @return
         */
        @Override
        public int getY(){
            return y;
        }

        /**
         * Gets the element data
         * @return
         */
        public T getData(){
            return data;
        }

        /**
         * Removes the element
         */
        @Override
        public void remove(){
            if(!(parent instanceof QuadtreeNode)){
                throw new Error("wrong parent class in quadtree (this shouldn't occur)");
            }
            ((QuadtreeNode<T>) parent).remove(this);
        }

        /**
         * Removes the element if it wraps object
         * @param object object to check for
         */
        @Override
        public void remove(T object){
            if(data == object){
                remove();
            }
        }

        /**
         * Gets the parent node
         * @return
         */
        @Override
        public QuadtreeElement<T> getParent() {
            return parent;
        }

        /**
         * Sets the parent node
         * @param _parent
         */
        @Override
        public void setParent(QuadtreeElement<T> parent){
            this.parent = parent;
        }

        /**
         * Checks whether the element is empty (data is null)
         * @return
         */
        @Override
        public boolean isEmpty() {
            return data == null;
        }

        /**
         * Gets the element data as a String
         * @return
         */
        @Override
        public String toString(){
            return "\"" + getData().toString() + "\"";
        }

        /**
         * Gets the element if the coordinates are correct
         * @param x element x coordinate
         * @param y element y coordinate
         * @return element or null
         */
        @Override
        public QuadtreeElement<T> get(int x, int y) {
            if(this.x == x && this.y == y){
                return this;
            }
            return null;
        }

        /**
         * Puts the element data into set
         * @param set
         */
        @Override
        public void values(HashSet<T> set) {
            if(data != null){
                set.add(data);
            }
        }

        /**
         * Checks whether the element contains object
         * @param object
         * @return
         */
        @Override
        public boolean contains(T object) {
            return data == object;
        }
    }
}
