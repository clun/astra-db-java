package com.datastax.astra.internal.command;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.internal.http.RetryHttpClient;
import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.api.ApiResponseHttp;
import com.datastax.astra.internal.utils.JsonUtils;
import com.datastax.astra.client.model.CommandRunner;
import com.datastax.astra.client.exception.DataApiResponseException;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.internal.utils.CompletableFutures;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Execute the command and parse results throwing DataApiResponseException when needed.
 */
@Slf4j
public abstract class AbstractCommandRunner implements CommandRunner {

    /** Static Http Client for the Client. */
    protected static RetryHttpClient httpClient;

    /** Could be useful to capture the interactions at client side. */
    protected Map<String, CommandObserver> observers = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     */
    protected AbstractCommandRunner() {
    }

    /** {@inheritDoc} */
    @Override
    public void registerListener(String name, CommandObserver observer) {
        observers.put(name, observer);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteListener(String name) {
        observers.remove(name);
    }

    /**
     * Access to the HttpClient.
     *
     * @return
     *      instance of http client support of retries.
     */
    protected synchronized RetryHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new RetryHttpClient(getHttpClientOptions());
        }
        return httpClient;
    }

    /** {@inheritDoc} */
    @Override
    public ApiResponse runCommand(Command command) {

        // Initializing the Execution infos (could be pushed to 3rd parties)
        ExecutionInfos.DataApiExecutionInfoBuilder executionInfo =
                ExecutionInfos.builder().withCommand(command);

        try {
            // (Custom) Serialization
            String jsonCommand = JsonUtils.marshall(command);
            ApiResponseHttp httpRes = getHttpClient().post(getApiEndpoint(), getToken(), jsonCommand);
            executionInfo.withHttpResponse(httpRes);
            ApiResponse jsonRes = JsonUtils.unMarshallBean(httpRes.getBody(), ApiResponse.class);
            executionInfo.withApiResponse(jsonRes);
            // Encapsulate Errors
            if (jsonRes.getErrors() != null) {
                throw new DataApiResponseException(Collections.singletonList(executionInfo.build()));
            }
            return jsonRes;
        } finally {
            // Notify the observers
            CompletableFuture.runAsync(()-> notifyASync(l -> l.onCommand(executionInfo.build())));
        }
    }

    /**
     * Asynchronously send calls to listener for tracing.
     *
     * @param lambda operations to execute
     */
    private void notifyASync(Consumer<CommandObserver> lambda) {
        CompletableFutures.allDone(observers.values().stream()
                .map(l -> CompletableFuture.runAsync(() -> lambda.accept(l)))
                .collect(Collectors.toList()));
    }

    /** {@inheritDoc} */
    @Override
    public <T> T runCommand(Command command, Class<T> documentClass) {
        return mapAsDocument(runCommand(command), documentClass);
    }

    /**
     * Document Mapping.
     *
     * @param api
     *      api response
     * @param documentClass
     *      document class
     * @return
     *      document
     * @param <T>
     *     document type
     */
    protected <T> T mapAsDocument(ApiResponse api, Class<T> documentClass) {
        String payload;
        if (api.getData() != null) {
            if (api.getData().getDocument() != null) {
                payload = JsonUtils.marshall(api.getData().getDocument());
            } else if (api.getData().getDocuments() != null) {
                payload = JsonUtils.marshall(api.getData().getDocuments());
            } else {
                throw new IllegalStateException("Cannot marshall into '" + documentClass + "' no documents returned.");
            }
        } else {
            payload = JsonUtils.marshall(api.getStatus());
        }
        return JsonUtils.unMarshallBean(payload, documentClass);
    }

    /**
     * The subclass should provide the endpoint, url to post request.
     *
     * @return
     *      url on which to post the request
     */
    protected abstract String getApiEndpoint();

    /**
     * Authentication token provided by subclass.
     *
     * @return
     *      authentication token
     */
    protected abstract String getToken();

    /**
     * Options to initialize the HTTP client.
     *
     * @return
     *      options for the http client.
     */
    protected abstract DataAPIOptions.HttpClientOptions getHttpClientOptions();

}
