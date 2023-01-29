package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.rci.SystemEncoderDecoder;
import bgu.spl.net.impl.rci.SystemProtocol;
import bgu.spl.net.srv.TPCServer;

public class TPCMain {
    public static void main(String[] args) {
        TPCServer server = new TPCServer(Integer.decode(args[0]).intValue(),()-> new SystemProtocol(), ()-> new SystemEncoderDecoder());
        server.serve();


    }
}
