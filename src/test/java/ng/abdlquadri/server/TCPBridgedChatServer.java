package ng.abdlquadri.server;

import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge;

import ng.abdlquadri.TestAttributes;

public class TCPBridgedChatServer extends AbstractVerticle {
  Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    vertx.eventBus().consumer(TestAttributes.PUBLISH_ADDRESS);

    vertx.eventBus().consumer(TestAttributes.HELLO_ADDRESS, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        message.reply(new JsonObject().put("value", "Hello " + message.body().getString("value")));
      }
    });

    TcpEventBusBridge bridge = TcpEventBusBridge.create(vertx, new BridgeOptions()
      .addInboundPermitted(new PermittedOptions().setAddress(TestAttributes.HELLO_ADDRESS))
      .addOutboundPermitted(new PermittedOptions().setAddress(TestAttributes.HELLO_ADDRESS))
    );


    bridge.listen(TestAttributes.PORT, new Handler<AsyncResult<TcpEventBusBridge>>() {
      @Override
      public void handle(AsyncResult<TcpEventBusBridge> res) {
        System.out.println("Ready");

        startFuture.complete();
      }
    });
  }
}
