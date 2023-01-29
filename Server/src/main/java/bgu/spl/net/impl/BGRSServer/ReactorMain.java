package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.MSG;
import bgu.spl.net.impl.rci.SystemEncoderDecoder;
import bgu.spl.net.impl.rci.SystemProtocol;
import bgu.spl.net.srv.Reactor;

public class ReactorMain {
    public static void main(String[] args) {
        Reactor<MSG> server = new Reactor<>(Runtime.getRuntime().availableProcessors(),Integer.parseInt(args[1]),
                ()-> new SystemProtocol(), ()-> new SystemEncoderDecoder());
        server.serve();
    }
}
