package bgu.spl.net;



    public class MSG {
        private final short opcode;
        private  String username;
        private  String password;
        private  short courseNum;
        private String returnSTR;
        private short returnOP;


        public MSG(short opcode, String username, String password, short courseNum, String returnSTR, short returnOP) {
            this.opcode = opcode;
            this.username = username;
            this.password = password;
            this.courseNum = courseNum;
            this.returnSTR = returnSTR;
            this.returnOP = returnOP;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setCourseNum(short courseNum) {
            this.courseNum = courseNum;
        }

        public void setReturnSTR(String returnSTR) {
            this.returnSTR = returnSTR;
        }

        public void setReturnOP(short returnOP) {
            this.returnOP = returnOP;
        }

        public MSG(short opcode){ this.opcode = opcode;}

        public short getCourseNum() {
            return courseNum;
        }

        public String getReturnSTR() {
            return returnSTR;
        }

        public short getReturnOP() {
            return returnOP;
        }

        public short getOpcode() {
            return opcode;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public void setReturnOP(int returnOP) {
            this.returnOP = (short)returnOP;
        }
    }

