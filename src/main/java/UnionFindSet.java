import com.google.common.collect.Iterators;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * {@code UnionFindSet} implements a set that
 */
public class UnionFindSet<T> implements Set<T> {

	private final Map<T, UnionFindNode<T>> backingMap;

	/**
	 * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has default initial capacity (16) and load
	 * factor (0.75).
	 */
	public UnionFindSet() {
		this.backingMap = new HashMap<>();
	}

	/**
	 * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has the specified initial capacity and default
	 * load factor (0.75).
	 *
	 * @param initialCapacity the initial capacity of the hash table
	 * @throws IllegalArgumentException if the initial capacity is less than zero
	 */
	public UnionFindSet(int initialCapacity) {
		this.backingMap = new HashMap<>(initialCapacity);
	}

	/**
	 * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has the specified initial capacity and the
	 * specified load factor.
	 *
	 * @param initialCapacity the initial capacity of the hash map
	 * @param loadFactor      the load factor of the hash map
	 * @throws IllegalArgumentException if the initial capacity is less than zero, or if the load factor is
	 *                                  non-positive
	 */
	public UnionFindSet(int initialCapacity, float loadFactor) {
		this.backingMap = new HashMap<>(initialCapacity, loadFactor);
	}

	/**
	 * Constructs a new set containing the elements in the specified collection.  The <tt>HashMap</tt> is created with
	 * default load factor (0.75) and an initial capacity sufficient to contain the elements in the specified
	 * collection.
	 *
	 * @param c the collection whose elements are to be placed into this set
	 * @throws NullPointerException if the specified collection is null
	 */
	public UnionFindSet(Collection<T> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		this.backingMap = c.stream().distinct().collect(toMap(identity(), UnionFindNode::new));
	}

	/**
	 * Determines whether two values are members of the same equivalence class within this set. A value that is not an
	 * element of the set is not considered to be in any equivalence class; passing such a value to this method will
	 * always return {@code false}.
	 *
	 * @param t1 A value
	 * @param t2 A value
	 * @return {@code true} if both arguments are members of the same equivalence class.
	 */
	public boolean areEquivalent(T t1, T t2) {
		if (!backingMap.containsKey(t1) || !backingMap.containsKey(t2)) {
			return false;
		}

		UnionFindNode<T> n1 = backingMap.get(t1);
		UnionFindNode<T> n2 = backingMap.get(t2);
		return n1.isEquivalentTo(n2);
	}

	/**
	 * Merges the equivalence classes of two values. Values passed to this method that are not already elements are
	 * added.
	 *
	 * @param t1 A value to be joined
	 * @param t2 A value to be joined
	 * @return {@code true} if the content or equivalence classes of this set change as a result of this method call.
	 */
	public boolean join(T t1, T t2) {
		UnionFindNode<T> n1;
		if (backingMap.containsKey(t1)) {
			n1 = backingMap.get(t1);
		} else {
			n1 = new UnionFindNode<>(t1);
			backingMap.put(t1, n1);
		}

		UnionFindNode<T> n2;
		if (backingMap.containsKey(t2)) {
			n2 = backingMap.get(t2);
		} else {
			n2 = new UnionFindNode<>(t2);
			backingMap.put(t2, n2);
		}

		return n1.join(n2);
	}

	/**
	 * Merges the equivalence classes of two values, provided that both values are already elements of the set. If one
	 * or both of the arguments are not elements of the set, this method is a no-op.
	 *
	 * @param t1 A value to be joined
	 * @param t2 A value to be joined
	 * @return {@code true} if the equivalence classes of this set change as a result of this method call.
	 */
	public boolean joinIfPresent(T t1, T t2) {
		if (!backingMap.containsKey(t1) || !backingMap.containsKey(t2)) {
			return false;
		}

		UnionFindNode<T> n1 = backingMap.get(t1);
		UnionFindNode<T> n2 = backingMap.get(t2);
		return n1.join(n2);
	}

	/**
	 * Gets an {@link Optional} containing a set of all elements in same the equivalence class as a given value
	 * (including the given value itself). If the value is not an element of this set, the {@code Optional} will be
	 * empty.
	 * <p>
	 * The returned set is not backed by this set; modifying it will not update the underlying equivalence classes, nor
	 * will modifying the equivalence classes update the returned set.
	 *
	 * @param t A value.
	 * @return An {@code Optional} set of all elements in the same equivalence class of {@code t}, provided {@code t} is
	 * an element of this set, or an empty {@code Optional} otherwise.
	 */
	public Optional<Set<T>> getEquivalenceClass(T t) {
		if (!backingMap.containsKey(t)) {
			return Optional.empty();
		}

		UnionFindNode<T> r = backingMap.get(t).getRoot();
		return Optional.of(backingMap.values().stream().filter(t2 -> t2.getRoot() == r).map(UnionFindNode::getValue)
			.collect(toSet()));
	}

