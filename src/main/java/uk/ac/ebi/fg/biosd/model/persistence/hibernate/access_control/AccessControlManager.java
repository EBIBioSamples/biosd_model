package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import java.util.Date;

import javax.persistence.EntityManager;

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

		String hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp.id IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg.acc = :sgAcc)", 
			BioSample.class.getName () 
		);
		
		entityManager.createQuery ( hql )
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

		String hql = String.format ( 
			"UPDATE %s smp SET smp.releaseDate = :releaseDate " +
			"WHERE smp.id IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg.acc = :sgAcc)", 
			BioSample.class.getName () 
		);
		
		entityManager.createQuery ( hql )
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
