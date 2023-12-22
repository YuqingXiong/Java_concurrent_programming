# 1 创建线程

## 方法一：Thread 类

创建Thread类的对象，重写其中的 run 方法：

```java
@Slf4j(topic = "c.Test1")
public class d1_Thread {
    public static void main(String[] args) {
        // 创建 Thread 类的对象
        Thread t = new Thread(){
            @Override
            public void run(){
                log.debug("running"); // t1 线程
            }
        };
        t.setName("t1");
        t.start();
        log.debug("running"); // main线程
    }
}
```

以继承的方式重写 run ：

```java
public class MyThread extends Thread{
    // 2. 重写Thread类的run方法
    @Override
    public void run() {
        // 1. 描述线程的执行任务
        for (int i = 0; i < 5; i++) {
            System.out.println("son Thread output : " + i);
        }
    }
}

public class ThreadTest1 {
    // main方法由一条默认的主线程负责执行
    public static void main(String[] args) {
        // 3. 创建MyThread线程类的对象代表一个线程
        Thread t = new MyThread();
        // 4. 启动线程（自动执行 run 方法）
        t.start();
        // 已经有两个线程了：main 线程, t线程
        // 它们这两个线程的输出没有前后
        for (int i = 0; i < 5; i++) {
            System.out.println("main Thread output : " + i);
        }
    }
}
```

注意实现：
1. 启动子线程要调用start方法，而不是run方法，否则还是只有main线程
2. 子线程任务要放在主线程之前，如果主线程在子线程之前，主线程任务就一定在子线程任务前

缺点：

- 线程类已经继承了Thread类，无法继承其他类，不利于扩展

## 方法二：Runnable接口与Thread类

创建Runnable接口的匿名内部类对象，重写其中的 run 方法

```java
/**
 * Represents an operation that does not return a result.
 *
 * <p> This is a {@linkplain java.util.function functional interface}
 * whose functional method is {@link #run()}.
 *
 * @author  Arthur van Hoff
 * @see     java.util.concurrent.Callable
 * @since   1.0
 */
@FunctionalInterface
public interface Runnable {
    /**
     * Runs this operation.
     */
    void run();
}
```

其中 Runnable接口只有一个方法，被注解为 `FunctionalInterface`，所以可以用 lambda表达式简化

```java
@Slf4j(topic = "c.Test2")
public class d2_Runnable {
    public static void main(String[] args) {
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                log.debug("running");
//            }
//        };
        // lambda精简代码
        Runnable r = ()->log.debug("running");
        Thread t = new Thread(r, "t2");
        t.start();
    }
}
```

以继承接口的方式实现：

```java
// 1. 定义一个任务类，实现Runnable接口
public class MyRunnable implements Runnable{

    // 2. 重写 Runnable接口的run方法
    @Override
    public void run() {
        // 线程执行的任务
        for (int i = 0; i < 5; i++) {
            System.out.println("子线程输出："+i);
        }
    }
}

public class ThreadTest2 {
    public static void main(String[] args) {
        // 3. 创建任务类的对象
        Runnable target = new MyRunnable();
        // 4. 任务对象交给线程对象处理
        Thread thread = new Thread(target);
        thread.start();

        for (int i = 0; i < 5; i++) {
            System.out.println("主线程执行：" + i);
        }
    }
}
```

优点：任务类只是实现接口，可以继续继承其他类，实现其他接口，扩展性强

**实现原理：**

`Runnable` 对象会被赋值给 `holder.task` 变量，在 `Thread` 类的 run 方法中会判断是否存在 `task` 变量，如果存在则优先执行。

直接创建 `Thread` 对象，对其中的 `run`方法重写，就等于覆盖了下面的方法。

```java
    @Override
    public void run() {
        Runnable task = holder.task;
        if (task != null) {
            Object bindings = scopedValueBindings();
            runWith(bindings, task);
        }
    }
```

## 方法三：FutureTask接口获取 run 的返回值

实现：

1. 创建 `FutureTask` 的对象，传入 Callable参数，泛型选择返回值类型，**重写其中的call方法**
2. 将 `FutureTask` 对象传入 Thread 对象
3. 调用 `FutureTask` 对象的 get 方法获取返回值

```java
@Slf4j(topic = "c.Test3")
public class d3_FutureTask {
    public static void main(String[] args) throws Exception {
//        FutureTask<Integer> task =  new FutureTask<>(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                log.debug("running");
//                Thread.sleep(1000);
//                return 100;
//            }
//        });

        // 简化：
        FutureTask<Integer> task = new FutureTask<>(()->{
            log.debug("running");
            Thread.sleep(1000);
            return 100;
        });

        new Thread(task, "t3").start();
        Integer result = task.get();
        log.debug("result = {}", result);
    }
}
```

以继承Callable接口的方式实现：

```java
public class MyCallable implements Callable<String> {

    private int n;

    public MyCallable(int n) {
        this.n = n;
    }

    @Override
    public String call() throws Exception {
        // 描述线程任务，返回线程执行结果
        int sum = 0;
        for(int i = 1; i <= n; ++ i){
            sum += i;
        }
        return "线程求出了1-"+ n + "的和是："+sum;
    }
}

public class ThreadTest3 {
    public static void main(String[] args) throws Exception {
        // 前两次重写run方法在不同线程中执行代码无法返回数据
        // Callable接口，FutureTask类的实现，可以直接返回数据

        // 创建一个重写了Callable接口的对象
        Callable<String> call = new MyCallable(100);

        // Callable的对象封装为FutureTask的对象
        FutureTask<String> f1 = new FutureTask<>(call);
        // 作用：
        // 1.任务对象，实现了Runnable接口
        // 2. 线程执行饭毕后可以调用get方法获取线程执行完毕后的结果
        new Thread(f1).start();

        Callable<String> call2 = new MyCallable(200);
        FutureTask<String> f2 = new FutureTask<>(call2);
        new Thread(f2).start();

        // 获取线程执行完毕后的结果.
        // get获取的返回结果就是call方法返回的结果
        // call返回的结果可能是符合返回类型的或者不符合，所有这里有异常，需要抛出
        System.out.println(f1.get());
        System.out.println(f2.get());
        // 如果上面的线程代码没有执行完毕，这里的f1.get方法会暂停，等待上面代码执行完毕才会获取结果
    }
}
```



实现原理（继承关系）：

1. 首先 `FutureTask` 实现了 `RunnableFuture` 接口
   - `RunnableFuture` 接口是由 ` Runnable` 和 `Future<V>` 接口组成的
   - 实现了 `Runnable` 接口的 run 方法
   - 实现了 `Future<V> `接口的 get 方法
2. 在 `FutureTask` 对 `run` 中的实现：
   1. 获取 callable对象
   2. 调用 callable对象中的 call 方法
   3. 等待 call 方法执行完成，获取返回值并赋值给 result
3. callable 对象的获取是通过 `FutureTask`的构造函数传入的
4. 调用实现的 get 方法获取 call方法的返回值 result
   1. set方法将 result -> outcome
   2. get方法调用 report方法获取outcome

```java
// 1. FutureTask实现了RunnableFuture接口
public class FutureTask<V> implements RunnableFuture<V>

// RunnableFuture接口 继承了 Runnable
public interface RunnableFuture<V> extends Runnable, Future<V> {
    /**
     * Sets this Future to the result of its computation
     * unless it has been cancelled.
     */
    void run();
}

// RunnableFuture接口 继承了 Future<V>接口
// get 方法可以获取返回值
public interface Future<V> {
    // ...
    V get() throws InterruptedException, ExecutionException;
	// ...
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
    // ...
}

// 2. FutrureTask类中对 run 方法的具体实现：
public void run() {
        if (state != NEW ||
            !RUNNER.compareAndSet(this, null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;   // 2.1. 获得一个 callable对象
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    result = c.call();	// 2.2. 调用 callable对象中的 call 方法（call方法一般由用户重写）
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    set(result); // 2.3. 如果 call 方法执行完毕，则将返回值赋值给result
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }
```



```java
// 3. FutureTask的构造方法，传入了 Callable 接口对象
public FutureTask(Callable<V> callable) {
    if (callable == null)
        throw new NullPointerException();
    this.callable = callable;
    this.state = NEW;       // ensure visibility of callable
}

// set 方法将 result 结果赋值给 outcome
protected void set(V v) {
    if (STATE.compareAndSet(this, NEW, COMPLETING)) {
        outcome = v;
        STATE.setRelease(this, NORMAL); // final state
        finishCompletion();
    }
}
// report 方法返回 outcome 结果
private V report(int s) throws ExecutionException {
    Object x = outcome;
    if (s == NORMAL)
        return (V)x;
    if (s >= CANCELLED)
        throw new CancellationException();
    throw new ExecutionException((Throwable)x);
}
// get方法 返回 report中的outcom结果
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING)
        s = awaitDone(false, 0L);
    return report(s);
}
```

# 2 线程运行

## 原理

栈与栈帧：

Java 虚拟机栈会为每个启动的线程分配一块栈内存，其中存储着栈帧（Frame）

- 每个栈由多个栈帧组成，栈帧对应调用方法（函数）所占用的内存
- 每个栈只有一个活动栈，对应当前正在执行的方法

### 单线程示例：

`main`, `method1`, `method2` 各自对应这一个栈帧，存储在一个栈中：

![image-20231214135238840](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214135238840.png)

```java
public class d1_Frame {
    public static void main(String[] args) {
        method1(10); // 调用method1
    }

    public static void method1(int x){
        int y = x + 1;
        Object obj = method2(); // 调用 method2
    }
    public static Object method2(){
        Object obj = new Object();
        return obj;
    }
}
```

