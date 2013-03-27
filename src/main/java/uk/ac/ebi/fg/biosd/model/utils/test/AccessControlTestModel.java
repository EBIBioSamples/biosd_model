/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.utils.test;

import java.util.GregorianCalendar;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.UserDAO;

/**
 * Like {@link TestModel}, but with additional objects regarding access control and ownership. 
 * <dl><dt>date</dt><dd>Mar 27, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlTestModel extends TestModel
{

	public User user1;
	public User user2;

	public AccessControlTestModel ()
	{
		this ( "" );
	}

	public AccessControlTestModel ( String prefix )
	{
		super ( prefix );
		
		user1 = new User ( prefix + "user1@test.net", "Mr", "Test 1", User.hashPassword ( "foo" ), "A test user 1" );
		user2 = new User ( prefix + "user2@test.net", "Mr", "Test 2", User.hashPassword ( "foo" ), "A test user 2" );
		
		this.smp1.addUser ( user1 );
		this.smp2.addUser ( user2 );
		this.smp1.setPublicFlag ( true );
		this.smp2.setReleaseDate ( new GregorianCalendar ( 2010, 3, 21 ).getTime () );
	}

	@Override
	public void delete ( EntityManager em )
	{
		super.delete ( em );
		
		UserDAO userDao = new UserDAO ( em );
		userDao.delete ( user1 );
		userDao.delete ( user2 );
	}
	
}
