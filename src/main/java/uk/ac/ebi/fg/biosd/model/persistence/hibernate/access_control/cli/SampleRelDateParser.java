package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.text.ParseException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;

/**
 * Manages release dates for {@link BioSample}. @see {@link RelDateParser}.
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class SampleRelDateParser extends CLIParser
{
	private AccessControlManager accMgr;

	public SampleRelDateParser ( EntityManager entityManager ) {
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
		
		if ( chunks.length < 2 ) throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );

		for ( int i = 0;  i < chunks.length - 1; i++ )
		{
			String sampleAcc = StringUtils.trimToNull ( chunks [ i ] ), dateStr = StringUtils.trimToNull ( chunks [ ++i ] );
			if ( sampleAcc == null || dateStr == null ) throw new IllegalArgumentException ( 
				"Syntax error in '" + cmd +"'" 
			);
			try {
				Date relDate = DateUtils.parseDate ( dateStr, RelDateParser.DATE_FMTS );
				if ( accMgr.setBioSampleReleaseDate ( sampleAcc, relDate ) ) result++;
			}
			catch ( ParseException ex ) {
				throw new IllegalArgumentException ( "Syntax error in '" + cmd + "': " + ex.getMessage (), ex );
			}
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
