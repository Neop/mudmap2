/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2015  Neop (email: mneop@web.de)
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import mudmap2.utils.Pair;
import mudmap2.backend.Layer;
import mudmap2.backend.Path;
import mudmap2.backend.Place;
import mudmap2.backend.World;
import mudmap2.backend.WorldCoordinate;

/**
 *
 * @author Neop
 */
public class MapPainterDefault implements MapPainter {

    static final float PATH_STROKE_WIDTH = 3;

    static final float TILE_SELECTION_STROKE_WIDTH = 3;
    static final java.awt.Color TILE_SELECTION_COLOR = new java.awt.Color(255, 0, 0);

    static final float TILE_RISK_LEVEL_STROKE_WIDTH = 2;
    static final int TILE_BORDER_WIDTH = 10;
    static final int EXIT_RADIUS = 5;

    HashSet<Place> selectePlaces;
    WorldCoordinate placeSelectionBoxStart, placeSelectionBoxEnd;
    int placeSelectedX, placeSelectedY;
    boolean placeSelectionEnabled;

    double graphicsWidth, graphicsHeight;
    int tileSize;
    WorldCoordinate curPos;

    Font lastTileFont;

    Boolean showPaths;
    Boolean showPathsCurved;

    public MapPainterDefault() {
        selectePlaces = null;
        placeSelectedX = placeSelectedY = 0;
        placeSelectionEnabled = false;
        lastTileFont = null;

        showPaths = true;
        showPathsCurved = true;
    }

    @Override
    public void setSelectedPlaces(HashSet<Place> group, WorldCoordinate boxStart, WorldCoordinate boxEnd) {
        selectePlaces = group;
        placeSelectionBoxStart = boxStart;
        placeSelectionBoxEnd = boxEnd;
    }

    @Override
    public void selectPlaceAt(int x, int y) {
        placeSelectedX = x;
        placeSelectedY = y;
    }

    @Override
    public void setSelectionVisible(boolean b) {
        placeSelectionEnabled = b;
    }

    /**
     * Returns true, if a place is selected by group selection
     * @param place
     * @return
     */
    private boolean isSelected(Place place){
        if(place != null){
            if(placeSelectionBoxEnd != null && placeSelectionBoxStart != null
                && placeSelectionBoxEnd.getLayer() == place.getLayer().getId()){
                int x1 = (int) Math.round(placeSelectionBoxEnd.getX());
                int x2 = (int) Math.round(placeSelectionBoxStart.getX());
                int y1 = (int) Math.round(placeSelectionBoxEnd.getY());
                int y2 = (int) Math.round(placeSelectionBoxStart.getY());

                int xMin = Math.min(x1, x2);
                int xMax = Math.max(x1, x2);
                int yMin = Math.min(y1, y2);
                int yMax = Math.max(y1, y2);

                if(place.getX() >= xMin && place.getX() <= xMax
                    && place.getY() >= yMin && place.getY() <= yMax) return true;
            }
            if(selectePlaces != null && selectePlaces.contains(place)) return true;
        }
        return false;
    }

    /**
     * Gets the current tile border area size
     * @return area border width
     */
    private int getTileBorderWidth(){
        // with interpolation for smooth transition
        return (int) Math.round(TILE_BORDER_WIDTH * Math.min(1.0, Math.max(0.5, (double) (tileSize - 20) / 80)));
    }

    /**
     * Gets the radius of the exit circles / dots
     * @return
     */
    private int getExitCircleRadius(){
        return (int) Math.round(EXIT_RADIUS * Math.min(1.0, Math.max(0.5, (double) (tileSize - 20) / 80)));
    }

    /**
     * Gets the stroke width of the tile selection box
     * @return
     */
    private float getTileSelectionStrokeWidth(){
        return TILE_SELECTION_STROKE_WIDTH * (float) (1.0 + tileSize / 200.0);
    }

    /**
     * Gets the stroke width of the risk level border
     * @return
     */
    private float getRiskLevelStrokeWidth(){
        return TILE_RISK_LEVEL_STROKE_WIDTH * (float) (1.0 + tileSize / 200.0);
    }

