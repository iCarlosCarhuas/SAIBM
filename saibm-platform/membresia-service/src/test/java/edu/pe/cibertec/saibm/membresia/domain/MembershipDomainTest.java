package edu.pe.cibertec.saibm.membresia.domain;
import static org.assertj.core.api.Assertions.*; import java.time.*; import java.util.*; import org.junit.jupiter.api.Test; import edu.pe.cibertec.saibm.membresia.domain.model.*;
class MembershipDomainTest {
 @Test void planIsValidOnlyInsideVersionedWindow(){var from=Instant.parse("2030-01-01T00:00:00Z");var p=new Plan(UUID.randomUUID(),"standard","Standard",13,2,from,null,true,"7");assertThat(p.validAt(from.plusSeconds(1))).isTrue();assertThat(p.validAt(from.minusSeconds(1))).isFalse();assertThat(p.revise("standard",21,from,null).version()).isEqualTo(3);}
 @Test void assignmentSnapshotsPlanLimitAndExpiresWithoutMagicConsumerRules(){var a=MembershipAssignment.create(UUID.randomUUID(),"user-7",UUID.randomUUID(),"custom",5,3,Instant.now(),Instant.now().plusSeconds(60),"key","hash",null);assertThat(a.validAt(Instant.now())).isTrue();assertThat(a.expire().active()).isFalse();assertThat(a.reservationLimit()).isEqualTo(3);}
}
