package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mudmap2.backend.Place;

/**
 * Removes a child from a place, if action performed
 */
public class RemoveChildrenActionListener implements ActionListener {

    final Place place;
    final Place child;

    public RemoveChildrenActionListener(final Place place, final Place child) {
        this.place = place;
        this.child = child;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (place != null && child != null) {
            place.removeChild(child);
        }
    }
}