    /**
     * Gets the path stroke width
     * @return
     */
    private float getPathStrokeWidth(){
        return PATH_STROKE_WIDTH * (float) (1.0 + tileSize / 200.0);
    }

    public Font getTileFont(){
        return lastTileFont;
    }

    public Boolean getShowPaths() {
        return showPaths;
    }

    public void setShowPaths(Boolean showPaths) {
        this.showPaths = showPaths;
    }

    /**
     * Returns true if curved path lines are enabled
     * @return
     */
    public boolean getPathsCurved(){
        return showPathsCurved;
    }

    /**
     * Enables or disables curved path lines
     * @param showPathsCurved
     */
    public void setPathsCurved(boolean showPathsCurved){
        this.showPathsCurved = showPathsCurved;
    }

    /**
     * Calculates the offset of the exit visualization (dot/circle) to the
     * upper left corner of a tile
     * @param dir eit direction
     * @return false if the dot/circle doesn't have to be drawn
     */
    private Pair<Integer, Integer> getExitOffset(String dir){
        Pair<Integer, Integer> ret = new Pair<>(0, 0);
        int borderWidth = getTileBorderWidth();
        switch (dir) {
            case "n":
                // north
                ret.first = tileSize / 2;
                ret.second = borderWidth;
                break;
            case "e":
                // east
                ret.first = tileSize - borderWidth;
                ret.second = tileSize / 2;
                break;
            case "s":
                // south
                ret.first = tileSize / 2;
                ret.second = tileSize - borderWidth;
                break;
            case "w":
                // west
                ret.first = borderWidth;
                ret.second = tileSize / 2;
                break;
            case "ne":
                // north-east
                ret.first = tileSize - borderWidth;
                ret.second = borderWidth;
                break;
            case "se":
                // south-east
                ret.first = ret.second = tileSize - borderWidth;
                break;
            case "nw":
                // north-west
                ret.first = ret.second = borderWidth;
                break;
            case "sw":
                // south-west
                ret.first = borderWidth;
                ret.second = tileSize - borderWidth;
                break;
            default:
                ret.first = ret.second = tileSize / 2;
                break;
        }
        return ret;
    }

    /**
    * Gets the normal vector of an exit
    * @param dir exit direction
    * @return normal vector
    */
    private Pair<Double, Double> getExitNormal(String dir){
        Pair<Double, Double> ret = new Pair<>(0.0, 0.0);
        switch (dir) {
            case "n":
                ret.first = 0.0;
                ret.second = 1.0;
                break;
            case "e":
                ret.first = 1.0;
                ret.second = 0.0;
                break;
            case "s":
                ret.first = 0.0;
                ret.second = -1.0;
                break;
            case "w":
                ret.first = -1.0;
                ret.second = 0.0;
                break;
            case "ne":
                ret.first = 1.0;
                ret.second = 1.0;
                break;
            case "se":
                ret.first = 1.0;
                ret.second = -1.0;
                break;
            case "nw":
                ret.first = -1.0;
                ret.second = 1.0;
                break;
            case "sw":
                ret.first = -1.0;
                ret.second = -1.0;
                break;
        }
        // normalize it
        if(ret.first != 0.0 && ret.second != 0.0){
            double length = Math.sqrt(ret.first * ret.first + ret.second * ret.second);
            ret.first /= length;
            ret.second /= length;
        }
        return ret;
    }

    /**
     * fits the string to max_width in px, cuts it at whitespaces if possible
     * @param str string to be fitted
     * @param fm fontmetrics
     * @param maxLength maximum length of the string in pixel
     * @param maxLines maximum number of lines
     * @return a list of strings
     */
    private LinkedList<String> fitLineLength(String str, FontMetrics fm, int maxLength, int maxLines){
        LinkedList<String> ret;

        if(maxLines == 0) return new LinkedList<>();

        if(fm.stringWidth(str) <= maxLength){ // string isn't too long, return it
            ret = new LinkedList<>();
            ret.add(str);
        } else { // string is too long
            // roughly fit the string
            int strlen = Math.min(str.length(), maxLength / fm.charWidth('.'));

            // find last ' ' before maxLength, if there is no ' ' cut the
            // string at maxLength
            while(fm.stringWidth(str.substring(0, strlen)) > maxLength){
                // remove last word
                int whitespace = str.substring(0, strlen).lastIndexOf(' ');
                // if a whitespace is found: cut the string
                if(whitespace != -1){
                    strlen = whitespace;
                } else {
                    --strlen;
                }
            }

            // cut the next part and return it, abbreviate the string if the max line number is reached
            if(maxLines > 0){
                ret = fitLineLength(str.substring(strlen).trim(), fm, maxLength, maxLines - 1);
                ret.addFirst(str.substring(0, strlen));
            } else {
                ret = new LinkedList<>();
                if(strlen > 3) ret.add(str.substring(0, strlen - 3) + "...");
                else ret.add("...");
            }
        }
        return ret;
    }

