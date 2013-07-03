package uk.ac.ebi.fg.biosd.model.persistence.hibernate;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.utils.test.TestModel;
import uk.ac.ebi.fg.core_model.expgraph.BioMaterial;
import uk.ac.ebi.fg.core_model.expgraph.Node;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicType;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;
import uk.ac.ebi.fg.core_model.utils.expgraph.DirectDerivationGraphDumper;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * Basic tests about model persistence.
 *
 * <dl><dt>date</dt><dd>Aug 23, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@SuppressWarnings ( { "rawtypes" } )
public class SubmissionPersistenceTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( Resources.getInstance ().getEntityManagerFactory () );

	private EntityManager em;
	private TestModel model; 
	
	private static final String DATA_ACC_PREFIX = "biosd.tests.dao.";

	
	/**
	 * Checks that the {@link Node nodes} in model are loaded/unloaded (depending on checkIsLoaded), issues warnings
	 * and triggers a test failure in case not. 
	 */
	public static void verifyTestModel ( Object model, boolean checkIsLoaded, EntityManager em ) throws Exception
	{
		AccessibleDAO<Accessible> accDao = new AccessibleDAO<Accessible> ( Accessible.class, em );
		
		boolean isOK = true;
		
		for ( Field f: model.getClass ().getFields () ) 
		{
			Object o = f.get ( model );
			if ( ! ( o instanceof Accessible ) ) continue;
			
			Accessible acc = (Accessible) o;
			Accessible accDB = (Accessible) accDao.find ( acc.getAcc (), acc.getClass () );
				
			if ( checkIsLoaded )
			{
				if ( accDB == null ) {
					out.println ( ">>>> Node '" + acc.getAcc () + "' not found in the DB!" );
					isOK = false;
				}
			}
			else
			{
				if ( accDB != null ) {
					out.println ( ">>>> Node '" + acc.getAcc () + "' still in the DB!" );
					isOK = false;
				}
			}
		} // for()
		assertTrue ( "Some test objects " + (checkIsLoaded ? "not": "still" ) + " in the DB!", isOK );
	}
	
	/**
	 * Initialises the DB and test data
	 */
	@Before
	public void init() throws Exception
	{
		em = emProvider.getEntityManager ();
		model = new TestModel ( DATA_ACC_PREFIX );
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

		verifyTestModel ( model, false, em );
	}
	
	@Test
	public void testBasics () throws Exception
	{
		AccessibleDAO<BioMaterial> biomaterialDao = new AccessibleDAO<BioMaterial> ( BioMaterial.class, em );
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

		verifyTestModel ( model, true, em );
	}

	@Test 
	public void testReuse () throws Exception
	{
		AccessibleDAO<BioMaterial> biomaterialDao = new AccessibleDAO<BioMaterial> ( BioMaterial.class, em );
		AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );

		// Save
		// 
		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		biomaterialDao.create ( model.smp1 );
		biomaterialDao.getOrCreate ( model.smp2 );
		msiDao.getOrCreate ( model.msi );
		tns.commit ();
		
		// Add new nodes and try to persist again
		//
		BioSample smpNew1 = new BioSample ( DATA_ACC_PREFIX + "smpNew1" );
		BioCharacteristicType ctNew1 = new BioCharacteristicType ( "foo" );
		BioCharacteristicValue cvNew1 = new BioCharacteristicValue ( "foo value", ctNew1 );
		smpNew1.addPropertyValue ( cvNew1 );
		
		model.smp6.addDerivedInto ( smpNew1 );
		
		MSI msiNew = new MSI ( DATA_ACC_PREFIX + "msiNew1" );
		msiNew.addSample ( smpNew1 );
		
		tns = em.getTransaction ();
		tns.begin ();
		msiDao.getOrCreate ( msiNew );
		tns.commit ();
		
		out.println ( "Saved model:" );
		new DirectDerivationGraphDumper ().dump ( out, model.smp1 );

		Node smp1DB = biomaterialDao.find ( model.smp1.getId () );
		assertNotNull ( "Could not fetch smp1!", smp1DB  );
		
		out.println ( "\n\nReloaded model:" );
		new DirectDerivationGraphDumper ().dump ( out, model.smp1 );

		List<MSI> msisNewDB = msiDao.findByExample ( new MSI ( msiNew.getAcc () ) );
		assertNotNull ( "Could not load the new saved submission!", msisNewDB );
		assertEquals ( "Could not load the new saved submission (wrong size)!", 1, msisNewDB.size () );
		MSI msiNewDB = msisNewDB.get ( 0 );
		
		assertNotNull ( "Could not load the new saved submission!", msiNewDB );
		assertNotNull ( "New saved submission has null ID!", msiNewDB.getId () );
		
		out.println ( "\n\nNew Submission saved: \n" +  msiNewDB );
		
		verifyTestModel ( model, true, em );
		
		// Delete
		tns.begin ();
		// TODO: verify why Hibernate needs this in order to remove the whole upstream graph in cleanUpDB()
		model.smp6.getDerivedInto ().remove ( smpNew1 );
		msiDao.delete ( msiNewDB );
		biomaterialDao.delete ( smpNew1 );
		tns.commit ();
	}
}