	/**
	 * Gets a collection containing sets that represent each of the equivalence classes within this set.
	 * <p>
	 * The returned sets are not backed by this set; modifying them will not update the underlying equivalence classes,
	 * nor will modifying the equivalence classes update the returned sets.
	 *
	 * @return A collection of sets representing the equivalence classes of this set.
	 */
	public Collection<Set<T>> getEquivalenceClasses() {
		return backingMap.values().stream()
			.collect(groupingBy(UnionFindNode::getRoot, mapping(UnionFindNode::getValue, toSet()))).values();
	}

	/**
	 * Returns the number of elements in this set (its cardinality).  If this set contains more than
	 * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of elements in this set (its cardinality)
	 */
	@Override
	public int size() {
		return backingMap.size();
	}

	/**
	 * Returns <tt>true</tt> if this set contains no elements.
	 *
	 * @return <tt>true</tt> if this set contains no elements
	 */
	@Override
	public boolean isEmpty() {
		return backingMap.isEmpty();
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element. More formally, returns <tt>true</tt> if and
	 * only if this set contains an element <tt>e</tt> such that <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 * @param o element whose presence in this set is to be tested
	 * @return <tt>true</tt> if this set contains the specified element
	 * @throws ClassCastException if the type of the specified element is incompatible with this set (<a
	 *                            href="Collection.html#optional-restrictions">optional</a>)
	 */
	@Override
	public boolean contains(Object o) {
		//noinspection SuspiciousMethodCalls
		return backingMap.containsKey(o);
	}

	/**
	 * Returns an iterator over the elements in this set.  The elements are returned in no particular order (unless this
	 * set is an instance of some class that provides a guarantee).
	 *
	 * @return an iterator over the elements in this set
	 */
	@Override
	public Iterator<T> iterator() {
		return Iterators.unmodifiableIterator(backingMap.keySet().iterator());
	}

	/**
	 * Creates a {@code Spliterator} over the elements in this set.
	 * <p>
	 * The {@code Spliterator} reports {@link Spliterator#DISTINCT}.
	 *
	 * @return a {@code Spliterator} over the elements in this set
	 * @implSpec The default implementation creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
	 * spliterator from the set's {@code Iterator}.  The spliterator inherits the <em>fail-fast</em> properties of the
	 * set's iterator.
	 * <p>
	 * The created {@code Spliterator} additionally reports {@link Spliterator#SIZED}.
	 * @implNote The created {@code Spliterator} additionally reports {@link Spliterator#SUBSIZED}.
	 */
	@Override
	public Spliterator<T> spliterator() {
		return backingMap.keySet().spliterator();
	}

	/**
	 * Returns an array containing all of the elements in this set. If this set makes any guarantees as to what order
	 * its elements are returned by its iterator, this method must return the elements in the same order.
	 * <p>
	 * The returned array will be "safe" in that no references to it are maintained by this set.  (In other words, this
	 * method must allocate a new array even if this set is backed by an array). The caller is thus free to modify the
	 * returned array.
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @return an array containing all the elements in this set
	 */
	@Override
	public Object[] toArray() {
		return backingMap.keySet().toArray();
	}

	/**
	 * Returns an array containing all of the elements in this set; the runtime type of the returned array is that of
	 * the specified array. If the set fits in the specified array, it is returned therein. Otherwise, a new array is
	 * allocated with the runtime type of the specified array and the size of this set.
	 * <p>
	 * If this set fits in the specified array with room to spare (i.e., the array has more elements than this set), the
	 * element in the array immediately following the end of the set is set to <tt>null</tt>.  (This is useful in
	 * determining the length of this set <i>only</i> if the caller knows that this set does not contain any null
	 * elements.)
	 * <p>
	 * If this set makes any guarantees as to what order its elements are returned by its iterator, this method must
	 * return the elements in the same order.
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between array-based and collection-based APIs.
	 * Further, this method allows precise control over the runtime type of the output array, and may, under certain
	 * circumstances, be used to save allocation costs.
	 * <p>
	 * Suppose <tt>x</tt> is a set known to contain only strings. The following code can be used to dump the set into a
	 * newly allocated array of <tt>String</tt>:
	 * <p>
	 * <pre>
	 *     String[] y = x.toArray(new String[0]);</pre>
	 *
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to <tt>toArray()</tt>.
	 *
	 * @param a the array into which the elements of this set are to be stored, if it is big enough; otherwise, a new
	 *          array of the same runtime type is allocated for this purpose.
	 * @return an array containing all the elements in this set
	 * @throws ArrayStoreException  if the runtime type of the specified array is not a super type of the runtime type
	 *                              of every element in this set
	 * @throws NullPointerException if the specified array is null
	 */
	@Override
	public <U> U[] toArray(U[] a) {
		//noinspection SuspiciousToArrayCall
		return backingMap.keySet().toArray(a);
	}

	/**
	 * Adds the specified element to this set if it is not already present (optional operation).  More formally, adds
	 * the specified element <tt>e</tt> to this set if the set contains no element <tt>e2</tt> such that
	 * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>. If this set already contains the element, the
	 * call leaves the set unchanged and returns <tt>false</tt>.  In combination with the restriction on constructors,
	 * this ensures that sets never contain duplicate elements.
	 * <p>
	 * The stipulation above does not imply that sets must accept all elements; sets may refuse to add any particular
	 * element, including <tt>null</tt>, and throw an exception, as described in the specification for {@link
	 * Collection#add Collection.add}. Individual set implementations should clearly document any restrictions on the
	 * elements that they may contain.
	 *
	 * @param t element to be added to this set
	 * @return <tt>true</tt> if this set did not already contain the specified element
	 * @throws ClassCastException if the class of the specified element prevents it from being added to this set
	 */
	@Override
	public boolean add(T t) {
		if (backingMap.containsKey(t)) {
			return false;
		}

		backingMap.put(t, new UnionFindNode<>(t));
		return true;
	}

	/**
	 * Throws an <tt>UnsupportedOperationException</tt>
	 */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("UnionFindSet does not support removal");
	}

