package edu.pe.cibertec.saibm.catalog.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BackfillMapperTest {
    private final BackfillMapper mapper = new BackfillMapper();

    @Test
    void preservesPositiveIdentityAndMapsNullableTextToEmptyValues() {
        BackfillCandidate candidate = mapper.map(new LegacyBookRow(42, null, null, 3, null, null));

        assertThat(candidate).isEqualTo(new BackfillCandidate(42, "", "", "", "", 3));
    }

    @Test
    void rejectsNullOrNegativeLegacyStockBeforeWriting() {
        assertThatThrownBy(() -> mapper.map(new LegacyBookRow(42, "Book", "", null, "Author", "")))
                .isInstanceOf(BackfillValidationException.class);
        assertThatThrownBy(() -> mapper.map(new LegacyBookRow(42, "Book", "", -1, "Author", "")))
                .isInstanceOf(BackfillValidationException.class);
    }
}
