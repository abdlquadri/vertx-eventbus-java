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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abdlquadri on 12/19/15.
 */
public class EventBusUtil {


    public static void sendPing(Channel channel) {

        String msg = Json.object()
                .set("type", "ping").toString();

        writeToWire(channel, msg, new WriteHandler() {
            @Override
            public void written(boolean isWritten) {

            }
        });
    }

    public static void writeToWire(Channel channel, String jsonObject, final WriteHandler writeHandler) {
        int length = jsonObject.length();
        ByteBuf buffer = Unpooled.buffer()
                .writeInt(length)
                .writeBytes(jsonObject.getBytes());

        ChannelFuture channelFuture = channel.writeAndFlush(buffer);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isDone() && future.isSuccess()) {
                    writeHandler.written(true);
                } else {
                    writeHandler.written(false);
                }
            }
        });
    }

    public static void addHandler(String address, Handler handler) {
        List<Handler> handlers = EventBus.handlers.get(address);
        if (handlers == null) {

            handlers = new ArrayList<Handler>();
            handlers.add(handler);

            EventBus.handlers.put(address, handlers);
        } else {
            handlers.add(handler);

            EventBus.handlers.replace(address, handlers);

        }
    }

    public static void addReplyHandler(String address, Handler handler) {
        EventBus.replyHandlers.putIfAbsent(address, handler);
    }

    public static void addReplySender(String address, ReplySender sender) {
        EventBus.replySenders.putIfAbsent(address, sender);
    }
}
