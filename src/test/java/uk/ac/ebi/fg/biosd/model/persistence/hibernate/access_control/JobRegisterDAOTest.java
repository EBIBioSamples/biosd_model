package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry;
import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * Some testing for {@link JobRegisterDAO}.
 *
 * <dl><dt>date</dt><dd>Apr 24, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class JobRegisterDAOTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( Resources.getInstance ().getEntityManagerFactory () );

	/**
	 * Basics of creation, search, deletion.
	 */
	@Test
	public void testBasics ()
	{
		EntityManager em = emProvider.getEntityManager ();
		JobRegisterDAO jrDao = new JobRegisterDAO ( em );
		
		JobRegisterEntry 
		  jre1 = new JobRegisterEntry ( "test.type", "test.acc1", Operation.ADD ),
		  jre2 = new JobRegisterEntry ( "test.type", "test.acc2", Operation.DELETE );
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		jrDao.create ( jre1 );
		jrDao.create ( jre2 );
		ts.commit ();
		
		jrDao.setEntityManager ( em = emProvider.newEntityManager () );
		List<JobRegisterEntry> log = jrDao.find ( 1 );
		
		assertEquals ( "Log entry storage didn't work!", 2, log.size () );
		assertTrue ( "Wrong data retrieved from the job register log!", 
			jre1.equals ( log.get ( 0 ) ) && jre2.equals ( log.get ( 1 ) ) 
			|| jre1.equals ( log.get ( 1 ) ) && jre2.equals ( log.get ( 0 ) ) 
	  );
		
		log = jrDao.find ( 1, Operation.DELETE );
		assertEquals ( "find with Operation didn't work!", 1, log.size () );
		assertEquals ( "find with Operation didn't work!", jre2, log.get ( 0 ) );

		assertTrue ( "Wrong result for hasEntry ()", jrDao.hasEntry ( jre1.getEntityType (), jre1.getAcc (), 1, jre1.getOperation () ) );
		assertFalse ( "Wrong result for hasEntry ()", jrDao.hasEntry ( 
			jre1.getEntityType (), jre1.getAcc (), 
			new GregorianCalendar ( 1990, 1, 1 ).getTime (), new GregorianCalendar ( 2000, 3, 12 ).getTime () ));
		
		ts = em.getTransaction ();
		ts.begin ();
		jrDao.clean ( 0 );
		ts.commit ();
		
		jrDao.setEntityManager ( em = emProvider.newEntityManager () );
		log = jrDao.find ( 1 );
		assertTrue ( "Job register entries not removed!", log.isEmpty () );
	}
}
