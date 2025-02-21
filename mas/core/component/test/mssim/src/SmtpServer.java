import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import java.util.Iterator;

public class SmtpServer {

    public static void main(String argv[]) throws Exception {
        int smtpPort = 2525;
        int optind = 0;
        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-p")) {
                smtpPort = Integer.parseInt(argv[++optind]);
            } else if (argv[optind].startsWith("-")) {
                System.out.println("Usage: SmtpServer [-p smtpPort]");
                System.exit(1);
            } else {
                break;
            }
        }

        SimpleSmtpServer smtpServer = SimpleSmtpServer.start(smtpPort);
        System.out.println("SmtpServer started on smtpPort: " + smtpPort);

        while (true) {
            Thread.sleep(10 * 1000);
            Iterator it = smtpServer.getReceivedEmail();
            while (it.hasNext()) {
                SmtpMessage msg = (SmtpMessage) it.next();
                System.out.println(msg.getHeaderValue("Subject"));
                //System.out.println(msg);
                it.remove();
            }
        }
    }
}
