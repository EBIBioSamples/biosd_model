package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.UserDAO;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class UserDeleteParser extends CLIParser
{
	private UserDAO userDao;

	public UserDeleteParser ( EntityManager entityManager ) {
		super ( entityManager );
	}


	/**
	 * @param cmd email[++] ++ for auto-removing the links to the {@link User#getOwnedEntities() entities the user owns}.
	 */
	public boolean run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null user specification)" );

		int cmdLen = cmd.length ();
		boolean isCascaded = cmdLen > 2 && "++".equals ( cmd.substring ( cmdLen - 2 ) );
		String email = isCascaded ? cmd.substring ( 0, cmdLen - 2 ) : cmd;
		
		email = email.toLowerCase ();

		EntityManager em = this.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();

		if ( !isCascaded )
		{
			User user = userDao.find ( email );
			if ( user == null ) return false;
			if ( !user.getOwnedEntities ().isEmpty () )
				throw new RuntimeException ( "Cannot remove user '" + email + "', it still owns database entities, use the ++ option" );
		}
		boolean result = userDao.delete ( email );
		ts.commit ();
		return result;
	}

	
	
	@Override
	public EntityManager getEntityManager () 
	{
		return this.userDao.getEntityManager ();
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		this.userDao = new UserDAO ( entityManager );
	}

}
