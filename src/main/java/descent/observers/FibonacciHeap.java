package descent.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/***********************************************************************
 * File: FibonacciHeap.java
 * Author: Keith Schwarz (htiek@cs.stanford.edu)
 *
 * An implementation of a priority queue backed by a Fibonacci heap,
 * as described by Fredman and Tarjan.  Fibonacci heaps are interesting
 * theoretically because they have asymptotically good runtime guarantees
 * for many operations.  In particular, insert, peek, and decrease-key all
 * run in amortized O(1) time.  dequeueMin and delete each run in amortized
 * O(lg n) time.  This allows algorithms that rely heavily on decrease-key
 * to gain significant performance boosts.  For example, Dijkstra's algorithm
 * for single-source shortest paths can be shown to run in O(m + n lg n) using
 * a Fibonacci heap, compared to O(m lg n) using a standard binary or binomial
 * heap.
 *
 * Internally, a Fibonacci heap is represented as a circular, doubly-linked
 * list of trees obeying the min-heap property.  Each node stores pointers
 * to its parent (if any) and some arbitrary child.  Additionally, every
 * node stores its degree (the number of children it has) and whether it
 * is a "marked" node.  Finally, each Fibonacci heap stores a pointer to
 * the tree with the minimum value.
 *
 * To insert a node into a Fibonacci heap, a singleton tree is created and
 * merged into the rest of the trees.  The merge operation works by simply
 * splicing together the doubly-linked lists of the two trees, then updating
 * the min pointer to be the smaller of the minima of the two heaps.  Peeking
 * at the smallest element can therefore be accomplished by just looking at
 * the min element.  All of these operations complete in O(1) time.
 *
 * The tricky operations are dequeueMin and decreaseKey.  dequeueMin works
 * by removing the root of the tree containing the smallest element, then
 * merging its children with the topmost roots.  Then, the roots are scanned
 * and merged so that there is only one tree of each degree in the root list.
 * This works by maintaining a dynamic array of trees, each initially null,
 * pointing to the roots of trees of each dimension.  The list is then scanned
 * and this array is populated.  Whenever a conflict is discovered, the
 * appropriate trees are merged together until no more conflicts exist.  The
 * resulting trees are then put into the root list.  A clever analysis using
 * the potential method can be used to show that the amortized cost of this
 * operation is O(lg n), see "Introduction to Algorithms, Second Edition" by
 * Cormen, Rivest, Leiserson, and Stein for more details.
 *
 * The other hard operation is decreaseKey, which works as follows.  First, we
 * update the key of the node to be the new value.  If this leaves the node
 * smaller than its parent, we're done.  Otherwise, we cut the node from its
 * parent, add it as a root, and then mark its parent.  If the parent was
 * already marked, we cut that node as well, recursively mark its parent,
 * and continue this process.  This can be shown to run in O(1) amortized time
 * using yet another clever potential function.  Finally, given this function,
 * we can implement delete by decreasing a key to -\infty, then calling
 * dequeueMin to extract it.
 */
public final class FibonacciHeap {

    public static final class Entry {
        public int mDegree = 0;
        public boolean mIsMarked = false;
        public Entry mNext;
        public Entry mPrev;
        public Entry mParent;
        public Entry mChild;
        public DictNode mElem;
        public double mPriority;

        private Entry(DictNode elem, double priority) {
            mNext = mPrev = this;
            mElem = elem;
            mPriority = priority;
        }
    }

    private Entry mMin = null;

    private int mSize = 0;

    public Entry enqueue(DictNode value, double priority) {
        checkPriority(priority);
        Entry result = new Entry(value, priority);
        mMin = mergeLists(mMin, result);
        ++mSize;
        return result;
    }

    public Entry min() {
        if (isEmpty()) {
            throw new NoSuchElementException("qwe");
        }
        return mMin;
    }

    public boolean isEmpty() {
        return mMin == null;
    }

    public int size() {
        return mSize;
    }

    private void checkPriority(double priority) {
        if (Double.isNaN(priority)) {
            throw new IllegalArgumentException("q");
        }
    }

