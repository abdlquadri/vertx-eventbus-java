# vertx-eventbus-java


A [Vert.x EventBus](http://vertx.io/docs/vertx-core/java/#event_bus) client written in Java uses [Netty](http://netty.io/), works on Android 2.3.7 +:

# Testing
`./gradlew test`

# Building

`./gradlew build` . The jar file will be in build/libs.

# Dependencies

```java
  compile "io.netty:netty-handler:4.1.8.Final"
  compile "com.google.code.gson:gson:2.8.0"
```

# Sample projects
* Sample Android Chat app [Vertx Event Bus Chat](https://github.com/abdlquadri/VertxEventBusChat).
* Sample Vert.x Server bridged to TCP [vertx-tcp-bridged-chat-server](https://github.com/abdlquadri/vertx-tcp-bridged-chat-server).

# Usage
```java
final CountDownLatch countDownLatch = new CountDownLatch(1);
EventBus.connect("localhost", 7000, isConnected -> {
    if (isConnected) {
      assertTrue(isConnected);
    } else {
      assertFalse(isConnected);
    }
    countDownLatch.countDown();
});
countDownLatch.await();
```


```java
final CountDownLatch countDownLatch = new CountDownLatch(1);
EventBus.registerHandler("hello", message -> {
	JsonObject body = new JsonParser().parse(message).getAsJsonObject().get("body").getAsJsonObject();
    assertEquals("some messgae",  body.get("value").asString());
    countDownLatch.countDown();
});
JsonObject json = new JsonObject();
json.addProperty("value", "some messgae");
EventBus.publish("hello", json.toString());
countDownLatch.await();
```
