#include <stdlib.h>
#include <connectionHandler.h>
#include <thread>
#include <Read.h>


/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    Read task(connectionHandler);
    std::thread Reader(&Read::run, &task);


    //From here we will see the rest of the ehco client implementation:
    int count = 0;
    while(!connectionHandler.getShouldLOGOUT()) {
        count++;
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);





        if (!connectionHandler.sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        if(line == "LOGOUT"){
            std::unique_lock<std::mutex> lock(connectionHandler.getLock());
            connectionHandler.getCon().wait(lock);
        }
        // connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.



        // We can use one of three options to read data from the server:
        // 1. Read a fixed number of characters
        // 2. Read a line (up to the newline character using the getline() buffered reader
        // 3. Read up to the null character


    }
    Reader.join();

    return 0;
}

