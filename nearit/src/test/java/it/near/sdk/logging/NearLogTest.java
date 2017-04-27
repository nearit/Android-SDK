package it.near.sdk.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NearLogTest {

    private static final String TAG_TEST = "test tag string";
    private static final String MESSAGE_TEST = "test message string";

    @Mock
    NearLogger logger;

    @Before
    public void setUp() {
        NearLog.setLogger(logger);
    }

    @Test
    public void shouldReturnLogger() {
        assertThat(NearLog.getLogger(), is(logger));
    }

    @Test(expected = NullPointerException.class)
    public void ifLoggerIsNull_shouldThrow() {
        NearLog.setLogger(null);
    }

    @Test
    public void verbose() {
        String tag = TAG_TEST;
        String message = MESSAGE_TEST;
        NearLog.v(tag, message);
        verify(logger).v(tag, message);
    }

    @Test
    public void verboseWithThrowable() {
        String tag = TAG_TEST;
        String message = MESSAGE_TEST;
        Throwable t = new Throwable();
        NearLog.v(tag, message, t);
        verify(logger).v(tag, message, t);
    }

    @Test
    public void debug() {
        String tag = TAG_TEST;
        String message = MESSAGE_TEST;
        NearLog.d(tag, message);
        verify(logger).d(tag, message);
    }

    @Test
    public void debugWithThrowable() {
        String tag = TAG_TEST;
        String message = MESSAGE_TEST;
        Throwable t = new Throwable();
        NearLog.d(tag, message, t);
        verify(logger).d(tag, message, t);
    }

    @Test
    public void info() {
        String tag = "TestTag";
        String message = "Test message";
        NearLog.i(tag, message);
        verify(logger).i(tag, message);
    }

    @Test
    public void infoWithThrowable() {
        String tag = "TestTag";
        String message = "Test message";
        Throwable t = new Throwable();
        NearLog.i(tag, message, t);
        verify(logger).i(tag, message, t);
    }

    @Test
    public void warning() {
        String tag = "TestTag";
        String message = "Test message";
        NearLog.w(tag, message);
        verify(logger).w(tag, message);
    }

    @Test
    public void warningWithThrowable() {
        String tag = "TestTag";
        String message = "Test message";
        Throwable t = new Throwable();
        NearLog.w(tag, message, t);
        verify(logger).w(tag, message, t);
    }

    @Test
    public void error() {
        String tag = "TestTag";
        String message = "Test message";
        NearLog.e(tag, message);
        verify(logger).e(tag, message);
    }

    @Test
    public void errorWithThrowable() {
        String tag = "TestTag";
        String message = "Test message";
        Throwable t = new Throwable();
        NearLog.e(tag, message, t);
        verify(logger).e(tag, message, t);
    }
}
