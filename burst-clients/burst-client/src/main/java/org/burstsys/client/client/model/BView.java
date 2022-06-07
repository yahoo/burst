/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model;

import org.burstsys.gen.thrift.api.client.view.BTView;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class BView {
    public static class Builder {
        private final String viewUdk;
        private final String domainUdk;
        private String moniker = "";
        private Long generationClock = 0L;
        private Map<String, String> storeProperties = new HashMap<>();
        private String viewMotif = "";
        private Map<String, String> viewProperties = new HashMap<>();
        private Map<String, String> labels = new HashMap<>();
        private String schemaName = "";

        public Builder(String domainUdk, String viewUdk) {
            this.viewUdk = viewUdk;
            this.domainUdk = domainUdk;
        }

        public Builder withMoniker(String moniker) {
            this.moniker = moniker;
            return this;
        }

        public Builder withGenerationClock(Long generationClock) {
            this.generationClock = generationClock;
            return this;
        }

        public Builder withStoreProperties(Map<String, String> storeProperties) {
            this.storeProperties = storeProperties;
            return this;
        }

        public Builder withViewMotif(String viewMotif) {
            this.viewMotif = viewMotif;
            return this;
        }

        public Builder withViewProperties(Map<String, String> viewProperties) {
            this.viewProperties = viewProperties;
            return this;
        }

        public Builder withLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder withSchemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public BView build() {
            return new BView(this);
        }
    }

    public static Builder withUdk(String domainUdk, String viewUdk) {
        return new Builder(domainUdk, viewUdk);
    }

    public static BView fromThrift(BTView fromThrift) {
        return new BView(fromThrift);
    }

    public static BTView asThrift(BView view) {
        BTView tStruct = new BTView();
        tStruct.setUdk(view.udk);
        tStruct.setUdk(view.udk);
        tStruct.setMoniker(view.moniker);
        tStruct.setDomainUdk(view.domainUdk);
        tStruct.setStoreProperties(view.storeProperties);
        tStruct.setViewMotif(view.viewMotif);
        tStruct.setViewProperties(view.viewProperties);
        tStruct.setLabels(view.labels);
        tStruct.setSchemaName(view.schemaName);
        return tStruct;
    }

    private Long pk;
    private final String udk;
    private final String domainUdk;
    private String moniker;
    private Long generationClock;
    private Map<String, String> storeProperties;
    private String viewMotif;
    private Map<String, String> viewProperties;
    private Map<String, String> labels;
    private String schemaName;
    private final Instant createTimestamp;
    private final Instant modifyTimestamp;
    private final Instant accessTimestamp;

    private BView(Builder builder) {
        udk = builder.viewUdk;
        domainUdk = builder.domainUdk;
        moniker = builder.moniker;
        generationClock = builder.generationClock;
        storeProperties = builder.storeProperties;
        viewMotif = builder.viewMotif;
        viewProperties = builder.viewProperties;
        labels = builder.labels;
        schemaName = builder.schemaName;
        createTimestamp = null;
        modifyTimestamp = null;
        accessTimestamp = null;
    }

    private BView(BView other) {
        pk = other.pk;
        udk = other.udk;
        moniker = other.moniker;
        domainUdk = other.domainUdk;
        generationClock = other.generationClock;
        storeProperties = new HashMap<>(other.storeProperties);
        viewMotif = other.viewMotif;
        viewProperties = new HashMap<>(other.viewProperties);
        labels = new HashMap<>(other.labels);
        schemaName = other.schemaName;
        createTimestamp = other.createTimestamp;
        modifyTimestamp = other.modifyTimestamp;
        accessTimestamp = other.accessTimestamp;
    }

    private BView(BTView fromThrift) {
        pk = fromThrift.pk;
        udk = fromThrift.udk;
        moniker = fromThrift.moniker;
        domainUdk = fromThrift.domainUdk;
        generationClock = fromThrift.generationClock;
        storeProperties = new HashMap<>(fromThrift.storeProperties);
        viewMotif = fromThrift.viewMotif;
        viewProperties = new HashMap<>(fromThrift.viewProperties);
        labels = new HashMap<>(fromThrift.labels);
        schemaName = fromThrift.schemaName;
        createTimestamp = Instant.ofEpochMilli(fromThrift.createTimestamp);
        modifyTimestamp = Instant.ofEpochMilli(fromThrift.modifyTimestamp);
        accessTimestamp = Instant.ofEpochMilli(fromThrift.accessTimestamp);
    }

    public BView copy() {
        return new BView(this);
    }

    public Long getPk() {
        return pk;
    }

    public String getUdk() {
        return udk;
    }

    public String getDomainUdk() {
        return domainUdk;
    }

    public String getMoniker() {
        return moniker;
    }

    public BView setMoniker(String moniker) {
        this.moniker = moniker;
        return this;
    }

    public Long getGenerationClock() {
        return generationClock;
    }

    public BView setGenerationClock(Long generationClock) {
        this.generationClock = generationClock;
        return this;
    }

    public Map<String, String> getStoreProperties() {
        return new HashMap<>(storeProperties);
    }

    public BView setStoreProperties(Map<String, String> storeProperties) {
        this.storeProperties.clear();
        this.storeProperties.putAll(storeProperties);
        return this;
    }

    public String getViewMotif() {
        return viewMotif;
    }

    public BView setViewMotif(String viewMotif) {
        this.viewMotif = viewMotif;
        return this;
    }

    public Map<String, String> getViewProperties() {
        return new HashMap<>(viewProperties);
    }

    public BView setViewProperties(Map<String, String> viewProperties) {
        this.viewProperties.clear();
        this.viewProperties.putAll(viewProperties);
        return this;
    }

    public Map<String, String> getLabels() {
        return new HashMap<>(labels);
    }

    public BView setLabels(Map<String, String> labels) {
        this.labels.clear();
        this.labels.putAll(labels);
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public BView setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public Instant getCreateTimestamp() {
        return createTimestamp;
    }

    public Instant getModifyTimestamp() {
        return modifyTimestamp;
    }

    public Instant getAccessTimestamp() {
        return accessTimestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BView{");
        sb.append("udk='").append(udk).append('\'');
        sb.append(", domainUdk='").append(domainUdk).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
