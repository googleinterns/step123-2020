package com.google.sps.data;

public final class Group {
  private final String calendarId;
  private final String description;
  private final long groupId;
  private final String image;
  private final String name;

  public Group(String calendarId, String description, long groupId, String image, String name) { 
    this.calendarId = calendarId;
    this.description = description;
    this.groupId = groupId;
    this.image = image;
    this.name = name;
  }
}
