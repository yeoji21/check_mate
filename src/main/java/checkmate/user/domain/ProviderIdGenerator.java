package checkmate.user.domain;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProviderIdGenerator {

    public static String kakao(String providerId) {
        return "KAKAO" + "_" + providerId;
    }

    public static String google(String providerId) {
        return "GOOGLE" + "_" + providerId;
    }

    public static String naver(String providerId) {
        return "NAVER" + "_" + providerId;
    }
}
