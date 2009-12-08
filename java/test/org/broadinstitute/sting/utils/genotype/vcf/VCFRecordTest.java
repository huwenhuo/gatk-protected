package org.broadinstitute.sting.utils.genotype.vcf;

import org.broadinstitute.sting.BaseTest;
import org.broadinstitute.sting.utils.fasta.IndexedFastaSequenceFile;
import org.broadinstitute.sting.utils.StingException;
import org.broadinstitute.sting.utils.GenomeLocParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.BeforeClass;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;


/**
 * @author aaron
 *         <p/>
 *         Class VCFRecordTest
 *         <p/>
 *         test the basic functionality of the vcf record
 */
public class VCFRecordTest extends BaseTest {

    private static IndexedFastaSequenceFile seq;

    @BeforeClass
    public static void beforeTests() {
        try {
            seq = new IndexedFastaSequenceFile(new File(seqLocation + "/references/Homo_sapiens_assembly18/v0/Homo_sapiens_assembly18.fasta"));
        } catch (FileNotFoundException e) {
            throw new StingException("unable to load the sequence dictionary");
        }
        GenomeLocParser.setupRefContigOrdering(seq);
    }

    /**
     * create a fake VCF record
     *
     * @return a VCFRecord
     */
    private static VCFRecord makeFakeVCFRecord(Map<String, String> infoFields) {
        List<VCFGenotypeEncoding> altBases = new ArrayList<VCFGenotypeEncoding>();
        altBases.add(new VCFGenotypeEncoding("C"));
        altBases.add(new VCFGenotypeEncoding("D1"));
        List<VCFGenotypeRecord> genotypeObjects = new ArrayList<VCFGenotypeRecord>();
        genotypeObjects.add(createGenotype("sample1", "A", "A"));
        return new VCFRecord('A', "chr1", 1, "RANDOM", altBases, 0, ".", infoFields, "GT:AA", genotypeObjects);
    }

    /**
     * create a fake VCF genotype record
     *
     * @param name    the name of the sample
     * @param Allele1 the first allele
     * @param Allele2 the second allele
     * @return a VCFGenotypeRecord
     */
    private static VCFGenotypeRecord createGenotype(String name, String Allele1, String Allele2) {
        Map<String, String> keyValues = new HashMap<String, String>();
        keyValues.put("AA", "2");
        List<VCFGenotypeEncoding> Alleles = new ArrayList<VCFGenotypeEncoding>();
        Alleles.add(new VCFGenotypeEncoding(Allele1));
        Alleles.add(new VCFGenotypeEncoding(Allele2));
        return new VCFGenotypeRecord(name, Alleles, VCFGenotypeRecord.PHASE.PHASED, keyValues);
    }

    @Test
     public void testAddReduntantAlts() {
        List<VCFGenotypeEncoding> altBases = new ArrayList<VCFGenotypeEncoding>();
        altBases.add(new VCFGenotypeEncoding("C"));
        altBases.add(new VCFGenotypeEncoding("D1"));
        altBases.add(new VCFGenotypeEncoding("D1"));
        List<VCFGenotypeRecord> genotypeObjects = new ArrayList<VCFGenotypeRecord>();
        genotypeObjects.add(createGenotype("sample1", "A", "A"));
        VCFRecord rec = new VCFRecord('A', "chr1", 1, "RANDOM", altBases, 0, ".", new HashMap<String,String>(), "GT:AA", genotypeObjects);
        Assert.assertEquals(2, rec.getAlternateAlleles().size());
    }

    @Test
    public void testGetOneGenotype() {
        Map<String, String> infoFields = new HashMap<String, String>();
        VCFRecord rec = makeFakeVCFRecord(infoFields);
        List<VCFGenotypeRecord> genotypeObjects = rec.getVCFGenotypeRecords();
        Assert.assertEquals(1, genotypeObjects.size());
        Assert.assertTrue(genotypeObjects.get(0).getSampleName().equals("sample1"));
        Assert.assertEquals(2, genotypeObjects.get(0).getAlleles().size());
        Assert.assertEquals("A", genotypeObjects.get(0).getAlleles().get(0).toString());
        Assert.assertEquals("A", genotypeObjects.get(0).getAlleles().get(1).toString());
    }

