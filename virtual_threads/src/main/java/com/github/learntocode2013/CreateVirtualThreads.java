package com.github.learntocode2013;

import java.time.Duration;
import lombok.SneakyThrows;

public class CreateVirtualThreads
{
    public static void main( String[] args ) {
        new CreateVirtualThreads().run();
    }

    @SneakyThrows
    private void run() {
        final Object monitor = new Object();
        Runnable printTask = () -> syncOnMonitor(monitor);
        //-- Option-1
        var vt = Thread.startVirtualThread(printTask);
        var vt1 = Thread.startVirtualThread(printTask);
        vt.join();
        vt1.join();
        //-- Option-2
//        OfVirtual vt = Thread.ofVirtual();
//        vt.start(printTask).join();
        //-- Option-3
//        try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
//            es.submit(printTask);
//            es.submit(printTask);
//            es.submit(printTask);
//            es.submit(printTask);
//            es.submit(printTask);
//        }
    }

    private void syncOnMonitor(Object monitor) {
        synchronized (monitor) {
            try {
                Thread.sleep(Duration.ofSeconds(1));
                System.out.printf("I am executing inside a virtual thread: %s %n",
                    Thread.currentThread());
            } catch (InterruptedException iex) {

            }
        }
        System.out.printf("Done with sync %n");
    }

//    private void demoContinuation() {
//        var scope = new ContinuationScope("scope");
//        var continuation = new Continuation(scope, () -> countUp(scope));
//        var scanner = new Scanner(System.in);
//        while(!continuation.isDone()) {
//            System.out.println("Press enter key to run one more step");
//            scanner.nextLine();
//            continuation.run();
//        }
//    }
//
//    private void countUp(ContinuationScope scope) {
//        for (var i = 0; i < 10; i++) {
//            System.out.println(i);
//            Continuation.yield(scope);
//        }
//    }
}
