/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.expgraph;

import java.util.Set;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import uk.ac.ebi.fg.core_model.expgraph.BioMaterial;
import uk.ac.ebi.fg.core_model.expgraph.Node;
import uk.ac.ebi.fg.core_model.expgraph.Process;
import uk.ac.ebi.fg.core_model.expgraph.Product;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;

/**
 * A biological sample, corresponding to the entity identified by 'Sample Name' in BioSD. This is essentially a specific 
 * type of {@link BioMaterial}, where the relations about the 'complex model' (see {@link Node}) are disabled, since we
 * only use the simple derivation model (see {@link #getDerivedFrom()}) in BioSD.
 *
 * <dl><dt>date</dt><dd>Jul 17, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@Table ( name = "bio_sample" )
@SequenceGenerator ( name = "hibernate_seq", sequenceName = "bio_sample_seq" )
@AssociationOverrides({ 
	@AssociationOverride ( name = "derivedFrom", joinTable = @JoinTable ( name = "biosample_derivation" ) ), 
	@AssociationOverride ( name = "annotations", joinTable = @JoinTable( name = "biosample_annotation" ) )
})
@SuppressWarnings ( "rawtypes" )
public class BioSample extends BioMaterial<ExperimentalPropertyValue>
{
	private void throwComplexModelNotSupported() 
	{
		throw new UnsupportedOperationException ( 
			"Internal error: the complex model, which includes processing steps instead of straight derivation relations, " +
			"is not supported in BioSD, you should not use this method (and probably read the documentation)." 
		);
	}
	
	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public Set<Process> getUpstreamProcesses () 
	{
		throwComplexModelNotSupported ();
		return null;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	protected void setUpstreamProcesses ( Set<Process> upstreamProcs )
	{
		throwComplexModelNotSupported ();
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public boolean removeUpstreamProcess ( Process proc )
	{
		throwComplexModelNotSupported ();
		return false;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public Set<Process> getDownstreamProcesses ()
	{
		throwComplexModelNotSupported ();
		return null;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	protected void setDownstreamProcesses ( Set<Process> downstreamProcs )
	{
		throwComplexModelNotSupported ();
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public boolean removeDownstreamProcess ( Process proc )
	{
		throwComplexModelNotSupported ();
		return false;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public <P extends Product> Set<P> getAllDerivedFrom ()
	{
		throwComplexModelNotSupported ();
		return null;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public <P extends Product> Set<P> getAllDerivedInto ()
	{
		throwComplexModelNotSupported ();
		return null;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public Set<Process> getUpstreamNodes ()
	{
		throwComplexModelNotSupported ();
		return null;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	protected void setUpstreamNodes ( Set<Process> upstreamNodes )
	{
		throwComplexModelNotSupported ();
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public boolean addUpstreamNode ( Process node )
	{
		throwComplexModelNotSupported ();
		return false;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public boolean removeUpstreamNode ( Process node )
	{
		throwComplexModelNotSupported ();
		return false;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public Set<Process> getDownstreamNodes ()
	{
		throwComplexModelNotSupported ();
		return null;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	protected void setDownstreamNodes ( Set<Process> downstreamNodes )
	{
		throwComplexModelNotSupported ();
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public boolean addDownstreamNode ( Process node )
	{
		throwComplexModelNotSupported ();
		return false;
	}

	/**
	 * The upstream model is not supported by BioSD, which uses the simpler model based on {@link #getDerivedFrom() straight derivation}. 
	 */
	@Transient
	@Override
	public boolean removeDownstreamNode ( Process node )
	{
		throwComplexModelNotSupported ();
		return false;
	}
	
}
