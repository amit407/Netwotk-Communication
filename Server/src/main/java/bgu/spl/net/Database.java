package bgu.spl.net;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {
	/*private ConcurrentHashMap<String,LinkedList<Integer>> userCourseList;*/
	private ConcurrentHashMap<Course, ConcurrentSkipListSet<String>> courseUserList;
	private ConcurrentHashMap<String,User> userList;
	private ConcurrentHashMap<Short,Course> courseList;
	private Object lock=new Object();


	private static class SingletonHolder {
		private static Database instance = new Database();
	}


	//to prevent user from creating new Database
	private Database() {
		courseUserList = new ConcurrentHashMap<>();
		userList = new ConcurrentHashMap<>();
		courseList = new ConcurrentHashMap<>();
		initialize("./Courses.txt");
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Database getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
	 * loades the courses from the file path specified 
	 * into the Database, returns true if successful.
	 */
	private boolean initialize(String coursesFilePath)  {
		try {
			FileReader input = new FileReader(coursesFilePath);
			BufferedReader bufRead = new BufferedReader(input);
			String myLine = null;


			for (int i = 0; (myLine = bufRead.readLine()) != null; i++) {
				String[] array = myLine.split("\\|");
				Course course = new Course(array[0], array[1], array[2], array[3], i);
				courseUserList.put(course,new ConcurrentSkipListSet<>());
				courseList.put(Short.parseShort(array[0]),course);
			}
			return true;
		}
		catch (Exception E) { E.printStackTrace();  return false;}
	}

	public String ADMINREG (String username, String password){
		synchronized (lock){
		if(userList.containsKey(username))
			return "false";
		userList.put(username, new User(username, password, true));
		return "true";
		}
	}
	public String STUDENTREG (String username, String password){
		synchronized (lock) {
			if (userList.containsKey(username))
				return "false";
			userList.put(username, new User(username, password, false));
			return "true";

		}
	}
	public String LOGIN(String username, String password){
		User user = userList.get(username);
		if(!userList.containsKey(username) || ( !user.getPassword().equals(password)))
			return "false";
		else synchronized (user){
			if(!user.isLoggedIn()) {
				user.LOGIN();
				return "true";
			}
			else
				return "false";
		}
	}

	public String LOGOUT(String username) {
		User user = userList.get(username);
		if(user == null)
			return "false";
		else {
			user.LOGOUT();
			return "true";
		}
	}


	public String COURSEREG(short courseNum, String username){
		User student = userList.get(username);
		Course course = courseList.get(courseNum);
		if(student == null || course == null || (student.isAdmin() |
				courseUserList.get(course).contains(username) |
				!student.isPassedKdam(course))){
			return "false";
		}
		synchronized (course) {
			if(!course.isFull()) {
				student.addCourse(course);
				course.addStudent();
				courseUserList.get(course).add(username);
				return "true";
			}
			return "false";
		}
	}

	public String KDAMCHECK(short courseNum, String username){
		Course course = courseList.get(courseNum);
		User student = userList.get(username);
		if(course == null | student == null | student.isAdmin())
			return "ERROR";
		return Arrays.toString(course.getKdamCoursesList()).replaceAll("\\s","");

	}
	public String COURSESTAT(short courseNum, String username){
		User user = userList.get(username);
		Course course = courseList.get(courseNum);
		if(user == null || course == null | !user.isAdmin())
			return "ERROR";

	/*	Collections.sort(courseUserList.get(course));*/
		return "Course: (" + courseNum + ") " + course.getCourseName() + "\nSeats Available: " +
				course.getFreeSeats() + "/" + course.getNumOfMaxStudents() + "\nStudents Registered: " +
				(courseUserList.get(course)).toString().replaceAll("\\s", "");
	}
	public String STUDENTSTAT(String studentName, String username){
		User admin = userList.get(username);
		User student = userList.get(studentName);
		if(admin == null | student == null || !admin.isAdmin())
			return "ERROR";

		LinkedList<Short> listCourseNum = arrangeUserCourses(student);
		return "Student: " + studentName + "\nCourses: " + listCourseNum.toString().replaceAll("\\s", "");


	}
	private LinkedList<Short> arrangeUserCourses(User student){
		LinkedList<Course> studentCourseList = student.getCoursesList();
		Collections.sort(studentCourseList,Comparator.comparingInt((course)->course.getOrder()));
		LinkedList<Short> listCourseNum = new LinkedList<>();
		for(int i = 0; i < studentCourseList.size(); i++){
			listCourseNum.add(studentCourseList.get(i).getCourseNum());
		}
		return listCourseNum;
	}
	public String ISREGISTERED( short courseNum, String username) {
		User user = userList.get(username);
		Course course = courseList.get(courseNum);
		if (course == null | user == null || user.isAdmin())
			return "ERROR";

		if (user.getCoursesList().contains(course))
			return "REGISTERED";
		return "NOT REGISTERED";


	}
	public String UNREGISTER( short courseNum, String username){
		User student = userList.get(username);
		Course course = courseList.get(courseNum);

		if(student == null | course == null || student.isAdmin())
			return "false";

		LinkedList<Course> studentsCourseList = student.getCoursesList();
		if(!studentsCourseList.contains(course))
			return "false";

		student.unregister(course);
		courseUserList.get(course).remove(username);
		course.omitStudent();
		return "true";


	}
	public String MYCOURSES(String username){
		User student = userList.get(username);
		if(student == null || student.isAdmin())
			return "ERROR";
		LinkedList<Short> list = arrangeUserCourses(student);
		return  list.toString().replaceAll("\\s", "");


	}





}
