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
  //3600000ms = 1 hour
  private final long TIME_OFFSET = 3600000L;
  private final String TEST_EVENT_DESCRIPTION = "test event description";
  private final String TEST_EVENT_HTML = "test event HTML";
  private final String TEST_EVENT_LOCATION = "test event location";
  private final String TEST_EVENT_TITLE = "test event title";
  
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private final DateTimeFormatter formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      .withZone(ZoneId.of(TIMEZONE));
  
  private String initialEventTime;
  private String endEventTime;


  private EventsServlet servlet;
  private DatastoreService datastore;
  private Entity groupEntity;
  private PrintWriter printWriter;
  private StringWriter stringWriter;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Spy
  EventsServlet servletSpy;

  @Before
  public void setUp() throws IOException {
    servlet = new EventsServlet();

    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);

    groupEntity = TestUtils.createGroupEntity(datastore);
    
    long initialEventTimeLong = System.currentTimeMillis();
    long endEventTimeLong = initialEventTimeLong + TIME_OFFSET;
    // Turn the times into a Rfc3339 string
    initialEventTime = formatter.format(new Date(initialEventTimeLong).toInstant());
    endEventTime = formatter.format(new Date(endEventTimeLong).toInstant());

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
  public void eventsGetWithGroupId() throws IOException, EntityNotFoundException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(TEST_GROUP_ID);

    servlet.doGet(request, response);

    String actualCalendarId = stringWriter.getBuffer().toString().trim();

    Assert.assertEquals(actualCalendarId, TEST_GROUP_CALENDARID);
    verify(response).setContentType(CONTENT_TYPE_PLAIN);
  }

  @Test
  public void eventsGetWithInvalidGroupId() throws IOException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(TEST_GROUP_ID_INVALID);

    servlet.doGet(request, response);

    String errorCodeActual = stringWriter.getBuffer().toString().trim();

    Assert.assertEquals(ENTITY_ERROR_MESSAGE, errorCodeActual);
  }

  @Test
  public void eventsGetWithNoIds() throws IOException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(null);
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
    when(request.getParameter(EVENT_END_PROPERTY)).thenReturn(endEventTime);
    
    // Create a stub event with the previous parameters
    Event expectedEvent = createTestEvent();

    // Return event inside the doPost method
    Mockito.doReturn(expectedEvent).when(servletSpy).addEvent(TEST_GROUP_CALENDARID, TEST_EVENT_TITLE, 
        TEST_EVENT_LOCATION, TEST_EVENT_DESCRIPTION, initialEventTime, endEventTime);

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
    when(request.getParameter(EVENT_END_PROPERTY)).thenReturn(endEventTime);
    
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
  public void createEvent() {
    Event event = servlet.createEvent(TEST_EVENT_TITLE, TEST_EVENT_LOCATION, 
        TEST_EVENT_DESCRIPTION, initialEventTime, endEventTime);

    Assert.assertEquals(TEST_EVENT_TITLE, event.getSummary());
    Assert.assertEquals(TEST_EVENT_LOCATION, event.getLocation());
    Assert.assertEquals(TEST_EVENT_DESCRIPTION, event.getDescription());
    Assert.assertEquals(initialEventTime, event.getStart().getDateTime().toStringRfc3339());
    Assert.assertEquals(endEventTime, event.getEnd().getDateTime().toStringRfc3339());
  }

  private Event createTestEvent() {
    return new Event().setSummary(TEST_EVENT_TITLE).setLocation(TEST_EVENT_LOCATION)
        .setDescription(TEST_EVENT_DESCRIPTION)
        .setStart(new EventDateTime().setDateTime(new DateTime(initialEventTime)).setTimeZone(TIMEZONE))
        .setEnd(new EventDateTime().setDateTime(new DateTime(endEventTime)).setTimeZone(TIMEZONE));
  }
}
