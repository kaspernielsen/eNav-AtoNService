/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.atonService.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.grad.eNav.atonService.models.domain.s125.S125Dataset;
import org.grad.eNav.atonService.services.DatasetContentLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * The Dataset Aspect Component Class.
 * <p/>
 * This class implements the dataset aspect operations.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
@Aspect
@Component
public class DatasetAspect {

    /**
     * The Dataset Content Service.
     */
    @Autowired
    DatasetContentLogService datasetContentLogService;

    /**
     * The logging dataset operation. This operation will use the output of the
     * logged function to update the database with the resulting dataset (the
     * one named as proceed).
     *
     * @param joinPoint The Joint Point to be used
     * @return the result object of the joint point
     * @throws Throwable for any errors that occurred during the operation
     */
    @Around("@annotation(LogDataset)")
    public Object logDataset(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the annotation parameters
        String operation = Optional.of(joinPoint)
                .map(ProceedingJoinPoint::getSignature)
                .map(MethodSignature.class::cast)
                .map(MethodSignature::getMethod)
                .map(m -> m.getAnnotation(LogDataset.class))
                .map(LogDataset::operation)
                .orElse("");

        // Process and get the result object
        Object proceed = joinPoint.proceed();

        // Handle if the object is an S-125 Dataset
        if(Optional.ofNullable(proceed).filter(S125Dataset.class::isInstance).isPresent()) {
            Optional.of(proceed)
                    .map(S125Dataset.class::cast)
                    .map(d -> this.datasetContentLogService.generateDatasetContentLog(d, operation))
                    .ifPresent(this.datasetContentLogService::save);
        }
        // Handle if the object is an S-125 Dataset collection
        else if(Optional.ofNullable(proceed).filter(p -> isObjectCollectionOfClass(p, S125Dataset.class)).isPresent()) {
            ((Collection<?>) proceed).stream()
                    .map(S125Dataset.class::cast)
                    .map(d -> this.datasetContentLogService.generateDatasetContentLog(d, operation))
                    .forEach(this.datasetContentLogService::save);
        }

        // And return the result object
        return proceed;
    }

    /**
     * A helper function that can determine whether a generic object is of
     * type collection and includes a specific object class.
     *
     * @param object    The object to be checked
     * @param clazz     The class that the collection should contain
     * @return whether the object is a collection of a specific class
     */
    protected boolean isObjectCollectionOfClass(Object object, Class clazz) {
        if (object instanceof Collection<?>) {
            return ((Collection<?>) object).stream().allMatch(clazz::isInstance);
        }
        return false;
    }

}
