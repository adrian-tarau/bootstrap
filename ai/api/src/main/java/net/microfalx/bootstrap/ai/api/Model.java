package net.microfalx.bootstrap.ai.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Represents an AI model.
 * <p>
 * The key parameters are:
 * <ul>
 * <li>Temperature: Controls randomness, higher values increase diversity.</li>
 * <li>Top-p (nucleus): The cumulative probability cutoff for token selection. Lower values mean sampling from a smaller, more top-weighted nucleus.</li>
 * <li>Top-k: Sample from the k most likely next tokens at each step. Lower k focuses on higher probability tokens.</li>
 * </ul>
 * <p>
 * <p>
 * In general:
 * <ul>
 * <li>Higher temperature will make outputs more random and diverse.</li>
 * <li>Lower top-p values reduce diversity and focus on more probable tokens.</li>
 * <li>Lower top-k also concentrates sampling on the highest probability tokens for each step.</li>
 * </ul>
 * <p>
 * So temperature increases variety, while top-p and top-k reduce variety and focus samples on the model’s top predictions.
 * You have to balance diversity and relevance when tuning these parameters for different applications.
 */
@ToString(callSuper = true)
public class Model extends NamedAndTaggedIdentifyAware<String> {

    public static final String DEFAULT_TAG = "default";
    public static final String TOOLS_TAG = "tools";
    public static final String THINKING_TAG = "thinking";

    private URI uri;
    private String apyKey;
    private URI downloadUri;
    private boolean enabled;
    private boolean _default;
    private boolean embedding;
    private Long size;
    private Quantization quantization;
    private Long numberOfParameters;

    private String modelName;
    private Double temperature;
    private Double topP;
    private Integer topK;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private boolean thinking;
    private int maximumContextLength;
    private Integer maximumOutputTokens;
    private Set<String> stopSequences;
    private ResponseFormat responseFormat = ResponseFormat.TEXT;

    @ToString.Exclude
    Provider provider;

    public static Builder create(String name, String modelName) {
        requireNotEmpty(name);
        requireNotEmpty(modelName);
        return (Builder) new Builder(toIdentifier(modelName)).modelName(modelName).name(name);
    }

    /**
     * Returns whether the model is enabled or not.
     *
     * @return {@code true} if the model is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Return true if the model is the default model for inference, otherwise false
     *
     * @return a non-null instance
     */
    public boolean isDefault() {
        return _default;
    }

    /**
     * Returns whether the model is an embedding model.
     *
     * @return {@code true} if the model is an embedding model, {@code false} otherwise
     */
    public boolean isEmbedding() {
        return embedding;
    }

    /**
     * Returns the weight of the model, in bytes. This is used to estimate the cost of using the model and to decide
     * which model to use when multiple models are available.
     *
     * @return the size of the model in bytes, null if not available
     */
    public Long getSize() {
        return size;
    }

    /**
     * Returns the number of parameters of the model.
     *
     * @return the number of parameters of the model, null if not available
     */
    public Long getNumberOfParameters() {
        return numberOfParameters;
    }

    /**
     * Returns the quantization level of the model, in bits. This is used to estimate the cost of using
     * the model and to decide which model to use when multiple models are available.
     *
     * @return a non-null integer representing the quantization level in bits, null if not available
     */
    public Quantization getQuantization() {
        return quantization;
    }

    /**
     * Returns the provider.
     *
     * @return a non-null
     */
    public Provider getProvider() {
        if (provider == null) throw new IllegalStateException("The model has no provider");
        return provider;
    }

    /**
     * Returns the URI of the model.
     *
     * @return the URI if the model is remote, null if the model is accessed over an API
     */
    public URI getUri() {
        return getUri(false);
    }

    /**
     * Returns the URI of the model.
     *
     * @param raw {@code true} to return the URI from model, {@code false} to return the URI from model, if available, or provider
     * @return the URI if the model is remote, null if the model is accessed over an API
     */
    public URI getUri(boolean raw) {
        if (uri != null || raw) {
            return uri;
        } else {
            return getProvider().getUri();
        }
    }

    /**
     * Returns the API key to use when accessing the model.
     *
     * @return the apy key, null if the model is accessed over an API
     * @see Model#getUri
     */
    public String getApyKey() {
        return getApyKey(false);
    }

    /**
     * Returns the URI to download the model, if available.
     *
     * @return the URI, null if not available
     */
    public URI getDownloadUri() {
        return downloadUri;
    }

