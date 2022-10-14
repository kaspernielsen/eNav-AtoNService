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
import org.grad.secom.core.base.DigitalSignatureCertificate;
import org.grad.secom.core.base.SecomSignatureProvider;
import org.grad.secom.core.models.enums.DigitalSignatureAlgorithmEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

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

    /**
     * Returns the digital signature algorithm for the signature provider.
     * In SECOM, by default this should be DSA, but ECDSA should be used
     * to generate smaller signatures.
     *
     * @return the digital signature algorithm for the signature provider
     */
    @Override
    public DigitalSignatureAlgorithmEnum getSignatureAlgorithm() {
        return DigitalSignatureAlgorithmEnum.ECDSA;
    }

    /**
     * This function overrides the interface definition to link the SECOM
     * signature provision with the cKeeper operation. A service can request
     * cKeeper to sign a payload, using a valid certificate based on the
     * provided digital signature certificate information.
     *
     * @param signatureCertificate  The digital signature certificate to be used for the signature generation
     * @param algorithm             The algorithm to be used for the signature generation
     * @param payload               The payload to be signed
     * @return
     */
    @Override
    public String generateSignature(DigitalSignatureCertificate signatureCertificate, DigitalSignatureAlgorithmEnum algorithm, byte[] payload) {
        // Get the signature generated from cKeeper
        final Response response = this.cKeeperClient.generateCertificateSignature(
                new BigInteger(signatureCertificate.getCertificateAlias()),
                Optional.ofNullable(algorithm).map(DigitalSignatureAlgorithmEnum::getValue).orElse(DigitalSignatureAlgorithmEnum.ECDSA.getValue()),
                payload);

        // Parse the response
        try {
            return DatatypeConverter.printHexBinary(response.body().asInputStream().readAllBytes());
        } catch (IOException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }
}
