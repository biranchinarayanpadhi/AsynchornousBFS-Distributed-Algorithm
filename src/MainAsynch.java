/*
 * Project 2: Asynch BFS
 *
 * @Authors
 * Biranchi Narayan Padhi  - bxp200001
 * Manasa M Bhat           - mmb190005
 * Siddarameshwar Kadagad  -  sxk190071
 This file contains the initialization and input processing
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class MainAsynch {

    public static void main(String args[]) {
        int numOfProcess;
        AsynchBFS.Process[] processObject;

        int[] uidArray;
        int[][] adj_list;
        int rootID;
        boolean isRoot = false;
        Thread[] threads;
//        System.out.println(args[0]);
        if (args.length == 1) {
            Scanner sc = null;

            try {

                sc = new Scanner(new File(args[0]));

            } catch (FileNotFoundException e) {
                System.out.println("Error while opening the file: " + args[0]);
                return;
            }

            numOfProcess = sc.nextInt();
            System.out.println("number of Process ->" + numOfProcess);
            // creating processObject Array
            processObject = new AsynchBFS.Process[numOfProcess];

            // creating UID Array
            uidArray = new int[numOfProcess];

            // 2D array to store adjacency matrix represented by 1 or 0.
            adj_list = new int[numOfProcess][numOfProcess];

            //creating threads for each processes
            threads = new Thread[numOfProcess];

            // reading uids from the file and filling the UID Array
            for (int i = 0; i < numOfProcess; i += 1) {
                uidArray[i] = Integer.parseInt(sc.next());
            }
            System.out.println("uidArray ->" + Arrays.toString(uidArray));
            rootID = sc.nextInt();
            AsynchBFS.Process rootProcess = null;
            System.out.println("rootId is " + rootID);

            // creating n no of process wand assigning each process an UID from UIDArray
            for (int i = 0; i < numOfProcess; i++) {
                processObject[i] = new AsynchBFS.Process(uidArray[i], uidArray[i] == rootID ? true : false);
                if(uidArray[i] == rootID) {
                    rootProcess = processObject[i];
                }
                threads[i] = new Thread(processObject[i], i + "_thread");
            }

            // 2D array to store adjacency matrix represented by 1 or 0.
            for (int row = 0; row < numOfProcess; row++) {
                for (int col = 0; col < numOfProcess; col += 1) {
                    adj_list[row][col] = Integer.parseInt(sc.next());
                    if (adj_list[row][col] == 1) {
                        processObject[row].setNeighbors(processObject[col]);
                        processObject[row].setNeighbors(threads[col]);
                    }
                }
            }

            AsynchBFS.constructAsyncBFS(numOfProcess,threads, processObject,rootProcess);

        }
    }
}
