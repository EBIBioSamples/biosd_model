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
		DirectDerivationGraphDumper gdumper = new DirectDerivationGraphDumper ()
		{
			@Override
			public void dumpProduct ( PrintStream out, Product<?> node )
			{
				super.dumpProduct ( out, node );
				out.println ( "\n    --------- Linked to Groups:" );
				out.println ( ((BioSample) node).getGroups () + "\n" );

				out.println ( "\n    --------- Derived From:" );
				String sep = "";
				for ( Product<?> smp: ((BioSample) node).getDerivedFrom () ) {
					out.print ( sep + smp.getAcc () ); sep = ", ";
				}
				out.println ( "\n" );
				
				out.println ( "\n    --------- Derived Into:" );
				sep = "";
				for ( Product<?> smp: ((BioSample) node).getDerivedInto () ) {
					out.print ( sep + smp.getAcc () ); sep = ", ";
				}
				out.println ( "\n" );
				
			}
			
		};
		gdumper.dump ( out, msi.getSamples () );
		
		out.println ( "\n  --------------- Sample Group Refs:" );
		out.println ( msi.getSampleGroupRefs () );

		out.println ( "\n  --------------- Sample Refs:" );
		gdumper.dump ( out, msi.getSampleRefs () );
		
		out.println ( "====================================================================\n\n" );
	}
}
