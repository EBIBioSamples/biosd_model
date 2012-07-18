/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.organizational;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.core_model.toplevel.DefaultAccessibleAnnotatable;

/**
 * A collection of samples, put together for whatever criteria (e.g., coming from the same experiment, prepared for the 
 * same project, other organisational reasons). This class corresponds to the concept with the same name in SampleTAB. 
 *
 * <dl><dt>date</dt><dd>Jul 17, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table( name = "bio_sample_group" )
@SequenceGenerator ( name = "hibernate_seq", sequenceName = "bio_sample_group_seq" )
public class BioSampleGroup extends DefaultAccessibleAnnotatable
{
	protected BioSampleGroup () {
		super ();
	}

	public BioSampleGroup ( String acc ) {
		super ( acc );
	}

	private Set<BioSample> samples = new HashSet<BioSample> ();

	public Set<BioSample> getSamples ()
	{
		return samples;
	}

	public void setSamples ( Set<BioSample> samples )
	{
		this.samples = samples;
	}
	
	public boolean addSample ( BioSample smp ) {
		return this.samples.add ( smp );
	}
}
