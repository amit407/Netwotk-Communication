//
// Created by spl211 on 02/01/2021.
//

#ifndef BOOST_ECHO_CLIENT_TASK_H
#define BOOST_ECHO_CLIENT_TASK_H

#endif //BOOST_ECHO_CLIENT_TASK_H

#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include <connectionHandler.h>

using boost::asio::ip::tcp;

class Read {
private:
    ConnectionHandler& _handler;
public:
    Read(ConnectionHandler& handler);
    virtual ~Read();
    void run();


};