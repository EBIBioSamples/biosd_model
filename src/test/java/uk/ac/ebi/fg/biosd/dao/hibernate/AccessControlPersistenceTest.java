package uk.ac.ebi.fg.biosd.dao.hibernate;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.AccessControlManager;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.UserDAO;
import uk.ac.ebi.fg.biosd.model.utils.test.AccessControlTestModel;
import uk.ac.ebi.fg.core_model.expgraph.BioMaterial;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.fg.core_model.utils.expgraph.DirectDerivationGraphDumper;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * Tests access control and ownership features.
 *
 * <dl><dt>date</dt><dd>Aug 23, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@SuppressWarnings ( { "rawtypes" } )
public class AccessControlPersistenceTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( Resources.getInstance ().getEntityManagerFactory () );

	private EntityManager em;
	private AccessControlTestModel model; 
	
	private static final String DATA_ACC_PREFIX = "biosd.tests.dao.acc_ctrl.";

	
	/**
	 * Initialises the DB and test data
	 */
	@Before
	public void init() throws Exception
	{
		em = emProvider.getEntityManager ();
		model = new AccessControlTestModel ( DATA_ACC_PREFIX );
	}

	
	/**
	 * Deletes test data
	 */
	@After
	public void cleanUpDB () throws Exception
	{
		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		model.delete ( em );
		tns.commit ();

		SubmissionPersistenceTest.verifyTestModel ( model, false, em );
	}
	
	@Test
	public void testBasics () throws Exception
	{
		AccessibleDAO<BioMaterial> biomaterialDao = new AccessibleDAO<BioMaterial> ( BioMaterial.class, em );
		UserDAO userDao = new UserDAO ( em );
		AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );

		// Save
		// 
		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		biomaterialDao.create ( model.smp1 );
		biomaterialDao.getOrCreate ( model.smp2 );
		msiDao.getOrCreate ( model.msi );
		tns.commit ();

		out.println ( "Saved model:" );
		new DirectDerivationGraphDumper ().dump ( out, model.smp1 );

		BioSample smp1DB = (BioSample) biomaterialDao.findById ( model.smp1.getId () );
		assertNotNull ( "Could not fetch smp1!", smp1DB  );
		
		out.println ( "\n\nReloaded model:" );
		new DirectDerivationGraphDumper ().dump ( out, smp1DB );

		SubmissionPersistenceTest.verifyTestModel ( model, true, em );
		
		User user1DB = userDao.findById ( model.user1.getId () );
		assertNotNull ( "Could not fetch user1!", user1DB  );

		User user2DB = userDao.findById ( model.user2.getId () );
		assertNotNull ( "Could not fetch user2!", user2DB  );

		assertTrue ( "smp1 not linked to user1!", user1DB.getBioSamples ().contains ( model.smp1 ));
		assertTrue ( "smp1 not linked to user1 (DB-check)!", user1DB.getBioSamples ().contains ( smp1DB ));
		
		assertTrue ( "smp2 not linked to user2!", user2DB.getBioSamples ().contains ( model.smp2 ));
	}

	@Test
	public void testAccessControlManager ()
	{
		AccessibleDAO<BioSample> sampleDao = new AccessibleDAO<BioSample> ( BioSample.class, em );
		AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, em );
		AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );
		AccessControlManager acMgr = new AccessControlManager ( em );
		
		// Save
		// 
		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		sampleDao.create ( model.smp1 );
		sampleDao.getOrCreate ( model.smp2 );
		msiDao.getOrCreate ( model.msi );
		tns.commit ();

		tns.begin ();
		acMgr.changeBioSampleVisibility ( model.smp1.getAcc (), false );
		Date relDate = new GregorianCalendar ( 2100, 1, 21 ).getTime ();  
		acMgr.changeBioSampleReleaseDate ( model.smp1.getAcc (), relDate );
		tns.commit ();
		
		BioSample smp1DB = sampleDao.find ( model.smp1.getAcc () );
		assertFalse ( "publicFlag not saved!", smp1DB.getPublicFlag () );
		assertEquals ( "releaseDate not saved!", relDate, smp1DB.getReleaseDate () );
		
		tns.begin ();
		acMgr.changeBioSampleGroupVisibility ( model.sg1.getAcc (), null, true );
		relDate = new GregorianCalendar ( 1971, 10, 1 ).getTime ();  
		acMgr.changeBioSampleGroupReleaseDate ( model.sg1.getAcc (), relDate, true );
		tns.commit ();
				
		em = emProvider.newEntityManager ();
		sampleDao.setEntityManager ( em );
		sgDao.setEntityManager ( em );
		
		smp1DB = sampleDao.find ( model.smp1.getAcc () );
		BioSampleGroup sg1DB = sgDao.find ( model.sg1.getAcc () );
		
		assertNull ( "sg didn't save publicFlag!", sg1DB.getPublicFlag () );
		assertEquals ( "sg didn't save releaseDate update!", relDate, sg1DB.getReleaseDate () );
		assertNull ( "sg didn't cascade publicFlag update!", smp1DB.getPublicFlag () );
		assertEquals ( "sg didn't cascade releaseDate update!", relDate, smp1DB.getReleaseDate () );
		
		assertTrue ( "sg.isPublic() not working!", sg1DB.isPublic () );
		assertTrue ( "smp.isPublic() not working!", smp1DB.isPublic () );
	}
}
