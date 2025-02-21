package com.mobeon.ntf.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: enikfyh
 * Date: 2008-mar-31
 * Time: 15:05:13
 */
public class TemplateConverter {
    private BufferedReader inReader = null;

    private ArrayList<String> buffer = new ArrayList<String>();
    private HashMap<String, TemplateEntry> templates = new HashMap<String, TemplateEntry>();

    public TemplateConverter(File fromFile, String andWord) throws Exception {
        inReader = new BufferedReader(new FileReader(fromFile));
        if( andWord != null ) {
            this.andWord = andWord + " ";
        }
        
    }

    private String slamdownFormat = "dMMM HH:mm";
    private String andWord = null;

    public void parse() throws Exception {
        String line = null;
        String tag = null;
        String text = null;
        String condition = null;

        while( (line = inReader.readLine()) != null ) {
            buffer.add(line);
            int delimIndex = line.indexOf("=");
            if( delimIndex != -1 ) {
                tag = line.substring(0, delimIndex);
                text = line.substring(delimIndex+1).trim();
                int condStartIndex = tag.indexOf("(");
                if( condStartIndex != -1 ) {
                    int condEndIndex = tag.indexOf(")");
                    condition =  tag.substring(condStartIndex, condEndIndex+1);
                    tag = tag.substring(0, condStartIndex );
                } else {
                    condition = null;
                }
                tag = tag.trim();
                TemplateEntry entry = templates.get(tag);
                if( entry == null ) {
                    entry = new TemplateEntry(tag, text, condition);
                    templates.put(tag, entry);
                } else {
                    entry.add(text, condition);
                }
                if( tag.equals("slamdownformat") ) {
                    slamdownFormat = text;
                }

            }
        }
        if( andWord == null ) {
            andWord = getAndWord();
        }
    }

    public void print() throws Exception {
        for( int i=0;i<buffer.size();i++ ) {
            String line = buffer.get(i);
            int delimIndex = line.indexOf("=");
            if( delimIndex != -1 ) {
                String tag = line.substring(0, delimIndex);
                int condStartIndex = tag.indexOf("(");
                if( condStartIndex != -1 ) {
                    tag = tag.substring(0, condStartIndex );
                }
                tag = tag.trim();
                TemplateEntry entry = templates.get(tag);
                if( entry != null ) {
                    entry.print();
                    templates.remove(tag);
                }
            } else {
                // empty line or comment. Just print it
                System.out.println(line);
            }
        }
    }

    public String getModifiedString(String text, String tag) {
        String modText = text;
        if( tag.contains("SIZE_TEXT")) {
            Pattern p = Pattern.compile("__([A-Z]+)__");
            Matcher m = p.matcher(text);
            modText = m.replaceAll("$1");
        } else {
            //Pattern p = Pattern.compile("__([a-zA-Z ]*=?_?[a-zA-Z ]*)__");
            Pattern p = Pattern.compile("__(\\S*)__");
            Matcher m = p.matcher(modText);
            modText = m.replaceAll("\" $1 \"");
            // Take out \u03AB tags and place them inside snuffs "
            Pattern p2 = Pattern.compile("(\\\\u[0-9a-zA-Z]+)");
            Matcher m2 = p2.matcher(modText);
            modText = m2.replaceAll("\" $1 \"");

        }

        modText = "\"" + modText + "\"";
        modText = modText.replaceAll("\" \"\\n", "\\n");
        modText = modText.replaceAll("\"\"", "");
        return modText;
    }

    public String getAndWord() {
        TemplateEntry template = templates.get("c");
        TextInfo one = null;
        TextInfo two = null;
        ArrayList<TextInfo> infos =  template.infos;
        for( int i=0;i<infos.size();i++ ) {
            if( infos.get(i).condition == null ) {
                continue;
            }
            if( infos.get(i).condition.equals("(1,0,0,0)") ) {
                one = infos.get(i);
            }
            if( infos.get(i).condition.equals("(1,1,0,0)") ||
                    infos.get(i).condition.equals("(1,0,1,0)") ||
                    infos.get(i).condition.equals("(1,0,0,1)")) {
                two = infos.get(i);
            }
            if( one != null && two != null ) {
                break;
            }
        }
        if( one != null && two != null ) {
            String[] oneWords = one.getWords();
            String[] twoWords = two.getWords();
            if( twoWords.length > oneWords.length ) {
                String and = twoWords[oneWords.length];
                return and;
            }
        }
        return ",";

    }


    private class TemplateEntry {
        String name;
        boolean isCount = false;
        ArrayList<TextInfo> infos;

