package cytoscape.data;

import cytoscape.data.readers.GraphReader;
import cytoscape.util.CyFileFilter;
import cytoscape.util.GMLFileFilter;
import cytoscape.util.SIFFileFilter;
import cytoscape.util.XGMMLFileFilter;

import java.awt.Component;
import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.swing.JOptionPane;

/**
 * Central registry for all Cytoscape import classes.
 *
 * @author Cytoscape Development Team.
 */
public class ImportHandler {

    /**
     * Filter Type:  NETWORK.
     */
    public static String GRAPH_NATURE = "NETWORK";

    /**
     * Filter Type:  NODE.
     */
    public static String NODE_NATURE = "NODE";

    /**
     * Filter Type:  EDGE.
     */
    public static String EDGE_NATURE = "EDGE";

    /**
     * Filer Type:  PROPERTIES.
     */
    public static String PROPERTIES_NATURE = "PROPERTIES";

    /**
     * Set of all registered CyFileFilter Objects.
     */
    protected static Set cyFileFilters = new HashSet();

    /**
     * Constructor.
     */
    public ImportHandler() {
        //  By default, register SIF, XGMML and GML File Filters
        init();
    }

    /**
     * Initialize ImportHandler.
     */
    private void init() {
        addFilter(new SIFFileFilter());
        addFilter(new XGMMLFileFilter());
        addFilter(new GMLFileFilter());
    }

    /**
     * Registers a new CyFileFilter.
     *
     * @param cff CyFileFilter.
     * @return true indicates filter was added successfully.
     */
    public boolean addFilter(CyFileFilter cff) {
        Set check = cff.getExtensionSet();

        for (Iterator it = check.iterator(); it.hasNext();) {
            String extension = (String) it.next();
            if (getAllExtensions().contains(extension)
                    && ! extension.equals("xml")) {
                return false;
            }
        }
        cyFileFilters.add(cff);
        return true;
    }

    /**
     * Registers an Array of CyFileFilter Objects.
     *
     * @param cff Array of CyFileFilter Objects.
     * @return true indicates all filters were added successfully.
     */
    public boolean addFilter(CyFileFilter[] cff) {
        //first check if suffixes are unique
        boolean flag = true;

        for (int j = 0; (j < cff.length) && (flag == true); j++) {
            flag = addFilter(cff[j]);
        }
        return flag;
    }

    /**
     * Gets the GraphReader that is capable of reading the specified file.
     *
     * @param fileName File name or null if no reader is capable of reading the file.
     * @return GraphReader capable of reading the specified file.
     */
    public GraphReader getReader(String fileName) {
        //  check if fileType is available
        CyFileFilter cff;

        for (Iterator it = cyFileFilters.iterator(); it.hasNext();) {
            cff = (CyFileFilter) it.next();

            if (cff.accept(fileName)) {
                return cff.getReader(fileName);
            }
        }
        return null;
    }

    /**
     * Gets descriptions for all registered filters, which are of the type:  fileNature.
     *
     * @param fileNature type:  GRAPH_NATURE, NODE_NATURE, EDGE_NATURE, etc.
     * @return Collection of String descriptions, e.g. "XGMML files"
     */
    public Collection getAllTypes(String fileNature) {
        Collection ans = new HashSet();
        CyFileFilter cff;

        for (Iterator it = cyFileFilters.iterator(); it.hasNext();) {
            cff = (CyFileFilter) it.next();

            //if statement to check if nature equals fileNature
            if (cff.getFileNature().equals(fileNature)) {
                cff.setExtensionListInDescription(false);
                ans.add(cff.getDescription());
                cff.setExtensionListInDescription(true);
            }
        }
        return ans;
    }

    /**
     * Gets a collection of all registered file extensions.
     *
     * @return Collection of Strings, e.g. "xgmml", "sif", etc.
     */
    public Collection getAllExtensions() {
        Collection ans = new HashSet();
        for (Iterator it = cyFileFilters.iterator(); it.hasNext();) {
            ans.addAll(((CyFileFilter) (it.next())).getExtensionSet());
        }
        return ans;
    }

    /**
     * Gets a collection of all registered filter descriptions.
     * Descriptions are of the form:  "{File Description} ({file extensions})".
     * For example: "GML files (*.gml)"
     *
     * @return Collection of Strings, e.g. "GML files (*.gml)", etc.
     */
    public Collection getAllDescriptions() {
        Collection ans = new HashSet();
        for (Iterator it = cyFileFilters.iterator(); it.hasNext();) {
            ans.add(((CyFileFilter) it.next()).getDescription());
        }
        return ans;
    }

