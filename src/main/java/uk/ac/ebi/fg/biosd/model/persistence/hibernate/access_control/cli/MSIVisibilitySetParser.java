package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class MSIVisibilitySetParser extends CLIParser
{
	private AccessControlManager accMgr;
	private AccessibleDAO<MSI> msiDao; 

	public MSIVisibilitySetParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	public List<MSI> run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null submission visibility specification)" );
		
		List<MSI> result = new ArrayList<MSI> ();
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = SAMPLE_GROUP_VISIBILITY_SET_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			
			String acc = specBits [ 2 ]; 
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? new Boolean ( true ) 
				: "-".equals ( specBits [ 1 ] ) ? new Boolean ( false ) 
				: null; // last case is --
			boolean isCascaded = "++".equals ( specBits [ 3 ] );
				
			MSI msi = accMgr.setMSIVisibility ( acc, publicFlag, isCascaded );
			
			result.add ( msi );
		}
		ts.commit ();
		return result;
	}

	
	@Override
	public EntityManager getEntityManager ()
	{
		return accMgr.getEntityManager ();
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		this.accMgr = new AccessControlManager ( entityManager );
		this.msiDao = new AccessibleDAO<MSI> ( MSI.class, entityManager );
	}

}
