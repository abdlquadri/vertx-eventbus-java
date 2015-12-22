package ng.abdlquadri.eventbus.handlers;

import io.netty.channel.ChannelFuture;

/**
 * Created by abdlquadri on 12/20/15.
 */
public interface ConnectHandler {
    public void connected(boolean isConnected);

}
