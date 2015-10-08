/**
 * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.volley_examples;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


import javax.net.ssl.HostnameVerifier;  
import javax.net.ssl.HttpsURLConnection;  
import javax.net.ssl.SSLContext;  
import javax.net.ssl.SSLSession; 

import org.apache.http.conn.ssl.SSLSocketFactory;


public class SsX509TrustManager implements javax.net.ssl.X509TrustManager {
	 private static TrustManager[] trustManagers;
    private ArrayList<X509TrustManager> mX509TrustManagers = new ArrayList<X509TrustManager>();

    protected SsX509TrustManager(InputStream keyStore, String keyStorePassword) throws GeneralSecurityException {
        // first add original trust manager
        final TrustManagerFactory originalFactory = 
        		TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        originalFactory.init((KeyStore) null);
        for( TrustManager tm : originalFactory.getTrustManagers() ) {
            if (tm instanceof X509TrustManager) {
                mX509TrustManagers.add( (X509TrustManager)tm );
            }
        }
        
        
        // them add our custom trust manager
        X509TrustManager mX509TrustManagerCustom = fetchTrustManager(keyStore, keyStorePassword);
        if (mX509TrustManagerCustom != null) {
            mX509TrustManagers.add(mX509TrustManagerCustom);
        } else {
            throw new IllegalArgumentException("Keystore is valid but cannot find TrustManagerFactory of type X509TrustManager.");
        }
    }




	public SsX509TrustManager() {
		// TODO Auto-generated constructor stub
	}




	private javax.net.ssl.X509TrustManager fetchTrustManager(InputStream keyStore, String keyStorePassword) throws GeneralSecurityException {
        javax.net.ssl.X509TrustManager ret = null;
        
        TrustManagerFactory tmf = prepareTrustManagerFactory(keyStore, keyStorePassword);
        TrustManager tms[] = tmf.getTrustManagers();            
        
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof javax.net.ssl.X509TrustManager) {
                ret = (javax.net.ssl.X509TrustManager) tms[i];
//              break;
            }
        }
        
        return ret;
    }


    private TrustManagerFactory prepareTrustManagerFactory(InputStream keyStore, String keyStorePassword) throws GeneralSecurityException {
        TrustManagerFactory ret = null;
        
        KeyStore ks;
        ks = KeyStore.getInstance("BKS");
        try {
            ks.load(keyStore, keyStorePassword.toCharArray());
        } catch (IOException e) {
            throw new GeneralSecurityException("Problem reading keystore stream", e);
        }
        ret = TrustManagerFactory.getInstance("X509");
        ret.init(ks);
        
        return ret;
    }


    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // Oh, I am easy!
    }


    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        boolean ok = false;
        for( X509TrustManager tm : mX509TrustManagers ) {
            try {
                tm.checkServerTrusted(chain,authType);
                ok = true;
                break;
            } catch( CertificateException e ) {
                // ignore
            }
        }
        if (!ok) { 
            throw new CertificateException();
        }
    }

    
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
        for( X509TrustManager tm : mX509TrustManagers )
            list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
        return list.toArray(new X509Certificate[list.size()]);
    }
    
    
    public static void allowAllSSL() {  
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {  
            @Override  
            public boolean verify(String arg0, SSLSession arg1) {  
                return true;  
            }  
        });  
  
        SSLContext context = null;  
        if (trustManagers == null) {  
            trustManagers = new TrustManager[] { new SsX509TrustManager() };  
        }  
  
        try {  
            context = SSLContext.getInstance("TLS");  
            context.init(null, trustManagers, new SecureRandom());  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        } catch (KeyManagementException e) {  
            e.printStackTrace();  
        }  
        
        HttpsURLConnection.setDefaultSSLSocketFactory(context  
                .getSocketFactory());  
    }  
    
    
    public static void trustEveryone() {   
    	try {   
    		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){   
    			public boolean verify(String hostname, SSLSession session) {   
    				return true;   
    			}});   
    		SSLContext context = SSLContext.getInstance("TLS");   
    		context.init(null, new X509TrustManager[]{new X509TrustManager(){   
    			public void checkClientTrusted(X509Certificate[] chain,   
    					String authType) throws CertificateException {}   
    			public void checkServerTrusted(X509Certificate[] chain,   
    					String authType) throws CertificateException {}   
    			public X509Certificate[] getAcceptedIssuers() {   
    				return new X509Certificate[0];   
    			}}}, new SecureRandom());   

    		HttpsURLConnection.setDefaultSSLSocketFactory(   
    				context.getSocketFactory());   
    	} catch (Exception e) { // should never happen   
    		e.printStackTrace();   
    	}   
}   
    

}
