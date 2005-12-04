/*
Copyright 2005 Paul Zepernick

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.  
*/
package com.pz.reader.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.pz.reader.structure.ColumnMetaData;

/**
 * @author zepernick
 *
 *Static utilities that are used to perform parsing in the DataSet class
 *These can also be used for low level parsing, if not wishing to
 *use the DataSet class.
 *
 *@version 2.0
 */
public class ParserUtils {

	/**
	 * Returns an ArrayList of items in a delimited string.  If there is no qualifier around the text,
	 * the qualifier parameter can be left null, or empty.  There should not be any line breaks in the string. 
	 * Each line of the file should be passed in individually.
	 * 
	 * @param line - String of data to be parsed
	 * @param delimiter - Delimiter seperating each element
	 * @param qualifier - qualifier which is surrounding the text
	 * @return ArrayList
	 */
	public static Vector splitLine(String line, String delimiter, String qualifier){
	    Vector list = new Vector();
	    //String temp = "";
	    boolean beginQualifier = false;
	    //this will be used for delimted files that have some items qualified and some items dont
	    boolean beginNoQualifier = false;
	    StringBuffer sb = new StringBuffer();
	    
	    //trim hard leading spaces at the begining of the line
	    line = lTrim(line);
	    for (int i = 0; i < line.length(); i++){
            String remainderOfLine = line.substring(i); //data of the line which has not yet been read
	        //check to see if there is a text qualifier
	        if (qualifier != null && qualifier.trim().length() > 0){
	            if (line.substring(i, i + 1).equals(qualifier) && !beginQualifier && !beginNoQualifier){
	                //begining of a set of data
	                beginQualifier = true;
	            }else if (!beginQualifier && !beginNoQualifier &&
	                    	!line.substring(i, i +1).equals(qualifier) && 
                            !lTrim(remainderOfLine).startsWith(qualifier)){ //try to account for empty space before qualifier starts
	                	//we have not yet begun a qualifier and the char we are on is NOT
	                	//a qualifier.  Start reading data
	                	beginNoQualifier = true;
                        //make sure that this is not just an empty column with no qualifiers. ie "data",,"data"
                        if (line.substring(i, i +1).equals(delimiter)){
                            list.add(sb.toString());
                            sb.delete(0,sb.length());
                            beginNoQualifier = false;
                            continue;//grab the next char
                        }
                        sb.append(line.substring(i, i + 1));
	            }else if ((!beginNoQualifier) && line.substring(i, i + 1).equals(qualifier) && beginQualifier &&
                        (lTrim(line.substring(i + 1)).length() == 0 || //this will be true on empty undelmited columns at the end of the line
	                    lTrim(line.substring(i + 1)).substring(0,1).equals(delimiter))){
	                //end of a set of data that was qualified
	                list.add(sb.toString());
	                sb.delete(0,sb.length());
	                beginQualifier = false;
	                //add to "i" so we can get past the qualifier, otherwise it is read into a set of data which 
	                //may not be qualified.  Find out how many spaces to the delimiter
                    int offset = getDelimiterOffset(line,i,delimiter) -1;//subtract 1 since i is going to get incremented again at the top of the loop
                    //System.out.println("offset: " + offset);
                    if (offset < 1){
                        i++;
                    }else{
                        i += offset;
                    }
	            }else if (beginNoQualifier && line.substring(i, i + 1).equals(delimiter)){
	                //check to see if we are done with an element that was not being qulified
	                list.add(sb.toString());
	                sb.delete(0,sb.length());
	                beginNoQualifier = false;
	            }else if (beginNoQualifier || beginQualifier){
	                //getting data in a NO qualifier element or qualified element
	                sb.append(line.substring(i, i + 1));
	            }
                        
	        }else{
	            //not using a qualifier.  Using a delimiter only
	            if (line.substring(i, i + 1).equals(delimiter)){
	                list.add(sb.toString());
	                sb.delete(0,sb.length());
	            }else{
	                sb.append(line.substring(i,i + 1));
	            }
	        }
	    }
	    
	    //remove the ending text qualifier if needed
	    if (qualifier != null && qualifier.trim().length() > 0 && sb.toString().trim().length()> 0){
	        if (sb.toString().trim().substring(sb.toString().trim().length() -1).equals(qualifier)){
	            String s = sb.toString().trim().substring(0,sb.toString().length() - 1); 
	            sb.delete(0,sb.length());
	            sb.append(s);
	        }
	        
	    }
	    
	    if (beginQualifier || beginNoQualifier || line.trim().endsWith(delimiter)){  
	        //also account for a delimiter with an empty column at the end that was not qualified
	        //check to see if we need to add the last column in..this will happen on empty columns
	        //add the last column
	        list.add(sb.toString());
	    }

	    sb = null;
	    
	    return list;
	}
	
