
import java.util.*;

public class MyStack<Item> extends Stack<Item> {
	/**
	 * Initializes an empty stack.
	 */
	public MyStack() {
		super();
	}

	/**
	 * Returns an iterator to this stack that iterates through the items in LIFO order.
	 */
	public Iterator<Item> iterator() {
		return new ReverseIterator<Item>(this);
	}

	@SuppressWarnings("hiding")
	public class ReverseIterator<Item> implements Iterator<Item> {
	    private final List<Item> list;
	    private int position;

	    public ReverseIterator(List<Item> list) {
	        this.list = list;
	        this.position = list.size() - 1;
	    }

	    @Override
	    public boolean hasNext() {
	        return position >= 0;
	    }

	    @Override
	    public Item next() {
	        return list.get(position--);
	    }

	    @Override
	    public void remove() {
	        throw new UnsupportedOperationException();
	    }

	}
}