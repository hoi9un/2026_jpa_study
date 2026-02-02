package com.example.anika.example;

import com.example.anika.example.entity.Item;
import com.example.anika.example.entity.Member;
import com.example.anika.example.entity.Order;
import com.example.anika.example.entity.OrderItem;
import com.example.anika.example.entity.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private final EntityManagerFactory emf;

    public Application(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        EntityManager em = emf.createEntityManager(); // 엔티티 매니저 생성
        EntityTransaction tx = em.getTransaction(); // 트랜잭션 기능 획득

        try {
            tx.begin(); // 트랜잭션 시작

            // 회원 생성
            Member member = new Member();
            member.setName("아니카");
            member.setCity("경기도");
            member.setStreet("안양시");
            member.setZipcode("12345");
            em.persist(member);

            // 상품 생성
            Item item1 = new Item();
            item1.setName("JPA 책");
            item1.setPrice(30000);
            item1.setStockQuantity(100);
            em.persist(item1);

            Item item2 = new Item();
            item2.setName("Spring 책");
            item2.setPrice(35000);
            item2.setStockQuantity(50);
            em.persist(item2);

            // 주문 생성 및 회원과 연관관계 설정
            Order order = new Order();
            order.setMember(member);
            order.setStatus(OrderStatus.ORDER);
            em.persist(order);

            // 주문상품 생성 및 연관관계 설정
            OrderItem orderItem1 = new OrderItem();
            orderItem1.setItem(item1);
            orderItem1.setOrderPrice(item1.getPrice());
            orderItem1.setCount(2);
            order.addOrderItem(orderItem1);  // 연관관계 편의 메서드 사용
            em.persist(orderItem1);

            OrderItem orderItem2 = new OrderItem();
            orderItem2.setItem(item2);
            orderItem2.setOrderPrice(item2.getPrice());
            orderItem2.setCount(1);
            order.addOrderItem(orderItem2);
            em.persist(orderItem2);

            em.flush();  // DB에 반영
            em.clear();  // 영속성 컨텍스트 초기화

            // 객체 그래프 탐색: Member -> Orders -> OrderItems -> Item
            System.out.println("=== 객체 그래프 탐색 ===");
            Member findMember = em.find(Member.class, member.getId());
            System.out.println("회원 이름: " + findMember.getName());

            for (Order findOrder : findMember.getOrders()) {
                System.out.println("\n주문 ID: " + findOrder.getId() + ", 상태: " + findOrder.getStatus());

                for (OrderItem oi : findOrder.getOrderItems()) {
                    System.out.println("  - 상품: " + oi.getItem().getName()
                            + ", 가격: " + oi.getOrderPrice()
                            + ", 수량: " + oi.getCount());
                }
            }

            // 역방향 탐색: OrderItem -> Order -> Member
            System.out.println("\n=== 역방향 탐색 ===");
            OrderItem findOrderItem = em.find(OrderItem.class, orderItem1.getId());
            System.out.println("주문상품: " + findOrderItem.getItem().getName());
            System.out.println("주문한 회원: " + findOrderItem.getOrder().getMember().getName());

            tx.commit(); // 트랜잭션 커밋

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback(); // 트랜잭션 롤백
        } finally {
            em.close(); // 엔티티 매니저 종료
        }
    }
}
