package uk.ac.ebi.fg.biosd.model.persistence.hibernate.organizational;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.SubmissionPersistenceTest;
import uk.ac.ebi.fg.biosd.model.utils.test.TestModel;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jun 2015</dd>
 *
 */
public class MSIDAOTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( 
		Resources.getInstance ().getEntityManagerFactory () 
	);

	private EntityManager em;
	private TestModel model;
	private MSIDAO msiDao;
	
	private static final String DATA_ACC_PREFIX = "biosd.tests.dao.";	

	@Test
	public void getSampleGroupRefs()
	{
		List<BioSampleGroup> sgs = msiDao.getSampleGroupRefs ( model.msi );
		Assert.assertEquals ( "getSampleGroupRefs() returns a wrong size!", 2, sgs.size () );

		Iterator<BioSampleGroup> sgitr = sgs.iterator ();
		BioSampleGroup sg1db = sgitr.next (), sg2db = sgitr.next ();
		
		Assert.assertTrue ( "getSampleGroupRefs() returns wrong results!", 
			model.sg1.equals ( sg1db ) && model.sg2.equals ( sg2db )
			|| model.sg1.equals ( sg2db ) && model.sg2.equals ( sg1db )
		);
	}

	
	@Test
	public void getSampleGroupRefsFromList()
	{
		List<BioSampleGroup> sgs = msiDao.getSampleGroupRefsFromList ( model.msi.getSampleGroupRefs () );
		Assert.assertEquals ( "getSampleGroupRefsFromList() returns a wrong size!", 2, sgs.size () );

		Iterator<BioSampleGroup> sgitr = sgs.iterator ();
		BioSampleGroup sg1db = sgitr.next (), sg2db = sgitr.next ();
		
		Assert.assertTrue ( "getSampleGroupRefsFromList() returns wrong results!", 
			model.sg1.equals ( sg1db ) && model.sg2.equals ( sg2db )
			|| model.sg1.equals ( sg2db ) && model.sg2.equals ( sg1db )
		);
	}

	
	
	
	
	@Test
	public void getSampleRefs()
	{
		List<BioSample> smps = msiDao.getSampleRefs ( model.msi );
		Assert.assertEquals ( "getSampleRefs() returns a wrong size!", 2, smps.size () );

		Iterator<BioSample> smpitr = smps.iterator ();
		BioSample smp1db = smpitr.next (), smp2db = smpitr.next ();
		
		Assert.assertTrue ( "getSampleRefs() returns wrong results!", 
			model.smp4.equals ( smp1db ) && model.smp5.equals ( smp2db )
			|| model.smp4.equals ( smp2db ) && model.smp5.equals ( smp1db )
		);
	}
	
	@Test
	public void getSampleRefsFromList()
	{
		List<BioSample> smps = msiDao.getSampleRefsFromList ( model.msi.getSampleRefs () );
		Assert.assertEquals ( "getSampleRefsFromList() returns a wrong size!", 2, smps.size () );

		Iterator<BioSample> smpitr = smps.iterator ();
		BioSample smp1db = smpitr.next (), smp2db = smpitr.next ();
		
		Assert.assertTrue ( "getSampleRefs() returns wrong results!", 
			model.smp4.equals ( smp1db ) && model.smp5.equals ( smp2db )
			|| model.smp4.equals ( smp2db ) && model.smp5.equals ( smp1db )
		);
	}
	
	
	/**
	 * Initialises the DB and test data
	 */
	@Before
	public void init() throws Exception
	{
		model = new TestModel ( DATA_ACC_PREFIX );
		MSI msi = model.msi;
		// You shouldn't have them as both members and references, we're doing it just for testing purposes
		msi.addSampleGroupRef ( model.sg1.getAcc () );
		msi.addSampleGroupRef ( model.sg2.getAcc () );
		msi.addSampleRef ( model.smp4.getAcc () );
		msi.addSampleRef ( model.smp5.getAcc () );
		
		em = emProvider.getEntityManager ();
    AccessibleDAO<BioSample> biomaterialDao = new AccessibleDAO<BioSample> ( BioSample.class, em );
    AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, em );

		msiDao = new MSIDAO ( em );

    // Save the model
    // 
    EntityTransaction tns = em.getTransaction ();
    tns.begin ();
    biomaterialDao.create ( model.smp4 );
    biomaterialDao.create ( model.smp5 );
    sgDao.create ( model.sg1 );
    sgDao.create ( model.sg2 );
    msiDao.getOrCreate ( model.msi );
    tns.commit ();
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
}
