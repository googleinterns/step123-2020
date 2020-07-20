package com.google.sps;

import static org.mockito.Mockito.when;

import com.google.sps.utils.ServletUtils;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public final class ServletUtilsTest extends Mockito {
  private static final String TESTING_PARAMETER = "language";
  private static final String TESTING_PARAMETER_VALUE = "english";
  private static final String NULL_PARAMETER = "age";

  @Mock
  HttpServletRequest request;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    
    // {language: english}
    when(request.getParameter(TESTING_PARAMETER)).thenReturn(TESTING_PARAMETER_VALUE);
    // {age: }
    when(request.getParameter(NULL_PARAMETER)).thenReturn(null);
  }

  @Test
  public void getParameterWithValue() throws IOException {
    String expected = ServletUtils.getParameter(request, TESTING_PARAMETER);

    Assert.assertEquals(expected, TESTING_PARAMETER_VALUE);
  }

  @Test
  public void getParameterWithNoValue() throws IOException {
    String expected = ServletUtils.getParameter(request, NULL_PARAMETER);

    Assert.assertEquals(expected, ServletUtils.DEFAULT_PARAM);
  }
}
