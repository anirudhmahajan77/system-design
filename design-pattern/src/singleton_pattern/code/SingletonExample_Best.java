package singleton_pattern.code;

/**
 * This is the best approach for singleton class
 * and is called Holder Idiom
 */
public class SingletonExample_Best {

    private SingletonExample_Best() {
    }

    private static final class InstanceHolder {
        private static final SingletonExample_Best instance = new SingletonExample_Best();
    }

    public static SingletonExample_Best getInstance() {
        return InstanceHolder.instance;
    }

}
