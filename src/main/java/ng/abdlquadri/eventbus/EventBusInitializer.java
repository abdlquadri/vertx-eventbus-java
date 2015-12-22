package ng.abdlquadri.eventbus;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created by abdlquadri on 12/9/15.
 */
public class EventBusInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65*1024,0,4));
        pipeline.addLast(new EventBusFrameHandler());

    }
}
