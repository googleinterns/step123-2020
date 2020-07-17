package com.google.sps;

import com.google.common.collect.Iterables;
import com.google.api.client.util.DateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public final class SortedMarkersTest {
    private final int DATE_OFFSET = 1900; 

    @Test
    public void noEventsAreLost() {
        ArrayList<EventMarker> events = new ArrayList<EventMarker>(); 
        Date date = new Date(2020-DATE_OFFSET,7,17,12,32);
       
        EventMarker event = new EventMarker("name", "description", "location", new DateTime(date), "groupName");
        EventMarker event2 = new EventMarker("name2", "description", "location", new DateTime(date), "groupName2");
        EventMarker event3 = new EventMarker("name3", "description", "location", new DateTime(date), "groupName3");
        EventMarker event4 = new EventMarker("name4", "description", "location", new DateTime(date), "groupName4");

        events.add(event);
        events.add(event2);
        events.add(event3);
        events.add(event4);
        
        int expected = events.size(); 
        SortedMarkers sortedMarkers = new SortedMarkers(events); 
        Assert.assertEquals(expected, sortedMarkers.getSortedMarkers().size());
    }

    @Test
    public void eventsChangeToChronological() {
        ArrayList<EventMarker> events = new ArrayList<EventMarker>(); 
        Date date = new Date(2020-DATE_OFFSET,7,17,12,32);
        Date date2 = new Date(2020-DATE_OFFSET,7,16,12,32);
        EventMarker event = new EventMarker("thisIsOne", "description", "location", new DateTime(date), "groupName");
        EventMarker event2 = new EventMarker("thisIsTwo", "description", "location", new DateTime(date2), "groupName2");

        events.add(event);
        events.add(event2);
        
        SortedMarkers sortedMarkers = new SortedMarkers(events); 
        Assert.assertEquals(event2.getName(), (Iterables.get(sortedMarkers.getSortedMarkers(), 0)).getName());
    }

    @Test
    public void orderSameDateDifferentTime() {
        ArrayList<EventMarker> events = new ArrayList<EventMarker>(); 
        Date date = new Date(2020-DATE_OFFSET,7,17,18,32);
        Date date2 = new Date(2020-DATE_OFFSET,7,17,1,32);
        Date date3 = new Date(2020-DATE_OFFSET,6,16,12,30);
        EventMarker event = new EventMarker("thisIsOne", "description", "location", new DateTime(date), "groupName");
        EventMarker event2 = new EventMarker("thisIsTwo", "description", "location", new DateTime(date2), "groupName2");
        EventMarker event3 = new EventMarker("thisIsThree", "description", "location", new DateTime(date3), "groupName3");

        events.add(event);
        events.add(event2);
        events.add(event3);
        
        SortedMarkers sortedMarkers = new SortedMarkers(events); 

        Assert.assertEquals(event3.getName(), (Iterables.get(sortedMarkers.getSortedMarkers(), 0)).getName());
        Assert.assertEquals(event2.getName(), (Iterables.get(sortedMarkers.getSortedMarkers(), 1)).getName());
        Assert.assertEquals(event.getName(), (Iterables.get(sortedMarkers.getSortedMarkers(), 2)).getName());

    }

}
