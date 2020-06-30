package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity; 
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import java.io.File;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet{

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Calls query on all entities of type Message
    Query messageQuery = new Query("Message").addSort("timestamp", SortDirection.ASCENDING);
    PreparedQuery preparedMessageQuery = datastore.prepare(messageQuery);

    // Creates list of the messages text
    ImmutableList<String> messagesList = Streams.stream(preparedMessageQuery.asIterable()).map(message -> 
        (String) message.getProperty("message-text")).collect(ImmutableList.toImmutableList());

    // Data will be passed in as a list of messages in a map (needed for template)
    ImmutableMap<String, ImmutableList<String>> data = ImmutableMap.of("messages", messagesList);

    SoyFileSet sfs = SoyFileSet
            .builder()
            // File path starts in target/portfolio-1
            .add(new File("../../src/main/java/templates/chat.soy"))
            .build();
    SoyTofu tofu = sfs.compileToTofu();

    final String out = tofu.newRenderer("templates.chat.chatPage").setData(data).render();
    response.getWriter().println(out);
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

    response.sendRedirect("/chat");
  }
}
