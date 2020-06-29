package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity; 
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet{

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("Message").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery pq = datastore.prepare(query);

    ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
    for (Entity message : pq.asIterable()) {
        builder.add(message.getProperty("message-text"));
    }
    ImmutableList<String> messages = builder.build();

    Map<String, ImmutableList<String>> data = new HashMap<>();
    data.put("Messages", messages);
  }


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String messageText = (String) request.getParameter("message-text");
    final long timestamp = System.currentTimeMillis();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity messageEntity = new Entity("Message");
    messageEntity.setProperty("message-text", messageText);
    messageEntity.setProperty("timestamp", timestamp);
    datastore.put(messageEntity);

    response.sendRedirect("/chat.html");
  }
}
