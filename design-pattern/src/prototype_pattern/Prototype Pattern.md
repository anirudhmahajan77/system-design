# Prototype Design Pattern

## Core Concept
The prototype pattern creates new objects by **cloning existing instances** rather than creating new ones from scratch. It's useful when object creation is expensive or when you need similar objects with slight variations.

## Basic Structure

```java
// 1. Prototype interface
public interface Prototype<T> {
    T clone();
}

// 2. Concrete prototype
public class ConcretePrototype implements Prototype<ConcretePrototype> {
    private String field1;
    private int field2;
    private List<String> items;
    
    public ConcretePrototype(String field1, int field2, List<String> items) {
        this.field1 = field1;
        this.field2 = field2;
        this.items = new ArrayList<>(items); // Defensive copy
    }
    
    // Copy constructor
    public ConcretePrototype(ConcretePrototype other) {
        this.field1 = other.field1;
        this.field2 = other.field2;
        this.items = new ArrayList<>(other.items); // Deep copy
    }
    
    @Override
    public ConcretePrototype clone() {
        return new ConcretePrototype(this);
    }
    
    // Getters and setters...
}
```

## Real-World Example: Document Templates

```java
// Prototype interface
public interface Document extends Cloneable {
    Document clone();
    void print();
    void setContent(String content);
    String getType();
}

// Concrete prototypes
public class ReportDocument implements Document {
    private String header;
    private String content;
    private String footer;
    private String author;
    private Date createdDate;
    private List<String> sections;
    
    public ReportDocument(String header, String author) {
        this.header = header;
        this.author = author;
        this.createdDate = new Date();
        this.sections = new ArrayList<>();
        this.footer = "Confidential";
        System.out.println("Expensive operation: Loading report template from database...");
    }
    
    // Copy constructor for cloning
    private ReportDocument(ReportDocument other) {
        this.header = other.header;
        this.content = other.content;
        this.footer = other.footer;
        this.author = other.author;
        this.createdDate = new Date(); // New date for clone
        this.sections = new ArrayList<>(other.sections); // Deep copy
    }
    
    @Override
    public ReportDocument clone() {
        return new ReportDocument(this);
    }
    
    @Override
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public void print() {
        System.out.println("=== " + header + " ===");
        System.out.println("Author: " + author);
        System.out.println("Date: " + createdDate);
        System.out.println("Content: " + content);
        System.out.println("Sections: " + sections);
        System.out.println("Footer: " + footer);
    }
    
    @Override
    public String getType() {
        return "Report";
    }
    
    public void addSection(String section) {
        this.sections.add(section);
    }
}

public class LetterDocument implements Document {
    private String recipient;
    private String sender;
    private String content;
    private String subject;
    private Date date;
    
    public LetterDocument(String sender) {
        this.sender = sender;
        this.date = new Date();
        System.out.println("Expensive operation: Loading letter template...");
    }
    
    private LetterDocument(LetterDocument other) {
        this.recipient = other.recipient;
        this.sender = other.sender;
        this.content = other.content;
        this.subject = other.subject;
        this.date = new Date();
    }
    
    @Override
    public LetterDocument clone() {
        return new LetterDocument(this);
    }
    
    @Override
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public void print() {
        System.out.println("To: " + recipient);
        System.out.println("From: " + sender);
        System.out.println("Subject: " + subject);
        System.out.println("Date: " + date);
        System.out.println("Content: " + content);
    }
    
    @Override
    public String getType() {
        return "Letter";
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
```

## Prototype Registry

```java
// Manages prototype instances
public class DocumentRegistry {
    private static Map<String, Document> prototypes = new HashMap<>();
    
    static {
        // Pre-create expensive prototype objects
        prototypes.put("monthlyReport", new ReportDocument("Monthly Report", "System"));
        prototypes.put("weeklyReport", new ReportDocument("Weekly Report", "System"));
        prototypes.put("businessLetter", new LetterDocument("Company Inc."));
        prototypes.put("personalLetter", new LetterDocument(""));
    }
    
    public static Document getPrototype(String type) {
        Document prototype = prototypes.get(type);
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown document type: " + type);
        }
        return prototype.clone();
    }
    
    public static void registerPrototype(String key, Document prototype) {
        prototypes.put(key, prototype);
    }
}
```

