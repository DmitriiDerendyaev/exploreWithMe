package org.example.subscription.repository;

import org.example.subscription.model.Subscription;
import org.example.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsBySubscriberAndSubscribedTo(User user, User author);

    Subscription findBySubscriberIdAndSubscribedToId(Long userId, Long authorId);

    List<Subscription> findBySubscriberId(Long userId, Pageable pageable);
}
