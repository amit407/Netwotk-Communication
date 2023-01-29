package bgu.spl.net.srv;

import bgu.spl.net.MSG;
import bgu.spl.net.impl.rci.SystemEncoderDecoder;
import bgu.spl.net.impl.rci.SystemProtocol;

import java.util.function.Supplier;

public class TPCServer extends BaseServer<MSG> {

    public TPCServer(int port, Supplier protocolFactory, Supplier encdecFactory) {
        super(port, protocolFactory, encdecFactory);
    }

    @Override
    protected void execute(BlockingConnectionHandler handler) {
        new Thread(handler).start();
    }
}
