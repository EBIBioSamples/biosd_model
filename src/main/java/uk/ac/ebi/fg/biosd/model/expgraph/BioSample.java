/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.expgraph;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.core_model.expgraph.BioMaterial;
import uk.ac.ebi.fg.core_model.expgraph.Node;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;

/**
 * A biological sample, corresponding to the entity identified by 'Sample Name' in BioSD. This is essentially a specific 
 * type of {@link BioMaterial}, where you're supposed to use the 'direct-derivation model' (see {@link Node}).
 *
 * <dl><dt>date</dt><dd>Jul 17, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@DiscriminatorValue ( "bio_sample" )
@SuppressWarnings ( "rawtypes" )
public class BioSample extends BioMaterial<ExperimentalPropertyValue>
{
	private Set<BioSampleGroup> groups = new HashSet<BioSampleGroup> (); 
	
	public BioSample () {
		super ();
	}

	public BioSample ( String acc ) {
		super ( acc );
	}
	

	@ManyToMany ( mappedBy = "samples" )
	public Set<BioSampleGroup> getGroups ()
	{
		return groups;
	}

	protected void setGroups ( Set<BioSampleGroup> groups )
	{
		this.groups = groups;
	}

	public boolean addGroup ( BioSampleGroup sg ) 
	{
		if ( !this.groups.add ( sg ) ) return false;
		sg.addSample ( this );
		return true;
	}

	public boolean deleteGroup ( BioSampleGroup sg ) 
	{
		if ( !this.groups.remove ( sg ) ) return false;
		sg.deleteSample ( this );
		return true;
	}
}
