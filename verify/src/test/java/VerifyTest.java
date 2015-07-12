//Let's import Mockito statically so that the code looks clearer
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class VerifyTest {
    class isValid extends ArgumentMatcher {
        public boolean matches(Object o) {
            return true;
        }
    }

    @Test
    public void verifyBehavior() {
        //mock creation
        List mockedList = mock(List.class);

        //using mock object
        mockedList.add("one");
        mockedList.clear();

        mockedList.add("second");
        mockedList.add("second");

        // since we do not mock size(), so it is 0
        assertThat(mockedList.size(), is(equalTo(0)));

        //verification
        verify(mockedList).clear();
        verify(mockedList).add("one");
        verify(mockedList, times(2)).add("second");
    }

    @Test
    public void useStubbing() {
        //You can mock concrete classes, not only interfaces
        LinkedList mockedList = mock(LinkedList.class);

        //stubbing
        when(mockedList.get(0)).thenReturn("first");
        when(mockedList.get(1)).thenThrow(new RuntimeException());

        //following prints "first"
        System.out.println(mockedList.get(0));
        assertThat(mockedList.get(0), is(equalTo("first")));

        //following throws runtime exception
        //TODO:may use jssert to assert an exception occurred
        // System.out.println(mockedList.get(1));

        //following is "null" because get(999) was not stubbed
        assertThat(mockedList.get(999), is(nullValue()));

        //Although it is possible to verify a stubbed invocation, usually it's just redundant
        //If your code cares what get(0) returns then something else breaks (often before even verify() gets executed).
        //If your code doesn't care what get(0) returns then it should not be stubbed. Not convinced? See here.
        verify(mockedList).get(0);
    }

    @Test
    public void useMatcher() {
        //stubbing can be override
        //stubbing using built-in anyInt() argument matcher
        LinkedList mockedList = mock(LinkedList.class);
        when(mockedList.get(anyInt())).thenReturn("element");
        when(mockedList.contains(argThat(new isValid()))).thenReturn(true);

        assertThat(mockedList.get(999), is(equalTo("element")));
        //you can also verify using an argument matcher
        verify(mockedList).get(anyInt());

        //stubbing using hamcrest (let's say isValid() returns your own hamcrest matcher):
        // when(mockedList.contains(argThat(isValid()))).thenReturn("element");

    }

    @Test
    public void verifyWithTimes() {
        //using mock 
        LinkedList mockedList = mock(LinkedList.class);
        mockedList.add("once");

        mockedList.add("twice");
        mockedList.add("twice");

        mockedList.add("three times");
        mockedList.add("three times");
        mockedList.add("three times");

        //following two verifications work exactly the same - times(1) is used by default
        verify(mockedList).add("once");
        verify(mockedList, times(1)).add("once");

        //exact number of invocations verification
        verify(mockedList, times(2)).add("twice");
        verify(mockedList, times(3)).add("three times");

        //verification using never(). never() is an alias to times(0)
        verify(mockedList, never()).add("never happened");

        //verification using atLeast()/atMost()
        verify(mockedList, atLeastOnce()).add("three times");
        verify(mockedList, atLeast(2)).add("twice");
        verify(mockedList, atMost(5)).add("three times");
    }

    @Test
    public void stubbingVoidMethod() {
        LinkedList mockedList = mock(LinkedList.class);
        doThrow(new RuntimeException()).when(mockedList).clear();

   
        //following throws RuntimeException:
        mockedList.clear();
    }

    @Test
    public void verifyInOrder() {
        List firstMock = mock(List.class);
        List secondMock = mock(List.class);
 
        //using mocks
        firstMock.add("was called first");
        secondMock.add("was called second");
 
        //create inOrder object passing any mocks that need to be verified in order
        InOrder inOrder = inOrder(firstMock, secondMock);
 
        //following will make sure that firstMock was called before secondMock
        inOrder.verify(firstMock).add("was called first");
        inOrder.verify(secondMock).add("was called second");
    }

    @Test
    public void noInteraction() {
        List mockOne = mock(List.class);
        List mockTwo = mock(List.class);
        List mockThree = mock(List.class);

        //using mocks - only mockOne is interacted
        mockOne.add("one");
 
        //ordinary verification
        verify(mockOne).add("one");
 
        //verify that method was never called on a mock
        verify(mockOne, never()).add("two");
 
        //verify that other mocks were not interacted
        verifyZeroInteractions(mockTwo, mockThree);
    }

    @Test
    public void findRedundantInvocation() {
        List mockedList = mock(List.class);
        //using mocks
        mockedList.add("one");
        mockedList.add("two");

        verify(mockedList).add("one");

        //following verification will fail , since add("two") does not verify
        verifyNoMoreInteractions(mockedList);
    }

    @Test
    public void stubConsecutiveCall(){
        List mockedList = mock(List.class);
        when(mockedList.get(0)).thenThrow(new RuntimeException()).thenReturn("foo");
        // throws a RuntimeException
        mockedList.get(0);
        // throws "foo" in the second call
        assertThat(mockedList.get(0), is(equalTo("foo")));
        // any consecutive calls wiil return "foo", last stubbing wins
        assertThat(mockedList.get(0), is(equalTo("foo")));
    }

    @Test
    public void stubCallback() {
        List mockedList = mock(List.class);
        when(mockedList.get(0)).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Object mock = invocation.getMock();
                return "foo";
             }
         });
 
        assertThat(mockedList.get(0), is(equalTo("foo")));
    }

    @Test
    public void useSpy() {
        // mock the real object
        List list = new LinkedList();
        List spy = spy(list);
     
        //optionally, you can stub out some methods:
        when(spy.size()).thenReturn(100);
     
        //using the spy calls real methods
        spy.add("one");
        spy.add("two");
     
        //prints "one" - the first element of a list
        assertThat(spy.get(0), is(equalTo("one")));
     
        //size() method was stubbed - 100 is printed
        assertThat(spy.size(), is(equalTo(100)));
     
        //optionally, you can verify
        verify(spy).add("one");
        verify(spy).add("two");
    }
}

