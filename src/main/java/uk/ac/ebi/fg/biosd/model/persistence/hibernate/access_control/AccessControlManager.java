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
	public boolean setBioSampleVisibility ( BioSample sample, Boolean publicFlag ) 
	{
		Boolean oldPubFlag = sample.getPublicFlag ();
		if ( oldPubFlag == null ? publicFlag == null : oldPubFlag.equals ( publicFlag ) ) return false;
		
		sample.setPublicFlag ( publicFlag );
		return true;
	}

	public boolean setBioSampleVisibility ( String sampleAcc, Boolean publicFlag ) 
	{
		BioSample sample = sampleDao.findAndFail ( sampleAcc );
		return this.setBioSampleVisibility ( sample, publicFlag );
	}
	
	
	/**
	 * Changes the release date for a sample 
	 */
	public boolean setBioSampleReleaseDate ( BioSample sample, Date releaseDate ) 
	{
		Date oldDate = sample.getReleaseDate ();
		if ( oldDate == null ? releaseDate == null : oldDate.equals ( releaseDate ) ) return false;
		
		sample.setReleaseDate ( releaseDate );
		return true;
	}

	public boolean setBioSampleReleaseDate ( String sampleAcc, Date releaseDate ) 
	{
		BioSample sample = sampleDao.findAndFail ( sampleAcc );
		return this.setBioSampleReleaseDate ( sample, releaseDate ); 
	}
	
	/**
	 * Changes the visibility for a sample group and optionally cascades the operation to all the samples linked to 
	 * the group.
	 */
	public int setBioSampleGroupVisibility ( BioSampleGroup sg, Boolean publicFlag, boolean isCascaded ) 
	{		
		int result = 0;
		
		Boolean oldPubFlag = sg.getPublicFlag ();
		if ( oldPubFlag == null ? publicFlag != null : !oldPubFlag.equals ( publicFlag ) ) {
			sg.setPublicFlag ( publicFlag );
			result++;
		}

		if ( !isCascaded ) return result;

		// PLEASE NOTE, do not say WHERE smp.*id* IN ... this is an Hibernate problem, the intuitive form doesn't work, this
		// not-so-good form is auto-translated into id comparison.
		// 
		String hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg.acc = :sgAcc)", 
			BioSample.class.getName () 
		);
		
		result += entityManager.createQuery ( hql )
			.setParameter ( "publicFlag", publicFlag )
			.setParameter ( "sgAcc", sg.getAcc () )
			.executeUpdate ();
		
		return result;
	}
	
	public int setBioSampleGroupVisibility ( String sgAcc, Boolean publicFlag, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.findAndFail ( sgAcc );
		return setBioSampleGroupVisibility ( sg, publicFlag, isCascaded );
	}
	
	
	/**
	 * Changes the visibility for all the sample groups and samples linked to a submission. If isCascaded is true, cascades 
	 * from all the sample groups to the the samples linked to them.
	 */
	public int setMSIVisibility ( MSI msi, Boolean publicFlag, boolean isCascaded )
	{
		int result = 0;
		
		Boolean oldPubFlag = msi.getPublicFlag ();
		if ( oldPubFlag == null ? publicFlag != null : !oldPubFlag.equals ( publicFlag ) ) {
			msi.setPublicFlag ( publicFlag );
			result++;
		}

		if ( !isCascaded ) return result;

		String hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.MSIs msi WHERE msi.acc = :msiAcc)", 
			BioSample.class.getName () 
		);
		
		String msiAcc = msi.getAcc (); 
		
		result += entityManager.createQuery ( hql )
		  .setParameter ( "publicFlag", publicFlag )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();

		hql = String.format ( 
			"UPDATE %s sg SET sg.publicFlag = :publicFlag " +
			"WHERE sg IN (SELECT sg1.id FROM %1$s sg1 JOIN sg1.MSIs msi WHERE msi.acc = :msiAcc)", 
			BioSampleGroup.class.getName () 
		);
		
		result += entityManager.createQuery ( hql )
			.setParameter ( "publicFlag", publicFlag )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();


		hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp IN " +
			"  (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg IN " +
			"    (SELECT sg1.id FROM %2$s sg1 JOIN sg1.MSIs msi WHERE msi.acc = :msiAcc))", 
			BioSample.class.getName (), BioSampleGroup.class.getName () 
		);
		
		result += entityManager.createQuery ( hql )
			.setParameter ( "publicFlag", publicFlag )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();

		return result;
	}
	
	public int setMSIVisibility ( String msiAcc, Boolean publicFlag, boolean isCascaded )
	{
		MSI msi = msiDao.findAndFail ( msiAcc );
		return this.setMSIVisibility ( msi, publicFlag, isCascaded );
	}

	
	/**
	 * Changes the release date for a sample group and optionally cascades the operation to all the samples linked to 
	 * the group.
	 */
	public int setBioSampleGroupReleaseDate ( String sgAcc, Date releaseDate, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.findAndFail ( sgAcc );
		
		int result = 0;
		
		Date oldDate = sg.getReleaseDate ();
		if ( oldDate == null ? releaseDate != null : !oldDate.equals ( releaseDate ) ) {
			sg.setReleaseDate ( releaseDate );
			result++;
		}

		if ( !isCascaded ) return result;

		String hql = String.format ( 
			"UPDATE %s smp SET smp.releaseDate = :releaseDate " +
			"WHERE smp IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg.acc = :sgAcc)", 
			BioSample.class.getName () 
		);
		
		result += entityManager.createQuery ( hql )
			.setParameter ( "releaseDate", releaseDate )
			.setParameter ( "sgAcc", sgAcc )
			.executeUpdate ();
		
		return result;
	}

	/**
	 * Changes the visibility for all the sample groups and samples linked to a submission. If isCascaded is true, cascades 
	 * from all the sample groups to the the samples linked to them.
	 */
	public int setMSIReleaseDate ( String msiAcc, Date releaseDate, boolean isCascaded )
	{
		MSI msi = msiDao.findAndFail ( msiAcc );

		int result = 0;
		
		Date oldDate = msi.getReleaseDate ();
		if ( oldDate == null ? releaseDate != null : !oldDate.equals ( releaseDate ) ) {
			msi.setReleaseDate ( releaseDate );
			result++;
		}

		if ( !isCascaded ) return result;

		String hql = String.format ( 
			"UPDATE %s smp SET smp.publicFlag = :publicFlag " +
			"WHERE smp IN (SELECT smp1.id FROM %1$s smp1 JOIN smp1.MSIs msi WHERE msi.releaseDate = :releaseDate)", 
			BioSample.class.getName () 
		);
		
		result += entityManager.createQuery ( hql )
		  .setParameter ( "releaseDate", releaseDate )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();

		hql = String.format ( 
			"UPDATE %s sg SET sg.releaseDate = :releaseDate " +
			"WHERE sg IN (SELECT sg1.id FROM %1$s sg1 JOIN sg1.MSIs msi WHERE msi.acc = :msiAcc)", 
			BioSampleGroup.class.getName () 
		);
		
		result += entityManager.createQuery ( hql )
			.setParameter ( "releaseDate", releaseDate )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();
		
		hql = String.format ( 
			"UPDATE %s smp SET smp.releaseDate = :releaseDate " +
			"WHERE smp IN " +
			"  (SELECT smp1.id FROM %1$s smp1 JOIN smp1.groups sg WHERE sg IN " +
			"    (SELECT sg1.id FROM %2$s sg1 JOIN sg1.MSIs msi WHERE msi.acc = :msiAcc))", 
			BioSample.class.getName (), BioSampleGroup.class.getName () 
		);
		
		result += entityManager.createQuery ( hql )
			.setParameter ( "releaseDate", releaseDate )
			.setParameter ( "msiAcc", msiAcc )
			.executeUpdate ();
		
		return result;
	}


	public boolean addSampleOwner ( BioSample sample, User user ) 
	{
		if ( sample.getUsers ().contains ( user ) ) return false;
		
		sample.addUser ( user );
		return true;
	}
	
	public boolean addSampleOwner ( String sampleAcc, String userAcc ) 
	{
		BioSample sample = sampleDao.findAndFail ( sampleAcc );
		User user = userDao.findAndFail ( userAcc );
		
		return this.addSampleOwner ( sample, user );
	}
	
	public boolean deleteSampleOwner ( BioSample sample, User user ) 
	{
		if ( !sample.getUsers ().contains ( user ) ) return false;

		sample.deleteUser ( user );
		return true;
	}

	public boolean deleteSampleOwner ( String sampleAcc, String userAcc ) 
	{
		BioSample sample = sampleDao.findAndFail ( sampleAcc );
		User user = userDao.findAndFail ( userAcc );
		
		return this.deleteSampleOwner ( sample, user );
	}

	
	public int setSampleOwner ( BioSample sample, User user ) 
	{
		int result = 0;
		boolean isAlreadyThere = false;
		for ( User susr: new LinkedList<User> ( sample.getUsers () ) )
			if ( user == null ) { 
				sample.deleteUser ( susr ); result++;
			}
			else { 
			 if ( susr.equals ( user ) ) isAlreadyThere = true; 
			 else { sample.deleteUser ( susr ); result++; }
		}
			
		if ( !isAlreadyThere && user != null ) { sample.addUser ( user ); result++; }
		return result;
	}
	
	public int setSampleOwner ( String sampleAcc, String userAcc ) 
	{
		BioSample sample = sampleDao.findAndFail ( sampleAcc );
		User user = userAcc == null ? null : userDao.findAndFail ( userAcc );
		
		return setSampleOwner ( sample, user );
	}
	
	
	public int addSampleGroupOwner ( BioSampleGroup sg, User user, boolean isCascaded )
	{
		int result = 0;
		
		if ( !sg.getUsers ().contains ( user ) ) {
			sg.addUser ( user );
			result++;
		}
		
		if ( !isCascaded ) return result;
		
		for ( BioSample smp: sg.getSamples () ) 
			if ( this.addSampleOwner ( smp, user ) ) result++;
		
		return result;
	}
	
	public int addSampleGroupOwner ( String sgAcc, String userAcc, boolean isCascaded )
	{
		BioSampleGroup sg = sgDao.findAndFail ( sgAcc );
		User user = userDao.findAndFail ( userAcc );

		return addSampleGroupOwner ( sg, user, isCascaded );
	}

	
	public int deleteSampleGroupOwner ( BioSampleGroup sg, User user, boolean isCascaded ) 
	{
		int result = 0;
		
		if ( sg.getUsers ().contains ( user ) ) {
			sg.deleteUser ( user );
			result++;
		} 

		if ( !isCascaded ) return result;
		
		for ( BioSample smp: sg.getSamples () ) 
			if ( this.deleteSampleOwner ( smp, user ) ) result++;
		
		return result;
	}

	
	public int deleteSampleGroupOwner ( String sgAcc, String userAcc, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.findAndFail ( sgAcc );
		User user = userDao.findAndFail ( userAcc );

		return deleteSampleGroupOwner ( sg, user, isCascaded );
	}

	public int setSampleGroupOwner ( BioSampleGroup sg, User user, boolean isCascaded ) 
	{
		int result = 0; 
		
		boolean isAlreadyThere = false;
		for ( User sgUsr: new LinkedList<User> ( sg.getUsers () ) )
			if ( user == null ) { 
				sg.deleteUser ( sgUsr ); result++;
			}
			else { 
				if ( sgUsr.equals ( user ) ) isAlreadyThere = true; 
				else { sg.deleteUser ( sgUsr ); result++; } 
		}
		
		if ( !isAlreadyThere && user != null ) {
			sg.addUser ( user ); result++;
		}
		
		if ( !isCascaded ) return result;
		for ( BioSample smp: sg.getSamples () ) result += this.setSampleOwner ( smp, user );
		
		return result;
	}
	
	public int setSampleGroupOwner ( String sgAcc, String userAcc, boolean isCascaded ) 
	{
		BioSampleGroup sg = sgDao.findAndFail ( sgAcc );
		User user = userAcc == null ? null : userDao.findAndFail ( userAcc );
	  return setSampleGroupOwner ( sg, user, isCascaded );
	}

	
	public int addMSIOwner ( MSI msi, User user, boolean isCascaded )
	{
		int result= 0;
		
		if ( !msi.getUsers ().contains ( user ) ) {
			msi.addUser ( user ); result++;
		}
		
		if ( !isCascaded ) return result;
		
		for ( BioSampleGroup sg: msi.getSampleGroups () ) result += this.addSampleGroupOwner ( sg, user, true );
		for ( BioSample smp: msi.getSamples () ) if ( this.addSampleOwner ( smp, user ) ) result++;
		
		return result;
	}

	
	public int addMSIOwner ( String msiAcc, String userAcc, boolean isCascaded )
	{
		MSI msi = msiDao.findAndFail ( msiAcc );
		User user = userDao.findAndFail ( userAcc );

		return addMSIOwner ( msi, user, isCascaded );
	}

	
	public int deleteMSIOwner ( MSI msi, User user, boolean isCascaded )
	{
		int result = 0;
		
		if ( msi.getUsers ().contains ( user ) ) {
			msi.deleteUser ( user ); result++;
		}
		
		if ( !isCascaded ) return result;
		
		for ( BioSampleGroup sg: msi.getSampleGroups () ) result += this.deleteSampleGroupOwner ( sg, user, true );
		for ( BioSample smp: msi.getSamples () ) if ( this.deleteSampleOwner ( smp, user ) ) result++;
		
		return result;
	}

	public int deleteMSIOwner ( String msiAcc, String userAcc, boolean isCascaded )
	{
		MSI msi = msiDao.findAndFail ( msiAcc );
		User user = userDao.findAndFail ( userAcc );
		
		return this.deleteMSIOwner ( msi, user, isCascaded );
	}

	public int setMSIOwner ( MSI msi, User user, boolean isCascaded )
	{
		int result = 0;
		
		boolean isAlreadyThere = false;
		for ( User msiUsr: new LinkedList<User> ( msi.getUsers () ) )
			if ( user == null ) { 
				msi.deleteUser ( msiUsr );
				result++;
			}
			else { 
				if ( msiUsr.equals ( user ) ) isAlreadyThere = true; 
				else { msi.deleteUser ( msiUsr ); result++; } 
		}
		
		if ( !isAlreadyThere && user != null ) { msi.addUser ( user ); result++; }
		
		if ( !isCascaded ) return result;
		for ( BioSampleGroup sg: msi.getSampleGroups () ) result += this.setSampleGroupOwner ( sg, user, isCascaded );
		for ( BioSample smp: msi.getSamples () ) result += this.setSampleOwner ( smp, user );
		
		return result;
	}
	
	public int setMSIOwner ( String msiAcc, String userAcc, boolean isCascaded )
	{
		MSI msi = msiDao.findAndFail ( msiAcc );
		User user = userDao.findAndFail ( userAcc );
		
		return this.setMSIOwner ( msi, user, isCascaded );
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
