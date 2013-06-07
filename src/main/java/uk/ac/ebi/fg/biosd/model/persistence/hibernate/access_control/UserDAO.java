package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import java.util.HashSet;

import javax.persistence.EntityManager;

import org.apache.commons.lang.Validate;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * DAO for {@link User} management.
 * 
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

	/**
	 * Removes {@link User#getOwnedEntities() links to owned entities} before removing the user. It's up to the caller 
	 * to allow that or not.
	 * 
	 */
	@Override
	public boolean delete ( User user )
	{
    Validate.notNull ( user, "Internal error: user must not be null" );
    EntityManager em = getEntityManager ();
    if ( !em.contains ( user ) ) return false;

		for ( BioSample smp: new HashSet<BioSample> ( user.getBioSamples () ) ) user.deleteBioSample ( smp ); 
		for ( BioSampleGroup sg: new HashSet<BioSampleGroup> ( user.getBioSampleGroups () ) ) user.deleteBioSampleGroup ( sg );
		for ( MSI msi: new HashSet<MSI> ( user.getMSIs () ) ) user.deleteMSI ( msi );
		
    em.remove ( user );
    return true;
	}
}
