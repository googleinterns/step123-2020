package com.google.sps.utils;

/**
 * This class is used exclusively to store constants used
 * throughout the application.
 */
public final class StringConstants {
  /**
   * Application and file constants
   */
  public static final String APPLICATION_NAME = "The Solidarity Initiative";
  public static final String CALENDAR_SOY_FILE = "calendar.soy";
  public static final String CALENDAR_TEMPLATE_NAMESPACE = "templates.calendar.calendarPage";
  public static final String CHAT_ERROR_NAMESPACE = "templates.chat.error";
  public static final String CHAT_PAGE_NAMESPACE = "templates.chat.chatPage";
  public static final String CHAT_SOY_FILE = "chat.soy";
  public static final String CHAT_TEMPLATE_NAMESPACE = "templates.chat.";
  public static final String GROUPS_SOY_FILE = "groups.soy";
  public static final String GROUPS_TEMPLATE_NAMESPACE = "templates.groups.groupsPage";
  public static final String MAP_SOY_FILE = "mapPages.soy";
  public static final String MAP_TEMPLATE_NAMESPACE = "templates.mapPages.mapPage";
  public static final String TIMEZONE = "America/Los_Angeles";
  
  /**
   * Chat constants
   */
  public static final Double COMMENT_SCORE_THRESHOLD = 0.85;
  public static final String ATTRIBUTE_SCORES = "attributeScores";
  public static final String ERROR_MESSAGE_KEY = "errorMessage";
  public static final String ERROR_MESSAGE_TEXT = "Your message contains content that may " + 
      "be deemed offensive by others. Please revise your message and try again.";
  public static final String KEYS_TXT_FILE = "keys.txt";
  public static final String MESSAGES_KEY = "messages";
  public static final String MESSAGE_KIND = "Message-";
  public static final String MESSAGE_TEXT_PROPERTY = "message-text";
  public static final String REFERER_HEADER = "Referer";
  public static final String SUMMARY_SCORE = "summaryScore";
  public static final String TIMESTAMP_PROPERTY = "timestamp";
  public static final String VALUE = "value";
  
  /**
   * Bad request servlet error messages
   */
  public static final String INVALID_GROUPID_BAD_REQUEST_MESSAGE = "Must have groupId in query string!";
  public static final String EVENTS_GET_BAD_REQUEST_MESSAGE = "Must have calendarId or groupId in query string!";
  public static final String EVENTS_GET_INVALID_CALENDARID_MESSAGE = "Invalid credentials or calendar ID";
  public static final String EVENTS_POST_BAD_REQUEST_MESSAGE = "POST body must contain title and start and end times!";
  public static final String ENTITY_ERROR_MESSAGE = "Invalid group ID!";

  /**
   * Servlets' content type
   */
  public static final String CONTENT_TYPE_PLAIN = "text/plain";
  public static final String CONTENT_TYPE_HTML = "text/html";
  public static final String CONTENT_TYPE_JSON = "application/json";

  /**
   * Events constant properties
   */
  public static final String EVENT_DESCRIPTION_PROPERTY = "description";
  public static final String EVENT_END_PROPERTY = "end";
  public static final String EVENT_LOCATION_PROPERTY = "location";
  public static final String EVENT_START_PROPERTY = "start";
  public static final String EVENT_TITLE_PROPERTY = "title";
  
  /**
   * Group constants
   */
  public static final String GROUP_KIND = "Group";
  public static final String GROUPS_KEY = "groups";
  public static final String GROUP_CALENDARID_PROPERTY = "calendarId";
  public static final String GROUP_DESCRIPTION_PROPERTY = "description";
  public static final String GROUP_ID_PROPERTY = "groupId";
  public static final String GROUP_IMAGE_PROPERTY = "image";
  public static final String GROUP_NAME_PROPERTY = "name";
  public static final String USER_KIND = "User";
  public static final String USER_EMAIL_PROPERTY = "email";
  
  /**
   * Map constants
   */
  public static final String API_KEY_NAME = "key";
  public static final String GROUP_NAME_KEY = "groupName";

  /**
   * Testing constants
   */
  public static final String NULL_PARAMETER = "age";
  public static final String TEST_GROUP_ID = "535";
  public static final String TEST_GROUP_ID_INVALID = "600";
  public static final String TEST_GROUP_NAME = "test name";
  public static final String TEST_GROUP_IMAGE = "test image";
  public static final String TEST_GROUP_DESCRIPTION = "test description";
  public static final String TEST_GROUP_CALENDARID = "test calendar id";
}