    /**
     * Gets the name of the filter which is capable of reading the specified file.
     *
     * @param fileName File Name.
     * @return name of filter capable of reading the specified file.
     */
    public String getFileType(String fileName) {
        CyFileFilter cff;
        String ans = null;
        for (Iterator it = cyFileFilters.iterator(); it.hasNext();) {
            cff = (CyFileFilter) it.next();

            if (cff.accept(fileName)) {
                cff.setExtensionListInDescription(false);
                ans = (cff.getDescription());
                cff.setExtensionListInDescription(true);
            }
        }
        return ans;
    }

    /**
     * Gets a list of all registered filters plus a catch-all super set filter.
     *
     * @return List of CyFileFilter Objects.
     */
    public List getAllFilters() {
        List ans = new ArrayList();

        for (Iterator it = cyFileFilters.iterator(); it.hasNext();) {
            ans.add((CyFileFilter) it.next());
        }
        if (ans.size() > 1) {
            String[] allTypes = concatAllExtensions(ans);
            ans.add(new CyFileFilter(allTypes, "All Natures"));
        }
        return ans;
    }

    /**
     * Gets a list of all registered filters, which are of type:  fileNature,
     * plus a catch-all super set filter.
     *
     * @param fileNature type:  GRAPH_NATURE, NODE_NATURE, EDGE_NATURE, etc.
     * @return List of CyFileFilter Objects.
     */
    public List getAllFilters(String fileNature) {
        List ans = new ArrayList();
        CyFileFilter cff;

        for (Iterator it = cyFileFilters.iterator(); it.hasNext();) {
            cff = (CyFileFilter) it.next();

            //if statement to check if nature equals fileNature
            if (cff.getFileNature().equals(fileNature)) {
                ans.add(cff);
            }
        }
        if (ans.size() > 1) {
            String[] allTypes = concatAllExtensions(ans);
            ans.add(new CyFileFilter(allTypes, "All "
                    + fileNature.toLowerCase() + " files", fileNature));
        }
        return ans;
    }

    /**
     * Unregisters all registered File Filters (except default file filters.)
     * Resets everything with a clean slate.
     */
    public void resetImportHandler() {
        cyFileFilters = new HashSet();
        init();
    }

    /**
     * Creates a String array of all extensions
     */
    private String[] concatAllExtensions(List cffs) {
        Set ans = new HashSet();

        for (Iterator it = cffs.iterator(); it.hasNext();) {
            ans.addAll(((CyFileFilter) (it.next())).getExtensionSet());
        }

        String[] stringAns = (String[]) ans.toArray(new String[0]);
        return stringAns;
    }
    
    /**
     * Download a temporary file from the given URL. The file will be saved in the 
     * temporary directory and will be deleted after Cytoscape exits.
     */
	public static File downloadFromURL(Component pParent, String pURLstr, Proxy pProxyServer)
	{				
		URL theUrl;
		try {
			theUrl = new URL(pURLstr);			
		}
		catch (MalformedURLException mExp) {
		    JOptionPane.showMessageDialog(pParent, "URL error!", "Warning", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		
		URLConnection conn = null;;
		
		try {
			if (pProxyServer == null) {
				conn = theUrl.openConnection();							
			}
			else {
				conn = theUrl.openConnection(pProxyServer);
			}			
		}
		catch (IOException ioEx) {
		    JOptionPane.showMessageDialog(pParent, "Failed to connect to the remote server!", "Warning", JOptionPane.INFORMATION_MESSAGE);
			return null;			
		}			

		// create a tmp file, which will be deleted upon exit
		String fileName = theUrl.getFile();
		String baseFilename = fileName.substring(fileName.lastIndexOf("/")+1);
		
		java.util.Properties theProperties = System.getProperties();
		File tmpFile = new File(theProperties.getProperty("java.io.tmpdir"), baseFilename);
		
	    tmpFile.deleteOnExit();

	    BufferedWriter out = null;
	    BufferedReader in = null;
	    try {
			// set the wait cursor
			pParent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		     out = new BufferedWriter(new FileWriter(tmpFile));
			 in = new BufferedReader(new InputStreamReader(conn.getInputStream()));	    	
	    }
	    catch (IOException ioExp) {
		    JOptionPane.showMessageDialog(pParent, "Failed to download the file!", "Warning", JOptionPane.INFORMATION_MESSAGE);
			pParent.setCursor(Cursor.getDefaultCursor());

		    return null;				    	
	    }
	    	    
		try {
			 String inputLine;

		     // Write to temp file
			 while ((inputLine = in.readLine()) != null) 
			 {
			     out.write(inputLine+"\n");
			 }
			 in.close();
		     out.close();				
		}
		catch (IOException ioExp)
		{
		    JOptionPane.showMessageDialog(pParent, "Failed to write to a tmp file on local disk!", "Warning", JOptionPane.INFORMATION_MESSAGE);
			return null;				    	
		}		
		finally {
			// Always restore the cursor, even there is exception
			pParent.setCursor(Cursor.getDefaultCursor());
		}			

		return tmpFile;
	}//downloadFromURL()
}