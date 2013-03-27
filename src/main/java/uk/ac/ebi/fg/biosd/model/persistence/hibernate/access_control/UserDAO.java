/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * TODO: Comment me!
 * TODO: constraint that disallows to delete a user still owning something
 * 
 * <dl><dt>date</dt><dd>Mar 27, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UserDAO extends AccessibleDAO<User>
{

	public UserDAO ( EntityManager entityManager )
	{
		super ( User.class, entityManager );
	}

}
