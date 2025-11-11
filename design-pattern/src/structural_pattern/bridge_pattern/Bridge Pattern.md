# Bridge Pattern - Step by Step

## Step 1: The Problem - Class Explosion

Let's say you're building a **remote control** system for different devices:

```java
// Without Bridge - This gets messy quickly!
class BasicTvRemote {
    public void powerOn() { /* TV specific */ }
    public void volumeUp() { /* TV specific */ }
    public void channelUp() { /* TV specific */ }
}

class AdvancedTvRemote extends BasicTvRemote {
    public void mute() { /* TV specific */ }
    public void openNetflix() { /* TV specific */ }
}

class BasicRadioRemote {
    public void powerOn() { /* Radio specific */ }
    public void volumeUp() { /* Radio specific */ }
    public void changeStation() { /* Radio specific */ }
}

class AdvancedRadioRemote extends BasicRadioRemote {
    public void mute() { /* Radio specific */ }
    public void savePreset() { /* Radio specific */ }
}

// What if we add DVD player? AC? Lights?
// BasicDVDRemote, AdvancedDVDRemote, BasicACRemote, AdvancedACRemote...
// This becomes unmaintainable!
```

## Step 2: Identify the Two Dimensions

**Dimension 1: Remote Types** (Abstraction)
- Basic Remote
- Advanced Remote

**Dimension 2: Devices** (Implementation)
- TV
- Radio
- DVD Player

## Step 3: Separate the Two Concerns

### Step 3.1: Create the Implementation Interface (Device)

```java
// This is what the remote controls
interface Device {
    void powerOn();
    void powerOff();
    void setVolume(int percent);
    int getVolume();
    // Common device operations
}
```

### Step 3.2: Create Concrete Devices

```java
class Tv implements Device {
    private boolean poweredOn = false;
    private int volume = 50;
    
    @Override
    public void powerOn() {
        poweredOn = true;
        System.out.println("TV: Powered ON");
    }
    
    @Override
    public void powerOff() {
        poweredOn = false;
        System.out.println("TV: Powered OFF");
    }
    
    @Override
    public void setVolume(int percent) {
        this.volume = percent;
        System.out.println("TV: Volume set to " + percent + "%");
    }
    
    @Override
    public int getVolume() {
        return volume;
    }
}

class Radio implements Device {
    private boolean poweredOn = false;
    private int volume = 30;
    private String station = "FM 98.5";
    
    @Override
    public void powerOn() {
        poweredOn = true;
        System.out.println("Radio: Powered ON - Tuned to " + station);
    }
    
    @Override
    public void powerOff() {
        poweredOn = false;
        System.out.println("Radio: Powered OFF");
    }
    
    @Override
    public void setVolume(int percent) {
        this.volume = percent;
        System.out.println("Radio: Volume set to " + percent + "%");
    }
    
    @Override
    public int getVolume() {
        return volume;
    }
    
    // Radio-specific method
    public void changeStation(String station) {
        this.station = station;
        System.out.println("Radio: Tuned to " + station);
    }
}
```

### Step 3.3: Create the Abstraction (Remote)

```java
// This is the remote - it uses a device
abstract class Remote {
    protected Device device;  // The BRIDGE - composition over inheritance
    
    public Remote(Device device) {
        this.device = device;
    }
    
    // Basic operations that all remotes have
    public void togglePower() {
        System.out.print("Remote: Toggling power - ");
        // We don't know if device is on/off - let device handle it
        if (device.getVolume() > 0) { // Simple check to see if device is "on"
            device.powerOff();
        } else {
            device.powerOn();
        }
    }
    
    public void volumeUp() {
        int current = device.getVolume();
        device.setVolume(current + 10);
    }
    
    public void volumeDown() {
        int current = device.getVolume();
        device.setVolume(current - 10);
    }
    
    // This is the bridge - we can change devices at runtime
    public void setDevice(Device device) {
        this.device = device;
        System.out.println("Remote: Switched to control " + device.getClass().getSimpleName());
    }
}
```

### Step 3.4: Create Refined Abstractions (Different Remote Types)

```java
class BasicRemote extends Remote {
    public BasicRemote(Device device) {
        super(device);
    }
    
    // Basic remote has only fundamental operations
    // All operations are implemented in base class
}

class AdvancedRemote extends Remote {
    public AdvancedRemote(Device device) {
        super(device);
    }
    
    // Advanced remote has extra features
    public void mute() {
        System.out.println("Advanced Remote: Muting device");
        device.setVolume(0);
    }
    
    public void setVolume(int exactPercent) {
        System.out.println("Advanced Remote: Setting exact volume to " + exactPercent + "%");
        device.setVolume(exactPercent);
    }
}
```

## Step 4: See the Magic - Usage

