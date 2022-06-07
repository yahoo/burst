/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client;

import org.burstsys.client.client.exception.BurstRequestException;
import org.burstsys.client.client.model.BDomain;
import org.burstsys.client.client.model.BParameter;
import org.burstsys.client.client.model.BView;
import org.burstsys.client.client.model.results.BResult;
import org.burstsys.gen.thrift.api.client.BTBurstService;
import org.burstsys.gen.thrift.api.client.BTRequestOutcome;
import org.burstsys.gen.thrift.api.client.BTResultStatus;
import org.burstsys.gen.thrift.api.client.domain.BTDomainResponse;
import org.burstsys.gen.thrift.api.client.query.BTQueryResponse;
import org.burstsys.gen.thrift.api.client.view.BTViewResponse;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BurstSyncClient {
    private final String endpoint;
    private final BTBurstService.Client thriftClient;

    private BurstSyncClient(String thriftEndpoint) throws TTransportException {
        THttpClient transport = new THttpClient(thriftEndpoint);
        thriftClient = new BTBurstService.Client(new TBinaryProtocol(transport));
        endpoint = thriftEndpoint;
    }

    public static BurstSyncClient httpClient(String host) throws TTransportException {
        return BurstSyncClient.httpClient(host, 4080);
    }

    public static BurstSyncClient httpClient(String host, int port) throws TTransportException {
        return BurstSyncClient.httpClient(host, port, "/thrift");
    }

    public static BurstSyncClient httpClient(String host, int port, String path) throws TTransportException {
        String thriftEndpoint = String.format("http://%s:%d%s/client", host, port, path);
        return new BurstSyncClient(thriftEndpoint);
    }

    public static BurstSyncClient httpsClient(String host) throws TTransportException {
        return BurstSyncClient.httpsClient(host, 4443);
    }

    public static BurstSyncClient httpsClient(String host, int port) throws TTransportException {
        return BurstSyncClient.httpsClient(host, port, "/thrift");
    }

    public static BurstSyncClient httpsClient(String host, int port, String path) throws TTransportException {
        String thriftEndpoint = String.format("https://%s:%d%s/client", host, port, path);
        return new BurstSyncClient(thriftEndpoint);
    }

    /**
     * Ensure domain takes a set of domain requirements and ensures that the provided domain exists,
     * creating a new domain if needed. Domain existance is determined by matching on the UDK field.
     *
     * @param spec the characteristics that the domain should have. It must include a UDK
     * @return the domain as it exists in the catalog
     */
    public BDomain ensureDomain(BDomain spec) throws BurstRequestException {
        try {
            BTDomainResponse response = thriftClient.ensureDomain(BDomain.asThrift(spec));
            handleOutcome(response.outcome);
            return BDomain.fromThrift(response.domain);
        } catch (TException e) {
            throw new BurstRequestException(errorMessage("ensureDomain"), e);
        }
    }

    public Optional<BDomain> findDomain(String domainUdk) throws BurstRequestException {
        try {
            BTDomainResponse response = thriftClient.findDomain(domainUdk);
            if (response.outcome.status == BTResultStatus.NotFound) {
                return Optional.empty();
            }
            handleOutcome(response.outcome);
            return Optional.of(response.domain).map(BDomain::fromThrift);
        } catch (TException e) {
            throw new BurstRequestException(errorMessage("findDomain"), e);
        }
    }

    public BView ensureDomainContainsView(String domainUdk, BView spec) throws BurstRequestException {
        try {
            BTViewResponse response = thriftClient.ensureDomainContainsView(domainUdk, BView.asThrift(spec));
            handleOutcome(response.outcome);
            return BView.fromThrift(response.view);
        } catch (TException e) {
            throw new BurstRequestException(errorMessage("ensureDomainContainsView"), e);
        }
    }

    public List<BView> listViewsInDomain(String domainUdk) throws BurstRequestException {
        try {
            BTViewResponse response = thriftClient.listViewsInDomain(domainUdk);
            handleOutcome(response.outcome);
            return response.views.stream().map(BView::fromThrift).collect(Collectors.toList());
        } catch (TException e) {
            throw new BurstRequestException(errorMessage("listViewsInDomain"), e);
        }
    }

    public BResult executeQuery(
            String queryUid,
            String domainUdk,
            String viewUdk,
            String querySource,
            String timezone,
            List<BParameter> parameters
    ) throws BurstRequestException {
        try {
            if (parameters == null) {
                parameters = new ArrayList<>();
            }
            BTQueryResponse response = thriftClient.executeQuery(queryUid, domainUdk, viewUdk, querySource, timezone,
                    parameters.stream().map(BParameter::toThrift).collect(Collectors.toList()));
            handleOutcome(response.outcome);
            return new BResult(response.result);
        } catch (TException e) {
            throw new BurstRequestException(errorMessage("executeQuery"), e);
        }
    }
    private String errorMessage(String methodName) {
        return methodName + " request to " + endpoint + " encountered a thrift exception";
    }

    private void handleOutcome(BTRequestOutcome outcome) throws BurstRequestException {
        switch (outcome.status) {
            case TimeoutStatus:
                throw new BurstRequestException("Received a time out response from Burst.");
            case UnknownStatus:
            case ExceptionStatus:
                throw new BurstRequestException("A remote exception occurred while processing the request: " + outcome.message);
            case InvalidStatus:
                throw new BurstRequestException("The request was malformed: " + outcome.message);
            case NotReadyStatus:
                throw new BurstRequestException("The upstream server is too busy.");
            case StoreErrorStatus:
                throw new BurstRequestException("An error occurred while loading the dataset: " + outcome.message);
            case NotFound:
                throw new BurstRequestException("The domain or view could not be found: " + outcome.message);
            case Conflict:
                throw new BurstRequestException("An error occurred while manipulating the domain or view: " + outcome.message);

            default:
                // do nothing
        }
    }

    public static <A, B> List<B> mappedList(List<A> source, Function<A, B> mapper) {
        return Collections.unmodifiableList(
               source.stream().map(mapper).collect(Collectors.toList())
        );
    }
}
