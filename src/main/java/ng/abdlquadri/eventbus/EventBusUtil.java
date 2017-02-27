package ng.abdlquadri.eventbus;

import static ng.abdlquadri.eventbus.EventBus.globalConnectHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import mjson.Json;

import ng.abdlquadri.eventbus.handlers.Handler;
import ng.abdlquadri.eventbus.handlers.WriteHandler;
import ng.abdlquadri.eventbus.senders.ReplySender;

/**
 * Created by abdlquadri on 12/19/15.
 */
public class EventBusUtil {
  private static Logger log = Logger.getLogger(EventBusUtil.class.getName());

  private EventBusUtil() {
  }

  public static void sendPing(Channel channel) {

    String msg = Json.object()
      .set("type", "ping").toString();
    log.log(Level.INFO, "Sending Ping to Server.");
    writeToWire(channel, msg, new WriteHandler() {
      @Override
      public void written(boolean isWritten) {
        log.log(Level.INFO, "Done Sending Ping to Server.");
      }
    });
  }

  public static void writeToWire(final Channel channel, String jsonObject, final WriteHandler writeHandler) {
    int length = jsonObject.length();
    ByteBuf buffer = Unpooled.buffer()
      .writeInt(length)
      .writeBytes(jsonObject.getBytes());
    log.log(Level.INFO, "Writing to wire.");

    if (channel != null) {
      final ChannelFuture channelFuture = channel.writeAndFlush(buffer);
      channelFuture.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          if (channel.isActive()) {
            log.log(Level.INFO, "CHANNEL IS ACTIVE");
            if (future.isDone() && future.isSuccess()) {
              writeHandler.written(true);
              log.log(Level.INFO, "Done Writing to wire.");
            } else {
              writeHandler.written(false);
              log.log(Level.SEVERE, "Failed Writing to wire.");
            }
          } else {
            log.log(Level.SEVERE, "CHANNEL NOT ACTIVE");
            globalConnectHandler.onDisConnect(new IllegalStateException("You are disconnected from the EventBus"));

          }

        }
      });
    } else {
      throw new IllegalStateException("Channel is not connected. Make sure the server is reachable and call EventBus.connect() method first.");
    }
  }

  public static void addHandler(String address, Handler handler) {
    List<Handler> handlers = EventBus.handlers.get(address);
    log.log(Level.INFO, "Adding Handlers to Eventbus. # of Current Hanlders {1}", handlers.size());
    if (handlers == null) {

      handlers = new ArrayList<Handler>();
      handlers.add(handler);

      EventBus.handlers.put(address, handlers);
      log.log(Level.INFO, "Done Adding Handlers to Eventbus. # of Current Hanlders {1}", handlers.size());

    } else {
      handlers.add(handler);

      EventBus.handlers.replace(address, handlers);
      log.log(Level.INFO, "Replaced Handler on Eventbus. # of Current Hanlders {1}", handlers.size());
    }

  }

  public static void addReplyHandler(String address, Handler handler) {
    log.log(Level.INFO, "Adding ReplyHandlers for Eventbus Address {1}", address);
    EventBus.replyHandlers.putIfAbsent(address, handler);
    log.log(Level.INFO, "Done Adding ReplyHandlers for Eventbus Address {1}", address);
  }

  public static void addReplySender(String address, ReplySender sender) {
    log.log(Level.INFO, "Adding ReplySenders for Eventbus Address {1}", address);
    EventBus.replySenders.putIfAbsent(address, sender);
    log.log(Level.INFO, "Done Adding ReplySenders for Eventbus Address {1}", address);
  }
}
