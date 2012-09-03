/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.organizational;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
public class BioSampleGroup extends DefaultAccessibleAnnotatable
{
	protected BioSampleGroup () {
		super ();
	}

	public BioSampleGroup ( String acc ) {
		super ( acc );
	}

	private Set<BioSample> samples = new HashSet<BioSample> ();

	@ManyToMany ( cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable ( name = "bio_sample_sample_group", 
		joinColumns = @JoinColumn ( name = "group_id" ), inverseJoinColumns = @JoinColumn ( name = "sample_id" ) )
	public Set<BioSample> getSamples ()
	{
		return samples;
	}

	protected void setSamples ( Set<BioSample> samples )
	{
		this.samples = samples;
	}
	
	public boolean addSample ( BioSample smp ) 
	{
		if ( !this.samples.add ( smp ) ) return false;
		smp.addGroup ( this );
		return true;
	}
	
	public boolean deleteSample ( BioSample smp ) 
	{
		if ( !this.samples.remove ( smp ) ) return false;
		smp.deleteGroup ( this );
		return true;
	}
	
}
