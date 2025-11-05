package singleton_pattern.code;

/**
 * This is a good approach but not the best
 * it is called Double-Checked Locking
 */
public class SingletonExample_Good {

    private static SingletonExample_Good instance;

    private SingletonExample_Good() {
    }

    public static SingletonExample_Good getInstance() {
        if (instance == null) {
            synchronized (SingletonExample_Good.class) {
                if (instance == null) {
                    instance = new SingletonExample_Good();
                }
            }
        }
        return instance;
    }

}
