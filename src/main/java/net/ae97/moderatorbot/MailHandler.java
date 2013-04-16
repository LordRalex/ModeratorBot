package net.ae97.moderatorbot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import org.pircbotx.PircBotX;

/**
 * @version 1.0
 * @author Laptop
 */
public class MailHandler extends Thread {

    protected final PircBotX driver;
    protected Session session;
    protected Store store;
    protected Map<String, MCF> map = new ConcurrentHashMap<String, MCF>();

    public MailHandler(PircBotX d) {
        super();
        this.setName("Mail_Thread_" + this.getName());
        driver = d;
        session = Session.getDefaultInstance(System.getProperties(), null);
        try {
            store = session.getStore("imap");
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(MailHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String relayChannel = "";
        while (driver.getChannelsNames().contains(relayChannel)) {
            try {
                store.connect("localhost", "username", "password");
                Folder inbox = store.getFolder("Inbox");
                inbox.open(Folder.READ_WRITE);
                Message[] messages = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
                for (Message message : messages) {
                    Address[] from = message.getFrom();
                    String sender = from == null ? null : ((InternetAddress) from[0]).getAddress();
                    if (sender == null) {
                        continue;
                    }
                    switch (Sender.getSender(sender)) {
                        case CURSE: {
                        }
                        break;
                        case VIRUSTOTAL: {
                        }
                        break;
                    }
                }
            } catch (MessagingException ex) {
                Logger.getLogger(MailHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private enum Sender {

        CURSE("noreply@curse.com"),
        VIRUSTOTAL("scan@virustotal.com"),
        NONE("None");
        private final String email;

        private Sender(String e) {
            email = e;
        }

        public static Sender getSender(String sender) {
            for (Sender senderEnum : Sender.values()) {
                if (senderEnum.email.equalsIgnoreCase(sender)) {
                    return senderEnum;
                }
            }
            return NONE;
        }
    }
}
