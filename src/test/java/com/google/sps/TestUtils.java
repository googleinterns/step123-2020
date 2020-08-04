package com.google.sps;

import static com.google.sps.utils.StringConstants.*;
import static org.mockito.Mockito.verify;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.sps.utils.ServletUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.mockito.Mockito;

/**
 * Class contains shared functionality between testing classes.
 */
public final class TestUtils extends Mockito {
  protected static Entity createGroupEntity(DatastoreService datastore) {
    Entity groupEntity = new Entity(GROUP_KIND, TEST_GROUP_ID);
    
    groupEntity.setProperty(GROUP_ID_PROPERTY, TEST_GROUP_ID);
    groupEntity.setProperty(GROUP_NAME_PROPERTY, TEST_GROUP_NAME);
    groupEntity.setProperty(GROUP_IMAGE_PROPERTY, TEST_GROUP_IMAGE);
    groupEntity.setProperty(GROUP_DESCRIPTION_PROPERTY, TEST_GROUP_DESCRIPTION);
    groupEntity.setProperty(GROUP_CALENDARID_PROPERTY, TEST_GROUP_CALENDARID);
    
    datastore.put(groupEntity);

    return groupEntity;
  }

  protected static void verifyBadRequest(HttpServletResponse response, String expectedErrorMessage, StringWriter stringWriter) throws IOException {
    String actualErrorMessage = stringWriter.getBuffer().toString().trim();

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(response).setContentType(CONTENT_TYPE_PLAIN);

    Assert.assertEquals(expectedErrorMessage, actualErrorMessage);
  }
}
