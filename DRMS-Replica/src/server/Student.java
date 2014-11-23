package server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Student 
{
	private String firstName; 
	private String lastName;
	private String emailAddress;
	private String phoneNumber; 
	private String userName; 
	private String password; 
	private String inst;
	
	ArrayList<Borrow> borrowList = new ArrayList<Borrow>();
	
	//Logger
	protected Logger logger;
	
	public void setLogger() {
		try {
			this.logger = Logger.getLogger(userName+Math.random());
			FileHandler fileTxt 	 = new FileHandler("./logs/students/"+userName+".txt");
			SimpleFormatter formatterTxt = new SimpleFormatter();
		    fileTxt.setFormatter(formatterTxt);
		    logger.addHandler(fileTxt);
		}
		catch(Exception err) {
			System.out.println("Couldn't Initiate Logger. Please check file permission");
		}
	}
	
	//End Logger
	
	public String getFirstName() {
		return firstName;
	}



	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}



	public String getLastName() {
		return lastName;
	}



	public void setLastName(String lastName) {
		this.lastName = lastName;
	}



	public String getEmailAddress() {
		return emailAddress;
	}



	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}



	public String getPhoneNumber() {
		return phoneNumber;
	}



	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}



	public String getUserName() {
		return userName;
	}



	public void setUserName(String userName) {
		this.userName = userName;
	}



	public String getPassword() {
		return password;
	}



	public void setPassword(String password) {
		this.password = password;
	}



	public String getInst() {
		return inst;
	}



	public void setInst(String inst) {
		this.inst = inst;
	}

	public Borrow borrowBook(Book book)
	{
		Borrow b = new Borrow(book);
		for(Borrow borrow:borrowList)
		{
			if(borrow.getBook().getName().equals(book.getName()))
			{
				this.logger.info("Already borrowed "+book.getName()+". before. ");
				return null;
			}
		}
		this.borrowList.add(b);
		this.logger.info("Just Borrowed "+book.getName()+". Total borrows: "+this.borrowList.size());
		return b;
	}
	
	public  ArrayList<Borrow> getBorrows()
	{
		return this.borrowList;
	}


	public Student(String userName, String password, String inst)
	{
		this.userName = userName;
		this.password = password;
		this.inst	  = inst;
		this.setLogger();
		this.logger.info("Student created with username: "+userName);
	}
}