![image-20231214140148426](https://xyqimg.com/img/202312212157386.png)

### 多线程示例：

包含两个线程 `main` 和 `t1` ，分别创建两个独立的栈，每个栈里包含各自的栈帧

`main`线程的栈：

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214140835906.png" alt="image-20231214140835906" style="zoom:50%;" />

`t1` 线程的栈：

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214140918679.png" alt="image-20231214140918679" style="zoom:50%;" />

```java
public class d1_Frame {
    public static void main(String[] args) {

        Thread t1 = new Thread() {
            @Override
            public void run() {
                method1(20); // t1 线程调用 method1
            }
        };
        t1.setName("t1");
        t1.start();
        method1(10); // main 调用method1
    }

    public static void method1(int x){
        int y = x + 1;
        Object obj = method2(); // 调用 method2
    }
    public static Object method2(){
        Object obj = new Object();
        return obj;
    }
}
```

## 线程上下文切换（Thread Context Switch）

线程上下文切换是指：CPU不再执行当前线程，转而执行其他线程的代码的过程

发生 Context Switch 的原因：

- 线程的 cpu 时间片用完
- 垃圾回收
- 有更高优先级的线程需要运行
- 线程自己调用了 sleep、yield、wait、join、park、synchronized、lock 等方法

当 Context Switch 发生时，需要由操作系统保存当前线程的状态，并恢复另一个线程的状态，Java 中对应的概念
就是程序计数器（Program Counter Register），它的作用是记住下一条 `jvm` 指令的执行地址，是线程私有的

- 状态包括程序计数器、虚拟机栈中每个栈帧的信息，如局部变量、操作数栈、返回地址等
- Context Switch 频繁发生会影响性能

## 线程运行的常见方法

![image-20231214141937826](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214141937826.png)

### run与start

run调用：直接调用 run 方法，则是 main 主线程执行的，并没有创建一个新线程执行

start调用：创建一个新的线程 `t1` ，`t1` 线程与 `main` 线程是同时进行的。调用后，线程的状态也会发生改变

```java
@Slf4j(topic = "c.d2_run_start")
public class d2_run_start {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.debug("running");
            }
        };

        t1.run();
        // 14:25:50 [main] c.d2_run_start - running
        System.out.println(t1.getState()); //NEW

        t1.start();
        // 14:28:11 [t1] c.d2_run_start - running
        System.out.println(t1.getState()); // RUNNABLE
    }
}
```



### sleep 

1. sleep 的对线程状态的影响：调用 sleep 会让当前线程从 Running 进入 Timed Waiting 状态（阻塞）

```java
@Slf4j(topic = "c.d3_sleep_yield")
public class d3_sleep_yield {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1"){
            @Override
            public void run() {
                log.debug("running");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                   e.printStackTrace();
                }
            }
        };
        log.debug("t1 state: {}", t1.getState()); // NEW
        t1.start();
        log.debug("t1 state: {}", t1.getState()); // RUNNABLE

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("t1 state: {}", t1.getState()); // TIMED_WAITING
    }
}
```

2. 其它线程可以使用 interrupt 方法中断正在睡眠的线程，这时 sleep 方法会抛出 `InterruptedException` 异常

```java
@Slf4j(topic = "c.d3_sleep_yield")
public class d3_sleep_yield {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1"){
            @Override
            public void run() {
                log.debug("enter sleep");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.debug("wake up ...");
                   e.printStackTrace();
                }
            }
        };
        t1.start();

        try {
            Thread.sleep(1000); // 当前 main 线程睡眠1s
            log.debug("interrupt...");
            t1.interrupt(); // 打断 t1 的 sleep

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * 输出：
         * 14:45:29 [t1] c.d3_sleep_yield - enter sleep
         * 14:45:30 [main] c.d3_sleep_yield - interrupt...
         * 14:45:30 [t1] c.d3_sleep_yield - wake up ...
         * java.lang.InterruptedException: sleep interrupted
         */
    }
}
```

3. 睡眠结束后的线程未必会立刻得到执行。睡眠结束后的线程仅仅是被唤醒了，还需要等待CPU分配时间片才能执行

4. `TimeUnit` 的 sleep 代替 Thread 的 sleep 来获得更好的可读性

```java
iimport java.util.concurrent.TimeUnit;
// 该方法实际上是封装的 Thread.sleep 方法
TimeUnit.SECONDS.sleep(1); // 睡眠 1 s
```

### yield

yield（让出）：让出当前线程 CPU 的使用权给其他线程

1. 调用 yield 会让当前线程从 Running 进入 Runnable 就绪状态，然后调度执行其它线程

2. 具体的实现依赖于操作系统的任务调度器

### 线程优先级 set/getPriority

- 线程优先级会提示（hint）调度器优先调度该线程，但它仅仅是一个提示，任务调度器可以忽略它
- 如果 CPU 比较忙，那么优先级高的线程会获得更多的时间片，但 CPU闲时，优先级几乎没作用

```java
public class d4_thread_priority {
    public static void main(String[] args) {
        Thread t1 = new Thread(()->{
            int count = 0;
            while(true){
                System.out.println("---> 1 " + count++);
            }
        }, "t1");


        Thread t2 = new Thread(()->{
            int count = 0;
            while(true){
//                Thread.yield();
                System.out.println("         ---> 2 " + count++);
            }
        }, "t2");

        // 设置线程优先级（默认优先级为5）
        t1.setPriority(Thread.MIN_PRIORITY);
        t2.setPriority(Thread.MAX_PRIORITY);
        t1.start();
        t2.start();
    }
}
```

- 设置 yield 会将当前线程占用的CPU时间片让给其他线程，所以 `t1`的 count 会比 `t2` 的大
- 设置优先级，优先级越大的线程可以获得更多的时间片，所以 `t2`的 count 会比 `t1` 的大

### 防止 CPU 占用100%

![image-20231214150803371](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214150803371.png)

### join

等待当前线程运行完毕

单线程等待：主线程 main 在同步等待 `t1` 线程

```java
@Slf4j(topic = "c.d5_join")
public class d5_join {
    static int r = 0;
    public static void main(String[] args) throws InterruptedException {
        test1();
    }
    private static void test1() throws InterruptedException {
        log.debug("开始");
        Thread t1 = new Thread(() -> {
            log.debug("开始");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("结束");
            r = 10;
        });
        t1.start();
        t1.join(); // 等待 t1 完成，再获取 r的结果
        log.debug("结果为:{}", r);
        log.debug("结束");
    }
}
```

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214151748683.png" alt="image-20231214151748683" style="zoom:50%;" />

多线程等待：



```java
@Slf4j(topic = "c.d5_join")
public class d5_join {
    static int r1 = 0;
    static int r2 = 0;
    public static void main(String[] args) throws InterruptedException {
        test2();
    }
    private static void test2() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            r1 = 10;
        });
        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            r2 = 20;
        });
        long start = System.currentTimeMillis();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        long end = System.currentTimeMillis();
        log.debug("r1: {} r2: {} cost: {}", r1, r2, end - start);
        // 15:23:57 [main] c.d5_join - r1: 10 r2: 20 cost: 2014
    }
```

先调用 t1 的 join 再调用 t2 的join，等待时间为 2s

- 第一个 join：等待 t1 时, t2 并没有停止, 而在运行
- 第二个 join：1s 后, 执行到此, t2 也运行了 1s, 因此也只需再等待 1s

颠倒两个 join 最终都是输出 2s

- 因为 t2等待运行完成的时候，t1 也在运行
- t2 完成后已经等待了 2s，此时t1已经运行完毕，不需要再等待了

![image-20231214152811982](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214152811982.png)

**有时效的 join**：

```java
t1.join(1500); // 等待1.5后继续当前线程
// 如果 t1 仅需要 1s 完成，则join也会提前结束
```

### interrupt

1. 用于打断 sleep, wait, join的线程

打断后的打断标记为 `false` （认为对 sleep,wait,join的线程进行打断不算打断）

```java
@Slf4j(topic = "c.d6_interrupt")
public class d6_interrupt {

    public static void main(String[] args) throws InterruptedException {
        test1();
    }
    private static void test1() throws InterruptedException {
        Thread t1 = new Thread(()->{
            log.debug("sleep...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "t1");
        t1.start();
        Thread.sleep(500);
        log.debug("interrupt...");
        t1.interrupt();
        log.debug(" 打断状态: {}", t1.isInterrupted());
        /** 输出：
         * 15:36:35 [t1] c.d6_interrupt - sleep...
         * 15:36:36 [main] c.d6_interrupt - interrupt...
         * 15:36:36 [main] c.d6_interrupt -  打断状态: false
         */
    }
}
```

2.  打断正常运行的线程

打断后的打断标记为 `true`。但是被打断的线程并不会停止运行

这样可以通过一个标记让该线程获取自己被其他线程打断了，以便进行后续处理并决定是否停止当前线程

```java 
@Slf4j(topic = "c.d6_interrupt")
public class d6_interrupt {

    public static void main(String[] args) throws InterruptedException {
        test2();
    }

    private static void test2() throws InterruptedException {
        Thread t2 = new Thread(()->{
            while(true) {
                // 获取当前线程的打断标记
                Thread current = Thread.currentThread();
                boolean interrupted = current.isInterrupted();
                // 自己决定是否退出当前线程
                if(interrupted) {
                    log.debug(" 打断状态: {}", interrupted);
                    // 15:39:55 [t2] c.d6_interrupt -  打断状态: true
                    break;
                }
            }
        }, "t2");
        t2.start();
        Thread.sleep(500);
        t2.interrupt();
    }
}
```

## 模式之两阶段终止

Two Phase Termination
在一个线程 T1 中如何“优雅”终止线程 T2？这里的【优雅】指的是给 T2 一个料理后事的机会。

1. **错误方法**
- 使用线程对象的 stop() 方法停止线程
  - stop 方法会真正杀死线程，如果这时线程锁住了共享资源，那么当它被杀死后就再也没有机会释放锁，其它线程将永远无法获取锁
- 使用 System.exit(int) 方法停止线程
  - 目的仅是停止一个线程，但这种做法会让整个程序都停止

2. **两阶段终止模式**

![image-20231214154710844](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214154710844.png)

### 利用 isInterrupted 

注意事项：

sleep过程中被打断会重置打断标记为false，所以需要重新设置打断标记为 true，以便在上面的if里判断标记，进行后续处理

如果不重新设置打断标记为 true，则不会进行料理后事的处理

```java
class Test{
    public static void main(String[] args) throws InterruptedException {
        d7_two_phase_termination tpt = new d7_two_phase_termination();
        tpt.start();

        Thread.sleep(3500);
        tpt.stop();
        /** 输出：
         * 16:07:57 [Thread-0] c.d7_two_phase_termination - 执行监控记录
         * 16:07:58 [Thread-0] c.d7_two_phase_termination - 执行监控记录
         * 16:07:59 [Thread-0] c.d7_two_phase_termination - 执行监控记录
         * java.lang.InterruptedException: sleep interrupted
         * 	at java.base/java.lang.Thread.sleep0(Native Method)
         * 	at java.base/java.lang.Thread.sleep(Thread.java:509)
         * 	at com.rainsun.d2_run_thread.d7_two_phase_termination.lambda$start$0(d7_two_phase_termination.java:30)
         * 	at java.base/java.lang.Thread.run(Thread.java:1583)
         * 16:08:00 [Thread-0] c.d7_two_phase_termination - 释放资源，锁，料理后事...
         */
    }
}

@Slf4j(topic = "c.d7_two_phase_termination")
public class d7_two_phase_termination {
    private Thread monitor;

    // 启动监控程序
    public void start(){
        monitor = new Thread(()->{
            while (true){
                Thread currentThread = Thread.currentThread();
                if(currentThread.isInterrupted()){
                    log.debug("释放资源，锁，料理后事...");
                    break;
                }

                try {
                    Thread.sleep(1000); // 情况一：sleep过程被打断
                    log.debug("执行监控记录"); // 情况二：执行其他过程被打断
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // sleep过程中被打断会重置打断标记为false
                    // 这里需要重新设置打断标记为 true，以便在上面的if里判断标记，进行后续处理
                    currentThread.interrupt();
                }
            }
        });
        monitor.start();
    }

    public void stop(){
        monitor.interrupt();
    }
}
```

### 主线程与守护线程

- 默认情况下，Java 进程需要等待所有线程都运行结束，才会结束。
  - 例如：两个线程`t1`，`main` ，main线程结束了， `t1` 线程没结束则Java进程会继续运行

- 有一种特殊的线程叫做守护线程，只要其它非守护线程运行结束了，即使守护线程的代码没有执行完，也会强制结束。
  - 设置一个线程为守护线程：`t1.setDaemon(true)`

`t1` 2s才运行完，但是 `t1` 被设置为守护线程，当其他线程 1s后运行完后，`t1` 线程也被迫停止了

```java
@Slf4j(topic = "c.d8_daemon")
public class d8_daemon {
    public static void main(String[] args) throws InterruptedException {
        log.debug("开始运行...");
        Thread t1 = new Thread(() -> {
            log.debug("开始运行...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("运行结束...");
        }, "daemon");
        // 设置该线程为守护线程
        t1.setDaemon(true);
        t1.start();
        Thread.sleep(1000);
        log.debug("运行结束...");
        /**
         * 16:22:39 [main] c.d8_daemon - 开始运行...
         * 16:22:39 [daemon] c.d8_daemon - 开始运行...
         * 16:22:40 [main] c.d8_daemon - 运行结束...
         */
    }
}
```

注意

- 垃圾回收器线程就是一种守护线程
- Tomcat 中的 Acceptor 和 Poller 线程都是守护线程，所以 Tomcat 接收到 shutdown 命令后，不会等待它们处理完当前请求

## 操作系统层面的五种状态

![image-20231214162622212](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214162622212.png)

- 【初始状态】仅是在语言层面创建了线程对象，还未与操作系统线程关联
- 【可运行状态】（就绪状态）指该线程已经被创建（与操作系统线程关联），可以由 CPU 调度执行
- 【运行状态】指获取了 CPU 时间片运行中的状态当 CPU 时间片用完，会从【运行状态】转换至【可运行状态】，会导致线程的上下文切换
- 【阻塞状态】
  - 如果调用了阻塞 API，如 BIO 读写文件，这时该线程实际不会用到 CPU，会导致线程上下文切换，进入【阻塞状态】
  - 等 BIO 操作完毕，会由操作系统唤醒阻塞的线程，转换至【可运行状态】
  - 与【可运行状态】的区别是，对【阻塞状态】的线程来说只要它们一直不唤醒，调度器就一直不会考虑调度它们
- 【终止状态】表示线程已经执行完毕，生命周期已经结束，不会再转换为其它状态

## Java API 层面的六种状态

![image-20231214162901522](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214162901522.png)

- NEW 线程刚被创建，但是还没有调用 start() 方法
- RUNNABLE 当调用了 start() 方法之后，注意，Java API 层面的 RUNNABLE 状态涵盖了 操作系统 层面的【可运行状态】、【运行状态】和【阻塞状态】（由于 BIO 导致的线程阻塞，在 Java 里无法区分，仍然认为是可运行）
- BLOCKED ， WAITING ， TIMED_WAITING 都是 Java API 层面对【阻塞状态】的细分，后面会在状态转换一节详述
- TERMINATED 当线程代码运行结束

六种状态的演示：

```java
@Slf4j(topic = "c.d9_thread_state")
public class d9_thread_state {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(()->{
            log.debug("running..."); // NEW
        }, "t1");


        Thread t2 = new Thread(()->{
            while (true){ // RUNNABLE

            }
        }, "t2");
        t2.start();

        Thread t3 = new Thread(()->{
            log.debug("running..."); // TERMINATED
        }, "t3");
        t3.start();

        Thread t4 = new Thread(()->{
            synchronized (d9_thread_state.class){
                try {
                    Thread.sleep(1000000); // timed_waiting
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, "t4");
        t4.start();

        Thread t5 = new Thread(()->{
            try {
                t2.join(); // t5等待t2完成，t2为死循环，所以是 waiting状态
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t5");
        t5.start();

        Thread t6 = new Thread(()->{
            // 这个锁已经被t4占据了，所以t6拿不到这个锁。陷入 blocked状态
            synchronized (d9_thread_state.class){
                try {
                    Thread.sleep(1000000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, "t6");
        t6.start();

        Thread.sleep(500);

        log.debug("t1 state {}", t1.getState());
        log.debug("t2 state {}", t2.getState());
        log.debug("t3 state {}", t3.getState());
        log.debug("t4 state {}", t4.getState());
        log.debug("t5 state {}", t5.getState());
        log.debug("t6 state {}", t6.getState());
        /** 输出：
         * 16:47:45 [t3] c.d9_thread_state - running...
         * 16:47:46 [main] c.d9_thread_state - t1 state NEW
         * 16:47:46 [main] c.d9_thread_state - t2 state RUNNABLE
         * 16:47:46 [main] c.d9_thread_state - t3 state TERMINATED
         * 16:47:46 [main] c.d9_thread_state - t4 state TIMED_WAITING
         * 16:47:46 [main] c.d9_thread_state - t5 state WAITING
         * 16:47:46 [main] c.d9_thread_state - t6 state BLOCKED
         */

    }
}
```

# 3 线程管理中的锁

## 线程安全问题

两个线程对初始值为 0 的静态变量一个做自增，一个做自减，各做 5000 次，结果不一定为 0 

```java
@Slf4j(topic = "c.d1_problem")
public class d1_problem {
    static int counter = 0;
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                counter++;
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                counter--;
            }
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("{}",counter);
        // 不一定为 0
        // 输出 102 ：21:04:59 [main] c.d1_problem - 102
    }
}
```

这里的共享变量 counter 的自增，自检并不是原子操作。是由多个步骤组成的，如果一个线程执行到其中一个步骤就停止了，另一个线程并不会接着上一个线程继续，而是重新开始算法（因为线程之间没有设置同步），此时获取到的counter值就不是最新的。

例如对于 i++ 而言（i 为静态变量），实际会产生如下的 JVM 字节码指令：

```java
getstatic i // 获取静态变量i的值
iconst_1 // 准备常量1
iadd // 自增
putstatic i // 将修改后的值存入静态变量i
```

Java 内存模型中，每个线程有自己的本地内存，存放着共享变量的副本。要想要其他线程看见，还需要将自己本地内存的数据更新到主内存中，这一步如果没完成就开始上下文切换就会有问题：

![image-20231214211232281](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214211232281.png)

- 黄色：线程 2 还没有写入共享变量的值到主内存中就发生了上下文切换
- 红色：线程 2 对主内存的写 覆盖了线程 1 的写入值

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214211503632.png" alt="image-20231214211503632" style="zoom:50%;" />

如果多个线程同时对共享变量读写，其中一个线程还没来得及将更改后的共享变量更新到主内存中，就切换到另一个线程从主内存中读该共享变量，此时读取的共享变量的值就是错的。而且切换回原来的线程后，之前被另一个线程更新后的共享变量的值也会被当前切换回来的线程所继续写的值给覆盖

### 临界区 Critical Section：

一段代码块内如果存在对共享资源的多线程读写操作，称这段代码块为临界区

```java
static int counter = 0;
static void increment()
// 临界区
{
counter++;
}
static void decrement()
// 临界区
{
counter--;
}
```

### 竞态条件 Race Condition
多个线程在临界区内执行，由于代码的执行序列不同而导致结果无法预测，称之为发生了竞态条件

## synchronized 解决方案

为了避免临界区的竞态条件发生，有多种手段可以达到目的。

- 阻塞式的解决方案：synchronized，Lock
- 非阻塞式的解决方案：原子变量

synchronized 采用互斥的方式让同一时刻至多只有一个线程能持有【对象锁】，其它线程再想获取这个【对象锁】时就会阻塞住。这样就能保证拥有锁的线程可以安全的执行临界区内的代码，不用担心线程上下文切换

语法：

```java
synchronized(对象) // 线程1， 线程2(blocked)
{
	临界区
}
```

注意：

**拥有锁对象的线程即使当前时间片被用完了也不会释放锁，而是等待下一次获得时间片继续运行临界区代码块，直到运行完成才会释放锁。在没有释放锁的期间，其他线程都是阻塞状态，释放锁后会唤醒其他线程将锁给其他线程执行其他临界区的代码**

画图显示：

**synchronized 实际是用对象锁保证了临界区内代码的原子性，临界区内的代码对外是不可分割的，不会被线程切换所打断。**

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214212905831.png" alt="image-20231214212905831" style="zoom:67%;" />

如果把 synchronized(obj) 放在 for 循环的外面，如何理解？-- 原子性：整个 for 循环将受到保护，不可分割
如果 t1 synchronized(obj1) 而 t2 synchronized(obj2) 会怎样运作？-- 锁对象：没有对 count进行保护
如果 t1 synchronized(obj) 而 t2 没有加会怎么样？如何理解？-- 锁对象：根本没有想去获得锁对象也就不会被阻塞住

Java中每一个对象都可以作为锁，具体表现为以下3种形式：

1. 对于普通同步方法，锁是当前实例对象

   ```java
   class Test{
       public synchronized void test() {
       }
   }
   等价于
   class Test{
       public void test() {
           synchronized(this) {
           }
       }
   }
   ```

   

2. 对于同步方法块，锁是Synchronized括号里面指定的对象（指定的对象也可以用this,即指定当前实例对象）

3. 对于静态同步方法，锁是当前类的Class对象

   ```java
   class Test{
       public synchronized static void test() {
       }
   }
   等价于
   class Test{
       public static void test() {
           synchronized(Test.class) {
           }
       }
   }
   ```

## 变量的线程安全分析

成员变量和静态变量是否线程安全？
- 如果它们没有共享，则线程安全
- 如果它们被共享了，根据它们的状态是否能够改变，又分两种情况
  - 如果只有读操作，则线程安全
  - 如果有读写操作，则这段代码是临界区，需要考虑线程安全


局部变量是否线程安全？
- 局部变量是线程安全的
- 但局部变量引用的对象则未必
  - 如果该对象没有逃离方法的作用访问，它是线程安全的
  - 如果该对象逃离方法的作用范围，需要考虑线程安全

### 局部变量线程安全分析

```java
public static void test1() {
    int i = 10;
    i++;
}
```

每个线程调用 test1() 方法时局部变量 i，会在每个线程的栈帧内存中被创建多份，因此不存在共享

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214220030285.png" alt="image-20231214220030285" style="zoom:50%;" />

局部变量的引用可能会发生共享：

一个增加一个删除，就会发生线程安全问题：可能删除的时候集合为空

```java
class d2_ThreadUnsafe {
    ArrayList<String> list = new ArrayList<>();
    public void method1(int loopNumber) {
        for (int i = 0; i < loopNumber; i++) {
            // { 临界区, 会产生竞态条件
            method2();
            method3();
            // } 临界区
        }
    }
    private void method2() {
        list.add("1");
    }
    private void method3() {
        list.remove(0);
    }

    static final int THREAD_NUMBER = 2;
    static final int LOOP_NUMBER = 200;
    public static void main(String[] args) {
        d2_ThreadUnsafe test = new d2_ThreadUnsafe();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            new Thread(() -> {
                test.method1(LOOP_NUMBER);
            }, "Thread" + i).start();
        }
    }
}
```

分析：

引用类型的成员变量会被存储在堆中，每个线程都共享一份成员变量

而不是像基本的变量类型存储在栈中，每个线程都有一份自己的新的成员变量

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214220608215.png" alt="image-20231214220608215" style="zoom:67%;" />

将 list 修改为局部变量，则每个线程都拥有一份 **新的 list**，那么就没有线程安全问题：

```java
class ThreadSafe {
    public final void method1(int loopNumber) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < loopNumber; i++) {
            method2(list);
            method3(list);
        }
    }
    private void method2(ArrayList<String> list) {
        list.add("1");
    }
    private void method3(ArrayList<String> list) {
        list.remove(0);
    }
}
```

分析：

- list 是局部变量，每个线程调用时会创建其不同实例，没有共享
- 而 method2 的参数是从 method1 中传递过来的，与 method1 中引用同一个对象
- method3 的参数分析与 method2 相同

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231214221123540.png" alt="image-20231214221123540" style="zoom: 50%;" />



如果把 method2 和 method3 的方法修改为 public
- 情况1：有其它线程调用 method2 和 method3：没有哦线程安全问题，传入的list是不同的
- 情况2：在 情况1 的基础上，为 ThreadSafe 类添加子类，子类覆盖 method2 或 method3 方法

  - ```java
    class ThreadSafeSubClass extends ThreadSafe{
        @Override
        public void method3(ArrayList<String> list) {
            new Thread(() -> {
                list.remove(0);
            }).start();
        }
    }
    ```

  - 存在线程安全问题：有两个线程操作同一个 list 了，可以通过添加 final 关键字防止子类重写

## 常见线程安全类

- String
- Integer
- StringBuffer
- Random
- Vector
- Hashtable
- java.util.concurrent 包下的类

这里说它们是线程安全的是指，多个线程调用它们同一个实例的某个方法时，是线程安全的

- 它们的每个方法是原子的
- 但注意它们多个方法的组合不是原子的

### 线程安全类方法的组合

```java
Hashtable table = new Hashtable();
// 线程1，线程2
if( table.get("key") == null) {
	table.put("key", value);
}
```

![image-20231215100012276](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215100012276.png)

### 不可变类线程安全性

String、Integer 等都是不可变类，因为其内部的状态不可以改变，因此它们的方法都是线程安全的

String 有 replace，substring 等方法并没有改变原有的值，而是创建了一个新的字符串赋给原来的变量，看起来改变了值

## Monitor 概念

### Java 对象头

![image-20231215103905400](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215103905400.png)

### Mark Word:

![image-20231215103926373](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215103926373.png)

### Monitor (锁)

Monitor 被翻译为监视器或管程

每个 Java 对象都可以关联一个 Monitor 对象，如果使用 synchronized 给对象上锁（重量级）之后，该对象头的Mark Word 中就被设置指向 Monitor 对象的指针

Monitor 结构：

![image-20231215104209600](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215104209600.png)

- 刚开始 Monitor 中 Owner 为 null
- 当 Thread-2 执行 synchronized(obj) 就会将 Monitor 的所有者 Owner 置为 Thread-2，Monitor中只能有一个 Owner
- 在 Thread-2 上锁的过程中，如果 Thread-3，Thread-4，Thread-5 也来执行 synchronized(obj)，就会进入 **EntryList BLOCKED**
- Thread-2 执行完同步代码块的内容，然后唤醒 EntryList 中等待的线程来竞争锁，竞争的时是非公平的
- 图中 WaitSet 中的 Thread-0，Thread-1 是之前获得过锁，但条件不满足进入 WAITING 状态的线程（与wait-notify有关）

注意：
- synchronized 必须是进入同一个对象的 monitor 才有上述的效果
- 不加 synchronized 的对象不会关联监视器，不遵从以上规则

一个锁对象中的 `MarkWord` 会指向一个 Monitor 对象，不同的锁对象关联不同的 Monitor

![image-20231215104847684](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215104847684.png)

## synchronized 原理

### 1. 轻量级锁

对象中轻量级锁的 `MarkWord` 结构：

![image-20231215111040387](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215111040387.png)

轻量级锁的使用场景：如果一个对象虽然有多线程要加锁，但加锁的时间是错开的（也就是没有竞争），那么可以使用轻量级锁来优化。

轻量级锁对使用者是透明的，即语法仍然是 `synchronized`

假设有两个方法同步块，利用同一个对象加锁：

```java
static final Object obj = new Object();
public static void method1() {
    synchronized( obj ) {
		// 同步块 A
        method2();
    }
}
public static void method2() {
    synchronized( obj ) {
		// 同步块 B
    }
}
```

轻量级锁的加锁解锁流程：

1. 创建**锁记录（Lock Record，LR）对象**，每个线程都的栈帧都会包含一个锁记录的结构，内部可以存储锁定对象的 Mark Word

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215111405654.png" alt="image-20231215111405654" style="zoom:50%;" />

01 表示没有锁（或者偏向锁），从上面MarkWord结构可知

2. 让锁记录中 Object reference 指向锁对象，并尝试用 cas 替换 Object 的 Mark Word，将 Mark Word 的值存入锁记录

   cas ：取出 MarkWord值，然后尝试修改该值，写回时比较当前MarkWord值是否和当初取出的值相同，如果相同则将修改的值写入

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215111653457.png" alt="image-20231215111653457" style="zoom:50%;" />

3. 如果 cas 替换成功，对象头中存储了锁记录地址和状态 00 ，表示由该线程给对象加锁，这时图示如下

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215111729648.png" alt="image-20231215111729648" style="zoom:50%;" />

4. 如果 cas 失败，有两种情况
   - 如果是其它线程已经持有了该 Object 的轻量级锁，这时表明有竞争，进入锁膨胀过程
   - 如果是自己执行了 synchronized 锁重入，那么再添加一条 Lock Record 作为重入的计数

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215111755058.png" alt="image-20231215111755058" style="zoom:50%;" />

5. 当退出 synchronized 代码块（解锁时）如果有取值为 null 的锁记录，表示有重入，这时重置锁记录，表示重入计数减一

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215111823128.png" alt="image-20231215111823128" style="zoom:50%;" />

6. 当退出 synchronized 代码块（解锁时）锁记录的值不为 null，这时使用 cas 将 Mark Word 的值恢复给对象头
   - 成功，则解锁成功
   - 失败，说明轻量级锁进行了锁膨胀或已经升级为重量级锁，进入重量级锁解锁流程

### 2. 锁膨胀——轻量级锁膨胀为重量级锁

如果在尝试加轻量级锁的过程中，CAS 操作无法成功，这时一种情况就是有其它线程为此对象加上了轻量级锁（有竞争），这时需要进行锁膨胀，将轻量级锁变为重量级锁。

- 当 Thread-1 进行轻量级加锁时，Thread-0 已经对该对象加了轻量级锁：

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215112213291.png" alt="image-20231215112213291" style="zoom:50%;" />

- 这时 Thread-1 加轻量级锁失败，进入锁膨胀流程

  - 即为 Object 对象申请 Monitor 锁，创建一个Monitor对象管理多线程，让 Object 指向重量级锁（Monitor对象）地址

  - 然后 Thread-1 自己进入 Monitor 的 EntryList BLOCKED

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215112433566.png" alt="image-20231215112433566" style="zoom:50%;" />

- 当 Thread-0 退出同步块解锁时，使用 cas 将 Mark Word 的值恢复给对象头，失败，因为现在MarkWord已经变为Monitor地址了。
  - 这时会进入重量级解锁流程，即按照 Monitor 地址找到 Monitor 对象，设置 Owner 为 null，唤醒 EntryList 中 BLOCKED 线程

### 3. 自旋优化

重量级锁竞争的时候，还可以使用自旋来进行优化，如果当前线程自旋成功（即这时候持锁线程已经退出了同步块，释放了锁），这时当前线程就可以避免阻塞。因为阻塞会发生上下文切换，避免阻塞，上下文切换也会避免，减少了上下文切换的次数，优化了性能

自旋重试成功的一种情况：（这里多核CPU下才有意义）

![image-20231215114340941](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215114340941.png)

自旋重试失败的一种情况：

![image-20231215114608620](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215114608620.png)

- 自旋会占用 CPU 时间，单核 CPU 自旋就是浪费，多核 CPU 自旋才能发挥优势。
- 在 Java 6 之后自旋锁是自适应的，比如对象刚刚的一次自旋操作成功过，那么认为这次自旋成功的可能性会高，就多自旋几次；反之，就少自旋甚至不自旋，总之，比较智能。
- Java 7 之后不能控制是否开启自旋功能

### 4. 偏向锁

==自JDK15起，偏向锁已被废弃，JDK20被移除，可以在JDK8中将其关闭以提高性能==

#### 偏向状态

轻量级锁在没有竞争时（就自己这个线程），每次重入仍然需要执行 CAS 操作。

Java 6 中引入了偏向锁来做进一步优化：只有第一次使用 CAS 将线程 ID 设置到对象的 Mark Word 头，之后发现这个线程 ID 是自己的就表示没有竞争，不用重新 CAS。以后只要不发生竞争，这个对象就归该线程所有

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215142334025.png" alt="image-20231215142334025" style="zoom:50%;" />

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215142351442.png" alt="image-20231215142351442" style="zoom:50%;" />



对象头格式：（延迟几秒后，MarkWord 最后三位是101）

![image-20231215141305761](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215141305761.png)

一个对象创建时：

- 如果开启了偏向锁（默认开启），那么对象创建后，markword 值为 0x05 即最后 3 位为 101，这时它的 thread、epoch、age 都为 0
- 偏向锁是默认是延迟的，不会在程序启动时立即生效，如果想避免延迟，可以加 VM 参数 `-XX:BiasedLockingStartupDelay=0` 来禁用延迟
- 如果没有开启偏向锁，那么对象创建后，markword 值为 0x01 即最后 3 位为 001，这时它的 hashcode、age 都为 0，第一次用到 hashcode 时才会赋值

**hashCode：正常状态对象一开始是没有 hashCode 的，第一次调用才生成；生成 hasCode 后，偏向锁就被禁止使用了，因为偏向锁无法存下 hashCode** ，偏向锁被撤销了

#### 偏向锁撤销的情况

1. 撤销 - 调用对象 hashCode

   - 调用了对象的 hashCode，但偏向锁的对象 MarkWord 中存储的是线程 id，如果调用 hashCode 会导致偏向锁被撤销

   - 轻量级锁会在锁记录中记录 hashCode

   - 重量级锁会在 Monitor 中记录 hashCode

2. 撤销 - 其它线程使用对象

   当有其它线程使用偏向锁对象时，会将偏向锁升级为轻量级锁

   <img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215143758505.png" alt="image-20231215143758505" style="zoom:50%;" />

3. 调用 wait/notify：只有重量级锁才有 wait/notify操作，会被升级为重量级锁

#### 批量重偏向

如果对象虽然被多个线程访问，但没有竞争（线程交替访问锁对象），这时有一个原本偏向 T1 的偏向锁，T2 在访问T1的偏向锁时会将偏向锁撤销，升级为轻量级锁。随后轻量级锁被释放，再访问锁对象时，又会产生一个偏向 T1 的偏向锁，反复多次这种流程会耗费性能

所以希望在偏向了线程 T1 的对象仍有机会重新偏向 T2，重偏向会重置对象的 Thread ID

当撤销偏向锁（加锁，解锁次数）阈值超过 20 次后，jvm 会这样觉得，我是不是偏向错了呢，于是会在给这些对象加锁时重新偏向至加锁线程

#### 批量撤销

当撤销偏向锁阈值超过 40 次后，jvm 会这样觉得，自己确实偏向错了，根本就不该偏向。于是整个类的所有对象都会变为不可偏向的，新建的对象也是不可偏向的

### 5. 锁消除

JIT 即时编译器，会优化代码，把不会被共享的锁对象的同步代码块中的加锁操作消除

`-XX:-EliminateLocks` ：vm option 指令，禁止锁消除优化

锁粗化：对相同对象多次加锁，导致线程发生多次重入，可以使用锁粗化方式来优化，这不同于之前讲的细分锁的粒度。

## wait notify 原理

![image-20231215150745022](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215150745022.png)

- Owner 线程发现条件不满足，调用 wait 方法，即可进入 WaitSet 变为 WAITING 状态
- BLOCKED 和 WAITING 的线程都处于阻塞状态，不占用 CPU 时间片
- BLOCKED 线程会在 Owner 线程释放锁时唤醒
- WAITING 线程会在 Owner 线程调用 notify 或 notifyAll 时唤醒，但唤醒后并不意味者立刻获得锁，仍需进入EntryList 重新竞争

waiting 状态的线程是获得锁后又放弃锁进入的状态，而 blocked 状态是还没获得锁的，他俩都是阻塞状态

### API 介绍

- `obj.wait() `让进入 object 监视器的线程到 waitSet 等待
- `obj.notify() ` 在 object 上正在 waitSet 等待的线程中挑一个唤醒
- `obj.notifyAll()`  让 object 上正在 waitSet 等待的线程全部唤醒

以上方法的调用必须是先获得锁（成为 Owner以后）；测试：

```java
@Slf4j(topic = "c.d4_wait_notify")
public class d4_wait_notify {
    final static Object obj = new Object();
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            synchronized (obj) {
                log.debug("执行....");
                try {
                    obj.wait(); // 让线程在obj上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("其它代码....");
            }
        }, "t1").start();
        new Thread(() -> {
            synchronized (obj) {
                log.debug("执行....");
                try {
                    obj.wait(); // 让线程在obj上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("其它代码....");
            }
        }, "t2").start();
        // 主线程两秒后执行
        Thread.sleep(2000);
        log.debug("唤醒 obj 上其它线程");
        synchronized (obj) {
            obj.notify(); // 唤醒obj上一个线程
            // obj.notifyAll(); // 唤醒obj上所有等待线程
        }
    }
}	
```

结果：

```java
15:18:02 [t1] c.d4_wait_notify - 执行....
15:18:02 [t2] c.d4_wait_notify - 执行....
15:18:04 [main] c.d4_wait_notify - 唤醒 obj 上其它线程
15:18:04 [t1] c.d4_wait_notify - 其它代码....
```

`wait() ` 方法会释放对象的锁，进入 WaitSet 等待区，从而让其他线程就机会获取对象的锁。无限制等待，直到 `notify`  为止
`wait(long n)`  有时限的等待, 到 n 毫秒后结束等待，或是被 `notify`

## wait notify 的正确使用方式

### `sleep(long n)`  和 `wait(long n)`  的区别

1. sleep 是 Thread 方法，而 wait 是 Object 的方法
2. sleep 不需要强制和 synchronized 配合使用，但 wait 需要和 synchronized 一起用 
3. sleep 在睡眠的同时，**不会释放对象锁的**，其他线程想用锁只能在EntryList里阻塞等待；但 **wait 在等待的时候会释放对象锁**，当前线程会进入 WaitList 等待，其他 EntryList里等待的线程会被唤醒一个获得释放的锁
4. 它们状态都是 TIMED_WAITING

当有多个线程进入 waitList ，我们要选择其中一个唤醒，可以为每个线程设置一个标志 flag，然后唤醒所有线程；唤醒后，每个线程检查自己的 flag条件，如果 flag 不满足，则一直 wait 等待：

```java
synchronized(lock) {
    while(条件不成立) {
    	lock.wait();
    }
	// 干活
}

//另一个线程
synchronized(lock) {
	lock.notifyAll();
}
```

## 同步模式之保护性暂停

### 1. 定义

即 Guarded Suspension，用在一个线程等待另一个线程的执行结果

要点：

- 有一个结果需要从一个线程传递到另一个线程，让他们关联同一个 GuardedObject
- 如果有结果不断从一个线程到另一个线程那么可以使用消息队列（见生产者/消费者）
- JDK 中，join 的实现、Future 的实现，采用的就是此模式
- 因为要等待另一方的结果，因此归类到同步模式

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215154553879.png" alt="image-20231215154553879" style="zoom:50%;" />

### 2. 实现：

```java
@Slf4j(topic = "c.d6_GuardedObject")
public class d6_GuardedObject {
    private Object response;
    private final Object lock = new Object();
    public Object get() {
        synchronized (lock) {
            // 条件不满足则等待
            while (response == null) {
                log.debug("等待 response ");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }
    }
    public void complete(Object response) {
        synchronized (lock) {
            // 条件满足，通知等待线程
            this.response = response;
            lock.notifyAll();
        }
    }

    public static void main(String[] args) {
        d6_GuardedObject guardedObject = new d6_GuardedObject();
        new Thread(()->{
            Object myResponse = guardedObject.get();
            log.debug("myResponse: {}", myResponse);
        }, "t1").start();

        new Thread(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            guardedObject.complete("等待结束...");
        }, "t2").start();
    }
}
```

```java
15:55:48 [t1] c.d6_GuardedObject - 等待 response 
15:55:49 [t1] c.d6_GuardedObject - myResponse: 等待结束...
```

### 3. 带有时间限制的实现：

```java
@Slf4j(topic = "c.d7_CuardedObjectV2")
public class d7_CuardedObjectV2 {
    private Object response;
    private final Object lock = new Object();

    public Object get(long millis) {
        synchronized (lock) {
            // 1) 记录最初时间
            long begin = System.currentTimeMillis();
            // 2) 已经经历的时间
            long timePassed = 0;
            while (response == null) {
                // 4) 假设 millis 是 1000，结果在 400 时唤醒了，那么还有 600 要等
                long waitTime = millis - timePassed;
                log.debug("waitTime: {}", waitTime);
                if (waitTime <= 0) {
                    log.debug("break...");
                    break;
                }
                try {
                    lock.wait(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 3) 如果提前被唤醒，这时已经经历的时间假设为 400
                timePassed = System.currentTimeMillis() - begin;
                log.debug("timePassed: {}, object is null {}",
                        timePassed, response == null);
            }
            return response;
        }
    }
    public void complete(Object response) {
        synchronized (lock) {
            // 条件满足，通知等待线程
            this.response = response;
            log.debug("notify...");
            lock.notifyAll();
        }
    }

    public static void main(String[] args) {
        d7_CuardedObjectV2 v2 = new d7_CuardedObjectV2();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            v2.complete(null);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            v2.complete(Arrays.asList("a", "b", "c"));
        }).start();

        Object response = v2.get(2500);
        if (response != null) {
            log.debug("get response: [{}] lines", ((List<String>) response).size());
        } else {
            log.debug("can't get response");
        }
    }
}
```

```java
16:45:02 [main] c.d7_CuardedObjectV2 - waitTime: 2500
16:45:03 [Thread-0] c.d7_CuardedObjectV2 - notify...
16:45:03 [main] c.d7_CuardedObjectV2 - timePassed: 1012, object is null true
16:45:03 [main] c.d7_CuardedObjectV2 - waitTime: 1488
16:45:04 [Thread-0] c.d7_CuardedObjectV2 - notify...
16:45:04 [main] c.d7_CuardedObjectV2 - timePassed: 2014, object is null false
16:45:04 [main] c.d7_CuardedObjectV2 - get response: [3] lines
```

## Join 原理

是调用者轮询检查线程 alive 状态

```java
t1.join();
// 等价于下面的代码
synchronized (t1) {
    // 调用者线程进入 t1 的 waitSet 等待, 直到 t1 运行结束
    while (t1.isAlive()) {
        t1.wait(0);
    }
}
```

join 源码：其实也是用wait实现的带有时间限制的保护性暂停

```java
public final void join(long millis) throws InterruptedException {
    // ... millis 非法异常判断
    synchronized (this) {
        if (millis > 0) {
            if (isAlive()) {
                final long startTime = System.nanoTime(); // 记录最初时间
                long delay = millis;
                do {
                    wait(delay);
                } while (isAlive() && (delay = millis -
                         NANOSECONDS.toMillis(System.nanoTime() - startTime)) > 0);
                // passTime = System.nanoTime() - startTime ： 获取已经经历过的时间
                // delay = millis - passTime ：更新剩余该等待的时间
            }
        } else {
            while (isAlive()) {
                wait(0);
            }
        }
    }
}
```

## Park & Unpark

它们是 LockSupport 类中的方法

```java
// 暂停当前线程
LockSupport.park();
// 恢复某个线程的运行
LockSupport.unpark(暂停线程对象)
```

### 特点

与 Object 的 wait & notify 相比

- wait，notify 和 notifyAll 必须配合 Object Monitor 一起使用，而 park，unpark 不必
- park & unpark 是以**指定线程为单位**来【阻塞】和【唤醒】线程，而 notify 只能**随机唤醒**一个等待线程，notifyAll是唤醒所有等待线程，就不那么【精确】
- park & unpark 可以先 unpark，而 wait & notify 不能先 notify

### 原理

每个线程都有自己的一个 Parker 对象，由三部分组成 _counter ， _cond 和 _mutex 打个比喻

- 线程就像一个旅人，Parker 就像他随身携带的背包，条件变量就好比背包中的帐篷。_counter 就好比背包中的备用干粮（0 为耗尽，1 为充足）

- 调用 park 就是要看需不需要停下来歇息

  - 如果备用干粮耗尽，那么钻进帐篷歇息

  - 如果备用干粮充足，那么不需停留，继续前进

- 调用 unpark，就好比令干粮充足
  - 如果这时线程还在帐篷，就唤醒让他继续前进
  - 如果这时线程还在运行，那么下次他调用 park 时，仅是消耗掉备用干粮，不需停留继续前进
    - 因为背包空间有限，多次调用 unpark 仅会补充一份备用干粮

调用 park

1. 当前线程调用 Unsafe.park() 方法
2. 检查 _counter ，本情况为 0，这时，获得 _mutex 互斥锁
3. 线程进入 _cond 条件变量阻塞
4. 设置 _counter = 0

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215171913892.png" alt="image-20231215171913892" style="zoom:50%;" />

调用 unpark:

1. 调用 Unsafe.unpark(Thread_0) 方法，设置 _counter 为 1
2. 唤醒 _cond 条件变量中的 Thread_0
3. Thread_0 恢复运行
4. 设置 _counter 为 0

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215172124724.png" alt="image-20231215172124724" style="zoom:50%;" />

先调用 unpark 再调用 park:

1. 调用 Unsafe.unpark(Thread_0) 方法，设置 _counter 为 1
2. 唤醒 _cond 条件变量中的 Thread_0
3. Thread_0 恢复运行
4. 设置 _counter 为 0

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215172420879.png" alt="image-20231215172420879" style="zoom:50%;" />

## 线程状态转换

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215172745564.png" alt="image-20231215172745564" style="zoom:50%;" />

假设有线程 Thread t

### NEW --> RUNNABLE

**情况 1 NEW --> RUNNABLE**

当调用 t.start() 方法时，由 NEW --> RUNNABLE

### RUNNABLE <--> WAITING：-> wait(), join(), park()

**情况 2 RUNNABLE <--> WAITING**

t 线程用 synchronized(obj) 获取了对象锁后

- 调用 obj.wait() 方法时，t 线程从 RUNNABLE --> WAITING

- 调用 obj.notify() ， obj.notifyAll() ， t.interrupt() 时

  - 竞争锁成功，t 线程从 WAITING --> RUNNABLE

  - **竞争锁失败，t 线程从 WAITING --> BLOCKED**

**情况 3 RUNNABLE <--> WAITING**

- 当前线程调用 t.join() 方法时，当前线程从 RUNNABLE --> WAITING
  - 注意是当前线程在t 线程对象的监视器上等待
- t 线程运行结束，或调用了当前线程的 interrupt() 时，当前线程从 WAITING --> RUNNABLE

**情况 4 RUNNABLE <--> WAITING**

- 当前线程调用 LockSupport.park() 方法会让当前线程从 RUNNABLE --> WAITING
- 调用 LockSupport.unpark(目标线程) 或调用了线程 的 interrupt() ，会让目标线程从 WAITING -->RUNNABLE

### RUNNABLE <--> TIMED_WAITING：-> wait(n), join(n), sleep(n)

**情况 5 RUNNABLE <--> TIMED_WAITING**

t 线程用 synchronized(obj) 获取了对象锁后

- 调用 obj.wait(long n) 方法时，t 线程从 RUNNABLE --> TIMED_WAITING
- t 线程等待时间超过了 n 毫秒，或调用 obj.notify() ， obj.notifyAll() ， t.interrupt() 时
  - 竞争锁成功，t 线程从 TIMED_WAITING --> RUNNABLE
  - 竞争锁失败，t 线程从 TIMED_WAITING --> BLOCKED

**情况 6 RUNNABLE <--> TIMED_WAITING**

- 当前线程调用 t.join(long n) 方法时，当前线程从 RUNNABLE --> TIMED_WAITING
  - 注意是当前线程在t 线程对象的监视器上等待
- 当前线程等待时间超过了 n 毫秒，或t 线程运行结束，或调用了当前线程的 interrupt() 时，当前线程从 TIMED_WAITING --> RUNNABLE

**情况 7 RUNNABLE <--> TIMED_WAITING**

- 当前线程调用 Thread.sleep(long n) ，当前线程从 RUNNABLE --> TIMED_WAITING
- 当前线程等待时间超过了 n 毫秒，当前线程从 TIMED_WAITING --> RUNNABLE

**情况 8 RUNNABLE <--> TIMED_WAITING**

- 当前线程调用 LockSupport.parkNanos(long nanos) 或 LockSupport.parkUntil(long millis) 时，当前线程从 RUNNABLE --> TIMED_WAITING
- 调用 LockSupport.unpark(目标线程) 或调用了线程 的 interrupt() ，或是等待超时，会让目标线程从TIMED_WAITING--> RUNNABLE

### RUNNABLE <--> BLOCKED 

**情况 9 RUNNABLE <--> BLOCKED**

t 线程用 synchronized(obj) 获取了对象锁时如果竞争失败，从 RUNNABLE --> BLOCKED

- 持 obj 锁线程的同步代码块执行完毕，会唤醒该对象上所有 BLOCKED 的线程重新竞争，如果其中 t 线程竞争成功，从 BLOCKED --> RUNNABLE ，其它失败的线程仍然 BLOCKED

### RUNNABLE <--> TERMINATED

**情况 10 RUNNABLE <--> TERMINATED**

当前线程所有代码运行完毕，进入TERMINATED

## 活跃性

### 死锁

有这样的情况：一个线程需要同时获取多把锁，这时就容易发生死锁
t1 线程 获得 A对象 锁，接下来想获取 B对象的锁 t2 线程 获得 B对象 锁

### 定位死锁

检测死锁可以使用 jconsole工具，或者使用 jps 定位进程 id，再用 jstack 定位死锁：

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215190751265.png" alt="image-20231215190751265" style="zoom: 67%;" />

jstack定位死锁：

t1 等待的锁对象被 t2 锁住了；t1锁住的对象被 t2等待

![image-20231215191259272](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215191259272.png)

stack也会提示找到的死锁：

![image-20231215191420513](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215191420513.png)

- 避免死锁要注意加锁顺序
- 另外如果由于某个线程进入了死循环，导致其它线程一直等待，对于这种情况 linux 下可以通过 top 先定位到 CPU 占用高的 Java 进程，再利用 top -Hp 进程id 来定位是哪个线程，最后再用 jstack 排查

### 活锁

活锁出现在两个线程互相改变对方的结束条件，最后**谁也无法结束**，例如：

死锁是无法继续运行下去，活锁是一直运行下去

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.d9_TestLiveLock")
public class d9_TestLiveLock {
    static volatile int count = 10;
    static final Object lock = new Object();
    public static void main(String[] args) {
        new Thread(() -> {
            // 期望减到 0 退出循环
            while (count > 0) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                count--;
                log.debug("count: {}", count);
            }
        }, "t1").start();
        new Thread(() -> {
            // 期望超过 20 退出循环
            while (count < 20) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                count++;
                log.debug("count: {}", count);
            }
        }, "t2").start();
    }
}
```

### 饥饿

很多教程中把饥饿定义为，一个线程由于优先级太低，始终得不到 CPU 调度执行，也不能够结束，饥饿的情况不易演示，讲读写锁时会涉及饥饿问题

的一个线程饥饿的例子，可以使用顺序加锁的方式解决之前的死锁问题

线程1锁住了A，线程2锁住了B，死锁发生：

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215192940761.png" alt="image-20231215192940761" style="zoom:50%;" />

顺序加锁来避免死锁的解决方案：

必须先获得锁对象A才能获得锁对象B，这样就不会出现，线程2在获取A的时候就会阻塞而一直等待线程1释放

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231215193057445.png" alt="image-20231215193057445" style="zoom:50%;" />

## ReentrantLock（可重入锁）

相对于 synchronized 它具备如下特点

- 可中断
- 可以设置超时时间
- 可以设置为公平锁（避免饥饿）
- 支持多个条件变量

与 synchronized 一样，都支持可重入

基本语法

```java
// 获取锁
reentrantLock.lock();
try {
	// 临界区
} finally {
	// 释放锁
	reentrantLock.unlock();
}
```

### 可重入

可重入是指同一个线程如果首次获得了这把锁，那么因为它是这把锁的拥有者，因此有权利再次获取这把锁

如果是不可重入锁，那么第二次获得锁时，自己也会被锁挡住

对一个锁对象反复加锁的示例：

```java
@Slf4j(topic = "c.d10_ReentrantLock")
public class d10_ReentrantLock {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        lock.lock();
        try {
            log.debug("enter main");
            method1();
        }finally {
            lock.unlock();
        }
    }

    public static void method1(){
        lock.lock();
        try {
            log.debug("enter method1");
            method2();
        }finally {
            lock.unlock();
        }
    }
    public static void method2(){
        lock.lock();
        try {
            log.debug("enter method2");
        }finally {
            lock.unlock();
        }
    }
}
```

输出：

```java
19:45:30 [main] c.d10_ReentrantLock - enter main
19:45:30 [main] c.d10_ReentrantLock - enter method1
19:45:30 [main] c.d10_ReentrantLock - enter method2
```

### 可打断

可以用让其他线程调用 interrupt 方法打断当前的锁

需要使用 `lockInterruptibly` 方法进行加锁才可被打断：

```java
@Slf4j(topic = "c.d11_TestLockInterrupt")
public class d11_TestLockInterrupt {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(()->{
            try {
                log.debug("尝试获得锁");
                // lockInterruptibly：
                //  如果没有竞争，此方法会获取lock对象的锁
                //  如果有竞争，则进去阻塞队列，可以被其他线程用interrupt方法打断
                lock.lockInterruptibly();
            }catch (InterruptedException e){
                e.printStackTrace();
                log.debug("没有获得锁，返回");
                return;
            }

            try {
                log.debug("获取到锁");
            }finally {
                lock.unlock();
            }
        }, "t1");

