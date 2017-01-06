# vertx-eventbus-java


A [Vert.x EventBus](http://vertx.io/docs/vertx-core/java/#event_bus) client written in Java uses [Netty](http://netty.io/), works on Android 2.3.7 +:

# Building

`./gradlew jar` . The jar file will be in build/libs.

# Dependencies

```java
    compile "io.netty:netty-handler:4.1.0.Beta8"
    compile "org.sharegov:mjson:1.3"
```

# Sample projects
* Sample Android Chat app [Vertx Event Bus Chat](https://github.com/abdlquadri/VertxEventBusChat).
* Sample Vert.x Server bridged to TCP [vertx-tcp-bridged-chat-server](https://github.com/abdlquadri/vertx-tcp-bridged-chat-server).

# Usage
```java
final CountDownLatch countDownLatch = new CountDownLatch(1);
EventBus.connect("127.0.0.1", 7000, new ConnectHandler() {
  @Override
  public void connected(boolean isConnected) {
    if (isConnected) {
      assertTrue(isConnected);
    } else {
      assertFalse(isConnected);
    }
    countDownLatch.countDown();
  }
});
countDownLatch.await();
```


```java
final CountDownLatch countDownLatch = new CountDownLatch(1);
EventBus.registerHandler("hello", new Handler() {
  @Override
  public void handle(String message) {

    assertEquals("some messgae", Json.read(message).at("body").at("value").asString());
    countDownLatch.countDown();
  }
});

EventBus.publish("hello", Json.object().set("value", "some messgae").toString());
countDownLatch.await();
```
