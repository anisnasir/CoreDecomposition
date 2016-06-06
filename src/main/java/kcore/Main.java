package kcore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Main {
	private static void ErrorMessage() {
		System.err.println("Choose the type of simulator using:");
		System.err
				.println("KCore: 0 inputFile <sliding_window> [epsilon] outFile [k] ");
		System.err
				.println("Charikar: 1 inputFile <sliding_window> [epsilon] outFile [k] " ) ; 
		System.err
				.println("Bahmani: 2 inputFile <sliding_window> <epsilon> outFile [k] " ) ;

		System.err
				.println("Dynamic K core: 3 inputFile <sliding_window> [epsilon] outFile [k] " ) ;
		System.err
				.println("Top-k Bahmani: 4 inputFile <sliding_window> epsilon outFile k " ) ;
		System.err
				.println("Top-k KCore: 5 inputFile <sliding_window> [epsilon] outFile k " ) ;
		System.err
				.println("Top-k Charikar: 6 inputFile <sliding_window> [epsilon] outFile k " ) ;
		System.err
				.println("Top-k KCoreDecomposition: 7 inputFile <sliding_window> [epsilon] outFile k " ) ;
		
		System.exit(1);
	}
	public static void main(String[] args) throws IOException {
		
		if(args.length < 4 ) {
			ErrorMessage();
		}
		int simulatorType = Integer.parseInt(args[0]);
		String inFileName= args[1];
		int sliding_window = Integer.parseInt(args[2]);
		double epsilon = 0.0;
		String outFileName = args[4];
		int k = 0 ;
		if(simulatorType == 2 ) {
			epsilon = Double.parseDouble(args[3]);
		}
		else if(simulatorType == 4) {
			epsilon = Double.parseDouble(args[3]);
			k = Integer.parseInt(args[5]);
		} else if (simulatorType == 5 || simulatorType == 6 || simulatorType == 7) {
			k = Integer.parseInt(args[5]);
		}
		
	
		
		String sep = "\t";
		BufferedReader in = null;
		
		long startTime = System.currentTimeMillis();
		try {
            InputStream rawin = new FileInputStream(inFileName);
            in = new BufferedReader(new InputStreamReader(rawin));
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            e.printStackTrace();
            System.exit(1);
        }

        //initialize the input reader
        StreamEdgeReader reader = new StreamEdgeReader(in,sep);
		StreamEdge item = reader.nextItem();
		
		//Declare outprint interval variables
		int PRINT_INTERVAL=1000000;
		long simulationStartTime = System.currentTimeMillis();
		
		//Data Structures specific to the Algorithm
		NodeMap nodeMap = new NodeMap();
		DegreeMap degreeMap = new DegreeMap();
		EdgeHandler utility = new EdgeHandler();
		FixedSizeSlidingWindow sw = new FixedSizeSlidingWindow(sliding_window);
		
		DensestSubgraph densest = null;
		
		if(simulatorType == 0) { 
			densest = new KCore();
		}else if (simulatorType == 1) {
			densest = new Charikar();
		} else if (simulatorType == 2) {
			densest = new Bahmani(epsilon);
		} else if (simulatorType == 3) {
			densest = new KCoreDecomposition(nodeMap.map);
		} else if (simulatorType == 4) {
			densest = new BahmaniTopK(epsilon, k);
		} else if (simulatorType == 5) {
			densest = new KCoreTopK(k);	
		} else if (simulatorType == 6) {
			densest = new CharikarTopK(k);
		} else if (simulatorType == 7) {
			densest = new KCoreDecompositionTopK(k, nodeMap);
		}
		
		
		OutputWriter ow = new OutputWriter(outFileName);
		ArrayList<Output> output = null;
		
		//Start reading the input
		System.out.println("Reading the input");
		int edgeCounter = 0;
		while (item != null) {
			if (++edgeCounter % PRINT_INTERVAL == 0) {
				System.out.println("Read " + edgeCounter/PRINT_INTERVAL
						+ "M edges.\tSimulation time: "
						+ (System.currentTimeMillis() - simulationStartTime)
						/ 1000 + " seconds");
				
			}
			
			utility.handleEdgeAddition(item,nodeMap,degreeMap);
			
			StreamEdge oldestEdge = sw.add(item);

			if(oldestEdge != null) {
				utility.handleEdgeDeletion(oldestEdge, nodeMap, degreeMap);
			}
			
			long endTime   = System.currentTimeMillis();
			System.out.println("Reading input time : " + ((endTime-startTime)/(double)1000) + " secs ");
			startTime = System.currentTimeMillis();
			//System.out.println(nodeMap.map);
			//System.out.println(degreeMap.map);
			
			if(simulatorType == 0) { 
				//System.out.println(degreeMap.map);
				
				output = densest.getDensest(degreeMap.getCopy(),nodeMap.getCopy());
				
			}else if (simulatorType == 1) {
				output = densest.getDensest(degreeMap.getCopy(),nodeMap.getCopy());
				
			} else if (simulatorType == 2) {
				output = densest.getDensest(degreeMap.getCopy(),nodeMap.getCopy());
				
			} else if (simulatorType == 3) {
				KCoreDecomposition kCore = (KCoreDecomposition) densest;
				
				kCore.addEdge(item.getSource(), item.getDestination());
				if(oldestEdge != null) 
					kCore.removeEdge(oldestEdge.getSource(), oldestEdge.getDestination());
				
				output = densest.getDensest(degreeMap.getCopy(),nodeMap.getCopy());
			
			} else if ( simulatorType == 4) {
				output = densest.getDensest(degreeMap.getCopy(),nodeMap.getCopy());
			} else if ( simulatorType == 5) {
				output = densest.getDensest(degreeMap.getCopy(),nodeMap.getCopy());
			} else if ( simulatorType == 6) {
				output = densest.getDensest(degreeMap.getCopy(),nodeMap.getCopy());
			} else if (simulatorType == 7) {
				KCoreDecompositionTopK kCoreTopK = (KCoreDecompositionTopK) densest;
				KCoreDecomposition kCore = kCoreTopK.densest;
				
				kCore.addEdge(item.getSource(), item.getDestination());
				if(oldestEdge != null) 
					kCore.removeEdge(oldestEdge.getSource(), oldestEdge.getDestination());
				
				output = densest.getDensest(degreeMap,nodeMap);
			}
			
			endTime   = System.currentTimeMillis();
			System.out.println("---------------priting output ------------");
			for(int i =0; i< output.size();i++) {
				output.get(i).setTimeTaken((endTime-startTime)/1000.0);
				output.get(i).printOutput();
				ow.writeOutput(output.get(i));
			}
			System.out.println("---------------priting output ------------");
			item = reader.nextItem();
			if(item !=null)
				while(nodeMap.contains(item)) {
					item = reader.nextItem();
					if(item == null)
						break;
				}
		}
	in.close();
	ow.close();
	
	
	
		
	}
	
	

}
