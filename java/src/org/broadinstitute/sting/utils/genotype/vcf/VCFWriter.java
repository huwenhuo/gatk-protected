package org.broadinstitute.sting.utils.genotype.vcf;


import java.io.*;
import java.util.TreeSet;

/**
 * this class writers VCF files
 */
public class VCFWriter {

    
    // the VCF header we're storing
    private VCFHeader mHeader;

    // the print stream we're writting to
    BufferedWriter mWriter;
    private final String FIELD_SEPERATOR = "\t";

    /**
     * create a VCF writer, given a VCF header and a file to write to
     *
     * @param header   the VCF header
     * @param location the file location to write to
     */
    public VCFWriter(VCFHeader header, File location) {
        FileOutputStream output;
        try {
            output = new FileOutputStream(location);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to create VCF file at location: " + location);
        }
        initialize(header, output);
    }


    /**
     * create a VCF writer, given a VCF header and a file to write to
     *
     * @param header   the VCF header
     * @param location the file location to write to
     */
    public VCFWriter(VCFHeader header, OutputStream location) {
        initialize(header, location);
    }

    private void initialize(VCFHeader header, OutputStream location) {
        this.mHeader = header;
        mWriter = new BufferedWriter(
                new OutputStreamWriter(location));
        try {
            // the fileformat field needs to be written first
            TreeSet<String> allMetaData = new TreeSet<String>(header.getMetaData());
            for ( String metadata : allMetaData ) {
                if ( metadata.startsWith(VCFHeader.FILE_FORMAT_KEY) ) {
                    mWriter.write(VCFHeader.METADATA_INDICATOR + metadata + "\n");
                    break;
                }
                else if ( metadata.startsWith(VCFHeader.OLD_FILE_FORMAT_KEY) ) {
                    mWriter.write(VCFHeader.METADATA_INDICATOR + VCFHeader.FILE_FORMAT_KEY + metadata.substring(VCFHeader.OLD_FILE_FORMAT_KEY.length()) + "\n");
                    break;
                }
            }

            // write the rest of the header meta-data out
            for ( String metadata : header.getMetaData() ) {
                if ( !metadata.startsWith(VCFHeader.FILE_FORMAT_KEY) && !metadata.startsWith(VCFHeader.OLD_FILE_FORMAT_KEY) )
                    mWriter.write(VCFHeader.METADATA_INDICATOR + metadata + "\n");                
            }
            
            // write out the column line
            StringBuilder b = new StringBuilder();
            b.append(VCFHeader.HEADER_INDICATOR);
            for (VCFHeader.HEADER_FIELDS field : header.getHeaderFields()) b.append(field + FIELD_SEPERATOR);
            if (header.hasGenotypingData()) {
                b.append("FORMAT" + FIELD_SEPERATOR);
                for (String field : header.getGenotypeSamples()) b.append(field + FIELD_SEPERATOR);
            }
            mWriter.write(b.toString() + "\n");
        }
        catch (IOException e) {
            throw new RuntimeException("IOException writing the VCF header", e);
        }
    }

    /**
     * output a record to the VCF file
     *
     * @param record the record to output
     */
    public void addRecord(VCFRecord record) {
        String vcfString = record.toStringEncoding(mHeader);
        try {
            mWriter.write(vcfString + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Unable to write the VCF object to a file");
        }

    }


    /**
     * attempt to close the VCF file
     */
    public void close() {
        try {
            mWriter.flush();
            mWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close VCFFile");
        }
    }

}
