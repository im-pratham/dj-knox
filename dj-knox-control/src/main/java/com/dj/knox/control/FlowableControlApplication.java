package com.dj.knox.control;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import com.flowable.control.logic.domain.ServerConfig;
import com.flowable.control.logic.service.http.HttpClientProvider;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

@SpringBootApplication
public class FlowableControlApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(FlowableControlApplication.class, args);
    }

    @Bean
    HttpClientProvider httpClientProvider () throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
    KeyStoreException, CertificateException, FileNotFoundException, IOException{
        return new HttpClientProvider(){
        
            @Override
            public CloseableHttpClient getHttpClient(ServerConfig serverConfig) {
                SSLContext sslContext;
                try {
                    sslContext = SSLContextBuilder.create()
                            .loadKeyMaterial(ResourceUtils.getFile("../keystore/admin.p12"), "changeit".toCharArray(),
                                    "changeit".toCharArray())
                            .build();
                            return HttpClients.custom().setSSLContext(sslContext).build();
                } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException
                        | KeyStoreException | CertificateException | IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unable to create the http client.", e);
                }
            }
        };
    }

}