```java
public class BridgeDemo {
    public static void main(String[] args) {
        System.out.println("=== BRIDGE PATTERN DEMO ===\n");
        
        // Step 1: Create devices
        Device tv = new Tv();
        Device radio = new Radio();
        
        // Step 2: Create remotes that control devices
        Remote basicRemote = new BasicRemote(tv);
        Remote advancedRemote = new AdvancedRemote(radio);
        
        System.out.println("--- Testing Basic Remote with TV ---");
        basicRemote.togglePower();  // Turns TV on
        basicRemote.volumeUp();     // Volume 60%
        basicRemote.volumeUp();     // Volume 70%
        basicRemote.volumeDown();   // Volume 60%
        
        System.out.println("\n--- Testing Advanced Remote with Radio ---");
        advancedRemote.togglePower();  // Turns Radio on  
        advancedRemote.volumeUp();     // Volume 40%
        ((AdvancedRemote) advancedRemote).mute();  // Volume 0%
        ((AdvancedRemote) advancedRemote).setVolume(25);  // Exact volume
        
        System.out.println("\n--- THE BRIDGE: Switching Devices at Runtime ---");
        // Basic remote now controls radio instead of TV
        basicRemote.setDevice(radio);
        basicRemote.volumeUp();  // Radio volume now 35%
        
        // Advanced remote now controls TV instead of radio  
        advancedRemote.setDevice(tv);
        advancedRemote.volumeUp();  // TV volume now 70%
    }
}
```

## Step 5: Output

```
=== BRIDGE PATTERN DEMO ===

--- Testing Basic Remote with TV ---
Remote: Toggling power - TV: Powered ON
TV: Volume set to 60%
TV: Volume set to 70%
TV: Volume set to 60%

--- Testing Advanced Remote with Radio ---
Remote: Toggling power - Radio: Powered ON - Tuned to FM 98.5
Radio: Volume set to 40%
Advanced Remote: Muting device
Radio: Volume set to 0%
Advanced Remote: Setting exact volume to 25%
Radio: Volume set to 25%

--- THE BRIDGE: Switching Devices at Runtime ---
Remote: Switched to control Radio
Radio: Volume set to 35%
Remote: Switched to control Tv
TV: Volume set to 70%
```

## Step 6: Add New Device (Easy Extension)

```java
class DvdPlayer implements Device {
    private boolean poweredOn = false;
    private int volume = 40;
    private boolean playing = false;
    
    @Override
    public void powerOn() {
        poweredOn = true;
        System.out.println("DVD Player: Powered ON");
    }
    
    @Override
    public void powerOff() {
        poweredOn = false;
        playing = false;
        System.out.println("DVD Player: Powered OFF");
    }
    
    @Override
    public void setVolume(int percent) {
        this.volume = percent;
        System.out.println("DVD Player: Volume set to " + percent + "%");
    }
    
    @Override
    public int getVolume() {
        return volume;
    }
    
    public void play() {
        playing = true;
        System.out.println("DVD Player: Playing movie");
    }
    
    public void stop() {
        playing = false;
        System.out.println("DVD Player: Stopped");
    }
}

// Usage with new device - NO CHANGES TO REMOTE CLASSES NEEDED!
public class ExtendedDemo {
    public static void main(String[] args) {
        Device dvdPlayer = new DvdPlayer();
        Remote advancedRemote = new AdvancedRemote(dvdPlayer);
        
        System.out.println("--- Controlling DVD Player with Existing Remote ---");
        advancedRemote.togglePower();  // Powers on DVD
        advancedRemote.volumeUp();     // Volume 50%
        ((AdvancedRemote) advancedRemote).mute();  // Mutes DVD
        
        // We can even create a new remote type without touching devices
        Remote basicRemote = new BasicRemote(dvdPlayer);
        basicRemote.volumeUp();  // Works with DVD player!
    }
}
```

## Step 7: Visualize the Bridge

```
     Abstraction         Implementation
    ┌─────────────┐      ┌─────────────┐
    │   Remote    │─────►│   Device    │
    └─────────────┘      └─────────────┘
           ▲                    ▲
           │                    │
    ┌─────────────┐      ┌─────────────┐
    │BasicRemote  │      │     TV      │
    │AdvancedRemote│     │    Radio    │
    └─────────────┘      │  DvdPlayer  │
                         └─────────────┘

The "bridge" is the composition:
Remote HAS-A Device (instead of Remote IS-A specific device controller)
```

## Key Takeaways

1. **Two Independent Dimensions**: Remotes and Devices can vary independently
2. **Composition over Inheritance**: Remote contains Device instead of extending specific device classes
3. **Runtime Flexibility**: Can switch devices without changing remote code
4. **Easy Extension**: Add new devices or new remote types without affecting the other dimension

## When You'd Actually Use This

- **Payment Systems**: Payment processors (abstraction) × Payment gateways (implementation)
- **UI Frameworks**: UI components (abstraction) × Rendering engines (implementation)
- **Database Layers**: Repository interfaces (abstraction) × Database engines (implementation)
- **Notification Systems**: Notification types (abstraction) × Delivery channels (implementation)

The Bridge pattern says: **"Don't build a hierarchy of TVRemote, RadioRemote, DvdRemote. Instead, build Remote that can control any Device."**
