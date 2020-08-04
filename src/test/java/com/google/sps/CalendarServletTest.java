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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.sps.servlets.CalendarServlet;
import com.google.sps.TestUtils;
import com.google.sps.utils.ServletUtils;
import com.google.sps.utils.SoyRendererUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.management.remote.JMXPrincipal;
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
public final class CalendarServletTest extends Mockito {
  private final static String TEST_POST_EXPECTED_CALENDARID = "mock calendar id";
  private final static String TEST_GET_USER_EMAIL = "email@example.com";
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
      .setEnvIsAdmin(true).setEnvIsLoggedIn(true);
  
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
  public void setUp() throws IOException {
    servlet = new CalendarServlet();

    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);

    groupEntity = TestUtils.createGroupEntity(datastore);

    MockitoAnnotations.initMocks(this);

    when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void calendarGetWithGroupIdHasCorrectSoyString() throws IOException {
    setUpCalendarGetWithGroupId();

    ImmutableList.Builder<ImmutableMap<String, String>> groupsListBuilder = new ImmutableList.Builder<>();
    ImmutableMap<String, String> groupMap = ImmutableMap.of(
        GROUP_NAME_PROPERTY, TEST_GROUP_NAME,
        GROUP_ID_PROPERTY, TEST_GROUP_ID,
        GROUP_DESCRIPTION_PROPERTY, TEST_GROUP_DESCRIPTION,
        GROUP_CALENDARID_PROPERTY, TEST_GROUP_CALENDARID,
        GROUP_IMAGE_PROPERTY, TEST_GROUP_IMAGE);
    groupsListBuilder.add(groupMap);

    ImmutableList<ImmutableMap<String, String>> testGroups = groupsListBuilder.build();

    String expectedHtml = SoyRendererUtils.getOutputString(CALENDAR_SOY_FILE, CALENDAR_TEMPLATE_NAMESPACE,
        ImmutableMap.of(GROUP_CALENDARID_PROPERTY, TEST_GROUP_CALENDARID, CURR_GROUP_KEY, TEST_GROUP_ID,
            GROUPS_KEY, testGroups, TIMEZONE_PARAM, TIMEZONE)); 
    String actualHtml = stringWriter.getBuffer().toString().trim();
    
    Assert.assertEquals(expectedHtml, actualHtml);
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
  
  @Test
  public void calendarPostWithValidGroupIdHasCorrectContentType() throws IOException, EntityNotFoundException {
    setUpCalendarPostWithGroupId(TEST_GROUP_ID);

    verify(response).setContentType(CONTENT_TYPE_PLAIN);
  }

  @Test
  public void calendarPostWithInvalidGroupId() throws IOException, EntityNotFoundException {
    setUpCalendarPostWithGroupId(TEST_GROUP_ID_INVALID);
    
    String errorCodeActual = stringWriter.getBuffer().toString().trim();

    Assert.assertEquals(ENTITY_ERROR_MESSAGE, errorCodeActual);
  }

  @Test
  public void calendarPostWithNoGroupId() throws IOException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(null);
    
    servlet.doPost(request, response);
    
    TestUtils.verifyBadRequest(response, INVALID_GROUPID_BAD_REQUEST_MESSAGE, this.stringWriter);
  }

  private void setUpCalendarGetWithGroupId() throws IOException {
    JMXPrincipal user = new JMXPrincipal(TEST_GET_USER_EMAIL);
    when(request.getUserPrincipal()).thenReturn(user);
    
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(TEST_GROUP_ID);
    
    servlet.doGet(request, response);
  }

  private void setUpCalendarPostWithGroupId(String groupId) throws IOException, EntityNotFoundException {
    when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(groupId);

    CalendarServlet servletSpy = Mockito.spy(servlet);
    Mockito.doReturn(TEST_POST_EXPECTED_CALENDARID).when(servletSpy).createCalendar(TEST_GROUP_ID);

    servletSpy.doPost(request, response);
  }
}
