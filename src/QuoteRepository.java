import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException; 
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class QuoteRepository {
	private SessionFactory sessFact;
	
	public QuoteRepository()
	{
		try {
			this.sessFact = new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) { 
			System.err.println("Failed to create sessionFactory object." + ex);
			throw new ExceptionInInitializerError(ex); 
		}
	}
	
	/*
	 * Add a quote to the database
	 */
	public int addQuote(String name, String quote, Date time)
	{
		Session session = sessFact.openSession();
		Transaction tx = null;
		int qtID = 0;
		
		try {
			tx = session.beginTransaction();
			Quote qt = new Quote(name, quote, time);
			qtID = (int) session.save(qt); 
			tx.commit();
		} catch (HibernateException e) {
			if (tx!=null) {tx.rollback();}
			e.printStackTrace(); 
		} finally {
			session.close();
		}
		
		return qtID;
	}
	
	/*
	 * Read quotes from the database
	 */
	@SuppressWarnings("unchecked")
	public List<Quote> readQuotes()
	{
		Session session = sessFact.openSession();
		Transaction tx = null;
		List<Quote> quotes = new ArrayList<Quote>();
		
		try {
			tx = session.beginTransaction();
			quotes = session.createQuery("FROM Quote").list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx!=null) {tx.rollback();}
			e.printStackTrace(); 
		} finally {
			session.close(); 
		}
		
		return quotes;
	}
}
