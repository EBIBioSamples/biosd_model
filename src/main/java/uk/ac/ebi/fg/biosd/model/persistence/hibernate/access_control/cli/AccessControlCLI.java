package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import static java.lang.System.out;

import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.utils.regex.RegEx;


/**
 * The access control API. This allows to manipulate users and entity permissions in the BioSD database. The access is 
 * to such features is unrestricted, since, in our simple editing model, we have a small group of editors, with writing 
 * permissions at Oracle level.
 * 
 * @see #getSyntax() for details.
 *
 * <dl><dt>date</dt><dd>May 7, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlCLI extends CLIParser
{
	/**
	 * General syntax for the CLI. The RE splits the input into the 3 chunks set|get|create|... visibility|user|... accessions, 
	 * then it's the code into {@link #run(String)} that ensures the first two chunks go well together and dispatches to sub-commands
	 * accordingly.
	 * 
	 */
	private static final RegEx CMDS_RE = new RegEx ( 
		"(set|get|search|create|modify|delete)\\s+(visibility|release-date|user|owner)\\s+(.+)", Pattern.CASE_INSENSITIVE );
	
	private VisibilityParser visibilityParser;
	private RelDateParser relDateParser;
	private UserSearchParser userSearchParser;
	private UserStoreParser userStoreParser;
	private UserDeleteParser userDeleteParser;
	private OwnerSetParser ownerSetParser;
	
	
	public AccessControlCLI ( EntityManager entityManager ) {
		super ( entityManager );
	}
 
	/**
	 * See the text returned by this method for details about the CLI syntax. See also JUnit tests.
	 */
	public static String getSyntax ()
	{
		return 
			"\tset|get visibility submissions|sample-groups|samples [+|-|--]acc[++] ...\n" +
		  "\t  sets/gets the items visibility, + = public, - = private, -- = null (let the release date to decide),\n" +
		  "\t  ++ = for submissions or sample groups, cascade the same permission to contained entities (e.g., from submissions to sample groups and samples)\n" +
		  
			"\n\tset release-date submissions|sample-groups|samples acc --|date[++] acc ...\n" +
		  "\t  sets item release dates, -- = null (let the visibility flag to decide), ++ cascades as above\n" +
			"\t  date format is: yyyyMMdd[-HHmmss]\n" +
			
			"\n\tset owner email|null samples|sample-groups|submissions +|-|=acc[++] ...\n" + 
			"\t  sets entities owner, + = adds to existing owners, - removes from existing owners, = sets exactly that owner, ++ = cascades as above\n" +
			"\t  email = null and '='  to remove all the owners\n" +
			
			"\n\tsearch|create|modify user --email|--name|--surname|--password|--notes = value ...\n" +
			"\t  searches (partial match) users, create a new user, changes attributes for existing users\n" + 
			
			"\n\tdelete user email[++]\n" + 
			"\t  removes a user from the database, ++ to removes links to the entities it owns (instead of raising an error)\n";
	}
	
	/**
	 * Parses according to {@link #getSyntax()} and dispatches to sub-parsers.
	 */
	public Object run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Null command" );
		String cmdBits[] = CMDS_RE.groups ( cmd );
		if ( cmdBits == null || cmdBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
		
		String mainCmd = cmdBits [ 1 ];
		String subCmd = cmdBits [ 2 ];
		String params = cmdBits [ 3 ];
		
		Object result = null;
		boolean isSyntaxError = false;
		
		if ( subCmd.equalsIgnoreCase ( "visibility" ) )
		{
			if ( "set".equalsIgnoreCase ( mainCmd ) ) result = visibilityParser.run ( params, true );
			else if ( "get".equalsIgnoreCase ( mainCmd ) ) result = visibilityParser.run ( params, false );
			else isSyntaxError = true;
		}
		else if ( subCmd.equalsIgnoreCase ( "release-date" ) )
		{
			if ( "set".equalsIgnoreCase ( mainCmd ) ) result = relDateParser.run ( params );
			else isSyntaxError = true;
		}
		else if ( subCmd.equalsIgnoreCase ( "user" ) ) 
		{
			if ( "search".equalsIgnoreCase ( mainCmd ) ) result = userSearchParser.run ( params );
			else if ( "create".equalsIgnoreCase ( mainCmd ) ) result = userStoreParser.run ( params, true ); 
			else if ( "modify".equalsIgnoreCase ( mainCmd ) ) result = userStoreParser.run ( params, false );
			else if ( "delete".equalsIgnoreCase ( mainCmd ) ) result = userDeleteParser.run ( params );
			else isSyntaxError = true;
		}
		else if ( subCmd.equalsIgnoreCase ( "owner" ) ) 
		{
			if ( "set".equalsIgnoreCase ( mainCmd ) ) result = ownerSetParser.run ( params );
			else isSyntaxError = true;
		}
		else isSyntaxError = true;
		
		if ( isSyntaxError )
			// None of the combinations above matched, syntax error
			throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
		
		// Report about the result
		out.println ( "\nDone!" );
		
		// All the rest is already reported by the underline interpreters
		if ( result instanceof Integer ) out.println ( "\n  " + result + " modified items"  );
		
		return result;
	}
	
	
	
	public EntityManager getEntityManager ()
	{
		return visibilityParser.getEntityManager ();
	}

	public void setEntityManager ( EntityManager entityManager )
	{
		visibilityParser = new VisibilityParser ( entityManager );
		relDateParser = new RelDateParser ( entityManager );
		userSearchParser = new UserSearchParser ( entityManager );
		userStoreParser = new UserStoreParser ( entityManager );
		userDeleteParser = new UserDeleteParser ( entityManager );
		ownerSetParser = new OwnerSetParser ( entityManager );
	}

}
