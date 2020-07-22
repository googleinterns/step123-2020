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
import com.google.sps.TestUtils;
import com.google.sps.utils.ServletUtils;
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
public final class ServletUtilsTest extends Mockito {
  private final String TESTING_PARAMETER = "language";
  private final String TESTING_PARAMETER_VALUE = "english";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  
  private DatastoreService datastore;
  private Entity groupEntityExpected;
  private PrintWriter printWriter;
  private StringWriter stringWriter;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);

    groupEntityExpected = TestUtils.createGroupEntity(this.datastore);

    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void getParameterWithValue() throws IOException {
    // {language: english} (param: value)
    when(request.getParameter(TESTING_PARAMETER)).thenReturn(TESTING_PARAMETER_VALUE);

    String expected = ServletUtils.getParameter(request, TESTING_PARAMETER);

    Assert.assertEquals(expected, TESTING_PARAMETER_VALUE);
  }

  @Test
  public void getParameterWithNoValue() throws IOException { 
    // {age: } (param: value)
    when(request.getParameter(NULL_PARAMETER)).thenReturn(null);

    String expected = ServletUtils.getParameter(request, NULL_PARAMETER);

    Assert.assertEquals(expected, ServletUtils.DEFAULT_PARAM);
  }

  @Test
  public void getGroupEntityValidId(){
    try {
      Entity groupEntityActual = ServletUtils.getGroupEntity(TEST_GROUP_ID);

      Assert.assertEquals(groupEntityExpected, groupEntityActual);
    } catch(EntityNotFoundException error) {
      Assert.fail("EntityNotFoundException not expected.");
    }
  }

  @Test
  public void getGroupEntityInvalidId() {
    try {
      Entity groupEntityActual = ServletUtils.getGroupEntity(TEST_GROUP_ID_INVALID);

      Assert.fail("EntityNotFoundException expected");
    } catch(EntityNotFoundException expectedError) {
      Assert.assertNotNull(expectedError);
    }
  }

  @Test
  public void getGroupPropertyValidProperty() throws EntityNotFoundException {
    Assert.assertEquals(TEST_GROUP_NAME, ServletUtils.getGroupProperty(TEST_GROUP_ID, GROUP_NAME_PROPERTY));
    Assert.assertEquals(TEST_GROUP_IMAGE, ServletUtils.getGroupProperty(TEST_GROUP_ID, GROUP_IMAGE_PROPERTY));
    Assert.assertEquals(TEST_GROUP_DESCRIPTION, ServletUtils.getGroupProperty(TEST_GROUP_ID, GROUP_DESCRIPTION_PROPERTY));
    Assert.assertEquals(TEST_GROUP_CALENDARID, ServletUtils.getGroupProperty(TEST_GROUP_ID, GROUP_CALENDARID_PROPERTY));
  }

  @Test
  public void getGroupPropertyInvalidProperty() throws EntityNotFoundException {
    Assert.assertNull(ServletUtils.getGroupProperty(TEST_GROUP_ID, NULL_PARAMETER));
  }

  @Test
  public void printBadRequestError() throws IOException {
    String errorMessage = "test error message";
    when(response.getWriter()).thenReturn(printWriter);

    ServletUtils.printBadRequestError(response, errorMessage);
    
    TestUtils.verifyBadRequest(response, errorMessage, this.stringWriter);
  }
}
