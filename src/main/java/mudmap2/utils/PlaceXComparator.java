package mudmap2.utils;

import java.util.Comparator;

import mudmap2.backend.Place;

public class PlaceXComparator implements Comparator<Place> {
    
    public static PlaceXComparator FORWARD = new PlaceXComparator(false);
    public static PlaceXComparator BACKWARD = new PlaceXComparator(true);
    private final boolean reverse;
    
    public PlaceXComparator(final boolean reverse) {
        this.reverse = reverse;
    }

    @Override
    public int compare(Place o1, Place o2) {
        return reverse ? Integer.compare(o2.getX(), o1.getX()) : Integer.compare(o1.getX(), o2.getX());
    }

}
