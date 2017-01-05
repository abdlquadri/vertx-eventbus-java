package ng.abdlquadri.eventbus.senders;

import ng.abdlquadri.eventbus.handlers.Handler;

/**
 * Created by abdlquadri on 12/20/15.
 */
public interface ReplySender {
  void send(String replyMessage);

  void send(String replyMessage, String headers);

  void send(String replyMessage, String headers, Handler handler);
}
