package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;

/**
 *  Manage the visibility commands for sample groups. @see {@link VisibilityParser}.
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class SampleGroupVisibilitySetParser extends CLIParser
{
	private AccessControlManager accMgr;

	public SampleGroupVisibilitySetParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * @return the number of entities that were changed.
	 */
	public int run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null sample-group visibility specification)" );

		int result = 0;
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = VisibilityParser.SAMPLE_GROUP_VISIBILITY_SET_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? new Boolean ( true )
				: "-".equals ( specBits [ 1 ] ) ? new Boolean ( false ) 
				: null; // last case is --   
			result += accMgr.setBioSampleGroupVisibility ( specBits [ 2 ], publicFlag, "++".equals ( specBits [ 3 ] ) );
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
	}

}