        public TemplateEntry(String tag, String text, String condition) {
            infos = new ArrayList<TextInfo>();
            String modText = text;
            if( condition != null ) {
                isCount = true;
            }
            name = tag;
            if( name.equals("slamdownbody")) {
                if( modText.contains("TIME")) {
                    modText = modText.replaceAll("TIME", "__DATE=" + slamdownFormat + "__");
                }
            }
            if( modText.contains("__DATE=")) {
                Pattern dp = Pattern.compile("__DATE=([\\WA-Za-z0-9 ]*)__");
                Matcher dm = dp.matcher(modText);
                if(dm.find()) {
                    String dateString = dm.group(1);
                    dateString = dateString.replaceAll(" ", "_");
                    modText = dm.replaceAll("__DATE=" + dateString + "__");
                }
            }
            TextInfo info = new TextInfo(modText, condition);
            infos.add(info);
        }

        public void add(String text, String condition) {
            if( condition != null ) {
                isCount = true;
            }
            TextInfo info = new TextInfo(text, condition);
            infos.add(info);
        }

        public void print() {
            if( isCount && infos.size() > 5) {
                int commonCount = -1;
                System.out.println(name + " = {");
                // print all common words. "You" "have" for example.
                ArrayList<HashInfo> allHashes = getAllHashWords();
                for( int i=0;i<allHashes.size();i++ ) {
                    HashInfo info = allHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < allHashes.size() ) {
                        next = allHashes.get(i+1);
                    }
                    if( info.isAllMarked() ) {
                        if( next == null || !next.merge(info, -1) ) {
                            info.print(-1);
                        }
                        commonCount = i;
                    } else {
                        break;
                    }
                }
                // get specific texts
                ArrayList<HashInfo> voiceHashes = getTypeStrings(0);
                ArrayList<HashInfo> faxHashes = getTypeStrings(1);
                ArrayList<HashInfo> emailHashes = getTypeStrings(2);
                ArrayList<HashInfo> videoHashes = getTypeStrings(3);

                for( int i=commonCount+1;i<voiceHashes.size();i++ ) {
                    HashInfo info = voiceHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < voiceHashes.size() ) {
                        next = voiceHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, 0) ) {
                        info.print(0);
                    }
                }

                if( faxHashes.size() > commonCount+1 || emailHashes.size() > commonCount+1 || videoHashes.size() > commonCount+1 ) {
                    System.out.println("(1-,1-,1-,*) \", \"");
                    System.out.println("(1-,0,1-,1-) \", \"");
                    System.out.println("(1-,1-,0,0) \" " + andWord + "\"");
                    System.out.println("(1-,0,1-,0) \" " + andWord + "\"");
                    System.out.println("(1-,0,0,1-) \" " + andWord + "\"");
                }

