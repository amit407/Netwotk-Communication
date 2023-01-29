#include <connectionHandler.h>
 
using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
using std::map;
 
ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_),
opcodeMap(initMap()), shouldLOGOUT(), mtx(), con() {}
    
ConnectionHandler::~ConnectionHandler() {
    close();
}
 
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error) {
            throw boost::system::system_error(error);
        }
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error) {
            throw boost::system::system_error(error);
        }
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        delete[] bytes;
        return false;
    }
    delete[] bytes;
    return true;
}
 
bool ConnectionHandler::getLine(std::string& line) {
    char *input = new char[4];
    if (!getBytes(input, 4)) {
         delete[] input;
         return false;
    }
    short opcode = bytesToShort(input);
    short msgOpcode = bytesToShort(input + 2);

    std:: string msgOpcodeString = std::to_string(msgOpcode);
    std:: string type = "ACK ";
    if((opcode == 12) && ((msgOpcode == 6) | (msgOpcode == 7) | (msgOpcode == 8) | (msgOpcode == 9) | (msgOpcode == 11))) {
        if (getFrameAscii(line, '\0')) {
            line = type + msgOpcodeString + line;
            delete[] input;
            return true;
        }
        else{
            delete[] input;
            return false;
        }
    }
    else if(opcode == 13)
        type = "ERROR ";
    line = type + msgOpcodeString;
    if((opcode == 12) & (msgOpcode == 4)) {
        shouldLOGOUT = true;
        std::unique_lock<std::mutex> lock(mtx);
        con.notify_all();

    }
    delete[] input;
    return true;
}

bool ConnectionHandler::sendLine(std::string& line) {

        int outputSize = 0;

        std::vector<std::string> strs = std::vector<std::string>();
        boost::split(strs, line, boost::is_any_of(" "));
        short opcode = opcodeMap.at(strs[0]);
        if ((opcode == 4) | (opcode == 11)) {
            char *output  = new char[2];
            outputSize = 2;
            shortToBytes(opcode,output);
            return sendBytes(output,outputSize);
        }
        else if ((opcode == 5) | (opcode == 6) | (opcode == 7) | (opcode == 9) | (opcode == 10)) {
            char* output = new char[4];
            outputSize = 4;
            shortToBytes(opcode,output);
            std::istringstream iss(strs[1]);
            short courseNum;
            iss >> courseNum;
            shortToBytes(courseNum,output + 2);
            return sendBytes(output,outputSize);
        }
        else if(opcode == 8){
            int numOfBytesUsername = strs[1].size() + 1;
            outputSize = 2 + numOfBytesUsername;
           const char* outputUsername = strs[1].c_str();
           char* output = new char[outputSize];
           shortToBytes(opcode,output);
           for(int i = 2; i < outputSize; i++){
               output[i] = outputUsername[i-2];
           }
            return sendBytes(output,outputSize);
        }
        else {
            int numOfBytesUsername = strs[1].size() + 1;
            int numOfBytesPassword = strs[2].size() + 1;
            outputSize = numOfBytesUsername + numOfBytesPassword + 2;
            const char* outputUsername = strs[1].c_str();
            const char* outputPassword = strs[2].c_str();
            char* output = new char[outputSize];
            shortToBytes(opcode,output);
            for(int i = 2; i < numOfBytesUsername + 2; i++){
                output[i] = outputUsername[i-2];
            }
            for(int i = numOfBytesUsername + 2; i < outputSize; i++){
                output[i] = outputPassword[i - (numOfBytesUsername + 2)];
            }
            return sendBytes(output,outputSize);
        }
    }




    bool ConnectionHandler::getFrameAscii(std::string &frame, char delimiter) {
        char ch;
        // Stop when we encounter the null character.
        // Notice that the null character is not appended to the frame string.
        try {
            do {
                if (!getBytes(&ch, 1)) {
                    return false;
                }
                if (ch != '\0')
                    frame.append(1, ch);
            } while (delimiter != ch);
        } catch (std::exception &e) {
            std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
            return false;
        }
        return true;
    }


    bool ConnectionHandler::sendFrameAscii(const std::string &frame, char delimiter) {
        bool result = sendBytes(frame.c_str(), frame.length());
        if (!result) return false;
        return sendBytes(&delimiter, 1);
    }


// Close down the connection properly.
    void ConnectionHandler::close() {
        try {
            socket_.close();
        } catch (...) {
            std::cout << "closing failed: connection already closed" << std::endl;
        }
    }
    std::map<std::string, short> ConnectionHandler::initMap() {
        std::map<std::string, short> map = std::map<std::string, short>();
        map.insert(std::pair<std::string, short>("ADMINREG", 1));
        map.insert(std::pair<std::string, short>("STUDENTREG", 2));
        map.insert(std::pair<std::string, short>("LOGIN", 3));
        map.insert(std::pair<std::string, short>("LOGOUT", 4));
        map.insert(std::pair<std::string, short>("COURSEREG", 5));
        map.insert(std::pair<std::string, short>("KDAMCHECK", 6));
        map.insert(std::pair<std::string, short>("COURSESTAT", 7));
        map.insert(std::pair<std::string, short>("STUDENTSTAT", 8));
        map.insert(std::pair<std::string, short>("ISREGISTERED", 9));
        map.insert(std::pair<std::string, short>("UNREGISTER", 10));
        map.insert(std::pair<std::string, short>("MYCOURSES", 11));
       return map;
    }
    void ConnectionHandler::shortToBytes(short num, char* bytesArr)
    {
        bytesArr[0] = ((num >> 8) & 0xFF);
        bytesArr[1] = (num & 0xFF);
    }
    short ConnectionHandler:: bytesToShort(char* bytesArr)
    {
       short result = (short)((bytesArr[0] & 0xff) << 8);
       result += (short)(bytesArr[1] & 0xff);
      return result;
    }
    bool ConnectionHandler::getShouldLOGOUT() {return shouldLOGOUT;}

    std::mutex& ConnectionHandler::getLock() {return mtx;}

    std::condition_variable& ConnectionHandler::getCon() {return con;}

