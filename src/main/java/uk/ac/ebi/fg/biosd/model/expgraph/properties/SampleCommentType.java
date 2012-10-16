/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.expgraph.properties;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;

/**
 * Used to accommodate SampleTAB headers of type Comment[X]. This can be attached to either {@link BioSample}s or
 * {@link BioSampleGroup}s.
 *
 * <dl><dt>date</dt><dd>Oct 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SampleCommentType extends ExperimentalPropertyType
{
	public SampleCommentType ()
	{
		super ();
	}

	public SampleCommentType ( String termText )
	{
		super ( termText );
	}

}
