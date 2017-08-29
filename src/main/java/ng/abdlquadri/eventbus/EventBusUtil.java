package ng.abdlquadri.eventbus;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import mjson.Json;
import ng.abdlquadri.eventbus.handlers.Handler;
import ng.abdlquadri.eventbus.handlers.WriteHandler;
import ng.abdlquadri.eventbus.senders.ReplySender;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    int length = jsonObject.toString().getBytes().length;
    ByteBuf buffer = null;
    try {
      buffer = Unpooled.buffer()
        .writeInt(length)
        .writeBytes(jsonObject.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    log.log(Level.INFO, "Writing to wire.");

    if (channel != null) {
      final ChannelFuture channelFuture = channel.writeAndFlush(buffer);
      channelFuture.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {

          if (future.isDone() && future.isSuccess()) {
            writeHandler.written(true);
            log.log(Level.INFO, "Done Writing to wire.");
          } else {
            writeHandler.written(false);
            log.log(Level.SEVERE, "Failed Writing to wire.");
          }

        }
      });
    } else {
      throw new IllegalStateException("Channel is not connected. Make sure the server is  reachable and call EventBus" +
        ".connect() method first.");
    }
  }

  public static void addHandler(String address, Handler handler) {
    List<Handler> handlers = EventBus.handlers.get(address);
    log.log(Level.INFO, "Adding Handlers to Eventbus.");
    if (handlers == null) {

      handlers = new ArrayList<Handler>();
      handlers.add(handler);

      EventBus.handlers.put(address, handlers);
      log.log(Level.INFO, "Done Adding Handlers to Eventbus. # of Current Hanlders {1}", handlers.size());

    } else {

      if (EventBus.handlers.containsKey(address)) {
        EventBus.handlers.put(address, handlers);
      }
      log.log(Level.INFO, "Replaced Handler on Eventbus. # of Current Hanlders {1}", handlers.size());
    }

  }

  public static void addReplyHandler(String address, Handler handler) {
    log.log(Level.INFO, "Adding ReplyHandlers for Eventbus Address {1}", address);
    if (!EventBus.replyHandlers.containsKey(address)) {
      EventBus.replyHandlers.put(address, handler);
    }
    log.log(Level.INFO, "Done Adding ReplyHandlers for Eventbus Address {1}", address);
  }

  public static void addReplySender(String address, ReplySender sender) {
    log.log(Level.INFO, "Adding ReplySenders for Eventbus Address {1}", address);
    if (!EventBus.replySenders.containsKey(address)) {
      EventBus.replySenders.put(address, sender);
    }
    log.log(Level.INFO, "Done Adding ReplySenders for Eventbus Address {1}", address);
  }
}
