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
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PrincipalUtil;
import org.grad.eNav.atonService.feign.CKeeperClient;
import org.grad.eNav.atonService.models.dtos.SignatureVerificationRequestDto;
import org.grad.secom.core.base.DigitalSignatureCertificate;
import org.grad.secom.core.base.SecomSignatureProvider;
import org.grad.secom.core.models.enums.DigitalSignatureAlgorithmEnum;
import org.grad.secom.core.utils.SecomPemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
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
     * The Application Name.
     */
    @Value("${spring.application.name:aton-service}")
    String appName;

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
     * @param payload               The payload to be signed, (preferably Base64 encoded)
     * @return The signature generated
     */
    @Override
    public byte[] generateSignature(DigitalSignatureCertificate signatureCertificate, DigitalSignatureAlgorithmEnum algorithm, byte[] payload) {
        // Get the signature generated from cKeeper
        final Response response = this.cKeeperClient.generateCertificateSignature(
                new BigInteger(signatureCertificate.getCertificateAlias()),
                algorithm.getValue(),
                Optional.ofNullable(payload).orElse(new byte[]{}));

        // Make sure the response is valid
        if(response == null || response.body() == null) {
            return null;
        }

        // Parse the response
        try {
            return response.body().asInputStream().readAllBytes();
        } catch (IOException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    /**
     * The signature validation operation. This should support the provision
     * of the message content (expected in a Base64 format) and the signature
     * to validate the content against.
     *
     * @param signatureCertificate  The digital signature certificate to be used for the signature generation
     * @param algorithm             The algorithm used for the signature generation
     * @param content               The context (in Base64 format) to be validated
     * @param signature             The signature to validate the context against
     * @return whether the signature validation was successful or not
     */
    @Override
    public boolean validateSignature(String signatureCertificate, DigitalSignatureAlgorithmEnum algorithm, byte[] signature, byte[] content) {
        // Construct the signature verification object
        final SignatureVerificationRequestDto verificationRequest = new SignatureVerificationRequestDto();
        verificationRequest.setContent(new String(content, StandardCharsets.UTF_8));
        verificationRequest.setSignature(Base64.getEncoder().encodeToString(signature));

        // Ask cKeeper to verify the signature
        X509Certificate certificate = null;
        try {
            certificate = SecomPemUtils.getCertFromPem(signatureCertificate);
        } catch (CertificateException ex) {
            log.error(ex.getMessage());
        }
        final Response response = this.cKeeperClient.verifyEntitySignature(
                Optional.ofNullable(certificate)
                        .map(c -> {
                            try {
                                return PrincipalUtil.getSubjectX509Principal(c);
                            } catch (CertificateEncodingException ex) {
                                return null;
                            }
                        })
                        .map(p -> p.getValues(X509Name.CN))
                        .map(v -> v.get(0))
                        .map(String::valueOf)
                        .orElse(this.appName),
                verificationRequest);

        // Make sure the response is valid
        if(response == null) {
            return false;
        }

        // If everything went OK, return a positive response
        return response.status() < 300;
    }

}