    /**
     * reads from the specified point in the line and returns how 
     * many chars to the specified delimter
     * 
     * @param line
     * @param start
     * @param delimiter
     * @return int
     */

    public static int getDelimiterOffset(String line, int start,String delimiter){
       int offset = 0;
       for (int i = start; i < line.length(); i++){
           offset++;
           if (line.substring(i,i+1).equals(delimiter)){
               return offset;
           }
       }
       return -1;
    }
    
	/**
	 * Removes empty space from the begining of a string
	 * @param value - to be trimmed
	 * @return String
	 */
    public static String lTrim(String value){  
        StringBuffer returnVal = new StringBuffer();  
        boolean gotAChar = false;  
          
        for (int i = 0; i < value.length(); i++){  
            if(value.substring(i,i+1).trim().length() == 0  
                    && !gotAChar){  
                continue;  
            }else{  
                gotAChar = true;  
                returnVal.append(value.substring(i,i+1));  
            }  
        }  
          
        return returnVal.toString();  
          
    }
    
    /**
     * Removes a single string character from a given string
     * @param character - string char
     * @param theString - string to search
     * @return String
     */
    public static String removeChar(String character,String theString){
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < theString.length(); i++){
            if (theString.substring(i,i+1).equalsIgnoreCase(character)){
                continue;
            }
            s.append(theString.substring(i,i+1));
        }
        
        return s.toString();
        
    }
   
    
    /**
     *Returns a list of ColumnMetaData objects.  This is for use with delimited files.
     *The first line of the file which contains data will be used as the column names
     *
     * @param theFile
     * @param delimiter
     * @param qualifier
     * @exception Exception
     * @return ArrayList - ColumnMetaData
     */
    public static Vector getColumnMDFromFile(File theFile, String delimiter, String qualifier) throws Exception{
        BufferedReader br = null;
        FileReader fr = null;
        String line = null;
        Vector lineData = null;
        Vector results = new Vector();
        
        try{
            fr = new FileReader(theFile);
            br = new BufferedReader(fr);
            
            while ((line = br.readLine()) != null){
                if (line.trim().length() == 0){
                    continue;
                }
                
                lineData = splitLine(line,delimiter,qualifier);
                for(int i = 0; i < lineData.size(); i++){
                    ColumnMetaData cmd = new ColumnMetaData();
                    cmd.setColName((String)lineData.get(i));
                    results.add(cmd);
                }
                break;
            }
        }finally{
            if (lineData != null) lineData.clear();
            if (br != null) br.close();
            if (fr != null) fr.close();
        }
        return results;
    }
   
    /**
     * 
     * @param columnName
     * @param columnMD - vector of ColumnMetaData objects
     * @return int - position of the column in the file
     * @throws NoSuchElementException
     */
	public static int findColumn(String columnName, Vector columnMD) throws NoSuchElementException{
	    for (int i = 0; i < columnMD.size(); i++){
	        ColumnMetaData cmd = (ColumnMetaData)columnMD.get(i);
	        if (cmd.getColName().equalsIgnoreCase(columnName)) return i;
	    }
	    
	    throw new NoSuchElementException("Column Name: " + columnName + " does not exist");
	}
}
