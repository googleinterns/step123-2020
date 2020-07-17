package com.google.sps;

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
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
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
    private static final String GROUP_ID_PARAM = "groupID";
    private static final String GROUP_ID = "123";
    private static final String MESSAGE_TEXT_TOXIC = "what kind of idiot name is foo?";
    private static final String MESSAGE_TEXT_PROPERTY = "message-text";
    private static final String TIMESTAMP_PROPERTY = "timestamp";
    private static final String MESSAGE_KIND = "Message-";
    private static final String MESSAGES_MAP_KEY = "messages";
    private static final String CHAT_TEMPLATE = "templates.chat.chatPage";
    private static final Double SCORE_OFFSET = 0.0000005;
    private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private File apiKeyFile;
    private Scanner scanner;
    private String API_KEY;
    private String REFERER;
    private String apiURL;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private SoyFileSet sfs;
    private SoyTofu tofu;
    private DatastoreService datastore;
    private ImmutableMap<String, ImmutableList<String>> templateData;

    private ChatServlet servlet;

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

        sfs = SoyFileSet
            .builder()
            .add(new File("src/main/java/templates/chat.soy"))
            .build();
        tofu = sfs.compileToTofu();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testsCommentScore() throws IOException {
        // Reads API Key and Referer from file 
        apiKeyFile = new File("src/main/resources/keys.txt");
        scanner = new Scanner(apiKeyFile);
        API_KEY = scanner.nextLine();
        REFERER = scanner.nextLine();

        apiURL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + API_KEY;

        Double expected = 0.9208521;
        // It may vary from call to call by a little, so as long
        // as it's +/- 0.0000005, it passes
        Double min = expected - SCORE_OFFSET;
        Double max = expected + SCORE_OFFSET;
        
        Double actual = servlet.getCommentScore(apiURL, REFERER, MESSAGE_TEXT_TOXIC);

        Assert.assertTrue(min <= actual);
        Assert.assertTrue(actual <= max);
    }

    @Test
    public void servletGetNoComments() throws IOException {
        // GET method is called and should respond with the HTML
        // string of the chat template with no comments

        when(request.getParameter(GROUP_ID_PARAM)).thenReturn(GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doGet(request, response);
        
        templateData = ImmutableMap.of(MESSAGES_MAP_KEY, ImmutableList.of());
        String expected = tofu.newRenderer(CHAT_TEMPLATE).setData(templateData).render();
        String actual = stringWriter.getBuffer().toString().trim();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void servletGetWithComment() throws IOException {
        // GET method should return the chat template but with one
        // comment that says "hello"

        // Creating a comment that says "hello" in our mock database
        Entity messageEntity = new Entity(MESSAGE_KIND + GROUP_ID);
        messageEntity.setProperty(MESSAGE_TEXT_PROPERTY, "hello");
        messageEntity.setProperty(TIMESTAMP_PROPERTY, 1);
        datastore.put(messageEntity);

        when(request.getParameter(GROUP_ID_PARAM)).thenReturn(GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doGet(request, response);

        templateData = ImmutableMap.of(MESSAGES_MAP_KEY, ImmutableList.of("hello"));
        String expected = tofu.newRenderer(CHAT_TEMPLATE).setData(templateData).render();
        String actual = stringWriter.getBuffer().toString().trim();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void servletGetWithTwoComments() throws IOException {
        // GET method should return the chat template with two 
        // posted messages

        Entity firstEntity = new Entity(MESSAGE_KIND + GROUP_ID);
        firstEntity.setProperty(MESSAGE_TEXT_PROPERTY, "First message");
        firstEntity.setProperty(TIMESTAMP_PROPERTY, 1);
        datastore.put(firstEntity);

        Entity secondEntity = new Entity(MESSAGE_KIND + GROUP_ID);
        secondEntity.setProperty(MESSAGE_TEXT_PROPERTY, "Second message");
        secondEntity.setProperty(TIMESTAMP_PROPERTY, 2);
        datastore.put(secondEntity);

        when(request.getParameter(GROUP_ID_PARAM)).thenReturn(GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doGet(request, response);

        templateData = ImmutableMap.of(MESSAGES_MAP_KEY, 
            ImmutableList.of("First message", "Second message"));
        String expected = tofu.newRenderer(CHAT_TEMPLATE).setData(templateData).render();
        String actual = stringWriter.getBuffer().toString().trim();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void servletPostGoodComment() throws IOException {
        // POST method should post the message to datastore and
        // redirect to /chat

        when(request.getParameter(MESSAGE_TEXT_PROPERTY)).thenReturn("hi");
        when(request.getParameter(GROUP_ID_PARAM)).thenReturn(GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doPost(request, response);

        // Only one message ("hi") should be in the datastore
        String expected = "hi";
        Query messageQuery = new Query(MESSAGE_KIND + GROUP_ID);
        Entity onlyMessageEntity = datastore.prepare(messageQuery).asSingleEntity();
        String actual = (String) onlyMessageEntity.getProperty(MESSAGE_TEXT_PROPERTY);

        verify(response, times(1)).sendRedirect("/chat");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void servletPostToxicComment() throws IOException {
        // POST method should not post to datastore and
        // should render an error message

        when(request.getParameter(MESSAGE_TEXT_PROPERTY)).thenReturn(MESSAGE_TEXT_TOXIC);
         when(request.getParameter(GROUP_ID_PARAM)).thenReturn(GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doPost(request, response);

        String expectedMessages = null;
        Query messageQuery = new Query(MESSAGE_KIND + GROUP_ID);
        // Should be null
        Entity actualMessages = datastore.prepare(messageQuery).asSingleEntity();

        ImmutableMap<String, String> errorData = ImmutableMap.of("errorMessage", 
        "Your message contains content that may be deemed offensive by others. " +
        "Please revise your message and try again.");

        String expectedOutput = tofu.newRenderer("templates.chat.error").setData(errorData).render();
        String actualOutput = stringWriter.getBuffer().toString().trim();
        
        Assert.assertEquals(expectedMessages, actualMessages);
        Assert.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void servletPostEmptyComment() throws IOException {
        // POST method should not post to datastore but should
        // redirect to /chat

        when(request.getParameter(MESSAGE_TEXT_PROPERTY)).thenReturn("");
        when(request.getParameter(GROUP_ID_PARAM)).thenReturn(GROUP_ID);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doPost(request, response);

        String expectedMessages = null;
        Query messageQuery = new Query(MESSAGE_KIND + GROUP_ID);
        // Should be null
        Entity actualMessages = datastore.prepare(messageQuery).asSingleEntity();
        
        verify(response, times(1)).sendRedirect("/chat");
        Assert.assertEquals(expectedMessages, actualMessages);
    }
}
