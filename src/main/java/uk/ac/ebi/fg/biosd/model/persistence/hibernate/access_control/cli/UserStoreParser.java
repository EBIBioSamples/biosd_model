package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.UserDAO;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * Manages the 'user modify' command. @see {@link AccessControlCLI}.
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class UserStoreParser extends CLIParser
{
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
	private static final RegEx USER_SPEC_ATTR_RE = new RegEx ( 
		"\\s*--(email|name|surname|password|notes)\\s*\\=", 
		Pattern.CASE_INSENSITIVE 
	);
	
	/** @see #USER_SPEC_ATTR_RE. This is the right-hand side of = */
	private static final RegEx USER_SPEC_VAL_RE = new RegEx ( 
		"(\\s*\"(\\\\\"|[^\"])*[\\\\]{0}\"\\s*|\\s*'(\\\\'|[^'])*[\\\\]{0}'\\s*|(\\\\\"|\\\\'|[^\"'])*)"
	);
	
	private AccessControlManager accMgr;
	private UserDAO userDao;
	

	public UserStoreParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * <pre>user modify user --email|--name|--surname|--password|--notes = value ...</pre>
	 * 
	 * @return the modified user
	 */
	public User run ( String cmd, boolean isNewUser )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error, null user description" );
		
		Map<String, String> attrs = parseUserAttributes ( cmd );
		log.debug ( "user attributes:\n" + attrs );

		String email = attrs.get ( "email" );
		String pwd = attrs.get ( "password" );
		
		if ( email == null ) throw new IllegalArgumentException ( "Syntax error in user specification (null email)" );
		if ( isNewUser && pwd == null ) throw new IllegalArgumentException ( "Syntax error in user specification (null password)" );
		
		email = email.toLowerCase ();
		
		User user = new User ( 
			email, attrs.get ( "name" ), attrs.get ( "surname" ), User.hashPassword ( pwd ), attrs.get ( "notes" ) 
		);
		
		log.debug ( "saving user: " + user );
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
			if ( isNewUser ) userDao.create ( user );
			else userDao.mergeBean ( user );
		ts.commit ();
		log.debug ( "saved" );
		return user;
	}
	
	public static Map<String, String> parseUserAttributes ( String attrSpec )
	{
		List<String> foundAttrs = new ArrayList<String> ();
		List<Integer> valIdxs = new ArrayList<Integer> ();
		
		// Reconstruct the positions of the --attr=value pair
		boolean isFirst = true;
		for ( Matcher matcher = USER_SPEC_ATTR_RE.matcher ( attrSpec ); matcher.find (); )
		{
			if ( matcher.groupCount () < 1 ) throw new IllegalArgumentException ( "Syntax error in '" + attrSpec + "'" );
			String attr = matcher.group ( 1 ).toLowerCase ();

			foundAttrs.add ( attr );
			if ( !isFirst ) valIdxs.add ( matcher.start () ); else isFirst = false;
			valIdxs.add ( matcher.end () );
		}
		
		valIdxs.add ( attrSpec.length () );
		
		Map<String, String> attrs = new HashMap<String, String> ();
		for ( int iAttr = 0; iAttr < foundAttrs.size (); iAttr++ )
		{
			String attr = foundAttrs.get ( iAttr );
			String val = attrSpec.substring ( valIdxs.get ( iAttr * 2 ), valIdxs.get ( iAttr * 2 + 1 ) );
			val = StringUtils.trimToNull ( val );
			if ( val == null ) continue;

			// Check the syntax and strip the quotes away
			
			String valBits[] = USER_SPEC_VAL_RE.groups ( val );
			if ( valBits == null || valBits.length < 2 ) throw new IllegalArgumentException ( 
				"Syntax error in '" + attrSpec + "'"
			);
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
		
		String email = attrs.get ( "email" ); if ( email != null ) attrs.put ( "email", email.toLowerCase () );
		return attrs;
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
		this.userDao = new UserDAO ( entityManager );
	}

}
