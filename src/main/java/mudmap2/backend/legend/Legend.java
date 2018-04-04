/*  MUD Map (v2) - A tool to create and organize maps for text-based games
 *  Copyright (C) 2018  Neop (email: mneop@web.de)
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
package mudmap2.backend.legend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import mudmap2.backend.Layer;
import mudmap2.backend.Place;
import mudmap2.backend.PlaceGroup;
import mudmap2.backend.RiskLevel;

/**
 * This class generates and renders layer legends for image export
 * @author neop
 */
public class Legend {

    public enum Orientation {
        HORIZONTAL, //< fixed width, grows vertically
        VERTICAL    //< fixed heigth, grows horizontally
    }

    public enum ExceptionReason {
        IMAGE_TOO_SMALL
    }

    public class RenderException extends Exception {
        ExceptionReason reason;

        private RenderException(ExceptionReason reason) {
            this.reason = reason;
        }

        public ExceptionReason getReason() {
            return reason;
        }
    }

    final static int ELEMENT_MARGIN_OUTSIDE         = 5;
    final static int ELEMENT_MARGIN_BETWEEN_HOR     = 5;
    final static int ELEMENT_MARGIN_BETWEEN_VERT    = 3;

    /**
     * Map layer
     */
    Layer layer;

    /**
     * Orientation of legend
     */
    Orientation orientation;

    /**
     * maximum width if Orientation.HORIZONTAL or maximum height if
     * Orientation.VERTICAL
     */
    int size; // TODO: remove?

    boolean includePathColors       = false;
    boolean includeRiskLevels       = false;
    boolean includePlaceGroups      = false;

    Color backgroundColor           = Color.LIGHT_GRAY;

    LinkedList<LegendEntry> legendEntries = null;

    public Legend(Layer layer, Orientation orientation, int size){
        this.layer = layer;
        this.orientation = orientation;
        this.size = size;
    }

    public boolean isIncludePathColors() {
        return includePathColors;
    }

    public void setIncludePathColors(boolean includePathColors) {
        this.includePathColors = includePathColors;
    }

    public boolean isIncludeRiskLevels() {
        return includeRiskLevels;
    }

    public void setIncludeRiskLevels(boolean includeRiskLevels) {
        this.includeRiskLevels = includeRiskLevels;
    }

    public boolean isIncludePlaceGroups() {
        return includePlaceGroups;
    }

    public void setIncludePlaceGroups(boolean includePlaceGroups) {
        this.includePlaceGroups = includePlaceGroups;
    }

