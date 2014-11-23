
package common;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style=Style.RPC)
public interface ILibrary
{
	public boolean createAccount(String firstName, String lastName, String emailAddress, 
			String phoneNumber, String userName, String password, String inst);
	
	public boolean reserveBook(String username, String password, String bookName, 
			String authorName);
	
	public String getNonRetuners(String username, String password, String inst,
			int numOfDays);
	
	public boolean reserveInterLibrary(String username, String password, String bookName, String authorName);
}
