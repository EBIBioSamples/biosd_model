package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.UserDAO;

/**
 * Manages the 'user search' command. @see {@link AccessControlCLI}.
 *
 * <dl><dt>date</dt><dd>Jun 5, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UserSearchParser extends CLIParser
{
	private UserDAO userDao;

	public UserSearchParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * <pre>search user --email|--name|--surname|--password|--notes = value</pre>
	 * @return the list of found users.
	 */
	public List<User> run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error, null user description" );

		Map<String, String> attrs = UserStoreParser.parseUserAttributes ( cmd );
		List<User> users = userDao.findByExample ( new User ( attrs ), true );
		Collections.sort ( users, new User.UserComparator () );
		
		for ( User user: users )
			System.out.format ( "email: %s, name: %s, surname %s, notes: %s\n", 
				user.getEmail (), user.getName (), user.getSurname (), StringEscapeUtils.escapeJava ( user.getNotes () ) );
		
		return users;
	}
	
	@Override
	public EntityManager getEntityManager ()
	{
		return userDao.getEntityManager ();
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		this.userDao = new UserDAO ( entityManager );
	}
}
