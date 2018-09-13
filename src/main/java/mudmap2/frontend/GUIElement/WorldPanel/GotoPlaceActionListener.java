package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mudmap2.backend.Place;

/**
 * Moves the map to the place, if action is performed
 */
public class GotoPlaceActionListener implements ActionListener {
    final WorldPanel parent;
    final Place place;

    public GotoPlaceActionListener(final WorldPanel worldpanel, final Place place) {
        this.parent = worldpanel;
        this.place = place;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (place != null) {
            parent.pushPosition(place.getCoordinate());
        }
    }
}