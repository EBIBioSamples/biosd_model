package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.UserDAO;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * Manages commands to set entitity's owners. @see {@link AccessControlCLI}.
 *
 * <dl><dt>date</dt><dd>May 30, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class OwnerSetParser extends CLIParser
{
	private static RegEx OWNER_SPEC_RE = new RegEx ( 
		"([^\\s]+)\\s+(samples|sample-groups|submissions)\\s+(.+)", Pattern.CASE_INSENSITIVE 
	);
	private static RegEx ENTITY_SPEC_RE = new RegEx ( "(\\+|\\-|\\=)?([^\\s\\+]+)(\\+\\+)?" );
	
	private UserDAO userDao;
	private AccessControlManager accMgr;

	public OwnerSetParser ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	/**
	 * set owner <uname> samples|sample-groups|submissions [+|-|=] acc[++] [acc[++]]...
	 * - default is +, or = when name == null
	 * - uname == null as a special case (removes all)
	 */
	public int  run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
		
		String cmdBits[] = OWNER_SPEC_RE.groups ( cmd );
		if ( cmdBits == null || cmdBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
		
		String userStr = cmdBits [ 1 ].toLowerCase (), entityTypeStr = cmdBits [ 2 ], entitySpecStr = cmdBits [ 3 ];
				
		String entitiesBits[] = entitySpecStr.split ( "\\s+" );
		if ( entitiesBits == null || entitiesBits.length < 1 ) throw new IllegalArgumentException (
			"Syntax error in '" + cmd + "' (bad entitites specification)" 
		);

		int result = 0;
		
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();

		for ( String singleEntityStr: entitiesBits )
		{
			String singleEntityBits [] = ENTITY_SPEC_RE.groups ( singleEntityStr );
			if ( singleEntityBits == null || singleEntityBits.length < 3 ) throw new IllegalArgumentException (
				"Syntax error in '" + singleEntityStr
			);
			
			String changeTypeStr = singleEntityBits [ 1 ], entityAcc = singleEntityBits [ 2 ], cascadingFlagStr = singleEntityBits [ 3 ];
			if ( changeTypeStr == null ) changeTypeStr = "+";
			
			boolean cascadingFlag = cascadingFlagStr != null;
			if ( "null".equals ( userStr ) ) userStr = null;

			// See which entity type is involved
			if ( "samples".equalsIgnoreCase ( entityTypeStr ) ) 
			{
				if ( cascadingFlagStr != null ) throw new IllegalArgumentException ( 
					"Error with '" + singleEntityStr + "' ('++' is invalid for samples)" 
				);
				// Do it for samples
				if ( userStr == null || "=".equals ( changeTypeStr ) ) result += accMgr.setSampleOwner ( entityAcc, userStr );
				else if ( "+".equals ( changeTypeStr ) ) { if ( accMgr.addSampleOwner ( entityAcc, userStr ) ) result++; }
				else { if ( accMgr.deleteSampleOwner ( entityAcc, userStr ) ) result++; } // it is '-'
			}
			else if ( "sample-groups".equalsIgnoreCase ( entityTypeStr ) ) 
			{
				// Do it for sample-groups
				if ( userStr == null || "=".equals ( changeTypeStr ) ) result += accMgr.setSampleGroupOwner ( entityAcc, userStr, cascadingFlag );
				else if ( "+".equals ( changeTypeStr ) ) result += accMgr.addSampleGroupOwner ( entityAcc, userStr, cascadingFlag );
				else result += accMgr.deleteSampleGroupOwner ( entityAcc, userStr, cascadingFlag ); // it is '-'
			}
			else // it's 'submissions'
			{
				// Do it for sample-groups
				if ( userStr == null || "=".equals ( changeTypeStr ) ) result += accMgr.setMSIOwner ( entityAcc, userStr, cascadingFlag );
				else if ( "+".equals ( changeTypeStr ) ) result += accMgr.addMSIOwner ( entityAcc, userStr, cascadingFlag );
				else result += accMgr.deleteMSIOwner ( entityAcc, userStr, cascadingFlag ); // it is '-'
			}
		}
		ts.commit ();
		return result;
	}
	
	
	
	
	@Override
	public EntityManager getEntityManager ()
	{
		return userDao.getEntityManager ();
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		userDao = new UserDAO ( entityManager );
		accMgr = new AccessControlManager ( entityManager );
	}

}