    @Test
    public void testGetGenotypes() {
        Map<String, String> infoFields = new HashMap<String, String>();
        VCFRecord rec = makeFakeVCFRecord(infoFields);
        rec.addGenotypeField(createGenotype("sample2", "C", "A"));
        List<VCFGenotypeRecord> genotypeObjects = rec.getVCFGenotypeRecords();
        Assert.assertEquals(2, genotypeObjects.size());
        Assert.assertTrue(genotypeObjects.get(0).getSampleName().equals("sample1"));
        Assert.assertEquals(2, genotypeObjects.get(0).getAlleles().size());
        Assert.assertEquals("A", genotypeObjects.get(0).getAlleles().get(0).toString());
        Assert.assertEquals("A", genotypeObjects.get(0).getAlleles().get(1).toString());

        // assert the second one
        Assert.assertTrue(genotypeObjects.get(1).getSampleName().equals("sample2"));
        Assert.assertEquals(2, genotypeObjects.get(1).getAlleles().size());
        Assert.assertEquals("C", genotypeObjects.get(1).getAlleles().get(0).toString());
        Assert.assertEquals("A", genotypeObjects.get(1).getAlleles().get(1).toString());

    }

    @Test
    public void testCreateInfoString() {
        Map<String, String> infoFields = new HashMap<String, String>();
        VCFRecord rec = makeFakeVCFRecord(infoFields);
        Assert.assertTrue(rec.createInfoString().equals("."));
        infoFields.put("DP", "50");
        VCFRecord rec2 = makeFakeVCFRecord(infoFields);
        Assert.assertTrue(rec2.createInfoString().equals("DP=50"));
        rec2.addInfoField("AB", "CD");
        Assert.assertTrue(rec2.createInfoString().equals("DP=50;AB=CD") || rec2.createInfoString().equals("AB=CD;DP=50"));
    }


    @Test
    public void testAddAlts() {
        Map<String, String> infoFields = new HashMap<String, String>();
        VCFRecord rec = makeFakeVCFRecord(infoFields);
        rec.addAlternateBase(new VCFGenotypeEncoding("T"));
        rec.addAlternateBase(new VCFGenotypeEncoding("T"));
        rec.addAlternateBase(new VCFGenotypeEncoding("T"));
        rec.addAlternateBase(new VCFGenotypeEncoding("T"));
        rec.addAlternateBase(new VCFGenotypeEncoding("T"));
        Assert.assertEquals(3,rec.getAlternateAlleles().size());                                
    }

    /**
     * create a fake header of known quantity
     *
     * @return a fake VCF header
     */
    public static VCFHeader createFakeHeader() {
        Set<String> metaData = new HashSet();
        metaData.add(VCFHeader.FULL_FORMAT_LINE); // required
        metaData.add("two=2");
        Set<String> additionalColumns = new HashSet<String>();
        additionalColumns.add("FORMAT");
        additionalColumns.add("sample1");
        return new VCFHeader(metaData, additionalColumns);
    }

    private static final String stringRep = "chr1\t1\tRANDOM\tA\tC,D1\t0.00\t.\tDP=50\tGT:AA\t0|0:2";
    private static final String stringRep2 = "chr1\t1\tRANDOM\tA\tC,D1\t0.00\t.\tAB=CD;DP=50\tGT:AA\t0|0:2";
    //private static final String stringRep3 = "chr1\t1\tRANDOM\tA\tC,D1\t0.00\t.\tAB=CD;DP=50\tGT:AA\t0|0:2";

    @Test
    public void testStringRepresentation() {
        Map<String, String> infoFields = new HashMap<String, String>();
        infoFields.put("DP", "50");
        VCFRecord rec = makeFakeVCFRecord(infoFields);
        Map<String, String> metaData = new HashMap<String, String>();
        List<String> additionalColumns = new ArrayList<String>();
        String rep = rec.toStringEncoding(createFakeHeader());
        Assert.assertTrue(stringRep.equals(rep));
        rec.addInfoField("AB", "CD");
        String rep2 = rec.toStringEncoding(createFakeHeader());
        Assert.assertTrue(stringRep2.equals(rep2));
        //rec.addGenotypeField(createGenotype("sample3","A","D12"));
    }


}
