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
 * <dl><dt>date</dt><dd>May 7, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlCLI extends CLIParser
{
	private static final RegEx CMDS_RE = new RegEx ( "(set|get) (visibility|release-date)\\s+(.+)", Pattern.CASE_INSENSITIVE );
	
	private VisibilityParser visibilityParser;
	private RelDateParser relDateParser;
	
	
	
	public AccessControlCLI ( EntityManager entityManager ) {
		super ( entityManager );
	}
		
	public List<? extends Accessible> run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Null command" );
		String cmdBits[] = CMDS_RE.groups ( cmd );
		if ( cmdBits == null || cmdBits.length < 4 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
		
		boolean isSetOrGet = "set".equalsIgnoreCase ( cmdBits [ 1 ] );
		String attrType = cmdBits [ 2 ]; // visibility|release-date
		String accSpec = cmdBits [ 3 ];
		
		if ( !isSetOrGet ) attrType = "visibility";
		if ( "visibility".equalsIgnoreCase ( attrType ) ) return visibilityParser.run ( accSpec, isSetOrGet );
		else return relDateParser.run ( accSpec ); // release-date
	}
	
	
	public EntityManager getEntityManager ()
	{
		return visibilityParser.getEntityManager ();
	}

	public void setEntityManager ( EntityManager entityManager )
	{
		visibilityParser = new VisibilityParser ( entityManager );
		relDateParser = new RelDateParser ( entityManager );
	}

}
