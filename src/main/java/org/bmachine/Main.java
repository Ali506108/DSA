package org.bmachine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static void main() throws InterruptedException {
        Thread virtual = Thread.ofVirtual().start(() ->
                System.out.println("Hello how are you? I'm from: " + Thread.currentThread()));

        virtual.join();

        System.out.println("Main done. Virtual thread was like a goroutine!");

        try(ExecutorService service  = Executors.newFixedThreadPool(5)){
            for (int i = 0; i < 10; i++) {
                service.submit(() -> {
                    IO.println("current Thread is  " + Thread.currentThread().getName());
                });
            }
        }

    }



}
