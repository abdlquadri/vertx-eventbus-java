package ng.abdlquadri.eventbus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import mjson.Json;
import ng.abdlquadri.eventbus.handlers.Handler;
import ng.abdlquadri.eventbus.senders.ReplySender;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static ng.abdlquadri.eventbus.EventBus.*;
import static ng.abdlquadri.eventbus.EventBusUtil.addReplySender;
import static ng.abdlquadri.eventbus.EventBusUtil.sendPing;

/**
 * Created by abdlquadri on 12/9/15.
 */
public class EventBusFrameHandler extends SimpleChannelInboundHandler {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        sendPing(channel);
        ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendPing(channel);
            }
        }, 5, 5, TimeUnit.SECONDS);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inMsg = (ByteBuf) msg;
        int messageLength = inMsg.readInt();
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < messageLength; i++) {
            char c = (char) inMsg.readByte();
            message.append(c);
        }

        String eventBusMessage = message.toString();
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
                System.out.println("we are here");
                List<Handler> messageHandlers = handlers.get(stAddress);
                for (Handler h : messageHandlers) {

//                    if(type.equals("err")) //if we have error what to do
                    h.handle(eventBusMessage);

                }
            } else if (replyHandlers.containsKey(stAddress)) {
                Handler replyMessageHandlers = replyHandlers.get(stAddress);
                replyMessageHandlers.handle(eventBusMessage);
                replyHandlers.remove(stAddress);
            } else {
                if (type.toString().equals("err")) {
                    System.out.println(json.toString());

                } else {
                    System.out.println(json.toString());
                }
            }
        }
//        ctx.close();// should we really leave the connection opened
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}


