package assignment;

public class Server implements Hello {
    public String sayHello() {
        System.out.println("Called sayHello()");
        return "Hello, world!";
    }
}
