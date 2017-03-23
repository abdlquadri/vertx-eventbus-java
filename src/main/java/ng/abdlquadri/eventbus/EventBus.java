package ng.abdlquadri.eventbus;

import static ng.abdlquadri.eventbus.EventBusUtil.addHandler;
import static ng.abdlquadri.eventbus.EventBusUtil.addReplyHandler;
import static ng.abdlquadri.eventbus.EventBusUtil.writeToWire;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import ng.abdlquadri.eventbus.handlers.ConnectHandler;
import ng.abdlquadri.eventbus.handlers.Handler;
import ng.abdlquadri.eventbus.handlers.WriteHandler;
import ng.abdlquadri.eventbus.senders.ReplySender;
import ng.abdlquadri.eventbus.util.EventBusMessageAttributes;

/**
 * Created by abdlquadri on 12/9/15.
 */
public class EventBus {
  private static Logger log = Logger.getLogger(EventBus.class.getName());

  public static Channel channel;
  public static final ConcurrentMap<String, List<Handler>> handlers = new ConcurrentHashMap<String, List<Handler>>();
  public static final ConcurrentMap<String, Handler> replyHandlers = new ConcurrentHashMap<String, Handler>();
  protected static HashMap<String, ReplySender> replySenders = new HashMap<String, ReplySender>();
  public static ConnectHandler globalConnectHandler;

  private EventBus() {
  }

