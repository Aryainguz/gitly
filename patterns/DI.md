# Dependemcy Injection (DI) Pattern

Dependency Injection (DI) is a design pattern used to implement Inversion of Control (IoC) where the control of creating and managing dependencies is transferred from the dependent object to an external entity. This pattern promotes loose coupling between classes and enhances testability and maintainability of the code.

## Key Concepts

- **Dependency**: An object that another object relies on to function.
- **Injection**: The process of providing the dependent object with its dependencies from an external source rather than creating them internally.

## Types of Dependency Injection

1.  **Constructor Injection**: Dependencies are provided through a class constructor.

    ```java
    class Service {
        void execute() {
            System.out.println("Service executed");
        }
    }
    class Client {
        private Service service;

        public Client(Service service) {
            this.service = service; // Dependency is injected via constructor
        }

        void doWork() {
            service.execute();
        }
    }
    ```

    2. **Setter Injection**: Dependencies are provided through setter methods.

       ```java
       class Service {
           void execute() {
               System.out.println("Service executed");
           }
       }
       class Client {
           private Service service;
           public void setService(Service service) {
               this.service = service; // Dependency is injected via setter
           }
           void doWork() {
               service.execute();
           }
       }
       ```

    3. **Interface Injection**: The dependency provides an injector method that will inject the dependency into any client passed to it.

       ```java
       interface Service {
           void execute();
       }
       class ServiceImpl implements Service {
           public void execute() {
               System.out.println("Service executed");
           }
       }
       interface Client {
           void setService(Service service);
           void doWork();
       }
       class ClientImpl implements Client {
           private Service service;
           public void setService(Service service) {
               this.service = service; // Dependency is injected via interface method
           }
           public void doWork() {
               service.execute();
           }
       }
       ```

## Benefits of Dependency Injection

- **Loose Coupling**: Reduces dependencies between classes, making the system more modular.
- **Improved Testability**: Makes it easier to test classes in isolation by injecting mock dependencies.
- **Flexibility**: Allows for easy swapping of implementations without changing the dependent class.
- **Maintainability**: Simplifies code maintenance by centralizing the management of dependencies