	/**
	 * Returns <tt>true</tt> if this set contains all of the elements of the specified collection.  If the specified
	 * collection is also a set, this method returns <tt>true</tt> if it is a <i>subset</i> of this set.
	 *
	 * @param c collection to be checked for containment in this set
	 * @return <tt>true</tt> if this set contains all of the elements of the specified collection
	 * @throws ClassCastException   if the types of one or more elements in the specified collection are incompatible
	 *                              with this set (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified collection is null
	 * @see #contains(Object)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return backingMap.keySet().containsAll(c);
	}

	/**
	 * Adds all of the elements in the specified collection to this set if they're not already present (optional
	 * operation).  If the specified collection is also a set, the <tt>addAll</tt> operation effectively modifies this
	 * set so that its value is the <i>union</i> of the two sets.  The behavior of this operation is undefined if the
	 * specified collection is modified while the operation is in progress.
	 *
	 * @param c collection containing elements to be added to this set
	 * @return <tt>true</tt> if this set changed as a result of the call
	 * @throws UnsupportedOperationException if the <tt>addAll</tt> operation is not supported by this set
	 * @throws ClassCastException            if the class of an element of the specified collection prevents it from
	 *                                       being added to this set
	 * @throws NullPointerException          if the specified collection is null
	 * @see #add(Object)
	 */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		if (c == null) {
			throw new NullPointerException();
		}

