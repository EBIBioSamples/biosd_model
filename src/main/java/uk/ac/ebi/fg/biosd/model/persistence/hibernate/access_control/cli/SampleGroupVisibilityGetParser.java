package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>May 23, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
class SampleGroupVisibilityGetParser extends CLIParser
{
	private AccessControlManager accMgr;
	private AccessibleDAO<BioSampleGroup> sgDao;

	public SampleGroupVisibilityGetParser ( EntityManager entityManager ) {
		super ( entityManager );
	}

	public List<BioSampleGroup> run ( String cmd )
	{
		cmd = StringUtils.trimToNull ( cmd );
		if ( cmd == null ) throw new IllegalArgumentException ( "Syntax error (null sample-group visibility specification)" );
		
		List<BioSampleGroup> result = new ArrayList<BioSampleGroup> ();
		
		for ( String singleSpec: cmd.split ( "\\s+" ) )
		{
			String specBits[] = VisibilityParser.SAMPLE_GROUP_VISIBILITY_GET_SPEC_RE.groups ( singleSpec );
			if ( specBits == null || specBits.length < 3 ) throw new IllegalArgumentException ( "Syntax error in '" + singleSpec +"'" );

			String acc = specBits [ 1 ];
			boolean isCascaded = "++".equals ( specBits [ 2 ] );

			BioSampleGroup sg = sgDao.findAndFail ( acc );
			result.add ( sg );
			
			MSIVisibilityGetParser.reportSampleGroup ( sg, isCascaded, 1 );
		}
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
		this.sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, entityManager );
	}

}
