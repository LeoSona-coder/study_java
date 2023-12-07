# ArrayList

## 成员变量

```java
    /**
     *  默认构造函数创建的ArrayList数组，在首次扩容时会使用到的默认容量。
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 使用有参构造函数创建ArrayList时，如果初始化容量大小为0时，会使用该空数组。
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * 用于默认大小的空实例的共享空数组实例。我们将其与EMPTY_ELEMENTDATA区分开来，以了解添加第一个元素时要膨胀多少。
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 存储 ArrayList 元素的数组缓冲区。
     * ArrayList 的容量是此数组缓冲区的长度。任何空 ArrayList 并且 elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * 将在添加第一个元素时扩展为 DEFAULT_CAPACITY。
     */
    transient Object[] elementData; // non-private to simplify nested class access

    /**
     * ArrayList 的大小（它包含的元素数）。
     * @serial
     */
    private int size;
    /**
     * 该集合的修改次数，指的是集合，而不是元素被增加或者修改删除多少次
     */
    protected transient int modCount = 0;

```
## 构造方法
```java
/**
 * 默认构造方法，将 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 这个空数组赋值给 elementData
 */
public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}

/**
 * 有参构造方法，构造具有指定初始容量的空数组，如果设置初始容量为 0，则将 EMPTY_ELEMENTDATA 赋值给 elementData，与上面的 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 不同。
 */
public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
        this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
        this.elementData = EMPTY_ELEMENTDATA;
    } else {
        throw new IllegalArgumentException("Illegal Capacity: "+initialCapacity);
    }
}

/**
 * 构造一个列表，其中包含指定集合的元素，按集合的迭代器返回这些元素的顺序排列。
 * 形参:
 * c – 要将其元素放入此列表中的集合
 * 抛出:
 * NullPointerException – 如果指定的集合为 null
 */
public ArrayList(Collection<? extends E> c) {
    elementData = c.toArray();
    if ((size = elementData.length) != 0) {
    // defend against c.toArray (incorrectly) not returning Object[]
    // 这里是个bug 使用c.toArray()不返回Object[]，如果传进来的数组是 int[] 返回的就是 int[]，bug出现在JAVA 1.8
    // (see e.g. https://bugs.openjdk.java.net/browse/JDK-6260652)
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, size, Object[].class);
    } else {
    // replace with empty array.
        this.elementData = EMPTY_ELEMENTDATA;
    }
}
```
## add() 及其相关方法
```java
/**
 * 将指定的元素追加到此列表的末尾。
 */
public boolean add(E e) {
    modCount++;
    add(e, elementData, size);
    return true;
}
/**
 * 将指定的元素添加到 elementData的末尾 因为 该方法是从上面的方法中分离出来的 s 
 * 固定为 size也就是list的末尾
 */
private void add(E e, Object[] elementData, int s) {
        /**
         * 如果list的长度s已经等于elementData的长度 
         * 那么肯定数组已经满了 需要扩容
         */
    if (s == elementData.length)
        elementData = grow();
    elementData[s] = e;
    size = s + 1;
}
/**
 *  在此列表中的指定位置插入指定的元素。将当前位于该位置的元素（如果有）和任何后续元素向右移动（在其索引中添加一个）。
 *  形参:
 *  index – 要插入指定元素的索引 element – 要插入的元素
 *  抛出: IndexOutOfBoundsException – 如果索引超出范围 （index < 0 || index > size()）
 */
public void add(int index, E element) {
    rangeCheckForAdd(index);
    modCount++;
    final int s;
    Object[] elementData;
    if ((s = size) == (elementData = this.elementData).length)
        elementData = grow();

        /**
         * 这是一个native方法 
         * 注意复制的时候 不要超出源数组以及目标数组的边界
         * 源数组和目标数组可以是同一个数组，实现数据的后移
         * 形参:
         * src – 源数组。 srcPos – 源数组中的起始位置。 dest – 目标数组。 destPos – 在目标数据中的起始位置。 length – 要复制的数组元素的数量。
         * 抛出:
         * IndexOutOfBoundsException – 如果复制会导致访问数组边界之外的数据。
         * ArrayStoreException – 如果数组中的 src 元素由于类型不匹配而无法存储到数组中 dest 。
         * NullPointerException – 如果 或 src dest 是 null.
         */
    System.arraycopy(elementData, index,elementData, index + 1,s - index);
    elementData[index] = element;
    size = s + 1;
}
```
## addAll()
```java
/**
 * 将指定集合中的所有元素追加到此列表的末尾，其顺序与指定集合的迭代器返回的顺序相同。如果在操作过程中修改了指定的集合，则此操作的行为是未定义的。
 * （这意味着，如果指定的集合是此列表，并且此列表为非空，则此调用的行为是未定义的。
 * 形参:
 * c – 包含要添加到此列表中的元素的集合
 * 返回值: true 如果此列表因调用而更改
 * 抛出: NullPointerException – 如果指定的集合为 null
 */

public boolean addAll(Collection<? extends E> c) {
    Object[] a = c.toArray();
    modCount++;
    int numNew = a.length;
    if (numNew == 0)
        return false;
    Object[] elementData;
    final int s;
    if (numNew > (elementData = this.elementData).length - (s = size))
        elementData = grow(s + numNew);
    System.arraycopy(a, 0, elementData, s, numNew);
    size = s + numNew;
    return true;
}
/**
 * 从指定位置开始，将指定集合中的所有元素插入到此列表中。将当前位于该位置的元素（如果有）和任何后续元素向右移动（增加其索引）。新元素将按照指定集合的迭代器返回的顺序显示在列表中。
 * 形参:
 * index – 从指定集合中插入第一个元素的索引 c – 包含要添加到此列表中的元素的集合
 * 返回值:
 * true 如果此列表因调用而更改
 * 抛出:
 * IndexOutOfBoundsException – 如果索引超出范围 （index < 0 || index > size()）
 * NullPointerException – 如果指定的集合为 null
 */
public boolean addAll(int index, Collection<? extends E> c) {
    rangeCheckForAdd(index);
    Object[] a = c.toArray();
    modCount++;
    int numNew = a.length;
    if (numNew == 0)
        return false;
    Object[] elementData;
    final int s;
    if (numNew > (elementData = this.elementData).length - (s = size))
        elementData = grow(s + numNew);
    int numMoved = s - index;
    if (numMoved > 0)
        System.arraycopy(elementData, index,elementData, index + numNew,numMoved);
    System.arraycopy(a, 0, elementData, index, numNew);
    size = s + numNew;
    return true;
}

/**
 * 
 * 如有必要，增加此 ArrayList 实例的容量，以确保它至少可以容纳最小容量参数指定的元素数。
 * 形参:
 * minCapacity – 所需的最小容量
 */
public void ensureCapacity(int minCapacity) {
    // 如果当前是 数组缓冲区是  DEFAULTCAPACITY_EMPTY_ELEMENTDATA
    // 并且最小容量参数小于 DEFAULT_CAPACITY（10）  则不需要扩容
    if (minCapacity > elementData.length
        && !(elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
             && minCapacity <= DEFAULT_CAPACITY)) {
        modCount++;
        grow(minCapacity);
    }
}
```


