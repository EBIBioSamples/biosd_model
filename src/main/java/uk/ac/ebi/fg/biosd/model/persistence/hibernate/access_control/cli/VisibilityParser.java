package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * Works out visibility commands (set|get visibility ...) and dispatches to sub-parsers:
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class VisibilityParser extends CLIParser
{
	public static final RegEx CMD_TYPE_RE = new RegEx ( "(submissions|sample-groups|samples)\\s+(.+)", Pattern.CASE_INSENSITIVE );

	/**
	 * How {@link MSI} and {@link BioSampleGroup} accessions are parsed when setting their visibility.
	 * It has 3 chunks: +|--|- accession [++].
	 * 
	 * @see {@link AccessControlCLI#getSyntax()}
	 * 
	 */
	static final RegEx SAMPLE_GROUP_VISIBILITY_SET_SPEC_RE = new RegEx ( "(\\+|\\-\\-|\\-)([^\\s\\+]+)(\\+\\+)?" );

	/**
	 * How {@link MSI} and {@link BioSampleGroup} accessions are parsed when retrieving their visibility.
	 * It has 2 chunks: accession [++].
	 * 
	 * @see {@link AccessControlCLI#getSyntax()}
	 */
	static final RegEx SAMPLE_GROUP_VISIBILITY_GET_SPEC_RE = new RegEx ( "([^\\s\\+]+)(\\+\\+)?" );

	private SampleVisibilitySetParser smpVisibilitySetParser;
	private SampleVisibilityGetParser smpVisibilityGetParser;
	private SampleGroupVisibilitySetParser sgVisibilitySetParser;
	private SampleGroupVisibilityGetParser sgVisibilityGetParser;
	private MSIVisibilitySetParser msiVisibilitySetParser;
	private MSIVisibilityGetParser msiVisibilityGetParser;
	
	public VisibilityParser ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	/**
	 * <pre>set|get visibility submissions|sample-groups|samples [+|-|--]acc[++]...</pre>
	 */
	public Object run ( String cmd, boolean isSetOrGet )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null visibility command)" );

		String cmdBits[] = CMD_TYPE_RE.groups ( cmd );
		if ( cmdBits == null || cmdBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
			if ( "submissions".equalsIgnoreCase ( cmdBits [ 1 ] ) ) 
				return isSetOrGet 
					? msiVisibilitySetParser.run ( cmdBits [ 2 ] ) 
					: msiVisibilityGetParser.run ( cmdBits [ 2 ] );
			else if ( "sample-groups".equalsIgnoreCase ( cmdBits [ 1 ] ) ) 
				return isSetOrGet
					? sgVisibilitySetParser.run ( cmdBits [ 2 ] )
					: sgVisibilityGetParser.run ( cmdBits [ 2 ] );
			else if ( "samples".equalsIgnoreCase ( cmdBits [ 1 ] ) ) 
				return isSetOrGet
					? smpVisibilitySetParser.run ( cmdBits [ 2 ] )
					: smpVisibilityGetParser.run ( cmdBits [ 2 ] );
			else throw new RuntimeException ( String.format (  
				"Internal error: unexpected chunk '%s' in visibility command string: '%s'", cmdBits [ 1 ], cmd ));
	}

	@Override
	public EntityManager getEntityManager ()
	{
		return smpVisibilitySetParser.getEntityManager ();
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		smpVisibilitySetParser = new SampleVisibilitySetParser ( entityManager );
		smpVisibilityGetParser = new SampleVisibilityGetParser ( entityManager );
		sgVisibilitySetParser = new SampleGroupVisibilitySetParser ( entityManager );
		sgVisibilityGetParser = new SampleGroupVisibilityGetParser ( entityManager );
		msiVisibilitySetParser = new MSIVisibilitySetParser ( entityManager );
		msiVisibilityGetParser = new MSIVisibilityGetParser ( entityManager );
	}

}
