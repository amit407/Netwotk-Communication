package bgu.spl.net.impl.BGRSServer;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class TesterClient implements Runnable {

    /**
     * Passive object representing the message sent from the testerClient to the server
     */

    private static class testerMessage {
        private final short OPCode;
        private short courseNum = 0;
        private String name = null;
        private String pass;
        private short returnOPCode = 0;
        private String returnInfo = "";

        public testerMessage(short OPCode, short courseNum, String name, String pass) {
            this.OPCode = OPCode;
            this.courseNum = courseNum;
            this.name = name;
            this.pass = pass;
        }

        public testerMessage(short OPCode, short returnOPCode) {
            this.OPCode = OPCode;
            this.returnOPCode = returnOPCode;
        }

        public short getOPCode() {
            return OPCode;
        }

        public int getCourseNum() {
            return courseNum;
        }

        public String getName() {
            return name;
        }

        public String getPass() {
            return pass;
        }

        public short getReturnOPCode() {
            return returnOPCode;
        }

        public String getReturnInfo() {
            return returnInfo;
        }

        public void setReturnInfo(String returnInfo) {
            this.returnInfo = returnInfo;
        }
    }

    DataInputStream socketReader;
    DataOutputStream socketWriter;

    public TesterClient(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            this.socketReader = new DataInputStream(socket.getInputStream());
            this.socketWriter = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(testerMessage toSend) {
        try {
            System.out.println(Thread.currentThread().getName() + " -> trying to " + toSend.getOPCode() + " " + toSend.getName() + " " + ((toSend.getCourseNum() != 0) ? toSend.getCourseNum() : ""));
            byte[] actualBytesToSend = encode(toSend);
            socketWriter.write(actualBytesToSend, 0, actualBytesToSend.length);
            socketWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] encode(testerMessage message) {
        byte[] ans = new byte[1 << 10];
        int index = 2;
        shortToBytes(message.getOPCode(), ans, 0);
        switch (message.getOPCode()) {
            case 1:
            case 2:
            case 3:
                byte[] nameAndPass = message.getName().getBytes(StandardCharsets.UTF_8);
                for (byte toAdd : nameAndPass) {
                    ans[index] = toAdd;
                    ans[index + nameAndPass.length + 1] = toAdd;
                    index++;
                }
                return Arrays.copyOf(ans, index + nameAndPass.length + 2);
            case 8:
                byte[] name = message.getName().getBytes(StandardCharsets.UTF_8);
                for (byte toAdd : name)
                    ans[index++] = toAdd;
                return Arrays.copyOf(ans, index + 1);
            case 5:
            case 6:
            case 7:
            case 9:
            case 10:
                shortToBytes((short) message.getCourseNum(), ans, index);
                return Arrays.copyOf(ans, 4);
            default:
                return Arrays.copyOf(ans, 2);
        }
    }

    private void read() {
        try {
            byte[] received = new byte[4];
            for (int i = 0; i < 4; i++)
                received[i] = socketReader.readByte();
            testerMessage answer = new testerMessage(bytesToShort(received, 0), bytesToShort(received, 2));
            if (answer.getOPCode() == 12
                    //Add this line if you aren't receiving a \0 byte at the end of every ACK message
                    && (answer.getReturnOPCode() == 6 || answer.getReturnOPCode() == 7 || answer.getReturnOPCode() == 8 || answer.getReturnOPCode() == 9 || answer.getReturnOPCode() == 11)) {
                received = new byte[1 << 10];
                int i = 0;
                do
                    received[i] = socketReader.readByte();
                while (received[i++] != (byte) 0);
                answer.setReturnInfo(new String(received, 0, i, StandardCharsets.UTF_8));
            } else answer.setReturnInfo("");
            System.out.println(Thread.currentThread().getName() + " -> " + ((answer.getOPCode() == 12) ? "ACK " : "ERR ") + answer.getReturnOPCode() + " " + answer.getReturnInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private short bytesToShort(byte[] arr, int index) {
        short result = (short) ((arr[index] & 0xff) << 8);
        result += (short) (arr[index + 1] & 0xff);
        return result;
    }

    private void shortToBytes(short num, byte[] arr, int index) {
        arr[index] = (byte) ((num >> 8) & 0xFF);
        arr[index + 1] = (byte) (num & 0xFF);
    }

    /**
     * each thread will preform all the following tasks and then exit.
     * you can add other tasks, the code supports all the different message types
     */

    @Override
    public void run() {
        String generatedUsername = UUID.randomUUID().toString().substring(0, 4);
        //register
        testerMessage toSend = new testerMessage((short) 2, (short) 0, generatedUsername, generatedUsername);
        write(toSend);
        read();
        //login
        toSend = new testerMessage((short) 3, (short) 0, generatedUsername, generatedUsername);
        write(toSend);
        read();
        //course register
        toSend = new testerMessage((short) 5, (short) (new Random().nextInt(4) + 1), generatedUsername, generatedUsername);
        write(toSend);
        read();
        //course register
        toSend = new testerMessage((short) 5, (short) (new Random().nextInt(4) + 1), generatedUsername, generatedUsername);
        write(toSend);
        read();
        //my courses
        toSend = new testerMessage((short) 11, (short) 0, generatedUsername, generatedUsername);
        write(toSend);
        read();
        //logout
        toSend = new testerMessage((short) 4, (short) 0, generatedUsername, generatedUsername);
        write(toSend);
        read();
    }
}