    /**
     * Returns the API key to use when accessing the model.
     *
     * @param raw {@code true} to return the key from model, {@code false} to return the URI from model, if available, or provider
     * @return the apy key, null if the model is accessed over an API
     * @see Model#getUri
     */
    public String getApyKey(boolean raw) {
        if (apyKey != null || raw) {
            return apyKey;
        } else {
            return getProvider().getApyKey();
        }
    }

    /**
     * Returns a reference to the model name.
     * <p>
     * Some models have a reference to the model name, and they know how to get the model from the internet.
     *
     * @return the model name, null if not set
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Returns the sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output
     * more random, while lower values like 0.2 will make it more focused and deterministic.
     *
     * @return a value between 0 and 2, null to use the default value
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * Returns an alternative to sampling with temperature, called nucleus sampling, where the model considers
     * the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising
     * the top 10% probability mass are considered.
     * <p>
     * We generally recommend altering this or temperature but not both.
     *
     * @return a value between 0 and 1, null to use the default value (1)
     */
    public Double getTopP() {
        return topP;
    }

    /**
     * Returns the number of most likely tokens to keep for top-k sampling.
     * <p>
     * The parameter limits the model’s output to the top-k most probable tokens at each step. This can help reduce
     * incoherent or nonsensical output by restricting the model’s vocabulary
     *
     * @return a positive integer, null to use the default value
     */
    public Integer getTopK() {
        return topK;
    }

    /**
     * Returns the frequency penalty to use, between -2 and 2. Positive values penalize new tokens based on
     * their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line
     * verbatim.
     *
     * @return a value between -2 and 2, null to use the default value
     */
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    /**
     * Returns the presence penalty to use, between -2 and 2. Positive values penalize new tokens based on
     * whether they appear in the text so far, increasing the model's likelihood to talk about new topics.
     *
     * @return a value between -2 and 2, null to use the default value
     */
    public Double getPresencePenalty() {
        return presencePenalty;
    }

    /**
     * Returns whether thinking is enabled for models which support thinking (tagged with 'thinking').
     * <p>
     * This is used to make the AI explain it's internal thinking process and it might be useful to be enabled,
     * at least during the development of the prompt.
     *
     * @return {@code true} if the model is thinking, {@code false} otherwise
     */
    public boolean isThinking() {
        return thinking && getTags().contains(THINKING_TAG);
    }

    /**
     * Returns the maximum context length in tokens that the model can handle.
     * <p>
     * This is the maximum number of tokens that can be processed in a single request, including both input and output.
     *
     * @return a positive integer, default is 64K tokens
     */
    public int getMaximumContextLength() {
        return maximumContextLength;
    }

    /**
     * Returns the maximum number of tokens that can be generated in the chat completion.
     *
     * @return a positive integer
     */
    public Integer getMaximumOutputTokens() {
        return maximumOutputTokens;
    }

    /**
     * Returns tokens to be used as a stop sequence. The API will stop generating further tokens after
     * it encounters the stop sequence.
     *
     * @return a non-null instance
     */
    public Set<String> getStopSequences() {
        return unmodifiableSet(stopSequences);
    }

    /**
     * Returns the format of the response from the model.
     *
     * @return a non-null instance
     */
    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    /**
     * Returns the memory footprint of the model, in bytes. This is used to estimate the cost of using the
     * model and to decide which model to use when multiple models are available.
     *
     * @return the estimated memory footprint of the model in bytes, -1 if not available
     */
    public long getMemoryFootprint() {
        long weight;
        if (size != null) {
            weight = size;
        } else if (numberOfParameters != null && quantization != null) {
            weight = (long) (numberOfParameters * quantization.bitCount / 8f);
        } else {
            return -1;
        }
        float ratio = 1;
        if (maximumContextLength < 64_000) {
            ratio = 1.1f;
        } else if (maximumContextLength < 128_000) {
            ratio = 1.2f;
        } else if (maximumContextLength < 256_000) {
            ratio = 1.3f;
        } else {
            ratio = 1.5f;
        }
        ;
        return (long) (weight * ratio);
    }

    /**
     * An enum for the quantization level of the model, in bits. This is used to estimate the cost of using
     */
    public enum Quantization {

        /**
         * 16-bit floating point quantization, which is the standard for most models. It provides a good balance
         * between performance and accuracy.
         */
        FP16(16),

        /**
         * 8-bit integer quantization, which can significantly reduce the model size and increase inference speed,
         * but might lead to a decrease in accuracy. It is suitable for models that are already small or for applications
         * where speed is more important than accuracy.
         */
        Q8(8),

        /**
         * 5-bit integer quantization, even less precisions compared with {@link #Q8}
         */
        Q6(6),

        /**
         * 5-bit integer quantization, even less precisions compared with {@link #Q6}
         */
        Q5(5),

