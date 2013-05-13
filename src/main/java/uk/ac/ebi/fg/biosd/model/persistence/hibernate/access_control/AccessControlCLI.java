package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 7, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlCLI
{
	private AccessControlManager accMgr;
	private UserDAO userDao;
	
	public static final RegEx CMDS_RE = new RegEx ( "(set) (visibility|release-date)\\s+(.+)", Pattern.CASE_INSENSITIVE );
	
	public static final RegEx CMD_TYPE_RE = new RegEx ( "(submissions|sample-groups|samples)\\s+(.+)", Pattern.CASE_INSENSITIVE );
	
	public static final RegEx SAMPLE_GROUP_VISIBILITY_SPEC_RE = new RegEx ( "(\\+|\\-\\-|\\-)([^\\s\\+]+)(\\+\\+)?" );
	public static final RegEx SAMPLE_VISIBILITY_SPEC_RE = new RegEx ( "(\\+|\\-|\\-\\-)([^\\s\\+]+)" );

	public static final String DATE_FMTS[] =  new String[] { "yyyyMMdd'-'HHmmss", "yyyyMMdd" };
	public static final RegEx SAMPLE_GROUP_REL_DATE_SPEC_RE = new RegEx ( "(\\-\\-|[0-9,\\-]+)(\\+\\+)?" );
	
	/**
	 * <pre> 
	 * --(email|name|surname|password|notes)\s*\=(\s*"(\\"|[^"])*[\\]{0}"\s*|\s*'(\\'|[^'])*[\\]{0}'\s*|(\\"|\\'|[^"'])*)
	 * </pre>
	 *                                                          1                          2                  3
	 * 1) "*", no doubles inside, except \"<br/>
	 * 2) '*', no single inside, except \'<br/>
	 * 3) unquoted, no quotes inside unless escaped<br/>
	 * 
	 * <p>Group 1 = attribute, 2 = value.</p>
	 * <p>This is about the right-hand side of =, the next is about the lvalue</
	 */
	public static final RegEx USER_SPEC_ATTR_RE = new RegEx ( 
		"\\s*--(email|name|surname|password|notes)\\s*\\=", 
		Pattern.CASE_INSENSITIVE 
	);
	
	/** @see #USER_SPEC_ATTR_RE. This is the right-hand side of = */
	public static final RegEx USER_SPEC_VAL_RE = new RegEx ( 
		"(\\s*\"(\\\\\"|[^\"])*[\\\\]{0}\"\\s*|\\s*'(\\\\'|[^'])*[\\\\]{0}'\\s*|(\\\\\"|\\\\'|[^\"'])*)"
	);

	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	
	public AccessControlCLI ( EntityManager entityManager )
	{
		this.setEntityManager ( entityManager );
	}
		
	public void run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Null command" );
		String cmdBits[] = CMDS_RE.groups ( cmd );
		if ( cmdBits == null || cmdBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
		if ( "set".equalsIgnoreCase ( cmdBits [ 1 ] ) )
		{
			if ( "visibility".equalsIgnoreCase ( cmdBits [ 2 ] ) ) setVisibility ( cmdBits [ 3 ] );
			else if ( "release-date".equalsIgnoreCase ( cmdBits [ 2 ] ) ) setReleaseDate ( cmdBits [ 3 ] );
			else throw new RuntimeException ( String.format (  
				"Internal error: unexpected chunk '%s' in release date command string: '%s'", cmdBits [ 2 ], cmd )
			);
		}
		// TODO: get
		else throw new RuntimeException ( String.format (  
			"Internal error: unexpected chunk '%s' in release date command string: '%s'", cmdBits [ 2 ], cmd )
		);
	}
	
	
	
	private void setVisibility ( String visibilityCommand )
	{
		visibilityCommand = StringUtils.trimToNull ( visibilityCommand );
		if ( visibilityCommand == null ) throw new IllegalArgumentException ( "Syntax error (null visibility command)" );

		String cmdBits[] = CMD_TYPE_RE.groups ( visibilityCommand );
		if ( cmdBits == null || cmdBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + visibilityCommand + "'" );
			if ( "submissions".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setMSIVisibility ( cmdBits [ 2 ] );
			else if ( "sample-groups".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setSampleGroupVisibility ( cmdBits [ 2 ] );
			else if ( "samples".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setSampleVisibility ( cmdBits [ 2 ] );
			else throw new RuntimeException ( String.format (  
				"Internal error: unexpected chunk '%s' in visibility command string: '%s'", cmdBits [ 1 ], visibilityCommand ));
	}
	
	private void setSampleVisibility ( String visibilitySpec ) 
	{
		visibilitySpec = StringUtils.trimToNull ( visibilitySpec );
		if ( visibilitySpec == null ) throw new IllegalArgumentException ( "Syntax error (null sample visibility specification)" );

		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: visibilitySpec.split ( "\\s+" ) )
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
	}

	private void setSampleGroupVisibility ( String visibilitySpec )
	{
		visibilitySpec = StringUtils.trimToNull ( visibilitySpec );
		if ( visibilitySpec == null ) throw new IllegalArgumentException ( "Syntax error (null sample-group visibility specification)" );
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: visibilitySpec.split ( "\\s+" ) )
		{
			String specBits[] = SAMPLE_GROUP_VISIBILITY_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? new Boolean ( true )
				: "-".equals ( specBits [ 1 ] ) ? new Boolean ( false ) 
				: null; // last case is --   
			accMgr.setBioSampleGroupVisibility ( specBits [ 2 ], publicFlag, "++".equals ( specBits [ 3 ] ) );
		}
		ts.commit ();
	}
	
	private void setMSIVisibility ( String visibilitySpec )
	{
		visibilitySpec = StringUtils.trimToNull ( visibilitySpec );
		if ( visibilitySpec == null ) throw new IllegalArgumentException ( "Syntax error (null submission visibility specification)" );
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		for ( String singleSpec: visibilitySpec.split ( "\\s+" ) )
		{
			String specBits[] = SAMPLE_GROUP_VISIBILITY_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );
			Boolean publicFlag = 
				"+".equals ( specBits [ 1 ] ) ? new Boolean ( true ) 
				: "-".equals ( specBits [ 1 ] ) ? new Boolean ( false ) 
				: null; // last case is --   
			accMgr.setMSIVisibility ( specBits [ 2 ], publicFlag, "++".equals ( specBits [ 3 ] ) );
		}
		ts.commit ();
	}
	
	
	
	
	private void setReleaseDate ( String relDateCommand )
	{
		relDateCommand = StringUtils.trimToNull ( relDateCommand );
		if ( relDateCommand == null ) throw new IllegalArgumentException ( "Syntax error (null visibility command)" );

		String cmdBits[] = CMD_TYPE_RE.groups ( relDateCommand );
		if ( cmdBits == null || cmdBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + relDateCommand + "'" );
			if ( "submissions".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setMSIReleaseDate ( cmdBits [ 2 ] );
			else if ( "sample-groups".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setSampleGroupReleaseDate ( cmdBits [ 2 ] );
			else if ( "samples".equalsIgnoreCase ( cmdBits [ 1 ] ) ) setSampleReleaseDate ( cmdBits [ 2 ] );
			else throw new RuntimeException ( String.format (  
				"Internal error: unexpected chunk '%s' in release date command string: '%s'", cmdBits [ 1 ], relDateCommand ));
	}
	
	
	private void setSampleReleaseDate ( String dateSpec ) 
	{
		dateSpec = StringUtils.trimToNull ( dateSpec );
		if ( dateSpec == null ) throw new IllegalArgumentException ( "Syntax error (null sample release date specification)" );

		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		
		String chunks[] = dateSpec.split ( "\\s+" );
		
		if ( chunks.length < 2 ) throw new IllegalArgumentException ( 
			"Syntax error in '" + dateSpec + "'"
		);

		for ( int i = 0;  i < chunks.length - 1; i++ )
		{
			String sampleAcc = StringUtils.trimToNull ( chunks [ i ] ), dateStr = StringUtils.trimToNull ( chunks [ ++i ] );
			if ( sampleAcc == null || dateStr == null ) throw new IllegalArgumentException ( 
				"Syntax error in '" + dateSpec +"'" 
			);
			try {
				Date relDate = DateUtils.parseDate ( dateStr, DATE_FMTS );
				accMgr.setBioSampleReleaseDate ( sampleAcc, relDate );
			}
			catch ( ParseException ex ) {
				throw new IllegalArgumentException ( "Syntax error in '" + dateSpec + "'" );
			}
		}
		ts.commit ();
	}
	
	
	private void setSampleGroupReleaseDate ( String dateSpec ) 
	{
		dateSpec = StringUtils.trimToNull ( dateSpec );
		if ( dateSpec == null ) throw new IllegalArgumentException ( "Syntax error (null sample release date specification)" );

		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		
		String chunks[] = dateSpec.split ( "\\s+" );
		
		if ( chunks.length < 2 ) throw new IllegalArgumentException ( 
			"Syntax error in '" + dateSpec + "'"
		);
		
		for ( int i = 0;  i < chunks.length - 1; i++ )
		{
			String sgAcc = StringUtils.trimToNull ( chunks [ i ] ), dateStr = StringUtils.trimToNull ( chunks [ ++i ] );
			if ( sgAcc == null || dateStr == null ) throw new IllegalArgumentException ( 
				"Syntax error in '" + dateSpec +"'" 
			);

			String dateBits[] = SAMPLE_GROUP_REL_DATE_SPEC_RE.groups ( dateStr );
			if ( dateBits == null || dateBits.length < 3 ) throw new IllegalArgumentException (
				"Syntax error in '" + dateSpec + "'"
			);

			Date relDate = null;
			try {
				if ( !"--".equals ( dateBits [ 1 ] ) )
					relDate = DateUtils.parseDate ( dateBits [ 1 ], DATE_FMTS );
			}
			catch ( ParseException ex ) {
				throw new IllegalArgumentException ( "Syntax error in '" + dateSpec + "'" );
			}
			
			accMgr.setBioSampleGroupReleaseDate ( sgAcc, relDate, "++".equals ( dateBits [ 2 ] ) );

		}
		ts.commit ();
	}
	
	
	private void setMSIReleaseDate ( String dateSpec ) 
	{
		dateSpec = StringUtils.trimToNull ( dateSpec );
		if ( dateSpec == null ) throw new IllegalArgumentException ( "Syntax error (null sample release date specification)" );

		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		
		String chunks[] = dateSpec.split ( "\\s+" );
		
		if ( chunks.length < 2 ) throw new IllegalArgumentException ( 
			"Syntax error in '" + dateSpec + "'"
		);
		
		for ( int i = 0;  i < chunks.length - 1; i++ )
		{
			String msiAcc = StringUtils.trimToNull ( chunks [ i ] ), dateStr = StringUtils.trimToNull ( chunks [ ++i ] );
			if ( msiAcc == null || dateStr == null ) throw new IllegalArgumentException ( 
				"Syntax error in '" + dateSpec +"'" 
			);

			String dateBits[] = SAMPLE_GROUP_REL_DATE_SPEC_RE.groups ( dateStr );
			if ( dateBits == null || dateBits.length < 3 ) throw new IllegalArgumentException (
				"Syntax error in '" + dateSpec + "'"
			);

			Date relDate = null ;
			try {
				if ( !"--".equals ( dateBits [ 1 ] ) )
					relDate = DateUtils.parseDate ( dateBits [ 1 ], DATE_FMTS );
			}
			catch ( ParseException ex ) {
				throw new IllegalArgumentException ( "Syntax error in '" + dateSpec + "'" );
			}
			
			accMgr.setMSIReleaseDate ( msiAcc, relDate, "++".equals ( dateBits [ 2 ] ) );

		}
		ts.commit ();
	}
	
	
	/**
	 * TODO:
	 * create user attr = value... 
	 *   attr = name|surname|pass|notes, use \attr to embed it as part of the attr-value
	 * set user ...
	 * set owner <uname> samples|sample-groups|submissions [+|-] acc[++] [acc[++]]...
	 * set -owner 
	 * delete user <uname>
	 * 
	 */
	
	public void createUser ( String userSpec )
	{
		userSpec = StringUtils.trimToNull ( userSpec );
		if ( userSpec == null ) throw new IllegalArgumentException ( "Syntax error, null user description" );
		
		List<String> foundAttrs = new ArrayList<String> ();
		List<Integer> valIdxs = new ArrayList<Integer> ();
		
		// Reconstruct the positions of the --attr=value pair
		boolean isFirst = true;
		for ( Matcher matcher = USER_SPEC_ATTR_RE.matcher ( userSpec ); matcher.find (); )
		{
			if ( matcher.groupCount () < 1 ) throw new IllegalArgumentException ( "Syntax error in '" + userSpec + "'" );
			String attr = matcher.group ( 1 ).toLowerCase ();

			foundAttrs.add ( attr );
			if ( !isFirst ) valIdxs.add ( matcher.start () ); else isFirst = false;
			valIdxs.add ( matcher.end () );
		}
		
		valIdxs.add ( userSpec.length () );
		
		Map<String, String> attrs = new HashMap<String, String> ();
		for ( int iAttr = 0; iAttr < foundAttrs.size (); iAttr++ )
		{
			String attr = foundAttrs.get ( iAttr );
			String val = userSpec.substring ( valIdxs.get ( iAttr * 2 ), valIdxs.get ( iAttr * 2 + 1 ) );
			val = StringUtils.trimToNull ( val );
			if ( val == null ) continue;

			// Check the syntax and strip the quotes away
			
			String valBits[] = USER_SPEC_VAL_RE.groups ( val );
			if ( valBits == null || valBits.length < 2 ) throw new IllegalArgumentException ( "Syntax error in " + userSpec );
			val = StringUtils.trimToNull ( valBits [ 1 ] );
			if ( val == null ) continue;
			char char0 = val.charAt ( 0 );
			if ( char0 == '\'' || char0 == '\"' ) {
				val = val.substring ( 1, val.length () - 1 );
				if ( val == null ) continue;
			}
			// Eventually the value
			attrs.put ( attr, val );
		}
		
		log.debug ( "user attributes:\n" + attrs );
		String email = attrs.get ( "email" );
		if ( email == null ) throw new IllegalArgumentException ( "Syntax error in user specification (null email)" );
		
		String pwd = attrs.get ( "password" );
		if ( pwd == null ) throw new IllegalArgumentException ( "Syntax error in user specification (null password)" );
		
		User user = new User ( 
			email, attrs.get ( "name" ), attrs.get ( "surname" ), User.hashPassword ( pwd ), attrs.get ( "notes" ) 
		);
		
		log.debug ( "saving user: " + user );
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		  userDao.create ( user );
		ts.commit ();
		log.debug ( "saved" );
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
