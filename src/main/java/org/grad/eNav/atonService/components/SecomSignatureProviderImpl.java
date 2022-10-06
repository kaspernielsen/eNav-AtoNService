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

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.atonService.feign.CKeeperClient;
import org.grad.eNav.atonService.models.dtos.McpEntityType;
import org.grad.secom.core.exceptions.SecomInvalidCertificateException;
import org.grad.secom.core.interfaces.SecomSignatureProvider;
import org.grad.secom.core.models.DigitalSignatureValue;
import org.grad.secom.core.models.SECOM_ExchangeMetadataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;

/**
 * The SECOM Signature Provider Implementation.
 *
 * In the current e-Navigation Service Architecture, it's the cKeeper
 * microservice that is responsible for generating the validating the
 * SECOM message signatures.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Slf4j
public class SecomSignatureProviderImpl implements SecomSignatureProvider {

    /**
     * The cKeeper Feign Client.
     */
    @Autowired
    @Lazy
    CKeeperClient cKeeperClient;

    // Class Variables
    final public static String DATA_PROTECTION_SCHEME = "SECOM";
    final public String CKEEPER_SERVICE_NAME = "aton-service";
    final public static String CKEEPER_PUBLIC_CERTIFICATE_HEADER = "PUBLIC_CERTIFICATE";
    final public static String CKEEPER_SIGNATURE_ALGORITHM_HEADER = "SIGNATURE_ALGORITHM";
    final public static String CKEEPER_ROOT_CERTIFICATE_THUMBPRINT = "ROOT_CERTIFICATE_THUMBPRINT";

    /**
     * The signature generation function. It simply required the payload that
     * will be used to generate the signature, which will be returned as a
     * String.
     *
     * @param payload       The payload to generate the signature for
     * @return The signature generated
     */
    @Override
    public void generateSignature(byte[] payload, SECOM_ExchangeMetadataObject metadata) {
        // Sanity Checks
        if(metadata == null) {
            return;
        }
        try {
            // Get the signature from cKeeper
            final Response response = this.cKeeperClient.generateEntitySignature(
                    CKEEPER_SERVICE_NAME,
                    null,
                    McpEntityType.SERVICE.getValue(),
                    payload);

            // Parse the response
            final String certificate = response.headers().get(CKEEPER_PUBLIC_CERTIFICATE_HEADER).stream().findFirst().orElse(null);
            final String algorithm = response.headers().get(CKEEPER_SIGNATURE_ALGORITHM_HEADER).stream().findFirst().orElse(null);
            final String rootCertThumbprint = response.headers().get(CKEEPER_ROOT_CERTIFICATE_THUMBPRINT).stream().findFirst().orElse(null);
            final String signature = DatatypeConverter.printHexBinary(response.body().asInputStream().readAllBytes());

            // And now populate the SECOM metadata
            if(metadata.getDigitalSignatureValue() == null) {
                metadata.setDigitalSignatureValue(new DigitalSignatureValue());
            }
            metadata.setDataProtection(Boolean.TRUE);
            metadata.setProtectionScheme(DATA_PROTECTION_SCHEME);
            metadata.setCompressionFlag(Boolean.FALSE);
            metadata.setDigitalSignatureReference(algorithm);
            metadata.getDigitalSignatureValue().setDigitalSignature(signature);
            metadata.getDigitalSignatureValue().setPublicCertificate(certificate);
            metadata.getDigitalSignatureValue().setPublicRootCertificateThumbprint(rootCertThumbprint);
        } catch(Exception ex) {
            throw new SecomInvalidCertificateException(ex.getMessage());
        }
    }
}
