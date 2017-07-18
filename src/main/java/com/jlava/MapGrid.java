package com.jlava;

import java.util.*;
import java.util.regex.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.stream.*;
import java.io.*; 

public class MapGrid implements Grid, Constants {
    //private MultivaluedHashMap gridMap;
    private List<Map<String, String>> gridMap;
    private Map<String, String> lineMap;
    private String fileName;
    
    MapGrid() {
        readFile(GRID_DEFAULT, null);
    }
    
    MapGrid(String fileName) {
        this.fileName = fileName;
        readFile(fileName, GRID_FILE);
    }
    
    MapGrid(int width, int height, String fileName) {
        this.fileName = (fileName == null)?GRID_DEFAULT:fileName;
        
        //Generate/Reset grid
        int size = width*height;
        this.gridMap = new ArrayList<Map<String, String>>(height);
        String key;
        String val;
        String newLine = "";
        
        for(int ctr = 0; ctr < height; ctr++) {
            lineMap = new LinkedHashMap<String, String>(width);
            for(int mapCtr = 0; mapCtr < width; mapCtr++) {
                key = getRandomString();
                val = getRandomString();
                
                lineMap.put(key, val);
                newLine += "(" + key + "," + val +") ";
            }
            //WRITE LINE ON FILE
            newLine += "\n";
            gridMap.add(lineMap);
        }
        writeFile(newLine, TEMP_FILE);
        new File(TEMP_FILE).renameTo(new File(this.fileName));
    }
    
    @Override
	public void print() {
		//To Do : print as grid 
        System.out.print("\n");
        
        gridMap.forEach(map -> {
            map.forEach((k,v) -> {System.out.print("("+ k + "," + v +") ");
            });
            System.out.print("\n");
        });
	}
	
    @Override
	public void edit(int option, int xIndex, int yIndex, String newVal) {
        // GET MAP
        lineMap = gridMap.get(xIndex);

        // GET KEY
        String key = String.valueOf(getEntryByIndex(lineMap, yIndex).getKey());
        
        switch(option) {
            case 1:
                if(newVal.equals(key)) {
                    System.out.println("Same key, no need to edit.");
                } else if (lineMap.containsKey(newVal)) {
                    System.out.println("Cannot edit key " + key + " - duplicate key.");
                } else {
                    // GET VALUE
                    String value = lineMap.get(key);
                    lineMap.remove(key);

                    // INSERT NEW <K, V> in map
                    put(lineMap, yIndex, newVal, value);

                    // REPRINT MAP, SAVE IN FILE
                    // UPDATE FILE
                    updateFile(writeMap(), false);
                    System.out.println("Successfully edited key " + key + " to " + newVal);
                }
                break;
            case 2:
                lineMap.put(key, newVal);   
                
                // REPRINT MAP, SAVE IN FILE
                // UPDATE FILE
                updateFile(writeMap(), false);
                System.out.println("Successfully edited value to " + newVal);
                break;
        }
	}
    
    @Override
	public void edit(int xIndex, int yIndex, String newKey, String newVal) {
        edit(1, xIndex, yIndex, newKey);
        edit(2, xIndex, yIndex, newVal);
	}
	
    @Override
	public void addUpdate(int cols, String[] keys, String[] vals) {
        String line = "";
        String err = null;
        Map<String,String> newMap = new LinkedHashMap<String,String>();
        
        for(int ctr = 0; ctr < cols; ctr++) {
            if(newMap.containsKey(keys[ctr])) {
                //Do not add
                err += "\nCannot add (" + keys[ctr] + "," + vals[ctr] + ") - duplicate key.";
            } else {
                newMap.put(keys[ctr], vals[ctr]);
                line += "(" + keys[ctr] + "," + vals[ctr] + ")";
            }
        }
        
        gridMap.add(newMap);
        
        line += "\n";
        updateFile(line, true);
        
        // print error
        if(err != null) {
            System.out.print(err);
        }
	}
	
    @Override
	public void sort(int sort) {
        // add sort order
        String line = "";
        
        for(Map<String, String> map : gridMap) {
            if(sort == 2) {
                map = new TreeMap<String, String>(map).descendingMap();
            } else {
                map = new TreeMap<String, String>(map);
            }
            
            for(Map.Entry entry : map.entrySet()) {
                line += "(" + entry.getKey() + "," + entry.getValue() + ") ";
            }
            line += "\n";
        }
        
        System.out.println("\nSORTED GRID : \n" + line);
        updateFile(line, false);
	}
	