		boolean isChanged = false;
		for (T t : c) {
			isChanged = add(t) || isChanged;
		}
		return isChanged;
	}

	/**
	 * Throws an <tt>UnsupportedOperationException</tt>
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws an <tt>UnsupportedOperationException</tt>
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws an <tt>UnsupportedOperationException</tt>
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws an <tt>UnsupportedOperationException</tt>
	 */
	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a sequential {@code Stream} with this collection as its source.
	 *
	 * @return a sequential {@code Stream} over the elements in this collection
	 */
	@Override
	public Stream<T> stream() {
		return backingMap.keySet().stream();
	}

	/**
	 * Returns a possibly parallel {@code Stream} with this collection as its source.  It is allowable for this method
	 * to return a sequential stream.
	 * <p>
	 * <p>This method should be overridden when the {@link #spliterator()} method cannot return a spliterator that is
	 * {@code IMMUTABLE}, {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()} for details.)
	 *
	 * @return a possibly parallel {@code Stream} over the elements in this collection
	 */
	@Override
	public Stream<T> parallelStream() {
		return backingMap.keySet().parallelStream();
	}

	/**
	 * Performs the given action for each element of the {@code Iterable} until all elements have been processed or the
	 * action throws an exception.  Unless otherwise specified by the implementing class, actions are performed in the
	 * order of iteration (if an iteration order is specified).  Exceptions thrown by the action are relayed to the
	 * caller.
	 *
	 * @param action The action to be performed for each element
	 * @throws NullPointerException if the specified action is null
	 */
	@Override
	public void forEach(Consumer<? super T> action) {
		backingMap.keySet().forEach(action);
	}

	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hash tables such as those
	 * provided by {@link HashMap}.
	 * <p>
	 * The general contract of {@code hashCode} is: <ul> <li>Whenever it is invoked on the same object more than once
	 * during an execution of a Java application, the {@code hashCode} method must consistently return the same integer,
	 * provided no information used in {@code equals} comparisons on the object is modified. This integer need not
	 * remain consistent from one execution of an application to another execution of the same application. <li>If two
	 * objects are equal according to the {@code equals(Object)} method, then calling the {@code hashCode} method on
	 * each of the two objects must produce the same integer result. <li>It is <em>not</em> required that if two objects
	 * are unequal according to the {@link Object#equals(Object)} method, then calling the {@code hashCode} method on
	 * each of the two objects must produce distinct integer results.  However, the programmer should be aware that
	 * producing distinct integer results for unequal objects may improve the performance of hash tables. </ul>
	 * <p>
	 * As much as is reasonably practical, the hashCode method defined by class {@code Object} does return distinct
	 * integers for distinct objects. (This is typically implemented by converting the internal address of the object
	 * into an integer, but this implementation technique is not required by the Java&trade; programming language.)
	 *
	 * @return a hash code value for this object.
	 * @see Object#equals(Object)
	 * @see System#identityHashCode
	 */
	@Override
	public int hashCode() {
		return backingMap.keySet().stream().collect(summingInt(Objects::hashCode));
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * The {@code equals} method implements an equivalence relation on non-null object references: <ul> <li>It is
	 * <i>reflexive</i>: for any non-null reference value {@code x}, {@code x.equals(x)} should return {@code true}.
	 * <li>It is <i>symmetric</i>: for any non-null reference values {@code x} and {@code y}, {@code x.equals(y)} should
	 * return {@code true} if and only if {@code y.equals(x)} returns {@code true}. <li>It is <i>transitive</i>: for any
	 * non-null reference values {@code x}, {@code y}, and {@code z}, if {@code x.equals(y)} returns {@code true} and
	 * {@code y.equals(z)} returns {@code true}, then {@code x.equals(z)} should return {@code true}. <li>It is
	 * <i>consistent</i>: for any non-null reference values {@code x} and {@code y}, multiple invocations of {@code
	 * x.equals(y)} consistently return {@code true} or consistently return {@code false}, provided no information used
	 * in {@code equals} comparisons on the objects is modified. <li>For any non-null reference value {@code x}, {@code
	 * x.equals(null)} should return {@code false}. </ul>
	 * <p>
	 * The {@code equals} method for class {@code Object} implements the most discriminating possible equivalence
	 * relation on objects; that is, for any non-null reference values {@code x} and {@code y}, this method returns
	 * {@code true} if and only if {@code x} and {@code y} refer to the same object ({@code x == y} has the value {@code
	 * true}).
	 * <p>
	 * Note that it is generally necessary to override the {@code hashCode} method whenever this method is overridden,
	 * so as to maintain the general contract for the {@code hashCode} method, which states that equal objects must have
	 * equal hash codes.
	 *
	 * @param obj the reference object with which to compare.
	 * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
	 * @see #hashCode()
	 * @see HashMap
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (!(obj instanceof Set)) {
			return false;
		}

		Set<?> that = (Set) obj;
		return backingMap.keySet().equals(that);
	}

	/**
	 * @return a string representation of this sett.
	 */
	@Override
	public String toString() {
		return getEquivalenceClasses().toString();
	}

	private static class UnionFindNode<T> {

		private final T value;
		private int rank = 0;
		private UnionFindNode<T> parent = this;

		public UnionFindNode(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		public UnionFindNode<T> getRoot() {
			if (parent != this) {
				parent = parent.getRoot();
			}
			return parent;
		}

		public boolean join(UnionFindNode<T> that) {
			if (that == this) {
				return false;
			}

			UnionFindNode<T> thisRoot = this.getRoot();
			UnionFindNode<T> thatRoot = that.getRoot();
			if (thisRoot == thatRoot) {
				return false;
			}

			if (thisRoot.rank < thatRoot.rank) {
				thisRoot.parent = thatRoot;
			} else if (thisRoot.rank > thatRoot.rank) {
				thatRoot.parent = thisRoot;
			} else {
				thatRoot.parent = thisRoot;
				thisRoot.rank++;
			}
			return true;
		}

		public boolean isEquivalentTo(UnionFindNode<?> that) {
			return this == that || (that != null && Objects.equals(this.getRoot(), that.getRoot()));
		}
	}
}
