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
        DateTime dateT = new DateTime(date); 
        EventMarker event = new EventMarker("name", "description", "location", dateT, "groupName");
        String expected = "Friday, January 17, 2020";
        Assert.assertEquals(expected, event.getDateString().substring(0,expected.length())); 
    }

  
}
