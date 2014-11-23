package server;

import java.util.Date;

public class Borrow 
{
	private Book book;
	
	private Date borrowTime;
	
	private int dueDate = 2;
	
	public Borrow(Book book)
	{
		this.book = book;
		this.borrowTime = new Date();
	}
	
	
	public Book getBook() {
		return book;
	}

	public void setDueDate(int dueDate) {
		this.dueDate = dueDate;
	}

	public long getDueDays()
	{
		Date currTime = new Date();
		long startTime = borrowTime.getTime();
		long endTime = currTime.getTime();
		long diffTime = endTime - startTime;
		long diffDays = diffTime / (1000 * 60 * 60 * 24);
		if (diffDays < 0) {
			diffDays = 0;
		}
		return (diffDays - dueDate);
	}
	
}
