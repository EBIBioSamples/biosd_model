package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.text.ParseException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * Manages release dates for {@link BioSampleGroup}. @see {@link RelDateParser}.
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class SampleGroupRelDateParser extends CLIParser
{
	static final RegEx SAMPLE_GROUP_REL_DATE_SPEC_RE = new RegEx ( "(\\-\\-|[0-9,\\-]+)(\\+\\+)?" );

	private AccessControlManager accMgr;

	public SampleGroupRelDateParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	public int run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null sample release date specification)" );

		int result = 0;
		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		
		String chunks[] = cmd.split ( "\\s+" );
		
		if ( chunks.length < 2 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'"	);
		
		for ( int i = 0;  i < chunks.length - 1; i++ )
		{
			String sgAcc = StringUtils.trimToNull ( chunks [ i ] ), dateStr = StringUtils.trimToNull ( chunks [ ++i ] );
			if ( sgAcc == null || dateStr == null ) throw new IllegalArgumentException ( 
				"Syntax error in '" + cmd +"'" 
			);

			String dateBits[] = SAMPLE_GROUP_REL_DATE_SPEC_RE.groups ( dateStr );
			if ( dateBits == null || dateBits.length < 3 ) throw new IllegalArgumentException (
				"Syntax error in '" + cmd + "'"
			);

			Date relDate = null;
			try {
				if ( !"--".equals ( dateBits [ 1 ] ) )
					relDate = DateUtils.parseDate ( dateBits [ 1 ], RelDateParser.DATE_FMTS );
			}
			catch ( ParseException ex ) {
				throw new IllegalArgumentException ( "Syntax error in '" + cmd + "': " + ex.getMessage (), ex );
			}
			
			result += accMgr.setBioSampleGroupReleaseDate ( sgAcc, relDate, "++".equals ( dateBits [ 2 ] ) );

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
