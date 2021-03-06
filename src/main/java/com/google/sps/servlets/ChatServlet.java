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
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.sps.data.PerspectiveRequest;
import com.google.sps.utils.ServletUtils;
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
    private static String defaultGroup = null;

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
        ServletUtils.enforceUserLogin(request, response);
        Entity userEntity = ServletUtils.getUserEntity(request.getUserPrincipal().getName());
        ImmutableList<ImmutableMap<String, String>> groupsList = ServletUtils.getGroupsList(userEntity);

        String groupId = (String) request.getParameter(GROUP_ID_PROPERTY);
        if (groupsList.isEmpty()) {
            // If there are no groups, then the current group is just an empty ID
            groupId = "";
        } else if (Strings.isNullOrEmpty(groupId)) {
            // If no group is chosen, the first group will be shown
            // Default group needs to be updated for the entire class as well
            defaultGroup = groupsList.get(0).get(GROUP_ID_PROPERTY);
            groupId = defaultGroup;
        }

        // Calls query on all entities of type Message
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query messageQuery = new Query(MESSAGE_KIND + groupId).addSort(TIMESTAMP_PROPERTY, SortDirection.ASCENDING);
        PreparedQuery preparedMessageQuery = datastore.prepare(messageQuery);

        // Creates lists of the data
        ImmutableList<String> messagesList = Streams.stream(preparedMessageQuery.asIterable())
            .map(message -> (String) message.getProperty(MESSAGE_TEXT_PROPERTY))
            .collect(toImmutableList());

        ImmutableMap messagesGroupsData = ImmutableMap.of(MESSAGES_KEY, messagesList, GROUPS_KEY,
            groupsList, CURR_GROUP_KEY, groupId);

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
        String groupId = (String) request.getParameter(GROUP_ID_PROPERTY);
        final long timestamp = System.currentTimeMillis();

        if (groupId.isEmpty()) {
            groupId = defaultGroup;
        }

        if (messageText.isEmpty() || messageText.trim().isEmpty()) {
            response.sendRedirect(CHAT_REDIRECT + groupId);
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
                ERROR_MESSAGE_TEXT, CURR_GROUP_KEY, groupId);

            final String errorPageHtml = getOutputString(CHAT_SOY_FILE, CHAT_ERROR_NAMESPACE, errorData);

            response.getWriter().println(errorPageHtml);
        } else {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity messageEntity = new Entity(MESSAGE_KIND + groupId);
            messageEntity.setProperty(MESSAGE_TEXT_PROPERTY, messageText);
            messageEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);
            datastore.put(messageEntity);

            response.sendRedirect(CHAT_REDIRECT + groupId);
        }
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
