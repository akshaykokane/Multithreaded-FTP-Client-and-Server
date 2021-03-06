import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.*;
import java.util.*;
 
public class CommandHandler extends Thread {
	 Socket socket;
	 int clientNo;	
	 String workingDirectory;
	 int tPort;
	 int commandId;
  HashMap<Integer,String> processTable;
	
	public CommandHandler(Socket inSocket, int count, int tPort, HashMap<Integer, String> processTable) throws ClassNotFoundException, IOException {
		socket = inSocket;
		clientNo = count;
		this.tPort = tPort;
		this.processTable = processTable;
		workingDirectory = System.getProperty("user.dir");//curemt workimg dirctory
	}
	@Override
	public void run() {
		// TODO Auto-generated constructor stub
			try {
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				
			while(true) {
				//get command(request) from client
				String command = (String) inputStream.readObject();
		
				String[] commandAndValue = command.split(" ");
				if(commandAndValue[0].equals("quit")) {
					System.out.println("Client disconnected");
					 outputStream.writeObject("quit");
					 inputStream.close();
          outputStream.flush();
					 outputStream.close();
					 socket.close();
          return;
					 
				}
					
				System.out.println("command recieved - > at client no ->"+clientNo + " - > " + command);
				
			
				workingDirectory.concat("/");
				switch(commandAndValue[0]) {
				
				case "get" : //Server send file to client
         int commandId = processTable.size() + 100 + 1;
					processTable.put(commandId, "Halted " + command); //1 is running
					outputStream.writeObject(String.valueOf(commandId));
    while(true){
         
           List<String> processes = new ArrayList<>();
           processes.addAll(Server.processTable.values());
           boolean parrallelProcessRunning = false;
           for(String process : processes)
           {
               
              if(process.equals("Running put " + commandAndValue[1]))
                  parrallelProcessRunning = true;
              
           }
           if(!parrallelProcessRunning){
               
             System.out.println("Now full-filling new request of client : "+clientNo);
              break;
           }
           System.out.println("Client "+clientNo+" need to wait to GET the file" + commandAndValue[1] + ". As PUT operation is in progress on the same file.");
           Thread.sleep(10000);
             
          /*
           1) Reterive values from hashmap
           2) check if there is put command with the current get command on the same file
           3) If there is a matching command, then find it is Running or Finished
           4) If running, then wait else continue
                     
          */
          
         }
         
          boolean terminated = false;
         // int commandId = processTable.size() + 100 + 1;
					processTable.put(commandId, "Running " + command); //1 is running
					//outputStream.writeObject(String.valueOf(commandId));
     Thread.sleep(10000);
					File myfile = new File(commandAndValue[1]);
					int length=(int) myfile.length();
					int counter=0;
					byte[] a = new byte[length];
					int rem=length%1000;
					int limit=length-rem;
					BufferedInputStream br = new BufferedInputStream(new FileInputStream(myfile));
			
      	
              br.read(a,0,a.length); 
             	br.close();
              sleep(1);
          
        /*********Lock ends*****************************************************/
        
					outputStream.writeInt(length);
					if(length>=1000)
					{
						while(counter<limit)
						{
              String state = processTable.get(Integer.parseInt( String.valueOf(commandId)));
              if(state.contains("terminate"))
                terminated = true;
              
             
							if(terminated)
                 outputStream.writeObject("terminated"); 
              else
               outputStream.writeObject("Still Runnuing");
               
               
              outputStream.write(a,counter,1000);
							counter+=1000;
							outputStream.flush();
						}
            if(terminated){
              	 System.out.println("Process with coomand Id " + commandId + " terminated by user");
            }
            else if(rem!=0)
						{
							outputStream.write(a,counter,rem);
						}
            
					}					
					else
					{
						outputStream.write(a,counter,length);
					}
					
					outputStream.flush();
				
            System.out.println("Finished" + command);
            processTable.put(commandId, "Finished " + command); //1 is running
					break;
					
				case "put" ://Server recieves file from client
        commandId = processTable.size() + 100 + 1;
       	outputStream.writeObject(String.valueOf(commandId));
        processTable.put(commandId, "Halted " + command); //1 is running
         /*********Write Lock when file is write from server to array********/
         while(true){
         
           List<String> processes = new ArrayList<>();
           processes.addAll(Server.processTable.values());
           boolean parrallelProcessRunning = false;
           for(String process : processes)
           {
           
          
              if(process.equals("Running " + command))
                  parrallelProcessRunning = true;
              
           }
           if(!parrallelProcessRunning){
               
             System.out.println("Now full-filling new request of client : "+clientNo);
              break;
           }
           System.out.println("PUT request on same file. Client "+clientNo+" need to wait till other request is full-filled");
           Thread.sleep(10000);
             
          /*
           1) Reterive values from hashmap
           2) check if there is matching command with the current command
           3) If there is a matching command, then find it is Running or Finished
           4) If running, then wait else continue
                     
          */
          
         }
         
          /*********Write Lock when file is write from server to array********/
     
        terminated = false;
        
        processTable.put(commandId, "Running " + command); //1 is running
			
        //System.out.println("lengthbefore");
          int l=inputStream.readInt();
          //    System.out.println(l);
					a = new byte[l];
					counter=0;
					rem=l%1000;
					limit=l-rem;
        
         
					FileOutputStream fos = new FileOutputStream(commandAndValue[1]);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
          outputStream.flush();
					if(l>=1000)
					{
         //  System.out.println("in first if");
						while(counter<limit)
						{
              String state = processTable.get(Integer.parseInt( String.valueOf(commandId)));
              if(state.contains("terminate"))
                terminated = true;
              
             
							if(terminated){
                outputStream.writeObject("terminated");
                break;
              }
                
              else
               outputStream.writeObject("Still Runnuing");
               
							inputStream.read(a,counter,1000);
							counter+=1000;
						}
            if(terminated){
                 System.out.println("Process with command Id " + commandId + " terminated by user! Deleting the garbage file creadted during the process..");
              	 File file = new File(commandAndValue[1]);
                 file.delete();
                 
            }
						else if(rem!=0)
						{
							//System.out.println("in if");
							//counter-=1000;
						//	System.out.println("The value of counter in IF is "+counter);
							inputStream.read(a,counter,rem);
						}
					}					
					else
					{
             System.out.println("in else if");
						inputStream.read(a,counter,l);
					}
					bos.write(a,0,a.length);
					bos.close();
					fos.close();
        
       
       
         /*********Write Lock Ends********/
           System.out.println("Finished ->" + command);
          processTable.put(commandId, "Finished " + command); //1 is running
          System.out.println("Now State of the command "+processTable.get(commandId));
					break;
             
   
		
				
				case "delete":
					 File file = new File(commandAndValue[1]);
				        if(file.delete()){
				        	outputStream.writeObject(commandAndValue[1]+ " File deleted");
				        }else 
				        	outputStream.writeObject("File" + commandAndValue[1] + "doesn't exists in project root directory");
					break;
					
				case "ls":
					
					File folder = new File(workingDirectory);
					File[] listOfFiles = folder.listFiles();
					String list = "";
					for (int i = 0; i < listOfFiles.length; i++) {
					  
					  list = list + listOfFiles[i].getName() + "\n";
				
					}
					outputStream.writeObject(list);
					break;
				
				case "cd":
						if(commandAndValue[1].equals(".."))
						{
            System.out.println("Inside cd..");
							int index = workingDirectory.lastIndexOf('/');
						    workingDirectory = workingDirectory.substring(0,index);
						    outputStream.writeObject("changing working directory");
						}
						else{
							workingDirectory = workingDirectory + "/" + commandAndValue[1];
							outputStream.writeObject("changing working directory");
						}
						
					break;
				case "mkdir":
					
					boolean fileCreated = new File(workingDirectory + "/" + commandAndValue[1]).mkdirs();
					if(fileCreated)
						outputStream.writeObject("Directory "+ commandAndValue[1] +" created successfully");
					else
						outputStream.writeObject("Opps! Directory cannot be created. May be directory name already exists");
					break;
				
				case "quit":
					 break;
					 
				case "pwd":
					outputStream.writeObject(workingDirectory);
					break;
        
				default:
					outputStream.writeObject("-bash: "+ command +" : command not found");
				
				}
				 
			
			}
			
			 
			
		
		}
		catch(Exception e) {
				System.out.println("exiting");
        
			}
		}
	}