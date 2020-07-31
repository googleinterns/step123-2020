package com.google.sps.data;

import java.util.HashSet;

public final class User {
  private final String email;
  private final HashSet<Long> groups = new HashSet<>();

  public User(String email) {
    this.email = email;
  }
}
