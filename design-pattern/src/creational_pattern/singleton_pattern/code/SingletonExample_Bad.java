package creational_pattern.singleton_pattern.code;

/**
 * This class will always create the instance during startup
 * even if we do not need it, so just avoid it
 * unless an eager singleton is required
 */
public class SingletonExample_Bad {

    private static SingletonExample_Bad instance = new SingletonExample_Bad();

    private SingletonExample_Bad() {
    }

    public static SingletonExample_Bad getInstance() {
        return instance;
    }

}
