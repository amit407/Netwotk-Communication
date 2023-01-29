package bgu.spl.net;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {
    private final String userName;
    private final String password;
    private final boolean isAdmin;
    private boolean isLoggedIn;
    private LinkedList<Course> coursesList;


    public User(String userName, String password, boolean isAdmin) {
        this.userName = userName;
        this.password = password;
        this.isAdmin = isAdmin;
        isLoggedIn = false;
        coursesList = new LinkedList<>();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    public boolean isPassedKdam(Course course){
        short[] kdamCourses = course.getKdamCoursesList();
        LinkedList<Short> courseNum = checkKdam();
        if(kdamCourses.length == 0)
            return true;
        for(int i = 0; i < kdamCourses.length; i++){
            if(!courseNum.contains(kdamCourses[i]))
                return false;
        }
        return true;
    }
    public LinkedList<Short> checkKdam(){
        LinkedList<Short> courseNum = new LinkedList<>();
        for(int i = 0; i < coursesList.size(); i++){
            courseNum.add(coursesList.get(i).getCourseNum());
        }
        return courseNum;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getPassword() {
        return password;
    }

    public LinkedList<Course> getCoursesList() {
        return coursesList;
    }

    public void addCourse(Course course){
        if(!coursesList.contains(course))
            coursesList.add(course);
    }
    public void unregister(Course course){
        coursesList.remove(course);

    }

    public void LOGIN(){
        isLoggedIn = true;
    }
    public void arrangeVector(){
        Database database = Database.getInstance();

    }



    public void LOGOUT(){
        isLoggedIn = false;
    }

}
