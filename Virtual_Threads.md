#### Why do we need to move away from the current concurrency model ?
 - [x] Platform threads are [expensive](virtual_threads/src/main/java/com/github/learntocode2013/PlatformThreadCost.java) to create
 - [x] ***Allows greater scalability via greater [throughput](virtual_threads/src/main/java/com/github/learntocode2013/ThroughputExample.java); Aim is not faster execution.***
       
    ```text
      Little's law for queueing systems:
      λ = N/d
   
      Throughput (λ): The average number of items (e.g., tasks, requests) completed per unit of time.
      Concurrency (N): The average number of items being processed simultaneously.
      Response Time (d): The average time it takes for a single item to be processed from start to finish.
   
    N cannot be increased beyond a limit for Platform threads due to OS-limits; then the only way to
    improve λ is by reducing d.
   
    Virtual threads can achieve superior throughput by allowing for a significantly higher number of concurrent tasks, particularly in I/O-bound scenarios 
    where reducing latency is not a viable option.   
    ```
 - [x] Simplified [async](virtual_threads/src/main/java/com/github/learntocode2013/SimplifiedAsyncOperations.java) operations
 - [x] We need to leverage the improvements being done in the runtime
 - [x] When should we use it ? 

       For I/O bound workloads mainly.

 - [x] How should we use it ?

   ```text
      In practice, we will use structured concurrency which provides a declarative style where we 
      can clearly express our intent - all or none/timeouts/exception handling, etc instead of 
      dealing with low level details like thread creation/interrupt/cancellation, etc
   ```
 
#### Can you show some concrete examples/use cases using virtual threads ?
- [x] Why and how can we limit the number of virtual threads ?
  
  ```text
     Application-Specific Constraints

    1) External Resource Limits: If your application interacts with external systems (e.g., databases, message queues) 
       that have their own concurrency limits, you may need to limit the number of virtual threads to avoid overwhelming those systems.
    2) Rate Limiting: In cases where you need to control the rate at which requests are processed 
       (e.g., API rate limiting, preventing DDoS attacks), limiting the number of active virtual threads can help 
       enforce these limits.
  ```

- [x] How can we increase the number of carrier threads beyond the default if we want to ?

  ```text
     You can set the jdk.virtualThreadScheduler.parallelism system property when launching your Java application
  ```
  ```text
    java -Djdk.virtualThreadScheduler.parallelism= -jar your-application.jar
  ```

- [x] How can we [interrupt](virtual_threads/src/main/java/com/github/learntocode2013/VTInterrupt.java) a virtual thread ?

#### Does it mean we can stop thinking about threads, locks, shared resources, etc with these newer constructs ?

#### What are the caveats ?
- [ ] Can improper usage result in performance issues ?
- [ ] Can improper usage result in resource exhaustion ?
- [x] Thread stack [trace](virtual_threads/src/main/java/com/github/learntocode2013/VTInterrupt.java) does not contain virtual thread trace

#### Can you tell me how virtual threads differ from platform threads under the hood ?

###### Stack Frames and Memory Management
Virtual threads is an alternative implementation of java.lang.Thread that stores
its stack frames in Java’s garbage-collected heap. In contrast, traditional threads
store stack frames in monolithic memory blocks allocated by the operating system. 
This novel approach eliminates the need to estimate a thread’s required stack size. 
A virtual thread’s memory footprint starts from just a few hundred bytes, and it 
automatically adjusts as the call stack grows and shrinks. This dynamic memory 
management significantly improves resource efficiency.


###### Carrier Threads and OS Involvement

The operating system is unaware of virtual threads; it only recognizes platform threads, 
which remain the unit of OS-level scheduling. To execute code in a virtual thread, 
the Java runtime mounts it onto a platform thread, known as a “carrier thread.” 
These carrier threads are part of a specialized `ForkJoinPool`. This process involves
temporarily copying the necessary stack frames from the heap to the carrier thread’s
stack. Essentially, the carrier thread is “borrowed” to run the virtual thread’s code.


###### Handling Blocking Operations

One of the most consequential improvements is how virtual threads deal with blocking operations.
When a virtual thread arrives at an operation that would typically block – perhaps it’s waiting
for I/O – it can be unmounted from its carrier thread. Its modified stack frames are copied back
to the heap, and the carrier thread gets freed to go off and do other work. This functionality
has been retrofitted to almost all blocking points in the JDK. It’s what makes virtual threads
highly efficient in resource utilization.



###### Scenarios When Thread Pinning Occurs

```text
Synchronized Blocks and Methods (Intrinsic Locks):
        When a virtual thread enters a synchronized block or method (which uses intrinsic locks), it becomes pinned to the current carrier thread. This is necessary because the underlying monitor (lock) is tied to the specific carrier thread, and moving the virtual thread to another carrier thread would violate the locking semantics.
        Example:

        java
```

```java
    synchronized (this) {
        // The virtual thread is pinned to the current carrier thread
        // for the duration of this block
    }
```

```text
Native Code Execution:

    When a virtual thread executes native code (typically through JNI – Java Native Interface), it may become pinned to the current carrier thread. This happens because the native code might require thread-local storage, or it might interact with OS-level thread-specific data that requires the virtual thread to stay on the same carrier thread.
    Any blocking I/O operation that isn't handled by the JVM's optimized mechanisms (e.g., non-blocking I/O) might also cause thread pinning if it involves native code.

```

```text
Thread-Local Variables:

    Using ThreadLocal variables can lead to thread pinning. Since ThreadLocal variables are tied to a specific thread (which, in the case of virtual threads, would be the carrier thread), the virtual thread can become pinned when it accesses a ThreadLocal variable.
```

```java
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("Pinned");

        // Accessing the threadLocal can cause the virtual thread to be pinned
        String value = threadLocal.get();
```

```text
    Critical Native Sections:
        When the JVM or native libraries need to perform operations that require the virtual thread to remain on the same OS thread (carrier thread), the virtual thread will be pinned. This is often related to interactions with OS-level features that are not safe to migrate across threads.

    Implications of Thread Pinning

      Reduced Scalability: When a virtual thread is pinned, it cannot be rescheduled onto another carrier thread, which reduces the overall flexibility and scalability of virtual threads. The primary benefit of virtual threads—being able to handle a large number of lightweight threads efficiently—can be diminished if many virtual threads are pinned.

      Performance Overhead: Pinned virtual threads can lead to performance issues because they might block the carrier thread they are pinned to, preventing other virtual threads from running on that carrier thread.

      Resource Contention: If too many virtual threads are pinned, you might experience resource contention or bottlenecks, as fewer carrier threads are available to handle the remaining virtual threads.

    Best Practices to Avoid Excessive Thread Pinning

      Minimize Use of Synchronized Blocks: Avoid unnecessary synchronization or consider using more fine-grained locking mechanisms. Use java.util.concurrent locks like ReentrantLock which are more flexible and might be better optimized by the JVM.

      Avoid ThreadLocal Where Possible: Try to avoid using ThreadLocal variables in virtual threads, or at least minimize their usage, to prevent unnecessary pinning.
```


