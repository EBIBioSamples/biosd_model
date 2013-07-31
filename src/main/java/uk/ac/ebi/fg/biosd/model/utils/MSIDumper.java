/*
 * 
 */
package uk.ac.ebi.fg.biosd.model.utils;

import java.io.PrintStream;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.expgraph.Product;
import uk.ac.ebi.fg.core_model.utils.expgraph.DirectDerivationGraphDumper;

/**
 * Handy little helper that prints out the content of a SampleTab submission (i.e., an {@link MSI} object and linked 
 * objects).
 *
 * <dl><dt>date</dt><dd>Jan 21, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MSIDumper
{
	public static void dump ( PrintStream out, MSI msi )
	{
		out.println ( "\n====================================================================" );
		out.println ( msi );
		
		out.println ( "\n  --------------- Sample Groups:" );
		out.println ( msi.getSampleGroups () );
		
		out.println ( "\n  --------------- Samples:" );
		new DirectDerivationGraphDumper ()
		{
			@Override
			public void dumpProduct ( PrintStream out, Product<?> node )
			{
				super.dumpProduct ( out, node );
				out.println ( "\n    --------- Linked to Groups:" );
				out.println ( ((BioSample) node).getGroups () + "\n" );
			}
			
		}.dump ( out, msi.getSamples () );
		out.println ( "====================================================================\n\n" );
	}
}
