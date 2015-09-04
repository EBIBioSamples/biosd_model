package uk.ac.ebi.fg.biosd.model.resources;

import org.apache.commons.lang3.ArrayUtils;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.BioSdSchemaEnhancer;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.fg.persistence.hibernate.schema_enhancer.DbSchemaEnhancer;

/**
 * The customisation of {@link Resources} needed for the biosd_model package.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>2 Sep 2015</dd>
 *
 */
public class BioSdResources extends Resources
{
	
	/**
	 * Overrides core_model.
	 */
	@Override
	public int getPriority () {
		return super.getPriority () + 10;
	}

	@Override
	public String[] getPackagesToScan ()
	{
		return ArrayUtils.add ( super.getPackagesToScan (), 0, "uk.ac.ebi.fg.biosd.model.**.*" );
	}

	@Override
	public DbSchemaEnhancer[] getDbSchemaEnhancers ()
	{
		return ArrayUtils.add ( super.getDbSchemaEnhancers (), new BioSdSchemaEnhancer () );
	}
	
}
