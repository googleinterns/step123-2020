package com.google.sps.data;

public final class Group {
  private final String name;
  private final long groupId;
  private final String calendarId;

  public Group(String name, long groupId, String calendarId) { 
    this.name = name;
    this.groupId = groupId;
    this.calendarId = calendarId;
  }
}
