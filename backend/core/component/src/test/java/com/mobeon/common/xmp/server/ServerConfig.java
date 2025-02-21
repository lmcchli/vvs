package com.mobeon.common.xmp.server;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-mar-08
 * Time: 12:25:49
 * To change this template use File | Settings | File Templates.
 */
public class ServerConfig extends Thread {

    private static ServerConfig inst;

    private ArrayList replyItems;

    public static ServerConfig get() {
        if( inst == null ) {
            inst = new ServerConfig();
        }
        return inst;

    }

    private ServerConfig() {
        replyItems = new ArrayList();
        try {
            parseDirectory("./rsp");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        start();

    }

    public int getCode(String number, String service) {
        for( int i=0;i<replyItems.size();i++ ) {
            ReplyItem item = (ReplyItem) replyItems.get(i);
            if( item.matches(number, service)) {
                return item.code;
            }
        }
        return 200;


    }

    public int getReplyTime(String number, String service) {
       for( int i=0;i<replyItems.size();i++ ) {
            ReplyItem item = (ReplyItem) replyItems.get(i);
            if( item.matches(number, service)) {
                return item.replyTime;
            }
        }
        return 0;
    }

    public Properties getParams(String number, String service) {
        for( int i=0;i<replyItems.size();i++ ) {
            ReplyItem item = (ReplyItem) replyItems.get(i);
            if( item.matches(number, service)) {
                return item.params;
            }
        }
        return null;


    }

    private void parseDirectory(String dir) throws IOException {
        // files in the format <SERVICE>_(<Number>).resp
        File directory = new File(dir);
        if( directory.exists() && directory.isDirectory() ) {
            String[] fileNames = directory.list();
            File[] files = directory.listFiles();
            for( int i=0;i<fileNames.length;i++ ) {
                String f = fileNames[i].toLowerCase();
                if( f.endsWith(".resp")) {
                    StringTokenizer tokenizer = new StringTokenizer(f, "_.");
                    String service = tokenizer.nextToken();
                    String num = "all";
                    if( tokenizer.countTokens() == 3 ) {
                        num = tokenizer.nextToken();
                    }
                    FileInputStream inputStream = new FileInputStream(dir + "/" + f);
                    Properties props = new Properties();
                    props.load(inputStream);
                    String codeStr = props.getProperty("code");
                    int code = 200;
                    if( codeStr == null ) {
                        code = Integer.parseInt(codeStr);
                    }
                    props.remove("code");

                    ReplyItem item = new ReplyItem();
                    item.code = code;
                    if( num.equals("all") ) {
                        num = ".*";
                    }
                    item.number = num;
                    if( service.equals("all") ){
                        service = ".*";
                    }
                    item.service = service;
                    item.params = props;
                    addReplyItem(item);
                }
            }
        }
    }

    public void run() {

        while( true ) {
            PrintWriter writer = null;
            try {
           ServerSocket listener = new ServerSocket(7777);

           Socket sock = listener.accept();
           BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
           writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
           writer.print("Welcome to XMPServer Configuration. Enter commands\n");
           writer.flush();
           while( sock != null ) {
               String line = reader.readLine();
               if( line.equalsIgnoreCase("exit")) {
                   sock.close();
                   sock = null;

               } else if( line.equalsIgnoreCase("help")) {
                   helpText(writer);


               } else if( line.startsWith("code")) {

                   StringTokenizer st = new StringTokenizer(line);
                   if( st.countTokens() != 3 ) {
                       writer.print("Not correct number of arguments for code\n");
                       writer.flush();
                   } else {
                       st.nextToken();
                       String num = st.nextToken();
                       if( num.equals("*"))
                            num = ".*";
                       String codeStr = st.nextToken();
                       ReplyItem item = new ReplyItem();
                       item.number = num;
                       item.code = Integer.parseInt(codeStr);

                       addReplyItem(item);
                   }
               } else if( line.startsWith("delay")) {

                   StringTokenizer st = new StringTokenizer(line);
                   if( st.countTokens() != 3 ) {
                       writer.print("Not correct number of arguments for delay\n");
                       writer.flush();
                   } else {
                       st.nextToken();
                       String num = st.nextToken();
                       if( num.equals("*"))
                            num = ".*";
                       String codeStr = st.nextToken();
                       ReplyItem item = new ReplyItem();
                       item.number = num;
                       item.replyTime = Integer.parseInt(codeStr);

                       addReplyItem(item);
                   }
               } else if( line.startsWith("param")) {
                   StringTokenizer st = new StringTokenizer(line);
                   if( st.countTokens() < 3 ) {
                       writer.print("Not correct number of arguments for param\n");
                       writer.flush();
                   } else {
                       st.nextToken();
                       String num = st.nextToken();
                       if( num.equals("*"))
                            num = ".*";
                       String paramKey = st.nextToken();
                       String paramValue = "";
                       if( st.hasMoreTokens() )
                        paramValue = st.nextToken();

                       ReplyItem item = getReplyItem(num);
                       if( item == null ){
                           item = new ReplyItem();
                           item.code = 200;
                           item.number = num;
                           addReplyItem(item);
                       }
                       if( paramKey.equalsIgnoreCase("clear")) {
                          item.params.clear();
                       } else {

                            item.params.put(paramKey, paramValue);
                       }

                   }
               } else if( line.startsWith("load")) {
                   StringTokenizer st = new StringTokenizer(line);
                   if( st.countTokens() < 2 ) {
                       writer.print("Not correct number of arguments for load\n");
                       writer.flush();
                   } else {
                       st.nextToken();
                       String directory = st.nextToken();
                       parseDirectory(directory);

                   }
               } else {
                   helpText(writer);
               }

           }
            } catch(IOException e)  {
                if( writer != null ) {
                    writer.print("Got exception!\n");
                    e.printStackTrace(writer);
                    writer.flush();
                } else {
                    e.printStackTrace();
                }

            }
        }
    }

    private void helpText(PrintWriter writer) {
        writer.print("Commands:\n");
        writer.print("help - get help text\n");
        writer.print("exit - diconnect\n");
        writer.print("code <number> <code> - sets return code <code> for <number> \n");
        writer.print("param <number> <param-key> <param-value> - sets return parameter for <number>\n");
        writer.print("param <number> clear - clears all parameters for <number>\n");
        writer.print("dalay <number> <seconds> - delay teh answer for X seconds. Default is 0. -1 for no answer att all \n");
        writer.print("load <directory> - loads files from <directory>\n");
        writer.print("\n<number> can be exact number or an expression like \"22*\" or \"*\" for all numbers\n");
        writer.print("The files in the <directory> shall be on the format <SERVICE>_<NUM>.resp\n"  );
        writer.print("both <SERVICE> and <NUM> can be \"all\" to indicate that it applies to all services and numbers\n");
        writer.print("The format on the files are:\n");
        writer.print("code=200\n");
        writer.print("param1=value1\n");
        writer.print("param2=value2\n");
        writer.print("...\n\n");
        writer.flush();
    }

    private ReplyItem  getReplyItem(String num) {
        for( int i=0;i<replyItems.size();i++ ) {
             ReplyItem item = (ReplyItem) replyItems.get(i);
            if( item.number.equals(num)) {
                return item;
            }
        }
        return null;
    }

    private void addReplyItem(ReplyItem item) {
        if( replyItems.size() == 0 ) {
            replyItems.add(item);
        } else {
            boolean added = false;
            for( int i=0;i<replyItems.size(); i++ ) {
                ReplyItem listItem = (ReplyItem) replyItems.get(i);
                if( listItem.number.equals( item.number ) && listItem.service.equals( item.service)) {
                    listItem.code = item.code;
                    listItem.params = item.params;
                } else if( item.number.length() <= listItem.number.length()  ) {
                    replyItems.add(i, item);
                } else { }
            }
            if( !added ) {
                replyItems.add(replyItems.size(), item);
            }

        }
    }


    private class ReplyItem {
        private String number;
        private int code = 200;
        private Properties params = new Properties();
        private int replyTime = 0;
        private String service;

        public boolean matches(String searchNumber) {
            if( searchNumber.matches(number)) {
                return true;
            }
            return false;
        }

        public boolean matches(String searchNumber, String service) {
            if( searchNumber.matches(number) &&
                    (service == null || service.matches(this.service) || service.equals(this.service)) ) {
                return true;
            }
            return false;

        }
    }

}
