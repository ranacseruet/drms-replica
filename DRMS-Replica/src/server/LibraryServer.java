package server;

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import UDP.UDPClient;
import UDP.UDPServer;
import common.*;

@WebService(endpointInterface="common.ILibrary")
public class LibraryServer implements ILibrary, Runnable
{	
	private static HashMap<Character, ArrayList<Student>> index = new HashMap<Character, ArrayList<Student>>();
	
	private String instituteName;
	private HashMap<String, Book> books   = new HashMap<String, Book>();
	private int udpPort = 0;
	private static ArrayList<LibraryServer> servers = new ArrayList<LibraryServer>();

	//TODO change later
	public String[] args;
	private ArrayList<Borrow> borrows = new ArrayList<Borrow>(); 
	
	public int getUdpPort() {
		return udpPort;
	}

	//Logger
	private Logger logger;
	
	/**
	 * Initialize logger for library server
	 * @param fileName
	 */
	private void setLogger(String fileName) {
		try{
			this.logger = Logger.getLogger(this.instituteName);
			FileHandler fileTxt 	 = new FileHandler(fileName);
			SimpleFormatter formatterTxt = new SimpleFormatter();
		    fileTxt.setFormatter(formatterTxt);
		    logger.addHandler(fileTxt);
		}
		catch(Exception err) {
			System.out.println("Couldn't Initiate Logger. Please check file permission");
		}
	}
	
	//End Logger
	
	public Student searchStudent(String userName)
	{
		Student st = null;
		ArrayList<Student> list = null;
		try{
			if(index != null) {
				synchronized(index) {
					if(index.containsKey(userName.charAt(0))) {
						
						list = index.get(userName.charAt(0));
					
						if(list != null) {
							for(Student s: list) {
								if(s.getUserName().equals(userName)){
									return s;
								}
							}
						}
						else {
							System.out.println("Null List");
						}
					}
				}
			}
		}
		catch(Exception err) {
			err.printStackTrace();
		}
		return st;
	}

	
	@Override
	/**
	 * Create new Account for student
	 * @param firstName
	 * @param lastName
	 * @param emailAddress
	 * @param phoneNumber
	 * @param userName
	 * @param password
	 * @param inst
	 * @return
	 */
	public synchronized boolean createAccount(String firstName, String lastName,
			String emailAddress, String phoneNumber, String userName,
			String password, String inst) {
		Student st;

		//System.out.println("Account creation request recieved by "+this.instituteName+ " Library Server");
		st = searchStudent(userName);
		
		if(st != null) {
			logger.info("User exists in the system");
			return false;
		}
		
		st = new Student(userName, password, instituteName);
		
		st.setFirstName(firstName);
		st.setLastName(lastName);
		st.setEmailAddress(emailAddress);
		st.setPhoneNumber(phoneNumber);
		
		ArrayList<Student> students = index.get(userName.charAt(0));
		if(students == null) {
			students = new ArrayList<Student>();
			index.put(userName.charAt(0), students);
		}
		students.add(st);
		
		//System.out.println("total student in the list: "+this.index.get(userName.charAt(0)).size());	
		logger.info("Account creation success for user: "+st.getUserName());
		
		return true;
	}

	//TODO where to put sync???
	@Override
	/**
	 * Reserve a book for user
	 * @param username
	 * @param password
	 * @param bookName
	 * @param authorName
	 * @return
	 */
	public boolean reserveBook(String username, String password,
			String bookName, String authorName) {
		
		Student st = searchStudent(username);
		
		if(st == null) {
			logger.info("Student not found");
			return false;
		}
		
		Book book = null;
		if(!st.getPassword().equals(password)) {
			logger.info("Password mismatch");
			return false;
		}
		
		if(!st.getInst().equals(this.instituteName))
		{
			logger.info("student doesn't belong to this server");
			return false;
		}
		
		book = findBook(bookName, authorName);
		if(book == null)
		{
			return false;
		}
		
		synchronized(book) {
			book.setNumOfCopy(book.getNumOfCopy()-1);
			Borrow b = st.borrowBook(book);
			if(b == null)
			{
				return false;
			}
			this.borrows.add(b);
		}
		logger.info("Reserve successfull for user "+username+" with book "+bookName+". Now "+book.getNumOfCopy()+" left");
			
		return true;
	}
	