  public static void send(String address, String jsonMessage, String jsonHeaders, Handler handler) {
    String replyAddress = UUID.randomUUID().toString();
    log.log(Level.FINE, "Making a SEND to EventBus Server");

   JsonObject json = new JsonObject();
   json.addProperty(EventBusMessageAttributes.TYPE, "send");
   json.addProperty(EventBusMessageAttributes.ADDRESS, address);
   json.addProperty(EventBusMessageAttributes.REPLY_ADDRESS, replyAddress);
   json.add(EventBusMessageAttributes.HEADERS, new JsonParser().parse(jsonHeaders));
   json.add(EventBusMessageAttributes.BODY, new JsonParser().parse(jsonMessage));;
    addReplyHandler(replyAddress, handler);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {
        log.log(Level.FINE, "Done Making a SEND to EventBus Server");
      }
    });
  }

  public static void send(String address, String jsonMessage, Handler handler) {
    String replyAddress = UUID.randomUUID().toString();
    JsonObject json = new JsonObject();
    json.addProperty(EventBusMessageAttributes.TYPE, "send");
    json.addProperty(EventBusMessageAttributes.ADDRESS, address);
    json.addProperty(EventBusMessageAttributes.REPLY_ADDRESS, replyAddress);
    json.add(EventBusMessageAttributes.HEADERS, new JsonObject());
    json.add(EventBusMessageAttributes.BODY, new JsonParser().parse(jsonMessage));;
    addReplyHandler(replyAddress, handler);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void send(String address, String jsonMessage, String jsonHeaders) {
    log.log(Level.FINE, "Making a SEND to EventBus Server");

    JsonObject json = new JsonObject();
    json.addProperty(EventBusMessageAttributes.TYPE, "send");
    json.addProperty(EventBusMessageAttributes.ADDRESS, address);
    json.add(EventBusMessageAttributes.HEADERS,  new JsonParser().parse(jsonHeaders));
    json.add(EventBusMessageAttributes.BODY,  new JsonParser().parse(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {
        log.log(Level.FINE, "Done Making a SEND to EventBus Server");

      }
    });
  }

  public static void publish(String address, String jsonMessage, String jsonHeaders) {
    log.log(Level.FINE, "Making a PUBLISH to EventBus Server");
    JsonObject json = new JsonObject();
    json.addProperty(EventBusMessageAttributes.TYPE, "publish");
    json.addProperty(EventBusMessageAttributes.ADDRESS, address);
    json.add(EventBusMessageAttributes.HEADERS,  new JsonParser().parse(jsonHeaders));
    json.add(EventBusMessageAttributes.BODY,  new JsonParser().parse(jsonMessage));;
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {
        log.log(Level.FINE, "Done Making a PUBLISH to EventBus Server");

      }
    });
  }

  public static void send(String address, String jsonMessage) {
   JsonObject json = new JsonObject();
   json.addProperty(EventBusMessageAttributes.TYPE, "send");
   json.addProperty(EventBusMessageAttributes.ADDRESS, address);
   json.add(EventBusMessageAttributes.HEADERS, new JsonObject());
   json.add(EventBusMessageAttributes.BODY,  new JsonParser().parse(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void publish(String address, String jsonMessage) {
    JsonObject json = new JsonObject();
    json.addProperty(EventBusMessageAttributes.TYPE, "publish");
    json.addProperty(EventBusMessageAttributes.ADDRESS, address);
    json.add(EventBusMessageAttributes.HEADERS, new JsonObject());
    json.add(EventBusMessageAttributes.BODY,  new JsonParser().parse(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }


  public static void registerHandler(String address, String jsonHeaders, Handler handler) {
    JsonObject json = new JsonObject();
    json.addProperty(EventBusMessageAttributes.TYPE, "register");
    json.addProperty(EventBusMessageAttributes.ADDRESS, address);
    json.add(EventBusMessageAttributes.HEADERS, new JsonParser().parse(jsonHeaders));

    addHandler(address, handler);

    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void registerHandler(String address, Handler handler) {
    JsonObject json = new JsonObject();
    json.addProperty(EventBusMessageAttributes.TYPE, "register");
    json.addProperty(EventBusMessageAttributes.ADDRESS, address);
    json.add(EventBusMessageAttributes.HEADERS, new JsonObject());

    addHandler(address, handler);

    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void unregisterHandler(String address, String jsonHeaders) {
    JsonObject json = new JsonObject();
    json.addProperty(EventBusMessageAttributes.TYPE, "unregister");
    json.addProperty(EventBusMessageAttributes.ADDRESS, address);
    json.add(EventBusMessageAttributes.HEADERS, new JsonParser().parse(jsonHeaders));

    handlers.remove(address);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void unregisterHandler(String address) {
    JsonObject json = new JsonObject();
    json.addProperty(EventBusMessageAttributes.TYPE, "unregister");
    json.addProperty(EventBusMessageAttributes.ADDRESS, address);
    json.add(EventBusMessageAttributes.HEADERS, new JsonObject());

    handlers.remove(address);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }


  public static void connect(String host, int port, final ConnectHandler connectHandler) {
    log.log(Level.FINE, "Connecting to EventBus Server");
    NioEventLoopGroup group = new NioEventLoopGroup();

    Bootstrap bootstrap = new Bootstrap();

    bootstrap.group(group)
      .channel(NioSocketChannel.class)
      .remoteAddress(new InetSocketAddress(host, port))
      .handler(new EventBusInitializer());

    final ChannelFuture channelFuture = bootstrap.connect();
    channelFuture.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess() && channelFuture.isDone() && channelFuture.channel().isActive()) {
          channel = future.channel();
          globalConnectHandler = connectHandler;
          connectHandler.onConnect(channelFuture.channel().isActive());
          log.log(Level.FINE, "Done Connecting to EventBus Server");

        } else {
          connectHandler.onConnect(false);
          
          // throw the exception OR log it, not both 
          // log.log(Level.SEVERE, "Failed Connecting to EventBus Server");

        }

      }
    });

  }

  public static void close() {
    if (channel != null) {
      final ChannelFuture closeFuture = channel.close();
      closeFuture.addListener(new ChannelFutureListener() {
        public void operationComplete(ChannelFuture future) throws Exception {
          if (future.isSuccess() && future.isDone()){
           // silent exit is better
           //  log.log(Level.SEVERE, "Channel Successful Closed");

          }else {
           // silent exit is better
           //   log.log(Level.SEVERE, "Channel Failed Closed {1}", future.cause());

          }
        }
      });
    } else {
      // silent exit is better 
      // log OR throw exception, never both
      // log.log(Level.SEVERE, "Channel is not connected. You can not close a non existent connection :). Make sure the server is reachable and call EventBus.connect() method first.");
      // throw new IllegalStateException("Channel is not connected. You can not close a non existent connection :). Make sure the server is reachable and call EventBus.connect() method first.");

    }
  }


}
