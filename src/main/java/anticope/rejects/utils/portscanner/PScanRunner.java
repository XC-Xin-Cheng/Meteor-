package anticope.rejects.utils.portscanner;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PScanRunner {
   public boolean running = true;
   public int portsScanned = 0;
   ExecutorService es;
   List<Future<PortScannerManager.ScanResult>> futures = new ArrayList<>();
   Thread runner;

   public PScanRunner(
      InetAddress address, int threads, int threadDelay, int timeoutMS, Collection<Integer> ports, Consumer<List<PortScannerManager.ScanResult>> callback
   ) {
      this.runner = new Thread(() -> {
         this.es = Executors.newFixedThreadPool(threads);
         ports.forEach(port -> this.futures.add(this.isPortOpen(this.es, address.getHostAddress(), port, timeoutMS, threadDelay)));

         try {
            this.es.awaitTermination(200L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException var12) {
         }

         List<PortScannerManager.ScanResult> results = new ArrayList<>();

         for (Future<PortScannerManager.ScanResult> fsc : this.futures) {
            try {
               results.add(fsc.get());
            } catch (ExecutionException | InterruptedException var11) {
               var11.printStackTrace();
            }
         }

         callback.accept(results);
      });
      this.runner.start();
   }

   public void cancel() {
      this.running = false;
   }

   private Future<PortScannerManager.ScanResult> isPortOpen(ExecutorService es, String ip, int port, int timeout, int delay) {
      return es.submit(() -> {
         if (!this.running) {
            return new PortScannerManager.ScanResult(port, false);
         } else {
            Thread.sleep(delay);
            this.portsScanned++;

            try {
               Socket socket = new Socket();
               socket.connect(new InetSocketAddress(ip, port), timeout);
               socket.close();
               return new PortScannerManager.ScanResult(port, true);
            } catch (Exception var6) {
               return new PortScannerManager.ScanResult(port, false);
            }
         }
      });
   }
}
