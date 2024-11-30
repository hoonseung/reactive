package reactor;


public class ReactorMain {
    public static void main(String[] args) {
        Reactor reactor = new Reactor(8080);
        reactor.run();
    }
}
