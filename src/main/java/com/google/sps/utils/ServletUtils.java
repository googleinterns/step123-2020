package com.google.sps.utils;

import com.google.gson.Gson;
import java.lang.ClassLoader;
import javax.servlet.http.HttpServletRequest;

/**
 * Class contains methods and constants shared by the Servlets.
 */
public class ServletUtils {
  public static final Gson gson = new Gson();

  public static final String APPLICATION_NAME = "The Solidarity Initiative";
  public static final String DEFAULT_PARAM = "";
  public static final String GROUP_CONSTANT = "Group";
  

  /**
   * @return the request parameter, or DEFAULT_PARAM if the parameter
   *         was not specified by the client
   */
  public static String getParameter(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    
    return value == null ? DEFAULT_PARAM : value;
  }
}
