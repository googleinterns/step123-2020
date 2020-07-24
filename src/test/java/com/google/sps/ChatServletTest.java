package com.google.sps;

import static com.google.sps.utils.SoyRendererUtils.getOutputString;
import static com.google.sps.utils.StringConstants.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.sps.servlets.ChatServlet;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public final class ChatServletTest extends Mockito {
    private static final String BLM_GROUP_ID = "123";
    private static final String MESSAGE_TEXT_NON_TOXIC = "hello";
    private static final String MESSAGE_TEXT_TOXIC = "what kind of idiot name is foo?";
    private static final String SIERRA_GROUP_ID = "456";
    
    private static final ImmutableList<ImmutableMap<String, String>> SAMPLE_GROUP_LIST = 
        ImmutableList.of(ImmutableMap.of(GROUP_NAME_PROPERTY, "Black Lives Matter",
        GROUP_ID_PROPERTY, BLM_GROUP_ID),
        ImmutableMap.of(GROUP_NAME_PROPERTY, "Sierra Club",
        GROUP_ID_PROPERTY, SIERRA_GROUP_ID));
    
    private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private DatastoreService datastore;
    private PrintWriter printWriter;
    private ChatServlet servlet;
    private StringWriter stringWriter;
    private ImmutableMap<String, ImmutableList> templateData;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Before
    public void setUp() {
        servlet = new ChatServlet();
        
        helper.setUp();
        datastore = DatastoreServiceFactory.getDatastoreService();

        MockitoAnnotations.initMocks(this);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testsCommentScore() throws IOException {
        final Double scoreOffset = 0.0000005;

        // Reads API Key and Referer from file 
        final File apiKeyFile = new File("src/main/resources/keys.txt");
        final Scanner scanner = new Scanner(apiKeyFile);
        final String apiKey = scanner.nextLine();
        final String referer = scanner.nextLine();

        Double expectedScore = 0.9208521;
        // It may vary from call to call by a little, so as long
        // as it's +/- 0.0000005, it passes
        Double min = expectedScore - scoreOffset;
        Double max = expectedScore + scoreOffset;
        
        Double actualScore = servlet.getCommentScore(ChatServlet.API_BASE_URL + apiKey, referer, MESSAGE_TEXT_TOXIC);

        Assert.assertTrue(min <= actualScore);
        Assert.assertTrue(actualScore <= max);
    }

    @Test
    public void servletGetNoComments() throws IOException {
        // GET method is called and should respond with the HTML
        // string of the chat template with no comments

        when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(BLM_GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doGet(request, response);
        
        templateData = ImmutableMap.of(MESSAGES_KEY, ImmutableList.of(), GROUPS_KEY, 
            SAMPLE_GROUP_LIST);
        String expectedHtml = getOutputString(CHAT_SOY_FILE, CHAT_PAGE_NAMESPACE, templateData);
        String actualHtml = stringWriter.getBuffer().toString().trim();

        Assert.assertEquals(expectedHtml, actualHtml);
    }

    @Test
    public void servletGetWithComment() throws IOException {
        // GET method should return the chat template but with one
        // comment that says "hello"

        // Creating a comment that says "hello" in our mock database
        Entity messageEntity = new Entity(MESSAGE_KIND + SIERRA_GROUP_ID);
        messageEntity.setProperty(MESSAGE_TEXT_PROPERTY, MESSAGE_TEXT_NON_TOXIC);
        messageEntity.setProperty(TIMESTAMP_PROPERTY, 1);
        datastore.put(messageEntity);

        when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(SIERRA_GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doGet(request, response);

        templateData = ImmutableMap.of(MESSAGES_KEY, ImmutableList.of(MESSAGE_TEXT_NON_TOXIC),
            GROUPS_KEY, SAMPLE_GROUP_LIST);
        String expectedHtml = getOutputString(CHAT_SOY_FILE, CHAT_PAGE_NAMESPACE, templateData);
        String actualHtml = stringWriter.getBuffer().toString().trim();

        Assert.assertEquals(expectedHtml, actualHtml);
    }

    @Test
    public void servletGetWithTwoComments() throws IOException {
        // GET method should return the chat template with only
        // the one in the BLM group
        final String firstMessage = "BLM message";
        final String secondMessage = "Sierra Message";

        Entity firstEntity = new Entity(MESSAGE_KIND + BLM_GROUP_ID);
        firstEntity.setProperty(MESSAGE_TEXT_PROPERTY, firstMessage);
        firstEntity.setProperty(TIMESTAMP_PROPERTY, 1);
        datastore.put(firstEntity);

        Entity secondEntity = new Entity(MESSAGE_KIND + SIERRA_GROUP_ID);
        secondEntity.setProperty(MESSAGE_TEXT_PROPERTY, secondMessage);
        secondEntity.setProperty(TIMESTAMP_PROPERTY, 2);
        datastore.put(secondEntity);

        when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(BLM_GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doGet(request, response);

        templateData = ImmutableMap.of(MESSAGES_KEY, ImmutableList.of(firstMessage),
            GROUPS_KEY, SAMPLE_GROUP_LIST);
        String expectedHtml = getOutputString(CHAT_SOY_FILE, CHAT_PAGE_NAMESPACE, templateData);
        String actualHtml = stringWriter.getBuffer().toString().trim();

        Assert.assertEquals(expectedHtml, actualHtml);
    }

    @Test
    public void servletPostGoodComment() throws IOException {
        // POST method should post the message to datastore and
        // redirect to /chat

        when(request.getParameter(MESSAGE_TEXT_PROPERTY)).thenReturn(MESSAGE_TEXT_NON_TOXIC);
        when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(BLM_GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doPost(request, response);

        // Only one message ("hello") should be in the datastore
        String expectedMessage = MESSAGE_TEXT_NON_TOXIC;
        Query messageQuery = new Query(MESSAGE_KIND + BLM_GROUP_ID);
        Entity onlyMessageEntity = datastore.prepare(messageQuery).asSingleEntity();
        String actualMessage = (String) onlyMessageEntity.getProperty(MESSAGE_TEXT_PROPERTY);

        verify(response, times(1)).sendRedirect(CHAT_REDIRECT + BLM_GROUP_ID);
        Assert.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void servletPostToxicComment() throws IOException {
       // POST method should not post to datastore and
       // should render an error message

        when(request.getParameter(MESSAGE_TEXT_PROPERTY)).thenReturn(MESSAGE_TEXT_TOXIC);
        when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(SIERRA_GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doPost(request, response);

        Query messageQuery = new Query(MESSAGE_KIND + SIERRA_GROUP_ID);
        Entity actualMessages = datastore.prepare(messageQuery).asSingleEntity();

        ImmutableMap<String, String> errorData = ImmutableMap.of(ERROR_MESSAGE_KEY, 
            ERROR_MESSAGE_TEXT);

        String expectedHtml = getOutputString(CHAT_SOY_FILE, CHAT_ERROR_NAMESPACE, errorData);
        String actualHtml = stringWriter.getBuffer().toString().trim();
        
        Assert.assertEquals(null, actualMessages);
        Assert.assertEquals(expectedHtml, actualHtml);
    }

    @Test
    public void servletPostEmptyComment() throws IOException {
        // POST method should not post to datastore but should
        // redirect to /chat

        when(request.getParameter(MESSAGE_TEXT_PROPERTY)).thenReturn("");
        when(request.getParameter(GROUP_ID_PROPERTY)).thenReturn(BLM_GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doPost(request, response);

        Query messageQuery = new Query(MESSAGE_KIND + BLM_GROUP_ID);
        Entity actualMessages = datastore.prepare(messageQuery).asSingleEntity();
        
        verify(response, times(1)).sendRedirect(CHAT_REDIRECT + BLM_GROUP_ID);
        Assert.assertEquals(null, actualMessages);
    }
}
