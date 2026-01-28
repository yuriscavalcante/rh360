package com.rh360.rh360.realtime;

public final class NoOpRealTimePublisher implements RealTimePublisher {
    public static final NoOpRealTimePublisher INSTANCE = new NoOpRealTimePublisher();

    private NoOpRealTimePublisher() {}

    @Override
    public void publish(RealTimeEvent event) {
        // noop (útil para testes unitários que instanciam services manualmente)
    }
}

