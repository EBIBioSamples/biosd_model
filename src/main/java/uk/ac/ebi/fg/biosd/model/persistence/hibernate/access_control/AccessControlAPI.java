package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.utils.regex.RegEx;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 7, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlAPI
{
	private AccessControlManager accMgr;
	private UserDAO userDao;
	
	public static final RegEx SAMPLE_GROUP_VISIBILITY_SPEC_RE = new RegEx ( "(\\+|\\-|\\-\\-)([^\\s\\+]+)(\\+\\+)?" );
	public static final RegEx SAMPLE_VISIBILITY_SPEC_RE = new RegEx ( "(\\+|\\-|\\-\\-)([^\\s\\+]+)" );
	public static final RegEx VISIBILITY_CMD_RE = new RegEx ( "(submissions|sample-groups|samples)\\s+(.+)", Pattern.CASE_INSENSITIVE );

	public AccessControlAPI ( EntityManager entityManager )
	{
		this.setEntityManager ( entityManager );
	}
	
	public void setSampleVisibility ( String visibilitySpec ) 
	{
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: visibilitySpec.split ( "\\s" ) )
		{
			String specBits[] = SAMPLE_VISIBILITY_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length <= 3 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? true 
				: "-".equals ( specBits [ 1 ] ) ? false 
				: null; // last case is --   
			accMgr.setBioSampleVisibility ( specBits [ 2 ], publicFlag );
		}
		ts.commit ();
	}

	public void setSampleGroupVisibility ( String visibilitySpec )
	{
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: visibilitySpec.split ( "\\s" ) )
		{
			String specBits[] = SAMPLE_GROUP_VISIBILITY_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length <= 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? true 
				: "-".equals ( specBits [ 1 ] ) ? false 
				: null; // last case is --   
			accMgr.setBioSampleGroupVisibility ( specBits [ 2 ], publicFlag, "++".equals ( specBits [ 3 ] ) );
		}
		ts.commit ();
	}
	
	public void setMSIVisibility ( String visibilitySpec )
	{
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: visibilitySpec.split ( "\\s" ) )
		{
			String specBits[] = SAMPLE_GROUP_VISIBILITY_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length <= 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? true 
				: "-".equals ( specBits [ 1 ] ) ? false 
				: null; // last case is --   
			accMgr.setMSIVisibility ( specBits [ 2 ], publicFlag, "++".equals ( specBits [ 3 ] ) );
		}
		ts.commit ();
	}
	
	
	public void setVisibility ( String visibilityCommand )
	{
		String cmdBits[] = VISIBILITY_CMD_RE.groups ( visibilityCommand );
		if ( cmdBits == null || cmdBits.length <= 3 ) throw new IllegalArgumentException ( "Syntax error in '" + visibilityCommand + "'" );
			if ( "submissions".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setMSIVisibility ( cmdBits [ 2 ] );
			else if ( "sample-groups".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setSampleGroupVisibility ( cmdBits [ 2 ] );
			else if ( "samples".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setSampleVisibility ( cmdBits [ 2 ] );
			else throw new RuntimeException ( String.format (  
				"Internal error: unexpected chunk '%s' in visibility command string: '%s'", cmdBits [ 1 ], visibilityCommand ));
	}
	
	
	public EntityManager getEntityManager ()
	{
		return accMgr.getEntityManager ();
	}

	public void setEntityManager ( EntityManager entityManager )
	{
		accMgr = new AccessControlManager ( entityManager );
		userDao = new UserDAO ( entityManager );
	}

}
