package com.google.sps;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;




public final class SortedMarkers {
    private final ArrayList<EventMarker> sortedMarkers = new ArrayList<EventMarker>();

    public SortedMarkers(Collection<EventMarker> calEvents) {
        sortedMarkers.addAll(calEvents); 
        sortByTime(sortedMarkers); 
    }

    public Collection<EventMarker> getSortedMarkers() {
        return Collections.unmodifiableCollection(sortedMarkers);
    }

    private void sortByTime(ArrayList<EventMarker> eventMarkerList) {
        Collections.sort(eventMarkerList, new Comparator<EventMarker>() {
            public int compare(EventMarker event1, EventMarker event2) {
                return Long.compare((event1.getDateTime().getValue()),(event2.getDateTime().getValue()));
            }
        });
    }
}
