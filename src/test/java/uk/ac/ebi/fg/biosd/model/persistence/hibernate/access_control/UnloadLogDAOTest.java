package uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control;

import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*; 

import uk.ac.ebi.fg.biosd.model.application_mgmt.UnloadLogEntry;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.UnloadLogDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * Some testing for {@link UnloadLogDAO}.
 *
 * <dl><dt>date</dt><dd>Apr 24, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnloadLogDAOTest
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
		UnloadLogDAO uloadDao = new UnloadLogDAO ( em );
		
		UnloadLogEntry 
		  loge1 = new UnloadLogEntry ( "test.type", "test.acc1" ),
		  loge2 = new UnloadLogEntry ( "test.type", "test.acc2" );
		
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		uloadDao.create ( loge1 );
		uloadDao.create ( loge2 );
		ts.commit ();
		
		uloadDao.setEntityManager ( em = emProvider.newEntityManager () );
		List<UnloadLogEntry> log = uloadDao.find ( 1 );
		
		assertEquals ( "Log entry storage didn't work!", 2, log.size () );
		assertTrue ( "Wrong data retrieved from the undeletion log!", 
			loge1.equals ( log.get ( 0 ) ) && loge2.equals ( log.get ( 1 ) ) 
			|| loge1.equals ( log.get ( 1 ) ) && loge2.equals ( log.get ( 0 ) ) 
	  );

		assertTrue ( "Wrong result for wasDeleted ()", uloadDao.wasDeleted ( loge1.getEntityType (), loge1.getAcc (), 1 ) );
		assertFalse ( "Wrong result for wasDeleted ()", uloadDao.wasDeleted ( 
			loge1.getEntityType (), loge1.getAcc (), 
			new GregorianCalendar ( 1990, 1, 1 ).getTime (), new GregorianCalendar ( 2000, 3, 12 ).getTime () ));
		
		ts = em.getTransaction ();
		ts.begin ();
		uloadDao.delete ( 0 );
		ts.commit ();
		
		uloadDao.setEntityManager ( em = emProvider.newEntityManager () );
		log = uloadDao.find ( 1 );
		assertTrue ( "Undelete Log entries not removed!", log.isEmpty () );
	}
}
