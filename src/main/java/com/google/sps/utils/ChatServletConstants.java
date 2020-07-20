package com.google.sps.utils;

/**
 * This class is used exclusively to store constants used in the
 * ChatServlet, UpdateChatServlet, or ChatServletTest
 */
public final class ChatServletConstants {
    public static final Double COMMENT_SCORE_THRESHOLD = 0.85;

    public static final String ATTRIBUTE_SCORES = "attributeScores";
    public static final String ERROR_MESSAGE_KEY = "errorMessage";
    public static final String ERROR_MESSAGE_TEXT = "Your message contains content that may " + 
        "be deemed offensive by others. Please revise your message and try again.";
    public static final String GROUP_ID_PROPERTY = "groupID";
    public static final String GROUP_KIND = "Group";
    public static final String GROUP_NAME_PROPERTY = "name";
    public static final String GROUPS_KEY = "groups";
    public static final String MESSAGES_KEY = "messages";
    public static final String MESSAGE_KIND = "Message-";
    public static final String MESSAGE_TEXT_PROPERTY = "message-text";
    public static final String SUMMARY_SCORE = "summaryScore";
    public static final String TIMESTAMP_PROPERTY = "timestamp";
    public static final String VALUE = "value";
    
    private ChatServletConstants() {
    }
}
