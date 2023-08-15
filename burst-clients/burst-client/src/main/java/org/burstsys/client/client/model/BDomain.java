/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model;

import org.burstsys.client.client.BurstSyncClient;
import org.burstsys.gen.thrift.api.client.domain.BTDomain;
import org.burstsys.gen.thrift.api.client.view.BTView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BDomain {
    public static class Builder {
        private final String udk;
        private String moniker;
        private Map<String, String> domainProperties = new HashMap<>();
        private Map<String, String> labels = new HashMap<>();
        private List<BView> views = new ArrayList<>();

        private Builder(String udk) {
            this.udk = udk;
        }

        public Builder withMoniker(String moniker) {
            this.moniker = moniker;
            return this;
        }

        public Builder withDomainProperties(Map<String, String> domainProperties) {
            this.domainProperties = domainProperties;
            return this;
        }

        public Builder withLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder withViews(List<BView> views) {
            this.views = views;
            return this;
        }

        public BDomain build() {
            return new BDomain(this);
        }
    }

    public static Builder withUdk(String udk) {
        return new Builder(udk);
    }

    public static BDomain fromThrift(BTDomain fromThrift) {
        return new BDomain(fromThrift);
    }

    public static BTDomain asThrift(BDomain domain) {
        BTDomain tStruct = new BTDomain();
        tStruct.setMoniker(domain.moniker);
        tStruct.setDomainProperties(domain.domainProperties);
        tStruct.setUdk(domain.udk);
        tStruct.setLabels(domain.labels);
        tStruct.setViews(BurstSyncClient.mappedList(domain.views, BView::asThrift));
        return tStruct;
    }

    private final Long pk;
    private String moniker;
    private Map<String, String> domainProperties;
    private final String udk;
    private Map<String, String> labels;
    private final Instant createTimestamp;
    private final Instant modifyTimestamp;
    private List<BView> views;

    private BDomain(Builder builder) {
        pk = null;
        moniker = builder.moniker;
        domainProperties = new HashMap<>(builder.domainProperties);
        udk = builder.udk;
        labels = new HashMap<>(builder.labels);
        createTimestamp = null;
        modifyTimestamp = null;
        if (builder.views == null) {
            views = null;
        } else {
            views = builder.views.stream().map(BView::copy).collect(Collectors.toList());
        }
    }

    private BDomain(BTDomain fromThrift) {
        pk = fromThrift.pk;
        moniker = fromThrift.moniker;
        domainProperties = fromThrift.domainProperties;
        udk = fromThrift.udk;
        labels = new HashMap<>(fromThrift.labels);
        createTimestamp = Instant.ofEpochMilli(fromThrift.createTimestamp);
        modifyTimestamp = Instant.ofEpochMilli(fromThrift.modifyTimestamp);
        views = (fromThrift.views == null ? new ArrayList<BTView>() : fromThrift.views).stream().map(BView::fromThrift).collect(Collectors.toList());
    }

    /**
     * @return the pk for a domain fetched from Burst
     */
    public Long getPk() {
        return pk;
    }

    /**
     * @return the user-friendly name of this domain
     */
    public String getMoniker() {
        return moniker;
    }

    public BDomain setMoniker(String moniker) {
        this.moniker = moniker;
        return this;
    }

    /**
     * @return the set of properties for this domain
     */
    public Map<String, String> getDomainProperties() {
        return new HashMap<>(domainProperties);
    }

    public BDomain setDomainProperties(Map<String, String> properties) {
        domainProperties.clear();
        domainProperties.putAll(properties);
        return this;
    }

    /**
     * @return the udk for this domain
     */
    public String getUdk() {
        return udk;
    }

    /**
     * @return the set of labels for this domain
     */
    public Map<String, String> getLabels() {
        return new HashMap<>(labels);
    }

    public BDomain setLabels(Map<String, String> labels) {
        this.labels.clear();
        this.labels.putAll(labels);
        return this;
    }

    /**
     * @return when this domain was created, if this domain was fetched from Burst, otherwise null
     */
    public Instant getCreateTimestamp() {
        return createTimestamp;
    }

    /**
     * @return when this domain was modified, if this domain was fetched from Burst, otherwise null
     */
    public Instant getModifyTimestamp() {
        return modifyTimestamp;
    }

    /**
     * @return the list of views for this domain
     */
    public List<BView> getViews() {
        return new ArrayList<>(views);
    }

    public BDomain setViews(List<BView> views) {
        this.views = views;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BDomain{");
        sb.append("udk='").append(udk).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BDomain domain = (BDomain) o;
        return udk.equals(domain.udk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(udk);
    }
}
