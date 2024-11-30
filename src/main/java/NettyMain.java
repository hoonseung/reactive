import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.EventLoop;

@Slf4j
public class NettyMain {

    public static void main(String[] args) {
        log.info("start main");
        List<EventLoop> eventLoops = List.of(new EventLoop(8080), new EventLoop(8080));
        eventLoops.forEach(EventLoop::run);
        log.info("end main");
    }

}