        /**
         * 4-bit integer quantization, even less precisions compared with {@link #Q5}
         */
        Q4(4);

        private final int bitCount;

        Quantization(int bitCount) {
            this.bitCount = bitCount;
        }
    }

    /**
     * An enum representing the strategy to use when pulling a model from the internet.
     */
    public enum PullStrategy {

        /**
         * Always pull the model, even if it's already available. Useful to ensure you're
         * using the latest version of that model.
         */
        ALWAYS,

        /**
         * Only pull the model if it's not already available. It might be an older version of
         * the model.
         */
        WHEN_MISSING,

        /**
         * Never pull the model.
         */
        NEVER
    }

    /**
     * A builder class.
     */
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private URI uri;
        private String apyKey;
        private URI downloadUri;
        private boolean enabled = true;
        private boolean _default;
        private Long size;
        private Quantization quantization = Quantization.FP16;
        private Long numberOfParameters;

        private String modelName;
        private Double temperature;
        private Double topP;
        private Integer topK;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private boolean thinking = true;
        private int maximumContextLength = 64 * 1028;
        private Integer maximumOutputTokens;
        private final Set<String> stopSequences = new HashSet<>();
        private ResponseFormat responseFormat = ResponseFormat.TEXT;

        private boolean embedding;

        private Provider provider;

        public Builder(String id) {
            super(id);
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder apyKey(String apyKey) {
            this.apyKey = apyKey;
            return this;
        }

        public Builder uri(URI uri, String apyKey) {
            this.uri = uri;
            this.apyKey = apyKey;
            return this;
        }

        public Builder downloadUri(String downloadUri) {
            if (isNotEmpty(downloadUri)) {
                return this.downloadUri(URI.create(downloadUri));
            } else {
                this.downloadUri = null;
                return this;
            }
        }

        public Builder downloadUri(URI downloadUri) {
            this.downloadUri = downloadUri;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder size(Long size) {
            this.size = size;
            return this;
        }

        public Builder numberOfParameters(Long numberOfParameters) {
            this.numberOfParameters = numberOfParameters;
            return this;
        }

        public Builder quantization(Quantization quantization) {
            this.quantization = quantization;
            return this;
        }

        public Builder thinking(boolean thinking) {
            this.thinking = thinking;
            return this;
        }

        public Builder asDisabled() {
            this.enabled = true;
            return this;
        }

        public Builder setDefault(boolean _default) {
            this._default = _default;
            return this;
        }

        public Builder asDefault() {
            this._default = true;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder maximumContextLength(int maximumContextLength) {
            this.maximumContextLength = maximumContextLength;
            return this;
        }

        public Builder maximumOutputTokens(Integer maximumOutputTokens) {
            this.maximumOutputTokens = maximumOutputTokens;
            return this;
        }

        public Builder addStopSequences(List<String> stopSequences) {
            requireNonNull(stopSequences);
            this.stopSequences.addAll(stopSequences);
            return this;
        }

        public Builder addStopSequences(String... stopSequences) {
            requireNonNull(stopSequences);
            this.stopSequences.addAll(Arrays.asList(stopSequences));
            return this;
        }

        public Builder responseFormat(ResponseFormat responseFormat) {
            requireNonNull(responseFormat);
            this.responseFormat = responseFormat;
            return this;
        }

        public Builder embedding(boolean embedding) {
            this.embedding = embedding;
            return this;
        }

        public Builder forEmbedding() {
            this.embedding = true;
            return this;
        }

        public Builder hasTools() {
            tag(TOOLS_TAG);
            return this;
        }

        public Builder canThink() {
            tag(THINKING_TAG);
            return this;
        }

        protected Builder provider(Provider provider) {
            this.provider = provider;
            id(provider.getId() + "." + id());
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Model();
        }

        @Override
        public Model build() {
            Model model = (Model) super.build();
            model.uri = uri;
            model.apyKey = apyKey;
            model.downloadUri = downloadUri;
            model.enabled = enabled;
            model._default = _default;
            model.embedding = embedding;
            model.thinking = thinking;
            model.modelName = modelName;
            model.temperature = temperature;
            model.topK = topK;
            model.topP = topP;
            model.frequencyPenalty = frequencyPenalty;
            model.presencePenalty = presencePenalty;
            model.maximumContextLength = maximumContextLength;
            model.maximumOutputTokens = maximumOutputTokens;
            model.stopSequences = stopSequences;
            model.responseFormat = responseFormat;
            model.size = size;
            model.numberOfParameters = numberOfParameters;
            model.quantization = quantization;
            model.provider = provider;

            return model;
        }
    }
}
