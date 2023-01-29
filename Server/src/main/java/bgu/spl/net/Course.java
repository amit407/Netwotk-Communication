package bgu.spl.net;

public class Course {
    private final short courseNum;
    private final String courseName;
    private  short[] kdamCoursesList;
    private final int numOfMaxStudents;
    private final int order;
    private int freeSeats;

    public Course(String courseNum, String courseName,String kdamCoursesList, String numOfMaxStudents, int order) {
        this.courseNum = Short.parseShort(courseNum);
        this.courseName = courseName;

        if(kdamCoursesList.length() == 2) //No kdam courses
             this.kdamCoursesList = new short[0];
        else if( kdamCoursesList.length() > 2 & !kdamCoursesList.contains(",")) { //Extactly one kdam course
            this.kdamCoursesList = new short[1];
            this.kdamCoursesList[0] = Short.parseShort(kdamCoursesList.substring(1,kdamCoursesList.length() -1));
        }
        else { //More than one kdam courses
            String[] kdamCourses = kdamCoursesList.split(",");
            this.kdamCoursesList = new short[kdamCourses.length];
            for (int i = 0; i < kdamCourses.length; i++) {
                if( i == 0) //remove char '[' from first kdam course num
                    this.kdamCoursesList[i] = Short.parseShort(kdamCourses[i].substring(1));
                else if (i == kdamCourses.length - 1) //remove char ']' from last kdam course num
                    this.kdamCoursesList[i] = Short.parseShort(kdamCourses[i].substring(0, kdamCourses[i].length() - 1));
                else //not first and not last course kdam
                    this.kdamCoursesList[i] = Short.parseShort(kdamCourses[i]);
            }
        }
        this.numOfMaxStudents = Integer.parseInt(numOfMaxStudents);
        this.order = order;
        freeSeats = Integer.parseInt(numOfMaxStudents);

    }
    public void addStudent(){
        freeSeats--;
    }
    public void omitStudent() { freeSeats++;}
    public int getOrder() {
        return order;
    }

    public boolean isFull(){
        return(freeSeats == 0);
    }

    public short getCourseNum() {
        return courseNum;
    }

    public int getFreeSeats() {
        return freeSeats;
    }

    public String getCourseName() {
        return courseName;
    }

    public short[] getKdamCoursesList() {
        return kdamCoursesList;
    }

    public int getNumOfMaxStudents() {
        return numOfMaxStudents;
    }
}
