/*
 * Copyright (C) 2013 Lord_Ralex
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ae97.moderatorbot;

import java.util.HashSet;
import java.util.Set;
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

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public final class MailHandler implements Runnable {

    private final MasterBot driver;
    private final Session session;
    private final Store store;
    private final MessagePattern pattern;
    private final Folder inbox;

    public MailHandler(MasterBot d, String server, String user, String pass, String prefix, String suffix) throws MessagingException {
        driver = d;
        session = Session.getDefaultInstance(System.getProperties(), null);
        Store temp;
        try {
            temp = session.getStore("imap");
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(MailHandler.class.getName()).log(Level.SEVERE, null, ex);
            temp = null;
        }
        store = temp;
        pattern = new MessagePattern(prefix, suffix);
        store.connect(server, user, pass);
        inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_WRITE);
    }

    @Override
    public void run() {
        try {
            Set<String> map = new HashSet<>();
            synchronized (inbox) {
                Message[] messages = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
                for (Message message : messages) {
                    Address[] from = message.getFrom();
                    String sender = from == null ? null : ((InternetAddress) from[0]).getAddress();
                    if (sender == null) {
                        continue;
                    }
                    switch (Sender.getSender(sender)) {
                        case CURSE: {
                            String topicLink;
                            synchronized (pattern) {
                                topicLink = pattern.getResult(message.getSubject().split("/n"));
                            }
                            if (topicLink != null) {
                                map.add(topicLink);
                            }
                        }
                        break;
                        case VIRUSTOTAL: {
                        }
                        break;
                    }
                    message.setFlag(Flag.SEEN, true);
                    message.setFlag(Flag.DELETED, true);
                }
            }
            if (map.isEmpty()) {
                return;
            }
            for (String entry : map) {
                driver.sendMessage("New topic posted: " + entry);
            }
            map.clear();
        } catch (MessagingException ex) {
            Logger.getLogger(MailHandler.class.getName()).log(Level.SEVERE, "A mail error occurred:", ex);
        }

    }

    private enum Sender {

        CURSE("noreply@curse.com"),
        VIRUSTOTAL("scan@virustotal.com"),
        NONE("");
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

    private class MessagePattern {

        private final String charsBefore;
        private final String charsAfter;

        public MessagePattern(String before, String after) {
            charsBefore = before;
            charsAfter = after;
        }

        public String getResult(String[] message) {
            for (String line : message) {
                if (!line.contains(charsBefore) && !line.contains(charsAfter)) {
                    return null;
                }
                String part = line.substring(charsBefore.length());
                return part.substring(0, part.length() - charsAfter.length());
            }
            return null;
        }

    }
}
