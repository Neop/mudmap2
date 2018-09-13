package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mudmap2.backend.Path;

/**
 * removes a path, if called
 */
public class RemovePathActionListener implements ActionListener {

    final Path path;

    public RemovePathActionListener(final Path path) {
        this.path = path;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        path.remove();
    }
}