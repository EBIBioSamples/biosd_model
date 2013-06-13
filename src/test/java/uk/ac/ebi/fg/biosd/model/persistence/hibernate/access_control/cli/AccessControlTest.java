package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli;

import static java.lang.System.out;
import static org.apache.commons.lang.time.DateFormatUtils.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.SubmissionPersistenceTest;
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
 * TODO: test the return values.
 *
 * <dl><dt>date</dt><dd>Aug 23, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@SuppressWarnings ( { "rawtypes", "unchecked" } )
public class AccessControlTest
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
		em.createQuery ( "delete from User" );
		tns.commit ();

		SubmissionPersistenceTest.verifyTestModel ( model, false, em );
		assertTrue( "User deletion at @After didn't work!", em.createQuery ( "FROM User" ).getResultList ().isEmpty () );
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

		BioSample smp1DB = (BioSample) biomaterialDao.find ( model.smp1.getId () );
		assertNotNull ( "Could not fetch smp1!", smp1DB  );
		
		out.println ( "\n\nReloaded model:" );
		new DirectDerivationGraphDumper ().dump ( out, smp1DB );

		SubmissionPersistenceTest.verifyTestModel ( model, true, em );
		
		User user1DB = userDao.find ( model.user1.getId () );
		assertNotNull ( "Could not fetch user1!", user1DB  );

		User user2DB = userDao.find ( model.user2.getId () );
		assertNotNull ( "Could not fetch user2!", user2DB  );

		assertTrue ( "smp1 not linked to user1!", user1DB.getBioSamples ().contains ( model.smp1 ));
		assertTrue ( "smp1 not linked to user1 (DB-check)!", user1DB.getBioSamples ().contains ( smp1DB ));
		
		assertTrue ( "smp2 not linked to user2!", user2DB.getBioSamples ().contains ( model.smp2 ));
	}

	@Test
	public void testAccessControlCLIVisibility ()
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
		acMgr.setBioSampleVisibility ( model.smp1.getAcc (), false );
		Date relDate = new GregorianCalendar ( 2100, 1, 21 ).getTime ();  
		acMgr.setBioSampleReleaseDate ( model.smp1.getAcc (), relDate );
		tns.commit ();
		
		BioSample smp1DB = sampleDao.find ( model.smp1.getAcc () );
		assertFalse ( "publicFlag not saved!", smp1DB.getPublicFlag () );
		assertEquals ( "releaseDate not saved!", relDate, smp1DB.getReleaseDate () );
		
		tns.begin ();
		acMgr.setBioSampleGroupVisibility ( model.sg1.getAcc (), null, true );
		relDate = new GregorianCalendar ( 1971, 10, 1 ).getTime ();  
		acMgr.setBioSampleGroupReleaseDate ( model.sg1.getAcc (), relDate, true );
		tns.commit ();
				
		// Re-using em doesn't work and replacing it doesn't work either
		EntityManager em1 = em.getEntityManagerFactory ().createEntityManager ();
		sampleDao.setEntityManager ( em1 );
		sgDao.setEntityManager ( em1 );
		
		smp1DB = sampleDao.find ( model.smp1.getAcc () );
		BioSampleGroup sg1DB = sgDao.find ( model.sg1.getAcc () );
		
		assertNull ( "sg didn't save publicFlag!", sg1DB.getPublicFlag () );
		assertEquals ( "sg didn't save releaseDate update!", relDate, sg1DB.getReleaseDate () );
		assertNull ( "sg didn't cascade publicFlag update!", smp1DB.getPublicFlag () );
		assertEquals ( "sg didn't cascade releaseDate update!", relDate, smp1DB.getReleaseDate () );
		
		assertTrue ( "sg.isPublic() not working!", sg1DB.isPublic () );
		assertTrue ( "smp.isPublic() not working!", smp1DB.isPublic () );
	}
	
	
	@Test
	public void testAccessControlCLI ()
	{
		AccessibleDAO<BioSample> sampleDao = new AccessibleDAO<BioSample> ( BioSample.class, em );
		AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, em );
		AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );
		
		
		// Save the test model
		// 
		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		sampleDao.create ( model.smp1 );
		sampleDao.getOrCreate ( model.smp2 );
		msiDao.getOrCreate ( model.msi );
		tns.commit ();

		// Re-using em doesn't work and replacing it doesn't work either
		AccessControlCLI acCli = new AccessControlCLI ( em.getEntityManagerFactory ().createEntityManager () );

		acCli.run ( String.format ( "set visibility samples -%s", model.smp1.getAcc () ));
		Date relDate = new GregorianCalendar ( 2100, 0, 21 ).getTime ();
		String cmd = 
			"set release-date samples " + model.smp1.getAcc () + "  " + format ( relDate, RelDateParser.DATE_FMTS [ 1 ] );
		out.println ( "Sending Command: " + cmd );
		acCli.run ( cmd );
		
		// TODO: use 'get-visibility'
		sampleDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		BioSample smp1DB = sampleDao.find ( model.smp1.getAcc () );
		assertFalse ( "publicFlag not saved!", smp1DB.getPublicFlag () );
		assertEquals ( "releaseDate not saved!", relDate, smp1DB.getReleaseDate () );
		
		
		acCli.run ( String.format ( "  set visibility  sample-groups --%s++ ", model.sg1.getAcc () ));
		relDate = new GregorianCalendar ( 1971, 9, 1, 14, 15, 33 ).getTime ();

		cmd = "set release-date  sample-groups  " + model.sg1.getAcc () + " " + format ( relDate, RelDateParser.DATE_FMTS [ 0 ] ) + "++";
		out.println ( "Sending Command: " + cmd );
		acCli.run ( cmd );
				
		
		// TODO: use 'get-visibility'
		// Re-using em doesn't work and replacing it doesn't work either
		EntityManager em1 = em.getEntityManagerFactory ().createEntityManager ();
		sampleDao.setEntityManager ( em1 );
		sgDao.setEntityManager ( em1 );
		
		smp1DB = sampleDao.find ( model.smp1.getAcc () );
		BioSampleGroup sg1DB = sgDao.find ( model.sg1.getAcc () );
		
		out.println ( "reloaded SG1:\n" + sg1DB );
		
		assertNull ( "sg didn't save publicFlag!", sg1DB.getPublicFlag () );
		assertEquals ( "sg didn't save releaseDate update!", relDate, sg1DB.getReleaseDate () );
		assertNull ( "sg didn't cascade publicFlag update!", smp1DB.getPublicFlag () );
		assertEquals ( "sg didn't cascade releaseDate update!", relDate, smp1DB.getReleaseDate () );
		
		assertTrue ( "sg.isPublic() not working!", sg1DB.isPublic () );
		assertTrue ( "smp.isPublic() not working!", smp1DB.isPublic () );		
	}
	
	
	@Test
	public void testAccessControlCLIUsers ()
	{
		// Re-using em doesn't work and replacing it doesn't work either
		AccessControlCLI cli = new AccessControlCLI ( em );
		String email = "test.user@somewhere.net", name = "Mr", surname = "Test 'The' Test", pwd = "the_secret", 
			notes = "This is a test user   ";
		String cmd = String.format ( 
			"create  user   --email = %s --name = '%s'  --surname = \"%s\" --password = %s --notes = '%s'", email, name, surname, pwd, notes 
		);
		out.println ( "Sending: " + cmd );
		cli.run ( cmd );
		
		UserDAO udao = new UserDAO ( em.getEntityManagerFactory ().createEntityManager () );
		User uDB = udao.find ( email );
		assertNotNull ( "user not saved!", uDB );
		
		out.println ( "Reloaded user:\n" + uDB );
		
		assertEquals ( "Wrong name", name, uDB.getName () );
		assertEquals ( "Wrong surname", surname, uDB.getSurname () );
		assertEquals ( "Password doesn't match!", User.hashPassword ( pwd ), uDB.getHashPassword () );

		String newSurname = "New Test Surname";
		cmd = String.format ( "modify user --surname = %s --email = %s", newSurname, email );
		out.println ( "Sending: " + cmd );
		cli.run ( cmd );
		
		udao = new UserDAO ( em = em.getEntityManagerFactory ().createEntityManager () );
		uDB = udao.find ( email );
		assertNotNull ( "Changed user not saved!", uDB );
		
		out.println ( "Reloaded modified user:\n" + uDB );
		assertEquals ( "name change didn't work!", newSurname, uDB.getSurname () );
		assertEquals ( "lost name during change command!", name, uDB.getName () );
		
		cli.run ( "delete user " + email + "++" );
		
		udao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		assertNull ( "Test user not deleted!", udao.find ( email ) );
	}
	
	
	@Test
	public void testOwnerSetCLI ()
	{
		AccessibleDAO<BioSample> sampleDao = new AccessibleDAO<BioSample> ( BioSample.class, em );
		AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, em );
		AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );

		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		sampleDao.create ( model.smp1 );
		sampleDao.getOrCreate ( model.smp2 );
		msiDao.getOrCreate ( model.msi );
		tns.commit ();
		
		UserStoreParser usrStoreCli = new UserStoreParser ( em );
		String email = "test.user@somewhere.net", name = "Mr", surname = "Test 'The' Test", pwd = "the_secret", 
			notes = "This is a test user   ";
		String cmd = String.format ( 
			"--email = %s --name = '%s'  --surname = \"%s\" --password = %s --notes = '%s'", email, name, surname, pwd, notes 
		);
		out.println ( "Sending: " + cmd );
		usrStoreCli.run ( cmd, true );
		
		UserDAO udao = new UserDAO ( em.getEntityManagerFactory ().createEntityManager () );
		User uDB = udao.find ( email );
		assertNotNull ( "user not saved!", uDB );

		AccessControlCLI cli = new AccessControlCLI ( em );
		cli.run ( String.format ( "set owner  %s samples %s", email, model.smp1.getAcc (), model.smp2.getAcc () ) );
		
		sampleDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		BioSample smp1DB = sampleDao.find ( model.smp1.getAcc () );
		assertTrue ( "User owner for smp1 not correctly saved!", smp1DB.getUsers ().contains ( uDB ) );
		
		
		// Do the same for SGs
		cli.run ( String.format ( "set owner %s sample-groups %s++", email, model.sg2.getAcc () ) );

		sgDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		BioSampleGroup sg2DB = sgDao.find ( model.sg2.getAcc () );

		sampleDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		BioSample smp4DB = sampleDao.find ( model.smp4.getAcc () );
		
		assertTrue ( "User owner for sg2 not correctly saved!", sg2DB.getUsers ().contains ( uDB ) );
		assertTrue ( "User owner for smp4 not correctly saved!", smp4DB.getUsers ().contains ( uDB ) );

		
		cli.run ( String.format ( "set  owner  %s sample-groups -%s++", email, model.sg2.getAcc () ) );

		sgDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		sg2DB = sgDao.find ( model.sg2.getAcc () );
		
		sampleDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		smp4DB = sampleDao.find ( model.smp4.getAcc () );
		
		assertFalse ( "User owner for sg2 not removed!", sg2DB.getUsers ().contains ( uDB ) );
		assertFalse ( "User owner for smp4 not removed!", smp4DB.getUsers ().contains ( uDB ) );
		
		
		cli.run ( String.format ( "set owner %s sample-groups =%s", email, model.sg2.getAcc () ) );

		sgDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		sg2DB = sgDao.find ( model.sg2.getAcc () );

		sampleDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		smp4DB = sampleDao.find ( model.smp4.getAcc () );

		assertTrue ( "User owner for sg2 not correctly saved!", sg2DB.getUsers ().contains ( uDB ) );
		assertFalse ( "User owner for sg2 were unexpectedely cascaded!", smp4DB.getUsers ().contains ( uDB ) );
		
		
		cli.run ( String.format ( "set owner %s sample-groups =%s++", email, model.sg1.getAcc () ) );
		
		sgDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		BioSampleGroup sg1DB = sgDao.find ( model.sg1.getAcc () );

		sampleDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		BioSample smp2DB = sampleDao.find ( model.smp2.getAcc () );
		
		assertTrue ( "User owner for sg1 not correctly saved!", sg1DB.getUsers ().contains ( uDB ) );
		assertTrue ( "User owner for sg1 weren't cascaded!", smp1DB.getUsers ().contains ( uDB ) );
		assertFalse ( "User owner for sg1 weren't cascaded!", smp2DB.getUsers ().contains ( model.user2 ) );

		
		cli.run ( String.format ( "set owner null sample-groups %s++", model.sg1.getAcc () ) );

		sgDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		sg1DB = sgDao.find ( model.sg1.getAcc () );

		sampleDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		smp1DB = sampleDao.find ( model.smp1.getAcc () );
		smp2DB = sampleDao.find ( model.smp2.getAcc () );
		
		assertTrue ( "User owner for sg1 not removed!", sg1DB.getUsers ().isEmpty () );
		assertTrue ( "User owner removal for sg1 wasn't cascaded!", smp1DB.getUsers ().isEmpty () );
		assertTrue ( "User owner removal for sg1 wasn't cascaded!", smp2DB.getUsers ().isEmpty () );
		
		
		cli.run ( String.format ( "set owner %s submissions %s++", email, model.msi.getAcc () ) );
	
		msiDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		MSI msiDB = msiDao.find ( model.msi.getAcc () );
		
		sgDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		sg2DB = sgDao.find ( model.sg2.getAcc () );

		sampleDao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		BioSample smp6DB = sampleDao.find ( model.smp6.getAcc () );
		
		assertTrue ( "owner-set for MSI didn't work! ", msiDB.getUsers ().contains ( uDB ) );
		assertTrue ( "owner-set for MSI didn't cascade to sg2!", sg2DB.getUsers ().contains ( uDB ) );
		assertTrue ( "owner-set for MSI didn't cascade to smp62!", smp6DB.getUsers ().contains ( uDB ) );
		
		// Just to test that users are auto-removed from their links.
		cli.run ( String.format ( "set owner %s submissions %s++", email, model.msi.getAcc () ) );
		
		// DEBUG
