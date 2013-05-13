package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import java.util.Date;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * An access control manager, for managing entity ownership and visibility (public/private, release date).
 * Note this object doesn't deal with transactions, you have to start/commit transactions on your own. 
 * 
 * The {@link AccessControlCLI} does that instead.
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
	private AccessibleDAO<MSI> msiDao;
	
	
	public AccessControlManager ( EntityManager entityManager )
	{
		super ();
		this.setEntityManager ( entityManager );
	}
	
	/**
	 * Changes the visibility of a bio-sample.  
	 */
	public void setBioSampleVisibility ( String sampleAcc, Boolean publicFlag ) 
	{
		BioSample sample = sampleDao.find ( sampleAcc );
		if ( sample == null ) throw new RuntimeException ( "Sample '" + sampleAcc + "' not found" );
		sample.setPublicFlag ( publicFlag );
	}

	/**
	 * Changes the release date for a sample 
	 */
	public void setBioSampleReleaseDate ( String sampleAcc, Date releaseDate ) 
	{
		BioSample sample = sampleDao.find ( sampleAcc );
		if ( sample == null ) throw new RuntimeException ( "Sample '" + sampleAcc + "' not found" );
		sample.setReleaseDate ( releaseDate );
	}

	
	/**
	 * Changes the visibility for a sample group and optionally cascades the operation to all the samples linked to 
	 * the group.
	 */
	public void setBioSampleGroupVisibility ( String sgAcc, Boolean publicFlag, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.find ( sgAcc );
		if ( sg == null ) throw new RuntimeException ( "Sample '" + sgAcc + "' not found" );
		sg.setPublicFlag ( publicFlag );
		if ( !isCascaded ) return;

		// PLEASE NOTE, do not say WHERE smp.*id* IN ... this is an Hibernate problem, the intuitive form doesn't work, this
		// not-so-good form is auto-translated into id comparison.
		// 
		String hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg.acc = :sgAcc)", 
			BioSample.class.getName () 
		);
		
		entityManager.createQuery ( hql )
			.setParameter ( "publicFlag", publicFlag )
			.setParameter ( "sgAcc", sgAcc )
			.executeUpdate ();
	}
	
	/**
	 * Changes the visibility for all the sample groups and samples linked to a submission. If isCascaded is true, cascades 
	 * from all the sample groups to the the samples linked to them.
	 */
	public void setMSIVisibility ( String msiAcc, Boolean publicFlag, boolean isCascaded )
	{
		MSI msi = msiDao.find ( msiAcc );
		if ( msi == null ) throw new RuntimeException ( "Submission '" + msiAcc + "' not found" );

		String hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.MSIs msi WHERE msi.acc = :msiAcc)", 
			BioSample.class.getName () 
		);
		
		entityManager.createQuery ( hql )
		  .setParameter ( "publicFlag", publicFlag )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();

		hql = String.format ( 
			"UPDATE %s sg SET sg.publicFlag = :publicFlag " +
			"WHERE sg IN (SELECT sg1.id FROM %1$s sg1 JOIN sg1.MSIS msi WHERE msi.acc = :msiAcc)", 
			BioSampleGroup.class.getName () 
		);
		
		entityManager.createQuery ( hql )
			.setParameter ( "publicFlag", publicFlag )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();

		
		if ( !isCascaded ) return;

		hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp IN " +
			"  (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg IN " +
			"    (SELECT sg1.id FROM %2$s sg1 JOIN sg1.MSIs msi WHERE msi.acc = :msiAcc))", 
			BioSample.class.getName (), BioSampleGroup.class.getName () 
		);
		
		entityManager.createQuery ( hql )
			.setParameter ( "publicFlag", publicFlag )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();
	}
	
	
	/**
	 * Changes the release date for a sample group and optionally cascades the operation to all the samples linked to 
	 * the group.
	 */
	public void setBioSampleGroupReleaseDate ( String sgAcc, Date releaseDate, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.find ( sgAcc );
		if ( sg == null ) throw new RuntimeException ( "Sample '" + sgAcc + "' not found" );
		sg.setReleaseDate ( releaseDate );
		if ( !isCascaded ) return;

		String hql = String.format ( 
			"UPDATE %s smp SET smp.releaseDate = :releaseDate " +
			"WHERE smp IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg.acc = :sgAcc)", 
			BioSample.class.getName () 
		);
		
		entityManager.createQuery ( hql )
			.setParameter ( "releaseDate", releaseDate )
			.setParameter ( "sgAcc", sgAcc )
			.executeUpdate ();
	}

	/**
	 * Changes the visibility for all the sample groups and samples linked to a submission. If isCascaded is true, cascades 
	 * from all the sample groups to the the samples linked to them.
	 */
	public void setMSIReleaseDate ( String msiAcc, Date releaseDate, boolean isCascaded )
	{
		MSI msi = msiDao.find ( msiAcc );
		if ( msi == null ) throw new RuntimeException ( "Submission '" + msiAcc + "' not found" );

		String hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.MSIS msi WHERE msi.releaseDate = :releaseDate)", 
			BioSample.class.getName () 
		);
		
		entityManager.createQuery ( hql )
		  .setParameter ( "releaseDate", releaseDate )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();

		hql = String.format ( 
			"UPDATE %s sg SET sg.releaseDate = :releaseDate " +
			"WHERE sg IN (SELECT sg1.id FROM %1$s sg1 JOIN sg1.MSIS msi WHERE msi.acc = :msiAcc)", 
			BioSampleGroup.class.getName () 
		);
		
		entityManager.createQuery ( hql )
			.setParameter ( "releaseDate", releaseDate )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();
		
		if ( !isCascaded ) return;

		hql = String.format ( 
			"UPDATE %s smp SET smp.releaseDate = :releaseDate " +
			"WHERE smp IN " +
			"  (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg IN " +
			"    (SELECT sg1.id FROM %2$s sg1 JOIN sg1.MSIS msi WHERE msi.acc = :msiAcc))", 
			BioSample.class.getName (), BioSampleGroup.class.getName () 
		);
		
		entityManager.createQuery ( hql )
			.setParameter ( "releaseDate", releaseDate )
			.setParameter ( "msiAcc", msiAcc )
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
		msiDao = new AccessibleDAO<MSI> ( MSI.class, entityManager );
	}
	
}
