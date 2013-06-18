package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * Manage the visibility of {@link BioSample} entities. @see {@link AccessControlCLI} 
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class SampleVisibilitySetParser extends CLIParser
{
	/**
	 * How {@link BioSample} accessions are parsed when setting their visibility. It is composed of the 2 chunks: 
	 * <+|-|-->acc 
	 */
	private static final RegEx SAMPLE_VISIBILITY_SET_SPEC_RE = new RegEx ( "(\\+|\\-|\\-\\-)([^\\s\\+]+)" );

	private AccessControlManager accMgr;

	public SampleVisibilitySetParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * @return the number of samples that were chnaged.
	 */
	public int run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null sample visibility specification)" );
		
		int result = 0;
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = SAMPLE_VISIBILITY_SET_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? true 
				: "-".equals ( specBits [ 1 ] ) ? false 
				: null; // last case is --   
			if ( accMgr.setBioSampleVisibility ( specBits [ 2 ], publicFlag ) ) result++;
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
