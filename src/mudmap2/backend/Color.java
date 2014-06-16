/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mudmap2.backend;

/**
 * A RGB Color
 * @author neop
 */
public class Color {
    
    int r, g, b;
    
    public Color(int _r, int _g, int _b) {
        r = clamp255(_r);
        g = clamp255(_g);
        b = clamp255(_b);
    }
    
    /**
     * Convert to java awt color
     * @return awt compatible color
     */
    public java.awt.Color get_awt_color(){
        return new java.awt.Color(r, g, b);
    }
    
    /**
     * Gets the red value
     * 
     * @return red value
     */
    public int get_r(){
        return r;
    }
    
    /**
     * Gets the green value
     * 
     * @return green value
     */
    public int get_g(){
        return g;
    }
    
    /**
     * Gets the blue value
     * 
     * @return blue value
     */
    public int get_b(){
        return b;
    }
    
    /**
     * Clamps the value to the inteval 0 - 255
     * 
     * @param v value to clamp
     * @return clamped value
     */
    private int clamp255(int v) {
        if(v > 255) v = 255;
        else if(v < 0) v = 0;
        return v;
    }
    
    @Override
    public String toString(){
        return "" + r + " " + g + " " + b;
    }
}