## Usage Examples

```java
public class PrototypeDemo {
    public static void main(String[] args) {
        System.out.println("=== Without Prototype (Expensive Creation) ===");
        Document report1 = new ReportDocument("Sales Report", "John Doe");
        report1.setContent("Q1 Sales Data...");
        
        Document report2 = new ReportDocument("Sales Report", "John Doe");
        report2.setContent("Q2 Sales Data...");
        // ❌ Expensive constructor called twice
        
        System.out.println("\n=== With Prototype (Efficient Cloning) ===");
        Document template = DocumentRegistry.getPrototype("monthlyReport");
        template.setContent("January Sales: $1M");
        
        Document febReport = DocumentRegistry.getPrototype("monthlyReport");
        febReport.setContent("February Sales: $1.2M");
        // ✅ Only expensive constructor called once during registry setup
        
        Document marchReport = DocumentRegistry.getPrototype("monthlyReport");
        marchReport.setContent("March Sales: $1.5M");
        
        System.out.println("\n=== Different Document Types ===");
        Document businessLetter = DocumentRegistry.getPrototype("businessLetter");
        ((LetterDocument) businessLetter).setRecipient("Client Corp");
        ((LetterDocument) businessLetter).setSubject("Partnership Proposal");
        businessLetter.setContent("We would like to propose...");
        
        Document personalLetter = DocumentRegistry.getPrototype("personalLetter");
        ((LetterDocument) personalLetter).setRecipient("Friend");
        ((LetterDocument) personalLetter).setSubject("Catching Up");
        personalLetter.setContent("Long time no see...");
        
        // Print all documents
        template.print();
        febReport.print();
        marchReport.print();
        businessLetter.print();
        personalLetter.print();
    }
}
```

## Advanced Example: Game Characters with Deep Copy

```java
public class GameCharacter implements Prototype<GameCharacter> {
    private String name;
    private int level;
    private Position position;
    private List<Item> inventory;
    private Stats stats;
    
    public GameCharacter(String name, int level, Position position) {
        this.name = name;
        this.level = level;
        this.position = position;
        this.inventory = new ArrayList<>();
        this.stats = new Stats(100, 50, 25); // Default stats
        System.out.println("Expensive: Loading character assets...");
    }
    
    // Deep copy constructor
    private GameCharacter(GameCharacter other) {
        this.name = other.name;
        this.level = other.level;
        this.position = new Position(other.position); // Deep copy
        this.inventory = new ArrayList<>();
        for (Item item : other.inventory) {
            this.inventory.add(item.clone()); // Deep copy each item
        }
        this.stats = other.stats.clone(); // Deep copy
    }
    
    @Override
    public GameCharacter clone() {
        return new GameCharacter(this);
    }
    
    public void addItem(Item item) {
        inventory.add(item);
    }
    
    public void setPosition(int x, int y) {
        this.position = new Position(x, y);
    }
    
    // Getters and setters...
}

// Supporting classes (also implement Prototype)
public class Position implements Prototype<Position> {
    private int x;
    private int y;
    
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public Position(Position other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    @Override
    public Position clone() {
        return new Position(this);
    }
}

public class Stats implements Prototype<Stats> {
    private int health;
    private int mana;
    private int stamina;
    
    public Stats(int health, int mana, int stamina) {
        this.health = health;
        this.mana = mana;
        this.stamina = stamina;
    }
    
    public Stats(Stats other) {
        this.health = other.health;
        this.mana = other.mana;
        this.stamina = other.stamina;
    }
    
    @Override
    public Stats clone() {
        return new Stats(this);
    }
}

public class Item implements Prototype<Item> {
    private String name;
    private int value;
    
    public Item(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    public Item(Item other) {
        this.name = other.name;
        this.value = other.value;
    }
    
    @Override
    public Item clone() {
        return new Item(this);
    }
}
```

