package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class SampleVisibilityParser extends CLIParser
{
	private static final RegEx SAMPLE_VISIBILITY_SPEC_RE = new RegEx ( "(\\+|\\-|\\-\\-)([^\\s\\+]+)" );

	private AccessControlManager accMgr;

	public SampleVisibilityParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	
	public <T> T run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null sample visibility specification)" );

		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = SAMPLE_VISIBILITY_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? true 
				: "-".equals ( specBits [ 1 ] ) ? false 
				: null; // last case is --   
			accMgr.setBioSampleVisibility ( specBits [ 2 ], publicFlag );
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