	private Book findBook(String bookName, String authorName)
	{
		Book book = (Book)this.books.get(bookName);
		if(book == null) {
			logger.info("Book not found");
			return null;
		}
		if(!book.getAuthor().trim().equals(authorName.trim())) {
			logger.info("Author mismatch "+authorName+" : "+book.getAuthor());
			return null;
		}
		if(book.getNumOfCopy() <=0) {
			logger.info("No copy left for book: "+book.getName());
			return null;
		}
		return book;
	}
	

	@Override
	/**
	 * Get non returners for all libraries
	 * @param username
	 * @param password
	 * @param inst
	 * @param numOfDays
	 * @return
	 * @throws RemoteException
	 */
	public String getNonRetuners(String username, String password,
			String inst, int numOfDays) {
		String response = "";
		
		if(!username.equals("Admin") || !password.equals("Admin")) {
			return "Invalid Credential";
		}
		
	    response += calculateNonReturners(numOfDays);
	    for(LibraryServer s : servers) { 
			synchronized(s) {
		    	if(s.instituteName != this.instituteName) {
		    			String requestData = "nonReturn:"+numOfDays;
		    	        UDPClient client = new UDPClient("localhost", s.getUdpPort());
		    	        response += client.send(requestData);
		    	}
			}
        }
	    
	    return response;
	}
	
	/**
	 * iterate through own students and create non-returner response string
	 * @param numOfDays
	 * @return
	 */
	private String calculateNonReturners(int numOfDays)
	{
		String response = this.instituteName+":\n";
		Iterator it = this.index.entrySet().iterator();
	    boolean foundStudent = false;
		while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        
	        ArrayList<Student> studentList = (ArrayList<Student>)pairs.getValue();
	        
	        for(Student st: studentList) {
		        if(st.getInst() == this.instituteName && st.getBorrows().size() > 0) {
		        	//System.out.println("Student "+st.getUserName()+" has borrowed item, checking due date");
		        	ArrayList<Borrow> list = st.getBorrows();
		        	for(Borrow b : list) {
		        		if(b.getDueDays() >= numOfDays) {
		        			response += st.getFirstName()+" "+st.getLastName()+" "+st.getPhoneNumber()+"\n";
		        			foundStudent = true;
		        			break;
		        		}
		        	}
		        }
	        }
	        
	        //it.remove(); // avoids a ConcurrentModificationException
	    }
		
	    if(!foundStudent) {
	    	response += "No student found\n";
	    }
	    