    /**
     * Converts world coordinates to screen coordinates
     * @param placeX a world (place) coordinate (x axis)
     * @return a screen coordinate x
     */
    private int getScreenPosX(int placeX){
        double screenCenterX = (graphicsWidth / tileSize) / 2; // note: wdtwd2
        int placeXOffset = (int) (Math.round(curPos.getX()) - Math.round(screenCenterX));
        return (int)((placeX - placeXOffset + remint(screenCenterX) - remint(curPos.getX())) * tileSize);
    }

    /**
     * Converts world coordinates to screen coordinates
     * @param placeY a world (place) coordinate (y axis)
     * @return a screen coordinate y
     */
    private int getScreenPosY(int placeY){
        double screenCenterY = (graphicsHeight / tileSize) / 2;
        int placeYOffset = (int) (Math.round(curPos.getY()) - Math.round(screenCenterY));
        return (int)((-placeY + placeYOffset - remint(screenCenterY) + remint(curPos.getY())) * tileSize + graphicsHeight);
    }

    /**
     * Checks whether a place is currently drawn on the screen
     * @param place
     * @return
     */
    private boolean isOnScreen(Place place){
        int x = getScreenPosX(place.getX());
        if(x < 0 || x > graphicsWidth) return false;

        int y = getScreenPosY(place.getY());
        /*
        if(y < 0 || y > graphicsHeight) return false;
        else return true;*/
        return !(y < 0 || y > graphicsHeight);
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
     *
     * @param g map graphics
     * @param col stroke color
     * @param pxpx place x coordinate in pixel
     * @param pypx place y coordinate in pixel
     * @param ssw selectionStrokeWidth
     */
    private void drawCursor(Graphics g, Color col, Integer pxpx, Integer pypx, Float ssw){
        g.setColor(col);
        ((Graphics2D)g).setStroke(new BasicStroke((ssw)));

        // precalculation
        final float sswts = ssw + tileSize;
        final float pxpssw = pxpx + ssw;
        final float pxmsswts = pxpx - ssw + tileSize;//Math.round(pxpx - sswts);
        final float pypssw = pypx + ssw;
        final float pymsswts = pypx - ssw + tileSize; //Math.round(pypx - sswts);
        final float sswtsd4 = ssw + tileSize / 4.0f;
        final float sswtsd4m3 = -ssw + tileSize / 4.0f * 3.0f;

        drawLine(g, pxpssw, pypssw, pxpssw, pypx + sswtsd4);
        drawLine(g, pxpssw, pypssw, pxpx + Math.round(sswtsd4), pypssw);

        drawLine(g, pxmsswts, pypssw, pxmsswts, pypx + sswtsd4);
        drawLine(g, pxmsswts, pypssw, pxpx + sswtsd4m3, pypssw);

        drawLine(g, pxpssw, pymsswts, pxpssw, pypx + sswtsd4m3);
        drawLine(g, pxpssw, pymsswts, pxpx + sswtsd4, pymsswts);

        drawLine(g, pxmsswts, pymsswts, pxmsswts, pypx + sswtsd4m3);
        drawLine(g, pxmsswts, pymsswts, pxpx + sswtsd4m3, pymsswts);
    }

    private void drawLine(Graphics g, float a, float b, float c, float d){
        g.drawLine(Math.round(a), Math.round(b), Math.round(c), Math.round(d));
    }

    @Override
    public void paint(Graphics g, int tileSize, double graphicsWidth, double graphicsHeight, Layer layer, WorldCoordinate curPos) {
        this.graphicsWidth = graphicsWidth;
        this.graphicsHeight = graphicsHeight;
        this.tileSize = tileSize;
        this.curPos = curPos;

        lastTileFont = g.getFont();

        final float selectionStrokeWidth = getTileSelectionStrokeWidth();
        final int tileBorderWidthScaled = getTileBorderWidth();

        // max number of text lines tht fit in a tile
        FontMetrics fm = g.getFontMetrics();
        final int maxLines = (int) Math.round((double)(tileSize - 3 * (tileBorderWidthScaled + (int) Math.ceil(getRiskLevelStrokeWidth()))) / fm.getHeight());
        final int maxLineLength = tileSize - 2 * (tileBorderWidthScaled + (int) selectionStrokeWidth + (int) Math.ceil(getRiskLevelStrokeWidth()));
        final Boolean drawText = fm.stringWidth("WW") < (tileSize - 2 * (getRiskLevelStrokeWidth() + getTileBorderWidth()));

        // screen center in world coordinates
        final double screenCenterX = (graphicsWidth / tileSize) / 2.0; // note: wdtwd2
        final double screenCenterY = (graphicsHeight / tileSize) / 2.0;

        final int placeXOffset = (int) (Math.round((float) curPos.getX()) - Math.round(screenCenterX));
        final int placeYOffset = (int) (Math.round((float) curPos.getY()) - Math.floor(screenCenterY));

        // more precalculation
        final double placeXpxConst = remint(screenCenterX) - remint(curPos.getX());
        final double placeYPXConst = remint(screenCenterY) + remint(curPos.getY());

        // prepare graphic for paths
        // Paths will be drawn on this graphic and later on copied to g
        ArrayList<Pair<Integer, Integer>> tilePositions = new ArrayList<>(); // to mask out the tile positions on graphic_path
        BufferedImage imagePath = new BufferedImage((int) graphicsWidth, (int) graphicsHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics graphicPath = imagePath.getGraphics();
        ((Graphics2D) graphicPath).setStroke(new BasicStroke(getPathStrokeWidth()));
        ((Graphics2D) graphicPath).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // getPlace the locations of copied places
        HashSet<Pair<Integer, Integer>> copiedPlaceLocations = mudmap2.CopyPaste.getCopyPlaceLocations();

        // clear screen
        g.clearRect(0, 0, (int) graphicsWidth + 1, (int) graphicsHeight + 1);

        // ------------------ draw the tiles / places ----------------------
        for(int tileX = (g.getClipBounds().x / tileSize) - 1; tileX < graphicsWidth / tileSize + 1; ++tileX){
            for(int tileY = (g.getClipBounds().y / tileSize) - 1; tileY < graphicsHeight / tileSize + 1; ++tileY){

                // place position on the map
                final int placeX = tileX + placeXOffset;
                final int placeY = (int)(graphicsHeight / tileSize) - tileY + placeYOffset;

                if(layer != null && layer.exist(placeX, placeY)){
                    Place curPlace = layer.get(placeX, placeY);

                    // place position in pixel on the screen
                    final int placeXpx = (int) Math.round((tileX + placeXpxConst) * tileSize);
                    final int placeYpx = (int) Math.round((tileY + placeYPXConst) * tileSize);

                    tilePositions.add(new Pair<>(placeXpx, placeYpx));

                    // number of drawn text lines
                    int lineNum = 0;

                    // draw area color
                    if(curPlace.getArea() != null){
                        g.setColor(curPlace.getArea().getColor());
                        g.fillRect(placeXpx, placeYpx, tileSize, tileSize);
                    }

                    // draw tile center color
                    if(drawText){
                        g.setColor(layer.getWorld().getTileCenterColor());
                        g.fillRect(placeXpx + tileBorderWidthScaled, placeYpx + tileBorderWidthScaled,
                                tileSize - 2 * tileBorderWidthScaled, tileSize - 2 * tileBorderWidthScaled);
                    }

                    // draw risk level border
                    if(curPlace.getRiskLevel() != null){
                        g.setColor(curPlace.getRiskLevel().getColor());
                        ((Graphics2D)g).setStroke(new BasicStroke(getRiskLevelStrokeWidth()));
                        g.drawRect(placeXpx + tileBorderWidthScaled, placeYpx + tileBorderWidthScaled,
                                tileSize - 2 * tileBorderWidthScaled - (int) (0.5 * getRiskLevelStrokeWidth()),
                                tileSize - 2 * tileBorderWidthScaled - (int) (0.5 * getRiskLevelStrokeWidth()));
                    }

                    // draw text, if tiles are large enough
                    if(drawText){
                        g.setColor(Color.BLACK);

                        // place name
                        // gets place name if unique, else place name with ID
                        String placeName = ((layer.getWorld().isPlaceNameUnique(curPlace.getName()) && layer.getWorld().getShowPlaceId() == World.ShowPlaceID.UNIQUE) || layer.getWorld().getShowPlaceId() == World.ShowPlaceID.NONE)
                                                ? curPlace.getName() : curPlace.toString();
                        LinkedList<String> line = fitLineLength(placeName, fm, maxLineLength, maxLines);
                        for(String str: line){
                            g.drawString(str,
                                    placeXpx + tileBorderWidthScaled + (int) selectionStrokeWidth + (int) Math.ceil(getRiskLevelStrokeWidth()),
                                    placeYpx + tileBorderWidthScaled + fm.getHeight() * (1 + lineNum));
                            lineNum++;
                        }

                        if(lineNum < maxLines){ // it isn't unusual for some places to fill up all the lines
                            // recommended level
                            int reclvlmin = curPlace.getRecLevelMin(), reclvlmax = curPlace.getRecLevelMax();
                            if(reclvlmin > -1 || reclvlmax > -1){
                                g.drawString("lvl " + (reclvlmin > -1 ? reclvlmin : "?") + " - " + (reclvlmax > -1 ? reclvlmax : "?"),
                                        placeXpx + tileBorderWidthScaled + (int) selectionStrokeWidth + (int) Math.ceil(getRiskLevelStrokeWidth()),
                                        placeYpx + tileBorderWidthScaled + fm.getHeight() * (1 + lineNum));
                                lineNum++;
                            }

                            // sub areas / parents
                            if(lineNum < maxLines && !curPlace.getParents().isEmpty()){
                                int parentsNum = curPlace.getParents().size();
                                String chStr = "Pa" + (parentsNum > 1 ? " (" + curPlace.getParents().size() + "): " : ": ");

                                boolean firstParent = true;
                                for(Place parent: curPlace.getParents()){
                                    chStr += (firstParent ? "" : ", ") + parent.getName();
                                    firstParent = false;
                                }
                                line = fitLineLength(chStr, fm, maxLineLength, maxLines - lineNum);
                                for(String str: line){
                                    g.drawString(str,
                                            placeXpx + tileBorderWidthScaled + (int) selectionStrokeWidth + (int) Math.ceil(getRiskLevelStrokeWidth()),
                                            placeYpx + tileBorderWidthScaled + fm.getHeight() * (1 + lineNum));
                                    lineNum++;
                                }
                            }

                            // sub areas / children
                            if(lineNum < maxLines && !curPlace.getChildren().isEmpty()){
                                int childrenNum = curPlace.getChildren().size();
                                String chStr = "Ch" + (childrenNum > 1 ? " (" + curPlace.getChildren().size() + "): " : ": ");

                                boolean firstChild = true;
                                for(Place child: curPlace.getChildren()){
                                    chStr += (firstChild ? "" : ", ") + child.getName();
                                    firstChild = false;
                                }
                                line = fitLineLength(chStr, fm, maxLineLength, maxLines - lineNum);
                                for(String str: line){
                                    g.drawString(str,
                                            placeXpx + tileBorderWidthScaled + (int) selectionStrokeWidth + (int) Math.ceil(getRiskLevelStrokeWidth()),
                                            placeYpx + tileBorderWidthScaled + fm.getHeight() * (1 + lineNum));
                                    lineNum++;
                                }
                            }

                            // flags
                            if(lineNum < maxLines){
                                String flags = "";
                                // place has comments
                                if(!curPlace.getComments().isEmpty()) flags += "Co";
                                if(!curPlace.getChildren().isEmpty()) flags += "Ch";
                                if(!curPlace.getParents().isEmpty()) flags += "Pa";

                                // other flags
                                for(Map.Entry<String, Boolean> flag: curPlace.getFlags().entrySet()){
                                    if(flag.getValue()) flags += flag.getKey().toUpperCase();
                                    if(fm.stringWidth(flags) >= tileSize - 2 * tileBorderWidthScaled) break;
                                }

                                // draw flags
                                g.drawString(flags,
                                        placeXpx + tileBorderWidthScaled + (int) Math.ceil(2 * selectionStrokeWidth),
                                        placeYpx + tileSize - tileBorderWidthScaled - (int) Math.ceil(2 * selectionStrokeWidth));
                            }
                        }
                    }

                    // mark place group selection
                    if(isSelected(curPlace) || (mudmap2.CopyPaste.isCut() && mudmap2.CopyPaste.isMarked(curPlace))){
                        g.setColor(new Color(255, 255, 255, 128));
                        g.fillRect(placeXpx, placeYpx, tileSize, tileSize);
                    }

                    // draw path lines here
                    boolean exitUp = false, exitDown = false, exitnstd = false;
                    if(getShowPaths()){
                        for(Path path: curPlace.getPaths()){
                            Place otherPlace = path.getOtherPlace(curPlace);

                            Color colorPlace1 = layer.getWorld().getPathColor(path.getExitDirections()[0]);
                            Color colorPlace2 = layer.getWorld().getPathColor(path.getExitDirections()[1]);
                            if(path.getPlaces()[0] != curPlace) {
                                Color tmp = colorPlace1;
                                colorPlace1 = colorPlace2;
                                colorPlace2 = tmp;
                            }

                            // if both places of a path are on the same layer and at least one of the two places is on the screen
                            // usually the main place (path.getPlaces()[0]) draws the path. If it isn't on screen, the other place draws it
                            if(Objects.equals(otherPlace.getLayer().getId(), layer.getId()) && (path.getPlaces()[0] == curPlace || !isOnScreen(otherPlace))){
                                Pair<Integer, Integer> exitOffset = getExitOffset(path.getExit(curPlace));
                                Pair<Integer, Integer> exitOffsetOther = getExitOffset(path.getExit(otherPlace));

                                boolean drawCurves = getPathsCurved();

                                // exit positions on the map
                                final double exit1x = placeXpx + exitOffset.first;
                                final double exit1y = placeYpx + exitOffset.second;
                                final double exit2x = placeXpx + (otherPlace.getX() - curPlace.getX()) * tileSize + exitOffsetOther.first;
                                final double exit2y = placeYpx - (otherPlace.getY() - curPlace.getY()) * tileSize + exitOffsetOther.second;

                                if(colorPlace1.equals(colorPlace2)){ // same color
                                    ((Graphics2D) graphicPath).setPaint(colorPlace1);
                                } else { // draw gradient
                                    GradientPaint gp = new GradientPaint((float) exit1x, (float) exit1y, colorPlace1,
                                                                         (float) exit2x, (float) exit2y, colorPlace2);
                                    ((Graphics2D) graphicPath).setPaint(gp);
                                }

                                if(drawCurves){
                                    Pair<Double, Double> normal1 = getExitNormal(path.getExit(curPlace));
                                    Pair<Double, Double> normal2 = getExitNormal(path.getExit(otherPlace));

                                    double dx = exit2x - exit1x;
                                    double dy = exit2y - exit1y;

                                    if(drawCurves = Math.sqrt(dx * dx + dy * dy) >= 1.5 * tileSize){
                                        CubicCurve2D c = new CubicCurve2D.Double();
                                        c.setCurve(// point 1
                                                exit1x, exit1y,
                                                // point 2
                                                exit1x + normal1.first * tileSize, exit1y - normal1.second * tileSize,
                                                // point 3
                                                exit2x + normal2.first * tileSize, exit2y - normal2.second * tileSize,
                                                // point 4
                                                exit2x, exit2y);
                                        ((Graphics2D) graphicPath).draw(c);
                                    }
                                }

                                if(!drawCurves) {
                                    graphicPath.drawLine((int) exit1x, (int) exit1y, (int) exit2x, (int) exit2y);
                                }
                            }

                            // draw exit dots, if tiles are larger than 20
                            if(tileSize >= 20){
                                g.setColor(colorPlace1);
                                String exit = path.getExit(curPlace);
                                switch (exit) {
                                    case "u":
                                        exitUp = true;
                                        break;
                                    case "d":
                                        exitDown = true;
                                        break;
                                    default:
                                        Pair<Integer, Integer> exitOffset = getExitOffset(exit);
                                        if(exitOffset.first != tileSize / 2 || exitOffset.second != tileSize / 2){
                                            int exitCircleRadius2 = getExitCircleRadius();
                                            g.fillOval(placeXpx + exitOffset.first - exitCircleRadius2, placeYpx + exitOffset.second - exitCircleRadius2, 2 * exitCircleRadius2, 2 * exitCircleRadius2);
                                        } else { // non-standard exit
                                            exitnstd = true;
                                        }
                                        break;
                                }
                            }
                        }
                    }

                    // draw exits
                    if(tileSize >= 20){
                        // the up / down flags have to be drawn after the
                        // exits to know whether they have to be drawn
                        if((exitUp || exitDown) && drawText && lineNum <= maxLines){
                            g.setColor(Color.BLACK);
                            // have some arrows: ⬆⬇ ↑↓
                            String updownstr = "" + (exitnstd ? "+" : "") + (exitUp ? "↑" : "") + (exitDown ? "↓" : "");

                            Font orig = g.getFont();
                            // derive font: increase font size and decrease character spacing
                            Map<TextAttribute, Object> attributes = new HashMap<>();
                            attributes.put(TextAttribute.SIZE, 17);
                            attributes.put(TextAttribute.TRACKING, 0.0);
                            g.setFont(orig.deriveFont(attributes));

                            g.drawString(updownstr,
                                    placeXpx + tileSize - tileBorderWidthScaled - g.getFontMetrics().stringWidth(updownstr) - (int) Math.ceil(selectionStrokeWidth),
                                    placeYpx + tileSize - tileBorderWidthScaled - (int) Math.ceil(selectionStrokeWidth));

                            g.setFont(orig);
                        }
                    }
                }

                //TODO: extract from parent loop
                if(copiedPlaceLocations != null){
                    boolean locationFound = false;
                    for(Pair<Integer, Integer> location: copiedPlaceLocations){

                        if(location.first == placeX - placeSelectedX && location.second == placeY - placeSelectedY){
                            locationFound = true;
                            break;
                        }
                    }

                    if(locationFound){
                        int placeXpx = (int)((tileX + remint(screenCenterX) - remint(curPos.getX())) * tileSize); // alternative: getScreenPosX();
                        int placeYpx = (int)((tileY + remint(screenCenterY) + remint(curPos.getY())) * tileSize);

                        drawCursor(g, Color.BLUE, placeXpx, placeYpx, selectionStrokeWidth);
                    }
                }

                // draw cursor / place selection
                if(placeSelectionEnabled && placeX == placeSelectedX && placeY == placeSelectedY){
                    int placeXpx = (int)((tileX + remint(screenCenterX) - remint(curPos.getX())) * tileSize); // alternative: getScreenPosX();
                    int placeYpx = (int)((tileY + remint(screenCenterY) + remint(curPos.getY())) * tileSize);

                    drawCursor(g, TILE_SELECTION_COLOR, placeXpx, placeYpx, selectionStrokeWidth);
                }
            }
        }

        // mask out tile positions on graphicPath
        ((Graphics2D) graphicPath).setBackground(new Color(0,0,0,0));
        int clearTileSize = tileSize - 2 * tileBorderWidthScaled;
        for(Pair<Integer, Integer> p: tilePositions)
            //graphicPath.clearRect(p.first, p.second, p.first + tileSize, p.second + tileSize);
            graphicPath.clearRect(p.first + tileBorderWidthScaled, p.second + tileBorderWidthScaled, clearTileSize, clearTileSize);

        // draw graphicPath to g
        if(getShowPaths()) g.drawImage(imagePath, 0, 0, null);
        graphicPath.dispose();
    }

}
