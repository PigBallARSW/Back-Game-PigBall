package co.edu.eci.pigball.game.utility;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class FixedSizeStackLikeQueue<T> {
    private final LinkedBlockingDeque<T> deque;

    public FixedSizeStackLikeQueue(int maxCapacity) {
        this.deque = new LinkedBlockingDeque<>(maxCapacity);
    }

    public synchronized void add(T element) {
        if (deque.size() == deque.remainingCapacity() + deque.size()) {
            deque.pollLast(); // Remove the newest (last) if full
        }
        deque.offerFirst(element); // Add to the front
    }

    public synchronized T remove() {
        return deque.pollFirst(); // Remove from front
    }

    public synchronized T peek() {
        return deque.peekFirst(); // Peek front
    }

    public synchronized int size() {
        return deque.size();
    }

    public synchronized boolean isEmpty() {
        return deque.isEmpty();
    }

    public synchronized boolean reset() {
        deque.clear(); // Clear the queue
        return true;
    }

    public synchronized List<T> getElements() {
        return List.copyOf(deque); // Return a copy of the elements
    }
}
