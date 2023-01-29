#include <iostream>
#include <mutex>
#include <thread>
#include <stdlib.h>
#include <Read.h>
#include <connectionHandler.h>



    Read::Read(ConnectionHandler& handler): _handler(handler){}
    Read::~Read() {}

    void Read::run(){
       while(!_handler.getShouldLOGOUT()){

           std::string answer;
           // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
           // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
           if (!(_handler).getLine(answer)) {
               std::cout << "Disconnected. Exiting...\n" << std::endl;
               break;
           }


           // A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
           // we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
           std::cout << answer << std::endl;
           if (answer == "bye") {
               std::cout << "Exiting...\n" << std::endl;
               break;
           }
        }

    }


