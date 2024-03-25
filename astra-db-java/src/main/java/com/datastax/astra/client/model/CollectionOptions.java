package com.datastax.astra.client.model;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Set of options to define and initialize a collection.
 */
@Data
public class CollectionOptions {

    /**
     * The 'defaultId' to allow working with different types of identifiers.
     */
    private Map<String, CollectionIdTypes> defaultId;

    /**
     * Vector options.
     */
    private VectorOptions vector;

    /**
     * Indexing options
     */
    private IndexingOptions indexing;

    /**
     * Default constructor.
     */
    public CollectionOptions() {}

    /**
     * Subclass representing the indexing options.
     */
    @Data
    public static class IndexingOptions {

        /**
         * If not empty will index everything but those properties.
         */
        private List<String> deny;

        /**
         * If not empty will index just those properties.
         */
        private List<String> allow;

        /**
         * Default constructor.
         */
        public IndexingOptions() {}
    }

    /**
     * Subclass representing the vector options.
     */
    @Data
    static public class VectorOptions {

        /**
         * Size of the vector.
         */
        private int dimension;

        /**
         * Similarity metric.
         */
        private SimilarityMetric metric;

        /**
         * Service for vectorization
         */
        private Service service;

        /** Default constructor. */
        public VectorOptions() {}
    }

    /**
     * Subclass representing the services options.
     */
    @Data
    public static class Service {

        /** LLM provider. */
        private String provider;

        /** LLM Model name. */
        private String modelName;

        /** Authentication information like keys and secrets. */
        private Authentication authentication;

        /** Free form parameters. */
        private Map<String, Parameters> parameters;

        /** Default constructor. */
        public Service() {}
    }


    /**
     * Subclass representing the Authentication options.
     */
    @Data
    public static class Authentication {

        /** Type of authentication: Oauth, API Key, etc. */
        private List<String> type;

        /** Name of the secret if sstored in Astra. */
        private String secretName;

        /** Default constructor. */
        public Authentication() {}
    }

    /**
     * Subclass representing a parameters for LLM Services
     */
    @Data
    public static class Parameters {

        /** Type for the parameters. */
        private String type;

        /** declare if mandatory or not. */
        private boolean required;

        /** the default value for the parameter. */
        @JsonProperty("default")
        private Object defaultValue;

        /** description of the parameter. */
        private String help;

        /** Default constructor. */
        public Parameters() {}
    }

    /**
     * Gets a builder.
     *
     * @return a builder
     */
    public static CreateCollectionOptionsBuilder builder() {
        return new CreateCollectionOptionsBuilder();
    }

    /**
     * Builder for {@link CollectionDefinition}.
     */
    public static class CreateCollectionOptionsBuilder {

        /**
         * Options for Vector
         */
        VectorOptions vector;

        /**
         * Options for Indexing
         */
        IndexingOptions indexing;

        /**
         * Options for Default Id
         */
        CollectionIdTypes defaultId;

        /**
         * Access the vector options.
         *
         * @return
         *      vector options
         */
        private VectorOptions getVector() {
            if (vector == null) {
                vector = new VectorOptions();
            }
            return vector;
        }

        /**
         * Access the indexing options.
         *
         * @return
         *      indexing options
         */
        private IndexingOptions getIndexing() {
            if (indexing == null) {
                indexing = new IndexingOptions();
            }
            return indexing;
        }

        /**
         * Default constructor.
         */
        public CreateCollectionOptionsBuilder() {}

        /**
         * Builder Pattern with the Identifiers.
         *
         * @param idType
         *      type of ids
         * @return
         *      self reference
         */
        public CreateCollectionOptionsBuilder withDefaultId(CollectionIdTypes idType) {
            this.defaultId = idType;
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param size size
         * @return self reference
         */
        public CreateCollectionOptionsBuilder withVectorDimension(int size) {
            getVector().setDimension(size);
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param function function
         * @return self reference
         */
        public CreateCollectionOptionsBuilder withVectorSimilarityMetric(@NonNull SimilarityMetric function) {
            getVector().setMetric(function);
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param properties size
         * @return self reference
         */
        public CreateCollectionOptionsBuilder withIndexingDeny(@NonNull String... properties) {
            if (getIndexing().getAllow() != null) {
                throw new IllegalStateException("'indexing.deny' and 'indexing.allow' are mutually exclusive");
            }
            getIndexing().setDeny(Arrays.asList(properties));
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param properties size
         * @return self reference
         */
        public CreateCollectionOptionsBuilder withIndexingAllow(String... properties) {
            if (getIndexing().getDeny() != null) {
                throw new IllegalStateException("'indexing.deny' and 'indexing.allow' are mutually exclusive");
            }
            getIndexing().setAllow(Arrays.asList(properties));
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param dimension dimension
         * @param function  function
         * @return self reference
         */
        public CreateCollectionOptionsBuilder vector(int dimension, @NonNull SimilarityMetric function) {
            withVectorSimilarityMetric(function);
            withVectorDimension(dimension);
            return this;
        }

        /**
         * Build the output.
         *
         * @return collection definition
         */
        public CollectionOptions build() {
            CollectionOptions req = new CollectionOptions();
            req.vector    = this.vector;
            req.indexing  = this.indexing;
            if (defaultId != null) {
                req.defaultId = Map.of("type", this.defaultId);
            }
            return req;
        }
    }

}