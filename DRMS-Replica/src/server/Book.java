package server;

public class Book 
{
	private String name;
	private String author;
	private int numOfCopy;
	
	public String getName() {
		return name;
	}

	public int getNumOfCopy() {
		return numOfCopy;
	}

	public void setNumOfCopy(int numOfCopy) {
		this.numOfCopy = numOfCopy;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public Book(String name, String author, int numOfCopy)
	{
		this.name 	= name;
		this.author = author;
		this.numOfCopy = numOfCopy;
	}
}
