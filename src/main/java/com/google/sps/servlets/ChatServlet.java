package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.sps.utils.SoyRendererUtils.getOutputString;
import static com.google.sps.utils.StringConstants.*;

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
import com.google.gson.Gson;
import com.google.sps.data.PerspectiveRequest;
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
public class ChatServlet extends HttpServlet {
    public static final String API_BASE_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=";

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
        String groupID = (String) request.getParameter(GROUP_ID_PROPERTY);

        // This will be a placeholder until we can coordinate how to pass
        // groupID from page to page
        if (groupID == null) {
            groupID = "123";
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // This is just a sample group until group creation is coordinated
        Entity blmEntity = new Entity(GROUP_KIND, "123");
        blmEntity.setProperty(GROUP_NAME_PROPERTY, "Black Lives Matter");
        datastore.put(blmEntity);

        Entity sierraEntity = new Entity(GROUP_KIND, "456");
        sierraEntity.setProperty(GROUP_NAME_PROPERTY, "Sierra Club");
        datastore.put(sierraEntity);

        // Calls query on all entities of type Message
        Query messageQuery = new Query(MESSAGE_KIND + groupID).addSort(TIMESTAMP_PROPERTY, SortDirection.ASCENDING);
        PreparedQuery preparedMessageQuery = datastore.prepare(messageQuery);

        Query groupQuery = new Query(GROUP_KIND);
        PreparedQuery preparedGroupQuery = datastore.prepare(groupQuery);

        ImmutableMap<String, ImmutableList> messagesGroupsData = getTemplateData(preparedMessageQuery,
            preparedGroupQuery);

        final String chatPageHtml = getOutputString(CHAT_SOY_FILE, CHAT_PAGE_NAMESPACE, messagesGroupsData);

        response.getWriter().println(chatPageHtml);
    }

    /**
     * Is called by a button on the chat form. doPost takes the message and its
     * attributes (such as timestamp and user) and stores them into Datastore.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String messageText = (String) request.getParameter(MESSAGE_TEXT_PROPERTY);
        String groupID = (String) request.getParameter(GROUP_ID_PROPERTY);
        final long timestamp = System.currentTimeMillis();

        // This will be a placeholder until we can coordinate how to pass
        // groupID from page to page
        if (groupID == null) {
            groupID = "123";
        }

        if (messageText.isEmpty()) {
            response.sendRedirect(CHAT_REDIRECT + groupID);
            return;
        }

        // Reads API Key and HTTP referer from file
        ClassLoader classLoader = ChatServlet.class.getClassLoader();
        File apiKeyFile = new File(classLoader.getResource(KEYS_TXT_FILE).getFile());
        Scanner scanner = new Scanner(apiKeyFile);
        final String apiKey = scanner.nextLine();
        final String referer = scanner.nextLine();
        scanner.close();

        final Double commentScore = getCommentScore(API_BASE_URL + apiKey, referer, messageText);

        // If the comment is  toxic, then do not post it to Datastore
        if (commentScore >= COMMENT_SCORE_THRESHOLD) {
            ImmutableMap<String, String> errorData = ImmutableMap.of(ERROR_MESSAGE_KEY, 
                ERROR_MESSAGE_TEXT);

            final String errorPageHtml = getOutputString(CHAT_SOY_FILE, CHAT_ERROR_NAMESPACE, errorData);

            response.getWriter().println(errorPageHtml);
        } else {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity messageEntity = new Entity(MESSAGE_KIND + groupID);
            messageEntity.setProperty(MESSAGE_TEXT_PROPERTY, messageText);
            messageEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);
            datastore.put(messageEntity);

            response.sendRedirect(CHAT_REDIRECT + groupID);
        }
    }

    /**
     * Iterates through the queries to put all the required text into a list.
     * A map is then created the pass the lists into the template.
     */
    private ImmutableMap<String, ImmutableList> getTemplateData(PreparedQuery preparedMessageQuery,
        PreparedQuery preparedGroupQuery) {

        // Creates lists of the data
        ImmutableList<String> messagesList = Streams.stream(preparedMessageQuery.asIterable())
            .map(message -> (String) message.getProperty(MESSAGE_TEXT_PROPERTY))
            .collect(toImmutableList());

        ImmutableList.Builder<ImmutableMap<String, String>> builder = new ImmutableList.Builder<>();
        for (Entity group : preparedGroupQuery.asIterable()) {
            ImmutableMap<String, String> groupMap = ImmutableMap.of(
                GROUP_NAME_PROPERTY, (String) group.getProperty(GROUP_NAME_PROPERTY),
                GROUP_ID_PROPERTY, group.getKey().getName());
            builder.add(groupMap);
        }
        ImmutableList<ImmutableMap<String, String>> groupsList = builder.build();

        // Data will be passed in as a list of messages/groups in a map (needed for template)
        return ImmutableMap.of(MESSAGES_KEY, messagesList, GROUPS_KEY, groupsList);
    }

    /**
     * Makes a POST request to the Perspective API with the message text, and receives
     * a toxicity score.
     */
    public Double getCommentScore(String apiURL, String referer, String messageText) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiURL);

        PerspectiveRequest perspectiveRequest = new PerspectiveRequest(messageText, Attribute.TOXICITY.name());
        final String inputJson = new Gson().toJson(perspectiveRequest);

        httpPost.setEntity(new StringEntity(inputJson));
        httpPost.addHeader(REFERER_HEADER, referer);
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