    public Color getBackground() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color background) {
        this.backgroundColor = background;
    }

    public BufferedImage generate() throws RenderException {
        prepareLegendEntries();
        BufferedImage image = renderLegend();
        return image;
    }

    /**
     * Collects data from world to be included in the legend
     */
    private void prepareLegendEntries(){
        legendEntries = new LinkedList<>();

        if(isIncludePathColors()){
            prepareLegendEntriesPathColors();
        }
        if(isIncludePlaceGroups()){
            prepareLegendEntriesPlaceGroups();
        }
        if(isIncludeRiskLevels()){
            prepareLegendEntriesRiskLevels();
        }
    }

    /**
     * Collects path color entries
     */
    private void prepareLegendEntriesPathColors(){
        legendEntries.add(new LegendEntryTitle("Path Colors"));

        Color colCardinal = layer.getWorld().getPathColor();
        Color colNonCardinal = layer.getWorld().getPathColorNstd();

        legendEntries.add(new LegendEntryColor("cardinal direction", colCardinal));
        // only show non cardinal color if it differs from cardinal color
        if(!colCardinal.equals(colNonCardinal)){
            legendEntries.add(new LegendEntryColor("non cardinal", colNonCardinal));
        }

        for(Entry<String, Color> entry: layer.getWorld().getPathColors().entrySet()){
            legendEntries.add(new LegendEntryColor(entry.getKey(), entry.getValue()));
        }

        //legendEntries.add(new LegendEntrySeparator());
    }

    /**
     * Collects risk level entries
     */
    private void prepareLegendEntriesRiskLevels(){
        legendEntries.add(new LegendEntryTitle("Risk Levels"));

        for(RiskLevel riskLevel: layer.getWorld().getRiskLevels()){
            // check if risk level is in use on this layer
            boolean isInUse = false;
            for(Place place: layer.getPlaces()){
                if(place.getRiskLevel() == riskLevel){
                    isInUse = true;
                    break;
                }
            }

            if(isInUse){
                legendEntries.add(new LegendEntryColor(riskLevel.getDescription(), riskLevel.getColor()));
            }
        }

        //legendEntries.add(new LegendEntrySeparator());
    }

    /**
     * Collects place group entries
     */
    private void prepareLegendEntriesPlaceGroups(){
        legendEntries.add(new LegendEntryTitle("Place Groups"));

        for(PlaceGroup placeGroup: layer.getWorld().getPlaceGroups()){
            // check if group is in use on this layer
            boolean isInUse = false;
            for(Place place: layer.getPlaces()){
                if(place.getPlaceGroup() == placeGroup){
                    isInUse = true;
                    break;
                }
            }

            if(isInUse){
                legendEntries.add(new LegendEntryColor(placeGroup.getName(), placeGroup.getColor()));
            }
        }

        //legendEntries.add(new LegendEntrySeparator());
    }

    /**
     * Renders the prepared entries
     * @param g graphic to draw on
     */
    private BufferedImage renderLegend() throws RenderException {
        /// TODO: javadoc
        final int numEntries        = legendEntries.size();

        // get sizing (maximum width -> number of columns, maximum heigth -> graphics size)
        int maxElementWidth = 0;
        int maxElementHeight = 0;

        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics tempGraphics = tempImage.getGraphics();

        for(LegendEntry entry: legendEntries){
            maxElementWidth    = Math.max(maxElementWidth, entry.getWidth(tempGraphics));
            maxElementHeight   = Math.max(maxElementHeight, entry.getHeight(tempGraphics));
        }

        int numColumns;
        int numRows;
        int targetWidth;
        int targetHeight;

        if(orientation == Orientation.HORIZONTAL){
            numColumns = Math.floorDiv(size - 2*ELEMENT_MARGIN_OUTSIDE,
                    maxElementWidth + ELEMENT_MARGIN_BETWEEN_HOR);
            if(numColumns < 1) throw new RenderException(ExceptionReason.IMAGE_TOO_SMALL);
            numRows = (int) Math.ceil((double) numEntries / (double) numColumns);

            targetWidth = size;
            targetHeight = numRows * (maxElementHeight + ELEMENT_MARGIN_BETWEEN_VERT)
                    + 2*ELEMENT_MARGIN_OUTSIDE;
        } else { // Orientation.VERTICAL
            numRows = Math.floorDiv(size - 2*ELEMENT_MARGIN_OUTSIDE,
                    maxElementHeight + ELEMENT_MARGIN_BETWEEN_VERT);
            if(numRows < 1) throw new RenderException(ExceptionReason.IMAGE_TOO_SMALL);
            numColumns = (int) Math.ceil((double) numEntries / (double) numRows);

            targetWidth = numColumns * (maxElementWidth + ELEMENT_MARGIN_BETWEEN_HOR)
                    + 2*ELEMENT_MARGIN_OUTSIDE;
            targetHeight = size;
        }


        // create temporary graphics
        BufferedImage image = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // draw background
        ((Graphics2D) graphics).setBackground(backgroundColor);
        graphics.clearRect(0, 0, targetWidth, targetHeight);

        // render entries
        ListIterator<LegendEntry> iterator = legendEntries.listIterator();
        int curColumn = 0;
        int curY = ELEMENT_MARGIN_OUTSIDE;
        int curX = ELEMENT_MARGIN_OUTSIDE;

        while(iterator.hasNext()){
            LegendEntry entry = iterator.next();

            // don't draw separator if it is the last entry
            if(entry instanceof LegendEntrySeparator && !iterator.hasNext()) break;

            final int widthRequest = entry.getWidth(graphics);
            final int heightRequest = entry.getHeight(graphics);

            entry.renderGraphic(graphics, curX, curY, maxElementWidth, heightRequest);

            // move coordinates for next element
            curY += heightRequest + ELEMENT_MARGIN_BETWEEN_VERT;
            if(curY + maxElementHeight > targetHeight){
                curY = ELEMENT_MARGIN_OUTSIDE;
                curColumn++;
                curX = curColumn * (maxElementWidth + ELEMENT_MARGIN_BETWEEN_HOR)
                        + ELEMENT_MARGIN_OUTSIDE;
            }
        }

        return image;
    }
}
