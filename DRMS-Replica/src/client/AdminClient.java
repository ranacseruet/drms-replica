package client;

import java.util.Scanner;

import UDP.UDPClient;
import common.ILibrary;

public class AdminClient extends BaseClient
{
	//Return basic menu.
	public void showMenu()
	{
		System.out.println("\n****Welcome to DRMS Admin Client System****\n");
		System.out.println("Please select an option (1-2)");
		System.out.println("1. Get Non Returner.");
		System.out.println("2. Exit");
	}
	
	public void run()
	{
		try {
			AdminClient client = new AdminClient();
			client.initializeServers(args);
			
			UDPClient server;
			
			int userChoice=0;
			Scanner keyboard = new Scanner(System.in);
			
			server = client.getValidServer(keyboard);
			
			client.showMenu();
			
			String userName, password;
			
			client.setLogger("Admin", "logs/admins/Admin.txt");
			
			while(true)
			{
				
				userChoice = client.getValidInt(keyboard);
				// Manage user selection.
				switch(userChoice)
				{
				case 1: 
					System.out.println("User Name: ");
					userName = client.getValidString(keyboard);
					System.out.println("Pass: ");
					password = client.getValidString(keyboard);
					System.out.println("No Of Days: ");
					short numOfDays = (short)client.getValidInt(keyboard);
					//TODO what to do with institute name
					String request = "req:getnon:"+userName+":"+password+":concordia:"+numOfDays;
					client.logger.info(server.send(request));
					client.showMenu();
					break;
				case 2:
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
	
	public static void main(String args[])
	{
		AdminClient client = new AdminClient();
		client.args = args;
		client.run();
	}
}
