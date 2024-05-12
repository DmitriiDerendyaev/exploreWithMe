package org.example.subscription.model;

import lombok.Data;
import org.example.user.model.User;

import javax.persistence.*;

@Data
@Entity
@Table(name = "subscriptions")
@IdClass(SubscriptionId.class)
public class Subscription {
    @Id
    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private User subscriber;

    @Id
    @ManyToOne
    @JoinColumn(name = "subscribed_to_id")
    private User subscribedTo;
}