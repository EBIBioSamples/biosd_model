package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.core_model.toplevel.Accessible;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class VisibilityParser extends CLIParser
{
	public static final RegEx CMD_TYPE_RE = new RegEx ( "(submissions|sample-groups|samples)\\s+(.+)", Pattern.CASE_INSENSITIVE );

	private SampleVisibilityParser smpVisibilityParser;
	private SampleGroupVisibilityParser sgVisibilityParser;
	private MSIVisibilitySetParser msiVisibilitySetParser;
	private MSIVisibilityGetParser msiVisibilityGetParser;
	
	public VisibilityParser ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	
	public List<? extends Accessible> run ( String cmd, boolean isSetOrGet )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null visibility command)" );

		String cmdBits[] = CMD_TYPE_RE.groups ( cmd );
		if ( cmdBits == null || cmdBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
			if ( "submissions".equalsIgnoreCase ( cmdBits [ 1 ] ) ) 
				return isSetOrGet 
					? msiVisibilitySetParser.run ( cmdBits [ 2 ] ) 
					: msiVisibilityGetParser.run ( cmdBits [ 2 ] );
			else if ( "sample-groups".equalsIgnoreCase ( cmdBits [ 1 ] ) ) return sgVisibilityParser.run ( cmdBits [ 2 ] );
			else if ( "samples".equalsIgnoreCase ( cmdBits [ 1 ] ) ) return smpVisibilityParser.run ( cmdBits [ 2 ] );
			else throw new RuntimeException ( String.format (  
				"Internal error: unexpected chunk '%s' in visibility command string: '%s'", cmdBits [ 1 ], cmd ));
	}

	@Override
	public EntityManager getEntityManager ()
	{
		return smpVisibilityParser.getEntityManager ();
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		smpVisibilityParser = new SampleVisibilityParser ( entityManager );
		sgVisibilityParser = new SampleGroupVisibilityParser ( entityManager );
		msiVisibilitySetParser = new MSIVisibilitySetParser ( entityManager );
		msiVisibilityGetParser = new MSIVisibilityGetParser ( entityManager );
	}

}
