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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
public class ChatServlet extends HttpServlet {
    private static final String GROUP_ID_PARAM = "groupID";
    private static final String MESSAGE_KIND = "Message-";
    private static final String MESSAGE_TEXT_PROPERTY = "message-text";
    private static final String TIMESTAMP_PROPERTY = "timestamp";
    private static final String GROUP_KIND = "Group";
    private static final String GROUP_NAME_PROPERTY = "name";
    private static final String ROOT_FILE_PATH = "../../";
    private static final Double COMMENT_SCORE_THRESHOLD = 0.85;
    private static final String MESSAGES_KEY = "messages";
    private static final String GROUPS_KEY = "groups";
    private static final String ATTRIBUTE_SCORES = "attributeScores";
    private static final String SUMMARY_SCORE = "summaryScore";
    private static final String VALUE = "value";
    private static final ClassLoader classLoader = ChatServlet.class.getClassLoader();

    enum Attribute {
        TOXICITY,
        SEVERE_TOXICITY
    }

    /**
     * When called, doGet will query all previously posted messages to the chat,
     * and render the messages into a HTML template.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String groupID = (String) request.getParameter(GROUP_ID_PARAM);

        // This will be a placeholder until we can coordinate how to pass
        // groupID from page to page
        if (groupID == null) {
            groupID = "123";
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // This is just a sample group until group creation is coordinated
        Entity sampleGroup = new Entity(GROUP_KIND, "BLM");
        sampleGroup.setProperty(GROUP_NAME_PROPERTY, "Black Lives Matter");
        datastore.put(sampleGroup);

        // Calls query on all entities of type Message
        Query messageQuery = new Query(MESSAGE_KIND + groupID).addSort(TIMESTAMP_PROPERTY, SortDirection.ASCENDING);
        PreparedQuery preparedMessageQuery = datastore.prepare(messageQuery);

        Query groupQuery = new Query(GROUP_KIND);
        PreparedQuery preparedGroupQuery = datastore.prepare(groupQuery);

        ImmutableMap<String, ImmutableList<String>> data = getTemplateData(preparedMessageQuery, preparedGroupQuery);

        final String out = getOutputString("chatPage", data);

        response.getWriter().println(out);
    }

    /**
     * Is called by a button on the chat form. doPost takes the message and its
     * attributes (such as timestamp and user) and stores them into Datastore.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String messageText = (String) request.getParameter(MESSAGE_TEXT_PROPERTY);
        String groupID = (String) request.getParameter(GROUP_ID_PARAM);
        final long timestamp = System.currentTimeMillis();

        // This will be a placeholder until we can coordinate how to pass
        // groupID from page to page
        if (groupID == null) {
            groupID = "123";
        }

        if (messageText.length() == 0) {
            response.sendRedirect("/chat");
            return;
        }

        // Reads API Key and HTTP referer from file 
        File apiKeyFile = new File(classLoader.getResource("keys.txt").getFile());
        Scanner scanner = new Scanner(apiKeyFile);
        final String apiKey = scanner.nextLine();
        final String referer = scanner.nextLine();
        scanner.close();

        final String perspectiveURL = 
            "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;

        final Double commentScore = getCommentScore(perspectiveURL, referer, messageText);

        // If the comment is not toxic, then post it to Datastore
        if (commentScore <= COMMENT_SCORE_THRESHOLD) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity messageEntity = new Entity(MESSAGE_KIND + groupID);
            messageEntity.setProperty(MESSAGE_TEXT_PROPERTY, messageText);
            messageEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);
            datastore.put(messageEntity);

            response.sendRedirect("/chat");
        } else {
            ImmutableMap<String, String> errorData = ImmutableMap.of("errorMessage", 
                "Your message contains content that may be deemed offensive by others. " +
                "Please revise your message and try again.");

            final String out = getOutputString("error", errorData);

            response.getWriter().println(out);
        }
    }

    /**
     * Returns the output string for the response. In other words,
     * it sets up the soy template with the passed in data.
     */
    private String getOutputString(String templateName, ImmutableMap data) {
        
        SoyFileSet sfs = SoyFileSet
            .builder()
            .add(new File(classLoader.getResource("chat.soy").getFile()))
            .build();
        SoyTofu tofu = sfs.compileToTofu();

        return tofu.newRenderer("templates.chat." + templateName).setData(data).render();
    }

    /**
     * Iterates through the queries to put all the required text into a list.
     * A map is then created the pass the lists into the template.
     */
    private ImmutableMap<String, ImmutableList<String>> getTemplateData(PreparedQuery preparedMessageQuery,                 PreparedQuery preparedGroupQuery) {

        // Creates lists of the data
        ImmutableList<String> messagesList = Streams.stream(preparedMessageQuery.asIterable())
            .map(message -> (String) message.getProperty(MESSAGE_TEXT_PROPERTY))
            .collect(toImmutableList());

        ImmutableList<String> groupsList = Streams.stream(preparedGroupQuery.asIterable())
            .map(group -> (String) group.getProperty(GROUP_NAME_PROPERTY))
            .collect(toImmutableList());

        // Data will be passed in as a list of messages/groups in a map (needed for template)
        return ImmutableMap.of(MESSAGES_KEY, messagesList, GROUPS_KEY, groupsList);
    }

    /**
     * Makes a POST request to the Perspective API with the message text, and recieves
     * a toxicity score.
     */
    public Double getCommentScore(String apiURL, String referer, String messageText) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiURL);

        PerspectiveRequest messageObject = new PerspectiveRequest(messageText, Attribute.TOXICITY.name());
        final String inputJson = new Gson().toJson(messageObject);

        httpPost.setEntity(new StringEntity(inputJson));
        httpPost.addHeader("Referer", referer);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        // Perspective API responds with JSON string
        final String result = EntityUtils.toString(httpResponse.getEntity());
        // Parsing JSON string to Java Maps
        Map resultMap = new Gson().fromJson(result, Map.class);
        Map attributeScores = (Map) resultMap.get(ATTRIBUTE_SCORES);
        Map toxicity = (Map) attributeScores.get(Attribute.TOXICITY.name());
        Map summaryScore = (Map) toxicity.get(SUMMARY_SCORE);

        return (Double) summaryScore.get(VALUE);
    }
}
