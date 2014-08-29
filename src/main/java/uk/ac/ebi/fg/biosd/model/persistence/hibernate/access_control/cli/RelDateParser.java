package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.utils.regex.RegEx;

/**
 * Manages the commands about the release date. @see {@link AccessControlCLI}.  
 * 
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class RelDateParser extends CLIParser
{
	protected static final String DATE_FMTS[] =  new String[] { "yyyyMMdd'-'HHmmss", "yyyyMMdd" };
	
	private static final RegEx CMD_TYPE_RE = new RegEx ( "(submissions|sample-groups|samples)\\s+(.+)", Pattern.CASE_INSENSITIVE );

	private SampleRelDateParser smpRelDateParser;
	private SampleGroupRelDateParser sgRelDateParser;
	private MSIRelDateParser msiRelDateParser;
	
	public RelDateParser ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	/**
	 * <pre>set release-date submissions|sample-groups|samples acc --|date[++] acc...</pre>
	 * @return the number of changed entities.
	 * 
	 */
	public int run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null visibility command)" );

		String cmdBits[] = CMD_TYPE_RE.groups ( cmd );
		if ( cmdBits == null || cmdBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
			if ( "submissions".equalsIgnoreCase ( cmdBits [ 1 ] ) ) return msiRelDateParser.run ( cmdBits [ 2 ] );
			else if ( "sample-groups".equalsIgnoreCase ( cmdBits [ 1 ] ) ) return sgRelDateParser.run ( cmdBits [ 2 ] );
			else if ( "samples".equalsIgnoreCase ( cmdBits [ 1 ] ) ) return smpRelDateParser.run ( cmdBits [ 2 ] );
			else throw new RuntimeException ( String.format (  
				"Internal error: unexpected chunk '%s' in visibility command string: '%s'", cmdBits [ 1 ], cmd ));
	}

	@Override
	public EntityManager getEntityManager ()
	{
		return null;
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		smpRelDateParser = new SampleRelDateParser ( entityManager );
		sgRelDateParser = new SampleGroupRelDateParser ( entityManager );
		msiRelDateParser = new MSIRelDateParser ( entityManager );
	}

}
