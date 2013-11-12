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

import java.io.IOException;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

/**
 * @version 1.0
 * @author Lord_Ralex
 */
public class MasterBot {

    protected final PircBotX driver;
    protected final String channel;

    public MasterBot(String mainChan) {
        driver = new PircBotX();
        channel = mainChan;
    }

    public void connect(String server, int port, String nick, String pass) throws IrcException, IOException {
        driver.setName(nick);
        driver.connect(server, port);
        if (pass != null && !pass.isEmpty()) {
            driver.identify(pass);
        }
        driver.joinChannel(channel);
    }

    public void sendMessage(String... message) {
        synchronized (driver) {
            for (String m : message) {
                driver.sendMessage(channel, m);
            }
        }
    }
}
