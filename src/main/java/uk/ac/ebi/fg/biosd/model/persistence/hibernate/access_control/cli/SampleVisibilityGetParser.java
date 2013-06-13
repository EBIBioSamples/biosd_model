package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class SampleVisibilityGetParser extends CLIParser
{
	private AccessibleDAO<BioSample> sampleDao;
	
	public SampleVisibilityGetParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	
	public List<BioSample> run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null sample visibility specification)" );

		List<BioSample> result = new ArrayList<BioSample> ();

		for ( String acc: cmd.split ( "\\s+" ) )
		{
			BioSample smp = sampleDao.findAndFail ( acc );
			MSIVisibilityGetParser.reportSample ( smp, 1 );
			result.add ( smp );
		}
		return result;
	}

	
	@Override
	public EntityManager getEntityManager ()
	{
		return this.sampleDao.getEntityManager (); 	
	}

	@Override
	public void setEntityManager ( EntityManager entityManager )
	{
		this.sampleDao = new AccessibleDAO<BioSample> ( BioSample.class, entityManager );
	}

}
