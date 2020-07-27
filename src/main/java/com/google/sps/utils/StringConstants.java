package com.google.sps.utils;

/**
 * This class is used exclusively to store constants used
 * throughout the application.
 */
public final class StringConstants {
    public static final Double COMMENT_SCORE_THRESHOLD = 0.85;

    public static final String ATTRIBUTE_SCORES = "attributeScores";
    public static final String CHAT_ERROR_NAMESPACE = "templates.chat.error";
    public static final String CHAT_PAGE_NAMESPACE = "templates.chat.chatPage";
    public static final String CHAT_REDIRECT = "/chat?groupId=";
    public static final String CHAT_SOY_FILE = "chat.soy";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CURR_GROUP_KEY = "currGroup";
    public static final String ERROR_MESSAGE_KEY = "errorMessage";
    public static final String ERROR_MESSAGE_TEXT = "Your message contains content that may " + 
        "be deemed offensive by others. Please revise your message and try again.";
    public static final String GROUP_DESCRIPTION_PROPERTY = "description";
    public static final String GROUP_ID_PROPERTY = "groupId";
    public static final String GROUP_IMAGE_PROPERTY = "image";
    public static final String GROUP_KIND = "Group";
    public static final String GROUP_NAME_PROPERTY = "name";
    public static final String GROUPS_KEY = "groups";
    public static final String GROUPS_PAGE_NAMESPACE = "templates.groups.groupsPage";
    public static final String GROUPS_SOY_FILE = "groups.soy";
    public static final String KEYS_TXT_FILE = "keys.txt";
    public static final String MESSAGES_KEY = "messages";
    public static final String MESSAGE_KIND = "Message-";
    public static final String MESSAGE_TEXT_PROPERTY = "message-text";
    public static final String REFERER_HEADER = "Referer";
    public static final String SUMMARY_SCORE = "summaryScore";
    public static final String TIMESTAMP_PROPERTY = "timestamp";
    public static final String VALUE = "value";
    
    private StringConstants() {
    }
}
