package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import java.util.Date;
import java.util.Formatter;

import javax.persistence.EntityManager;

import org.hibernate.annotations.common.util.impl.Log;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * An access control manager, for managing entity ownership and visibility (public/private, release date). 
 *
 * <dl><dt>date</dt><dd>Mar 30, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessControlManager
{
	private EntityManager entityManager;
	private AccessibleDAO<BioSample> sampleDao;
	private AccessibleDAO<BioSampleGroup> sgDao;
	
	public AccessControlManager ( EntityManager entityManager )
	{
		super ();
		this.setEntityManager ( entityManager );
	}
	
	/**
	 * Changes the visibility of a bio-sample.  
	 */
	public void changeBioSampleVisibility ( String sampleAcc, Boolean publicFlag ) 
	{
		BioSample sample = sampleDao.find ( sampleAcc );
		if ( sample == null ) throw new RuntimeException ( "Sample '" + sampleAcc + "' not found" );
		sample.setPublicFlag ( publicFlag );
	}

	/**
	 * Changes the release date for a sample 
	 */
	public void changeBioSampleReleaseDate ( String sampleAcc, Date releaseDate ) 
	{
		BioSample sample = sampleDao.find ( sampleAcc );
		if ( sample == null ) throw new RuntimeException ( "Sample '" + sampleAcc + "' not found" );
		sample.setReleaseDate ( releaseDate );
	}

	
	/**
	 * Changes the visibility for a sample group and optionally cascades the operation to all the samples linked to 
	 * the group.
	 */
	public void changeBioSampleGroupVisibility ( String sgAcc, Boolean publicFlag, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.find ( sgAcc );
		if ( sg == null ) throw new RuntimeException ( "Sample '" + sgAcc + "' not found" );
		sg.setPublicFlag ( publicFlag );
		if ( !isCascaded ) return;

		// TODO: it seems complex UPDATEs are not possible in HQL, use org.hibernate.annotations.common.reflection.java.JavaReflectionManager
		// to gather SQL mappings.
		//
		entityManager.createNativeQuery ( 
			"UPDATE bio_product SET public_flag = :publicFlag WHERE product_type = 'bio_sample' AND id IN " +
			"  (SELECT sample_id FROM bio_sample_sample_group br JOIN bio_sample_group sg ON br.group_id = sg.id WHERE sg.acc = :sgAcc)" )
			.setParameter ( "publicFlag", publicFlag )
			.setParameter ( "sgAcc", sgAcc )
			.executeUpdate ();
	}
	
	/**
	 * Changes the release date for a sample group and optionally cascades the operation to all the samples linked to 
	 * the group.
	 */
	public void changeBioSampleGroupReleaseDate ( String sgAcc, Date releaseDate, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.find ( sgAcc );
		if ( sg == null ) throw new RuntimeException ( "Sample '" + sgAcc + "' not found" );
		sg.setReleaseDate ( releaseDate );
		if ( !isCascaded ) return;

		entityManager.createNativeQuery ( 
			"UPDATE bio_product SET release_date = :releaseDate WHERE product_type = 'bio_sample' AND id IN " +
			"  (SELECT sample_id FROM bio_sample_sample_group br JOIN bio_sample_group sg ON br.group_id = sg.id WHERE sg.acc = :sgAcc)" )
			.setParameter ( "releaseDate", releaseDate ) 
			.setParameter ( "sgAcc", sgAcc )
			.executeUpdate ();
	}

	
	public EntityManager getEntityManager ()
	{
		return entityManager;
	}

	public void setEntityManager ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
		sampleDao = new AccessibleDAO<BioSample> ( BioSample.class, entityManager );
		sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, entityManager );
	}
	
}
