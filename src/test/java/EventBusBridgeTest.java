
import mjson.Json;
import ng.abdlquadri.eventbus.EventBus;
import ng.abdlquadri.eventbus.handlers.ConnectHandler;
import ng.abdlquadri.eventbus.handlers.Handler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;


/**
 * Created by user on 11/22/15.
 */
public class EventBusBridgeTest {

    @BeforeClass
    public static void createServer() throws InterruptedException {
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
    }

    @AfterClass
    public static void stopServer() {
        EventBus.close();
    }


    @Test
    public void testSend() throws InterruptedException {

        EventBus.send("hello", Json.object().set("value", "from send Bridge").toString());
    }

    @Test
    public void testSendWithReply() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        EventBus.send("hello", Json.object().set("value", "from sendW Bridge").toString(), new Handler() {
            @Override
            public void handle(String message) {
                String value = Json.read(message).at("body").at("value").asString();
                assertEquals("Hello from sendW Bridge", value);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }

    @Test
    public void testRegister() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        EventBus.registerHandler("hello", new Handler() {
            @Override
            public void handle(String message) {
                System.out.println("TEST " + message);
                assertEquals("some messgae", Json.read(message).at("body").at("value").asString());
                countDownLatch.countDown();
            }
        });

        EventBus.publish("hello", Json.object().set("value", "some messgae").toString());
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
