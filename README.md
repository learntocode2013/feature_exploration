-[ ] [Structured concurrency basics](https://medium.com/wearewaes/structured-concurrency-with-java-21-in-4-steps-37e72997ed2a)
-[x] [Virtual threads basics](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html#GUID-2BCFC2DD-7D84-4B0C-9222-97F9C7C6C521)
-[x] [Nuances of virtual threads](https://medium.com/@liakh-aliaksandr/concurrent-programming-in-java-with-virtual-threads-8f66bccc6460)
  -[x] [Sealed classes](https://www.youtube.com/watch?v=LCJjgoONy7g)
  -[x] [Java Coding Problems - book VT lessons](https://learning.oreilly.com/library/view/java-coding-problems/9781837633944/Text/Chapter_10.xhtml#_idParaDest-447)
  -[x] [Java Coding Problems - book VT lessons] (https://learning.oreilly.com/library/view/java-coding-problems/9781837633944/Text/Chapter_11.xhtml#_idParaDest-487)
-[ ] [Continuations]()
-[ ] [Scoped value](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ScopedValue.html)
-[ ] [Thread flock class]()




#### Things to try out

-[ ] New structured concurrency subclass; RetryOnFailure

#### Observe virtual threads
java --enable-preview --add-modules jdk.incubator.concurrent -XX:StartFlightRecording=filename=recording.jfr,settings=../vtEvent.jfc -classpath target/classes com.github.learntocode2013.DemoVTWithXS

jfr print recording.jfr

#### Reference(s)
[Structured concurrency oracle doc](https://docs.oracle.com/en/java/javase/21/core/structured-concurrency.html#GUID-B1AAAFB8-56E1-4289-8EAF-4BF581BF28FF)
[Interesting examples](https://github.com/PacktPublishing/Java-Coding-Problems)

