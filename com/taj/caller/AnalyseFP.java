package com.taj.caller;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRule;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.AlgoTNS;
import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.Rule;
import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;
import ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Wrapper to call the library FP Growth algorithm from R.
 */
public class AnalyseFP {

    public static void main(String [] arg) throws Exception{
        doIt("/home/geantvert/workspace/FEC Visualization/arfff", "/home/geantvert/workspace/FEC Visualization/result", 0.3d, 0.3d);
    }

    public static void doIt(String arffPath, String convertedFilePath, double minsupp, double minconf) throws IOException {

        System.out.print("Started...\n");

        Map<Integer, String> conversionMap = convertArffToSPMF(arffPath, convertedFilePath);


        AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
        Itemsets patterns = fpgrowth.runAlgorithm(convertedFilePath, null, minsupp);
        int databaseSize = fpgrowth.getDatabaseSize();
        patterns.printItemsets(databaseSize);

        // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
        double  minlift = 0;

        AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
        // the next line run the algorithm.
        // Note: we pass null as output file path, because we don't want
        // to save the result to a file, but keep it into memory.
        AssocRules rules = algoAgrawal.runAlgorithm(patterns,null, databaseSize, minconf, minlift);

        printRulesWithLift(rules.getRules(), databaseSize, conversionMap);

        algoAgrawal.printStats();

        fpgrowth.printStats();

    }

    static public void printRulesWithLift(List<AssocRule> rules, int databaseSize, Map<Integer, String> conversionMap){
        int i=0;
        System.out.println(" --------------------------------");
        for(AssocRule rule : rules){
            String left = printItemSet(rule.getItemset1(), conversionMap);
            String right = printItemSet(rule.getItemset2(), conversionMap);
            System.out.print(left + ";" + right + "\n");
//            System.out.print("  rule " + i + ":  " + left + " ==> " + right);
//           System.out.print("support :  " + rule.getRelativeSupport(databaseSize) +
//                    " (" + rule.getAbsoluteSupport() + "/" + databaseSize + ") ");
//            System.out.print("confidence :  " + rule.getConfidence());
//           System.out.print(" lift :  " + rule.getLift());
//            System.out.println("");
            i++;
        }
        System.out.println(" --------------------------------");
    }

    static public Map<Integer, String> convertArffToSPMF(String arffPath, String convertedFilePath) throws IOException {
        if (!new File(arffPath).exists()) {
            throw new IllegalArgumentException("Input file " + arffPath + " doesn't exist.\nNo conversion possible.");
        }
        File output = new File(convertedFilePath);
        if(output.exists()) {
            output.delete();
        }
        TransactionDatabaseConverter converter = new TransactionDatabaseConverter();
        // Call the method to convert the input file from ARFF to the SPMF format
        return converter.convertARFFandReturnMap(arffPath, convertedFilePath, Integer.MAX_VALUE);
    }

    /**
     * Convert the Int[] to a human readable String
     * @param input
     * @param conversionMap
     * @return
     */
    static private String printItemSet(int[] input, Map<Integer, String> conversionMap){
        return convertItemSetToString(convertItemSet(input, conversionMap));
    }

    /**
     * Build readable ItemSet String from Int[]
     * @param ItemSet
     * @return
     */
    static private String convertItemSetToString(String[] ItemSet){
        StringBuilder sb = new StringBuilder(50);
        for(String set : ItemSet) {
            sb.append(set).append(", ");
        }
        return sb.toString();
    }

    /**
     * Replace the Int by their real name
     * @param input
     * @param conversionMap
     * @return
     */
    static private String[] convertItemSet(int[] input, Map<Integer, String> conversionMap) {
        String[] result = new String[input.length];
        int count = 0;
        for(int i : input) {
            result[count] = conversionMap.get(i);
            count++;
        }
        return result;
    }
}