        lock.lock(); // 主线程先获取锁，t1等待
        t1.start();

        Thread.sleep(1000);
        log.debug("打断 t1");
        t1.interrupt();
    }
}
```

```java
19:53:49 [t1] c.d11_TestLockInterrupt - 尝试获得锁
19:53:50 [main] c.d11_TestLockInterrupt - 打断 t1
19:53:50 [t1] c.d11_TestLockInterrupt - 没有获得锁，返回
java.lang.InterruptedException at ... ...
```

### 锁超时

可打断是被动的，等待其他线程打断。锁超时是主动的打断，避免无限时的等待下去

使用 `tryLock` 方法实现 没获得到锁就立刻失败：

```java
ReentrantLock lock = new ReentrantLock();
Thread t1 = new Thread(() -> {
    log.debug("启动...");
    if (!lock.tryLock()) { // trylock尝试获取锁
        log.debug("获取立刻失败，返回");
        return;
    }
    try {
        log.debug("获得了锁");
    } finally {
        lock.unlock();
    }
}, "t1");
lock.lock();
log.debug("获得了锁");
t1.start();
try {
    Thread.sleep(2000);
} finally {
    lock.unlock();
}
```

超时失败实现：

`lock.tryLock(1, TimeUnit.SECONDS)`

### 公平锁

ReentrantLock 默认是不公平的。根据构造参数可以设置为公平锁：

```java
ReentrantLock lock = new ReentrantLock(true);
```

### 条件变量
synchronized 中也有条件变量，就是我们讲原理时那个 waitSet 休息室，当条件不满足时进入 waitSet 等待

ReentrantLock 的条件变量比 synchronized 强大之处在于，它是支持多个条件变量的，不同在于：

- synchronized 是那些不满足条件的**线程都在一间休息室**等消息
- 而 ReentrantLock 支持**多间休息室**，有专门等烟的休息室、专门等早餐的休息室、唤醒时也是按休息室来唤醒

使用要点：

- await 前需要获得锁
- await 执行后，会释放锁，进入 conditionObject 等待
- await 的线程被唤醒（或打断、或超时）取重新竞争 lock 锁
  竞争 lock 锁成功后，从 await 后继续执行

使用方式：

通过 `lock.newCondtition()` 的方法创建一个新的条件变量对象：

```java
@Slf4j(topic = "c.d12_TestConditionLock")
public class d12_TestConditionLock {
    static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();

