package uk.ac.ebi.fg.biosd.model.performance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * TODO: Comment me!
 * 
 * <dl>
 * <dt>date</dt>
 * <dd>12 Jul 2013</dd>
 * </dl>
 * 
 * @author Marco Brandizi
 * 
 */
public class AttributePerformanceTest
{

	static class Counter extends Number
	{
		private static final long serialVersionUID = 698465133888087160L;

		private int count;

		public Counter ()
		{
			count = 0;
		}

		public Counter ( int init )
		{
			count = init;
		}

		public int inc ()
		{
			return ++count;
		}

		public int add ( int v )
		{
			return count += v;
		}

		public int dec ()
		{
			return --count;
		}

		@Override
		public int intValue ()
		{
			return count;
		}

		@Override
		public String toString ()
		{
			return String.valueOf ( count );
		}

		@Override
		public double doubleValue ()
		{
			return count;
		}

		@Override
		public float floatValue ()
		{
			return count;
		}

		@Override
		public long longValue ()
		{
			return count;
		}

	}

	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( Resources.getInstance ().getEntityManagerFactory () );

	private Map<String, Counter> counts = new HashMap<String, Counter> ();
	
	private long startTime;

	@Before
	public void init () {
		startTime = System.currentTimeMillis ();
	}
	
	
	@Test // @Ignore ( "Not a real Junit test and too time-consuming" )
	public void testSimpleLoop ()
	{
		Map<String, Counter> counts = new HashMap<String, Counter> ();
		EntityManager em = emProvider.getEntityManager ();
		Query pvq = em.createQuery ( "SELECT pt FROM BioSample smp JOIN smp.propertyValues pv JOIN pv.type pt WHERE smp.id = :smpId" );
		long nitems = 0;
		for ( BioSampleGroup sg: (List<BioSampleGroup>) em.createQuery ( "FROM BioSampleGroup sg" ).getResultList () )
		{
			for ( BioSample smp: sg.getSamples () )
			{
				for ( ExperimentalPropertyValue<?> pv: smp.getPropertyValues () )
				// for ( ExperimentalPropertyType pt: (List<ExperimentalPropertyType>) pvq.setParameter ( "smpId", smp.getId ()
				// ).getResultList () )
				{
					// String ptypeLabel = pt.getTermText ();
					String ptypeLabel = pv.getType ().getTermText ();
					Counter ct = counts.get ( ptypeLabel );
					if ( ct == null )
						counts.put ( ptypeLabel, new Counter ( 1 ) );
					else
						ct.inc ();
				}
				System.out.println ( "\n\n\n _____________ Done " + ( ++nitems ) + " samples _______________\n\n\n\n" );
				if ( nitems >= 100 )
					break;
			}
			if ( nitems >= 100 )
				break;
		}
	}

	@Test //@Ignore ( "Not a real Junit test and too time-consuming" )
	public void testSimpleLoopWithSQL ()
	{
		EntityManager em = emProvider.getEntityManager ();
		Query ptq = em.createNativeQuery ( "select pt.TERM_TEXT "
				+ "from BIO_PRODUCT smp join pruduct_pv smp2pv on smp.id = smp2pv.OWNER_ID "
				+ "join EXP_PROP_VAL pv on smp2pv.PV_ID = pv.id " + "join EXP_PROP_TYPE pt on pv.TYPE_ID = pt.id "
				+ "where smp.id = :smpId" );
		long nitems = 0;
		for ( BioSampleGroup sg: (List<BioSampleGroup>) em.createQuery ( "FROM BioSampleGroup sg" ).getResultList () )
		{
			for ( BioSample smp: sg.getSamples () )
			{
				for ( String ptypeLabel: (List<String>) ptq.setParameter ( "smpId", smp.getId () ).getResultList () )
				{
					Counter ct = counts.get ( ptypeLabel );
					if ( ct == null )
						counts.put ( ptypeLabel, new Counter ( 1 ) );
					else
						ct.inc ();
				}
				System.out.println ( "\n\n\n _____________ Done " + ( ++nitems ) + " samples _______________\n\n\n\n" );
				if ( nitems >= 100 )
					break;
			}
			if ( nitems >= 100 )
				break;
		}
	}

	@After
	public void report ()
	{
		System.out.println ( "\n\n _______ The loop took: " + ( System.currentTimeMillis () - startTime ) / 1000d + "s"  );
	}
}
