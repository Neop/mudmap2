package mudmap2.utils;

import java.util.Comparator;

import mudmap2.backend.Place;

public class PlaceYComparator implements Comparator<Place> {

    public static PlaceYComparator FORWARD = new PlaceYComparator(false);
    public static PlaceYComparator BACKWARD = new PlaceYComparator(true);
    private final boolean reverse;

    public PlaceYComparator(final boolean reverse) {
        this.reverse = reverse;
    }

    @Override
    public int compare(final Place o1, final Place o2) {
        return reverse ? Integer.compare(o2.getY(), o1.getY()) : Integer.compare(o1.getY(), o2.getY());
    }

}
