package edu.arizona.cs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.esotericsoftware.kryo.util.Null;
import com.fasterxml.jackson.core.sym.Name;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import edu.stanford.nlp.io.EncodingPrintWriter.out;
import scala.Int;

import org.apache.commons.lang3.StringUtils;

public class lawyer {
    static List<String> thanks=new ArrayList<String>(); //list of thanks words
    static Set<String> categories = new HashSet<String>(); //categories of questions
    static String[] labels ={"attorney","Juvenile","Individual Rights","Family and Children","Education","Health and Disability"
    ,"Housing and Homelessness","Consumer Financial Questions","Other","Income Maintenance","Work, Employment and Unemployment"}; //
    static HashMap<String, String> convo= new HashMap<String, String>(); //conversation question to last sentence
    static HashMap<String, String> fullConvo= new HashMap<String, String>(); //conversation question to last sentence
    static HashMap<String, String> qCategory= new HashMap<String, String>(); //question mapping to category
    static HashMap<String, List<String>> attorney= new HashMap<String, List<String>>(); //lawyer map to questions
    static HashMap<String, HashMap<String, List<String>>> scores= new HashMap<String, HashMap<String, List<String>>>(); //layer mapped to categorized questions
    static HashMap<String, HashMap<String, Float>> scoresFinal= new HashMap<String, HashMap<String,Float>>(); //lawyer mapped to map of performance
    static ArrayList<String> sample = new ArrayList<String>();
    private static void scoring() throws IOException{
        int j=0;
        for (String worker: attorney.keySet()){
            j++;
            System.out.println(j);
            HashMap<String, List<String>> score=score_builder();
            scores.put(worker, score);
            HashMap<String, Float> scoref=scoreFinal_builder();
            scoresFinal.put(worker, scoref);
            List<String> questions= attorney.get(worker);
            for (String q: questions){
                String conv= convo.get(q);
                String cat= qCategory.get(q);
                if (isThanks(conv)){
                    scores.get(worker).get(cat).add("t");
                }else{
                    scores.get(worker).get(cat).add("f");
                }
            }
        }
        for (String worker: attorney.keySet()){
            HashMap<String, List<String>> mapping= scores.get(worker);
            for (String cat : categories){
                List<String> scoreMap=mapping.get(cat);
                float t=0;
                for (String s : scoreMap){
                    if (s.equals("t")){t++;}
                }
                float size=scoreMap.size();
                if (size!=0){scoresFinal.get(worker).put(cat, t*100/size);}else{scoresFinal.get(worker).put(cat, (float) 0.0);}
            }
        }
    }

    private static boolean isThanks(String conv) throws IOException {
        int i=0;
        for (String s: thanks){
            if (conv!=null && conv.contains(s)){i=1;}
        }
        if (i==1){return true;}
        return false;
    }

    private static void isThanks_helper() throws IOException{
        File tFile = new File("thanks.txt");
        BufferedReader br= new BufferedReader(new FileReader(tFile));
        String st;
        while ((st = br.readLine()) != null){
               String[] split= st.split(" ");
               for (String p: split){
                if(!NumberUtils.isNumber(p)){
                    p=p.toLowerCase();
                    thanks.add(p);
                }
               }
        }

    }

    private static HashMap<String, List<String>> score_builder(){
        HashMap<String, List<String>> score= new HashMap<String, List<String>>();
        for (String category: categories){
            List<String> points=new ArrayList<String>();
            score.put(category,points);
        }
        return score;
    }

    private static HashMap<String, Float> scoreFinal_builder(){
        HashMap<String, Float> score= new HashMap<String, Float>();
        for (String category: categories){
            score.put(category,(float) 0.0);
        }
        return score;
    }

    private static String lineCleaner(String [] record){
        String s="";
        for (int i=3; i<record.length-1;i++){
            s+=record[i];
        }
        return s;

    }

    private static  void convoExtractor() throws IOException{
        int j=0;
        for (int i=1;i<7;i++){
            String name="questionposts_"+String.valueOf(i)+".csv";
            try (BufferedReader reader = new BufferedReader(new FileReader(name))) {
                String rec = reader.readLine();
                rec= reader.readLine();
                String [] record= rec.split(",");
                String s=lineCleaner(record);
                while ( rec!=null && record.length>3){
                    if (convo.get(record[2])==null){fullConvo.put(record[2],"START");}else{fullConvo.put(record[2],fullConvo.get(record[2])+" NEXT "+s);}
                    convo.put(record[2],record[3].toLowerCase());
                    rec = reader.readLine();
                    if (rec!=null){record= rec.split(",");s=lineCleaner(record);}
                }
                reader.close();
            }
            }
    }



    private static void lawyers() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader("questions.csv"));
            String rec= reader.readLine();
            rec= reader.readLine();
            String[] record= rec.split(",");
            while ( rec!=null) {
                record= rec.split(",");
                if (record[4].equals("\"Work")){
                    if (record[12].equals(record[10]) && !record[10].equals("NULL")){
                        categories.add("Work, Employment and Unemployment");
                        qCategory.put(record[2], "Work, Employment and Unemployment");
                        if (attorney.get(record[12])==null){ 
                            attorney.put(record[12],new ArrayList<String>());
                        }else{
                            attorney.get(record[12]).add(record[2]);
                        }
                    }
                }else{
                if (record[9]!=null ){
                    if (record[11].equals(record[9]) && !record[9].equals("NULL")){
                    categories.add(record[4].trim());
                    qCategory.put(record[2], record[4]);
                    if (attorney.get(record[11])==null){ 
                        attorney.put(record[11],new ArrayList<String>());
                    }else{
                        attorney.get(record[11]).add(record[2]);
                    }
                }
            }
                }
                rec = reader.readLine();
                
            }
            reader.close();
        }
    

    private static void sampler(int sampleSize){
        int max=fullConvo.size()-1;
        int min=0;
        Set<Integer> nums = new HashSet<Integer>();
        while (nums.size()<=sampleSize){int b = (int)(Math.random()*(max-min+1)+min); nums.add(b);}
        Set<String> s=fullConvo.keySet();
        List<String> arr = new ArrayList<String>();
        for (String i : s)
            arr.add(fullConvo.get(i));
        for (int n : nums){
            sample.add(arr.get(n));
        }

    }
    public static void main(String[] args ) throws IOException {

        convoExtractor();
        lawyers();
        isThanks_helper();
        scoring();
        FileWriter outputfile = new FileWriter("analysis.csv");
        CSVWriter writer = new CSVWriter(outputfile);
        writer.writeNext(labels);
        for (String a: scoresFinal.keySet()){
            HashMap<String, Float> curr= scoresFinal.get(a);
            String[] label = {a,String.valueOf(curr.get("Juvenile")),String.valueOf(curr.get("\"Individual Rights\"")),
            String.valueOf(curr.get("\"Family and Children\"")), String.valueOf(curr.get("Education")),
            String.valueOf(curr.get("\"Health and Disability\"")), String.valueOf(curr.get("\"Housing and Homelessness\"")),
            String.valueOf(curr.get("\"Consumer Financial Questions\"")), String.valueOf(curr.get("Other")),
            String.valueOf(curr.get("\"Income Maintenance\"")),String.valueOf(curr.get("Work, Employment and Unemployment"))
        };
        writer.writeNext(label);
        }
        writer.close();
        outputfile = new FileWriter("sample.csv");
        sampler(384);
        writer = new CSVWriter(outputfile);
        for (String s: sample){
            String[] l={s};
            writer.writeNext(l);
        }
        writer.close();
    }
    
}
