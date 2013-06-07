package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.text.ParseException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;

/**
 * TODO: Comment me!
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

	public <T> T run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null sample release date specification)" );

		EntityManager em = accMgr.getEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		
		String chunks[] = cmd.split ( "\\s+" );
		
		if ( chunks.length < 2 ) throw new IllegalArgumentException ( 
			"Syntax error in '" + cmd + "'"
		);

		for ( int i = 0;  i < chunks.length - 1; i++ )
		{
			String sampleAcc = StringUtils.trimToNull ( chunks [ i ] ), dateStr = StringUtils.trimToNull ( chunks [ ++i ] );
			if ( sampleAcc == null || dateStr == null ) throw new IllegalArgumentException ( 
				"Syntax error in '" + cmd +"'" 
			);
			try {
				Date relDate = DateUtils.parseDate ( dateStr, RelDateParser.DATE_FMTS );
				accMgr.setBioSampleReleaseDate ( sampleAcc, relDate );
			}
			catch ( ParseException ex ) {
				throw new IllegalArgumentException ( "Syntax error in '" + cmd + "'" );
			}
		}
		ts.commit ();
		return null;
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
