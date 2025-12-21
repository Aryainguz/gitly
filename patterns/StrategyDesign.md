# Tight and Loose Coupling

---

Tight coupling refers to a scenario where components or classes are highly dependent on each other. Changes in one component often require changes in the other, making the system less flexible and harder to maintain. Loose coupling, on the other hand, minimizes dependencies between components, allowing them to interact through well-defined interfaces. This makes the system more modular, easier to maintain, and adaptable to change.
Example of Tight Coupling

---

In a tightly coupled system, a class directly instantiates another class, leading to strong dependencies.

```java
class Engine {
    void start() {
        System.out.println("Engine started");
    }
}
class Car {
    private Engine engine;

    public Car() {
        this.engine = new Engine(); // Tight coupling
    }

    void drive() {
        engine.start();
        System.out.println("Car is driving");
    }
}
```

In this example, the `Car` class is tightly coupled to the `Engine` class because
it directly creates an instance of `Engine`. Any change in the `Engine` class may require changes in the `Car` class.
Example of Loose Coupling

---

In a loosely coupled system, classes interact through interfaces or abstractions, reducing direct dependencies.

```java
interface Engine {
    void start();
}
class V8Engine implements Engine {
    public void start() {
        System.out.println("V8 Engine started");
    }
}
class Car {
    private Engine engine;
    public Car(Engine engine) {
        this.engine = engine; // Loose coupling
    }
    void drive() {
        engine.start();
        System.out.println("Car is driving");
    }
}
```

In this example, the `Car` class depends on the `Engine` interface rather than a specific implementation. This allows for different types of engines to be used with the `Car` class without modifying it, promoting flexibility and maintainability for the system; e.g., you can easily switch from `V8Engine` to another engine type without changing the `Car` class

```java
Engine myEngine = new V8Engine();
Car myCar = new Car(myEngine);
myCar.drive();
```

## Stragegy Design Pattern is an example of Loose Coupling where different algorithms or strategies can be selected at runtime without changing the context class that uses them.
