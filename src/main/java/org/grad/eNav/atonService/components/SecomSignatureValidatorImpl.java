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
import org.grad.eNav.atonService.models.dtos.SignatureVerificationRequestDto;
import org.grad.secom.core.base.DigitalSignatureCertificate;
import org.grad.secom.core.base.SecomSignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * The SECOM Signature Validator Implementation.
 *
 * In the current e-Navigation Service Architecture, it's the cKeeper
 * microservice that is responsible for generating the validating the
 * SECOM message signatures.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Slf4j
public class SecomSignatureValidatorImpl implements SecomSignatureValidator {

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
     * This function overrides the interface definition to link the SECOM
     * signature verification with the cKeeper operation. A service can request
     * cKeeper to verify a content, using a valid certificate and the signature
     * provided.
     *
     * @param signatureCertificate  The digital signature certificate to be used for the signature generation
     * @param content               The context (in Base64 format) to be validated
     * @param signature             The signature to validate the context against
     * @return
     */
    @Override
    public boolean validateSignature(DigitalSignatureCertificate signatureCertificate, String content, String signature) {
        // Construct the signature verification object
        final SignatureVerificationRequestDto verificationRequest = new SignatureVerificationRequestDto();
        verificationRequest.setContent(content);
        verificationRequest.setSignature(signature);

        // Ask cKeeper to verify the signature
        final Response response = this.cKeeperClient.verifyEntitySignature(this.appName, verificationRequest);

        // If everything went OK, return a positive response
        return response.status() < 300;
    }
}
