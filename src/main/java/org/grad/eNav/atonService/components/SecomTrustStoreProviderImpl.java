/*
 * Copyright (c) 2022 GLA Research and Development Directorate
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

package org.grad.eNav.atonService.components;

import lombok.extern.slf4j.Slf4j;
import org.grad.secom.core.base.SecomTrustStoreProvider;
import org.grad.secom.core.utils.KeyStoreUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * The SECOM Trust Store Provider Implementation.
 *
 * This class contains the implementation of the SECOM trust store provider.
 * This is required for the SECOM library to be able to automatically pick up
 * the keystore that contains the trusted SECOM CA certificate chain.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Slf4j
public class SecomTrustStoreProviderImpl implements SecomTrustStoreProvider {

    /**
     * The X.509 Trust-Store.
     */
    @Value("${gla.rad.aton-service-client.secom.trustStore:truststore.jks}")
    String trustStore;

    /**
     * The X.509 Trust-Store Password.
     */
    @Value("${gla.rad.aton-service-client.secom.trustStorePassword:password}")
    String trustStorePassword;

    /**
     * The X.509 Trust-Store Type.
     */
    @Value("${gla.rad.aton-service-client.secom.trustStoreType:JKS}")
    String trustStoreType;

    /**
     * The X.509 Root Certificate Alias.
     */
    @Value("${gla.rad.aton-service-client.secom.rootCertificateAlias:rootCertificate}")
    String rootCertificateAlias;

    /**
     * Returns the alias of the root certificate as it is contained in the
     * provided trust store. This can be used to pinpoint the actual root
     * certificate entry.
     *
     * @return the alias of the root certificate
     */
    @Override
    public String getCARootCertificateAlias() {
        return this.rootCertificateAlias;
    }

    /**
     * Returns the trust store that contains the trusted SECOm certificate chain.
     * This is required to validate the received certificate for every applicable
     * request.
     *
     * @return the SECOM trust store
     */
    @Override
    public KeyStore getTrustStore() {
        try {
            return KeyStoreUtils.getKeyStore(this.trustStore, this.trustStorePassword, this.trustStoreType);
        } catch (KeyStoreException | NoSuchAlgorithmException | IOException | CertificateException ex) {
            return null;
        }
    }

}
