package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;

/**
 * Allows to set the visibility (public/private flag) of SampleTab submissions (and possibly the objects it contains).
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class MSIVisibilitySetParser extends CLIParser
{
	private AccessControlManager accMgr;

	public MSIVisibilitySetParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * Splits the input into spaced chunks (using \s+), then uses {@link VisibilityParser#SAMPLE_GROUP_VISIBILITY_SET_SPEC_RE}
	 * to match accession specifications and set the corresponding visibility.
	 * 
	 * @return the number of entities that were changed.
	 */
	public int run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null submission visibility specification)" );
		
		int result = 0; 
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = VisibilityParser.SAMPLE_GROUP_VISIBILITY_SET_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			
			String acc = specBits [ 2 ]; 
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? new Boolean ( true ) 
				: "-".equals ( specBits [ 1 ] ) ? new Boolean ( false ) 
				: null; // last case is --
			boolean isCascaded = "++".equals ( specBits [ 3 ] );
				
			result += accMgr.setMSIVisibility ( acc, publicFlag, isCascaded );
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
