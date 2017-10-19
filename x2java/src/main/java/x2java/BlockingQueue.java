// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2java;

import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

public class BlockingQueue<T> {
    private Queue<T> queue;
    private boolean closing;

    public BlockingQueue() {
        queue = new LinkedList<T>();
    }

    public void close() {
        close(null);
    }

    public void close(T finalItem) {
        synchronized (queue) {
            if (finalItem != null) {
                queue.offer(finalItem);
            }
            closing = true;
            queue.notifyAll();
        }
    }

    public T dequeue() {
        synchronized (queue) {
            while (queue.size() == 0) {
                if (closing) {
                    return null;
                }
                try {
                    queue.wait();
                }
                catch (InterruptedException ie) {
                    return null;
                }
            }
            return queue.poll();
        }
    }

    public int dequeue(List<T> values) {
        synchronized (queue) {
            while (queue.size() == 0) {
                if (closing) {
                    return 0;
                }
                try {
                    queue.wait();
                }
                catch (InterruptedException ie) {
                    return 0;
                }
            }
            int n = 0;
            while (queue.size() != 0) {
                values.add(queue.poll());
                ++n;
            }
            return n;
        }
    }

    public void enqueue(T item) {
        synchronized (queue) {
            if (!closing) {
                queue.offer(item);
                if (queue.size() == 1) {
                    queue.notify();
                }
            }
        }
    }

    public T tryDequeue() {
        synchronized (queue) {
            if (queue.size() == 0) {
                return null;
            }
            return queue.poll();
        }
    }

    public int tryDequeue(List<T> values) {
        synchronized (queue) {
            if (queue.size() == 0) {
                return 0;
            }
            int n = 0;
            while (queue.size() != 0) {
                values.add(queue.poll());
                ++n;
            }
            return n;
        }
    }
}
