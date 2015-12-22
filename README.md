# vertx-eventbus-java


A Vert.x EventBus client written in Java, works on Android:

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