        lock.lock();
        // 进入条件1（休息室1）等待
        condition1.await();

        // 叫醒休息室1中的线程：
        condition1.signal();
        condition1.signalAll();
    }
}
```

# 4 Java 内存模型 与 volatile原理

JMM 即 Java Memory Model，它定义了主存、工作内存抽象概念，底层对应着 CPU 寄存器、缓存、硬件内存、CPU 指令优化等。

JMM 体现在以下几个方面

- 原子性 - 保证指令不会受到线程上下文切换的影响
- 可见性 - 保证指令不会受 cpu 缓存的影响
- 有序性 - 保证指令不会受 cpu 指令并行优化的影响

## 可见性

JMM 中每个线程有自己的本地内存，对于那些共享的变量会先从主内存中读取再存入本地内存中，以减少对主内存的访问次数，提高效率。但是如果其中一个内存的值发生了改变，就无法及时更新到其他内存中，导致线程直接对共享变量存储的值不一致，也就是线程之间对其他线程是不可见的。

解决方案之一是 **volatile（易变关键字）**

它可以用来修饰成员变量和静态成员变量，他可以避免线程从自己的工作缓存中查找变量的值，**必须到主存中获取它的值**，线程操作 volatile 变量都是直接操作主存。

## 原子性

前面例子体现的实际就是可见性，它保证的是在多个线程之间，一个线程对 volatile 变量的修改对另一个线程可见， 但是不能保证原子性

volatile 适用于仅用在一个写线程，多个读线程的情况
> synchronized 语句块既可以保证代码块的原子性，也同时保证代码块内变量的可见性。但缺点是synchronized 是属于重量级操作，性能相对更低
>
> 如果在前面示例的死循环中加入 System.out.println() 会发现即使不加 volatile 修饰符，线程 t 也能正确看到对 run 变量的修改了，因为 println底层用synchronized加了锁

## 有序性

JVM 会在不影响正确性的前提下，可以调整语句的执行顺序。

volatile 修饰的变量也会禁止重排序

## volatile 原理

volatile 的底层实现原理是内存屏障，Memory Barrier（Memory Fence）

- 对 volatile 变量的写指令后会加入写屏障：写屏障（sfence）保证在该屏障之前的，对共享变量的改动，都同步到主存当中
- 对 volatile 变量的读指令前会加入读屏障：而读屏障（lfence）保证在该屏障之后，对共享变量的读取，加载的是主存中最新数据

![image-20231217160432003](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231217160432003.png)

### 有序性的保证

写屏障会确保指令重排序时，不会将写屏障之前的代码排在写屏障之后

读屏障会确保指令重排序时，不会将读屏障之后的代码排在读屏障之前

**不能解决指令交错：**

- 写屏障仅仅是保证之后的读能够读到最新的结果，但不能保证读跑到它前面去
- 而有序性的保证也只是保证了本线程内相关代码不被重排序

即使 i 使用 volatile 保证每次读取是最新主存中的值，但是如果 t2 线程提前读了 i 的值，读到的数据也是不对的

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231217160808796.png" alt="image-20231217160808796" style="zoom:50%;" />

### double-checked locking 问题

```java
public final class d1_Singleton {
    private d1_Singleton() { }
    private static d1_Singleton INSTANCE = null;
    public static d1_Singleton getInstance() {
        if(INSTANCE == null) { // t2
            // 首次访问会同步，而之后的使用没有 synchronized
            synchronized(d1_Singleton.class) {
                if (INSTANCE == null) { // t1
                    INSTANCE = new v();
                }
            }
        }
        return INSTANCE;
    }
}
```

以上的实现特点是：

- 懒惰实例化
- 首次使用 getInstance() 才使用 synchronized 加锁，后续使用时无需加锁，后续直接 return 了
- 有隐含的，但很关键的一点：第一个 if 使用了 INSTANCE 变量，是在同步块之外
- **需要判断两次 INSTANCE是否为 null ** ：因为可能有多个线程同时都判断为 null 进入 if 里面，但是只有一个线程会进入同步代码块，在前面的线程进入之后会创建一个 INSTANCE，那么后面进入的那个线程就需要不判断 INSTANCE 是否为空，就会创建两个对象。

但在多线程环境下，上面的代码是有问题的，synchronized 无法保证有序性

getInstance 方法对应的字节码为：

```java
0: getstatic #2 // Field INSTANCE:Lcn/itcast/n5/Singleton;
3: ifnonnull 37
6: ldc #3 // class cn/itcast/n5/Singleton
8: dup
9: astore_0
10: monitorenter
11: getstatic #2 // Field INSTANCE:Lcn/itcast/n5/Singleton;
14: ifnonnull 27
17: new #3 // class cn/itcast/n5/Singleton
20: dup
21: invokespecial #4 // Method "<init>":()V
24: putstatic #2 // Field INSTANCE:Lcn/itcast/n5/Singleton;
27: aload_0
28: monitorexit
29: goto 37
32: astore_1
33: aload_0
34: monitorexit
35: aload_1
36: athrow
37: getstatic #2 // Field INSTANCE:Lcn/itcast/n5/Singleton;
40: areturn
```

其中

- 17 表示创建对象，将对象引用入栈 // new Singleton
- 20 表示复制一份对象引用 // 引用地址
- 21 表示利用一个对象引用，调用构造方法
- 24 表示利用一个对象引用，赋值给 static INSTANCE

也许 jvm 会优化为：**先执行 24，再执行 21** （先赋值一个地址的空壳，再调用构造方法）

如果两个线程 t1，t2 按如下时间序列执行：

![image-20231217163000777](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231217163000777.png)

关键在于 0: getstatic 这行代码在 monitor 控制之外，它就像之前举例中不守规则的人，可以越过 monitor 读取INSTANCE 变量的值

这时 t1 还未完全将构造方法执行完毕，如果在构造方法中要执行很多初始化操作，那么 t2 拿到的是将是一个未初始化完毕的单例

对 INSTANCE 使用 volatile 修饰即可，可以禁用指令重排，但要注意在 JDK 5 以上的版本的 volatile 才会真正有效

![image-20231217163457255](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231217163457255.png)

## happens-before

happens-before 规定了对共享变量的写操作对其它线程的读操作可见，它是可见性与有序性的一套规则总结，抛开以下 happens-before 规则，JMM 并不能保证一个线程对共享变量的写，对于其它线程对该共享变量的读可见

- 线程解锁 m 之前对变量的写，对于接下来对 m 加锁的其它线程对该变量的读可见

- 线程对 volatile 变量的写，对接下来其它线程对该变量的读可见

- 线程 start 前对变量的写，对该线程开始后对该变量的读可见

  ```java
  static int x;
  x = 10;
  new Thread(()->{
  	System.out.println(x);
  },"t2").start();
  ```

- 线程结束前对变量的写，对其它线程得知它结束后的读可见（比如其它线程调用 t1.isAlive() 或 t1.join()等待 它结束）

  ```java
  static int x;
  Thread t1 = new Thread(()->{
  	x = 10;
  },"t1");
  t1.start();
  t1.join();
  System.out.println(x);
  ```

- 线程 t1 打断 t2（interrupt）前对变量的写，对于其他线程得知 t2 被打断后对变量的读可见（通过 t2.interrupted 或 t2.isInterrupted）

- 对变量默认值（0，false，null）的写，对其它线程对该变量的读可见

-  happens before 具有传递性，如果 x happens before -> y 并且 y  happens before-> z 那么有 x  happens before-> z ，配合 volatile 的防指令重排

  ```java
  volatile static int x;
  static int y;
  new Thread(()->{
      y = 10;
      x = 20;
  },"t1").start();
  
  new Thread(()->{
      // x=20 对 t2 可见, 同时 y=10 也对 t2 可见
      System.out.println(x);
  },"t2").start();
  ```

# 5 并发编程中的无锁实现

## CAS 与 voltaile

```java
public class d1_AccountCAS implements Account{
    private AtomicInteger balance;
    public d1_AccountCAS(Integer balance) {
        this.balance = new AtomicInteger(balance);
    }
    @Override
    public Integer getBalance() {
        return balance.get();
    }
    @Override
    public void withdraw(Integer amount) {
        while (true) {
            // 原先值
            int prev = balance.get();
            // 当前值
            int next = prev - amount;
            // 比较并修改
            if (balance.compareAndSet(prev, next)) {
                break;
            }
        }
        // 可以简化为下面的方法
        // balance.addAndGet(-1 * amount);
    }