    @Override
	public void search(String sub) {
        int keyInstance;
        int valInstance;
        boolean found = false;
        
        if(sub.isEmpty()) {
            System.out.println("Invalid! : Empty string");
            return;
        }
        
        for(Map<String, String> map : gridMap) {
            for(Map.Entry entry : map.entrySet()) {
                keyInstance = 0;
                valInstance = 0;
                String key = String.valueOf(entry.getKey());
                String val = String.valueOf(entry.getValue());
                
                // Get # of instances of 'sub' in key
                if(key.contains(sub)) {
                    keyInstance = getValInstance(key, sub);
                }
                
                // Get # of instances of 'sub' in value
                if(val.contains(sub)) {
                    valInstance = getValInstance(val, sub);
                }
                
                // Display result if found in entry
                if(keyInstance > 0 || valInstance > 0) {
                    System.out.println("\n(" + key + "," + val + ")");

                    if(keyInstance > 0) {
                        System.out.println("\t" + keyInstance + " instance(s) in key");
                    }

                    if(valInstance > 0) {
                        System.out.println("\t" + valInstance + " instance(s) in value");
                    }
                    
                    found = true;
                }
            }
        }
        
        if (!found) {
            System.out.println("\n'" + sub + "' not found in grid...");
        }
	}
    
    private void put(Map<String, String> input, int index, String key, String value) {
        if (index >= 0 && index <= input.size()) {
            Map<String, String> output=new LinkedHashMap<String, String>();
            int i = 0;
            if (index == 0) {
                output.put(key, value);
                output.putAll(input);
            } else {
                for (Map.Entry<String, String> entry : input.entrySet()) {
                    if (i == index) {
                        output.put(key, value);
                    }
                    output.put(entry.getKey(), entry.getValue());
                    i++;
                }
            }
            if (index == input.size()) {
                output.put(key, value);
            }
            input.clear();
            input.putAll(output);
            output.clear();
            output = null;
        } else {
            throw new IndexOutOfBoundsException("index " + index
                    + " must be equal or greater than zero and less than size of the map");
        }
    }
    
    private int getValInstance(String base, String sub) {
		int index = base.indexOf(sub);
		int count = 0;
		
		while(index != -1) {
			index = base.indexOf(sub, index);

			if(index != -1) {
				count++;
				index++;
			}
		}
		
		return count;
	}
    
    private String getRandomString() {
		Random rand = new Random();
		int random = 0;
		String randString = "";
		for(int ctr = 0; ctr < 5; ctr++) {
			random = rand.nextInt(74) + 48;
			randString += (char)random;
		}
		
		return randString;
	}
    
    private void readFile(String file, String defaultFile){
        //change function, populate list, map
        gridMap = new ArrayList<Map<String, String>>();
        
        //read file into stream, try-with-resources
		try (Stream<String> lines = Files.lines(Paths.get(file))) {            
			lines.forEach(currentLine -> {
                System.out.println(currentLine);
                Matcher match = Pattern.compile("\\((.*?)\\)").matcher(currentLine);
                lineMap = new LinkedHashMap<String, String>();
                
                // Get values inside parenthesis
                while(match.find()) {
                    String entry = match.group(1);          

                    // Split values, and add in grid.
                    String[] keyVal = entry.split(",", 2);
                    if(keyVal.length >= 2) {
                        lineMap.put(keyVal[0], keyVal[1]);
                    }
                }
                
                gridMap.add(lineMap);
            });
            this.fileName = file;
            //print();
		} catch (IOException e) {
            this.fileName = defaultFile;
            
			if(fileName != null) {
                readFile(fileName, null);
            }
		}
    }
    
    private void writeFile(String line, String fileName) { 
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(line);
            writer.flush();

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
        
    private void updateFile(String content, boolean append){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, append))) {
			writer.write(content); 
            
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private String writeMap() {
        String line = "";
        for(Map<String, String> map : gridMap) {
            for(Map.Entry entry : map.entrySet()) {
                line += "(" + entry.getKey() + "," + entry.getValue() + ") ";
            }
            line += "\n";
        }
        
        return line;
    }
    
    @Override
    public int getMaxY(int xIndex) {
        return gridMap.get(xIndex).size()-1;
    }
    
    @Override
    public int getMaxX() {
        return gridMap.size()-1;
    }
    
    public static <K, V> Map.Entry<K, V> getEntryByIndex(Map<K, V> map, int i) {
        if(i >= map.size()) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }

        // use Iterator
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();

        // skip to i
        for(; i > 0; --i) {
            it.next();
        }

        return it.next();
    }
    
    public String getFileName() {
        return this.fileName;
    }
}