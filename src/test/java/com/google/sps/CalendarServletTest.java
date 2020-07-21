package com.google.sps;

import static com.google.sps.utils.StringConstants.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.servlets.CalendarServlet;
import com.google.sps.TestUtils;
import com.google.sps.utils.ServletUtils;
import com.google.sps.utils.SoyRendererUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

@RunWith(JUnit4.class)
public final class CalendarServletTest extends Mockito {
  private final static String TEST_POST_EXPECTED_CALENDARID = "mock calendar id";
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  
  private CalendarServlet servlet;
  private DatastoreService datastore;
  private Entity groupEntity;
  private PrintWriter printWriter;
  private StringWriter stringWriter;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Before
  public void setUp() {
    servlet = new CalendarServlet();

    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);

    groupEntity = TestUtils.createGroupEntity(datastore);

    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void calendarGetWithGroupIdHasCorrectSoyString() throws IOException {
    setUpCalendarGetWithGroupId();

    String expectedHtml = SoyRendererUtils.getOutputString(CALENDAR_SOY_FILE, CALENDAR_TEMPLATE_NAMESPACE, null);
    String actualHtml = stringWriter.getBuffer().toString().trim();
    
    Assert.assertEquals(expectedHtml, actualHtml);
  }

  @Test
  public void calendarGetWithGroupIdHasCorrectContentType() throws IOException {
    setUpCalendarGetWithGroupId();

    verify(response).setContentType("text/html");
  }

  @Test
  public void calendarGetWithoutGroupId() throws IOException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(null);
    when(response.getWriter()).thenReturn(printWriter);
    
    servlet.doGet(request, response);
    
    TestUtils.verifyBadRequest(response, CALENDAR_BAD_REQUEST_MESSAGE, this.stringWriter);
  }

  @Test
  public void calendarPostWithValidGroupIdWritesCorrectCalendarId() throws IOException, EntityNotFoundException {
    setUpCalendarPostWithGroupId(TEST_GROUP_ID);

    String actualCalendarId = stringWriter.getBuffer().toString().trim();

    Assert.assertEquals(TEST_POST_EXPECTED_CALENDARID, actualCalendarId);
  }

  @Test
  public void calendarPostWithValidGroupIdSetsGroupCalendarId() throws IOException, EntityNotFoundException {
    setUpCalendarPostWithGroupId(TEST_GROUP_ID);
    
    Assert.assertEquals(TEST_POST_EXPECTED_CALENDARID, ServletUtils.getGroupProperty(TEST_GROUP_ID, GROUP_CALENDARID_PROPERTY));
  }

  public void calendarPostWithValidGroupIdHasCorrectContentType() throws IOException, EntityNotFoundException {
    setUpCalendarPostWithGroupId(TEST_GROUP_ID);

    verify(response).setContentType("text/plain");
  }

  @Test
  public void calendarPostWithInvalidGroupId() throws IOException, EntityNotFoundException {
    setUpCalendarPostWithGroupId(TEST_GROUP_ID_INVALID);
    
    String errorCodeActual = stringWriter.getBuffer().toString().trim();

    Assert.assertEquals(CALENDAR_ENTITY_ERROR_MESSAGE, errorCodeActual);
  }

  @Test
  public void calendarPostWithNoGroupId() throws IOException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(null);
    when(response.getWriter()).thenReturn(printWriter);
    
    servlet.doPost(request, response);
    
    TestUtils.verifyBadRequest(response, CALENDAR_BAD_REQUEST_MESSAGE, this.stringWriter);
  }

  private void setUpCalendarGetWithGroupId() throws IOException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(TEST_GROUP_ID);
    when(response.getWriter()).thenReturn(printWriter);
    
    servlet.doGet(request, response);
  }

  private void setUpCalendarPostWithGroupId(String groupId) throws IOException, EntityNotFoundException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(groupId);
    when(response.getWriter()).thenReturn(printWriter);

    CalendarServlet servletSpy = Mockito.spy(servlet);
    Mockito.doReturn(TEST_POST_EXPECTED_CALENDARID).when(servletSpy).createCalendar(TEST_GROUP_ID);

    servletSpy.doPost(request, response);
  }
}
