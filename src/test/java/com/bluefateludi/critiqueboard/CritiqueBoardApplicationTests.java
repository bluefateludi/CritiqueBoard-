package com.bluefateludi.critiqueboard;

import com.bluefateludi.critiqueboard.review.service.ReviewTaskPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:critiqueboard;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class CritiqueBoardApplicationTests {

    @MockitoBean
    private ReviewTaskPublisher reviewTaskPublisher;

    @Test
    void contextLoads() {
    }
}
