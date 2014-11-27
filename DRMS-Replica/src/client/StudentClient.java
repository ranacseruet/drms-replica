package client;
import UDP.UDPClient;
import common.*;

import java.util.Scanner;
import java.util.logging.Logger;

public class StudentClient extends BaseClient
{
	private int id;
	public StudentClient()
	{
		
	}
	
	public StudentClient(int id)
	{
		this.id = id;
	}
	
	public void showMenu() 
	{
		System.out.println("\n****Welcome to DRMS Student Client System****\n");
		System.out.println("Please select an option (1-3)");
		System.out.println("1. Create An Account.");
		System.out.println("2. Reserve a Book");
		System.out.println("3. Multi Thread Test");
		System.out.println("4. Concurrent Reservation Test");
		System.out.println("5. Exit");
	}
	
	public Logger getLogger(String userName)
	{
		this.setLogger(userName, "logs/students/"+userName+".txt");
		return this.logger;
	}
	
	public void run()
	{
		try
		{
			createAccount();
		}
		catch(Exception err) {
			err.printStackTrace();
		}
	}
	
	public void createAccount()
	{
		
		try {
			UDPClient server = concordiaServer;
			//String username = "";
			/*if(id == 0)
			{
				username = "boromirking";
			}
			else
			{
				username = "Frodobagging";
			}*/
			String username = "test"+this.id;
			String pass = "1234";
			String bookName = "Bones";
			String authorName = "Kathy";
			String message = "req:create:firstName:lastName:email:phoneNumber:"+username+":"+pass+":Concordia";
			String response = server.send(message);
			System.out.println("UDP create account status: "+response);
			boolean test =  Boolean.parseBoolean(response);
			//boolean test = server.createAccount("test", "test", "test@test", "123123123", username, pass, "concordia");
			if(test)
			{
				System.out.println("student created with username: "+username);
				message = "req:reserv:"+username+":"+pass+":"+bookName+":"+authorName+":Concordia";
				response = server.send(message);
				if(Boolean.parseBoolean(response))
				{
					this.getLogger(username).info("Inter-Library Reservation Success with book: "+bookName);
				}
				else
				{
					this.getLogger(username).info("Inter-Library Reservation Failed For book: "+bookName);
				}
			}
			else
			{
				System.out.println("couldn't create student with username: "+username);
			}
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}
	
	
	public static void main(String args[]) {
		try {
			
			//createAccount();
			StudentClient client = new StudentClient();
			client.args = args;
			client.initializeServers(args);
			
			UDPClient server;
			
			int userChoice=0;
			Scanner keyboard = new Scanner(System.in);
			
			server = client.getValidServer(keyboard);
			
			client.showMenu();
			
			String userName, password, inst;
			boolean isSuccess;
			int numThread;
			String request;
			while(true)
			{
				
				userChoice = client.getValidInt(keyboard);
				// Manage user selection.
				switch(userChoice)
				{
				case 1: 
					System.out.println("First Name: ");
					String firstName = client.getValidString(keyboard);
					System.out.println("Last Name: ");
					String lastName = client.getValidString(keyboard);
					System.out.println("Email: ");
					String emailAddress = client.getValidString(keyboard);
					System.out.println("Phone No: ");
					String phoneNumber = client.getValidString(keyboard);
					System.out.println("User Name: ");
					userName = client.getValidString(keyboard);
					System.out.println("Pass: ");
					password = client.getValidString(keyboard);
					
					request = "req:create:"+firstName+":"+lastName+":"+emailAddress+":"+phoneNumber+":"+userName+":"+password+":"+"Concordia"; //last part optional
					client.getLogger(userName).info(server.send(request));
					//TODO what to do with institute name
					//client.getLogger(userName).info(""+server.createAccount(firstName, lastName, emailAddress, phoneNumber, userName, password, client.instituteName));

					client.showMenu();
					break;
				case 2:
					System.out.println("User Name: ");
					userName = client.getValidString(keyboard);
					System.out.println("Pass: ");
					password = client.getValidString(keyboard);
					System.out.println("Book Name: ");
					String bookName = client.getValidString(keyboard);
					System.out.println("Author: ");
					String authorName = client.getValidString(keyboard);
					request = "req:reserv:"+userName+":"+password+":"+bookName+":"+authorName+":Concordia";
					if(Boolean.parseBoolean(server.send(request))) {
						client.getLogger(userName).info("Reserve Success");
					}
					else {
						client.getLogger(userName).info("Direct Reserve failed. Trying with inter-library reserve request");
						//Try to reserve on other libraries
						request = "req:intrese:"+userName+":"+password+":"+bookName+":"+authorName+":Concordia";
						if(Boolean.parseBoolean(server.send(request))) {
							client.getLogger(userName).info("Inter-Library Reservation Success");
						}
						else {
							client.getLogger(userName).info("Couldn't reserve via Inter-Library Reservation either");
						}
					}

					client.showMenu();
					break;
				case 3:
					System.out.println("Number Of Thread");
					numThread = client.getValidInt(keyboard);
					for(int i=0; i <numThread;i++)
					{
						StudentClient st = new StudentClient();
						st.initializeServers(args);
						st.start();
					}
					client.showMenu();
					break;
				case 4:
					System.out.println("Concurrent Reservation Test");
					numThread = client.getValidInt(keyboard);
					for(int i=0; i < numThread;i++)
					{
						StudentClient st = new StudentClient(i);
						st.initializeServers(args);
						st.start();
					}
					client.showMenu();
					break;	
				case 5:
					System.out.println("Have a nice day!");
					keyboard.close();
					System.exit(0);
				default:
					System.out.println("Invalid Input, please try again.");
				}
			}
			
		}
		catch(Exception err) {
			err.printStackTrace();
		}
		
	}
}
