package checkmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Profile("!test")
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter getCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); //내 서버가 응답을 할 때 json을 자바스트립트에서 처리할 수 있도록
        config.addAllowedOrigin("*");     //모든 ip에 응답 허용
        config.addAllowedHeader("*");     //모든 header에 응답 허용
        config.addAllowedMethod("*");     //모든 post,get,put,delete,patch 요청 허용
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
