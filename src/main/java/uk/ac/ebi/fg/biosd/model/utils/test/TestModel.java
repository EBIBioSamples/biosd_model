package uk.ac.ebi.fg.biosd.model.utils.test;

import java.util.Collections;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicType;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;
import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.OntologyEntryDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.xref.ReferenceSourceDAO;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * A mock-up model to be used for testing purposes.
 *
 * <dl><dt>date</dt><dd>Sep 3, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class TestModel
{	
	
	public BioSample smp1;
	public BioSample smp2;
	public BioSample smp3;
	public BioSample smp4;
	public BioSample smp5;
	public BioSample smp6;
	
	public BioCharacteristicType ch1;
	public BioCharacteristicValue cv1;
	public BioCharacteristicType ch2;
	public BioCharacteristicValue cv2;
	public UnitDimension timeDim;
	public Unit monthsUnit;
	public BioCharacteristicValue cv3;
	public BioCharacteristicValue cv4;
	public BioCharacteristicType ch3;
	public BioCharacteristicValue cv5;
	public BioCharacteristicValue cv5b;

	public UnitDimension concentrationUnit;
	public Unit percent;
	
	public OntologyEntry oe1, oe2;
	public ReferenceSource src1, src2;
	
	public ContactRole cntRole1;

	public BioSampleGroup sg1;
	public BioSampleGroup sg2;
	
	public MSI msi;
	public Contact cnt;
	

	/**
	 * Calls {@link #TestModel(String)} with "".
	 */
	public TestModel () {
		this ( "" );
	}

	/**
	 * 	<pre>
	 *  smp1 -----> smp3 ----> smp4 ---> smp6
	 *  smp2 ----/       \---> smp5 --/     
	 *  
	 *  sg1 contains (1,2,3)
	 *  sg2 contains (3,4,5,6) 
	 *  smp1 is a reference layer sample.
	 *  </pre>        
	 */
	public TestModel ( String prefix )
	{
		smp1 = new BioSample ( prefix + "smp1" );
		smp2 = new BioSample ( prefix + "smp2" );
		smp3 = new BioSample ( prefix + "smp3" );
		smp4 = new BioSample ( prefix + "smp4" );
		smp5 = new BioSample ( prefix + "smp5" );
		smp6 = new BioSample ( prefix + "smp6" );
		
		
		// These relations are symmetric
		smp3.addDerivedFrom ( smp1 );
		smp2.addDerivedInto ( smp3 );

		smp4.addDerivedFrom ( smp3 );
		smp5.addDerivedFrom ( smp3 );

		smp6.addDerivedFrom ( smp4 );
		smp5.addDerivedInto ( smp6 );
		
		smp1.setInReferenceLayer ( true );
		
		ch1 = new BioCharacteristicType ( "Organism" );
		cv1 = new BioCharacteristicValue ( "mus-mus", ch1 );
      cv1.addOntologyTerm ( oe1 = new OntologyEntry ( prefix + "123", src1 = new ReferenceSource ( "EFO", null ) ) );
      cv1.addOntologyTerm ( oe2 = new OntologyEntry ( prefix + "456", src2 = new ReferenceSource ( "MA", null ) ) );
		smp1.addPropertyValue ( cv1 );
		
		ch2 = new BioCharacteristicType ();
		ch2.setTermText ( "Age" );
		cv2 = new BioCharacteristicValue ();
		cv2.setTermText ( "10" );
		cv2.setType ( ch2 );
		timeDim = new UnitDimension ( "time" );
		monthsUnit = new Unit ( "months", timeDim );
		cv2.setUnit ( monthsUnit );
		smp1.addPropertyValue ( cv2 );

		// Cannot be re-used, you need to create a new one, even if it is the same
		// TODO: change JPA annotations and allow for n-m
		cv3 = new BioCharacteristicValue ( "mus-mus", ch1 );
		smp2.addPropertyValue ( cv3 );
		
		cv4 = new BioCharacteristicValue ( "8", ch2 );
		// Units can be recycled instead
		cv4.setUnit ( monthsUnit );
		smp2.addPropertyValue ( cv4 ); 
		
		
		ch3 = new BioCharacteristicType ();
		ch3.setTermText ( "concentration" );
		cv5 = new BioCharacteristicValue ( "2%", ch3 );
		concentrationUnit = new UnitDimension ( "Concentration" );
		percent = new Unit ( "percent", concentrationUnit );
		cv5.setUnit ( percent );
		
		smp4.addPropertyValue ( cv5 );
		
		
		sg1 = new BioSampleGroup ( prefix + "sg1" );
		sg2 = new BioSampleGroup ( prefix + "sg2" );
		
		// Again don't share property values among different owners, you risk integrity-constraint errors and the like.
		// sg2.addPropertyValue ( cv5 );
		cv5b = new BioCharacteristicValue ( "2%", ch3 );
		cv5.setUnit ( percent );
		sg2.addPropertyValue ( cv5b );
		
		sg1.addSample ( smp1 );
		smp2.addGroup ( sg1 );
		sg1.addSample ( smp3 );
		
		sg2.addSample ( smp4 );
		sg2.addSample ( smp5 );
		sg2.addSample ( smp6 );
		smp3.addGroup ( sg2 ); // same sample in two groups
		
		msi = new MSI ( prefix + "msi1" );
		msi.setReleaseDate ( new GregorianCalendar( 2010, 3, 21 ).getTime () );

		
		cnt = new Contact ();
		cnt.setFirstName ( prefix + "Mister" );
		cnt.setLastName ( prefix + "Test" );
		Collections.addAll ( cnt.getContactRoles (), cntRole1 = new ContactRole ( prefix + "test-submitter" ) );
		msi.addContact ( cnt );



		msi.addSample ( smp1 );
		msi.addSample ( smp2 );
		msi.addSample ( smp3 );
		msi.addSample ( smp4 );
		msi.addSample ( smp5 );
		msi.addSample ( smp6 );

		msi.addSampleGroup ( sg1 );
		msi.addSampleGroup ( sg2 );

	}
	
	public void delete ( EntityManager em )
	{
		AccessibleDAO<MSI> msidao = new AccessibleDAO<MSI> ( MSI.class, em );
		msidao.delete ( msi );
		
		AccessibleDAO<BioSampleGroup> sgdao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, em );
		sgdao.delete ( sg1 );
		sgdao.delete ( sg2 );
		
		AccessibleDAO<BioSample> smpDao = new AccessibleDAO<BioSample> ( BioSample.class, em );
		smpDao.delete ( smp1 );
		smpDao.delete ( smp2 );
		smpDao.delete ( smp3 );
		smpDao.delete ( smp4 );
		smpDao.delete ( smp5 );
		smpDao.delete ( smp6 );
		
		CVTermDAO<ContactRole> roleDao = new CVTermDAO<ContactRole> ( ContactRole.class, em );
		roleDao.delete ( cntRole1 );
		
		OntologyEntryDAO<OntologyEntry> oeDao = new OntologyEntryDAO<OntologyEntry> ( OntologyEntry.class, em );
		oeDao.delete ( oe1 );
		oeDao.delete ( oe2 );
		
		ReferenceSourceDAO<ReferenceSource> srcDao = new ReferenceSourceDAO<ReferenceSource> ( ReferenceSource.class, em );
		srcDao.delete ( src1 );
		srcDao.delete ( src2 );
	}	
}