    public static void main(String[] args) {
        Account.demo(new d1_AccountCAS(10000));
    }
}

interface Account {
    // 获取余额
    Integer getBalance();
    // 取款
    void withdraw(Integer amount);
    /**
     * 方法内会启动 1000 个线程，每个线程做 -10 元 的操作
     * 如果初始余额为 10000 那么正确的结果应当是 0
     */
    static void demo(Account account) {
        List<Thread> ts = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            ts.add(new Thread(() -> {
                account.withdraw(10);
            }));
        }
        ts.forEach(Thread::start);
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long end = System.nanoTime();
        System.out.println(account.getBalance()
                + " cost: " + (end-start)/1000_000 + " ms");
    }
}
```

compareAndSet，它的简称就是 CAS （也有 Compare And Swap 的说法），它必须是原子操作。

- 其实 CAS 的底层是 lock cmpxchg 指令（X86 架构），在单核 CPU 和多核 CPU 下都能够保证【比较-交换】的原子性。
- 在多核状态下，某个核执行到带 lock 的指令时，CPU 会让总线锁住，当这个核把此指令执行完毕，再开启总线。这个过程中不会被线程的调度机制所打断，保证了多个线程对内存操作的准确性，是原子的。

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231218110018631.png" alt="image-20231218110018631" style="zoom:50%;" />

**volatile**

获取共享变量时，为了保证该变量的可见性，需要使用 volatile 修饰。

它可以用来修饰成员变量和静态成员变量，避免线程从自己的工作缓存中查找变量的值，必须到主存中获取它的值，线程操作 volatile 变量都是直接操作主存。即一个线程对 volatile 变量的修改，对另一个线程可见。

**注意**

volatile 仅仅保证了共享变量的可见性，让其它线程能够看到最新值，但不能解决指令交错问题（不能保证原子性）

CAS 必须借助 volatile 才能读取到共享变量的最新值来实现【比较并交换】的效果

**CAS 的特点**

结合 CAS 和 volatile 可以实现无锁并发，适用于线程数少、多核 CPU 的场景下

- CAS 是基于乐观锁的思想：最乐观的估计，不怕别的线程来修改共享变量，就算改了也没关系，我吃亏点再重试呗。

- synchronized 是基于悲观锁的思想：最悲观的估计，得防着其它线程来修改共享变量，我上了锁你们都别想改，我改完了解开锁，你们才有机会。

- CAS 体现的是无锁并发、无阻塞并发，请仔细体会这两句话的意思

  - 因为没有使用 synchronized，所以线程不会陷入阻塞，这是效率提升的因素之一

  - 但如果竞争激烈，可以想到重试必然频繁发生，反而效率会受影响

## 原子整数

Atomic 系列类型也可以不用锁保证线程安全

J.U.C 并发包提供了：

- AtomicBoolean

- AtomicInteger

- AtomicLong

以 AtomicInteger 为例

```java
/**
 * get 在前获取的计算前的结果
 * get 在后获取的计算后的结果
 */
AtomicInteger i = new AtomicInteger(0);
// 获取并自增（i = 0, 结果 i = 1, 返回 0），类似于 i++
System.out.println(i.getAndIncrement());
// 自增并获取（i = 1, 结果 i = 2, 返回 2），类似于 ++i
System.out.println(i.incrementAndGet());
// 自减并获取（i = 2, 结果 i = 1, 返回 1），类似于 --i
System.out.println(i.decrementAndGet());
// 获取并自减（i = 1, 结果 i = 0, 返回 1），类似于 i--
System.out.println(i.getAndDecrement());
// 获取并加值（i = 0, 结果 i = 5, 返回 0）
System.out.println(i.getAndAdd(5));
// 加值并获取（i = 5, 结果 i = 0, 返回 0）
System.out.println(i.addAndGet(-5));

// 获取并更新（i = 0, p 为 i 的当前值, 结果 i = -2, 返回 0）
// 其中函数中的操作能保证原子，但函数需要无副作用
// lambda 里面可以做任何操作，例如加减乘除等待。。
System.out.println(i.getAndUpdate(p -> p - 2));
// 更新并获取（i = -2, p 为 i 的当前值, 结果 i = 0, 返回 0）
// 其中函数中的操作能保证原子，但函数需要无副作用
System.out.println(i.updateAndGet(p -> p + 2));

// 获取并计算（i = 0, p 为 i 的当前值, x 为参数1, 结果 i = 10, 返回 0）
// 其中函数中的操作能保证原子，但函数需要无副作用
// getAndUpdate 如果在 lambda 中引用了外部的局部变量，要保证该局部变量是 final 的
// getAndAccumulate 可以通过 参数1 来引用外部的局部变量，但因为其不在 lambda 中因此不必是 final
System.out.println(i.getAndAccumulate(10, (p, x) -> p + x));

// 计算并获取（i = 10, p 为 i 的当前值, x 为参数1, 结果 i = 0, 返回 0）
// 其中函数中的操作能保证原子，但函数需要无副作用
System.out.println(i.accumulateAndGet(-10, (p, x) -> p + x));
```

## 原子引用

- AtomicReference
- AtomicMarkableReference
- AtomicStampedReference

```java
class DecimalAccountSafeCas implements DecimalAccount {
    AtomicReference<BigDecimal> ref;
    public DecimalAccountSafeCas(BigDecimal balance) {
        ref = new AtomicReference<>(balance);
    }
    @Override
    public BigDecimal getBalance() {
        return ref.get();
    }
    @Override
    public void withdraw(BigDecimal amount) {
        while (true) {
            BigDecimal prev = ref.get();
            BigDecimal next = prev.subtract(amount);
            if (ref.compareAndSet(prev, next)) {
                break;
            }
        }
    }
}
```

## ABA 问题及解决

compareAndSet (CAS) 仅能判断出共享变量的值与最初值 A 是否相同，不能感知到这种从 A 改为 B 又 改回 A 的情况

只要有其它线程【动过了】共享变量，那么自己的 cas 就算失败，这时，仅比较值是不够的，需要再加一个版本号

### AtomicStampedReference 

AtomicStampedReference 对象需要提供引用类型，和初始版本号

每次进行更改的时候还需要传入 原始版本号 和 新版本号（这里是原始版本号+1）

```java
@Slf4j(topic = "c.d2_ABA")
public class d2_ABA {
    static AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

    public static void main(String[] args) throws InterruptedException {
        log.debug("main start...");

        String prev = ref.getReference();
        int stamp = ref.getStamp(); // 获取版本号
        log.debug("stamp: {}", stamp);

        other();
        Thread.sleep(1000);
        log.debug("stamp: {}", stamp);
        log.debug("change A->C {}", ref.compareAndSet(prev, "C", stamp, stamp+1));
    }

    private static void other() throws InterruptedException {
        new Thread(()->{
            int stamp = ref.getStamp();
            log.debug("stamp: {}", stamp);
            log.debug("change A->B {}", ref.compareAndSet(ref.getReference(), "B", stamp, stamp + 1));
        }, "t1").start();
        Thread.sleep(500);
        new Thread(()->{
            int stamp = ref.getStamp();
            log.debug("stamp: {}", stamp);
            log.debug("change B->A {}", ref.compareAndSet(ref.getReference(), "A", stamp, stamp + 1));
        }, "t2").start();
    }
}
```

AtomicStampedReference 可以给原子引用加上版本号，追踪原子引用整个的变化过程，如： A -> B -> A ->C ，通过AtomicStampedReference，我们可以知道，引用变量中途被更改了几次。

### AtomicMarkableReference 

但是有时候，并不关心引用变量更改了几次，只是单纯的关心是否更改过，所以就有了 AtomicMarkableReference

初始需要指定初始值，还需要提供标记的类型

```java
// 初始化：
AtomicMarkableReference<data_type> ref = new AtomicMarkableReference<>(init_value, true);
// 更改：需要指定之前的状态和更改状态
ref.compareAndSet(prev_value, next_value, true, false)
```

## 原子数组

- AtomicIntegerArray
- AtomicLongArray
- AtomicReferenceArray

有时候我们不是单纯的想修改一个数，而是修改一个数组里面的值

```java
public class d3_AtomicArray {
    /**
     参数1，提供数组、可以是线程不安全数组或线程安全数组
     参数2，获取数组长度的方法
     参数3，自增方法，回传 array, index
     参数4，打印数组的方法
     */
    // supplier 提供者 无中生有 ()->结果
    // function 函数 一个参数一个结果 (参数)->结果 , BiFunction (参数1,参数2)->结果
    // consumer 消费者 一个参数没结果 (参数)->void, BiConsumer (参数1,参数2)->
    private static <T> void demo(
            Supplier<T> arraySupplier,
            Function<T, Integer> lengthFun,
            BiConsumer<T, Integer> putConsumer,
            Consumer<T> printConsumer ) {
        List<Thread> ts = new ArrayList<>();
        T array = arraySupplier.get();
        int length = lengthFun.apply(array);
        for (int i = 0; i < length; i++) {
            // 每个线程对数组作 10000 次操作
            ts.add(new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    putConsumer.accept(array, j%length);
                }
            }));
        }
        ts.forEach(t -> t.start()); // 启动所有线程
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }); // 等所有线程结束
        printConsumer.accept(array);
    }

    public static void main(String[] args) {
        demo(
                ()->new int[10],
                array-> array.length,
                (array, index) -> array[index]++,
                array-> System.out.println(Arrays.toString(array))
        );

        // 原子数组的使用
        demo(
                () -> new AtomicIntegerArray(10),
                array -> array.length(),
                (array, index) -> array.getAndIncrement(index),
                array-> System.out.println(array)
        );

        // 输出
        /**
         * [6290, 6313, 6296, 6271, 6231, 6298, 6289, 6272, 6295, 6330]
         * [10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000]
         */
    }
}
```

## 字段更新器

- AtomicReferenceFieldUpdater // 域 字段
- AtomicIntegerFieldUpdater
- AtomicLongFieldUpdater

利用字段更新器，可以针对对象的某个域（Field）进行原子操作，只能配合 volatile 修饰的字段使用，否则会出现异常

用于保护类中的成员变量

```java
public class d4_AtomicField {
    public static void main(String[] args) {
        Student student = new Student();
        AtomicReferenceFieldUpdater updater = AtomicReferenceFieldUpdater.newUpdater(Student.class, String.class, "name");

        boolean flag = updater.compareAndSet(student, null, "rainsun");
        System.out.println(flag);
        System.out.println(student);
        // true
	   // Student{name='rainsun'}
    }
}

class Student{
    volatile String name;

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                '}';
    }
}
```

## 原子累加器

```java
public class d5_AtomicAdder {

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            demo(() -> new LongAdder(), adder -> adder.increment());
        }

        for (int i = 0; i < 5; i++) {
            demo(() -> new AtomicLong(), adder -> adder.getAndIncrement());
        }
    }
    private static <T> void demo(Supplier<T> adderSupplier, Consumer<T> action) {
        T adder = adderSupplier.get();
        long start = System.nanoTime();
        List<Thread> ts = new ArrayList<>();
        // 4 个线程，每人累加 50 万
        for (int i = 0; i < 40; i++) {
            ts.add(new Thread(() -> {
                for (int j = 0; j < 500000; j++) {
                    action.accept(adder);
                }
            }));
        }
        ts.forEach(t -> t.start());
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long end = System.nanoTime();
        System.out.println(adder + " cost:" + (end - start)/1000_000);
    }
}
```

比较 AtomicLong 与 LongAdder：LongAdder花费的时间短很多

```java
20000000 cost:65
20000000 cost:14
20000000 cost:15
20000000 cost:36
20000000 cost:15
20000000 cost:373
20000000 cost:356
20000000 cost:295
20000000 cost:390
20000000 cost:404
```

性能提升的原因：**在有竞争时，设置多个累加单元**，Therad-0 累加 Cell[0]，而 Thread-1 累加Cell[1]... 最后将结果汇总。

这样它们在累加时操作的不同的 Cell 变量，因此减少了 CAS 重试失败，从而提高性能。

## LongAdder原理

LongAdder 类有几个关键域:

```java
// 累加单元数组, 懒惰初始化
transient volatile Cell[] cells;
// 基础值, 如果没有竞争, 则用 cas 累加这个域
transient volatile long base;
// 在 cells 创建或扩容时, 置为 1, 表示加锁
transient volatile int cellsBusy;
```

LongAdder 的 Cell 累加单元实现：

```java
// 防止缓存行伪共享
@sun.misc.Contended
static final class Cell {
    volatile long value;
    Cell(long x) { value = x; }
    // 最重要的方法, 用来 cas 方式进行累加, prev 表示旧值, next 表示新值
    final boolean cas(long prev, long next) {
        return UNSAFE.compareAndSwapLong(this, valueOffset, prev, next);
    }
    // 省略不重要代码
}
```

### 缓存行伪共享

因为 CPU 与 内存的速度差异很大，需要靠预读数据至缓存来提升效率。
而缓存以缓存行为单位，每个缓存行对应着一块内存，一般是 64 byte（8 个 long）
缓存的加入会造成数据副本的产生，即同一份数据会缓存在不同核心的缓存行中
CPU 要保证数据的一致性，如果某个 CPU 核心更改了数据，其它 CPU 核心对应的整个缓存行必须失效

![image-20231219100708214](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231219100708214.png)

因为 Cell 是数组形式，在内存中是连续存储的，一个 Cell 为 24 字节（16 字节的对象头和 8 字节的 value），因此缓存行可以存下 2 个的 Cell 对象。这样问题来了：

Core-0 要修改 Cell[0]
Core-1 要修改 Cell[1]

无论谁修改成功，都会导致对方 Core 的缓存行失效，比如 Core-0 中 Cell[0]=6000, Cell[1]=8000 要累加 Cell[0]=6001, Cell[1]=8000 ，这时会让 Core-1 的缓存行失效

![image-20231219100923120](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231219100923120.png)

# 6 并发编程中的不可变设计

## 日期转换的问题

SimpleDateFormat 不是线程安全的，可以用 synchronized 加锁解决问题，但带来的是性能上的损失

**不可变思想**

如果一个对象在不能够修改其内部状态（属性），那么它就是线程安全的，因为不存在并发修改啊！这样的对象在Java 中有很多，例如在 Java 8 后，提供了一个新的日期格式化类：

```java
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
for (int i = 0; i < 10; i++) {
    new Thread(() -> {
        LocalDate date = dtf.parse("2018-10-01", LocalDate::from);
        log.debug("{}", date);
    }).start();
}
```

DateTimeFormatter 的源码：

```java
@implSpec
This class is immutable and thread-safe.
```

不可变对象，实际是另一种避免竞争的方式。

## 不可变设计

另一个大家更为熟悉的 String 类也是不可变的，以它为例，说明一下不可变设计的要素

```java
public final class String // final 修饰 String 表示 没有子类
implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];	// final 修饰，表示 value 表示的char数组的地址是不可更改的
    /** Cache the hash code for the string */
    private int hash; // Default to 0
    // ...
}
```

### final 的使用

发现该类、类中所有属性都是 final 的

- 属性用 final 修饰保证了该属性是只读的，不能修改
- 类用 final 修饰保证了该类中的方法不能被覆盖，防止子类无意间破坏不可变性

### 保护性拷贝

使用字符串时，也有一些跟修改相关的方法啊，比如 substring 等，那么下面就看一看这些方法是如何实现的，就以 substring 为例：

```java
public String substring(int beginIndex) {
    if (beginIndex < 0) {
    	throw new StringIndexOutOfBoundsException(beginIndex);
    }
    int subLen = value.length - beginIndex;
    if (subLen < 0) {
    	throw new StringIndexOutOfBoundsException(subLen);
    }
    return (beginIndex == 0) ? this : new String(value, beginIndex, subLen);
}
```

发现其内部是调用 String 的构造方法创建了一个新字符串，再进入这个构造看看，是否对 final char[] value 做出了修改：

```java
public String(char value[], int offset, int count) {
    if (offset < 0) {
    	throw new StringIndexOutOfBoundsException(offset);
    }
    if (count <= 0) {
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        if (offset <= value.length) {
            this.value = "".value;
            return;
        }
    }
    if (offset > value.length - count) {
    	throw new StringIndexOutOfBoundsException(offset + count);
    }
    this.value = Arrays.copyOfRange(value, offset, offset+count);
}
```

构造新字符串对象时，会生成新的 char[] value，对内容进行复制 。这种通过创建副本对象来避免共享的手段称之为【保护性拷贝（defensive copy）】

### final 的原理

final 变量的赋值也会通过 putfield 指令来完成，同样在这条指令之后也会加入写屏障（防止后面的指令重排序到前面），保证在其它线程读到它的值时不会出现为 0 的情况

```java
public class TestFinal {
	final int a = 20;
}
```

字节码：

```java
0: aload_0
1: invokespecial #1 // Method java/lang/Object."<init>":()V
4: aload_0
5: bipush 20
7: putfield #2 // Field a:I
<-- 写屏障
10: return
```

获取 final 对象时候，会直接从常量池中获取，因为这些已经被缓存在常量池中了，而不是从堆内存中获取较慢开销较大。

# 7 并发编程中的线程池

## 自定义线程池

![image-20231219105650074](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231219105650074.png)

```java
package com.rainsun.d7_thread_pool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic = "c.d1_mypool")
public class d1_mypool {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(
                1,
                1000, TimeUnit.MILLISECONDS,
                1,
                (queue, task) -> {
                    // 1. 一直等待
//                    queue.put(task);
                    // 2. 带超时等待
//                    queue.offer(task, 500, TimeUnit.MILLISECONDS);
                    // 3. 放弃执行
//                    log.debug("放弃 {}", task);
                    // 4. 抛出异常,与放弃执行的区别是后面的任务不会被执行
//                    throw new RuntimeException("任务执行失败" + task);
                    // 5. 调用这自己执行任务
                    task.run();
                });

