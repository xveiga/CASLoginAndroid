package com.example.caslogin.data.utils;

import android.net.SSLCertificateSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSSocketFactory extends SSLSocketFactory {

    private String[] enabledProtocols = {"TLSv1"};
    private String[] enabledCipherSuites = {"TLS_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"};

    private SSLSocketFactory sf;

    public TLSSocketFactory() {
        sf = (SSLSocketFactory) SSLCertificateSocketFactory.getDefault(0);
    }

    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setEnabledProtocols(String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sf.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sf.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
        SSLSocket sslSock = (SSLSocket) sf.createSocket(socket, s, i, b);
        sslSock.setEnabledProtocols(enabledProtocols);
        //sslSock.setEnabledCipherSuites(enabledCipherSuites);
        return sslSock;
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
        SSLSocket sslSock = (SSLSocket) sf.createSocket(s, i);
        sslSock.setEnabledProtocols(enabledProtocols);
        //sslSock.setEnabledCipherSuites(enabledCipherSuites);
        return sslSock;
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException {
        SSLSocket sslSock = (SSLSocket) sf.createSocket(s, i, inetAddress, i1);
        sslSock.setEnabledProtocols(enabledProtocols);
        //sslSock.setEnabledCipherSuites(enabledCipherSuites);
        return sslSock;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        SSLSocket sslSock = (SSLSocket) sf.createSocket(inetAddress, i);
        sslSock.setEnabledProtocols(enabledProtocols);
        //sslSock.setEnabledCipherSuites(enabledCipherSuites);
        return sslSock;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        SSLSocket sslSock = (SSLSocket) sf.createSocket(inetAddress, i, inetAddress1, i1);
        sslSock.setEnabledProtocols(enabledProtocols);
        sslSock.setEnabledCipherSuites(enabledCipherSuites);
        return sslSock;
    }
}
