import mjson.Json;
import ng.abdlquadri.eventbus.EventBus;
import ng.abdlquadri.eventbus.handlers.ConnectHandler;
import ng.abdlquadri.eventbus.handlers.Handler;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static ng.abdlquadri.eventbus.EventBus.channel;
import static ng.abdlquadri.eventbus.EventBusUtil.sendPing;

/**
 * Created by user on 12/9/15.
 */
public class Test {


    public static void main(String[] args) {
       final CountDownLatch countDownLatch = new CountDownLatch(1);
        EventBus.connect("127.0.0.1", 7000, new ConnectHandler() {
            @Override
            public void connected(boolean isConnected) {
                if (isConnected) {

                    EventBus.registerHandler("chat.to.client", new Handler() {
                        @Override
                        public void handle(String message) {
                            System.out.println(message);
                        }
                    });
                    EventBus.send("chat.to.server", Json.object().set("mesage", "Message").toString());

//                    EventBus.send("hello", Json.object().set("value", "from clitn Bridge").toString(), new Handler() {
//                        @Override
//                        public void handle(String message) {
//                            System.out.println("Response " + message);
//                            String address = Json.read(message).at("address").asString();
//                            System.out.println(address);
//                            countDownLatch.countDown();
//
//                        }
//                    });
                }
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        EventBus.channel.eventLoop().scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                EventBus.send("hello", Json.object().set("value", "from clitn Bridge").toString());
//            }
//        }, 5, 5, TimeUnit.SECONDS);
//        EventBus.send("hello", Json.object().set("value", "from clitn Bridge").toString());

        System.out.println("Can we get here");
    }
}