## Using Java's Built-in Cloneable

```java
// Alternative approach using Java's Cloneable (not recommended)
public class ShallowPrototype implements Cloneable {
    private String name;
    private List<String> items;
    
    public ShallowPrototype(String name, List<String> items) {
        this.name = name;
        this.items = items;
    }
    
    @Override
    protected ShallowPrototype clone() {
        try {
            ShallowPrototype clone = (ShallowPrototype) super.clone();
            // ❌ Warning: items list is shared between original and clone!
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }
    
    // Better: deep clone
    public ShallowPrototype deepClone() {
        try {
            ShallowPrototype clone = (ShallowPrototype) super.clone();
            clone.items = new ArrayList<>(this.items); // Deep copy
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

## Prototype with Configuration

```java
public class UIComponent implements Prototype<UIComponent> {
    private String type;
    private int width;
    private int height;
    private String color;
    private String text;
    private Map<String, String> styles;
    
    public UIComponent(String type, int width, int height) {
        this.type = type;
        this.width = width;
        this.height = height;
        this.styles = new HashMap<>();
        System.out.println("Loading component assets for: " + type);
    }
    
    private UIComponent(UIComponent other) {
        this.type = other.type;
        this.width = other.width;
        this.height = other.height;
        this.color = other.color;
        this.text = other.text;
        this.styles = new HashMap<>(other.styles); // Deep copy
    }
    
    @Override
    public UIComponent clone() {
        return new UIComponent(this);
    }
    
    public UIComponent withColor(String color) {
        this.color = color;
        return this;
    }
    
    public UIComponent withText(String text) {
        this.text = text;
        return this;
    }
    
    public UIComponent withStyle(String property, String value) {
        this.styles.put(property, value);
        return this;
    }
    
    public void render() {
        System.out.printf("Rendering %s [%dx%d] color: %s text: %s styles: %s%n",
            type, width, height, color, text, styles);
    }
}

// Usage
public class UIPrototypeDemo {
    public static void main(String[] args) {
        // Create button prototype
        UIComponent buttonPrototype = new UIComponent("Button", 100, 40)
            .withColor("blue")
            .withStyle("border", "1px solid")
            .withStyle("border-radius", "5px");
        
        // Clone and customize
        UIComponent primaryButton = buttonPrototype.clone()
            .withText("Submit")
            .withColor("green");
            
        UIComponent secondaryButton = buttonPrototype.clone()
            .withText("Cancel")
            .withColor("gray");
            
        UIComponent dangerButton = buttonPrototype.clone()
            .withText("Delete")
            .withColor("red")
            .withStyle("font-weight", "bold");
        
        primaryButton.render();
        secondaryButton.render();
        dangerButton.render();
    }
}
```

## When to Use Prototype Pattern

### ✅ Use When:
- **Object creation is expensive** (database calls, file I/O, network requests)
- **You need similar objects with slight variations**
- **Avoiding subclass explosion** for minor variations
- **Runtime object configuration** is complex
- **System should be independent of how objects are created**

### ❌ Avoid When:
- **Objects are simple** and cheap to create
- **Subclasses are few** and well-defined
- **Deep copying is too complex** or impossible
- **Performance overhead** of cloning is significant

## Key Benefits

1. **Performance**: Avoids expensive initialization
2. **Flexibility**: Runtime object configuration
3. **Reduced Subclasses**: Avoids class explosion for minor variations
4. **Dynamic Object Creation**: Objects can be created based on runtime state

## Implementation Notes

- **Prefer copy constructors** over `Cloneable` interface
- **Always implement deep copy** for mutable fields
- **Use prototype registry** for managing common prototypes
- **Consider object identity** vs object equality in your use case

The prototype pattern is essentially about **object templating** - create once, customize many times. It's particularly valuable in scenarios where object initialization is resource-intensive or when you need to create many similar objects with minor differences.
