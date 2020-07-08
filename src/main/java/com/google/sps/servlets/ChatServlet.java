package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity; 
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import static com.google.common.collect.ImmutableList.toImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet{
    private static final String MESSAGE_TEXT_PROPERTY = "message-text";
    private static final String TIMESTAMP_PROPERTY = "timestamp";
    private static final String MESSAGE_KIND = "Message";

  /**
   * When called, doGet will query all previously posted messages to the chat,
   * and render the messages into a HTML template.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Calls query on all entities of type Message
    Query messageQuery = new Query(MESSAGE_KIND).addSort(TIMESTAMP_PROPERTY, SortDirection.ASCENDING);
    PreparedQuery preparedMessageQuery = datastore.prepare(messageQuery);

    ImmutableMap<String, ImmutableList<String>> data = getTemplateData(preparedMessageQuery);

    // File path starts in target/portfolio-1
    SoyFileSet sfs = SoyFileSet
        .builder()
        .add(new File("../../src/main/java/templates/chat.soy"))
        .build();
    SoyTofu tofu = sfs.compileToTofu();

    final String out = tofu.newRenderer("templates.chat.chatPage").setData(data).render();
    response.getWriter().println(out);
  }

/**
 * Is called by a button on the chat form. doPost takes the message and its
 * attributes (such as timestamp and user) and stores them into Datastore.
 */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String messageText = (String) request.getParameter(MESSAGE_TEXT_PROPERTY);
    final long timestamp = System.currentTimeMillis();
    
    // Reads API Key and Referer from file 
    File apiKeyFile = new File("../../keys.txt");
    Scanner scanner = new Scanner(apiKeyFile);
    final String API_KEY = scanner.nextLine();
    final String REFERER = scanner.nextLine();
    scanner.close();

    final String perspectiveURL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + API_KEY;

    Double commentScore = getCommentScore(perspectiveURL, REFERER, messageText);

    // If the comment is not toxic, then post it to Datastore
    if (commentScore <= 0.85) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity messageEntity = new Entity(MESSAGE_KIND);
        messageEntity.setProperty(MESSAGE_TEXT_PROPERTY, messageText);
        messageEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);
        datastore.put(messageEntity);
    } else {
        // TODO: display a message for the user
        // Either render a text message directly to 
        // the page or a Javascript style alert
    }

    response.sendRedirect("/chat");
  }

  /**
   * Iterates through the message query to put all the message text into a list.
   * A map is then created the pass the list of messages into the template.
   */
  public ImmutableMap<String, ImmutableList<String>> getTemplateData(PreparedQuery preparedMessageQuery) {
    // Creates list of the messages text
    ImmutableList<String> messagesList = Streams.stream(preparedMessageQuery.asIterable()).map(message -> 
    (String) message.getProperty(MESSAGE_TEXT_PROPERTY)).collect(toImmutableList());

    // Data will be passed in as a list of messages in a map (needed for template)
    return ImmutableMap.of("messages", messagesList);
  }

  /**
   * Makes a POST request to the Perspective API with the message text, and recieves
   * a toxicity score.
   */
  public Double getCommentScore(String apiURL, String referer, String messageText) throws IOException {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(apiURL);
 
    PerspectiveRequest messageObject = new PerspectiveRequest(messageText);
    final String inputJson = new Gson().toJson(messageObject);
 
    httpPost.setEntity(new StringEntity(inputJson));
    httpPost.addHeader("Referer", referer);
    CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
 
    // Perspective API responds with JSON string
    final String result = EntityUtils.toString(httpResponse.getEntity());
    // Parsing JSON string to Java Maps
    Map resultMap = new Gson().fromJson(result, Map.class);
    Map attributeScores = (Map) resultMap.get("attributeScores");
    Map toxicity = (Map) attributeScores.get("TOXICITY");
    Map summaryScore = (Map) toxicity.get("summaryScore");
    
    return (Double) summaryScore.get("value");     
  }
}
