package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import java.util.Date;
import java.util.LinkedList;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli.AccessControlCLI;
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
	private UserDAO userDao;
	
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
	public MSI setMSIVisibility ( String msiAcc, Boolean publicFlag, boolean isCascaded )
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

		
		if ( !isCascaded ) return msi;

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

		return msi;
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


	public void addSampleOwner ( String sampleAcc, String userAcc ) 
	{
		BioSample sample = sampleDao.findAndFail ( sampleAcc );
		User user = userDao.findAndFail ( userAcc );
		
		sample.addUser ( user );
	}
	
	public void deleteSampleOwner ( String sampleAcc, String userAcc ) 
	{
		BioSample sample = sampleDao.findAndFail ( sampleAcc );
		User user = userDao.findAndFail ( userAcc );
		
		sample.deleteUser ( user );
	}

	private void setSampleOwner ( BioSample sample, User user ) 
	{
		boolean isAlreadyThere = false;
		for ( User susr: new LinkedList<User> ( sample.getUsers () ) )
			if ( user == null ) 
				sample.deleteUser ( susr );
			else { 
			 if ( susr.equals ( user ) ) isAlreadyThere = true; else sample.deleteUser ( susr ); 
		}
			
		if ( !isAlreadyThere && user != null ) sample.addUser ( user );
	}
	
	public void setSampleOwner ( String sampleAcc, String userAcc ) 
	{
		BioSample sample = sampleDao.findAndFail ( sampleAcc );
		User user = userAcc == null ? null : userDao.findAndFail ( userAcc );
		
		setSampleOwner ( sample, user );
	}
	
	
	public void addSampleGroupOwner ( String sgAcc, String userAcc, boolean isCascaded )
	{
		BioSampleGroup sg = sgDao.findAndFail ( sgAcc );
		User user = userDao.findAndFail ( userAcc );
		
		sg.addUser ( user );
		
		if ( !isCascaded ) return;
		
		for ( BioSample smp: sg.getSamples () ) smp.addUser ( user );
	}
	
	public void deleteSampleGroupOwner ( String sgAcc, String userAcc, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.findAndFail ( sgAcc );
		User user = userDao.findAndFail ( userAcc );
		
		sg.deleteUser ( user );

		if ( !isCascaded ) return;
		
		for ( BioSample smp: sg.getSamples () ) smp.deleteUser ( user );
	}

	private void setSampleGroupOwner ( BioSampleGroup sg, User user, boolean isCascaded ) 
	{
		boolean isAlreadyThere = false;
		for ( User sgUsr: new LinkedList<User> ( sg.getUsers () ) )
			if ( user == null ) 
				sg.deleteUser ( sgUsr );
			else { 
				if ( sgUsr.equals ( user ) ) isAlreadyThere = true; else sg.deleteUser ( sgUsr ); 
		}
		
		if ( !isAlreadyThere && user != null ) sg.addUser ( user );
		
		if ( !isCascaded ) return;
		for ( BioSample smp: sg.getSamples () ) this.setSampleOwner ( smp, user );
	}
	
	public void setSampleGroupOwner ( String sgAcc, String userAcc, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.findAndFail ( sgAcc );
		User user = userAcc == null ? null : userDao.findAndFail ( userAcc );
	  setSampleGroupOwner ( sg, user, isCascaded );
	}

	
	public void addMSIOwner ( String msiAcc, String userAcc, boolean isCascaded )
	{
		MSI msi = msiDao.findAndFail ( msiAcc );
		User user = userDao.findAndFail ( userAcc );
		
		msi.addUser ( user );
		
		if ( !isCascaded ) return;
		
		for ( BioSampleGroup sg: msi.getSampleGroups () ) sg.addUser ( user );
		for ( BioSample smp: msi.getSamples () ) smp.addUser ( user );
	}
	
	public void deleteMSIOwner ( String msiAcc, String userAcc, boolean isCascaded )
	{
		MSI msi = msiDao.findAndFail ( msiAcc );
		User user = userDao.findAndFail ( userAcc );
		
		msi.deleteUser ( user );
		
		if ( !isCascaded ) return;
		
		for ( BioSampleGroup sg: msi.getSampleGroups () ) sg.deleteUser ( user );
		for ( BioSample smp: msi.getSamples () ) smp.deleteUser ( user );
	}
	
	public void setMSIOwner ( String msiAcc, String userAcc, boolean isCascaded )
	{
		MSI msi = msiDao.findAndFail ( msiAcc );
		User user = userDao.findAndFail ( userAcc );
		
		boolean isAlreadyThere = false;
		for ( User msiUsr: new LinkedList<User> ( msi.getUsers () ) )
			if ( user == null ) 
				msi.deleteUser ( msiUsr );
			else { 
				if ( msiUsr.equals ( user ) ) isAlreadyThere = true; else msi.deleteUser ( msiUsr ); 
		}
		
		if ( !isAlreadyThere && user != null ) msi.addUser ( user );
		
		if ( !isCascaded ) return;
		for ( BioSampleGroup sg: msi.getSampleGroups () ) this.setSampleGroupOwner ( sg, user, isCascaded );
		for ( BioSample smp: msi.getSamples () ) this.setSampleOwner ( smp, user );
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
		userDao = new UserDAO ( entityManager );
	}

}
