package org.apache.myriad.state;

import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.event.EventHandler;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMAppEvent;

/**
 * Mock Dispatcher implementation for unit tests
 */
public class MockDispatcher implements Dispatcher {
  EventHandler<RMAppEvent> handler = new MockEventHandler();

  /**
   * Mock EventHandler implementation for unit tests
   */
  public static class MockEventHandler implements EventHandler<RMAppEvent> {
    @Override
    public void handle(RMAppEvent event) {
      //noop
    }  
  }

  @Override
  public EventHandler<RMAppEvent> getEventHandler() {
    return handler;
  }

  @Override
  public void register(Class<? extends Enum> eventType, EventHandler handler) {
    //noop
  }
}