package com.google.sps;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;




public final class SortedMarkers {
    private final Collection<EventMarker> sortedMarkers = new ArrayList<>();
    private final ArrayList<EventMarker> al = new ArrayList<EventMarker>();

    public SortedMarkers(Collection<EventMarker> calEvents) {
        //Gson g = new Gson(); 
        //TypeToken<ArrayList<Event>> token = new TypeToken<ArrayList<Event>>() {};
        //ArrayList<Animal> calEvents = gson.fromJson(jsonString, token.getType());
        //System.out.println(calEvents(0)); 
        sortedMarkers.addAll(calEvents);
        al.addAll(calEvents); 
        sortByTime(al); 
    }

    public Collection<EventMarker> getSortedMarkers() {
        return Collections.unmodifiableCollection(al);
    }

    private void sortByTime(ArrayList<EventMarker> al) {
        Collections.sort(al, new Comparator<EventMarker>() {
            public int compare(EventMarker event1, EventMarker event2) {
                return Long.compare((event1.getDateTime().getValue()),(event2.getDateTime().getValue()));
            }
        });
    }
}
