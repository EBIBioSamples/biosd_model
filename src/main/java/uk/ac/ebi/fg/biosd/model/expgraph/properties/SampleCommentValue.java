/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.expgraph.properties;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.utils.test.TestModel;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;

/**
 * 
 * Used to accommodate SampleTAB headers of type Comment[X]. This can be attached to either {@link BioSample}s or
 * {@link BioSampleGroup}s.
 *
 * TODO: this is not tested yet in the {@link TestModel mock-up model}.
 * TOOD: the SampleTab parser needs to be changed. 
 *
 * <dl><dt>date</dt><dd>Oct 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
@Entity
@DiscriminatorValue ( "sample_comment" )
public class SampleCommentValue extends ExperimentalPropertyValue<SampleCommentType>
{

	public SampleCommentValue ()
	{
		super ();
	}

	public SampleCommentValue ( String termText, SampleCommentType type )
	{
		super ( termText, type );
	}

}
