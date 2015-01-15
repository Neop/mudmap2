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
    final int default_length = 1 << 30;
            
    private QuadtreeNode root;
    
    /** Constructs a quadtree */
    public Quadtree(){
        root = null;
    }
    
    /**
     * Constructs a quadtree, manually balanced
     * @param center_x center x coordinate
     * @param center_y center y coordinate
     */
    public Quadtree(int center_x, int center_y){
        root = new QuadtreeNode(null, center_x, center_y, default_length); 
    }
    
    /**
     * Gets the element data at position x, y
     * @param x
     * @param y
     * @return element data or null
     */
    public T get(int x, int y){
        if(root == null) return null;
        QuadtreeElement ret = root.get(x, y);
        if(ret == null) return null;
        else return ((QuadtreeLeaf<T>) ret).get_data();
    }
    
    /**
     * Returns true, if an element exists at x, y
     * @param x
     * @param y
     * @return 
     */
    public boolean exist(int x, int y){
        if(root == null) return false;
        return root.get(x, y) != null;
    }
    
    /**
     * Inserts obj at x, y
     * @param obj
     * @param x
     * @param y
     * @throws Exception throws an exception, if the element couldn't be inserted
     */
    public void insert(T obj, int x, int y) throws Exception{
        if(root == null) root = new QuadtreeNode(null, x, y, default_length);
        root.insert(new QuadtreeLeaf(obj, x, y));
    }
    
    /**
     * Removes el from the quadtree
     * @param el
     * @throws Exception throws an exception, if e couldn't be removed
     */
    private void remove(QuadtreeElement el) throws Exception{
        el.remove();
    }
    
    /**
     * Removes element at x, y, if there is one
     * @param x
     * @param y 
     */
    public void remove(int x, int y){
        if(root != null) root.remove(x, y);
    }
    
    /**
     * Moves an element
     * @param x_bef previous x coordinate
     * @param y_bef previous y coordinate
     * @param x_aft new x coordinate
     * @param y_aft new y coordinate
     * @throws Exception Throws an exception, if the element couldn'T be moved
     */
    public void move(int x_bef, int y_bef, int x_aft, int y_aft) throws Exception{
        if(root == null) throw new Exception("Couldn't move element, quadtree is empty");
        T obj = get(x_bef, y_bef);
        remove(x_bef, y_bef);
        insert(obj, x_aft, y_aft);
    }
    
    /**
     * Gets the data of all elements
     * @return 
     */
    public HashSet<T> values(){
        HashSet<T> ret = new HashSet<T>();
        if(root != null) root.values(ret);
        return ret;
    }
    
    /**
     * Gets a String that represents the tree structure
     * @return 
     */
    @Override
    public String toString(){
        if(root == null) return "root: {null}";
        else return "root: " + root.toString();
    }
    
    /**
     * QuadtreeElement interface class
     * @param <T> element data type
     */
    private interface QuadtreeElement<T>{
        /** Gets the x (center) coordinate of the element */
        public int get_x();
        /** Gets the y (center) coordinate of the element */
        public int get_y();
        
        /**
         * Gets the element at x, y or null
         * @param x
         * @param y
         * @return element at x, y or null
         */
        public QuadtreeElement<T> get(int x, int y);
        
        /** gets the parent node or null */
        public QuadtreeElement<T> get_parent();
        /** sets the parent node */
        public void set_parent(QuadtreeElement<T> parent);
        
        /** removes the node from the quadtree */
        public void remove();
        
        /**
         * Gets the element data of each child
         * @param set element data will be inserted in this set
         */
        public void values(HashSet<T> set);
        
        /** checks whether the element is empty */
        public boolean isEmpty();
    }
    
    /**
     * A quadtree node with up to four children, but no own data
     * @param <T> element data type
     */
    private class QuadtreeNode<T> implements QuadtreeElement<T>{

        QuadtreeElement<T> parent;
        int length; // size in each direction
        // max amount of children below this node: (2 * length)^2
        int x, y;
        private QuadtreeElement<T> elements[];
        
        // fake enum (no not-static enums in Java?)
        private static final int NW = 0, NE = 1, SW = 2, SE = 3;

        /**
         * Constructs a new node
         * @param _parent parent node or null (root)
         * @param _x coordinate next to the center (-0.5, -0.5)
         * @param _y coordinate next to the center (-0.5, -0.5)
         * @param _length size in each direction
         */
        public QuadtreeNode(QuadtreeNode<T> _parent, int _x, int _y, int _length) {
            parent = _parent;
            x = _x;
            y = _y;
            length = _length;
            elements = new QuadtreeElement[4];
            for(int i = 0; i < 4; ++i) elements[i] = null;
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
                x > (this.x + length) || y > (this.y + length))
                return -1;
            // calculate child num
            int id = 0;
            if(x > this.x) id = 1;
            if(y > this.y) id |= 2;
            return id;
        }
        
        /**
         * Gets the index of the child at x, y, or -1 if outside of this node
         * for "static class simulation"
         * @param x
         * @param y
         * @return index or -1
         */
        private int getChildNum(int x, int y, int center_x, int center_y, int length){
            // check whether the child is in this node's range
            if(Math.abs(center_x - x) > length || Math.abs(center_y - y) > length) 
                return -1;
            // calculate child num
            int id = 0;
            if(x > center_x) id = 1;
            if(y > center_y) id |= 2;
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
            int childnum = getChildNum(newelement.get_x(), newelement.get_y());
            QuadtreeElement<T> predecessor = elements[childnum];
            
            if(predecessor != null){ // child node exists
                // child node is a node and newelement's position is in that node
                if(predecessor instanceof QuadtreeNode && 
                   ((QuadtreeNode) predecessor).getChildNum(newelement.get_x(), newelement.get_y()) != -1)
                    ((QuadtreeNode) predecessor).insert(newelement);
                
                else { // child node is a leaf -> create split node
                    if(length < 2) throw new Exception("Can't split quadtree node"); // shouldn't occur
                    
                    int newx = x, newy = y, newlength = length;
                    final int compx = Math.min(newelement.get_x(), predecessor.get_x());
                    final int compy = Math.min(newelement.get_y(), predecessor.get_y());
                    // calculate new center and length
                    do {
                        newlength /= 2;
                        if(compx > newx) newx += newlength;
                        else newx -= newlength;
                        if(compy > newy) newy += newlength;
                        else newy -= newlength;
                    } while(newlength > 1 && getChildNum(newelement.get_x(), newelement.get_y(), newx, newy, newlength) == getChildNum(predecessor.get_x(), predecessor.get_y(), newx, newy, newlength));
                    
                    // insert new node
                    QuadtreeNode newnode = new QuadtreeNode(this, newx, newy, newlength);
                    elements[childnum] = newnode;
                    newnode.set_parent(this);
                    
                    newnode.insert(predecessor);
                    newnode.insert(newelement);
                }
            } else { // child node doesn't exist
                // insert new element
                elements[childnum] = newelement;
                newelement.set_parent(this);
            }
        }
        
        /**
         * Remove element by reference
         * @param element 
         */
        public void remove(QuadtreeElement element){    
            // remove node
            int id = getChildNum(element.get_x(), element.get_y());
            if(elements[id] instanceof QuadtreeLeaf || elements[id] == element) elements[id] = null;
            else ((QuadtreeNode) elements[id]).remove(element);

            if(isEmpty()) remove();
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
                if(!(parent instanceof QuadtreeNode)) throw new Error("wrong parent class in quadtree (this shouldn't occur)");
                ((QuadtreeNode<T>) parent).remove(this);
            }
        }

        /**
         * Gets the center x coordinate
         * @return 
         */
        @Override
        public int get_x() {
            return x;
        }

        /**
         * Gets the center y coordinate
         * @return 
         */
        @Override
        public int get_y() {
            return y;
        }
        
        /**
         * Sets the parent node
         * @param _parent 
         */
        @Override
        public void set_parent(QuadtreeElement<T> _parent) {
            parent = _parent;
        }

        /**
         * Gets the parent node or null if root
         * @return parent node or null
         */
        @Override
        public QuadtreeElement<T> get_parent() {
            return parent;
        }

        /**
         * Checks whether the node is empty
         * @return 
         */
        @Override
        public boolean isEmpty() {
            for(int i = 0; i < 4; ++i) 
                if(elements[i] != null && !elements[i].isEmpty())
                    return false;
            return true;
        }
        
        /**
         * Gets the partial tree as a string
         * @return 
         */
        @Override
        public String toString(){
            return "{(" + get_x() + ", " + get_y() + ", " + length + "), NW: " + (elements[NW] != null ? elements[NW].toString() : "null") + 
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
            for(int i = 0; i < 4; ++i) if(elements[i] != null) elements[i].values(set);
        }
    }
    
    /**
     * A quadtree leaf
     * @param <T> 
     */
    private class QuadtreeLeaf<T> implements QuadtreeElement<T>{
        
        private QuadtreeElement<T> parent;
        private T data;
        private int x, y;
        
        /**
         * Constructs a new quadtree leaf
         * @param object element data
         * @param _x element x coordinate
         * @param _y element y coordinate
         */
        public QuadtreeLeaf(T object, int _x, int _y){
            parent = null;
            data = object;
            x = _x;
            y = _y;
        }
        
        /**
         * Gets the x coordinate
         * @return 
         */
        @Override
        public int get_x(){
            return x;
        }
        
        /**
         * Gets the y coordinate
         * @return 
         */
        @Override
        public int get_y(){
            return y;
        }
        
        /**
         * Gets the element data
         * @return 
         */
        public T get_data(){
            return data;
        }
        
        /**
         * Removes the element
         */
        @Override
        public void remove(){
            if(!(parent instanceof QuadtreeNode)) throw new Error("wrong parent class in quadtree (this shouldn't occur)");
            ((QuadtreeNode<T>) parent).remove(this);
        }

        /**
         * Gets the parent node
         * @return 
         */
        @Override
        public QuadtreeElement<T> get_parent() {
            return parent;
        }
        
        /**
         * Sets the parent node
         * @param _parent 
         */
        @Override
        public void set_parent(QuadtreeElement<T> _parent){
            parent = _parent;
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
            return "\"" + get_data().toString() + "\"";
        }

        /**
         * Gets the element if the coordinates are correct
         * @param x element x coordinate
         * @param y element y coordinate
         * @return element or null
         */
        @Override
        public QuadtreeElement<T> get(int x, int y) {
            if(this.x == x && this.y == y) return this;
            return null;
        }

        /**
         * Puts the element data into set
         * @param set 
         */
        @Override
        public void values(HashSet<T> set) {
            if(data != null) set.add(data);
        }
    }
}