                // print fax texts
                for( int i=commonCount+1;i<faxHashes.size();i++ ) {
                    HashInfo info = faxHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < faxHashes.size() ) {
                        next = faxHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, 1) ) {
                        info.print(1);
                    }
                }

                if( emailHashes.size() > commonCount+1 || videoHashes.size() > commonCount+1 ) {
                    System.out.println("(*,1-,1-,1-) \", \"");
                    System.out.println("(*,1-,1-,0) \" " + andWord + "\"");
                    System.out.println("(*,1-,0,1-) \" " + andWord + "\"");
                }

                // print email texts
                for( int i=commonCount+1;i<emailHashes.size();i++ ) {
                    HashInfo info = emailHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < emailHashes.size() ) {
                        next = emailHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, 2) ) {
                        info.print(2);
                    }
                }

                if( videoHashes.size() > commonCount+1 ) {
                    System.out.println("(*,*,1-,1-) \" " + andWord + "\"");
                }

                // print video texts
                for( int i=commonCount+1;i<videoHashes.size();i++ ) {
                    HashInfo info = videoHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < videoHashes.size() ) {
                        next = videoHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, 3) ) {
                        info.print(3);
                    }
                }

                System.out.println("} \n");

            } else if( isCount ) {
                // count with less than 6 entries for example updateaftersms
                System.out.println(name + " = {");
                int commonCount = -1;
                int totalCount = -1;
                boolean hasTotal = false;
                boolean hasVoice = false;
                boolean hasFax = false;
                boolean hasEmail = false;
                boolean hasVideo = false;


                // hitta common
                ArrayList<HashInfo> allHashes = getAllHashWords();
                for( int i=0;i<allHashes.size();i++ ) {
                    HashInfo info = allHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < allHashes.size() ) {
                        next = allHashes.get(i+1);
                    }
                    if( info.count == infos.size() ) {
                        if( next == null || !next.merge(info, -1) ) {
                            info.print(-1);
                        }
                        commonCount = i;
                    } else {
                        break;
                    }
                }

                totalCount = commonCount;
                // hitta totalcounts
                ArrayList<HashInfo> oneHashes = getOneStrings();
                for( int i=commonCount+1;i<oneHashes.size();i++ ) {
                    HashInfo info = oneHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < oneHashes.size() ) {
                        next = oneHashes.get(i+1);
                    }
                    if( info.count == 4 ) {
                        if( next == null || !next.merge(info, -1) ) {
                            info.print(101);
                        }
                        totalCount = i;
                        hasTotal = true;
                    } else {
                        break;
                    }
                }

                // get specific texts
                ArrayList<HashInfo> voiceHashes = getTypeStrings(0);
                ArrayList<HashInfo> faxHashes = getTypeStrings(1);
                ArrayList<HashInfo> emailHashes = getTypeStrings(2);
                ArrayList<HashInfo> videoHashes = getTypeStrings(3);

                for( int i=totalCount+1;i<voiceHashes.size();i++ ) {
                    HashInfo info = voiceHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < voiceHashes.size() ) {
                        next = voiceHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, 0) ) {
                        info.print(0);
                    }
                    hasVoice = true;
                }

                // print fax texts
                for( int i=totalCount+1;i<faxHashes.size();i++ ) {
                    HashInfo info = faxHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < faxHashes.size() ) {
                        next = faxHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, 1) ) {
                        info.print(1);
                    }
                    hasFax = true;
                }

                // print email texts
                for( int i=totalCount+1;i<emailHashes.size();i++ ) {
                    HashInfo info = emailHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < emailHashes.size() ) {
                        next = emailHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, 2) ) {
                        info.print(2);
                    }
                    hasEmail = true;
                }

                // print video texts
                for( int i=totalCount+1;i<videoHashes.size();i++ ) {
                    HashInfo info = videoHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < videoHashes.size() ) {
                        next = videoHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, 3) ) {
                        info.print(3);
                    }
                    hasVideo = true;
                }

                // general och försök hitta rätt count.
                ArrayList<HashInfo> backupHashes = getBackupStrings();
                for( int i=commonCount+1;i<backupHashes.size();i++ ) {
                    HashInfo info = backupHashes.get(i);
                    HashInfo next = null;
                    if( i+1 < backupHashes.size() ) {
                        next = backupHashes.get(i+1);
                    }
                    if( next == null || !next.merge(info, -1) ) {
                        String cond = "";
                        if( hasTotal ) {
                            cond = "(2-";
                        } else {
                            cond += "(";
                            if( hasVoice ) {
                                cond += "2-,";
                            } else {
                                cond += "*,";
                            }
                            if( hasFax ) {
                                cond += "2-,";
                            } else {
                                cond += "*,";
                            }
                            if( hasEmail ) {
                                cond += "2-,";
                            } else {
                                cond += "*,";
                            }
                            if( hasVideo ) {
                                cond += "2-";
                            } else {
                                cond += "*";
                            }
                        }
                        cond += ")";
                        info.print(cond);
                    }

                }
                 System.out.println("}\n");

            } else if (!name.equals("slamdowntimeformat")) {
                System.out.println(name + " = {");
                System.out.println(getTransformedString());
                System.out.println("}\n");
            }
        }

        /**
         * used to return normal templates.
         * @return the text to be printed
         */
        public String getTransformedString() {
            return getModifiedString(infos.get(0).text, name);

        }

        private ArrayList<HashInfo> getAllHashWords() {
            return getHashWords(infos);
        }

        /**
         * return a hashmap of words and which condition they match
         */
        private ArrayList<HashInfo> getHashWords(ArrayList<TextInfo> infoList) {

            ArrayList<HashInfo> hashArray = new ArrayList<HashInfo>();
            if( infoList == null ) {
                return hashArray;
            }
            for( int i=0;i<infoList.size();i++ ) {
                TextInfo textInfo = infoList.get(i);
                String [] words = textInfo.getWords();
                for( int w=0;w<words.length;w++ ) {
                    String word = words[w];

                    if( word.length() == 0 ) {
                        continue;
                    }
                    HashInfo info = null;
                    for( int h=0;h<hashArray.size();h++) {
                        if( hashArray.get(h).word.equals(word) && hashArray.get(h).pos == w) {
                            info = hashArray.get(h);
                            break;
                        }
                    }
                    if( info != null ) {
                        info.count++;
                        for( int t=0;t<4;t++ ) {
                            for( int c=0;c<3;c++ ) {
                                if(textInfo.conditionArray[t][c] ) {
                                    info.conditionArray[t][c] = true;
                                }
                            }
                        }
                    } else {
                        info = new HashInfo();
                        info.word = word;
                        info.pos = w;
                        info.tag = name;
                        info.count = 1;
                        for( int t=0;t<4;t++ ) {
                            for( int c=0;c<3;c++ ) {
                                if(textInfo.conditionArray[t][c] ) {
                                    info.conditionArray[t][c] = true;
                                }
                            }
                        }
                        hashArray.add(info);
                    }

                }
            }
            Collections.sort(hashArray);
            return hashArray;

        }

        public ArrayList<HashInfo> getTypeStrings(int type) {
            String match1 = conditionTable[type*2];
            String match2 = conditionTable[type*2+1];

            ArrayList<TextInfo> infoList = new ArrayList<TextInfo>();
            // find type infos
            for( int i=0;i<infos.size();i++ ) {
                TextInfo info = infos.get(i);
                if( info.condition != null && (info.condition.equals(match1) ||
                    info.condition.equals(match2))) {
                        infoList.add(info);

                }
            }
            return getHashWords(infoList);

        }

        public ArrayList<HashInfo> getOneStrings() {
            ArrayList<TextInfo> infoList = new ArrayList<TextInfo>();
            for( int i=0;i<infos.size();i++ ) {
                TextInfo info = infos.get(i);
                if( info.condition != null &&
                        (info.condition.equals("(1,0,0,0)") ||
                        info.condition.equals("(0,1,0,0)") ||
                        info.condition.equals("(0,0,1,0)") ||
                        info.condition.equals("(0,0,0,1)") )) {
                        infoList.add(info);

                }
            }
            return getHashWords(infoList);
        }

        public ArrayList<HashInfo> getBackupStrings() {
            ArrayList<TextInfo> infoList = new ArrayList<TextInfo>();
            for( int i=0;i<infos.size();i++ ) {
                TextInfo info = infos.get(i);
                if( info.condition == null ) {
                        infoList.add(info);
                }
            }
            return getHashWords(infoList);
        }

    }

    private class TextInfo {
        String text;
        String condition;
        boolean [][] conditionArray = new boolean [4][3];

        public TextInfo(String t, String cond) {
            this.condition = cond;
            this.text = t;
            this.text = t.replaceAll("\\.$", " ");
            //this.text = text.replaceAll("\\,", " ");
            if( condition != null ) {
                String temp = condition.replaceAll("\\(", "");
                temp = temp.replaceAll("\\)", "");
                String[] conds = temp.split(",");
                for( int i=0;i<conds.length;i++ ) {
                    int value = Integer.parseInt(conds[i].trim());
                    conditionArray[i][value] = true;
                }
            }
        }

        public String[] getWords() {
            String[] words = text.split(" ");
            ArrayList<String> resArray = new ArrayList<String>();
            ArrayList<String> tempArray = new ArrayList<String>();
            for( int i=0;i<words.length;i++ ) {
                boolean noAdd = false;
                String word = words[i];
                if(isTag(word) ) {

                    // can contain ". and ,"
                    word = word.replaceAll("\\.", " \\. ");
                    word = word.replaceAll(",", " , ");

                    String[] tagWords = word.split(" ");
                    if( tagWords.length > 1) {
                        for( int w=0;w<tagWords.length;w++ ) {
                            String tagWord = tagWords[w];
                            tagWord = tagWord.replaceAll(" \\. ", ".");
                            tagWord = tagWord.replaceAll(" , ", ",");
                            tempArray.add(tagWord);
                            noAdd = true;
                        }
                    }
                }
                if( !noAdd ) {
                    tempArray.add(word);
                }
            }

            for( int i=0;i<tempArray.size();i++ ) {
                boolean noAdd = false;
                String word = tempArray.get(i);
                if( isTag(word)) {
                    if( tempArray.size() > i+1 ) {
                        if( isTag(tempArray.get(i+1)) ) {
                            // 2 tags in row, add an " " word
                            resArray.add(word);
                            resArray.add(" ");
                            noAdd = true;
                        } else if( !(tempArray.get(i+1).equals(".") || tempArray.get(i+1).equals(",")) ) {
                            tempArray.set(i+1, " " + tempArray.get(i+1));
                        }
                    }
                } else if( i+1 != tempArray.size() ) {
                    // add empty space to all non tags except the last
                    word += " ";
                }
                if( !noAdd ) {
                    resArray.add(word);
                }

            }
            String [] res = resArray.toArray(words);
            return res;
        }


        private boolean isTag(String word) {
            Pattern p = Pattern.compile("__.*__");
            Matcher m = p.matcher(word);
            if( m.find()) {

            //if( word.matches("__.*__") ) {
                return true;
            }
            return false;
        }


    }

    private class HashInfo implements Comparable {
        String tag;
        String word;
        int count;
        int pos;
        boolean isTag;
        boolean [][] conditionArray = new boolean[4][3];

        public boolean isAllMarked() {
            for( int t=0;t<4;t++ )  {
                for( int c=0;c<3;c++ ) {
                    if(!conditionArray[t][c]) {
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean isTag() {
            if( word.matches("__.*__") ) {
                return true;
            }
            return false;
        }

        public String toString() {
            String res = word + " ";
            for( int t=0;t<4;t++ ) {
                for( int c=0;c<3;c++ ) {
                    if( conditionArray[t][c] ) {
                        res += "J";
                    } else {
                        res += "N";
                    }

                }
                res += "-";
            }
            return res;
        }

        private String getConditionString(int type) {

            String cond = "(";
            for( int i=0;i<4;i++ ) {
                if( type != -1 && type != i) {
                    cond += "*";
                } else if( conditionArray[i][0] &&
                    conditionArray[i][1] &&
                    conditionArray[i][2] ) {
                    cond += "*";
                } else if( conditionArray[i][0] &&
                    conditionArray[i][1] &&
                    ! conditionArray[i][2] ) {
                    cond += "0-1";
                } else if( conditionArray[i][0] &&
                    ! conditionArray[i][1] &&
                    ! conditionArray[i][2] ) {
                    cond += "0";
                } else if( conditionArray[i][0] &&
                    ! conditionArray[i][1] &&
                    conditionArray[i][2] ) {
                    cond += "2-";
                } else if( !conditionArray[i][0] &&
                    conditionArray[i][1] &&
                    ! conditionArray[i][2] ) {
                    cond += "1";
                } else if( !conditionArray[i][0] &&
                    conditionArray[i][1] &&
                    conditionArray[i][2] ) {
                    cond += "1-";
                } else if( ! conditionArray[i][0] &&
                    ! conditionArray[i][1] &&
                    conditionArray[i][2] ) {
                    cond += "2-";
                }


                if( i!= 3) {
                    cond += ",";
                } else {
                    cond += ")";
                }
            }
            return cond;
        }

        public void print(String cond) {
            String wordPrint = word;
            if( !isTag() ) {
                wordPrint = "\"" + word + "\"";
            } else {
                wordPrint = getModifiedString(word, tag);
            }
            System.out.println(cond + " " + wordPrint);
        }

        public void print(int type) {
            String cond = "(*)";
            if( type > 100 ) {
                cond = "(" + (type-100) + ")";
            } else if( type != -1 ) {
                cond = getConditionString(type);
            }
            print(cond);

        }

        public int compareTo(Object o) {
            HashInfo other = (HashInfo)o;
            if( other.pos > pos ) {
                return -1;
            } else if( other.pos < pos ) {
                return 1;
            }
            return 0;
        }

        /**
         * adds the word from info to this. Merges from previos to current
         * @param info
         */
        public boolean merge(HashInfo info, int type) {
            if( !info.isTag() && !isTag() && pos-1 == info.pos &&
                    getConditionString(type).equals(info.getConditionString(type)) &&
                    info.count == count) {
                word = info.word + word;
                return true;
            } else {
                return false;
            }

        }
    }


    public static void main(String [] args) {
        if( args.length < 1 ) {
            usage();
            return;
        }

        String fileName = args[0];
        String andWord = null;
        File f = new File(fileName);
        if(!f.isFile() || !f.exists()) {
            System.out.println("File " + fileName + " does not exist or is not a file.");
            return;
        }
        if( args.length > 1 ) {
            andWord = args[1];
        }

        try {
            TemplateConverter converter = new TemplateConverter(f, andWord);
            converter.parse();
            converter.print();
        } catch (Exception e) {
            System.out.println("Failed to convert file: " + e.toString() );
            e.printStackTrace();
        }
    }

    public static void usage() {
        System.out.println("Usage: java TemplateConverter <phr file> <word for and>"  );

    }

    private String[] conditionTable = { "(1,0,0,0)", "(2,0,0,0)",
                                        "(0,1,0,0)", "(0,2,0,0)",
                                        "(0,0,1,0)", "(0,0,2,0)",
                                        "(0,0,0,1)", "(0,0,0,2)"};
}
