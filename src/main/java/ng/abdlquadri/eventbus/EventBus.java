package ng.abdlquadri.eventbus;

import static ng.abdlquadri.eventbus.EventBusUtil.addHandler;
import static ng.abdlquadri.eventbus.EventBusUtil.addReplyHandler;
import static ng.abdlquadri.eventbus.EventBusUtil.writeToWire;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import mjson.Json;
import ng.abdlquadri.eventbus.handlers.ConnectHandler;
import ng.abdlquadri.eventbus.handlers.Handler;
import ng.abdlquadri.eventbus.handlers.WriteHandler;
import ng.abdlquadri.eventbus.util.EventBusMessageAttributes;

/**
 * Created by abdlquadri on 12/9/15.
 */
public class EventBus {

  private static Channel channel;
  private static final ConcurrentHashMap<String, List<Handler>> handlers = new ConcurrentHashMap<String, List<Handler>>();

  private EventBus() {
  }

  public static void send(String address, String jsonMessage, String jsonHeaders, Handler handler) {
    String replyAddress = UUID.randomUUID().toString();

    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "send")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.REPLY_ADDRESS, replyAddress)
      .set(EventBusMessageAttributes.HEADERS, Json.read(jsonHeaders))
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    addReplyHandler(replyAddress, handler);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void send(String address, String jsonMessage, Handler handler) {
    String replyAddress = UUID.randomUUID().toString();
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "send")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.REPLY_ADDRESS, replyAddress)
      .set(EventBusMessageAttributes.HEADERS, Json.object())
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    addReplyHandler(replyAddress, handler);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void send(String address, String jsonMessage, String jsonHeaders) {
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "send")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.read(jsonHeaders))
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void publish(String address, String jsonMessage, String jsonHeaders) {
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "publish")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.read(jsonHeaders))
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void send(String address, String jsonMessage) {
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "send")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.object())
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void publish(String address, String jsonMessage) {
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "publish")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.object())
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }


  public static void registerHandler(String address, String jsonHeaders, Handler handler) {
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "register")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.read(jsonHeaders));

    addHandler(address, handler);

    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void registerHandler(String address, Handler handler) {
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "register")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.object());

    addHandler(address, handler);

    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void unregisterHandler(String address, String jsonHeaders) {
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "unregister")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.read(jsonHeaders));

    handlers.remove(address);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }

  public static void unregisterHandler(String address) {
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "unregister")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.object());

    handlers.remove(address);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {

      }
    });
  }


  public static void connect(String host, int port, final ConnectHandler connectHandler) {

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
          connectHandler.connected(channelFuture.channel().isActive());
        } else {
          connectHandler.connected(false);
        }

      }
    });

  }

  public static void close() {
    if (channel != null) {
      channel.close();
    } else {
      throw new IllegalStateException("Channel is not connected. You can not close a non existent connection :). Make sure the server is reachable and call EventBus.connect() method first.");

    }
  }


}