	    response += "------*-------\n\n";
	   
	    
	    return response;
	}
	
	private ORB orb;
	/**
	 * Run thread + UDP server 
	 */
	public void run()
	{
		UDPServer udpServer = null;
		try {
			
			Endpoint endpoint = Endpoint.publish("http://localhost:8080/"+this.instituteName, this);
			
			if(endpoint.isPublished()) {
				System.out.println("Endpoint published for "+this.instituteName);
			}	
			
			//UDP part
			udpServer = new UDPServer("",this.udpPort);
			
			byte [] buffer = new byte[10000];
			this.logger.info("UPD server for "+this.instituteName+" is running on port: "+udpPort);
			String response = "";
			while(true) {
				String data = udpServer.recieveRequest();
				String[] requestParts = data.split(":");
				
				if(requestParts.length == 2 ) {
					//non return request
					logger.info("Nonreturner request received at"+this.instituteName);
					response = calculateNonReturners(Integer.parseInt(requestParts[1].trim()));
				}
				else if(requestParts[0].equals("reserve")) {
					//inter library reserve request
					logger.info("Interlibrary reserve request recieved at: "+this.instituteName);
					Book book = findBook(requestParts[1], requestParts[2]);
					if(book != null)
					{
						logger.info("Requested book "+book.getName()+"(Num Of Copy: "+book.getNumOfCopy()+")");
						synchronized(book) 
						{
							book.setNumOfCopy(book.getNumOfCopy()-1);
						}
						response = "true";
						logger.info("Now, book "+book.getName()+" has "+book.getNumOfCopy()+") copies left");
					}
					else
					{
						response = "false";
					}
				}
				else if(requestParts[1].equals("create")){
					//create account
					 response = createAccount(requestParts[2], requestParts[3], requestParts[4], 
							 requestParts[5], requestParts[6], requestParts[7], requestParts[8])?"true":"false";
				}
					
				udpServer.sendResponse(response);
			}
		}
		catch(Exception err) {
			err.printStackTrace();
		}
		finally{
			udpServer.close();
		}
	}

	/**
	 * Constructor
	 * @param instituteName
	 * @param udpPort
	 */
	public LibraryServer(String instituteName, int udpPort)
	{
		this.instituteName = instituteName;
		this.udpPort	   = udpPort;
		this.setLogger("logs/libraries/"+instituteName+".txt");
	}
	
	/**
	 * Debug Tool to set custom numOfDays
	 * @param username
	 * @param bookName
	 * @param numDays
	 */
	public static void setDuration(String username, String bookName, int numDays)
	{
		
		for(LibraryServer s: servers) {
			Student st = s.searchStudent(username);
			if(st != null && st.getInst().equals(s.instituteName)) {
				
				Book book = (Book)s.books.get(bookName);
				if(book == null) {
					System.out.println("Book not found");
				}
				
				if(book.getNumOfCopy() <= 0) {
					System.out.println("No copy left");
				}
				
				book.setNumOfCopy(book.getNumOfCopy()-1);
				Borrow b = st.borrowBook(book);
				b.setDueDate(numDays);
			}
			else {
				//System.out.println("Student not found");
			}
		}
	}
	
	private static int i=1;
	/**
	 * Dummy data creation for server
	 * @param server
	 */
	public static void addData(LibraryServer server)
	{
		Book book = new Book("Bones","Kathy", 2);
		server.books.put(book.getName(), book);
		
		/*Book book = new Book("Cuda","Nicholas",2);
		server.books.put(book.getName(), book);
		book = new Book("Opencl","Munshi", 3);
		server.books.put(book.getName(), book);
		book = new Book("3DMath","Fletcher", 1);
		server.books.put(book.getName(), book);*/
	}
	
	/**
	 * Main entry point for server
	 * @param args
	 */
	public static void main(String args[])
	{
		try{

			LibraryServer library1 = new LibraryServer("Concordia", 5001);
			Thread server1 =  new Thread(library1);
			server1.start();
			
			LibraryServer library2 = new LibraryServer("Mcgill", 5002);
			Thread server2 =  new Thread(library2);
			server2.start();
			
			LibraryServer library3 = new LibraryServer("Montreal", 5003);
			Thread server3 =  new Thread(library3);
			server3.start();
			
			
			
			
			addData(library3);
			
			servers.add(library1);
			servers.add(library2);
			servers.add(library3);
			
			//runDebugTool();
		}
		catch(Exception err){
			err.printStackTrace();
		}
		
	}
	
	/**
	 * Debug tool menu
	 */
	public static void runDebugTool()
	{
		Scanner keyboard = new Scanner(System.in);
		while(true)
		{
			try {
				keyboard.nextLine();
				System.out.println("\n****Welcome to DRMS Debug Tool System****\n");
				System.out.println("User Name: ");
				String userName = keyboard.nextLine();
				System.out.println("Book: ");
				String bookName = keyboard.nextLine();
				System.out.println("No Of Days: ");
				int numOfDays = keyboard.nextInt();
				//keyboard.nextLine();
				setDuration(userName, bookName, numOfDays);
			}
			catch(Exception er) {
				//er.printStackTrace();
			}
		}
	}

	@Override
	public boolean reserveInterLibrary(String username, String password,
			String bookName, String authorName) {
		
		for(LibraryServer s : servers) { 
	    	if(s.instituteName != this.instituteName) {
	    		try{
	    			
	    			String requestString = "reserve:"+bookName+":"+authorName;
	    	        UDPClient udpClient = new UDPClient("localhost", s.getUdpPort());	    	        
	    	        String response = udpClient.send(requestString);

	    	        if(response.trim().equals("true")) {
	    	        	Book book = new Book(bookName,authorName, 1);
	    	    		this.books.put(book.getName(), book);
	    	    		this.logger.info("Book "+book.getName()+" available at "+s.instituteName);
	    	        	return reserveBook(username, password, bookName, authorName);
	    	        }
	    	        else {
	    	        	this.logger.info("Book "+bookName+" not available at "+s.instituteName);
	    	        }
	    	        
	    		}catch(Exception e){
	    			e.printStackTrace();
	    		}finally{
	    			
	    		}
	    	}
        }

		return false;
	}

}
