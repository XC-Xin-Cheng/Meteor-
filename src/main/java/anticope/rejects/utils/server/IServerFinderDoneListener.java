package anticope.rejects.utils.server;

public interface IServerFinderDoneListener {
   void onServerDone(ServerPinger var1);

   void onServerFailed(ServerPinger var1);
}
