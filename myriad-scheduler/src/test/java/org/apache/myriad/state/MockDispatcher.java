package org.apache.myriad.state;

import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.event.Event;
import org.apache.hadoop.yarn.event.EventHandler;

/**
 * Mock Dispatcher implementation for unit tests
 */
public class MockDispatcher implements Dispatcher {
  EventHandler<Event> handler = new MockEventHandler();

  /**
   * Mock EventHandler implementation for unit tests
   */
  public static class MockEventHandler implements EventHandler<Event> {
    @Override
    public void handle(Event event) {
      //noop
    }  
  }

  @Override
  public EventHandler<Event> getEventHandler() {
    return handler;
  }

  @Override
  public void register(Class<? extends Enum> eventType, EventHandler handler) {
    //noop
  }
}