//		tns = em.getTransaction ();
//		tns.begin ();
//		uDB.deleteBioSample ( model.smp1 );
//		uDB.deleteBioSample ( model.smp2 );
//		tns.commit ();
		
		
		UserDeleteParser udelParser = new UserDeleteParser ( em );
		udelParser.run ( email + "++" );
		
		udao.setEntityManager ( em.getEntityManagerFactory ().createEntityManager () );
		assertNull ( "Test user not deleted!", udao.find ( email ) );
	}
	
	@Test
	public void testUserSearchCLI ()
	{
		AccessibleDAO<BioSample> sampleDao = new AccessibleDAO<BioSample> ( BioSample.class, em );
		AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );

		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		sampleDao.create ( model.smp1 );
		sampleDao.getOrCreate ( model.smp2 );
		msiDao.getOrCreate ( model.msi );
		tns.commit ();
		
		List<User> users = new UserSearchParser ( this.em ).run ( "--email = @test.net" );
		assertEquals ( "Wrong size for user search! ", 2, users.size () );
		assertTrue ( "user search doesn't return user1!", users.contains ( model.user1 ) );
		assertTrue ( "user search doesn't return user2!", users.contains ( model.user2 ) );
	}
	
	@Test
	public void testVisibilityGetCLI ()
	{
		AccessibleDAO<BioSample> sampleDao = new AccessibleDAO<BioSample> ( BioSample.class, em );
		AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );

		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		sampleDao.create ( model.smp1 );
		sampleDao.getOrCreate ( model.smp2 );
		msiDao.getOrCreate ( model.msi );
		tns.commit ();

		AccessControlCLI cli = new AccessControlCLI ( em );
		List<MSI> msis = (List<MSI>) cli.run ( String.format ( "get visibility submissions %s++", model.msi.getAcc () ) );
		assertNotNull ( "'get visibility' returned null!", msis );
		assertEquals ( "'get visibility' returned a wrong-size result!", 1, msis.size () );

		out.println ( "\n\n" );
		List<BioSampleGroup> sgs = (List<BioSampleGroup>) cli.run ( String.format ( 
			"get visibility sample-groups %s++ %s++", model.sg1.getAcc (), model.sg2.getAcc () ) );
		assertNotNull ( "'get visibility sample-groups' returned null!", sgs );
		assertEquals ( "'get visibility sample-groups' returned a wrong-size result!", 2, sgs.size () );
	}
}
