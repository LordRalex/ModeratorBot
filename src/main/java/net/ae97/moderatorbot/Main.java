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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import org.pircbotx.exception.IrcException;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class Main {

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws IrcException, IOException, MessagingException {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("nick", "MCFTopic");
        parameters.put("channel", "#mcftopicbot");
        parameters.put("server", "irc.esper.net");
        parameters.put("port", "6667");
        parameters.put("pass", null);
        parameters.put("mailserver", "localhost");
        parameters.put("mailuser", "username");
        parameters.put("mailpass", "password");
        parameters.put("prefix", "http://");
        parameters.put("suffix", "unread");
        if (args.length == 0) {
            try (BufferedReader reader = new BufferedReader(new FileReader(new File("config.cfg")))) {
                String[] lines = (String[]) reader.lines().toArray();
                for (String line : lines) {
                    String[] parts = line.split("=");
                    if (parts.length != 2) {
                        continue;
                    }
                    parameters.put(parts[0], parts[1]);
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
        MasterBot masterBot = new MasterBot(parameters.get("channel"));
        masterBot.connect(parameters.get("server"), Integer.parseInt(parameters.get("port")), parameters.get("nick"), parameters.get("pass"));
        MailHandler handler = new MailHandler(masterBot, parameters.get("mailserver"), parameters.get("mailuser"), parameters.get("mailpass"), parameters.get("prefix"), parameters.get("suffix"));
        executorService.scheduleWithFixedDelay(handler, 50, 50, TimeUnit.MILLISECONDS);
    }
}
