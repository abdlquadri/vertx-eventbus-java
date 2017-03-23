package ng.abdlquadri.eventbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import ng.abdlquadri.TestAttributes;
import ng.abdlquadri.eventbus.handlers.ConnectHandler;
import ng.abdlquadri.eventbus.handlers.Handler;
import ng.abdlquadri.server.TCPBridgedChatServer;


/**
 * Created by user on 11/22/15.
 */
public class EventBusBridgeTest {

  @BeforeClass
  public static void createServer() throws InterruptedException {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    Vertx.vertx().deployVerticle(new TCPBridgedChatServer(), new io.vertx.core.Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();

    final CountDownLatch countDownLatch1 = new CountDownLatch(1);
    EventBus.connect(TestAttributes.SERVER, TestAttributes.PORT, new ConnectHandler() {
      public void onConnect(boolean isConnected) {
        if (isConnected) {
          assertTrue(isConnected);
        } else {
          assertFalse(isConnected);
        }
        countDownLatch1.countDown();
      }

      public void onDisConnect(Throwable cause) {
        cause.printStackTrace();
      }

    });
    countDownLatch1.await();
  }

  @AfterClass
  public static void stopServer() {
    EventBus.close();
  }


  @Test
  public void testSend() throws InterruptedException {
	JsonObject json = new JsonObject();
	json.addProperty("value", "from send Bridge");
    EventBus.send(TestAttributes.HELLO_ADDRESS, json.toString());
  }

  @Test
  public void testSendWithReply() throws InterruptedException {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
	JsonObject json = new JsonObject();
	json.addProperty("value", "from sendW Bridge");
    EventBus.send(TestAttributes.HELLO_ADDRESS, json.toString(), new Handler() {
      @Override
      public void handle(String message) {
        String value = new JsonParser().parse(message).getAsJsonObject().get("body")
        		.getAsJsonObject().get("value").getAsString();
        assertEquals("Hello from sendW Bridge", value);
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
  }

  @Test
  public void testRegister() throws InterruptedException {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    EventBus.registerHandler(TestAttributes.HELLO_ADDRESS, new Handler() {
      @Override
      public void handle(String message) {
        String value = new JsonParser().parse(message).getAsJsonObject().get("body")
        		.getAsJsonObject().get("value").getAsString();
        assertEquals("from sendW Bridge", value);
        countDownLatch.countDown();
      }
    });
	JsonObject json = new JsonObject();
	json.addProperty("value", "from sendW Bridge");
    EventBus.publish(TestAttributes.HELLO_ADDRESS, json.toString());
    countDownLatch.await();
  }

  //{"type":"err","message":"access_denied"}
//    @Test
//    public void testAccessDenied() throws InterruptedException {
//        EventBus.send("holla", Json.object().set("value", "from send Bridge").toString());
//
//    }


  //{"failureCode":-1,"failureType":"TIMEOUT","type":"message","message":"Timed out waiting for a reply"}
//    @Test
//    public void testReplyTimeout() throws InterruptedException {
//        final CountDownLatch countDownLatch = new CountDownLatch(1);
//        EventBus.send("hellonoreply", Json.object().set("value", "from sendW Bridge").toString(), new Handler() {
//            @Override
//            public void handle(String message) {
//                String value = Json.read(message).at("body").at("value").asString();
//                assertEquals("Hello from sendW Bridge", value);
//                countDownLatch.countDown();
//            }
//        });
//        countDownLatch.await();
//    }


}
