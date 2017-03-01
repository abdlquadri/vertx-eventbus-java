package ng.abdlquadri.eventbus;

import static ng.abdlquadri.eventbus.EventBus.channel;
import static ng.abdlquadri.eventbus.EventBus.handlers;
import static ng.abdlquadri.eventbus.EventBus.replyHandlers;
import static ng.abdlquadri.eventbus.EventBusUtil.addReplySender;
import static ng.abdlquadri.eventbus.EventBusUtil.sendPing;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import mjson.Json;

import ng.abdlquadri.eventbus.handlers.Handler;
import ng.abdlquadri.eventbus.senders.ReplySender;

/**
 * Created by abdlquadri on 12/9/15.
 */
public class EventBusFrameHandler extends SimpleChannelInboundHandler {
  private Logger log = Logger.getLogger(EventBusFrameHandler.class.getName());

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    if (channel.isActive()) {
      sendPing(channel);
    }
    ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        if (channel.isActive()) {
          sendPing(channel);
        }
      }
    }, 5, 5, TimeUnit.SECONDS);

  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf inMsg = (ByteBuf) msg;
    String eventBusMessage = messageBufferToString(inMsg);
    Json json = Json.read(eventBusMessage);
    final Json replyAddress = json.at("replyAddress");
    Json address = json.at("address");
    Json type = json.at("type");
    if (replyAddress != null) {
      addReplySender(replyAddress.toString(), new ReplySender() {
        @Override
        public void send(String replyMessage) {
          EventBus.send(replyAddress.toString(), replyMessage);

        }

        @Override
        public void send(String replyMessage, String headers) {
          EventBus.send(replyAddress.toString(), replyMessage, headers);

        }

        @Override
        public void send(String replyMessage, String headers, Handler handler) {
          EventBus.send(replyAddress.toString(), replyMessage, headers, handler);
        }
      });
    }

    if (address != null) {
      String stAddress = address.asString();
      if (handlers.containsKey(stAddress)) {
        List<Handler> messageHandlers = handlers.get(stAddress);
        for (Handler h : messageHandlers) {

          h.handle(eventBusMessage);

        }
      } else if (replyHandlers.containsKey(stAddress)) {
        Handler replyMessageHandlers = replyHandlers.get(stAddress);
        replyMessageHandlers.handle(eventBusMessage);
        replyHandlers.remove(stAddress);
      } else {
        if ("err".equals(type.toString())) {
          //TODO what do we do?
          log.log(Level.WARNING, json.toString());

        } else {
          log.log(Level.SEVERE, json.toString());

        }
      }
    }
    //TODO should we really leave the connection opened
    //TODO        ctx.close();
  }

  private String messageBufferToString(ByteBuf inMsg) {
    int messageLength = inMsg.readInt();
    StringBuilder message = new StringBuilder();
    for (int i = 0; i < messageLength; i++) {
      char c = (char) inMsg.readByte();
      message.append(c);
    }
    return message.toString();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.log(Level.WARNING, cause.getMessage());
    ctx.close();
  }


}


