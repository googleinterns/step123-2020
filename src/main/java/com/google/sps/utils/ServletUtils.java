package com.google.sps.utils;

import static com.google.sps.utils.StringConstants.GROUP_KIND;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.ClassLoader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class contains methods and constants shared by the Servlets.
 */
public final class ServletUtils {
  public static final Gson gson = new Gson();

  public static final String DEFAULT_PARAM = "";
  
  /**
   * @return the request parameter, or DEFAULT_PARAM if the parameter
   *         was not specified by the client
   */
  public static String getParameter(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    
    return value == null ? DEFAULT_PARAM : value;
  }

  public static Entity getGroupEntity(String groupId) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      
    Key groupKey = KeyFactory.createKey(GROUP_KIND, groupId);

    return datastore.get(groupKey);
  }

  /**
   * If the property is invalid, will return null. 
   */
  public static String getGroupProperty(String groupId, String property) throws EntityNotFoundException {
    Entity groupEntity = getGroupEntity(groupId);

    return (String) groupEntity.getProperty(property);
  }
  
  public static void printBadRequestError(HttpServletResponse response, String errorMessage) throws IOException {
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.getWriter().println(errorMessage);
  }
}
