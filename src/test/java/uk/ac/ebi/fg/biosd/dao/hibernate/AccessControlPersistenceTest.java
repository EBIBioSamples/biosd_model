/*
 * 
 */
package uk.ac.ebi.fg.biosd.dao.hibernate;

import static java.lang.System.out;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.access_control.User;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.UserDAO;
import uk.ac.ebi.fg.biosd.model.utils.test.AccessControlTestModel;
import uk.ac.ebi.fg.biosd.model.utils.test.TestModel;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.expgraph.BioMaterial;
import uk.ac.ebi.fg.core_model.expgraph.Node;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicType;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;
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

}
