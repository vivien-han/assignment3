package part1;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

   AtomicInteger count = new AtomicInteger();

   public void increase() {
    count.incrementAndGet();
  }

   public int getValue() {
    return this.count.get();
  }




}
