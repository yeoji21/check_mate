package checkmate;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class MapperTest {
    protected void isEqualTo(Object A, Object B) {
        assertThat(A).isEqualTo(B);
    }
}
