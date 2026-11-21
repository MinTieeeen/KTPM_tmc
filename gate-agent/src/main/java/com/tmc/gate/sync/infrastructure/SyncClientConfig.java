package com.tmc.gate.sync.infrastructure;

import com.tmc.contract.SyncPaths;
import com.tmc.gate.sync.domain.NetworkFaultSwitch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

/** {@link RestClient} tới Central (connect 2 s, read 5 s, header {@code X-Gate-Key}) và công tắc giả lập offline. */
@Configuration(proxyBeanMethods = false)
class SyncClientConfig {

    static final String CENTRAL_REST_CLIENT = "centralRestClient";

    @Bean(CENTRAL_REST_CLIENT)
    RestClient centralRestClient(RestClient.Builder builder, CentralProperties props) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(props.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(props.readTimeout());
        return builder
                .baseUrl(props.url())
                .requestFactory(requestFactory)
                .defaultHeader(SyncPaths.GATE_KEY_HEADER, props.apiKey())
                .build();
    }

    @Bean
    NetworkFaultSwitch networkFaultSwitch() {
        return new NetworkFaultSwitch();
    }
}
