package homework.chapter_5.problem1;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 문제 1-4: 1차 캐시 문제
 *
 * 목표: 양쪽 다 설정해야 하는 이유 (1차 캐시 동기화)
 */
class Problem1_4Test {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private EntityTransaction tx;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("jpabook");
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        tx = em.getTransaction();
        tx.begin();
    }

    @AfterEach
    void tearDown() {
        if (tx.isActive()) tx.rollback();
        if (em != null) em.close();
    }

    @Test
    @DisplayName("문제 1-4: flush 전후 team.getMembers().size() 차이")
    void 일차캐시_문제() {

        // 1. Team, Member 저장
        Team team = new Team("개발팀");
        em.persist(team);
        System.out.println("1. Team '개발팀' 저장 완료");

        Member member = new Member("홍길동");
        em.persist(member);
        System.out.println("2. Member '홍길동' 저장 완료");

        // 2. member.setTeam(team)만 호출
        member.setTeam(team);

        // 3. flush 전에 team.getMembers().size() 출력
        int sizeBeforeFlushOfTeamMemebers = team.getMembers().size();

        // 4. flush/clear 후 Team을 다시 조회
        em.flush();
        em.clear();
        System.out.println("4. flush/clear 완료");

        // 5. team.getMembers().size() 출력
        Team reloadedTeam = em.find(Team.class, team.getId());
        int sizeAfterFlushOfTeamMemebers = reloadedTeam.getMembers().size();

        assertEquals(0, sizeBeforeFlushOfTeamMemebers);
        assertEquals(1, sizeAfterFlushOfTeamMemebers);

        // 해결: sizeBeforeFlushOfTeamMemebers도 1로 만들려면 team.getMembers().add(member) 추가
        // → 양쪽 다 설정해야 1차 캐시에서도 정합성 유지
    }
}