## 其他关键方法
```java

/**
 * 非常重要： 这个方法的参数接收的值为最少容量值
 * 也就是当前的添加行为需要保证的数组的最小容量
 */
private Object[] grow(int minCapacity) {
    int oldCapacity = elementData.length;
    //如果原本数组缓冲区中的长度大于0 或者 数组缓冲区 不等于 DEFAULTCAPACITY_EMPTY_ELEMENTDATA
    if (oldCapacity > 0 || elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        // newLength() 方法会返回 最少需要扩容 和  首选扩容1.5倍 较大的那个
        /**
         * 在调用add()方法的情况下：
         * 如果是空数组缓冲区（也就是 EMPTY_ELEMENTDATA 的情况）
         * 那么  minCapacity 就是1 oldCapacity 就是 0，所以newCapacity 就是 1
         * 如果 oldCapacity 大于 0，则会扩容 1.5倍
         */
        
        int newCapacity = ArraysSupport.newLength(
                oldCapacity, /* 之前的容量 */
                minCapacity - oldCapacity, /* 最少需要扩容多少 */
                oldCapacity >> 1  /* 首选扩容1.5倍 */);
        return elementData = Arrays.copyOf(elementData, newCapacity);
    } else {
        /**
         * 在调用add()方法的情况下：
         * 如果是 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 的情况
         * 那么会比较 minCapacity 和 DEFAULT_CAPACITY（10） 选择较大的那个
         * 也就是我们平时使用默认无参构造方法创建 ArrayList的时候，使用add() 方法时，容量会 从 0 变成 10。
         */
        return elementData = new Object[Math.max(DEFAULT_CAPACITY, minCapacity)];
    }
}

/**
 * 在首选扩容长度 和 最少需要扩容长度 选择最大的那个返回
 * 。
 * 如果当前长度和最小增长值之和不超过 MAX_ARRAY_LENGTH，则 MAX_ARRAY_LENGTH 返回。
 * 如果总和没有溢出 int，则 Integer.MAX_VALUE 返回。否则， OutOfMemoryError 将抛出。
 */
public static int newLength(int oldLength, int minGrowth, int prefGrowth) {
    // assert oldLength >= 0
    // assert minGrowth > 0
    int newLength = Math.max(minGrowth, prefGrowth) + oldLength;
    // 如果新长度，不超过 MAX_ARRAY_LENGTH，则返回新长度。
    if (newLength - MAX_ARRAY_LENGTH <= 0) {
        return newLength;
    }
    return hugeLength(oldLength, minGrowth);
}

private static int hugeLength(int oldLength, int minGrowth) {
    // minLength 是扩容后数组最少需要的长度 不然数据无法放入
    int minLength = oldLength + minGrowth;
    if (minLength < 0) { // overflow
        throw new OutOfMemoryError("Required array length too large");
    }
    // 如果这个数组需要的这个最小长度小于 MAX_ARRAY_LENGTH 则数据组的长度返回为 MAX_ARRAY_LENGTH
    if (minLength <= MAX_ARRAY_LENGTH) {
        return MAX_ARRAY_LENGTH;
    }
    // 如果最小需要的长度也大于 MAX_ARRAY_LENGTH 则返回 Integer的 最大值
    return Integer.MAX_VALUE;
}
int indexOfRange(Object o, int start, int end) {
    Object[] es = elementData;
    if (o == null) {
        for (int i = start; i < end; i++) {
            if (es[i] == null) {
                return i;
            }
        }
    } else {
        for (int i = start; i < end; i++) {
            if (o.equals(es[i])) {
                return i;
            }
        }
    }
    return -1;
}



```

