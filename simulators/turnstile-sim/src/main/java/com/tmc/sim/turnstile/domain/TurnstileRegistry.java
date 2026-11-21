package com.tmc.sim.turnstile.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Danh sách cổng; cổng chưa biết được tự đăng ký khi gặp lần đầu (D3). */
public final class TurnstileRegistry {

    private final Map<String, Turnstile> turnstiles = new ConcurrentHashMap<>();

    public TurnstileRegistry(Collection<String> initialIds) {
        initialIds.forEach(this::getOrCreate);
    }

    public Turnstile getOrCreate(String id) {
        return turnstiles.computeIfAbsent(id, Turnstile::new);
    }

    public Optional<Turnstile> find(String id) {
        return Optional.ofNullable(turnstiles.get(id));
    }

    public List<Turnstile> all() {
        return turnstiles.values().stream().sorted(Comparator.comparing(Turnstile::id)).toList();
    }
}
