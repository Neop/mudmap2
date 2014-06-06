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
    
    /// Combined RGB values
    private int rgb;

    public Color(int r, int g, int b) {
        r = clamp255(r);
        g = clamp255(g);
        b = clamp255(b);
        
        rgb = (r << (8 * 2)) | (g << 8) | b;
    }
    
    /**
     * Convert to java awt color
     * @return awt compatible color
     */
    public java.awt.Color get_awt_color(){
        return new java.awt.Color(rgb);
    }
    
    /**
     * Gets the red value
     * 
     * @return red value
     */
    public int get_r(){
        return (rgb >>> 8 * 2) & 0xF;
    }
    
    /**
     * Gets the green value
     * 
     * @return green value
     */
    public int get_g(){
        return (rgb >>> 8) & 0xF;
    }
    
    /**
     * Gets the blue value
     * 
     * @return blue value
     */
    public int get_b(){
        return rgb & 0xF;
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
}
