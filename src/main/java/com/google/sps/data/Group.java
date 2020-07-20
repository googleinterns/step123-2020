package com.google.sps.data;

public final class Group {
  private final String name;
  private final String image;
  private final String description;
  private final long groupId;
  private final String calendarId;

  public Group(String name, String image, String description, long groupId, String calendarId) { 
    this.name = name;
    this.image = image;
    this.description = description;
    this.groupId = groupId;
    this.calendarId = calendarId;
  }
}
