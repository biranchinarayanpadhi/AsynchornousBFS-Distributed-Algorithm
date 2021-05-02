/*
 * Project 2: Asynch BFS
 *
 * @Authors
 * Biranchi Narayan Padhi  - bxp200001
 * Manasa M Bhat           - mmb190005
 * Siddarameshwar Kadagad  -  sxk190071
 *
 This file contains the message class for test,ack and nack messages
 *contribution-------------------------
 *Biranchi Narayan Padhi : bxp200001
 *-Message class Structure
 */
public class Message{
    
    int messageType; //1 if it is a response(ACK) to a test message, 0 if it is a test message, -1 if it is a reponse to a test message
    int distanceFromRoot;
    AsynchBFS.Process sender; //the process id corresponding to the initial sender of the message

    public Message(int messageType, int distanceFromRoot, AsynchBFS.Process sender){

        this.messageType = messageType;
        this.distanceFromRoot = distanceFromRoot;
        this.sender = sender;
        
    }

    public int getMessageType(){
        return messageType;
    }

    public int getDistanceFromRoot(){
        return distanceFromRoot;
    }

    public AsynchBFS.Process getSenderProcessID(){
        return sender;
    }

}
