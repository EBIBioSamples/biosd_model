package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class MSIRelDateParser extends CLIParser
{

	private AccessControlManager accMgr;

	public MSIRelDateParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	public <T> T run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null submission visibility specification)" );
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = SAMPLE_GROUP_VISIBILITY_SET_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? new Boolean ( true ) 
				: "-".equals ( specBits [ 1 ] ) ? new Boolean ( false ) 
				: null; // last case is --   
			accMgr.setMSIVisibility ( specBits [ 2 ], publicFlag, "++".equals ( specBits [ 3 ] ) );
		}
		ts.commit ();
		return null;
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
	}

}
