package com.google.sps.data;

import java.util.HashSet;

public final class User {
  private final String email;
  private final HashSet<Long> groups;

  public User(String email) {
    this.email = email;
    this.groups = new HashSet<>();
  }
}
