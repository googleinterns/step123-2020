package com.google.sps;


import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import java.time.LocalDateTime;
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
public final class EventMarkerTest {

    private final int DATE_OFFSET = 1900; 

    @Test
    public void dateFormat() { 
        Date date = new Date(2020-DATE_OFFSET,0,17,12,32);
        EventMarker event = new EventMarker("name", "description", "location", new DateTime(date), "groupName");
        String expected = "Friday, January 17, 2020";
        Assert.assertEquals(expected, event.getDateString().substring(0,expected.length())); 
    }

    @Test
    public void dateTimeFormat() {
        Date date = new Date(2020-DATE_OFFSET,7,21,1,30);
        EventMarker event = new EventMarker("Rally at City Hall", 
            "Join us at San Jose City Hall to rally for the BLM Movement!", 
            "200 E Santa Clara St, San Jose, CA 95113", 
            new DateTime(date), 
            "Black Lives Matter");
        String expected = "Friday, August 21, 2020 01:30 AM";
        Assert.assertEquals(expected, event.getDateString());
    }

    @Test
    public void simpleDateFormat() {
        Date date = new Date(2020-DATE_OFFSET,1,17,18,30);
        EventMarker event = new EventMarker("Peaceful Protest at the Lake", 
            "We're having a peaceful protest at Almaden Lake, come and support!", 
            "6099 Winfield Blvd, San Jose, CA 95120", 
            new DateTime(date), 
            "Black Lives Matter");
        String expected = "2020-02-17";
        Assert.assertEquals(expected, event.getSimpleDate());
    }
}
