package org.batgen.generators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GenUtil {
    public static final String PROTECTED_CODE = "PROTECTED CODE";
    public static final String TAB = "    ";

    /**
     * @return a path derived from the passed in package.
     */
    public static String packageToPath( String pkg ) {
        return "src/main/java/" + pkg.replace( ".", "/" );
    }

    /**
     * Writes the content of the string to a specified file.
     * 
     * @param fileName
     *            Fully qualified filename with path
     * @param content
     */
    public static void writeToFile( String fileName, String content ) {
        File file = new File( fileName );

        file = new File( file.getParent() );
        if ( !file.exists() )
            file.mkdirs();
        try {
            PrintWriter pw = new PrintWriter( fileName );
            pw.write( content );
            pw.close();

        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Used for finding the protected lines in the original file.
     *
     * @param fileName
     *            filename and path of the file to be checked for protected
     *            lines
     * @return returns a list of strings
     */
    public static List<String> getProtectedLines( String fileName ) {
        List<String> protectedLines = new ArrayList<String>();

        File exportFile = new File( fileName );
        BufferedReader br = null;

        try {

            br = new BufferedReader( new FileReader( exportFile ) );
            String line = "";

            // find start of protection
            while ( !line.contains( PROTECTED_CODE ) ) {

                line = br.readLine();
                if ( line == null )
                    break;
            }

            // read next line
            line = br.readLine();
            while ( line != null ) {

                line = "\n" + line;
                protectedLines.add( line );
                line = br.readLine();
            }

            br.close();

        } catch ( FileNotFoundException e ) {
            // Ignored if file doesn't exist.

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return protectedLines;
    }

}
