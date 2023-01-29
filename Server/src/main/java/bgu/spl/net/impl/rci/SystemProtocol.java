package bgu.spl.net.impl.rci;

import bgu.spl.net.Database;
import bgu.spl.net.Lambda;
import bgu.spl.net.MSG;
import bgu.spl.net.api.MessagingProtocol;

public class SystemProtocol implements MessagingProtocol<MSG> {
  private Lambda[] funcList = new Lambda[12];
  private String username = "";
  private boolean login=false;
   private boolean shouldTerminate = false;

    public SystemProtocol() {
        Database database = Database.getInstance();
            funcList[1] = (msg) -> {

                if (login==false & database.ADMINREG(msg.getUsername(), msg.getPassword()).equals("true")){
                    MSG ack = new MSG((short)12, null, null, (short)0,"", (short)1);

                    return ack;
                }
                MSG error = new MSG((short)13, null, null, (short)0,"", (short)1);

                return error;
            };
        funcList[2] = (msg) -> {

            if (login==false & database.STUDENTREG(msg.getUsername(), msg.getPassword()).equals("true")){
                MSG ack = new MSG((short)12, null, null, (short)0,"", (short)2);

                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)2);

            return error;
        };

        funcList[3] = (msg) -> {
        if (!login & database.LOGIN(msg.getUsername(), msg.getPassword()).equals("true")){
            MSG ack = new MSG((short)12, null, null, (short)0,"", (short)3);

            username = msg.getUsername();
            login=true;
            return ack;
        }
        MSG error = new MSG((short)13, null, null, (short)0,"", (short)3);

        return error;
    };
        funcList[4] = (msg) -> {
            if (login & database.LOGOUT(username).equals("true")){
                MSG ack = new MSG((short)12, null, null, (short)0,"", (short)4);

                shouldTerminate = true;
                login=false;
                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)4);

            return error;
        };
        funcList[5] = (msg) -> {
            if (login & database.COURSEREG(msg.getCourseNum(),username).equals("true")){
                MSG ack = new MSG((short)12, null, null, (short)0,"", (short)5);

                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)5);

            return error;
        };
        funcList[6] = (msg) -> {
            String KdamList = database.KDAMCHECK(msg.getCourseNum(),username);
            if (login & !KdamList.equals("ERROR")){
                MSG ack = new MSG((short)12, null, null, (short)0, KdamList, (short)6);

                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)6);

            return error;
        };
        funcList[7] = (msg) -> {
            String CourseStat = database.COURSESTAT(msg.getCourseNum(),username);
            if (login & !CourseStat.equals("ERROR")){
                MSG ack = new MSG((short)12, null, null, (short)0, CourseStat, (short)7);

                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)7);

            return error;
        };
        funcList[8] = (msg) -> {
            String StudentStat = database.STUDENTSTAT(msg.getReturnSTR(),username);
            System.out.println(StudentStat + " of this USER!!!!!!!: " + msg.getReturnSTR());
            if (login & !StudentStat.equals("ERROR")){
                MSG ack = new MSG((short)12, null, null, (short)0, StudentStat, (short)8);

                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)8);

            return error;
        };
        funcList[9] = (msg) -> {
            String IsRegistered = database.ISREGISTERED(msg.getCourseNum(),username);
            if (login & (IsRegistered.equals("REGISTERED") | IsRegistered.equals("NOT REGISTERED")) ){
                MSG ack = new MSG((short)12, null, null, (short)0, IsRegistered, (short)9);
                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)9);
            return error;
        };
        funcList[10] = (msg) -> {
            String Unregister = database.UNREGISTER(msg.getCourseNum(),username);
            if (login & Unregister.equals("true")){
                MSG ack = new MSG((short)12, null, null, (short)0, "", (short)10);
                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)10);
            return error;
        };
        funcList[11] = (msg) -> {
            String MyCourses = database.MYCOURSES(username);
            if (login & !MyCourses.equals("ERROR")){
                MSG ack = new MSG((short)12, null, null, (short)0, MyCourses, (short)11);
                return ack;
            }
            MSG error = new MSG((short)13, null, null, (short)0,"", (short)11);
            return error;
        };

    }
    @Override
    public MSG process(MSG msg) {
        return funcList[msg.getOpcode()].run(msg);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

}