        for(int i = 0; i < 3; ++ i){
            int j = i;
            threadPool.execute(()->{
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("{}", j);
            });
        }
    }
}

// 拒绝策略
@FunctionalInterface
interface RejectPolicy<T>{
    void reject(BlockQueue<T> queue, T task);
}

@Slf4j(topic = "c.ThreadPool")
class ThreadPool{
    // 任务队列
    private BlockQueue<Runnable> taskQueue;

    // 线程集合
    private HashSet<Worker> workers = new HashSet<>();

    // 核心线程数
    private int coreSize;

    // 获取任务的超时时间
    private long timeout;
    private TimeUnit timeUnit;

    // 拒绝策略
    private RejectPolicy<Runnable> rejectPolicy;

    public ThreadPool(int coreSize, long timeout, TimeUnit timeUnit, int queueCapacity, RejectPolicy<Runnable> rejectPolicy) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.taskQueue = new BlockQueue<>(queueCapacity);
        this.rejectPolicy = rejectPolicy;
    }

    // 执行任务
    public void execute(Runnable task){
        // 任务数没有超过 coreSize 时，直接交给 worker 对象执行
        // 如果任务数超过 coreSize 时，加入任务队列 taskQueue 暂存
        synchronized (workers){
            if(workers.size() < coreSize){
                Worker worker = new Worker(task);
                log.debug("新增 worker{}, task{}", worker, task);
                workers.add(worker);
                worker.start();
            }else{
//                taskQueue.put(task);

                /**
                 * 拒绝策略：
                 * 1. 一直等待
                 * 2. 带超时的等待
                 * 3. 放弃任务执行
                 * 4. 抛出异常
                 * 5. 调用者自己执行任务
                 */
                taskQueue.tryPut(rejectPolicy, task);
            }
        }
    }

    class Worker extends Thread{
        private Runnable task;
        public Worker(Runnable task){
            this.task = task;
        }

        @Override
        public void run(){
            // 执行任务
            // 1. task不为空，执行任务
            // 2. task 为空，则接着从任务队列获取新任务再执行任务
            while(task != null || (task = taskQueue.poll(timeout, timeUnit)) != null){
                try {
                    log.debug("正在执行任务...{}", task);
                    task.run();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    task = null;
                }
            }
            synchronized(workers){
                log.debug("worker 被移除 {}", this);
                workers.remove(this);
            }
        }
    }
}

/* 消费者生产者模型 */
@Slf4j(topic = "c.BlockQueue")
class BlockQueue<T>{
    // 1. 任务队列
    private Deque<T> queue = new ArrayDeque<>();

    // 2. 锁
    private ReentrantLock lock = new ReentrantLock();

    // 3. 生产者条件变量
    private Condition fullWaitSet = lock.newCondition();

    // 4. 消费者条件变量，获取 task 任务
    private Condition emptyWaitSet = lock.newCondition();

    // 5. 容量
    private int capacity;

    public BlockQueue(int capacity) {
        this.capacity = capacity;
    }

