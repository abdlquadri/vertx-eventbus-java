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
import ng.abdlquadri.eventbus.senders.ReplySender;
import ng.abdlquadri.eventbus.util.EventBusMessageAttributes;

/**
 * Created by abdlquadri on 12/9/15.
 */
public class EventBus {
  private static Logger log = Logger.getLogger(EventBus.class.getName());
  private static NioEventLoopGroup group;
  private static Bootstrap bootstrap;
  public static Channel channel;
  public static final ConcurrentMap<String, List<Handler>> handlers = new ConcurrentHashMap<String, List<Handler>>();
  public static final ConcurrentMap<String, Handler> replyHandlers = new ConcurrentHashMap<String, Handler>();
  protected static HashMap<String, ReplySender> replySenders = new HashMap<String, ReplySender>();
  public static ConnectHandler globalConnectHandler;

  private EventBus() {
  }

  public static void send(String address, String jsonMessage, String jsonHeaders, Handler handler) {
    String replyAddress = UUID.randomUUID().toString();
    log.log(Level.INFO, "Making a SEND to EventBus Server");

    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "send")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.REPLY_ADDRESS, replyAddress)
      .set(EventBusMessageAttributes.HEADERS, Json.read(jsonHeaders))
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    addReplyHandler(replyAddress, handler);
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {
        log.log(Level.INFO, "Done Making a SEND to EventBus Server");
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
  public static void send(String address, String jsonMessage,String reply_address,Boolean nouse,Handler handler) {
    String replyAddress = reply_address;
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
    log.log(Level.INFO, "Making a SEND to EventBus Server");

    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "send")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.read(jsonHeaders))
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {
        log.log(Level.INFO, "Done Making a SEND to EventBus Server");

      }
    });
  }

  public static void publish(String address, String jsonMessage, String jsonHeaders) {
    log.log(Level.INFO, "Making a PUBLISH to EventBus Server");
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "publish")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.HEADERS, Json.read(jsonHeaders))
      .set(EventBusMessageAttributes.BODY, Json.read(jsonMessage));
    writeToWire(channel, json.toString(), new WriteHandler() {
      @Override
      public void written(boolean isWritten) {
        log.log(Level.INFO, "Done Making a PUBLISH to EventBus Server");

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
  public static void send(String address, String jsonMessage,String reply_address,Boolean nouse) {
    String replyAddress = reply_address;
    Json json = Json.object().set(EventBusMessageAttributes.TYPE, "send")
      .set(EventBusMessageAttributes.ADDRESS, address)
      .set(EventBusMessageAttributes.REPLY_ADDRESS, replyAddress)
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
    log.log(Level.INFO, "Connecting to EventBus Server");
//      final NioEventLoopGroup group = new NioEventLoopGroup();
  final  NioEventLoopGroup group =groupCache();
//    final Bootstrap bootstrap = new Bootstrap();
//
//    bootstrap.group(group)
//      .channel(NioSocketChannel.class)
//      .remoteAddress(new InetSocketAddress(host, port))
//      .handler(new EventBusInitializer());
final  Bootstrap  bootstrap =bootstrapCache(host,port);
    final ChannelFuture channelFuture = bootstrap.connect();
    channelFuture.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess() && channelFuture.isDone() && channelFuture.channel().isActive()) {
          channel = future.channel();
          globalConnectHandler = connectHandler;
          connectHandler.onConnect(channelFuture.channel().isActive());
          log.log(Level.INFO, "Done Connecting to EventBus Server");

        } else {
          connectHandler.onConnect(false);
//          group.shutdownGracefully();
          log.log(Level.SEVERE, "Failed Connecting to EventBus Server");

        }

      }
    });

  }

  private static   NioEventLoopGroup  groupCache(){
          if (group==null){
            group = new NioEventLoopGroup();
          }
          return  group;
  }

  private static  Bootstrap  bootstrapCache(String host, int port){
        if (bootstrap==null){
           bootstrap = new Bootstrap();

          bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(host, port))
            .handler(new EventBusInitializer());
        }
        return bootstrap;
  }
  public static void close() {
    if (channel != null) {
      final ChannelFuture closeFuture = channel.close();

      closeFuture.addListener(new ChannelFutureListener() {
        public void operationComplete(ChannelFuture future) throws Exception {
          if (future.isSuccess() && future.isDone()){
            log.log(Level.SEVERE, "Channel Successful Closed");

          }else {
            log.log(Level.SEVERE, "Channel Failed Closed {1}", future.cause());

          }
        }
      });
    } else {
      log.log(Level.SEVERE, "Channel is not connected. You can not close a non existent connection :). Make sure the server is reachable and call EventBus.connect() method first.");
      throw new IllegalStateException("Channel is not connected. You can not close a non existent connection :). Make sure the server is reachable and call EventBus.connect() method first.");

    }
  }


}
