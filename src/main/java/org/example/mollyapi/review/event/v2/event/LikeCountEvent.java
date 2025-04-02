package org.example.mollyapi.review.event.v2.event;

public record LikeCountEvent(
        Long reviewId,
        Long userId,
        LikeAction action
) {
    public enum LikeAction {
        INCREASE, DECREASE
    }
}