    // 带超时的阻塞获取
    public T poll(long timeout, TimeUnit unit){
        lock.lock();
        try {
            // 将 timeout 时间统一转换为 纳秒
            long nanos = unit.toNanos(timeout);
            while(queue.isEmpty()){
                try{
                    if(nanos <= 0){
                        return null;
                    }
                    // 返回剩余等待时间
                    nanos = emptyWaitSet.awaitNanos(nanos);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 获取任务
    public T take(){
        lock.lock();
        try {
            while(queue.isEmpty()){
                try{
                    emptyWaitSet.await();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 添加任务
    public void put(T task){
        lock.lock();
        try {
            while(queue.size() == capacity){
                try{
                    log.debug("等待加入任务队列 {}", task);
                    fullWaitSet.await();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入任务队列 {}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
        }finally {
            lock.unlock();
        }
    }

    // 具有超时时间的阻塞添加
    public boolean offer(T task, long timeout, TimeUnit timeUnit){
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while(queue.size() == capacity){
                try{
                    if(nanos <= 0){
                        return false;
                    }
                    log.debug("等待加入任务队列 {}", task);
                    nanos = fullWaitSet.awaitNanos(nanos);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入任务队列 {}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
            return true;
        }finally {
            lock.unlock();
        }
    }

    public int getSize(){
        lock.lock();;
        try {
            return queue.size();
        }finally {
            lock.unlock();
        }
    }

    // 带有 用户自定义拒绝策略 的任务添加
    public void tryPut(RejectPolicy<T> rejectPolicy, T task){
        lock.lock();;
        try {
            if(queue.size() == capacity){
                rejectPolicy.reject(this, task);
            }else{
                log.debug("加入任务队列 {}", task);
                queue.addLast(task);
                emptyWaitSet.signal();
            }
        }finally {
            lock.unlock();
        }
    }
}
```

## ThreadPoolExecutor

![image-20231219220932088](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231219220932088.png)

### 线程池状态

ThreadPoolExecutor 使用 int 的高 3 位来表示线程池状态，低 29 位表示线程数量

![image-20231219221009144](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231219221009144.png)

从数字上比较，TERMINATED > TIDYING > STOP > SHUTDOWN > RUNNING

这些信息存储在一个原子变量 ctl 中，目的是**将线程池状态与线程个数合二为一，这样就可以用一次 cas 原子操作进行赋值**

```java
// c 为旧值， ctlOf 返回结果为新值
ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))));
// rs 为高 3 位代表线程池状态， wc 为低 29 位代表线程个数，ctl 是合并它们
private static int ctlOf(int rs, int wc) { return rs | wc; }
```

### 构造方法

```java
public ThreadPoolExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler)
```

- corePoolSize 核心线程数目 (最多保留的线程数)
- maximumPoolSize 最大线程数目
- keepAliveTime 生存时间 - 针对救急线程
- unit 时间单位 - 针对救急线程
- workQueue 阻塞队列
- threadFactory 线程工厂 - 可以为线程创建时起个好名字
- handler 拒绝策略

工作方式：

线程池中分核心线程核救急线程，任务首先会交给核心线程运行。

如果核心线程满了，就会在阻塞队列中等待。

如果阻塞队列也满了，就会交给救急线程运行，当救急线程运行完了就会结束，但是核心线程会一直执行，不会结束（即使没有任务）。

当救急线程也满了，新任务就会执行拒绝策略

![image-20231220105756413](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231220105756413.png)

- 线程池中刚开始没有线程，当一个任务提交给线程池后，线程池会创建一个新线程来执行任务。

- 当线程数达到 corePoolSize 并没有线程空闲，这时再加入任务，新加的任务会被加入workQueue 队列排队，直到有空闲的线程。

- 如果队列选择了有界队列，那么任务超过了队列大小时，会创建maximumPoolSize - corePoolSize 数目的线程来救急。

- 如果线程到达 maximumPoolSize 仍然有新任务这时会执行拒绝策略。拒绝策略 jdk 提供了 4 种实现，其它著名框架也提供了实现

  - AbortPolicy 让调用者抛出 RejectedExecutionException 异常，这是默认策略

  - CallerRunsPolicy 让调用者运行任务

  - DiscardPolicy 放弃本次任务

  - DiscardOldestPolicy 放弃队列中最早的任务，本任务取而代之

  - Dubbo 的实现，在抛出 

    RejectedExecutionException 异常之前会记录日志，并 dump 线程栈信息，方便定位问题

    Netty 的实现，是创建一个新线程来执行任务

    ActiveMQ 的实现，带超时等待（60s）尝试放入队列，类似我们之前自定义的拒绝策略
    PinPoint 的实现，它使用了一个拒绝策略链，会逐一尝试策略链中每种拒绝策略

- 当高峰过去后，超过corePoolSize 的救急线程如果一段时间没有任务做，需要结束节省资源，这个时间由keepAliveTime 和 unit 来控制。

![image-20231220110558471](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231220110558471.png)

根据这个构造方法，JDK Executors 类中提供了众多工厂方法来创建各种用途的线程池

### newFixedThreadPool

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>());
}
```

特点

- 核心线程数 == 最大线程数（没有救急线程被创建），因此也无需超时时间
- 阻塞队列是无界的，可以放任意数量的任务
- 评价 适用于任务量已知，相对耗时的任务

### newCachedThreadPool

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                    60L, TimeUnit.SECONDS,
                                    new SynchronousQueue<Runnable>());
}
```

特点

- 核心线程数是 0， 最大线程数是 Integer.MAX_VALUE，救急线程的空闲生存时间是 60s，意味着
  - 全部都是救急线程（60s 后可以回收）
  - 救急线程可以无限创建
- 队列采用了 SynchronousQueue 实现特点是，它没有容量，没有线程来取是放不进去的（一手交钱、一手交货）

```java
SynchronousQueue<Integer> integers = new SynchronousQueue<>();
new Thread(() -> {
    try {
        log.debug("putting {} ", 1);
        integers.put(1);
        log.debug("{} putted...", 1);
        log.debug("putting...{} ", 2);
        integers.put(2);
        log.debug("{} putted...", 2);
    } catch (InterruptedException e) {
    	e.printStackTrace();
    }
},"t1").start();
sleep(1);
new Thread(() -> {
    try {
        log.debug("taking {}", 1);
        integers.take();
    } catch (InterruptedException e) {
    	e.printStackTrace();
    }
},"t2").start();
sleep(1);
new Thread(() -> {
    try {
        log.debug("taking {}", 2);
        integers.take();
    } catch (InterruptedException e) {
    	e.printStackTrace();
    }
},"t3").start();
```

只有 take 了，才能 put 进去：

```java
11:48:15.500 c.TestSynchronousQueue [t1] - putting 1
11:48:16.500 c.TestSynchronousQueue [t2] - taking 1
11:48:16.500 c.TestSynchronousQueue [t1] - 1 putted...
11:48:16.500 c.TestSynchronousQueue [t1] - putting...2
11:48:17.502 c.TestSynchronousQueue [t3] - taking 2
11:48:17.503 c.TestSynchronousQueue [t1] - 2 putted...
```

适用情况：整个线程池表现为线程数会根据任务量不断增长，没有上限，当任务执行完毕，空闲 1分钟后释放线程。 **适合任务数比较密集，但每个任务执行时间较短的情况**

### newSingleThreadExecutor

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
    (new ThreadPoolExecutor(1, 1,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<Runnable>()));
}
```

使用场景：
希望多个任务排队执行。线程数固定为 1，任务数多于 1 时，会放入无界队列排队。任务执行完毕，这唯一的线程也不会被释放。

区别：

- 自己创建一个单线程串行执行任务，如果任务执行失败而终止那么没有任何补救措施，而线程池还会新建一个线程，保证池的正常工作

- Executors.newSingleThreadExecutor() 线程个数始终为1，不能修改

  - FinalizableDelegatedExecutorService 应用的是装饰器模式，只对外暴露了 ExecutorService 接口，因此不能调用 ThreadPoolExecutor 中特有的方法

- Executors.newFixedThreadPool(1) 初始时为1，以后还可以修改
   - 对外暴露的是 ThreadPoolExecutor 对象，可以强转后调用setCorePoolSize 等方法进行修改

### 提交任务

```java
// 执行任务
void execute(Runnable command);

// 提交任务 task，用返回值 Future 获得任务执行结果
<T> Future<T> submit(Callable<T> task);

// 提交 tasks 中所有任务
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;

// 提交 tasks 中所有任务，带超时时间
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
long t imeout, TimeUnit unit)throws InterruptedException;

// 提交 tasks 中所有任务，哪个任务先成功执行完毕，返回此任务执行结果，其它任务取消
<T> T invokeAny(Collection<? extends Callable<T>> tasks)
throws InterruptedException, ExecutionException;

// 提交 tasks 中所有任务，哪个任务先成功执行完毕，返回此任务执行结果，其它任务取消，带超时时间
<T> T invokeAny(Collection<? extends Callable<T>> tasks,
long timeout, TimeUnit unit)
throws InterruptedException, ExecutionException, TimeoutException;
```

### 关闭线程池

**shutdown**

线程池状态变为 SHUTDOWN
- 不会接收新任务
- 但已提交任务会执行完
- 此方法不会阻塞调用线程的执行

```java
void shutdown();
```

```java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        // 修改线程池状态
        advanceRunState(SHUTDOWN);
        // 仅会打断空闲线程
        interruptIdleWorkers();
        onShutdown(); // 扩展点 ScheduledThreadPoolExecutor
    } finally {
    	mainLock.unlock();
    }
    // 尝试终结(没有运行的线程可以立刻终结，如果还有运行的线程也不会等)
    tryTerminate();
}
```

**shutdownNow**

```java
/*
线程池状态变为 STOP
- 不会接收新任务
- 会将队列中的任务返回
- 并用 interrupt 的方式中断正在执行的任务
*/
List<Runnable> shutdownNow();
```

```java
public List<Runnable> shutdownNow() {
    Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        // 修改线程池状态
        advanceRunState(STOP);
        // 打断所有线程
        interruptWorkers();
        // 获取队列中剩余任务
        tasks = drainQueue();
    } finally {
        mainLock.unlock();
    }
    // 尝试终结
    tryTerminate();
    return tasks;
}
```

其他方法：

```java
// 不在 RUNNING 状态的线程池，此方法就返回 true
boolean isShutdown();
// 线程池状态是否是 TERMINATED
boolean isTerminated();
// 调用 shutdown 后，由于调用线程并不会等待所有任务运行结束，因此如果它想在线程池 TERMINATED 后做些事
情，可以利用此方法等待
boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
```

### 任务调度线程池

指定一段时间后运行线程

在『任务调度线程池』功能加入之前，可以使用 java.util.Timer 来实现定时功能，Timer 的优点在于简单易用，但由于所有任务都是由同一个线程来调度，因此所有任务都是串行执行的，同一时间只能有一个任务在执行，前一个任务的延迟或异常都将会影响到之后的任务。

ScheduledExecutorService

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
// 添加两个任务，希望它们都在 1s 后执行
executor.schedule(() -> {
    System.out.println("任务1，执行时间：" + new Date());
    try { Thread.sleep(2000); } catch (InterruptedException e) { }
}, 1000, TimeUnit.MILLISECONDS);
executor.schedule(() -> {
	System.out.println("任务2，执行时间：" + new Date());
}, 1000, TimeUnit.MILLISECONDS);
```

scheduleAtFixedRate 例子：

```java
ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
log.debug("start...");
pool.scheduleAtFixedRate(() -> {
	log.debug("running...");
}, 1, 1, TimeUnit.SECONDS);
```

scheduleAtFixedRate 例子（任务执行时间超过了间隔时间）：

```java
ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
log.debug("start...");
pool.scheduleAtFixedRate(() -> {
    log.debug("running...");
    sleep(2);
}, 1, 1, TimeUnit.SECONDS);
```

输出分析：一开始，延时 1s，接下来，由于任务执行时间 > 间隔时间，间隔被『撑』到了 2s

scheduleWithFixedDelay 例子：(与上面AtFixedRate 是设置的线程结束后的延时时间)

```java
ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
log.debug("start...");
pool.scheduleWithFixedDelay(()-> {
    log.debug("running...");
    sleep(2);
}, 1, 1, TimeUnit.SECONDS);
```

输出分析：一开始，延时 1s，scheduleWithFixedDelay 的间隔是 上一个任务结束 <-> 延时 <-> 下一个任务开始 所以间隔都是 3s

### 正确处理执行任务异常

方法1：主动捉异常(任务自己处理异常)

```java
ExecutorService pool = Executors.newFixedThreadPool(1);
pool.submit(() -> {
    try {
    	log.debug("task1");
    int i = 1 / 0;
    } catch (Exception e) {
    	log.error("error:", e);
    }
});
```

方法2：使用 Future

```java
ExecutorService pool = Executors.newFixedThreadPool(1);
Future<Boolean> f = pool.submit(() -> {
    log.debug("task1");
    int i = 1 / 0;
    return true;
});
log.debug("result:{}", f.get()); // get 中封装了异常信息
```

### Tomcat 线程池

![image-20231220153013638](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231220153013638.png)

- LimitLatch 用来限流，可以控制最大连接个数，类似 J.U.C 中的 Semaphore 
- Acceptor 只负责【接收新的 socket 连接】
- Poller 只负责监听 socket channel 是否有【可读的 I/O 事件】
- 一旦可读，封装一个任务对象（socketProcessor），提交给 Executor 线程池处理
- Executor 线程池中的工作线程最终负责【处理请求】

Tomcat 线程池扩展了 ThreadPoolExecutor，行为稍有不同

- 如果总线程数达到 maximumPoolSize
  - 这时不会立刻抛 RejectedExecutionException 异常
  - 而是再次尝试将任务放入队列，如果还失败，才抛出 RejectedExecutionException 异常

## Fork/Join

概念：

Fork/Join 是 JDK 1.7 加入的新的线程池实现，它体现的是一种分治思想，适用于能够进行任务拆分的 cpu 密集型运算

所谓的任务拆分，是将一个大任务拆分为算法上相同的小任务，直至不能拆分可以直接求解。跟递归相关的一些计算，如归并排序、斐波那契数列、都可以用分治思想进行求解

Fork/Join 在分治的基础上加入了多线程，可以把每个任务的分解和合并交给不同的线程来完成，进一步提升了运算效率

Fork/Join 默认会创建与 cpu 核心数大小相同的线程池

使用：

提交给 Fork/Join 线程池的任务需要继承 RecursiveTask（有返回值）或 RecursiveAction（没有返回值），例如下面定义了一个对 1~n 之间的整数求和的任务

```java
@Slf4j(topic = "c.d2_fork_join")
public class d2_fork_join {
    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool(4);
        System.out.println(pool.invoke(new MyTask((5))));
    }
}

class MyTask extends RecursiveTask<Integer>{
    private int n;

    public MyTask(int n) {
        this.n = n;
    }

    @Override
    protected Integer compute() {
        if(n == 1) return 1;

        MyTask t1 = new MyTask(n-1);
        t1.fork(); // fork 让 pool 中的一个线程执行 t1 任务，也就是执行t1中的compute方法

        int result = n + t1.join(); // join 获取任务结果
        return result;
    }
}
```

![image-20231220155409210](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231220155409210.png)

改进：

task(5)等待task(4)，任务之间具有依赖关系

合理的拆分可以减少依赖关系，增大并行度

```java
@Slf4j(topic = "c.d2_fork_join")
public class d2_fork_join {
    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool(4);
        System.out.println(pool.invoke(new AddTask(0, 5)));
    }
}

class AddTask extends RecursiveTask<Integer>{
    private int begin;
    private int end;

    public AddTask(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        if(begin == end) return begin;
        if(end - begin == 1) return end + begin;

        int mid = (end+begin) / 2;
        AddTask t1 = new AddTask(begin, mid);
        AddTask t2 = new AddTask(mid + 1, end);
        t1.fork();
        t2.fork();
        return t1.join() + t2.join();

    }
}
```

![image-20231220160338091](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231220160338091.png)

# 8 J.U.C

Java 并发工具包

## AQS 原理

AQS：AbstractQueuedSynchronizer（抽象队列同步器），阻塞式锁和相关同步器工具的框架

特点：

- 用 state 属性来表示资源的状态（分独占模式和共享模式），子类需要定义如何维护这个状态，控制如何获取锁和释放锁
  - getState - 获取 state 状态
  - setState - 设置 state 状态
  - compareAndSetState - cas 机制设置 state 状态
- 独占模式是只有一个线程能够访问资源，而共享模式可以允许多个线程访问资源
- 提供了基于 FIFO 的等待队列，类似于 Monitor 的 EntryList
- 条件变量来实现等待、唤醒机制，支持多个条件变量，类似于 Monitor 的 WaitSet

子类主要实现这样一些方法（不重写的话是会默认抛出 UnsupportedOperationException）

- tryAcquire
- tryRelease
- tryAcquireShared
- tryReleaseShared
- isHeldExclusively

获取锁的姿势

```java
// 如果获取锁失败
if (!tryAcquire(arg)) {
	// 入队, 可以选择阻塞当前线程 park unpark
}
```

释放锁的姿势

```java
// 如果释放锁成功
if (tryRelease(arg)) {
	// 让阻塞线程恢复运行
}
```

### 实现不可重入锁

```java
package com.rainsun.d7_thread_pool;

import lombok.extern.slf4j.Slf4j;

import java.net.MulticastSocket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Slf4j(topic = "c.d3_TestAqs")
public class d3_TestAqs {
    public static void main(String[] args) {
        MyLock lock = new MyLock();
        new Thread(()->{
            lock.lock();
            try {
                log.debug("locking...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.debug("unlocking...");
                lock.unlock();
            }
        }, "t1").start();

        new Thread(()->{
            lock.lock();
            try {
                log.debug("locking...");
            } finally {
                log.debug("unlocking...");
                lock.unlock();
            }
        }, "t2").start();
    }
}

// 自定义锁（不可重入锁）
class MyLock implements Lock {
    private MySync sync = new MySync();

    class MySync extends AbstractQueuedSynchronizer{
        @Override // 独占锁
        protected boolean tryAcquire(int arg) {
            if(compareAndSetState(0, 1)){
                // 加上了锁，设置 owner 为当前线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        @Override // 是否持有独占锁
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        public Condition newCondition(){
            return new ConditionObject();
        }
    }

    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}
```

```java
17:19:48 [t1] c.d3_TestAqs - locking...
17:19:49 [t1] c.d3_TestAqs - unlocking...
17:19:49 [t2] c.d3_TestAqs - locking...
17:19:49 [t2] c.d3_TestAqs - unlocking...
```

## ReentrantLock 原理

![image-20231221094204968](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221094204968.png)

ReentrantLock 实现了 Lock 接口，内部有一个 Sync 的类继承自 AbstractQueuedSynchronizer (AQS)，内部还有两个锁实现非公平锁和公平锁都继承了 Sync 类

### 非公平锁实现原理

默认创建非公平锁，当没有竞争时：执行的线程为当前线程

```java
final boolean initialTryLock() {
    Thread current = Thread.currentThread();
    if (compareAndSetState(0, 1)) { // first attempt is unguarded
        setExclusiveOwnerThread(current);
        return true;
    } else if (getExclusiveOwnerThread() == current) {
        int c = getState() + 1;
        if (c < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(c);
        return true;
    } else
        return false;
}
```



![image-20231221095442890](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221095442890.png)

第一个竞争出现时：CAS 修改 state 失败，进入FIFO队列等待

![image-20231221095601204](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221095601204.png)

Thread-1 执行了
1. CAS 尝试将 state 由 0 改为 1，结果失败

   ```java
   @ReservedStackAccess
   final void lock() {
       if (!initialTryLock())
           acquire(1);
   }
   ```

2. 进入 tryAcquire 逻辑，这时 state 已经是1，结果仍然失败

   ```java
   public final void acquire(int arg) {
       if (!tryAcquire(arg))
           acquire(null, arg, false, false, false, 0L);
   }
    protected final boolean tryAcquire(int acquires) {
       if (getState() == 0 && compareAndSetState(0, acquires)) {
           setExclusiveOwnerThread(Thread.currentThread());
           return true;
       }
       return false;
   }
   ```
3. 接下来进入 addWaiter 逻辑，将 Thread 封装为Node，构造 Node 队列

  - 图中黄色三角表示该 Node 的 waitStatus 状态，其中 0 为默认正常状态
  - Node 的创建是懒惰的
  - 其中第一个 Node 称为 Dummy（哑元）或哨兵，用来占位，并不关联线程

![image-20231221095650779](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221095650779.png)

当前线程进入 acquireQueued 逻辑

1. acquireQueued 会在一个死循环中不断尝试获得锁，失败后进入 park 阻塞
2. 如果自己是紧邻着 head（排第二位），那么再次 tryAcquire 尝试获取锁，当然这时 state 仍为 1，失败
3. 进入 shouldParkAfterFailedAcquire 逻辑，将前驱 node，即 head 的 waitStatus 改为 -1，这次返回 false

![image-20231221095936151](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221095936151.png)

4. shouldParkAfterFailedAcquire 执行完毕回到 acquireQueued ，再次 tryAcquire 尝试获取锁，当然这时 state 仍为 1，失败
5. 当再次进入 shouldParkAfterFailedAcquire 时，这时因为其前驱 node 的 waitStatus 已经是 -1，这次返回 true
6. 进入 parkAndCheckInterrupt， Thread-1 park（灰色表示）

![image-20231221102544145](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221102544145.png)

再次有多个线程经历上述过程竞争失败，变成这个样子

![image-20231221102558350](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221102558350.png)

Thread-0 释放锁，进入 tryRelease 流程，如果成功

- 设置 exclusiveOwnerThread 为 null
- state = 0

![image-20231221102621825](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221102621825.png)

当前队列不为 null，并且 head 的 waitStatus = -1，进入 unparkSuccessor 流程
找到队列中离 head 最近的一个 Node（没取消的），unpark 恢复其运行，本例中即为 Thread-1
回到 Thread-1 的 acquireQueued 流程

![image-20231221102655249](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221102655249.png)

如果加锁成功（没有竞争），会设置

- exclusiveOwnerThread 为 Thread-1，state = 1
- head 指向刚刚 Thread-1 所在的 Node，该 Node 清空 Thread
- 原本的 head 因为从链表断开，而可被垃圾回收

如果这时候有其它线程来竞争（非公平的体现），例如这时有 Thread-4 来了

如果不巧又被 Thread-4 占了先

- Thread-4 被设置为 exclusiveOwnerThread，state = 1
- Thread-1 再次进入 acquireQueued 流程，获取锁失败，重新进入 park 阻塞

### 可重入原理

**加锁时，state 加1：**

```java
final boolean initialTryLock() {
    Thread current = Thread.currentThread();
    if (compareAndSetState(0, 1)) { // first attempt is unguarded
        setExclusiveOwnerThread(current);
        return true;
    } else if (getExclusiveOwnerThread() == current) {
        int c = getState() + 1;
        if (c < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(c);
        return true;
    } else
        return false;
}
```

`getExclusiveOwnerThread() == current` 表示当前线程已经获得锁了

这时会 `int c = getState() + 1;` state 的值加1

**解锁时，state 减 1：**

```java
@ReservedStackAccess
protected final boolean tryRelease(int releases) {
    int c = getState() - releases; // state - 1
    if (getExclusiveOwnerThread() != Thread.currentThread())
        throw new IllegalMonitorStateException();
    boolean free = (c == 0); // 只有 state 等于 0 才会 free，释放锁
    if (free)
        setExclusiveOwnerThread(null);
    setState(c);
    return free;
}
```

### 可打断原理

不可打断模式下，即使它被打断，仍会驻留在 AQS 队列中，一直要等到获得锁后方能得知自己被打断了

```java
final int acquire(Node node, int arg, boolean shared,
                  boolean interruptible, boolean timed, long time) {
    Thread current = Thread.currentThread();
    // ...
    for (;;) {
        Node t;
        if ((t = tail) == null) {
            // ...
        } else {
            long nanos;
            spins = postSpins = (byte)((postSpins << 1) | 1);
            if (!timed)
                LockSupport.park(this);
            else if ((nanos = time - System.nanoTime()) > 0L)
                LockSupport.parkNanos(this, nanos);
            else
                break;
            node.clearStatus();
            if ((interrupted |= Thread.interrupted()) && interruptible) // Thread.interrupted()打断了，但是如果interruptible为false 并不会 break 停止，只将 interrupted标记 置为 true 了
                break;
        }
    }
    return cancelAcquire(node, interrupted, interruptible);
}

private int cancelAcquire(Node node, boolean interrupted,
                              boolean interruptible) {
    if (node != null) {
        node.waiter = null;
        node.status = CANCELLED;
        if (node.prev != null)
            cleanQueue();
    }
    if (interrupted) {
        if (interruptible)
            return CANCELLED;
        else
            Thread.currentThread().interrupt();
    }
    return 0;
}
```

### 公平锁实现原理

```java
final boolean initialTryLock() {
    Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        // 队列中没有元素才会竞争
        if (!hasQueuedThreads() && compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    } else if (getExclusiveOwnerThread() == current) {
        if (++c < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(c);
        return true;
    }
    return false;
}

/**
 * Acquires only if thread is first waiter or empty
 */
protected final boolean tryAcquire(int acquires) {
    // hasQueuedPredecessors 会检查当前 AQS 队列是否有前驱节点，没有才会竞争
    if (getState() == 0 && !hasQueuedPredecessors() &&
        compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(Thread.currentThread());
        return true;
    }
    return false;
}
```

## 读写锁

### ReentrantReadWriteLock

当读操作远远高于写操作时，这时候使用 读写锁 让 读-读 可以并发，提高性能。 类似于数据库中的 select ... from ... lock in share mode

提供一个 数据容器类 内部分别使用读锁保护数据的 read() 方法，写锁保护数据的 write() 方法

 ```java
 package com.rainsun.d8_JUC;
 
 import lombok.extern.slf4j.Slf4j;
 
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 @Slf4j(topic = "c.d2_ReadWriteLockTest")
 public class d2_ReadWriteLockTest {
     public static void main(String[] args) throws InterruptedException {
         DataContainer dataContainer = new DataContainer();
         new Thread(()->{
             dataContainer.read();
         },"t1").start();
         Thread.sleep(100);
         new Thread(()->{
             dataContainer.write();
         },"t2").start();
     }
 }
 
 @Slf4j(topic = "c.DataContainer")
 class DataContainer{
     private Object data;
 
     private ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
     private ReentrantReadWriteLock.ReadLock r = rw.readLock();
     private ReentrantReadWriteLock.WriteLock w = rw.writeLock();
 
     public Object read(){
         log.debug("get read lock ...");
         r.lock();
         try {
             log.debug("read");
             Thread.sleep(1000);
             return data;
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         } finally {
             r.unlock();
             log.debug("release read lock...");
         }
     }
 
     public void write(){
         log.debug("get write lock");
         w.lock();
         try {
             log.debug("write");
         }finally {
             log.debug("release write lock");
             w.lock();
         }
 
     }
 }
 ```

```java
11:22:49 [t1] c.DataContainer - get read lock ...
11:22:49 [t1] c.DataContainer - read
11:22:49 [t2] c.DataContainer - get write lock
11:22:50 [t1] c.DataContainer - release read lock...
11:22:50 [t2] c.DataContainer - write
11:22:50 [t2] c.DataContainer - release write lock
```

读完才能写，读读是并发的

注意事项

- 读锁不支持条件变量
- 重入时升级不支持：即持有读锁的情况下去获取写锁，会导致获取写锁永久等待

升级：读锁里包含写锁，则写锁会一直等待读完，但是读在等待写完

```java
r.lock();
try {
    // ...
    w.lock();
    try {
    	// ...
    } finally{
    	w.unlock();
    }
} finally{
	r.unlock();
}
```

- 重入时降级支持：即持有写锁的情况下去获取读锁

写锁后，在内部加上读锁，这样当写锁释放的时候，就获得了读锁。就能保证自己写的数据不会被其他线程获得写锁而篡改（因为我获取了读锁，与其他线程的写锁互斥，其他线程的写锁就不能改变我刚改的值）

```java
class CachedData {
    Object data;
    // 是否有效，如果失效，需要重新计算 data
    volatile boolean cacheValid;
    final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    void processCachedData() {
        rwl.readLock().lock();
        if (!cacheValid) {
            // 获取写锁前必须释放读锁
            rwl.readLock().unlock();
            rwl.writeLock().lock();
            try {
            // 判断是否有其它线程已经获取了写锁、更新了缓存, 避免重复更新
            if (!cacheValid) {
                data = ...
                cacheValid = true;
            }
                // 降级为读锁, 释放写锁时可以获取读锁，这样能够让其它线程读取缓存，但写不进去
                rwl.readLock().lock();
            } finally {
                rwl.writeLock().unlock();
            }
        }
        // 自己用完数据, 释放读锁
        try {
        	use(data);
        } finally {
        	rwl.readLock().unlock();
        }
    }
}
```

### 读写锁原理

读写锁用的是同一个 Sycn 同步器，因此等待队列、state 等也是同一个

不同的是，state 中的低 16 为用于给写锁状态，高 16 用于给读锁状态计数

#### 加锁原理

**（1）t1 w.lock**

t1 成功上锁，流程与 ReentrantLock 加锁相比没有特殊之处，不同是写锁状态占了 state 的低 16 位，而读锁使用的是 state 的高 16 位。

即 state 低 16 位为 1，ownerThread 为当前的 t1 线程

![image-20231221150834559](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221150834559.png)

**（2）t2. r.lock**

t2 执行 r.lock，这时进入读锁的 sync.acquireShared(1) 流程，首先会进入 tryAcquireShared 流程。如果有写锁占据，那么 tryAcquireShared 返回 -1 表示失败

```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        acquire(null, arg, true, false, false, 0L);
}
```

```java
@ReservedStackAccess
protected final int tryAcquireShared(int unused) {
    Thread current = Thread.currentThread();
    int c = getState();
    // 有写锁，且不是当前线程的写锁，那就不可以加读锁
    // 如果有写锁但是当前线程的写锁，那可以加读锁，因为可以锁降级
    if (exclusiveCount(c) != 0 &&
        getExclusiveOwnerThread() != current)
        return -1;
    int r = sharedCount(c);
    if (!readerShouldBlock() &&
        r < MAX_COUNT &&
        compareAndSetState(c, c + SHARED_UNIT)) {
        // ...
        return 1;
    }
    return fullTryAcquireShared(current);
}
```

3）这时会进入 sync.doAcquireShared(1) 流程，首先也是调用 addWaiter 添加节点，不同之处在于节点被设置为 Node.SHARED 模式而非 Node.EXCLUSIVE 模式，注意此时 t2 仍处于活跃状态

![image-20231221153058067](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153058067.png)

4）t2 会看看自己的节点是不是老二，如果是，还会再次调用 tryAcquireShared(1) 来尝试获取锁

5）如果没有成功，在 doAcquireShared 内 for (;;) 循环一次，把前驱节点的 waitStatus 改为 -1，再 for (;;) 循环一次尝试 tryAcquireShared(1) 如果还不成功，那么在 parkAndCheckInterrupt() 处 park

![image-20231221153109887](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153109887.png)

**（6）t3 r.lock，t4 w.lock**

这种状态下，假设又有 t3 加读锁和 t4 加写锁，这期间 t1 仍然持有锁，就变成了下面的样子

![image-20231221153154330](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153154330.png)

#### 解锁原理

t1 w.unlock：

这时会走到写锁的 sync.release(1) 流程，调用 sync.tryRelease(1) 成功，变成下面的样子

![image-20231221153258083](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153258083.png)

接下来执行唤醒流程 sync.unparkSuccessor，即让老二恢复运行，这时 t2 在 doAcquireShared 内 parkAndCheckInterrupt() 处恢复运行

这回再来一次 for (;;) 执行 tryAcquireShared 成功则让读锁计数加一

这时 t2 已经恢复运行，接下来 t2 调用 setHeadAndPropagate(node, 1)，它原本所在节点被置为头节点

![image-20231221153410110](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153410110.png)

事情还没完，在 setHeadAndPropagate 方法内还会检查下一个节点是否是 shared，如果是则调用 doReleaseShared() 将 head 的状态从 -1 改为 0 并唤醒老二，这时 t3 在 doAcquireShared 内parkAndCheckInterrupt() 处恢复运行

![image-20231221153435614](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153435614.png)

这回再来一次 for (;;) 执行 tryAcquireShared 成功则让读锁计数加一

这时 t3 已经恢复运行，接下来 t3 调用 setHeadAndPropagate(node, 1)，它原本所在节点被置为头节点

![image-20231221153516742](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153516742.png)

下一个节点不是 shared 了，因此不会继续唤醒 t4 所在节点

**t2 r.unlock，t3 r.unlock**

t2 进入 sync.releaseShared(1) 中，调用 tryReleaseShared(1) 让计数减一，但由于计数还不为零

![image-20231221153600513](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153600513.png)

t3 进入 sync.releaseShared(1) 中，调用 tryReleaseShared(1) 让计数减一，这回计数为零了，进入 doReleaseShared() 将头节点从 -1 改为 0 并唤醒老二，即

![image-20231221153618984](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153618984.png)

之后 t4 在 acquireQueued 中 parkAndCheckInterrupt 处恢复运行，再次 for (;;) 这次自己是老二，并且没有其他 竞争，tryAcquire(1) 成功，修改头结点，流程结束

![image-20231221153631882](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221153631882.png)

### StampedLock 配合戳使用的读写锁

该类自 JDK 8 加入，是为了进一步优化读性能，它的特点是在使用读锁、写锁时都必须配合【戳】使用

乐观读，StampedLock 支持 tryOptimisticRead() 方法（乐观读），读取完毕后需要做一次 戳校验 如果校验通过，表示这期间确实没有写操作，数据可以安全使用，如果校验没通过，需要重新获取读锁，保证数据安全

提供一个 数据容器类 内部分别使用读锁保护数据的 read() 方法，写锁保护数据的 write() 方法

## Semaphore 信号量

Semaphore 用来限制能同时访问共享资源的线程上限。

```java
@Slf4j(topic = "c.d3_SemaphoreTest")
public class d3_SemaphoreTest {
    public static void main(String[] args) {
        // 1. 创建 semaphore 对象
        Semaphore semaphore = new Semaphore(3);

        // 2. 10 线程同时执行
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                // 3. acquire 获得许可
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    log.debug("running ... ");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    log.debug("end...");
                } finally {
                    // 4. 释放许可
                    semaphore.release();
                }
            }).start();
        }
    }
}
```

### Semaphore 原理

Semaphore 有点像一个停车场，permits 就好像停车位数量，当线程获得了 permits 就像是获得了停车位，然后停车场显示空余车位减一

刚开始，permits（state）为 3，这时 5 个线程来获取资源

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221170806944.png" alt="image-20231221170806944" style="zoom:50%;" />

假设其中 Thread-1，Thread-2，Thread-4 cas 竞争成功，而 Thread-0 和 Thread-3 竞争失败，进入 AQS 队列 park 阻塞

![image-20231221170702951](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221170702951.png)

这时 Thread-4 释放了 permits，接下来 Thread-0 竞争成功，permits 再次设置为 0，设置自己为 head 节点，断开原来的 head 节点，unpark 接下来的 Thread-3 节点，但由于 permits 是 0，因此 Thread-3 在尝试不成功后再次进入 park 状态

![image-20231221170754665](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221170754665.png)

## CountdownLatch 倒计时锁

用来进行线程同步协作，等待所有线程完成倒计时。

其中构造参数用来初始化等待计数值，await() 用来等待计数归零，countDown() 用来让计数减一

```java
@Slf4j(topic = "c.d4_CountDownLatchTest")
public class d4_CountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        new Thread(()->{
            log.debug("begin...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...");
        }).start();
        new Thread(()->{
            log.debug("begin...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...");
        }).start();
        new Thread(()->{
            log.debug("begin...");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...");
        }).start();

        log.debug("waiting...");
        countDownLatch.await();
        log.debug("waiting end...");
    }
}
```

配合线程池使用：

```java
@Slf4j(topic = "c.d4_CountDownLatchTest")
public class d4_CountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(4);
        CountDownLatch countDownLatch = new CountDownLatch(3);

