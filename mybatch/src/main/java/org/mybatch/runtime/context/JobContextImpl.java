/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mybatch.runtime.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.batch.api.JobListener;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.context.JobContext;

import org.mybatch.creation.ArtifactFactory;
import org.mybatch.job.Job;
import org.mybatch.job.Listener;
import org.mybatch.job.Listeners;
import org.mybatch.metadata.ApplicationMetaData;
import org.mybatch.util.BatchLogger;
import org.mybatch.util.BatchUtil;
import org.mybatch.util.PropertyResolver;

public class JobContextImpl<T> extends BatchContextImpl<T> implements JobContext<T> {
    private Job job;
    private long instanceId;
    private long executionId;

    private Properties jobParameters;

    private ApplicationMetaData applicationMetaData;
    private ArtifactFactory artifactFactory;

    private JobListener[] jobListeners;

    public JobContextImpl(Job job, long instanceId, long executionId, Properties jobParameters, ApplicationMetaData applicationMetaData, ArtifactFactory artifactFactory) {
        super(job.getId());
        this.job = job;
        this.instanceId = instanceId;
        this.executionId = executionId;
        this.jobParameters = jobParameters;
        this.applicationMetaData = applicationMetaData;
        this.classLoader = applicationMetaData.getClassLoader();
        this.artifactFactory = artifactFactory;

        resolveProperties();
        initJobListeners();
        setBatchStatus(JobOperator.BatchStatus.STARTING);
    }

    public ArtifactFactory getArtifactFactory() {
        return artifactFactory;
    }

    public JobListener[] getJobListeners() {
        return this.jobListeners;
    }

    public ApplicationMetaData getApplicationMetaData() {
        return applicationMetaData;
    }

    public Properties getJobParameters() {
        return jobParameters;
    }

    @Override
    public long getInstanceId() {
        return instanceId;
    }

    @Override
    public long getExecutionId() {
        return executionId;
    }

    @Override
    public Properties getProperties() {
        return BatchUtil.toJavaUtilProperties(job.getProperties());
    }

    public Job getJob() {
        return job;
    }

    /**
     * Creates a batch artifact by delegating to the proper ArtifactFactory and passing along data needed for artifact
     * loading and creation.
     * @param ref ref name of the artifact
     * @param props batch properties directly for the artifact to create (does not include any properties from upper enclosing levels)
     * @param stepContext optional StepContext, needed for step-level artifact, but not for job-level ones
     * @return the created artifact
     */
    public <A> A createArtifact(String ref, org.mybatch.job.Properties props, StepContextImpl... stepContext) {
        Map<ArtifactFactory.DataKey, Object> artifactCreationData = new HashMap<ArtifactFactory.DataKey, Object>();
        artifactCreationData.put(ArtifactFactory.DataKey.APPLICATION_META_DATA, applicationMetaData);
        artifactCreationData.put(ArtifactFactory.DataKey.JOB_CONTEXT, this);
        if (props != null) {
            artifactCreationData.put(ArtifactFactory.DataKey.BATCH_PROPERTY, props);
        }
        if(stepContext.length > 0) {
            artifactCreationData.put(ArtifactFactory.DataKey.STEP_CONTEXT, stepContext[0]);
        }
        A a = null;
        try {
            a = (A) artifactFactory.create(ref, classLoader, artifactCreationData);
        } catch (Exception e) {
            BatchLogger.LOGGER.failToCreateArtifact(e, ref);
        }
        return a;
    }

    private void initJobListeners() {
        Listeners listeners = job.getListeners();
        if (listeners != null) {
            List<Listener> listenerList = listeners.getListener();
            int count = listenerList.size();
            this.jobListeners = new JobListener[count];
            for (int i = 0; i < count; i++) {
                Listener listener = listenerList.get(i);
                this.jobListeners[i] = createArtifact(listener.getRef(), listener.getProperties());
            }
        }
    }

    private void resolveProperties() {
        PropertyResolver resolver = new PropertyResolver();
        resolver.setJobParameters(this.jobParameters);
        org.mybatch.job.Properties props = job.getProperties();
        if (props != null) {
            resolver.pushJobProperties(props);
        }
        resolver.resolve(job);
    }
}