package ng.abdlquadri.eventbus.handlers;

/**
 * Created by abdlquadri on 12/20/15.
 */
public interface ConnectHandler {
  public void onConnect(boolean isConnected);
  public void onDisConnect(Throwable cause);
}