        service.submit(()->{
            log.debug("begin...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...{}", countDownLatch.getCount());
        });

        service.submit(()->{
            log.debug("begin...");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...{}", countDownLatch.getCount());
        });

        service.submit(()->{
            log.debug("begin...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
            log.debug("end...{}", countDownLatch.getCount());
        });
        service.submit(()->{
            log.debug("waiting...");
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("waiting end...");
        });
    }
}
```

```java
16:38:58 [pool-1-thread-4] c.d4_CountDownLatchTest - waiting...
16:38:58 [pool-1-thread-2] c.d4_CountDownLatchTest - begin...
16:38:58 [pool-1-thread-1] c.d4_CountDownLatchTest - begin...
16:38:58 [pool-1-thread-3] c.d4_CountDownLatchTest - begin...
16:38:59 [pool-1-thread-1] c.d4_CountDownLatchTest - end...2
16:38:59 [pool-1-thread-2] c.d4_CountDownLatchTest - end...1
16:39:00 [pool-1-thread-3] c.d4_CountDownLatchTest - end...0
16:39:00 [pool-1-thread-4] c.d4_CountDownLatchTest - waiting end...
```

等待其他线程完成的 demo:

```java
public class d5_CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        String[] allstate = new String[10];
        for(int i = 0; i < 10;  ++ i){
            int finalI = i;
            new Thread(()->{
                for(int load = 0; load <= 100; ++ load){
                    allstate[finalI] = load + "%";
                    System.out.print("\r" + Arrays.toString(allstate));
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("\nGame begin !");
        service.shutdown();
    }
}
```

## CyclicBarrier 同步屏障

循环栅栏，用来进行线程协作，等待线程满足某个计数。构造时设置『计数个数』，每个线程执行到某个需要“同步”的时刻调用 await() 方法进行等待，每等待一个线程计数个数就加1

当等待的线程数满足『计数个数』时，即最后一个线程到达同步点的屏障时，屏障解除，线程继续执行

CyclicBarrier 与 CountDownLatch 的主要区别在于 **CyclicBarrier 是可以重用的** 

CyclicBarrier 可以被比喻为『人满发车』，没坐满，前面已上车的线程就需要继续等待

```java
@Slf4j(topic = "c.d6_CyclicBarrierTest")
public class d6_CyclicBarrierTest {
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2, ()->{
            log.debug("task1 task2 finish...");
        });

        for (int i = 0; i < 3; i++) {
            service.submit(()->{
                log.debug("task1 begin...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    // 初始计数-1
                    barrier.await();
                    log.debug("task1 end...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            service.submit(()->{
                log.debug("task2 begin...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    // 初始计数-1
                    barrier.await();
                    log.debug("task2 end...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        service.shutdown();
    }
}
```

# 9  线程安全集合类（容器）

![image-20231221192635559](C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221192635559.png)

线程安全集合类可以分为三大类：

- 遗留的线程安全集合如 Hashtable ， Vector

- 使用 Collections 装饰的线程安全集合，为原来的普通容器的每个操作进行重写，方法内都加上synchronized关键字 如：

  - Collections.synchronizedCollection

  - Collections.synchronizedList

  - Collections.synchronizedMap

  - Collections.synchronizedSet

  - Collections.synchronizedNavigableMap

  - Collections.synchronizedNavigableSet

  - Collections.synchronizedSortedMap

  - Collections.synchronizedSortedSet

- java.util.concurrent.*

重点介绍

java.util.concurrent.* 下的线程安全集合类，可以发现它们有规律，里面包含三类关键词：Blocking、CopyOnWrite、Concurrent

- Blocking 大部分实现基于锁，并提供用来阻塞的方法
- CopyOnWrite 之类容器修改开销相对较重
- Concurrent 类型的容器
  - 内部很多操作使用 cas 优化，一般可以提供较高吞吐量
  - 弱一致性
    - 遍历时弱一致性，例如，当利用迭代器遍历时，如果容器发生修改，迭代器仍然可以继续进行遍历，这时内容是旧的
    - 求容器大小操作是弱一致性，size 操作未必是 100% 准确
    - 读取弱一致性

> 遍历时如果发生了修改，对于非安全容器来讲，使用 fail-fast 机制也就是让遍历立刻失败，抛出ConcurrentModificationException，不再继续遍历

## ConcurrentHashMap

**JDK 8 ConcurrentHashMap**

```java
// 默认为 0
// 当初始化时, 为 -1
// 当扩容时, 为 -(1 + 扩容线程数)
// 当初始化或扩容完成后，为 下一次的扩容的阈值大小
private transient volatile int sizeCtl;
// 整个 ConcurrentHashMap 就是一个 Node[]
static class Node<K,V> implements Map.Entry<K,V> {}
// hash 表
transient volatile Node<K,V>[] table;
// 扩容时的 新 hash 表
private transient volatile Node<K,V>[] nextTable;
// 扩容时如果某个 bin 迁移完毕, 用 ForwardingNode 作为旧 table bin 的头结点
static final class ForwardingNode<K,V> extends Node<K,V> {}
// 用在 compute 以及 computeIfAbsent 时, 用来占位, 计算完成后替换为普通 Node
static final class ReservationNode<K,V> extends Node<K,V> {}
// 作为 treebin 的头节点, 存储 root 和 first
static final class TreeBin<K,V> extends Node<K,V> {}
// 作为 treebin 的节点, 存储 parent, left, right
static final class TreeNode<K,V> extends Node<K,V> {}
```

**重要方法**

```java
// 获取 Node[] 中第 i 个 Node
static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i)
// cas 修改 Node[] 中第 i 个 Node 的值, c 为旧值, v 为新值
static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i, Node<K,V> c, Node<K,V> v)
// 直接修改 Node[] 中第 i 个 Node 的值, v 为新值
static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v)
```

**构造器分析**

可以看到实现了懒惰初始化，在构造方法中仅仅计算了 table 的大小，以后在第一次使用时才会真正创建

且传入的 initialCapcity 大小并不一定是真实大小，因为 hashtable 要保证长度是2的次方

```java
public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
    if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
    	throw new IllegalArgumentException();
    if (initialCapacity < concurrencyLevel) // Use at least as many bins
    	initialCapacity = concurrencyLevel; // as estimated threads
    long size = (long)(1.0 + (long)initialCapacity / loadFactor);
    // tableSizeFor 仍然是保证计算的大小是 2^n, 即 16,32,64 ...
    int cap = (size >= (long) MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : tableSizeFor((int)size);
    this.sizeCtl = cap;
}
```

**get 流程**

```java
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    // 计算hashcode, spread 方法能确保返回结果是正数
    int h = spread(key.hashCode());
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (e = tabAt(tab, (n - 1) & h)) != null) { // (n-1)&h = h%n 找到数组下标
        // 如果头结点已经是要查找的 key
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
            	return e.val;
        }
        // hash 为负数表示该 bin 在扩容中或是 treebin, 这时调用 find 方法来查找
        else if (eh < 0)
        	return (p = e.find(h, key)) != null ? p.val : null;
        // 正常遍历链表, 用 equals 比较
        while ((e = e.next) != null) {
            if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek))))
            return e.val;
        }
    }
    return null;
}
```

**==put 流程==**

以下数组简称（table），链表简称（bin）

```java
public V put(K key, V value) {
    return putVal(key, value, false);
}

final V putVal(K key, V value, boolean onlyIfAbsent) {
	if (key == null || value == null) throw new NullPointerException();
	// 其中 spread 方法会综合高位低位, 具有更好的 hash 性
	int hash = spread(key.hashCode());
	int binCount = 0;
	for (Node<K,V>[] tab = table;;) {
        // f 是链表头节点
        // fh 是链表头结点的 hash
        // i 是链表在 table 中的下标
        Node<K,V> f; int n, i, fh;
        // 1. 要创建 table
        if (tab == null || (n = tab.length) == 0)
            // 初始化 table 使用了 cas, 无需 synchronized 创建成功, 进入下一轮循环
            tab = initTable();
        // 2. 要创建链表头节点
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            // 添加链表头使用了 cas, 无需 synchronized
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null)))
            	break;
        }
        // 3. 帮忙扩容
    	else if ((fh = f.hash) == MOVED)
            // 帮忙之后, 进入下一轮循环
            tab = helpTransfer(tab, f);
    	else {
            V oldVal = null;
            // 锁住链表头节点
            synchronized (f) {
    			// 再次确认链表头节点没有被移动
                if (tabAt(tab, i) == f) {
                    // 链表
                    if (fh >= 0) {
                        binCount = 1;
                        // 遍历链表
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            // 找到相同的 key
                            if (e.hash == hash && 
                                ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                // 更新
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            // 已经是最后的节点了, 新增 Node, 追加至链表尾
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key, value, null);
                                break;
                            }
                        }
                    }
                    // 红黑树
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        // putTreeVal 会看 key 是否已经在树中, 是, 则返回对应的 TreeNode
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key, value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            // 释放链表头节点的锁
            }
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    // 如果链表长度 >= 树化阈值(8), 进行链表转为红黑树
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    // 增加 size 计数
    addCount(1L, binCount);
    return null;
}
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) {
        if ((sc = sizeCtl) < 0)
        	Thread.yield();
        // 尝试将 sizeCtl 设置为 -1（表示初始化 table）
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            // 获得锁, 创建 table, 这时其它线程会在 while() 循环中 yield 直至 table 创建
            try {
                if ((tab = table) == null || tab.length == 0) {
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    sc = n - (n >>> 2);
                }	
            } finally {
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}

// check 是之前 binCount 的个数
private final void addCount(long x, int check) {
    CounterCell[] as; long b, s;
    if (
        // 已经有了 counterCells, 向 cell 累加
        (as = counterCells) != null ||
        // 还没有, 向 baseCount 累加
        !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)
    ){
        CounterCell a; long v; int m;
        boolean uncontended = true;
        if (
            // 还没有 counterCells
            as == null || (m = as.length - 1) < 0 ||
            // 还没有 cell
            (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
            // cell cas 增加计数失败
            !(uncontended = U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))
        ){
            // 创建累加单元数组和cell, 累加重试
            fullAddCount(x, uncontended);
            return;
        }
        if (check <= 1)
            return;
        // 获取元素个数
        s = sumCount();
    }
    if (check >= 0) {
    	Node<K,V>[] tab, nt; int n, sc;
        while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
        (n = tab.length) < MAXIMUM_CAPACITY) {
            int rs = resizeStamp(n);
            if (sc < 0) {
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                transferIndex <= 0)
                    break;
                // newtable 已经创建了，帮忙扩容
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            // 需要扩容，这时 newtable 未创建
            else if (U.compareAndSwapInt(this, SIZECTL, sc,
            (rs << RESIZE_STAMP_SHIFT) + 2))
                transfer(tab, null);
            s = sumCount();
        }
    }
}
```

### JDK 7 ConcurrentHashMap

它维护了一个 segment 数组，每个 segment 对应一把锁

- 优点：如果多个线程访问不同的 segment，实际是没有冲突的，这与 jdk8 中是类似的
- 缺点：Segments 数组默认大小为16，这个容量初始化指定后就不能改变了，并且不是懒惰初始化

<img src="C:\Users\rainsun\AppData\Roaming\Typora\typora-user-images\image-20231221210726950.png" alt="image-20231221210726950" style="zoom:50%;" />

可以看到 ConcurrentHashMap 没有实现懒惰初始化，空间占用不友好

其中 this.segmentShift 和 this.segmentMask 的作用是决定将 key 的 hash 结果匹配到哪个 segment

例如，根据某一 hash 值求 segment 位置，先将高位向低位移动 this.segmentShift 位















