package com.google.sps;

import static com.google.sps.utils.StringConstants.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.servlets.EventsServlet;
import com.google.sps.TestUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@RunWith(JUnit4.class)
public final class EventsServletTest extends Mockito {
  // Number of milliseconds in an hour
  private final long TIME_OFFSET = 3600000L;
  // Formatter to convert minutes to Rfc3339
  private final String CREATE_TEST_EVENT_FORMATTER = ":00";
  private final String TEST_EVENT_DESCRIPTION = "test event description";
  private final String TEST_EVENT_HOURS = "1";
  private final String TEST_EVENT_HTML = "test event HTML";
  private final String TEST_EVENT_LOCATION = "test event location";
  private final String TEST_EVENT_MINUTES = "1";
  private final String TEST_EVENT_TITLE = "test event title";

  private final DateTimeFormatter formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm")
      .withZone(ZoneId.of(TIMEZONE));
  private final EventsServlet servlet = new EventsServlet();  
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private final StringWriter stringWriter = new StringWriter();
  
  private String initialEventTime;

  private DatastoreService datastore;
  private Entity groupEntity;
  private PrintWriter printWriter;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Spy
  EventsServlet servletSpy;

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    
    stringWriter.flush();
    printWriter = new PrintWriter(stringWriter);

    groupEntity = TestUtils.createGroupEntity(datastore);
    
    long initialEventTimeLong = System.currentTimeMillis();
    // Turn the time into a Rfc3339 string
    initialEventTime = formatter.format(new Date(initialEventTimeLong).toInstant());

    MockitoAnnotations.initMocks(this);
    
    when(response.getWriter()).thenReturn(printWriter);

    servletSpy = Mockito.spy(servlet);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void eventsGetWithCalendarId() throws IOException {
    when(request.getParameter(GROUP_CALENDARID_PROPERTY)).thenReturn(TEST_GROUP_CALENDARID);

    Events expectedEvents = new Events();

    List<Event> event = new ArrayList<>();
    event.add(createTestEvent());
    
    expectedEvents.setItems(event);

    Mockito.doReturn(expectedEvents).when(servletSpy).getEventsList(TEST_GROUP_CALENDARID);

    servletSpy.doGet(request, response);

    String actualEventsString = stringWriter.getBuffer().toString().trim();
    Assert.assertEquals(expectedEvents.toString(), actualEventsString);
    verify(response).setContentType(CONTENT_TYPE_JSON);
  }

  @Test
  public void eventsGetWithCalendarIdInvalidAuthentication() throws IOException {
    when(request.getParameter(GROUP_CALENDARID_PROPERTY)).thenReturn(TEST_GROUP_CALENDARID);

    Mockito.doThrow(IOException.class).when(servletSpy).getEventsList(TEST_GROUP_CALENDARID);
    
    servletSpy.doGet(request, response);

    String errorCodeActual = stringWriter.getBuffer().toString().trim();

    Assert.assertEquals(EVENTS_GET_INVALID_CALENDARID_MESSAGE, errorCodeActual);
  }
  
  @Test
  public void eventsGetWithNoIds() throws IOException {
    when(request.getParameter(GROUP_CALENDARID_PROPERTY)).thenReturn(null);
    
    servlet.doGet(request, response);
    
    TestUtils.verifyBadRequest(response, EVENTS_GET_BAD_REQUEST_MESSAGE, this.stringWriter);
  }
  
  @Test
  public void eventsPostWithValidQuery() throws IOException { 
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(TEST_GROUP_ID);
    when(request.getParameter(EVENT_TITLE_PROPERTY)).thenReturn(TEST_EVENT_TITLE);
    when(request.getParameter(EVENT_DESCRIPTION_PROPERTY)).thenReturn(TEST_EVENT_DESCRIPTION);
    when(request.getParameter(EVENT_LOCATION_PROPERTY)).thenReturn(TEST_EVENT_LOCATION);
    when(request.getParameter(EVENT_START_PROPERTY)).thenReturn(initialEventTime);
    when(request.getParameter(EVENT_HOURS_PROPERTY)).thenReturn(TEST_EVENT_HOURS);
    when(request.getParameter(EVENT_MINUTES_PROPERTY)).thenReturn(TEST_EVENT_MINUTES);
    
    // Create a stub event with the previous parameters
    Event expectedEvent = createTestEvent();

    // Return event inside the doPost method
    Mockito.doReturn(expectedEvent).when(servletSpy).addEvent(TEST_GROUP_CALENDARID, TEST_EVENT_TITLE, 
        TEST_EVENT_LOCATION, TEST_EVENT_DESCRIPTION, initialEventTime + TIMEZONE_OFFSET,
        servlet.getEventEndTime(initialEventTime, TEST_EVENT_HOURS, TEST_EVENT_MINUTES));

    servletSpy.doPost(request, response);

    String actualEventJson = stringWriter.getBuffer().toString().trim();

    Assert.assertEquals(expectedEvent.toString(), actualEventJson);
    verify(response).setContentType(CONTENT_TYPE_JSON);
  }

  @Test
  public void eventsPostWithInvalidGroupId() throws IOException{
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(TEST_GROUP_ID_INVALID);
    when(request.getParameter(EVENT_TITLE_PROPERTY)).thenReturn(TEST_EVENT_TITLE);
    when(request.getParameter(EVENT_START_PROPERTY)).thenReturn(initialEventTime);
    when(request.getParameter(EVENT_HOURS_PROPERTY)).thenReturn(TEST_EVENT_HOURS);
    when(request.getParameter(EVENT_MINUTES_PROPERTY)).thenReturn(TEST_EVENT_MINUTES);
    
    servlet.doPost(request, response);
    
    String errorCodeActual = stringWriter.getBuffer().toString().trim();
    Assert.assertEquals(ENTITY_ERROR_MESSAGE, errorCodeActual);
  }

  @Test
  public void eventsPostWithInvalidQuery() throws IOException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(null);
    
    servlet.doPost(request, response);
    
    TestUtils.verifyBadRequest(response, EVENTS_POST_BAD_REQUEST_MESSAGE, this.stringWriter);
  }

  @Test
  public void getEventEndTime() {
    String expectedDateTestOne = "2020-12-12T08:30" + TIMEZONE_OFFSET;
    Assert.assertEquals(expectedDateTestOne, servlet.getEventEndTime("2020-12-12T07:30", "1", "0"));

    String expectedDateTestTwo = "2020-11-11T13:01" + TIMEZONE_OFFSET;
    Assert.assertEquals(expectedDateTestTwo, servlet.getEventEndTime("2020-11-11T12:00", "1", "1"));

    String expectedDateTestThree = "2020-09-09T20:59" + TIMEZONE_OFFSET;
    Assert.assertEquals(expectedDateTestThree, servlet.getEventEndTime("2020-09-09T00:00", "20", "59"));

    String expectedDateTestFour = "2020-08-08T00:00" + TIMEZONE_OFFSET;
    Assert.assertEquals(expectedDateTestFour, servlet.getEventEndTime("2020-08-07T23:59", "0", "1"));
  }

  private Event createTestEvent() {
    return new Event().setSummary(TEST_EVENT_TITLE).setLocation(TEST_EVENT_LOCATION)
        .setDescription(TEST_EVENT_DESCRIPTION)
        .setStart(new EventDateTime().setDateTime(
        new DateTime(initialEventTime + CREATE_TEST_EVENT_FORMATTER)).setTimeZone(TIMEZONE));
  }
}