    public void delete(Entry entry) {
        decreaseKeyUnchecked(entry, Double.NEGATIVE_INFINITY);
        dequeueMin();
    }

    public static FibonacciHeap merge(FibonacciHeap one, FibonacciHeap two) {
        FibonacciHeap result = new FibonacciHeap();
        result.mMin = mergeLists(one.mMin, two.mMin);
        result.mSize = one.mSize + two.mSize;
        one.mSize = two.mSize = 0;
        one.mMin = null;
        two.mMin = null;
        return result;
    }

    public Entry dequeueMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("ww");
        }
        --mSize;

        Entry minElement = mMin;

        if (mMin.mNext == mMin) {
            mMin = null;
        } else {
            mMin.mPrev.mNext = mMin.mNext;
            mMin.mNext.mPrev = mMin.mPrev;
            mMin = mMin.mNext;
        }

        if (minElement.mChild != null) {
            Entry curr = minElement.mChild;
            do {
                curr.mParent = null;
                curr = curr.mNext;
            } while (curr != minElement.mChild);
        }

        mMin = mergeLists(mMin, minElement.mChild);

        if (mMin == null) return minElement;

        List<Entry> treeTable = new ArrayList<Entry>();

        List<Entry> toVisit = new ArrayList<Entry>();

        for (Entry curr = mMin; toVisit.isEmpty() || toVisit.get(0) != curr; curr = curr.mNext) {
            toVisit.add(curr);
        }

        for (Entry curr : toVisit) {
            while (true) {
                while (curr.mDegree >= treeTable.size()) {
                    treeTable.add(null);
                }

                if (treeTable.get(curr.mDegree) == null) {
                    treeTable.set(curr.mDegree, curr);
                    break;
                }

                Entry other = treeTable.get(curr.mDegree);
                treeTable.set(curr.mDegree, null);

                Entry min = (other.mPriority < curr.mPriority) ? other : curr;
                Entry max = (other.mPriority < curr.mPriority) ? curr : other;

                max.mNext.mPrev = max.mPrev;
                max.mPrev.mNext = max.mNext;

                max.mNext = max.mPrev = max;
                min.mChild = mergeLists(min.mChild, max);

                max.mParent = min;

                max.mIsMarked = false;

                ++min.mDegree;

                curr = min;
            }

            if (curr.mPriority <= mMin.mPriority) {
                mMin = curr;
            }
        }
        return minElement;
    }


    private void decreaseKeyUnchecked(Entry entry, double priority) {
        entry.mPriority = priority;
        if (entry.mParent != null && entry.mPriority <= entry.mParent.mPriority) {
            cutNode(entry);
        }
        if (entry.mPriority <= mMin.mPriority) {
            mMin = entry;
        }
    }

    private void cutNode(Entry entry) {
        entry.mIsMarked = false;
        if (entry.mParent == null) return;

        if (entry.mNext != entry) {
            entry.mNext.mPrev = entry.mPrev;
            entry.mPrev.mNext = entry.mNext;
        }

        if (entry.mParent.mChild == entry) {
            if (entry.mNext != entry) {
                entry.mParent.mChild = entry.mNext;
            } else {
                entry.mParent.mChild = null;
            }
        }

        --entry.mParent.mDegree;

        entry.mPrev = entry.mNext = entry;
        mMin = mergeLists(mMin, entry);

        if (entry.mParent.mIsMarked) {
            cutNode(entry.mParent);
        } else {
            entry.mParent.mIsMarked = true;
        }

        entry.mParent = null;
    }

    public void decreaseKey(Entry entry, double newPriority) {
        checkPriority(newPriority);
        if (newPriority > entry.mPriority) {
            throw new IllegalArgumentException("");
        }
    }

    private static Entry mergeLists(Entry one, Entry two) {
        if (one == null && two == null) {
            return null;
        } else if (one != null && two == null) {
            return one;
        } else if (one == null && two != null) {
            return two;
        } else {
            Entry oneNext = one.mNext;
            one.mNext = two.mNext;
            one.mNext.mPrev = one;
            two.mNext = oneNext;
            two.mNext.mPrev = two;
            return one.mPriority < two.mPriority ? one : two;
        }
    }

}