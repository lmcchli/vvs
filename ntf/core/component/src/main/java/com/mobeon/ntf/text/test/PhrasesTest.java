/****************************************************************
 * COPYRIGHT ABCXYZ RADIO SYSTEMS AB, 2002
 *
 * The copyright to the computer program(s) herein is the property of ABCXYZ
 * RADIO SYSTEMS AB, Sweden. The program(s) may be used and/or copied
 * only with the written permission from ABCXYZ RADIO SYSTEMS AB or
 * in accordance with the terms and conditions stipulated in the agreement/
 * contract under which the program(s) have been supplied.
 */
package com.mobeon.ntf.text.test;

import com.mobeon.ntf.text.Phrases;
import com.mobeon.ntf.text.TemplateTokenizerCphr;
import com.mobeon.ntf.test.NtfTestCase;
import java.util.*;

public class PhrasesTest extends NtfTestCase {

    public PhrasesTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

     public void test() throws Exception {
         l("test");
        //int i;
        //Enumeration e;
        //String name;
        //Properties pr;
        String cphr="";
        Phrases.refresh();
        //pr= p.getTemplateStrings("sv");
        //      for (e = pr.keys() ; e.hasMoreElements() ;) {
        //          name= (String)(e.nextElement());
        //          System.out.println("\t" + name + "\t" + pr.getProperty(name));
        //      }
        //      pr= p.getTemplateStrings("ru");
        //      for (e = pr.keys() ; e.hasMoreElements() ;) {
        //          name= (String)(e.nextElement());
        //          System.out.println("\t" + name + "\t" + pr.getProperty(name));
        //      }
        //      pr= p.getTemplateStrings("no");
        //      for (e = pr.keys() ; e.hasMoreElements() ;) {
        //          name= (String)(e.nextElement());
        //          System.out.println("\t" + name + "\t" + pr.getProperty(name));
        //      }
        //  pr= p.getTemplateStrings("en");
        // for (e = pr.keys() ; e.hasMoreElements() ;) {
        // name= (String)(e.nextElement());
        // System.out.println("\t" + name + "\t" + pr.getProperty(name));
        //}
        if(Phrases.isCphrPhraseFound("en","c", null))
            cphr = Phrases.getCphrTemplateStrings("en");
        //System.out.println(cphr);

        TemplateTokenizerCphr ttline = new TemplateTokenizerCphr(cphr);
        String line = "";
        String s="";
        while ((line = ttline.getNextLine()) != null) {
            TemplateTokenizerCphr tt = new TemplateTokenizerCphr(line);
            while((s = tt.getNext()) != null);
        }
        assertTrue(cphr.indexOf("You have") != -1);
    }
}

