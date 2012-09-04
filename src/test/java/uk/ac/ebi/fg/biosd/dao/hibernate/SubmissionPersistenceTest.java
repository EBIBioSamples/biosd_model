/*
 * 
 */
package uk.ac.ebi.fg.biosd.dao.hibernate;

import static java.lang.System.out;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.utils.test.TestModel;
import uk.ac.ebi.fg.core_model.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.expgraph.BioMaterial;
import uk.ac.ebi.fg.core_model.expgraph.Node;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;
import uk.ac.ebi.fg.core_model.utils.expgraph.DirectDerivationGraphDumper;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * TODO: Comment me!
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

	
	/**
	 * Checks that the {@link Node nodes} in model are loaded/unloaded (depending on checkIsLoaded), issues warnings
	 * and triggers a test failure in case not. 
	 */
	private void verifyTestModel ( Object model, boolean checkIsLoaded ) throws Exception
	{
		AccessibleDAO<Accessible> accDao = new AccessibleDAO<Accessible> ( Accessible.class, em );
		
		boolean isOK = true;
		
		for ( Field f: this.getClass ().getFields () ) 
		{
			Object o = f.get ( this );
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
			assertTrue ( (checkIsLoaded ? "Some test objects not in the DB!": "Some objects still in the DB!" ), isOK );
		}		
	}
	
	@Before
	public void init() throws Exception
	{
		em = emProvider.getEntityManager ();
		model = new TestModel ( "biosd.tests.dao." );
	}

	
	@After
	public void cleanUpDB () throws Exception
	{
		EntityTransaction tns = em.getTransaction ();
		tns.begin ();
		model.delete ( em );
		tns.commit ();

		verifyTestModel ( model, false );
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
		DirectDerivationGraphDumper.dump ( out, model.smp1 );

		Node smp1DB = biomaterialDao.findById ( model.smp1.getId () );
		assertNotNull ( "Could not fetch smp1!", smp1DB  );
		
		out.println ( "\n\nReloaded model:" );
		DirectDerivationGraphDumper.dump ( out, model.smp1 );

		verifyTestModel ( model, true );
	}
}
