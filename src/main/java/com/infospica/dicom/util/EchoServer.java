package com.infospica.dicom.util;

import com.infospica.dicom.command.SCPStartCommand;
import com.infospica.dicom.context.Context;

import java.net.Socket;

public class EchoServer {

    private EchoServer() {
        super();
    }

    public static boolean isServerListening() {
        return isServerListening(Context.getStoreSCPConfigurationProperties().getHostName(), Context.getStoreSCPConfigurationProperties().getPort());
    }

    public static boolean isRemoteServerListening() {
        return isServerListening(Context.getStoreSCUConfigurationProperties().getHostName(), Context.getStoreSCUConfigurationProperties().getPort());
    }

    public static boolean isServerListening(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            return true;
        } catch (Exception e) {
            LoggerUtility.log(EchoServer.class, LoggerUtility.LogLevel.WARN, "Port not reachable. "
                    + host + "@" + port);
        }
        finally {
            if(socket != null)
                try {socket.close();}
                catch(Exception e){}
        }
        return false;
    }
}
