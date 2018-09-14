package mudmap2.frontend.GUIElement.WorldPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mudmap2.backend.Path;
import mudmap2.backend.Place;

/**
 * Connects a new path, if called
 */
public class ConnectPathActionListener implements ActionListener {

    final Place pl1;
    final Place pl2;
    final String dir1;
    final String dir2;

    public ConnectPathActionListener(final Place pl1, final Place pl2, final String dir1, final String dir2) {
        this.pl1 = pl1;
        this.pl2 = pl2;
        this.dir1 = dir1;
        this.dir2 = dir2;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        pl1.connectPath(new Path(pl1, dir1, pl2, dir2));
    }
}