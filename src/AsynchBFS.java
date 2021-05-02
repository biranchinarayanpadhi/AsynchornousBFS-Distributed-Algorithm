/*
 * Project 2: Asynch BFS
 *
 * @Authors
 * Biranchi Narayan Padhi  - bxp200001
 * Manasa M Bhat           - mmb190005
 * Siddarameshwar Kadagad  -  sxk190071
 *
 This file contains the Process class for the Asynchronous BFS algorithm, and the algorithm for asynchronous BFS algorithm in asynchronous distributed system
 *Contribution---------------------------------:
 * @Manasa M Bhat
 * -Introducing logic for making sure child terminates after parent termiantes
 * -buildBFS Sequential and Level Order Traversal
 * -Adding delays for sending ACK/NACK 
 * -logic for managing Test Messages 
 *
 * @Biranchi Narayan Padhi
 * -AsynchBFS and Process class Structure
 * -Introducing delays to simulate Asynchronous Nature
 * -Sending Message to Neighbors 
 * -termiantion process logic
 * 
 *
 * @Siddarameshwar Kadagad
 * -AsynchBFS logic implementation
 * -Managing ACK/NACK Messages for process
 * -logic for  total number of messages
 * -CountDownLatch in master thread
 * 
 * -----------------------------------------------
 *
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynchBFS {

    //to store parent as key and its children list as value 
    HashMap<Integer, List<Integer>> map = new HashMap<>();
    Boolean terminateAllProcess = false;
    public static final int MESSAGE = 0;
    public static final int MESSAGE_ACK = 1;
    public static final int MESSAGE_NACK = -1;
    public static AtomicInteger totalNumOfMessagesInNetwork = new AtomicInteger(0);
    public static boolean terminateProcesses = false;
    private static CountDownLatch latch;

    public static class Process implements Runnable {

        // attributes of a process Node
        int processID;
        int distanceFromRoot;
        Process parent;
        int numOfMessagesFromMyNode = 0;

        boolean isRoot = false;

        List<Process> neighbors;
        List<Process> childProcess;

        List<Thread> neighborThreads;
        List<Thread> childThreads;

        BlockingQueue<Message> incomingMessagesQueue;
        BlockingQueue<Message> acksNacksQueue;
        Map<Integer, Integer> acksNackMap;
        Map<Integer, AsynchBFS.Process> acksNackSenderMap;

        // constructor for Process Class
        public Process(int id, boolean isRoot) {

            distanceFromRoot = isRoot ? 0 : Integer.MAX_VALUE;
            this.processID = id;
            this.isRoot = isRoot;
            this.parent = null;
            incomingMessagesQueue = new ArrayBlockingQueue<Message>(100);
            acksNacksQueue = new ArrayBlockingQueue<Message>(100);
            acksNackMap = new HashMap<>();
            acksNackSenderMap = new HashMap<>();
        }

        // all getter methods
        public int getProcessID() {
            return processID;
        }

        public boolean isRoot() {
            return isRoot;
        }

        public Process getParent() {
            return parent;
        }

        public List<Process> getNeighbors() {
            return this.neighbors;
        }

        public List<Thread> getNeighborThreads() {
            return this.neighborThreads;
        }

        public List<Process> getChildProcess() {
            return this.childProcess;
        }

        public List<Thread> getChildThread() {
            return this.childThreads;
        }

        public int getDistanceFromRoot() {
            return this.distanceFromRoot;
        }

        // all setter methods
        public void setParent(Process parent) {
            this.parent = parent;
        }

        public void setNeighbors(Process neighbor) {
            if (neighbors == null) {
                neighbors = new ArrayList<>();
            }
            neighbors.add(neighbor);
        }

        public void setNeighbors(Thread neighbor) {
            if (neighborThreads == null) {
                neighborThreads = new ArrayList<>();
            }
            neighborThreads.add(neighbor);
        }

        public void setChild(Process child) {
            if (childProcess == null) {
                childProcess = new ArrayList<>();
            }
            childProcess.add(child);
        }

        public void setChild(Thread child) {
            if (childThreads == null) {
                childThreads = new ArrayList<>();
            }
            childThreads.add(child);
        }

        public void setDistanceFromRoot(int distance) {
            this.distanceFromRoot = distance;
        }

        // function which other process can use to add their reponse/message in
        // repective processe's Queue
        public void addMessage(Message message) {
            try {
                if (message.messageType == MESSAGE) {
                    this.incomingMessagesQueue.put(message);
                } else {
                    this.acksNacksQueue.put(message);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        // message handling functions

        /*
         * this function generates a random delays within 1 to 12 ms and stores it in
         * array of size depending on the number of Neighbors Based on the delays, it
         * sends messages to it's neighbors.
         *
         * @param message: the messaeg to be sent to all the neighbors.
         *
         * @throws InterruptedException since the Thread.sleep() function is being
         * called.
         */

        // TODO: maintain a global variable to calculate the total number of messages
        // sent to terminate the algorithm.

        public void sendMessageToNeighbors(Message message) {

            int numNeighbors = neighbors.size();
            int delays[] = new int[numNeighbors];

            // generate Delays for each Neighbors within 1 to 12 milliseconds.
            for (int i = 0; i < delays.length; i += 1) {
                int generatedDelay = (int) ((Math.random() * 12) + 1);
                delays[i] = generatedDelay;
            }

            // sendMessage to Neighbor based on the delay
            for (int i = 1; i <= 12; i += 1) {
                for (int j = 0; j < delays.length; j += 1) {

                    // if neighbor is the Process's parent then process is going to send a message
                    if (delays[j] == i && neighbors.get(j) != this.getParent()) {
                        numOfMessagesFromMyNode++;
                        System.out.println("Message from " + processID + " to " + neighbors.get(j).processID);
                        sendMessage(message, neighbors.get(j));
                    }
                }
                sleep(10);
            }
        }

        /*
         * this message send a message to a single process
         *
         * @param message: the message which to be sent
         *
         * @param process: the process to recieve the message
         */
        public void sendMessage(Message message, Process process) {
            try {
                if (message.messageType == MESSAGE) {
                    process.incomingMessagesQueue.put(message);
                } else {
                    process.acksNacksQueue.put(message);
                }
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }

        public void run() {

            sleep(1000);
            if (isRoot) {
                Message message = new Message(0, 0, this);
                sendMessageToNeighbors(message);
                //Run this look till the root get acks/nacks from all its neigbhors
                while (acksNacksQueue.size() < neighbors.size()) {
                    if (incomingMessagesQueue.size() > 0) {
                        message = incomingMessagesQueue.poll();
                        Message rejectMessage = new Message(MESSAGE_NACK, message.distanceFromRoot, this);
                        numOfMessagesFromMyNode++;
                        System.out.println("Reject message from " + processID + " to " + message.sender.processID);
                        randomDelay();
                        message.sender.addMessage(rejectMessage);
                    }
                }

                System.out.println("Terminate Processes Flag is set");
                numOfMessagesFromMyNode--;
                terminateProcesses = true;
                terminateProcess();

            } else {
                while (true) {
                    while (incomingMessagesQueue.size() == 0 && !terminateProcesses) {
                        //wait till you get any messages or terminateProcesses flag is set
                        while (acksNacksQueue.size() > 0) {
                            //process acksNacks if any present
                            Message ackNack = acksNacksQueue.poll();
                            processAcksNacks(ackNack);
                        }
                    }

                    if (terminateProcesses) {
                        //terminate all processes if terminateProcesses flag is set
                        //terminate only after the parent is terminated , This ensures that the children list is printed when the parent terminates
                        Thread parThread = getThreadOfParent();
                        while (parThread.isAlive()) ;
                        terminateProcess();
                        break;
                    }

                    Message message = incomingMessagesQueue.poll();
                    if (message.distanceFromRoot + 1 < this.distanceFromRoot) {
                        //consider message only if it provides route with shorter distance to root
                        if (this.parent != null) {
                            //remove it as child of previous parent
                            this.parent.childProcess.remove(this);
                        }
                        message.sender.setChild(this);
                        this.parent = message.sender;
                        this.distanceFromRoot = message.distanceFromRoot + 1;

                        if (neighbors.size() > 1) {
                            //We only need to send messages if the node has more than 1 neighbor
                            Message newMessage = new Message(MESSAGE, this.distanceFromRoot, this);
                            acksNackMap.put(this.distanceFromRoot, neighbors.size() - 1);
                            acksNackSenderMap.put(this.distanceFromRoot, message.sender);
                            sendMessageToNeighbors(newMessage);
                        } else {
                            //send done message to sender
                            Message doneMessage = new Message(MESSAGE_ACK, message.distanceFromRoot, this);
                            numOfMessagesFromMyNode++;
                            randomDelay();
                            message.sender.addMessage(doneMessage);
                            System.out.println("Done message from " + processID + " to " + message.sender.processID);
                        }

                    } else {
                        //send reject
                        Message rejectMessage = new Message(MESSAGE_NACK, message.distanceFromRoot, this);
                        numOfMessagesFromMyNode++;
                        randomDelay();
                        message.sender.addMessage(rejectMessage);
                        System.out.println("Reject message from " + processID + " to " + message.sender.processID);
                    }

                    while (acksNacksQueue.size() > 0) {
                        //process acksNacks before other messages.
                        Message ackNack = acksNacksQueue.poll();
                        processAcksNacks(ackNack);
                    }
                }
            }
        }

        public Thread getThreadOfParent() {
            int i = 0;

            for (int k = 0; k < neighbors.size(); k++) {
                if (neighbors.get(k).processID == this.parent.processID) {
                    i = k;
                    break;
                }
            }
            return neighborThreads.get(i);
        }

        public void processAcksNacks(Message ackNack) {
            if (acksNackMap.get(ackNack.getDistanceFromRoot()) == 1) {
                //number of acks/nacks remaining for distance ackNack.getDistanceFromRoot() is 1. This ends that "round" after processing this ack/nack
                if (this.distanceFromRoot == ackNack.getDistanceFromRoot()) {
                    //send done: least distance to root is not changed by other messages
                    AsynchBFS.Process messageSender = acksNackSenderMap.get(this.distanceFromRoot);
                    if (this.parent != null) {
                        this.parent.childProcess.remove(this);
                    }
                    messageSender.setChild(this);
                    this.parent = messageSender;
                    Message doneMessage = new Message(MESSAGE_ACK, this.distanceFromRoot - 1, this);
                    numOfMessagesFromMyNode++;
                    randomDelay();
                    messageSender.addMessage(doneMessage);
                    System.out.println("Done message from " + processID + " to " + messageSender.processID);
                } else {
                    //send reject: least distance to root has been changed by some other message
                    AsynchBFS.Process messageSender = acksNackSenderMap.get(ackNack.getDistanceFromRoot());
                    Message rejectMessage = new Message(MESSAGE_NACK, ackNack.getDistanceFromRoot() - 1, this);
                    numOfMessagesFromMyNode++;
                    randomDelay();
                    messageSender.addMessage(rejectMessage);
                    System.out.println("Reject message from " + processID + " to " + messageSender.processID);
                }

                acksNackMap.remove(ackNack.getDistanceFromRoot());
                acksNackSenderMap.remove(ackNack.getDistanceFromRoot());
            } else {
                acksNackMap.put(ackNack.getDistanceFromRoot(), acksNackMap.get(ackNack.getDistanceFromRoot()) - 1);
            }
        }

        public void sleep(int milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void terminateProcess() {
            //Terminate process and print its children
            List<Integer> myChildrenInBFSTree = new ArrayList<>();
            if (childProcess != null) {
                for(Process child: childProcess){
                    if(child != null) {
                        myChildrenInBFSTree.add(child.processID);
                    }
                }
                //childProcess.forEach(child -> myChildrenInBFSTree.add(child.processID));
            }
            System.out.println("Process with ID " + processID + " terminating. Its children are " + myChildrenInBFSTree + " Number of message from process " + processID + " is " + numOfMessagesFromMyNode);

            totalNumOfMessagesInNetwork.addAndGet(numOfMessagesFromMyNode);
//            sleep(1000);
            latch.countDown();
        }

        private void randomDelay() {
            int randomNum = (int) ((Math.random() * 12) + 1);
            sleep(10 * randomNum);
        }

    }

    //constructAsynchBFS function to start all thread and start building BFS
    public static void constructAsyncBFS(int num_of_process, Thread[] threads, Process[] processes, Process rootProcess) {
        latch = new CountDownLatch(num_of_process);

        //start threads
        for (int i = 0; i < num_of_process; i++) {
            threads[i].start();
        }

        //The master thread waits for all the threads to terminate
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //wait for all threads to print the terminating messages
        sleep(1000);
        int numOfTerminateMessages = num_of_process - 1;
        System.out.println("Number of messages for terminate all processes = " + numOfTerminateMessages);
        totalNumOfMessagesInNetwork.addAndGet(numOfTerminateMessages);
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("total number of messages is  = " + totalNumOfMessagesInNetwork);

        buildBFSTree(rootProcess);
    }

    private static void buildBFSTree(Process rootProcess) {
        System.out.println("---------BFS tree traversal displayed as list---------------------------");
        LinkedList<Process> processQueue = new LinkedList<Process>();
        processQueue.addLast(rootProcess);
        // One below the other
        while (!processQueue.isEmpty()) {
            Process cur = processQueue.remove();
            if(cur != null) {
                System.out.print(cur.getProcessID() + " ");
                List<Process> children = cur.getChildProcess();
                if (children != null) {
                    for (Process child : children) {
                        processQueue.addLast(child);
                    }
                }
            }
        }
        System.out.println("");
        System.out.println("---------BFS tree displayed with Levels---------------------------");
        //Level order traversal
        processQueue.addLast(rootProcess);
        processQueue.addLast(null);
        int level = 0;
        while (!processQueue.isEmpty()) {
            Process cur = processQueue.remove();
            if(cur != null){
                System.out.println("");
                System.out.print("Level " + level + " : ");

            while (cur != null) {
                System.out.print(cur.getProcessID() + " ");
                List<Process> children = cur.getChildProcess();
                if (children != null) {
                    for (Process child : children) {
                        processQueue.addLast(child);
                    }
                }
                cur = processQueue.remove();
            }
            if (!processQueue.isEmpty()) {
                processQueue.addLast(null);
            }
                level++;
            }

        }
        System.out.println("");
        System.out.println("---------------------------------------------------------------------------");
    }

    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
