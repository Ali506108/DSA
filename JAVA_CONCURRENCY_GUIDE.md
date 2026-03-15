# Java Modern Concurrency for Backend High-Load Systems

## Table of Contents
1. [Virtual Threads (Project Loom)](#virtual-threads)
2. [Thread Fundamentals](#thread-fundamentals)
3. [ExecutorService & Thread Pools](#executorservice)
4. [CompletableFuture & Async Programming](#completablefuture)
5. [Synchronization & Locks](#synchronization)
6. [Concurrent Collections](#concurrent-collections)
7. [Atomic Variables](#atomic-variables)
8. [CountDownLatch, CyclicBarrier, Phaser](#coordination)
9. [Semaphore & Rate Limiting](#semaphore)
10. [Fork/Join Framework](#forkjoin)
11. [Reactive Programming (Project Reactor)](#reactive)
12. [Best Practices for High-Load Systems](#best-practices)

---

## 1. Virtual Threads (Project Loom) {#virtual-threads}

**Available in Java 21+** - Lightweight threads managed by JVM, not OS.

### Basic Usage
```java
// Create and start virtual thread
Thread vThread = Thread.ofVirtual().start(() -> {
    System.out.println("Running on: " + Thread.currentThread());
});
vThread.join();

// Factory pattern
Thread.Builder builder = Thread.ofVirtual().name("worker-", 0);
Thread t1 = builder.start(() -> System.out.println("Task 1"));
Thread t2 = builder.start(() -> System.out.println("Task 2"));
```

### Virtual Thread Executor
```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return "Done";
        });
    }
} // Auto-shutdown
```

### When to Use Virtual Threads
- **High I/O operations**: Database calls, HTTP requests, file operations
- **Blocking operations**: Thread.sleep(), blocking I/O
- **Massive concurrency**: Millions of concurrent tasks

### When NOT to Use
- **CPU-intensive tasks**: Use platform threads or ForkJoinPool
- **Pinning scenarios**: synchronized blocks, native methods

---

## 2. Thread Fundamentals {#thread-fundamentals}

### Creating Threads
```java
// Method 1: Extend Thread
class MyThread extends Thread {
    public void run() {
        System.out.println("Thread running");
    }
}
new MyThread().start();

// Method 2: Implement Runnable
Thread t = new Thread(() -> System.out.println("Lambda runnable"));
t.start();

// Method 3: Platform thread builder
Thread.ofPlatform()
    .name("worker")
    .priority(Thread.MAX_PRIORITY)
    .start(() -> System.out.println("Platform thread"));
```

### Thread States
```java
Thread t = new Thread(() -> {
    try {
        Thread.sleep(1000); // TIMED_WAITING
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

System.out.println(t.getState()); // NEW
t.start();
System.out.println(t.getState()); // RUNNABLE
t.join();
System.out.println(t.getState()); // TERMINATED
```

### Thread Interruption
```java
Thread t = new Thread(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore flag
            break;
        }
    }
});
t.start();
Thread.sleep(500);
t.interrupt(); // Signal to stop
```

---

## 3. ExecutorService & Thread Pools {#executorservice}

### Fixed Thread Pool
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    int taskId = i;
    executor.submit(() -> {
        System.out.println("Task " + taskId + " on " + Thread.currentThread().getName());
    });
}
executor.shutdown();
executor.awaitTermination(1, TimeUnit.MINUTES);
```

### Cached Thread Pool
```java
// Creates threads as needed, reuses idle threads
ExecutorService executor = Executors.newCachedThreadPool();
```

### Single Thread Executor
```java
// Guarantees sequential execution
ExecutorService executor = Executors.newSingleThreadExecutor();
```

### Scheduled Executor
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

// Run once after delay
scheduler.schedule(() -> System.out.println("Delayed task"), 5, TimeUnit.SECONDS);

// Run periodically (fixed rate)
scheduler.scheduleAtFixedRate(() -> 
    System.out.println("Every 10s"), 0, 10, TimeUnit.SECONDS);

// Run periodically (fixed delay)
scheduler.scheduleWithFixedDelay(() -> 
    System.out.println("10s after previous"), 0, 10, TimeUnit.SECONDS);
```

### Custom ThreadPoolExecutor
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5,                      // corePoolSize
    10,                     // maximumPoolSize
    60L,                    // keepAliveTime
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100), // workQueue
    new ThreadPoolExecutor.CallerRunsPolicy() // rejectionHandler
);

// Rejection policies:
// - AbortPolicy: Throws RejectedExecutionException
// - CallerRunsPolicy: Runs task in caller's thread
// - DiscardPolicy: Silently discards task
// - DiscardOldestPolicy: Discards oldest unhandled task
```

### Work Stealing Pool
```java
// Uses ForkJoinPool, good for CPU-intensive tasks
ExecutorService executor = Executors.newWorkStealingPool();
```

---

## 4. CompletableFuture & Async Programming {#completablefuture}

### Basic Usage
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    Thread.sleep(1000);
    return "Result";
});

String result = future.get(); // Blocking
String result2 = future.join(); // Blocking, unchecked exception
```

### Chaining Operations
```java
CompletableFuture.supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World")
    .thenApply(String::toUpperCase)
    .thenAccept(System.out::println)
    .join();
```

### Combining Futures
```java
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "World");

// Combine both results
CompletableFuture<String> combined = f1.thenCombine(f2, (s1, s2) -> s1 + " " + s2);

// Wait for both
CompletableFuture<Void> both = CompletableFuture.allOf(f1, f2);

// Wait for any
CompletableFuture<Object> any = CompletableFuture.anyOf(f1, f2);
```

### Error Handling
```java
CompletableFuture.supplyAsync(() -> {
    if (Math.random() > 0.5) throw new RuntimeException("Error");
    return "Success";
})
.exceptionally(ex -> "Fallback: " + ex.getMessage())
.thenAccept(System.out::println);

// Or handle both success and error
future.handle((result, ex) -> {
    if (ex != null) return "Error: " + ex.getMessage();
    return result;
});
```

### Async vs Sync Methods
```java
// Runs in same thread
future.thenApply(s -> s.toUpperCase());

// Runs in ForkJoinPool.commonPool()
future.thenApplyAsync(s -> s.toUpperCase());

// Runs in custom executor
ExecutorService executor = Executors.newFixedThreadPool(10);
future.thenApplyAsync(s -> s.toUpperCase(), executor);
```

### Real-World Example: Parallel API Calls
```java
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> fetchUser(userId));
CompletableFuture<List<Order>> ordersFuture = CompletableFuture.supplyAsync(() -> fetchOrders(userId));
CompletableFuture<Profile> profileFuture = CompletableFuture.supplyAsync(() -> fetchProfile(userId));

CompletableFuture<UserData> result = userFuture
    .thenCombine(ordersFuture, (user, orders) -> new UserData(user, orders))
    .thenCombine(profileFuture, (userData, profile) -> {
        userData.setProfile(profile);
        return userData;
    });

UserData data = result.join();
```


---

## 5. Synchronization & Locks {#synchronization}

### Synchronized Keyword
```java
class Counter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
    
    public synchronized int getCount() {
        return count;
    }
}

// Synchronized block
public void method() {
    synchronized(this) {
        // Critical section
    }
}
```

### ReentrantLock
```java
class SafeCounter {
    private final Lock lock = new ReentrantLock();
    private int count = 0;
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock(); // Always unlock in finally
        }
    }
    
    public boolean tryIncrement() {
        if (lock.tryLock()) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }
}
```

### ReadWriteLock
```java
class Cache {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Map<String, String> cache = new HashMap<>();
    
    public String get(String key) {
        rwLock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    public void put(String key, String value) {
        rwLock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

### StampedLock (Java 8+)
```java
class Point {
    private final StampedLock lock = new StampedLock();
    private double x, y;
    
    // Optimistic read
    public double distanceFromOrigin() {
        long stamp = lock.tryOptimisticRead();
        double currentX = x, currentY = y;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }
    
    public void move(double deltaX, double deltaY) {
        long stamp = lock.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
```

---

## 6. Concurrent Collections {#concurrent-collections}

### ConcurrentHashMap
```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Thread-safe operations
map.put("key", 1);
map.putIfAbsent("key", 2); // Returns 1
map.computeIfAbsent("newKey", k -> 100);
map.computeIfPresent("key", (k, v) -> v + 1);
map.merge("key", 1, Integer::sum);

// Atomic operations
map.compute("counter", (k, v) -> v == null ? 1 : v + 1);

// Bulk operations (parallel)
map.forEach(1, (k, v) -> System.out.println(k + "=" + v));
map.search(1, (k, v) -> v > 10 ? k : null);
map.reduce(1, (k, v) -> v, Integer::sum);
```

### CopyOnWriteArrayList
```java
// Good for read-heavy, write-rare scenarios
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
list.add("item1");
list.add("item2");

// Iterator never throws ConcurrentModificationException
for (String item : list) {
    System.out.println(item);
}
```

### ConcurrentLinkedQueue
```java
ConcurrentLinkedQueue<Task> queue = new ConcurrentLinkedQueue<>();
queue.offer(new Task()); // Add
Task task = queue.poll(); // Remove and return
Task peek = queue.peek(); // Return without removing
```

### BlockingQueue Implementations
```java
// ArrayBlockingQueue - bounded
BlockingQueue<String> bounded = new ArrayBlockingQueue<>(100);

// LinkedBlockingQueue - optionally bounded
BlockingQueue<String> unbounded = new LinkedBlockingQueue<>();

// PriorityBlockingQueue - ordered by priority
BlockingQueue<Task> priority = new PriorityBlockingQueue<>();

// Producer-Consumer pattern
class Producer implements Runnable {
    private final BlockingQueue<String> queue;
    
    public void run() {
        try {
            queue.put("item"); // Blocks if full
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer implements Runnable {
    private final BlockingQueue<String> queue;
    
    public void run() {
        try {
            String item = queue.take(); // Blocks if empty
            process(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### ConcurrentSkipListMap/Set
```java
// Sorted concurrent map
ConcurrentSkipListMap<Integer, String> sortedMap = new ConcurrentSkipListMap<>();
sortedMap.put(3, "three");
sortedMap.put(1, "one");
sortedMap.put(2, "two");

// Always sorted
sortedMap.forEach((k, v) -> System.out.println(k + "=" + v)); // 1, 2, 3
```

---

## 7. Atomic Variables {#atomic-variables}

### AtomicInteger/Long/Boolean
```java
AtomicInteger counter = new AtomicInteger(0);

// Atomic operations
counter.incrementAndGet(); // ++counter
counter.getAndIncrement(); // counter++
counter.addAndGet(5);      // counter += 5
counter.compareAndSet(5, 10); // CAS operation

// Use in high-contention scenarios
class Statistics {
    private final AtomicLong requests = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    
    public void recordRequest() {
        requests.incrementAndGet();
    }
    
    public void recordError() {
        errors.incrementAndGet();
    }
}
```

### AtomicReference
```java
class Node {
    String value;
    Node next;
}

AtomicReference<Node> head = new AtomicReference<>(new Node());

// Lock-free update
Node oldHead, newHead;
do {
    oldHead = head.get();
    newHead = new Node();
    newHead.next = oldHead;
} while (!head.compareAndSet(oldHead, newHead));
```

### LongAdder/DoubleAdder
```java
// Better performance than AtomicLong under high contention
LongAdder counter = new LongAdder();

// Multiple threads can increment
counter.increment();
counter.add(5);

// Get sum (not atomic)
long total = counter.sum();

// Use case: metrics, counters in high-throughput systems
```

### AtomicIntegerArray
```java
AtomicIntegerArray array = new AtomicIntegerArray(10);
array.set(0, 100);
array.compareAndSet(0, 100, 200);
int value = array.getAndIncrement(0);
```

---

## 8. CountDownLatch, CyclicBarrier, Phaser {#coordination}

### CountDownLatch
```java
// Wait for N events before proceeding
CountDownLatch latch = new CountDownLatch(3);

// Worker threads
for (int i = 0; i < 3; i++) {
    new Thread(() -> {
        System.out.println("Task completed");
        latch.countDown();
    }).start();
}

// Main thread waits
latch.await(); // Blocks until count reaches 0
System.out.println("All tasks completed");

// Use case: Wait for services to start
class Application {
    private final CountDownLatch startupLatch = new CountDownLatch(3);
    
    public void start() throws InterruptedException {
        startDatabase();
        startCache();
        startWebServer();
        startupLatch.await(30, TimeUnit.SECONDS);
        System.out.println("Application ready");
    }
    
    private void startDatabase() {
        new Thread(() -> {
            // Initialize DB
            startupLatch.countDown();
        }).start();
    }
}
```

### CyclicBarrier
```java
// Reusable barrier for coordinating threads
CyclicBarrier barrier = new CyclicBarrier(3, () -> {
    System.out.println("All threads reached barrier");
});

for (int i = 0; i < 3; i++) {
    new Thread(() -> {
        try {
            System.out.println("Working...");
            barrier.await(); // Wait for others
            System.out.println("Continuing...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

// Use case: Parallel computation phases
class ParallelProcessor {
    private final CyclicBarrier barrier;
    
    public void process() {
        barrier = new CyclicBarrier(numThreads, () -> mergeResults());
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                processChunk();
                barrier.await(); // Wait for all chunks
                processNextPhase();
            });
        }
    }
}
```

### Phaser
```java
// More flexible than CyclicBarrier
Phaser phaser = new Phaser(1); // Register main thread

for (int i = 0; i < 3; i++) {
    phaser.register(); // Register each worker
    new Thread(() -> {
        System.out.println("Phase 1");
        phaser.arriveAndAwaitAdvance();
        
        System.out.println("Phase 2");
        phaser.arriveAndAwaitAdvance();
        
        phaser.arriveAndDeregister(); // Done
    }).start();
}

phaser.arriveAndDeregister(); // Main thread done
```

---

## 9. Semaphore & Rate Limiting {#semaphore}

### Semaphore
```java
// Limit concurrent access to resource
Semaphore semaphore = new Semaphore(3); // 3 permits

public void accessResource() {
    try {
        semaphore.acquire(); // Get permit
        try {
            // Access limited resource
            System.out.println("Accessing resource");
        } finally {
            semaphore.release(); // Return permit
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}

// Try acquire with timeout
if (semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
    try {
        // Access resource
    } finally {
        semaphore.release();
    }
}
```

### Connection Pool Example
```java
class ConnectionPool {
    private final Semaphore available;
    private final List<Connection> connections;
    
    public ConnectionPool(int size) {
        available = new Semaphore(size, true); // Fair mode
        connections = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            connections.add(new Connection());
        }
    }
    
    public Connection getConnection() throws InterruptedException {
        available.acquire();
        return getNextAvailableConnection();
    }
    
    public void returnConnection(Connection conn) {
        if (markAsUnused(conn)) {
            available.release();
        }
    }
}
```

### Rate Limiter (Token Bucket)
```java
class RateLimiter {
    private final Semaphore semaphore;
    private final int maxPermits;
    private final long refillPeriodMs;
    
    public RateLimiter(int permitsPerSecond) {
        this.maxPermits = permitsPerSecond;
        this.semaphore = new Semaphore(permitsPerSecond);
        this.refillPeriodMs = 1000;
        startRefillThread();
    }
    
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }
    
    private void startRefillThread() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            int available = semaphore.availablePermits();
            if (available < maxPermits) {
                semaphore.release(maxPermits - available);
            }
        }, refillPeriodMs, refillPeriodMs, TimeUnit.MILLISECONDS);
    }
}
```


---

## 10. Fork/Join Framework {#forkjoin}

### RecursiveTask (Returns Result)
```java
class SumTask extends RecursiveTask<Long> {
    private final long[] array;
    private final int start, end;
    private static final int THRESHOLD = 1000;
    
    public SumTask(long[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            // Direct computation
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // Split task
            int mid = (start + end) / 2;
            SumTask left = new SumTask(array, start, mid);
            SumTask right = new SumTask(array, mid, end);
            
            left.fork(); // Async execute
            long rightResult = right.compute(); // Execute in current thread
            long leftResult = left.join(); // Wait for result
            
            return leftResult + rightResult;
        }
    }
}

// Usage
ForkJoinPool pool = ForkJoinPool.commonPool();
long[] array = new long[10_000_000];
long sum = pool.invoke(new SumTask(array, 0, array.length));
```

### RecursiveAction (No Result)
```java
class SortTask extends RecursiveAction {
    private final int[] array;
    private final int start, end;
    private static final int THRESHOLD = 100;
    
    @Override
    protected void compute() {
        if (end - start <= THRESHOLD) {
            Arrays.sort(array, start, end);
        } else {
            int mid = (start + end) / 2;
            invokeAll(
                new SortTask(array, start, mid),
                new SortTask(array, mid, end)
            );
            merge(array, start, mid, end);
        }
    }
}
```

### Parallel Streams (Uses ForkJoinPool)
```java
List<Integer> numbers = IntStream.range(0, 1_000_000)
    .boxed()
    .collect(Collectors.toList());

// Parallel processing
long sum = numbers.parallelStream()
    .filter(n -> n % 2 == 0)
    .mapToLong(Integer::longValue)
    .sum();

// Custom ForkJoinPool
ForkJoinPool customPool = new ForkJoinPool(4);
customPool.submit(() -> 
    numbers.parallelStream().forEach(this::process)
).join();
```

---

## 11. Reactive Programming (Project Reactor) {#reactive}

### Dependencies
```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
    <version>3.6.0</version>
</dependency>
```

### Mono (0 or 1 element)
```java
Mono<String> mono = Mono.just("Hello")
    .map(String::toUpperCase)
    .filter(s -> s.length() > 3)
    .defaultIfEmpty("Default");

String result = mono.block(); // Blocking

// Async subscription
mono.subscribe(
    value -> System.out.println("Got: " + value),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);
```

### Flux (0 to N elements)
```java
Flux<Integer> flux = Flux.range(1, 10)
    .filter(n -> n % 2 == 0)
    .map(n -> n * 2)
    .delayElements(Duration.ofMillis(100));

flux.subscribe(System.out::println);

// From collection
Flux<String> fromList = Flux.fromIterable(Arrays.asList("a", "b", "c"));

// Generate
Flux<Integer> generated = Flux.generate(
    () -> 0,
    (state, sink) -> {
        sink.next(state);
        if (state == 10) sink.complete();
        return state + 1;
    }
);
```

### Combining Streams
```java
Mono<User> user = fetchUser(id);
Mono<Orders> orders = fetchOrders(id);

// Combine
Mono<UserData> combined = Mono.zip(user, orders)
    .map(tuple -> new UserData(tuple.getT1(), tuple.getT2()));

// Merge multiple Flux
Flux<String> merged = Flux.merge(flux1, flux2, flux3);

// Concat (sequential)
Flux<String> concatenated = Flux.concat(flux1, flux2);
```

### Error Handling
```java
Mono<String> mono = Mono.error(new RuntimeException("Error"))
    .onErrorReturn("Fallback")
    .onErrorResume(e -> Mono.just("Alternative"))
    .retry(3)
    .timeout(Duration.ofSeconds(5));
```

### Backpressure
```java
Flux.range(1, 1000)
    .onBackpressureBuffer(100)
    .onBackpressureDrop()
    .onBackpressureLatest()
    .subscribe(new BaseSubscriber<Integer>() {
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            request(10); // Request 10 items
        }
        
        @Override
        protected void hookOnNext(Integer value) {
            process(value);
            request(1); // Request next item
        }
    });
```

### Schedulers
```java
// Parallel scheduler (CPU-bound)
Flux.range(1, 100)
    .parallel()
    .runOn(Schedulers.parallel())
    .map(this::cpuIntensiveTask)
    .sequential()
    .subscribe();

// Bounded elastic (I/O-bound)
Mono.fromCallable(() -> blockingDatabaseCall())
    .subscribeOn(Schedulers.boundedElastic())
    .subscribe();

// Single thread
flux.subscribeOn(Schedulers.single());

// Immediate (current thread)
flux.subscribeOn(Schedulers.immediate());
```

---

## 12. Best Practices for High-Load Systems {#best-practices}

### 1. Choose Right Concurrency Model

```java
// I/O-bound: Use Virtual Threads (Java 21+)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> callExternalAPI());
    }
}

// CPU-bound: Use ForkJoinPool
ForkJoinPool.commonPool().invoke(new ComputeTask());

// Mixed: Use custom ThreadPoolExecutor
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    Runtime.getRuntime().availableProcessors(),
    Runtime.getRuntime().availableProcessors() * 2,
    60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(1000)
);
```

### 2. Avoid Thread Leaks

```java
// Always use try-with-resources
try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
    // Submit tasks
} // Auto-shutdown

// Or explicit shutdown
ExecutorService executor = Executors.newFixedThreadPool(10);
try {
    // Submit tasks
} finally {
    executor.shutdown();
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow();
    }
}
```

### 3. Thread Pool Sizing

```java
// CPU-bound tasks
int cpuBound = Runtime.getRuntime().availableProcessors();

// I/O-bound tasks (Little's Law)
// threads = targetCPU * (1 + waitTime/computeTime)
int ioBound = cpuBound * (1 + 50); // If 50x more wait than compute

// Practical example
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    cpuBound,           // Core threads
    cpuBound * 2,       // Max threads
    60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(1000),
    new ThreadPoolExecutor.CallerRunsPolicy()
);
```

### 4. Avoid Shared Mutable State

```java
// Bad: Shared mutable state
class BadCounter {
    private int count = 0; // Race condition
    public void increment() { count++; }
}

// Good: Immutable or thread-safe
class GoodCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    public void increment() { count.incrementAndGet(); }
}

// Better: Immutable
record Result(int value, String status) {} // Immutable by default
```

### 5. Use Concurrent Collections

```java
// Bad: Synchronized wrapper
Map<String, String> map = Collections.synchronizedMap(new HashMap<>());

// Good: ConcurrentHashMap
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

// Atomic operations
map.computeIfAbsent("key", k -> expensiveComputation());
map.merge("counter", 1, Integer::sum);
```

### 6. Handle Interruption Properly

```java
public void processTask() {
    try {
        while (!Thread.currentThread().isInterrupted()) {
            // Do work
            Thread.sleep(100);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restore flag
        // Cleanup
    }
}
```

### 7. Avoid Deadlocks

```java
// Bad: Lock ordering issue
synchronized(lock1) {
    synchronized(lock2) { }
}

// Good: Consistent lock ordering
Lock lock1 = new ReentrantLock();
Lock lock2 = new ReentrantLock();

public void transfer() {
    Lock first = lock1.hashCode() < lock2.hashCode() ? lock1 : lock2;
    Lock second = first == lock1 ? lock2 : lock1;
    
    first.lock();
    try {
        second.lock();
        try {
            // Transfer logic
        } finally {
            second.unlock();
        }
    } finally {
        first.unlock();
    }
}

// Better: Use tryLock with timeout
if (lock1.tryLock(1, TimeUnit.SECONDS)) {
    try {
        if (lock2.tryLock(1, TimeUnit.SECONDS)) {
            try {
                // Transfer logic
            } finally {
                lock2.unlock();
            }
        }
    } finally {
        lock1.unlock();
    }
}
```

### 8. Monitor Thread Pools

```java
class MonitoredExecutor extends ThreadPoolExecutor {
    private final AtomicLong taskCount = new AtomicLong();
    private final AtomicLong completedCount = new AtomicLong();
    
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        taskCount.incrementAndGet();
    }
    
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        completedCount.incrementAndGet();
        if (t != null) {
            // Log error
        }
    }
    
    public long getTaskCount() { return taskCount.get(); }
    public long getCompletedCount() { return completedCount.get(); }
}
```

### 9. Graceful Shutdown

```java
class Application {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public void shutdown() {
        executor.shutdown(); // Stop accepting new tasks
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Force shutdown
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

### 10. Circuit Breaker Pattern

```java
class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    private State state = State.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final int threshold = 5;
    private long lastFailureTime = 0;
    private final long timeout = 60_000; // 1 minute
    
    public <T> T execute(Supplier<T> operation) throws Exception {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > timeout) {
                state = State.HALF_OPEN;
            } else {
                throw new Exception("Circuit breaker is OPEN");
            }
        }
        
        try {
            T result = operation.get();
            if (state == State.HALF_OPEN) {
                state = State.CLOSED;
                failureCount.set(0);
            }
            return result;
        } catch (Exception e) {
            lastFailureTime = System.currentTimeMillis();
            if (failureCount.incrementAndGet() >= threshold) {
                state = State.OPEN;
            }
            throw e;
        }
    }
}
```

### 11. Bulkhead Pattern

```java
class BulkheadExecutor {
    private final Map<String, ExecutorService> executors = new ConcurrentHashMap<>();
    
    public BulkheadExecutor() {
        // Separate thread pools for different services
        executors.put("database", Executors.newFixedThreadPool(10));
        executors.put("cache", Executors.newFixedThreadPool(5));
        executors.put("external-api", Executors.newFixedThreadPool(20));
    }
    
    public <T> CompletableFuture<T> execute(String service, Supplier<T> task) {
        ExecutorService executor = executors.get(service);
        return CompletableFuture.supplyAsync(task, executor);
    }
}
```

### 12. Performance Tips

```java
// Use LongAdder for high-contention counters
LongAdder counter = new LongAdder();
counter.increment(); // Better than AtomicLong under contention

// Batch operations
List<CompletableFuture<Void>> futures = new ArrayList<>();
for (Task task : tasks) {
    futures.add(CompletableFuture.runAsync(task::execute));
}
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

// Use StampedLock for read-heavy workloads
StampedLock lock = new StampedLock();
long stamp = lock.tryOptimisticRead();
// Read data
if (!lock.validate(stamp)) {
    // Fallback to read lock
}

// Avoid context switching
// Keep thread pool size reasonable
int optimalSize = Runtime.getRuntime().availableProcessors() * 2;
```

---

## Real-World High-Load Example

```java
@Service
class OrderProcessingService {
    private final ExecutorService virtualExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
    private final ForkJoinPool cpuPool = ForkJoinPool.commonPool();
    private final ConcurrentHashMap<String, Order> cache = new ConcurrentHashMap<>();
    private final Semaphore rateLimiter = new Semaphore(1000);
    
    public CompletableFuture<OrderResult> processOrder(OrderRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            // Rate limiting
            if (!rateLimiter.tryAcquire()) {
                throw new RateLimitException();
            }
            
            try {
                // Parallel I/O operations (virtual threads)
                CompletableFuture<User> userFuture = 
                    CompletableFuture.supplyAsync(() -> fetchUser(request.userId()), virtualExecutor);
                CompletableFuture<Inventory> inventoryFuture = 
                    CompletableFuture.supplyAsync(() -> checkInventory(request.items()), virtualExecutor);
                CompletableFuture<Payment> paymentFuture = 
                    CompletableFuture.supplyAsync(() -> processPayment(request.payment()), virtualExecutor);
                
                // Wait for all I/O
                CompletableFuture.allOf(userFuture, inventoryFuture, paymentFuture).join();
                
                // CPU-intensive calculation (ForkJoinPool)
                double discount = cpuPool.invoke(
                    new CalculateDiscountTask(userFuture.join(), request.items())
                );
                
                // Create order
                Order order = createOrder(
                    userFuture.join(),
                    inventoryFuture.join(),
                    paymentFuture.join(),
                    discount
                );
                
                // Cache result
                cache.put(order.id(), order);
                
                return new OrderResult(order);
            } finally {
                rateLimiter.release();
            }
        }, virtualExecutor)
        .exceptionally(ex -> new OrderResult(null, ex.getMessage()));
    }
}
```

---

## Summary Cheat Sheet

| Use Case | Solution |
|----------|----------|
| Massive I/O operations | Virtual Threads |
| CPU-intensive parallel tasks | ForkJoinPool |
| Scheduled tasks | ScheduledExecutorService |
| Async composition | CompletableFuture |
| Reactive streams | Project Reactor |
| Thread-safe counter | AtomicInteger/LongAdder |
| Thread-safe collection | ConcurrentHashMap |
| Producer-Consumer | BlockingQueue |
| Resource limiting | Semaphore |
| Coordination | CountDownLatch/CyclicBarrier |
| Read-heavy cache | StampedLock + ConcurrentHashMap |
| Rate limiting | Semaphore + ScheduledExecutor |

---

**Java Version: 21+** (for Virtual Threads)  
**Last Updated: 2024**

For more details, refer to:
- [Java Concurrency in Practice](https://jcip.net/)
- [Project Loom Documentation](https://openjdk.org/projects/loom/)
- [Project Reactor Reference](https://projectreactor.io/docs)
