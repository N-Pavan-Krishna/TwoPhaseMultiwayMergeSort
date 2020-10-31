import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class TPMMS {

    // static boolean input = false;
    // static long freeMemory = Runtime.getRuntime().freeMemory();
    static double inputBuffer, outputBuffer;
    static int inputBlocks, outputBlocks;
    static long startTime;
    static int counter = 0;

    public static void main(String[] args) throws IOException {

        startTime = System.currentTimeMillis();
        String fileName = "sample_1.txt";
        String fileDirectory = "Input_File/" + fileName;

        FileInputStream fis = new FileInputStream(fileDirectory);
        Scanner scan = new Scanner(fis);

        int numberOfTuples = scan.nextInt();
        int fileSize = numberOfTuples * 4;

        double mainMemorySize = scan.nextDouble() * Math.pow(10, 6);
        double totalMemorySize = 5 * Math.pow(10, 6);

        int numberOfTuplesPerList;
        int numberOfSubList = 0;

        //Size of input file < size of MainMemory // Working as expected
        if (fileSize < mainMemorySize) {
            numberOfTuplesPerList = numberOfTuples;
            numberOfSubList = 1;
        }
        //Size of input file = size of MainMemory // Working as expected
        else if (fileSize == mainMemorySize) {

            //Size of input file = size of MainMemory = size of programming environment // Not working as expected
            // at 70% of available free memory program is not crashing
            if (mainMemorySize == totalMemorySize) {
                numberOfTuplesPerList = (int) (0.70 * Runtime.getRuntime().freeMemory() / 4);
                numberOfSubList = (int) Math.ceil((double) numberOfTuples / numberOfTuplesPerList);
            } else {
                numberOfTuplesPerList = (int) mainMemorySize / 4;
                numberOfSubList = 1;
            }
        }

        //size of input file > size of MainMemory //Working as expected
        else { //if (fileSize > mainMemorySize)

            //Size of input file = size of MainMemory = size of programming environment // Not working as expected
            // at 70% of available free memory program is not crashing
            if (mainMemorySize == totalMemorySize) {
                numberOfTuplesPerList = (int) (0.70 * Runtime.getRuntime().freeMemory() / 4);
                numberOfSubList = (int) Math.ceil((double) numberOfTuples / numberOfTuplesPerList);
            } else {
                numberOfTuplesPerList = (int) mainMemorySize / 4;
                numberOfSubList = (int) Math.ceil((double) numberOfTuples / numberOfTuplesPerList);
            }
        }

        System.out.println("File Size :- " + fileSize);
        System.out.println("MainMemory Size :- " + mainMemorySize);
        System.out.println("Number of tuples/sublist :- " + numberOfTuplesPerList);

        int[] subList = new int[numberOfTuplesPerList];
        System.out.println("SubList Size :- " + subList.length);


        if (mainMemorySize == totalMemorySize) {
            outputBuffer = (int) (0.2 * Runtime.getRuntime().freeMemory());
            inputBuffer =  (int) (0.8 * Runtime.getRuntime().freeMemory());
            outputBlocks = (int) outputBuffer/4;
            inputBlocks = (int) inputBuffer/4;
        }
        else{
            outputBuffer = (mainMemorySize * 20) / 100;
            inputBuffer = mainMemorySize - outputBuffer;
            outputBlocks = (int) (mainMemorySize) / 4;
            inputBlocks = (int) (inputBuffer) / 4;
        }

		/*
        outputBuffer = (mainMemorySize * 20) / 100;
        inputBuffer = mainMemorySize - outputBuffer;
        outputBlocks = (int) (mainMemorySize) / 4;
        inputBlocks = (int) (inputBuffer) / 4;
		*/
        int index = 0;
        int subListNum = 0;
        int totalTuplesCount = 0;

        Runtime.getRuntime().gc();
        long start = System.currentTimeMillis();

        String directoryName = "PHASE_1";
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
        }

        int[] subListCount = new int[numberOfSubList];
        System.out.println("Number of subList :- " + subListCount.length); //working good

        while (scan.hasNext()) {

            totalTuplesCount++;
            subList[index] = scan.nextInt();
            index++;

            if (index == numberOfTuplesPerList || totalTuplesCount == numberOfTuples) {

                subListCount[subListNum] = subList.length;

                index = 0;
                Arrays.sort(subList);

                System.out.println("SubList - " + subListNum + " : " + subList.length);

                FileWriter fw = new FileWriter(directoryName + "/subList_" + subListNum + ".txt");
                subListNum++;
                BufferedWriter bw = new BufferedWriter(fw);

                for (int i = 0; i < subList.length; i++) {
                    bw.write(Integer.toString(subList[i]));
                    bw.newLine();
                }

                bw.close();

                if (numberOfTuples - totalTuplesCount < numberOfTuplesPerList) { // If we have less number of tuples we have to reduce the subList size
                    subList = new int[numberOfTuples - totalTuplesCount];
                }
            }
        }
        scan.close();

        System.out.println("----------------------------------------------------");

        if (numberOfSubList > 1) {
            mergeSublists(numberOfTuples, numberOfSubList, fileName);
        }
        System.out.println("Final Running Time: " + (System.currentTimeMillis() - startTime) + " milli seconds!");

    }

    private static void mergeSublists(int numberOfTuples, int numberSubList, String fileName) throws IOException {
        int elementsOfBuffer = ((outputBlocks) / (numberSubList + 1));

       /* if (elementsOfBuffer >= numberOfTuples) {
            elementsOfBuffer = (numberOfTuples / (numberSubList + 1));
        }*/

        int[][] bufferArray = new int[numberSubList][elementsOfBuffer];
        int[] outputBufferArray = new int[elementsOfBuffer];
        Scanner[] reader = new Scanner[numberSubList];
        BufferedWriter out = new BufferedWriter(new FileWriter(new File("Output_File/" + fileName))); //ADD FILES
        int[] pointerToElement = new int[numberSubList];

        for (int i = 0; i < numberSubList; i++) {
            reader[i] = new Scanner(new File("PHASE_1/subList_" + i + ".txt")); //ADD FILES
            pointerToElement[i] = elementsOfBuffer;
        }

        int fileCount = 0;
        while (true) {
            while (fileCount < numberSubList) {

                if (pointerToElement[fileCount] == elementsOfBuffer && reader[fileCount].hasNext()) {
                    pointerToElement[fileCount] = 0;

                    for (int i = 0; i < elementsOfBuffer; i++) {
                        if (reader[fileCount].hasNext()) {
                            bufferArray[fileCount][i] = reader[fileCount].nextInt();
                        } else
                            break;
                    }
                }
                fileCount++;
            }

            fileCount = 0;


            for (int i = 0; i < elementsOfBuffer; i++) {
                int min = Integer.MAX_VALUE;
                for (int j = 0; j < numberSubList; j++) {
                    if (pointerToElement[j] < elementsOfBuffer) {
                        if (bufferArray[j][pointerToElement[j]] < min) {
                            min = bufferArray[j][pointerToElement[j]];
                            pointerToElement[j] += 1;
                        }
                    }
                }
                outputBufferArray[i] = min;
            }

            for (int i = 0; i < elementsOfBuffer; i++) {
                out.write(Integer.toString(outputBufferArray[i]));
                out.newLine();
                counter++;

                if (counter == numberOfTuples)
                    break;

            }
            if (counter == numberOfTuples)
                break;
        }

        System.out.println("count" + counter);

        out.close();
        for (int i = 0; i < numberSubList; i++) {
            reader[i].close();
        }
    }


}
